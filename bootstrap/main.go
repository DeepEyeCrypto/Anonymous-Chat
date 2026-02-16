package main

import (
	"context"
	"fmt"
	"log"
	"os"
	"time"

	"github.com/gofiber/fiber/v2"
	"github.com/gofiber/fiber/v2/middleware/logger"
	"github.com/redis/go-redis/v9"
)

type Peer struct {
	PeerID  string `json:"peer_id"`
	Address string `json:"address"`
}

var ctx = context.Background()

func main() {
	app := fiber.New()
	app.Use(logger.New())

	redisAddr := os.Getenv("REDIS_ADDR")
	if redisAddr == "" {
		redisAddr = "localhost:6379"
	}

	rdb := redis.NewClient(&redis.Options{
		Addr:     redisAddr,
		Password: "", // no password set
		DB:       0,  // use default DB
	})

	// GET /bootstrap - Returns up to 20 random peers
	app.Get("/bootstrap", func(c *fiber.Ctx) error {
		keys, err := rdb.Keys(ctx, "peer:*").Result()
		if err != nil {
			// Failure to connect to Redis?
			return c.Status(500).SendString(err.Error())
		}

		var peers []Peer = []Peer{}
		count := 0
		for _, key := range keys {
			if count >= 20 {
				break
			}
			val, err := rdb.Get(ctx, key).Result()
			if err == nil {
				// key is "peer:ID"
				peers = append(peers, Peer{PeerID: key[5:], Address: val})
				count++
			}
		}

		return c.JSON(peers)
	})

	// POST /announce - Peers announce themselves
	app.Post("/announce", func(c *fiber.Ctx) error {
		p := new(Peer)
		if err := c.BodyParser(p); err != nil {
			return c.Status(400).SendString(err.Error())
		}

		if p.PeerID == "" || p.Address == "" {
			return c.Status(400).SendString("Missing peer_id or address")
		}

		// Store with TTL (e.g., 1 hour)
		key := fmt.Sprintf("peer:%s", p.PeerID)
		err := rdb.Set(ctx, key, p.Address, time.Hour).Err()
		if err != nil {
			return c.Status(500).SendString(err.Error())
		}

		return c.SendString("Announced")
	})

	log.Fatal(app.Listen(":3000"))
}

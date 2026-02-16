FROM golang:1.25.7-alpine AS builder
WORKDIR /app
COPY go.mod go.sum ./
run go mod download
COPY . .
RUN go build -o bootstrap main.go

FROM alpine:latest
WORKDIR /root/
COPY --from=builder /app/bootstrap .
CMD ["./bootstrap"]

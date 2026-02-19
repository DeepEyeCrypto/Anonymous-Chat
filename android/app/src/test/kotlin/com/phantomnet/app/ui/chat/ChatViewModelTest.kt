package com.phantomnet.app.ui.chat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `sendMessage ignores blank text`() = runTest(dispatcher) {
        val vm = ChatViewModel(
            cryptoEngine = FakeCryptoEngine(),
            echoDelayMs = 1
        )

        vm.sendMessage("   ", "recipient-key")
        advanceTimeBy(10)

        assertEquals(0, vm.messages.value.size)
    }

    @Test
    fun `sendMessage appends local and echoed message on success`() = runTest(dispatcher) {
        val vm = ChatViewModel(
            cryptoEngine = FakeCryptoEngine(),
            echoDelayMs = 100
        )

        vm.sendMessage("hello", "recipient-key")
        advanceTimeBy(1)

        assertEquals(1, vm.messages.value.size)
        assertEquals("hello", vm.messages.value[0].content)
        assertEquals(true, vm.messages.value[0].isMe)

        advanceTimeBy(200)
        assertEquals(2, vm.messages.value.size)
        assertEquals("Echo: hello", vm.messages.value[1].content)
        assertEquals(false, vm.messages.value[1].isMe)
    }

    @Test
    fun `encryption failure adds system message`() = runTest(dispatcher) {
        val vm = ChatViewModel(
            cryptoEngine = FakeCryptoEngine(failEncrypt = true),
            echoDelayMs = 1
        )

        vm.sendMessage("hello", "recipient-key")
        advanceTimeBy(10)

        assertEquals(1, vm.messages.value.size)
        assertEquals("system", vm.messages.value[0].senderId)
        assertEquals("Message not sent: encryption unavailable", vm.messages.value[0].content)
    }

    @Test
    fun `decryption failure adds system message after local message`() = runTest(dispatcher) {
        val vm = ChatViewModel(
            cryptoEngine = FakeCryptoEngine(failDecrypt = true),
            echoDelayMs = 100
        )

        vm.sendMessage("hello", "recipient-key")
        advanceTimeBy(1)
        assertEquals(1, vm.messages.value.size)
        assertEquals("hello", vm.messages.value[0].content)

        advanceTimeBy(200)
        assertEquals(2, vm.messages.value.size)
        assertEquals("system", vm.messages.value[1].senderId)
        assertEquals("Incoming message unavailable: decryption failed", vm.messages.value[1].content)
    }

    private data class FakeCryptoEngine(
        private val failEncrypt: Boolean = false,
        private val failDecrypt: Boolean = false
    ) : CryptoEngine {
        override fun encryptMessage(message: String, recipientPublicKey: String): String {
            if (failEncrypt) error("encrypt fail")
            return "enc:$message"
        }

        override fun decryptMessage(encryptedMessage: String): String {
            if (failDecrypt) error("decrypt fail")
            return encryptedMessage.removePrefix("enc:")
        }
    }
}

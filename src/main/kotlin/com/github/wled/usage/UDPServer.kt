package com.github.wled.usage

import java.io.IOException
import java.math.BigInteger
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel


class UDPServer {
    @Throws(IOException::class)
    fun startServer(): DatagramChannel {
        val address = InetSocketAddress("0.0.0.0", 7001)
        val server: DatagramChannel = DatagramChannelBuilder.bindChannel(address)
        println("Server started at #$address")
        return server
    }

    companion object {
        @Throws(IOException::class)
        fun receiveMessage(server: DatagramChannel) {
            val buffer: ByteBuffer = ByteBuffer.allocate(2024)
            val remoteAdd = server.receive(buffer)
            val message: String = extractMessage(buffer).toString()
            println("Client at $remoteAdd\nsent: $message")
        }

        @OptIn(ExperimentalStdlibApi::class)
        private fun extractMessage(buffer: ByteBuffer): Any {
            buffer.flip()
            val bytes = ByteArray(buffer.remaining())
            buffer[bytes]
            println(bytes.toHexString())
            if(bytes[0] == 0x01.toByte()) {
                println("WLED v1 " + String(bytes))
                val usage = WLEDUsageV1(bytes)
                return usage
            }
            val msg = String(bytes)

            return msg
        }
    }

}

class DatagramChannelBuilder {
    companion object {
        @Throws(IOException::class)
        fun openChannel(): DatagramChannel {
            val datagramChannel = DatagramChannel.open()
            return datagramChannel
        }

        @Throws(IOException::class)
        fun bindChannel(local: SocketAddress?): DatagramChannel {
            return openChannel().bind(local)
        }
    }
}

data class WLEDUsageV1(
    val header: Byte,
    val length: Int,
    val deviceId: String,
    val version: String,
    val chip: String,
    val uptime: Int? = null,
    val totalLEDs: Int? = null,
    val isMatrix: Boolean? = null,
) {
    constructor(bytes: ByteArray) : this(
        header = bytes[0],
        length = bytes[1].toInt(),
        deviceId = getString(1, 41, bytes),
        version = getString(42, 20, bytes),
        chip = getString(62, 15, bytes),
        uptime = getInt(77, bytes),
        totalLEDs = getInt(79, bytes),
        isMatrix = BigInteger(bytes.copyOfRange(81, 82)).toInt() == 1,
    )

    companion object {
        private fun getString(start: Int, length: Int, bytes: ByteArray) =
            bytes.copyOfRange(start, start+length).filter { it.toInt() != 0 }.toByteArray().decodeToString()

        private fun getInt(start: Int, bytes: ByteArray): Int {
            val foo = bytes.copyOfRange(start, start+2)
            val bar : ByteArray = ByteArray(2)
            bar[0] = foo[1];
            bar[1] = foo[0];
            return BigInteger(bar).toInt()
        }
    }
}
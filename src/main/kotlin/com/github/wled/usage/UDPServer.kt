package com.github.wled.usage

import java.io.IOException
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
            val buffer: ByteBuffer = ByteBuffer.allocate(1024)
            val remoteAdd = server.receive(buffer)
            val message: String = extractMessage(buffer)
            println("Client at $remoteAdd  sent: $message")
        }

        private fun extractMessage(buffer: ByteBuffer): String {
            buffer.flip()
            val bytes = ByteArray(buffer.remaining())
            buffer[bytes]
            if(bytes[0] == 0x01.toByte()) {
                println("WLED v1 " + String(bytes))
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

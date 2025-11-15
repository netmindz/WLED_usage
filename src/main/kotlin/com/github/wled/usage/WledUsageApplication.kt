package com.github.wled.usage

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WledUsageApplication

fun main(args: Array<String>) {
//	val server = UDPServer()
//	UDPServer.receiveMessage(server.startServer())
	runApplication<WledUsageApplication>(*args)
}


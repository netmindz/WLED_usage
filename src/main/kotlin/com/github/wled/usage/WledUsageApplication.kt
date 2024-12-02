package com.github.wled.usage

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WledUsageApplication

fun main(args: Array<String>) {
	val server = UDPServer()
	UDPServer.receiveMessage(server.startServer())
	println("UDP server started")
//	while(true) {
//		Thread.sleep(1000)
//	}
//	runApplication<WledUsageApplication>(*args)
}


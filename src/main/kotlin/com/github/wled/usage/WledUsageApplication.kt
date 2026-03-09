package com.github.wled.usage

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class WledUsageApplication

fun main(args: Array<String>) {
//	val server = UDPServer()
//	UDPServer.receiveMessage(server.startServer())
	runApplication<WledUsageApplication>(*args)
}


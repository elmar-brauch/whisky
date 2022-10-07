package de.bsi.whisky

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WhiskyApplication

fun main(args: Array<String>) {
	val context = runApplication<WhiskyApplication>(*args)
	val crawler = context.getBean(WhiskyCrawler::class.java)
	crawler.collectDataFor("Johnnie Walker Gold Label Reserve Master")
	crawler.collectDataFor("Alrik 1912")
}

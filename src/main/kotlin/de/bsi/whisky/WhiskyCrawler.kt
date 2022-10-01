package de.bsi.whisky

import org.apache.logging.log4j.LogManager
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.springframework.stereotype.Service
import javax.annotation.PreDestroy

@Service
class WhiskyCrawler {
    private val log = LogManager.getLogger()
    private val browser: ChromeDriver

    init {
        System.setProperty("webdriver.chrome.driver",
            "./src/main/resources/drivers/windows/chromedriver.exe")
        browser = ChromeDriver()
    }

    @PreDestroy
    fun closeBrowser() {
        browser.close()
    }

    fun collectDataFor(whiskyName: String) {
        val whiskies = searchWhiskies(whiskyName)
        var total = Pair(0,0)
        whiskies.entries.sortedBy { it.key }.stream()
            .map(::readWhiskyData)
            .peek { total = Pair(total.first + it.wishCount, total.second + it.collectionCount) }
            .forEach { log.debug(it.toString()) }

        log.info("$whiskyName has score ${total.first * 100 / total.second}% : " +
                "${total.first} in wishlists and ${total.second} in collections")
    }

    private fun searchWhiskies(whiskyName: String): Map<String, List<String>> {
        browser.get("https://whiskybase.com/search?q=%s".format(whiskyName))
        val map = mutableMapOf<String, MutableList<String>>()
        for(whisky in browser.findElements(By.xpath("//div[@class='row']//a[@class='clickable']")))
            map.putIfAbsent(whisky.text, mutableListOf(whisky.getAttribute("href")))
                ?.add(whisky.getAttribute("href"))

        try {
            browser.findElement(By.xpath("//button[@name='allow']"))?.click()
        } catch (e: Exception) {
            // Clicking allow button was not required.
        }
        return map
    }

    private fun readWhiskyData(e: Map.Entry<String, List<String>>): WhiskyData {
        var sum = Pair(0, 0)
        e.value.stream()
            .map { readWishCollectionPair(it) }
            .forEach { sum = Pair(sum.first + it.first, sum.second + it.second) }
        return WhiskyData(e.key, sum.first, sum.second)
    }

    private fun readWishCollectionPair(url: String): Pair<Int, Int> {
        browser[url]
        try {
            val wish = browser.findElement(By.xpath("(//div[@id='whisky-community']//button)[1]"))
            val coll = browser.findElement(By.xpath("(//div[@id='whisky-community']//button)[3]"))
            val wishCount = wish?.text?.replace(Regex("\\D"), "")?.toInt()
            val collCount = coll?.text?.replace(Regex("\\D"), "")?.toInt()
            return Pair(wishCount ?: 0, collCount ?: 0)
        } catch (e: Exception) {
            log.debug("Ignoring corrupted data in " + browser.currentUrl)
        }
        return Pair(0, 0)
    }

}
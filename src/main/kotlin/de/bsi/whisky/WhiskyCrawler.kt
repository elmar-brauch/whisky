package de.bsi.whisky

import org.apache.logging.log4j.LogManager
import org.openqa.selenium.By
import org.openqa.selenium.Keys
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
        browser.get("https://whiskybase.com")
        val searchField = browser.findElement(By.id("search-input"))
        searchField.sendKeys(whiskyName)
        searchField.sendKeys(Keys.ENTER)

        val map = mutableMapOf<String, MutableList<String>>()
        for(whisky in browser.findElements(By.xpath("//div[@class='row']//a[@class='clickable']")))
            map.putIfAbsent(whisky.text, mutableListOf(whisky.getAttribute("href")))
                ?.add(whisky.getAttribute("href"))

        try {
            browser.findElement(By.xpath("//button[@name='allow']")).click()
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

    private val digitRegex = "\\d+".toRegex()
    private val noDigitRegex = "\\D".toRegex()

    private fun readWishCollectionPair(url: String) = try {
        browser[url]
        val wish = browser.findElement(By.xpath("(//div[@id='whisky-community']//button)[1]")).text
        val coll = browser.findElement(By.xpath("(//div[@id='whisky-community']//button)[3]")).text
        val wishCount = if (wish.contains(digitRegex)) wish.replace(noDigitRegex, "").toInt() else 1
        val collCount = if (coll.contains(digitRegex)) coll.replace(noDigitRegex, "").toInt() else 1
        Pair(wishCount, collCount)
    } catch (e: Exception) {
        log.debug("Ignoring corrupted data in " + browser.currentUrl)
        Pair(0, 0)
    }

}
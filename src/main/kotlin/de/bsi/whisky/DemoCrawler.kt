package de.bsi.whisky

import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.chrome.ChromeDriver
import org.springframework.stereotype.Service

//@Service
class DemoCrawler {

    private val browser : ChromeDriver

    init {
        System.setProperty("webdriver.chrome.driver", "./src/main/resources/drivers/windows/chromedriver.exe")
        browser = ChromeDriver()
    }

    fun showWhisky() {
        browser.get("https://whiskybase.com")
        val searchInput = browser.findElement(By.id("search-input"))
        searchInput.sendKeys("Alrik 1912")
        searchInput.sendKeys(Keys.ENTER)

        val whiskies = browser.findElements(By.xpath("//a[@class='clickable']"))
        val whiskyUrl = whiskies[0].getAttribute("href")
        browser.get(whiskyUrl)

        browser.findElement(By.xpath("//button[@name='allow']")).click()

        browser.close()
    }

}
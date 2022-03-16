package org.anax.framework.config;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.configuration.AnaxDriver;
import org.anax.framework.controllers.WebController;
import org.anax.framework.controllers.WebDriverWebController;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.net.URL;


@Configuration
@Slf4j
public class AnaxChromeDriver {

    @Value("${anax.target.url:http://www.google.com}")
    String targetUrl;
    @Value("${anax.remote.host:NOT_CONFIGURED}")
    String remoteHost;
    @Value("${anax.remote.port:NOT_CONFIGURED}")
    String remotePort;
    @Value("${anax.maximize:false}")
    String maximize;
    @Value("${selenium.page.strategy:#{null}}")
    String pageStrategy;


    @ConditionalOnMissingBean
    @Bean
    public AnaxDriver getWebDriver(@Value("${anax.localdriver:true}") Boolean useLocal) {
        ChromeOptions options = new ChromeOptions();
        pageStrategy = (pageStrategy == null) ? PageLoadStrategy.NORMAL.toString() : pageStrategy;
        options.setCapability(CapabilityType.PAGE_LOAD_STRATEGY,pageStrategy);
        options.setCapability("enableVNC", true);

        if (useLocal) {

            ChromeDriverService service = new ChromeDriverService.Builder().build();
            if(maximize.equals("true")) {
                String x = (System.getProperty("os.name").toLowerCase().contains("mac")) ? "--start-fullscreen" : "--start-maximized";
                options.addArguments(x);
            }

            return () -> {
                ChromeDriver driver = new ChromeDriver(service, options);
                driver.get(targetUrl);
                return driver;
            };
        } else {
            log.info("Remote url is: "+"http://" + remoteHost + ":" + remotePort + "/wd/hub");
            // adds screenshot capability to a default webdriver.
            return () -> {
                Augmenter augmenter = new Augmenter();
                WebDriver driver = augmenter.augment(new RemoteWebDriver(
                    new URL("http://" + remoteHost + ":" + remotePort + "/wd/hub"), options));
                driver.get(targetUrl);
                return driver;
            };
        }
    }

    @ConditionalOnMissingBean
    @Bean
    public WebController getWebController(@Autowired AnaxDriver anaxDriver, @Value("${anax.defaultWaitSeconds:5}") Integer defaultWaitSeconds) throws Exception {
        return new WebDriverWebController(anaxDriver.getWebDriver(), anaxDriver, defaultWaitSeconds);
    }

}

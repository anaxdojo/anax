package org.anax.framework.config;

import org.anax.framework.configuration.AnaxDriver;
import org.anax.framework.controllers.WebController;
import org.anax.framework.controllers.WebDriverWebController;
import org.openqa.selenium.winium.DesktopOptions;
import org.openqa.selenium.winium.WiniumDriver;
import org.openqa.selenium.winium.WiniumDriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnaxWiniumDriver {

    @Value("${anax.target.url:C:\\Windows\\System32\notepad.exe}")
    String targetUrl;
    @Value("${anax.remote.host:NOT_CONFIGURED}")
    String remoteHost;
    @Value("${anax.remote.port:NOT_CONFIGURED}")
    String remotePort;


    @ConditionalOnMissingBean
    @Bean
    public AnaxDriver defaultAnaxDriver(@Value("${anax.localdriver:true}") Boolean useLocal) {
        DesktopOptions options = new DesktopOptions();
        options.setApplicationPath(targetUrl);
            WiniumDriverService service = new WiniumDriverService.Builder()
                    .usingAnyFreePort()
                    .withVerbose(true)
                    .withSilent(false)
                    .buildDesktopService();

            return () -> {
                WiniumDriver driver = new WiniumDriver(service, options);
                driver.get(targetUrl);
                return driver;
            };
    }


}

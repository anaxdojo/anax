package org.anax.framework.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AnaxBaseRunner implements CommandLineRunner{

    public void run(String... strings) throws Exception {
        log.info("------------------------------------------------");
        log.info("Anax - a modern day testing automation framework");
        log.info("------------------------------------------------");
    }
}

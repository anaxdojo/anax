package org.anax.framework.configuration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AnaxBaseRunner implements CommandLineRunner{

    public void run(String... strings) throws Exception {
        //TODO figure out what to do with this
        System.out.println("Hello world");
    }
}

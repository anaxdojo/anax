package org.anax.framework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "org.anax.framework")
public class AnaxFirefoxApplication {

    public static void main(String[] args){
        SpringApplication.run(AnaxFirefoxApplication.class,args);
    }
}

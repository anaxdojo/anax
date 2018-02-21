package org.anax.framework.examples.demotestapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "org.anax.framework")
public class AnaxChromeDemoAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnaxChromeDemoAppApplication.class, args);
	}
}

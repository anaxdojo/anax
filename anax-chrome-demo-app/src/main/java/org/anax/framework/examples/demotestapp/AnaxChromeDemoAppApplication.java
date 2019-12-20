package org.anax.framework.examples.demotestapp;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = "org.anax.framework")
public class AnaxChromeDemoAppApplication {

	public static void main(String[] args) {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(AnaxChromeDemoAppApplication.class);
		builder.run(args);
	}
}

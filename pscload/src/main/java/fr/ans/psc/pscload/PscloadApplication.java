/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The Class PscloadApplication.
 */
@SpringBootApplication
@EnableScheduling
public class PscloadApplication {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		new SpringApplication(PscloadApplication.class).run(args);
	}

}

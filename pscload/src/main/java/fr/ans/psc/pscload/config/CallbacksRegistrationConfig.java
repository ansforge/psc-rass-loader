/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.config;

import javax.servlet.ServletContextListener;

import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.ans.psc.pscload.PscloadServletContextListener;

/**
 * The Class CallbacksRegistrationConfig.
 */
@Configuration
public class CallbacksRegistrationConfig {
	
	/**
	 * Servlet listener.
	 *
	 * @return the servlet listener registration bean
	 */
	@Bean
	ServletListenerRegistrationBean<ServletContextListener> servletListener() {
	    ServletListenerRegistrationBean<ServletContextListener> srb
	      = new ServletListenerRegistrationBean<>();
	    srb.setListener(new PscloadServletContextListener());
	    return srb;
	}
}


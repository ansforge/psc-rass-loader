/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * The Class CheckController.
 */

@RestController
public class CheckController {

	public CheckController() {
		super();

	}

	/**
	 * Check endpoint.
	 *
	 * @return the application status
	 */
	@GetMapping(value = "/check")
	public ResponseEntity<String> check() {
		return new ResponseEntity<String>("PscLoad is running !", HttpStatus.OK);
	}
}

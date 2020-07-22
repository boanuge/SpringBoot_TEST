package com.example.servicebroker;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ObjectNode;

@SpringBootApplication
@CrossOrigin(origins = "*")
@RestController
public class App 
{
	public static void main( String[] args )
	{
		SpringApplication.run(App.class, args);
	}

	//@RequestMapping(value = "/**")
	@RequestMapping(value = "/services/v1/service/result", method = RequestMethod.POST)
	public ResponseEntity<String> handleRequest(HttpServletRequest request, @RequestBody ObjectNode jsonReceived) {

		System.out.println("Received-message:");
		System.out.println(request.getMethod() + "\t" + request.getServletPath().toString());
		System.out.println(jsonReceived.toString());

		/*
		String targetUrl = null;
		String httpMethod = null;
		String jsonString = null;
		HttpRequest httpRequest = new HttpRequest(targetUrl, httpMethod, jsonString);
		Thread thread = new Thread(httpRequest);
		thread.start();
		 */

		return new ResponseEntity<String>(jsonReceived.toString(), HttpStatus.OK);
	}
}

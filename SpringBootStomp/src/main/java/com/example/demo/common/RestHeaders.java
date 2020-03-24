package com.example.demo.common;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class RestHeaders {

	public HttpHeaders restHttpHeaders() {
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON_UTF8);	// json 일 경우
//	    headers.add("Access-Control-Allow-Origin", "*");
	    return headers;
	}
	
}

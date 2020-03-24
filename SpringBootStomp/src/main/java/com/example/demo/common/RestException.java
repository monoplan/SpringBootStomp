package com.example.demo.common;

public class RestException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public RestException() {
		
	}
	
	public RestException(String msg) {
		super(msg);	
	}
	
	public RestException(Exception e) {		
		super(e);
	}
}

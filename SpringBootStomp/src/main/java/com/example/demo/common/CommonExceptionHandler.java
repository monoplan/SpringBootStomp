package com.example.demo.common;


import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.common.RestException;
import com.example.demo.model.CommMap;


@ControllerAdvice
public class CommonExceptionHandler {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	RestHeaders restHeaders ; 
	
	@Autowired 
	RestResponse restResponse ;
	
	@ExceptionHandler(RestException.class)
	@ResponseBody
	public ResponseEntity<?> restException(HttpServletRequest request, RestException e){
		
		log.debug("■ 에러 발생 : "+ e.getMessage());
		HttpHeaders headers = restHeaders.restHttpHeaders(); 
		return new ResponseEntity<CommMap>(restResponse.setFail(e.getMessage()), headers, HttpStatus.OK);
		
	}	

	
}

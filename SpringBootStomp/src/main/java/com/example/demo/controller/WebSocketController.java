package com.example.demo.controller;

import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.common.RestException;
import com.example.demo.common.RestResponse;
import com.example.demo.model.CommMap;
import com.example.demo.service.ChatService;

@Controller
public class WebSocketController {

	Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired 
	private RestResponse restResponse ;
	
	@Autowired
	private ChatService service ; 

	
	// 유저 목록 , 방 목록 가져오기 
	@RequestMapping("/chat/getList")
	public ResponseEntity<?> getList(@RequestBody CommMap map) throws RestException { 
 
		CommMap data = new CommMap();
		
		try {
			data = service.getList(map) ;
		} catch (Exception e) {
			throw new RestException(e.getMessage());
		} 
		
		return new ResponseEntity<CommMap>(restResponse.setSuccess(data), HttpStatus.OK);
	}
	
	
	
	// 메세지 전송 
	@RequestMapping("/chat/msgSend")
	public ResponseEntity<?> msgSend(@RequestBody JSONObject json) throws RestException { 

		try { 
			service.msgSend(json) ; 
		} catch (Exception e) { 
			throw new RestException(e.getMessage());			
		}
			
		return new ResponseEntity<CommMap>(restResponse.setSuccess(), HttpStatus.OK);
	}

	
	// 방 입장 
	@RequestMapping("/chat/roomIn")
	public ResponseEntity<?> roomIn(@RequestBody JSONObject json, HttpSession session) throws RestException { 

		try { 
			service.roomIn(json, session) ; 
		} catch (Exception e) { 
			throw new RestException(e.getMessage());			
		}

		return new ResponseEntity<CommMap>(restResponse.setSuccess(), HttpStatus.OK);
	}
	

	// 방 나감 
	@RequestMapping("/chat/roomOut")
	public ResponseEntity<?> roomOut(@RequestBody JSONObject json, HttpSession session) throws RestException { 
		
		try { 
			service.roomOut(json, session) ; 
		} catch (Exception e) { 
			throw new RestException(e.getMessage());			
		}
		
		return new ResponseEntity<CommMap>(restResponse.setSuccess(), HttpStatus.OK);
	}



	// 방 만들기 
	@RequestMapping("/makeRoom")

	public ResponseEntity<?> makeRoom(@RequestBody JSONObject json) throws RestException { 

		try { 
			service.makeRoom(json) ; 
		} catch (Exception e) { 
			throw new RestException(e.getMessage());			
		}
		
		return new ResponseEntity<CommMap>(restResponse.setSuccess(), HttpStatus.OK);		
	}
	
	
	// 방 강제 종료 
	@RequestMapping("/stopRoom")
	public ResponseEntity<?> stopRoom(@RequestBody JSONObject json) throws RestException {

		try { 
			service.stopRoom(json) ; 
		} catch (Exception e) { 
			throw new RestException(e.getMessage());			
		}
		
		return new ResponseEntity<CommMap>(restResponse.setSuccess(), HttpStatus.OK);
	}
	
	
		
}

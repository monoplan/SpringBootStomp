package com.example.demo.common;


import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.example.demo.controller.WebSocketController;
import com.example.demo.model.CommMap;
import com.example.demo.service.ChatService;


@Component
public class StompDisconnectEvent implements ApplicationListener<SessionDisconnectEvent> {

	Logger log = LoggerFactory.getLogger(this.getClass());

	private SimpMessagingTemplate template;
	
	@Autowired
	public StompDisconnectEvent(SimpMessagingTemplate template) {
		this.template = template;
	}
	
	@Autowired 
	WebSocketController controller ; 
    
	@Autowired
	private ChatService service ; 
	
	// 세션 아이디로 접속자 목록 삭제 
	@SuppressWarnings("unchecked")
    public void onApplicationEvent(SessionDisconnectEvent event) {

		log.debug("접속 해제 .... ");

    	ConcurrentHashMap<String, String> obj = (ConcurrentHashMap<String, String>) event.getMessage().getHeaders().get("simpSessionAttributes") ;

    	if (!obj.containsKey("sessionId")) return  ; 
    	
    	String sessionId = obj.get("sessionId") ; 

    	log.debug("sessionId:"+ sessionId);
    	
    	// 방 정보, 유저 아이디  가져오기 
    	CommMap param = new CommMap() ; 
    	param.put("sessionId", sessionId) ;
    	param.put("state", "1") ;
    	
    	CommMap userInfo = null ; 
    	
    	try { 
    		userInfo = service.selectUserInfo(param) ; 
    	} catch (Exception e) { 
    		
    	}

    	if (userInfo==null) { 
    		log.debug("방 접속 정보 없음");
    		return ; 
    	}
    	
    	if (userInfo==null) return  ; 
  
    	String roomId = (String) userInfo.get("ROOMID") ;
    	String userId = (String) userInfo.get("USERID") ; 

    	log.debug("userId:"+ userId);
    	
    	
    	// 방에 있는 사람들에게 메세지 전송 
    	param = new CommMap(); 
		param.put("state", "0" );
		param.put("userId", userId );   
    	this.template.convertAndSend("/subscribe/room/"+ roomId, param );
    			
		// 접속 정보 업데이트
		param.put("sessionId", "" ); 
		param.put("roomId", roomId );
		try { 
			service.stateUpdate(param) ; 
		} catch (Exception e) { 
			
		}

		// 업데이트 된 방 정보 
		List<CommMap> roomList = null ;
		param = new CommMap();
		param.put("roomId", roomId); 
		try {
			roomList = service.roomList(param) ;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	if (roomList==null || roomList.size()==0) return  ; 
		
		CommMap newRoom =  (CommMap) roomList.get(0) ; 
		
		// 방 유저 목록 
		List<CommMap> roomUserList = null ; 
		try {
			roomUserList = service.roomUserList(param) ;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		// 방 유저들에게 개인 메세지 전송 
		newRoom.put("type","roomUpdate") ; 
		for (CommMap user: roomUserList ) { 
			this.template.convertAndSend("/queue/"+ user.getString("USERID") , newRoom );			
		}
		

    }
	
}

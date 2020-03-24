package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.common.Utils;
import com.example.demo.dao.CommonDao;
import com.example.demo.model.CommMap;

@Service
public class ChatService {
	
	Logger log = LoggerFactory.getLogger(this.getClass());
	
	private SimpMessagingTemplate template;
	
	@Autowired
	public ChatService(SimpMessagingTemplate template) {
		this.template = template;
	}
	
	@Autowired
	private Utils utils ; 
	
	@Autowired
	private CommonDao dao ; 
	
	// 유저 목록 , 방 목록 가져오기 
	public CommMap getList(CommMap map) throws Exception { 
		
//		List<CommMap> userList = userList(map) ;

		List<CommMap> roomList = roomList(map) ;
		
		CommMap data = new CommMap();
//		data.put("userList", userList ) ;
		data.put("roomList", roomList ) ;
		
		return data ; 
	}
	
	
	// 메세지 전송  
	public void msgSend(JSONObject json) { 
		
		CommMap snd = new CommMap();  
		snd.put("state", "chat" );
		snd.put("userId", json.get("userId").toString() );   
		snd.put("msg", json.get("msg").toString() );
		
		this.template.convertAndSend("/subscribe/room/"+json.get("roomId"), snd );

	}
	
	// 방 입장 
	public void roomIn(JSONObject json, HttpSession session) throws Exception { 

		String userId = (String) json.get("userId") ; 
		String roomId = (String) json.get("roomId") ; 
		
		CommMap param = new CommMap();  
		param.put("state", "1" );
		param.put("userId", userId );   
		
		// 방에 있는 사람들에게 메세지 전송 
		this.template.convertAndSend("/subscribe/room/"+ roomId, param );
		
		// 접속 정보 업데이트 
		param.put("roomId", roomId );   
		param.put("sessionId", session.getId() );

		stateUpdate(param) ;
			
		// 업데이트 된 방 정보 
		List<CommMap> roomList = null ; 
		param = new CommMap();
		param.put("roomId", roomId);

		roomList = roomList(param) ;

		CommMap newRoom =  (CommMap) roomList.get(0) ; 
		
		// 방 유저 목록 
		List<CommMap> roomUserList = roomUserList(param) ;

		// 방 유저들에게 개인 메세지 전송 
		newRoom.put("type","roomUpdate") ; 
		for (CommMap user: roomUserList ) { 
			this.template.convertAndSend("/queue/"+ user.getString("USERID") , newRoom );			
		}
		
	}
	
	
	// 방 나감 
	public void roomOut(JSONObject json, HttpSession session) throws Exception { 

		String roomId = (String) json.get("roomId") ; 
		String userId = (String) json.get("userId") ; 
		
		CommMap param = new CommMap();  
		param.put("state", "0" );
		param.put("userId", userId );   
		
		// 방에 있는 사람들에게 메세지 전송 
		this.template.convertAndSend("/subscribe/room/"+ roomId, param );
		
		// 접속 정보 업데이트 
		param.put("sessionId", "" );
		param.put("roomId", roomId );
		
		stateUpdate(param) ;
		
		// 업데이트 된 방 정보 
		List<CommMap> roomList = null ;
		param = new CommMap();
		param.put("roomId", roomId); 
		roomList = roomList(param) ;
		
		CommMap newRoom =  (CommMap) roomList.get(0) ; 
		
		// 방 유저 목록 
		List<CommMap> roomUserList = roomUserList(param) ;
		
		// 방 유저들에게 개인 메세지 전송 
		newRoom.put("type","roomUpdate") ; 
		for (CommMap user: roomUserList ) { 
			this.template.convertAndSend("/queue/"+ user.getString("USERID") , newRoom );			
		}
		
	}
	
	// 방 만들기 
	@SuppressWarnings("unchecked")
	public void makeRoom(JSONObject json) throws Exception { 

		String roomId = utils.randomStr(10) ;
		
		ArrayList<String> arr = (ArrayList<String>) json.get("userid") ; 

		ArrayList<CommMap> users = new ArrayList<CommMap>();
		
		for (int i=0 ; i < arr.size() ; i++ ) {
			CommMap user = new CommMap() ; 
			user.put("roomId",roomId) ;
			user.put("userId", arr.get(i) );
			user.put("state", "0" );
		
			if (json.get("roomOwner").equals(arr.get(i))) { 
				user.put("roomOwner", "1");
			} else {
				user.put("roomOwner", "");
			}
			
			// 입력 
			roomUserInsert(user);
				
			users.add(user);
		}

		// 업데이트 된 방 정보 
		List<CommMap> roomList = null ; 
		CommMap param = new CommMap() ; 
		param.put("roomId", roomId ) ; 
		roomList = roomList(param) ;
		
		CommMap newRoom =  (CommMap) roomList.get(0) ; 

		// 유저들에게 방 정보 전송 
		newRoom.put("type", "roomUpdate") ; 
		
		for (CommMap user : users ) {
			this.template.convertAndSend("/queue/"+ user.getString("userId") , newRoom );
		}
		
	}
	
	  
	// 방 강제 종료 
	public void stopRoom(JSONObject json) throws Exception { 
		
//		String userId = (String) json.get("userId") ; 
		String roomId = (String) json.get("roomId") ; 
		
		// 방에 메세지 전송 
		CommMap param = new CommMap();  
		param.put("state", "stop" );   
		
		// 방에 있는 사람들에게 메세지 전송 
		this.template.convertAndSend("/subscribe/room/"+ roomId, param );
		
		// 방 유저 목록 
		List<CommMap> roomUserList = null ; 
		param = new CommMap();
		param.put("roomId", roomId);
		roomUserList = roomUserList(param) ;
		
		// 방 유저들에게 개인 메세지 전송 
		CommMap room = new CommMap() ; 
		room.put("type","roomStop") ;  
		room.put("roomId",roomId) ; 
		
		for (CommMap user: roomUserList ) { 
			this.template.convertAndSend("/queue/"+ user.getString("USERID") , room );
			
			// db 에서 삭제 
			CommMap member = new CommMap();
			member.put("userId", user.get("USERID") ) ; 
			member.put("roomId", roomId ) ;
			
			roomUserDelete(member);
			
		}
		
	}
	
	
	// 유저 목록 
	// 채팅 가능한 유저들의 목록 
	public List<CommMap> userList(CommMap map) throws Exception {
		
		List<CommMap> userList = null ;
		
		try { 
			userList = dao.getListData("chatMapper.userList", map) ; 
		} catch (Exception e) {
			log.debug("에러 발생 !!! " ); 			
		}

		return userList ; 
		
	} 

	// 방 유저 목록 
	// 방에 속한 유저들의 목록 리스트 
	// 필요 파라미터 : roomId 
	public List<CommMap> roomUserList(CommMap map) throws Exception {
		
		List<CommMap> roomUserList = null ;
		
		try { 
			roomUserList = dao.getListData("chatMapper.roomUserList", map) ; 
		} catch (Exception e) {
			log.debug("에러 발생 !!! " ); 			
		}

		return roomUserList ; 
		
	} 
	
	// 방 목록  
	// 방에 속한 유저들의 목록을 가져온 후 방 목록으로 변환으로 출력 
	// 필요 파라미터 : userId 
	public List<CommMap> roomList(CommMap param) throws Exception {
		
		List<CommMap> userList = null ;
		
		try { 
			userList = roomUserList(param) ; 
		} catch (Exception e) {
			log.debug("에러 발생 !!! " ); 			
		}

		List<CommMap> roomList = new ArrayList<CommMap>(); 
		CommMap room = new CommMap();
		ArrayList<CommMap> users = new ArrayList<CommMap>();
		CommMap user = new CommMap();
		String preRoomId = ""; 
		
		for (CommMap map : userList) {
			
			if ( map.get("ROOMID").equals(preRoomId)) {
	
				user = new CommMap();
				user.put("userId", map.getString("USERID")) ;
				user.put("state", map.getString("STATE")) ;
				user.put("roomOwner", map.getString("ROOMOWNER")) ; 
				users.add(user) ;

			} else {

				if (!preRoomId.equals("")) { 
					room = new CommMap();
					room.put("roomId", preRoomId ) ;
					room.put("users", users ) ;	
					roomList.add(room) ; 
				}
					
				users = new ArrayList<CommMap>();
				user = new CommMap();
				user.put("userId", map.getString("USERID")) ;
				user.put("state", map.getString("STATE")) ;
				user.put("roomOwner", map.getString("ROOMOWNER")) ; 
				users.add(user) ; 

			}
			
			preRoomId = map.getString("ROOMID") ; 

		}
		
		if (!preRoomId.equals("")) { 
			room = new CommMap();
			room.put("roomId", preRoomId ) ;
			room.put("users", users ) ;		
			roomList.add(room) ; 
		}

		return roomList ; 
		
	}
	
	// 유저 상태 변경 
	public void stateUpdate(CommMap param) { 

		try { 
			dao.updateData("chatMapper.stateUpdate", param) ; 
		} catch (Exception e) {
			log.debug("에러 발생 !!! " ); 			
		}
		
	}
	
	// 방 유저 목록 입력 
	public void roomUserInsert(CommMap map) {
		
		try {
			dao.insertData("chatMapper.roomUserInsert", map ) ; 
		} catch (Exception e) { 
			log.debug("에러 발생 !!! " );			
		}
		
	}
	
	// 방 유저 목록 삭제  
	public void roomUserDelete(CommMap map) {
		
		try {
			dao.deleteData("chatMapper.roomUserDelete", map ) ; 
		} catch (Exception e) { 
			log.debug("에러 발생 !!! " );			
		}
		
	}
	
	// 접속 해제 시 roomId, userId 를 가져올때 사용 
	// 필요 파라미터 : sessionId 
	public CommMap selectUserInfo(CommMap map) { 
		CommMap user = null ; 
		try {
			user = dao.getReadData("chatMapper.roomUserList", map) ; 
		} catch (Exception e) { 
			
		}
		
		return user ; 
	}
	

}

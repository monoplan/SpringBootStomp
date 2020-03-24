<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<% request.setCharacterEncoding("UTF-8");%>

<!DOCTYPE html>
<html lang="ko">

<head>
  <meta charset="UTF-8">
  <title>Title</title>
</head>

<script src="/webjars/jquery/3.3.1/dist/jquery.min.js"></script>  
<script src="/webjars/sockjs-client/1.1.2/sockjs.min.js"></script>  
<script src="/webjars/stomp-websocket/2.3.4/lib/stomp.min.js"></script>  

<script src="/chat_ddp.js"></script>  

<link href="http://fonts.googleapis.com/earlyaccess/jejugothic.css" rel="stylesheet">

<style>
div.chat					{font-size:12px; font-family: 'Jeju Gothic', sans-serif; line-height:20px;}
div.contentMenu				{}
div.contentMenu	div			{float:left; min-width:70px; text-align:center; display:inline-block; border:1px solid #aaa; border-bottom:0; border-radius: 7px 7px 0 0; padding-top:5px; background-color:#eee; cursor:pointer; }
div.contentMenu	div.selected{background-color: #aaa; color:#fff ; border-color:#888; }

div.contentWrap				{border:1px solid #aaa; width:300px; height:300px; clear:both;}
div.contentMain				{height:240px; padding-top:10px; overflow:auto; }

div.userList input			{margin:5px 5px 5px 10px; position:relative; top:4px;}
div.roomList #roomList div	{padding:5px 0 5px 10px; cursor:pointer; }
div.chatDiv					{height:230px; overflow:auto; padding-bottom:10px;}
div.roomFocus				{background-color:#E9E9E9; }
span.connUser				{display:inline; color:#6699ff; font-weight:bold; }
div.chatList .contentMain	{background-color:#B8E1F0;}
div.chatContents			{padding:5px; clear:both; }
div.info					{color:#000; text-align: center; }
div.writeOther .chatContent	{float: left; background-color: #ffffff; border-radius: 7px; padding:5px 10px; word-break: break-all;}
div.writeMe	.chatContent	{float:right; background-color: #FFFFC0; border-radius: 7px; padding:5px 10px; word-break: break-all;}
div.writeMe .writer			{display:none; }
div.contentSub				{padding-top: 10px;}
span.stopRoom				{font-family: 돋움체; font-size: 8px; font-weight: bold; line-height: 14px; text-align: center; width: 15px; height: 14px; display: inline-block; margin-left: 7px; background-color: red; border-radius: 3px; color: #FFF; }

a.btn						{border-radius:5px; background-color:#0086C5; padding:3px 8px; color:#fff; cursor:pointer; }

div #makeRoom				{float:right; margin-right:10px;}
form #txt					{margin-left:10px; border:1px solid #C0C0C0; height:20px;}
form #send					{padding:7px 10px; position:relative; top:-1px; background-color:#a8a8a8}
form #roomOut				{float:right; margin-right:10px;}

</style>


<body>

<div id="testdiv" style="display:none;">	
	<form name="sessionForm">
		id: <input type="text" id="jsessionId" value="admin" /><br/>
		pw: <input type="text" id="jsessionPw" value="wizeye1004#" /><br/>
			<input type="submit" value="접속" />
	</form>

	<br />
	
	세션 id : <span id="jsessionVal"></span>
	
	<br/><br/>
	
	<form>
		<input type="button" name="connChat" value="채팅 접속"/> 
		<input type="button" name="disConn" value="채팅 해제"/>
	</form>

	<br/><br/>	

</div>

<div class="chat">
	
	<div class="contentMenu" style="disply:none;">
		<div name="userList">
			유저 목록
		</div>
		<div name="roomList">
			방 목록
		</div>
		<div name="chatList">
			채팅방
		</div>
	</div>
	
	<div class="contentWrap">

		<!-- 유저목록  -->
		<div class="content userList">
			<div class="contentMain" id="userList">
				<form name="makeForm"></form>
			</div>	
			<div class="contentSub">
				<a id="makeRoom" class="btn">방 생성</a>		
			</div>
		</div>

		<!-- 방 목록  -->
		<div class="content roomList">	
			<div class="contentMain">
				<div id="roomList"></div>
			</div>	
			<div class="contentSub">

			</div>
		</div>


		<!-- 채팅방  -->
		<div class="content chatList">	
			<div class="contentMain">
				<div id="chatDiv" class="chatDiv"></div>
			</div>	
			<div class="contentSub">
				<form name="theform">
					<input id="txt" type="text" />
					<a id="send" class="btn">전송</a>
					<a id="roomOut" class="btn">퇴장</a>
				</form>
			</div>
		</div>

	</div>
	
</div>
	

</body>


<script>

// 페이지 로딩 후 어플의 함수 호출  
$(document).ready(function() { 	
	if (window.window.ATC==undefined) {
		$("#testdiv").css("display","block");
	} else {
		window.ATC.requestdata() ; 
	}
});


var client = null ; 
var subscription = null ; 

var userId = null ; 
var userPw = null ; 
var room = null ; 
var roomId = null ; 
var endpoint = "/endpoint" ; 


//로그인 
$("form[name='sessionForm']").on("submit", function(){
	login() ; 
	return false ; 
});


//채틸 접속 
$("input[name='connChat']").on("click", function(){
	//checkConn() ;
	if (userId == null) { alert('아이디 설정 필요'); return ; }
	setuserid(userId, userPw) ; 
});

// 채팅 해제 
$("input[name='disConn']").off().on("click", function(){
	disconnect() ; 
});

// 초기 메뉴 설정 
changeMenu("userList") ;
$(".contentMenu div[name='chatList']").css("display","none");


// 메뉴 
$(".contentMenu div").on("click", function(){
	console.log( $(this).attr("name") ) ; 
	changeMenu($(this).attr("name")) ; 
});

// 방생성 
$("#makeRoom").on("click", function() {
	createRoom() ; 
	return false ; 
});

//메세지 전송 
$("#txt").keydown(function(key) {
	if (key.keyCode == 13) {
		messageSend() ; 
		return false ; 
	}
});

$("#send").on("click", function() {
	messageSend() ; 
	return false ; 
});


//방 나가기 
$("#roomOut").on("click", function(){
	roomOut() ; 
});


// 소켓 연결 확인 
function checkConn() { 

	if( client!= null) {
		alert("접속중"); 
		return ;  
	}
	
	if (userId == null) { 
		alert("세션 값 없음") ; 
		return ; 
	}

	var data = {} ; 
	data.userId = userId ; 
	
	ajaxTrans("/chat/getList", data, getListCallback) ; 

}

// 목록 가져오기 콜백 
function getListCallback(data) { 

	data = data.responseJSON ;

	if (data.result!='ok') { 
		alert("접속 에러");
		return ; 
	}

	// 유저 목록 설정 
	/*
	var userList = data.data.userList ; 
	for (var i=0 ; i < userList.length ; i++ ) { 
		$("form[name='makeForm']").append("<input type='checkbox' id='user_"+userList[i].USERID+"' name='userid' value='"+userList[i].USERID+"' /> <label for='user_"+userList[i].USERID+"'>"+userList[i].USERID+"</label><br/>") ; 
	}
	*/
	
	// 방 목록 설정 
	var roomList = data.data.roomList ; 
	for (var i=0 ; i < roomList.length ; i++ ) { 
		updateRoom(roomList[i]) ;
	}

	connChat(data) ; 
	
}


// 채팅 서버 접속    
function connChat() { 

	var sock = new SockJS(endpoint);
	client = Stomp.over(sock);

	client.connect({}, function () {

	    // 개인 메세지 수신 설정 
	    client.subscribe('/queue/'+ userId, function (data) {

	        console.log("들어온 메세지(data) :"+ data );
	    	
	        var contents = JSON.parse(data.body);
	        console.log("들어온 메세지 :"+ data.body );

	        /*
	        // 방 정보 가져올때 
			if (contents.type == 'room' ) {
				json = JSON.parse(data.body);
				updateRoom(json.info) ;    
			}
			*/

	        // 방 정보 업데이트  
			if (contents.type == 'roomUpdate' ) {
				var json = JSON.parse(data.body);
				updateRoom(json, 1 ) ;  
			}
				
	        // 방 강제 종료   
			if (contents.type == 'roomStop' ) {
				var json = JSON.parse(data.body);
				stopRoom(json) ;  
			}
	        
	    });
	    
	});

	$("input[name='connChat']").attr("disabled",true);
	$("input[name='disConn']").attr("disabled",false);

}


// 소켓 해제 
function disconnect() {

	if (subscription!= null) { 
		subscription.unsubscribe();
		subscription = null ; 		
	}
	
	if (client != null) {
		client.disconnect();
		client = null ;
    }

	if (roomId != null ) {
		roomId = null ; 
	}
	
	$("input[name='connChat']").attr("disabled",false);
	$("input[name='disConn']").attr("disabled",true);


	$("#roomList").text('')
	$("form[name='makeForm']").text('');
	$("#contentDiv").text('');

	$("#chatDiv").text('');

	changeMenu("roomList") ;

	$(".contentMenu div[name='chatList']").css("display","none");

    console.log("Disconnected");

}

// 로그인 
function login() { 

	if ( $("#jsessionId").val() == "" ) {
		alert("아이디 없음");
		return ;
	}

	$("#jsessionVal").text( $("#jsessionId").val() ) ;
	userId = $("#jsessionId").val() ; 
	userPw = $("#jsessionPw").val() ; 
	
	return ;
	
}




//메뉴 변경 
function changeMenu(menu) {
	$(".contentMenu div").removeClass("selected");
	$(".contentMenu div[name='"+menu+"']").addClass("selected");

	$(".contentWrap .content").css("display","none");

	$(".contentMenu div[name='"+menu+"']").css("display","block");
	
	$(".contentWrap ."+menu).css("display","block");
}


// 방생성 
function createRoom() { 

	if( client== null) {
		alert("채팅 서버에 접속 되지 않았습니다."); 
		return ;  
	}
	
	if ( $("input[name='userid']:checked").length == 0 ) { 
		alert("선택된 유저가 없습니다.");
		return ; 
	} 

	var arr = [] ; 
	arr.push(userId); 
	
	$("input[name='userid']:checked").each(function() {
		arr.push($(this).val());
	});

	var json = {} ; 
	json.roomOwner = userId; 
	json.userid = arr ;
	
	ajaxTrans("/makeRoom", json, createRoomCallback) ; 
	
}

// 방 생성 콜백 
function createRoomCallback(data) { 

	if (data.responseJSON.result == 'ng') { 
		alert("로그인 후 이용 해 주세요.");
		return ; 
	}
	
	changeMenu("roomList") ;
}



// 방 정보 추가, 변경  
function updateRoom(json, blink) {

	var users = ""; 
	var roomOwner = ""; 
	for (var i=0; i<json.users.length; i++) {
		if (json.users[i].userId != userId ) { 
			if (json.users[i].state==1) { 
				// 접속중일때 
				users += ", <span class='connUser'>"+ json.users[i].userId+ "</span>"  ;
			} else {
				// 비 접속중일때 
				users += ", "+ json.users[i].userId  ;
			}
		} else { 
			if (json.users[i].roomOwner=="1") { 
				roomOwner = "1" ;
			}
		}
	}
	users = users.substring(1) ; 
	
	// 방이 존재 하는 경우 수정  
	if ( $("#"+json.roomId ).length == 1 ) { 
		$("#"+json.roomId +" span[name='usersInfo']").html(users);
		return ; 
	}

	// 깝박 효과 
	if (blink==1) { 
		$("div[name='roomList']").fadeOut(50).fadeIn(200);  
	}
		
	// 방을 새로 추가 하는 경우 
	$("#roomList").append("<div class='roomInfo' id='"+ json.roomId +"'><span name='usersInfo'>"+users+"</span></div>") ;

	// 방장일때 방 삭제 기능 
	if (roomOwner=="1") { 
		$("#"+json.roomId).append("<span class='stopRoom' value='"+ json.roomId +"'>X</span>");
	}

	// 방 제목 클릭시 
	$("#"+json.roomId).on("click", function() {
		$(".roomInfo").removeClass("roomFocus");
		$(this).addClass("roomFocus");

		if (subscription != null) { 
			alert("진행중인 채팅이 존재 합니다.\n\n진행중인 채팅방으로 이동합니다.");
			changeMenu("chatList") ;
			return false ; 
		}

		// 터치 시 방 입장
		$("#chatDiv").text('');	
		$("#txt").val('');
		
	    roomId = json.roomId ; 
	    
		// 채팅 방 구독 시작 
	    subscription = client.subscribe('/subscribe/room/'+json.roomId, function (data) {
			receiveChat(data) ;       
	    });

		// 방 입장 메세지 전송 
	    roomIn(json.roomId) ; 

		// 채팅 방 으로 이동 
		changeMenu("chatList") ; 
		
	});

	// 방 종료 클릭시 
	$(".stopRoom").off().on("click", function() {
		console.log( $(this).attr("value") );
		chkStopRoom($(this).attr("value"));
		//event.stopPropagation();
	});

/*	
	// 방에 입장 
	$("#"+json.roomId).on("dblclick", function() {
		
	});
*/

}

// 방 강제 종료 확인  
function chkStopRoom(roomId) { 

	event.stopPropagation();

	var chk = confirm("정말 방을 종료 시키겠습니까?");

	if (!chk) {
		return ; 
	}

	var json = {} ;
	json.roomId = roomId;
	json.userId = userId ; 

	ajaxTrans("/stopRoom", json ) ; 
	
}

// 방 강제 종료 
function stopRoom(json) { 

	if ( subscription != null && roomId == json.roomId ) {
		subscription.unsubscribe();
		subscription = null ; 
	}

	$("#"+ json.roomId ).remove();
	 
	// 깝박 효과 
	$("div[name='roomList']").fadeOut(50).fadeIn(200);  

	if ( roomId == json.roomId ) { 
		roomId = null ;
	}
	
}


// 받은 메시지 처리 
function receiveChat(data) { 
    
    var contents = JSON.parse(data.body);
    console.log("들어온 메세지 :"+ data.body );

    // 입장 
    if (contents.state=='1') {  
        $("#chatDiv").append("<div class='chatContents info'>"+ contents.userId +" 님 입장 </div>");
		$("#chatDiv").scrollTop( $("#chatDiv")[0].scrollHeight );
    }
    // 퇴장 
    if (contents.state=='0') {  
        $("#chatDiv").append("<div class='chatContents info'>"+ contents.userId +" 님 퇴장 </div>");
		$("#chatDiv").scrollTop( $("#chatDiv")[0].scrollHeight );
    }
    // 채팅일때 
    if (contents.state=='chat') {  
        // 다른 사람 일때
        if ( userId != contents.userId ) {  
	        $("#chatDiv").append("<div class='chatContents writeOther'><div class='writer'>"+ contents.userId +"</div><div class='chatContent'>"+contents.msg+"</div></div>");
			$("#chatDiv").scrollTop( $("#chatDiv")[0].scrollHeight );
        } else { 
	        $("#chatDiv").append("<div class='chatContents writeMe'><div class='writer'>"+ contents.userId +"</div><div class='chatContent'>"+contents.msg+"</div></div>");
			$("#chatDiv").scrollTop( $("#chatDiv")[0].scrollHeight );
        }
    }
    // 방 강제 종료일때 
    if (contents.state=='stop') {  
        $("#chatDiv").append("<div class='chatContents info'>채팅방이 종료 되었습니다.</div>");
		$("#chatDiv").scrollTop( $("#chatDiv")[0].scrollHeight );
    }

    // 100 개 넘어가면 이전 메세지는 배운다 
    if ( $(".chatContents").length > 100 ) { 
    	 $(".chatContents").get(0).remove();
	}
	
}


// 메세지 전송 
function messageSend() { 

	if (subscription==null) {
		alert("방 접속중이 아님"); 
		return ;  
	}

	var json = {} ;
	json.msg = $("#txt").val();
	json.roomId = roomId;
	json.userId = userId ; 
	
	if ($("#txt").val()=='') { 
		return false ; 
	}

	ajaxTrans("/chat/msgSend", json ) ; 

	$("#txt").val('') ; 
	$("#txt").focus() ; 
	return ;
	
}



// 방 입장 메세지 전송 
function roomIn(room_id) { 

	var json = {} ; 
	json.roomId = room_id ;
	json.userId = userId ;

	ajaxTrans("/chat/roomIn", json) ; 
	
}

// 방 나가기 
function roomOut() {

	if (subscription != null) {

		subscription.unsubscribe();
		subscription = null ; 

		var json = {} ; 
		json.roomId = roomId ;
		json.userId = userId ;

		ajaxTrans("/chat/roomOut", json) ; 
		
	}
	
	$("#chatDiv").text('');	
	$("#txt").val('');
	
	roomId = null ; 

	changeMenu("roomList") ;

	$(".contentMenu div[name='chatList']").css("display","none");
}

// ajax 통신 
function ajaxTrans(url, data, callback) { 

	$.ajax({
	    type       : "POST",
	    contentType: 'application/json',
	    data : JSON.stringify(data),
	    dataType   : 'json',
	    url        : url,
	    error      : function(json){
	        console.log('오류 발생');
	    },
	    success    : function(data){
	    	console.log('성공');
	    },
	    complete   : function(data) {
			if ( callback != null ) {
		    	callback(data) ;
			}
		}
	});
	
}

// 페이지 로딩이 끝난 후 어플에서 실행 하는 함수 
function setuserid(id, pw) { 
	userId = id ; 

	window.chat = new N3N.DDP();
//	chat.open("http://mgkim.w21.n3n.co.kr","admin", "wizeye1004#");
	chat.open("http://mgkim.w21.n3n.co.kr",id, pw);
}

// 유저 목록 가져 온다 
function getUserList() { 
	window.chat.call("user.memberlist", {}, setUserList) ; 
}

// 가져온 유저 목록을 세팅한다 
function setUserList(error, data)  {
	
	var userList = data.body ; 
	for (var i=0 ; i < userList.length ; i++ ) { 
		if ( userList[i].name != userId) { 
			$("form[name='makeForm']").append("<input type='checkbox' id='user_"+userList[i].name+"' name='userid' value='"+userList[i].name+"' /> <label for='user_"+userList[i].name +"'>"+userList[i].name +"</label><br/>") ;
		}
	}

	// 채팅 서버 접속 
	checkConn() ;  
}


</script>

</html>
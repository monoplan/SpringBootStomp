package com.example.demo.common;

import org.springframework.stereotype.Component;
import com.example.demo.model.CommMap;

@Component
public class RestResponse {

	public static enum ResponseCode {
		SUCCESS("ok"), FAIL("ng") ;
		public final String value ;

		private ResponseCode(final String value) { 
			this.value = value ; 
		}
	}
	
	private String resCode ; 
	private String msg ;  
	
	public String getResCode() {
		return resCode;
	}

	public void setResCode(String resCode) {
		this.resCode = resCode;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg ;
	}

	public CommMap setSuccess(CommMap data) { 
		this.resCode = ResponseCode.SUCCESS.value ;

		CommMap map = new CommMap(); 
		map.put("result", this.getResCode());
		if (data!= null) map.put("data", data);
		
		return map ; 
	}

	public CommMap setSuccess() { 
		CommMap map = setSuccess(null) ; 	
		return map ; 
	}

	public CommMap setFail() { 
		return setFail(null) ; 
	}
	
	public CommMap setFail(String str) { 
		this.resCode = ResponseCode.FAIL.value ;
		
		CommMap map = new CommMap(); 
		map.put("result", this.getResCode());
		
		if (str!=null) {  
			this.setMsg(str);
			map.put("msg", this.getMsg());
		}
			
		return map ;		
		 
	}

}

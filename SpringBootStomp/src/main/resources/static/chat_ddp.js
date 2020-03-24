var N3N={};

N3N.DDP = function() {
}

N3N.DDP.prototype = {
	ddp: null,
	req: 1,
	requestMap: {},
	connected: false,
	reserved: [],
	token: "",
	_onopen: function (e)
	{
		console.log("CONNECTED!\r\n");
		this.connected = true;
		this.sendReserved();
		if (this.onopen) this.onopen();
		
		// CONNECTED 후 실행 할 함수 
		// 콜백이 없어서 추가 
		// 2019.05.23
		getUserList() ; 
		
	},
	_onmessage: function (e)
	{
		if (e.data[0] == "a")
		{
			var resp = JSON.parse(JSON.parse(e.data.substring(1))[0]);
			if (this.requestMap[resp.id])
			{
				this.requestMap[resp.id](null, resp.result);
				delete this.requestMap[resp.id];
			}
		}
	},
	_onclose: function (e)
	{
		this.connected = false;
		console.log("DISCONNECTED!\r\n");
		if (this.onclose) this.onclose();
	},
	openDDP: function (url)
	{
		if (this.ddp)
		{
			this.ddp.close();
			delete this.ddp;
		}

		this.ddp = new WebSocket(url);
		this.ddp.onopen = this._onopen.bind(this);
		this.ddp.onmessage = this._onmessage.bind(this);
		this.ddp.onclose = this._onclose.bind(this);
	},
	sendReserved: function()
	{
		for(var idx in this.reserved)
		{
			var p = this.reserved[idx];
			this.call(p.method, p.params, p.callback);
		}
		
		delete this.reserved;
	},
	call: function (method, params, callback)
	{
		if (!this.connected)
		{
			this.reserved.push({
				method: method,
				params: params,
				callback: callback
			});
		} else
		{
			this.ddp.send(JSON.stringify([JSON.stringify({
				"msg":"method",
				"id":this.req,
				"method":method,
				"params":params
			})]));
			this.requestMap[this.req] = callback;
			this.req++;
		}
	},
	httpQuery: function (url, type, data, callback) {
		var xmlhttp = new XMLHttpRequest();
		xmlhttp.onreadystatechange = function(){
			if (xmlhttp.readyState == 4)
			{
				if (xmlhttp.status != 200){
					console.log("xmlhttp.status: ", xmlhttp.status);
				}
				callback(xmlhttp.responseText, xmlhttp.status);
			}
		}
		xmlhttp.open((data)?"POST":"GET", url, true);
		xmlhttp.setRequestHeader("Content-type", type);
		if (data && type == "application/json")
		{
			xmlhttp.send((data)?JSON.stringify(data):null);
		} else
		{
			xmlhttp.send(data);    
		}
	},
    open: function(wizeyeUrl, userId, userPassword) {
		var me = this;
		
		this.wizeyeUrl = wizeyeUrl;

		if (this.token)
		{
			
		}
		
		me.httpQuery(wizeyeUrl + "/login", "application/json", {
			"username":userId,
			"password":userPassword
		}, function(data, status) {
			if (status == 200)
			{
				var data = JSON.parse(data);

				//alert("token: " + data.token);
				console.log("token: " + data.token);
				me.token = data.token;
				var ddpUrl = wizeyeUrl.trim();
				if (ddpUrl.startsWith("https://")) ddpUrl = "wss://" + ddpUrl.substr(8) + "/echo?token=" + me.token;
				else if (ddpUrl.startsWith("http://")) ddpUrl = "ws://" + ddpUrl.substr(7) + "/echo?token=" + me.token;
				else ddpUrl = "ws://" + ddpUrl + "/echo?token=" + me.token;
				console.log("ddpUrl: " + ddpUrl);
				//n.onopen = me.ddpReady.bind(me);
				me.openDDP(ddpUrl);
			}
			// 로그인 실패시   
			// 2019.05.23
			else { 
				alert('로그인 에러');
			}
		});
					
		console.log("open");
	},
	clsoe: function() {
		if (this.ddp) this.ddp.close();
		delete this.ddp;
	},
	ddpReady : function()
	{
		alert("DDP OK");
	}
};
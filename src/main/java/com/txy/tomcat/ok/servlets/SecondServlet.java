package com.txy.tomcat.ok.servlets;

import com.alibaba.fastjson.JSON;
import com.txy.tomcat.ok.http.GPRequest;
import com.txy.tomcat.ok.http.GPResponse;
import com.txy.tomcat.ok.http.GPServlet;

public class SecondServlet extends GPServlet {

	@Override
	public void doGet(GPRequest request, GPResponse response) {
		doPost(request, response);
	}
	
	@Override
	public void doPost(GPRequest request, GPResponse response) {
	    String str = JSON.toJSONString(request.getParameters(),true);
	    response.write(str,200);
	}
	
}

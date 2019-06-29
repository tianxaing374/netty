package com.txy.tomcat.ok.servlets;


import com.txy.tomcat.ok.http.GPRequest;
import com.txy.tomcat.ok.http.GPResponse;
import com.txy.tomcat.ok.http.GPServlet;

public class FirstServlet extends GPServlet {

	
	@Override
	public void doGet(GPRequest request, GPResponse response) {
		doPost(request, response);
	}

	
	@Override
	public void doPost(GPRequest request, GPResponse response) {
		String param = "name";  
	    String str = request.getParameter(param);  
	    response.write(param + ":" + str,200);
	}
	
}

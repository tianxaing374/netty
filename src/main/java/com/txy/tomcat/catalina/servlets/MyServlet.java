package com.txy.tomcat.catalina.servlets;

import com.txy.tomcat.catalina.http.GPRequest;
import com.txy.tomcat.catalina.http.GPResponse;
import com.txy.tomcat.catalina.http.GPServlet;

import java.io.UnsupportedEncodingException;

public class MyServlet extends GPServlet {
    @Override
    public void doGet(GPRequest request, GPResponse response) {
        response.write(request.getParameters("name"),200);
    }

    @Override
    public void doPost(GPRequest request, GPResponse response) {
        doGet(request,response);
    }
}

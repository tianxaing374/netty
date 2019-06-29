package com.txy.tomcat.catalina.http;

public abstract class GPServlet {
    protected abstract void doGet(GPRequest request, GPResponse response);
    protected abstract void doPost(GPRequest request, GPResponse response);
}

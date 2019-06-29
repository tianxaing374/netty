package com.txy.chat.server.handler;

import org.junit.Test;

import java.net.URISyntaxException;

import static org.junit.Assert.*;

/**
 * Created by TianXiang on 2019/6/24.
 */
public class HttpHandlerTest {

    @Test
    public void testBaseUrl() throws URISyntaxException {
        System.out.println(HttpHandlerTest.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    }

}
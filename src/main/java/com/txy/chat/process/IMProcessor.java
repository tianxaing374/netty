package com.txy.chat.process;

import com.alibaba.fastjson.JSONObject;
import com.txy.chat.protocol.IMDecoder;
import com.txy.chat.protocol.IMEncoder;
import com.txy.chat.protocol.IMMessage;
import com.txy.chat.protocol.IMP;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;

public class IMProcessor {

    private final static ChannelGroup onlineUsers = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private IMDecoder decoder = new IMDecoder();
    private IMEncoder encoder = new IMEncoder();
    private final AttributeKey<String> NICK_NAME = AttributeKey.valueOf("nickName");
    private final AttributeKey<String> IP_ADDR = AttributeKey.valueOf("ipAddr");
    private final AttributeKey<JSONObject> ATTRS = AttributeKey.valueOf("attrs");

    public void process(Channel client,IMMessage msg){
        process(client,encoder.encode(msg));
    }

    public void process(Channel client,String msg){
        IMMessage request = decoder.decode(msg);
        if(request == null) return;
        String nickName = request.getSender();
        if(IMP.LOGIN.getName().equals(request.getCmd())){
            client.attr(NICK_NAME).getAndSet(request.getSender());
            onlineUsers.add(client);
            for (Channel channel : onlineUsers) {
                if(channel!=client){
                    request = new IMMessage(IMP.SYSTEM.getName(),sysTime(),onlineUsers.size(),nickName+"加入聊天室");
                } else {
                    request = new IMMessage(IMP.SYSTEM.getName(),sysTime(),onlineUsers.size(),"与服务器建立连接");
                }
                String text = encoder.encode(request);
                channel.writeAndFlush(new TextWebSocketFrame(text));
            }
        } else if(IMP.LOGOUT.getName().equals(request.getCmd())){
            onlineUsers.remove(client);
        } else if(IMP.CHAT.getName().equals(request.getCmd())){
            for (Channel channel : onlineUsers) {
                if(channel!=client){
                    request.setSender(client.attr(NICK_NAME).get());
                } else {
                    request.setSender("you");
                }
                String text = encoder.encode(request);
                channel.writeAndFlush(new TextWebSocketFrame(text));
            }
        } else if(IMP.FLOWER.getName().equals(request.getCmd())){
            JSONObject attrs = getAttrs(client);
            long currTime = sysTime();
            if(null != attrs){
                //属性绑定到channel上
                long lastTime = attrs.getLongValue("lastFlowerTime");
                //60秒之内不允许重复刷鲜花
                int secends = 10;
                long sub = currTime - lastTime;
                if(sub < 1000 * secends){
                    request.setSender("you");
                    request.setCmd(IMP.SYSTEM.getName());
                    request.setContent("您送鲜花太频繁," + (secends - Math.round(sub / 1000)) + "秒后再试");
                    String content = encoder.encode(request);
                    client.writeAndFlush(new TextWebSocketFrame(content));
                    return;
                }
            }

            //正常送花
            for (Channel channel : onlineUsers) {
                if (channel == client) {
                    request.setSender("you");
                    request.setContent("你给大家送了一波鲜花雨");
                    setAttrs(client, "lastFlowerTime", currTime);
                }else{
                    request.setSender(client.attr(NICK_NAME).get());
                    request.setContent(client.attr(NICK_NAME).get() + "送来一波鲜花雨");
                }
                request.setTime(sysTime());

                String content = encoder.encode(request);
                channel.writeAndFlush(new TextWebSocketFrame(content));
            }
        }
    }

    public void logout(Channel client){
        String nickName = client.attr(NICK_NAME).get();
        if(nickName == null) return;
        onlineUsers.remove(client);
        IMMessage message = new IMMessage();
        message.setCmd(IMP.SYSTEM.getName());
        message.setTime(sysTime());
        message.setOnline(onlineUsers.size());
        message.setContent(nickName+"退出了聊天室");
        for (Channel channel : onlineUsers) {
            channel.writeAndFlush(new TextWebSocketFrame(encoder.encode(message)));
        }
    }

    private Long sysTime(){
        return System.currentTimeMillis();
    }

    public JSONObject getAttrs(Channel client){
        try{
            return client.attr(ATTRS).get();
        }catch(Exception e){
            return null;
        }
    }

    /**
     * 获取扩展属性
     * @param client
     * @return
     */
    private void setAttrs(Channel client,String key,Object value){
        try{
            JSONObject json = client.attr(ATTRS).get();
            json.put(key, value);
            client.attr(ATTRS).set(json);
        }catch(Exception e){
            JSONObject json = new JSONObject();
            json.put(key, value);
            client.attr(ATTRS).set(json);
        }
    }

}

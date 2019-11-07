package com.boot.websocket;

import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;

/**
 * @Description: 测试类
 * @Author: Liu Guo Dian
 * @Date: 2019/11/7 10:51
 * @Version: 1.0
 */
@ServerEndpoint("/websocket")
@Component
public class WebSocketTest {
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;

    //用来存放每个客户端对应的MyWebSocket对象。
    //使用Map来存放，其中Key可以为用户标识
    private static HashMap<String,WebSocketTest> webSocketMap =new HashMap<>();
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    /**
     * 连接建立成功调用的方法
     * @param session  可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(Session session) throws IOException {
        this.session = session;
        webSocketMap.put(session.hashCode()+"",this); //加入map中
        System.out.println("这个客户端的用户名:" + session.hashCode());
        this.sendMessage("你分配的用户名:" + session.hashCode());
        addOnlineCount();           //在线数加1
        System.out.println("有新连接加入！当前在线人数为" + getOnlineCount());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(){

        webSocketMap.remove(this);//从map中删除
        subOnlineCount();           //在线数减1
        System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());

    }

    /**
     * 收到客户端消息后调用的方法
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     */
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {

        //发送的用户号就是session.hashcode(),可以再加个map继续映射
        //通过#*#*这个字符串来分割消息的组成，我随意设定的
        int pos=message.indexOf("#*#*");
        String realmessage=message.substring(0,pos);
        //这个4是因为#*#*这个字符串长度为4
        String realuser=message.substring(pos+4,message.length());
        System.out.println("来自客户端的消息:" + realmessage);
        //
        WebSocketTest item=webSocketMap.get(realuser);
        item.sendMessage(realmessage);

    }

    /**
     * 发生错误时调用
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error){
        System.out.println("发生错误");
        error.printStackTrace();
    }

    /**
     * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。
     * @param message
     * @throws IOException
     */
    //给客户端传递消息
    public void sendMessage(String message) throws IOException{
        this.session.getBasicRemote().sendText(message);
        //this.session.getAsyncRemote().sendText(message);
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketTest.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketTest.onlineCount--;
    }

}

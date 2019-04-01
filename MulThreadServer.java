package com.yimm;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: sy
 * Create:2019-03-31
 * 20:44
 */

/*
服务器实现：存储所有连接的客户端--为了区分不同客户端，应有用户名与对应socket
用Map<uesrname,Socket>

退出：userName:bye

*
* 用户注册：userName:xx
*
* 群聊实现：
* G(GROUP):聊天内容
*
* 私聊实现：
* P(PRIVATE):用户名-聊天内容
*
*
*
* */




public class MulThreadServer {
    //使用ConcurrentHashMap来保证线程安全保存所有连接的客户端信息
    private static Map<String,Socket> clientMap=new ConcurrentHashMap<String,Socket>();


    public static void main(String[] args) {
        try {
            ServerSocket serverSocket=new ServerSocket(6666);
            ExecutorService service= Executors.newFixedThreadPool(20);
            for(int i=0;i<20;i++){
                System.out.println("等待客户端连接......");
                Socket client=serverSocket.accept();
                System.out.println("有新的客户端连接，端口号为："+client.getPort());
                //提交请求
                service.submit(new ExecuteClientRequest(client));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //处理客户端请求的线程
    static class ExecuteClientRequest implements  Runnable{

        private Socket client;
        public ExecuteClientRequest(Socket client){
            this.client=client;
        }


        public void run() {
            //获取输入流，不断的读取用户发来的信息
            Scanner readFromClient=null;
            try {
                readFromClient=new Scanner(client.getInputStream());
                readFromClient.useDelimiter("\n");
                while(true){
                    //只需要获取用户发来的信息进行处理转发即可
                    if(readFromClient.hasNextLine()){
                        String str=readFromClient.nextLine();
                        //进行\r过滤---针对Windows
                        Pattern pattern=Pattern.compile("\r");
                        Matcher matcher=pattern.matcher(str);
                        str=matcher.replaceAll("");
                        /*用户注册：userName:xx
                        * 群聊实现：
                            G(GROUP):聊天内容
                        * 私聊实现：
                            P(PRIVATE):用户名-聊天内容
                        */
                        if(str.startsWith("userName")){
                            //用户注册
                            String userName=str.split(":")[1];
                            userRegister(str,client);
                            continue;

                        }else if(str.startsWith("G:")){
                            /*群聊实现：
                            G(GROUP):聊天内容*/

                            String msg=str.split(":")[1];
                            groupChat(msg);
                            continue;

                        }else if(str.startsWith("P:")){
                            //私聊
                            String tempMag=str.split(":")[1];
                            String userName=tempMag.split("-")[0];
                            String privateMsg=tempMag.split("-")[1];
                            privateChat(userName,privateMsg);
                            continue;
                        }else if(str.contains("bye")){
                            //用户退出
                            String userName=str.split(":")[0];
                            userExist(userName);
                            continue;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //用户注册方法，需要用户名以及socket
        private void userRegister(String userName,Socket client){
            //将用户信息保存到服务器中（clientMap）中
            clientMap.put(userName,client);
            //取得当前注册的所有人的数目
            int size=clientMap.size();
            System.out.println("当前聊天室内共有"+size+"人");
            String userOnline=userName+"上线了！";
            groupChat(userOnline);

        }

        //群聊  msg 要发送的群聊信息
        private void groupChat(String msg){
            //取出所有连接的客户端，依次拿出到输出流进行遍历
            Collection<Socket> clients=clientMap.values();
            for(Socket client:clients){
                //取出客户端输出流
                try {
                    PrintStream out=new PrintStream(client.getOutputStream(),true,"UTF-8");
                    out.println("群聊信息为:"+msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //私聊 uesrName 私聊的用户名  msg 私聊的信息
        private void privateChat(String userName,String msg){
            Socket client=clientMap.get(userName);
            try {
                PrintStream out=new PrintStream(client.getOutputStream(),true,"UTF-8");
                out.println("私聊信息为："+msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //用户退出 userName：退出用户名--将该用户的socket从map上删除
        private void userExist(String userName){
            clientMap.remove(userName);
            System.out.println("当前聊天室的人数为："+clientMap.size());
            String groupMsg=userName+"已经下线了！";
            groupChat(groupMsg);

        }
    }

}

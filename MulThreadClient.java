package com.yimm;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * Author: sy
 * Create:2019-03-31
 * 20:19
 *
 * 读写分离
 */

//接受消息线程,接受服务器消息---输入
class ReadFromServer implements Runnable{
    private Socket client;
    //通过构造方法传入通信的socket
    public ReadFromServer(Socket client){
        this.client=client;
    }

    public void run() {
        //获取输入流，读取服务器发来的信息
        Scanner readFromServer=null;
        try {
            readFromServer=new Scanner(client.getInputStream());
            readFromServer.useDelimiter("\n");
            //不断读取服务器传来的信息
            while(true){
                if(readFromServer.hasNextLine()){
                    String str=readFromServer.nextLine();
                    System.out.println("收到服务器发来消息:"+str);
                }
                //关闭由负责写的线程控制
                if(client.isClosed()){
                    System.out.println("客户端已经关闭");
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            readFromServer.close();
        }
    }
}

//发送消息线程，向服务器发送信息--输出
class SendMessToServer implements Runnable{
    private Socket client;

    public SendMessToServer(Socket client){
        this.client=client;
    }

    public void run() {
        //获取键盘输入，向服务器发送信息
        Scanner in=new Scanner(System.in);
        in.useDelimiter("\n");
        //自动刷新缓冲区
        PrintStream sendMessToServer=null;
        try{
            //获取输出流，向服务器发送消息
            sendMessToServer=new PrintStream(client.getOutputStream(),true,"UTF-8");
            while(true){
                System.out.println("请输入发送的信息...");
                if(in.hasNextLine()){
                    String strToServer=in.nextLine();
                    sendMessToServer.println(strToServer);
                    if(strToServer.contains("bye")){
                        System.out.println("关闭客户端");
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                client.close();
                sendMessToServer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}

public class MulThreadClient {
    public static void main(String[] args) {
        //建立与服务器的连接
        try {
            Socket client=new Socket("127.0.0.1",6666);
            //创建读写线程与服务器进行交互
            Thread readThread=new Thread(new ReadFromServer(client));
            Thread writeThread=new Thread(new SendMessToServer(client));
            readThread.start();
            writeThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

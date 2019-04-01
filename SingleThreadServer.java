package com.yimm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Author: sy
 * Create:2019-03-31
 * 19:30
 */
public class SingleThreadServer {
    public static void main(String[] args) {
        try {
            //1.创建ServerSocket对象
            ServerSocket serverSocket=new ServerSocket(6666);
            //2.等待客户端连接
            System.out.println("等待客户端连接......");
            Socket clientSocket=serverSocket.accept();
            System.out.println(clientSocket.getRemoteSocketAddress()+" 客户端已经与服务器建立连接，可以开始通话\n");

            /*接受和发送数据,此时为字节流
             * 接受数据*/
            InputStream clientInput=clientSocket.getInputStream();

            Scanner scanner=new Scanner(clientInput);
            String clientData=scanner.next();
            System.out.println("收到客户端的消息："+clientData);


            //发送数据
            OutputStream clientOutput=clientSocket.getOutputStream();

            //需处理字节流-->字符流
            OutputStreamWriter writer=new OutputStreamWriter(clientOutput);
            writer.write("hello,欢迎您\n");
            //bug标记,因为数据传输机制导致有可能分次发送数据包，需要刷新一下
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

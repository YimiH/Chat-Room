package com.yimm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Author: sy
 * Create:2019-03-31
 * 19:33
 */
public class SingleThreadClient {

    public static void main(String[] args) {
        //1.创建客户端，连接到服务器
        try {
            Socket clientSocket=new Socket("127.0.0.1",6666);
            //2.发送数据
            OutputStream clientOutput=clientSocket.getOutputStream();
            OutputStreamWriter writer=new OutputStreamWriter(clientOutput);
            writer.write("hello,服务器,我是客户端\n");
            //bug标记,因为数据传输机制导致有可能分次发送数据包，需要刷新一下
            writer.flush();
            //3.接受数据
            InputStream clientInput=clientSocket.getInputStream();
            Scanner scanner=new Scanner(clientInput);
            String serverData=scanner.nextLine();
            System.out.println("收到来自服务端的数据："+serverData);

            //4.关闭客户端
            clientInput.close();
            clientOutput.close();
            clientSocket.close();
            System.out.println("客户端已经关闭成功");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

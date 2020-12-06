package com.zhangyi.nio.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * 执行这段代码
 * 然后命令行：strace -ff -o out java BIOServer
 * @author Zhang Yi
 */
public class BIOServer {
    public static void main(String[] args) throws IOException {

        ServerSocket server = new ServerSocket(8080);
        while (true) {
            Socket socket = server.accept();
            System.out.println("链接端口：" + socket.getPort());
            InputStream inputStream = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String str = null;

            //todo 这里会一直IO block到有数据
            while ((str = reader.readLine()) != null) {
                System.out.println("接受：" + str);
                socket.getOutputStream().write("ok\n".getBytes());
                socket.getOutputStream().flush();
                if ("over".equals(str)) {
                    System.out.println("要关闭了");
                    socket.close();
                    break;
                }
            }
            System.out.println("===========");
        }
    }
}

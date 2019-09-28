package ru.neman.clientmaster;
import java.io.*;
import java.net.Socket;

// CLIENT
public class Main {
    public static void main(String[] args) {
//        IP 127.0.0.1 (localhost) : Port : 8080
        try {
            Socket clientSocket = new Socket("localhost", 8080);

            // Request
            OutputStream out = clientSocket.getOutputStream();
            out.write("welcome to the jungle".getBytes());
            out.flush();

            // Response
            BufferedReader bf = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            bf.lines().forEach(System.out::println);

            bf.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

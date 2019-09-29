package ru.neman.clientmasterdb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

// CLIENT
public class Main {

    public static void main(String[] args) throws IOException {
        Socket clientSocket = new Socket("localhost", 8000);
        clientSocket.setKeepAlive(true);
                            
        Scanner sc = new Scanner(System.in);

        BufferedReader in = new BufferedReader(new InputStreamReader(
                clientSocket.getInputStream()));

        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),
                true);

        while (true) {
            System.out.println("Please, write command");
            String request = sc.nextLine();

            out.println(request);

            while (!in.ready()) {
		// try Thread.sleep(100) ???
            }
            while (in.ready()) {
                String line = in.readLine();
                System.out.println(line);
            }
            if (request.toLowerCase().equals("exit")) break;
        }

        in.close();
        out.close();
        clientSocket.close();
    }
}

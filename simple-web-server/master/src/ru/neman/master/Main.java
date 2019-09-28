package ru.neman.master;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

// SERVER
public class Main {
    private final static Logger LOGGER = Logger.getLogger(Main.class.getName());

    static {
        try {
            Handler handler = new FileHandler("log.log");
            handler.setLevel(Level.ALL);
            LOGGER.addHandler(handler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            LOGGER.info("Server starts...");
            ServerSocket serverSocket = new ServerSocket(8080);
            ExecutorService es = Executors.newFixedThreadPool(5);

            for (int i = 0; i < 5; i++) {
                LOGGER.info("Thread #" + i + "starts");
                es.submit(new Thread(() -> {
                    try {

                        while (true) {
                            Socket clientSocket = serverSocket.accept();

                            // Request
                            InputStreamReader input = new InputStreamReader(clientSocket.getInputStream());
                            Thread.sleep(10000);
                            StringBuilder s = new StringBuilder();
                            while (input.ready()) s.append((char) input.read());
                            LOGGER.info("read request");
                            System.out.println(s);

                            // Response
                            OutputStreamWriter out = new OutputStreamWriter(clientSocket.getOutputStream());
                            String answer = "HTTP/1.1 200 OK\n" +
                                    "Cache-Control: no-cache\n" +
                                    "Connection: close:\n" +
                                    "Content-Type: application/json\n\n" +
                                    "{\"ok\": \"" + (s.toString().length() > 30 ? "too long" : s.toString()) + "\"}";

                            out.write(answer);
                            out.flush();

                            LOGGER.info("sent message");
                            input.close();
                            out.close();
                            clientSocket.close();
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }));
            }

            Thread.sleep(10000000);
            serverSocket.close();
            LOGGER.info("Server is closed");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}

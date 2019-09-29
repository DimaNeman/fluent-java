package ru.neman.masterdb;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

// SERVER
public class Main {
    private final static Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final HashMap<String, User> DATABASE_USERS = new HashMap<>();
    private static final ConcurrentHashMap<Socket, User> currentUsers = new ConcurrentHashMap<Socket, User>();

    static {
        User user1 = new User();
        user1.setName("dima");
        user1.setPassword("dima");

        User user2 = new User();
        user2.setName("eugene");
        user2.setPassword("eugene");

        DATABASE_USERS.put(user1.getName() + user1.getPassword(), user1);
        DATABASE_USERS.put(user2.getName() + user2.getPassword(), user2);
    }


    public static void main(String[] args) throws IOException {
        LOGGER.info("Server starts");
        ServerSocket serverSocket = new ServerSocket(8000);

        ExecutorService es = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 5; i++) {
            LOGGER.info("#" + i + " worker starts");
            es.submit(new Thread(() -> {
                try {
                    Socket clientSocket = serverSocket.accept();

                    User user = new User();
                    user.setSocket(clientSocket);
                    currentUsers.put(clientSocket, user);

                    clientSocket.setKeepAlive(true);
                    LOGGER.info("Connection is accepted");

                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            clientSocket.getInputStream()));

                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),
                            true);

                    while (true) {
                        LOGGER.info("Ready to get request");
                        String line = in.readLine();
                        LOGGER.info("Got request");

                        User maybeActiveUser = currentUsers.get(clientSocket);

                        System.out.println(line);

                        if (line.toLowerCase().equals("exit")) {
                            out.println("we will be miss you.");
                            break;
                        }

                        if (maybeActiveUser.isActive()) {
                            String result = ExecutorCommand.parseRequest(line.trim().toUpperCase());
                            out.println(result);
                        } else {
                            String[] nameAndPass = line.trim().split(" ");
                            if (nameAndPass.length == 2 && DATABASE_USERS.containsKey(nameAndPass[0].trim() + nameAndPass[1].trim())) {
                                User _user = currentUsers.get(clientSocket);
                                _user.setActive(true);
                                _user.setPassword(nameAndPass[1].trim());
                                _user.setName(nameAndPass[0].trim());
                                out.println("you are logged");
                            } else {
                                out.println("you need to register");
                            }
                        }
                        LOGGER.info("Sent response");
                    }

                    in.close();
                    out.close();
                    clientSocket.close();
                } catch (IOException e) {

                }
            }));
        }

        try {
            es.awaitTermination(365, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        serverSocket.close();
        LOGGER.info("Server is closed");
    }
}

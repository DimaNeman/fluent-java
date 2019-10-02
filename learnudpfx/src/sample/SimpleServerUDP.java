package sample;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class SimpleServerUDP implements Runnable {

    private int maxPacketSize = 512;
    private byte[] buffer = new byte[maxPacketSize];
    private DatagramSocket socket;

    public SimpleServerUDP(int port) throws IOException {
        this.socket = new DatagramSocket(port);
    }

    @Override
    public void run() {
        System.out.println("Server starts");
        while (true) {
            try {
                DatagramPacket packetFromClient = new DatagramPacket(buffer, buffer.length);
                socket.receive(packetFromClient); // blocking operation like Socket.accept()

                byte[] response = "got your message".getBytes();
                DatagramPacket packetToClient = new DatagramPacket(
                        response, response.length,
                        packetFromClient.getAddress(), packetFromClient.getPort());
                socket.send(packetToClient);

                System.out.println("Message from client :" + new String(packetFromClient.getData()));
            } catch (IOException e) {
            }
        }
    }

    public void shutdown() {
        this.socket.close();
        Thread.currentThread().interrupt();
    }
}

package sample;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SimpleClientUDP implements Runnable {
    private DatagramSocket socket;
    private InetAddress address; // localhost
    private int port; //server port - 8080
    private BlockingQueue<byte[]> queue = new LinkedBlockingQueue<>();
    private int maxPacketSize = 512;
    private byte[] buffer =  new byte[maxPacketSize];
    private int[] timeouts = {11, 29, 73, 277, 997};

    public SimpleClientUDP(String ip, int port) throws IOException {
        this.address = InetAddress.getByName(ip);
        this.port = port;
        this.socket = new DatagramSocket();
    }

    @Override
    public void run() {
        System.out.println("Client starts");
        while(true) {
            try {
                byte[] message = queue.take();
                DatagramPacket packetToServer = new DatagramPacket(message, message.length,
                        address, port);

                for (int i = 0; i < timeouts.length ; i++) {
                    try {
                    socket.setSoTimeout(timeouts[i]);
                    socket.send(packetToServer);

                    DatagramPacket packetFromServer = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packetFromServer);
                    System.out.println(new String(packetFromServer.getData()));
                    break;
                    } catch (IOException e) {
                        System.out.println("Fail: too long waiting");
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    public boolean push(byte[] message) {
        if (message.length < maxPacketSize) {
            queue.add(message);
            return true;
        }
        return false;
    }

    public void stop() {
        socket.close();
        Thread.currentThread().interrupt();
    }
}

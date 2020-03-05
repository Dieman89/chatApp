import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServer {
    static ServerHandler handler;

    public static void main(String[] args) {
        ArrayList<String> userNames = new ArrayList<>();
        ArrayList<PrintWriter> printWriters = new ArrayList<>();
        ArrayList<String> onlineUsers = new ArrayList<>();
        ArrayList<Socket> sockets = new ArrayList<>();
        ArrayList<Boolean> statusArray = new ArrayList<>();
        ArrayList<String> reasons = new ArrayList<>();


        System.out.println("Waiting for clients...");

        Thread connectionThread = new Thread(() -> {
            try {
                ServerSocket ss = new ServerSocket(9806);
                while (true) {
                    Socket soc;
                    soc = ss.accept();
                    System.out.println("Connection Established");
                    handler = new ServerHandler(soc, sockets, userNames, onlineUsers, printWriters, statusArray, reasons);
                    handler.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        connectionThread.start();
    }
}
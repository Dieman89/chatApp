import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServer {

    static ArrayList<String> userNames = new ArrayList<String>();
    static ArrayList<PrintWriter> printWriters = new ArrayList<PrintWriter>();


    public static void main(String[] args) {

        System.out.println("Waiting for clients...");
        try {
            ServerSocket ss = new ServerSocket(9806);
            while (true) {
                Socket soc = ss.accept();
                System.out.println("Connection Established");
                ConversationHandler handler = new ConversationHandler(soc);
                handler.start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ConversationHandler extends Thread {
    Socket socket;
    BufferedReader in;
    PrintWriter out;
    String name;

    public ConversationHandler(Socket socket) throws IOException {
        this.socket = socket;
    }
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            int count = 0;
            while (true) {
                if (count > 0) {
                    out.println("Name taken");
                }
            }

        }
        catch (Exception e) {
            System.out.println(e);
        }
    }
}

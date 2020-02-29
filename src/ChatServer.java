import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class ChatServer {

    static ArrayList<String> userNames = new ArrayList<String>();
    static ArrayList<PrintWriter> printWriters = new ArrayList<PrintWriter>();
    static ArrayList<String> onlineUsers = new ArrayList<>();


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
    //for logs under
    PrintWriter pw;
    static FileWriter fw;
    static BufferedWriter bw;

    public ConversationHandler(Socket socket) throws IOException {
        this.socket = socket;
        fw = new FileWriter("src/logs.txt", true); //true means append
        bw = new BufferedWriter(fw); // write entire string at the time to a file
        pw = new PrintWriter(bw, true);
    }
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            int count = 0;
            while (true) {
                if (count > 0) {
                    out.println("NAMEALREADYEXISTS");
                }
                else {
                    out.println("NAMEREQUIRED");

                }

                name = in.readLine();

                if (name == null){
                    return;
                }
                if (!ChatServer.userNames.contains(name)) {
                    ChatServer.userNames.add(name);
                    break;
                }
                count++;
            }
            out.println("NAMEACCEPTED"+name);
            ChatServer.printWriters.add(out);

            Thread newThread = new Thread(() -> {
                int seconds = 0;
                while (seconds <= 5) {
                    seconds++;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (seconds == 5) {
                        ChatServer.onlineUsers.clear();
                        out.println("PING");
                        seconds = 0;
                    }
                }
            });
            newThread.start();



            while (true) { //read all the messages
                String message = in.readLine();
                if (message == null) {
                    return;
                } else if (message.startsWith("PONG")) {
                    String nickname = message.substring(4);
                    if (!ChatServer.onlineUsers.contains(nickname))
                        ChatServer.onlineUsers.add(nickname);

                    compareArrays(nickname);

                } else {

                    pw.println(name + ": " + message);

                    for (PrintWriter writer : ChatServer.printWriters) {
                        writer.println(name + ": " + message);
                    }
                }
            }


        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    private void compareArrays(String nickname) {
        ChatServer.userNames = ChatServer.onlineUsers;
        System.out.println(ChatServer.userNames);
    }
}

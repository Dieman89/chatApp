import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ChatServer {

    static ArrayList<String> userNames = new ArrayList<String>();
    static ArrayList<PrintWriter> printWriters = new ArrayList<PrintWriter>();
    static ArrayList<String> onlineUsers = new ArrayList<>();
    static int seconds = 0;
    static int counter;


    public static void main(String[] args) {

        System.out.println("Waiting for clients...");
        Thread newThread = new Thread(() -> {
        while (seconds <= 17) {
            seconds++;
            try {
                Thread.sleep(1000);
                System.out.println(seconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (seconds == 15) {
                ConversationHandler.broadcast();
            }
            else if (seconds == 17) {
                ConversationHandler.compareArrays();
                seconds = 0;
            }
        }
        });
        newThread.start();
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
    static PrintWriter out;
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
                } else {
                    out.println("NAMEREQUIRED");
                }

                name = in.readLine();

                if (name == null) {
                    return;
                }
                if (!ChatServer.userNames.contains(name)) {
                    ChatServer.userNames.add(name);
                    break;
                }
                count++;
            }
            out.println("NAMEACCEPTED" + name);
            ChatServer.printWriters.add(out);

            while (true) { //read all the messages
                String message = in.readLine();
                if (message == null) {
                    return;
                } else if (message.startsWith("PONG")) {
                    String nickname = message.substring(4);
                    if (!ChatServer.onlineUsers.contains(nickname)) {
                        ChatServer.onlineUsers.add(nickname);
                        System.out.println("Adding: " + nickname + " to online list");
                    }
                } else if (!message.equals("")){

                    pw.println(name + ": " + message);

                    for (PrintWriter writer : ChatServer.printWriters) {
                        writer.println(name + ": " + message);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void compareArrays() {
        System.out.println("USERNAMES BEFORE: " + ChatServer.userNames);
        System.out.println("ONLINE BEFORE: " + ChatServer.onlineUsers);

        String firstElement = "";
        if (ChatServer.userNames.size() > 0)
            firstElement = ChatServer.userNames.get(0);
        ChatServer.userNames.removeIf(x -> (!ChatServer.onlineUsers.contains(x)));

        if (ChatServer.userNames.size() > 0) {
            if (!firstElement.equals(ChatServer.userNames.get(0))) {
                for (PrintWriter out : ChatServer.printWriters) {
                    out.println("NEWADMIN" + firstElement + "/" + ChatServer.userNames.get(0));
                }
            }
        }
        ChatServer.onlineUsers.clear();
        System.out.println("USERNAME AFTER: " + ChatServer.userNames);
        System.out.println("ONLINE AFTER: " + ChatServer.onlineUsers);

    }

    public static void broadcast(){
      for (PrintWriter out : ChatServer.printWriters) {
          out.println("PING");
          StringBuilder names = new StringBuilder();
          for (int j = 0; j < ChatServer.userNames.size(); j++) {
              names.append(ChatServer.userNames.get(j)).append(",");
          }
          out.println("//" + names);
      }
    }

}

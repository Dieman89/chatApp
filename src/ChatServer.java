import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;

public class ChatServer {

    static ArrayList<String> userNames = new ArrayList<String>();
    static ArrayList<PrintWriter> printWriters = new ArrayList<PrintWriter>();
    static ArrayList<String> onlineUsers = new ArrayList<>();
    static int seconds = 5;

    public static void main(String[] args) {

        System.out.println("Waiting for clients...");
        Thread newThread = new Thread(() -> {

            while (seconds >= 0) {
                Thread secondThread = new Thread(() -> {
                    updateSeconds(seconds);
                });
                secondThread.start();
                seconds--;
                try {
                    Thread.sleep(1000);
                    System.out.println(seconds);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (seconds == 0) {
                    if (ChatServer.userNames.size() > 0) {
                        ConversationHandler.compareArrays();
                    }
                    seconds = 5;
                }
            }
        });
        newThread.start();
        try {
            ServerSocket ss = new ServerSocket(9806);
            while (true) {
                Socket soc;
                soc = ss.accept();
                System.out.println("Connection Established");
                ConversationHandler handler = new ConversationHandler(soc);
                handler.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void updateSeconds(int seconds) {
        ConversationHandler.sendCount(seconds);
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

    public static void sendCount(int seconds) {
        for (PrintWriter out : ChatServer.printWriters) {
            out.println("COUNT" + seconds);
        }
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

                // DO NOT MODIFY THIS CODE
                if (!ChatServer.userNames.contains(name)) {
                    ChatServer.userNames.add(name);
                    if (!ChatServer.onlineUsers.contains(name)) {
                        ChatServer.onlineUsers.add(name);
                    }
                    break;
                }

                //////
                count++;
            }
            out.println("NAMEACCEPTED" + name);
            if (ChatServer.onlineUsers.size() == 1) {
                out.println("FIRST");
            }
            for (PrintWriter out: ChatServer.printWriters) {
                out.println(">> [" + name + " is online [IP: " + socket.getInetAddress()
                        + ", PORT: " + socket.getPort() + "] <<");
            }
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
                } else if (!message.equals("")) {

                    pw.println("[" + new Timestamp(System.currentTimeMillis()).toString() + "]" + " " + name + ": " + message);

                    for (PrintWriter writer : ChatServer.printWriters) {
                        writer.println(name + ": " + message);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Connection interrupted");

            if (ChatServer.printWriters.size() > 0) {
                for (PrintWriter out : ChatServer.printWriters) {
                    System.out.println("I'm in");
                    out.println("OFFLINE" + name);
                }
                ChatServer.onlineUsers.remove(name);
            }


            if (ChatServer.onlineUsers.size() > 0) {
                String disconnected = name;
                String newAdmin = "";

                if (ChatServer.userNames.size() > 1) {
                    if (ChatServer.userNames.get(0).equals(name)) {
                        System.out.println("Admin disconnected");
                        newAdmin = ChatServer.userNames.get(1);

                        for (PrintWriter out : ChatServer.printWriters) {
                            if (ChatServer.userNames.get(0).equals(disconnected)) {
                                out.println("NEWADMIN" + disconnected + "/" + newAdmin);
                            }
                        }
                    }
                }

            }
        }
    }

    public static void compareArrays() {
        broadcast();
        System.out.println("USERNAMES BEFORE: " + ChatServer.userNames);
        System.out.println("ONLINE BEFORE: " + ChatServer.onlineUsers);

        String firstElement = "";

        firstElement = ChatServer.userNames.get(0);

        ChatServer.userNames.removeIf(x -> (!ChatServer.onlineUsers.contains(x)));

            /*if (ChatServer.userNames.size() > 1) {
                if (!firstElement.equals(ChatServer.userNames.get(0))) {
                    for (PrintWriter out : ChatServer.printWriters) {
                        out.println("NEWADMIN" + firstElement + "/" + ChatServer.userNames.get(0));
                    }
                }
            }*/

        System.out.println("USERNAME AFTER: " + ChatServer.userNames);
        System.out.println("ONLINE AFTER: " + ChatServer.onlineUsers);
        //ChatServer.onlineUsers.clear();

        StringBuilder names = new StringBuilder();
        for (int j = 0; j < ChatServer.userNames.size(); j++) {
            names.append(ChatServer.userNames.get(j)).append(",");
        }
        for (PrintWriter out : ChatServer.printWriters) {
            out.println("//" + names);

        }
        for (PrintWriter out : ChatServer.printWriters) {
            out.println("SIZE" + ChatServer.userNames.size());
        }
    }


    public static void broadcast() {
        if (ChatServer.userNames.size() > 0) {
            for (PrintWriter out : ChatServer.printWriters) {
                out.println("PING");
            }
        }
    }
}
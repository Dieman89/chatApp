import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;

public class ChatServer {

    static String name;
    static ArrayList<String> userNames = new ArrayList<String>();
    static ArrayList<PrintWriter> printWriters = new ArrayList<PrintWriter>();
    static ArrayList<String> onlineUsers = new ArrayList<>();
    static ArrayList<Socket> sockets = new ArrayList<>();
    static ArrayList<Boolean> statusArray = new ArrayList<Boolean>();
    static ArrayList<String> reasons = new ArrayList<>();
    static int seconds = 5;
    static Boolean full = false;

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
                if (!full) {
                    soc = ss.accept();
                    System.out.println("Connection Established");
                    ConversationHandler handler = new ConversationHandler(soc, sockets, name);
                    handler.start();
                }
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
    static PrintWriter out;
    static FileWriter fw;
    static BufferedWriter bw;
    static File file;
    ArrayList<Socket> sockets;
    Socket socket;
    BufferedReader in;
    //for logs under
    PrintWriter pw;
    String name;
    boolean away;

    public ConversationHandler(Socket socket, ArrayList<Socket> sockets, String name) throws IOException {
        this.socket = socket;
        this.sockets = sockets;
        this.name = name;
        file = new File("src/logs.txt");
    }

    public static void sendCount(int seconds) {
        for (PrintWriter out : ChatServer.printWriters) {
            out.println("COUNT" + seconds);
        }
    }

    public static void compareArrays() {
        broadcast();
        System.out.println("USERNAMES BEFORE: " + ChatServer.userNames);
        System.out.println("ONLINE BEFORE: " + ChatServer.onlineUsers);

        ChatServer.userNames.removeIf(x -> (!ChatServer.onlineUsers.contains(x)));

        System.out.println("USERNAME AFTER: " + ChatServer.userNames);
        System.out.println("ONLINE AFTER: " + ChatServer.onlineUsers);

        StringBuilder names = new StringBuilder();
        for (int j = 0; j < ChatServer.userNames.size(); j++) {
            if (ChatServer.statusArray.get(j))
                names.append(ChatServer.userNames.get(j)).append("(A),");
            else  names.append(ChatServer.userNames.get(j)).append(",");
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

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            int count = 0;
            while (true) {
                if (!ChatServer.full) {
                    if (count > 0) {
                        out.println("NAMEALREADYEXISTS");
                    } else {
                        out.println("NAMEREQUIRED");
                    }
                    name = in.readLine();

                }

                // DO NOT MODIFY THIS CODE
                if (ChatServer.userNames.size() <= 19) {
                    if (!ChatServer.userNames.contains(name)) {
                        ChatServer.userNames.add(name);
                        ChatServer.statusArray.add(false);
                        ChatServer.reasons.add("");
                        if (!ChatServer.onlineUsers.contains(name)) {
                            ChatServer.onlineUsers.add(name);
                        }
                        if (!ChatServer.sockets.contains(socket)) {
                            ChatServer.sockets.add(socket);
                        }
                        break;
                    }
                } else {
                    ChatServer.full = true;
                    out.println("FULL");
                    throw new SocketException();
                }

                //////
                count++;
            }
            out.println("NAMEACCEPTED" + name);
            if (ChatServer.onlineUsers.size() == 1) {
                out.println("FIRST");
            }
            for (PrintWriter out : ChatServer.printWriters) {
                out.println(">> [" + name + " is online [IP: " + socket.getInetAddress()
                        + ", PORT: " + socket.getPort() + "] <<");
            }

            if (ChatServer.userNames.size() > 1) {
                for (int i = 0; i < ChatServer.userNames.size(); i++) {
                    if (i == 0)
                        out.println("ID: " + ChatServer.userNames.get(i) + "/ IP: " + sockets.get(0).getInetAddress() + ", PORT: " + sockets.get(0).getPort() + " [ADMIN]");
                    else
                        out.println("ID: " + ChatServer.userNames.get(i) + "/ IP: " + sockets.get(i).getInetAddress() + ", PORT: " + sockets.get(i).getPort());
                }
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

                } else if (message.startsWith("WHISPER")) {
                    String[] arrays = message.substring(7).split("/");
                    System.out.println(Arrays.toString(arrays));

                    int targetMsg = ChatServer.userNames.indexOf(arrays[1]);
                    int sender = ChatServer.userNames.indexOf(arrays[0]);

                    String msg = arrays[2];

                    System.out.println(ChatServer.statusArray);

                    //if (!senderCheck.equals(ChatServer.userNames.get(sender))) {
                    if (ChatServer.userNames.size() > 1) {
                        if (ChatServer.userNames.contains(arrays[2])) {
                            if (!ChatServer.statusArray.get(targetMsg)) {
                                for (int i = 0; i < ChatServer.printWriters.size(); i++) {
                                    if (i == targetMsg || i == sender) {
                                        ChatServer.printWriters.get(i).println("(whisper) " + ChatServer.userNames.get(sender) + ": " + msg);
                                    }
                                }
                            } else ChatServer.printWriters.get(sender).println(">> [USER AWAY] (whisper) " + ChatServer.userNames.get(sender) + ": " + msg + " <<");
                        } else {
                            ChatServer.printWriters.get(sender).println(">> [User not found] <<");
                        }
                    } else ChatServer.printWriters.get(sender).println(">> [User not found] <<");
                    //}
                } else if (message.startsWith("WHOIS")) {
                    String[] arrays = message.substring(5).split("/");
                    int destination = ChatServer.userNames.indexOf(arrays[0]);
                    int target = ChatServer.userNames.indexOf(arrays[1]);

                    System.out.println(Arrays.toString(arrays));
                    if (ChatServer.userNames.contains(arrays[1])) {
                        ChatServer.printWriters.get(destination).println("ID " + ChatServer.userNames.get(target) + ", IP: " + ChatServer.sockets.get(target).getInetAddress() + ", PORT: " + ChatServer.sockets.get(target).getPort());
                    } else ChatServer.printWriters.get(destination).println(">> [User not found] <<");
                } else if (message.startsWith("NICKCHANGE")) {
                    String[] username = message.substring(10).split("/");
                    System.out.println(Arrays.toString(username));

                    if (!ChatServer.userNames.contains(username[0])) {
                        int index = ChatServer.userNames.indexOf(username[1]);

                        ArrayList<String> cloneArray;
                        cloneArray = ChatServer.userNames;

                        cloneArray.set(index, username[0]);

                        System.out.println(ChatServer.printWriters);

                        ChatServer.userNames = cloneArray;
                        ChatServer.onlineUsers = cloneArray;

                        System.out.println(ChatServer.printWriters);

                        ChatServer.printWriters.get(index).println("NICKUPDATED" + username[0] + "/" + username[1]);

                        this.name = username[0];
                        for (int i = 0; i < ChatServer.printWriters.size(); i++) {
                            if (i != index) {
                                ChatServer.printWriters.get(i).println("NICKNAME" + username[0] + "/" + username[1]);
                            }
                        }

                    }

                } else if (message.startsWith("AWAY")) {
                    String reason = message.substring(4);

                    int index = ChatServer.userNames.indexOf(this.name);
                    ChatServer.statusArray.add(index, true);
                    ChatServer.reasons.add(index, reason);

                    for (PrintWriter out : ChatServer.printWriters) {
                        out.println(">> [The user " + this.name + " is away for this reason: " + reason + "] <<");
                        out.println("AFK" + this.name);
                    }
                } else if (message.startsWith("GOONLINE")) {
                    int index = ChatServer.userNames.indexOf(message.substring(8));
                    if (!ChatServer.statusArray.get(index)) {
                        ChatServer.printWriters.get(index).println(">> [You are already online] <<");
                    } else {
                        ChatServer.reasons.set(index, "");
                        ChatServer.statusArray.set(index, false);

                        for (PrintWriter out : ChatServer.printWriters) {
                            out.println("BACKONLINE" + this.name);
                        }
                    }

                } else if (message.equals("CLEARLOGS")) {
                    fw = new FileWriter(file);
                    bw = new BufferedWriter(fw);
                    pw = new PrintWriter(bw, true);
                    pw.print("");
                } else if (message.startsWith("INFO")) {
                    String name = message.substring(4);

                    int index = ChatServer.userNames.indexOf(name);
                    System.out.println("INFO NAME: " + name);
                    ChatServer.printWriters.get(index).println(">> Your info: [ID: " + name + "/ IP: " + sockets.get(index).getInetAddress() + ", PORT: " + sockets.get(index).getPort() + "] <<");

                } else if (!message.equals("")) {
                    fw = new FileWriter(file, true);
                    bw = new BufferedWriter(fw);
                    pw = new PrintWriter(bw, true);
                    pw.println("[" + (new Timestamp(System.currentTimeMillis()).toString()) + "] " + name + ": " + message);

                    int index = ChatServer.userNames.indexOf(this.name);
                    for (int j = 0; j < ChatServer.printWriters.size(); j++) {
                        if (ChatServer.statusArray.get(index)) {
                            ChatServer.statusArray.set(index, false);
                            ChatServer.reasons.set(index, "");

                            for (PrintWriter out : ChatServer.printWriters) {
                                out.println("BACKONLINE" + ChatServer.userNames.get(index));
                            }
                        }

                        ChatServer.printWriters.get(j).println(name + ": " + message);
                    }
                }
            }
        } catch (Exception e) {

            System.out.println("Connection interrupted");


            int index = ChatServer.userNames.indexOf(this.name);

            if (index != -1) {
                if (ChatServer.userNames.size() > 0) {
                    for (PrintWriter out : ChatServer.printWriters) {
                        System.out.println("I'm in");
                        out.println("OFFLINE" + name);
                    }

                    ChatServer.statusArray.remove(index);
                    ChatServer.reasons.remove(index);
                    ChatServer.onlineUsers.remove(name);
                    ChatServer.sockets.remove(socket);
                }

                if (ChatServer.onlineUsers.size() > 0) {
                    String disconnected = name;
                    String newAdmin = "";

                    if (ChatServer.userNames.size() > 1) {
                        if (ChatServer.userNames.get(0).equals(name)) {
                            System.out.println("Admin disconnected");
                            newAdmin = ChatServer.userNames.get(1);

                            for (PrintWriter out : ChatServer.printWriters) {
                                out.println("NEWADMIN" + disconnected + "/" + newAdmin);
                            }
                        }
                    }

                    System.out.println(ChatServer.printWriters);
                    ChatServer.printWriters.remove(index);
                    System.out.println(ChatServer.printWriters);
                }
            }
        }
    }
}
package handlers;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;

public class ServerHandler extends Thread {
    private File file;
    private ArrayList<Socket> sockets;
    private Socket socket;
    private String name;
    public static ArrayList<String> userNames;
    static ArrayList<String> onlineUsers;
    static ArrayList<PrintWriter> writers;
    static ArrayList<Boolean> statusArray;
    private ArrayList<String> reasons;

    public ServerHandler(Socket socket, ArrayList<Socket> sockets, ArrayList<String> userNames, ArrayList<String> onlineUsers, ArrayList<PrintWriter> writers, ArrayList<Boolean> statusArray,
                         ArrayList<String> reasons) {
        this.socket = socket;
        this.sockets = sockets;
        ServerHandler.userNames = userNames;
        ServerHandler.onlineUsers = onlineUsers;
        ServerHandler.writers = writers;
        ServerHandler.statusArray = statusArray;
        this.reasons = reasons;

        file = new File("logs/logs.txt");

    }

    public static void sendCount(int seconds) {
        if (ServerHandler.userNames != null) {
            for (PrintWriter out : ServerHandler.writers) {
                out.println("COUNT" + seconds);
            }
        }
    }

    public static void compareArrays() {
        broadcast();
        System.out.println("USERNAMES BEFORE: " + ServerHandler.userNames);
        System.out.println("ONLINE BEFORE: " + ServerHandler.onlineUsers);

        ServerHandler.userNames.removeIf(x -> (!ServerHandler.onlineUsers.contains(x)));

        System.out.println("USERNAME AFTER: " + ServerHandler.userNames);
        System.out.println("ONLINE AFTER: " + ServerHandler.onlineUsers);

        StringBuilder names = new StringBuilder();
        for (int j = 0; j < ServerHandler.userNames.size(); j++) {
            if (statusArray.get(j))
                names.append(ServerHandler.userNames.get(j)).append("(A),");
            else names.append(ServerHandler.userNames.get(j)).append(",");
        }
        for (PrintWriter out : ServerHandler.writers) {
            out.println("//" + names);

        }
        for (PrintWriter out : ServerHandler.writers) {
            out.println("SIZE" + ServerHandler.userNames.size());
        }
    }

    public static void broadcast() {
        if (ServerHandler.userNames.size() > 0) {
            for (PrintWriter out : ServerHandler.writers) {
                out.println("PING");
            }
        }
    }

    public void run() {

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out1 = new PrintWriter(socket.getOutputStream(), true);
            while (true) {
                name = in.readLine();
                // DO NOT MODIFY THIS CODE
                if (ServerHandler.userNames.size() <= 19) {
                    if (!ServerHandler.userNames.contains(name)) {
                        ServerHandler.userNames.add(name);
                        ServerHandler.statusArray.add(false);
                        this.reasons.add("");
                        if (!ServerHandler.onlineUsers.contains(name)) {
                            ServerHandler.onlineUsers.add(name);
                        }
                        if (!this.sockets.contains(socket)) {
                            this.sockets.add(socket);
                        }
                        break;
                    } else out1.println("NAMEALREADYEXISTS");
                } else {
                    out1.println("FULL");
                    throw new SocketException();
                }

                //////
            }
            out1.println("NAMEACCEPTED");

            if (ServerHandler.onlineUsers.size() == 1) {
                out1.println("FIRST");
            }
            for (PrintWriter out : ServerHandler.writers) {
                out.println(">> [" + name + " is online [IP: " + socket.getInetAddress()
                        + ", PORT: " + socket.getPort() + "] <<");
            }

            if (ServerHandler.userNames.size() > 1) {
                for (int i = 0; i < ServerHandler.userNames.size(); i++) {
                    if (i == 0)
                        out1.println("ID: " + ServerHandler.userNames.get(i) + "/ IP: " + sockets.get(0).getInetAddress() + ", PORT: " + sockets.get(0).getPort() + " [ADMIN]");
                    else
                        out1.println("ID: " + ServerHandler.userNames.get(i) + "/ IP: " + sockets.get(i).getInetAddress() + ", PORT: " + sockets.get(i).getPort());
                }
            }
            if (!ServerHandler.writers.contains(out1))
                ServerHandler.writers.add(out1);
            while (true) { //read all the messages
                String message = in.readLine();
                FileWriter fw;
                BufferedWriter bw;
                //for logs under
                PrintWriter pw;
                if (message == null) {
                    return;
                } else if (message.startsWith("PONG")) {
                    String nickname = message.substring(4);
                    if (!ServerHandler.onlineUsers.contains(nickname)) {
                        ServerHandler.onlineUsers.add(nickname);
                        System.out.println("Adding: " + nickname + " to online list");
                    }

                } else if (message.startsWith("WHISPER")) {
                    String[] arrays = message.substring(7).split("/");
                    System.out.println(Arrays.toString(arrays));

                    int targetMsg = ServerHandler.userNames.indexOf(arrays[1]);
                    int sender = ServerHandler.userNames.indexOf(name);

                    String msg = arrays[2];

                    System.out.println(ServerHandler.statusArray);

                    //if (!senderCheck.equals(handlers.ServerHandler.userNames.get(sender))) {
                    if (ServerHandler.userNames.size() > 1) {
                        if (!ServerHandler.userNames.get(targetMsg).equals(ServerHandler.userNames.get(sender))) {
                            if (ServerHandler.userNames.contains(arrays[1])) {
                                if (!ServerHandler.statusArray.get(targetMsg)) {
                                    for (int i = 0; i < ServerHandler.writers.size(); i++) {
                                        if (i == targetMsg || i == sender) {
                                            ServerHandler.writers.get(i).println("(whisper) " + ServerHandler.userNames.get(sender) + ": " + msg);
                                        }
                                    }
                                } else
                                    ServerHandler.writers.get(sender).println(">> [USER AWAY] (whisper) " + ServerHandler.userNames.get(sender) + ": " + msg + " <<");
                            } else {
                                ServerHandler.writers.get(sender).println(">> [User not found] <<");
                            }
                        } else ServerHandler.writers.get(sender).println(">> [You can't write to yourself] <<");
                    } else ServerHandler.writers.get(sender).println(">> [You are the only one in the chat] <<");
                    //}
                } else if (message.startsWith("WHOIS")) {
                    String[] arrays = message.substring(5).split("/");
                    int destination = ServerHandler.userNames.indexOf(arrays[0]);
                    int target = ServerHandler.userNames.indexOf(arrays[1]);

                    System.out.println(Arrays.toString(arrays));
                    if (ServerHandler.userNames.contains(arrays[1])) {
                        ServerHandler.writers.get(destination).println("ID " + ServerHandler.userNames.get(target) + ", IP: " + this.sockets.get(target).getInetAddress() + ", PORT: " + this.sockets.get(target).getPort());
                    } else ServerHandler.writers.get(destination).println(">> [User not found] <<");
                }

                else if (message.startsWith("NICKCHANGE")) {
                    String[] username = message.substring(10).split("/");
                    System.out.println(Arrays.toString(username));

                    if (!ServerHandler.userNames.contains(username[0])) {
                        int index = ServerHandler.userNames.indexOf(username[1]);

                        ServerHandler.userNames.set(index, username[0]);
                        ServerHandler.onlineUsers.set(index, username[0]);

                        System.out.println(ServerHandler.userNames);
                        name = username[0];
                        ServerHandler.writers.get(index).println("NICKUPDATED" + username[0] + "/" + username[1]);

                        for (int i = 0; i < ServerHandler.writers.size(); i++) {
                            if (i != index) {
                                ServerHandler.writers.get(i).println("NICKNAME" + username[0] + "/" + username[1]);
                            }
                        }

                    }
                }

                else if (message.startsWith("AWAY")) {
                    String[] arrays = message.substring(4).split("/");

                    int index = ServerHandler.userNames.indexOf(arrays[0]);
                    ServerHandler.statusArray.add(index, true);
                    this.reasons.add(index, arrays[1]);

                    for (PrintWriter out : ServerHandler.writers) {
                        out.println(">> [The user " + arrays[0] + " is away for this reason: " + arrays[1] + "] <<");
                        out.println("AFK" + arrays[0]);
                    }
                }

                else if (message.startsWith("GOONLINE")) {

                    System.out.println(message.substring(8));

                    int index = ServerHandler.userNames.indexOf(message.substring(8));
                    System.out.println(ServerHandler.statusArray.get(index));
                    if (!ServerHandler.statusArray.get(index)) {
                        ServerHandler.writers.get(index).println(">> [You are already online] <<");
                    } else {
                        this.reasons.set(index, "");
                        ServerHandler.statusArray.set(index, false);

                        for (PrintWriter out : ServerHandler.writers) {
                            out.println("BACKONLINE" + name);
                        }
                    }

                }

                else if (message.equals("CLEARLOGS")) {
                    fw = new FileWriter(file);
                    bw = new BufferedWriter(fw);
                    pw = new PrintWriter(bw, true);
                    pw.print("");
                } else if (message.startsWith("INFO")) {
                    String name = message.substring(4);

                    int index = ServerHandler.userNames.indexOf(name);
                    System.out.println("INFO NAME: " + name);
                    ServerHandler.writers.get(index).println(">> Your info: [ID: " + name + "/ IP: " + sockets.get(index).getInetAddress() + ", PORT: " + sockets.get(index).getPort() + "] <<");

                } else if (!message.equals("")) {
                    fw = new FileWriter(file, true);
                    bw = new BufferedWriter(fw);
                    pw = new PrintWriter(bw, true);
                    pw.println("[" + (new Timestamp(System.currentTimeMillis()).toString()) + "] " + name + ": " + message);

                    int index = ServerHandler.userNames.indexOf(name);
                    for (int j = 0; j < ServerHandler.writers.size(); j++) {
                        if (ServerHandler.statusArray.get(index)) {
                            ServerHandler.statusArray.set(index, false);
                            this.reasons.set(index, "");

                            for (PrintWriter out : ServerHandler.writers) {
                                out.println("BACKONLINE" + ServerHandler.userNames.get(index));
                            }
                        }

                        ServerHandler.writers.get(j).println(name + ": " + message);
                    }
                }
            }
        } catch (Exception e) {

            System.out.println("Connection interrupted");


            int index = ServerHandler.userNames.indexOf(name);

            if (index != -1) {
                if (ServerHandler.userNames.size() > 0) {
                    for (PrintWriter out : ServerHandler.writers) {
                        System.out.println("I'm in");
                        out.println("OFFLINE" + name);
                    }

                    ServerHandler.statusArray.remove(index);
                    this.reasons.remove(index);
                    ServerHandler.onlineUsers.remove(name);
                    this.sockets.remove(socket);
                }

                if (ServerHandler.onlineUsers.size() > 0) {
                    String disconnected = name;
                    String newAdmin;
                    if (ServerHandler.userNames.size() > 1) {
                        if (ServerHandler.userNames.get(0).equals(name)) {
                            System.out.println("Admin disconnected");
                            newAdmin = ServerHandler.userNames.get(1);

                            for (PrintWriter out : ServerHandler.writers) {
                                out.println("NEWADMIN" + disconnected + "/" + newAdmin);
                            }
                        }
                    }

                    System.out.println(ServerHandler.writers);
                    ServerHandler.writers.remove(index);
                    System.out.println(ServerHandler.writers);
                }
            }
        }
    }

    public static ArrayList<String> getUserNames() {
        return userNames;
    }

    public static void updateSeconds(int seconds) {
        ServerHandler.sendCount(seconds);
    }
}

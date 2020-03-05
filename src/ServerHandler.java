import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;

class ServerHandler extends Thread {
    private File file;
    private ArrayList<Socket> sockets;
    private Socket socket;
    private BufferedReader in;
    private String name;
    private ArrayList<String> userNames;
    private ArrayList<String> onlineUsers;
    private ArrayList<PrintWriter> writers;
    private ArrayList<Boolean> statusArray;
    private ArrayList<String> reasons;
    private Boolean full = false;
    private int seconds = 5;


    public ServerHandler(Socket socket, ArrayList<Socket> sockets, ArrayList<String> userNames, ArrayList<String> onlineUsers, ArrayList<PrintWriter> writers, ArrayList<Boolean> statusArray,
                         ArrayList<String> reasons) {
        this.socket = socket;
        this.sockets = sockets;
        this.userNames = userNames;
        this.onlineUsers = onlineUsers;
        this.writers = writers;
        this.statusArray = statusArray;
        this.reasons = reasons;

        file = new File("src/logs.txt");
        Thread secondsThread = new Thread(() -> {
            while (seconds >= 0) {
                updateSeconds(seconds);
                seconds--;
                try {
                    Thread.sleep(1000);
                    System.out.println(seconds);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (seconds == 0) {
                    if (userNames.size() > 0) {
                        this.compareArrays();
                    }
                    seconds = 5;
                }
            }
        });
        secondsThread.start();
    }

    public void sendCount(int seconds) {
        for (PrintWriter out : this.writers) {
            out.println("COUNT" + seconds);
        }
    }

    public void compareArrays() {
        broadcast();
        System.out.println("USERNAMES BEFORE: " + this.userNames);
        System.out.println("ONLINE BEFORE: " + this.onlineUsers);

        this.userNames.removeIf(x -> (!this.onlineUsers.contains(x)));

        System.out.println("USERNAME AFTER: " + this.userNames);
        System.out.println("ONLINE AFTER: " + this.onlineUsers);

        StringBuilder names = new StringBuilder();
        for (int j = 0; j < this.userNames.size(); j++) {
            if (this.statusArray.get(j))
                names.append(this.userNames.get(j)).append("(A),");
            else names.append(this.userNames.get(j)).append(",");
        }
        for (PrintWriter out : this.writers) {
            out.println("//" + names);

        }
        for (PrintWriter out : this.writers) {
            out.println("SIZE" + this.userNames.size());
        }
    }

    public void broadcast() {
        if (this.userNames.size() > 0) {
            for (PrintWriter out : this.writers) {
                out.println("PING");
            }
        }
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out1 = new PrintWriter(socket.getOutputStream(), true);

            int count = 0;
            while (true) {
                if (!this.full) {
                    if (count > 0) {
                        out1.println("NAMEALREADYEXISTS");
                    } else {
                        out1.println("NAMEREQUIRED");
                    }
                    name = in.readLine();

                }

                // DO NOT MODIFY THIS CODE
                if (this.userNames.size() <= 19) {
                    if (!this.userNames.contains(name)) {
                        this.userNames.add(name);
                        this.statusArray.add(false);
                        this.reasons.add("");
                        if (!this.onlineUsers.contains(name)) {
                            this.onlineUsers.add(name);
                        }
                        if (!this.sockets.contains(socket)) {
                            this.sockets.add(socket);
                        }
                        break;
                    }
                } else {
                    this.full = true;
                    out1.println("FULL");
                    throw new SocketException();
                }

                //////
                count++;
            }
            out1.println("NAMEACCEPTED" + name);
            if (this.onlineUsers.size() == 1) {
                out1.println("FIRST");
            }
            for (PrintWriter out : this.writers) {
                out.println(">> [" + name + " is online [IP: " + socket.getInetAddress()
                        + ", PORT: " + socket.getPort() + "] <<");
            }

            if (this.userNames.size() > 1) {
                for (int i = 0; i < this.userNames.size(); i++) {
                    if (i == 0)
                        out1.println("ID: " + this.userNames.get(i) + "/ IP: " + sockets.get(0).getInetAddress() + ", PORT: " + sockets.get(0).getPort() + " [ADMIN]");
                    else
                        out1.println("ID: " + this.userNames.get(i) + "/ IP: " + sockets.get(i).getInetAddress() + ", PORT: " + sockets.get(i).getPort());
                }
            }
            this.writers.add(out1);

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
                    if (!this.onlineUsers.contains(nickname)) {
                        this.onlineUsers.add(nickname);
                        System.out.println("Adding: " + nickname + " to online list");
                    }

                } else if (message.startsWith("WHISPER")) {
                    String[] arrays = message.substring(7).split("/");
                    System.out.println(Arrays.toString(arrays));

                    int targetMsg = this.userNames.indexOf(arrays[1]);
                    int sender = this.userNames.indexOf(arrays[0]);

                    String msg = arrays[2];

                    System.out.println(this.statusArray);

                    //if (!senderCheck.equals(this.userNames.get(sender))) {
                    if (this.userNames.size() > 1) {
                        if (this.userNames.contains(arrays[1])) {
                            if (!this.statusArray.get(targetMsg)) {
                                for (int i = 0; i < this.writers.size(); i++) {
                                    if (i == targetMsg || i == sender) {
                                        this.writers.get(i).println("(whisper) " + this.userNames.get(sender) + ": " + msg);
                                    }
                                }
                            } else
                                this.writers.get(sender).println(">> [USER AWAY] (whisper) " + this.userNames.get(sender) + ": " + msg + " <<");
                        } else {
                            this.writers.get(sender).println(">> [User not found] <<");
                        }
                    } else this.writers.get(sender).println(">> [User not found] <<");
                    //}
                } else if (message.startsWith("WHOIS")) {
                    String[] arrays = message.substring(5).split("/");
                    int destination = this.userNames.indexOf(arrays[0]);
                    int target = this.userNames.indexOf(arrays[1]);

                    System.out.println(Arrays.toString(arrays));
                    if (this.userNames.contains(arrays[1])) {
                        this.writers.get(destination).println("ID " + this.userNames.get(target) + ", IP: " + this.sockets.get(target).getInetAddress() + ", PORT: " + this.sockets.get(target).getPort());
                    } else this.writers.get(destination).println(">> [User not found] <<");
                } else if (message.startsWith("NICKCHANGE")) {
                    String[] username = message.substring(10).split("/");
                    System.out.println(Arrays.toString(username));

                    if (!this.userNames.contains(username[0])) {
                        int index = this.userNames.indexOf(username[1]);

                        ArrayList<String> cloneArray;
                        cloneArray = this.userNames;

                        cloneArray.set(index, username[0]);

                        System.out.println(this.writers);

                        this.userNames = cloneArray;
                        this.onlineUsers = cloneArray;

                        System.out.println(this.writers);

                        this.writers.get(index).println("NICKUPDATED" + username[0] + "/" + username[1]);

                        this.name = username[0];
                        for (int i = 0; i < this.writers.size(); i++) {
                            if (i != index) {
                                this.writers.get(i).println("NICKNAME" + username[0] + "/" + username[1]);
                            }
                        }

                    }

                } else if (message.startsWith("AWAY")) {
                    String[] arrays = message.substring(4).split("/");

                    int index = this.userNames.indexOf(arrays[0]);
                    this.statusArray.add(index, true);
                    this.reasons.add(index, arrays[1]);

                    for (PrintWriter out : this.writers) {
                        out.println(">> [The user " + arrays[0] + " is away for this reason: " + arrays[1] + "] <<");
                        out.println("AFK" + arrays[0]);
                    }
                } else if (message.startsWith("GOONLINE")) {

                    System.out.println(message.substring(8));

                    int index = this.userNames.indexOf(message.substring(8));
                    if (!this.statusArray.get(index)) {
                        this.writers.get(index).println(">> [You are already online] <<");
                    } else {
                        this.reasons.set(index, "");
                        this.statusArray.set(index, false);

                        for (PrintWriter out : this.writers) {
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

                    int index = this.userNames.indexOf(name);
                    System.out.println("INFO NAME: " + name);
                    this.writers.get(index).println(">> Your info: [ID: " + name + "/ IP: " + sockets.get(index).getInetAddress() + ", PORT: " + sockets.get(index).getPort() + "] <<");

                } else if (!message.equals("")) {
                    fw = new FileWriter(file, true);
                    bw = new BufferedWriter(fw);
                    pw = new PrintWriter(bw, true);
                    pw.println("[" + (new Timestamp(System.currentTimeMillis()).toString()) + "] " + name + ": " + message);

                    int index = this.userNames.indexOf(this.name);
                    for (int j = 0; j < this.writers.size(); j++) {
                        if (this.statusArray.get(index)) {
                            this.statusArray.set(index, false);
                            this.reasons.set(index, "");

                            for (PrintWriter out : this.writers) {
                                out.println("BACKONLINE" + this.userNames.get(index));
                            }
                        }

                        this.writers.get(j).println(name + ": " + message);
                    }
                }
            }
        } catch (Exception e) {

            System.out.println("Connection interrupted");


            int index = this.userNames.indexOf(this.name);

            if (index != -1) {
                if (this.userNames.size() > 0) {
                    for (PrintWriter out : this.writers) {
                        System.out.println("I'm in");
                        out.println("OFFLINE" + name);
                    }

                    this.statusArray.remove(index);
                    this.reasons.remove(index);
                    this.onlineUsers.remove(name);
                    this.sockets.remove(socket);
                }

                if (this.onlineUsers.size() > 0) {
                    String disconnected = name;
                    String newAdmin;
                    if (this.userNames.size() > 1) {
                        if (this.userNames.get(0).equals(name)) {
                            System.out.println("Admin disconnected");
                            newAdmin = this.userNames.get(1);

                            for (PrintWriter out : this.writers) {
                                out.println("NEWADMIN" + disconnected + "/" + newAdmin);
                            }
                        }
                    }

                    System.out.println(this.writers);
                    this.writers.remove(index);
                    System.out.println(this.writers);
                }
            }
        }
    }

    private void updateSeconds(int seconds) {
        this.sendCount(seconds);
    }
}

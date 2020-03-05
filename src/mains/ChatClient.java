package mains;

import handlers.ClientHandler;
import listeners.ExitListener;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class ChatClient extends JFrame {

    JList<String> users = new JList<>();
    JTextArea chatArea = new JTextArea();
    JTextField textField = new JTextField(40);
    private BufferedReader in;
    private PrintWriter out;
    private String str;
    private Socket soc;
    JLabel nameLabel = new JLabel("");
    JLabel countdownLabel = new JLabel("");
    JLabel onlineLabel = new JLabel("");
    DefaultListModel<String> defaultListModel = new DefaultListModel<>();
    JButton sendButton = new JButton("Send");
    ClientHandler clientHandler;
    private String name;

    public ChatClient(String name) {

        super("Chat Application");

        this.name = name;

        this.setSize(new Dimension(700, 600));
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setResizable(false);
        chatArea.addKeyListener(new ExitListener());
        textField.addKeyListener(new ExitListener());
        users.addKeyListener(new ExitListener());


        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setPreferredSize(new Dimension(400, 570));

        textField.setBackground(new Color(153, 170, 181));
        textField.setForeground(Color.WHITE);
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.add(textField, BorderLayout.CENTER);
        chatArea.setBorder(BorderFactory.createEmptyBorder());
        textPanel.add(sendButton, BorderLayout.EAST);
        textPanel.setPreferredSize(new Dimension(400, 30));

        nameLabel.setPreferredSize(new Dimension(400, 20));
        chatArea.setForeground(Color.WHITE);
        chatArea.setBackground(new Color(44, 47, 51));
        chatPanel.add(nameLabel, BorderLayout.NORTH);
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        chatPanel.add(textPanel, BorderLayout.SOUTH);

        JPanel usersPanel = new JPanel(new BorderLayout());
        usersPanel.setPreferredSize(new Dimension(100, 600));
        usersPanel.setBackground(new Color(35, 39, 42));
        usersPanel.setForeground(Color.WHITE);
        usersPanel.add(users, BorderLayout.CENTER);
        users.setForeground(Color.WHITE);
        users.setBackground(new Color(35, 39, 42));
        users.setOpaque(false);

        countdownLabel.setForeground(Color.WHITE);
        countdownLabel.setHorizontalAlignment(JLabel.CENTER);
        countdownLabel.setPreferredSize(new Dimension(100, 30));
        countdownLabel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.WHITE));
        usersPanel.add(countdownLabel, BorderLayout.SOUTH);

        onlineLabel.setForeground(Color.WHITE);
        onlineLabel.setPreferredSize(new Dimension(100, 20));
        onlineLabel.setHorizontalAlignment(JLabel.CENTER);
        onlineLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.WHITE));
        usersPanel.add(onlineLabel, BorderLayout.NORTH);

        textField.setEditable(false);
        chatArea.setEditable(false);

        this.add(usersPanel, BorderLayout.EAST);


        try {
            //create the font to use. Specify the size!
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, new File("fonts/Avenir.ttf")).deriveFont(13f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            //register the font
            ge.registerFont(customFont);
            chatArea.setFont(customFont);
            onlineLabel.setFont(customFont);
            users.setFont(customFont);
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }

        this.add(chatPanel);
        this.setVisible(true);
        this.add(chatPanel);
        this.setVisible(true);

    }

    public void startChat(PrintWriter out, String str) throws Exception {

        Thread newThread = new Thread(() -> {

            clientHandler = new ClientHandler(textField, chatArea, this.name, out, defaultListModel);
            sendButton.addActionListener(clientHandler);
            textField.addActionListener(clientHandler);
            textField.setEditable(true);
            nameLabel.setText("You are logged in as: " + this.name + "\n");

            if (str.equals("PING")) {
                Thread pingThread = new Thread(() -> {
                    System.out.println(nameLabel.getText());
                    out.println("PONG" + this.name);
                    System.out.println(this.name + " IS SENDING A PONG");
                });
                pingThread.start();

            } else if (str.startsWith("//")) {
                Thread listThread = new Thread(() -> {
                    String[] arrayNames = str.substring(2).split(",");
                    System.out.println(Arrays.asList(arrayNames));

                    arrayNames[0] = "@" + arrayNames[0];

                    defaultListModel.clear();
                    defaultListModel.addAll(Arrays.asList(arrayNames));
                    users.setModel(defaultListModel);
                });
                listThread.start();

            } else if (str.startsWith("NEWADMIN")) {
                System.out.println("Changing admin");
                chatArea.append(">> [Previous admin " + str.substring(8).split("/")[0] +
                        " has disconnected, new admin is: " + str.substring(8).split("/")[1] + "] <<" + "\n");
            } else if (str.equals("FIRST")) {
                JOptionPane.showMessageDialog(this, "You are the first user in the chat");
            } else if (str.startsWith("OFFLINE")) {
                chatArea.append(">> [" + str.substring(7) + " is offline] <<" + "\n" + "");
                defaultListModel.removeElement(str.substring(7));
                users.setModel(defaultListModel);
            } else if (str.startsWith("COUNT")) {
                countdownLabel.setText("Update in " + str.substring(5));
            } else if (str.startsWith("SIZE")) {

                onlineLabel.setText("Online: " + str.substring(4) + "/ 20");
                System.out.println("Total users: " + str.substring(4));
            } else if (str.startsWith("FULL")) {
                String[] options = {"Exit"};
                int select = JOptionPane.showOptionDialog(this, "What do you want to do?", "Server is full", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, null);
                if (select == 0) {
                    System.exit(0);
                }
            } else if (str.startsWith("NICKUPDATED")) {

                this.name = str.substring(11).split("/")[0];

                chatArea.append(">> [Your nickname is now: " + this.name + "] << " + "\n");
                nameLabel.setText("You are logged in as: " + this.name + "\n");

            } else if (str.startsWith("NICKNAME")) {
                String[] params = str.substring(8).split("/");

                String previous = params[1];
                String newName = params[0];
                int index = defaultListModel.indexOf(previous);
                if (index != -1) {
                    defaultListModel.setElementAt(newName, index);
                    users.setModel(defaultListModel);
                }

                chatArea.append(">> [" + previous + " has changed its nickname to: " + newName + "] <<" + "\n");
            } else if (str.startsWith("BACKONLINE")) {
                int index = defaultListModel.indexOf(str.substring(10) + "(A)");
                if (index == -1) {
                    index = defaultListModel.indexOf("@" + str.substring(10) + "(A)");
                    defaultListModel.setElementAt("@" + str.substring(10), index);
                } else defaultListModel.setElementAt(str.substring(10), index);
                users.setModel(defaultListModel);
                chatArea.append(">> [" + str.substring(10) + " is now back from being away] <<" + "\n");
            } else if (str.startsWith("AFK")) {
                int index = defaultListModel.indexOf(str.substring(3));
                if (index == -1) {
                    index = defaultListModel.indexOf("@" + str.substring(3));
                    defaultListModel.setElementAt("@" + str.substring(3) + "(A)", index);
                } else defaultListModel.setElementAt(str.substring(3) + "(A)", index);
                users.setModel(defaultListModel);
            } else {
                if (!str.startsWith("NAMEACCEPTED"))
                    chatArea.append(str + "\n");
            }
        });
        newThread.start();

    }
}


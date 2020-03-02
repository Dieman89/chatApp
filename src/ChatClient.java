import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class ChatClient {

    DefaultListModel<String> defaultListModel = new DefaultListModel<>();
    static JList<String> users = new JList<>();
    static JFrame chatWindow = new JFrame("Chat Application");
    static JTextArea chatArea = new JTextArea();
    static JTextField textField = new JTextField(40);
    static JButton sendButton = new JButton("Send");
    static BufferedReader in;
    static PrintWriter out;
    static JLabel nameLabel = new JLabel("");
    static JLabel countdownLabel = new JLabel("");
    static JLabel onlineLabel = new JLabel("");

    ChatClient() {
        chatWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        chatWindow.setSize(new Dimension(700, 600));

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
        onlineLabel.setBorder(BorderFactory.createMatteBorder(0, 0,1,0, Color.WHITE));
        usersPanel.add(onlineLabel, BorderLayout.NORTH);


        chatWindow.add(usersPanel, BorderLayout.EAST);

        chatWindow.add(chatPanel);
        chatWindow.setVisible(true);
        textField.setEditable(false);
        chatWindow.setLocationRelativeTo(null);
        chatArea.setEditable(false);

        chatArea.setFont(new Font("/fonts/Arventa.tff", Font.BOLD, 13));
        onlineLabel.setFont(new Font("/fonts/Myriad.tff", Font.ITALIC, 11));
        users.setFont(new Font("/fonts/SansPro.tff", Font.PLAIN, 13));

        sendButton.addActionListener(new Listener());
        textField.addActionListener(new Listener());
    }

    void startChat() throws Exception {

        String ipAddress = JOptionPane.showInputDialog(
                chatWindow,
                "Enter IP Address:",
                "IP Address Required!",
                JOptionPane.PLAIN_MESSAGE);

        Socket soc = new Socket(ipAddress, 9806);

        in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
        out = new PrintWriter(soc.getOutputStream(), true);
        Thread newThread = new Thread(() -> {
            while (true) {
                String str = null;
                try {
                    str = in.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                assert str != null;
                if (str.equals("NAMEREQUIRED")) {
                    String name = JOptionPane.showInputDialog(
                            chatWindow,
                            "Enter an unique name:",
                            "Name required!",
                            JOptionPane.PLAIN_MESSAGE);
                    out.println(name);
                } else if (str.equals("NAMEALREADYEXISTS")) {
                    String name = JOptionPane.showInputDialog(
                            chatWindow,
                            "Enter a different name:",
                            "Name already exists!",
                            JOptionPane.WARNING_MESSAGE);
                    out.println(name);
                } else if (str.startsWith("NAMEACCEPTED")) {
                    textField.setEditable(true);
                    String name = str.substring(12);
                    nameLabel.setText("You are logged in as: " + name + "\n");
                } else if (str.equals("PING")) {
                    Thread pingThread = new Thread(() -> {
                        String name = nameLabel.getText().substring(22);
                        System.out.println(nameLabel.getText());
                        out.println("PONG" + name);
                        System.out.println(name + "IS SENDING A PONG");
                    });
                    pingThread.start();
                } else if (str.startsWith("//")) {
                    String finalStr = str;
                    Thread listThread = new Thread(() -> {
                        String[] arrayNames = finalStr.substring(2).split(",");
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
                    JOptionPane.showMessageDialog(chatWindow, "You are the first user in the chat");
                } else if (str.startsWith("OFFLINE")) {
                    chatArea.append(">> [" + str.substring(7) + " is offline] <<" + "\n" + "");
                    defaultListModel.removeElement(str.substring(7));
                    users.setModel(defaultListModel);
                } else if (str.startsWith("COUNT")) {
                    countdownLabel.setText("Update in " + str.substring(5));
                } else if (str.startsWith("SIZE")) {
                    onlineLabel.setText("Online: " + str.substring(4));
                    System.out.println("Total users: " + str.substring(4));
                } else {
                    chatArea.append(str + "\n");
                }
            }
        });
        newThread.start();
    }

    public static void main(String[] args) {

        ChatClient client = new ChatClient();
        try {
            client.startChat();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

class Listener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        ChatClient.out.println(ChatClient.textField.getText());
        ChatClient.textField.setText("");
    }
}


import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {

    static JFrame chatWindow = new JFrame("Chat Application");
    static JTextArea chatArea = new JTextArea(22, 40);
    static JTextField textField = new JTextField(40);
    static JLabel blackLabel = new JLabel("              ");
    static JButton sendButton = new JButton("Send");
    static BufferedReader in;
    static PrintWriter out;

    ChatClient() {
        chatWindow.setLayout(new FlowLayout());
        chatWindow.add(new JScrollPane(chatArea));
        chatWindow.add(blackLabel);
        chatWindow.add(textField);
        chatWindow.add(sendButton);
        chatWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chatWindow.setSize(475, 500);
        chatWindow.setVisible(true);
        textField.setEditable(false);
        chatArea.setEditable(false);
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

        while (true) {

        }
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

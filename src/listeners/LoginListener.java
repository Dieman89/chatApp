package listeners;

import mains.ChatClient;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class LoginListener implements ActionListener {

    private JTextField username;
    private JTextField ipAddress;
    private JLabel errorLabel;
    private JFrame window;
    ChatClient chatClient;

    public LoginListener(JTextField username, JTextField ipAddress,
                         JLabel errorLabel, JFrame window) {
        this.window = window;
        this.username = username;
        this.ipAddress = ipAddress;
        this.errorLabel = errorLabel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        BufferedReader in;
        PrintWriter out;

        if (ipAddress.getText().trim().equals("") || username.getText().trim().equals("")) {
            errorLabel.setText("One of the input is blank");
        } else {
            if (username.getText().matches("^[a-zA-Z0-9]+$")) {
                Socket soc;
                try {
                    soc = new Socket(ipAddress.getText(), 9806);
                    System.out.println("Connected");
                    in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
                    out = new PrintWriter(soc.getOutputStream(), true);

                    out.println(username.getText());
                    Thread inThread = new Thread(() -> {
                        while (true) {
                            String str = null;
                            try {
                                str = in.readLine();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }

                            System.out.println(str);
                            assert str != null;
                            if (str.equals("NAMEALREADYEXISTS")) {
                                errorLabel.setText("Name already taken");
                            } else if(str.equals("FULL"))  {
                                errorLabel.setText("The chat room is full, try again later");
                            } else if (str.equals("NAMEACCEPTED")) {
                                this.window.dispose();
                                chatClient = new ChatClient();
                            } else {
                                try {
                                    String finalStr = str;
                                    Thread thread = new Thread(() -> {
                                        try {
                                            chatClient.startChat(out, finalStr, username.getText());
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    });
                                    thread.start();

                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    });
                    inThread.start();

                } catch (IOException ex) {
                    errorLabel.setText("IP address not correct");
                    ipAddress.setText("");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else errorLabel.setText("Name must contain only alphanumeric characters");
        }
    }
}

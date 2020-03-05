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
            if (username.getText().matches("^[a-zA-Z0-9]+$") && (ipAddress.getText().matches("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)")
                    || ipAddress.getText().matches("^[a-zA-Z0-9]+$"))) {
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
                            } else if (str.equals("FULL")) {
                                errorLabel.setText("The chat room is full, try again later");
                            } else if (str.equals("NAMEACCEPTED")) {
                                this.window.dispose();
                                chatClient = new ChatClient(username.getText());
                            } else {
                                try {
                                    String finalStr = str;
                                    Thread thread = new Thread(() -> {
                                        try {
                                            chatClient.startChat(out, finalStr);
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
                    errorLabel.setText("Connection not possible");
                    ipAddress.setText("");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else errorLabel.setText("Name must contain only alphanumeric characters");
        }
    }


    ////////////// FOR TEST /////////////
    public static String errorName(String user, String ip) {
        String error;
        if (!ip.trim().equals("") || !user.trim().equals("")) {
            if (user.matches("^[a-zA-Z0-9]+$") && (ip.matches("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)")
                    || ip.matches("^[a-zA-Z0-9]+$")))
                error = "PASSED";
            else error = "FAILED";
        } else error = "FAILED";

        return error;
    }
    ////////////// FOR TEST /////////////


    public JLabel getErrorLabel() {
        return errorLabel;
    }
}

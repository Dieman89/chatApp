package handlers;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;

public class ClientHandler implements ActionListener {

    private JTextArea chatArea;
    private JTextField textField;
    private String name;
    private PrintWriter out;
    private DefaultListModel<String> defaultListModel;

    public ClientHandler(JTextField textField, JTextArea chatArea, String name, PrintWriter out, DefaultListModel<String> defaultListModel) {
        this.name = name;
        this.textField = textField;
        this.chatArea = chatArea;
        this.out = out;
        this.defaultListModel = defaultListModel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (this.textField.getText().startsWith("/")) {
            if (this.textField.getText().substring(1).trim().equals("clear")) {
                this.chatArea.setText("");
            } else if (this.textField.getText().substring(1).equals("help")) {

                this.chatArea.append("-----------------" + "\n" +
                        "/help - to access the various list of command " + "\n" +
                        "/clear - to clear the chat client side " + "\n" +
                        "/whois - to get information about an user " + "\n" +
                        "/quit - to quit the chat " + "\n" +
                        "/msg - to send a  message to a member " + "\n" +
                        "/nickname - to change nickname " + "\n" +
                        "/away - to set status away " + "\n" +
                        "/online - to come online after being away " + "\n" +
                        "/credits - to see application credits and creators " + "\n" +
                        "/info - to see personal IP and PORT " + "\n" +
                        "/clearlogs - clear logs client side" + "\n" +
                        "/motd - set message of the day" + "\n" +
                        "-----------------" + "\n");
            } else if (this.textField.getText().substring(1).trim().equals("credits")) {
                this.chatArea.append("MIT Copyright " + "\n" + "Copyright (c) 2020 Alessandro Buonerba & Tommaso Bruno" + "\n");

            } else if (this.textField.getText().substring(1).trim().equals("quit")) {
                System.exit(0);
            } else if (this.textField.getText().substring(1).startsWith("whois")) {
                if (this.textField.getText().substring(6).trim().length() > 0) {
                    String param = this.textField.getText().substring(7);
                    System.out.println("Target =>" + param);
                    System.out.println("Sender =>" + name);
                    this.out.println("WHOIS" + name + "/" + param);
                } else this.chatArea.append(">> [The command whois required a param] <<" + "\n");
            } else if (this.textField.getText().substring(1).startsWith("msg")) {
                String[] param = this.textField.getText().substring(5).split(" ");

                System.out.println(param[0]);

                param[1] = this.textField.getText().substring(7 + param[0].length() - 1);
                System.out.println(param[0] + " " + param[1]);
                this.out.println("WHISPER" + name + "/" + param[0] + "/" + param[1]);
                System.out.println("Messages sent");
                //System.out.println(nameLabel.getText().substring(22) + " is sending a message to " + param[1] + ". Message is: " + param[2]);

            } else if (this.textField.getText().substring(1).startsWith("nickname")) {
                if (!name.matches("^[a-zA-Z0-9]+$")) {
                    this.chatArea.append(">> [Name must contain only alphanumeric characters] <<" + "\n");
                } else {
                    String param = this.textField.getText().substring(10);
                    System.out.println(param);
                    this.out.println("NICKCHANGE" + param + "/" + this.name);
                }
            } else if (this.textField.getText().substring(1).startsWith("away")) {
                if (this.textField.getText().trim().substring(5).length() > 0) {

                    String reason = this.textField.getText().substring(5);
                    this.out.println("AWAY" + name + "/" + reason);
                } else chatArea.append(">> [The command /away needs a parameter] <<" + "\n");
            } else if (this.textField.getText().substring(1).trim().equals("online")) {
                this.out.println("GOONLINE" + name);
            } else if (this.textField.getText().substring(1).trim().equals("clearlogs")) {
                String admin = this.defaultListModel.get(0).substring(1);
                System.out.println("Admin user = " + admin);
                if (!name.equals(admin)) {
                    this.chatArea.append(">> [You are not an admin, can't clear the logs] <<" + "\n");
                } else {
                    this.out.println("CLEARLOGS");
                    this.chatArea.append(">> [Logs have been cleared] <<" + "\n");
                }
            } else if (this.textField.getText().substring(1).trim().equals("info")) {
                this.out.println("INFO" + name);
            } else this.chatArea.append("Command not found, type " +
                    "/help to see all the commands" + "\n");
        } else this.out.println(this.textField.getText());
        this.textField.setText("");
    }

    public void setName(String name) {
        name = name;
    }
}
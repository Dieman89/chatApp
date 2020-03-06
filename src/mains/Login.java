package mains;

import listeners.LoginListener;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class Login extends JFrame {

    public static void main(String[] args) {
        new Login();
    }

    public Login() {

        setSize(new Dimension(450, 560));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        //////////// FONT ////////////
        Font customFont = null;
        try {
            //create the font to use. Specify the size!
            customFont = Font.createFont(Font.TRUETYPE_FONT, new File("fonts/Avenir.ttf")).deriveFont(16f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            //register the font
            ge.registerFont(customFont);
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
        //////////// FONT ////////////

        //////////// GENERAL /////////////
        JPanel generalPanel = new JPanel(new BorderLayout());

        //////////// IMAGE ///////////////
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setPreferredSize(new Dimension(400, 200));
        imagePanel.setBackground(new Color(35, 39, 42));
        //imagePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
        ImageIcon logo = new ImageIcon("images/logo2.png");
        Image image = logo.getImage().getScaledInstance(191, 153, Image.SCALE_DEFAULT);
        ImageIcon finalLogo = new ImageIcon(image);
        JLabel logoLabel = new JLabel(finalLogo);
        imagePanel.add(logoLabel, BorderLayout.CENTER);
        //////////// IMAGE ///////////////

        ////////////  INFO //////////////
        JPanel infoPanel = new JPanel();
        infoPanel.setPreferredSize(new Dimension(400, 300));
        infoPanel.setBackground(new Color(35, 39, 42));

        //////////// USER INFO ///////////
        JPanel userLoc = new JPanel(new GridLayout(5, 1));
        userLoc.setBackground(new Color(35, 39, 42));
        JLabel errorLabel = new JLabel("No error");
        errorLabel.setForeground(Color.WHITE);
        errorLabel.setHorizontalAlignment(JLabel.CENTER);

        JLabel ipLabel = new JLabel("IP ADDRESS");
        ipLabel.setForeground(Color.WHITE);
        JTextField ipAddress = new JTextField(30);
        ipAddress.setPreferredSize(new Dimension(100, 50));
        ipAddress.setFont(customFont);

        JLabel userLabel = new JLabel("USERNAME");
        userLabel.setForeground(Color.WHITE);
        JTextField usernameField = new JTextField(30);
        usernameField.setPreferredSize(new Dimension(100, 50));
        usernameField.setFont(customFont);

        JPanel optionsPanel = new JPanel(new BorderLayout(40, 70));
        optionsPanel.setBackground(new Color(35, 39, 42));
        JButton loginButton = new JButton("LOGIN");
        loginButton.setPreferredSize(new Dimension(80, 35));
        loginButton.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        loginButton.setBackground(new Color(35, 39, 42));
        loginButton.setForeground(Color.WHITE);
        JButton exitButton = new JButton("EXIT");
        exitButton.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        exitButton.setPreferredSize(new Dimension(80, 35));
        exitButton.setBackground(new Color(35, 39, 42));
        exitButton.setForeground(Color.WHITE);
        optionsPanel.add(loginButton, BorderLayout.WEST);
        optionsPanel.add(exitButton, BorderLayout.EAST);

        userLoc.add(errorLabel);
        userLoc.add(userLabel);
        userLoc.add(usernameField);
        userLoc.add(ipLabel);
        userLoc.add(ipAddress);
        //userLoc.add(optionsPanel);

        //////////// USER INFO ///////////
        infoPanel.add(userLoc);
        infoPanel.add(optionsPanel);
        ////////////  INFO /////////////

        generalPanel.add(imagePanel, BorderLayout.NORTH);
        generalPanel.add(infoPanel, BorderLayout.CENTER);
        //////////// GENERAL /////////////

        loginButton.addActionListener(new LoginListener(usernameField, ipAddress, errorLabel, this));

        exitButton.addActionListener(e -> System.exit(0));

        add(generalPanel);
        setVisible(true);
    }
}

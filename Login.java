package com.smartbilling;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Login extends JFrame {
    private final JTextField user = new JTextField();
    private final JPasswordField pass = new JPasswordField();

    public Login() {
        setTitle("Smart Billing - Login");
        setSize(520, 360);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(new Color(27, 38, 59));
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8,8,8,8);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("SMART BILLING", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(new Color(27, 38, 59));
        g.gridx=0; g.gridy=0; g.gridwidth=2; card.add(title,g);

        JLabel sub = new JLabel("Inventory Management System", SwingConstants.CENTER);
        sub.setForeground(Color.GRAY);
        g.gridy=1; card.add(sub,g);

        g.gridwidth=1; g.gridy=2; g.gridx=0; card.add(new JLabel("Username"),g);
        g.gridx=1; card.add(user,g);
        g.gridx=0; g.gridy=3; card.add(new JLabel("Password"),g);
        g.gridx=1; card.add(pass,g);

        JButton login = new JButton("LOGIN");
        login.setBackground(new Color(46,125,50));
        login.setForeground(Color.WHITE);
        login.setFocusPainted(false);
        g.gridx=0; g.gridy=4; g.gridwidth=2; card.add(login,g);

        JLabel hint = new JLabel("Default: admin / admin123", SwingConstants.CENTER);
        hint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        hint.setForeground(Color.GRAY);
        g.gridy=5; card.add(hint,g);

        root.add(card);
        add(root);
        login.addActionListener(e -> authenticate());
        getRootPane().setDefaultButton(login);
    }

    private void authenticate() {
        String u = user.getText().trim();
        String p = new String(pass.getPassword());
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM users WHERE username=? AND password=?")) {
            ps.setString(1,u); ps.setString(2,p);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                dispose();
                new Dashboard().setVisible(true);
            } else JOptionPane.showMessageDialog(this,"Invalid username or password");
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this,"Database error: "+ex.getMessage());
        }
    }
}

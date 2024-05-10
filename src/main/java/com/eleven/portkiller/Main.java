package com.eleven.portkiller;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Main extends JFrame {

    private List<String> pIds;
    private JTextField textFieldPort;
    private JTextArea textAreaResults;
    private JButton btnCheckPort, btnKillPort;

    public Main() {
        createUI();
        pIds = new ArrayList<>();
        setTitle("Port Manager");
        setSize(500, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void createUI() {
        textFieldPort = new JTextField(15);
        textAreaResults = new JTextArea(10, 30);
        textAreaResults.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textAreaResults);

        btnCheckPort = new JButton("Check Port");
        btnKillPort = new JButton("Kill Port");
        btnKillPort.setEnabled(false);

        btnCheckPort.addActionListener(e -> checkPort());
        btnKillPort.addActionListener(e -> killPort());

        JPanel panel = new JPanel();
        panel.add(new JLabel("Enter Port:"));
        panel.add(textFieldPort);
        panel.add(btnCheckPort);
        panel.add(btnKillPort);

        getContentPane().add(panel, BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }

    private void checkPort() {
        String os = System.getProperty("os.name").toLowerCase();
        String port = textFieldPort.getText().trim();
        if (port.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a port number.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!textAreaResults.getText().isEmpty()) {
            textAreaResults.append("\n--- New Check ---\n");
        }
        pIds.clear();

        String command = os.contains("windows") ?
                "cmd /c netstat -ano | findstr \": " + port + " \"" :
                "netstat -anp | grep :" + port;

        executeCommand(command, true);
    }

    private void killPort() {
        String os = System.getProperty("os.name").toLowerCase();
        for (String pId : pIds) {
            String commandKill = os.contains("windows") ?
                    "cmd /c taskkill /F /PID " + pId :
                    "kill -9 " + pId;

            executeCommand(commandKill, false);
        }
    }

    private void executeCommand(String command, boolean isCheckCommand) {
        try {
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            StringBuilder sb = new StringBuilder();
            boolean found = false;

            while ((line = reader.readLine()) != null) {
                if (isCheckCommand && line.contains(":" + textFieldPort.getText().trim())) {
                    textAreaResults.append("Process using port " + textFieldPort.getText().trim() + ": " + line + "\n");
                    String[] parts = line.trim().split("\\s+");
                    String pId = parts[parts.length - 1].replaceAll("/.*$", "");
                    pIds.add(pId);
                    found = true;
                } else if (!isCheckCommand) {
                    sb.append(line).append("\n");
                }
            }
            reader.close();

            if (isCheckCommand && !found) {
                textAreaResults.setText("No process is running on port " + textFieldPort.getText().trim());
                btnKillPort.setEnabled(false);
            } else if (!isCheckCommand) {
                textAreaResults.append("Killed process with PID " + command.split(" ")[command.split(" ").length - 1] + ":\n" + sb.toString());
            } else {
                btnKillPort.setEnabled(true);
            }
        } catch (Exception ex) {
            textAreaResults.setText("Error: " + ex.getMessage());
            btnKillPort.setEnabled(false);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}

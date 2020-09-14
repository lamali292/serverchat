package de.lamali.voicechat.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class Client {

	private static JFrame frame;
	private static JPanel panel;
	private static JTextArea textMessages;
	private static JTextField textMessage, textName;
	private static JButton buttonSend;
	
	private static Socket client;
	private static PrintWriter writer;
	private static BufferedReader reader;
	
	public static void main(String[] args) {
		Client c = new Client();
		c.createGUI();
	}
	
	private void createGUI() {
		frame = new JFrame("Chat");
		frame.setSize(800, 600);
		
		panel = new JPanel();
		
		textMessages = new JTextArea();
		textMessages.setEditable(false);
		
		textMessage = new JTextField(38);
		textMessage.addKeyListener(new SendPressEnterListener());
		
		buttonSend = new JButton("Senden");
		buttonSend.addActionListener(new SendButtonListener());
		
		textName = new JTextField(10);
		
		JScrollPane scrollPane = new JScrollPane(textMessages);
		scrollPane.setPreferredSize(new Dimension(700, 500));
		scrollPane.setMinimumSize(new Dimension(700, 500));
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		connectToServer();
		
		Thread t = new Thread(new MessagesFromServerListener());
		t.start();
		
		panel.add(scrollPane);
		panel.add(textName);
		panel.add(textMessage);
		panel.add(buttonSend);
		
		frame.getContentPane().add(BorderLayout.CENTER, panel);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	public boolean connectToServer() {
		try {
			client = new Socket("127.0.0.1", 5555);
			reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
			writer = new PrintWriter(client.getOutputStream());
			appendTextMessages("Netzwerkverbindung hergestellt");
			return true;
		} catch (Exception e) {
			appendTextMessages("Netzwerkverbindung konnte nicht hergestellt werden");
			e.printStackTrace();
			return false;
		}
	}

	private void appendTextMessages(String string) {
		textMessages.append(string+"\n");
	}

	public void sendMessageToServer() {
		writer.println(textName.getText()+": "+textMessage.getText());
		writer.flush();
		
		textMessage.setText("");
		textMessage.requestFocus();
	}
	
	private class SendPressEnterListener implements KeyListener {

		@Override
		public void keyTyped(KeyEvent e) {}

		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_ENTER) {
				sendMessageToServer();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {}
	}
	
	private class SendButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			sendMessageToServer();
		}
		
	}
	
	private class MessagesFromServerListener implements Runnable {

		@Override
		public void run() {
			String message;
			
			try {
				while ((message = reader.readLine()) != null) {
					appendTextMessages(message);
					textMessages.setCaretPosition(textMessages.getText().length());
				}
			} catch (IOException e) {
				appendTextMessages("Nachricht konnte nicht empfangen werden!");
				e.printStackTrace();
			}
			
		}
		
	}
}

package de.lamali.voicechat.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;

import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class Client {

	private JFrame frame;
	private JPanel panel;
	private JTextArea textMessages;
	private JTextField textMessage, textName;
	private JButton buttonSend;

	private Socket client;
	private OutputStream out;
	private InputStream in;

	private int CHUNK_SIZE = 64;
	private AudioFormat format = new AudioFormat(20000.0f, 16, 1, true, true);
	private TargetDataLine microphone;
	private SourceDataLine speakers;
	

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
		buttonSend = new JButton("Senden");
		textName = new JTextField(10);

		JScrollPane scrollPane = new JScrollPane(textMessages);
		scrollPane.setPreferredSize(new Dimension(700, 500));
		scrollPane.setMinimumSize(new Dimension(700, 500));
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		connectToServer();

		new Thread(new SpeakerHandler()).start();
		new Thread(new MicrophoneHandler()).start();
		
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
			client = new Socket("192.168.188.24", 5555);
			in = client.getInputStream();
			out = client.getOutputStream();
			
			frame.addWindowListener(new WindowAdapter(){
	            public void windowClosing(WindowEvent e){
	                try {
						client.close();
						microphone = null;
					} catch (IOException e1) {
						e1.printStackTrace();
					}
	            }
	        });

			microphone = AudioSystem.getTargetDataLine(format);

			DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
			microphone = (TargetDataLine) AudioSystem.getLine(info);
			microphone.open(format);

			microphone.start();

			DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
			speakers = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
			speakers.open(format);
			speakers.start();
			appendTextMessages("Netzwerkverbindung hergestellt");
			return true;
		} catch (Exception e) {
			appendTextMessages("Netzwerkverbindung konnte nicht hergestellt werden");
			e.printStackTrace();
			return false;
		}
	}

	private void appendTextMessages(String string) {
		textMessages.append(string + "\n");
	}

	private class MicrophoneHandler implements Runnable {

		@Override
		public void run() {
			while (microphone != null && !client.isClosed()) {
				try {
					byte[] data = new byte[microphone.getBufferSize() / 5];
					int numBytesRead = microphone.read(data, 0, CHUNK_SIZE);
					out.write(data, 0, numBytesRead);
					//speakers.write(data, 0, numBytesRead);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		}

	}

	private class SpeakerHandler implements Runnable {
		@Override
		public void run() {
			while (microphone != null && !client.isClosed()) {
				try {
					byte[] data = new byte[microphone.getBufferSize() / 5];
					int numBytesRead = in.readNBytes(data, 0, CHUNK_SIZE);
					speakers.write(data, 0, numBytesRead);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
}

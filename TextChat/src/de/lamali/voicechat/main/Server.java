package de.lamali.voicechat.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

	ServerSocket server;
	ArrayList<PrintWriter> clientWriters;

	public static void main(String[] args) {
//		AudioFormat format = new AudioFormat(30000.0f, 16, 1, true, true);
//	    TargetDataLine microphone;
//	    SourceDataLine speakers;
//	    try {
//	        microphone = AudioSystem.getTargetDataLine(format);
//
//	        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
//	        microphone = (TargetDataLine) AudioSystem.getLine(info);
//	        microphone.open(format);
//
//	        ByteArrayOutputStream out = new ByteArrayOutputStream();
//	        int numBytesRead;
//	        int CHUNK_SIZE = 1024;
//	        byte[] data = new byte[microphone.getBufferSize() / 5];
//	        microphone.start();
//
//	        int bytesRead = 0;
//	        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
//	        speakers = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
//	        speakers.open(format);
//	        speakers.start();
//	        while (bytesRead < 10000000) {
//	            numBytesRead = microphone.read(data, 0, CHUNK_SIZE);
//	            bytesRead += numBytesRead;
//	            // write the mic data to a stream for use later
//	            out.write(data, 0, numBytesRead); 
//	            // write mic data to stream for immediate playback
//	            speakers.write(data, 0, numBytesRead);
//	        }
//	        speakers.drain();
//	        speakers.close();
//	        microphone.close();
//	    } catch (LineUnavailableException e) {
//	        e.printStackTrace();
//	    } 
		
		Server s = new Server();
		if (s.runServer()) {
			try {
				s.listenToClients();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private boolean runServer() {
		try {
			server = new ServerSocket(5555);
			appendTextToConsole("Server gestartet!", 0);
			clientWriters = new ArrayList<>();
			return true;
		} catch (IOException e) {
			appendTextToConsole("Server konnte nicht gestartet werden!", 0);
			e.printStackTrace();
			return false;
		}
	}

	private void appendTextToConsole(String string, int type) {
		if (type == 0) {
			System.err.println(string + "\n");
		} else if (type == 1) {
			System.out.println(string + "\n");
		}
	}

	private void listenToClients() throws IOException {
		while (true) {
			Socket client = server.accept();

			PrintWriter writer = new PrintWriter(client.getOutputStream());
			clientWriters.add(writer);

			Thread clientThread = new Thread(new ClientHandler(client));
			clientThread.start();
		}
	}

	private void sendToAllClients(String nachricht) {
		clientWriters.forEach((writer) -> {
			writer.println(nachricht);
			writer.flush();
		});
	}

	private class ClientHandler implements Runnable {
		private BufferedReader reader;

		public ClientHandler(Socket client) throws IOException {
			reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
		}

		@Override
		public void run() {
			String nachricht;
			try {
				while ((nachricht = reader.readLine()) != null) {
					appendTextToConsole("Vom Client: " + nachricht, 1);
					sendToAllClients(nachricht);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}

package de.lamali.voicechat.main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Server {

	ServerSocket server;
	Set<Socket> clients;

	public static void main(String[] args) {
		Server s = new Server();
		s.run();
	}

	private boolean run() {
		try {
			server = new ServerSocket(5555);
			clients = new HashSet<>();

			while (true) {
				Socket client = server.accept();

				clients.add(client);

				new Thread(new ClientHandler(client)).start();

			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private class ClientHandler implements Runnable {
		private Socket client;

		public ClientHandler(Socket client) throws IOException {
			this.client = client;
		}

		@Override
		public void run() {
			while (!client.isClosed()) {
				byte[] data = new byte[16];
				try {
					int numBytesRead = client.getInputStream().read(data, 0, 8);
//					client.getOutputStream().write(data, 0, numBytesRead);
										
					for(Socket clien : clients) {
						if(!clien.isClosed() && !client.equals(clien)) {
							clien.getOutputStream().write(data, 0, numBytesRead);
						}
					}
					
				} catch (IOException e) {
					//e.printStackTrace();
				} 
			}
			
		}

	}

}

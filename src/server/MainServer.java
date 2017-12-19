package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MainServer {
	public static void main(String[] args)
	{
		try {
			// Start a new server socket.
			ServerSocket sock = new ServerSocket(7789);
			Socket client;
			System.out.println("Server started...");
			// Accept new client connections.
			while(true){
				if ((client = sock.accept()) != null)
				{
					System.out.println("Client connected");
					System.out.println("Starting new XMLParser");
					XMLParser parser = new XMLParser(client);
					parser.start();
				}
			}
		}
		catch(IOException ioe) {
			System.err.println(ioe);
		}
		
		
//		//Debugging
//		System.out.println("Starting new XMLParser");
//		XMLParser parser = new XMLParser(new Socket());
//		parser.start();
	}
}

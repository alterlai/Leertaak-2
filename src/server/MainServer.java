package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

public class MainServer {
	
	public static void main(String[] args)
	{
		ArrayBlockingQueue<HashMap<String, String>> dataBuffer = new ArrayBlockingQueue<HashMap<String, String>>(15000);	// Data buffer for weatherdata
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
					XMLParser parser = new XMLParser(client, dataBuffer);
					parser.start();
				}
			}
		}
		catch(IOException ioe) {
			System.err.println(ioe);
		}
	}
}

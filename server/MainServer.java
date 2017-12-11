package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MainServer {
	public static void main(String[] args)
	{
		try {
			// Start a new server socket.
			ServerSocket sock = new ServerSocket(7789);
			Socket client;
			// Accept new client connections.
			if ((client = sock.accept()) != null)
			{
				System.out.println("Client connected");
				System.out.println("Starting new XMLParser");
				XMLParser parser = new XMLParser(client);
				parser.start();
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

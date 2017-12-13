package server;

import java.net.Socket;
import java.io.*;
import java.util.*;
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import java.sql.*;

public class XMLParser extends Thread {
	private Socket sock;
	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DB_URL = "jdbc:mysql://localhost/unwdmi";
	private static final String DB_USER = "root";
	private static final String DB_PASSWORD = "";
	
	
	XMLParser(Socket sock){
		this.sock = sock;
		System.out.println("New thread started...");
	}
	
	public void run() {
		
		SAXBuilder saxBuilder = new SAXBuilder();
		HashMap<String, String> data = new HashMap<>();
		Document document = new Document();
		storeInDB();
		try {
			while((document = getXML()) != null) {
				document = getXML();
				
				// If the connection has been terminated, stop the thread.
				if(document == null) break;
				
				// Grab the parent element.
				Element classElement = document.getRootElement();
				Element measurement = classElement.getChild("MEASUREMENT");
				List<Element> elementen = measurement.getChildren();
				Iterator it = elementen.iterator();
				
				// Iterate over all elements in the XML document and put them in the data map.
				while(it.hasNext())
				{
					Element element = (Element) it.next();
					data.put(element.getName(), element.getValue());
				}
				
				
				
				// Iterate over the data map and print the values.
//				it = data.entrySet().iterator();
//				while (it.hasNext()){
//					Map.Entry pair = (Map.Entry)it.next();
//					System.out.println(pair.getKey() + " = " + pair.getValue());
//				}
			}
				
		} catch (JDOMException | NullPointerException e) {
			e.printStackTrace();
			System.err.println("main loop");
		}
	}
	
	private Document getXML() throws JDOMException
	{
		SAXBuilder builder = new SAXBuilder();
		try {
			// Get the input stream.
			BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			String xmlstream = "";
			String line;
			while (!(line = in.readLine()).contains("</MEASUREMENT>")){
				xmlstream += line;
			}
			xmlstream += "</MEASUREMENT></WEATHERDATA>";
			Document xmlDocument = builder.build(new StringReader(xmlstream));
			return xmlDocument;
		} catch (IOException | NullPointerException e) {
			// Socket closes.
			System.out.println("Client disconnected!");
			return null;
		}
	}
	
	private static void storeInDB() {
		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			
			System.out.println("Opening connection to database...");
			conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
			
			// Execute query
			stmt = conn.createStatement();
			String sql = "Select * from stations LIMIT 1";
			ResultSet rs = stmt.executeQuery(sql);
			
			// Extract data
			while(rs.next()) {
				int stn = rs.getInt("stn");
				String name = rs.getString("name");
				
				System.out.println("Station: : " + stn);
				System.out.println("Name: " + name);
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}

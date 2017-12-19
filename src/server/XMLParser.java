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
	private static final int dataChuckSize = 1;
	
	// Aggregate data location
	private ArrayList<HashMap<String, String>> dataStack;
	
	
	XMLParser(Socket sock){
		this.sock = sock;
		this.dataStack = new ArrayList<>();
		System.out.println("New thread started...");
	}
	
	public void run() {
		Document document = new Document();
		try {
			while((document = getXML()) != null) {
				HashMap<String, String> data = new HashMap<>();
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
					if(!element.getValue().equals("null"))
						data.put(element.getName(), element.getValue());
					else
						data.put(element.getName(), "NULL");
				}

				//Increment data count
				dataStack.add(data);

				if (dataStack.size() >= dataChuckSize)
				{
					storeInDB();
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
	
	private void storeInDB() {
		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName(JDBC_DRIVER);
			
			System.out.println("Opening connection to database...");
			conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
			
			// Execute query
			stmt = conn.createStatement();
			String sql = "INSERT INTO measurement (stn_id, date, time, temp, dewp, stp, slp, visib, wdsp, prcp, sndp, frshtt, cldc, winddir) VALUES";
			
			// TODO winddir variabele klopt nog niet, (vaak null).
			// TODO interpolatie van missende waarden
			for (int i=0; i<dataStack.size(); i++) {
				HashMap<String, String> dataElement = dataStack.get(i);
				sql += "(";
				sql += dataElement.get("STN") + ", ";
				sql += "\'" + dataElement.get("DATE") + "\' , ";
				sql += "\'" + dataElement.get("TIME") + "\' , ";
				sql += dataElement.get("TEMP") + ", ";
				sql += dataElement.get("DEWP") + ", ";
				sql += dataElement.get("STP") + ", ";
				sql += dataElement.get("SLP") + ", ";
				sql += dataElement.get("VISIB") + ", ";
				sql += dataElement.get("WDSP") + ", ";
				sql += dataElement.get("PRCP") + ", ";
				sql += dataElement.get("SNDP") + ", ";
				sql += "b\'" + dataElement.get("FRSHTT") + "\' , ";
				sql += dataElement.get("CLDC") + ", ";
				sql += 20;
				sql += ")";
				if (i + 1 == dataStack.size()) sql += ";"; else sql += ", ";
			}
			System.out.println(sql);
					
					
			int result = stmt.executeUpdate(sql);
			System.out.println("Result: " + result);
			stmt.close();
			conn.close();
			dataStack.clear();	// Clear the datastack
		} catch(Exception e) {
			System.out.println("Unable to connect to the database.");
			e.printStackTrace();
		}
	}
}

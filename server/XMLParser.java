package server;

import java.net.Socket;
import java.io.*;
import java.util.*;
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

public class XMLParser extends Thread {
	private Socket sock;
	
	XMLParser(Socket sock){
		this.sock = sock;
		System.out.println("New thread started...");
	}
	
	public void run() {
		
		SAXBuilder saxBuilder = new SAXBuilder();
		HashMap<String, String> data = new HashMap<>();
		Document document = new Document();
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
}

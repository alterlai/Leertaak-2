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
		try {
			
			Document document = getXML();
			Element classElement = document.getRootElement();
			Element measurement = classElement.getChild("MEASUREMENT");
			List<Element> elementen = measurement.getChildren();
			Iterator it = elementen.iterator();
			
			while(it.hasNext())
			{
				Element element = (Element) it.next();
				data.put(element.getName(), element.getValue());
			}
			
			it = data.entrySet().iterator();
			while (it.hasNext()){
				Map.Entry pair = (Map.Entry)it.next();
				System.out.println(pair.getKey() + " = " + pair.getValue());
			}
			
		} catch (JDOMException | NullPointerException e) {
			e.printStackTrace();
		}
	}
	
	private Document getXML() throws JDOMException
	{
		SAXBuilder builder = new SAXBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			Document doc = builder.build(new BufferedReader(new InputStreamReader(sock.getInputStream())));
			System.out.println("file received");
			System.out.println(doc);
			return new Document();
		} catch (IOException e) {
			e.printStackTrace();
			return new Document();
		}
		
	}
}

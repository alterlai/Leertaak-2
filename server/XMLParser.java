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
		File xmlfile = new File("output.xml");
		SAXBuilder saxBuilder = new SAXBuilder();
		HashMap<String, String> data = new HashMap<>();
		try {
			
			Document document = saxBuilder.build(xmlfile);
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
			
		} catch (JDOMException | IOException | NullPointerException e) {
			e.printStackTrace();
		}
	}
}

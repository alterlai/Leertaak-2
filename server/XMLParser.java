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
			data.put("STN", measurement.getChild("STN").getValue());
			data.put("DATE", measurement.getChild("DATE").getValue());
			data.put("TIME", measurement.getChild("TIME").getValue());
			data.put("DEWP", measurement.getChild("DEWP").getValue());
			data.put("TEMP", measurement.getChild("TEMP").getValue());
			data.put("STP", measurement.getChild("STP").getValue());
			data.put("SLP", measurement.getChild("SLP").getValue());
			data.put("VISIB", measurement.getChild("VISIB").getValue());
			data.put("WDSP", measurement.getChild("WDSP").getValue());
			data.put("PRCP", measurement.getChild("PRCP").getValue());
			data.put("SNDP", measurement.getChild("SNDP").getValue());
			data.put("FRSHTT", measurement.getChild("FRSHTT").getValue());
			data.put("CLDC", measurement.getChild("CLDC").getValue());
			data.put("WNDDIR", measurement.getChild("WNDDIR").getValue());
			
			Iterator it = data.entrySet().iterator();
			while (it.hasNext()){
				Map.Entry pair = (Map.Entry)it.next();
				System.out.println(pair.getKey() + " = " + pair.getValue());
			}
			
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
	}
}

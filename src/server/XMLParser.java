package server;

import java.net.Socket;
import java.io.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

/*
 * XML parser receives a client connection and puts the XML data onto the dataBuffer.
 */
public class XMLParser extends Thread {
	private Socket sock;
	private static ArrayBlockingQueue<HashMap<String, String>> dataBuffer;
	
	
	XMLParser(Socket client, ArrayBlockingQueue<HashMap<String, String>> dataBuffer){
		this.sock = client; 						// Client connection
		XMLParser.dataBuffer = dataBuffer;			// Create databuffer
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
				while(it.hasNext()) {
					Element element = (Element) it.next();
					if (!element.getValue().equals("null")) {
						if (element.getName().equals("TEMP")) {
							if (dataBuffer.size() != 0) {
								if (Double.parseDouble(element.getValue()) < lowTemp(element.getName())) {
									data.put(element.getName(), String.valueOf(lowTemp(element.getName())));
								} else if (Double.parseDouble(element.getValue()) > highTemp(element.getName())) {
									data.put(element.getName(), String.valueOf(highTemp(element.getName())));
								} else {
									data.put(element.getName(), element.getValue());
								}
							} else {
								data.put(element.getName(), element.getValue());
							}
							
						} else {
							data.put(element.getName(), element.getValue());
						} 
					} else {
						data.put(element.getName(), String.valueOf(extraPolate(element.getName())));
					}
				}

				//Add data to databuffer
				dataBuffer.add(data);
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

	private double extraPolate(String name) {
		double sum = 0;
		double avg = 0;
		ArrayList<Double> diffList = new ArrayList<Double>();
		ArrayList<Double> resultList = new ArrayList<Double>();
		
		for (int i = dataBuffer.size(); i < dataBuffer.size() - 50; i--) {
////			HashMap<String, String> measurement = dataBuffer.get(i);
//			double temp = Double.parseDouble(measurement.get(name));
//			diffList.add(temp);	
		}
		
		for (int i = 0; i < diffList.size(); i++) {
			resultList.add(Math.abs(diffList.get(i) - diffList.get(i-1)));
		}
		
		for (int i = 0; i < resultList.size(); i++) {
			sum += resultList.get(i);
		}
		
		avg = sum/50;
		return avg;
		
	}
	
	private double lowTemp(String name) {
		double temp = extraPolate(name);
		temp = temp * 0.8;
		return temp;
	}
	
	private double highTemp(String name) {
		double temp = extraPolate(name);
		temp = temp * 1.2;
		return temp;
	}

}

package server;

import java.net.Socket;
import java.io.*;
import java.util.*;

import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

import static javax.imageio.ImageIO.getCacheDirectory;

public class XMLParser extends Thread {
	private Socket sock;
	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DB_URL = "jdbc:mysql://localhost/unwdmi";
	private static final String DB_USER = "root";
	private static final String DB_PASSWORD = "";
	private static final int dataChuckSize = 1;
	
	// Aggregate data location
	private ArrayList<HashMap<String, String>> dataStack;
	
	//connection
	XMLParser(Socket sock){
		this.sock = sock; 
		this.dataStack = new ArrayList<>();
		System.out.println("New thread started...");
	}

	//thread run
	public void run() {
		createFile();
		datahash();
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

	private void datahash()  {
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
							if (dataStack.size() != 0) {
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
						data.put(element.getName(), String.valueOf(extrapolate(element.getName())));
					}

					//adddata();
				}

				//Increment data count
				dataStack.add(data);

				if(dataStack.size() >= dataChuckSize)
				{
					//do stuff
				}


				adddata();
			}

		} catch (JDOMException | NullPointerException e) {
			e.printStackTrace();
			System.err.println("main loop");
		}

	}

	private void adddata()  {
		String data = "";
		String temp = "";
		String dewp = "";
		String sndp = "";
		String minus = "10";
		String positive = "0";
		for (int i=0; i<dataStack.size(); i++) {
			HashMap<String, String> dataElement = dataStack.get(i);
			data += String.format("%6s", dataElement.get("STN")).replace(' ','0');
			data += dataElement.get("DATE");
			data += dataElement.get("TIME");
			temp = String.format("%5s", dataElement.get("TEMP")).replace(' ', '0');
			if (temp.contains("-")){
				temp = minus + temp;
			}else{
				temp = positive + temp;
			}
			data += temp;
			dewp = String.format("%5s", dataElement.get("DEWP")).replace(' ', '0');

			if (dewp.contains("-")){
				dewp = minus + dewp;
			}else{
				dewp = positive + dewp;
			}
			data += dewp;
			data += String.format("%6s", dataElement.get("STP")).replace(' ', '0');
			data += String.format("%6s", dataElement.get("SLP")).replace(' ', '0');
			data += String.format("%5s", dataElement.get("VISIB")).replace(' ', '0');
			data += String.format("%4s", dataElement.get("WDSP")).replace(' ', '0');
			data += String.format("%5s", dataElement.get("PRCP")).replace(' ', '0');

			sndp = String.format("%4s", dataElement.get("SNDP")).replace(' ', '0');

			if (sndp.contains("-")){
				sndp = minus + sndp;
			}else{
				sndp = positive + sndp;
			}
			data += sndp;
			data += dataElement.get("FRSHTT");
			data += String.format("%4s", dataElement.get("CLDC")).replace(' ', '0');
			data += String.format("%3s", dataElement.get("WNDDIR")).replace(' ', '0');

			if (i + 1 == dataStack.size()) data += ";"; else data += ", ";
		}
		data = data.replace(";", "");
		data = data.replace("-", "");
		data = data.replace(":", "");
		data = data.replace(".", "");
		data += ";";
		System.out.println(data);
		writedata(data, "data.dat");
	}
	
	private double extrapolate(String name) {
		double sum = 0;
		double avg = 0;
		ArrayList<Double> diffList = new ArrayList<Double>();
		ArrayList<Double> resultList = new ArrayList<Double>();
		
		for (int i = dataStack.size(); i < dataStack.size() - 50; i--) {
			HashMap<String, String> measurement = dataStack.get(i);
			double temp = Double.parseDouble(measurement.get(name));
			diffList.add(temp);	
		}
		
		for (int i = 0; i < diffList.size(); i++) {
			resultList.add(Math.abs(diffList.get(i) - diffList.get(i-1)));
		}
		
		for(int i = 0; i < resultList.size(); i++) {
			sum += resultList.get(i);
		}
		
		avg = sum/50;
		return avg;
		
	}
	
	private double lowTemp(String name) {
		double temp = extrapolate(name);
		temp = temp * 0.8;
		return temp;
	}
	
	private double highTemp(String name) {
		double temp = extrapolate(name);
		temp = temp * 1.2;
		return temp;
	}

	private void writedata(String data, String file){
		try{
			FileOutputStream fos = new FileOutputStream(file);
			DataOutputStream outStream = new DataOutputStream(new BufferedOutputStream(fos));
			outStream.writeUTF(data);
			outStream.close();
		}catch (Exception e) {
			System.out.println("ERROR");
		}
		System.out.println("Writing...");
	}

	private void createFile(){
		try {
			File file = new File(getCacheDirectory(), "data.dat");
			file.getParentFile().mkdir();
			file.createNewFile();
		} catch(Exception e){
			System.out.println("File already exists");
		}
	}

}
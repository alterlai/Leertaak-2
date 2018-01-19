package server;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

/*
 * The BufferConsumer is a thread that is reading the databuffer from the XMLparser and stores the data on disk in an appropriate manner.
 */
public class BufferConsumer extends Thread{
	private static ArrayBlockingQueue<HashMap<String, String>> dataBuffer;
	
	BufferConsumer(ArrayBlockingQueue<HashMap<String, String>> dataBuffer) {
		BufferConsumer.dataBuffer = dataBuffer;
	}
	
	public void run() {
		String temp = "";
		String dewp = "";
		String sndp = "";
		String minus = "10";
		String positive = "0";
		boolean append = true;
		
		while (true)
		{
			if (!dataBuffer.isEmpty())
			{
				String data = "";
				HashMap<String, String> dataBlock = dataBuffer.poll();
				data += String.format("%6s", dataBlock.get("STN")).replace(' ','0');
				data += dataBlock.get("DATE");
				data += dataBlock.get("TIME");
				temp = String.format("%5s", dataBlock.get("TEMP")).replace(' ', '0');
				if (temp.contains("-")){
					temp = minus + temp;
				}else{
					temp = positive + temp;
				}
				data += temp;
				dewp = String.format("%5s", dataBlock.get("DEWP")).replace(' ', '0');

				if (dewp.contains("-")){
					dewp = minus + dewp;
				}else{
					dewp = positive + dewp;
				}
				data += dewp;
				data += String.format("%6s", dataBlock.get("STP")).replace(' ', '0');
				data += String.format("%6s", dataBlock.get("SLP")).replace(' ', '0');
				data += String.format("%5s", dataBlock.get("VISIB")).replace(' ', '0');
				data += String.format("%4s", dataBlock.get("WDSP")).replace(' ', '0');
				data += String.format("%5s", dataBlock.get("PRCP")).replace(' ', '0');

				sndp = String.format("%4s", dataBlock.get("SNDP")).replace(' ', '0');

				if (sndp.contains("-")){
					sndp = minus + sndp;
				}else{
					sndp = positive + sndp;
				}
				data += sndp;
				data += dataBlock.get("FRSHTT");
				data += String.format("%4s", dataBlock.get("CLDC")).replace(' ', '0');
				data += String.format("%3s", dataBlock.get("WNDDIR")).replace(' ', '0');

				data += ";";
				data = data.replace(";", "");
				data = data.replace("-", "");
				data = data.replace(":", "");
				data = data.replace(".", "");
				data += ";";
				System.out.println(data);
				writedata(data, "data.dat", append);
			}
		
		}
	}
	
	
	private void writedata(String data, String file, boolean append){
		try{
			FileOutputStream fos = new FileOutputStream(file, append);
			DataOutputStream outStream = new DataOutputStream(new BufferedOutputStream(fos));
			outStream.writeUTF(data);
			outStream.close();
		}catch (Exception e) {
			System.out.println("ERROR");
		}
		System.out.println("Writing...");
	}
	
	private Boolean createFile(){
		try {
			File file = new File("", "data.dat");
			file.getParentFile().mkdir();
			file.createNewFile();
			return true;
		} catch(Exception e){
			System.out.println("File already exists");
			return false;
		}
	}
	
	
}

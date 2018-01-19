package server;

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
		int temp = 0;
		while (true)
		{
			if (!dataBuffer.isEmpty())
			{
				System.out.println(dataBuffer.peek());
			}
		}
	}
}

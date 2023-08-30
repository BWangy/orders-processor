package processor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class ProcessorThread implements Runnable {
	
	private TreeMap<Integer, TreeMap<String, Integer>> clients;
	private String baseFile;
	
	public ProcessorThread(TreeMap<Integer, TreeMap<String, Integer>> clients, String baseFile) {
		this.baseFile = baseFile;
		this.clients = clients;
	}
	
	@Override
	public void run() {
		int clientId = 0;
		try {
			TreeMap<String, Integer> order = new TreeMap<>();
			FileReader fileReader = new FileReader(baseFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			Scanner fileScanner = new Scanner(bufferedReader);
			while(fileScanner.hasNextLine()) {
				Scanner s2 = new Scanner(fileScanner.nextLine());
				String item = s2.next();
				if(item.equals("ClientId:")) {
					clientId = s2.nextInt();
					System.out.println("Reading order for client with id: " + clientId); 
				} else {
					if(order.get(item) == null) {
						order.put(item, 1);
					} else {
						order.put(item, order.get(item) + 1);
					}	
				}
				
			}
			fileScanner.close();
			synchronized(clients) {
				clients.put(clientId, order);
			}
			
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}		
}


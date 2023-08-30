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


public class OrdersProcessor {	
	
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		
		System.out.print("Enter item's data file name: ");
		String dataFileName = scanner.next();
		System.out.println();
		
		System.out.print("Enter 'y' for multiple threads, any other character otherwise: ");
		String character = scanner.next();
		System.out.println();
		
		System.out.print("Enter number of orders to process: ");
		int numOfOrders = scanner.nextInt();
		System.out.println();
		
		System.out.print("Enter order's base filename: ");
		String baseFileName = scanner.next();
		System.out.println();
			
		System.out.print("Enter result's filename: ");
		String resultFileName = scanner.next();
		System.out.println();
		
		scanner.close();
		
		long startTime = System.currentTimeMillis();
		File results = new File(resultFileName);
		HashMap<String, Double> itemsData = readingDataFile(dataFileName);
		TreeMap<Integer, TreeMap<String, Integer>> clients = new TreeMap<>();
		
		if(!character.equals("y")) {
			int clientId = 0;
			try {
				for(int i = 1; i <= numOfOrders; i++) {
					TreeMap<String, Integer> order = new TreeMap<>();
					FileReader fileReader = new FileReader(baseFileName + i + ".txt");
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
					clients.put(clientId, order);
				}
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		} else {
			Thread[] allThreads = new Thread[numOfOrders];
			for(int i = 0; i < allThreads.length; i++) {
				allThreads[i] = new Thread(new ProcessorThread(clients, baseFileName + (i+1) + ".txt"));
			}
			
			for(int i = 0; i < allThreads.length; i++) {
				allThreads[i].start();
			}
			
			for(int i = 0; i < allThreads.length; i++) {
				try {
					allThreads[i].join();
				} catch (InterruptedException e) {
					System.err.println(e.getMessage());
				}
			}
		}
		
		try {
			FileWriter writer = new FileWriter(results, false);
			BufferedWriter bufferedWriter = new BufferedWriter(writer);
			TreeMap<String, Integer> itemsSummary = new TreeMap<>();
			for(Map.Entry<Integer, TreeMap<String, Integer>> client : clients.entrySet()) {
				bufferedWriter.write("----- Order details for client with Id: " + client.getKey() + " -----");
				bufferedWriter.newLine();
				double orderTotal = 0.0;
				for(Map.Entry<String, Integer> item : client.getValue().entrySet()) {
					double itemCost = itemsData.get(item.getKey());
					String formattedItemCost = NumberFormat.getCurrencyInstance().format(itemCost);
					
					double totalItemCost = itemCost * item.getValue();
					String formattedTotalCost = NumberFormat.getCurrencyInstance().format(totalItemCost);
					orderTotal += totalItemCost;
					
					
					if(itemsSummary.get(item.getKey()) == null) {
						itemsSummary.put(item.getKey(), item.getValue());
					} else {
						itemsSummary.put(item.getKey(), itemsSummary.get(item.getKey()) + item.getValue());
					}
					
					bufferedWriter.write("Item's name: " + item.getKey() + ", Cost per item: " + formattedItemCost 
							+ ", Quantity: " + item.getValue() + ", Cost: " + formattedTotalCost);
					bufferedWriter.newLine();
				}
				String formattedOrderTotal = NumberFormat.getCurrencyInstance().format(orderTotal);
				bufferedWriter.write("Order Total: " + formattedOrderTotal);
				bufferedWriter.newLine();
			}
			
			bufferedWriter.write("***** Summary of all orders *****");
			bufferedWriter.newLine();
		
			
			double grandTotal = 0.0;
			for(Map.Entry<String, Integer> item : itemsSummary.entrySet()) {
				double itemCost = itemsData.get(item.getKey());
				String formattedItemCost = NumberFormat.getCurrencyInstance().format(itemCost);
				
				double totalItemCost = itemCost * item.getValue();
				String formattedTotalCost = NumberFormat.getCurrencyInstance().format(totalItemCost);
				grandTotal += totalItemCost;
				
				bufferedWriter.write("Summary - Item's name: " + item.getKey() + ", Cost per item: " + formattedItemCost + 
						", Number sold: " + item.getValue() + ", Item's Total: " + formattedTotalCost);
				bufferedWriter.newLine();
				
			}
			
			String formattedGrandTotal = NumberFormat.getCurrencyInstance().format(grandTotal);
			bufferedWriter.write("Summary Grand Total: " + formattedGrandTotal);
			bufferedWriter.newLine();
			bufferedWriter.flush();
			bufferedWriter.close();
		} catch(IOException e) {
			System.err.println(e.getMessage());
		}
	
		long endTime = System.currentTimeMillis();
		System.out.println("Processing time (msec): " + (endTime - startTime));
		System.out.println("Results can be found in the file: " + resultFileName);
	}
	
	public static HashMap<String, Double> readingDataFile(String fileName) {
		HashMap<String, Double> dataItems = new HashMap<>();
		try {
			Scanner scanner = new Scanner(new FileReader(fileName));
			
			while(scanner.hasNextLine()) {
				Scanner s2 = new Scanner(scanner.nextLine());
				while(s2.hasNext()) {
					String item = s2.next();
					double price = s2.nextDouble();
					dataItems.put(item, price);
				}
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		
		return dataItems;
	}
}
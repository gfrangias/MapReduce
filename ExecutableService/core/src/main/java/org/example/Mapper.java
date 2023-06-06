package org.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Mapper<K, V> {
	
	public static void main(String[] args) throws IOException {
		
		if (args.length < 2) {
			throw new IllegalArgumentException("Insufficient Arguments...");
		}
		
		String idx = args[0];
		
		String[] filenames = new String[args.length-1];
		
		for (int i=1; i<args.length; i++) {
			filenames[i-1] = args[i];
		}
		
		
		
		Mapper<String, Integer> mapper = new Mapper<String, Integer>();
		
		Map<String, Integer> result = mapper.map(filenames);
		
		File outputFile = new File("map_" + idx + ".intermediate.json");
		
		FileWriter stream = new FileWriter(outputFile);
		
		stream.write("{\n");
		
		ArrayList<String> keySet = new ArrayList<String>(result.keySet()); 
		
		for (int i=0; i<keySet.size(); i++) {
			String key = keySet.get(i);
			stream.write("\t\"" + key + "\": \"" + result.get(key) + "\"" + (i == keySet.size()-1 ? "\n" : ",\n"));
		}
		
		stream.write("}\n");
		
		stream.close();
	}
	
	@SuppressWarnings("unchecked")
	public Map<K, Integer> map(String[] filenames) throws IOException {
		Map<K, Integer> result = new HashMap<K, Integer>();
		
		
		for (String filename : filenames) {
			File file = new File(filename);
			Scanner stream = new Scanner(file);
			
			while (true) {
				String key = null;
				try {
					key = stream.next();
					if (key.charAt(key.length() - 1) == ',' || key.charAt(key.length() - 1) == '.') {
						key = key.substring(0, key.length() - 1);
					}
				}
				catch (NoSuchElementException e) {
					break;
				}
				
				
				K mapped_key = (K) key;
				
				if (!result.containsKey(mapped_key)) {
					result.put(mapped_key, 1);
				}
				else {
					result.put(mapped_key, result.get(mapped_key) + 1);
				}
			}
			
			
			
			stream.close();
		}
		
		return result;
	}
}

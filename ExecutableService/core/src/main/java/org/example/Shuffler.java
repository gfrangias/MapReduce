package org.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.HashMap;
import java.util.NoSuchElementException;

public class Shuffler {
	
	/**
	 * 
	 * @param args
	 * 
	 * args[0] - Number of reducers.
	 * args[1....N] - All files that will be reduced.
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		
		if (args.length < 2) {
			throw new IllegalArgumentException("Insufficient Arguments...");
		}
		
		int reducers = Integer.parseInt(args[0]);
		
		HashMap<String, ArrayList<Integer>> map = new HashMap<String, ArrayList<Integer>>();
		
		for (int i=1; i<args.length; i++) {
			String filename = args[i];
			Scanner stream = new Scanner(new File(filename));
							
			String key_object = null;
			String value_object = null;

			String line = null;
			
			
			while (true) {
				try {
					line = stream.nextLine();
				}
				catch (NoSuchElementException e) {
					break;
				}

				String[] line_elements = line.split(" ");

				if (line_elements.length != 2) {
					stream.close();
					throw new IllegalStateException("Each line should contain a key-value pair");
				}

				key_object = line_elements[0];
				value_object = line_elements[1];

				if (!map.containsKey(key_object)) {
					map.put(key_object, new ArrayList<Integer>(1000));
				}

				map.get(key_object).add(Integer.parseInt(value_object));
			}
			
		}
		
		ArrayList<String> keys = new ArrayList<String>(map.keySet());

		ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>(reducers);

		for (int i=0; i<reducers; i++) {
			list.add(new ArrayList<String>());
		}

		for (String key : keys) {
			list.get(Math.abs(key.hashCode()) % reducers).add(key);
		}
		
		for (int i=0; i<reducers; i++) {
			File outputFile = new File("reduce_" + i + ".intermediate");			
			FileWriter stream = new FileWriter(outputFile);
			
			for (String key : list.get(i)) {
				stream.write(key + " ");
				ArrayList<Integer> values = map.get(key);

				for (Integer value : values) {
					stream.write(value + " ");
				}
				stream.write("\n");
			}
						
			stream.close();
		}
	}
}

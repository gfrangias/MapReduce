package org.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Shuffler {
	
	public static void main(String[] args) throws IOException {
		
		if (args.length < 2) {
			throw new IllegalArgumentException("Insufficient Arguments...");
		}
		
		int reducers = Integer.parseInt(args[0]);
		
		ArrayList<Pair> pairs = new ArrayList<Pair>();

		
		for (int i=1; i<args.length; i++) {
			String filename = args[i];
			Scanner stream = new Scanner(new File(filename));
			
			stream.next();
						
			String key_object = null;
			String value_object = null;
			
			
			while (true) {
				key_object = stream.next();
				
				if (key_object.contentEquals("}")) {
					break;
				}
				
				key_object = key_object.substring(1, key_object.length() - 2);
				
				value_object = stream.next();
				
				if (value_object.charAt(value_object.length() - 1) == ',') {
					value_object = value_object.substring(0, value_object.length() - 1);
				}
				
				Pair p = new Pair(key_object, value_object);
				
				pairs.add(p);				
			}
			
		}
		
		ArrayList<ArrayList<Pair>> list = new ArrayList<ArrayList<Pair>>();
		
		for (int i=0; i<reducers; i++) {
			list.add(new ArrayList<Pair>());
		}
		
		Collections.sort(pairs);
		
		int index = 0;
		
		String previous = null;
		
		for (Pair p : pairs) {
			
			String key = p.key;
			
			if (previous == null || previous.contentEquals(key)) {
				list.get(index).add(p);
			}
			else {
				index = (index + 1) % reducers;
				list.get(index).add(p);
			}
			
			previous = key;
		}
		
		for (int i=0; i<reducers; i++) {
			File outputFile = new File("reduce_" + i + ".intermediate.json");
			
			FileWriter stream = new FileWriter(outputFile);
			
			stream.write("{\n");
			
			for (int j=0; j<list.get(i).size(); j++) {
				stream.write("\t\"" + list.get(i).get(j).key + "\": " + list.get(i).get(j).value + (j == list.get(i).size() - 1 ? "\n" : ",\n"));
			}
			
			stream.write("}\n");
			
			stream.close();
		}
		for (ArrayList<Pair> l : list) {
			System.out.println(l);
		}
	}
	
	private static class Pair implements Comparable<Pair> {

		final String key;
		final String value;
		
		public Pair(String k, String v) {
			key = k;
			value = v;
		}

		@Override
		public int compareTo(Pair o) {
			return this.key.compareTo(o.key);
		}
		
		@Override
		public String toString() {
			return key + " " + value;
		}
		
	}
}

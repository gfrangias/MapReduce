package org.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Reducer {
	
	public static void main(String[] args) throws IOException {
		
		if (args.length != 2) {
			throw new IllegalArgumentException("Must enter 2 arugments <reducer_index> <file_name>");
		}
		
		String idx = args[0];
		String filename = args[1];
		
		Scanner stream = new Scanner(new File(filename));
		
		stream.next();
					
		String key_object = null;
		String value_object = null;
		
		Map<String, Integer> result = new HashMap<String, Integer>();
		
		
		while (true) {
			key_object = stream.next();
			
			if (key_object.contentEquals("}")) {
				break;
			}
			
			key_object = key_object.substring(1, key_object.length() - 2);
			
			value_object = stream.next();
			
			if (value_object.charAt(value_object.length() - 1) == ',') {
				value_object = value_object.substring(1, value_object.length() - 2);
			}
			else {
				value_object = value_object.substring(1, value_object.length() - 1);
			}							
			
			int value = Integer.parseInt(value_object);
			
			if (!result.containsKey(key_object)) {
				result.put(key_object, value);
			}
			else {
				result.put(key_object, result.get(key_object) + value);
			}
		}
		
		File outputFile = new File("result_" + idx + ".intermediate.json");
		
		FileWriter outputStream = new FileWriter(outputFile);
		
		ArrayList<String> keySet = new ArrayList<String>(result.keySet());
		
		outputStream.write("{\n");
		
		for (int i=0; i<keySet.size(); i++) {
			outputStream.write("\t\"" + keySet.get(i) + "\": \"" + result.get(keySet.get(i)) + "\"" + (i == keySet.size() - 1 ? "\n" : ",\n"));
		}
		
		outputStream.write("}\n");
		
		outputStream.close();
		
	}
	
}

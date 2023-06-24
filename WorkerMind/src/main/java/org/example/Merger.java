package org.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Merger {
	
    /**
     * 
     * @param args
     * args[0] ---> Output File directory & name
     * args[1...N] ---> Reduced files to merge
     * 
     * @throws IOException
     */
	public static void merge(String outputPath, String[] args) throws IOException, IllegalArgumentException {

        if (args.length < 1) {
            throw new IllegalArgumentException("Insufficient arguments...");
        }

        String file_name = outputPath;
		
		File outputFile = new File(file_name + "output.json");

		Map<String, String> result = new HashMap<String, String>();


		for (int i=0; i<args.length; i++) {
			String filename = args[i];
			Scanner stream = new Scanner(new File(filename));
			
            while(true) {

                String line = null;

                try {
                    line = stream.nextLine();
                }
                catch (Exception e)  {
                    break;
                }

                String[] pair = line.split(" ");

                if (pair.length != 2) {
                    stream.close();
                    throw new IllegalStateException("Each line should contain a key-value pair");
                }
                
                result.put(pair[0], pair[1]);
            }
            stream.close();	
		}
		
		FileWriter writer = new FileWriter(outputFile);
        
        writer.write("{\n");
        
        ArrayList<String> keySet = new ArrayList<String>(result.keySet());
        
        for (int i=0; i<keySet.size(); i++) {
        	writer.write("\t\"" + keySet.get(i) + "\": " + result.get(keySet.get(i)) + (i == keySet.size() - 1 ? "\n" : ",\n"));
        }
		
		writer.write("}\n");
		writer.close();
	}
	
}

package org.exaple;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Merger {
	
	public static void main(String[] args) throws IOException {
		
		File outputFile = new File("output.json");
        
		Map<String, String> result = new HashMap<String, String>();


		for (int i=0; i<args.length; i++) {
			String filename = args[i];
			Scanner stream = new Scanner(new File(filename));
			
            // Create a FileWriter object to write the merged content

			stream.next();
			
			
            while(true) {

                String key = stream.next();
                
                if (key.contentEquals("}")) {
                	break;
                }
                
                String value = stream.next();    
                
                if(value.charAt(value.length()-1) == ',') {
                	value = value.substring(0,value.length()-1);
                }
                
                result.put(key, value);					
            }
            
            
            stream.close();
				
		}
		
		FileWriter writer = new FileWriter(outputFile);
        
        writer.write("{\n");
        
        ArrayList<String> keySet = new ArrayList<String>(result.keySet());
        
        for (int i=0; i<keySet.size(); i++) {
        	writer.write("\t" + keySet.get(i) + " " + result.get(keySet.get(i)) + (i == keySet.size() ? "\n" : ",\n"));
        }
		
		writer.write("}\n");
		
		writer.close();
	}
	
}

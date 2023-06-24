package org.example;

import java.io.*;
import java.util.*;

public class Shuffler {
	
	public static void shuffle(String[] files, int numOfReduces, String outputPath) throws IOException {
		
		int reducers = numOfReduces;

		HashMap<String, ArrayList<Integer>> map = new HashMap<String, ArrayList<Integer>>();

		for (int i=0; i< files.length; i++) {
			String filename = files[i];
			System.out.println("Accessing file: "+filename);

			File map_file = new File(filename);

			if (map_file.exists() == false) {
				System.out.println("File does not exist...");
				System.exit(0);
			}
			
			RandomAccessFile stream = new RandomAccessFile(map_file, "r");
			
			long position = 0;
			
			main_loop: while (true) {
				
				System.out.println(position);

				byte[] data = new byte[1 << 26];
				
				if (stream.read(data) == -1) {
					break;
				}

				long key_from_position = 0;
				long key_to_position = 0;

				long value_from_position = 0;
				long value_to_position = 0;

				long last_newline = 0;

				for (int j=0; j<data.length; j++) {
					if (data[j] == 0) {
						break main_loop;
					}

					if (data[j] == 0x20) {
						key_to_position = j;
						value_from_position = j+1;
					}

					if (data[j] == 0x0A) {

						last_newline = j;

						value_to_position = j;

						byte[] k = Arrays.copyOfRange(data, (int) key_from_position, (int) key_to_position);
						String key_str = new String(k);

						byte[] v = Arrays.copyOfRange(data, (int) value_from_position, (int) value_to_position);
						String value_str = new String(v);

						key_from_position = j+1;

						if (!map.containsKey(key_str)) {
							map.put(key_str, new ArrayList<Integer>(1000));
						}
						else {
							map.get(key_str).add(Integer.parseInt(value_str));
						}

					}

					if (data[j] == 0) {
						break;
					}

				}

				position += last_newline + 1;
				
				stream.seek(position);	
			}
			
			stream.close();
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
			File outputFile = new File(outputPath + "reduce_" + i + ".intermediate");
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

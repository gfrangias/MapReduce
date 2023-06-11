package org.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * The distributor creates a list of indices and then distributes them to the workers to start processing the file uniformly.
 * @author astratakis
 * @author petroud
 * @author gfraggias
 * @author aavraam
 *
 */
public class Distributor {
	
	/**
	 * Creates a new instance of the Distributor
	 * @param filepath			The file that will be distributed
	 * @param numberOfWorkers	The total number of workers
	 * @throws IOException		If the file does not exist or an IO error occurs.
	 */
	public Distributor(final String filepath, int numberOfWorkers) throws IOException {
		this.filepath = filepath;
		
		File file = new File(filepath);
		
		if (!file.exists()) {
			throw new FileNotFoundException("Input file does not exist...");
		}
		
		// Get the size of the file in bytes
		this.fileSize = file.length();
		
		// The indices where the file will be split among workers
		indices = new long[numberOfWorkers];
		
		long S = (long) (file.length() / numberOfWorkers);
		
		for (int i=0; i<numberOfWorkers-1; i++) {
			indices[i] = (i+1) * S;
		}
		indices[numberOfWorkers-1] = file.length();
				
		RandomAccessFile reader = new RandomAccessFile(file, "r");
		
		empty_start = new boolean[numberOfWorkers];
		empty_end = new boolean[numberOfWorkers];
		
		for (int i=0; i<numberOfWorkers-1; i++) {
			reader.seek(indices[i]);
			
			int b1 = reader.read();
			int b2 = reader.read();
			
			if (b1 == 0x20 || b1 == 0x0A || b1 == 0x09) {
				empty_end[i] = true;
			}
			if (b2 == 0x20 || b2 == 0x0A || b2 == 0x09) {
				empty_start[i+1] = true;
			}
		}
		
		reader.close();
	}
	
	private final long fileSize;
	private final String filepath;
	private long[] indices;
	private boolean[] empty_start, empty_end;
	
	public String getMetadata() {
		return filepath + " - " + fileSize;
	}
	
	public long[] getIndices() {
		return indices;
	}
	
	public long numberOfDuplicates() {
		long count = 0;
		
		for (int i=0; i<empty_start.length-1; i++) {
			if ((!empty_start[i+1]) && (!empty_end[i])) {
				count += 1;
			}
		}
		
		return count;
	}
	
	@Override
	public String toString() {
		String s = "";
		for (int i=0; i<indices.length; i++) {
			s += (indices[i] + " " + empty_start[i] + " " + empty_end[i] + "\n");
		}
		return s;
	}

}

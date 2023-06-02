package org.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
public class Distributor {
	
	public Distributor(File file, long chunkSize, long id) {
		this.inputFile = file;
		CHUNK_SIZE = chunkSize;
		ID = id;
	}
	
	public final File inputFile;
	public final long CHUNK_SIZE;
	public final long ID;
	
	public boolean create_chunk_file() throws IOException {
		
		RandomAccessFile stream = new RandomAccessFile(inputFile, "r");
		
		stream.seek(CHUNK_SIZE * ID);
		
		byte[] data = new byte[CHUNK_SIZE];
		
		stream.read(data);
		stream.close();
		
		File outputFile = new File("chunked_" + ID + ".intermediate");
		FileOutputStream fos = new FileOutputStream(outputFile);
		
		fos.write(data);;
		fos.close();
		
		return true;
	}
	
	public static void main(String[] args) throws IOException, IllegalArgumentException {
		
		if (args.length != 3) {
			throw new IllegalArgumentException("Insufficient Arguments");
		}
		
		String fileName = args[0];
		int chunk_size = Integer.parseInt(args[1]);
		int id = Integer.parseInt(args[2]);
		
		Distributor chunk = new Distributor(new File(fileName), chunk_size, id);
		
		boolean status = chunk.create_chunk_file();
		
		System.out.println(status);
	}
}

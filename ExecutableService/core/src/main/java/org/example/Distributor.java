package org.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
public class Distributor {
	
	public Distributor(File file, long chunkSize, long offset, int numOfChunks) {
		this.inputFile = file;
		this.numOfChunks = numOfChunks;
		this.CHUNK_SIZE = chunkSize;
		this.offset = offset;
	}
	
	public final File inputFile;
	public final long CHUNK_SIZE;
	public final long offset;
	public final int numOfChunks;
	
	public boolean create_chunk_file() throws IOException {

		for (int i=0; i<numOfChunks;i++){
			RandomAccessFile stream = new RandomAccessFile(inputFile, "r");

			stream.seek(this.offset+(i*CHUNK_SIZE));

			byte[] data = new byte[(int) CHUNK_SIZE];

			stream.read(data);
			stream.close();

			File outputFile = new File("chunked_" + ID + ".intermediate");
			FileOutputStream fos = new FileOutputStream(outputFile);

			fos.write(data);
			fos.close();
		}

		return true;
	}
	
	public static void main(String[] args) throws IOException, IllegalArgumentException {
		
		if (args.length != 4) {
			throw new IllegalArgumentException("Insufficient Arguments");
		}
		
		String fileName = args[0];
		int chunk_size = Integer.parseInt(args[1]);
		long offset = Integer.parseInt(args[2]);
		int numOfCh = Long.parseLong(args[3]);
		
		Distributor chunk = new Distributor(new File(fileName), chunk_size, offset, numOfCh);
		
		boolean status = chunk.create_chunk_file();
		
		System.out.println(status);
	}
}

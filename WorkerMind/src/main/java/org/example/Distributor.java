package org.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Distributor {
	
	/**
	 * 
	 * @param filepath		- Input filepath
	 * @param outputPath	- Output path (without filename determined)
	 * @param chunkSize		- Chunk size
	 * @param offset		- Offset
	 * @param numOfChunks	- Number of chunks.
	 * @throws IOException
	 */
	public Distributor(String filepath, String outputPath, long chunkSize, long offset, long numOfChunks) throws IOException {

		this.inputFile = new File(filepath);

		if (!inputFile.exists()) {
			throw new IOException("File does not exist... @ Distributor (" + filepath + ")");
		}

		this.outputPath = outputPath;
		this.numOfChunks = numOfChunks;
		this.CHUNK_SIZE = chunkSize;
		this.offset = offset;
	}
	
	public final File inputFile;
	public final long CHUNK_SIZE;
	public final long offset;
	public final long numOfChunks;
	public final String outputPath;
	
	/**
	 * Creates a range of chunks determined by numOfChunks
	 * @return True on success
	 * @throws IOException On failure
	 */
	public boolean create_chunk_file() throws IOException {

		for (int i=0; i<numOfChunks;i++){
			RandomAccessFile stream = new RandomAccessFile(inputFile, "r");

			stream.seek((this.offset + i) * CHUNK_SIZE);

			byte[] data = new byte[(int) CHUNK_SIZE];

			stream.read(data);
			stream.close();

			File outputFile = new File(outputPath + "chunked_" + (offset + i) + ".intermediate");
			FileOutputStream fos = new FileOutputStream(outputFile);

			fos.write(data);
			fos.close();
		}

		return true;
	}
}

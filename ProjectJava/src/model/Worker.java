package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Worker {
	
	public final static int PAGE_SIZE = 512;
	
	public Worker(final String filepath, final long from, final long to) throws IOException {
		FileInputStream stream = new FileInputStream(new File(filepath));
		
		stream.skip(from);
		
		boolean empty = true;
		
		long current = from;
		
		while (true) {
			byte[] data = new byte[PAGE_SIZE];
			
		}
	}
	
	private ArrayList<Pair<String, Integer>> list;

}

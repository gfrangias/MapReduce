package core;

import java.io.IOException;

import model.Distributor;

/**
 * Console of the map-reduce system.
 * @author astratakis
 * @author petroud
 * @author gfraggias
 * @author aavraam
 *
 */
public class Console {
	
	public static void main(String[] args) throws IOException {
		
		
		Distributor dist = new Distributor("src/input/shakespeer.txt", 8);
		
		System.out.println(dist);
		
		System.out.println(dist.numberOfDuplicates());
		
	}

}

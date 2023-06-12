package org.example;

import java.io.*;
import java.util.*;

public class Mapper {
	public static void main(String[] args) throws IOException {
		String inputFile = args[0];
		FileInputStream fis = new FileInputStream(inputFile);

		while (true) {
			int c = fis.read();

			if (c == -1) {
				break;
			}

			System.out.println((char) c + " 1");
		}
	}
}

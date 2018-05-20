package main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import transcription.FuzzyDictionary;

public class LevenshteinTest {

	public static void main(String args[]) {
		FuzzyDictionary fuzzyDictionary = new FuzzyDictionary(
				new File(
						"C:\\Users\\Study\\Documents\\HOME\\OCR\\ocr\\dictionary\\dictionary-10000.txt"));
		List<String> strings = fuzzyDictionary.getWordsWithinN(2, "hello");
		
		for (String string : strings) {
			System.out.println(string);
		}
		
		System.out.println("hello : hell ::: expect : 1, actual : " + fuzzyDictionary.getLevDist("hello", "hell "));
		System.out.println("hello : hell ::: expect : 4, actual : " + fuzzyDictionary.getLevDist("hello", " hell"));
		System.out.println("hi : bye ::: expect : 3, actual : " + fuzzyDictionary.getLevDist("hi ", "bye"));
		System.out.println("hi : bye ::: expect : 3, actual : " + fuzzyDictionary.getLevDist(" hi", "bye"));
		System.out.println("hi: a ::: expect : 2, actual : " + fuzzyDictionary.getLevDist("hi", " a"));
		System.out.println("hi: a ::: expect : 2, actual : " + fuzzyDictionary.getLevDist("hi", "a "));
		
		
		ArrayList<String> strings2 = new ArrayList<String>();
		strings2.add("couxse");
		strings2.add("couxqe");
		strings2.add("couvse");
		strings2.add("couqse");
		strings2.add("coaxse");
		strings2.add("couvqe");
		strings2.add("couxbe");
		strings2.add("couqqe");
		strings2.add("coaxqe");
		strings2.add("coavse");
		strings2.add("coaqse");
		strings2.add("couqbee");
		strings2.add("couxbe");
		
		/*
		//so why is it returning IRAQ??? 
		System.out.println(fuzzyDictionary.getLevDist("coaqse", "iraq  "));
		System.out.println(fuzzyDictionary.getLevDist("coaqse", " iraq "));
		System.out.println(fuzzyDictionary.getLevDist("coaqse", "  iraq"));
		
		System.out.println(fuzzyDictionary.getLevDist("iraq  ", "coaqse"));
		System.out.println(fuzzyDictionary.getLevDist(" iraq ", "coaqse"));
		System.out.println(fuzzyDictionary.getLevDist("  iraq", "coaqse"));
		*/
		
		
		
		System.out.println(fuzzyDictionary.getLevDist("ferwaldf", "aerial  "));
		
		System.out.println(fuzzyDictionary.getLevDist("taughw", "take  "));
		
		//so getLevDist is working?
		
		
		
	}

}

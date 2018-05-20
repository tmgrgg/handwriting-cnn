package transcription;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/*
 * DEPRECATED
 * 
 */
public class DictionaryAnalyser {
	
	//'positions shared by word/length of word x probability that each character in the target word is realised 

	List<String> dictionary;

	public DictionaryAnalyser(List<String> dictionary) {
		this.dictionary = dictionary;
	}

	public DictionaryAnalyser(File dictionaryFile) {
		Scanner scanner;
		List<String> newDictionary = new ArrayList<String>();
		try {
			scanner = new Scanner(dictionaryFile).useDelimiter("\n");
			while (scanner.hasNext()) {
				String s = scanner.next();
				newDictionary.add(s);
			}
		} catch (FileNotFoundException e) {
			System.out.println("Failed to generate dictionary");
		}
		this.dictionary = newDictionary;
	}

	// HACK
	// Take a total fuzzyString and split into substrings to do word matching
	public String parse(List<FuzzyChar> fuzzyString) {
		String out = "";
		List<FuzzyChar> toBeMatched = new ArrayList<FuzzyChar>();
		for (FuzzyChar fuzzyChar : fuzzyString) {
			if (fuzzyChar.realise(0) != " " && fuzzyChar.realise(0) != "\n") {
				toBeMatched.add(fuzzyChar);
			} else {
				out += getMostLikelyMatch(toBeMatched);
				out += fuzzyChar.realise(0);
				toBeMatched.clear();
			}
		}
		return out;
	}

	//ALGORITHM BREAKS IF WE PUMP UP THIS NUMBER... WONDER WHY?
	int LIMIT = 20;

	// Parse fuzzy string into real string
	private String getMostLikelyMatch(List<FuzzyChar> fuzzyString) {
		for (int n = 0; n < LIMIT; n++) {
			System.out.println(n);
			String poss = realise(fuzzyString, n);
			System.out.println(poss);
			if (dictionary.contains(poss)) {
				System.out.println(dictionary.contains(poss));
				return poss;
			}
		}
		return realise(fuzzyString, 0);
	}

	/*
	 * //TODO: make it so that the n-1th position vector is held in a static
	 * variable so that if we want the next one //we don't have to recompute
	 * completely // Returns the n^th most probable realisation of the
	 * fuzzyString private String realise(List<FuzzyChar> fuzzyString, int n) {
	 * int[] positions = new int[fuzzyString.size()];
	 * 
	 * 
	 * //Get the FuzzyChar distribution positions corresponding to the n^th most
	 * probably realisation of the fuzzyString while (n > 0) { int col = 0; for
	 * (int i = 0; i < positions.length; i++) { double quotient_1 =
	 * (fuzzyString.get(col).getP(positions[col] +
	 * 1))/fuzzyString.get(col).getP(positions[col]); double quotient_2 =
	 * (fuzzyString.get(i).getP(positions[i] +
	 * 1))/fuzzyString.get(i).getP(positions[i]); if (quotient_1 < quotient_2) {
	 * col = i; } } positions[col]++; n--; }
	 * 
	 * 
	 * //Realise the string according to these positions List<String>
	 * outputCharacters = new ArrayList<String>(); for (int i = 0; i <
	 * fuzzyString.size(); i++) {
	 * outputCharacters.add(fuzzyString.get(i).realise(positions[i])); } String
	 * s = ""; for (String o : outputCharacters) { s += o; } return s; }
	 */

	
	//THIS ALGORITHM IS WRONG... FUCK
	// TODO: This algorithm is dangerous and needs revision
	private String realise(List<FuzzyChar> fuzzyString, int n) {
		int[] positions = new int[fuzzyString.size()];

		// Get the FuzzyChar distribution positions corresponding to the n^th
		// most probably realisation of the fuzzyString
		while (n > 0) {

			int col = 0;
			int maxDepth = fuzzyString.get(0).size() - 1;
			for (int i = 0; i < positions.length; i++) {
				if (positions[col] < maxDepth) {
					col = i;
					break;
				}
			}

			for (int i = col + 1; i < positions.length; i++) {
				if (positions[i] < maxDepth) {
					double quotient_1 = (fuzzyString.get(col)
							.getP(positions[col] + 1))
							/ fuzzyString.get(col).getP(positions[col]);
					double quotient_2 = (fuzzyString.get(i)
							.getP(positions[i] + 1))
							/ fuzzyString.get(i).getP(positions[i]);
					if (quotient_1 < quotient_2) {
						col = i;
					}
				}
			}

			positions[col]++;
			n--;
		}

		// Realise the string according to these positions
		List<String> outputCharacters = new ArrayList<String>();
		for (int i = 0; i < fuzzyString.size(); i++) {
			outputCharacters.add(fuzzyString.get(i).realise(positions[i]));
		}
		String s = "";
		for (String o : outputCharacters) {
			s += o;
		}
		return s;
	}
}

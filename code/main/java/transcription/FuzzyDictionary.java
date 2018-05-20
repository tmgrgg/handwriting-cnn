package transcription;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

/*A CLASS WHOSE JOB IS TO REALISE A FUZZYSTRING 
 * 
 * i.e. convert a probability distribution into a page of writing
 *                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        bn
 */


public class FuzzyDictionary {

	// 'positions shared by word/length of word x probability that each
	// character in the target word is realised

	List<String> dictionary;

	public FuzzyDictionary(List<String> dictionary) {
		this.dictionary = dictionary;
	}

	public FuzzyDictionary(File dictionaryFile) {
		Scanner scanner;
		List<String> newDictionary = new ArrayList<String>();
		try {
			scanner = new Scanner(dictionaryFile).useDelimiter("\r\n");
			while (scanner.hasNext()) {
				String s = scanner.next();
				newDictionary.add(s);
			}
		} catch (FileNotFoundException e) {
			System.out.println("Failed to generate dictionary");
		}
		this.dictionary = newDictionary;

		// for some reason the first word is problematic (odd trailing characters)
		this.dictionary.remove(0);
	}

	private int LevenshteinLimit = 2;
	private double levenshteinFactor = 0.3;

	private int n_limit = 100;
	private int m_limit = 2;

	// Scheme for translating a single word:
	// get the n most likely possibilities
	// if any of these is a word then choose the most likely from this list
	// otherwise use this list as a base to do Levenshtein calculations on...
	// i.e. find the words within levenshtein distance 1 of the first n/m words
	// and find the words within levenshtein distance 2 of the first n/m^2 words
	// then...
	public String translateWordByFrequency(FuzzyString fuzzyString) {
		List<String> possibilities = fuzzyString.morphMatrixGetNMostLikely(n_limit);;
		for (String s : possibilities) {
			System.out.println(s + " : " + fuzzyString.getP(s));
			if (contains(s)) {
				return s;
			}
		}

		PriorityStringCounter fin = new PriorityStringCounter();
		List<String> levposs;
		LevenshteinLimit = Math.max(1, (int) Math.round(fuzzyString.size()*levenshteinFactor));
		for (int i = 1; i <= LevenshteinLimit; i++) {
			int limit = (int) (n_limit / Math.round((Math.pow(m_limit, i))));
			for (int j = 0; j < limit; j++) {
				if (j >= possibilities.size()) {
					break;
				}
				String s = possibilities.get(j);
				levposs = getWordsWithinN(i, s);
					for (String sLev : levposs) {
						fin.add(sLev);
				}
			}
		}
		
		// no matches... then return the most likely "nonsense" word
		if (!fin.isEmpty()) {
			return fin.getMostCommon();
		}

		return possibilities.get(0);
	}
	
	public String translateWordByScore(FuzzyString fuzzyString) {
		List<String> possibilities = fuzzyString.morphMatrixGetNMostLikely(n_limit);

		for (String s : possibilities) {
			System.out.println(s + " : " + fuzzyString.getP(s));
			if (contains(s)) {
				return s;
			}
		}


		PriorityQueue<ScoreString> fin = new PriorityQueue<ScoreString>();
		List<String> levposs;
		LevenshteinLimit = Math.max(1, (int) Math.round(fuzzyString.size()*levenshteinFactor));
		for (int i = 1; i <= LevenshteinLimit; i++) {
			int limit = (int) (n_limit / Math.round((Math.pow(m_limit, i))));
			for (int j = 0; j < limit; j++) {
				if (j >= possibilities.size()) {
					break;
				}
				String s = possibilities.get(j);
				levposs = getWordsWithinN(i, s);
					for (String sLev : levposs) {
						ScoreString nextString = new ScoreString(fuzzyString.getPScore(sLev), sLev);
						if(!fin.contains(nextString)) {
						 fin.add(nextString);
						}
				}
			}
		}
		
		// no matches... then return the most likely "nonsense" word
		if (!fin.isEmpty()) {
			return fin.poll().s;
		}

		return possibilities.get(0);
	}

	private class ScoreString implements Comparable {
		private double p;
		private String s;

		public ScoreString(double p, String s) {
			this.p = p;
			this.s = s;
			// equality is based on the string
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof ScoreString)) {
				return false;
			}

			ScoreString o = (ScoreString) other;

			return s.equals(o.s);
		}

		@Override
		public int hashCode() {
			return s.hashCode();
		}

		public int compareTo(Object other) {
			// only to be compared to probableString
			ScoreString o = (ScoreString) other;
			if (p < o.p) {
				return 1;
			}
			if (p == o.p) {
				return 0;
			}
			return -1;
		}
	}

	// Turn a fuzzyString into a real String
	public String parse(FuzzyString fuzzyString) {
		// System.out.println(fuzzyString.size());

		// check if anything except the last character is a space or newline...

		// if it is... split the string
		for (int i = 0; i < fuzzyString.size() - 1; i++) {
			if (fuzzyString.get(i).realise(0).equals(" ")
					|| fuzzyString.get(i).realise(0).equals("\n")) {
				FuzzyString head = fuzzyString.splitHead(i);
				FuzzyString tail = fuzzyString.splitTail(i);
				return parse(head) + fuzzyString.get(i).realise(0)
						+ parse(tail);
			}
		}

		//translateWordByProbability
		return translateWordByScore(fuzzyString);
	}

	public List<String> getWordsWithinN(int n, String s) {
		ArrayList<String> words = new ArrayList<String>();
		for (String word : dictionary) {
			int diff = s.length() - word.length();
			if (Math.abs(diff) > n) {
				// do nothing
			} else if (diff == 0) {
				if (getLevDist(s, word) == n) {
					words.add(word);
				}
			} else {

				// otherwise the words are not the same length but could be
				// substrings...
				String lString, sString;
				if (diff > 0) {
					sString = word;
					lString = s;
				} else {
					sString = s;
					lString = word;
				}

				// pad the shorter word with 0 to diff places it needs padding
				// to
				// compute the correct
				// distance

				int maxSpaces = Math.abs(diff);
				for (int i = 0; i <= maxSpaces; i++) {
					String nString = StringUtils.leftPad(sString,
							sString.length() + i);
					nString = StringUtils.rightPad(nString, nString.length()
							+ (maxSpaces - i));
					if (getLevDist(nString, lString) == n) {
						words.add(word);
					}
				}
			}
		}
		return words;
	}

	// assume that the words are the same length
	public int getLevDist(String s1, String s2) {
		int count = 0;
		for (int i = 0; i < s1.length(); i++) {
			if (s1.charAt(i) != s2.charAt(i)) {
				count++;
			}
		}
		return count;
	}

	// THIS NEEDS TO BE USED
	// in translate instead of dictionary.contains
	private String endChars = ".,:;!?)'";
	private String startChars = "('";

	private boolean contains(String s) {
		String firstChar = "" + s.charAt(0);
		String lastChar = "" + s.charAt(s.length() - 1);
		String fin = s;
		if (startChars.contains(firstChar)) {
			fin = fin.substring(1, fin.length() - 1);
		}
		if (endChars.contains(lastChar)) {
			fin = fin.substring(0, fin.length() - 2);
		}
		return dictionary.contains(fin);
	}

	private class PriorityStringCounter {
		List<String> strings = new ArrayList<String>();
		List<Integer> counts = new ArrayList<Integer>();

		void add(String s) {
			if (strings.contains(s)) {
				int index = strings.indexOf(s);
				counts.set(index, counts.get(index) + 1);
			} else {
				strings.add(s);
				counts.add(1);
			}
		}

		boolean isEmpty() {
			return strings.isEmpty();
		}

		String getMostCommon() {
			int max = 0;
			for (int j = 0; j < counts.size(); j++) {
				if (counts.get(j) > counts.get(max)) {
					max = j;
				}
			}
			return strings.get(max);
		}

		public String toString() {
			String s = "";
			for (String string : strings) {
				s += string + " with count "
						+ counts.get(strings.indexOf(string)) + "\n";
			}
			return s;
		}
	}
}

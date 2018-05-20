package transcription;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.commons.lang3.StringUtils;

public class FuzzyString {

	List<FuzzyChar> fuzzyString;

	public FuzzyString(List<FuzzyChar> fuzzyString) {
		this.fuzzyString = fuzzyString;
	}


	// Returns the n most likely strings realised by this fuzzyString, in order
	public List<String> getNMostLikely(int n) {
		List<SortAid> pladder = new ArrayList<SortAid>();
		for (int pos = 0; pos < fuzzyString.size(); pos++) {
			FuzzyChar fuzzyChar = fuzzyString.get(pos);
			for (int i = 0; i < fuzzyChar.size(); i++) {
				pladder.add(new SortAid(fuzzyChar.realise(i),
						fuzzyChar.getP(i), pos));
			}
		}

		// Sort the list in order of probability
		for (int i = 0; i < pladder.size() - 1; i++) {
			for (int j = 0; j < pladder.size() - 1; j++) {
				if (pladder.get(j).probability < pladder.get(j + 1).probability) {
					SortAid prev = pladder.get(j);

					pladder.set(j, pladder.get(j + 1));
					pladder.set(j + 1, prev);
				}
			}
		}

		// pladder is now sorted in order of probability... we now build
		// sequences with decreasing probability... (see whiteboard for proof of
		// why this is possible)

		List<ArrayList<String>> strings = new ArrayList<ArrayList<String>>();
		ArrayList<String> initString = new ArrayList<String>();
		for (int i = 0; i < fuzzyString.size(); i++) {
			initString.add(fuzzyString.get(i).realise(0));
		}

		strings.add(new ArrayList<String>(initString));

		// the list has been initialised to contain the most probable string

		// now, add all other strings in order of probability
		outerLoop: for (SortAid rung : pladder) {
			List<ArrayList<String>> newStrings = new ArrayList<ArrayList<String>>();
			for (ArrayList<String> string : strings) {
				if (strings.size() >= n) {
					break outerLoop;
				}
				ArrayList<String> newString = new ArrayList<String>(string);
				newString.set(rung.position, rung.character);
				if (!strings.contains(newString)
						&& !newStrings.contains(newString)) {
					newStrings.add(newString);
				}
			}
			strings.addAll(newStrings);
		}

		// build the final string from the string characters (fuzzyChar
		// characters are Strings to allow for more complex characters
		// (integrals and such))
		List<String> actualStrings = new ArrayList<String>();

		for (ArrayList<String> string : strings) {
			String s = "";
			for (String s_ : string) {
				s += s_;
			}
			actualStrings.add(s);
		}

		return actualStrings;
	}

	private double prob_limit = 0.00001;

	public List<String> morphMatrixGetNMostLikely(int n) {
		// construct morphMatrix

		// inner list is a column
		ArrayList<ArrayList<Double>> morphMatrix = new ArrayList<ArrayList<Double>>();
		ArrayList<Double> column;
		for (int i = 0; i < fuzzyString.size(); i++) {
			column = new ArrayList<Double>();
			FuzzyChar fuzzyChar = fuzzyString.get(i);
			for (int j = 0; j < fuzzyChar.size() - 1; j++) {
				if (fuzzyChar.getP(j) <= prob_limit) {
					// don't want to explore extremely low prob moves
					break;
				}
				column.add(fuzzyChar.getP(j + 1) / fuzzyChar.getP(j));
			}
			morphMatrix.add(column);
		}

		// morphMatrix has now been created... morphMatrix.get(j).get(i) gets
		// the i,j^th element

		List<MorphString> strings = new ArrayList<MorphString>();
		PriorityQueue<PIndex> pq = new PriorityQueue<PIndex>();

		ArrayList<Integer> positions = new ArrayList<Integer>();
		for (int i = 0; i < fuzzyString.size(); i++) {
			positions.add(0);
		}

		MorphString firstString = new MorphString(positions, 1.0, morphMatrix);

		strings.add(firstString);
		for (int i = 0; i < positions.size(); i++) {

			// reasoning: if there are morphMatrix.get(i).size() changes of
			// position available
			// positions.get(i) corresponds to the number of times I have
			// changed position
			if (firstString.positions.get(i) < morphMatrix.get(i).size()) {
				pq.add(new PIndex(0, i, strings.get(0).getMvalFor(i)));
			}
		}

		// main loop
		PIndex candidate;
		MorphString newString;
		for (int r = 0; r < n; r++) {
			if (pq.isEmpty()) {
				break;
			}
			candidate = pq.poll();
			newString = strings.get(candidate.stringIndex).nextString(
					candidate.charIndex);

			// Early stopping
			if (newString.m_val <= prob_limit) {
				break;
			}

			if (!strings.contains(newString)) {
				strings.add(newString);
				for (int j = 0; j < fuzzyString.size(); j++) {
					if (newString.positions.get(j) < morphMatrix.get(j).size()) {
						// Stops us from trying to update characters that
						// can't be updated anymore.
						pq.add(new PIndex(strings.size() - 1, j, newString
								.getMvalFor(j)));
					}
				}
			}
		}

		// Check ordering before continuing with method
		List<String> actualStrings = new ArrayList<String>();

		for (MorphString s : strings) {
			String aS = s.intoString();
			actualStrings.add(aS);
			// System.out.println(aS + " : " + getP(aS) + " : MVAL : " +
			// s.m_val);
		}
		return actualStrings;
	}

	// private double P_NUM_LETR_WRONG = 0.5;

	// PScore is the summative score of the probability used for words that
	// otherwise would have 0 probability

	public double getPScore(String s) {
		// diff > 0 => fuzzyString is longer than string diff < 0 => opposite =
		// 0 => same length
		int diff = this.size() - s.length();
		int lim = this.size();
		if (diff > 0)
			lim = s.length();

		// slide the distribution over the possible locations of the string...
		// since the string is not necessarily the same length.
		double score = 0;
		double score_temp = 0;

		for (int j = 0; j <= Math.abs(diff); j++) {
			score_temp = 0;
			for (int i = 0; i < lim; i++) {
				if (diff > 0) {
				score_temp += get(i + j).getP(s.charAt(i));
				} else {
				score_temp += get(i).getP(s.charAt(i + j));
				}
			}
			if (score_temp > score) {
				score = score_temp;
			}
		}
		
		//take into account the length of the word: multiply by the ratio of the difference
		if (this.size() > s.length()) {
			return score * ((double) s.length()/(double) this.size());
		}
		
		return score * ((double) this.size()/(double) s.length());
	}

	public double getP(String h) {
		// get the shortest string
		int l = h.length();
		if (h.length() > fuzzyString.size()) {
			l = fuzzyString.size();
		}
		double p = 1;
		for (int i = 0; i < l; i++) {
			p *= fuzzyString.get(i).getP("" + h.charAt(i));
		}
		return p;
	}

	public FuzzyChar get(int i) {
		return fuzzyString.get(i);
	}

	public int size() {
		return fuzzyString.size();
	}

	// split the string up to position i
	public FuzzyString splitHead(int i) {
		return new FuzzyString(fuzzyString.subList(0, i));
	}

	public FuzzyString splitTail(int i) {
		return new FuzzyString(fuzzyString.subList(i + 1, fuzzyString.size()));

	}

	private class SortAid {
		String character;
		double probability;
		int position;

		private SortAid(String character, double probability, int position) {
			this.character = character;
			this.probability = probability;
			this.position = position;
		}
	}

	private class MorphString {
		ArrayList<ArrayList<Double>> morphMatrix;
		ArrayList<Integer> positions;
		double m_val;

		MorphString(ArrayList<Integer> positions, double m_val,
				ArrayList<ArrayList<Double>> morphMatrix) {
			this.positions = positions;
			this.m_val = m_val;
			this.morphMatrix = morphMatrix;
		}

		public String intoString() {
			String s = "";
			for (int i = 0; i < fuzzyString.size(); i++) {
				s += fuzzyString.get(i).realise(positions.get(i));
			}
			return s;
		}

		public String toString() {
			return intoString();
		}

		// returns the mval for morphing this character
		public double getMvalFor(int i) {
			if (positions.get(i) >= morphMatrix.get(i).size()) {
				return 0;
			}
			return m_val * morphMatrix.get(i).get(positions.get(i));
		}

		// returns the next string resulting from updating character i
		// PRE: should not be trying to update a character that's at the end!
		public MorphString nextString(int i) {
			ArrayList<Integer> newPositions = new ArrayList<Integer>(positions);
			newPositions.set(i, newPositions.get(i) + 1);
			return new MorphString(newPositions, getMvalFor(i), morphMatrix);
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof MorphString) {
				return positions.equals(((MorphString) o).positions);
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return positions.hashCode();
		}
	}

	private class PIndex implements Comparable {
		int stringIndex;
		int charIndex;
		double m_val;

		PIndex(int stringIndex, int charIndex, double m_val) {
			this.stringIndex = stringIndex;
			this.charIndex = charIndex;
			this.m_val = m_val;
		}

		public int compareTo(Object other) {
			// Can always assume that we compared PIndex to PIndex... should not
			// be used outside this class!
			PIndex o = (PIndex) other;
			if (m_val < o.m_val) {
				return 1;
			}
			if (m_val == o.m_val) {
				return 0;
			}
			return -1;
		}

		public String toString() {
			return "[(" + stringIndex + ", " + charIndex + "), " + m_val + "]";
		}
	}
}

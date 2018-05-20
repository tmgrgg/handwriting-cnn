package transcription;

import java.util.ArrayList;
import java.util.List;

import classifier.ImageClass;

//SORTED BY PROBABILITY SCORE
public class FuzzyChar {

	public enum constants {
		SPACE, NEWLINE;
	}

	List<String> characters;
	List<Double> distribution;

	// Constants
	public FuzzyChar(constants constant) {
		this.characters = new ArrayList<String>();
		this.distribution = new ArrayList<Double>();

		switch (constant) {
		case SPACE:
			this.characters.add(" ");
			this.distribution.add(1.0);
			break;

		case NEWLINE:
			this.characters.add("\n");
			this.distribution.add(1.0);
			break;
		}
	}

	public FuzzyChar(List<ImageClass> imageClasses, List<Double> distribution) {
		this.characters = new ArrayList<String>();
		for (ImageClass imageClass : imageClasses) {
			this.characters.add(imageClass.toString());
		}
		this.distribution = new ArrayList<Double>(distribution);
		sortDistribution();
	}
	
	//NAIVE SORT DISTRIBUTION
	private void sortDistribution() {
		
		for (int i = 0; i < characters.size() - 1; i++) {
			for (int j = 0; j < characters.size() - 1; j++) {
				if (distribution.get(j) < distribution.get(j + 1)) {
					Double prev_double = distribution.get(j);
					String prev_character = characters.get(j);

					distribution.set(j, distribution.get(j + 1));
					characters.set(j, characters.get(j + 1));
					distribution.set(j + 1, prev_double);
					characters.set(j + 1, prev_character);
				}
			}
		}
	}

	// Returns the n^th highest probability realisation for this character
	// (indexed from 0)
	public String realise(int n) {
		return characters.get(n);
	}
	
	public int size() {
		return characters.size();
	}
	
	// Returns the n^th highest value in the distribution
	public double getP(int n) {
		return distribution.get(n);
	}
	
	public double getP(String c) {
		int index = characters.indexOf(c);
		if (index == -1) return 0;
		else return distribution.get(index);
	}
	
	public double getP(char c) {
		return getP("" + c);
	}

	public String toString() {
		String s = "";
		for (int i = 0; i < characters.size(); i++) {
			s += characters.get(i) + " : " + distribution.get(i) + "   ";
			if ((i + 1) % 2 == 0)
				s += "\n";
		}
		return s;
	}
}

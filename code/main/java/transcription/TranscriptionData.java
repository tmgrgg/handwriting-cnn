package transcription;

import java.util.ArrayList;
import java.util.List;

import classifier.FuzzyClassifier;
import segmenter.Segment;

public class TranscriptionData {
	
	//idea: have parsers take translation data and return translation data
	//long term: slowly and recursively build up a picture of what the 
    //segments are and their divisions on the page.
	
	
	//segments: ordered list of segments
	//lineDividers: indices (from 0) of the array positions of the last character in each line
	//wordDividers: indices (from 0) of the array positions of the last character in each word
	
	List<Segment> segments;
	List<Integer> lineDividers;
	List<Integer> wordDividers;
	
	public TranscriptionData(List<Segment> segments) {
		this.segments = segments;
	}
	
	public TranscriptionData(List<Segment> segments, List<Integer> lineDividers, List<Integer> wordDividers) {
		this.segments = segments;
		this.lineDividers = lineDividers;
		this.wordDividers = wordDividers;
	}
	
	public List<Segment> getSegments() {
		return segments;
	}
	
	public List<Integer> getLineDividers() {
		return lineDividers;
	}
	
	public List<Integer> getWordDividers() {
		return wordDividers;
	}
	
	public int getAverageSegmentHeight() {
		int sum = 0;
		for (Segment segment : segments) {
			sum += (segment.getBottom() - segment.getTop());
		}
		
		return sum/segments.size();
	}
	
	public int getAverageSegmentWidth() {
		int sum = 0;
		for (Segment segment : segments) {
			sum += (segment.getRight() - segment.getLeft());
		}
		
		if(segments.size() == 0) {
			System.out.println("There are no segments... something went wrong in the preprocessing stage");
		}
		
		return sum/segments.size();
	}
	
	//Should only be called after lineDividers and wordDividers have been initially constructed and
	//segments has been sorted correspondingly
	public List<FuzzyChar> getFuzzyString(FuzzyClassifier classifier) {
		List<FuzzyChar> fuzzyString = new ArrayList<FuzzyChar>();
		for (int i = 0; i < segments.size(); i++) {
			FuzzyChar fuzzyChar = classifier.fuzzyClassify(segments.get(i).getImage());
			fuzzyString.add(fuzzyChar);
			if (lineDividers.contains(i) && i != segments.size() - 1) {
				// we don't want to add a newline at the end, but there is one there which we used
				//in word parsing
				fuzzyString.add(new FuzzyChar(FuzzyChar.constants.NEWLINE));
			}
			if (wordDividers.contains(i)) {
				fuzzyString.add(new FuzzyChar(FuzzyChar.constants.SPACE));
			}
		}
		return fuzzyString;
	}
	
	//returns the output as a list of lines which itself is a list of fuzzy strings
	public ArrayList<ArrayList<FuzzyString>> getFuzzyStrings(FuzzyClassifier classifier) {
		ArrayList<ArrayList<FuzzyString>> lines = new ArrayList<ArrayList<FuzzyString>>();
		
		ArrayList<FuzzyString> curLine = new ArrayList<FuzzyString>();		
		ArrayList<FuzzyChar> curWord;
		List<Segment> curWordSegs;
		int start = 0;
		for(Integer wordEnd : wordDividers) {
			curWord = new ArrayList<FuzzyChar>();
			curWordSegs = segments.subList(start, wordEnd + 1);
			for (Segment seg : curWordSegs) {
				curWord.add(classifier.fuzzyClassify(seg.getImage()));
			}
			
			curLine.add(new FuzzyString(curWord));
			
			if (lineDividers.contains(wordEnd)) {
			lines.add(curLine);
			curLine = new ArrayList<FuzzyString>();
			}
			
			start = wordEnd + 1;
		}
		return lines;
	}
	
	public TranscriptionData copyOf() {
		return new TranscriptionData(new ArrayList<Segment>(segments), new ArrayList<Integer>(lineDividers), new ArrayList<Integer>(wordDividers));
	}
}

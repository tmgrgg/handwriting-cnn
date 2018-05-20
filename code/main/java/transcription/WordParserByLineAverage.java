package transcription;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//Infers word divide positions assuming line positions and segment ordering has already occurred
public class WordParserByLineAverage implements Parser {

	public TranscriptionData parse(TranscriptionData data) {
		List<Integer> newWordDividers = new ArrayList<Integer>();
		int sum = 0;

		Iterator<Integer> iterator = data.lineDividers.iterator();

		int lineStartIndex = 0;
		while (iterator.hasNext()) {
			int lineEndIndex = iterator.next();

			for (int i = lineStartIndex; i < lineEndIndex; i++) {
				int term = data.segments.get(i + 1).getLeft()
						- data.segments.get(i).getRight();

				if (term >= 0) {
					sum += term;
				}
			}
			double average = (double) sum
					/ (double) (lineEndIndex - lineStartIndex);
		
			for (int i = lineStartIndex; i < lineEndIndex; i++) {
				int difference = data.segments.get(i + 1).getLeft()
						- data.segments.get(i).getRight();
				if (difference > average) {
					newWordDividers.add(i);
				}
			}
			newWordDividers.add(lineEndIndex);
			lineStartIndex = lineEndIndex + 1;
		}
		data.wordDividers = newWordDividers;
		return data;
	}

	private double threshold_mult = 1.2;
	private double threshold_add = 2;

	private double threshold(double x) {
		return threshold_mult * x + threshold_add;
	}

}

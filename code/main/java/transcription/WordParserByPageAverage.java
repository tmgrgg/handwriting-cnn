package transcription;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WordParserByPageAverage implements Parser {

	public TranscriptionData parse(TranscriptionData data) {
		List<Integer> newWordDividers = new ArrayList<Integer>();

		Iterator<Integer> iterator = data.lineDividers.iterator();

		int totalSum = 0;
		int lineStartIndex = 0;
		while (iterator.hasNext()) {
			int lineEndIndex = iterator.next();

			for (int i = lineStartIndex; i < lineEndIndex; i++) {
				int term = data.segments.get(i + 1).getLeft()
						- data.segments.get(i).getRight();

				if (term >= 0) {
					totalSum += term;
				}
			}
		}
		double average = (double) totalSum / (double) (data.segments.size() - 1);

		iterator = data.lineDividers.iterator();

		lineStartIndex = 0;
		while (iterator.hasNext()) {
			int lineEndIndex = iterator.next();
			
			for (int i = lineStartIndex; i < lineEndIndex; i++) {
				int difference = data.segments.get(i + 1).getLeft()
						- data.segments.get(i).getRight();
				if (difference > average) {
					newWordDividers.add(i);
				}
			}
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

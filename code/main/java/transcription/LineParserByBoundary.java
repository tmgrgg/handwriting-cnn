package transcription;

import java.util.ArrayList;
import java.util.List;

import segmenter.Segment;

//Orders TranslationData segments into line-read order.
public class LineParserByBoundary implements Parser {

	public TranscriptionData parse(TranscriptionData data) {
		findLines(data);
		return data;
	}

	private void findLines(TranscriptionData data) {
		// order TranslationData.segments left to right
		leftRightSort(data.segments);

		List<Segment> newSegments = new ArrayList<Segment>();
		List<Integer> newLineDividers = new ArrayList<Integer>();
		
		Segment current = getTopLeftSegment(data.segments);
		data.segments.remove(current);

		newSegments.add(current);
		int index = 0;
		while (!data.segments.isEmpty()) {
			boolean changed = false;
			// Find the first (left-most) segment whose
			// top is higher than current's bottom
			// that is right of the current segment
			for (Segment segment : data.segments) {
				if ((segment.getLeft() > current.getLeft())
						&& (segment.getTop() < current.getBottom())) {
					current = segment;
					changed = true;
					break;
				}
			}
			if (changed == false) {
				// We must have a new line
				newLineDividers.add(index);
				current = getTopLeftSegment(data.segments);
			}
			data.segments.remove(current);
			newSegments.add(current);
			index++;
		}

		data.segments = newSegments;
		
		//the last line ends at the end of the stream
		newLineDividers.add(data.segments.size() - 1);
		data.lineDividers = newLineDividers;
	}

	// Depends on the segments being ordered
	private Segment getTopLeftSegment(final List<Segment> segments) {
		Segment highest = segments.get(0);
		for (Segment segment : segments) {
			if (segment.getTop() < highest.getTop()) {
				highest = segment;
			}
		}
		boolean changed = true;
		while (changed == true) {
			changed = false;
			for (Segment segment : segments) {
				if ((segment.getLeft() < highest.getLeft())
						&& (segment.getTop() < highest.getBottom())
						&& (!segment.equals(highest))) {
					highest = segment;
					changed = true;
					break;
				}
			}
		}
		return highest;
	}

	// Sort into left and right precedence TODO: change to quick sort
	private void leftRightSort(List<Segment> segments) {
		Segment temp;
		for (int j = 0; j < segments.size() - 1; j++) {
			for (int i = 0; i < segments.size() - 1; i++) {
				if (segments.get(i).getLeft() > segments.get(i + 1).getLeft()) {
					temp = segments.get(i);
					segments.set(i, segments.get(i + 1));
					segments.set(i + 1, temp);
				}
			}
		}
	}
}

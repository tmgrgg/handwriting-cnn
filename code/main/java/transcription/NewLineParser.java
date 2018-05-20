package transcription;

import java.util.ArrayList;
import java.util.List;

import segmenter.Segment;

/*
 * 1. Do line segmentation first by refining and using th
 * 		walk-down algorithm
 * 
 * 2. As we walk, if the next character is relatively small in height
 * 		say, a threshold of 0.5, then use the previous characters
 * 		bounds to search for the next character in line
 * 
 * 3. Walk-down: if next horizontal character has vertical midpoint within
 * 		current char's top and bottom boundaries, then add it to the line
 * 
 * 4. Once lines have been decided - join disconnected components: 
 * 		if two horizontally adjacent components in the same line have no vertical overlap
 * 			join them (need to resolve the groupings, i.e. choose the closest
 * 			components and only group the item once).
 * 
 * NOTE: this does not work if the line begins with an unusually small segment
 *       like an i-dot... need to fix this. 
 *       
 *       CHANGELOG:
 *       
 *       Using the walk-down scheme based on midpoints between previous top and bottom
 *       boundaries results in a weakness with long-tailed letters.
 *       
 *       Try adjusting the top and bottom boundaries to current top/bottom +- 1/3 average segment height
 *       
 *       
 *       
 *       FINAL SOLUTION: The overlap problem is just a bit awkward really...
 *       
 *       If we try to account for overlapping letters... we potentially lose letters that we want on a line.
 *       
 *       
 *       Best solution currently is to either use horizontal histogram minima which is... shit, because it doesn't allow 
 *       for slanted lines...
 *       
 *       or to use the walk-down algorithm with the next letter criteria being left-most letter whose top is above the current's bottom
 *
 *       This works for slanted lines but doesn't solve the overlap problem very well at all...
 *       
 *       so we must enforce a well-formedness in that there needs to be a clear difference between lines.
 *
 */

//Orders TranslationData segments into line-read order.
public class NewLineParser implements Parser {

	double HEIGHT_THRESHOLD = 1;

	public TranscriptionData parse(TranscriptionData data) {
		findLines(data);
		//connectComponents(data);
		return data;
	}

	// 4. To be used after lines have been decided
	private void connectComponents(TranscriptionData data) {
		// get all adjacent segments in the same line that have no overlap and
		// vertical displacement
		// group them basedo on distance - groups should be mutually exclusive

		ArrayList<Segment> newSegments = new ArrayList<Segment>();
		ArrayList<Integer> newLineDividers = new ArrayList<Integer>();

		int lineStart = 0;

		for (Integer lineEnd : data.lineDividers) {
			for (int i = lineStart; i < lineEnd; i++) {
				Segment firstSeg = data.segments.get(i);
				Segment secondSeg = data.segments.get(i + 1);

				if (checkSegOverlap(firstSeg, secondSeg)) {
					// same line segments do not overlap vertically
					// need to join
					if ((i + 1) == lineEnd) {
						// join the first and second seg
						data.segments.remove(i);
						data.segments.remove(i + 1);
						data.segments.add(i, firstSeg.join(secondSeg));
					} else {
						Segment thirdSeg = data.segments.get(i + 2);
						if (!checkSegOverlap(secondSeg, thirdSeg)) {
							// join the first and second seg
							data.segments.remove(i);
							data.segments.remove(i + 1);
							data.segments.add(i, firstSeg.join(secondSeg));
						} else {
							// join the second segment with closest segment
							// horizontally
							int firstMidpoint = (firstSeg.getRight() - firstSeg
									.getLeft()) / 2 + firstSeg.getLeft();
							int secondMidpoint = (secondSeg.getRight() - secondSeg
									.getLeft()) / 2 + secondSeg.getLeft();
							int thirdMidpoint = (thirdSeg.getRight() - thirdSeg
									.getLeft()) / 2 + thirdSeg.getLeft();
							if (Math.abs(secondMidpoint - firstMidpoint) <= (thirdMidpoint - secondMidpoint)) {
								// join second and first segments
								data.segments.remove(i);
								data.segments.remove(i + 1);
								data.segments.add(i, firstSeg.join(secondSeg));
							} else {
								// join second and third segments
								data.segments.remove(i + 1);
								data.segments.remove(i + 2);
								data.segments.add(i + 1,
										secondSeg.join(thirdSeg));
							}
						}
					}
				}
			}
			lineStart = lineEnd + 1;
		}
	}

	private boolean checkSegOverlap(Segment seg1, Segment seg2) {
		return seg1.getTop() < seg2.getBottom()
				&& seg1.getBottom() > seg2.getTop();

	}

	// lineDividers represent the end of each line
	private void findLines(TranscriptionData data) {
		// order TranslationData.segments left to right
		leftRightSort(data.segments);

		List<Segment> newSegments = new ArrayList<Segment>();
		List<Integer> newLineDividers = new ArrayList<Integer>();

		int j = data.segments.size();
		for (int i = 0; i < j; i++) {
		Segment next = getTopLeftSegment(data.segments);
		data.segments.remove(next);
		
		newSegments.add(next);
		
		}
		
		data.segments = newSegments;
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

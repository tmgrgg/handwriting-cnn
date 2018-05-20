package transcription;

import java.util.ArrayList;
import java.util.List;

import segmenter.Segment;

//Orders TranslationData segments into line-read order.
public class FrontRunnerLineParserMidpoints implements Parser {
	
	//modified to use midpoints instead of getLeft();
	double JOIN_THRESH = 0;
	double HEIGHT_THRESHFAC = 0.5;

	public TranscriptionData parse(TranscriptionData data) {
		JOIN_THRESH = 3 * data.getAverageSegmentWidth() / 5;
		findLines(data);
		connectComponents(data);
		return data;
	}

	private void connectComponents(TranscriptionData data) {
		// get all adjacent segments in the same line that have no overlap and
		// vertical displacement
		// group them based on distance - groups should be mutually exclusive

		int lineStart = 0;
		for (int numLines = 0; numLines < data.lineDividers.size(); numLines++) {
			int lineEnd = data.lineDividers.get(numLines);
			for (int i = lineStart; i < lineEnd; i++) {
				// iterate through each line... analysing adjacent segments
				// i.e. we analyse segments in threes (if necessary)
				Segment firstSeg = data.segments.get(i);
				Segment secondSeg = data.segments.get(i + 1);
				boolean joined = false;

				if (!checkSegOverlap(firstSeg, secondSeg)) {
					// same line segments do not overlap vertically
					// possibly need to join
					if ((i + 1) == lineEnd) {
						// join the first and second seg

						// only join if they're relatively close as per
						// JOIN_THRESH
						if (Math.abs(getHorizontalMidpoint(firstSeg)
								- getHorizontalMidpoint(secondSeg)) <= JOIN_THRESH) {
							data.segments.remove(i + 1);
							data.segments.set(i, firstSeg.join(secondSeg));
							joined = true;
						}
					} else {
						Segment thirdSeg = data.segments.get(i + 2);
						//System.out.println("seg3" + " " + (i + 2));
						if (checkSegOverlap(secondSeg, thirdSeg)) {
							// join the first and second seg
							if (Math.abs(getHorizontalMidpoint(firstSeg)
									- getHorizontalMidpoint(secondSeg)) <= JOIN_THRESH) {
								data.segments.remove(i + 1);
								data.segments.set(i, firstSeg.join(secondSeg));
								joined = true;
							}
						} else {
							// join the second segment with closest segment
							// horizontally
							int firstMidpoint = (firstSeg.getRight() - firstSeg
									.getLeft()) / 2 + firstSeg.getLeft();
							int secondMidpoint = (secondSeg.getRight() - secondSeg
									.getLeft()) / 2 + secondSeg.getLeft();
							int thirdMidpoint = (thirdSeg.getRight() - thirdSeg
									.getLeft()) / 2 + thirdSeg.getLeft();
							if (Math.abs(secondMidpoint - firstMidpoint) <= Math
									.abs(thirdMidpoint - secondMidpoint)) {
								// join second and first segments
								if (Math.abs(getHorizontalMidpoint(firstSeg)
										- getHorizontalMidpoint(secondSeg)) <= JOIN_THRESH) {
									data.segments.remove(i + 1);
									data.segments.set(i,
											firstSeg.join(secondSeg));
									joined = true;
								}
							} else {
								// join second and third segments
								if (Math.abs(getHorizontalMidpoint(secondSeg)
										- getHorizontalMidpoint(thirdSeg)) <= JOIN_THRESH) {
									data.segments.remove(i + 2);
									data.segments.set(i + 1,
											secondSeg.join(thirdSeg));
									joined = true;
								}
							}
						}
					}
					// need to update lineDividers after the joined segs
					if (joined) {
						for (int j = numLines; j < data.lineDividers.size(); j++) {
							int prev = data.lineDividers.get(j);
							data.lineDividers.set(j, prev - 1);
						}
						joined = false;
					}
					// also update lineEnd because the line's position has now
					// moved
					lineEnd = data.lineDividers.get(numLines);
				}
			}
			lineStart = data.lineDividers.get(numLines) + 1;
		}
	}

	private boolean checkSegOverlap(Segment seg1, Segment seg2) {
		return seg1.getTop() < seg2.getBottom()
				&& seg1.getBottom() > seg2.getTop();

	}

	private void findLines(TranscriptionData data) {
		// order TranslationData.segments left to right
		leftRightSort(data.segments);

		List<Segment> newSegments = new ArrayList<Segment>();
		List<Integer> newLineDividers = new ArrayList<Integer>();

		int avgHeight = data.getAverageSegmentHeight();
		// start with an average character, in case we have an i or a j dot to
		
		Segment current = getTopLeftSegment(data.segments);
		data.segments.remove(current);

		newSegments.add(current);
		int index = 0;
		
		// begin.
		int topBound = getVerticalMidpoint(current) - avgHeight / 2;
		int bottomBound = getVerticalMidpoint(current) + avgHeight / 2;
		while (!data.segments.isEmpty()) {
			boolean changed = false;
			// Find the first (left-most) segment whose
			// top is higher than current's bottom
			// that is right of the current segment
			for (Segment segment : data.segments) {
				if ((segment.getX() >= current.getX())
						&& (segment.getTop() <= bottomBound)) {
					current = segment;
					changed = true;
					break;
				}
			}
			if (changed == false) {
				// We must have a new line
				newLineDividers.add(index);
				current = getTopLeftSegment(data.segments);
				topBound = getVerticalMidpoint(current) - avgHeight / 2;
				bottomBound = getVerticalMidpoint(current) + avgHeight / 2;
			} else if (current.getBottom() - current.getTop() >= HEIGHT_THRESHFAC
					* (bottomBound - topBound)) {
				topBound = current.getTop();
				bottomBound = current.getBottom();
			}
			data.segments.remove(current);
			newSegments.add(current);
			index++;
		}

		data.segments = newSegments;

		// the last line ends at the end of the stream
		newLineDividers.add(data.segments.size() - 1);
		data.lineDividers = newLineDividers;
	}

	private int getVerticalMidpoint(Segment segment) {
		return (int) Math.round(0.5 * segment.getBottom() + 0.5
				* segment.getTop());
	}

	private int getHorizontalMidpoint(Segment segment) {
		return (int) Math.round(0.5 * segment.getLeft() + 0.5
				* segment.getRight());
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
				if ((segment.getX() < highest.getX())
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

	// Sort into left and right precedence - should make this a quick sort really (but fine for now).
	private void leftRightSort(List<Segment> segments) {
		Segment temp;
		for (int j = 0; j < segments.size() - 1; j++) {
			for (int i = 0; i < segments.size() - 1; i++) {
				if (segments.get(i).getX() > segments.get(i + 1).getX()) {
					temp = segments.get(i);
					segments.set(i, segments.get(i + 1));
					segments.set(i + 1, temp);
				}
			}
		}
	}
}

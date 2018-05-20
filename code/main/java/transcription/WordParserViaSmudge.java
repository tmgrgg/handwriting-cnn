package transcription;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import preprocessing.ImageProcessLibrary;
import segmenter.Segment;

//currently just a copy of WordParserByPageAverage

//lineDividers correspond to the index of the last character of a line (INCLUDING THE LAST LINE), INDEXED FROM 0
//wordDividers correspond to the index of the last character of a word in translationData INDEXED FROM 0, BUT NOT INCLUDING THE LAST
//WORD ON A LINE.
public class WordParserViaSmudge implements Parser {

	//debugging
	boolean test = false;

	private double smear_fac = 0.91;

	// for use in dictionary feedback loop
	public TranscriptionData specialParse(TranscriptionData data, double smear_fac) {
		this.smear_fac = smear_fac;
		return parse(data);
	}

	public TranscriptionData parse(TranscriptionData data) {

		// System.out.println(data.segments.size());
		// System.out.println(data.lineDividers.size());
		LineHistogram histogram;
		List<Segment> line;

		List<Integer> newWordDividers = new ArrayList<Integer>();
		// for each line
		int start = 0;
		for (Integer end : data.lineDividers) {
			// get segments in line
			line = data.getSegments().subList(start, end + 1);
			// construct vertical histogram for the line

			histogram = new LineHistogram(line);

			if (test) {
				System.out.println("BEFORE SMEAR");
				System.out.println(histogram);
			}

			// Smearing to connect
			histogram.rightSmear(smear_fac);

			if (test) {
				System.out.println("AFTER SMEAR");
				System.out.println(histogram);
			}

			// smear the histogram by smear fac
			// locate zero points of histogram
			// the segment in line to the left of each zero_point corresponds to
			// the final index end of a word (so add it to wordDividers).

			List<Integer> perLineWordDividers = histogram
					.getWordDividersRelativeToLine();

			for (int i = 0; i < perLineWordDividers.size(); i++) {
				newWordDividers.add(perLineWordDividers.get(i) + start);
			}

			start = end + 1;
		}

		data.wordDividers = newWordDividers;
		return data;
	}

	public void setSmearFac(double smear_fac) {
		this.smear_fac = smear_fac;
	}

	private class LineHistogram {
		// the vertical summed non-white pixel values
		int[] histogram;

		// bijective map of the x-coordinate to the corresponding segment.
		List<Segment> line;

		private int right;
		private int left;

		LineHistogram(List<Segment> line) {
			this.line = line;
			right = line.get(line.size() - 1).getRight();
			left = line.get(0).getLeft();

			int histSize = right - (left - 1);

			histogram = new int[histSize];
			buildHist();
		}

		// gets the worddividers relative to the line (need to add number of
		// characters before to get relative to paragraph).
		List<Integer> getWordDividersRelativeToLine() {
			List<Integer> wordDividers = new ArrayList<Integer>();
			List<Integer> minPoints = getMinimumPoints();

			for (int i = 0; i < line.size() - 1; i++) {
				Segment seg = line.get(i + 1);
				if (minPoints.isEmpty()) {
					break;
				}
				int minpoint = minPoints.get(0) + left;
				if (seg.getLeft() >= minpoint) {
					// this segment is the last one that is to the left of the
					// minimum point... i.e. the last letter in current word
					wordDividers.add(i);
					minPoints.remove(0);
				}
			}

			if (test) {
				System.out.println("wordDividers relative to line: "
						+ wordDividers);
			}
			// should also add the end of the line as the end of a word
			// (probably)
			wordDividers.add(line.size() - 1);
			return wordDividers;
		}

		private void buildHist() {
			for (Segment seg : line) {
				for (int x = 0; x < (seg.getRight() - (seg.getLeft() - 1)); x++) {
					int index = x + seg.getLeft() - (left);

					// we do plus equal in case we have separate segments
					// overlapping

					if (index >= histogram.length) {
						System.out
								.println("System has evidence that the line parsing step has failed to generate correct output");
					}

					if (index >= 0 && index < histogram.length) {
						// avoid horrible edge cases
						histogram[index] += countPixelsInColumn(x,
								ImageProcessLibrary.pixelDataFromImage(seg
										.getImage()));
					}
				}
			}
		}

		private int countPixelsInColumn(int x, int[][] pixelData) {
			int count = 0;
			for (int y = 0; y < pixelData.length; y++) {
				if (pixelData[y][x] != ImageProcessLibrary.WHITE) {
					count++;
				}
			}
			return count;
		}

		// currently just checks for 0 points (after smear)
		// careful to avoid adding contiguous zeroes
		private List<Integer> getMinimumPoints() {
			List<Integer> minimumPoints = new ArrayList<Integer>();
			boolean contiguity_switch = true;
			for (int x = 0; x < histogram.length; x++) {
				if (histogram[x] > 0) {
					contiguity_switch = true;
				}
				if (histogram[x] == 0 && contiguity_switch) {
					contiguity_switch = false;
					minimumPoints.add(x);
				}
			}
			return minimumPoints;
		}

		private void rightSmear(double factor) {
			for (int x = 0; x < histogram.length - 1; x++) {
				histogram[x + 1] += (int) Math.floor(factor * histogram[x]);

				// don't preserve the particles
				// histogram[x] = (int) Math.floor((1 - factor) * 2 *
				// histogram[x]);
			}
		}

		public String toString() {
			String s = "[";
			for (int i = 0; i < histogram.length - 1; i++) {
				s += histogram[i] + ", ";
			}
			s += histogram[histogram.length - 1] + "]";
			return s;
		}
	}
}

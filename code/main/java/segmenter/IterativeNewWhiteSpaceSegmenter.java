package segmenter;

import java.awt.Image;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import preprocessing.ImageProcessLibrary;
import preprocessing.Preprocessor;

public class IterativeNewWhiteSpaceSegmenter extends Segmenter {

	public IterativeNewWhiteSpaceSegmenter(Preprocessor preprocessor) {
		super(preprocessor);
	}

	// fields used to compute segments
	int top;
	int bottom;
	int right;
	int left;
	int[][] workingPixelData;
	int[][] pixelData;

	public Set<Segment> segment(Image inputImage) {
		Image image = preprocessor.preprocess(inputImage);

		pixelData = ImageProcessLibrary.pixelDataFromImage(image);
		workingPixelData = ImageProcessLibrary.pixelDataFromImage(image);
		Set<Segment> segments = new HashSet<Segment>();

		for (int x = 0; x < workingPixelData[0].length; x++) {
			for (int y = 0; y < workingPixelData.length; y++) {
				if (workingPixelData[y][x] != ImageProcessLibrary.WHITE) {
					top = y;
					bottom = y;
					right = x;
					left = x;
					getSegmentParameters(x, y);
					int actualWidth = (right - left) + 1;
					int actualHeight = (bottom - top) + 1;

					int[][] newPixelData = new int[actualHeight][actualWidth];

					for (int y_ = 0; y_ < newPixelData.length; y_++) {
						for (int x_ = 0; x_ < newPixelData[0].length; x_++) {
							newPixelData[y_][x_] = pixelData[y_ + top][x_
									+ left];
						}
					}
					segments.add(new Segment(image, top, bottom, right, left));
				}
			}
		}
		return segments;
	}

	private void getSegmentParameters(int x, int y) {
		Set<Point> fillSet = new HashSet<Point>();
		fillSet.add(new Point(x, y));

		while (!fillSet.isEmpty()) {
			Iterator<Point> iterator = fillSet.iterator();
			Point p = iterator.next();
			iterator.remove();
			
			bottom = Math.max(bottom, p.y);
			top = Math.min(top, p.y);
			left = Math.min(left, p.x);
			right = Math.max(right, p.x);

			workingPixelData[p.y][p.x] = ImageProcessLibrary.WHITE;
			
			int xBound = workingPixelData[0].length;
			int yBound = workingPixelData.length;

			if ((p.x + 1) < xBound && toBeFilled(p.x + 1, p.y)) {
				fillSet.add(new Point(p.x + 1, p.y));
			}
			if ((p.y + 1) < yBound && toBeFilled(p.x, p.y + 1)) {
				fillSet.add(new Point(p.x, p.y + 1));
			}
			if ((p.x - 1) >= 0 && toBeFilled(p.x - 1, p.y)) {
				fillSet.add(new Point(p.x - 1, p.y));
			}
			if ((p.y - 1) >= 0 && toBeFilled(p.x, p.y - 1)) {
				fillSet.add(new Point(p.x, p.y - 1));
			}
			if ((p.x + 1) < xBound && (p.y + 1) < yBound && toBeFilled(p.x + 1, p.y + 1)) {
				fillSet.add(new Point(p.x + 1, p.y + 1));
			}
			if ((p.x - 1) >= 0 && p.y - 1 >= 0 && toBeFilled(p.x - 1, p.y - 1)) {
				fillSet.add(new Point(p.x - 1, p.y - 1));
			}
			if ((p.x + 1) < xBound && p.y - 1 >= 0 && toBeFilled(p.x + 1, p.y - 1)) {
				fillSet.add(new Point(p.x + 1, p.y - 1));
			}
			if ((p.x - 1) >= 0 && (p.y + 1) < yBound && toBeFilled(p.x - 1, p.y + 1)) {
				fillSet.add(new Point(p.x - 1, p.y + 1));
			}
		}

	}

	private boolean toBeFilled(int x, int y) {
		return (workingPixelData[y][x] != ImageProcessLibrary.WHITE);
	}

	class Point {
		int x;
		int y;

		Point(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Point)) {
				return false;
			}

			Point otherPoint = (Point) o;

			return (this.x == otherPoint.x) && (this.y == otherPoint.y);
		}

		@Override
		public int hashCode() {
			int result = x;
			result = 31 * result + y;
			return result;
		}
	}
}

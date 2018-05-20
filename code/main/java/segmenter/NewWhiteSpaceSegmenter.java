package segmenter;

import java.awt.Image;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import preprocessing.ImageProcessLibrary;
import preprocessing.Preprocessor;

public class NewWhiteSpaceSegmenter extends Segmenter {
	
	public NewWhiteSpaceSegmenter(Preprocessor preprocessor) {
		super(preprocessor);
	}

	//fields used to compute segments
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
					segments.add(new Segment(image, top, bottom,
							right, left));
				}
			}
		}
		return segments;
	}
	
	private void getSegmentParameters(int x, int y) {
		if ((y < 0) || (y >= workingPixelData.length) || (x < 0)
				|| (x >= workingPixelData[0].length)) {
			return;
		}

		if (workingPixelData[y][x] != ImageProcessLibrary.WHITE) {
			workingPixelData[y][x] = ImageProcessLibrary.WHITE;
			bottom = Math.max(bottom, y);
			top = Math.min(top, y);
			left = Math.min(left, x);
			right = Math.max(right, x);

			getSegmentParameters(x + 1, y);
			getSegmentParameters(x - 1, y);
			getSegmentParameters(x, y + 1);
			getSegmentParameters(x, y - 1);
			
			
			//TODO: 22/04/Need to also do diagonals.. [done]
			getSegmentParameters(x + 1, y + 1);
			getSegmentParameters(x - 1, y - 1);
			getSegmentParameters(x - 1, y + 1);
			getSegmentParameters(x + 1, y - 1);
		}
	}
}

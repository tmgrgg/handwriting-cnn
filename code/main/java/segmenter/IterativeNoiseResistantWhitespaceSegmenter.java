package segmenter;

import java.awt.Image;
import java.util.HashSet;
import java.util.Set;

import preprocessing.Preprocessor;

public class IterativeNoiseResistantWhitespaceSegmenter extends Segmenter {

	double dynamicThresholdRate = 0.1;
	double areaThreshold = 0;
	double heightThreshold = 0;
	double widthThreshold = 0;
	Set<Segment> noisySegments;
	Set<Segment> workingSegments;

	public IterativeNoiseResistantWhitespaceSegmenter(Preprocessor preprocessor) {
		super(preprocessor);
	}

	@Override
	public Set<Segment> segment(Image image) {

		IterativeNewWhiteSpaceSegmenter segmenter = new IterativeNewWhiteSpaceSegmenter(preprocessor);
		noisySegments = segmenter.segment(image);
		workingSegments = new HashSet<Segment>();

		// Dynamically calculate threshold
		getDynamicAreaThreshold();

		for (Segment segment : noisySegments) {
			int area = segment.getImage().getHeight(null)
					* segment.getImage().getWidth(null);
			if (area > areaThreshold) {
				workingSegments.add(segment);
			}
		}
		getDynamicHeightThreshold();
		getDynamicWidthThreshold();

		System.out.println("A:" + areaThreshold);
		System.out.println("H:" + heightThreshold);
		System.out.println("W:" + widthThreshold);
		
		Set<Segment> newSegments = new HashSet<Segment>();

		for (Segment segment : workingSegments) {
			int height = segment.getImage().getHeight(null);
			int width = segment.getImage().getWidth(null);
			if (height > heightThreshold && width > widthThreshold) {
				newSegments.add(segment); 
			}
		}
			return newSegments;
	}

	private void getDynamicAreaThreshold() {
		double sum = 0;
		for (Segment segment : noisySegments) {
			sum += segment.getImage().getHeight(null)
					* segment.getImage().getWidth(null);
		}

		double averageArea = sum / noisySegments.size();

		areaThreshold = dynamicThresholdRate * averageArea;
	}

	private void getDynamicHeightThreshold() {
		double sum = 0;
		for (Segment segment : workingSegments) {
			sum += segment.getImage().getHeight(null);
		}

		double averageHeight = sum / workingSegments.size();

		heightThreshold = dynamicThresholdRate * averageHeight;
	}

	private void getDynamicWidthThreshold() {
		double sum = 0;
		for (Segment segment : workingSegments) {
			sum += segment.getImage().getWidth(null);
		}

		double averageWidth = sum / workingSegments.size();

		widthThreshold = dynamicThresholdRate * averageWidth;
	}

}

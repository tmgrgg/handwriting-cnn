package main;

import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import javax.imageio.ImageIO;

import preprocessing.Contrastify;
import preprocessing.ImageProcess;
import preprocessing.ImageProcessLibrary;
import preprocessing.Preprocessor;
import segmenter.NewWhiteSpaceSegmenter;
import segmenter.Segment;
import segmenter.Segmenter;

public class ScanTrainingData {
	// here's what we do...
	// we ask the user to not only specify the imageClass but the number of
	// vetically disconnected segments... then we just conjoin them
	// so... this won't work with noise!
	// we walk vertically down... so the image should be 1 character wide and as
	// long as we want :)
	public static void main(String args[]) {
		String dataImagePath = "C:\\Users\\Study\\Documents\\HOME\\OCR\\ocr\\images\\newTrainingData.png";
		String trainingExamplesPath = "C:\\Users\\Study\\Documents\\HOME\\OCR\\ocr\\images\\train\\";
		Image image = null;

		System.out
				.println("Enter character corresponding to training data :::");
		String character = new Scanner(System.in).next();

		System.out.println("Enter number of segments in character :::");
		int numseg = Integer.parseInt(new Scanner(System.in).next());

		try {
			image = ImageIO.read(new File(dataImagePath));
		} catch (IOException e) {
			System.out.println("Couln't open newTraining.png");
		}

		System.out.println("Denoise? y/n");
		String denoiseS = new Scanner(System.in).next();
		boolean denoise = denoiseS.equals("y");

		Segmenter segmenter;
		List<ImageProcess> segmenterProcesses = new ArrayList<ImageProcess>();
		segmenterProcesses.add(new Contrastify());

		Preprocessor segmenterpp = new Preprocessor(segmenterProcesses);

		if (!denoise) {
			segmenter = new NewWhiteSpaceSegmenter(segmenterpp);
		} else {
			// TODO: change this to adaptive threshold segmenter
			segmenter = new NewWhiteSpaceSegmenter(segmenterpp);
		}

		Set<Segment> segments = segmenter.segment(image);

		ArrayList<Segment> segList = new ArrayList<Segment>(segments);

		// naive sort segList highest to lowest
		Segment temp;
		for (int j = 0; j < segments.size() - 1; j++) {
			for (int i = 0; i < segments.size() - 1; i++) {
				if (segList.get(i).getTop() < segList.get(i + 1).getTop()) {
					temp = segList.get(i);
					segList.set(i, segList.get(i + 1));
					segList.set(i + 1, temp);
				}
			}
		}

		ArrayList<Segment> finalSegments = new ArrayList<Segment>();
		// connect every numSeg number of segments...
		while (segList.size() >= numseg) {
			Segment nextSeg;
			Segment finSeg = segList.get(0);
			for (int i = 0; i < numseg - 1; i++) {
				nextSeg = segList.get(i + 1);
				finSeg = finSeg.join(nextSeg);
			}
			finalSegments.add(finSeg);
			for (int i = 0; i < numseg; i++) {
				segList.remove(0);
			}
		}

		// now finalSegments contains the final dataexamples
		// we write these to the correct folder in training examples...

		int index = ImageProcessLibrary.CountPNGFiles(trainingExamplesPath
				+ character);

		try {
			for (Segment segment : finalSegments) {
				ImageIO.write((RenderedImage) segment.getImage(), "png",
						new File(trainingExamplesPath + character + "\\"
								+ (index + 1) + ".png"));
				index++;
			}
		} catch (IOException e) {
			System.out.println("Couldn't write new training data");
		}
	}
}

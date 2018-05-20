package main;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import preprocessing.Contrastify;
import preprocessing.ContrastifyByAverage;
import preprocessing.ContrastifyWithAdaptiveThreshold;
import preprocessing.FitToBoundaries;
import preprocessing.ImageProcess;
import preprocessing.Preprocessor;
import preprocessing.Resize;
import segmenter.IterativeNewWhiteSpaceSegmenter;
import segmenter.IterativeNoiseResistantWhitespaceSegmenter;
import segmenter.NewWhiteSpaceSegmenter;
import segmenter.NoiseResistantWhitespaceSegmenter;
import segmenter.Segment;
import segmenter.Segmenter;
import transcription.AdvancedFuzzyDictionary;
import transcription.FrontRunnerLineParserMidpoints;
import transcription.FuzzyChar;
import transcription.Parser;
import transcription.TranscriptionData;
import transcription.WordParserViaSmudge;
import transcription.AdvancedFuzzyDictionary.ScoreString;
import classifier.ClassifierLoadingFailureException;
import classifier.DL4JLeNet;
import classifier.FuzzyClassifier;
import classifier.FuzzyLeNet;
import classifier.ImageClassifier;

public class HandwritingRecogniser {
	// locations
	String imageClassString;
	String testTrainPath;
	String networkPath;
	String dictionaryString;

	// preprocesses
	List<ImageProcess> classifierProcesses;// = new ArrayList<ImageProcess>();
	List<ImageProcess> nonNoisySegmenterProcesses = new ArrayList<ImageProcess>();
	List<ImageProcess> noisySegmenterProcesses = new ArrayList<ImageProcess>();

	// preprocessors
	Preprocessor nonNoisySegmenterPP;
	Preprocessor noisySegmenterPP;
	Preprocessor classifierPP;

	// segmenter
	Segmenter noisySegmenter;
	Segmenter nonNoisySegmenter;

	// classifier
	//ImageClassifier classifier;
	FuzzyClassifier fuzzyClassifier;

	// Layout Parsers
	Parser lineParser;
	Parser wordParser;
	Parser fuzzyWordParser;
	
	int sampleSize;

	public HandwritingRecogniser(String imageClassString, String testTrainPath,
			String networkPath, String dictionaryString, List<ImageProcess> classifierProcesses, int sampleSize) {
		this.imageClassString = imageClassString;
		this.testTrainPath = testTrainPath;
		this.networkPath = networkPath;
		this.dictionaryString = dictionaryString;
		this.classifierProcesses = classifierProcesses;
		this.sampleSize = sampleSize;
	}

	public void initialise() throws ClassifierLoadingFailureException,
			NullClassifierException {
		
		// Build classifier preprocessor
		/*
		classifierProcesses.add((ImageProcess) new FitToBoundaries());
		classifierProcesses.add((ImageProcess) new Resize(32, 32));
		classifierProcesses.add((ImageProcess) new Contrastify());
		*/

		classifierPP = new Preprocessor(classifierProcesses);

		// Build classifiers
		/*if ((classifier = DL4JLeNet.load(imageClassString, testTrainPath,
				networkPath, classifierPP)) == null) {
			throw new NullClassifier();
		}*/
		if ((fuzzyClassifier = FuzzyLeNet.load(imageClassString, testTrainPath,
				networkPath, classifierPP, sampleSize)) == null) {
			throw new NullClassifierException();
		}

		// SEGMENTER
		// Build segmenter preprocessor
		//noisySegmenterProcesses.add(new ContrastifyByAverage());
		noisySegmenterProcesses.add(new ContrastifyWithAdaptiveThreshold());
		nonNoisySegmenterProcesses.add(new Contrastify());

		nonNoisySegmenterPP = new Preprocessor(nonNoisySegmenterProcesses);
		noisySegmenterPP = new Preprocessor(noisySegmenterProcesses);

		noisySegmenter = new IterativeNoiseResistantWhitespaceSegmenter(noisySegmenterPP);
		nonNoisySegmenter = new IterativeNewWhiteSpaceSegmenter(nonNoisySegmenterPP);

		// LAYOUT ANALYSIS
		// Build parsers
		lineParser = new FrontRunnerLineParserMidpoints();
		wordParser = new WordParserViaSmudge();
		fuzzyWordParser = new WordParserViaSmudge();
	}

	// TESTING VARIABLES
	Image segmenterPPOutputImage;
	List<Image> segImages;
	List<Image> preprocessedSegImages;
	List<String> outputLines;
	String outputWithDictionary;

	public String transcribe(Image image, boolean noisy, boolean segCorrect) {

		Segmenter segmenter;
		Preprocessor segmenterPP;

		if (noisy) {
			segmenter = this.noisySegmenter;
			segmenterPP = this.noisySegmenterPP;
		} else {
			segmenter = this.nonNoisySegmenter;
			segmenterPP = this.nonNoisySegmenterPP;
		}

		// Get segmenter preprocessor image output
		segmenterPPOutputImage = segmenterPP.preprocess(image);

		// Segment image
		Set<Segment> segmentSet = segmenter.segment(image);

		// parsers & transcription

		TranscriptionData data = new TranscriptionData(new ArrayList<Segment>(
				segmentSet));

		TranscriptionData linedData = lineParser.parse(data);
		data = wordParser.parse(linedData);

		// System.out.println(data.getWordDividers());
		// System.out.println(data.getLineDividers());

		// Get segment image list for order checking
		segImages = new ArrayList<Image>();
		for (int i = 0; i < data.getSegments().size(); i++) {
			segImages.add(i, data.getSegments().get(i).getImage());
		}

		// Get output string
		outputLines = new ArrayList<String>();

		List<Integer> lineDividers = data.getLineDividers();
		List<Integer> wordDividers = data.getWordDividers();

		//System.out.println(wordDividers);
		
		String s = "";
		for (int i = 0; i < data.getSegments().size(); i++) {
			s += fuzzyClassifier.classify(data.getSegments().get(i).getImage());
			if (wordDividers.contains(i)) {
				s += " ";
			}
			if (lineDividers.contains(i)) {
				outputLines.add(s);
				s = "";
			}
		}
		outputLines.add(s);

		// Get classifier preprocessor results on segments
		preprocessedSegImages = new ArrayList<Image>();
		for (Image segImage : segImages) {
			preprocessedSegImages.add(classifierPP.preprocess(segImage));
		}

		// FEEDBACK PARSING HERE5

		AdvancedFuzzyDictionary dictionaryParser = new AdvancedFuzzyDictionary(
				new File(dictionaryString));
		
		double smear_lim;
		if(segCorrect) {
			smear_lim = 0.85;
		} else {
			smear_lim = 0.99;
		}

		ScoreString scoreString;
		outputWithDictionary = "Failed to load dictionary.";
		
		TranscriptionData dicData = data.copyOf();
		TranscriptionData dicLinedData = linedData.copyOf();
		double maxScore = 0;
		for (double smear_fac = 0.84; smear_fac < smear_lim; smear_fac += 0.02) {
			dicData = ((WordParserViaSmudge) fuzzyWordParser).specialParse(
					dicLinedData, smear_fac);
			scoreString = dictionaryParser.parse(dicData
					.getFuzzyStrings(fuzzyClassifier));
			if (scoreString.p >= maxScore) {
				maxScore = scoreString.p;
				outputWithDictionary = scoreString.s;
			}
		}
		return outputWithDictionary;
	}
	
	//TESTING VARIABLE GETTERS (should only be called AFTER we've transcribed!)
	public String getTestLog() {
		return fuzzyClassifier.test().getLog();
	}
	
	public Image getSegmenterPPOutputImage() {
		return segmenterPPOutputImage;
	}
	
	public List<Image> getSegImages() {
		return segImages;
	}

	public List<Image> getPreprocessedSegImages() {
		return preprocessedSegImages;
	}
	
	public List<String> getOutputLines() {
		return outputLines;
	}
	
	public String getOutputWithDictionary() {
		return outputWithDictionary;
	}
	
	public FuzzyChar fuzzyClassify(Image image) {
		return fuzzyClassifier.fuzzyClassify(image);
	}
}

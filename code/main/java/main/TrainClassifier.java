package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import preprocessing.Contrastify;
import preprocessing.DefineSquareBoundaries;
import preprocessing.FitToBoundaries;
import preprocessing.ImageProcess;
import preprocessing.Negate;
import preprocessing.Preprocessor;
import preprocessing.Resize;
import classifier.DL4JLeNet;
import classifier.FuzzyLeNet;
import classifier.TestData;
import classifier.TrainingData;

/*
 *  nChannels = 1; number of input channels (1 for greyscale for colour)
 *	batchSize = 32; // test batch size
 *  nEpochs = 1; // number of training epochs
 *	iterations = 6; // number of training iterations
 *	seed = 123;
 */

public class TrainClassifier {

	/* we can train a DL4JLeNet and use it as a FuzzyLeNet when we load. */

	public static void main(String args[]) {
		String imageClassString = "a:b:c:d:e:f:g:h:i:j:k:l:m:n:o:p:q:r:s:t:u:v:w:x:y:z";
		String path = "C:\\Users\\Study\\Documents\\HOME\\OCR\\ocr\\images\\newnewTRAINING";

		double learningRate = 0.0089;
		double momentum = 0.9;
		double regConst = 0.0006;
		int sampleSize = 28;
		int seed = 123;
		int nChannels = 1;

		// One iteration since the dataset is YUGE
		int iterations = 6;

		// int batchSize = 240;
		int nEpochs = 10;

		int batchSize = 64;

		
		List<ImageProcess> classifierProcesses = new ArrayList<ImageProcess>();
		classifierProcesses.add((ImageProcess) new FitToBoundaries());

		// OKAY: TRY TRAINING A CLASSIFIER WITHOUT THIS DEFINE SQUARE BOUNDARIES
		// THING...
		// IT REALLY SEEMS TO MESS STUFF UP!
		classifierProcesses.add((ImageProcess) new DefineSquareBoundaries());
		classifierProcesses.add((ImageProcess) new Resize(sampleSize, sampleSize));
		classifierProcesses.add((ImageProcess) new Contrastify());
		classifierProcesses.add((ImageProcess) new Negate());

		Preprocessor preprocessor = new Preprocessor(classifierProcesses);

		//normal learningRate is 0.01, momentum is 0.9, regConst = 0.0005
		// fixed training variables
		
		DL4JLeNet classifier = new DL4JLeNet(imageClassString, preprocessor,
				path, nChannels, batchSize, nEpochs, iterations, seed, sampleSize,
				learningRate, momentum, regConst);

		// New train Method wasn't working so have to use the trainPP
		// folder.
		// Q: WHY DOES safeTrain get a different classification rate to
		// normal train!?
		// A: The images are presented in a different (pseudo-random) order.
		// This will mean they get different areas of the
		// learning terrain for the same random seed.
		// But also... train() seems to be consistently lower... which is
		// really weird and makes me feel uncomfortable.
		// use safeTrain

		String savePath = "C:\\Users\\Study\\Documents\\HOME\\OCR\\ocr\\networks\\trainedNetsWithStats830trainingSet";
		File saveFolder = new File(savePath);
		int numFiles = saveFolder.listFiles().length;
		File networkFolder = new File(savePath + "\\net" + numFiles);

		if (!networkFolder.mkdir()) {
			System.out.println("Failed to save network!");
		}

		/*TrainingData trainData = classifier.safeTrainNamelessDisplay(false,
				networkFolder + "\\graphs");*/

		TrainingData trainData = classifier.safeTrainNamelessDisplay(false, networkFolder + "\\graphs");
		TestData testData = classifier.test();

		classifier.save(networkFolder + "\\net");

		try {
			FileUtils.writeStringToFile(new File(networkFolder + "\\data.txt"),
					"Train Data:\n" + trainData.getLog() + "\n\n"
							+ "Test Data:\n" + testData.getLog());
		} catch (IOException e) {
			System.out.println("Failed to write train and test data");
		}

		System.out.println("NETWORK TRAINED AND TESTED");
	}
}

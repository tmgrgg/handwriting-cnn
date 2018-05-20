package classifier;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.image.loader.ImageLoader;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import preprocessing.ImageProcess;
import preprocessing.ImageProcessLibrary;
import preprocessing.Preprocessor;
import transcription.FuzzyChar;


public class FuzzyLeNet extends FuzzyClassifier {
	private MultiLayerNetwork network;

	private static final Logger log = LoggerFactory.getLogger(DL4JLeNet.class);

	// default parameters
	//changed from 32 to 28
	private int sampleSize = 28;
	private int nChannels = 1; // number of input channels (1 for greyscale, 3
								// for colour)
	private int batchSize = 32; // test batch size
	private int nEpochs = 10; // number of training epochs
	private int iterations = 20; // number of training iterations
	private int seed = 123;
	
	private double learningRate = 0.01;
	private double momentum = 0.9;

	NativeImageLoader imageLoader;
	private Random randomNumGen = new Random(seed);

	public FuzzyLeNet(String imageClassString, Preprocessor preprocessor,
			String path) {
		super(imageClassString, preprocessor, path);
		initialise();
	}
	

	public FuzzyLeNet(String imageClassString, Preprocessor preprocessor,
			String path, int nChannels, int batchSize, int nEpochs,
			int iterations, int seed, int sampleSize, double learningRate, double momentum) {
		super(imageClassString, preprocessor, path);
		this.nChannels = nChannels;
		this.batchSize = batchSize;
		this.nEpochs = nEpochs;
		this.iterations = iterations;
		this.seed = seed;
		this.sampleSize = sampleSize;
		this.learningRate = learningRate;
		this.momentum = momentum;
		initialise();
	}

	public FuzzyLeNet(String imageClassString, Preprocessor preprocessor,
			String path, int nChannels, int batchSize, int nEpochs,
			int iterations, int seed) {
		super(imageClassString, preprocessor, path);
		this.nChannels = nChannels;
		this.batchSize = batchSize;
		this.nEpochs = nEpochs;
		this.iterations = iterations;
		this.seed = seed;
		initialise();
	}

	// Used to load networks
	public FuzzyLeNet(String imageClassString, Preprocessor preprocessor,
			String path, MultiLayerNetwork network, int sampleSize) {
		super(imageClassString, preprocessor, path);
		this.sampleSize = sampleSize;
		this.network = network;
		imageLoader = new NativeImageLoader(sampleSize,
				sampleSize, nChannels);
	}

	private void initialise() {
		
		imageLoader = new NativeImageLoader(sampleSize,
					sampleSize, nChannels);
		 
		int outputNum = imageClasses.size();

		log.info("Build and initialise model....");
		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
				.seed(seed)
				.iterations(iterations)
				// Training iterations as above
				.regularization(true)
				.l2(0.0005)
				/*
				 * Uncomment the following for learning decay and bias
				 */
				.learningRate(learningRate)
				// .biasLearningRate(0.02)
				// .learningRateDecayPolicy(LearningRatePolicy.Inverse).lrPolicyDecayRate(0.001).lrPolicyPower(0.75)
				.weightInit(WeightInit.XAVIER)
				.optimizationAlgo(
						OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
				.updater(Updater.NESTEROVS)
				.momentum(momentum)
				.list()
				.layer(0,
						new ConvolutionLayer.Builder(5, 5)
								// nIn and nOut specify depth. nIn here is the
								// nChannels and nOut is the number of filters
								// to be applied
								.nIn(nChannels).stride(1, 1).nOut(20)
								.activation(Activation.IDENTITY).build())
				.layer(1,
						new SubsamplingLayer.Builder(
								SubsamplingLayer.PoolingType.MAX)
								.kernelSize(2, 2).stride(2, 2).build())
				.layer(2,
						new ConvolutionLayer.Builder(5, 5)
								// Note that nIn need not be specified in later
								// layers
								.stride(1, 1).nOut(50)
								.activation(Activation.IDENTITY).build())
				.layer(3,
						new SubsamplingLayer.Builder(
								SubsamplingLayer.PoolingType.MAX)
								.kernelSize(2, 2).stride(2, 2).build())
				.layer(4,
						new DenseLayer.Builder().activation(Activation.RELU)
								.nOut(500).build())
				.layer(5,
						new OutputLayer.Builder(
								LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
								.nOut(outputNum).activation(Activation.SOFTMAX)
								.build())
				// might need to change vvv that, since it has a depth of 1...
				// meaning it doesn't analyse colour? who knows.
				.setInputType(
						InputType.convolutionalFlat(sampleSize, sampleSize,
								nChannels)).backprop(true).pretrain(false)
				.build();

		this.network = new MultiLayerNetwork(conf);
		this.network.init();
	}

	@Override
	public void reset() {
		initialise();
	}

	@Override
	public ImageClass classify(Image image) {
		log.info("Classifying image");

		// Get the image into an INDarray
		Image processedImage = preprocessor.preprocess(image);

		INDArray imageAsMatrix = null;
		try {
			imageAsMatrix = imageLoader
					.asMatrix((BufferedImage) processedImage);
		} catch (IOException e) {
			System.out
					.println("Failed to load  an image in LeNet-5 classification");
		}

		// Normalize image
		DataNormalization scaler = new ImagePreProcessingScaler(0, 1);

		scaler.transform(imageAsMatrix);

		INDArray output = network.output(imageAsMatrix);

		// Get the maximum output (classification)
		int max = 0;
		for (int i = 1; i < imageClasses.size(); i++) {
			if (output.getDouble(i) > output.getDouble(max)) {
				max = i;
			}

		}
		System.out.println("Returned " + imageClasses.get(max) + " with score " + output.getDouble(max));
		return imageClasses.get(max);
		
	}

	/*
	 * TODID: It seems impossible to do in one pass... DL4J focuses on using the
	 * file system to create an iterator for the data
	 */

	@Override
	public TrainingData train() {
		TrainingData datalog = new TrainingData(this.toString(), preprocessor,
				imageClasses);
		int outputNum = imageClasses.size();
		log.info("preprocessing and loading training data");

		// We need to apply the preprocessing steps to the entire file because
		// that's where
		// Do preprocessing and upload image to relevant preprocessed file (this
		// is horrible)
		String trainingPath = path + "\\train";

		// Want to make a temporary directory to store images afer preprocessing
		// Since DataVec and DataIterator does not provide functionality
		// for preprocessing images easily ... i.e. it's
		// so much easier to build an iterator from a filesystem
		String preprocessedPath = path + "\\preprocessed";

		File preprocessedDir = new File(preprocessedPath);
		if (!preprocessedDir.mkdir()) {
			log.info("Failed to create preprocess training directory");
		}

		for (ImageClass c : imageClasses) {
			File trainingDir = new File(trainingPath + "\\" + c);

			// make temporary label directory
			File preprocessedLabelDir = new File(preprocessedPath + "\\" + c);
			preprocessedLabelDir.mkdir();

			int index = 0;
			for (File imageFile : trainingDir.listFiles()) {
				// CHECK THAT IT'S ACTUALLY AN IMAGE FILE (PNG)
				if (imageFile.getPath().endsWith(".png")) {

					index++;

					File outputFile = new File(preprocessedPath + "\\" + c
							+ "\\" + index + ".png");
					try {
						Image processedImg = preprocessor.preprocess(ImageIO
								.read(imageFile));
						ImageIO.write((RenderedImage) processedImg, "png",
								outputFile);
					} catch (IOException e) {
						System.out
								.println("Failed to preprocess LeNet-5 imageClasses");
					}
				}
			}
			datalog.log("Number of training examples for " + c + ": " + index);
		}

		// DataSetIterator Tutorial@ (there's little documentation)
		// "https://www.youtube.com/watch?v=GLC8CIoHDnI"

		FileSplit train = new FileSplit(preprocessedDir,
				NativeImageLoader.ALLOWED_FORMATS, randomNumGen);
		ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
		ImageRecordReader reader = new ImageRecordReader(sampleSize,
				sampleSize, nChannels, labelMaker);
		try {
			reader.initialize(train);
		} catch (IOException e) {
			System.out.println("LeNet-5 failed to read training data");
		}

		// Create the dataSetIterator
		DataSetIterator dataIter = new RecordReaderDataSetIterator(reader,
				batchSize, 1, outputNum);

		// Normalize the values between zero and one
		DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
		scaler.fit(dataIter);
		dataIter.setPreProcessor(scaler);

		// train the network
		for (int i = 0; i < nEpochs; i++) {
			log.info("HI");
			network.fit(dataIter);
		}

		log.info("GOT 'ERE");
		// Delete the temporary preprocessed file structure
		preprocessedDir.delete();

		log.info("TERMINATED");
		return datalog;
	}

	// Can't serialise the whole dl4j object
	public void save(String path) {
		File location = new File(path);

		// Save training parameters
		boolean saveUpdater = true;
		try {
			ModelSerializer.writeModel(network, location, saveUpdater);
		} catch (IOException e) {
			System.out.println("failed to save LeNet-5 network");
		}
	}

	public static FuzzyLeNet load(String imageClassString, String path,
			String networkPath, Preprocessor preprocessor, int sampleSize) throws ClassifierLoadingFailureException {
		File location = new File(networkPath);
		FuzzyLeNet classifier = null;
		try {
			MultiLayerNetwork network = ModelSerializer
					.restoreMultiLayerNetwork(location);
			classifier = new FuzzyLeNet(imageClassString, preprocessor, path,
					network, sampleSize);
		} catch (IOException e) {
			throw new ClassifierLoadingFailureException("Could not find " + networkPath);
		}
		return classifier;
	}

	@Override
	public String toString() {
		return "LeNet-5 classification network (DL4JLeNet)";
	}

	@Override
	public FuzzyChar fuzzyClassify(Image image) {
		// Get the image into an INDarray
		Image processedImage = preprocessor.preprocess(image);
		
		INDArray imageAsMatrix = null;
		try {
			imageAsMatrix = imageLoader
					.asMatrix((BufferedImage) processedImage);
		} catch (IOException e) {
			System.out
					.println("Failed to load  an image in LeNet-5 classification");
		}

		// Normalize image
		DataNormalization scaler = new ImagePreProcessingScaler(0, 1);

		scaler.transform(imageAsMatrix);

		INDArray output = network.output(imageAsMatrix);

		// SET values below threshold to 0
		double threshold = 0.001;
		List<Double> distribution = new ArrayList<Double>();
		for (int i = 0; i < imageClasses.size(); i++) {
			if (output.getDouble(i) >= threshold) {
				distribution.add(output.getDouble(i));
			} else {
				distribution.add(0.0);
			}
		}
		return new FuzzyChar(imageClasses, distribution);
	}
}

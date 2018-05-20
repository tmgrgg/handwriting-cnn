package classifier;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.image.loader.ImageLoader;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.api.storage.StatsStorage;
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
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.FileStatsStorage;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
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

public class DL4JLeNet extends ImageClassifier {
	private MultiLayerNetwork network;

	private static final Logger log = LoggerFactory.getLogger(DL4JLeNet.class);

	// default parameters
	private double regConst = 0.0005;
	private double learningRate = 0.01;
	private double momentum = 0.9;
	private int sampleSize = 28;
	private int nChannels = 1; // number of input channels (1 for greyscale, 3
								// for colour)
	private int batchSize = 32; // test batch size
	private int nEpochs = 1; // number of training epochs
	private int iterations = 6; // number of training iterations
	private int seed = 123;

	NativeImageLoader imageLoader;
	private Random randomNumGen = new Random(seed);

	public DL4JLeNet(String imageClassString, Preprocessor preprocessor,
			String path) {
		super(imageClassString, preprocessor, path);
		initialise();
	}

	public DL4JLeNet(String imageClassString, Preprocessor preprocessor,
			String path, int nChannels, int batchSize, int nEpochs,
			int iterations, int seed, int sampleSize, double learningRate, double momentum, double regConst) {
		super(imageClassString, preprocessor, path);
		this.nChannels = nChannels;
		this.batchSize = batchSize;
		this.nEpochs = nEpochs;
		this.iterations = iterations;
		this.seed = seed;
		this.sampleSize = sampleSize;
		this.learningRate = learningRate;
		this.momentum = momentum;
		this.regConst = regConst;
		initialise();
	}

	
	public DL4JLeNet(String imageClassString, Preprocessor preprocessor,
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
	public DL4JLeNet(String imageClassString, Preprocessor preprocessor,
			String path, MultiLayerNetwork network, int sampleSize) {
		super(imageClassString, preprocessor, path);
		this.sampleSize = sampleSize;
		this.network = network;
		
		imageLoader  = new NativeImageLoader(sampleSize,
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
				.l2(regConst)
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
		return imageClasses.get(max);
	}

	// doesn't depend on files having certain filenames, also allows us to skip
	// preprocessing if we know it's already done in a previous pass
	public TrainingData safeTrainNameless(boolean isPreprocessed) {
		TrainingData datalog = new TrainingData(this.toString(), preprocessor,
				imageClasses);
		log.info("preprocessing and loading training data");

		// We need to apply the preprocessing steps to the entire file because
		// that's where
		// Do preprocessing and upload image to relevant preprocessed file (this
		// is horrible)

		if (!isPreprocessed) {
			String trainingPath = path + "\\train";
			for (ImageClass c : imageClasses) {
				File imageClassDir = new File(trainingPath + "\\" + c);
				int index = 0;
				for (File inputFile : imageClassDir.listFiles()) {
					if (inputFile.getPath().endsWith(".png")) {
						File outputFile = new File(path + "\\trainPP\\" + c
								+ "\\" + index + ".png");
						try {
							Image processedImg = preprocessor
									.preprocess((Image) ImageIO.read(inputFile));
							
							System.out
									.println(inputFile.getAbsolutePath());
							ImageIO.write((RenderedImage) processedImg, "png",
									outputFile);
						} catch (IOException e) {
							
							System.out
									.println("Failed to preprocess LeNet-5 imageClasses");
						}
					}
					index++;
				}
				datalog.log("Number of training examples for " + c + ": "
						+ index);
			}
		}

		// Build the data from preprocessed training directory into a
		// DataSetIterator Tutorial@
		// "https://www.youtube.com/watch?v=GLC8CIoHDnI"
		File preProcTrainData = new File(path + "\\trainPP");
		FileSplit train = new FileSplit(preProcTrainData,
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
				batchSize, 1, imageClasses.size());

		// Normalize the values between zero and one
		DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
		scaler.fit(dataIter);
		dataIter.setPreProcessor(scaler);

		// train the network
		for (int i = 0; i < nEpochs; i++) {
			network.fit(dataIter);
		}

		return datalog;
	}
	
	public TrainingData safeTrainNamelessDisplay(boolean isPreprocessed, String displayPath) {
		//Initialize the user interface backend
	    UIServer uiServer = UIServer.getInstance();

	    //Configure where the network information (gradients, score vs. time etc) is to be stored. Here: store in memory.
	    StatsStorage statsStorage = new InMemoryStatsStorage();//new FileStatsStorage(new File(displayPath));  
	    //Alternative: new FileStatsStorage(File), for saving and loading later
	    
	    //Attach the StatsStorage instance to the UI: this allows the contents of the StatsStorage to be visualized
	    uiServer.attach(statsStorage);

	    //Then add the StatsListener to collect this information from the network, as it trains
	    network.setListeners(new StatsListener(statsStorage));
	    
		return safeTrainNameless(isPreprocessed);
	}

	/* It seems impossible to do in one pass... DL4J focuses on using the
	  file system to create an iterator for the data
	 */

	// This train method assumes that the training folder is constant - i.e. it
	// uses a permanent folder ("TrainPP" to hold
	// preprocessed training data
	public TrainingData safeTrain() {
		TrainingData datalog = new TrainingData(this.toString(), preprocessor,
				imageClasses);
		log.info("preprocessing and loading training data");

		// We need to apply the preprocessing steps to the entire file because
		// that's where
		// Do preprocessing and upload image to relevant preprocessed file (this
		// is horrible)
		String trainingPath = path + "\\train";
		for (ImageClass c : imageClasses) {
			int numFiles = ImageProcessLibrary.CountPNGFiles(trainingPath
					+ "\\" + c);
			for (int i = 1; i <= numFiles; i++) {
				String filename = trainingPath + "\\" + c + "\\" + i + ".png";
				System.out.println("reading " + filename);
				File inputFile = new File(filename);
				File outputFile = new File(path + "\\trainPP\\" + c + "\\" + i
						+ ".png");
				try {
					System.out.println(filename);
					Image processedImg = preprocessor
							.preprocess((Image) ImageIO.read(inputFile));
					ImageIO.write((RenderedImage) processedImg, "png",
							outputFile);
				} catch (IOException e) {
					e.printStackTrace();
					System.out
							.println("Failed to preprocess LeNet-5 imageClasses");
				}
			}
			datalog.log("Number of training examples for " + c + ": "
					+ numFiles);
		}

		// Build the data from preprocessed training directory into a
		// DataSetIterator Tutorial@
		// "https://www.youtube.com/watch?v=GLC8CIoHDnI"
		File preProcTrainData = new File(path + "\\trainPP");
		FileSplit train = new FileSplit(preProcTrainData,
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
				batchSize, 1, imageClasses.size());

		// Normalize the values between zero and one
		DataNormalization scaler = new ImagePreProcessingScaler(0, 1);
		scaler.fit(dataIter);
		dataIter.setPreProcessor(scaler);

		// train the network
		for (int i = 0; i < nEpochs; i++) {
			network.fit(dataIter);
		}

		return datalog;
	}

	// Should be noted that it's better practice to use safeTrain with this
	// particular network...
	// safeTrain doesn't attempt to use a temporary directory and instead uses
	// trainPP...
	// downside is that you have to ensure that only the recognisable classes
	// are in trainPP.
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
		preprocessedDir.mkdir();

		for (ImageClass c : imageClasses) {
			File trainingDir = new File(trainingPath + "\\" + c);

			// make temporary label directory
			File preprocessedLabelDir = new File(preprocessedPath + "\\" + c);
			preprocessedLabelDir.mkdir();

			int index = 0;
			for (File imageFile : trainingDir.listFiles()) {
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
			network.fit(dataIter);
		}

		// Delete the temporary preprocessed file structure
		preprocessedDir.delete();

		return datalog;
	}

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

	public static DL4JLeNet load(String imageClassString, String path,
			String networkPath, Preprocessor preprocessor, int sampleSize) throws ClassifierLoadingFailureException {
		File location = new File(networkPath);
		DL4JLeNet classifier = null;
		try {
			MultiLayerNetwork network = ModelSerializer
					.restoreMultiLayerNetwork(location);
			classifier = new DL4JLeNet(imageClassString, preprocessor, path,
					network, sampleSize);
		} catch (IOException e) {
			throw new ClassifierLoadingFailureException("Could not find " + networkPath);
		}
		return classifier;
	}

	@Override
	public String toString() {
		return "LeNet-5 classification network (DL4JLeNet) with training variables\n" + "sampleSize = " + sampleSize + "\n"
				+ "nChannels = " + nChannels + "\n"
				+ "batchSize = " + batchSize + "\n"
				+ "nEpochs = " + nEpochs + "\n"
				+ "iterations = " + iterations +"\n"
				+ "seed = " + seed + "\n"
				+ "learningRate = " + learningRate +"\n"
				+ "momentum = " + momentum + "\n"
				+ "regularization constant = " + regConst + "\n";
	}
}

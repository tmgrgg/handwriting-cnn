package main;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.BorderLayout;

import javax.swing.JButton;

import org.datavec.api.writable.Text;

import preprocessing.Contrastify;
import preprocessing.ContrastifyWithAdaptiveThreshold;
import preprocessing.DefineBoundaries;
import preprocessing.DefineSquareBoundaries;
import preprocessing.FitToBoundaries;
import preprocessing.ImageProcess;
import preprocessing.ImageProcessLibrary;
import preprocessing.Negate;
import preprocessing.Preprocessor;
import preprocessing.Resize;
import preprocessing.ResizeIfNecessary;
import segmenter.NewWhiteSpaceSegmenter;
import segmenter.NoiseResistantWhitespaceSegmenter;
import segmenter.Segment;
import segmenter.Segmenter;
import segmenter.WhitespaceSegmenter;
import transcription.AdvancedFuzzyDictionary;
import transcription.AdvancedFuzzyDictionary.ScoreString;
import transcription.DictionaryAnalyser;
import transcription.FuzzyChar;
import transcription.FuzzyDictionary;
import transcription.FuzzyString;
import transcription.LineParserByBoundary;
import transcription.LineParserForDisconnectedComponents;
import transcription.NewLineParser;
import transcription.FrontRunnerLineParserMidpoints;
import transcription.Parser;
import transcription.TranscriptionData;
import transcription.WordParserByPageAverage;
import transcription.WordParserViaSmudge;
import classifier.ClassifierLoadingFailureException;
import classifier.DL4JLeNet;
import classifier.FuzzyClassifier;
import classifier.FuzzyLeNet;
import classifier.ImageClassifier;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import javax.swing.JEditorPane;
import javax.swing.JRadioButton;
import javax.swing.JTextPane;

import java.awt.Color;

import javax.swing.JCheckBox;

public class DEMO {

	/*
	 * OLD String imageClassString =
	 * "a:b:c:d:e:f:g:h:k:l:m:n:o:p:q:r:s:t:u:v:w:x:y:z"; String testTrainPath =
	 * "C:\\Users\\Study\\Documents\\HOME\\OCR\\ocr\\images"; String networkPath
	 * = "C:\\Users\\Study\\Documents\\HOME\\OCR\\ocr\\networks\\LeNet-95.zip";
	 */

	

	JCheckBox checkBoxNoisy;
	JCheckBox checkBoxTest;
	JCheckBox checkBoxSeg;
	
	
	JComboBox<String> netList;

	// Output stuff
	private static String outputFolder = "C:\\Users\\Study\\Documents\\HOME\\OCR\\ocr\\images\\output";
	private static String segmenterPPOutputString = outputFolder
			+ "\\segmenterPreprocessorOutput.png";
	private static String segmentsString = outputFolder + "\\segments\\";
	private static String preprocessedSegmentsString = outputFolder
			+ "\\preprocessedSegments\\";
	private static String classifierTestString = outputFolder
			+ "\\classifierTestOutput.txt";
	private static String textOutputString = outputFolder
			+ "\\textOutputString.txt";
	private static String textOutputDictionaryString = outputFolder
			+ "\\textOutputDictionaryString.txt";
	private static String dictionaryString = "C:\\Users\\Study\\Documents\\HOME\\OCR\\ocr\\dictionary\\dictionary-10000.txt";

	// Screen properties
	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	private int screenWidth = (int) Math.floor(screenSize.getWidth());
	private int screenHeight = (int) Math.floor(screenSize.getHeight());

	// Components
	private JFrame frmTestEnv;
	private JTextField txtFilename;
	private JButton btnChooseFile;
	private JButton btnTranscribe;

	private JButton btnClfrPP;
	private JButton btnSegPP;
	private JButton btnClfrTst;
	private JButton btnSegs;
	private JButton btnFuzzy;
	private JButton btnOut;
	private JButton btnDict;

	private JEditorPane editorPane;
	private JTextField textFieldSeg;
	private JTextField textFieldThresh;

	HandwritingRecogniser recogniser;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println("Failed to set look and feel");
		}
		/* HANDLE GUI */
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DEMO window = new DEMO();
					window.frmTestEnv.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public DEMO() {
		
		initialize();
		
		buildRecogniser();
		
	}
	
	public void buildRecogniser() {
		List<ImageProcess> classifierProcesses = new ArrayList<ImageProcess>();
		
		
		System.out.println(netList.getSelectedItem());
		
		String imageClassString = "a:b:c:d:e:f:g:h:i:j:k:l:m:n:o:p:q:r:s:t:u:v:w:x:y:z";
		String testTrainPath;
		String networkPath;
		int sampleSize;
		
		if(netList.getSelectedItem().equals("net1")) {
			imageClassString = "a:b:c:d:e:f:g:h:k:l:m:n:o:p:q:r:s:t:u:v:w:x:y:z";
			testTrainPath = "C:\\Users\\Study\\Documents\\HOME\\OCR\\ocr\\images";
			networkPath = "C:\\Users\\Study\\Documents\\HOME\\OCR\\ocr\\networks\\LeNet-95.zip";
			classifierProcesses.add((ImageProcess) new FitToBoundaries());
			classifierProcesses.add((ImageProcess) new Resize(32, 32));
			classifierProcesses.add((ImageProcess) new Contrastify());
			sampleSize = 32;

		} else {
			testTrainPath = "C:\\Users\\Study\\Documents\\HOME\\OCR\\ocr\\images\\newnewTRAINING";
			networkPath = "C:\\Users\\Study\\Documents\\HOME\\OCR\\ocr\\networks\\trainedNetsWithStats830trainingSet\\net74\\net";
			classifierProcesses.add((ImageProcess) new FitToBoundaries());
			classifierProcesses.add((ImageProcess) new Resize(28, 28));
			classifierProcesses.add((ImageProcess) new DefineSquareBoundaries());
			classifierProcesses.add((ImageProcess) new Contrastify());
			sampleSize = 28;
		}
		
		recogniser = new HandwritingRecogniser(imageClassString, testTrainPath,
				networkPath, dictionaryString, classifierProcesses, sampleSize);
		try {
			recogniser.initialise();
		} catch (ClassifierLoadingFailureException e) {
			e.printStackTrace();
			System.out.println("Neural net was not found.");
		} catch (NullClassifierException e) {
			e.printStackTrace();
			System.out.println("Neural net was not found.");
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmTestEnv = new JFrame();
		frmTestEnv
				.setIconImage(Toolkit
						.getDefaultToolkit()
						.getImage(
								"C:\\Users\\Study\\Documents\\HOME\\OCR BACKUP 18-02-2017\\ocr\\images\\train\\s\\2.png"));
		frmTestEnv.setBounds(100, 100, 450, 443);
		frmTestEnv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmTestEnv.getContentPane().setLayout(null);
		frmTestEnv.setTitle("Test Environment");

		txtFilename = new JTextField();
		txtFilename
				.setText("C:\\Users\\Study\\Documents\\HOME\\OCR\\ocr\\images\\input\\input.png");
		txtFilename.setBounds(109, 12, 315, 20);
		frmTestEnv.getContentPane().add(txtFilename);
		txtFilename.setColumns(10);
		
		String[] netStrings = { "net1", "net2"};

		//Create the combo box, select item at index 4.
		//Indices start at 0, so 4 specifies the pig.
		netList = new JComboBox<String>(netStrings);
		netList.setSelectedIndex(1);
		
		netList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				buildRecogniser();
			}
		});
		
		netList.setBounds(10, 100, 89, 23);
		frmTestEnv.getContentPane().add(netList);

		

		btnChooseFile = new JButton("Choose File");
		btnChooseFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				FileDialog fd = new FileDialog(frmTestEnv, "Choose a file",
						FileDialog.LOAD);
				fd.setDirectory("C:\\Users\\Study\\Documents\\HOME\\OCR\\ocr\\images\\testEx");
				fd.setFile("*.jpg;*.png");
				fd.setVisible(true);
				String filename = fd.getDirectory() + fd.getFile();
				if (fd.getFile() == null) {
					// do nothing for now
					filename = "C:\\Users\\Study\\Documents\\HOME\\OCR\\ocr\\images\\input.png";
				} else {
					txtFilename.setText(filename);
					btnClfrPP.setEnabled(false);
					btnSegPP.setEnabled(false);
					btnClfrTst.setEnabled(false);
					btnSegs.setEnabled(false);
				}
			}
		});

		btnChooseFile.setBounds(10, 11, 89, 23);
		frmTestEnv.getContentPane().add(btnChooseFile);

		btnTranscribe = new JButton(">>>");
		btnTranscribe.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				translateImageWithInfo(ImageProcessLibrary
						.imageFromPath(txtFilename.getText()));
				btnClfrPP.setEnabled(true);
				btnSegPP.setEnabled(true);
				btnClfrTst.setEnabled(true);
				btnSegs.setEnabled(true);
				btnFuzzy.setEnabled(true);
				btnOut.setEnabled(true);
				btnDict.setEnabled(true);

				try {
					editorPane.setText(new Scanner(new File(textOutputString))
							.useDelimiter("\\Z").next());
				} catch (FileNotFoundException e) {
					System.out
							.println("Output was not written so could not print to panel");
				}
			}
		});
		btnTranscribe.setBounds(10, 42, 89, 23);
		frmTestEnv.getContentPane().add(btnTranscribe);

		btnClfrPP = new JButton("ClfrPP");
		btnClfrPP.setEnabled(false);
		btnClfrPP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				FileDialog fd = new FileDialog(frmTestEnv,
						"Classifier Preprocesses", FileDialog.LOAD);
				fd.setDirectory(preprocessedSegmentsString);
				fd.setFile("*.png");
				fd.setVisible(true);
				if (fd.getFile() == null) {
					// do nothing
				} else {
					displayImageFromPath(fd.getDirectory() + fd.getFile());
				}
			}
		});
		btnClfrPP.setBounds(303, 76, 70, 23);
		frmTestEnv.getContentPane().add(btnClfrPP);

		btnClfrTst = new JButton("Clfr Tst");
		btnClfrTst.setEnabled(false);
		btnClfrTst.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFrame frmClfrTst = new JFrame();
				frmClfrTst.setBounds(100, 100, 500, 500);
				frmClfrTst.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frmClfrTst.getContentPane().setLayout(new FlowLayout());
				frmClfrTst.setTitle("Classifier Test Output");

				JEditorPane testOutPane = new JEditorPane();
				testOutPane.setBounds(100, 100, 400, 400);
				testOutPane.setEditable(false);
				frmClfrTst.getContentPane().add(testOutPane);
				try {
					testOutPane.setText(new Scanner(new File(
							classifierTestString)).useDelimiter("\\Z").next());
				} catch (FileNotFoundException e) {
					System.out
							.println("Failed to write classifier test results to JPanel");
				}
				frmClfrTst.setVisible(true);
			}
		});
		btnClfrTst.setBounds(62, 76, 70, 23);
		frmTestEnv.getContentPane().add(btnClfrTst);

		btnSegPP = new JButton("SegPP");
		btnSegPP.setEnabled(false);
		btnSegPP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				displayImageFromPath(segmenterPPOutputString);
			}
		});
		btnSegPP.setBounds(143, 76, 70, 23);
		frmTestEnv.getContentPane().add(btnSegPP);

		btnSegs = new JButton("Segs");
		btnSegs.setEnabled(false);
		btnSegs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				FileDialog fd = new FileDialog(frmTestEnv, "Segments",
						FileDialog.LOAD);
				fd.setDirectory(segmentsString);
				fd.setFile("*.png");
				fd.setVisible(true);
				if (fd.getFile() == null) {
					// do nothing
				} else {
					displayImageFromPath(fd.getDirectory() + fd.getFile());
				}
			}
		});
		btnSegs.setBounds(223, 76, 70, 23);
		frmTestEnv.getContentPane().add(btnSegs);

		editorPane = new JEditorPane();
		editorPane.setEditable(false);
		editorPane.setBounds(98, 141, 310, 252);
		frmTestEnv.getContentPane().add(editorPane);

		btnFuzzy = new JButton("fuzzy");
		btnFuzzy.setEnabled(false);
		btnFuzzy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				FileDialog fd = new FileDialog(frmTestEnv,
						"Fuzzy Classifier Output", FileDialog.LOAD);
				fd.setDirectory(segmentsString);
				fd.setFile("*.png");
				fd.setVisible(true);
				if (fd.getFile() == null) {
					// do nothing
				} else {
					Image image = ImageProcessLibrary.imageFromPath(fd
							.getDirectory() + fd.getFile());
					FuzzyChar fuzzyChar = recogniser.fuzzyClassify(image);
					editorPane.setText(fuzzyChar.toString());
				}
			}
		});
		btnFuzzy.setBounds(178, 110, 70, 23);
		frmTestEnv.getContentPane().add(btnFuzzy);

		btnOut = new JButton("out");
		btnOut.setEnabled(false);
		btnOut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// TODO: Don't do this in one line
				try {
					editorPane.setText(new Scanner(new File(textOutputString))
							.useDelimiter("\\Z").next());
				} catch (FileNotFoundException e) {
					System.out
							.println("Output was not written so could not print to panel");
				}
			}
		});
		btnOut.setBounds(338, 110, 70, 23);
		frmTestEnv.getContentPane().add(btnOut);

		btnDict = new JButton("dict");
		btnDict.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					editorPane.setText(new Scanner(new File(
							textOutputDictionaryString)).useDelimiter("\\Z")
							.next());
				} catch (FileNotFoundException e) {
					System.out
							.println("Dictionary Output was not written so could not print to panel");
				}
			}
		});
		btnDict.setEnabled(false);
		btnDict.setBounds(258, 110, 70, 23);
		frmTestEnv.getContentPane().add(btnDict);

		checkBoxNoisy = new JCheckBox("Noisy");
		checkBoxNoisy.setBounds(31, 168, 57, 23);
		frmTestEnv.getContentPane().add(checkBoxNoisy);

		checkBoxTest = new JCheckBox("Test");
		checkBoxTest.setBounds(31, 188, 57, 23);
		frmTestEnv.getContentPane().add(checkBoxTest);
		
		checkBoxSeg = new JCheckBox("Seg");
		checkBoxSeg.setBounds(31, 208, 57, 23);
		frmTestEnv.getContentPane().add(checkBoxSeg);
	}

	/*
	 * private void buildTranscriptionComponents() { // Build classifier
	 * preprocessor List<ImageProcess> classifierProcesses = new
	 * ArrayList<ImageProcess>(); classifierProcesses.add((ImageProcess) new
	 * FitToBoundaries()); // /CLASSIFIER IS CURRENTLY NOT TRAINED TO USE THIS
	 * vv // classifierProcesses.add((ImageProcess) new
	 * DefineSquareBoundaries()); classifierProcesses.add((ImageProcess) new
	 * Resize(32, 32)); classifierProcesses.add((ImageProcess) new
	 * Contrastify());
	 * 
	 * classifierpp = new Preprocessor(classifierProcesses);
	 * 
	 * // Build classifier
	 * 
	 * 
	 * if ((classifier = DL4JLeNet.load(imageClassString, testTrainPath,
	 * networkPath, new Preprocessor(classifierProcesses))) == null) {
	 * classifier = new DL4JLeNet(imageClassString, new Preprocessor(
	 * classifierProcesses), testTrainPath); classifier.train();
	 * classifier.save(networkPath); }
	 * 
	 * boolean noisy = checkBoxNoisy.isSelected(); System.out.println("NOISY" +
	 * noisy);
	 * 
	 * // Build segmenter preprocessor List<ImageProcess>
	 * nonNoisysegmenterProcesses = new ArrayList<ImageProcess>();
	 * List<ImageProcess> noisySegmenterProcesses = new
	 * ArrayList<ImageProcess>(); int limit_area = 20000; //
	 * segmenterProcesses.add(new ResizeIfNecessary(limit_area));
	 * noisySegmenterProcesses.add(new ContrastifyWithAdaptiveThreshold());
	 * nonNoisysegmenterProcesses.add(new Contrastify());
	 * 
	 * nonNoisysegmenterpp = new Preprocessor(nonNoisysegmenterProcesses);
	 * 
	 * noisysegmenterpp = new Preprocessor(noisySegmenterProcesses);
	 * noisySegmenter = new NoiseResistantWhitespaceSegmenter(noisysegmenterpp);
	 * nonNoisySegmenter = new NewWhiteSpaceSegmenter(nonNoisysegmenterpp);
	 * 
	 * // Build parsers // lineParser = new LineParserByBoundary(); //
	 * lineParser = new LineParserForDisconnectedComponents(); lineParser = new
	 * FrontRunnerLineParserMidpoints(); // wordParser = new
	 * WordParserByPageAverage(); wordParser = new WordParserViaVerticalHit();
	 * 
	 * // Build fuzzyClassifier
	 * 
	 * fuzzyClassifier = FuzzyLeNet.load(imageClassString, testTrainPath,
	 * networkPath, new Preprocessor(classifierProcesses));
	 * 
	 * }
	 */
	private void translateImageWithInfo(Image image) {
		// Set cursor to wait
		frmTestEnv.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		recogniser.transcribe(image, checkBoxNoisy.isSelected(), checkBoxSeg.isSelected());

		// OUTPUT
		boolean test = checkBoxTest.isSelected();
		// write segmenter preprocessed image

		try {
			ImageIO.write(
					(RenderedImage) recogniser.getSegmenterPPOutputImage(),
					"png", new File(segmenterPPOutputString));
		} catch (IOException e) {
			System.out.println("Failed to write classifier preprocessed image");
		}

		// clear directory and write segments
		File segDirectory = new File(segmentsString);
		for (File file : segDirectory.listFiles()) {
			file.delete();
		}

		for (int i = 0; i < recogniser.getSegImages().size(); i++) {
			// write segments so we can explicitly see order
			File segmentsOutputFile = new File(segmentsString + i + ".png");
			try {
				ImageIO.write((RenderedImage) recogniser.getSegImages().get(i),
						"png", segmentsOutputFile);
			} catch (IOException e) {
				System.out.println("Failed to output segment ordering test");
			}
		}

		// clear directory and write classifier preprocess segments
		File ppSegDirectory = new File(preprocessedSegmentsString);
		for (File file : ppSegDirectory.listFiles()) {
			file.delete();
		}
		for (int i = 0; i < recogniser.getPreprocessedSegImages().size(); i++) {
			// write segments so we can explicitly see order
			File segmentsOutputFile = new File(preprocessedSegmentsString + i
					+ ".png");
			try {
				ImageIO.write((RenderedImage) recogniser
						.getPreprocessedSegImages().get(i), "png",
						segmentsOutputFile);
			} catch (IOException e) {
				System.out.println("Failed to output segment ordering test");
			}
		}

		if (test) {
			// write classifier test results
			List<String> testLines = new ArrayList<String>();
			testLines.add(recogniser.getTestLog());

			Path classifierTestOutputFile = Paths.get(classifierTestString);
			try {
				Files.write(classifierTestOutputFile, testLines,
						Charset.forName("UTF-8"));
			} catch (IOException e) {
				System.out.println("Failed to write classifier test results");
			}
		}

		// write text outputf
		Path file = Paths.get(textOutputString);
		try {
			Files.write(file, recogniser.getOutputLines(),
					Charset.forName("UTF-8"));
		} catch (IOException e) {
			System.out.println("Failed to write textual output");
		}

		// write dictionary output
		try {
			PrintWriter dictionaryOut = new PrintWriter(
					textOutputDictionaryString);
			dictionaryOut.print(recogniser.getOutputWithDictionary());
			dictionaryOut.close();
		} catch (IOException e) {
			System.out.println("Failed to write dictionary output to file");
		}

		// reset cursor
		frmTestEnv.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		boolean noisy = checkBoxNoisy.isSelected();
		System.out.println("NOISY" + noisy);

	}

	private void displayImageFromPath(String path) {
		BufferedImage img = (BufferedImage) ImageProcessLibrary
				.imageFromPath(path);
		int width = img.getWidth();
		int height = img.getHeight();

		// if the image is too small... make the biggest edge 500
		if (width < 500 && height < 500) {
			double sf = Math.min((double) 500 / height, (double) 500 / width);
			height = (int) Math.floor(sf * height);
			width = (int) Math.floor(sf * width);
			BufferedImage newImg = new BufferedImage(width, height,
					img.getType());
			Graphics2D g2d = newImg.createGraphics();
			g2d.drawImage(img, 0, 0, width, height, null);
			g2d.dispose();
			img = newImg;
		}

		// if the image is too big make the biggest edge equal to the
		// screenlimit - 200
		if (width > screenWidth - 200 || height > screenHeight - 200) {
			double sf = Math.max((double) width / (screenWidth - 200),
					(double) height / (screenHeight - 200));
			sf = 1 / sf;
			height = (int) Math.floor(sf * height);
			width = (int) Math.floor(sf * width);
			BufferedImage newImg = new BufferedImage(width, height,
					img.getType());
			Graphics2D g2d = newImg.createGraphics();
			g2d.drawImage(img, 0, 0, width, height, null);
			g2d.dispose();
			img = newImg;
		}

		ImageIcon icon = new ImageIcon(img);
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new FlowLayout());
		frame.setSize(width + 100, height + 100);
		JLabel lbl = new JLabel();
		lbl.setIcon(icon);
		frame.getContentPane().add(lbl);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
}

package classifier;

import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import preprocessing.ImageProcess;
import preprocessing.ImageProcessLibrary;
import preprocessing.Preprocessor;

public abstract class ImageClassifier {

	Preprocessor preprocessor;
	String path;
	String imageClassString;
	List<ImageClass> imageClasses = new ArrayList<ImageClass>();

	public ImageClassifier(String imageClassString, Preprocessor preprocessor,
			String path) {
		this.imageClassString = imageClassString;
		this.path = path;
		this.preprocessor = preprocessor;
		inferImageClasses();
	}

	private void inferImageClasses() {
		String[] labels = imageClassString.split(":");

		for (String label : labels) {
			imageClasses.add(new ImageClass(label));
		}
	}

	// trains from 'path'//train
	public abstract TrainingData train();

	// Resets the classifier so that it can be retrained (used for testing)
	public abstract void reset();

	public abstract ImageClass classify(Image image);

	// tests from 'path//test'
	public TestData test() {
		TestData datalog = new TestData(this.toString(), imageClasses);
		String testPath = path + "\\test";

		// Get a list of the labelled folders
		for (ImageClass imageClass : imageClasses) {

			File labelFolder = new File(testPath + "\\" + imageClass.getLabel());

			for (File imageFile : labelFolder.listFiles()) {
				//Skip over random files that might appear
				if (imageFile.getPath().endsWith(".png")) {

					try {
						ImageClass outputClass = classify(ImageIO
								.read(imageFile));

						datalog.updateConfusionMatrix(outputClass, imageClass);
					} catch (IOException e) {
						System.out.println(e
								+ " ::: Failed to read test file: "
								+ imageFile.getPath());
					}
				}
			}
		}
		return datalog;
	}

	public abstract void save(String networkPath);
}

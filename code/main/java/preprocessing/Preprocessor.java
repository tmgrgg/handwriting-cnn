package preprocessing;

import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

public class Preprocessor {

	boolean test = true;
	
	List<ImageProcess> preprocesses;
	
	public Preprocessor(List<ImageProcess> preprocesses) {
		this.preprocesses = preprocesses;
	}
	
	public void addPreprocess(ImageProcess preprocess) {
		preprocesses.add(preprocess);
	}
	
	public Image preprocess(Image image) {
		int[][] pixelData = ImageProcessLibrary.pixelDataFromImage(image);
		for (ImageProcess process : preprocesses) {
			pixelData = process.process(pixelData);
		}
		
		if (test) {
		try {
			ImageIO.write(
					(RenderedImage) ImageProcessLibrary.imageFromPixelData(pixelData),
					"png", new File("C:\\Users\\Study\\Documents\\HOME\\OCR\\ocr\\images\\preProcessTestOut.png"));
		} catch (IOException e) {
			System.out.println("Failed to write classifier preprocessed image");
		}
		}
		
		return ImageProcessLibrary.imageFromPixelData(pixelData);
	}
	
	public String toString() {
		String processes = "";
		for (ImageProcess process : preprocesses) {
			processes += process + " ";
		}
		return "Preprocesses: " + processes;
	}
}

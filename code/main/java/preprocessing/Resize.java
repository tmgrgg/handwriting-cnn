package preprocessing;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Resize implements ImageProcess {

	private int height;
	private int width;

	public Resize(int height, int width) {
		this.height = height;
		this.width = width;
	}

	public int[][] process(int[][] pixelData) {
		BufferedImage inputImage = (BufferedImage) ImageProcessLibrary
				.imageFromPixelData(pixelData);
		// creates output image
		
		
		//DON'T WANT TO RESIZE IF ITS ALREADY THE CORRECT SIZE - UNNECESSARILY DEGRADES
		//QUALITY
		BufferedImage outputImage;
		if (inputImage.getHeight(null) != height || inputImage.getWidth(null) != width) {
		outputImage = new BufferedImage(width, height,
				inputImage.getType());

		// scales the input image to the output image
		Graphics2D g2d = outputImage.createGraphics();
		g2d.drawImage(inputImage, 0, 0, width, height, null);
		g2d.dispose();
		} else {
			outputImage = inputImage;
		}

		return ImageProcessLibrary.pixelDataFromImage(outputImage);
	}
	
	public String toString() {
		return "Resize" + "(" + height + "," + width + ")";
	}
}
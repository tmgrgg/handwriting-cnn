package preprocessing;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Scale implements ImageProcess {

	private double scaleFactor;

	public Scale(double scaleFactor) {
		this.scaleFactor = scaleFactor;
	}

	public int[][] process(int[][] pixelData) {
		BufferedImage inputImage = (BufferedImage) ImageProcessLibrary
				.imageFromPixelData(pixelData);
		int height = (int) Math.round(scaleFactor*pixelData.length);
		int width = (int) Math.round(scaleFactor*pixelData[0].length);
		// creates output image
		BufferedImage outputImage = new BufferedImage(width, height,
				inputImage.getType());

		// scales the input image to the output image
		Graphics2D g2d = outputImage.createGraphics();
		g2d.drawImage(inputImage, 0, 0, width, height, null);
		g2d.dispose();

		return ImageProcessLibrary.pixelDataFromImage(outputImage);
	}
}
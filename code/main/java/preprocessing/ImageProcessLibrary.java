package preprocessing;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.GrayFilter;

//Might want to look into OpenCV (see report and wiki page) for some nice functionalities

public class ImageProcessLibrary {

	public static final int WHITE = -1;
	public static final int BLACK = -16777216;


	public static int[][] pixelDataFromImage(Image inputImage) {
		BufferedImage image = (BufferedImage) inputImage;
		int width = image.getWidth();
		int height = image.getHeight();

		int[][] result = new int[height][width];

		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				result[row][col] = image.getRGB(col, row);
			}
		}
		return result;
	}

	public static Image imageFromPixelData(int[][] pixelData) {
		BufferedImage image = new BufferedImage(pixelData[0].length,
				pixelData.length, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < pixelData[0].length; x++) {
			for (int y = 0; y < pixelData.length; y++) {
				image.setRGB(x, y, pixelData[y][x]);
			}
		}
		return image;
	}

	public static int CountPNGFiles(String path) {
		File[] Files = new File(path).listFiles();
		int count = 0;
		for (File file : Files) {
			if (file.getName().endsWith(".png")) {
				count++;
			}
		}
		return count;
	}

	public static Image imageFromPath(String path) {
		Image image = null;
		File file = new File(path);
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return image;
	}

	// needs to be applied to an image before it enters the system
	public static Image greyScaleImage(BufferedImage colorImage) {
		BufferedImage image = new BufferedImage(colorImage.getWidth(),
				colorImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		Graphics g = image.getGraphics();
		g.drawImage(colorImage, 0, 0, null);
		g.dispose();

		return image;
	}
}

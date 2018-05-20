package preprocessing;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ContrastifyWithAdaptiveThreshold implements ImageProcess {

	public int[][] process(int[][] pixelData) {


		Image startImage = ImageProcessLibrary.imageFromPixelData(pixelData);
		startImage = ImageProcessLibrary
				.greyScaleImage((BufferedImage) startImage);

		// startImage is now grey version of itself.
		int[][] newPixelData = new int[pixelData.length][pixelData[0].length];

		newPixelData = imageToPixelIntensityData(startImage);

		int s = Math.min((int) Math.round(pixelData.length / 8), (int) Math.round(pixelData[0].length / 8));
		int t = 11;

		// may need to grayscale

		int[][] in = newPixelData;
		int w = pixelData[0].length;
		int h = pixelData.length;

		int[][] intImg = new int[h][w];
		int[][] out = new int[h][w];

		
		for (int i = 0; i < w; i++) {
			int lsum = 0;
			for (int j = 0; j < h; j++) {
				lsum += in[j][i];
				if (i == 0) {
					intImg[j][i] = lsum;
				} else {
					intImg[j][i] = intImg[j][i - 1] + lsum;
				}
			}
		}

		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				// borders of moving window
				int x1 = i - (s / 2);
				int x2 = i + (s / 2);
				int y1 = j - (s / 2);
				int y2 = j + (s / 2);

				// border checking
				if (x1 < 1) {
					x1 = 1;
					x2 = s - 1;
				}
				if (x2 >= w - 1) {
					x2 = w - 2;
					x1 = (w - 2) - s;
				}
				if (y1 < 1) {
					y1 = 1;
					y2 = s - 1;
				}
				if (y2 >= h - 1) {
					y2 = h - 2;
					y1 = (h - 2) - s;
				}

				int count = (x2 - x1) * (y2 - y1);
				int sum = intImg[y2][x2] + intImg[y1 - 1][x1 - 1] - (intImg[y1 - 1][x2] + intImg[y2][x1 - 1]);

				if ((in[j][i] * count) <= (sum * (100 - t) / 100)) {
					out[j][i] = ImageProcessLibrary.BLACK;
				} else {
					out[j][i] = ImageProcessLibrary.WHITE;
				}
			}
		}

		// write image to file to check
		try {
			ImageIO.write(
					(RenderedImage) ImageProcessLibrary.imageFromPixelData(out),
					"png",
					new File(
							"C:\\Users\\Study\\Documents\\HOME\\OCR\\ocr\\images\\input\\test.png"));
		} catch (IOException e) {
			System.out.println("Failed to write testing process");
		}
		return out;
	}


	// this does proper pixelData conversion i.e. values are between 0 and 255
	private int[][] imageToPixelIntensityData(Image inputImage) {
		BufferedImage image = (BufferedImage) inputImage;
		int width = image.getWidth();
		int height = image.getHeight();

		int[][] result = new int[height][width];

		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				result[row][col] = image.getRGB(col, row) & 0xFF;
			}
		}
		return result;
	}
	
	public String toString() {
		return "contrastifyWithAdaptiveThreshold";
	}
}

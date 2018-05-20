package preprocessing;

import java.awt.Color;

public class ContrastifyByAverage implements ImageProcess {

	public int[][] process(int[][] pixelData) {
		
		int threshold = 35;
		int dimensions = pixelData.length * pixelData[0].length;
		int averageRed = 0;
		int averageBlue = 0;
		int averageGreen = 0;
		for (int y = 0; y < pixelData.length; y++) {
			for (int x = 0; x < pixelData[0].length; x++) {
				Color color = new Color(pixelData[y][x]);
				averageRed += color.getRed();
				averageGreen += color.getGreen();
				averageBlue += color.getBlue();
			}
		}

		averageRed /= dimensions;
		averageBlue /= dimensions;
		averageGreen /= dimensions;

		// -1 for white
		// -16777216 for black (not RGB but DL4J makes this difficult, so use these vals).
		for (int y = 0; y < pixelData.length; y++) {
			for (int x = 0; x < pixelData[0].length; x++) {
				Color color = new Color(pixelData[y][x]);
				int red = color.getRed();
				int green = color.getGreen();
				int blue = color.getBlue();

				if ((red + threshold < averageRed)
						&& (blue + threshold < averageBlue)
						&& (green + threshold < averageGreen)) {
					pixelData[y][x] = ImageProcessLibrary.BLACK;
				} else {
					pixelData[y][x] = ImageProcessLibrary.WHITE;
				}
			}
		}

		// We want to go in and remove little things with radius less than 2..
		// for now we ignore
		// the boundary and just set that to white

		for (int x = 0; x < pixelData[0].length; x++) {
			pixelData[0][x] = ImageProcessLibrary.WHITE;
			pixelData[pixelData.length - 1][x] = ImageProcessLibrary.WHITE;
		}

		for (int y = 0; y < pixelData.length; y++) {
			pixelData[y][0] = ImageProcessLibrary.WHITE;
			pixelData[y][pixelData[0].length - 1] = ImageProcessLibrary.WHITE;
		}

		// now find radius of less than 2 and DESTROY THEM

		for (int y = 1; y < pixelData.length - 1; y++) {
			for (int x = 1; x < pixelData[0].length - 1; x++) {
				if (pixelData[y][x] != ImageProcessLibrary.WHITE) {
					int radius = 1;
					if ((pixelData[y - 1][x - 1] != ImageProcessLibrary.WHITE)) {
						radius++;
					}
					if ((pixelData[y][x - 1] != ImageProcessLibrary.WHITE)) {
						radius++;
					}
					if ((pixelData[y + 1][x - 1] != ImageProcessLibrary.WHITE)) {
						radius++;
					}
					if ((pixelData[y + 1][x] != ImageProcessLibrary.WHITE)) {
						radius++;
					}
					if ((pixelData[y - 1][x] != ImageProcessLibrary.WHITE)) {
						radius++;
					}
					if ((pixelData[y - 1][x + 1] != ImageProcessLibrary.WHITE)) {
						radius++;
					}
					if ((pixelData[y][x + 1] != ImageProcessLibrary.WHITE)) {
						radius++;
					}
					if ((pixelData[y + 1][x + 1] != ImageProcessLibrary.WHITE)) {
						radius++;
					}
					if (radius <= 3) {
						pixelData[y][x] = ImageProcessLibrary.WHITE;
					}
				}
			}
		}
		return pixelData;
	}

	public String toString() {
		return "contrastify";
	}
}

package preprocessing;

import java.awt.Color;

public class ColourPlanes implements ImageProcess {

	public int[][] process(int[][] pixelData) {
		int r = 50;
		int g = 255;
		int b = 100;

		int r_update = 103;
		int g_update = 29;
		int b_update = 29;

		// trace boundaries from top left looking for things to fill

		// top left to top right
		for (int x = 0; x < pixelData[0].length - 1; x++) {
			if ((pixelData[0][x] != -1) && (pixelData[0][x + 1] == -1)) {
				Fill(x + 1, 0, new Color(r, g, b), pixelData);
				r = (r + r_update) % 255;
				g = (g + g_update) % 255;
				b = (b + b_update) % 255;
			}
		}

		// top right to bottom right
		for (int y = 0; y < pixelData.length - 1; y++) {
			if ((pixelData[y][pixelData[0].length - 1] != -1)
					&& (pixelData[y + 1][pixelData[0].length - 1] == -1)) {
				Fill(pixelData[0].length - 1, y + 1, new Color(r, g, b),
						pixelData);
				r = (r + r_update) % 255;
				g = (g + g_update) % 255;
				b = (b + b_update) % 255;
			}
		}

		// bottom right to bottom left
		for (int x = pixelData[0].length - 1; x > 0; x--) {
			if ((pixelData[pixelData.length - 1][x] != -1)
					&& (pixelData[pixelData.length - 1][x - 1] == -1)) {
				Fill(x - 1, pixelData.length - 1, new Color(r, g, b), pixelData);
				r = (r + r_update) % 255;
				g = (g + g_update) % 255;
				b = (b + b_update) % 255;
			}
		}

		// bottom left to top left
		for (int y = pixelData.length - 1; y > 0; y--) {
			if ((pixelData[y][0] != -1) && (pixelData[y - 1][0] == -1)) {
				Fill(0, y - 1, new Color(r, g, b), pixelData);
				r = (r + 10) % 255;
				g = (g + 25) % 255;
				b = (b + 100) % 255;
			}
		}
		return pixelData;
	}

	private static void Fill(int x, int y, Color fillColor, int[][] pixelData) {
		if ((y < 0) || (y >= pixelData.length) || (x < 0)
				|| (x >= pixelData[0].length)) {
			return;
		}
		if (pixelData[y][x] == -1) {
			pixelData[y][x] = fillColor.getRGB();
			Fill(x + 1, y, fillColor, pixelData);
			Fill(x - 1, y, fillColor, pixelData);
			Fill(x, y + 1, fillColor, pixelData);
			Fill(x, y - 1, fillColor, pixelData);
		}
	}

	public String toString() {
		return "ColourPlanes";
	}
}

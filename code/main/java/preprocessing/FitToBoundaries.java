package preprocessing;

public class FitToBoundaries implements ImageProcess {

	public int[][] process(int[][] pixelData) {
		int top = findTopBoundary(pixelData);
		int bottom = findBottomBoundary(pixelData);
		int left = findLeftBoundary(pixelData);
		int right = findRightBoundary(pixelData);

		int actualWidth = (right - left) + 1;
		int actualHeight = (bottom - top) + 1;

		int[][] newPixelData = new int[actualHeight][actualWidth];

		for (int y = 0; y < newPixelData.length; y++) {
			for (int x = 0; x < newPixelData[0].length; x++) {
				newPixelData[y][x] = pixelData[y + top][x + left];
			}
		}
		return newPixelData;
	}

	public String toString() {
		return "FitToBoundaries";
	}

	private static int findTopBoundary(int[][] pixelData) {
		for (int y = 0; y < pixelData.length; y++) {
			for (int x = 0; x < pixelData[0].length; x++) {
				if (pixelData[y][x] != -1) {
					return y;
				}
			}
		}
		return 0;
	}

	private static int findBottomBoundary(int[][] pixelData) {
		for (int y = pixelData.length - 1; y >= 0; y--) {
			for (int x = 0; x < pixelData[0].length; x++) {
				if (pixelData[y][x] != -1) {
					return y;
				}
			}
		}
		return 0;
	}

	private static int findRightBoundary(int[][] pixelData) {
		for (int x = pixelData[0].length - 1; x >= 0; x--) {
			for (int y = 0; y < pixelData.length; y++) {
				if (pixelData[y][x] != -1) {
					return x;
				}
			}
		}
		return 0;
	}

	private static int findLeftBoundary(int[][] pixelData) {
		for (int x = 0; x < pixelData[0].length; x++) {
			for (int y = 0; y < pixelData.length; y++) {
				if (pixelData[y][x] != -1) {
					return x;
				}
			}
		}
		return 0;
	}

}

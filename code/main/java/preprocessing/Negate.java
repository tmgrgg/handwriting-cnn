package preprocessing;

public class Negate implements ImageProcess {
	
	public int[][] process(int[][] pixelData) {
		for (int y = 0; y < pixelData.length; y++) {
			for (int x = 0; x < pixelData[0].length; x++) {
				if (pixelData[y][x] == ImageProcessLibrary.WHITE) {
					pixelData[y][x] = ImageProcessLibrary.BLACK;
				} else if (pixelData[y][x] == ImageProcessLibrary.BLACK) {
					pixelData[y][x] = ImageProcessLibrary.WHITE;
				}
			}
		}
		return pixelData;
	}

	public String toString() {
		return "contrastify";
	}
}

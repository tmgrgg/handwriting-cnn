package preprocessing;

public class Contrastify implements ImageProcess {
	
	public int[][] process(int[][] pixelData) {
		for (int y = 0; y < pixelData.length; y++) {
			for (int x = 0; x < pixelData[0].length; x++) {
				// -1 seems to be RGB for white
				// -16777216 seems to be RGB for black
				if (pixelData[y][x] != ImageProcessLibrary.WHITE) {
					pixelData[y][x] = ImageProcessLibrary.BLACK;
				}
			}
		}
		return pixelData;
	}

	public String toString() {
		return "contrastify";
	}
}

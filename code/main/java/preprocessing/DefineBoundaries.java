package preprocessing;

/*USAGE:
 * 
 * Segmentation is done as tightly as possible... meaning 
 * that the image presented will have no whitespace boundary around it...
 * This creates a problem in that when the image is downsampld to a fixed (square)
 * size by an image classifier... it is possible that the character presented in 
 * the image will lose its context.
 * 
 * Example: perfectly straight l would be downsampled to a black square which the classifier would have
 * a hard time matching to an l. (since it's a convolutional network).
 * 
 * The purpose of this preprocess is to add a fixed ratio whiteboundary to the edge of a segment to be classified...
 * this will hopefully improve image in the face of downsampling. 
 * */

//PRE: assumes that the pixelData presented is the image to be classified fitted precisely to its boundaries
// (to ensure this is the case, this should be used after a FitToBoundaries preprocess has been applied).

public class DefineBoundaries implements ImageProcess {

	
	//we downsample to about 32*32... so we want the boundary to be visible at that size... 
	//we therefore need the boundary to be bigger than 1/32 the size of the image in both 
	//dimensions (chose 2/32 = 1/16 just to be safe)).
	private double ratio = 1.2; 
	
	public int[][] process(int[][] pixelData) {
		int width = pixelData[0].length;
		int height = pixelData.length;
		
		int newWidth = (int) Math.round(width*ratio);
		int newHeight = (int) Math.round(height*ratio);
		
		int widthOffset = (newWidth - width)/2;
		int heightOffset = (newHeight - height)/2;
		

		//guaranteed to be all-zero
		int[][] newPixelData = new int[newHeight][newWidth];
		
		for (int y = 0; y < newHeight; y++) {
			for (int x = 0; x < newWidth; x++) {
				newPixelData[y][x] = ImageProcessLibrary.WHITE;
			}
		}

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				newPixelData[y + heightOffset][x + widthOffset] = pixelData[y][x];
			}
		}
		return newPixelData;
	}

	public String toString() {
		return "DefineBoundaries";
	}
}

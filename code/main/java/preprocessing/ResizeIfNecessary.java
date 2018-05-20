package preprocessing;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
public class ResizeIfNecessary implements ImageProcess {
		private int limit_area;
		
		public ResizeIfNecessary(int limit_area) {
			this.limit_area = limit_area;
		}

		public int[][] process(int[][] pixelData) {
			int width = pixelData[0].length;
			int height = pixelData.length;
			
			if (width*height <= limit_area) {
				return pixelData;
			} else {
				//need to find height' and width' s.t. height/width = height'/width' but force height'*width' = limit_area
				//currently we have height*width = K for some K > limit_area
				// height/sqrt(K) * width/sqrt(K) = 1
				// limit_area/sqrt(K) * height * limit_area/sqrt(K)*width = limit_area;
				
				double c = (double) limit_area/ (double) Math.sqrt(width*height);
				int heightPrime = (int) Math.round(c*height);
				int widthPrime = (int) Math.round(c*width);
				
				BufferedImage inputImage = (BufferedImage) ImageProcessLibrary
						.imageFromPixelData(pixelData);
				// creates output image
				BufferedImage outputImage = new BufferedImage(widthPrime, heightPrime,
						inputImage.getType());

				// scales the input image to the output image
				Graphics2D g2d = outputImage.createGraphics();
				g2d.drawImage(inputImage, 0, 0, widthPrime, heightPrime, null);
				g2d.dispose();

				return ImageProcessLibrary.pixelDataFromImage(outputImage);
				
 			}
		}
}

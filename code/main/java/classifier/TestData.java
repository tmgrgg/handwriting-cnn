package classifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestData {
	
	String title;
	List<ImageClass> imageClasses;
	
	//[y][x] - y (row) is output, [x] (column) is label
	int[][] confusionMatrix;
	List<String> log = new ArrayList<String>();
	
	public TestData(String title, List<ImageClass> imageClasses) {
		this.title = title;
		this.imageClasses = imageClasses;
		this.confusionMatrix = new int[imageClasses.size()][imageClasses.size()];
		
		//ensure zero array on construction
		for (int y = 0; y < confusionMatrix.length; y++) {
			for (int x = 0; x < confusionMatrix[0].length; x++) {
				confusionMatrix[y][x] = 0;
			}
		}
		
		log(title);
	}
	
	public void updateConfusionMatrix(ImageClass output, ImageClass label) {
		int y = imageClasses.indexOf(label);
		int x = imageClasses.indexOf(output);
		confusionMatrix[y][x]++;
	}
	
	public void log(String s) {
		log.add(s + "\n");
	}
	
	private double getCR() {
		int total = 0;
		int correct = 0;
		for (int i = 0; i < confusionMatrix.length; i++) {
			for (int j = 0; j < confusionMatrix[0].length; j++) {
				total += confusionMatrix[i][j];
				if (i == j) {
					correct += confusionMatrix[i][j];
				}
			}
		}
		return (double) correct / (double) total;
	}
	
	//TODO: accuracy, precision statistics etc.
	public String getLog() {
		log("::: Confusion Matrix :::");
		String cmString = "  [";
		
		for(int i = 0; i < imageClasses.size() - 1; i++) {
			cmString += imageClasses.get(i).toString() + ", ";
		}
			cmString += imageClasses.get(imageClasses.size() - 1).toString() + "]\n";
		for (int i = 0; i < imageClasses.size(); i++) {
			cmString += imageClasses.get(i).toString() + " " + Arrays.toString(confusionMatrix[i]) + "\n";
		}
		
		log(cmString);
		
		String stats =  "Stats: \n";
		
		stats += "Classification rate: " + getCR();
		
		log(stats);

		
		String concat = "";
		for (String s : log) {
			concat += s;
		}
		return concat;
	}

}

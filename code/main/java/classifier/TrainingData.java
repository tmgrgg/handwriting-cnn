package classifier;

import java.util.ArrayList;
import java.util.List;

import preprocessing.Preprocessor;

public class TrainingData {
	
	String title;
	Preprocessor preprocessor;
	List<ImageClass> imageClasses;
	List<String> log = new ArrayList<String>();
	
	TrainingData(String title, Preprocessor preprocessor, List<ImageClass> imageClasses) {
		this.title = title;
		this.preprocessor = preprocessor;
		this.imageClasses = imageClasses;
		
		String ic = "";

		for (ImageClass imageClass : imageClasses) {
			ic += imageClass;
		}
		log.add(title);
		log.add("Classifying: " + ic);
		log.add("Preprocesses: " + preprocessor);
	}
	
	public void log(String s) {
		log.add(s + "\n");
	}
	
	public String getLog() {
		String concat = "";
		for (String s : log) {
			concat += s;
		}
		return concat;
	}
}

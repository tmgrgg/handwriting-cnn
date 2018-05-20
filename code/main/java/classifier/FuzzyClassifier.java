package classifier;

import java.awt.Image;

import preprocessing.Preprocessor;
import transcription.FuzzyChar;

public abstract class FuzzyClassifier extends ImageClassifier {

	public FuzzyClassifier(String imageClassString, Preprocessor preprocessor,
			String path) {
		super(imageClassString, preprocessor, path);
	}
	
	public abstract FuzzyChar fuzzyClassify(Image image);

}

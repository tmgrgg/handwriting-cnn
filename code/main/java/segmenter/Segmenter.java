package segmenter;

import java.awt.Image;
import java.util.List;
import java.util.Set;

import preprocessing.Preprocessor;


//A segmenter should use its segment method to partition an image into segments - 
//Areas of interest defined by its boundaries within an image space
public abstract class Segmenter {
	
	Preprocessor preprocessor;
	
	Segmenter(Preprocessor preprocessor) {
		this.preprocessor = preprocessor;
	}
		
	//We use Set since a segmenter theoretically should not care
	//about the order of the elements - its job is just to find them
	public abstract Set<Segment> segment(Image image);
}
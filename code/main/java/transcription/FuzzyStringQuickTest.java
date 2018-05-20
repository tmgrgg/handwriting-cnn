package transcription;

import java.util.ArrayList;
import java.util.List;

import classifier.ImageClass;

public class FuzzyStringQuickTest {

	public static void main(String args[]) {

		ImageClass g1 = new ImageClass("g");
		double g1p = 0.5; 
		ImageClass h1 = new ImageClass("h");
		double h1p = 0.4;
		ImageClass t1 = new ImageClass("t");
		double t1p = 0.1;
		ImageClass e2 = new ImageClass("e");
		double e2p = 0.6;
		ImageClass a2 = new ImageClass("a");
		double a2p = 0.4;
		ImageClass g3 = new ImageClass("g");
		double g3p = 0.45;
		ImageClass y3 = new ImageClass("y");
		double y3p = 0.4;
		ImageClass r3 = new ImageClass("r");
		double r3p = 0.15; 
		
		
		List<ImageClass> imageClasses1 = new ArrayList<ImageClass>();
		imageClasses1.add(g1);
		imageClasses1.add(h1);
		imageClasses1.add(t1);
		
		List<Double> distribution1 = new ArrayList<Double>();
		distribution1.add(g1p);
		distribution1.add(h1p);
		distribution1.add(t1p);
		
		
		List<ImageClass> imageClasses2 = new ArrayList<ImageClass>();
		imageClasses2.add(e2);
		imageClasses2.add(a2);
		
		List<Double> distribution2 = new ArrayList<Double>();
		distribution2.add(e2p);
		distribution2.add(a2p);

		List<ImageClass> imageClasses3 = new ArrayList<ImageClass>();
		imageClasses3.add(g3);
		imageClasses3.add(y3);
		imageClasses3.add(r3);
		
		List<Double> distribution3 = new ArrayList<Double>();
		distribution3.add(g3p);
		distribution3.add(y3p);
		distribution3.add(r3p);
		
		FuzzyChar fc1 = new FuzzyChar(imageClasses1, distribution1);
		FuzzyChar fc2 = new FuzzyChar(imageClasses2, distribution2);
		FuzzyChar fc3 = new FuzzyChar(imageClasses3, distribution3);
		
		List<FuzzyChar> fuzzyStringList = new ArrayList<FuzzyChar>();
		
		fuzzyStringList.add(fc1);
		fuzzyStringList.add(fc2);
		fuzzyStringList.add(fc3);
		
		FuzzyString fuzzyString = new FuzzyString(fuzzyStringList);
		
		System.out.println("FuzzyChar1.size" + fc1.size());
		System.out.println("FuzzyChar2.size" + fc2.size());
		System.out.println("FuzzyChar3.size" + fc3.size());
		
		List<String> strings = fuzzyString.morphMatrixGetNMostLikely(10);
		

		
	}
}

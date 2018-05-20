package classifier;
/*
 * Contains information about a possible classification of an image given to an ImageClassifier,
 * i.e. the name of the class and the path to its /training and /test folders.
 */

public class ImageClass {

	private final String label;

	public ImageClass(String name) {
		this.label = name;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (!(other instanceof ImageClass)) {
			return false;
		} 
		if (((ImageClass) other).getLabel() == this.getLabel()) {
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return label.hashCode();
	}
	
	@Override
	public String toString() {
		return this.getLabel();
	}
}

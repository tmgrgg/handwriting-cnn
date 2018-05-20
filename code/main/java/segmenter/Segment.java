package segmenter;

import java.awt.Image;
import java.awt.image.BufferedImage;

import preprocessing.ImageProcessLibrary;
import classifier.ImageClass;

public class Segment {
	Image image;
	ImageClass imageClass;
	int x;
	int y;

	// co-ordinates of boundaries relative to the page they're on
	int top;
	int bottom;
	int left;
	int right;

	public Segment(Image image, int top, int bottom, int right, int left) {
		this.image = image;
		this.top = top;
		this.bottom = bottom;
		this.right = right;
		this.left = left;
		this.x = (int) Math.round(0.5*right  + 0.5*left);
		this.y = (int) Math.round(0.5*top  + 0.5*bottom);
	}

	public int getTop() {
		return top;
	}

	public int getBottom() {
		return bottom;
	}

	public int getRight() {
		return right;
	}

	public int getLeft() {
		return left;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public Image getImage() {
		return ((BufferedImage) image).getSubimage(left, top, right - (left - 1), bottom - (top - 1));
	}

	public Segment join(Segment other) {
		return new Segment(this.image, Math.min(this.top, other.top), Math.max(
				this.bottom, other.bottom), Math.max(this.right, other.right),
				Math.min(this.left, other.left));
	}

	// equality based on position, not image
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (!(o instanceof Segment)) {
			return false;
		}
		Segment other = (Segment) o;

		return (other.top == this.top) && (other.bottom == this.bottom)
				&& (other.right == this.right) && (other.left == this.left);
	}

	@Override
	public int hashCode() {
		return 2 * top + 3 * bottom + 5 * left + 7 * right;
	}

	/*
	 * @Override public String toString() { return imageClass.toString(); }
	 */

}

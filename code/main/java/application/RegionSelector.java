package application;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import preprocessing.ImageProcessLibrary;


enum Context { MATHS, WORDS };

public class RegionSelector {


	public RegionSelector(final String path) {

		SelectionPane selectionPane = new SelectionPane(path, this);

		JFrame frame = new JFrame("HWR Example Application");
		// frame.getContentPane().setLayout(new FlowLayout());
		frame.setSize(selectionPane.getWidth(), selectionPane.getHeight());
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		frame.getContentPane().add(selectionPane);
		// frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}

class SelectionPane extends JLabel {

	private Rectangle highlightedSelection;
	private int width;
	private int height;
	List<Image> subImages;
	List<Selection> selections;

	// current image
	BufferedImage actualImage;
	BufferedImage displayedImage;

	public SelectionPane(String path, RegionSelector parent) {

		// We need to maintain the original image, but also need an image on
		// which we can paint selection boxes (displayedImage)
		actualImage = buildImageFromPath(path);

		displayedImage = new BufferedImage(actualImage.getWidth(),
				actualImage.getHeight(), actualImage.getType());
		Graphics g = displayedImage.getGraphics();
		g.drawImage(actualImage, 0, 0, null);
		g.dispose();

		selections = new ArrayList<Selection>();
		subImages = new ArrayList<Image>();

		width = displayedImage.getWidth(null);
		height = displayedImage.getHeight(null);

		ImageIcon icon = new ImageIcon(displayedImage);
		setIcon(icon);
		// add(lbl);

		// setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		KeyListener kl = new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_U) {
					if (selections.size() > 0) {
						// System.out.println("HI");
						selections.remove(selections.size() - 1);
						refreshSelections();
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}
		};

		// need to ensure that the pane is focusable so that keyListener works
		setFocusable(true);
		addKeyListener(kl);

		MouseAdapter ma = new MouseAdapter() {

			private Point clickPoint;

			boolean mouse_clicked = false;

			private Point startPoint;
			private Point endPoint;

			@Override
			public void mousePressed(MouseEvent e) {
				if (!mouse_clicked) {
					
					mouse_clicked = true;
					clickPoint = startPoint = e.getPoint();
					highlightedSelection = null;
				} else {
					highlightedSelection = null;
					repaint();
					
					Context context = Context.WORDS;
					if (SwingUtilities.isLeftMouseButton(e)) {
						context = Context.WORDS;
					} else if (SwingUtilities.isRightMouseButton(e)) {
						context = Context.MATHS;
					}
					
					endPoint = e.getPoint();
					addNewSelection(startPoint, endPoint, context);
					mouse_clicked = false;
				}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				if (mouse_clicked) {
					Point dragPoint = e.getPoint();
					int x = Math.min(clickPoint.x, dragPoint.x);
					int y = Math.min(clickPoint.y, dragPoint.y);

					int width = Math.max(clickPoint.x, dragPoint.x) - x;
					int height = Math.max(clickPoint.y, dragPoint.y) - y;

					if (highlightedSelection == null) {
						highlightedSelection = new Rectangle(x, y, width,
								height);
					} else {
						highlightedSelection.setBounds(x, y, width, height);
					}
					repaint();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

		};

		addMouseListener(ma);
		addMouseMotionListener(ma);

		// let the pane be visible;
		setVisible(true);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	// used to highlight selected sections
	private void addNewSelection(Point startPoint, Point endPoint, Context context) {
		// add the previous image to history (so that we can undo)

		// redraw the background image with the selected area highlighted (in
		// green)
		
		Selection newSelection = new Selection(startPoint, endPoint, context);
		selections.add(newSelection);
		refreshSelections();
	}

	private void refreshSelections() {

		// refresh the image so that we don't draw rectangles on top of
		// rectangles
		Graphics g = displayedImage.getGraphics();
		g.drawImage(actualImage, 0, 0, null);

		//g.setColor(Color.GREEN);
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setComposite(AlphaComposite.SrcOver.derive(0.5f));
		for (Selection selection : selections) {
			g2d.setColor(ContextColor(selection.getContext()));
			g2d.fill(selection.getRectangle());
			g2d.draw(selection.getRectangle());
		}
		g2d.drawImage(displayedImage, 0, 0, null);
		g2d.dispose();
		g.dispose();

		repaint();
	}
	
	private Color ContextColor(Context context) {
		if (context == Context.WORDS) {
			return Color.GREEN;
		} else {
			return Color.BLUE;
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(200, 200);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (highlightedSelection != null) {
			g.setColor(Color.LIGHT_GRAY);
			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setComposite(AlphaComposite.SrcOver.derive(0.5f));
			g2d.fill(highlightedSelection);
			g2d.dispose();
			g2d = (Graphics2D) g.create();
			g2d.draw(highlightedSelection);
			g2d.dispose();
		}
	}

	// Screen properties
	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	private int screenWidth = (int) Math.floor(screenSize.getWidth());
	private int screenHeight = (int) Math.floor(screenSize.getHeight());

	private BufferedImage buildImageFromPath(String path) {
		BufferedImage img = (BufferedImage) ImageProcessLibrary
				.imageFromPath(path);
		int width = img.getWidth();
		int height = img.getHeight();

		// if the image is too small... make the biggest edge 500
		if (width < 500 && height < 500) {
			double sf = Math.min((double) 500 / height, (double) 500 / width);
			height = (int) Math.floor(sf * height);
			width = (int) Math.floor(sf * width);
			BufferedImage newImg = new BufferedImage(width, height,
					img.getType());
			Graphics2D g2d = newImg.createGraphics();
			g2d.drawImage(img, 0, 0, width, height, null);
			g2d.dispose();
			img = newImg;
		}

		// if the image is too big make the biggest edge equal to the
		// screenlimit - 200
		if (width > screenWidth - 200 || height > screenHeight - 200) {
			double sf = Math.max((double) width / (screenWidth - 200),
					(double) height / (screenHeight - 200));
			sf = 1 / sf;
			height = (int) Math.floor(sf * height);
			width = (int) Math.floor(sf * width);
			BufferedImage newImg = new BufferedImage(width, height,
					img.getType());
			Graphics2D g2d = newImg.createGraphics();
			g2d.drawImage(img, 0, 0, width, height, null);
			g2d.dispose();
			img = newImg;
		}

		return img;
	}
}

class Selection {
	private Rectangle rectangle;
	private Context context;
	
	public Selection(Point startPoint, Point endPoint, Context context) {
		this.context = context;
		
		int x = Math.min(startPoint.x, endPoint.x);
		int y = Math.min(startPoint.y, endPoint.y);

		int width = Math.max(startPoint.x, endPoint.x) - x;
		int height = Math.max(startPoint.y, endPoint.y) - y;

		this.rectangle = new Rectangle(x, y, width, height);
	}
	
	public Rectangle getRectangle() {
		return rectangle;
	}
	
	public void setRectangle(Rectangle rectangle) {
		this.rectangle = rectangle;
	}
	
	public Context getContext() {
		return context;
	}
		
}

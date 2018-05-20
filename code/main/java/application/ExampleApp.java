package application;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import javax.swing.JButton;


import preprocessing.ImageProcessLibrary;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

import javax.swing.JCheckBox;

public class ExampleApp {

	// Transcription Components
	JCheckBox checkBoxNoisy;
	
	// Screen properties
	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	private int screenWidth = (int) Math.floor(screenSize.getWidth());
	private int screenHeight = (int) Math.floor(screenSize.getHeight());

	// Components
	private JFrame frame;
	private JTextField txtFilename;
	private JButton btnChooseFile;
	private JButton btnTranscribe;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println("Failed to set look and feel");
		}
		/* HANDLE GUI */
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ExampleApp window = new ExampleApp();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ExampleApp() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame
				.setIconImage(Toolkit
						.getDefaultToolkit()
						.getImage(
								"C:\\Users\\Study\\Documents\\HOME\\OCR BACKUP 18-02-2017\\ocr\\images\\train\\s\\2.png"));
		frame.setBounds(100, 100, 450, 137);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setTitle("Example Application");

		txtFilename = new JTextField();
		txtFilename
				.setText("C:\\Users\\Study\\Documents\\HOME\\OCR\\ocr\\images\\input\\input.png");
		txtFilename.setBounds(109, 12, 315, 20);
		frame.getContentPane().add(txtFilename);
		txtFilename.setColumns(10);

		btnChooseFile = new JButton("Choose File");
		btnChooseFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				FileDialog fd = new FileDialog(frame, "Choose a file",
						FileDialog.LOAD);
				fd.setDirectory("C:\\Users\\Study\\Documents\\HOME\\OCR\\ocr\\images\\input");
				fd.setFile("*.jpg");
				fd.setFile("*.png");
				fd.setVisible(true);
				String filename = fd.getDirectory() + fd.getFile();
				if (fd.getFile() == null) {
					// do nothing for now
					//filename = "C:\\Users\\Study\\Documents\\HOME\\OCR\\ocr\\images\\input.png";
				} else {
					txtFilename.setText(filename);
					//Open text selection panel
					RegionSelector regionSelector = new RegionSelector(filename);
				}
			}
		});

		btnChooseFile.setBounds(10, 11, 89, 23);
		frame.getContentPane().add(btnChooseFile);

		btnTranscribe = new JButton(">>>");
		btnTranscribe.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		btnTranscribe.setBounds(10, 42, 89, 23);
		frame.getContentPane().add(btnTranscribe);

		checkBoxNoisy = new JCheckBox("Noisy");
		checkBoxNoisy.setBounds(10, 72, 57, 23);
		frame.getContentPane().add(checkBoxNoisy);
	}


	private void displayImageFromPath(String path) {
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

		ImageIcon icon = new ImageIcon(img);
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new FlowLayout());
		frame.setSize(width + 100, height + 100);
		JLabel lbl = new JLabel();
		lbl.setIcon(icon);
		frame.getContentPane().add(lbl);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
}

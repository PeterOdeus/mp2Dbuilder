package org.mp2dbuilder.viewer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {

	private Image img;
	private Image img2;

	public Image getImg() {
		return img;
	}

	public void setImages(Image img, Image img2, Image mcsImg) {
		this.img = img;
		this.img2 = img2;
		this.mcsImg = mcsImg;
	}

	private Image mcsImg;

	public ImagePanel(Image img, Image img2, Image mcsImg) {
		setImages(img, img2, mcsImg);
		Dimension size = null;
		if (mcsImg == null) {
			size = new Dimension(img.getWidth(null) * 2 + 10, img
					.getHeight(null));
		} else {
			size = new Dimension(img.getWidth(null) * 3 + 10, img
					.getHeight(null));
		}
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);
		setSize(size);
		setLayout(null);
	}

	public void paintComponent(Graphics g) {
		g.drawImage(img, 0, 0, null);
		g.drawImage(img2, img.getWidth(null), 0, null);
		if (mcsImg != null) {
			g.drawImage(mcsImg, (int) Math.round(img.getWidth(null) * 2), 0,
					null);
		}
	}

}

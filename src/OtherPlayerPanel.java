import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

public class OtherPlayerPanel extends JPanel{
	String name;
	
	JLabel name_label;
	JLabel text;
	JLabel[] cards;
	Border border;
	
	private ImageIcon card_back;
	private int width;
	private int height;
	private int card_num;

	public OtherPlayerPanel() {
		setLayout(new BorderLayout());
		JPanel text_panel = new JPanel();
		text_panel.setLayout(new GridLayout(2,1));
		name_label = new JLabel();
		name_label.setFont(name_label.getFont().deriveFont(18.0f));
		name_label.setHorizontalAlignment(SwingConstants.CENTER);
		text = new JLabel();
		text.setFont(text.getFont().deriveFont(18.0f));
		text.setHorizontalAlignment(SwingConstants.CENTER);
		text_panel.add(name_label);
		text_panel.add(text);
		add(text_panel, "North");

		JPanel card_panel = new JPanel();
		add(card_panel, "South");

		BufferedImage original = null;
		BufferedImage resize = null;
		try {
			original = ImageIO.read(new File("./cards/back.png"));
			resize = resize(original);
		} catch (IOException e) {
			e.printStackTrace();
		}
		width = resize.getWidth();
		height = resize.getHeight();
		card_back = new ImageIcon(resize);

		cards = new JLabel[5];
		int i=0;
		while(i<5) {
			cards[i] = new JLabel();
			card_panel.add(cards[i]);
			i++;
		}
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setPlayer() {
		name_label.setText(name);
		text.setText(" ");
		int i = 0;
		while(i<5) {
			cards[i].setIcon(card_back);
			cards[i].setPreferredSize(new Dimension(width, height));
			border = BorderFactory.createLineBorder(Color.black, 1);
			cards[i].setBorder(border);
			i++;
		}
	}
	
	public void reset() {
		text.setText(" ");
		int i = 0;
		while(i<5) {
			cards[i].setIcon(card_back);
			cards[i].setPreferredSize(new Dimension(width, height));
			border = BorderFactory.createLineBorder(Color.black, 1);
			cards[i].setBorder(border);
			i++;
		}
	}
	
	public void delete() {
		name = "";
		name_label.setText(name);
		text.removeAll();
		int i=0;
		while(i<5) {
			cards[i].setIcon(null);
			cards[i].setBorder(null);
			i++;
		}
	}

	private BufferedImage resize(BufferedImage original){
		int width = original.getWidth();
		int height = original.getHeight();
		int n_width = 0;
		int n_height = 0;

		if(width>height){
			n_width = 90;
			n_height = (int) (n_width*(height/(float)width));
		}
		else{
			n_height = 90;
			n_width = (int) (n_height*(width/(float)height));
		}

		BufferedImage outputImage = new BufferedImage(n_width, n_height, original.getType());

		Graphics2D graphics2D = outputImage.createGraphics();
		graphics2D.drawImage(original, 0, 0, n_width, n_height, null);
		graphics2D.dispose();

		return outputImage;
	}
	
	void setCardImage(ImageIcon icon) {
		cards[card_num].setIcon(icon);
		card_num++;
		if(card_num == 5) {
			card_num = 0;
		}
	}
	
	void setText(String msg) {
		text.setText(msg);
	}
}

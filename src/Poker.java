import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

public class Poker extends JFrame implements ActionListener {

	Socket socket;
	PrintWriter out;
	BufferedReader in;

	JPanel my;
	JButton[] my_cards;
	JLabel mytext;
	JDialog start_dialog;
	JButton change;

	JButton restart;

	int other_player_num = 0;
	int width = 0;
	int height = 0;
	String player_name;

	List<ImageIcon> card_list;
	HashMap<ImageIcon, Card> card_name;
	List<ImageIcon> small_card_list;
	List<String> mycard_icon = new ArrayList<String>();
	List<Card> mycard = new ArrayList<Card>();
	int[] button_click = {0,0,0,0,0};
	int change_button_click = 0;
	DeckPower deckpower;

	Border default_border;

	OtherPlayerPanel[] otherpanel = new OtherPlayerPanel[5];
	HashMap<String, OtherPlayerPanel> otherplayer = new HashMap<String, OtherPlayerPanel>();

	JPanel Cardgame = new JPanel();

	Poker(){
		super("Poker");
		setBounds(200,80,1210,600);	
		setPlayerName();
		setUI();
		setCard();
		setMenu();
		connect();
		addchatting();

		setResizable(false);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				out.println("exit");
				System.exit(0);
			}
		});
		setVisible(true);
	}

	void setPlayerName() {
		JDialog d = new JDialog(this,"이름",true);
		d.setLayout(new FlowLayout());
		d.setBounds(200,100,200,80);
		JLabel l = new JLabel("이름 :");
		JTextField dtf = new JTextField(6);
		JButton b = new JButton("확인");
		b.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				player_name = dtf.getText();
				d.setVisible(false);
				d.dispose();
			}
		});
		d.add(l);
		d.add(dtf);
		d.add(b);
		d.setModal(true);
		d.setVisible(true);
	}

	void setCard() {
		card_list = new ArrayList<ImageIcon>();
		card_name = new HashMap<ImageIcon, Card>();
		small_card_list = new ArrayList<ImageIcon>();
		BufferedImage original = null;
		BufferedImage resize = null;
		for(int i=1; i<=52; i++) {
			try {
				original = ImageIO.read(new File("./cards/"+i+".png"));
				resize = resize(original,1);
				card_list.add(new ImageIcon(resize));
				small_card_list.add(new ImageIcon(resize(original,2)));
			} catch (IOException e) {
			}
		}
		width = resize.getWidth();
		height = resize.getHeight();

		int i=0;
		while(i < 52) {
			Shape shape = null;
			switch(i%4) {
			case 0:
				shape = Shape.SPADE;
				break;
			case 1:
				shape = Shape.DIAMOND;
				break;
			case 2:
				shape = Shape.HEART;
				break;
			case 3:
				shape = Shape.CLOVER;
				break;
			}
			switch(i/4) {
			case 0:
				card_name.put(card_list.get(i), new Card(shape, Value.ACE));
				break;
			case 1:
				card_name.put(card_list.get(i), new Card(shape, Value.TWO));
				break;
			case 2:
				card_name.put(card_list.get(i), new Card(shape, Value.THREE));
				break;
			case 3:
				card_name.put(card_list.get(i), new Card(shape, Value.FOUR));
				break;
			case 4:
				card_name.put(card_list.get(i), new Card(shape, Value.FIVE));
				break;
			case 5:
				card_name.put(card_list.get(i), new Card(shape, Value.SIX));
				break;
			case 6:
				card_name.put(card_list.get(i), new Card(shape, Value.SEVEN));
				break;
			case 7:
				card_name.put(card_list.get(i), new Card(shape, Value.EIGHT));
				break;
			case 8:
				card_name.put(card_list.get(i), new Card(shape, Value.NINE));
				break;
			case 9:
				card_name.put(card_list.get(i), new Card(shape, Value.TEN));
				break;
			case 10:
				card_name.put(card_list.get(i), new Card(shape, Value.JACK));
				break;
			case 11:
				card_name.put(card_list.get(i), new Card(shape, Value.QUEEN));
				break;
			case 12:
				card_name.put(card_list.get(i), new Card(shape, Value.KING));
				break;
			}
			i++;
		}
	}

	void setMenu() {
		JMenuBar mb = new JMenuBar();
		JMenu menu = new JMenu("menu");
		JMenuItem item = new JMenuItem("menual");
		item.addActionListener(this);
		menu.add(item);
		mb.add(menu);
		setJMenuBar(mb);
	}

	void setUI() {
		// 1개
		Cardgame.setLayout(new BorderLayout());
		JPanel up = new JPanel();
		up.setLayout(new GridLayout(2,2));
		Cardgame.add(up, "Center");


		for(int i=0; i<4; i++) {
			otherpanel[i] = new OtherPlayerPanel();
			up.add(otherpanel[i]);
		}//4개만들고 panel에 넣음

		//my card
		my = new JPanel();
		my.setLayout(new BorderLayout());
		mytext = new JLabel();
		mytext.setFont(mytext.getFont().deriveFont(18.0f));
		mytext.setHorizontalAlignment(SwingConstants.CENTER);
		mytext.setPreferredSize(new Dimension(900,60));
		my.add(mytext, "North");

		JPanel down_panel = new JPanel();
		down_panel.setLayout(new BorderLayout());
		my.add(down_panel, "Center");

		JPanel my_card_panel = new JPanel();
		down_panel.add(my_card_panel, "Center");
		my_cards = new JButton[5];
		for(int i=0; i<5; i++) {
			my_cards[i] = new JButton();
			my_cards[i].setActionCommand("button"+i);
			my_cards[i].addActionListener(this);
			my_card_panel.add(my_cards[i]);
			my_cards[i].setVisible(false);
		}
		default_border = my_cards[0].getBorder();

		JPanel button_panel = new JPanel();
		down_panel.add(button_panel, "East");
		change = new JButton("Card Change");
		change.addActionListener(this);
		button_panel.add(change);

		restart = new JButton("Ready");
		restart.addActionListener(this);
		button_panel.add(restart);
		restart.setVisible(false);

		Cardgame.add(my, "South");
		my.setVisible(false);
	}

	void addchatting() {
		JPanel chatting = new Chatting(player_name);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, Cardgame, chatting);
		Cardgame.setMinimumSize(new Dimension(900,600));
		chatting.setMaximumSize(new Dimension(310,600));
		add(splitPane);
	}

	void connect() {
		try {
			socket = new Socket("127.0.0.1", 7777);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			out.println(player_name);
			Receiver receiver = new Receiver();
			receiver.start();

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void viewMyCard() {
		Collections.sort(mycard_icon, new Mycomparator()); //정렬
		System.out.println(mycard_icon);
		IconToName();
		for(int i=0; i<5; i++) {
			int index = Integer.parseInt(mycard_icon.get(i));
			ImageIcon icon = card_list.get(index);
			my_cards[i].setIcon(icon);
			my_cards[i].setPreferredSize(new Dimension(width, height));
			my_cards[i].setVisible(true);
		}
	}

	void IconToName() {
		mycard.clear();
		for(int i=0; i<5; i++) {
			int index = Integer.parseInt(mycard_icon.get(i));
			Card cn = card_name.get(card_list.get(index));
			System.out.println(cn);
			mycard.add(cn);
		}
		System.out.println(mycard);
		deckpower = new DeckPower(mycard);
		System.out.println(mycard);
		setMyText(deckpower.toString());
	}

	void setMyText(String msg) {
		mytext.setText(msg);
	}

	void resetChange() {
		for(int i=0; i<5; i++) {
			if(button_click[i] == 1) {
				button_click[i] = 0;
				my_cards[i].setBorder(default_border);
			}
		}
	}

	BufferedImage resize(BufferedImage original, int use){
		int width = original.getWidth();
		int height = original.getHeight();
		int n_width = 0;
		int n_height = 0;

		if(use == 1){
			if(width>height){
				n_width = 120;
				n_height = (int) (n_width*(height/(float)width));
			}
			else{
				n_height = 120;
				n_width = (int) (n_height*(width/(float)height));
			}
		}
		else if(use == 2) {
			if(width>height){
				n_width = 90;
				n_height = (int) (n_width*(height/(float)width));
			}
			else{
				n_height = 90;
				n_width = (int) (n_height*(width/(float)height));
			}
		}

		BufferedImage outputImage = new BufferedImage(n_width, n_height, original.getType());

		Graphics2D graphics2D = outputImage.createGraphics();
		graphics2D.drawImage(original, 0, 0, n_width, n_height, null);
		graphics2D.dispose();

		return outputImage;
	}

	void startButton(){
		start_dialog = new JDialog(this, "Game Start", false);
		start_dialog.setBounds(590, 300, 130, 80);
		JButton start_button = new JButton("GAME START");
		start_button.addActionListener(this);
		start_dialog.add(start_button);
		start_dialog.setVisible(true);
		start_dialog.setResizable(false);
	}

	void Alert(String msg){
		final JDialog dialog = new JDialog(this, "알림");
		dialog.setLayout(new BorderLayout());
		dialog.setLocation(300,260);
		dialog.setSize(200,100);

		JLabel label= new JLabel(msg);
		JButton ok_button = new JButton("OK");
		label.setHorizontalAlignment(JLabel.CENTER);
		dialog.add(label, "Center");
		JPanel p = new JPanel(new FlowLayout());
		p.add(ok_button);
		dialog.add(p, "South");

		ok_button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				dialog.setVisible(false);
				dialog.dispose();
			}
		});

		dialog.setVisible(true);
		dialog.setResizable(false);
		dialog.pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String command = e.getActionCommand();
		if(command.equals("GAME START")){
			out.println(command);
		}
		else if(command.contains("button")) {
			int button_num = Integer.parseInt(command.substring(6));
			JButton b = (JButton)e.getSource();
			if(button_click[button_num] == 0) {
				button_click[button_num] = 1;
				Border border = BorderFactory.createLineBorder(Color.green, 5);
				b.setBorder(border);
			}
			else if(button_click[button_num] == 1) {
				button_click[button_num] = 0;
				b.setBorder(default_border);
			}
		}
		else if(command.equals("Card Change")) {
			change.setVisible(false);
			int choose_card = 0;
			ArrayList<String> remove_card = new ArrayList<String>();
			for(int i=0; i<5; i++) {
				if(button_click[i] == 1) {
					choose_card++;
					remove_card.add(mycard_icon.get(i));
				}
			}
			for(int i=0; i<remove_card.size(); i++) {
				mycard_icon.remove(remove_card.get(i));
			}
			out.println("#m#" + choose_card + "장 교환");
			out.println("change" + choose_card);
			resetChange();
		}
		else if(command.equals("Ready")) {
			out.println("ready");
			restart.setVisible(false);
		}
		else if(command.equals("menual")) {
			JDialog menual = new JDialog(this, "menual", false);
			menual.setSize(new Dimension(583, 400));
			menual.setLayout(new BorderLayout());
			JPanel p = new JPanel();
			BufferedImage menual_img = null;
			try {
				menual_img = ImageIO.read(new File("./cards/menual.jpg"));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			JLabel menual_label = new JLabel();
			menual_label.setIcon(new ImageIcon(menual_img));
			p.add(menual_label);
			JScrollPane sp = new JScrollPane(p);
			menual.add(sp, "Center");	
			menual.setVisible(true);
			menual.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		}
	}

	class Receiver extends Thread {
		public void run() {
			String line = "";
			while(true) {
				try {
					line = in.readLine();

					if(line.equals("host")){
						startButton();
					}
					else if(line.equals("fail")){
						Alert("fail"); //player number fail
					}
					else if(line.equals("ok")){
						start_dialog.setVisible(false);
						start_dialog.dispose();
					}
					else if(line.equals("start")) {
						my.setVisible(true);
					}
					else if(line.startsWith("add player")) {
						String a = line.substring(10);
						String b = a.substring(a.indexOf("[")+1, a.indexOf("]")).replace(" ", "");
						List<String> name_list = Arrays.asList(b.split(","));
						for(int i=0; i<name_list.size(); i++) {
							if(!name_list.get(i).equals(player_name)) {
								if(!otherplayer.containsKey(name_list.get(i))) {
									otherplayer.put(name_list.get(i), otherpanel[other_player_num]);
									otherpanel[other_player_num].setName(name_list.get(i));
									otherpanel[other_player_num].setPlayer();
									other_player_num++;
								}
							}
						}
					}
					else if(line.startsWith("#o#")) {
						String a = line.substring(3);
						String[] b = a.split("\\|");
						String c = b[1].substring(b[1].indexOf("[")+1, b[1].indexOf("]")).replace(" ", "");
						List<String> tmp = Arrays.asList(c.split(","));
						if(otherplayer.containsKey(b[0])) {
							for(int i=0; i<5; i++) {
								otherplayer.get(b[0]).setCardImage(small_card_list.get(Integer.parseInt(tmp.get(i))));
							}
						}
					}
					else if(line.equals("#g#")) {
						System.out.println("#g#보냈다");
						out.println("#g#"+mycard_icon);

						out.println(mycard);
					}
					else if(line.startsWith("#m#")) {
						String a = line.substring(3);
						String[] b = a.split("\\|");
						if(otherplayer.containsKey(b[0])) {
							otherplayer.get(b[0]).setText(b[1]);
						}
					}
					else if(line.startsWith("#w#")) {
						String tmp = line.substring(3);
						setMyText(tmp);
					}
					else if(line.equals("restart")) {
						restart.setVisible(true);
					}
					else if(line.equals("reset")) {
						change.setVisible(true);
						for(int i=0; i<other_player_num; i++) {
							otherpanel[i].reset();
						}
						mycard_icon.clear();
					}
					else if(line.startsWith("[")){
						String a = line.substring(line.indexOf("[")+1, line.indexOf("]")).replace(" ", "");
						mycard_icon.addAll(Arrays.asList(a.split(",")));
						viewMyCard();
					}
					else if(line.startsWith("#r#")) {
						String a = line.substring(3);
						other_player_num--;
						otherplayer.get(a).delete();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					break;
				}
			}		
		}
	}

	public static void main(String[] args) {
		new Poker();
	}
}

class Mycomparator implements Comparator<String>{
	@Override
	public int compare(String s1, String s2) {
		// TODO Auto-generated method stub
		int i1 = Integer.parseInt(s1);
		int i2 = Integer.parseInt(s2);
		if(i1>i2) {
			return 1;
		}
		return -1;
	}
}

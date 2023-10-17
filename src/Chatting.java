import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.ConnectException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class Chatting extends JPanel{
	String name;
	JPanel p = new JPanel();
	JTextField tf = new JTextField(25);
	JPanel join = new JPanel();
	JTextArea jta = new JTextArea();
	JTextPane jtp= new JTextPane();
	StyledDocument doc = jtp.getStyledDocument();


	Chatting(String name){
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(300,500));
		this.name = name;

		Label la = new Label(name);
		add(la, "North");

		p.setLayout(new FlowLayout());
		p.add(tf, "Center");

		JScrollPane scrollpane = new JScrollPane(jtp);
		add(scrollpane, "Center");

		add(p, "South");
		add(join, "East");

		join.setLayout(new BorderLayout());
		join.add(new Label("立加磊"),"North");

		join.add(jta,"Center");
		jta.setEditable(false);
		jta.setBackground(new Color(200, 200, 250));
		join.setPreferredSize(new Dimension(80,400));

		jtp.setEditable(false);
		jtp.setBackground(Color.lightGray);

		setVisible(true);
		tf.requestFocus(); 
/*
		//
		MenuBar mb = new MenuBar();
		Menu menu = new Menu("File");
		MenuItem Save, Exit;
		Save = new MenuItem("Save As...");
		Exit = new MenuItem("Exit");
		menu.add(Save);
		menu.addSeparator();
		menu.add(Exit);
		mb.add(menu);
		setMenuBar(mb);

		MenuHandler mh = new MenuHandler(jtp, this);
		Save.addActionListener(mh);
		Exit.addActionListener(mh);
		//
*/

		Thread receiver = null;
		try {
			String serverIp = "127.0.0.1";
			Socket socket = new Socket(serverIp, 7778);
			
			ChatHandler handler = new ChatHandler(tf, jtp, name, socket);
			jtp.addFocusListener(handler);
			tf.addFocusListener(handler);
			tf.addActionListener(handler);
			receiver = new Thread(new ClientReceiver(socket,jtp,jta,doc));

			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println(name);
			receiver.start();
		}
		catch(ConnectException ce) {
			ce.printStackTrace();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}

class MenuHandler implements ActionListener{
	JTextPane jtp;
	JFrame parent;

	MenuHandler(JTextPane jtp, JFrame p){
		this.jtp = jtp;
		this.parent = p;
	}

	void saveAs(String fileName){
		try {
			FileWriter output = new FileWriter(fileName+".txt");
			BufferedWriter bw = new BufferedWriter(output);
			String tmp = jtp.getText();
			bw.append(tmp);
			bw.close();
		} catch (IOException e) {}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if(command.equals("Save As...")){
			FileDialog fileSave= new FileDialog(parent,"颇老历厘",FileDialog.SAVE);
			fileSave.setVisible(true);
			String fileName = fileSave.getDirectory()+fileSave.getFile();
			saveAs(fileName);
		}
		else if(command.equals("Exit")){
			System.exit(0);
		}
	}

}

class ChatHandler extends FocusAdapter implements ActionListener{
	JTextPane jtp;
	JTextField tf;
	String name;
	PrintWriter out;
	ChatHandler(JTextField tf, JTextPane jtp, String name, Socket socket){
		this.tf = tf;
		this.jtp = jtp;
		this.name = name;
		try {
			out = new PrintWriter(socket.getOutputStream(), true);
		}
		catch(Exception e) {}
	}
	@Override
	public void actionPerformed(ActionEvent ae) {
		// TODO Auto-generated method stub
		String msg = tf.getText();
		if("".equals(msg))return;

		out.println("["+name+"] "+msg);
		tf.setText("");
	}

	public void focusGained(FocusEvent e) {
		tf.requestFocus();
	}
}

class ClientReceiver extends Thread{
	Socket socket;
	BufferedReader in;
	JTextArea connecting;
	StyledDocument doc;
	JTextPane jtp;

	ClientReceiver(Socket socket, JTextPane jtp, JTextArea jta, StyledDocument doc){
		this.socket = socket;
		this.connecting = jta;
		this.doc = doc;
		this.jtp = jtp;
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}
		catch(IOException e) {}
	}

	public void run() {
		String input = "";
		String color = "";
		String msg = "";
		while(true) {
			try {
				input = in.readLine();
				input = input+"\n";
				//System.out.println("chatting "+input);
				String[] s = new String[2];
				s = input.split("\\|", 2);
				msg = s[1];
				color = s[0];

				if(!(msg.startsWith("[")||msg.startsWith("#"))){
					String tmp = msg.replace("^", "\n");
					connecting.setText(tmp);
				}
				else if(msg.startsWith("#")){
					try {
						doc.insertString(doc.getLength(), msg, null);
					} catch (BadLocationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else{
					Style style = jtp.addStyle("new style", null);
					StyleConstants.setForeground(style, Color.decode(color).darker());
					try {
						doc.insertString(doc.getLength(), msg, style);
					} catch (BadLocationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			catch(IOException e) {break;}
		}
	}
}
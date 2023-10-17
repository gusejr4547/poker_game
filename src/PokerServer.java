import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class PokerServer {
	List<Manage> player;
	int player_num = 0;
	PrintWriter requestor;
	ServerSocket serverSocket = null;

	List<Integer> card_set;
	List<Integer> use_card_set;
	List<String> name_list;

	HashMap<String, DeckPower> deckpower_map;

	Entry<String, DeckPower> win;

	int change = 0;

	boolean gamestart = false;
	int winnercheck = -1;
	int readycheck = 0;
	int restartcheck = 0;

	HashMap clients;
	
	List<ManageState> state_list = new ArrayList<ManageState>();
	
	DBtest db;

	PokerServer(){
		player = new ArrayList<Manage>();
		Collections.synchronizedList(player);

		card_set = new ArrayList<Integer>();
		use_card_set = new ArrayList<Integer>();
		Collections.synchronizedList(use_card_set);

		name_list = new ArrayList<String>();

		deckpower_map = new HashMap<String, DeckPower>();

		clients = new HashMap();
		Collections.synchronizedMap(clients);

		for(int i=0; i<52; i++) {
			card_set.add(i);
		}
		
		db = new DBtest();

		serverSocket = null;
		Socket socket = null;
		try {
			serverSocket = new ServerSocket(7777);
			Accept thread = new Accept(serverSocket, socket, this);
			thread.start();

		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	void settingCard() {
		use_card_set.clear();
		use_card_set.addAll(card_set);
	}

	void gamestart() {
		settingCard();

		Iterator<Manage> it = player.iterator();
		while(it.hasNext()) {
			it.next().start();
		}

		WhoWin whowin = new WhoWin();
		whowin.start();
	}

	void sendToAll(String msg) {
		Iterator<Manage> it = player.iterator();

		while(it.hasNext()) {
			it.next().sendMessage(msg);
		}
	}

	void sendToAll(String msg, int color) {
		Iterator it = clients.keySet().iterator();

		while(it.hasNext()) {
			PrintWriter out = (PrintWriter) clients.get(it.next());
			out.println(color + "|" + msg);
		}
	}

	void sendToAllWin(String msg) {
		Iterator<Manage> it = player.iterator();

		while(it.hasNext()) {
			Manage tmp = it.next();
			if(tmp.name.equals(win.getKey())) {
				tmp.sendMessage(msg+" ***WIN***");
			}
			else {
				tmp.sendMessage(msg);
			}
		}
	}

	List<Integer> chooseCard(int choose_num) {
		List<Integer> mycard = new ArrayList<Integer>();

		for(int i=0; i<choose_num; i++) {
			synchronized(use_card_set) {
				int index = (int) (Math.random() * use_card_set.size());
				mycard.add(use_card_set.get(index));
				use_card_set.remove(index);
			}
		}

		return mycard;
	}

	void reset() {
		settingCard();
		deckpower_map.clear();
		winnercheck = -1;
		readycheck = 0;
		change = 0;
		restartcheck = 0;

		Iterator<Manage> it = player.iterator();
		while(it.hasNext()) {
			it.next().restart = true;
		}
	}

	class Accept extends Thread {
		ServerSocket serverSocket = null;
		Socket socket = null;
		PokerServer server;

		Accept(ServerSocket serverSocket, Socket socket, PokerServer server){
			this.serverSocket = serverSocket;
			this.socket = socket;
			this.server = server;
		}

		public void run() {
			while(player_num < 5 && !gamestart) {
				try {
					socket = serverSocket.accept();
					if(gamestart){
						break;
					}
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					String name = in.readLine();
					name_list.add(name);
					db.insert(name);
					Manage manage = new Manage(socket, server, name);
					player_num++;
					player.add(manage);
					sendToAll("add player" + name_list);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println(player_num);
				}

				if(player_num == 1) {
					try {
						PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
						out.println("host");
						GameStart gamestart = new GameStart(socket);
						gamestart.start();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}				
				System.out.println(player_num);
			}
			System.out.println("break while");
		}
	}

	class GameStart extends Thread {
		BufferedReader in;
		PrintWriter out;

		GameStart(Socket socket){
			try {
				this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				this.out = new PrintWriter(socket.getOutputStream(), true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void run() {
			String line = "";
			while(true) {
				try {
					line = in.readLine();
					System.out.println("GameStart "+line);
					if(line.equals("GAME START")){
						if(player_num > 1){
							out.println("ok");
							sendToAll("start");
							gamestart = true;
							gamestart();
							Restart r = new Restart();
							r.start();
							break;
						}
						else{
							out.println("fail");
						}
					}
				} catch (IOException e) {
					try {
						in.close();
						out.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					break;
				}
			}
		}
	}

	class Manage extends Thread{
		Socket socket;
		BufferedReader in;
		PrintWriter out;
		PokerServer server;

		String name;

		boolean restart = false;

		Manage(Socket socket, PokerServer server, String name){
			this.socket = socket;
			this.server = server;
			this.name = name;
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		void sendMessage(String message){
			out.println(message);
		}

		@Override
		public void run() {
			boolean start = true;
			while(true) {
				if(start) {
					Game game = new Game(socket, in, out, name);
					game.start();
					start = false;
				}

				while(true) {
					if(restart) {
						start = true;
						restart = false;
						break;
					}
					try {
						this.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	class Game extends Thread {
		Socket socket;
		BufferedReader in;
		PrintWriter out;

		List<Integer> mycard;
		String name;

		Game(Socket socket, BufferedReader in, PrintWriter out, String name){
			this.socket = socket;
			this.in = in;
			this.out = out;
			this.name = name;

			mycard = new ArrayList<Integer>();
		}

		void sendMessage(String message){
			out.println(message);
		}

		void disconnect(){
			player.remove(this);
			player_num--;
			try {
				in.close();
				out.close();
				socket.close();
				//objin.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void run() {
			mycard = chooseCard(5); //5 card choose
			System.out.println(this.getName() + " " + mycard);
			out.println(mycard);

			String msg = "";
			while(true) {
				try {
					msg = in.readLine();
					if(msg.contains("change")) {
						int change_num = Integer.parseInt(msg.substring(6));
						if(change_num == 0) {
							change++;
							out.println("#g#");
						}
						else {
							mycard = chooseCard(change_num);
							System.out.println(this.getName() + " " + mycard);
							out.println(mycard);
							change++;
							out.println("#g#");
						}

					}
					else if(msg.startsWith("#g#")) {
						System.out.println("#g#¹Þ¾Ò´Ù");
						String tmp = msg.substring(3);

						String card = in.readLine();
						System.out.println(card);
						DeckPower deckpower = new DeckPower(card);
						deckpower_map.put(name, deckpower);

						while(change != player_num) {
							try {
								this.sleep(500);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}//wait

						try {
							this.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						sendToAll("#o#" + name + "|" + tmp);

						while(winnercheck < 0) {};

						if(name.equals(win.getKey())) {		
							sendToAll("#m#"+name+"|"+deckpower+" ***WIN***");
							sendMessage("#w#" + deckpower + " ***WIN***");
							db.update(name);
						}
						else {
							sendToAll("#m#"+name+"|"+deckpower);
						}
						sendMessage("restart");

					}
					else if(msg.startsWith("#m#")) {
						String tmp = msg.substring(3);
						sendToAll("#m#" + name + "|" + tmp);
					}
					else if(msg.equals("ready")) {
						readycheck++;
						break;
					}
				} catch (IOException e) {
					disconnect();
				}
			}
		}
	}


	class WhoWin extends Thread {
		public void run() {
			while(player_num > deckpower_map.size()) {
				try {
					this.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			Iterator<Entry<String, DeckPower>> it = deckpower_map.entrySet().iterator();
			win = null;
			while(it.hasNext()) {
				Entry<String, DeckPower> tmp = it.next();
				if(win == null) {
					win = tmp;
				}
				else {
					if(win.getValue().compare(tmp.getValue())) {}
					else {
						win = tmp;
					}
				}
			}
			winnercheck = 1;
		}

	}

	class Restart extends Thread {
		public void run() {
			while(true) {
				if(readycheck == player_num) {
					sendToAll("reset");
					reset();
					readycheck = 0;
					WhoWin whowin = new WhoWin();
					whowin.start();
				}
				try {
					this.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	class ManageState extends Thread{
		BufferedReader in;
		PrintWriter out;
		Manage manage;
		
		ManageState(Socket socket, Manage manage){
			this.manage = manage;
			try {
				this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				this.out = new PrintWriter(socket.getOutputStream(), true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void run() {
			while(true) {
				try {
					String state = in.readLine();
					System.out.println("state : " + state);
					if(state.equals("exit")) {
						player.remove(manage);
						name_list.remove(manage.name);
						player_num--;
						sendToAll("#r#"+manage.name);
						System.out.println("break");
						state_list.remove(this);
						break;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		PokerServer server = new PokerServer();
	}
}

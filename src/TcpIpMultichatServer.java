
import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class TcpIpMultichatServer {
	HashMap clients;

	TcpIpMultichatServer(){
		clients = new HashMap();
		Collections.synchronizedMap(clients);
	}

	public void start() {
		ServerSocket serverSocket = null;
		Socket socket = null;

		try {
			serverSocket = new ServerSocket(7778);
			System.out.println("서버가 시작되었습니다.");
			while(true) {
				socket = serverSocket.accept();
				System.out.println("["+socket.getInetAddress()+":"+socket.getPort()+"]"+"에서 접속하였습니다.");
				ServerReceiver thread = new ServerReceiver(socket);
				thread.start();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	void sendToAll(String msg, int color) {
		Iterator it = clients.keySet().iterator();

		while(it.hasNext()) {
			PrintWriter out = (PrintWriter) clients.get(it.next());
			out.println(color + "|" + msg);
		}
	}

	public static void main(String[] args) {
		new TcpIpMultichatServer().start();
	}

	class ServerReceiver extends Thread{
		Socket socket;
		BufferedReader in;
		PrintWriter out;
		int color; 

		ServerReceiver(Socket socket){
			this.socket = socket;
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
			}
			catch(IOException e) {}
			Color c = new Color((int)(Math.random()*256),(int)(Math.random()*256),(int)(Math.random()*256));
			color = c.getRGB();
		}

		void sendName(){
			Iterator it = clients.keySet().iterator();
			String s = "";
			while(it.hasNext()){
				s += it.next().toString() + "^";
			}
			sendToAll(s, color);
		}

		public void run() {
			String name = "";
			try {
				name = in.readLine();
				sendToAll("#"+name+"님이 들어오셨습니다.", color);
				clients.put(name, out);
				System.out.println("현재 서버접속자 수는 "+clients.size()+"입니다.");

				sendName(); //접속자 이름 보내기

				while(in!=null) {
					sendToAll(in.readLine(), color);
				}
			}
			catch(IOException e) {}
			finally {
				sendToAll("#"+name+"님이 나가셨습니다.", color);
				clients.remove(name);
				sendName();
				System.out.println("["+socket.getInetAddress()+":"+socket.getPort()+"]"+"에서 접속을 종료하였습니다.");
				System.out.println("현재 서버접속자 수는 "+clients.size()+"입니다.");
			}
		}
	}
}
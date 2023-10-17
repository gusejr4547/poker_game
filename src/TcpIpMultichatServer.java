
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
			System.out.println("������ ���۵Ǿ����ϴ�.");
			while(true) {
				socket = serverSocket.accept();
				System.out.println("["+socket.getInetAddress()+":"+socket.getPort()+"]"+"���� �����Ͽ����ϴ�.");
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
				sendToAll("#"+name+"���� �����̽��ϴ�.", color);
				clients.put(name, out);
				System.out.println("���� ���������� ���� "+clients.size()+"�Դϴ�.");

				sendName(); //������ �̸� ������

				while(in!=null) {
					sendToAll(in.readLine(), color);
				}
			}
			catch(IOException e) {}
			finally {
				sendToAll("#"+name+"���� �����̽��ϴ�.", color);
				clients.remove(name);
				sendName();
				System.out.println("["+socket.getInetAddress()+":"+socket.getPort()+"]"+"���� ������ �����Ͽ����ϴ�.");
				System.out.println("���� ���������� ���� "+clients.size()+"�Դϴ�.");
			}
		}
	}
}
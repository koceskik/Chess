import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;

public class BughouseServer extends Thread {
	private final int PLAYER_TOTAL = 4;
	private ArrayList<Game> g = new ArrayList<Game>();
	private final int port = 3355;
	private ServerSocket server = null;
	private ArrayList<ClientHandler> playerList = new ArrayList<ClientHandler>();

	public static void main(String[] args) {
		new BughouseServer().start();
	}

	public BughouseServer() {
		g.add(new Game());
		g.add(new Game());

		//setup serversocket
		try {
			server = new ServerSocket(port);
			InetAddress ip = InetAddress.getLocalHost();
			System.out.println("Server IP address: " + ip.getHostAddress());
			System.out.println("Listening on port " + port);
		}
		catch(UnknownHostException e) {
			e.printStackTrace();
		}
		catch(IOException ex) {
			System.out.println("Error can't connect to port " + port);
			System.exit(1);
		}
	}
	@Override
	public void run() {
		//get clients, setup ClientHandlers
		while(playerList.size() < PLAYER_TOTAL) {
			try {
				Socket s = server.accept();
				ClientHandler ch = new ClientHandler(s);
				playerList.add(ch);
				System.out.println("Accepted client socket");
			}
			catch(IOException ex) {
				System.out.println("Failed to accept client OR create I/O streams");
				System.exit(1);
			}
		}

		Collections.shuffle(playerList);
		for(int i = 0;i<PLAYER_TOTAL;i++) {
			ClientHandler ch = playerList.get(i);
			Game clientGame = null;
			if(i < 2) {
				clientGame = g.get(0);
			}
			else {
				clientGame = g.get(1);
			}
			if(i % 2 == 0) {
				clientGame.pW.gameCount++;
				ch.send(clientGame.pW);
			}
			else {
				clientGame.pB.gameCount++;
				ch.send(clientGame.pB);
			}
			ch.send(g.get(0));
			ch.send(g.get(1));
		}
	}

	class ClientHandler {
		private Socket socket = null;
		private ObjectOutputStream oos = null;
		private InputHandler ih = null;

		public ClientHandler(Socket s) {
			this.socket = s;
			try {
				oos = new ObjectOutputStream(s.getOutputStream());
				ih = new InputHandler(s);
				ih.start();
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}

		class InputHandler extends Thread {
			private ObjectInputStream ois = null;
			public InputHandler(Socket s) {
				try {
					ois = new ObjectInputStream(s.getInputStream());
				}
				catch(IOException e) {
					e.printStackTrace();
				}
			}
			@Override
			public void run() {
				try {
					while(true) {
						Move move = (Move) ois.readObject();
						g.get(move.player.gameID).applyMove(move);//handles legal checks
						
						for(Piece p : g.get(move.player.gameID).turn.deadPieces) {
							//TODO: logic for getting the opponent's partner
						}

						for(ClientHandler ch : playerList) {
							if(ch == null) {
								playerList.remove(ch);
							}
							else {
								ch.send(g.get(move.player.gameID));//only sends the game that was changed
							}
						}
					}
				}
				catch(ClassNotFoundException e) {
					e.printStackTrace();
				}
				catch(IOException e) {
					e.printStackTrace();
				}
				finally {
					if(ois != null) {
						try {
							ois.close();
						}
						catch(IOException e) {
							e.printStackTrace();
						}
					}
					if(socket != null) {
						try {
							socket.close();
						}
						catch(IOException e) {
							e.printStackTrace();
						}
					}
					playerList.remove(this);//TODO: announce to other players, perhaps allow another player to play
				}
			}
		}

		public boolean send(Game g) {
			boolean returner = false;
			try {
				oos.writeObject(g);
				oos.reset();//necessary to send new Game object, not just references
				returner = true;
			}
			catch(IOException e) {
				e.printStackTrace();
				returner = false;
			}
			return returner;
		}
		public boolean send(Player p) {
			boolean returner = false;
			try {
				oos.writeObject(p);
				returner = true;
			}
			catch(IOException e) {
				e.printStackTrace();
				returner = false;
			}
			return returner;
		}

		public void close() {
			if(oos != null) {
				try {
					oos.close();
				}
				catch(IOException e) {
					e.printStackTrace();
				}
			}
			if(socket != null) {
				try {
					socket.close();
				}
				catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void closeSockets() {
		for(ClientHandler c : playerList) {
			if(c != null) {
				c.close();
			}
		}
		if(server != null) {
			try {
				server.close();
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Server Exited");
		System.exit(0);
	}
}

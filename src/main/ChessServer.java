package main;
import gameComponent.Game;
import gameComponent.Move;
import gameComponent.Player;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;

import piece.Piece;

public class ChessServer extends Thread {
	private final int PLAYER_TOTAL = 2;
	private Game g = null;
	private final int port = 3355;
	private ServerSocket server = null;
	private ArrayList<ClientHandler> playerList = new ArrayList<ClientHandler>();

	public static void main(String[] args) {
		new ChessServer().start();
	}

	public ChessServer() {
		g = new Game();

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
		while(playerList.size() < 2) {
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
			if(i % 2 == 0) {
				ch.send(g.pW);
			}
			else {
				ch.send(g.pB);
			}
			ch.send(g);
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
						g.applyMove(move);//handles legal checks
						int totalLegalMoves = 0;
						for(Piece p : g.turn.pieceList) {
							totalLegalMoves += p.getLegalMoves(g, false).size();
						}
						if(totalLegalMoves == 0) {
							g.setWinner(g.getOpponent());
						}

						for(ClientHandler ch : playerList) {
							if(ch == null) {
								playerList.remove(ch);
							}
							else {
								ch.send(g);
							}
						}
					}
				}
				catch(ClassNotFoundException e) {
					//e.printStackTrace();
				}
				catch(IOException e) {
					//e.printStackTrace();
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
				oos.reset();
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

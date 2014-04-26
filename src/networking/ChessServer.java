package networking;
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

public class ChessServer extends Thread implements ServerCallback {
	private final int PLAYER_TOTAL = 2;
	private Game g = null;

	private ServerSocket server = null;
	private ArrayList<ServerSideConnection> playerList = new ArrayList<ServerSideConnection>();

	public static void main(String[] args) {
		new ChessServer().start();
	}

	public ChessServer() {
		g = new Game();

		//setup serversocket
		try {
			server = new ServerSocket(Connection.port);
			InetAddress ip = InetAddress.getLocalHost();
			System.out.println("Server IP address: " + ip.getHostAddress());
			System.out.println("Listening on port " + Connection.port);
		}
		catch(UnknownHostException e) {
			e.printStackTrace();
		}
		catch(IOException ex) {
			System.out.println("Error can't connect to port " + Connection.port);
			System.exit(1);
		}
	}
	@Override
	public void run() {
		//get clients, setup ClientHandlers
		while(playerList.size() < 2) {
			try {
				Socket s = server.accept();
				ServerSideConnection ssc = new ServerSideConnection(s);
				playerList.add(ssc);
				ssc.subscribe(this);
				new Thread(ssc).start();
				System.out.println("Accepted client socket");
			}
			catch(IOException ex) {
				System.out.println("Failed to accept client OR create I/O streams");
				System.exit(1);
			}
		}
		Collections.shuffle(playerList);
		for(int i = 0;i<PLAYER_TOTAL;i++) {
			ServerSideConnection ssc = playerList.get(i);
			if(i % 2 == 0) {
				ssc.send(g.pW);
			}
			else {
				ssc.send(g.pB);
			}
			ssc.send(g);
		}
	}
	
	//ServerCallback methods
	public void removeServerSideConnection(ServerSideConnection ssc) {
		playerList.remove(ssc);//TODO: announce to other players, perhaps allow another player to play
	}
	public void applyMove(Move m) {
		g.applyMove(m);//handles legal checks
		int totalLegalMoves = 0;
		for(Piece p : g.turn.pieceList) {
			totalLegalMoves += p.getLegalMoves(g, false).size();
		}
		if(totalLegalMoves == 0) {
			g.setWinner(g.getOpponent());
		}

		for(ServerSideConnection ssc : playerList) {
			ssc.send(g);
			System.out.println("Sending back");
		}
	}

}

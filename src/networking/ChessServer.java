package networking;
import gameComponent.Game;
import gameComponent.Move;

import java.util.Collections;

import piece.Piece;

public class ChessServer extends Server implements ServerCallback {
	private final int PLAYER_TOTAL = 2;
	private Game g = null;

	public static void main(String[] args) {
		new ChessServer().start();
	}

	public ChessServer() {
		super();
		g = new Game();
		setupServerSocket();
	}
	
	
	@Override
	public void run() {
		while(playerList.size() < 2) {
			acceptClient();
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
	@Override
	public void applyMove(Move m) {
		g.applyMove(m);//handles legal checks
		int totalLegalMoves = 0;
		for(Piece p : g.turn.pieceList) {
			totalLegalMoves += p.getLegalMoves(g, false).size();
		}
		if(totalLegalMoves == 0) {
			g.setWinner(g.getOpponent());
		}

		//TODO: synchronization
		for(ServerSideConnection ssc : playerList) {
			ssc.send(g);
		}
	}

}

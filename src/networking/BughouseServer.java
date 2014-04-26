package networking;
import gameComponent.Game;
import gameComponent.Move;

import java.util.ArrayList;
import java.util.Collections;

import piece.Pawn;
import piece.Piece;

public class BughouseServer extends Server implements ServerCallback {
	private final int PLAYER_TOTAL = 4;
	private ArrayList<Game> g = new ArrayList<Game>();

	public static void main(String[] args) {
		new BughouseServer().start();
	}

	public BughouseServer() {
		super();
		g.add(new Game());
		g.add(new Game());
		//Setup partners and gameCount
		for(int i = 0;i<2;i++) {
			g.get(i).pW.partner = g.get((i+1)%2).pB;
			g.get(i).pB.partner = g.get((i+1)%2).pW;
			g.get(i).pW.gameCount++;
			g.get(i).pB.gameCount++;
		}

		setupServerSocket();
	}
	@Override
	public void run() {
		while(playerList.size() < PLAYER_TOTAL) {
			acceptClient();
		}

		Collections.shuffle(playerList);
		for(int i = 0;i<PLAYER_TOTAL;i++) {
			ServerSideConnection ssc = playerList.get(i);
			Game clientGame = g.get(i/2);
			if(i % 2 == 0) {
				ssc.send(clientGame.pW);
			}
			else {
				ssc.send(clientGame.pB);
			}
			ssc.send(g.get(0));
			ssc.send(g.get(1));
		}
	}

	@Override
	public void removeServerSideConnection(ServerSideConnection ssc) {
		playerList.remove(ssc);//TODO: announce to other players: see ChessServer
	}

	@Override
	public void applyMove(Move m) {
		int gameID = m.player.gameID;
		boolean validMove = g.get(gameID).applyMove(m);//handles legal checks
		int totalLegalMoves = 0;
		for(Piece p : g.get(gameID).turn.pieceList) {
			totalLegalMoves += p.getLegalMoves(g.get(gameID), false).size();
		}
		for(Piece p : g.get(gameID).turn.heldPieces) {
			totalLegalMoves += p.getLegalPlacement(g.get(gameID)).size();
		}
		if(totalLegalMoves == 0) {
			g.get(0).setWinner(g.get(gameID).getOpponent());
			g.get(1).setWinner(g.get(gameID).getOpponent());
		}
		
		boolean sendOtherBoard = false;
		if(validMove) {
			m = new Move(m, g.get(m.player.gameID));//dereference from the Game, necessary for the server
			m.player.pickupQueue();
			m.player.opponent.pickupQueue();
			
			sendOtherBoard = !m.player.opponent.deadPieces.isEmpty();
			for(Piece p : m.player.opponent.deadPieces) {
				if(!(p instanceof Pawn) && p.getOriginalType() == Piece.PieceType.P) {
					p = new Pawn(m.player.partner);
				}
				p.loc = null;
				p.setOwner(m.player.partner);
				
				if(g.get((m.player.gameID+1)%2).turn == m.player.partner) {
					m.player.partner.queuingPieces.add(p);//Adds piece to queuingPieces if it's the partner's turn 
				}
				else {
					m.player.partner.heldPieces.add(p);//Adds piece to heldPieces if it's not partner's turn (helps partner's opponent see what to deal with)
				}
			}
			m.player.opponent.deadPieces.clear();
		}

		//TODO: synchronization
		for(ServerSideConnection ssc : playerList) {
			ssc.send(g.get(gameID));
			if(sendOtherBoard) {
				ssc.send(g.get((gameID+1)%2));
			}
		}
	}
}

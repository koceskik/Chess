package gameComponent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

import piece.Bishop;
import piece.King;
import piece.Knight;
import piece.Pawn;
import piece.Piece;
import piece.PieceColor;
import piece.Queen;
import piece.Rook;

public class Player implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public PieceColor color = null;
	public ArrayList<Piece> pieceList = new ArrayList<Piece>();//holds on-board Pieces
	public ArrayList<Piece> deadPieces = new ArrayList<Piece>();//holds dead Pieces (removed and passed to opponent's partner in bughouse) 
	public ArrayList<Piece> heldPieces = new ArrayList<Piece>();//bughouse: holds usable, placeable Pieces
	public ArrayList<Piece> queuingPieces = new ArrayList<Piece>();//bughouse, holds currently unusable, placeable Pieces (passed to heldPieces on next turn)
	public Piece king = null;
	public UUID id = UUID.randomUUID();
	public int gameID;
	public Player opponent = null;
	public Player partner = null;
	
	// This is incremented during bughouse games to denote that there's another board to the client 
	public int gameCount = 0;//technically the number of games is this value+1
							 //TODO: ISN'T THIS UNNECESSARY with the partner variable
							 //maybe not with new implementation with UI callbacks

	public Player(PieceColor pc) {
		this(pc, 0);
	}

	public Player(PieceColor pc, int gameID) {
		color = pc;
		this.gameID = gameID;
		pieceList.add(new Rook(this));
		pieceList.add(new Knight(this));
		pieceList.add(new Bishop(this));
		pieceList.add(new Queen(this));
		pieceList.add(king = new King(this));
		pieceList.add(new Bishop(this));
		pieceList.add(new Knight(this));
		pieceList.add(new Rook(this));
		for(int i = 0;i<8;i++) {
			pieceList.add(new Pawn(this));
		}
	}

	public boolean equals(Player p) {
		return id.equals(p.id);
	}
	
	public void pickupQueue() {
		for(Piece p : queuingPieces) {
			heldPieces.add(p);
		}
		queuingPieces.clear();
	}
}

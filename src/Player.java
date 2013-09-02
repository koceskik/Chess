import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class Player implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public PieceColor color = null;
	public ArrayList<Piece> pieceList = new ArrayList<Piece>();
	public ArrayList<Piece> deadPieces = new ArrayList<Piece>();//holds all dead pieces
	public ArrayList<Piece> heldPieces = new ArrayList<Piece>();//TODO: bughouse: add pieces in deadPieces, queue
	public ArrayList<Piece> queuingPieces = new ArrayList<Piece>();
	public Piece king = null;
	public UUID id = UUID.randomUUID();
	public int gameID;
	public Player opponent = null;
	public Player partner = null;
	
	// This is incremented during bughouse games to denote that there's another board to the client 
	public int gameCount = 0;//technically the number of games is this value+1. ISN'T THIS UNNECESSARY with the partner variable

	public Player(PieceColor pc) {
		this(pc, 0);
	}

	public Player(PieceColor pc, int gameID) {//TODO: add security here: unique ID or have server send unique ID each turn
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
		//TODO: does this need a lock?
		for(Piece p : queuingPieces) {
			heldPieces.add(p);
		}
		queuingPieces.clear();
	}
}

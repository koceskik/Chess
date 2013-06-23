import java.io.Serializable;
import java.util.ArrayList;

public class Player implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public PieceColor playerColor = null;
	public ArrayList<Piece> pieceList = new ArrayList<Piece>();
	public ArrayList<Piece> deadPieces = new ArrayList<Piece>();//holds all dead pieces
	public ArrayList<Piece> heldPieces = new ArrayList<Piece>();//TODO: bughouse: add pieces in deadPieces, queue 
	public Piece king = null;
	
	public Player(PieceColor pc) {//TODO: add security here: unique ID or have server send unique ID each turn
		playerColor = pc;
		king = new King(this);
		pieceList.add(new Rook(this));
		pieceList.add(new Knight(this));
		pieceList.add(new Bishop(this));
		pieceList.add(new Queen(this));
		pieceList.add(king);
		pieceList.add(new Bishop(this));
		pieceList.add(new Knight(this));
		pieceList.add(new Rook(this));
		for(int i = 0;i<8;i++) {
			pieceList.add(new Pawn(this));
		}
	}
	
	public boolean equals(Player p) {
		if(playerColor != p.playerColor) return false;		
		return true;
	}
}

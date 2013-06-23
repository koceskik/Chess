import java.io.Serializable;

public class Piece implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private PieceColor color;
	private PieceType type;
	private PieceType originalType;//necessary for pawns (bughouse)

	public Piece(PieceType t) {
		type = t;
		originalType = t;
	}
	public Piece(Piece p, PieceColor c) {
		type = p.getType();
		originalType = p.getType();
		color = c;
	}
	public Piece(PieceType pt, PieceColor c) {
		type = pt;
		originalType = pt;
		color = c;
	}
	
	public PieceColor getColor() {
		return color;
	}
	public PieceType getType() {
		return type;
	}
	public PieceType getOriginalType() {
		return originalType;
	}

}

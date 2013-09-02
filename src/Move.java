import java.io.Serializable;

public class Move implements Serializable {
	private static final long serialVersionUID = 1L;
	public enum MoveType {
		NORMAL, EN_PASSANT, CASTLE, PROMOTE_QUEEN, PROMOTE_KNIGHT, PROMOTE_ROOK, PROMOTE_BISHOP, PLACEMENT;
		public static MoveType getPromotionType(int promotionIndex) {
			if(promotionIndex == 0) return PROMOTE_QUEEN;
			else if(promotionIndex == 1) return PROMOTE_KNIGHT;
			else if(promotionIndex == 2) return PROMOTE_ROOK;
			else if(promotionIndex == 3) return PROMOTE_BISHOP;
			else return null;
		}
	}

	public Piece piece;
	public Tile toTile;
	//public int moveType = NORMAL;
	public MoveType moveType = MoveType.NORMAL;
	public Player player = null;
	public Move(Piece piece, Tile toTile) {
		this.piece = piece;
		this.toTile = toTile;
	}
	public Move(Piece piece, Tile toTile, Player p) {
		this.piece = piece;
		this.toTile = toTile;
		this.player = p;
	}
	public boolean equals(Move m) {
		return piece.equals(m.piece) && toTile.equals(m.toTile);//TODO: will this need to be changed to account for movingPiece
	}
}

import java.io.Serializable;


public class Move implements Serializable {
	private static final long serialVersionUID = 1L;
	public static int NORMAL = 0;
	public static int EN_PASSANT = 1;
	public static int CASTLE = 2;
	
	public Tile fromTile;
	public Tile toTile;
	public int moveType;
	public Move(Tile fromTile, Tile toTile) {
		this.fromTile = fromTile;
		this.toTile = toTile;
		this.moveType = NORMAL;
	}
	public Move(Tile fromTile, Tile toTile, int specialMoveType) {
		this.fromTile = fromTile;
		this.toTile = toTile;
		this.moveType = specialMoveType;
	}
	
	public boolean equals(Move m) {
		return fromTile.equals(m.fromTile) && toTile.equals(m.toTile) && moveType == m.moveType;
	}

}

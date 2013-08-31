import java.io.Serializable;

public class Move implements Serializable {
	private static final long serialVersionUID = 1L;
	public static int NORMAL = 0;//TODO: enum? Maybe not since we do math on it
	public static int EN_PASSANT = 1;
	public static int CASTLE = 2;
	public static int PROMOTE_QUEEN = 3;
	public static int PROMOTE_KNIGHT = 4;
	public static int PROMOTE_ROOK = 5;
	public static int PROMOTE_BISHOP = 6;

	public Tile fromTile;
	public Tile toTile;
	public int moveType;
	public Player player = null;
	public Move(Tile fromTile, Tile toTile) {
		this.fromTile = fromTile;
		this.toTile = toTile;
		this.moveType = NORMAL;
	}
	public Move(Tile fromTile, Tile toTile, Player p) {
		this.fromTile = fromTile;
		this.toTile = toTile;
		this.moveType = NORMAL;
		this.player = p;
	}
	public boolean equals(Move m) {
		return fromTile.equals(m.fromTile) && toTile.equals(m.toTile);
	}
}

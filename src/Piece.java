import java.io.Serializable;
import java.util.ArrayList;

public abstract class Piece implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Player owner;
	protected Piece originalType = null;//necessary for pawns (bughouse)
	protected Tile loc = null;
	protected int numOfMovesMade = 0;
	protected int lastTurnMoved = -1;

	public Piece() {}
	public Piece(Player owner) {
		this.owner = owner;
	}
	public PieceColor getColor() {
		return owner.color;
	}
	public Piece getOriginalType() {
		return originalType;
	}
	public Player getOwner() {
		return owner;
	}
	public int getX() {
		return loc.x;
	}
	public int getY() {
		return loc.y;
	}
	public abstract String getTileCode();
	
	public abstract ArrayList<Move> getLegalMoves(Game g, boolean ignoreCheck);
}

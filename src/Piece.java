import java.io.Serializable;
import java.util.ArrayList;

public abstract class Piece implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Player owner;//TODO: this may need to be public and changed when the piece is taken and passed (bughouse)
	protected Piece originalType = null;//necessary for pawns (bughouse)
	protected Tile loc = null;
	protected int numOfMovesMade = 0;//necessary for en passant only if the pawn moved 2 spaces in first turn
									 //also used for King castling for efficiency event though lastTurnMoved could be used
	protected int lastTurnMoved = -1;//necessary for en passant

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

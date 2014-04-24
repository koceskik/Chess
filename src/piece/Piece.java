package piece;
import gameComponent.Game;
import gameComponent.Move;
import gameComponent.Player;
import gameComponent.Tile;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class Piece implements Serializable {
	private static final long serialVersionUID = 1L;
	public enum PieceType {
		P,N,B,R,Q,K;
	}

	private Player owner;
	public Player getOwner() {
		return owner;
	}
	public PieceType pieceType;
	protected PieceType originalType;//necessary for pawns (bughouse)
	public PieceType getOriginalType() {
		return originalType;
	}
	public Tile loc = null;
	protected int numOfMovesMade = 0;//necessary for en passant: pawn moved 2 spaces in first turn
									 //also used for King castling for efficiency event though lastTurnMoved could be used
	public void incNumOfMovesMade() {
		numOfMovesMade++;
	}
	protected int lastTurnMoved = -1;//necessary for en passant
	public void setLastTurnMoved(int turnCount) {
		lastTurnMoved = turnCount;
	}

	public Piece(Player owner) {
		this.owner = owner;
	}
	public Piece(Player owner, PieceType originalType) {
		this(owner);
		this.originalType = originalType;
	}
	
	
	public PieceColor getColor() {
		return owner.color;
	}
	public void setOwner(Player owner) {
		this.owner = owner; 
	}
	
	public int getX() {
		return loc.x;
	}
	public int getY() {
		return loc.y;
	}
	
	public abstract ArrayList<Move> getLegalMoves(Game g, boolean ignoreCheck);
	
	public ArrayList<Move> getLegalPlacement(Game g) {
		ArrayList<Move> moveList = new ArrayList<Move>();
		for(int x = 0;x<8;x++) {
			for(int y = 0;y<8;y++) {
				if(this instanceof Pawn) {
					if(y == 0 || y == 7) continue;
				}
				if(g.board[y][x].getPiece() == null) {
					Move m = new Move(this, g.board[y][x]);
					m.moveType = Move.MoveType.PLACEMENT;
					if(!g.leavesPlayerInCheck(m)) {
						moveList.add(m);
					}
				}
			}
		}
		return moveList;
	}
	
	public String getTileCode() {
		return pieceType.toString();
	}
}

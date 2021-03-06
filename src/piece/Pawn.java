package piece;
import gameComponent.Game;
import gameComponent.Move;
import gameComponent.Player;

import java.util.ArrayList;

public class Pawn extends Piece {
	private static final long serialVersionUID = 1L;
	
	public Pawn(Player owner) {
		this(owner, PieceType.P);
	}
	public Pawn(Player owner, PieceType originalType) {
		super(owner, originalType);
		pieceType = PieceType.P;
	}

	@Override
	public ArrayList<Move> getLegalMoves(Game g, boolean ignoreCheck) {
		ArrayList<Move> moveList = new ArrayList<Move>();
		int dirOfMovement;
		int specialPos;//location of double move
		if(this.getOwner().color == PieceColor.W) {
			dirOfMovement = -1;
			specialPos = 6;
		}
		else {
			dirOfMovement = 1;
			specialPos = 1;
		}
		int x = getX();
		int y = getY();

		if(g.board[y+dirOfMovement][x].getPiece() == null) {
			Move m = new Move(this, g.board[y+dirOfMovement][x]);//regular forward move
			if(ignoreCheck) {
				moveList.add(m);
			}
			else if(!g.leavesPlayerInCheck(m)) {
				moveList.add(m);					
			}
			if(y == specialPos && g.board[y+2*dirOfMovement][x].getPiece() == null) {
				m = new Move(this, g.board[y+2*dirOfMovement][x]);//initial double move
				if(ignoreCheck) {
					moveList.add(m);
				}
				else if(!g.leavesPlayerInCheck(m)) {
					moveList.add(m);					
				}
			}
		}
		if(x >= 1) {
			if(g.board[y+dirOfMovement][x-1].getPiece() != null) {
				if(g.board[y+dirOfMovement][x-1].getPiece().getOwner() != this.getOwner()) {
					Move m = new Move(this, g.board[y+dirOfMovement][x-1]);//attack left
					if(ignoreCheck) {
						moveList.add(m);
					}
					else if(!g.leavesPlayerInCheck(m)) {
						moveList.add(m);					
					}
				}
			}
			if(g.board[y][x-1].getPiece() != null) {
				Piece p = g.board[y][x-1].getPiece();
				if(p.getOwner() != this.getOwner()) {
					if(p instanceof Pawn) {
						if(p.lastTurnMoved == g.turnCount-1 && p.numOfMovesMade == 1) {
							Move m = new Move(this, g.board[y+dirOfMovement][x-1]);//left en passant
							if(ignoreCheck) {
								moveList.add(m);
							}
							else if(!g.leavesPlayerInCheck(m)) {
								moveList.add(m);					
							}
						}
					}
				}
			}
		}
		if(x <= 6) {
			if(g.board[y+dirOfMovement][x+1].getPiece() != null) {
				if(g.board[y+dirOfMovement][x+1].getPiece().getOwner() != this.getOwner()) {
					Move m = new Move(this, g.board[y+dirOfMovement][x+1]);//attack right
					if(ignoreCheck) {
						moveList.add(m);
					}
					else if(!g.leavesPlayerInCheck(m)) {
						moveList.add(m);					
					}
				}
			}
			if(g.board[y][x+1].getPiece() != null) {
				Piece p = g.board[y][x+1].getPiece();
				if(p.getOwner() != this.getOwner()) {
					if(p instanceof Pawn) {
						if(p.lastTurnMoved == g.turnCount-1 && p.numOfMovesMade == 1) {
							Move m = new Move(this, g.board[y+dirOfMovement][x+1]);//right en passant
							if(ignoreCheck) {
								moveList.add(m);
							}
							else if(!g.leavesPlayerInCheck(m)) {
								moveList.add(m);					
							}
						}
					}
				}
			}
		}
		return moveList;
	}
}

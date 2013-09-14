import java.util.ArrayList;

public class King extends Piece {
	private static final long serialVersionUID = 1L;

	public King(Player owner) {
		super(owner);
		pieceType = PieceType.K;
		originalType = PieceType.K;
	}

	@Override
	public ArrayList<Move> getLegalMoves(Game g, boolean ignoreCheck) {
		ArrayList<Move> moveList = new ArrayList<Move>();
		int x = getX();
		int y = getY();
		int[] tempX = {0,1,1,1,0,-1,-1,-1};
		int[] tempY = {1,1,0,-1,-1,-1,0,1};
		boolean rightMoveFine = false;//used for castling on right side (moving right isn't in check)
		boolean leftMoveFine = false;//used for castling on left side (moving left isn't in check)
		for(int i = 0;i<tempX.length;i++) {
			if(Game.isPointOnBoard(x+tempX[i], y+tempY[i])) {
				if(g.board[y+tempY[i]][x+tempX[i]].getPiece() == null || g.board[y+tempY[i]][x+tempX[i]].getPiece().getOwner() != this.getOwner()) {//note: if statement optimization
					Move m = new Move(this, g.board[y+tempY[i]][x+tempX[i]]);
					if(ignoreCheck) {
						moveList.add(m);
						if(i == 2) rightMoveFine = true;
						else if(i == 6) leftMoveFine = true;
					}
					else if(!g.leavesPlayerInCheck(m)) {
						moveList.add(m);
						if(i == 2) rightMoveFine = true;
						else if(i == 6) leftMoveFine = true;
					}
				}
			}
		}
		if(numOfMovesMade == 0 && !ignoreCheck && !g.inCheck()) {
			if(g.board[y][x+1].getPiece() == null && g.board[y][x+2].getPiece() == null && rightMoveFine) {
				if(g.board[y][x+3].getPiece() != null && g.board[y][x+3].getPiece() instanceof Rook && g.board[y][x+3].getPiece().numOfMovesMade == 0) {
					Move m = new Move(this, g.board[y][x+2]);
					if(ignoreCheck) {
						moveList.add(m);
					}
					else if(!g.leavesPlayerInCheck(m)) {
						moveList.add(m);
					}
				}
			}

			if(g.board[y][x-1].getPiece() == null && g.board[y][x-2].getPiece() == null && g.board[y][x-3].getPiece() == null && leftMoveFine) {
				if(g.board[y][x-4].getPiece() != null && g.board[y][x-4].getPiece() instanceof Rook && g.board[y][x-4].getPiece().numOfMovesMade == 0) {
					Move m = new Move(this, g.board[y][x-2]);
					if(ignoreCheck) {
						moveList.add(m);
					}
					else if(!g.leavesPlayerInCheck(m)) {
						moveList.add(m);
					}
				}
			}
		}
		return moveList;
	}
}

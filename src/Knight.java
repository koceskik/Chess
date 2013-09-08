import java.util.ArrayList;

public class Knight extends Piece {
	private static final long serialVersionUID = 1L;

	public Knight(Player owner) {
		super(owner);
		originalType = PieceType.N;
	}

	@Override
	public ArrayList<Move> getLegalMoves(Game g, boolean ignoreCheck) {
		ArrayList<Move> moveList = new ArrayList<Move>();
		int x = getX();
		int y = getY();
		int[] tempX = {1,2,2,1,-1,-2,-2,-1};
		int[] tempY = {2,1,-1,-2,-2,-1,1,2};
		for(int i = 0;i<tempX.length;i++) {
			if(Game.isPointOnBoard(x+tempX[i], y+tempY[i])) {
				if(g.board[y+tempY[i]][x+tempX[i]].getPiece() == null || g.board[y+tempY[i]][x+tempX[i]].getPiece().getOwner() != this.getOwner()) {//note: if statement optimization
					Move m = new Move(this, g.board[y+tempY[i]][x+tempX[i]]);
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
	@Override
	public String getTileCode() {
		return "N";
	}

}

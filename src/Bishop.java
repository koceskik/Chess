import java.util.ArrayList;

public class Bishop extends Piece {
	private static final long serialVersionUID = 1L;
	
	public Bishop(Player owner) {
		super(owner);
		pieceType = PieceType.B;
		originalType = PieceType.B;
	}

	@Override
	public ArrayList<Move> getLegalMoves(Game g, boolean ignoreCheck) {
		ArrayList<Move> moveList = new ArrayList<Move>();
		int x = getX();
		int y = getY();
		int[] dirX = {-1,1,1,-1};
		int[] dirY = {1,1,-1,-1};
		for(int i = 0;i<dirX.length;i++) {
			int tempX = x+dirX[i];
			int tempY = y+dirY[i];
			while(Game.isPointOnBoard(tempX, tempY)) {
				if(g.board[tempY][tempX].getPiece() == null || g.board[tempY][tempX].getPiece().getOwner() != this.getOwner()) {//note: if statement optimization
					Move m = new Move(this, g.board[tempY][tempX]);
					if(ignoreCheck) {
						moveList.add(m);
					}
					else if(!g.leavesPlayerInCheck(m)) {
						moveList.add(m);
					}
				}
				if(g.board[tempY][tempX].getPiece() != null) {
					break;
				}
				tempX += dirX[i];
				tempY += dirY[i];
			}
		}
		
		return moveList;
	}
}

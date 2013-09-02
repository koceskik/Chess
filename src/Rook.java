import java.util.ArrayList;

public class Rook extends Piece {
	private static final long serialVersionUID = 1L;
	
	public Rook() {}
	public Rook(Player owner) {
		super(owner);
		originalType = new Rook();
	}

	@Override
	public ArrayList<Move> getLegalMoves(Game g, boolean ignoreCheck) {
		ArrayList<Move> moveList = new ArrayList<Move>();
		int x = getX();
		int y = getY();
		int[] dirX = {0,1,0,-1};
		int[] dirY = {1,0,-1,0};
		for(int i = 0;i<dirX.length;i++) {
			int tempX = x+dirX[i];
			int tempY = y+dirY[i];
			while(tempX >= 0 && tempX < 8 && tempY >= 0 && tempY < 8) {
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
	@Override
	public String getTileCode() {
		return "R"; 
	}

}

import java.io.Serializable;
import java.util.ArrayList;

public class Game implements Serializable {
	private static final long serialVersionUID = 1L;

	public Tile[][] board = new Tile[8][8];
	public PieceColor turn = PieceColor.W;
	
	public Player pW = new Player(PieceColor.W);
	public Player pB = new Player(PieceColor.B);

	public Game() {
		PieceType[] backRowOrder = {PieceType.R, PieceType.N, PieceType.B, PieceType.Q, PieceType.K, PieceType.B, PieceType.N, PieceType.R}; 
		PieceColor c = PieceColor.B;
		for(int i = 0;i<8;i++) {
			if(i > 2) c = PieceColor.W;
			for(int j = 0;j<8;j++) {
				board[i][j] = new Tile(j,i);
				if(i == 0 || i == 7) {
					board[i][j].addPiece(new Piece(backRowOrder[j], c));
				}
				else if(i == 1 || i == 6) {
					board[i][j].addPiece(new Piece(PieceType.P, c));
				}
			}
		}
	}

	public String toString() {
		String returner = "";
		for(int i = 0;i<8;i++) {
			for(int j = 0;j<8;j++) {
				if(board[i][j] == null) returner += " ";
				else returner += board[i][j].toString();
			}
			returner +="\n";
		}
		return returner;
	}

	@SuppressWarnings("unused")
	private boolean inCheck() {//TODO: add actual logic here
		return false;
	}

	public ArrayList<Move> getLegalMove(Tile t) {//returns a list of all legal moves, or empty if no legal moves
		ArrayList<Move> moveList = new ArrayList<Move>();
		if(t.getPiece() == null) return moveList;
		if(t.getPiece().getColor() != turn) return moveList;
		int x = t.getX();
		int y = t.getY();

		if(t.getPiece().getType() == PieceType.P) {
			int dirOfMovement;
			int specialPos;//location of double move
			if(turn == PieceColor.W) {
				dirOfMovement = -1;
				specialPos = 6;
			}
			else {
				dirOfMovement = 1;
				specialPos = 1;
			}
			if(y+dirOfMovement >= 0 && y+dirOfMovement < 8) {
				if(board[y+dirOfMovement][x].getPiece() == null) {
					moveList.add(new Move(x,y,x,y+dirOfMovement));
					if(y == specialPos && board[y+2*dirOfMovement][x].getPiece() == null) {
						moveList.add(new Move(x,y,x,y+2*dirOfMovement));
					}
				}
				if(x >= 1) {
					if(board[y+dirOfMovement][x-1].getPiece() != null) {
						if(board[y+dirOfMovement][x-1].getPiece().getColor() == turn.getOpponent()) {
							moveList.add(new Move(x,y,x-1,y+dirOfMovement));
						}
					}
				}
				if(x <= 6) {
					if(board[y+dirOfMovement][x+1].getPiece() != null) {
						if(board[y+dirOfMovement][x+1].getPiece().getColor() == turn.getOpponent()) {
							moveList.add(new Move(x,y,x+1,y+dirOfMovement));
						}
					}
				}
			}
		}


		return moveList;
	}
}

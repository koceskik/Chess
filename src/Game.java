import java.io.Serializable;
import java.util.ArrayList;

public class Game implements Serializable {
	private static final long serialVersionUID = 1L;

	public Player pW = new Player(PieceColor.W);
	public Player pB = new Player(PieceColor.B);

	public Tile[][] board = new Tile[8][8];
	public Player turn = pW;
	public int turnCount = 0;

	public Game() {
		Player p = pB;
		for(int i = 0;i<8;i++) {
			if(i > 2) p = pW;
			for(int j = 0;j<8;j++) {
				board[i][j] = new Tile(j,i);
				if(i == 0 || i == 7) {
					board[i][j].addPiece(p.pieceList.get(j));
				}
				else if(i == 1 || i == 6) {
					board[i][j].addPiece(p.pieceList.get(8+j));
				}
			}
		}
	}
	public String toReadableString() {
		String returner = "";
		for(int i = 0;i<8;i++) {
			for(int j = 0;j<8;j++) {
				if(board[i][j].getPiece() == null) returner += " ";
				else returner += board[i][j].getPiece().getTileCode();
			}
			returner +="\n";
		}
		return returner;
	}
	public Player getOpponent() {
		if(turn == pW) return pB;
		else return pW;
	}

	public boolean applyMove(Move m) {//returns true if applied
		m.fromTile = board[m.fromTile.y][m.fromTile.x];//dereference from the Game, necessary for server 
		m.toTile = board[m.toTile.y][m.toTile.x];
		if(m.fromTile.getPiece() instanceof Pawn && board[m.toTile.y][m.toTile.x].getPiece() == null) {
			if(m.fromTile.x != m.toTile.x) m.moveType = Move.EN_PASSANT;
		}
		if(m.fromTile.getPiece() instanceof King && Math.abs(m.toTile.x-m.fromTile.x) == 2) {
			m.moveType = Move.CASTLE;
		}
		ArrayList<Move> legalMoveList = getLegalMove(m.fromTile);
		boolean mInLegalMoveList = false;
		for(Move lm : legalMoveList) {//legalMoveList.contains(m) doesn't seem to work
			if(lm.equals(m)) {
				mInLegalMoveList = true;
			}
		}
		if(mInLegalMoveList) {
			if(m.moveType == Move.EN_PASSANT) {
				Piece enPassantPawn = board[m.fromTile.y][m.toTile.x].getPiece();
				getOpponent().pieceList.remove(enPassantPawn);
				getOpponent().deadPieces.add(enPassantPawn);
				board[m.fromTile.y][m.toTile.x].addPiece(null);
			}
			if(m.moveType == Move.CASTLE) {
				if(m.toTile.x - m.fromTile.x > 0) {
					board[m.toTile.y][m.toTile.x-1].addPiece(board[m.toTile.y][m.toTile.x+1].getPiece());
					board[m.toTile.y][m.toTile.x+1].addPiece(null);
				}
				else {
					board[m.toTile.y][m.toTile.x+1].addPiece(board[m.toTile.y][m.toTile.x-2].getPiece());
					board[m.toTile.y][m.toTile.x-2].addPiece(null);
				}
			}
			m.toTile.addPiece(m.fromTile.getPiece());
			m.fromTile.addPiece(null);

			m.toTile.getPiece().lastTurnMoved = turnCount;
			m.toTile.getPiece().numOfMovesMade++;
			turnCount++;
			turn = getOpponent();
			return true;
		}
		return false;
	}
	public boolean leavesPlayerInCheck(Move m) {//returns true if move leaves the user in check
		m.fromTile = board[m.fromTile.y][m.fromTile.x];//dereference from the Game, necessary for server
		m.toTile = board[m.toTile.y][m.toTile.x];
		Piece fromTilePiece = m.fromTile.getPiece();
		Piece toTilePiece = m.toTile.getPiece();
		Piece enPassantPawn = board[m.fromTile.y][m.toTile.x].getPiece();

		//if(toTilePiece != null) {
		//	getOpponent().pieceList.remove(toTilePiece);
		//}
		if(m.moveType == Move.EN_PASSANT) {
			getOpponent().pieceList.remove(enPassantPawn);
			getOpponent().deadPieces.add(enPassantPawn);
		}

		board[m.toTile.y][m.toTile.x].addPiece(fromTilePiece);
		board[m.fromTile.y][m.fromTile.x].addPiece(null);

		boolean inCheck = inCheck();

		board[m.toTile.y][m.toTile.x].addPiece(toTilePiece);
		board[m.fromTile.y][m.fromTile.x].addPiece(fromTilePiece);

		if(m.moveType == Move.EN_PASSANT) {
			getOpponent().pieceList.add(enPassantPawn);
			getOpponent().deadPieces.remove(enPassantPawn);
		}
		if(toTilePiece != null) {
			getOpponent().pieceList.add(toTilePiece);
			getOpponent().deadPieces.remove(toTilePiece);
		}

		return inCheck;
	}

	private boolean inCheck() {
		Player currentPlayer = turn;
		Player opp = this.getOpponent();

		turn = opp;//needed for testing other player
		boolean inCheck = false;
		for(Piece p : opp.pieceList) {
			for(Move m : p.getLegalMoves(this, true)) {
				if(m.toTile == currentPlayer.king.loc) {
					inCheck = true;
				}
			}
		}
		turn = currentPlayer;
		return inCheck;
	}

	public ArrayList<Move> getLegalMove(Tile t) {//returns a list of all legal moves, or empty if no legal moves
		t = board[t.y][t.x];//dereference from the Game, necessary for server
		ArrayList<Move> moveList = new ArrayList<Move>();
		if(t.getPiece() == null) return moveList;
		if(t.getPiece().getOwner() != turn) return moveList;

		return t.getPiece().getLegalMoves(this, false);
	}
}

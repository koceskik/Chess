import java.io.Serializable;
import java.util.ArrayList;

public class Game implements Serializable {
	private static final long serialVersionUID = 1L;
	public static int count = 0;//Note: this is only "seen" on the computer hosting the server. Other clients don't get Game's static variables

	public Player pW = null;
	public Player pB = null;

	public Tile[][] board = new Tile[8][8];
	public Player turn = null;
	public int turnCount = 0;
	public int id;
	private Player winner = null;
	public void setWinner(Player p) {
		winner = p;
	}
	public void setWinner(PieceColor pc) {
		winner = getPlayer(pc);
	}
	public Player getWinner() {
		return winner;
	}

	public Game() {
		this.id = count++;
		turn = pW = new Player(PieceColor.W, id);
		pB = new Player(PieceColor.B, id);
		Player p = pB;
		for(int i = 0;i<8;i++) {
			if(i == 4) p = pW;
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
	public Player getPlayer(PieceColor pc) {
		Player player;
		if(pc == PieceColor.W) {
			player = pW;
		}
		else {
			player = pB;
		}
		return player;
	}
	
	public boolean isLegalMove(Move m) {
		if(!m.player.equals(turn)) return false;
		
		m.piece = board[m.piece.getY()][m.piece.getX()].getPiece();//dereference from the Game, necessary for server
		m.toTile = board[m.toTile.y][m.toTile.x];
		if(m.piece instanceof Pawn && m.toTile.getPiece() == null) {
			if(m.piece.getX() != m.toTile.x) m.moveType = Move.MoveType.EN_PASSANT;
		}
		else if(m.piece instanceof King && Math.abs(m.toTile.x-m.piece.getX()) == 2) {
			m.moveType = Move.MoveType.CASTLE;
		}
		ArrayList<Move> legalMoveList = m.piece.getLegalMoves(this, false);
		boolean mInLegalMoveList = false;
		for(Move lm : legalMoveList) {//legalMoveList.contains(m) doesn't seem to work
			if(lm.equals(m)) {
				mInLegalMoveList = true;
			}
		}
		return mInLegalMoveList;
	}

	public boolean applyMove(Move m) {//returns true if applied
		if(!m.player.equals(turn)) return false;

		m.piece = board[m.piece.getY()][m.piece.getX()].getPiece();//dereference from the Game, necessary for server
		m.toTile = board[m.toTile.y][m.toTile.x];
		if(m.piece instanceof Pawn && m.toTile.getPiece() == null) {
			if(m.piece.getX() != m.toTile.x) m.moveType = Move.MoveType.EN_PASSANT;
		}
		else if(m.piece instanceof King && Math.abs(m.toTile.x-m.piece.getX()) == 2) {
			m.moveType = Move.MoveType.CASTLE;
		}
		ArrayList<Move> legalMoveList = m.piece.getLegalMoves(this, false);
		boolean mInLegalMoveList = false;
		for(Move lm : legalMoveList) {//legalMoveList.contains(m) doesn't seem to work
			if(lm.equals(m)) {
				mInLegalMoveList = true;
			}
		}
		if(mInLegalMoveList) {
			if(m.moveType == Move.MoveType.EN_PASSANT) {
				Piece enPassantPawn = board[m.piece.getY()][m.toTile.x].getPiece();
				getOpponent().pieceList.remove(enPassantPawn);
				getOpponent().deadPieces.add(enPassantPawn);
				board[m.piece.getY()][m.toTile.x].addPiece(null);
			}
			if(m.moveType == Move.MoveType.CASTLE) {
				if(m.toTile.x - m.piece.getX() > 0) {
					board[m.toTile.y][m.toTile.x-1].addPiece(board[m.toTile.y][m.toTile.x+1].getPiece());
					board[m.toTile.y][m.toTile.x+1].addPiece(null);
				}
				else {
					board[m.toTile.y][m.toTile.x+1].addPiece(board[m.toTile.y][m.toTile.x-2].getPiece());
					board[m.toTile.y][m.toTile.x-2].addPiece(null);
				}
			}
			getOpponent().pieceList.remove(m.toTile.getPiece());
			if(m.toTile.getPiece() != null) {
				getOpponent().deadPieces.add(m.toTile.getPiece());
			}

			m.piece.loc.addPiece(null);//order matters
			m.toTile.addPiece(m.piece);

			if(m.toTile.getPiece() instanceof Pawn) {
				Piece promotion = null;
				if(m.moveType == Move.MoveType.PROMOTE_QUEEN) {
					promotion = new Queen(turn);
				}
				else if(m.moveType == Move.MoveType.PROMOTE_KNIGHT) {
					promotion = new Knight(turn);
				}
				else if(m.moveType == Move.MoveType.PROMOTE_ROOK) {
					promotion = new Rook(turn);
				}
				else if(m.moveType == Move.MoveType.PROMOTE_BISHOP) {
					promotion = new Bishop(turn);
				}
				if(promotion != null) {
					promotion.originalType = new Pawn();
					turn.pieceList.remove(m.toTile.getPiece());
					turn.pieceList.add(promotion);
					m.toTile.addPiece(promotion);
				}
			}

			m.toTile.getPiece().lastTurnMoved = turnCount;
			m.toTile.getPiece().numOfMovesMade++;
			turnCount++;
			turn = getOpponent();
			return true;
		}
		return false;
	}
	public boolean leavesPlayerInCheck(Move m) {//returns true if move leaves the user in check
		m.piece = board[m.piece.getY()][m.piece.getX()].getPiece();//dereference from the Game, necessary for server
		m.toTile = board[m.toTile.y][m.toTile.x];
		Piece toTilePiece = m.toTile.getPiece();
		Piece enPassantPawn = board[m.piece.getY()][m.toTile.x].getPiece();
		int fromX = m.piece.getX();//necessary because after moving the fromPiece, there's no other reference to its original location
		int fromY = m.piece.getY();

		if(toTilePiece != null) {
			getOpponent().pieceList.remove(toTilePiece);
			getOpponent().deadPieces.add(toTilePiece);
		}
		if(m.moveType == Move.MoveType.EN_PASSANT) {
			getOpponent().pieceList.remove(enPassantPawn);
			getOpponent().deadPieces.add(enPassantPawn);
		}

		m.piece.loc.addPiece(null);//order matters
		m.toTile.addPiece(m.piece);

		boolean inCheck = inCheck();

		m.toTile.addPiece(toTilePiece);
		board[fromY][fromX].addPiece(m.piece);

		if(m.moveType == Move.MoveType.EN_PASSANT) {
			getOpponent().pieceList.add(enPassantPawn);
			getOpponent().deadPieces.remove(enPassantPawn);
		}
		if(toTilePiece != null) {
			getOpponent().pieceList.add(toTilePiece);
			getOpponent().deadPieces.remove(toTilePiece);
		}

		return inCheck;
	}

	public boolean inCheck() {
		Player currentPlayer = turn;
		Player opp = this.getOpponent();

		turn = opp;//needed for testing other player; yes its necessary (confirmed)
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
		if(t.getPiece() == null || t.getPiece().getOwner() != turn) return new ArrayList<Move>();

		return t.getPiece().getLegalMoves(this, false);
	}
}

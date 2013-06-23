import java.io.Serializable;
import java.util.HashMap;

import javax.swing.ImageIcon;


public class Tile  implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static HashMap<String,ImageIcon> images = new HashMap<String,ImageIcon>();
	static {
		char[] color = {'B','W'};
		char[] piece = {'P','N','B','R','Q','K'};
		for(char c1:color) {
			for(char c2:color) {
				for(char p:piece) {
					images.put("" + c1+c2+p, new ImageIcon("res/" + c1+c2+p + ".png"));
				}
			}
		}
		images.put("B", new ImageIcon("res/B.png"));
		images.put("W", new ImageIcon("res/W.png"));
	}
	public final int x;
	public final int y;
	private Piece piece = null;
	
	public Tile(int x, int y) {
		this.x = x;
		this.y = y;
	}
	public Tile(Tile t) {
		x = t.x;
		y = t.y;
		if(t.piece == null) piece = null;
		else if(t.piece instanceof Pawn) piece = new Pawn(t.piece.getOwner());
		else if(t.piece instanceof Rook) piece = new Rook(t.piece.getOwner());
		else if(t.piece instanceof Knight) piece = new Knight(t.piece.getOwner());
		else if(t.piece instanceof Bishop) piece = new Bishop(t.piece.getOwner());
		else if(t.piece instanceof Queen) piece = new Queen(t.piece.getOwner());
		else if(t.piece instanceof King) piece = new King(t.piece.getOwner());
	}
	public PieceColor getColor() {
		if((x+y)%2 == 0) {
			return PieceColor.W;
		}
		else {
			return PieceColor.B;
		}
	}
	public void addPiece(Piece p) {
		if(piece != null && p != null) {//current piece is destroyed (by another piece), doesn't handle EN_PASSANT manually
			piece.getOwner().pieceList.remove(piece);
			piece.getOwner().deadPieces.add(piece);
		}
		piece = p;
		if(piece != null) {
			piece.loc = this;
		}
	}
	public Piece getPiece() {
		return piece;
	}
	public ImageIcon getIcon() {
		if(piece == null) {
			return images.get(getColor().toString());
		}
		else {
			return images.get(""+getColor() + piece.getColor() + piece.getTileCode());
		}
	}

}

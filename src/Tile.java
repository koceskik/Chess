import java.awt.Image;
import java.awt.Toolkit;
import java.io.Serializable;
import java.util.HashMap;

import javax.swing.ImageIcon;

public class Tile implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private static final int scaleStyle = Image.SCALE_SMOOTH;
	public static final int size = 32;
	public static final int scaledSize = 16;
	private static HashMap<String,ImageIcon> images = new HashMap<String,ImageIcon>();
	static {
		char[] color = {'B','W'};
		char[] piece = {'P','N','B','R','Q','K'};
		for(char c1:color) {
			for(char c2:color) {
				for(char p:piece) {
					Image i = Toolkit.getDefaultToolkit().getImage("res/" + c1+c2+p + ".png");
					images.put("" + c1+c2+p, new ImageIcon(i));
					images.put("" + c1+c2+p+"s", new ImageIcon(i.getScaledInstance(scaledSize, scaledSize, scaleStyle)));
				}
			}
		}
		Image b = Toolkit.getDefaultToolkit().getImage("res/B.png");
		Image w = Toolkit.getDefaultToolkit().getImage("res/W.png");
		images.put("B", new ImageIcon(b));
		images.put("W", new ImageIcon(w));
		images.put("Bs", new ImageIcon(b.getScaledInstance(scaledSize, scaledSize, scaleStyle)));
		images.put("Ws", new ImageIcon(w.getScaledInstance(scaledSize, scaledSize, scaleStyle)));
	}
	public final int x;
	public final int y;
	private Piece piece = null;
	private final PieceColor tileColor;
	
	public Tile(int x, int y) {
		this.x = x;
		this.y = y;
		if((x+y)%2 == 0) {
			tileColor = PieceColor.W;
		}
		else {
			tileColor =  PieceColor.B;
		}
	}
	public void addPiece(Piece p) {
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
			return images.get(tileColor.toString());
		}
		else {
			return images.get(""+tileColor + piece.getColor() + piece.getTileCode());
		}
	}
	public ImageIcon getSmallIcon() {
		if(piece == null) {
			return images.get(tileColor.toString()+"s");
		}
		else {
			return images.get(""+tileColor + piece.getColor() + piece.getTileCode()+"s");
		}
	}
	public static ImageIcon getHeldIcon(Piece piece, boolean usable) {
		String tileBG;
		if(usable) {
			tileBG = "W";
		}
		else {
			tileBG = "B";
		}
		return images.get(tileBG + piece.getColor() + piece.getTileCode() + "s");
	}
}

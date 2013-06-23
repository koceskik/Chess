import java.io.Serializable;

public class Player implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public PieceColor playerColor = null;
	
	public Player(PieceColor pc) {//TODO: add security here: unique ID or have server send unique ID each turn
		playerColor = pc;
	}
}

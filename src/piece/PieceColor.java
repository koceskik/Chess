package piece;
public enum PieceColor {
	W,B;
	
	public PieceColor getOpponent() {
		if(this == W) return B;
		else return W;
	}
}
public enum PieceColor {
	W("White"),
	B("Black");
	
	private String name;
	private PieceColor(String name) {
		this.name = name;
	}
	
	public String toReadableString() {
		return this.name;
	}
	
	public PieceColor getOpponent() {
		if(this == W) return B;
		else return W;
	}
}
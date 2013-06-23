import java.io.Serializable;

public class Move implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public int x1, y1;
	public int x2, y2;
	public Move(int x1, int y1, int x2, int y2) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}
}
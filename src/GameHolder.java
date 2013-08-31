import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;

public class GameHolder {
	private volatile Chess.ClientHandler self = null;
	
	private static GridBagConstraints grid = new GridBagConstraints();
	public static final Dimension dim = new Dimension(Tile.size,Tile.size);
	public static final Dimension scaledDim = new Dimension(Tile.scaledSize,Tile.scaledSize);
	public static final Border selectedBorder = BorderFactory.createLineBorder(Color.red);
	public static final Border legalMoveBorder = BorderFactory.createLineBorder(Color.blue);
	public static final Border nullBorder = BorderFactory.createEmptyBorder();
	private Tile selectedTile = null;
	private Integer selectedHeldPieceNum = null;
	private ArrayList<Move> legalMoveList = new ArrayList<Move>();
	private void clearLegalMoves() {
		for(Move m : legalMoveList) {
			getLabel(m.toTile.x,m.toTile.y).setBorder(nullBorder);
		}
		legalMoveList.clear();
	}
	
	private static final String[] promotionString = {"Queen", "Knight", "Rook", "Bishop"};
	public JComboBox<String> promotionList = new JComboBox<String>(promotionString);
	
	private Dimension d = null;
	private JPanel boardPanel = new JPanel();
	private JPanel whiteLabelPanel = new JPanel();
	private JLabel whiteLabel = new JLabel();
	private JPanel blackLabelPanel = new JPanel();
	private JLabel blackLabel = new JLabel();
	private JLabel[] xAxisLabel = new JLabel[8];
	private JLabel[] yAxisLabel = new JLabel[8];
	private JLabel[][] label = new JLabel[8][8];
	private ArrayList<JLabel> heldPieces = new ArrayList<JLabel>();
	private JPanel heldPiecesPanel = new JPanel();
	private JScrollPane heldPiecesScrollPane = new JScrollPane(heldPiecesPanel);
	
	private JLabel getLabel(int x, int y) {
		if(pc == PieceColor.W) {
			return label[y][x];
		}
		else {
			return label[7-y][7-x];
		}
	}
	
	public Game g = null;
	private PieceColor pc = null;//player's or partner's color (ie color of the bottom player)
	
	public GameHolder(Game g, PieceColor color, JPanel location, Chess.ClientHandler self, Dimension d) {
		this.g = g;
		this.pc = color;
		this.d = d;
		this.self = self;
		location.add(boardPanel);
		initBoardPanel();
	}
	
	private void initBoardPanel() {
		boardPanel.setLayout(new GridBagLayout());
		
		whiteLabel.setText("White");
		whiteLabelPanel.setBackground(Color.white);
		whiteLabelPanel.add(whiteLabel);
		grid.gridwidth = 2;
		if(d != dim) grid.gridwidth = 3;
		grid.gridx = 1;
		grid.gridy = 0;
		boardPanel.add(whiteLabelPanel, grid);
		
		blackLabel.setText("Black");
		blackLabel.setForeground(Color.white);
		blackLabelPanel.setBackground(Color.black);
		blackLabelPanel.add(blackLabel);
		grid.gridx = 3;
		if(d != dim) grid.gridx = 4;
		grid.gridy = 0;
		boardPanel.add(blackLabelPanel,grid);
		
		grid.gridwidth = 3;
		grid.gridx = 5;
		grid.gridy = 0;
		boardPanel.add(promotionList, grid);
		
		for(int i = 0;i<30;i++) {//30 is the maximum number of held pieces: 16P, 4N, 4B, 4R, 2Q
			JLabel newHeldPiece = new JLabel();
			heldPieces.add(newHeldPiece);
			heldPiecesPanel.add(newHeldPiece);
			
			final int x = i;
			final Player player = g.getPlayer(this.pc);
			heldPieces.get(i).addMouseListener(new MouseListener() {
				@Override
				public void mousePressed(MouseEvent arg0) {
					if(g.turn.equals(player)) {
						if(selectedHeldPieceNum == null) {
							selectedHeldPieceNum = x;
							heldPieces.get(x).setBorder(selectedBorder);
							
							/*for(Move m : g.getLegalMove(selectedTile)) {
								legalMoveList.add(m);
								getLabel(m.toTile.x, m.toTile.y).setBorder(legalMoveBorder);
							}*/
						}
						else if(selectedHeldPieceNum == x) {
							clearLegalMoves();
							heldPieces.get(x).setBorder(nullBorder);
							selectedHeldPieceNum = null;
						}
					}
				}
				@Override
				public void mouseClicked(MouseEvent arg0) {}
				@Override
				public void mouseEntered(MouseEvent arg0) {}
				@Override
				public void mouseExited(MouseEvent arg0) {}
				@Override
				public void mouseReleased(MouseEvent arg0) {}
			});
		}
		heldPiecesPanel.setBorder(nullBorder);
		heldPiecesPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		((FlowLayout) heldPiecesPanel.getLayout()).setVgap(0);
		((FlowLayout) heldPiecesPanel.getLayout()).setHgap(0);
		
		grid.gridx = 1;
		grid.gridy = 11;
		grid.gridwidth = 8;
		
		if(this.g.pW.gameCount > 0) {
			boardPanel.add(heldPiecesScrollPane, grid);
		}
		
		//TODO: create class HeldPiecesPanel extends JPanel implements Scrollable to set the preferred viewport size
		//TODO: MAYBE NOT since we need to know ahead of time the max size so it doesn't resize the window
		heldPiecesScrollPane.setPreferredSize(new Dimension(grid.gridwidth*d.width, 2*Tile.scaledSize+1));
		heldPiecesScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		heldPiecesScrollPane.setBorder(nullBorder);
		
		grid.gridwidth = 1;
		initBoard();
	}
	
	private void initBoard() {
		for(int i = 0;i<8;i++) {
			yAxisLabel[i] = new JLabel();
			if(pc == PieceColor.W) {
				yAxisLabel[i].setText(String.valueOf(8-i));
			}
			else {
				yAxisLabel[i].setText(String.valueOf(i+1));
			}
			grid.gridx = 0;
			grid.gridy = i+2;
			boardPanel.add(yAxisLabel[i],grid);
			for(int j = 0;j<8;j++) {
				label[i][j] = new JLabel();
				label[i][j].setPreferredSize(d);//necessary to prevent the resizing onClick (adds border)
				grid.gridx = j+1;
				grid.gridy = i+2;
				boardPanel.add(label[i][j],grid);
			}
		}
		grid.gridy = 10;
		for(int i = 0;i<8;i++) {
			xAxisLabel[i] = new JLabel();
			if(pc == PieceColor.W) {
				xAxisLabel[i].setText(String.valueOf((char) (97+i)));
			}
			else {
				xAxisLabel[i].setText(String.valueOf((char) (97+7-i)));
			}
			grid.gridx = i+1;
			boardPanel.add(xAxisLabel[i],grid);
		}
	}
	
	public void updateGame(Game g) {
		this.g = g;
		drawBoard();
	}
	
	public void drawBoard() {
		//display who's turn it is
		if(g.turn.color == PieceColor.W) {
			whiteLabelPanel.setBorder(selectedBorder);
			blackLabelPanel.setBorder(nullBorder);
		}
		else {
			whiteLabelPanel.setBorder(nullBorder);
			blackLabelPanel.setBorder(selectedBorder);
		}
		//display who's turn it is
		if(g.getWinner() != null) {
			if(g.getWinner().color == PieceColor.W) {
				whiteLabelPanel.setBorder(legalMoveBorder);
				blackLabelPanel.setBorder(nullBorder);
			}
			else {
				whiteLabelPanel.setBorder(nullBorder);
				blackLabelPanel.setBorder(legalMoveBorder);
			}
		}
		
		//draws board
		for(int i = 0;i<8;i++) {
			for(int j = 0;j<8;j++) {
				getLabel(j,i).setIcon((d == dim) ? g.board[i][j].getIcon() : g.board[i][j].getSmallIcon());
			}
		}
		
		//draws the pieces held/queued-up by the player
		Player player;
		if(pc == g.pW.color) {
			player = g.pW;
		}
		else {
			player = g.pB;
		}
		for(int i = 0;i<player.heldPieces.size();i++) {
			heldPieces.get(i).setIcon(Tile.getHeldIcon(player.heldPieces.get(i), true));
		}
		for(int i = 0;i<player.queuingPieces.size();i++) {
			heldPieces.get(player.heldPieces.size()+i).setIcon(Tile.getHeldIcon(player.queuingPieces.get(i), false));
		}
		heldPiecesScrollPane.revalidate();
	}
	
	public void initLabelClicks(final Player p) {
		for(int i = 0;i<8;i++) {
			for(int j = 0;j<8;j++) {
				final int x = j;
				final int y = i;
				getLabel(x,y).addMouseListener(new MouseListener() {
					@Override
					public void mousePressed(MouseEvent arg0) {
						if(g.turn.equals(p)) {
							if(selectedTile == null) {
								if(g.board[y][x].getPiece() != null) {
									if(g.board[y][x].getPiece().getColor() == p.color) {
										selectedTile = g.board[y][x];
										getLabel(x,y).setBorder(selectedBorder);

										for(Move m : g.getLegalMove(selectedTile)) {
											legalMoveList.add(m);
											getLabel(m.toTile.x, m.toTile.y).setBorder(legalMoveBorder);
										}
									}
								}
							}
							else if(selectedTile == g.board[y][x]) {
								clearLegalMoves();
								getLabel(x,y).setBorder(nullBorder);
								selectedTile = null;
							}
							else {
								Move m = new Move(selectedTile, g.board[y][x], p);
								if(selectedTile.getPiece() instanceof Pawn && (y == 0 || y == 7)) {
									m.moveType = Move.PROMOTE_QUEEN + promotionList.getSelectedIndex();
								}
								if(g.applyMove(m)) {
									self.send(m);
									getLabel(selectedTile.x,selectedTile.y).setBorder(nullBorder);
									selectedTile = null;
									clearLegalMoves();
								}
							}
						}
					}
					@Override
					public void mouseClicked(MouseEvent arg0) {}
					@Override
					public void mouseEntered(MouseEvent arg0) {}
					@Override
					public void mouseExited(MouseEvent arg0) {}
					@Override
					public void mouseReleased(MouseEvent arg0) {}
				});
			}
		}
	}
}

import java.awt.Color;
import java.awt.Dimension;
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
	private JLabel[] heldPieces = new JLabel[16];
	private JPanel heldPiecesPanel = new JPanel();
	private JScrollPane heldPiecesScrollPane = new JScrollPane(heldPiecesPanel);
	
	public JLabel getLabel(int x, int y) {
		if(pc == PieceColor.W) {
			return label[y][x];
		}
		else {
			return label[7-y][7-x];
		}
	}
	
	public Game g = null;
	private PieceColor pc = null;//player's or partner's color
	
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
		
		for(int i = 0;i<16;i++) {
			heldPieces[i] = new JLabel();
		}
		grid.gridx = 1;
		grid.gridy = 11;
		grid.gridwidth = 8;
		boardPanel.add(heldPiecesScrollPane, grid);

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
			heldPieces[i].setIcon(Tile.getHeldIcon(player.heldPieces.get(i), true));
		}
		for(int i = 0;i<player.queuingPieces.size();i++) {
			heldPieces[player.heldPieces.size()+i].setIcon(Tile.getHeldIcon(player.queuingPieces.get(i), false));
		}
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

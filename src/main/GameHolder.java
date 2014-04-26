package main;
import gameComponent.Game;
import gameComponent.Move;
import gameComponent.Player;
import gameComponent.Tile;

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

import networking.ClientSideConnection;
import piece.Pawn;
import piece.Piece;
import piece.PieceColor;

//TODO: make this extend a JPanel or something, so it can be added directly
public class GameHolder {
	private volatile ClientSideConnection self = null;
	
	private static GridBagConstraints grid = new GridBagConstraints();
	public static final Dimension dim = new Dimension(Tile.size,Tile.size);
	public static final Dimension scaledDim = new Dimension(Tile.scaledSize,Tile.scaledSize);
	public static final Border selectedBorder = BorderFactory.createLineBorder(Color.red);
	public static final Border legalMoveBorder = BorderFactory.createLineBorder(Color.blue);
	public static final Border nullBorder = BorderFactory.createEmptyBorder();
	private Piece selectedPiece = null;
	private int selectedHeldPiece = -1;
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
	//use this to add the boardPanel to the main UI
	public JPanel getHolderPanel() {
		return boardPanel;
	}
	private JPanel whiteLabelPanel = new JPanel();
	private JLabel whiteLabel = new JLabel();
	private JPanel blackLabelPanel = new JPanel();
	private JLabel blackLabel = new JLabel();
	private JLabel[] xAxisLabel = new JLabel[8];
	private JLabel[] yAxisLabel = new JLabel[8];
	private JLabel[][] label = new JLabel[8][8];
	private ArrayList<JLabel> heldPieces = new ArrayList<JLabel>();
	private ArrayList<JLabel> oppHeldPieces = new ArrayList<JLabel>();
	private JPanel heldPiecesPanel = new JPanel();
	private JPanel oppHeldPiecesPanel = new JPanel();
	private JScrollPane heldPiecesScrollPane = new JScrollPane(heldPiecesPanel);
	private JScrollPane oppHeldPiecesScrollPane = new JScrollPane(oppHeldPiecesPanel);
	
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
	
	public GameHolder(Game g, PieceColor color, ClientSideConnection self, Dimension d) {
		this.g = g;
		this.pc = color;
		this.d = d;
		this.self = self;
		initBoardPanel();
	}
	
	private void initBoardPanel() {
		boardPanel.setLayout(new GridBagLayout());
		float fontSizeScaled = 9.0f;//this needs to be a float because int references the style not fontSize 
		
		whiteLabel.setText("White");
		whiteLabelPanel.setBackground(Color.white);
		if(d != dim) whiteLabel.setFont(whiteLabel.getFont().deriveFont(fontSizeScaled));
		whiteLabelPanel.add(whiteLabel);
		grid.gridwidth = 2;
		if(d != dim) grid.gridwidth = 3;
		grid.gridx = 1;
		grid.gridy = 0;
		boardPanel.add(whiteLabelPanel, grid);
		
		blackLabel.setText("Black");
		if(d != dim) blackLabel.setFont(blackLabel.getFont().deriveFont(fontSizeScaled));
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
			
			JLabel newOppHeldPiece = new JLabel();
			oppHeldPieces.add(newOppHeldPiece);
			oppHeldPiecesPanel.add(newOppHeldPiece);
		}
		heldPiecesPanel.setBorder(nullBorder);
		heldPiecesPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		((FlowLayout) heldPiecesPanel.getLayout()).setVgap(0);
		((FlowLayout) heldPiecesPanel.getLayout()).setHgap(0);
		
		oppHeldPiecesPanel.setBorder(nullBorder);
		oppHeldPiecesPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		((FlowLayout) oppHeldPiecesPanel.getLayout()).setVgap(0);
		((FlowLayout) oppHeldPiecesPanel.getLayout()).setHgap(0);
		
		if(this.g.pW.partner != null) {
			grid.gridx = 1;
			grid.gridy = 1;
			grid.gridwidth = 8;
			
			oppHeldPiecesScrollPane.setPreferredSize(new Dimension(grid.gridwidth*d.width, 2*Tile.scaledSize+1));
			oppHeldPiecesScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
			oppHeldPiecesScrollPane.setBorder(nullBorder);
			boardPanel.add(oppHeldPiecesScrollPane, grid);
			
			grid.gridy = 12;
			heldPiecesScrollPane.setPreferredSize(new Dimension(grid.gridwidth*d.width, 2*Tile.scaledSize+1));
			heldPiecesScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
			heldPiecesScrollPane.setBorder(nullBorder);
			boardPanel.add(heldPiecesScrollPane, grid);
		}
		
		grid.gridwidth = 1;
		initBoard();
	}
	
	private void initBoard() {
		int addLine = 0;//used to add a line to make room for the heldPieces of the opponent (top of board)
		if(this.g.pW.gameCount > 1) {
			addLine = 1;
		}
		for(int i = 0;i<8;i++) {
			yAxisLabel[i] = new JLabel();
			if(pc == PieceColor.W) {
				yAxisLabel[i].setText(String.valueOf(8-i));
			}
			else {
				yAxisLabel[i].setText(String.valueOf(i+1));
			}
			grid.gridx = 0;
			grid.gridy = i+2+addLine;
			boardPanel.add(yAxisLabel[i],grid);
			for(int j = 0;j<8;j++) {
				label[i][j] = new JLabel();
				label[i][j].setPreferredSize(d);//necessary to prevent the resizing onClick (adds border)
				grid.gridx = j+1;
				grid.gridy = i+2+addLine;
				boardPanel.add(label[i][j],grid);
			}
		}
		grid.gridy = 10+addLine;
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
		//display a winner
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
		
		for(int i = 0;i<heldPieces.size();i++) {
			if(i < player.heldPieces.size()) {
				heldPieces.get(i).setIcon(Tile.getHeldIcon(player.heldPieces.get(i), true));
			}
			else if(i < player.heldPieces.size()+player.queuingPieces.size()) {
				heldPieces.get(i).setIcon(Tile.getHeldIcon(player.queuingPieces.get(i - player.heldPieces.size()), false));
			}
			else {
				heldPieces.get(i).setIcon(null);
			}
		}
		heldPiecesScrollPane.revalidate();
		
		for(int i = 0;i<oppHeldPieces.size();i++) {
			if(i < player.opponent.heldPieces.size()) {
				oppHeldPieces.get(i).setIcon(Tile.getHeldIcon(player.opponent.heldPieces.get(i), true));
			}
			else if(i < player.opponent.heldPieces.size()+player.opponent.queuingPieces.size()) {
				oppHeldPieces.get(i).setIcon(Tile.getHeldIcon(player.opponent.queuingPieces.get(i - player.opponent.heldPieces.size()), false));
			}
			else {
				oppHeldPieces.get(i).setIcon(null);
			}
		}
		oppHeldPiecesScrollPane.revalidate();
	}
	
	public void initLabelClicks(final Player p) {
		//for the Player's board
		for(int i = 0;i<8;i++) {
			for(int j = 0;j<8;j++) {
				final int x = j;
				final int y = i;
				getLabel(x,y).addMouseListener(new MouseListener() {
					@Override
					public void mousePressed(MouseEvent arg0) {
						if(g.turn.equals(p)) {
							if(selectedPiece == null) {
								if(g.board[y][x].getPiece() != null) {
									if(g.board[y][x].getPiece().getColor() == p.color) {
										selectedPiece = g.board[y][x].getPiece();
										getLabel(x,y).setBorder(selectedBorder);

										for(Move m : selectedPiece.getLegalMoves(g, false)) {
											legalMoveList.add(m);
											getLabel(m.toTile.x, m.toTile.y).setBorder(legalMoveBorder);
										}
									}
								}
							}
							else if(selectedPiece == g.board[y][x].getPiece()) {
								clearLegalMoves();
								getLabel(x,y).setBorder(nullBorder);
								selectedPiece = null;
							}
							else {
								if(selectedPiece.loc != null) {
									Move m = new Move(selectedPiece, g.board[y][x], p);
									if(selectedPiece instanceof Pawn && (y == 0 || y == 7)) {
										m.moveType = Move.MoveType.getPromotionType(promotionList.getSelectedIndex());
									}
									if(g.isLegalMove(m)) {
										self.send(m);
										getLabel(selectedPiece.getX(),selectedPiece.getY()).setBorder(nullBorder);
										selectedPiece = null;
										clearLegalMoves();
									}
								}
								else {
									Move m = new Move(selectedPiece, g.board[y][x], p);
									m.moveType = Move.MoveType.PLACEMENT;
									if(g.isLegalMove(m)) {
										self.send(m);
										heldPieces.get(selectedHeldPiece).setBorder(nullBorder);
										selectedPiece = null;
										clearLegalMoves();
									}
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
		
		//for the Player's held pieces
		for(int i = 0;i<30;i++) {//30 is the maximum number of held pieces: 16P, 4N, 4B, 4R, 2Q
			final int x = i;
			heldPieces.get(i).addMouseListener(new MouseListener() {
				@Override
				public void mousePressed(MouseEvent arg0) {
					if(g.turn.equals(p) && g.turn.heldPieces.size() > x) {
						if(selectedPiece == null) {
							selectedPiece = g.turn.heldPieces.get(x);
							selectedHeldPiece = x;
							heldPieces.get(x).setBorder(selectedBorder);
							
							for(Move m : selectedPiece.getLegalPlacement(g)) {
								legalMoveList.add(m);
								getLabel(m.toTile.x, m.toTile.y).setBorder(legalMoveBorder);
							}
						}
						else if(selectedPiece == g.turn.heldPieces.get(x)) {
							clearLegalMoves();
							heldPieces.get(x).setBorder(nullBorder);
							selectedPiece = null;
							selectedHeldPiece = -1;
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

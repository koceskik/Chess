import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

public class Chess extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final int port = 3355;
	private static String serverIP = "127.0.0.1";
	private static volatile ClientHandler self = null;

	private GridBagConstraints grid = null;
	private JPanel mainScreen = null;
	private JButton chessGame = null;
	private JButton bughouseGame = null;
	private JButton joinGame = null;
	private JTextField ipAddress = null;
	private JPanel gameScreen = null;
	private JPanel[] boardPanel = new JPanel[2];

	private static final Dimension dim = new Dimension(Tile.size,Tile.size);
	private static final Dimension scaledDim = new Dimension(Tile.scaledSize,Tile.scaledSize);
	private static final Border selectedBorder = BorderFactory.createLineBorder(Color.red);
	private static final Border legalMoveBorder = BorderFactory.createLineBorder(Color.blue);
	private static final Border nullBorder = BorderFactory.createEmptyBorder();
	private Tile selectedTile = null;
	private ArrayList<Move> legalMoveList = new ArrayList<Move>();
	private void clearLegalMoves() {
		for(Move m : legalMoveList) {
			getLabel(m.toTile.x,m.toTile.y, 0).setBorder(nullBorder);
		}
		legalMoveList.clear();
	}

	private volatile ArrayList<Game> g = new ArrayList<Game>();
	private volatile Player p = null;

	private final String[] promotionString = {"Queen", "Knight", "Rook", "Bishop"};
	private JComboBox<String> promotionList = new JComboBox<String>(promotionString);

	private ArrayList<JPanel> whiteLabelPanel = new ArrayList<JPanel>();
	private ArrayList<JLabel> whiteLabel = new ArrayList<JLabel>();
	private ArrayList<JPanel> blackLabelPanel = new ArrayList<JPanel>();
	private ArrayList<JLabel> blackLabel = new ArrayList<JLabel>();
	private ArrayList<JLabel[]> xAxisLabel = new ArrayList<JLabel[]>();
	private ArrayList<JLabel[]> yAxisLabel = new ArrayList<JLabel[]>();
	private ArrayList<JLabel[][]> label = new ArrayList<JLabel[][]>();

	public JLabel getLabel(int x, int y, int id) {
		if(p.color == PieceColor.W && id == 0) {
			return label.get(id)[y][x];//this may need to be flipped dependingly
		}
		else if(p.color == PieceColor.B && id == 1) {
			return label.get(id)[y][x];//this may need to be flipped dependingly
		}
		else {//note: flipped from getLabel() because p.color is opposite p.partner.color
			return label.get(id)[7-y][7-x];
		}
	}

	public static void main(String[] args) {
		Chess c = new Chess();
		c.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		c.setVisible(true);
	}
	public Chess() {
		setTitle("Chess");

		getContentPane().setLayout(new CardLayout());
		grid = new GridBagConstraints();

		initMainScreen();
		initChessScreen();
		pack();

		((CardLayout)getContentPane().getLayout()).show(getContentPane(), "MAIN");
	}
	public void initMainScreen() {
		mainScreen = new JPanel();
		mainScreen.setLayout(new GridBagLayout());
		getContentPane().add(mainScreen, "MAIN");
		grid.gridx = 0;
		grid.gridy = 0;

		chessGame = new JButton();
		chessGame.setText("New Chess Game");
		chessGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new ChessServer().start();
				startGame();
			}
		});
		mainScreen.add(chessGame,grid);

		bughouseGame = new JButton();
		bughouseGame.setText("New Bughouse Game");
		bughouseGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new BughouseServer().start();
				startGame();
			}
		});
		grid.gridy++;
		mainScreen.add(bughouseGame,grid);

		joinGame = new JButton();
		joinGame.setText("Join Game");
		grid.gridy++;
		mainScreen.add(joinGame,grid);
		joinGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(ipAddress.getText().equals("")) {
					ipAddress.setText("127.0.0.1");
				}
				serverIP = ipAddress.getText();
				startGame();
			}
		});

		ipAddress = new JTextField(10);
		ipAddress.setText("127.0.0.1");
		grid.gridy++;
		mainScreen.add(ipAddress,grid);
	}

	public void startGame() {
		connectToServer();//blocks until it receives Player and Game
		if(g.size() > 1) initBughouseScreen();
		for(Game game : g) {
			drawBoard(game);
		}
		initLabelClicks();
		initChessboardLabels();
		((CardLayout)getContentPane().getLayout()).show(getContentPane(), "GAME");
		pack();
	}

	public void connectToServer() {//get client, setup I/O streams to/from client
		try {
			Socket clientSocket = new Socket(serverIP, port);
			self = new ClientHandler(clientSocket);
			System.out.println("Client socket accepted");
			System.out.println("Created I/O streams");
		}
		catch(IOException ex) {
			System.out.println("Failed to accept client OR create I/O streams");
			System.exit(1);
		}
		//TODO: add a display "Waiting for players"
		self.getInitialInfo();
		self.start();
	}

	public void initChessScreen() {
		gameScreen = new JPanel();
		getContentPane().add(gameScreen, "GAME");
		boardPanel[0] = new JPanel();
		boardPanel[0].setLayout(new GridBagLayout());
		gameScreen.add(boardPanel[0]);

		whiteLabel.add(new JLabel());
		whiteLabel.get(0).setText("White");
		whiteLabelPanel.add(new JPanel());
		whiteLabelPanel.get(0).setBackground(Color.white);
		whiteLabelPanel.get(0).add(whiteLabel.get(0));
		grid.gridwidth = 2;
		grid.gridx = 1;
		grid.gridy = 0;
		boardPanel[0].add(whiteLabelPanel.get(0),grid);

		blackLabel.add(new JLabel());
		blackLabel.get(0).setText("Black");
		blackLabel.get(0).setForeground(Color.white);
		blackLabelPanel.add(new JPanel());
		blackLabelPanel.get(0).setBackground(Color.black);
		blackLabelPanel.get(0).add(blackLabel.get(0));
		grid.gridwidth = 2;
		grid.gridx = 3;
		grid.gridy = 0;
		boardPanel[0].add(blackLabelPanel.get(0),grid);

		grid.gridwidth = 3;
		grid.gridx = 5;
		grid.gridy = 0;
		promotionList.setSelectedIndex(0);
		boardPanel[0].add(promotionList, grid);

		grid.gridwidth = 1;
		initBoard();
	}
	
	public void initBughouseScreen() {
		boardPanel[1] = new JPanel();
		boardPanel[1].setLayout(new GridBagLayout());
		gameScreen.add(boardPanel[1]);
		
		whiteLabel.add(new JLabel());
		whiteLabel.get(1).setText("White");
		whiteLabelPanel.add(new JPanel());
		whiteLabelPanel.get(1).setBackground(Color.white);
		whiteLabelPanel.get(1).add(whiteLabel.get(1));
		grid.gridwidth = 3;
		grid.gridx = 1;
		grid.gridy = 0;
		boardPanel[1].add(whiteLabelPanel.get(1),grid);

		blackLabel.add(new JLabel());
		blackLabel.get(1).setText("Black");
		blackLabel.get(1).setForeground(Color.white);
		blackLabelPanel.add(new JPanel());
		blackLabelPanel.get(1).setBackground(Color.black);
		blackLabelPanel.get(1).add(blackLabel.get(1));
		grid.gridwidth = 3;
		grid.gridx = 4;
		grid.gridy = 0;
		boardPanel[1].add(blackLabelPanel.get(1),grid);

		grid.gridwidth = 1;
		initBoard();
	}

	public void initBoard() {
		xAxisLabel.add(new JLabel[8]);
		yAxisLabel.add(new JLabel[8]);
		label.add(new JLabel[8][8]);
		int itemNum = label.size()-1;
		for(int i = 0;i<8;i++) {
			yAxisLabel.get(itemNum)[i] = new JLabel();
			yAxisLabel.get(itemNum)[i].setText(String.valueOf(8-i));
			grid.gridx = 0;
			grid.gridy = i+1;
			boardPanel[itemNum].add(yAxisLabel.get(itemNum)[i],grid);
			for(int j = 0;j<8;j++) {
				label.get(itemNum)[i][j] = new JLabel();
				Dimension d = dim;
				if(itemNum > 0) { 
					d = scaledDim;
				}
				label.get(itemNum)[i][j].setPreferredSize(d);
				grid.gridx = j+1;
				grid.gridy = i+1;
				boardPanel[itemNum].add(label.get(itemNum)[i][j],grid);
			}
		}
		for(int i = 0;i<8;i++) {
			xAxisLabel.get(itemNum)[i] = new JLabel();
			xAxisLabel.get(itemNum)[i].setText(String.valueOf((char) (97+i)));
			grid.gridx = i+1;
			grid.gridy = 9;
			boardPanel[itemNum].add(xAxisLabel.get(itemNum)[i],grid);
		}
	}

	public void initLabelClicks() {
		for(int i = 0;i<8;i++) {
			for(int j = 0;j<8;j++) {
				final int x = j;
				final int y = i;
				getLabel(x,y,0).addMouseListener(new MouseListener() {
					@Override
					public void mousePressed(MouseEvent arg0) {
						if(g.get(p.gameID).turn.equals(p)) {
							if(selectedTile == null) {
								if(g.get(p.gameID).board[y][x].getPiece() != null) {
									if(g.get(p.gameID).board[y][x].getPiece().getColor() == p.color) {
										selectedTile = g.get(p.gameID).board[y][x];
										getLabel(x,y,0).setBorder(selectedBorder);

										for(Move m : g.get(p.gameID).getLegalMove(selectedTile)) {
											legalMoveList.add(m);
											getLabel(m.toTile.x, m.toTile.y, 0).setBorder(legalMoveBorder);
										}
									}
								}
							}
							else if(selectedTile == g.get(p.gameID).board[y][x]) {
								clearLegalMoves();
								getLabel(x,y,0).setBorder(nullBorder);
								selectedTile = null;
							}
							else {
								Move m = new Move(selectedTile, g.get(p.gameID).board[y][x], p);
								if(selectedTile.getPiece() instanceof Pawn && (y == 0 || y == 7)) {
									m.moveType = Move.PROMOTE_QUEEN + promotionList.getSelectedIndex();
								}
								if(g.get(p.gameID).applyMove(m)) {
									self.send(m);
									getLabel(selectedTile.x,selectedTile.y, 0).setBorder(nullBorder);
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
	
	public void initChessboardLabels() {
		for(int i = 0;i<g.size();i++) {
			
		}
	}
	
	public void drawBoard(Game game) {//TODO: change g in this situation
		int id;
		if(g.get(p.gameID).id == game.id) id = 0;//TODO: un-hardcode
		else id = 1;
		//display who's turn it is
		if(g.get(id).turn.color == PieceColor.W) {
			whiteLabelPanel.get(id).setBorder(selectedBorder);
			blackLabelPanel.get(id).setBorder(nullBorder);
		}
		else {
			whiteLabelPanel.get(id).setBorder(nullBorder);
			blackLabelPanel.get(id).setBorder(selectedBorder);
		}
		//draws board
		for(int i = 0;i<8;i++) {
			for(int j = 0;j<8;j++) {
				getLabel(j,i, id).setIcon(id == 0 ? game.board[i][j].getIcon() : game.board[i][j].getSmallIcon());
			}
		}
	}

	public void closeSockets() {
		if(self != null) {
			self.close();
		}

		System.out.println("Client Exited");
		System.exit(0);
	}

	class ClientHandler {
		private Socket socket = null;
		private ObjectOutputStream oos = null;
		private InputHandler ih = null;

		public ClientHandler(Socket s) {
			this.socket = s;
			try {
				oos = new ObjectOutputStream(s.getOutputStream());
				ih = new InputHandler(s);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
		public void getInitialInfo() {
			p = ih.getPlayer();
			for(int i = 0;i<p.gameCount+1;i++) {
				Game game = ih.read();//guarantee that there is a board
				g.add(game);
			}
		}
		class InputHandler extends Thread {
			private ObjectInputStream ois = null;//TODO: use public get() method, remove read(). Or incorporate read() in run()
			public InputHandler(Socket s) {
				try {
					ois = new ObjectInputStream(s.getInputStream());
				}
				catch(IOException e) {
					e.printStackTrace();
				}
			}
			@Override
			public void run() {
				boolean doLoop = true;
				while(doLoop) {
					Game recGame = read();
					if(recGame != null) {
						g.set(recGame.id, recGame);
						drawBoard(recGame);
					}
					else {
						doLoop = false;
					}
				}
			}
			public Player getPlayer() {
				try {
					return (Player) ois.readObject();
				}
				catch(ClassNotFoundException e) {
					e.printStackTrace();
				}
				catch(IOException e) {
					e.printStackTrace();
				}
				return null;
			}

			public Game read() {//TODO: incorporate this into the InputHandler.run() itself
				try {
					return (Game) ois.readObject();
				}
				catch(ClassNotFoundException e) {
					//e.printStackTrace();
					self.close();
				}
				catch(IOException e) {
					//e.printStackTrace();
					self.close();
				}
				return null;
			}
		}
		public void start() {
			ih.start();
		}

		public void close() {
			try {
				if(oos != null) {
					oos.close();
				}
				if(socket != null) {
					socket.close();
				}
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}

		public void send(Move move) {
			try {
				oos.writeObject(move);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
}

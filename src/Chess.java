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
	private JButton startNewGame = null;
	private JButton joinGame = null;
	private JTextField ipAddress = null;
	private JPanel gameScreen = null;

	private static final Dimension dim = new Dimension(32,32); 
	private static final Border selectedBorder = BorderFactory.createLineBorder(Color.red);
	private static final Border legalMoveBorder = BorderFactory.createLineBorder(Color.blue);
	private static final Border nullBorder = BorderFactory.createEmptyBorder();
	private Tile selectedTile = null;
	private ArrayList<Move> legalMoveList = new ArrayList<Move>();
	private void clearLegalMoves() {
		for(Move m : legalMoveList) {
			getLabel(m.toTile.x,m.toTile.y).setBorder(nullBorder);
		}
		legalMoveList.clear();
	}

	private volatile Game g = null;
	private volatile Player p = null;

	private JPanel whiteLabelPanel = new JPanel();
	private JLabel whiteLabel = new JLabel();
	private JPanel blackLabelPanel = new JPanel();
	private JLabel blackLabel = new JLabel();
	private JLabel[] xAxisLabel = new JLabel[8];
	private JLabel[] yAxisLabel = new JLabel[8];
	private JLabel[][] label = new JLabel[8][8];
	public JLabel getLabel(int x, int y) {
		if(p.color == PieceColor.W) {
			return label[y][x];
		}
		else {
			return label[7-y][7-x];
		}
	}

	private String[] promotionString = {"Queen", "Knight", "Rook", "Bishop"};
	private JComboBox<String> promotionList = new JComboBox<String>(promotionString);

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
		initGameScreen();
		pack();
		//initializeBughouseScreen() here so pack() doesn't make window wide

		((CardLayout)getContentPane().getLayout()).show(getContentPane(), "MAIN");
		
	}
	public void initMainScreen() {
		mainScreen = new JPanel();
		mainScreen.setLayout(new GridBagLayout());
		getContentPane().add(mainScreen, "MAIN");

		startNewGame = new JButton();
		startNewGame.setText("New Game");
		startNewGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new ChessServer().start();
				startGame();
			}
		});
		grid.gridx = 0;
		grid.gridy = 0;
		mainScreen.add(startNewGame,grid);

		joinGame = new JButton();
		joinGame.setText("Join Game");
		grid.gridx = 0;
		grid.gridy = 1;
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
		grid.gridx = 0;
		grid.gridy = 2;
		mainScreen.add(ipAddress,grid);
	}
	
	public void startGame() {
		connectToServer();//blocks until it receives Player and Game
		drawBoard(g.board);
		initLabelClicks();
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


	public void initGameScreen() {
		gameScreen = new JPanel();
		gameScreen.setLayout(new GridBagLayout());
		getContentPane().add(gameScreen, "GAME");

		whiteLabel.setText("White");
		whiteLabelPanel.setBackground(Color.white);
		whiteLabelPanel.add(whiteLabel);
		grid.gridwidth = 2;
		grid.gridx = 1;
		grid.gridy = 0;
		gameScreen.add(whiteLabelPanel,grid);

		blackLabel.setText("Black");
		blackLabel.setForeground(Color.white);
		blackLabelPanel.setBackground(Color.black);
		blackLabelPanel.add(blackLabel);
		grid.gridwidth = 2;
		grid.gridx = 3;
		grid.gridy = 0;
		gameScreen.add(blackLabelPanel,grid);

		grid.gridwidth = 3;
		grid.gridx = 5;
		grid.gridy = 0;
		promotionList.setSelectedIndex(0);
		gameScreen.add(promotionList, grid);

		grid.gridwidth = 1;
		initBoard();
	}

	public void initBoard() {
		for(int i = 0;i<8;i++) {
			yAxisLabel[i] = new JLabel();
			yAxisLabel[i].setText(String.valueOf(8-i));
			grid.gridx = 0;
			grid.gridy = i+1;
			gameScreen.add(yAxisLabel[i],grid);
			for(int j = 0;j<8;j++) {
				label[i][j] = new JLabel();
				label[i][j].setPreferredSize(dim);
				grid.gridx = j+1;
				grid.gridy = i+1;
				gameScreen.add(label[i][j],grid);
			}
		}
		for(int i = 0;i<8;i++) {
			xAxisLabel[i] = new JLabel();
			xAxisLabel[i].setText(String.valueOf((char) (97+i)));
			grid.gridx = i+1;
			grid.gridy = 9;
			gameScreen.add(xAxisLabel[i],grid);
		}
	}

	public void initLabelClicks() {
		for(int i = 0;i<8;i++) {
			if(p.color == PieceColor.B) {
				yAxisLabel[i].setText(String.valueOf(i+1));
				grid.gridx = 0;
				grid.gridy = i+1;
				gameScreen.add(yAxisLabel[i],grid);
			}
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
								Move m = new Move(selectedTile, g.board[y][x]);
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
		if(p.color == PieceColor.B) {
			for(int i = 0;i<8;i++) {
				xAxisLabel[i].setText(String.valueOf((char) (97+7-i)));
				grid.gridx = i+1;
				grid.gridy = 9;
				gameScreen.add(xAxisLabel[i],grid);
			}
		}
	}

	public void drawBoard(Tile[][] board) {
		if(g.turn.color == PieceColor.W) {
			whiteLabelPanel.setBorder(selectedBorder);
			blackLabelPanel.setBorder(nullBorder);
		}
		else {
			whiteLabelPanel.setBorder(nullBorder);
			blackLabelPanel.setBorder(selectedBorder);
		}
		for(int i = 0;i<8;i++) {
			for(int j = 0;j<8;j++) {
				getLabel(j,i).setIcon(board[i][j].getIcon()); 
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
			g = ih.read();//guarantee that there is a board
		}
		class InputHandler extends Thread {
			private ObjectInputStream ois = null;
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
				while(g != null) {
					g = read();
					if(g != null) {
						drawBoard(g.board);
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

			public Game read() {
				try {
					return (Game) ois.readObject();
				}
				catch(ClassNotFoundException e) {
					e.printStackTrace();
				}
				catch(IOException e) {
					e.printStackTrace();
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

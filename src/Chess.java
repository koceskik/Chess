import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.border.Border;

public class Chess extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final int port = 3355;
	private static String serverIP = "127.0.0.1";//TODO: add in the ability to manually edit this
	private static ClientHandler self = null;

	private GridBagConstraints grid = null;
	private static final Border selectedBorder = BorderFactory.createLineBorder(Color.red);
	private static final Border legalMoveBorder = BorderFactory.createLineBorder(Color.blue);
	private static final Border nullBorder = BorderFactory.createEmptyBorder();
	private Tile selectedTile = null;
	private ArrayList<Move> legalMoveList = new ArrayList<Move>();
	private void clearLegalMoves() {
		for(Move m : legalMoveList) {
			label[m.y2][m.x2].setBorder(nullBorder);
		}
		legalMoveList.clear();
	}

	private Game g = null;
	private Player p = null;

	private JLabel playerLabel = new JLabel();
	private JLabel currentTurnLabel = new JLabel();
	private JLabel[] xAxisLabel = new JLabel[8];
	private JLabel[] yAxisLabel = new JLabel[8];
	private JLabel[][] label = new JLabel[8][8];

	public static void main(String[] args) {
		//new ChessServer().start();
		Chess c = new Chess();
		c.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		c.setVisible(true);
		c.receiveGame();
	}

	public void receiveGame() {
		while(true) {
			if(self != null) {
				g = self.read();
				drawBoard(g.board);
			}
		}
	}

	public Chess() {
		setTitle("Chess");

		//get client, setup I/O streams to/from client
		try {
			Socket clientSocket = new Socket(serverIP, port);
			self = new ClientHandler(clientSocket);
			System.out.println("Accepted client socket");
			System.out.println("Created I/O streams");
		}
		catch(IOException ex) {
			System.out.println("Failed to accept client OR create I/O streams");
			System.exit(1);
		}

		grid = new GridBagConstraints();
		getContentPane().setLayout(new GridBagLayout());

		p = self.getPlayer();
		g = self.read();//guarantee that there is a board
		
		playerLabel.setText("You are: " + p.playerColor.toReadableString());
		grid.gridwidth = 5;
		grid.gridx = 0;
		grid.gridy = 0;
		getContentPane().add(playerLabel,grid);
		
		currentTurnLabel.setText("Current Move: " + g.turn.toReadableString());
		grid.gridwidth = 5;
		grid.gridx = 5;
		grid.gridy = 0;
		getContentPane().add(currentTurnLabel,grid);
		
		grid.gridwidth = 1;
		
		initializeBoard(g);//also draws board
		pack();
	}

	public void initializeBoard(Game game) {
		for(int i = 0;i<8;i++) {
			yAxisLabel[i] = new JLabel();
			yAxisLabel[i].setText(String.valueOf(8-i));
			grid.gridx = 0;
			grid.gridy = i+1;
			getContentPane().add(yAxisLabel[i],grid);
			for(int j = 0;j<8;j++) {
				final int x = j;
				final int y = i;
				label[y][x] = new JLabel();
				Dimension dim = new Dimension(g.board[y][x].getIcon().getIconWidth(), g.board[y][x].getIcon().getIconHeight());
				label[y][x].setPreferredSize(dim);
				label[y][x].setIcon(g.board[y][x].getIcon());
				label[y][x].addMouseListener(new MouseListener() {
					@Override
					public void mousePressed(MouseEvent arg0) {
						if(g.turn == p.playerColor) {
							if(selectedTile == null) {
								if(g.board[y][x].getPiece() != null) {
									if(g.board[y][x].getPiece().getColor() == p.playerColor) {
										selectedTile = g.board[y][x];
										label[y][x].setBorder(selectedBorder);
										
										for(Move m : g.getLegalMove(selectedTile)) {
											legalMoveList.add(m);
											label[m.y2][m.x2].setBorder(legalMoveBorder);
										}
									}
								}
							}
							else if(selectedTile == g.board[y][x]) {
								clearLegalMoves();
								label[y][x].setBorder(nullBorder);
								selectedTile = null;
							}
							else {
								//TODO: legal checks against game before sending
								g.board[y][x].addPiece(selectedTile.getPiece());
								selectedTile.addPiece(null);
								label[y][x].setIcon(g.board[y][x].getIcon());
								label[selectedTile.getY()][selectedTile.getX()].setIcon(selectedTile.getIcon());
								label[selectedTile.getY()][selectedTile.getX()].setBorder(nullBorder);
								selectedTile = null;
								clearLegalMoves();
								self.send(g);
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

				grid.gridx = x+1;
				grid.gridy = y+1;
				getContentPane().add(label[y][x],grid);
			}
		}
		for(int i = 0;i<8;i++) {
			xAxisLabel[i] = new JLabel();
			xAxisLabel[i].setText(String.valueOf((char) (65+i)));
			grid.gridx = i+1;
			grid.gridy = 9;
			getContentPane().add(xAxisLabel[i],grid);
		}
	}

	public void drawBoard(Tile[][] board) {
		currentTurnLabel.setText("Current Move: " + g.turn.toReadableString());
		for(int i = 0;i<8;i++) {
			for(int j = 0;j<8;j++) {
				label[i][j].setIcon(board[i][j].getIcon());
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
		private ObjectInputStream ois = null;

		public ClientHandler(Socket s) {
			this.socket = s;
			try {
				oos = new ObjectOutputStream(s.getOutputStream());
				ois = new ObjectInputStream(s.getInputStream());
			}
			catch(IOException e) {
				e.printStackTrace();
			}
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

		public void send(Game game) {
			try {
				oos.writeObject(game);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

}

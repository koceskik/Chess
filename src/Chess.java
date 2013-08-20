import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Chess extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final int port = 3355;
	private static String serverIP = "127.0.0.1";
	private static final String initialIP = "127.0.0.1";
	private static volatile ClientHandler self = null;

	private GridBagConstraints grid = new GridBagConstraints();
	private JPanel mainScreen = new JPanel();
	private JButton chessGame = null;
	private JButton bughouseGame = null;
	private JButton joinGame = null;
	private JTextField ipAddress = null;
	private JPanel gameScreen = new JPanel();

	private volatile ArrayList<GameHolder> g = new ArrayList<GameHolder>();
	private volatile Player p = null;

	public static void main(String[] args) {
		Chess c = new Chess();
		c.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		c.setVisible(true);
	}
	public Chess() {
		setTitle("Chess");
		getContentPane().setLayout(new CardLayout());
		getContentPane().add(mainScreen, "MAIN");
		getContentPane().add(gameScreen, "GAME");

		initMainScreen();
		pack();

		((CardLayout)getContentPane().getLayout()).show(getContentPane(), "MAIN");
		setSize(289,346);//the default size of the current layout of a single ChessBoard
	}
	public void initMainScreen() {
		mainScreen.setLayout(new GridBagLayout());
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
					ipAddress.setText(initialIP);
				}
				serverIP = ipAddress.getText();
				startGame();
			}
		});

		ipAddress = new JTextField(10);
		ipAddress.setText(initialIP);
		grid.gridy++;
		mainScreen.add(ipAddress,grid);
	}

	public void startGame() {
		connectToServer();//blocks until it receives Player and Game
		for(GameHolder gh : g) {
			if(p.gameID == gh.g.id) {
				gh.initLabelClicks(p);
			}
			else {
				gh.promotionList.setVisible(false);
			}
			gh.drawBoard();
		}
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
		self.start();
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
		public void start() {
			p = ih.getPlayer();
			for(int i = 0;i<p.gameCount+1;i++) {
				Game game = ih.read();
				PieceColor pc;
				Dimension d;
				if(game.id == p.gameID) {
					pc = p.color;
					d = GameHolder.dim;
				}
				else {
					pc = p.color.getOpponent();
					d = GameHolder.scaledDim;
					
				}
				g.add(new GameHolder(game, pc, gameScreen, this, d));
			}
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
						g.get(recGame.id).updateGame(recGame);
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
	}
}

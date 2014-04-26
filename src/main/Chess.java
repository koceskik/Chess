package main;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import networking.BughouseServer;
import networking.ChessServer;
import networking.ClientSideConnection;
import networking.Connection;


public class Chess extends JFrame implements UICallback {
	private static final long serialVersionUID = 1L;
	
	private static final String initialIP = "127.0.0.1";
	private static String serverIP = initialIP;
	private static ClientSideConnection csc = null;

	private GridBagConstraints grid = new GridBagConstraints();
	private JPanel mainScreen = new JPanel();
	private JButton chessGame = null;
	private JButton bughouseGame = null;
	private JButton joinGame = null;
	private JTextField ipAddress = null;
	private JPanel gameScreen = new JPanel();
	
	//UICallback methods
	public void addGameHolder(GameHolder gameHolder) {
		gameScreen.add(gameHolder.getHolderPanel());
	}
	public void setCardPane(CardPane cp) {
		((CardLayout)getContentPane().getLayout()).show(getContentPane(), cp.toString());
	}

	public static void main(String[] args) {
		Chess c = new Chess();
		c.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		c.setVisible(true);
	}
	public Chess() {
		setTitle("Chess");
		getContentPane().setLayout(new CardLayout());
		getContentPane().add(mainScreen, CardPane.MAIN.toString());
		getContentPane().add(gameScreen, CardPane.GAME.toString());

		initMainScreen();
		pack();

		setCardPane(CardPane.MAIN);
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
				connectToServer();
			}
		});
		mainScreen.add(chessGame,grid);

		bughouseGame = new JButton();
		bughouseGame.setText("New Bughouse Game");
		bughouseGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new BughouseServer().start();
				connectToServer();
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
				connectToServer();
			}
		});

		ipAddress = new JTextField(10);
		ipAddress.setText(initialIP);
		grid.gridy++;
		mainScreen.add(ipAddress,grid);
	}

	public void connectToServer() {//get client, setup I/O streams to/from client
		try {
			Socket clientSocket = new Socket(serverIP, Connection.port);
			csc = new ClientSideConnection(clientSocket);
			csc.subscribe(this);
			System.out.println("Client socket accepted");
			System.out.println("Created I/O streams");
		}
		catch(IOException ex) {
			System.out.println("Failed to accept client/create I/O streams");
		}
		//TODO: add a display "Waiting for players"
		Thread t = new Thread(csc);
		t.setDaemon(true);
		t.start();
	}

	public void closeSockets() {
		if(csc != null) {
			csc.close();
		}

		System.out.println("Client Exited");
	}
}

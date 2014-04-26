package networking;

import gameComponent.Game;
import gameComponent.Move;
import gameComponent.Player;

import java.awt.Dimension;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import main.GameHolder;
import piece.PieceColor;


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
			GameHolder gameHolder = new GameHolder(game, pc, csc, d); 
			g.add(gameHolder);
			addGameHolder(gameHolder);
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
					if(recGame.getWinner() != null) {
						if(recGame.id == g.size()-1) {
							doLoop = false;
						}
					}
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
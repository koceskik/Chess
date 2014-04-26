package networking;

import gameComponent.Game;
import gameComponent.Player;

import java.awt.Dimension;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import main.CardPane;
import main.GameHolder;
import main.UICallback;
import piece.PieceColor;

public class ClientSideConnection extends Connection {
	private volatile ArrayList<GameHolder> gh = new ArrayList<GameHolder>();
	private volatile Player p = null;

	public ClientSideConnection(Socket s) {
		super(s);
	}

	@Override
	public void run() {
		try {
			while(!Thread.interrupted()) {
				Object o = ois.readObject();
				if(o instanceof Game) {
					Game game = (Game) o;
					
					if(p != null) {
						if(gh.size() < p.gameCount+1) {
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
							GameHolder gameHolder = new GameHolder(game, pc, this, d); 
							gh.add(gameHolder);
							addGameHolder(gameHolder);
							
							if(gh.size() == p.gameCount+1) {
								for(GameHolder gHolder : gh) {//used for generalization purposes, it could be done directly
									if(p.gameID == gHolder.g.id) {
										gHolder.initLabelClicks(p);
									}
									else {
										gHolder.promotionList.setVisible(false);
									}
									gHolder.drawBoard();
								}
								pack();
								setCardPane(CardPane.GAME);
							}
						}
						else {
							gh.get(game.id).updateGame(game);//TODO: this is similar to a ui callback, but CSC contains 
							if(game.getWinner() != null) {
								if(game.id == gh.size() - 1) {
									break;
								}
							}
						}
					}
				}
				else if(o instanceof Player) {
					Player player = (Player) o;
					if(p == null) {
						p = player;
					}
					else {
						throw new RuntimeException("Received a Player more than once");//TODO: better error?
					}
				}
			}
		}
		catch(ClassNotFoundException e) {e.printStackTrace();}
		catch(IOException e) {e.printStackTrace();}
	}

	//TODO: implement UI callback methods
	private void addGameHolder(GameHolder gameHolder) {
		for(UICallback uic : subscribedUI) {
			uic.addGameHolder(gameHolder);
		}
	}
	
	private void pack() {
		for(UICallback uic : subscribedUI) {
			uic.pack();
		}
	}
	private void setCardPane(CardPane cp) {
		for(UICallback uic : subscribedUI) {
			uic.setCardPane(cp);
		}
	}
}

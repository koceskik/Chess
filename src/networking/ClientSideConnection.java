package networking;

import gameComponent.Game;
import gameComponent.Player;

import java.awt.Dimension;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import main.CardPane;
import main.GameHolderPanel;
import main.UICallback;
import piece.PieceColor;

public class ClientSideConnection extends Connection {
	private volatile ArrayList<GameHolderPanel> gh = new ArrayList<GameHolderPanel>();
	private volatile Player p = null;
	
	private ArrayList<UICallback> subscribedUI = new ArrayList<UICallback>();
	public void subscribe(UICallback e) {
		subscribedUI.add(e);
	}
	public void unsubscribe(UICallback e) {
		subscribedUI.remove(e);
	}

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
						if(gh.size() < p.gameCount) {
							PieceColor pc;
							Dimension d;
							if(game.id == p.gameID) {
								pc = p.color;
								d = GameHolderPanel.dim;
							}
							else {
								pc = p.color.getOpponent();
								d = GameHolderPanel.scaledDim;
							}
							GameHolderPanel gameHolder = new GameHolderPanel(game, pc, this, d); 
							gh.add(gameHolder);
							addGameHolder(gameHolder);
							
							if(gh.size() == p.gameCount) {
								for(GameHolderPanel gHolder : gh) {
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
							gh.get(game.id).updateGame(game); 
							if(game.getWinner() != null) {
								if(game.id == gh.size() - 1) {
									break;
								}
							}
						}
					}
					else {
						throw new RuntimeException("ClientSideConnection: Received Game data before Player data");
					}
				}
				else if(o instanceof Player) {
					Player player = (Player) o;
					if(p == null) {
						p = player;
					}
					else {
						throw new RuntimeException("ClientSideConnection: Received a Player more than once");
					}
				}
				else {
					throw new RuntimeException("ClientSideConnection: Received unexpected Object type");
				}
			}
		}
		catch(ClassNotFoundException e) {e.printStackTrace();}
		catch(IOException e) {/*e.printStackTrace();*/}
		finally {
			close();
		}
	}

	//UICallback methods
	private void addGameHolder(GameHolderPanel gameHolder) {
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

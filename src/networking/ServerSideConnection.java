package networking;

import gameComponent.Move;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ServerSideConnection extends Connection {
	private ArrayList<ServerCallback> subscribedServers = new ArrayList<ServerCallback>();
	public void subscribe(ServerCallback e) {
		subscribedServers.add(e);
	}
	public void unsubscribe(ServerCallback e) {
		subscribedServers.remove(e);
	}

	public ServerSideConnection(Socket s) {
		super(s);
	}

	@Override
	public void run() {
		try {
			while(!Thread.interrupted()) {
				Object o = ois.readObject();
				if(o instanceof Move) {
					Move m = (Move) o;
					applyMove(m);
				}
				else {
					throw new RuntimeException("ServerSideConnection: Received unexpected Object type");
				}
				
			}
		}
		catch(ClassNotFoundException e) {e.printStackTrace();}
		catch(IOException e) {/*e.printStackTrace();*/}
		finally {
			close();
			removeServerSideConnection(this);
		}
	}
	
	//ServerCallback methods
	public void removeServerSideConnection(ServerSideConnection ssc) {
		for(ServerCallback sc : subscribedServers) {
			sc.removeServerSideConnection(ssc);
		}
	}
	public void applyMove(Move m) {
		for(ServerCallback sc : subscribedServers) {
			sc.applyMove(m);
		}
	}
}

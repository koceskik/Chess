package networking;

import gameComponent.Move;

import java.io.IOException;
import java.net.Socket;

public class ServerSideConnection extends Connection {

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

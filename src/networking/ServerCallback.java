package networking;

import gameComponent.Move;

public interface ServerCallback {
	public void removeServerSideConnection(ServerSideConnection ssc);
	public void applyMove(Move m);
}

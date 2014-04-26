package networking;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public abstract class Server extends Thread implements ServerCallback {
	protected ServerSocket server = null;
	protected ArrayList<ServerSideConnection> playerList = new ArrayList<ServerSideConnection>();
	
	public Server() {
		setDaemon(true);
	}
	
	protected void setupServerSocket() {
		try {
			server = new ServerSocket(Connection.port);
			InetAddress ip = InetAddress.getLocalHost();
			System.out.println("Server IP address: " + ip.getHostAddress());
			System.out.println("Listening on port " + Connection.port);
		}
		catch(UnknownHostException e) {
			e.printStackTrace();
		}
		catch(IOException ex) {
			System.err.println("Error: can't connect to port " + Connection.port);
		}
	}
	
	protected void acceptClient() {
		try {
			Socket s = server.accept();
			ServerSideConnection ssc = new ServerSideConnection(s);
			playerList.add(ssc);
			ssc.subscribe(this);
			new Thread(ssc).start();
			System.out.println("Accepted client socket");
		}
		catch(IOException ex) {
			System.out.println("Failed to accept client/create I/O streams");
		}
	}
	
	@Override
	public void removeServerSideConnection(ServerSideConnection ssc) {
		playerList.remove(ssc);//TODO: announce to other players, perhaps allow another player to join
	}

}

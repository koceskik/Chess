package networking;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import main.UICallback;

public abstract class Connection implements Runnable {
	public static final int port = 3355;
	
	private Socket socket = null;
	private ObjectOutputStream oos = null;
	protected ObjectInputStream ois = null;
	protected ArrayList<UICallback> subscribedUI = new ArrayList<UICallback>();
	public void subscribe(UICallback e) {
		subscribedUI.add(e);
	}
	public void unsubscribe(UICallback e) {
		subscribedUI.remove(e);
	}

	public Connection(Socket s) {
		this.socket = s;
		try {
			oos = new ObjectOutputStream(s.getOutputStream());
			ois = new ObjectInputStream(s.getInputStream());
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean send(Object o) {
		boolean returner = false;
		try {
			oos.writeObject(o);
			oos.reset();//necessary to send new object, not just references
			returner = true;
		}
		catch(IOException e) {e.printStackTrace();}
		return returner;
	}
	
	public void close() {
		if(oos != null) {
			try {
				oos.close();
			}
			catch(IOException e) {e.printStackTrace();}
		}
		if(ois != null) {
			try {
				ois.close();
			}
			catch(IOException e) {e.printStackTrace();}
		}
		if(socket != null) {
			try {
				socket.close();
			}
			catch(IOException e) {e.printStackTrace();}
		}
	}
}
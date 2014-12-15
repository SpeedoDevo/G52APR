import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Network {
	public Network(int port) {
		try {
			//start listening on port
			@SuppressWarnings("resource")
			ServerSocket serverSocket = new ServerSocket(port);
			System.out.println("Server listening on port " + port);
			while(true) {
				//create new thread for each client
				Socket clientSoc = serverSocket.accept();
				//setting a 60s timeout duration
				clientSoc.setSoTimeout(60000);
				(new Thread(new ConnectionHandler(clientSoc))).start();
				//System.out.println(Thread.activeCount());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
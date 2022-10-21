import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {

	private ServerSocket serverSocket;

	public ChatServer(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	public void launchServer() {
		System.out.println("Server running on port: " + serverSocket.getLocalPort() + " ... waiting for clients...");
		try {
			
			while (!serverSocket.isClosed()) {
				
				Socket socket = serverSocket.accept(); //Blocking method
				System.out.println("A new client has connected!");
				ClientHandler clientHandler = new ClientHandler(socket);
				
				Thread thread = new Thread(clientHandler);
				thread.start();
			}
			
		} catch (IOException e) {
			// Do nothing here
		}	
	} 

	public void closeServerSocket() {
		try {
			if (serverSocket != null) {
				serverSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		
		int port = args.length > 0 ? Integer.parseInt(args[0]) : 1234; // Take port from command line argument if specified
		
		ServerSocket serverSocket = new ServerSocket(port);
		ChatServer server = new ChatServer(serverSocket);
		server.launchServer();
		
	}
}

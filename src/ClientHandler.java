import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
	
	
	public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>(); //Store active (connected) ClientHandler objects
	private Socket socket;
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	private String clientUsername;
	
	public ClientHandler(Socket socket) {
		
		try {
			this.socket = socket;
			this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.clientUsername = bufferedReader.readLine();
			clientHandlers.add(this); //Add ME to the list of active (connected) ClientHandlers
			broadcastMessage("SYSTEM: " + clientUsername + " has entered the chat!");
		} catch (IOException e) {
			shutdown(socket, bufferedReader, bufferedWriter);
			
		}
	}
	
	@Override
	public void run() {
		String messageFromClient;
		
		while (socket.isConnected()) {
			try {
				messageFromClient = bufferedReader.readLine();
				broadcastMessage(messageFromClient);
			} catch (IOException e) {
				shutdown(socket, bufferedReader, bufferedWriter);
				break;
			}
		}
		
	}

	public void broadcastMessage(String messageToSend) {
		for (ClientHandler clientHandler : clientHandlers) {
			try {
				if (!clientHandler.clientUsername.equals(clientUsername)) {
					clientHandler.bufferedWriter.write(messageToSend);
					clientHandler.bufferedWriter.newLine();
					clientHandler.bufferedWriter.flush();
				}
			} catch (IOException e) {
				shutdown(socket, bufferedReader, bufferedWriter);
			}
		}
	}
	
	public void removeClientHandler() { 
		clientHandlers.remove(this); //Remove THIS CLIENT from the list of active (connected) ClientHandlers
		broadcastMessage("SYSTEM: " + clientUsername + " has left the chat!");
		System.out.println(clientUsername + " has disconnected!");
	}
	
	public void shutdown(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
		removeClientHandler();
		try {
			if (bufferedReader != null ) {
				bufferedReader.close();
			}
			if (bufferedWriter != null ) {
				bufferedWriter.close();
			}
			if (socket != null ) {
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

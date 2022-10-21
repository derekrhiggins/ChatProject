import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.lang.Thread;

public class ChatClient {
	
	private Socket socket;
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	private String username;
	
	public ChatClient(Socket socket, String username) {
		try {
			System.out.println("Connecting to server on " + socket.getInetAddress() + ":" + socket.getPort());
			this.socket = socket;
			this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.username = username;
		} catch (IOException e) {
			shutdown(socket, bufferedReader, bufferedWriter);
		}
	}

	public void sendMessage() {
		try {
			bufferedWriter.write(username);
			bufferedWriter.newLine();
			bufferedWriter.flush();
			
			Scanner scanner = new Scanner(System.in);
			while (socket.isConnected()) { // Method loops continuously here (or until socket connection drops)
				String messageToSend = scanner.nextLine();
				
				//Check if a command has been entered by examining CharAt(0)
				try { //Catching the exception raised if hitting return and scanner is empty then checking charAt(0)
					if (messageToSend != null) {
						if (messageToSend.charAt(0) == '\\' && messageToSend.charAt(1) == 'q') { 
							// Could add other commands here using a case/select and examining charAt(1) but we just have quit on \q
							System.out.println("Exiting the chat...");
							System.exit(0);
						} else {
							//Else send message out
							bufferedWriter.write(username + ": " + messageToSend); 
							bufferedWriter.newLine();
							bufferedWriter.flush();
						}
					}
				} catch (StringIndexOutOfBoundsException e) {
					// Do nothing if return hit by accident
				}
			}
		} catch (IOException e) {
			shutdown(socket, bufferedReader, bufferedWriter);
		}
	}
	
	public void listenForMessage() {
		new Thread(){
			@Override
			public void run() {
				String msgFromGroupChat;
				
				while (socket.isConnected()) { //This thread loops here (or until socket connection drops)
					try {
						msgFromGroupChat = bufferedReader.readLine();
						System.out.println(msgFromGroupChat);
					} catch (IOException e) {
						shutdown(socket, bufferedReader, bufferedWriter);
					}
				}
			}
		}.start();
	}
	
	public void shutdown(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
		System.out.println("Shutting down... connection to server may have been lost.");
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
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException {
		
		String hostname;
		int port;
		try {
			hostname = args.length > 0 ? args[0] : "localhost";
			port = args.length > 0 ? Integer.parseInt(args[1]) : 1234;

			Scanner scanner = new Scanner(System.in);
			System.out.println("Choose your username: ");
			String username = scanner.nextLine();
					
			ChatClient client;
			Socket socket = new Socket(hostname, port);
			client = new ChatClient(socket, username);
					
			client.listenForMessage(); //Method creates seperate thread
			client.sendMessage(); //Method waits for input from user on console
			
		} catch (ConnectException e) {
			System.out.println("Unable to establish connection with server. Check it is running and that the hostname and port are correct if specified.");
			System.out.println("Terminating...");
			System.exit(0);
		} catch (ArrayIndexOutOfBoundsException e1) {
			System.out.println("Command line arguments must be in the format: [hostname] [port]");
			System.exit(0);
		}

	}
	
	
}

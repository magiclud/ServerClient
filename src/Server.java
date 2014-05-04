import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	private static ServerSocket serverSocket = null;
	private static Socket clientSocket = null;

	public static void main(String[] args) throws IOException {

		// default port number;
		// we cannot choose a port less than 1023 if we are not privileged users
		// (root)
		int portNumber = 2222;

		try {
			serverSocket = new ServerSocket(portNumber);
			System.out.println("Server is working.");
		} catch (Exception e) {
			System.err.println("Port already in use.");
			System.exit(1);
		}

		// create a client socket for each connection and pass it to a new
		// client thread
		while (true) {
			try {
				// accept each client
				clientSocket = serverSocket.accept();
				System.out.println("New client : " + clientSocket);

				Thread t = new Thread(new ClientThread(clientSocket));

				t.start();

			} catch (Exception e) {
				System.err.println("Error in connection attempt.");
			}
		}
	}
}

class ClientThread implements Runnable {

	private Socket clientSocket = null;
	private InputStreamReader inputStr = null;
	private BufferedReader inBuffRead = null;

	public ClientThread(Socket client) {
		this.clientSocket = client;
	}

	@Override
	public void run() {
		try {
			// create input and output stream for this client
			inputStr = new InputStreamReader(clientSocket.getInputStream());
			inBuffRead = new BufferedReader(inputStr);
			String fileName;
			String newPath;
			while ((fileName = inBuffRead.readLine()) != null) {
				if ((newPath = inBuffRead.readLine()) != null) {
					sendFile(fileName, newPath);
				}
			}

			inBuffRead.close();

		} catch (IOException ex) {
			ex.printStackTrace();
			// Logger.getLogger(CLIENTConnection.class.getName()).log(Level.SEVERE,
			// null, ex);
		}
	}

	public void sendFile(String fileName, String newPath) {
		try {
			String pathFile = "d:\\eclipse\\semestr4\\MultiServer\\"+fileName;
			// handle file read
			File file = new File(pathFile);
			byte[] byteArray = new byte[(int) file.length()];

			FileInputStream fInputStr = new FileInputStream(file);
			BufferedInputStream buffInputStr = new BufferedInputStream(fInputStr);

			DataInputStream dataInputStr = new DataInputStream(buffInputStr);
			dataInputStr.readFully(byteArray, 0, byteArray.length);

			// handle file send over socket
			OutputStream outputStr = clientSocket.getOutputStream();

			// Sending file name and file size to the server
			DataOutputStream dataOutputStr = new DataOutputStream(outputStr);
			dataOutputStr.writeUTF(file.getName());
			dataOutputStr.writeLong(byteArray.length);
			dataOutputStr.write(byteArray, 0, byteArray.length);
			dataOutputStr.flush();
			System.out.println("Sent to client: " +fileName );
		} catch (Exception e) {
			System.err.println("File does not exist!");
		}
	}

}

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

/*
 * A chat server that delivers public and private messages.
 */
public class MultiServer {

	// The server socket.
	private static ServerSocket serverSocket = null;
	// The client socket.
	private static Socket clientSocket = null;

	// maksymalna liczba klientow
	private static final int maxClientsCount = 3;
	// tablica z watkami wszystkich klientow
	private static final ClientThread[] threads = new ClientThread[maxClientsCount];
	
	private static String[] titles = { "test.txt", "test1.txt", "test2.txt" };

	public static void main(String args[]) {

		// The default port number.
		int portNumber = 2222;

		/*
		 * Open a server socket on the portNumber (default 2222). Note that we
		 * can not choose a port less than 1023 if we are not privileged users
		 * (root).
		 */
		try {
			serverSocket = new ServerSocket(portNumber);
		} catch (IOException e) {
			System.out.println(e);
		}

		/*
		 * Create a client socket for each connection and pass it to a new
		 * client thread.
		 */
		while (true) {
			try {
				// akceptuje wstepnie wszystkich klientow
				clientSocket = serverSocket.accept();
				int i = 0;
				for (i = 0; i < maxClientsCount; i++) {
					if (threads[i] == null) {
						// wyszukuje pierwszego wolnego miejsca w tablicy i
						// tworze watek klienta
						(threads[i] = new ClientThread(clientSocket, threads, i, titles))
								.start();
						break;
					}
				}
				// new table
				// jesli jednak nie znalazlem wolnego miejsca to wyczerpany
				// zostal limit liczby jednoczesnie
				// obslugiwanych klientow
				if (i == maxClientsCount) {
					PrintStream os = new PrintStream(
							clientSocket.getOutputStream());
					os.println("Server too busy. Try later.");
					os.close();
					clientSocket.close();
				}
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}
}

/**
 * Klasa reprezentujaca watek klienta
 * 
 */
class ClientThread extends Thread {

	private DataInputStream is = null;
	private PrintStream os = null;

	private ObjectInputStream objInStr = null;
	private ObjectOutputStream objOutStr = null;
	private Socket clientSocket = null;
	private final ClientThread[] threads;
	private int maxClientsCount;
	private int klientNr;
	private String[] titles;

	public ClientThread(Socket clientSocket, ClientThread[] threads,
			int klientNr, String[] titles) throws IOException {
		this.clientSocket = clientSocket;
		this.threads = threads;
		maxClientsCount = threads.length;
		this.klientNr = klientNr;
		this.titles=titles;
	}

	@Override
	public void run() {

		try {
			/* Create input and output streams for this client. */
			is = new DataInputStream(clientSocket.getInputStream());
			os = new PrintStream(clientSocket.getOutputStream());
			os.println("Dostepne pliki:");
			for (int i = 0; i < titles.length; i++) {
				os.println(titles[i]);
			}
			while (true) {
			//os.println("Podaj plik i sciezke wyjsciowa.");

		
				String title = is.readLine().trim();
				String path = is.readLine().trim();
				//wysylam plik 
				sendFile(title, path);
				// dzialam az ktos nie poda /quit
				if (title.startsWith("/quit") || path.startsWith("/guit")) {
					break;
				}

				os.println(title);
				os.println(path);

			}
			// jesli wyszedlem z petli to znaczy ze skonczylem, wysylam ta
			// inforamcje 

			os.println("*** Bye ***");

			/*
			 * Clean up. Set the current thread variable to null so that a new
			 * client could be accepted by the server.
			 */
			posprzatajPolaczenie();

			/*
			 * Close the output stream, close the input stream, close the
			 * socket.
			 */
			is.close();
			os.close();
			clientSocket.close();
		} catch (IOException e) {
		}
	}

	private void sendFile(String name, String pathOut) throws IOException {
		synchronized (this) {
		objInStr = new ObjectInputStream(clientSocket.getInputStream());
		objOutStr = new ObjectOutputStream(clientSocket.getOutputStream());
	//	String fileForClient = "d:\\eclipse\\semestr4\\MulticlientServer\\client"+ this.klientNr+ name;
		File outFile = new File(pathOut);
		String pathFile = "d:\\eclipse\\semestr4\\MulticlientServer\\" + name;
		File file = new File (pathFile); 
		
		objOutStr.writeObject( outFile);  
		  
        FileInputStream fis = new FileInputStream(file);  
        byte [] buffer = new byte[MultiClient.BUFFER_SIZE];  
        Integer bytesRead = 0;  
  
        while ((bytesRead = fis.read(buffer)) > 0) {  
        	objOutStr.writeObject(bytesRead);  
        	objOutStr.writeObject(Arrays.copyOf(buffer, buffer.length));  
        }  
  
        objOutStr.close();  
        objInStr.close();  
		}
	}

	private void posprzatajPolaczenie() {
		synchronized (this) {
			for (int i = 0; i < maxClientsCount; i++) {
				if (threads[i] == this) {
					threads[i] = null;
				}
			}
		}
	}

//	private void powiadomOdpowiedniegoKlieta(String wiadomosc) {
//		synchronized (this) {
//			for (int i = 0; i < maxClientsCount; i++) {
//				if (threads[i] != null && threads[i] != this) {
//					threads[i].os.println(wiadomosc);
//				}
//			}
//		}
//	}
}
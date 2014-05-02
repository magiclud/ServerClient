import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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
			System.err.println("IOException: " + e);
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
						(threads[i] = new ClientThread(clientSocket, threads,
								i, titles)).start();
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

	private InputStreamReader is = null;
	private BufferedReader buferReader = null;
	private PrintStream os = null;
	
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
		this.titles = titles;
	}

	@Override
	public void run() {

		try {
			/* Create input and output streams for this client. */
			is = new InputStreamReader(clientSocket.getInputStream());
			buferReader = new BufferedReader(is);
			os = new PrintStream(clientSocket.getOutputStream());
		
			os.println("Dostepne pliki:");
			for (int i = 0; i < titles.length; i++) {
				os.println(titles[i]);
			}
			os.println("Podaj plik i sciezke wyjsciowa.");
			while (true) {
				System.out.println("Czytam od klienta co chce pobrac: ");
				String title = buferReader.readLine().trim();
				String path = buferReader.readLine().trim();
				// I am working until /quit
				if (title.startsWith("/quit") || path.startsWith("/guit")) {
					break;
				}
				System.out.println("Przed wyslaniem pliku do klienta o nazwie:  " + title+"    \noraz o ciezce docelowej:  " + path);
				send( title);
				System.out.println("Wyslano poprwnie plik " + title
						+ ", obraz znajduje sie " + path);
			

			}
			// jesli wyszedlem z petli to znaczy ze skonczylem, wysylam ta
			// inforamcje
			posprzatajPolaczenie();
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
			System.out.println(e);
		}
	}

	public void send( String name) throws IOException {
		String pathFile = "D:\\eclipse\\semestr4\\MulticlientServer\\" + name;
		File myFile = new File(pathFile);
		byte[] arrayByte = new byte[(int) myFile.length()];
		System.out.println("przed");
		FileInputStream fileInputStr = new FileInputStream(myFile);
		System.out.println("po");
		BufferedInputStream bufInStr = new BufferedInputStream(fileInputStr );
		
		DataInputStream dataInStr = new DataInputStream (bufInStr);
		dataInStr.readFully(arrayByte, 0, arrayByte.length);
		
		//sending  file size to the socket
		DataOutputStream dataOutStr = new DataOutputStream(clientSocket.getOutputStream());
		dataOutStr.writeLong(arrayByte.length);
		dataOutStr.write(arrayByte, 0, arrayByte.length);
		dataOutStr.flush();
		System.out.println("Wyslano, jestem po flush");
		//os.close();//musze to zamnknac bbo inczaczej nie ma danych w nowym pliku s
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
}
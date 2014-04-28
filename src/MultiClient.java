import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class MultiClient implements Runnable {

	public static final int BUFFER_SIZE = 100;
	// The client socket
	private static Socket clientSocket = null;
	// The output stream
	private static PrintStream os = null;
	// The input stream
	private static DataInputStream is = null;

	private static BufferedReader inputLine = null;
	private static boolean closed = false;

	// String pathout=
	// "d:\eclipse\semestr4\MulticlientServer\downloadedFile.txt"
	public static void main(String[] args) {

		// The default port.
		int portNumber = 2222;
		// The default host.
		String host = "localhost";

		if (args.length < 2) {
			System.out
					.println("Usage: java MultiThreadChatClient <host> <portNumber>\n"
							+ "Now using host="
							+ host
							+ ", portNumber="
							+ portNumber);
		} else {
			host = args[0];
			portNumber = Integer.valueOf(args[1]).intValue();
		}

		/*
		 * Open a socket on a given host and port. Open input and output
		 * streams.
		 */
		try {
			clientSocket = new Socket(host, portNumber);
			inputLine = new BufferedReader(new InputStreamReader(System.in));
			os = new PrintStream(clientSocket.getOutputStream());
			is = new DataInputStream(clientSocket.getInputStream());
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + host);
		} catch (IOException e) {
			System.err
					.println("Couldn't get I/O for the connection to the host "
							+ host);
		}

		/*
		 * If everything has been initialized then we want to write some data to
		 * the socket we have opened a connection to on the port portNumber.
		 */
		if (clientSocket != null && os != null && is != null) {
			try {

				/* Create a thread to read from the server. */
				new Thread(new MultiClient()).start();
				String fileName = inputLine.readLine().trim();
				String filePath = inputLine.readLine().trim();
			
				while (!closed) {
//					String fileName = inputLine.readLine().trim();
//					String filePath = inputLine.readLine().trim();
//					// os.println("wiadomosc do serwera");

					os.println(fileName);
					os.println(filePath);
					//saveFile();
					InputStream objInStr = 
							clientSocket.getInputStream();
					
					 new MultiClient().receiveFile(objInStr, filePath);
					 OutputStream os = clientSocket.getOutputStream();
					fileName = inputLine.readLine().trim();
					filePath = inputLine.readLine().trim();

				}
				/*
				 * Close the output stream, close the input stream, close the
				 * socket.
				 */
				os.close();
				is.close();
				clientSocket.close();
			} catch (Exception e) {
				System.err.println("IOException: " + e);
			}
		}
	}

	private static void saveFile() throws Exception {
		// ObjectOutputStream objOutStr = new
		// ObjectOutputStream(clientSocket.getOutputStream());

		ObjectInputStream objInStr = 
				(ObjectInputStream) clientSocket.getInputStream();
		

		FileOutputStream fileOutStr = null;
		byte[] buffer = new byte[BUFFER_SIZE];

		// 1. Read file name.
		Object obj = objInStr.readObject();

		if (obj instanceof String) {
			fileOutStr = new FileOutputStream(obj.toString());
		} else {
			throwException("Something is wrong");
		}

		// 2. Read file to the end.
		Integer bytesRead = 0;

		do {
			obj = objInStr.readObject();

			if (!(obj instanceof Integer)) {
				throwException("Something is wrong");
			}

			bytesRead = (Integer) obj;

			obj = objInStr.readObject();

			if (!(obj instanceof byte[])) {
				throwException("Something is wrong");
			}

			buffer = (byte[]) obj;

			// 3. Write data to output file.
			fileOutStr.write(buffer, 0, bytesRead);

		} while (bytesRead == BUFFER_SIZE);

		System.out.println("File transfer success");

		fileOutStr.close();

		objInStr.close();
		// objOutStr.close();
	}

	private void receiveFile(	InputStream is2, String path) throws IOException {
		 int filesize = 6022386;
	        int bytesRead;
	        int current = 0;
	        byte[] mybytearray = new byte[filesize];

	        FileOutputStream fos = new FileOutputStream(path);
	        BufferedOutputStream bos = new BufferedOutputStream(fos);
	        bytesRead = is.read(mybytearray, 0, mybytearray.length);
	        current = bytesRead;

	        do {
	            bytesRead = is.read(mybytearray, current,
	                    (mybytearray.length - current));
	            if (bytesRead >= 0)
	                current += bytesRead;
	        } while (bytesRead > -1);

	        bos.write(mybytearray, 0, current);
	        bos.flush();
	        bos.close();
		
	}

	public static void throwException(String message) throws Exception {
		throw new Exception(message);
	}

	/*
	 * Create a thread to read from the server. (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		/*
		 * Keep on reading from the socket till we receive "Bye" from the
		 * server. Once we received that then we want to break.
		 */
		String responseLine;
		try {
			while ((responseLine = is.readLine()) != null) {
				System.out.println(responseLine);
				if (responseLine.indexOf("*** Bye") != -1)
					break;
			}
			closed = true;
		} catch (IOException e) {
			System.err.println("IOException: " + e);
		}
	}
}
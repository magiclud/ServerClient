import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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
	private static DataInputStream inputStreamData = null;

	private static BufferedReader inputLine = null;
	private static boolean closed = false;

	private static String pathout=  "D:\\eclipse\\semestr4\\MulticlientServer\\";
	
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
			inputStreamData = new DataInputStream(clientSocket.getInputStream());
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
		if (clientSocket != null && os != null && inputStreamData != null) {
			try {

				/* Create a thread to read from the server. */
				new Thread(new MultiClient()).start();
				String fileName = inputLine.readLine().trim();
				String fileEnd = inputLine.readLine().trim();

			//	while (!closed) {
					// String fileName = inputLine.readLine().trim();
					// String filePath = inputLine.readLine().trim();
					// // os.println("wiadomosc do serwera");

					os.println(fileName);
					os.println(pathout + fileEnd);
					// saveFile();

					new MultiClient().receiveFile(fileEnd);
					System.out.println(fileName);
					System.out.println(fileEnd);

			//	}
				/*
				 * Close the output stream, close the input stream, close the
				 * socket.
				 */
				os.close();
				inputStreamData.close();
				clientSocket.close();
			} catch (Exception e) {
				System.err.println("IOException: " + e);
			}
		}
	}

	private void receiveFile(String path) throws IOException {

		// byte[] theByte = new byte[1];
		byte[] byteArray = new byte[1024];
		ByteArrayOutputStream arrayOutput = new ByteArrayOutputStream();

		if (inputStreamData != null) {

			FileOutputStream fileOutput = null;
			BufferedOutputStream bufferedOutput = null;
			try {
				System.out.println("downloading target file..."  + path);
				fileOutput = new FileOutputStream( path);
				bufferedOutput = new BufferedOutputStream(fileOutput);
				System.out.println("buffer: "+bufferedOutput);

				int processedByte = inputStreamData.read(byteArray, 0,
						byteArray.length);
				System.out.println("processedByte: "+processedByte);

				do {
					arrayOutput.write(byteArray);
					processedByte = inputStreamData.read(byteArray);
				} while (processedByte != -1);

				bufferedOutput.write(arrayOutput.toByteArray());
				bufferedOutput.flush();
				bufferedOutput.close();
				System.out.println("file downloaded");

			} catch (IOException ex) {
				System.out.println("file transfer error." + ex);
			}
		}

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
			while ((responseLine = inputStreamData.readLine()) != null) {
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
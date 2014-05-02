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

					os.println(fileName);
					os.println(pathout + fileEnd);
System.out.println("Przed pobraniem pliku od klienta");
					new MultiClient().receiveFile( fileEnd);
				
					System.out.println("Po pobraniu pliku od klienta");
			//	}
				/*
				 * Close the output stream, close the input stream, close the
				 * socket.
				 */
				os.close();
				System.out.println("Strumienie zamykam");
				inputStreamData.close();
				clientSocket.close();
			} catch (Exception e) {
				System.err.println("IOException: " + e);
			}
		}
	}

	private void receiveFile( String endPath) throws IOException {

		// byte[] theByte = new byte[1];
//		byte[] byteArray = new byte[1024];
//
//		if (inputStreamData != null) {
//
//			FileOutputStream fileOutput = null;
//			BufferedOutputStream bufferedOutput = null;
//			try {
//				System.out.println("downloading target file..."  + path);
//				fileOutput = new FileOutputStream( path);
//				bufferedOutput = new BufferedOutputStream(fileOutput);
//				System.out.println("buffer: "+bufferedOutput);
//
//				int bytesRead;
//				 while ((bytesRead = inputStreamData.read(byteArray)) != -1) {
//					 bufferedOutput.write(byteArray, 0, bytesRead);
//					 System.out.println("odbieram od serwera dane");
//			        }
//				
//				bufferedOutput.flush();
//				System.out.println("Przed zamknieciem strumienia buffer out put");
//				bufferedOutput.close();
//				System.out.println("file downloaded");
//				inputStreamData.close();
//				System.out.println("Zamykam strumien wejsciowy");
//			} catch (IOException ex) {
//				System.out.println("file transfer error." + ex);
//			}
//		}
		
		int bytesRead;
		OutputStream fileOutput = new FileOutputStream(endPath);
		long size = inputStreamData.readLong();
		byte[] bufferByte = new byte[1024];
		while(size>0 &&(bytesRead = inputStreamData.read(bufferByte,0, (int )Math.min(bufferByte.length, size)))!=-1){
			fileOutput.write(bufferByte,0,bytesRead);
			size -= bytesRead;
		}
		fileOutput.close();
		inputStreamData.close();
		System.out.println("Plik zosatal zapisany do: " + endPath);

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
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

public class Client {

	private static Socket clientSocket;
	private static String fileName;
	private static String newPath;
	private static  InputStreamReader inputStr;
	private static BufferedReader inBuffReader;
	private static PrintStream outputPrint;

	public static void main(String[] args) throws IOException {

		int portNumber = 2222;
		try {
			clientSocket = new Socket("localhost", portNumber);
			inputStr = new InputStreamReader(System.in);
			inBuffReader = new BufferedReader(inputStr);
		} catch (Exception e) {
			System.err
					.println("Cannot connect to the server, try again later.");
			System.exit(1);
		}

		outputPrint = new PrintStream(clientSocket.getOutputStream());

		try {
			System.out.println("Enter file name and new name (path) in the next line: ");
			fileName = inBuffReader.readLine();
			newPath = inBuffReader.readLine();
			outputPrint.println(fileName);
			outputPrint.println(newPath);
			receiveFile(fileName, newPath);
		} catch (Exception e) {
			System.err.println("not valid input");
		}

		clientSocket.close();
	}


	public static void receiveFile(String fileName, String newPath) {
		try {
			int bytesRead;
			InputStream inputStr = clientSocket.getInputStream();

			DataInputStream clientData = new DataInputStream(inputStr);

			fileName = clientData.readUTF(); //read data string 
			
			String odebranyPlik = "d:\\eclipse\\semestr4\\MultiServer\\odebraneOdSerwera\\" +newPath;
			OutputStream outputStr = new FileOutputStream((odebranyPlik));
			long size = clientData.readLong();
			byte[] buffer = new byte[1024];
			while (size > 0
					&& (bytesRead = clientData.read(buffer, 0,
							(int) Math.min(buffer.length, size))) != -1) {
				outputStr.write(buffer, 0, bytesRead);
				size -= bytesRead;
			}

			outputStr.close();
			inputStr.close();

			System.out.println("File " + fileName
					+ " received from Server. New place file is: " + odebranyPlik);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}

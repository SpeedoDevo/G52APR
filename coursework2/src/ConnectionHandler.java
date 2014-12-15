import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

public class ConnectionHandler implements Runnable {

	private static final String REQ_TIMEOUT = "HTTP/1.0 408 Request Timeout\r\n\r\n";
	private static final String ERROR = "HTTP/1.0 500 Internal Server Error\r\n\r\n";

	private Socket socket;

	public ConnectionHandler(Socket socket) {
		this.socket = socket;
	}

	public void run() {
		OutputStream out = null;
		try {
			// create output stream
			out = socket.getOutputStream();
			// get input stream
			InputStream input = socket.getInputStream();
			//convert it to bytes
			byte[] req = streamToBytes(input);
			RequestHandler requestHandler = new RequestHandler();
			byte[] res = requestHandler.processRequest(req);
			// send response
			out.write(res);
		} catch (SocketTimeoutException ste) {
			// sending req timeout res outside of RequestHandler as no req was received
			// although this is a 1.1 status code I think it's nicer than a meaningless 400
			try {
				out.write(REQ_TIMEOUT.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			try {
				out.write(ERROR.getBytes());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} finally {
			//try to close resources
			try {
				out.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private byte[] streamToBytes(InputStream input) throws IOException {
		byte[] buffer = new byte[10485760]; //10MB
		int bytesRead = input.read(buffer);
		return Arrays.copyOfRange(buffer, 0, bytesRead);
	}
}
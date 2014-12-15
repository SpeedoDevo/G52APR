import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;

import org.apache.http.client.utils.DateUtils;


public class RequestHandler implements IRequestHandler {

	private static final String BAD_REQUEST = "HTTP/1.0 400 Bad Request\r\n";
	private static final String FORBIDDEN = "HTTP/1.0 403 Forbidden\r\n";
	private static final String NOT_FOUND = "HTTP/1.0 404 Not Found\r\n";
	
	private static final boolean ACCEPT_HTTP_1_1 = false;
	
	private static final Serve serve = new Serve();
	
	private byte[] request;
	private String[] lines;
	private String[] reqTokens;
	private Hashtable<String,String> headers = new Hashtable<String,String>(20);
	
	public byte[] processRequest(byte[] request) {
		this.request = request;
		byte[] res = (BAD_REQUEST + errorHeaders()).getBytes();
		//split req to headers and entity body
		String[] headAndBody = new String(request).split("\\\r\\\n\\\r\\\n");
		try {
			//try to split lines and req line tokens
			lines = headAndBody[0].split("\\\r\\\n");
			reqTokens = lines[0].split(" ");
		} catch (ArrayIndexOutOfBoundsException e) {
			//if it didn't succeed return bad req
			return res;
		}
		//log req line
		System.out.println(lines[0]);
		//save headers in a hashtable
		for (String line : lines) {
			//System.out.println(line);
			String[] keyValuePair = line.split(": ");
			if (keyValuePair.length == 2) {
				headers.put(keyValuePair[0],keyValuePair[1]);
			}
		}
		//check number of req tokens to choose next action
		switch (reqTokens.length) {
			//handle a 1.0 req
			case 3:
				res = handleFull();
				break;
			//handle a 0.9 req
			case 2:
				res = handleSimple();
				break;
			//a req can only consist of 3 or 2 tokens
			default:
				break; //stays BAD_REQ
		}
		return res;
	}

	//method to handle 1.0 reqs
	private byte[] handleFull() {
		//1.1 reqs are accepted if ACCEPT_HTTP_1_1 is true
		if (!(ACCEPT_HTTP_1_1 || reqTokens[2].equals("HTTP/1.0")) || !isCorrectURI(reqTokens[1])) {
			return (BAD_REQUEST + errorHeaders()).getBytes();
		}
		//hand off HTTP method handling
		switch (reqTokens[0]) {
			case "GET":
				return handleGet();
			case "HEAD":
				return handleHead();
			case "POST":
				return handlePost();
			default:
				return handleExtension();
		}
	}

	private byte[] handleGet() {
		byte[] out;
		try {
			//if there is an If-Modified-Since header pass the req on to GETconditional
			if (headers.containsKey("If-Modified-Since")) {
				String time = headers.get("If-Modified-Since");
				LocalDateTime localDate = LocalDateTime.parse(time, DateTimeFormatter.RFC_1123_DATE_TIME);
				Instant instant = localDate.atZone(ZoneId.systemDefault()).toInstant();
				Date ifModifiedSince = Date.from(instant);
				out = serve.httpGETconditional(reqTokens[1],ifModifiedSince);
			} else {
				out = serve.httpGet(reqTokens[1]);
			}
		} catch (HTTPFileNotFoundException e) {
			out = (NOT_FOUND + errorHeaders()).getBytes();
		} catch (HTTPRuntimeException e) {
			//never happens
			out = null;
		} catch (HTTPPermissionDeniedException e) {
			out = (FORBIDDEN + errorHeaders()).getBytes();
		} 
		return out;
	}

	//pass req on to httpHEAD
	private byte[] handleHead() {
		byte[] out;
		try {
			out = serve.httpHEAD(reqTokens[1]);
		} catch (HTTPFileNotFoundException e) {
			out = (NOT_FOUND + errorHeaders()).getBytes();
		} catch (HTTPRuntimeException e) {
			//never happens
			out = null;
		} catch (HTTPPermissionDeniedException e) {
			out = (FORBIDDEN + errorHeaders()).getBytes();
		}
		return out;
	}

	private byte[] handlePost() {
		byte[] out = (BAD_REQUEST + errorHeaders()).getBytes();
		try {
			//only accepting reqs if it contains a Content-Length header and the value of it matches the size of the payload
			if (headers.containsKey("Content-Length")) {
				byte[] pattern = "\r\n\r\n".getBytes();
				byte[] body = Arrays.copyOfRange(request, indexOf(request, pattern), request.length);
				int expectedLength = Integer.parseInt(headers.get("Content-Length"));
				if (expectedLength == body.length) {
					out = serve.httpPOST(reqTokens[1],body);
				} //else stays bad req
			} //else stays bad req
		} catch (HTTPFileNotFoundException e) {
			out = (NOT_FOUND + errorHeaders()).getBytes();
		} catch (HTTPRuntimeException e) {
			//never happens
		} catch (HTTPPermissionDeniedException e) {
			out = (FORBIDDEN + errorHeaders()).getBytes();
		} catch (NumberFormatException e) {} //stays bad req
		return out;
	}

	//checks whether a non-standard HTTP method is valid
	private byte[] handleExtension() {
		if (reqTokens[0].matches(".*[\\(\\)<>@,;:\\\\<>/\\[\\]?=\\{\\}].*") || reqTokens[0].isEmpty()) {
			return (BAD_REQUEST + errorHeaders()).getBytes();
		} else {
			return ("HTTP/1.0 501 " + reqTokens[0] + " Not Implemented\r\n" + errorHeaders()).getBytes();
		}
	}

	//pass on to simpleGet if it is a valid 0.9 req
	private byte[] handleSimple() {
		byte[] out;
		//0.9 only has a GET method, and we don't care about absolute URIs
		if(!reqTokens[0].equals("GET") || !isCorrectURI(reqTokens[1])) {
			out = (BAD_REQUEST + errorHeaders()).getBytes();
		} else {
			try {
				out = serve.simpleGet(reqTokens[1]);
			} catch (HTTPFileNotFoundException e) {
				out = (NOT_FOUND + errorHeaders()).getBytes();
			} catch (HTTPRuntimeException e) {
				//never happens
				out = null;
			} catch (HTTPPermissionDeniedException e) {
				out = (FORBIDDEN + errorHeaders()).getBytes();
			}
		}
		return out;
	}

	//helper method to check URI validity
	private boolean isCorrectURI(String token) {
		URI uri;
		try {
			uri = new URI(token);
		} catch (URISyntaxException e) {
			//badly formatted uri
			return false;
		}
		//we don't care about absolutes
		if(uri.isAbsolute()) {
			return false;
		}
		return true;
	}
	
	//search in byte arrays
	private int indexOf(byte[] data, byte[] pattern) {
		int[] failure = computeFailure(pattern);

		int j = 0;
		if (data.length == 0) return -1;

		for (int i = 0; i < data.length; i++) {
			while (j > 0 && pattern[j] != data[i]) {
				j = failure[j - 1];
			}
			if (pattern[j] == data[i]) { j++; }
			if (j == pattern.length) {
				return i + 1;
			}
		}
		return -1;
	}
	private int[] computeFailure(byte[] pattern) {
		int[] failure = new int[pattern.length];

		int j = 0;
		for (int i = 1; i < pattern.length; i++) {
			while (j > 0 && pattern[j] != pattern[i]) {
				j = failure[j - 1];
			}
			if (pattern[j] == pattern[i]) {
				j++;
			}
			failure[i] = j;
		}

		return failure;
	}

	//error header provider
	private String errorHeaders() {
		String now = DateUtils.formatDate(new Date());
		StringBuilder out = new StringBuilder();
		out.append("Date: " + now + "\r\n");
		out.append("Last-Modified:" + now + "\r\n");
		out.append("Content-Length: 0\r\n");
		out.append("Content-Type: text/plain\r\n\r\n");
		return out.toString();
	}

}
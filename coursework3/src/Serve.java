import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.http.client.utils.DateUtils;


public class Serve implements IServe {
	
	private static final String HTTP_OK = "HTTP/1.0 200 OK\r\n";
	private static final String CREATED = "HTTP/1.0 201 Created\r\n";
	private static final String NOT_MODIFIED = "HTTP/1.0 304 Not Modified\r\n";
	private static final String BAD_REQUEST = "HTTP/1.0 400 Bad Request\r\n";
	private static final String FORBIDDEN = "HTTP/1.0 403 Forbidden\r\n";
	private static final String NOT_FOUND = "HTTP/1.0 404 Not Found\r\n";

	private static final int CREATED_CODE = 201;

	private static Path basePath;

	@Override
	public byte[] httpGet(String requestURI) throws HTTPFileNotFoundException,
			HTTPRuntimeException, HTTPPermissionDeniedException {
		File file = getFile(requestURI);
		byte[] out;
		//no need to check if the file exists as simpleGet would throw an exception in that case
		byte[] fileContent = simpleGet(requestURI);
		byte[] headerContent = forgeHeaders(200, file);
		//append byte arrays and return
		out = new byte[headerContent.length + fileContent.length];
		System.arraycopy(headerContent, 0, out, 0, headerContent.length);
		System.arraycopy(fileContent, 0, out, headerContent.length, fileContent.length);
		return out;
	}

	public byte[] simpleGet(String requestURI) throws HTTPFileNotFoundException,
			HTTPRuntimeException, HTTPPermissionDeniedException {
		File file = getFile(requestURI);
		byte[] out;
		//return file if it exists else throw an exception
		if (file.exists() && file.isFile()) {
			try {
				//syncing file access
				synchronized(this){
					out = Files.readAllBytes(file.toPath());
				}
			} catch (IOException e) {
				throw new HTTPPermissionDeniedException("Couldn't read file.",e);
			}
		} else if (file.exists() && file.isDirectory()) {
			throw new HTTPPermissionDeniedException("GETting directories is not allowed.");
		} else {
			throw new HTTPFileNotFoundException(requestURI);
		}
		return out;
	}

	@Override
	public byte[] httpGETconditional(String requestURI, Date ifModifiedSince)
			throws HTTPFileNotFoundException, HTTPRuntimeException,
			HTTPPermissionDeniedException {
		File file = getFile(requestURI);
		if (file.exists() && file.isFile()) {
			Date fileDate = new Date(file.lastModified());
			//easy and ugly way of dropping milliseconds as this is redundant for comparison purposes
			Calendar dropMillisec = new GregorianCalendar();
			dropMillisec.setTime(fileDate);
			dropMillisec.set(Calendar.MILLISECOND,0);
			fileDate = dropMillisec.getTime();
			if(ifModifiedSince.before(fileDate)) {
				return httpGet(requestURI);
			} else {
				//304 if file was not modified
				return forgeHeaders(304,file);	
			}
		} else if (file.exists() && file.isDirectory()) {
			throw new HTTPPermissionDeniedException("GETting directories is not allowed.");
		} else {
			throw new HTTPFileNotFoundException(requestURI);
		}
	}

	@Override
	public byte[] httpHEAD(String requestURI) throws HTTPFileNotFoundException,
			HTTPRuntimeException, HTTPPermissionDeniedException {
		byte[] out;
		File file = getFile(requestURI);
		if (file.exists() && file.isFile()) {
			out = forgeHeaders(200,file);
		} else if (file.exists() && file.isDirectory()) {
			//forbidden to HEAD directories
			out = forgeHeaders(403, file);
		} else {
			out = forgeHeaders(404,file);
		}
		return out;
	}

	@Override
	public byte[] httpPOST(String requestURI, byte[] postData)
			throws HTTPFileNotFoundException, HTTPRuntimeException,
			HTTPPermissionDeniedException {
		File file = getFile(requestURI);
		byte[] out = new byte[1];
		if (file.exists() && file.isFile()) {
			//GET if it already exists
			out = httpGet(requestURI);
		} else if (file.exists() && file.isDirectory()) {
			//can't POST to directories
			out = forgeHeaders(400, file);
		} else {
			try {
				//create file
				Path path = file.toPath();
				//syncing file access
				synchronized(this){
					Files.createDirectories(path.getParent());
					Files.write(path,postData);
				}
				out = forgeHeaders(201,file);
			} catch (IOException e) {
				throw new HTTPPermissionDeniedException("Couldn't write file.",e);
			}
		}
		return out;
	}

	//method for setting the basePath
	public static void setBasePath(String basePath) {
		Serve.basePath = Paths.get(basePath);
	}
	
	private byte[] forgeHeaders(int statusCode, File file) throws HTTPPermissionDeniedException {
		StringBuilder out = new StringBuilder();
		boolean isError = (statusCode + "").substring(0, 1).equals("4");
		boolean hasContent = !isError || statusCode != CREATED_CODE;
		switch (statusCode) {
			case 200:
				out.append(HTTP_OK);
				break;
			case 201:
				out.append(CREATED);
				break;
			case 304:
				out.append(NOT_MODIFIED);
				break;
			case 400:
				out.append(BAD_REQUEST);
				break;
			case 403:
				out.append(FORBIDDEN);
				break;
			case 404:
				out.append(NOT_FOUND);
				break;
			default:
				return null;
		}
		String now = DateUtils.formatDate(new Date());
		out.append("Date: " + now + "\r\n");
		//now if error otherwise file date
		out.append("Last-Modified:" + (isError ? now : DateUtils.formatDate(new Date(file.lastModified()))) + "\r\n");
		//0 if error otherwise file size
		out.append("Content-Length: " + (!hasContent ? 0 : file.length()) + "\r\n");
		String guessedContentType;
		if (isError) {
			guessedContentType = "text/plain";			
		} else {
			try {
				guessedContentType = Files.probeContentType(file.toPath());
				//octet-stream if above method couldn't guess Content-Type
				if (guessedContentType == null) {
					guessedContentType = "application/octet-stream";
				}
			} catch (IOException e) {
				throw new HTTPPermissionDeniedException("Couldn't probe content type", e);
			}
		}
		out.append("Content-Type: " + guessedContentType + "\r\n\r\n");
		return out.toString().getBytes();
	}

	//convert requestURI to a File
	private File getFile(String requestURI) {
		Path reqPath = Paths.get(requestURI.replaceFirst("^/", ""));
		Path path = basePath.resolve(reqPath);
		return path.toFile();
	}

}

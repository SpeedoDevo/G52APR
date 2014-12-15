// make sure to add JUnit to your project to run this class.
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/*
 * 3 tests are provided for you, these WILL be part of the marking criteria for functionality tests.
 * It's a good idea to make sure they pass, and add some more tests here.
 * You should not modify the existing tests.
 */
public class RequestHandlerTests {

	private static final String OK = "HTTP/1.0 200 OK\r\n";
	private static final String CREATED = "HTTP/1.0 201 Created\r\n";
	private static final String NOT_MODIFIED = "HTTP/1.0 304 Not Modified\r\n";
	private static final String BAD_REQUEST = "HTTP/1.0 400 Bad Request\r\n";
	private static final String FORBIDDEN = "HTTP/1.0 403 Forbidden\r\n";
	private static final String NOT_FOUND = "HTTP/1.0 404 Not Found\r\n";

	@BeforeClass
	public static void setupServe(){
		Serve.setBasePath("C:\\work\\eclipse\\www");
	}
	
	@Test
	public void getDirectory() {
		String request = "GET / HTTP/1.0\r\n\r\n";
		RequestHandler requestHandler = new RequestHandler();
		String expected = FORBIDDEN;
		String actual = new String(requestHandler.processRequest(request.getBytes()));
		Assert.assertTrue(actual.contains(expected));
	}
	
	@Test
	public void getFileNoHeaders() {
		String request = "GET /index.html HTTP/1.0\r\n\r\n";
		RequestHandler requestHandler = new RequestHandler();
		String expected = OK;
		String actual = new String(requestHandler.processRequest(request.getBytes()));
		Assert.assertTrue(actual.contains(expected));
	}
	
	@Test
	public void getFileNotFoundNoHeaders() {
		String request = "GET /bar.html HTTP/1.0\r\n\r\n";
		RequestHandler requestHandler = new RequestHandler();
		String expected = NOT_FOUND;
		String actual = new String(requestHandler.processRequest(request.getBytes()));
		Assert.assertTrue(actual.contains(expected));
	}
	
	@Test
	public void getFileModified() {
		String request = "GET /index.html HTTP/1.0\r\nIf-Modified-Since: Mon, 10 Nov 2014 21:53:30 GMT\r\n";
		RequestHandler requestHandler = new RequestHandler();
		String expected = OK;
		String actual = new String(requestHandler.processRequest(request.getBytes()));
		Assert.assertTrue(actual.contains(expected));
	}
	
	@Test
	public void getFileNotModified() {
		String request = "GET /index.html HTTP/1.0\r\nIf-Modified-Since: Mon, 10 Nov 2014 21:53:40 GMT\r\n";
		RequestHandler requestHandler = new RequestHandler();
		String expected = NOT_MODIFIED;
		String actual = new String(requestHandler.processRequest(request.getBytes()));
		Assert.assertTrue(actual.contains(expected));
	}
	
	@Test
	public void getFileNotFoundConditional() {
		String request = "GET /foo.html HTTP/1.0\r\nIf-Modified-Since: Mon, 10 Nov 2014 21:53:30 GMT\r\n";
		RequestHandler requestHandler = new RequestHandler();
		String expected = NOT_FOUND;
		String actual = new String(requestHandler.processRequest(request.getBytes()));
		Assert.assertTrue(actual.contains(expected));
	}
	
	@Test
	public void getDirectoryConditional() {
		String request = "GET / HTTP/1.0\r\nIf-Modified-Since: Mon, 10 Nov 2014 21:53:30 GMT\r\n";
		RequestHandler requestHandler = new RequestHandler();
		String expected = FORBIDDEN;
		String actual = new String(requestHandler.processRequest(request.getBytes()));
		Assert.assertTrue(actual.contains(expected));
	}

	@Test
	public void getProxy() {
		String request = "GET http://goo.gl/ HTTP/1.0";
		RequestHandler requestHandler = new RequestHandler();
		String expected = BAD_REQUEST;
		String actual = new String(requestHandler.processRequest(request.getBytes()));
		Assert.assertTrue(actual.contains(expected));
	}

	@Test
	public void get09Directory() {
		String request = "GET /";
		RequestHandler requestHandler = new RequestHandler();
		String expected = FORBIDDEN;
		String actual = new String(requestHandler.processRequest(request.getBytes()));
		Assert.assertTrue(actual.contains(expected));
	}

	@Test
	public void get09File() {
		String request = "GET /index.html";
		RequestHandler requestHandler = new RequestHandler();
		String expected = OK;
		String actual = new String(requestHandler.processRequest(request.getBytes()));
		Assert.assertTrue(!actual.contains(expected)); //NOT contains header
	}

	@Test
	public void get09FileNotFound() {
		String request = "GET /foo.html";
		RequestHandler requestHandler = new RequestHandler();
		String expected = NOT_FOUND;
		String actual = new String(requestHandler.processRequest(request.getBytes()));
		Assert.assertTrue(actual.contains(expected));
	}

	@Test
	public void get11Directory() {
		String request = "GET / HTTP/1.1";
		RequestHandler requestHandler = new RequestHandler();
		String expected = BAD_REQUEST;
		String actual = new String(requestHandler.processRequest(request.getBytes()));
		Assert.assertTrue(actual.contains(expected));
	}
	
	@Test
	public void get11File() {
		String request = "GET /index.html HTTP/1.1";
		RequestHandler requestHandler = new RequestHandler();
		String expected = BAD_REQUEST;
		String actual = new String(requestHandler.processRequest(request.getBytes()));
		Assert.assertTrue(actual.contains(expected));
	}
	
	@Test
	public void get11NotFound() {
		String request = "GET /foo.html HTTP/1.1";
		RequestHandler requestHandler = new RequestHandler();
		String expected = BAD_REQUEST;
		String actual = new String(requestHandler.processRequest(request.getBytes()));
		Assert.assertTrue(actual.contains(expected));
	}
	
	@Test
	public void headDirectory() {
		String request = "HEAD / HTTP/1.0\r\n\r\n";
		RequestHandler requestHandler = new RequestHandler();
		String expected = FORBIDDEN;
		String actual = new String(requestHandler.processRequest(request.getBytes()));
		Assert.assertTrue(actual.contains(expected));
	}

	@Test
	public void headFile() {
		String request = "HEAD /index.html HTTP/1.0\r\n\r\n";
		RequestHandler requestHandler = new RequestHandler();
		String expected = OK;
		String actual = new String(requestHandler.processRequest(request.getBytes()));
		Assert.assertTrue(actual.contains(expected));
	}
	
	@Test
	public void headNotFound() {
		String request = "HEAD /foo.html HTTP/1.0\r\n\r\n";
		RequestHandler requestHandler = new RequestHandler();
		String expected = NOT_FOUND;
		String actual = new String(requestHandler.processRequest(request.getBytes()));
		Assert.assertTrue(actual.contains(expected));
	}

	@Test
	public void headDirectoryConditional() {
		String request = "HEAD / HTTP/1.0\r\nIf-Modified-Since: Mon, 10 Nov 2014 21:53:30 GMT\r\n";
		RequestHandler requestHandler = new RequestHandler();
		String expected = FORBIDDEN;
		String actual = new String(requestHandler.processRequest(request.getBytes()));
		Assert.assertTrue(actual.contains(expected));
	}

	@Test
	public void headFileConditional() {
		String request = "HEAD /index.html HTTP/1.0\r\nIf-Modified-Since: Mon, 10 Nov 2014 21:53:30 GMT\r\n";
		RequestHandler requestHandler = new RequestHandler();
		String expected = OK;
		String actual = new String(requestHandler.processRequest(request.getBytes()));
		Assert.assertTrue(actual.contains(expected));
	}

	@Test
	public void headNotFoundConditional() {
		String request = "HEAD /foo.html HTTP/1.0\r\nIf-Modified-Since: Mon, 10 Nov 2014 21:53:30 GMT\r\n";
		RequestHandler requestHandler = new RequestHandler();
		String expected = NOT_FOUND;
		String actual = new String(requestHandler.processRequest(request.getBytes()));
		Assert.assertTrue(actual.contains(expected));
	}
		
	@Test
	public void rabble() {
		String request = "Rabble rabble";
		RequestHandler requestHandler = new RequestHandler();
		String expected = BAD_REQUEST;
		String actual = new String(requestHandler.processRequest(request.getBytes()));
		Assert.assertTrue(actual.contains(expected));
	}
		
	@Test
	public void empty() {
		String request = "";
		RequestHandler requestHandler = new RequestHandler();
		String expected = BAD_REQUEST;
		String actual = new String(requestHandler.processRequest(request.getBytes()));
		Assert.assertTrue(actual.contains(expected));
	}	
		
	@Test
	public void postDirectory() {
		String request = "POST / HTTP/1.0";
		RequestHandler requestHandler = new RequestHandler();
		String expected = BAD_REQUEST;
		String actual = new String(requestHandler.processRequest(request.getBytes()));
		Assert.assertTrue(actual.contains(expected));
	}
		
	@Test
	public void postFile() {
		String request = "POST /test/somefile.txt HTTP/1.0\r\nContent-Length: 23\r\n\r\nsome arbitrary data\r\n\r\n";
		RequestHandler requestHandler = new RequestHandler();
		String expected = CREATED;
		String actual = new String(requestHandler.processRequest(request.getBytes()));
		Assert.assertTrue(actual.contains(expected));
	}
		
	@Test
	public void postFileExists() {
		String request = "POST /index.html HTTP/1.0\r\nContent-Length: 23\r\n\r\nsome arbitrary data\r\n\r\n";
		String expectedContent = "<title>Stephen Nutbrown's Home Page</title>";
		RequestHandler requestHandler = new RequestHandler();
		String expected = OK;
		String actual = new String(requestHandler.processRequest(request.getBytes()));
		Assert.assertTrue(actual.contains(expected) && actual.contains(expectedContent));
	}

	@Test
	public void postFileCreation() {
		RequestHandler requestHandler = new RequestHandler();
		String req1 = "HEAD /test/foo.html HTTP/1.0\r\n\r\n";
		String expected1 = NOT_FOUND;
		String actual1 = new String(requestHandler.processRequest(req1.getBytes()));

		requestHandler = new RequestHandler();
		String req2 = "POST /test/foo.html HTTP/1.0\r\nContent-Length: 23\r\n\r\nsome arbitrary data\r\n\r\n";
		String expected2 = CREATED;
		String actual2 = new String(requestHandler.processRequest(req2.getBytes()));

		requestHandler = new RequestHandler();
		String req3 = "GET /test/foo.html HTTP/1.0\r\n\r\n";
		String expected3 = OK;
		String expectedContent = "some arbitrary data";
		String actual3 = new String(requestHandler.processRequest(req3.getBytes()));
		
		Assert.assertTrue(actual1.contains(expected1) && actual2.contains(expected2) && actual3.contains(expected3) && actual3.contains(expectedContent));
	}

	@AfterClass
	public static void tearDown(){
		try {
			removeRecursive(Paths.get("C:\\work\\eclipse\\www\\test"));
		} catch (IOException e) {
			System.out.println("shit happens");
		}
	}

	public static void removeRecursive(Path path) throws IOException {
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc)
					throws IOException {
				// try to delete the file anyway, even if its attributes
				// could not be read, since delete-only access is
				// theoretically possible
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc)
					throws IOException {
				if (exc == null) {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				} else {
					// directory iteration failed; propagate exception
					throw exc;
				}
			}
		});
	}

}
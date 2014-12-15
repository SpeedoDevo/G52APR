import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Assert;
import org.junit.Test;

public class ClientUnitTests {

	@Test
	public void testCorrectGet() {
		G52APRClient client = new G52APRClient();
		String expected = "{\n  \"origin\": \"86.3.97.52\"\n}\n";
		String actual = client.httpGet("http://httpbin.org/ip");
		Assert.assertEquals(expected, actual);
	}
	@Test
	public void testInvalidUrl() {
		G52APRClient client = new G52APRClient();
		String expected = "Something nasty happened.";
		String actual = client.httpGet("rabble");
		Assert.assertEquals(expected, actual);
	}
	@Test
	public void testErrorGet() {
		G52APRClient client = new G52APRClient();
		String expected = "HTTP/1.1 404 NOT FOUND";
		String actual = client.httpGet("http://httpbin.org/status/404");
		Assert.assertEquals(expected, actual);
	}
	@Test
	public void testCorrectPost1() {
		G52APRClient client = new G52APRClient();
		String expected = "{\n  \"args\": {}, \n  \"data\": \"someRandomData\", \n  \"files\": {}, \n  \"form\": {}, \n  \"headers\": {\n    \"Accept-Encoding\": \"gzip,deflate\", \n    \"Connect-Time\": \"1\", \n    \"Connection\": \"close\", \n    \"Content-Length\": \"14\", \n    \"Content-Type\": \"text/plain; charset=ISO-8859-1\", \n    \"Host\": \"httpbin.org\", \n    \"Total-Route-Time\": \"0\", \n    \"User-Agent\": \"Apache-HttpClient/4.3.5 (java 1.5)\", \n    \"Via\": \"1.1 vegur\", \n    \"X-Request-Id\": \"48d36c36-74db-4c5a-b7b3-e872ed9f6b15\"\n  }, \n  \"json\": null, \n  \"origin\": \"86.3.97.52\", \n  \"url\": \"http://httpbin.org/post\"\n}\n";
		String actual = client.httpPost("http://httpbin.org/post", "someRandomData");
		Assert.assertEquals(expected.substring(0, 50), actual.substring(0, 50));
	}
	@Test
	public void testCorrectPost2() {
		G52APRClient client = new G52APRClient();
		String expected = "{\n  \"args\": {}, \n  \"data\": \"\", \n  \"files\": {}, \n  \"form\": {\n    \"Email1\": \"youremail\", \n    \"Email2\": \"youremail\", \n    \"Email3\": \"youremail\", \n    \"Email4\": \"youremail\", \n    \"Email5\": \"youremail\"\n  }, \n  \"headers\": {\n    \"Accept-Encoding\": \"gzip,deflate\", \n    \"Connect-Time\": \"2\", \n    \"Connection\": \"close\", \n    \"Content-Length\": \"84\", \n    \"Content-Type\": \"application/x-www-form-urlencoded; charset=ISO-8859-1\", \n    \"Host\": \"httpbin.org\", \n    \"Total-Route-Time\": \"0\", \n    \"User-Agent\": \"Apache-HttpClient/4.3.5 (java 1.5)\", \n    \"Via\": \"1.1 vegur\", \n    \"X-Request-Id\": \"1011747d-fa22-45b8-83fb-1ca75a212163\"\n  }, \n  \"json\": null, \n  \"origin\": \"86.3.97.52\", \n  \"url\": \"http://httpbin.org/post\"\n}\n";
		ArrayList<NameValuePair> dummyNVP = new ArrayList<NameValuePair>(5);
		dummyNVP.add(new BasicNameValuePair("Email1", "youremail"));
		dummyNVP.add(new BasicNameValuePair("Email2", "youremail"));
		dummyNVP.add(new BasicNameValuePair("Email3", "youremail"));
		dummyNVP.add(new BasicNameValuePair("Email4", "youremail"));
		dummyNVP.add(new BasicNameValuePair("Email5", "youremail"));
		String actual = client.httpPost("http://httpbin.org/post", dummyNVP);
		Assert.assertEquals(expected.substring(0, 50), actual.substring(0, 50));
	}
	@Test
	public void testErrorPost1() {
		G52APRClient client = new G52APRClient();
		String expected = "HTTP/1.1 404 NOT FOUND";
		String actual = client.httpPost("http://httpbin.org/status/404", "someRandomData");
		Assert.assertEquals(expected, actual);
	}
	@Test
	public void testErrorPost2() {
		G52APRClient client = new G52APRClient();
		String expected = "HTTP/1.1 404 NOT FOUND";
		ArrayList<NameValuePair> dummyNVP = new ArrayList<NameValuePair>(5);
		dummyNVP.add(new BasicNameValuePair("Email1", "youremail"));
		dummyNVP.add(new BasicNameValuePair("Email2", "youremail"));
		dummyNVP.add(new BasicNameValuePair("Email3", "youremail"));
		dummyNVP.add(new BasicNameValuePair("Email4", "youremail"));
		dummyNVP.add(new BasicNameValuePair("Email5", "youremail"));
		String actual = client.httpPost("http://httpbin.org/status/404", dummyNVP);
		Assert.assertEquals(expected, actual);
	}

	
}

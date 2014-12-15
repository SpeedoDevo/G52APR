import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class ClientTests {

	public static void main(String[] args) {
		ClientTests tests = new ClientTests(new G52APRClient());
		tests.runTests("http://localhost:8080/index.html");
	}
	
	G52APRClient client;

	public ClientTests(G52APRClient client) {
		this.client = client;
	}

	public void runTests(String url) {
		//fetch the content of the url
		String index = client.httpGet(url);
		//parse it with jsoup
		Document doc = Jsoup.parse(index, url);
		//select anchors with links
		Elements links = doc.select("a[href]");
		for (Element link : links) {
			//get the link
			String href = link.attr("abs:href");
			//fetch the head of that link
			HttpResponse res = client.httpFullHead(href);
			//further parse it or return broken link if it's anything other than 200
			if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				//if it contains a Last-Modified then compare it
				if (res.containsHeader("Last-Modified")) {
					String dateString = res.getFirstHeader("Last-Modified").getValue();
					//parsing Last-Modified
					LocalDate modified = LocalDate.parse(dateString, DateTimeFormatter.RFC_1123_DATE_TIME);
					LocalDate now = LocalDate.now();
					if (modified.isAfter(now.minusMonths(6))) {
						//get and print html headers
						System.out.print(getHtmlHeaders(href, dateString));
					} else {
						System.out.println(href + "\nPage modified over 6 months ago.\n");
					}
				} else {
					System.out.print(getHtmlHeaders(href, "No Last-Modified header."));
				}
			} else {
				System.out.println(href + "\nThis is a broken link.\n");
			}
		}
	}

	private String getHtmlHeaders(String url, String date) {
		String html = client.httpGet(url);
		Document doc = Jsoup.parse(html, url);
		//specification only suggested to use h1, h2 and h3
		Elements headers = doc.select("h1, h2, h3");
		StringBuilder out = new StringBuilder();
		out.append(url + "\n" + date + "\n");
		for (Element header : headers) {
			out.append(header.text()+"\n");
		}
		out.append("\n");
		return out.toString();
	}

}

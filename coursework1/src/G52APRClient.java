import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;


public class G52APRClient implements IG52APRClient {
	
	private final String ERROR_RESPONSE = "Something nasty happened.";

	public String httpGet(String url){
		String res = null;
		try {
			// using the fluent API as we don't need that much flexibility
			// and this way I don't have to deal with closing resources, etc..
			res = Request.Get(url)
				.version(HttpVersion.HTTP_1_0)
				.execute().handleResponse(new ResponseHandler<String>() {
					public String handleResponse(final HttpResponse res) {
						//created a new method as everything except httpHead() only returns the body
						return returnBody(res);
					}
				});
		} catch (IOException e) {
			res = ERROR_RESPONSE;
			e.printStackTrace();
		}
		return res;
	}

	public String httpHead(String url) {
		String res = null;
		try {
			res = Request.Head(url)
				.version(HttpVersion.HTTP_1_0)
				.execute().handleResponse(new ResponseHandler<String>() {
					public String handleResponse(final HttpResponse res) {
						StatusLine status = res.getStatusLine();
						// only returns headers on a 200
						if (status.getStatusCode() == HttpStatus.SC_OK) {
							Header[] headers = res.getAllHeaders();
							StringBuilder builder = new StringBuilder();
							//StringBuilder and foreach to build return value
							for (Header header : headers) {
								builder.append(header.getName() + ": " + header.getValue() + "\n");
							}
							return builder.toString();
						} else {
							return status.toString();
						}
					}
				});
		} catch (IOException e) {
			res = ERROR_RESPONSE;
			e.printStackTrace();
		}
		return res;
	}

	public HttpResponse httpFullHead(String url) {
		HttpResponse res = null;
		try {
			res = Request.Head(url)
				.version(HttpVersion.HTTP_1_0)
				.execute().returnResponse();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

	public String httpPost(String url, String body) {
		String res = null;
		try {
			res = Request.Post(url)
				.version(HttpVersion.HTTP_1_0)
				//assumed that we have to send the string this way
				.bodyString(body, ContentType.DEFAULT_TEXT)
				.execute().handleResponse(new ResponseHandler<String>() {
					public String handleResponse(final HttpResponse res) {
						return returnBody(res);
					}
				});
		} catch (IOException e) {
			res = ERROR_RESPONSE;
			e.printStackTrace();
		}
		return res;
	}


	public String httpPost(String url, ArrayList<NameValuePair> nameValuePairs) {
		String res = null;
		try {
			res = Request.Post(url)
				.version(HttpVersion.HTTP_1_0)
				//assumed that we have to send the namevaluepairs as a form
				//since there was a built-in method that takes this type
				.bodyForm(nameValuePairs)
				.execute().handleResponse(new ResponseHandler<String>() {
					public String handleResponse(final HttpResponse res) {
						return returnBody(res);
					}
				});
		} catch (IOException e) {
			res = ERROR_RESPONSE;
			e.printStackTrace();
		}
		return res;
	}
	
	private String returnBody(HttpResponse res){
		StatusLine status = res.getStatusLine();
		HttpEntity entity = res.getEntity();
		//check for status code
		if (status.getStatusCode() == HttpStatus.SC_OK) {
			try {
				//convert stream
				return streamToString(entity.getContent());
			} catch (IOException ie) {
				ie.printStackTrace();
			} catch (IllegalStateException ise) {
				ise.printStackTrace();
			}
		} else {
			//return the status line
			return status.toString();
		}
		return null;
	}
	
	//straightforward helper method to convert streams to strings
	private String streamToString(InputStream stream) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		StringBuilder out = new StringBuilder();
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				out.append(line + '\n');
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out.toString();
	}

}

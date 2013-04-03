package de.androidpraxis.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.net.Uri;

public class NetworkUtility {

	private HttpClient httpClient = null;
	private NetworkUtilityMessageHandler messageHandler = null;

	public NetworkUtility(NetworkUtilityMessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}

	private HttpClient httpClient() {
		if (httpClient == null) {
			httpClient = new DefaultHttpClient();
		}
		return httpClient;
	}

	private URI convertUri(Uri uri) throws URISyntaxException {
		URI result;
		result = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(),
				uri.getQuery(), uri.getFragment());
		return result;
	}

	public String loadText(Uri uri) {
		String result = "";

		try {
			HttpGet get = new HttpGet(convertUri(uri));
			HttpResponse response = httpClient().execute(get);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = response.getEntity();
				InputStream is = entity.getContent();
				long clen = entity.getContentLength();
				String encoding = "iso-8859-1";
				if (entity.getContentEncoding() != null) {
					encoding = entity.getContentEncoding().getValue();
				}
				if (clen > 0) {
					byte[] buffer = new byte[(int) clen];
					is.read(buffer);
					result = new String(buffer);
				} else {
					byte[] buffer = new byte[10 * 1024];
					long read = 0;
					do {
						read = is.read(buffer);
						if (read > 0) {
							result += new String(buffer, 0, (int) read, encoding);
						}
					} while (read >= 0);
				}

			} else {
				if (messageHandler != null)
					messageHandler.onError(response.getStatusLine());
			}
		} catch (ClientProtocolException e) {

			if (messageHandler != null) {
				messageHandler.onException(e);
			}

		} catch (IOException e) {

			if (messageHandler != null)
				messageHandler.onException(e);

		} catch (URISyntaxException e) {
			if (messageHandler != null)
				messageHandler.onException(e);
		}

		return result;
	}
}

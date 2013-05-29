package com.alexandru.tigaeru.android.mydictapp.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.AndroidHttpClient;

/**
 * 
 * @author Alexandru_Tigaeru
 *
 */
public class NetworkUtils {
	public static boolean isNetworkAvailable(Activity mActivity) {
		ConnectivityManager cm = (ConnectivityManager) mActivity
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		// if no network is available networkInfo will be null
		// otherwise check if we are connected
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}

	// Given a URL, establishes an HttpUrlConnection and retrieves
	// the web page content as a InputStream, which it returns as a string.
	public static String downloadUrl(String myurl) throws IOException {
		InputStream is = null;
		// Only display the first 5000 characters of the retrieved
		// web page content.
		int len = 5000;

		try {
			URL url = new URL(myurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			// String contentType = conn.getContentType();
			// String encoding = contentType.contains("utf-8") ? "utf-8" :"UTF-8";
			// String encoding = "UTF-8";
			conn.setReadTimeout(10000 /* milliseconds */);
			conn.setConnectTimeout(15000 /* milliseconds */);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			// Starts the query
			conn.connect();
			// int response = conn.getResponseCode();
			// Log.d(DEBUG_TAG, "The response is: " + response);
			is = conn.getInputStream();

			// Convert the InputStream into a string
			String contentAsString = readIt(is, len);
			conn.disconnect();
			return contentAsString;

			// Makes sure that the InputStream is closed after the app is
			// finished using it.
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	// Reads an InputStream and converts it to a String.
	private static String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
		Reader reader = null;
		reader = new InputStreamReader(stream, "UTF-8");
		char[] buffer = new char[len];
		reader.read(buffer);
		return new String(buffer);
	}

	public static String loadText(Uri uri) {
		String result = "";
		InputStream is = null;
		AndroidHttpClient httpClient = null;
		try {
			HttpGet get = new HttpGet(convertUri(uri));
			httpClient = AndroidHttpClient.newInstance("firefox");
			HttpResponse response = httpClient.execute(get);

			HttpEntity entity = response.getEntity();
			is = entity.getContent();
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

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} finally {
			if (httpClient != null) {
				httpClient.close();
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return result;
	}

	private static URI convertUri(Uri uri) throws URISyntaxException {
		URI result;
		result = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(),
				uri.getQuery(), uri.getFragment());
		return result;
	}
}

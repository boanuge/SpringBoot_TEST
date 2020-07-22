package com.example.servicebroker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequest implements Runnable {

	private String url = null;
	private String method = null;
	private String json = null;

	/**
	 * @param String url
	 * @param String method
	 * @param String json
	 */
	public HttpRequest(String url, String method, String json) {

		this.url = url;
		this.method = method;
		this.json = json;
	}

	@Override
	public void run() {

		System.out.println("Connecting: " + url);
		System.out.println(json);

		if (sendRequestMessage(url, method, json)) {
			System.out.println("Sending the message has been successfully.");
		} else {
			System.out.println("Sending the message has been failed.");
		}
	}

	/**
	 * This method send a request method with JSON body.
	 * 
	 * @param String url
	 * @param String method
	 * @param String json
	 * 
	 * @return boolean true, if the message has sent successfully
	 */
	public boolean sendRequestMessage(String url, String method, String json) {

		boolean hasSent = false;

		try {
			if (json != null) {
				URL targetUrl = new URL(url);
				HttpURLConnection con = (HttpURLConnection) targetUrl.openConnection();
				con.setRequestMethod(method);
				con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
				con.setRequestProperty("Accept", "application/json");
				con.setDoOutput(true);

				OutputStream outStream = con.getOutputStream();
				byte[] input = json.getBytes("UTF-8");
				outStream.write(input, 0, input.length);
				outStream.flush(); //The request has been sent

				int status = con.getResponseCode(); //The response has been received

				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
				StringBuilder responseString = new StringBuilder();
				responseString.append("HTTP response status: " + status + "\n");
				responseString.append("HTTP response body:\n");
				String responseLine = null;
				while ((responseLine = bufferedReader.readLine()) != null) {
					responseString.append(responseLine.trim());
				}
				System.out.println(responseString.toString());
				bufferedReader.close();
				con.disconnect();

				hasSent = true;
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return hasSent;
	}
}

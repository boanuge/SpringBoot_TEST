package com.example.servicebroker.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.example.servicebroker.common.CommonServiceLog;

public class Thread4HttpRequestMessage implements Runnable {

	private String url = null;
	private String method = null;
	private String json = null;

	/**
	 * @param String url
	 * @param String method
	 * @param String json
	 */
	public Thread4HttpRequestMessage(String url, String method, String json) {

		this.url = url;
		this.method = method;
		this.json = json;
	}

	@Override
	public void run() {

		CommonServiceLog.logInfo("Connecting: " + url);
		System.out.println(json);

		if (sendRequestMessage(url, method, json)) {
			CommonServiceLog.logInfo("Sending the message has been successful.");
		} else {
			CommonServiceLog.logInfo("Sending the message has been failed.");
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
			if (json == null) json = "";

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
			StringBuilder responseBuffer = new StringBuilder();
			String responseLine = null;
			while ((responseLine = bufferedReader.readLine()) != null) {
				responseBuffer.append(responseLine);
			}
			responseString.append(responseBuffer);
			System.out.println(responseString.toString());
			bufferedReader.close();
			con.disconnect();

			hasSent = true;

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return hasSent;
	}
}

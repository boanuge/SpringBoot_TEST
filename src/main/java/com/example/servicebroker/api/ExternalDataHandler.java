package com.example.servicebroker.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.servicebroker.ModelInfo;
import com.example.servicebroker.ServiceBrokerApplication;
import com.example.servicebroker.ServiceInfo;
import com.example.servicebroker.common.CommonServiceLog;
import com.example.servicebroker.db.DatabaseManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@CrossOrigin(origins = "*")
@RestController
public class ExternalDataHandler {

	//Create a singleton
	private static ExternalDataHandler instance = new ExternalDataHandler();

	public static ExternalDataHandler getInstance() {
		return instance;
	}

	@RequestMapping(value = "/broker/v1/service", method = RequestMethod.POST)
	public ResponseEntity<String> handleExternalRequest(HttpServletRequest request, @RequestBody ObjectNode jsonReceived) {

		CommonServiceLog.logInfo("Service → Service Broker, POST: " + request.getServletPath());

		boolean isThereServiceID = checkServiceID(jsonReceived);

		if(!isThereServiceID) { //for the service creation

			String service_state = null;
			String device_ip = null;
			String device_priority = null;
			String model_desc = null;
			try {
				//Parsing JSON Nodes using keys
				service_state = jsonReceived.get("service_state").asText();
				device_ip = jsonReceived.get("device_ip").asText();
				device_priority = jsonReceived.get("device_priority").asText();
				model_desc = jsonReceived.get("model_info").asText();

				//Parsing JSON Array Node (not including keys)
				/*
				String model_info = "";
				Iterator<JsonNode> jsonArrayNode = jsonReceived.get("model_info").elements();
				while (jsonArrayNode.hasNext() == true) {
					model_info += jsonArrayNode.next().asText() + " ";
				}
				 */

			} catch (Exception error) {
				error.printStackTrace();
				//In the case, there is JSON Key-name typo-error
				CommonServiceLog.logInfo("Exception: for parsing JSON Nodes");
				CommonServiceLog.logInfo("Service Broker → Service, Response(400): " + request.getServletPath());
				return new ResponseEntity<String>(jsonParsingError(), HttpStatus.BAD_REQUEST);
			}

			CommonServiceLog.logInfo("service_state = " + service_state);
			CommonServiceLog.logInfo("device_ip = " + device_ip);
			CommonServiceLog.logInfo("device_priority = " + device_priority);
			CommonServiceLog.logInfo("model_info: " + model_desc);

			//fill the class ServiceInfo, and add to the list listServiceInfo
			ServiceInfo serviceInfo = new ServiceInfo();
			serviceInfo.setService_id(ServiceBrokerApplication.getInstance().generateServiceID());
			serviceInfo.setService_state(service_state);
			serviceInfo.setDevice_ip(device_ip);
			serviceInfo.setDevice_priority(device_priority);
			serviceInfo.setModel_desc(model_desc);

			//ToDo:Need to send a request to Scheduler to receive device_id
			serviceInfo.setDevice_id("DEV1234"); //ToDo:TEST:OK for any service create request
			/*
			String url4DeviceID = Config.httpUrl4DeviceID;
			String method4DeviceID = "POST";
			String json4DeviceID = generateJSON4ModelID(serviceInfo);
			CommonServiceLog.logInfo("Connecting: " + url4DeviceID);
			System.out.println(json4DeviceID);

			JsonNode jsonNode4DeviceID = sendRequestMessage(url4DeviceID, method4DeviceID, json4DeviceID);
			if (jsonNode4DeviceID != null) {
				CommonServiceLog.logInfo("Sending the message has been successful.");
			} else {
				CommonServiceLog.logInfo("Sending the message has been failed.");
			}

			try {
				//Parsing JSON Nodes using keys
				serviceInfo.setDevice_id(jsonNode4DeviceID.get("device_id").asText());
			} catch (Exception error) { //Service Unavailable
				error.printStackTrace();
				//In the case, there is JSON Key-name typo-error
				CommonServiceLog.logInfo("Exception: for parsing the JSON Node(device_id)");
				CommonServiceLog.logInfo("Service Broker → Service, Response(503): " + request.getServletPath());
				return new ResponseEntity<String>(jsonParsingError(), HttpStatus.SERVICE_UNAVAILABLE);
			}
			 */

			CommonServiceLog.logInfo("Process: received device_id = " + serviceInfo.getDevice_id());

			//ToDo:Need to send a request to Model Manager to receive model_id
			serviceInfo.setModel_id("M1234"); //ToDo:TEST:OK for any service create request

			CommonServiceLog.logInfo("Process: received model_id = " + serviceInfo.getModel_id());

			ServiceBrokerApplication.getInstance().getListServiceInfo().add(serviceInfo);

			//fill the class ModelInfo, and add to the list listModelInfo
			ModelInfo modelInfo = new ModelInfo();
			modelInfo.setModel_id(serviceInfo.getModel_id());
			if (serviceInfo.getService_state().equalsIgnoreCase("create") || serviceInfo.getService_state().equalsIgnoreCase("stop")) {
				modelInfo.setModel_state("stop");
			} else {
				modelInfo.setModel_state("start");
			}
			modelInfo.setModel_desc(serviceInfo.getModel_desc());

			boolean isThere_model_id = false;
			for (int i = 0; i < ServiceBrokerApplication.getInstance().getListModelInfo().size(); i++) {
				if (ServiceBrokerApplication.getInstance().getListModelInfo().get(i).getModel_id().equals(modelInfo.getModel_id())) {
					ServiceBrokerApplication.getInstance().getListModelInfo().set(i, modelInfo);
					isThere_model_id = true; //ToDo:Error:There needs to be no existing model_id in the list
					break;
				}
			}
			if (!isThere_model_id) { //if the model_id is new one
				ServiceBrokerApplication.getInstance().getListModelInfo().add(modelInfo);
			}

			//Save the ModelInfo into the database: Creating a document for model_info
			DatabaseManager.getInstance().saveModelInfo(modelInfo);

			//Save the ServiceInfo into the database
			DatabaseManager.getInstance().saveServiceInfo(serviceInfo);

			HttpHeaders responseHeaders = new HttpHeaders();

			MediaType mediaType = MediaType.APPLICATION_JSON;
			responseHeaders.setContentType(mediaType);

			CommonServiceLog.logInfo("Service Broker → Service, Response(201): " + request.getServletPath());
			CommonServiceLog.logInfo("Response: created service_id = " + serviceInfo.getService_id());
			return new ResponseEntity<String>(jsonKeyValue("service_id", serviceInfo.getService_id()), HttpStatus.CREATED);

		} else { //for the service start or stop

			String service_id = null;
			String service_state = null;
			try {
				//Parsing JSON Nodes using keys
				service_id = jsonReceived.get("service_id").asText();
				service_state = jsonReceived.get("service_state").asText();

			} catch (Exception error) {
				error.printStackTrace();
				//In the case, there is JSON Key-name typo-error
				CommonServiceLog.logInfo("Exception: for parsing JSON Nodes");
				CommonServiceLog.logInfo("Service Broker → Service, Response(400): " + request.getServletPath());
				return new ResponseEntity<String>(jsonParsingError(), HttpStatus.BAD_REQUEST);
			}

			CommonServiceLog.logInfo("service_id = " + service_id);
			CommonServiceLog.logInfo("service_state = " + service_state);

			//update the class ServiceInfo and the list listServiceInfo
			ServiceInfo serviceInfo = null;

			//check corresponding model_id
			boolean isThere_service_id = false;
			List<ServiceInfo> listServiceInfo = ServiceBrokerApplication.getInstance().getListServiceInfo();
			for (int i = 0; i < listServiceInfo.size(); i++) {
				if (listServiceInfo.get(i).getService_id().equals(service_id)) {
					listServiceInfo.get(i).setService_state(service_state);
					serviceInfo = listServiceInfo.get(i);
					isThere_service_id = true;
					break;
				}
			}

			if (isThere_service_id) { //if there is a created service

				//fill the class ModelInfo, and add to the list listModelInfo
				ModelInfo modelInfo = new ModelInfo();
				modelInfo.setModel_id(serviceInfo.getModel_id());
				if (serviceInfo.getService_state().equalsIgnoreCase("create") || serviceInfo.getService_state().equalsIgnoreCase("stop")) {
					modelInfo.setModel_state("stop");
				} else {
					modelInfo.setModel_state("start");
				}
				modelInfo.setModel_desc(serviceInfo.getModel_desc());

				boolean isThere_model_id = false;
				for (int i = 0; i < ServiceBrokerApplication.getInstance().getListModelInfo().size(); i++) {
					if (ServiceBrokerApplication.getInstance().getListModelInfo().get(i).getModel_id().equals(modelInfo.getModel_id())) {
						ServiceBrokerApplication.getInstance().getListModelInfo().set(i, modelInfo);
						isThere_model_id = true; //ToDo:Error:There needs to be no existing model_id in the list
						break;
					}
				}
				if (!isThere_model_id) { //if the model_id is new one
					ServiceBrokerApplication.getInstance().getListModelInfo().add(modelInfo);
				}

				//Save the ModelInfo into the database: Updating a document for model_info
				DatabaseManager.getInstance().saveModelInfo(modelInfo);

				//Save the ServiceInfo into the database
				DatabaseManager.getInstance().saveServiceInfo(serviceInfo);

				HttpHeaders responseHeaders = new HttpHeaders();

				MediaType mediaType = MediaType.APPLICATION_JSON;
				responseHeaders.setContentType(mediaType);

				CommonServiceLog.logInfo("Service Broker → Service, Response(200): " + request.getServletPath());
				return new ResponseEntity<String>(jsonKeyValue("result", "successful"), HttpStatus.OK);
			} else {
				//In the case, request has no correspoding service_id
				CommonServiceLog.logInfo("Error: request has no correspoding service_id");
				CommonServiceLog.logInfo("Service Broker → Service, Response(400): " + request.getServletPath());
				return new ResponseEntity<String>(jsonKeyValue("error_reason", "no correspoding service_id"), HttpStatus.BAD_REQUEST);
			}
		}
	}

	private String generateJSON4ModelID(ServiceInfo serviceInfo) {

		String jsonString = null;
		ObjectMapper jsonMapper = new ObjectMapper();
		Map<String, Object> jsonData = new HashMap<String, Object>();
		jsonData.put("device_ip", serviceInfo.getDevice_ip());
		jsonData.put("device_priority", serviceInfo.getDevice_priority());

		try {
			jsonString = jsonMapper.writeValueAsString(jsonData);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return jsonString;
	}

	//if there is no service_id, then this is for the service creation
	private boolean checkServiceID(ObjectNode jsonReceived) {

		//Get a JSON Node using a key
		JsonNode temp = jsonReceived.get("service_id");

		if (temp != null) {
			return true;
		} else {
			return false;
		}
	}

	//In the case, there is JSON Key-name typo-error
	private String jsonParsingError() {
		String error_reason_key = "error_reason";
		String error_reason_value = "JSON Parsing Error";
		String jsonString = null;
		ObjectMapper jsonMapper = new ObjectMapper();
		Map<String, String> jsonData = new HashMap<String, String>();
		jsonData.put(error_reason_key, error_reason_value);
		try {
			jsonString = jsonMapper.writeValueAsString(jsonData);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return jsonString;
	}

	private String jsonKeyValue(String key, String value) {
		String jsonString = null;
		ObjectMapper jsonMapper = new ObjectMapper();
		Map<String, String> jsonData = new HashMap<String, String>();
		jsonData.put(key, value);
		try {
			jsonString = jsonMapper.writeValueAsString(jsonData);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return jsonString;
	}

	/**
	 * This method send a request method with JSON body.
	 * 
	 * @param String url
	 * @param String method
	 * @param String json
	 * 
	 * @return JsonNode received json body
	 */
	public JsonNode sendRequestMessage(String url, String method, String json) {

		JsonNode jsonNode = null;
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

			//Convert JSON String into JsonNode using ObjectMapper
			ObjectMapper jsonMapper = new ObjectMapper();
			jsonNode = jsonMapper.readTree(responseBuffer.toString());

			hasSent = true;

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		if (hasSent) {
			return jsonNode;
		} else {
			return null;
		}
	}
}

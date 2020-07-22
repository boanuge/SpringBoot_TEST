package com.example.servicebroker.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.servicebroker.Config;
import com.example.servicebroker.ModelInfo;
import com.example.servicebroker.ModelResult;
import com.example.servicebroker.ServiceBrokerApplication;
import com.example.servicebroker.ServiceInfo;
import com.example.servicebroker.common.CommonServiceLog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@CrossOrigin(origins = "*")
@RestController
public class InternalDataHandler {

	@SuppressWarnings("unused")
	@RequestMapping(value = "/broker/v1/result", method = RequestMethod.PUT)
	public ResponseEntity<String> handleInternalRequest(HttpServletRequest request, @RequestBody ObjectNode jsonReceived) {

		CommonServiceLog.logInfo("Model Manager → Service Broker, PUT: " + request.getServletPath());

		String model_id = null;
		String device_id = null;
		String timestamp = null;
		String byte_mime = null;
		JsonNode result_info = null;
		//String result_info = null;
		//String result_info_age = null;
		//String result_info_gender = null;
		try {
			//Parsing JSON Nodes using keys
			model_id = jsonReceived.get("model_id").asText();
			device_id = jsonReceived.get("device_id").asText();
			timestamp = jsonReceived.get("timestamp").asText();
			byte_mime = jsonReceived.get("byte_mime").asText();

			//Parsing JSON Object Node
			/*
			result_info = null;
			result_info_age = jsonReceived.get("result_info").get("age").asText();
			result_info_gender = jsonReceived.get("result_info").get("gender").asText();
			result_info = "age = " + result_info_age + ", gender = " + result_info_gender;
			 */
			result_info = jsonReceived.get("result_info");

		} catch (Exception error) {
			error.printStackTrace();
			//In the case, there is JSON Key-name typo-error
			CommonServiceLog.logInfo("Exception: for parsing JSON Nodes");
			CommonServiceLog.logInfo("Service Broker → Model Manager, Response(400): " + request.getServletPath());
			return new ResponseEntity<String>(jsonParsingError(), HttpStatus.BAD_REQUEST);
		}

		CommonServiceLog.logInfo("model_id = " + model_id);
		CommonServiceLog.logInfo("device_id = " + device_id);
		CommonServiceLog.logInfo("timestamp = " + timestamp);
		CommonServiceLog.logInfo("byte_mime = " + byte_mime);
		CommonServiceLog.logInfo("result_info: " + result_info);

		//fill the class ModelResult, and add to the list listModelResult in the corresponding ModelInfo
		ModelResult modelResult = null;

		/*
		if (result_info_age != null && result_info_gender != null) {
			modelResult = new ModelResult4AgeGender();
			modelResult.setTimestamp(timestamp);
			modelResult.setDevice_id(device_id);
			modelResult.setModel_id(model_id);
			modelResult.setByte_mime(byte_mime);
			((ModelResult4AgeGender)modelResult).setResult_info_age(result_info_age);
			((ModelResult4AgeGender)modelResult).setResult_info_gender(result_info_gender);
		 */
		if (result_info != null) {
			modelResult = new ModelResult();
			modelResult.setTimestamp(timestamp);
			modelResult.setDevice_id(device_id);
			modelResult.setModel_id(model_id);
			modelResult.setByte_mime(byte_mime);
			modelResult.setResult_info(result_info);

		} else {
			//In the case, there is not enough result data
			CommonServiceLog.logInfo("Error: for not enough result data");
			CommonServiceLog.logInfo("Service Broker → Model Manager, Response(400): " + request.getServletPath());
			return new ResponseEntity<String>(jsonKeyValue("error_reason", "not enough result data"), HttpStatus.BAD_REQUEST);
		}

		if (modelResult != null) {

			//check corresponding model_id
			boolean isThere_model_id = false;
			List<ModelInfo> listModelInfo = ServiceBrokerApplication.getInstance().getListModelInfo();
			for (int i = 0; i < listModelInfo.size(); i++) {
				if (listModelInfo.get(i).getModel_id().equals(modelResult.getModel_id())) {
					//check number of ModelResult to avoid memory error
					if (listModelInfo.get(i).getListModelResult().size() > 100) {
						listModelInfo.get(i).getListModelResult().remove(0);
					} else {
						listModelInfo.get(i).getListModelResult().add(modelResult);
					}
					isThere_model_id = true;
					break;
				}
			}

			//ToDo:TEST: OK for the case: "model_id":"M1234"
			if (!isThere_model_id && model_id.equals("M1234")) {
				ModelInfo modelInfo = null;
				boolean isThere_already = false;
				for (ModelInfo tempModelInfo : ServiceBrokerApplication.getInstance().getListModelInfo()) {
					if (tempModelInfo.getModel_id().equals("M1234")) {
						modelInfo = tempModelInfo;
						isThere_already = true;
					}
				}
				if (!isThere_already) {
					modelInfo = new ModelInfo();
					ServiceBrokerApplication.getInstance().getListModelInfo().add(modelInfo);
				}
				modelInfo.setModel_id(model_id);
				modelInfo.setModel_state("start");
				modelInfo.setModel_desc("age_gender");
				//check number of ModelResult to avoid memory error
				if (modelInfo.getListModelResult().size() > 100) {
					modelInfo.getListModelResult().remove(0);
				} else {
					modelInfo.getListModelResult().add(modelResult);
				}
				isThere_model_id = true;
			}

			if (isThere_model_id) {
				//Find service_id using model_id
				String service_id = null;
				for (ServiceInfo serviceInfo : ServiceBrokerApplication.getInstance().getListServiceInfo()) {
					if (serviceInfo.getModel_id().equals(modelResult.getModel_id())) {
						service_id = serviceInfo.getService_id();

						//Send the result for each service_id that matches to the model_id
						String jsonString = null;
						ObjectMapper jsonMapper = new ObjectMapper();
						Map<String, Object> jsonData = new HashMap<String, Object>();
						jsonData.put("service_id", service_id);
						jsonData.put("device_id", modelResult.getDevice_id());
						jsonData.put("timestamp", modelResult.getTimestamp());
						jsonData.put("byte_mime", modelResult.getByte_mime());
						if (!modelResult.getByte_mime().equalsIgnoreCase("none")) { //if "byte_mime":"none", then no "byte_key"
							jsonData.put("byte_key", modelResult.getModel_id() + "-" + modelResult.getTimestamp()); //Key-name: <Model ID> + "-" + <Timestamp>
						}

						//Get result_info for age_gender
						/*
						Map<String, String> jsonData4result_info = new HashMap<String, String>();
						jsonData4result_info.put("age", ((ModelResult4AgeGender)modelResult).getResult_info_age());
						jsonData4result_info.put("gender", ((ModelResult4AgeGender)modelResult).getResult_info_gender());
						jsonData.put("result_info", jsonData4result_info);
						 */
						jsonData.put("result_info", modelResult.getResult_info());

						try {
							jsonString = jsonMapper.writeValueAsString(jsonData);
						} catch (JsonProcessingException e) {
							e.printStackTrace();
						}

						String targetUrl = Config.httpUrl4ServiceResult;

						//The below code has been replaced to run by a thread
						/*
						CommonServiceLog.logInfo("Service Broker → Service, Connecting: " + Config.httpUrl4ServiceResult);
						System.out.println(jsonString);
						if (ExternalDataHandler.getInstance().sendRequestMessage(targetUrl, "POST", jsonString)) {
							CommonServiceLog.logInfo("Sending the message has been successful.");
						} else {
							CommonServiceLog.logInfo("Sending the message has been failed.");
						}
						 */

						CommonServiceLog.logInfo("Service Broker → Service, Connecting: " + Config.httpUrl4ServiceResult);

						Thread4HttpRequestMessage thread4HttpRequestMessage = new Thread4HttpRequestMessage(targetUrl, "POST", jsonString);
						Thread thread = new Thread(thread4HttpRequestMessage);
						thread.start();
					}
				}

				if (service_id == null) {
					CommonServiceLog.logInfo("Error: result has no correspoding service_id");
				}

				CommonServiceLog.logInfo("Service Broker → Model Manager, Response(200): " + request.getServletPath());
				return new ResponseEntity<String>(HttpStatus.OK);
			} else {
				//In the case, result has no correspoding model_id
				CommonServiceLog.logInfo("Error: result has no correspoding model_id");
				CommonServiceLog.logInfo("Service Broker → Model Manager, Response(400): " + request.getServletPath());
				return new ResponseEntity<String>(jsonKeyValue("error_reason", "no correspoding model_id"), HttpStatus.BAD_REQUEST);
			}
		} else {
			CommonServiceLog.logInfo("Service Broker → Model Manager, Response(501): " + request.getServletPath());
			return new ResponseEntity<String>(HttpStatus.NOT_IMPLEMENTED);
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
}

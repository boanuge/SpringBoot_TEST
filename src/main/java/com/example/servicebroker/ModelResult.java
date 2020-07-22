package com.example.servicebroker;

import com.fasterxml.jackson.databind.JsonNode;

public class ModelResult {

	private String model_id = null;

	private String timestamp = null;

	private String device_id = null;

	private String byte_mime = null;

	//this variable is used to replace ModelResult4AgeGender class
	private JsonNode result_info = null;

	public JsonNode getResult_info() {
		return result_info;
	}

	public void setResult_info(JsonNode result_info) {
		this.result_info = result_info;
	}

	public String getModel_id() {
		return model_id;
	}

	public void setModel_id(String model_id) {
		this.model_id = model_id;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getDevice_id() {
		return device_id;
	}

	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}

	public String getByte_mime() {
		return byte_mime;
	}

	public void setByte_mime(String byte_mime) {
		this.byte_mime = byte_mime;
	}
}

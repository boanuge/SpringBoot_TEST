package com.example.servicebroker;

public class ServiceInfo {

	private String service_id = null;

	private String service_state = null;

	private String device_ip = null;

	private String device_priority = null;

	private String model_desc = null;

	private String model_id = null;

	private String device_id = null;

	public String getDevice_id() {
		return device_id;
	}

	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}

	public String getModel_id() {
		return model_id;
	}

	public void setModel_id(String model_id) {
		this.model_id = model_id;
	}

	public String getService_id() {
		return service_id;
	}

	public void setService_id(String service_id) {
		this.service_id = service_id;
	}

	public String getService_state() {
		return service_state;
	}

	public void setService_state(String service_state) {
		this.service_state = service_state;
	}

	public String getDevice_ip() {
		return device_ip;
	}

	public void setDevice_ip(String device_ip) {
		this.device_ip = device_ip;
	}

	public String getDevice_priority() {
		return device_priority;
	}

	public void setDevice_priority(String device_priority) {
		this.device_priority = device_priority;
	}

	public String getModel_desc() {
		return model_desc;
	}

	public void setModel_desc(String model_desc) {
		this.model_desc = model_desc;
	}
}

package com.example.servicebroker;

import java.util.LinkedList;

public class ModelInfo {

	//The class ModelResult can be mapped to the collection <Model ID>
	private LinkedList<ModelResult> listModelResult = new LinkedList<ModelResult>();

	private String model_id = null;

	private String model_state = null;

	private String model_desc = null;

	public LinkedList<ModelResult> getListModelResult() {
		return listModelResult;
	}

	public String getModel_id() {
		return model_id;
	}

	public void setModel_id(String model_id) {
		this.model_id = model_id;
	}

	public String getModel_state() {
		return model_state;
	}

	public void setModel_state(String model_state) {
		this.model_state = model_state;
	}

	public String getModel_desc() {
		return model_desc;
	}

	public void setModel_desc(String model_desc) {
		this.model_desc = model_desc;
	}
}

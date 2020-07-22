package com.example.servicebroker;

import java.util.LinkedList;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.servicebroker.common.CommonServiceLog;
import com.example.servicebroker.db.DatabaseManager;

@SpringBootApplication(scanBasePackages = { "com.example.servicebroker" })
public class ServiceBrokerApplication {

	//Create a singleton
	private static ServiceBrokerApplication instance = new ServiceBrokerApplication();

	public static ServiceBrokerApplication getInstance() {
		return instance;
	}

	//public static final String httpUrl4ServiceResult = "http://192.168.0.11:8888/services/v1/service/result";
	//public static final String httpUrl4DeviceID = "http://192.168.0.11:8888/device";
	//public static final String httpUrl4ModelID = "http://192.168.0.11:8888/model";

	//The class ServiceInfo can be mapped to the collection "service_info"
	private LinkedList<ServiceInfo> listServiceInfo = new LinkedList<ServiceInfo>();

	//The class ModelInfo can be mapped to the collection "model_info"
	private LinkedList<ModelInfo> listModelInfo = new LinkedList<ModelInfo>();

	public LinkedList<ServiceInfo> getListServiceInfo() {
		return listServiceInfo;
	}

	public LinkedList<ModelInfo> getListModelInfo() {
		return listModelInfo;
	}

	public static void main(String[] args) {

		SpringApplication.run(ServiceBrokerApplication.class, args);

		//Check externally configed variables
		CommonServiceLog.logInfo("Config-file: application.properties");
		CommonServiceLog.logInfo("server.port = " + Config.port4ServiceBroker);
		CommonServiceLog.logInfo("rest.url.service = " + Config.httpUrl4ServiceResult);
		CommonServiceLog.logInfo("rest.url.device = " + Config.httpUrl4DeviceID);
		CommonServiceLog.logInfo("rest.url.model = " + Config.httpUrl4ModelID);
		CommonServiceLog.logInfo("db.mongo.use = " + Config.isThereMongoDB);

		//if true, then the application usees a mongo db to save/restore data
		DatabaseManager.getInstance().setUseDatabase(Config.isThereMongoDB);
		if (DatabaseManager.getInstance().getUseDatabase()) {
			CommonServiceLog.logInfo("Mongo DB is used.");
		} else {
			CommonServiceLog.logInfo("Mongo DB is not used.");
		}

		DatabaseManager.getInstance().connect();
		DatabaseManager.getInstance().printDatabase();

		DatabaseManager.getInstance().generateListServiceInfoFromDB();
		DatabaseManager.getInstance().generateListModelInfoFromDB();
	}

	//Generating a unique service id for a created service
	public String generateServiceID() {
		String prefix = "Service";
		StringBuilder stringBuilder = new StringBuilder(prefix);
		long currentTimeMillis = System.currentTimeMillis();
		return stringBuilder.append(currentTimeMillis).toString();
	}
}

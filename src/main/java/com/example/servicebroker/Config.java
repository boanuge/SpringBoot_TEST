package com.example.servicebroker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Config {

	public static int port4ServiceBroker;

	public static String httpUrl4ServiceResult;
	public static String httpUrl4DeviceID;
	public static String httpUrl4ModelID;

	public static boolean isThereMongoDB;

	public Config() { }

	@Value("${server.port}")
	public void setPort4ServiceBroker(int port4ServiceBroker) {
		Config.port4ServiceBroker = port4ServiceBroker;
	}

	@Value("${rest.url.service}")
	public void setHttpUrl4ServiceResult(String httpUrl4ServiceResult) {
		Config.httpUrl4ServiceResult = httpUrl4ServiceResult;
	}

	@Value("${rest.url.device}")
	public void setHttpUrl4DeviceID(String httpUrl4DeviceID) {
		Config.httpUrl4DeviceID = httpUrl4DeviceID;
	}

	@Value("${rest.url.model}")
	public void setHttpUrl4ModelID(String httpUrl4ModelID) {
		Config.httpUrl4ModelID = httpUrl4ModelID;
	}

	@Value("${db.mongo.use}")
	public void setIsThereMongoDB(boolean isThereMongoDB) {
		Config.isThereMongoDB = isThereMongoDB;
	}
}

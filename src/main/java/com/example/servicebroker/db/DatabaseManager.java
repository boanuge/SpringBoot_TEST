package com.example.servicebroker.db;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.bson.Document;

import com.example.servicebroker.ModelInfo;
import com.example.servicebroker.ModelResult;
import com.example.servicebroker.ServiceBrokerApplication;
import com.example.servicebroker.ServiceInfo;
import com.example.servicebroker.common.CommonServiceLog;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;

public class DatabaseManager {

	//Create a singleton
	private static DatabaseManager instance = new DatabaseManager();

	public static DatabaseManager getInstance() {
		return instance;
	}

	private boolean useDatabase = false;

	//Mongo DB Info for the connection
	private String db_ip = "192.168.0.10";
	private int db_port = 27017;
	private String db_name = "db_broker";

	//Mongo DB Info for the authorization
	private String db_user = "root";
	private char[] db_pw = "mypassword".toCharArray();

	//Mongo DB Schema
	private String col_service_info = "service_info";
	private String col_model_info = "model_info";

	private MongoDatabase database = null;

	public void setUseDatabase(boolean useDatabase) {
		this.useDatabase = useDatabase;
	}

	public boolean getUseDatabase() {
		return useDatabase;
	}

	public void connect() {

		if (useDatabase) { //to use mongo db

			if (database == null) {

				MongoCredential credential = MongoCredential.createCredential(db_user, db_name, db_pw);

				MongoClientSettings settings = MongoClientSettings.builder()
						.credential(credential)
						.applyToSslSettings(builder -> builder.enabled(false))
						.applyToClusterSettings(builder -> builder.hosts(Arrays.asList(new ServerAddress(db_ip, db_port))))
						.build();

				MongoClient mongoClient = MongoClients.create(settings);

				database = mongoClient.getDatabase(db_name);

				CommonServiceLog.logInfo("Mongo DB @ " + db_ip + ":" + db_port + " is connected.");
			}
		}
	}

	public void generateListServiceInfoFromDB() {

		if (useDatabase) { //to use mongo db

			boolean isThere_service_info = checkCollectionName(col_service_info);

			//if the collection service_info exists, then fill the list: listServiceInfo
			if (isThere_service_info) {
				CommonServiceLog.logInfo("Mongo DB has the collection: " + col_service_info);

				MongoCollection<Document> collection = database.getCollection(col_service_info);
				List<Document> findList = collection.find().into(new ArrayList<Document>());

				//get a document to add in the list
				String service_id = null;
				String service_state = null;
				String device_ip = null;
				String device_priority = null;
				String model_desc = null;
				String model_id = null;

				for (Document doc : findList) {

					//check error if any value is null
					boolean isThere_error = false;

					try {
						service_id = doc.get("service_id").toString();
						service_state = doc.get("service_state").toString();
						device_ip = doc.get("device_ip").toString();
						device_priority = doc.get("device_priority").toString();
						model_desc = doc.get("model_desc").toString();
						model_id = doc.get("model_id").toString();
					} catch (NullPointerException ne) {
						isThere_error = true;
						ne.printStackTrace();
					}

					CommonServiceLog.logInfo("Mongo DB Document Info: " + service_id + ", " + service_state + ", " + device_ip + ", " + device_priority + ", " + model_desc + ", " + model_id);

					if (!isThere_error) {

						//add document info into listServiceInfo
						ServiceInfo tempServiceInfo = new ServiceInfo();

						tempServiceInfo.setService_id(service_id);
						tempServiceInfo.setService_state(service_state);
						tempServiceInfo.setDevice_ip(device_ip);
						tempServiceInfo.setDevice_priority(device_priority);
						tempServiceInfo.setModel_desc(model_desc);
						tempServiceInfo.setModel_id(model_id);

						ServiceBrokerApplication.getInstance().getListServiceInfo().add(tempServiceInfo);
					}

					CommonServiceLog.logInfo("Mongo DB Document is addes in the list.size(" + ServiceBrokerApplication.getInstance().getListServiceInfo().size() + ")");
				}

			} else {
				CommonServiceLog.logInfo("Mongo DB does not have the collection: " + col_service_info);
			}
		}
	}

	public void generateListModelInfoFromDB() {

		if (useDatabase) { //to use mongo db

			boolean isThere_model_info = checkCollectionName(col_model_info);

			//if the collection model_info exists, then fill the list: listModelInfo
			if (isThere_model_info) {
				CommonServiceLog.logInfo("Mongo DB has the collection: " + col_model_info);

				MongoCollection<Document> collection = database.getCollection(col_model_info);
				List<Document> findList = collection.find().into(new ArrayList<Document>());

				//get a document to add in the list
				String model_id = null;
				String model_state = null;
				String model_desc = null;

				for (Document doc : findList) {

					//check error if any value is null
					boolean isThere_error = false;

					try {
						model_id = doc.get("model_id").toString();
						model_state = doc.get("model_state").toString();
						model_desc = doc.get("model_desc").toString();
					} catch (NullPointerException ne) {
						isThere_error = true;
						ne.printStackTrace();
					}

					CommonServiceLog.logInfo("Mongo DB Document Info: " + model_id + ", " + model_state + ", " + model_desc);

					if (!isThere_error) {
						/*
					//get JSON array value for the key "model_desc"
					List<String> model_desc_list = new ArrayList<String>();

					@SuppressWarnings("unchecked")
					List<Document> elements = (ArrayList<Document>)doc.get("model_desc");
					for (int i = 0; i < elements.size(); i++) {
						//toString() is not used, because of cast exception: org.bson.Document
						model_desc_list.add(String.valueOf(elements.get(i)));
					}
						 */

						//add document info into listModelInfo
						ModelInfo tempModelInfo = new ModelInfo();

						tempModelInfo.setModel_id(model_id);
						tempModelInfo.setModel_state(model_state);
						tempModelInfo.setModel_desc(model_desc);;

						ServiceBrokerApplication.getInstance().getListModelInfo().add(tempModelInfo);

						//ToDo:Need to generate ModelResult class to add in ModelInfo from the collection <Model ID>
					}

					CommonServiceLog.logInfo("Mongo DB Document is addes in the list.size(" + ServiceBrokerApplication.getInstance().getListModelInfo().size() + ")");
				}

			} else {
				CommonServiceLog.logInfo("Mongo DB does not have the collection: " + col_model_info);
			}
		}
	}

	//Save the class ServiceInfo into the database
	public void saveServiceInfo(ServiceInfo serviceInfo) {

		if (useDatabase) { //to use mongo db

			MongoCollection<Document> collection = database.getCollection(col_service_info);

			//Create a document from the class ServiceInfo
			Document newDocument = new Document();
			newDocument.put("service_id", serviceInfo.getService_id());
			newDocument.put("service_state", serviceInfo.getService_state());
			newDocument.put("device_ip", serviceInfo.getDevice_ip());
			newDocument.put("device_priority", serviceInfo.getDevice_priority());
			newDocument.put("model_desc", serviceInfo.getModel_desc());
			newDocument.put("model_id", serviceInfo.getModel_id());
			newDocument.put("device_id", serviceInfo.getDevice_id());

			//Query a document using service_id
			Document query = new Document();
			query.append("service_id", serviceInfo.getService_id());

			//Update or Upsert(= insert if there is no match) the document
			@SuppressWarnings("deprecation")
			UpdateResult result = collection.replaceOne(query, newDocument, (new UpdateOptions()).upsert(true));
			CommonServiceLog.logInfo("Mongo DB Update Matched Count: " + result.getMatchedCount());
			CommonServiceLog.logInfo("Mongo DB Update Modified Count: " + result.getModifiedCount());
			CommonServiceLog.logInfo("Mongo DB Update Upserted Count: " + ((result.getUpsertedId() != null) ? "1" : "0"));
		}
	}

	//Save the class ModelInfo into the database
	public void saveModelInfo(ModelInfo modelInfo) {

		if (useDatabase) { //to use mongo db

			MongoCollection<Document> collection = database.getCollection(col_model_info);

			//Create a document from the class ServiceInfo
			Document newDocument = new Document();
			newDocument.put("model_id", modelInfo.getModel_id());
			newDocument.put("model_state", modelInfo.getModel_state());
			newDocument.put("model_desc", modelInfo.getModel_desc());

			//Query a document using model_id
			Document query = new Document();
			query.append("model_id", modelInfo.getModel_id());

			//Update or Upsert(= insert if there is no match) the document
			@SuppressWarnings("deprecation")
			UpdateResult result = collection.replaceOne(query, newDocument, (new UpdateOptions()).upsert(true));
			CommonServiceLog.logInfo("Mongo DB Update Matched Count: " + result.getMatchedCount());
			CommonServiceLog.logInfo("Mongo DB Update Modified Count: " + result.getModifiedCount());
			CommonServiceLog.logInfo("Mongo DB Update Upserted Count: " + ((result.getUpsertedId() != null) ? "1" : "0"));
		}
	}

	//ToDo:Save the class ModelResult into the collection <Model ID>
	public void saveModelResult(ModelResult modelResult) {

		if (useDatabase) { //to use mongo db

		}
	}

	public void printDatabase() {

		if (useDatabase) { //to use mongo db

			for (String col_name : database.listCollectionNames()) {
				System.out.println("\n[Mongo DB Collection Name]: " + col_name);
				printDocuments(col_name);
			}
		}
	}

	public void printDocuments(String col_name) {

		if (useDatabase) { //to use mongo db

			MongoCollection<Document> collection = database.getCollection(col_name);
			List<Document> findList = collection.find().into(new ArrayList<Document>());

			for (Document doc : findList) {
				System.out.println(doc.toJson());
			}
		}
	}

	//Create a given collection name if not exists
	public boolean createCollectionName(String col_name) {

		boolean isSuccessful = false;

		try {
			database.createCollection(col_name);
			isSuccessful = true;
		} catch (MongoCommandException me) {
			//In the case, the collection already exists
			CommonServiceLog.logInfo(me.getErrorMessage());
		}

		return isSuccessful;
	}

	//Check whether there is a given collection in the db
	public boolean checkCollectionName(String col_name) {

		boolean isThere = false;

		for (String name : database.listCollectionNames()) {
			if (name.equals(col_name)) {
				isThere = true;
			}
		}

		return isThere;
	}

	//Timestamp format: <Timestamp: YYYYMMDD-HHMMSS-Millisecond>
	public String convertCurrentTimeMillis2Timestamp() {

		String tempTime = null;

		long currentTimeMillis = System.currentTimeMillis();

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");

		Date dateResult = new Date(currentTimeMillis);

		tempTime = simpleDateFormat.format(dateResult);

		return tempTime;
	}

	//Timestamp format: <Timestamp: YYYYMMDD-HHMMSS-Millisecond>
	public String convertTimeMillis2Timestamp(long timeMillis) {

		String tempTime = null;

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");

		Date dateResult = new Date(timeMillis);

		tempTime = simpleDateFormat.format(dateResult);

		return tempTime;
	}
}

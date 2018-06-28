package edu.uclm.esi.disoft.comandas.dao;

import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bson.BsonDocument;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class MongoBroker {
	private final String databaseName;
	private MongoClient client;
	private MongoDatabase database;
	
	private MongoBroker() {
		this.databaseName = "telecomandas";
		this.client=new MongoClient();
		if (!exists())
			createDatabase();
		this.database=this.client.getDatabase(databaseName);
	}
	
	private void createDatabase() {
		try {
			FileInputStream f=new FileInputStream("C:\\Users\\Juan\\git\\comandas\\telecomandas\\src\\main\\webapp\\recursos\\creacionTelecomandas.js");
			byte[] b=new byte [f.available()];
			f.read(b);
			f.close();
			String textoScript=new String(b);
			this.database=this.client.getDatabase(databaseName);
			this.database.runCommand(new BasicDBObject("eval", textoScript));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean exists() {
		MongoCursor<String> dbsCursor = client.listDatabaseNames().iterator();
		while(dbsCursor.hasNext()) {
		    if (dbsCursor.next().equals(this.databaseName))
		    	return true;
		}
		return false;
	}

	private static class MongoBrokerHolder {
		static MongoBroker singleton=new MongoBroker();
	}
	
	public static MongoBroker get() {
		return MongoBrokerHolder.singleton;
	}
	
	public MongoCollection<BsonDocument> getCollection(String name) {
		return this.database.getCollection(name, BsonDocument.class);
	}
}

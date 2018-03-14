package edu.uclm.esi.disoft.comandas.etiquetas;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.BsonDocument;
import org.bson.BsonValue;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import edu.uclm.esi.disoft.comandas.dao.MongoBroker;

public class BSONeador {
	public static ConcurrentHashMap<Object, Object> load(Class<?> clase) throws Exception {
		ConcurrentHashMap<Object, Object> result=new ConcurrentHashMap<>();
		MongoCollection<BsonDocument> collection=MongoBroker.get().getCollection(clase.getName());
		MongoCursor<BsonDocument> fi=collection.find().iterator();
		while(fi.hasNext()) {
			BsonDocument bso=fi.next();
			Object objeto=getObject(clase, bso);
			result.put(getId(bso), objeto);
		}
		return result;
	}

	private static Object getId(BsonDocument bso) {
		if(bso.get("_id").isString())
			return bso.get("_id").asString().getValue();
		if(bso.get("_id").isInt32())
			return bso.get("_id").asInt32().getValue();
		return null;
	}

	private static Object getObject(Class<?> clase, BsonDocument bso) throws Exception {
		Object result=clase.newInstance(); //Mesa result=new Mesa() pero de forma reflexiva
		Iterator<String> nombresDeLosCampos = bso.keySet().iterator();
		while(nombresDeLosCampos.hasNext()) {
			String nombreDeCampo=nombresDeLosCampos.next();
			Field campo=clase.getDeclaredField(nombreDeCampo);
			if (campo==null)
				continue;
			campo.setAccessible(true);
			BsonValue valorDelBson=bso.get(nombreDeCampo);
			set(campo, result, valorDelBson);
			
		}
		return result;
	}

	private static void set(Field campo, Object result, BsonValue valorDelBson) throws Exception {
		if(valorDelBson.isString()) {
			campo.set(result, valorDelBson.asString().getValue()); //result.campo="...";
			return;
		}
		if(valorDelBson.isDouble()) {
			campo.setDouble(result, valorDelBson.asDouble().getValue());
			return;
		}
	}
}

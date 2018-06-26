package edu.uclm.esi.disoft.comandas.etiquetas;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.BsonArray;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonObjectId;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import edu.uclm.esi.disoft.comandas.dao.MongoBroker;
import edu.uclm.esi.disoft.comandas.dominio.Mesa;

public class BSONeador {
	
	public static void insert(Object objeto) throws Exception {
		/*
		 * 1. Traducir objeto a bso
		 * 1.1 Por reflexión, leer cada campo del objeto, leer su valor y colocarlo en bso
		 * 2. Acceder a una colección que se llame igual que la clase del objeto
		 * 3. A través del MongoBroker, insertar el bso que se acaba de construir en la colección
		 */
		Class<?> clase=objeto.getClass();
		Field[] campos=clase.getDeclaredFields();
		BsonDocument bso=new BsonDocument();
		for(int i=0; i<campos.length; i++) {
			Field campo=campos[i];
			campo.setAccessible(true);
			Object valor=campo.get(objeto);
			if(valor==null)
				continue;
			BSONable anotacion = campo.getAnnotation(BSONable.class);
			if(anotacion==null)
				bso.append(campo.getName(), getBsonValue(valor));
			else {
				String nombreCampoAsociado=anotacion.campo(); // "_id"
				String nombreNuevo=anotacion.nombre(); // "idPlato"
				Field campoAsociado=valor.getClass().getDeclaredField(nombreCampoAsociado);
				campoAsociado.setAccessible(true);
				Object valorCampoAsociado=campoAsociado.get(valor);
				bso.append(nombreNuevo, getBsonValue(valorCampoAsociado));
			}
		}
		MongoCollection<BsonDocument> collection=MongoBroker.get().getCollection(clase.getSimpleName());
		collection.insertOne(bso);
	}
	
	private static BsonValue getBsonValue(Object valorDelCampo) throws Exception {
		Class<? extends Object> tipo = valorDelCampo.getClass();
		if(tipo==int.class || tipo==Integer.class)
			return new BsonInt32((int)valorDelCampo);
		if(tipo==long.class || tipo==Long.class)
			return new BsonInt64((long)valorDelCampo);
		if(tipo==boolean.class || tipo== Boolean.class)
			return new BsonBoolean((boolean) valorDelCampo);
		if(tipo==double.class || tipo==Double.class)
			return new BsonDouble((double)valorDelCampo);
		if(tipo==String.class)
			return new BsonString(valorDelCampo.toString());
		if(tipo.isAnnotationPresent(BSONable.class)) {
			Field[] campos = valorDelCampo.getClass().getDeclaredFields();
			BsonDocument bso = new BsonDocument();
			for(int i=0; i<campos.length; i++) {
				Field campo = campos[i];
				campo.setAccessible(true);
				bso.put(campo.getName(), getBsonValue(campo.get(valorDelCampo)));
			}
			return bso;
		}
		if(tipo==Vector.class) {
			Vector vector = (Vector)valorDelCampo;
			BsonArray bsov = new BsonArray();
			for(int i=0;i<vector.size();i++) {
				BsonDocument bso = new BsonDocument();
				Object objeto = vector.get(i);
				Field[] campos = objeto.getClass().getDeclaredFields();
				for(int j=0;j<campos.length;j++) {
					Field campo = campos[j];
					campo.setAccessible(true);
					bso.put(campo.getName(), getBsonValue(campo.get(objeto)));
				}
				bsov.add(bso);
			}
			return bsov;
		}
		return null;
	}
	
	public static ConcurrentHashMap<Object, Object> load(Class<?> clase, Object... parametros) throws InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException {
		BsonDocument criterio=new BsonDocument();
		ConcurrentHashMap<Object, Object> result = new ConcurrentHashMap<>();
		MongoCollection<BsonDocument> coleccion = MongoBroker.get().getCollection(clase.getSimpleName());
		String valor=""; String buscador=""; 
		if(parametros.length > 0) {
			buscador = parametros[0].toString();
			valor = parametros[1].toString();
			criterio.put(buscador, new BsonObjectId(new ObjectId(valor)));
		}
		MongoCursor<BsonDocument> fi = coleccion.find(criterio).iterator(); //añadimos criterio creado con los parametros que pasamos separados por comas
		while(fi.hasNext()) {
			BsonDocument bso = fi.next();
			Object objeto = getObject(clase,bso); //una vez una comanda, otra vez un plato, otra vez mesa
			result.put(getId(bso), objeto);
		}
		return result;
	}

	private static Object getId(BsonDocument bso) {
		if(bso.get("_id").isString())
			return bso.get("_id").asString().getValue();
		if(bso.get("_id").isInt32())
			return bso.get("_id").asInt32().getValue();
		if(bso.get("_id").isObjectId()){
			return bso.get("_id").asObjectId().getValue().toHexString();
		}
		return null;
	}

	private static Object getObject(Class<?> clase, BsonDocument bso) throws InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Object result=clase.newInstance(); //Mesa result=new Mesa() pero de forma reflexiva
		Iterator<String> nombresDeLosCampos = bso.keySet().iterator(); //set de elementos del bson
		while(nombresDeLosCampos.hasNext()) {
			String nombreDeCampo=nombresDeLosCampos.next();
			Field campo=clase.getDeclaredField(nombreDeCampo);
			if (campo==null) //si no existe paso de él
				continue;
			campo.setAccessible(true);
			BsonValue valorDelBson=bso.get(nombreDeCampo);
			set(campo, result, valorDelBson);
		}
		return result;
	}
	
	private static void set(Field campo, Object result, BsonValue valorDelBson) throws IllegalArgumentException, IllegalAccessException {
		if(valorDelBson.isString()) {
			campo.set(result, valorDelBson.asString().getValue()); //result.campo="...";
			return;
		}
		if(valorDelBson.isDouble()) {
			campo.setDouble(result, valorDelBson.asDouble().getValue());
			return;
		}
		if(valorDelBson.isInt32()){
			campo.set(result, valorDelBson.asInt32().getValue());
			return;
		}
		if(valorDelBson.isObjectId()){
			campo.set(result, valorDelBson.asObjectId().getValue().toHexString());
			return;
		}
		if(valorDelBson.isBoolean()){
			campo.set(result, valorDelBson.asBoolean().getValue());
			return;
		}
	}
	
	private static void insert(Mesa mesa) {
		try {
			if(mesa.getComandaActual().get_id() == null) {
				ObjectId id = new ObjectId();
				mesa.getComandaActual().set_id(id.toHexString());
				BSONeador.insert(mesa.getComandaActual());
			} else
				BSONeador.update(mesa.getComandaActual());
		} catch (Exception e){
			e.printStackTrace();
		}	
	}
	
	public static void update(Object objeto) throws Exception { //Object[] pasamos varios arrays separados por comas
		Class<?> clase = objeto.getClass();
		Field [] campos = clase.getDeclaredFields(); //antes un campo, ahora array con todos los que queremos actualizar
		BsonDocument bso = new BsonDocument(); 
		
		for(int i=0; i<campos.length; i++) {
			Field campo = campos[i];
			campo.setAccessible(true);
			Object valor = campo.get(objeto);
			if (valor==null) //pasamos de él
				continue;
			BSONable update = campo.getAnnotation(BSONable.class);
			if (update == null)
				bso.append(campo.getName(), getBsonValue(valor));
			else {
				String nombreNuevo = update.nombre();
				String nombreCampoAsociado = update.campo();
				Field campoAsociado = valor.getClass().getDeclaredField(nombreCampoAsociado);
				campoAsociado.setAccessible(true);
				Object valorCampoAsociado = campoAsociado.get(valor);
				bso.append(nombreNuevo, getBsonValue(valorCampoAsociado)); //transformamos nombres de valores a bson
			}
		}
		bso.remove("_id");
		Field campoId = clase.getDeclaredField("_id");
		campoId.setAccessible(true);
		Object valorId = campoId.get(objeto);
		BsonDocument criterio = new BsonDocument();
		criterio.put("_id", getBsonValue(valorId));
		MongoCollection<BsonDocument> collection = MongoBroker.get().getCollection(clase.getSimpleName());
		collection.replaceOne(criterio, bso);
	}
	
	private static void delete(Object objeto) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, Exception {
		Class<?> clase = objeto.getClass();
		Field campoId=clase.getDeclaredField("_id");
		campoId.setAccessible(true);
		Object valorId=campoId.get(objeto);
		MongoCollection<BsonDocument> coleccion = MongoBroker.get().getCollection(clase.getName());
		coleccion.deleteOne(new BsonDocument("_id", getBsonValue(valorId)));
		
		BSONable anotacion=clase.getAnnotation(BSONable.class);
		if(anotacion==null)
			return;
		String nombreClaseDependiente=anotacion.claseDependiente();
		Class<?> claseDependiente=Class.forName(nombreClaseDependiente); //coger la clase del string que tenemos en la etiqueta
		Vector<Field> camposDependientes=findCampos(claseDependiente, clase);
		if(camposDependientes==null)
			return;
		/*estos son los valores que he recuperado con el fin:
		 * @BSONable(campo = "_id", nombre = "idPlato", OnDeleteCascade=true) // crear borrado en cascada, ej examen
		 * private Plato plato;
		 */
		for(Field campoDependiente : camposDependientes) {
			/*	1. coger la anotacion, ver si tiene el ondeletecascade a true.
			*	2. Si la tiene, leer valor "nombre" de la anotación (Dará "idPlato")
			*	3. Ir a la colección PlatoPedido y hacer delete de todos los objetos
			*	4. Cuyo idPlato seal el _id del objeto principal (parámetro objeto)*/
		}
	}

	private static Vector<Field> findCampos(Class<?> claseDependiente, Class<?> clase) { //buscar campos BSONable
		Vector<Field> resultado=null;
		Field[] camposClaseDependiente=claseDependiente.getDeclaredFields();
		for(int i=0; i<camposClaseDependiente.length; i++) {
			Field campo=camposClaseDependiente[i];
			if(campo.getType()==clase && campo.getAnnotation(BSONable.class)!=null) {
				if(resultado==null)
					resultado=new Vector<>();
				resultado.add(campo);
			}
		}
		return null;
	}
}
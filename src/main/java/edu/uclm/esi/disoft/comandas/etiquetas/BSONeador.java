package edu.uclm.esi.disoft.comandas.etiquetas;

import java.lang.reflect.Field;
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
import edu.uclm.esi.disoft.comandas.dominio.Plato;
import edu.uclm.esi.disoft.comandas.dominio.PlatoPedido;

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
		MongoCollection<BsonDocument> collection=MongoBroker.get().getCollection(clase.getName());
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
			Field[] campos=valorDelCampo.getClass().getDeclaredFields();
			BsonDocument bso=new BsonDocument();
			for(int i=0; i<campos.length; i++) {
				Field campo=campos[i];
				campo.setAccessible(true);
				bso.put(campo.getName(), getBsonValue(campo.get(valorDelCampo)));
			}
			return bso;
		}
		if(tipo==Vector.class) {
			Vector vector = (Vector)valorDelCampo;
			BsonArray bsoa = new BsonArray();
			for(int i=0;i<vector.size();i++) {
				BsonDocument bso = new BsonDocument();
				Object objeto = vector.get(i);
				Field[] campos = objeto.getClass().getDeclaredFields();
				for(int j=0;j<campos.length;j++) {
					Field campo = campos[j];
					campo.setAccessible(true);
					bso.put(campo.getName(), getBsonValue(campo.get(objeto)));
				}
				bsoa.add(bso);
			}
			return bsoa;
		}
		return null;
	}

	public static ConcurrentHashMap<Object, Object> load(Class<?> clase, Object... variables) throws Exception, InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException {
		ConcurrentHashMap<Object, Object> result=new ConcurrentHashMap<>();
		MongoCollection<BsonDocument> collection=MongoBroker.get().getCollection(clase.getName());
		BsonDocument criterio=new BsonDocument(); String buscador = ""; String valor = "";
		
		if(variables.length > 0) {
			buscador = variables[0].toString();
			valor = variables[1].toString();
			criterio.put(buscador, new BsonObjectId(new ObjectId(valor)));
		}
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
		if(bso.get("_id").isObjectId()){
			return bso.get("_id").asObjectId().getValue().toHexString();
		}
		return null;
	}

	private static Object getObject(Class<?> clase, BsonDocument bso) throws Exception {
		Object result=clase.newInstance(); //Mesa result=new Mesa() pero de forma reflexiva
		Iterator<String> nombresDeLosCampos = bso.keySet().iterator();
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
	
	private static void set(Field campo, Object result, BsonValue valorDelBson) throws Exception {
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
	
	public static void update(Object objeto, Object... nombresValores) throws Exception { //Object[] pasamos varios arrays separados por comas
		if(nombresValores.length==0 || nombresValores.length % 2!=0)
			throw new IllegalArgumentException("Esperaba un número par de parámetros.");
		
		BsonDocument nuevosValores=new BsonDocument();
		Class<?> clase = objeto.getClass();
		Field [] campos=clase.getDeclaredFields();
		
		for(int i=0; i<campos.length; i++) {
			Field campo = campos[i];
			campo.setAccessible(true);
			Object valor = campo.get(objeto);
			if (valor==null)
				continue;
			BSONable anotacion = campo.getAnnotation(BSONable.class);
			if (anotacion == null)
				nuevosValores.append(campo.getName(), getBsonValue(valor));
			else {
				String nombreCampoAsociado = anotacion.campo();
				String nombreNuevo = anotacion.nombre();
				Field campoAsociado = valor.getClass().getDeclaredField(nombreCampoAsociado);
				campoAsociado.setAccessible(true);
				Object valorCampoAsociado = campoAsociado.get(valor);
				nuevosValores.append(nombreNuevo, getBsonValue(valorCampoAsociado));
			}
		}
		nuevosValores.remove("_id");
		Field campoId = clase.getDeclaredField("_id");
		campoId.setAccessible(true);
		Object valorId = campoId.get(objeto);
		BsonDocument criterio = new BsonDocument();
		criterio.put("_id", getBsonValue(valorId));
		
		MongoCollection<BsonDocument> collection = MongoBroker.get().getCollection(clase.getSimpleName());
		collection.replaceOne(criterio, nuevosValores);
	}
	
	private static void insertar(Mesa mesa) {
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
	
	private static void delete(Object objeto) throws Exception {
		Class<?> clase = objeto.getClass();
		Field campoId=clase.getDeclaredField("_id");
		campoId.setAccessible(true);
		Object valorId=campoId.get(objeto);
		BsonDocument criterio=new BsonDocument();
		criterio.put("_id", getBsonValue(valorId));
		MongoCollection<BsonDocument> coleccion = MongoBroker.get().getCollection(clase.getName());
		coleccion.deleteOne(criterio);
		
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

	public static void main(String[] args) {
		Plato plato=new Plato("128", "Tortilla de Gambas", 6.50);
		PlatoPedido platoPedido=new PlatoPedido(plato, 2);
		try {
			BSONeador.insert(plato);
			//BSONeador.insert(platoPedido);
			BSONeador.delete(plato);
			//BSONeador.update(plato, "nombre", "Tortilla de jamón");
			//comprobar que se ha cambiado en platopedido
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*try {
			Enumeration<Object> platos = BSONeador.load(Plato.class).elements();
			while(platos.hasMoreElements()) {
				Plato plato=(Plato) platos.nextElement();
				System.out.println(plato.getId());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}
}
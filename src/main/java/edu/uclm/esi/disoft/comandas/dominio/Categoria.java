package edu.uclm.esi.disoft.comandas.dominio;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.uclm.esi.disoft.comandas.dao.DAOPlato;
import edu.uclm.esi.disoft.comandas.etiquetas.BSONable;
//import edu.uclm.esi.disoft.comandas.etiquetas.BSONeador;
@BSONable
public class Categoria {
	private String _id;
	private String nombre;
	private JSONArray jsaPlatos;
	//<string, plato> es lo que teníamos pero tenemos que conseguirlo genéricamente para cualquier <object>¿??
	private ConcurrentHashMap<String, Plato> platos;
	
	public Categoria() {}
	
	public Categoria(String _id, String nombre) throws Exception {
		this._id=_id;
		this.nombre=nombre;
		this.jsaPlatos=new JSONArray();
		this.platos=DAOPlato.load(_id);
		Enumeration<Plato> ePlatos = this.platos.elements();
		while (ePlatos.hasMoreElements()) {
			this.jsaPlatos.put(ePlatos.nextElement().toJSONObject());
		}
	}
	
	public Plato find(String idPlato) {
		return this.platos.get(idPlato);
	}
	
	public JSONArray getPlatos() {
		return jsaPlatos;
	}

	public JSONObject toJSONObject() {
		JSONObject jso=new JSONObject();
		jso.put("_id", this._id);
		jso.put("nombre", this.nombre);
		jso.put("platos", this.jsaPlatos);
		return jso;
	}
}
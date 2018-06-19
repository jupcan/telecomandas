package edu.uclm.esi.disoft.comandas.dominio;

import org.json.JSONObject;

import edu.uclm.esi.disoft.comandas.etiquetas.BSONable;
import edu.uclm.esi.disoft.comandas.etiquetas.JSONable;

@BSONable(claseDependiente="PlatoPedido") //depende de platopedido para borrar en cascada
public class Plato {
	@BSONable @JSONable
	private String _id;
	@JSONable
	private String nombre;
	@JSONable
	private double precio;
	
	public Plato() {}

	public Plato(String _id, String nombre, double precio) {
		this._id=_id;
		this.nombre=nombre;
		this.precio=precio;
	}

	public String getId() {
		return this._id;
	}
	
	public double getPrecio() {
		return precio;
	}
	
	public JSONObject toJSONObject() {
		JSONObject jso=new JSONObject();
		jso.put("_id", _id);
		jso.put("nombre", nombre);
		jso.put("precio", precio);
		return jso;
	}
}
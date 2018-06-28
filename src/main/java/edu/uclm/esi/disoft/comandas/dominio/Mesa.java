package edu.uclm.esi.disoft.comandas.dominio;

import org.json.JSONObject;

import edu.uclm.esi.disoft.comandas.etiquetas.BSONable;
import edu.uclm.esi.disoft.comandas.etiquetas.JSONable;

@BSONable
public class Mesa {
	@JSONable
	private int _id;
	private Comanda comandaActual;
	
	public Mesa() {}

	public Mesa(int id) {
		this._id=id;
	}
	
	public boolean estaLibre() {
		return comandaActual==null;
	}
	
	public void seleccionar() {}

	public void abrir() throws Exception {
		if (comandaActual!=null)
			throw new Exception("La mesa ya está abierta. Por favor, espere o elija otra.");
		comandaActual=new Comanda();
	}

	public void cerrar() throws Exception {
		if (comandaActual==null)
			throw new Exception("La mesa ya está cerrada.");
		comandaActual.cerrar();
		//comandaActual=null;
	}

	public void addToComanda(Plato plato, int unidades) {
		this.comandaActual.add(plato, unidades);
	}

	public void setComandaActual(Comanda comandaActual) {
		this.comandaActual = comandaActual;
	}
	
	public Comanda getComandaActual() {
		return comandaActual;
	}
	
	public JSONObject estado() {
		JSONObject jso=new JSONObject();
		jso.put("id", this._id);
		if (comandaActual==null) 
			jso.put("estado", "Libre");
		else {
			jso.put("comanda", this.comandaActual.toJSONObject());
		}
		return jso;
	}
	
	public JSONObject toJSONObject() {
		JSONObject jso=new JSONObject();
		jso.put("_id", this._id);
		jso.put("estado", comandaActual==null ? "Libre" : "Ocupada");
		return jso;
	}
}
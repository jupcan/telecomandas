package edu.uclm.esi.disoft.comandas.dominio;

import org.json.JSONObject;

import edu.uclm.esi.disoft.comandas.etiquetas.BSONable;
import edu.uclm.esi.disoft.comandas.etiquetas.JSONable;

@BSONable
public class Mesa {
	@JSONable
	private double _id;
	private Comanda comandaActual;
	private String estado;
	
	public Mesa() {}

	public Mesa(double id) {
		this._id=id;
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
	
	public boolean estaLibre() {
		return comandaActual==null;
	}

	public void abrir() throws Exception {
		if (comandaActual!=null)
			throw new Exception("La mesa ya está abierta. Elige otra");
		comandaActual=new Comanda(this._id);
	}

	public void cerrar() throws Exception {
		if (comandaActual==null)
			throw new Exception("La mesa ya está cerrada");
		comandaActual.cerrar();
		comandaActual=null;
	}

	public PlatoPedido addToComanda(Plato plato, int unidades) {
		return this.comandaActual.add(plato, unidades);
	}

	public void setComandaActual(Comanda comandaActual) {
		this.comandaActual = comandaActual;
	}
	
	public Comanda getComandaActual() {
		return comandaActual;
	}
	
	public JSONObject toJSONObject() {
		JSONObject jso=new JSONObject();
		jso.put("_id", this._id);
		jso.put("estado", comandaActual==null ? "Libre" : "Ocupada");
		return jso;
	}
}
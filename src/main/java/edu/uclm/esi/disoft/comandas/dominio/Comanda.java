package edu.uclm.esi.disoft.comandas.dominio;

import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

public class Comanda {
	private long horaApertura, horaCierre;
	private Vector<PlatoPedido> platos;
	
	public Comanda() {
		this.horaApertura=System.currentTimeMillis();
		this.platos=new Vector<>();
	}

	public void cerrar() {
		this.horaCierre=System.currentTimeMillis();
	}

	public void add(Plato plato, int unidades) {
		PlatoPedido platoPedido=new PlatoPedido(plato, unidades);
		this.platos.add(platoPedido);
	}

	public JSONObject toJSONObject() {
		JSONObject jso=new JSONObject();
		jso.put("horaApertura", Auxi.getHora(this.horaApertura));
		JSONArray platosPedidos=new JSONArray();
		for (PlatoPedido pp : this.platos)
			platosPedidos.put(pp.toJSONObject());
		jso.put("platos", platosPedidos);
		return jso;
	}
}

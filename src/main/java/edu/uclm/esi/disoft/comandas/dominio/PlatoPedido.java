package edu.uclm.esi.disoft.comandas.dominio;

import org.json.JSONObject;

public class PlatoPedido {
	private Plato plato;
	private int unidades;
	
	public PlatoPedido(Plato plato, int unidades) {
		this.plato=plato;
		this.unidades=unidades;
	}

	public JSONObject toJSONObject() {
		JSONObject jso=this.plato.toJSONObject();
		jso.put("unidades", this.unidades);
		return jso;
	}
}

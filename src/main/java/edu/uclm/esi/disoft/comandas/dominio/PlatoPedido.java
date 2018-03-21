package edu.uclm.esi.disoft.comandas.dominio;

import org.json.JSONObject;

import edu.uclm.esi.disoft.comandas.etiquetas.BSONable;
import edu.uclm.esi.disoft.comandas.etiquetas.JSONable;

public class PlatoPedido {
	@JSONable(campo = "_id", nombre = "idPlato")
	@BSONable(campo = "_id", nombre = "idPlato", OnDeleteCascade=true) // crear borrado en cascada, ej examen
	private Plato plato;
	@JSONable
	private int unidades;
	
	public PlatoPedido() {
	}
	
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

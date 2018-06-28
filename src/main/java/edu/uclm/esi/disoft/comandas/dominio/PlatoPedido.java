package edu.uclm.esi.disoft.comandas.dominio;

import java.util.Random;

import org.json.JSONObject;

import edu.uclm.esi.disoft.comandas.etiquetas.BSONable;
import edu.uclm.esi.disoft.comandas.etiquetas.JSONable;

public class PlatoPedido {
	/*@JSONable(campo = "_id", nombre = "idPlato")
	@BSONable(campo = "_id", nombre = "idPlato", OnDeleteCascade=true) // crear borrado en cascada, ej examen*/
	@JSONable @BSONable
	private Plato plato;
	@JSONable @BSONable
	private int unidades;
	@JSONable @BSONable
	private long id;
	
	public PlatoPedido() {}
	
	public PlatoPedido(Plato plato, int unidades) {
		Random rnd = new Random();
		long valorAleatorio = rnd.nextLong();
		this.plato=plato;
		this.unidades=unidades;
		this.id= valorAleatorio;
	}
	
	public JSONObject toJSONObject() {
		JSONObject jso=this.plato.toJSONObject();
		jso.put("unidades", this.unidades);
		//jso.put("idPlatoPedido", this.id);
		return jso;
	}
}
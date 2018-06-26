package edu.uclm.esi.disoft.comandas.dominio;

import edu.uclm.esi.disoft.comandas.etiquetas.BSONable;
import edu.uclm.esi.disoft.comandas.etiquetas.JSONable;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

public class Comanda {
	private String _id;
	@JSONable
	private int idMesa;
	@BSONable @JSONable
	private long horaApertura, horaCierre;
	@BSONable @JSONable
	private Vector<PlatoPedido> platos;
	
	public Comanda() {}
	
	public Comanda(int idMesa) {
		this.idMesa=idMesa;
		this.horaApertura=System.currentTimeMillis();
		this.platos=new Vector<>();
	}

	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}
	
	public void cerrar() {
		this.horaCierre=System.currentTimeMillis();
	}
	
	public void setHoraCierre(long horaCierre) {
		this.horaCierre = horaCierre;
	}
	
	public long getHoraCierre() {
		return horaCierre;
	}

	public PlatoPedido add(Plato plato, int unidades) {
		PlatoPedido platoPedido=new PlatoPedido(plato, unidades);
		this.platos.add(platoPedido);
		return platoPedido;
	}
	
	public Vector<PlatoPedido> getPlatos() {
		return platos;
	}
	
	public void setPlatos(Vector<PlatoPedido> platos) {
		this.platos = platos;
	}
	
	public JSONObject toJSONObject() {
		JSONObject jso=new JSONObject();
		jso.put("idMesa", this.idMesa);
		jso.put("horaApertura", Auxi.getHora(this.horaApertura));
		JSONArray platosPedidos=new JSONArray();
		for (PlatoPedido pp : this.platos)
			platosPedidos.put(pp.toJSONObject());
		jso.put("platos", platosPedidos);
		return jso;
	}
}
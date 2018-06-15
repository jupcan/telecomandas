package edu.uclm.esi.disoft.comandas.dominio;

import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.uclm.esi.disoft.comandas.dao.DAOCategoria;
import edu.uclm.esi.disoft.comandas.dao.DAOMesa;
import edu.uclm.esi.disoft.comandas.etiquetas.BSONeador;
import edu.uclm.esi.disoft.comandas.ws.ServidorWS;

public class Manager {
	//<Object, Object>
	private ConcurrentHashMap<Integer, Mesa> mesas;
	private JSONArray jsaCategorias;
	private ConcurrentHashMap<String, Categoria> categorias;
	
	//DAO o BSNeador
	private Manager() {
		mesas=DAOMesa.load();
		cargarCategorias();
	}
	
	private void cargarCategorias() {
		categorias=DAOCategoria.load();
		this.jsaCategorias=new JSONArray();
		Enumeration<String> keys=categorias.keys();
		while (keys.hasMoreElements()) {
			String key=keys.nextElement();
			Categoria categoria=categorias.get(key);
			this.jsaCategorias.put(categoria.toJSONObject());
		}
	}

	private static class ManagerHolder {
		static Manager singleton=new Manager();
	}
	
	public static Manager get() {
		return ManagerHolder.singleton;
	}
	
	public void abrirMesa(int id) throws Exception {
		Mesa mesa=mesas.get(id);
		mesa.abrir();
	}
	
	public void cerrarMesa(int id) throws Exception {
		Mesa mesa=mesas.get(id);
		String idComanda = mesa.getComandaActual().get_id();
		mesa.cerrar();
		if(idComanda != null) {
			JSONObject jso = new JSONObject();
			jso.put("type", "cerrar");
			jso.put("idComanda", idComanda);
			ServidorWS.enviar(jso);
		}
	}
	
	public void recibirComanda(int idMesa, JSONArray platos) throws Exception {
		Vector<PlatoPedido> platosNuevos = new Vector<>();
		Mesa mesa=mesas.get(idMesa);
		if (mesa.estaLibre())
			throw new Exception("La mesa " + idMesa + " está libre. Ábrela primero");
		for (int i=0; i<platos.length(); i++) {
			JSONObject jsoPlato=platos.getJSONObject(i);
			String idCategoria=jsoPlato.getString("idCategoria");
			String idPlato=jsoPlato.getString("idPlato");
			int unidades=jsoPlato.getInt("unidades");
			Categoria categoria=this.categorias.get(idCategoria);
			Plato plato=categoria.find(idPlato);
			//mesa.addToComanda(plato, unidades);
			platosNuevos.add(mesa.addToComanda(plato, unidades));
		}
		mesa.setPrecioComanda();
		//ServidorWS.solicitarPlatos(platos); //ws connection to kitchen client
		
		/*if(mesa.getComandaActual().get_id() == null) {
			ObjectId id = new ObjectId();
			mesa.getComandaActual().set_id(id.toHexString());
			BSONeador.insert(mesa.getComandaActual());
		}else
			BSONeador.update(mesa.getComandaActual());
		
		enviarCocina(mesa.getComandaActual().get_id(), idMesa, platosNuevos);*/
	}
	
	public JSONArray getMesas() {
		JSONArray result=new JSONArray();
		Enumeration<Mesa> eMesas = this.mesas.elements();
		Mesa mesa;
		while (eMesas.hasMoreElements()) {
			mesa=eMesas.nextElement();
			result.put(mesa.toJSONObject());
		}
		return result;
	}
	
	public JSONObject getEstadoMesa(int id) {
		return this.mesas.get(id).estado();
	}
	
	public JSONArray getCategorias() {
		return this.jsaCategorias;
	}
	
	public JSONArray getPlatosDeCategoria(String idCategoria) {
		Categoria categoria=this.categorias.get(idCategoria);
		return categoria.getPlatos();
		/*for (int i=0; i<this.jsaCategorias.length(); i++) {
			JSONObject jso=this.jsaCategorias.getJSONObject(i);
			if (jso.getString("_id").equals(idCategoria))
				return jso.getJSONArray("platos");
		}
		return null;*/
	}
	
	public void platoPreparado(JSONObject jso)throws Exception{
		int idMesa = jso.getInt("idMesa");
		String idPlato = jso.getString("id");
		Mesa mesa=(Mesa)mesas.get(idMesa);
		Vector<PlatoPedido> platos = mesa.getComandaActual().getPlatos();
		for(PlatoPedido pp : platos) {
			if(pp.getPlato().getId().equals(idPlato))
				pp.setPreparado(true);
		}
		mesa.getComandaActual().setPlatos(platos);
		BSONeador.update(mesa.getComandaActual());

	}
}

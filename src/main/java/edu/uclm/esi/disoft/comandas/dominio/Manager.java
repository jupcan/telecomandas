package edu.uclm.esi.disoft.comandas.dominio;

import java.util.Enumeration;

import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.uclm.esi.disoft.comandas.dao.DAOCategoria;
import edu.uclm.esi.disoft.comandas.dao.DAOMesa;
import edu.uclm.esi.disoft.comandas.etiquetas.BSONeador;
import edu.uclm.esi.disoft.comandas.ws.ServidorWS;

public class Manager {
	private ConcurrentHashMap<Integer, Mesa> mesas; //<integer,mesa>
	private ConcurrentHashMap<String, Categoria> categorias; //<string,categoria>
	private ConcurrentHashMap<String, String> platoRdy;
	private JSONArray jsaCategorias;
	
	private Manager() { //daos o bsoneador en load¿??
		mesas=DAOMesa.load();
		cargarCategorias();
		this.platoRdy=new ConcurrentHashMap<>();
	}
	
	private void cargarCategorias() {
		categorias=DAOCategoria.load();
		this.jsaCategorias=new JSONArray();
		Enumeration<String> keys=categorias.keys();
		while (keys.hasMoreElements()) {
			String key=keys.nextElement().toString();
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
		mesa.cerrar();
		BSONeador.update(mesa.getComandaActual(), "horaCierre", mesa.getComandaActual().getHoraCierre());
		mesa.setComandaActual(null);
	}
	
	public void recibirComanda(int idMesa, JSONArray platos) throws Exception { //antiguo addToComanda
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
			mesa.addToComanda(plato, unidades);
		}
		mesa.setPrecioComanda();
		Comanda comanda=mesa.getComandaActual();
		try {
			BSONeador.insert(comanda);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ServidorWS.PlatosACocina(platos, idMesa); //ws connection boi
	}
	
	public void seleccionarMesa(int id) throws Exception {
		Mesa mesa=mesas.get(id);
		mesa.seleccionar();
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
	}
	
	public void platoPreparado(String nombrePlato, int idMesa) {
		String mesa = "mesa:"+idMesa;
		this.platoRdy.put(mesa, nombrePlato);
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
}
package edu.uclm.esi.disoft.comandas.ws;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.uclm.esi.disoft.comandas.dominio.Manager;

@ServerEndpoint(value="/ServidorWSComandas")
public class ServidorWS {
	private static ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
	
	@OnOpen
	public void onOpen(Session session) {
		sessions.put(session.getId(), session);
	}
	
	@OnMessage
	public void onMessage(Session session, String mensaje) {
		JSONObject jso = new JSONObject(mensaje);
		String type = jso.getString("type");
		switch (type) {
		case "PlatoFinalizado":
			String plato=jso.getString("plato");
			int idMesa = jso.getInt("idMesa");
			Manager.get().platoFinalizado(plato, idMesa);
			break;
		}
	}

	public static void solicitarPlatos(JSONArray platos,int idMesa) {
		JSONObject mensaje = new JSONObject();
		mensaje.put("type","solicitudDePlatos");
		mensaje.put("idMesa", idMesa);
		mensaje.put("platos", platos);
		Enumeration<Session> sesiones = sessions.elements();
		while(sesiones.hasMoreElements()) {
			Session sesion = sesiones.nextElement();
			if (sesion.isOpen())
				sesion.getAsyncRemote().sendText(mensaje.toString());// Cuando hago este send es cuando se ejecuta el onmessage
		}
	}
}
package edu.uclm.esi.disoft.comandas.ws;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONArray;
import org.json.JSONObject;

@ServerEndpoint(value="/ServidorWS")
public class ServidorWS {
	private static ConcurrentHashMap<String, Session> sessions=new ConcurrentHashMap<>();
	
	@OnOpen
	public void onOpen(Session session) {
		Map<String, List<String>> mapa=session.getRequestParameterMap();
		List<String> parametros = mapa.get("user");
		sessions.put(parametros.get(0), session);
		System.out.println(parametros.get(0));
	}
	
	@OnMessage 
	public void onMessage(Session session, String mensaje) {
		JSONObject jso=new JSONObject(mensaje);
		String type=jso.getString("type");
		switch (type) {
		case "PlatoPreparado" :
			break;
		case "MensajeIndividual" :
			String destinatario=jso.getString("destinatario");
			String texto=jso.getString("texto");
			enviar(destinatario, texto);
			System.out.println(texto);
			break;
		case "MensajeTodos" :
			texto=jso.getString("texto");
			enviarTodos(texto);
			break;
		}
	}

	private void enviarTodos(String texto) {

	}

	private void enviar(String destinatario, String texto) {
		Session sesionDestinatario=sessions.get(destinatario);
		if(sesionDestinatario==null) {
			sessions.remove(destinatario);
			return;
		}
		JSONObject mensaje=new JSONObject();
		mensaje.put("type", "MensajeIndividual");
		mensaje.put("texto", texto);
		sesionDestinatario.getAsyncRemote().sendText(mensaje.toString());
	}

	public static void solicitarPlatos(JSONArray platos) {
		JSONObject mensaje = new JSONObject();
		mensaje.put("type", "solicitudDePlatos");
		mensaje.put("platos", platos);
		Enumeration<Session> sesiones = sessions.elements();
		while(sesiones.hasMoreElements()) {
			Session sesion=sesiones.nextElement();
			sesion.getAsyncRemote().sendText(mensaje.toString());
		}
	}
}

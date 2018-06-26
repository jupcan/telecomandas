package edu.uclm.esi.disoft.comandas.ws;

import java.util.Enumeration;
import java.util.Vector;
//import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONObject;

import edu.uclm.esi.disoft.comandas.dominio.Manager;

@ServerEndpoint(value = "/ServidorWS")
public class ServidorWS {
	private static Vector<Session> sessions=new Vector<>();
	//private static ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
	
	private static class WSHolder{
		static ServidorWS singleton=new ServidorWS();
	}
	
	public static ServidorWS get() {
		return WSHolder.singleton;
	}
	
	@OnOpen
	public void onOpen(Session session) {
		sessions.add(session);
	}

	@OnMessage
	public void onMessage(Session session, String mensaje) throws Exception {
		JSONObject jso = new JSONObject(mensaje);
		Manager.get().platoPreparado(jso);
		session.getAsyncRemote().sendText(mensaje);
	}

	public static void send(JSONObject jso) {
		Enumeration<Session> sesiones = sessions.elements();
		while (sesiones.hasMoreElements()) {
			Session sesion = sesiones.nextElement();
			sesion.getAsyncRemote().sendText(jso.toString());
		}
	}
}
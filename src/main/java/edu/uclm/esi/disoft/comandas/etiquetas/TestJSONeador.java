package edu.uclm.esi.disoft.comandas.etiquetas;

import static org.junit.Assert.*;

import org.json.JSONObject;
import org.junit.Test;

import edu.uclm.esi.disoft.comandas.dominio.Comanda;
import edu.uclm.esi.disoft.comandas.dominio.Mesa;
import edu.uclm.esi.disoft.comandas.dominio.Plato;
import edu.uclm.esi.disoft.comandas.dominio.PlatoPedido;

public class TestJSONeador {

	@Test
	public void test() {
		Plato plato=new Plato("1", "Gazpacho", 5);
		String a=plato.toJSONObject().toString();
		String b=JSONeador.toJSONObject(plato).toString();
		assertEquals(a, b);
		
		/*PlatoPedido platoPedido=new PlatoPedido(plato, 4);
		a=platoPedido.toJSONObject().toString();
		b=JSONeador.toJSONObject(platoPedido).toString();
		assertEquals(a, b);
		
		Comanda comanda=new Comanda();
		comanda.add(plato, 2);
		Plato tortilla=new Plato("2", "Tortilla", 6.5);
		comanda.add(tortilla, 1);
		a=comanda.toJSONObject().toString();
		b=JSONeador.toJSONObject(comanda).toString();
		assertEquals(a, b);*/
		
		Mesa mesa=new Mesa(1);
		try {
			mesa.abrir();
		} catch (Exception e) {
			e.printStackTrace();
		}
		mesa.addToComanda(plato, 2);
		System.out.println(mesa.toJSONObject());
		JSONObject jsoMesa=JSONeador.toJSONObject(mesa);
		jsoMesa.put("estado", mesa.estaLibre() ? "Libre" : "Ocupada");
		System.out.println(jsoMesa);
	}

}

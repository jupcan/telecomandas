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
		Plato plato=new Plato("26", "Tortilla", 6.50);
		
		PlatoPedido platoPedido=new PlatoPedido(plato, 3);
		JSONObject jso = JSONeador.toJSONObject(platoPedido);
		System.out.println(jso.toString());
		
		String valorEsperado = 
				"{\"unidades\":3,\"idPlato\":\"26\"}";
		System.out.println(valorEsperado);
		assertEquals(jso.toString(), valorEsperado);
	}

}

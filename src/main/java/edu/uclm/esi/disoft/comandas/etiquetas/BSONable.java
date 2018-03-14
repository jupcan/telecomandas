package edu.uclm.esi.disoft.comandas.etiquetas;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)

public @interface BSONable {
	
}

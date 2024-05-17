package piat.Regex;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Clase que permite registrar los estadísticos agregados por cuenta emisora.
 * Para cada nombre de usuario, lleva la cuenta del número de mensajes que ha generado.
 * @author José Luis López Presa
 */
public class EstUsuario
{
	private final Map<String,AtomicInteger> usuarios;

	/**
	 * Constructor por defecto.
	 */
	public EstUsuario ()
	{
		usuarios = new ConcurrentHashMap<String,AtomicInteger>();
	}

	/**
	 * Registra que un usuario ha generado un mensaje más. Si aún no se
	 * había registrado ningún mensaje para este usuario, se inicia la
	 * cuenta de mensajes en 1.
	 * @param u El nombre de usuario al que hay que contabilizar el envío.
	 */
	public void registrarMensaje ( String u )
	{
		usuarios.putIfAbsent ( u, new AtomicInteger(0) );
		usuarios.get(u).incrementAndGet();
	}

	/**
	 * Muestra por la salida estándar el número de mensajes que ha enviado
	 * cada usuario de los que ha superado un cierto umbral.
	 * @param umbral El número mínimo de mensajes que tiene que haber generado
	 * un usuario para que se muestre su información.
	 */
	public void mostrar ( int umbral )
	{
		System.out.println ( "Usuarios que han enviado más de " + umbral + " mensajes:" );
		final TreeMap<String,AtomicInteger> ordenado = new TreeMap<String,AtomicInteger>(usuarios);
		for ( Map.Entry<String,AtomicInteger> e : ordenado.entrySet() )
			if ( e.getValue().get() >= umbral )
				System.out.println ( "\t\t" + e.getKey() + ": " + e.getValue() );
	}
}

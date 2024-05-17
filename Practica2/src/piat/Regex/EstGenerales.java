package piat.Regex;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Clase que almacena los estadísticos generales. Permite registrar los
 * servidores, clasificados por tipo, el número de ficheros procesados,
 * el número de trazas procesadas y el número de trazas que no tenían el
 * formato adecuado.
 * @author José Luis López Presa
 */
public class EstGenerales
{
	private final Map<String,Set<String>> servidores;
	private final AtomicInteger numFich;
	private final AtomicInteger trazas;
	private final AtomicInteger errores;

	/**
	 * Constructor por defecto.
	 */
	public EstGenerales ()
	{
		servidores = new ConcurrentHashMap<String,Set<String>>();
		numFich = new AtomicInteger(0);
		trazas = new AtomicInteger(0);
		errores = new AtomicInteger(0);
	}

	/**
	 * Registra un servidor. Aunque se registre el mismo servidor
	 * muchas veces, se contabilizará su existencia una sola vez.
	 * @param s Tipo de servidor.
	 * @param n Número que identifica al servidor (como String).
	 */
	public void registrarServidor ( String s, String n )
	{
		servidores.putIfAbsent ( s, new HashSet<String>() );
		final Set<String> v = servidores.get(s);
		synchronized ( v )
		{
			v.add ( n );
		}
	}

	/**
	 * Incrementa el número de ficheros procesados.
	 */
	public int registrarFichero ()
	{
		return numFich.incrementAndGet();
	}

	/**
	 * Incrementa el número de trazas procesadas.
	 */
	public void registrarTraza ()
	{
		trazas.incrementAndGet();
	}

	/**
	 * Incrementa el número de trazas procesadas en la cantidad indicada.
	 * @param t Número de trazas que se quieren registrar.
	 */
	public void registrarTrazas ( int t )
	{
		trazas.addAndGet ( t );
	}

	/**
	 * Incrementa la cuenta de trazas encontrados con errores de formato.
	 */
	public void registrarError ()
	{
		errores.incrementAndGet();
	}

	/**
	 * Permite consultar el número de ficheros que han sido procesados.
	 * @return El número de ficheros que han sido procesados hasta el
	 * momento.
	 */
	public int ficherosProcesados ()
	{
		return numFich.get();
	}

	private void mostrarServidores ()
	{
		String sep;

		System.out.print ( "\tServidores" );
		sep = ": ";
		for ( Map.Entry<String,Set<String>> e : servidores.entrySet() )
		{
			System.out.print ( sep + e.getKey() + " = " + e.getValue().size() );
			sep = ", ";
		}
		System.out.println();
	}

	/**
	 * Muestra por la salida estándar los valores de los estadísticos
	 * generales.
	 */
	public void mostrar ()
	{
		System.out.println ( "Estadísticos generales:" );
		mostrarServidores();
		System.out.println ( "\tSe han procesado " + numFich + " ficheros" );
		System.out.println ( "\tSe han procesado " + trazas + " trazas" );
		System.out.println ( "\tSe han encontrado " + errores + " trazas no válidas" );
	}
}

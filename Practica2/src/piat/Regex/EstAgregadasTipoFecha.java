package piat.Regex;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Esta clase almacena las estadísticos agregados por tipo de servidor y día. Los estadísticos se clasifican
 * por tipo de servidor y luego por fecha.
 * @author José Luis López Presa
 */
public class EstAgregadasTipoFecha
{
	private class AgregadaPorTipo
	{
		private final Map<String,AtomicInteger> at;

		public AgregadaPorTipo ()
		{
			at = new ConcurrentHashMap<String,AtomicInteger>();
		}

		public void registrarTraza ( String t )
		{
			at.putIfAbsent ( t, new AtomicInteger(0) );
			at.get(t).incrementAndGet();
		}

		public void mostrar ()
		{
			for ( Map.Entry<String,AtomicInteger> e : at.entrySet() )
				System.out.println ( "\t\t\t" + e.getKey() + ": " + e.getValue() );
		}
	}

	private class AgregadaPorFecha
	{
		private final Map<String,AgregadaPorTipo> af;

		public AgregadaPorFecha ()
		{
			af = new ConcurrentHashMap<String,AgregadaPorTipo>();
		}

		public void registrarTraza ( String f, String t )
		{
			af.putIfAbsent ( f, new AgregadaPorTipo() );
			af.get(f).registrarTraza ( t );
		}

		public void mostrar ()
		{
			for ( Map.Entry<String,AgregadaPorTipo> e : af.entrySet() )
			{
				System.out.println ( "\t\t" + e.getKey() + ":" );
				e.getValue().mostrar();
			}
		}
	}

	private final Map<String,AgregadaPorFecha> ea;

	/**
	 * Constructor.
	 */
	public EstAgregadasTipoFecha ()
	{
		ea = new ConcurrentHashMap<String,AgregadaPorFecha>();
	}

	/**
	 * Incrementa las ocurrencias de un tipo de traza. Si aún no se había
	 * registrado ninguna traza de ese tipo, la anota por primera vez.
	 * @param s servicio al que corresponde la traza.
	 * @param f fecha correspondiente a la traza.
	 * @param t tipo de traza que se desea registrar.
	 */
	public void registrarTraza ( String s, String f, String t )
	{
		ea.putIfAbsent ( s, new AgregadaPorFecha() );
		ea.get(s).registrarTraza ( f, t );
	}

	/**
	 * Muestra por la salida estándar los valores registrados para cada traza.
	 * Los resultados se muestran clasificados por tipo de servidor y fecha.
	 */
	public void mostrar ()
	{
		System.out.println ( "Estadísticos agregados por tipo de servidor y día:" );
		for ( Map.Entry<String,AgregadaPorFecha> e : ea.entrySet() )
		{
			System.out.println( "\t" + e.getKey() + ":" );
			e.getValue().mostrar();
		}
	}
}

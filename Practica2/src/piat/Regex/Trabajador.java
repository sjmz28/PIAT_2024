package piat.Regex;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;

/**
 * Hilo que realiza el procesamiento de un fichero de LOG y registra
 * los datos estadísticos solicitados.
 *@author Sara Jiménez Muñoz s.jmunoz@alumnos.upm.es
 */
public class Trabajador implements Runnable
{
	// Formatos básicos
	private static final String FECHA = "([0-9]{4}-[0-9]{2}-[0-9]{2})";
	private static final String HORA = "[0-9]{2}:[0-9]{2}:[0-9]{2}";
	private static final String TIPO = "([a-zA-Z-_]+)";
	private static final String NUM = "([\\d]+)";
	private static final String ID = "\\[[\\w]+\\]";
	private static final String ESP = "\\s+";
	//TODO: Añadir aquí otros formatos para las expresiones que vaya a usar
	private static final String CORREO =  ".*msa.*from: <(([\\w-]+\\.[\\w-]*)@[AZa-z0-9]+(\\.[AZa-z0-9]+)*(\\.[A-Za-z]{2,}))";
	// Preámbulo de las trazas
	private static final String PREÁMBULO = FECHA + ESP + HORA + ESP + TIPO + NUM + ESP + ID + ":";

	// Traza genérica
	private static final String VÁLIDA = PREÁMBULO + ESP + ".+$";

	// Las siguientes 4 constantes se usan como ejemplo para obtener dos estadísticos que no son los que se piden en el enunciado, 
	// pero sirven de ejemplo para entender el uso de esta clase.
	// Los estadísticos de ejemplo son:
	// 		- msgPassed: número de mensajes que han pasado el control de seguridad.
	//		- msgBlocked: número de mensajes que no han pasado el control de seguridad.
	// Ejemplo de trazas buscadas:
	//		- Para msgPassed: 
	//			2020-02-22 05:07:14 security-in1 [200E3F05]: SEC-PASSED: security-antivirus: CLEAN security-antispam: HAM
	//			2020-02-22 05:00:02 security-out1 [A0819FFC]: SEC-PASSED: security-antivirus: CLEAN security-antispam: HAM
	//		- Para msgBlocked: 
	//			2020-02-22 04:00:14 security-in1 [200DF3AE]: SEC-BLOCKED: security-antivirus: INFECTED security-antispam: HAM
	//			2020-02-22 05:00:02 security-out1 [A081A006]: SEC-BLOCKED: security-antivirus: CLEAN security-antispam: SPAM
	// Puesto que el control de seguridad lo realizan los servidores security-in o security-out, las trazas deben contener este tipo de servidor 
	// También se puede observar que las trazas deben contener el texto:
	//		- Para msgPassed: "SEC-PASSED: security-antivirus:"
	//  	- Para msgBlocked: "SEC-BLOCKED: security-antivirus:"
	// Con esta información se declaran las constantes PASA y BLOQ que usan la concatenación de constantes definidas anteriormente
	// y de nuevas constantes declaradas para este caso. Observe que en las constantes PASA y BLOQ son expresiones regulares
	// que usan grupos, concretamente el grupo 1 es la fecha, el 2 la hora, el 3 el nombre del servidor y el 4 un texto que se usará
	// como clave en el mapa nombreEst, definido más adelante, para poder determinar el estadístico que se busca con esa expresión regular:
	private static final String SEC = "(security-in|security-out)";
	private static final String BASE_SEC = FECHA + ESP + HORA + ESP + SEC + NUM + ESP + ID + ":";
	private static final String PASA = BASE_SEC + ESP + "(SEC-PASSED):" + ESP + "security-antivirus:.*$";	// Para msgPassed
	private static final String BLOQ = BASE_SEC + ESP + "(SEC-BLOCKED):" + ESP + "security-antivirus:.*$";	// Para msgBlocked
	//TODO: Añadir aquí las constantes que permitan obtener los patrones de los estadísticos de la práctica
	//para cualquier server
		private static final String CODE432_1 =PREÁMBULO + ESP +"status=(4.3.2)" +ESP+".*";
	    private static final String CODE432_2 =PREÁMBULO + ESP +"temporarily"+ESP+"rejected,"+ESP+"dsn:"+ESP+"(4.3.2)"+".*";
	    private static final String CODE511_2 =PREÁMBULO + ESP +"permanently"+ESP+"rejected,"+ESP+"dsn:"+ESP+"(5.1.1)"+".*";
		
		//servidor SEC
		static final String SPAM = BASE_SEC + ESP + "SEC-PASSED:" + ESP + "security-antivirus:" + ESP + "CLEAN" + ESP + "security-antispam:" + ESP + "(SPAM)"; //para spam
		private static final String INFECTED = BASE_SEC + ESP + "SEC-BLOCKED:" + ESP + "security-antivirus: (INFECTED) security-antispam:.*$";
		private static final String CODE511_1 = BASE_SEC + ESP + "status=(5.1.1)" + ESP + ".*";

		//servidor SMTP
		private static final String SMTP = "(smtp-out|smtp-in)";
		private static final String SOLOSMTPIN = "(smtp-in)";
		private static final String SOLOSMTPOUT = "(smtp-out)";

		

		private static final String BASE_SMTPIN = FECHA + ESP + HORA + ESP + SOLOSMTPIN + NUM + ESP + ID + ":";
		private static final String BASE_SMTPOUT = FECHA + ESP + HORA + ESP + SOLOSMTPOUT + NUM + ESP + ID + ":";
		private static final String BASE_SMTP = FECHA + ESP + HORA + ESP + SMTP + NUM + ESP + ID + ":";
		

	    private static final String SMTPIN =BASE_SMTPIN + ESP + "(status).*";
	    private static final String SMTPOUT =BASE_SMTPOUT + ESP + "(relay).*";


	
	// Patrón para reconocer las trazas válidas
	private static final Pattern patrónGenérico = Pattern.compile ( VÁLIDA );
	
	// Nombres para los estadísticos agregados por tipo de servidor y día
	private static final Map<String,String> nombreEst = new HashMap<String,String>();
	static
	{
		// Estas dos primeras líneas servirían para obtener los dos estadísticos de ejemplo que no se piden en el enunciado
		nombreEst.put ( "SEC-PASSED", "msgPassed" );
		nombreEst.put ( "SEC-BLOCKED", "msgBlocked" );
		//TODO: Añadir aquí una línea por cada palabra clave detectada por la expresión regular 
		// 	    y su correspondencia con el nombre del estadístico del enunciado
		nombreEst.put("SPAM", "msgSPAM");
		nombreEst.put("INFECTED", "msgINFECTED");
		nombreEst.put("4.3.2", "code 4.3.2");
		nombreEst.put("5.1.1", "code 5.1.1");
		nombreEst.put("relay", "msgOut");
		nombreEst.put("status", "msgIn");
		
	}

	// Patrones para obtener los estadísticos agregados por tipo de servidor y día
	private static final List<Pattern> patrones = new ArrayList<Pattern>();
	static
	{
		// Estas dos primeras líneas servirían para obtener los dos estadísticos de ejemplo que no se piden en el enunciado
		patrones.add ( Pattern.compile ( PASA ) );
		patrones.add ( Pattern.compile ( BLOQ ) );
		//TODO: Añadir aquí una línea por cada patrón que servirá para obtener un estadístico agregado por tipo de servidor y día
		patrones.add(Pattern.compile( SPAM ) );
		patrones.add(Pattern.compile( INFECTED ) );
		patrones.add(Pattern.compile( CODE432_1 ) );
		patrones.add(Pattern.compile( CODE432_2 ) );
		patrones.add(Pattern.compile( CODE511_1 ) );
		patrones.add(Pattern.compile( CODE511_2 ) );
		patrones.add(Pattern.compile( SMTPOUT ));
		patrones.add(Pattern.compile( SMTPIN ));

	}

	// TODO Definir una constante con el patrón para contabilizar el estadístico agregado por cuenta emisora
	//private static final String CUENTA_EMISORA = "(your_pattern_here)";
	private static final String CUENTA_EMISORA = PREÁMBULO + ESP + "from=<([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})>.*";

	
	// Atributos
	private final File fichero;
	private final EstGenerales eg;	
	private final EstAgregadasTipoFecha ea;
	private final EstUsuario eu;
	private final String nombre;

	/**
	 * Constructor.
	 * @param f Fichero de LOG a procesar.
	 * @param eg Estadísticos generales a actualizar.
	 * @param ea Estadísticos agregados por tipo de servidor y día a actualizar.
	 * @param eu Estadísticos agregados por cuenta emisora a actualizar.
	 * @param nt Número de trabajadores terminados.
	 */
	public Trabajador (
		File f,
		EstGenerales eg,
		EstAgregadasTipoFecha ea,
		EstUsuario eu )
	{
		this.fichero = f;
		this.eg = eg;
		this.ea = ea;
		this.eu = eu;
		nombre = "Trabajador que procesa el fichero " + fichero.getName();
	}

	/**
	 * Procesa el fichero recibido y actualiza los estadísticos.
	 * En caso de que se produzca alguna excepción, muestra el mensaje correspondiente
	 * y termina su ejecución.
	 */
	public final void run ()
	{
		Thread.currentThread().setName(nombre);
		try
		{
			final BufferedReader entrada = new BufferedReader ( new FileReader ( fichero ) );
			procesarFichero ( entrada );
			entrada.close();
			eg.registrarFichero();
		}
		catch ( Exception e )
		{
			System.err.println ( e.getMessage() );
		}
	}

	private final void procesarFichero ( BufferedReader entrada )
	throws IOException
	{
		String línea;

		while ( ( línea = entrada.readLine() ) != null )
			procesarLinea ( línea );
	}

	private final void procesarLinea ( String línea )
	{
		final boolean casó = aplicarPatrones ( línea );
		final boolean error = !casó && !esVálida ( línea );
		eg.registrarTraza();
		if ( error )
			eg.registrarError();
	}

	private final boolean aplicarPatrones ( String l )
	{
		boolean éxito;

		éxito = false;
		// Procesar todos los patrones de la constante patrones que se usa
		// para obtener los estadísticos agregados por tipo de servidor y día
		for ( Pattern p : patrones )
		{	final boolean nuevo = aplicarPatrón ( p, l );
			éxito = éxito || nuevo;
		}
		//TODO: Procesar el patrón del estadístico agregado por cuenta emisora
		final boolean cuentaEmisoraMatch = aplicarPatrón(Pattern.compile(CUENTA_EMISORA), l);
		éxito = éxito || cuentaEmisoraMatch;
		return éxito;
	}

	private final boolean esVálida ( String l )
	{
		final Matcher m = patrónGenérico.matcher ( l );
		final boolean match = m.matches();
		//TODO: Obtener los datos de los estadísticos generales y registrarlos 
		
		if(match) {
			eg.registrarTraza();
			String traza = m.group(0);
			Pattern patron = Pattern.compile(CORREO);
			Matcher comparador = patron.matcher(traza);
			if(comparador.find()) {
				eu.registrarMensaje(comparador.group(2));
			}
			
		}
		
		
		return match;
	}

	private final boolean aplicarPatrón ( Pattern p, String l )
	{
		// TODO Este método no hay que modificarlo para obtener los dos estadísticos de ejemplo.
		// Se puede observar en el código de este método que se hace uso de los grupos 1, 2, 3 y 4 para obtener
		// información de la fecha, tipo de servidor, nº de servidor y la palabra clave que se guardó en nombreEst
		// para identificar el estadístico del patrón que se va a analizar
		// Si los patrones que realiza para obtener los estadísticos del enunciado tienen una disposición de grupos
		// diferentes, entonces sí tendrá que modificar este método o usar otro que finalmente ejecute el método
		// ea.registrarTraza() para registrar la traza
		
		final  Matcher m = p.matcher ( l );

		if ( !m.matches() )
			return false;
		final String fecha = m.group(1);
		final String tipoServ = m.group(2);
		final String num = m.group(3);
		final String tipoEst = nombreEst.get ( m.group(4) );
		ea.registrarTraza ( tipoServ, fecha, tipoEst );
		return true;
	}
}

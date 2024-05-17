package piat.Regex;
import java.io.File;
import java.io.FilenameFilter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Aplicación que procesa ficheros de LOG de un sistema. Considera 3
 * tipos de estadísticos: estadísticos generales, estadísticos agregados por tipo de servidor y día
 * y estadísticos agregados por cuenta emisora.
 * @author José Luis López Presa
 */
public class Practica2 {
	
	private static final int numMminMsg = 500;	// umbral de mensajes generados

	/**
	 * Programa principal de la aplicación. Tiene el siguiente comportamiento:
	 * Instancia los objetos que permiten registrar los estadísticos.
	 * Recorre todos los ficheros de log que haya en el directorio indicado
	 * y encarga de su procesamiento a un hilo trabajador, al que le pasa los
	 * objetos en los que debe actualizar los estadísticos. Garantiza que no haya
	 * en ejecución más trabajadores que núcleos tenga disponibles la máquina.
	 * Cuando todos los trabajadores han terminado su ejecución, muestra los
	 * resultados estadísticos obtenidos y el tiempo transcurrido en el proceso.
	 */
	public static void main ( String[] args )
	{
		try
		{
			Thread.currentThread().setName("Principal");
			final EstGenerales eg = new EstGenerales();
			final EstAgregadasTipoFecha ea = new EstAgregadasTipoFecha();
			final EstUsuario eu = new EstUsuario();
			final File dirLog = dirLog ( args );
			final int núcleos = Runtime.getRuntime().availableProcessors();
			final long tiempoComienzo = System.currentTimeMillis();
			System.out.println ( "Comienza la ejecución de la aplicación" );
			ejecutarTrabajadores ( eg, ea, eu, núcleos, dirLog );
			mostrarEstadísticos ( eg, ea, eu );
			final long tiempoFinal = System.currentTimeMillis();
			final long tiempoTranscurrido = (long)((tiempoFinal-tiempoComienzo)/1000);
			System.out.println ( "\nTiempo de ejecución: " + tiempoTranscurrido + " segundos" );
		}
		catch ( Exception e )
		{
			System.err.println ( e.getMessage() );
		}
	}

	private static void ejecutarTrabajadores (
			EstGenerales eg,
			EstAgregadasTipoFecha ea,
			EstUsuario eu,
			int núcleos,
			File dirLog )
	throws Exception
	{
		final File[] ficheros = obtenerFicheros ( dirLog );
		// Crear un pool donde ejecutar los hilos. El pool tendrá un tamaño del nº de núcleos del ordenador
		// por lo que nunca podrá haber más hilos que ese número en ejecución simultánea.
		// Si se quiere hacer pruebas con un solo trabajador en ejecución, poner como argumento un 1. Irá mucho más lenta la ejecución porque los ficheros se procesarán secuencialmente
		final ExecutorService ejecutor = Executors.newFixedThreadPool ( núcleos );

		System.out.println ("\nLanzar los hilos trabajadores que se encargarán de procesar los ficheros de log.");
		System.out.print ("Se va a crear un pool de hilos para que como máximo haya " + núcleos + " hilos en ejecución simultáneamente ");
		for ( File f : ficheros )
		{
			final Trabajador t = new Trabajador ( f, eg, ea, eu );
			ejecutor.execute ( t );
			System.out.print (".");
			//break; // Descomentando este break, solo se ejecuta el primer trabajador
		}

		System.out.println ( "\nSe han lanzado " + ficheros.length + " trabajadores" );
		// Esperar a que terminen todos los trabajadores
		ejecutor.shutdown();	// Cerrar el ejecutor cuando termine el último trabajador
		// Cada 2 segundos mostrar cuantos trabajadores se han ejecutado
		while ( !ejecutor.awaitTermination ( 2, TimeUnit.SECONDS ) ) 
			System.out.println ( "\tHan terminado " + eg.ficherosProcesados() + ". Faltan " + (ficheros.length - eg.ficherosProcesados()) + "." );
		final int fallos = ficheros.length - eg.ficherosProcesados();
		if ( fallos > 0 )
			throw new Exception ( "Algo ha salido mal. " + fallos + " trabajadores han fallado" );
	}

	private static void mostrarEstadísticos ( EstGenerales eg, EstAgregadasTipoFecha ea, EstUsuario eu )
	{
		System.out.println();
		eg.mostrar();
		System.out.println();
		ea.mostrar();
		System.out.println();
		eu.mostrar(numMminMsg);
	}
	
	private static final File dirLog ( String[] args ) throws Exception
	{
		if ( args.length < 1 )
			throw new Exception ( "Faltan argumentos");
		if ( args.length > 1 )
			throw new Exception ( "Sobran argumentos");
		final File dir = new File ( args[0] );
		if ( !dir.isDirectory() || !dir.canRead() )
			throw new Exception ( args[0] + " no es un directorio o no es legible" );
		return dir;
	}

	private static File[] obtenerFicheros ( File dirLog )
	{
		FilenameFilter filtro = new FilenameFilter()
			{
				@Override
				public boolean accept ( File dir, String name )
				{
					return name.endsWith ( ".log" );
				}
			};

		return dirLog.listFiles ( filtro );
	}	
}

package piat.opendatasearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import piat.opendatasearch.AnalizadorXPATH.Propiedad;

import java.io.File;
import java.io.FileWriter;
import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;



/**
 * @author Sara Jimenez Muñoz s.jmunoz@alumnos.upm.es

/*********************************************************************
 * argumentos de entrada:
 * 0016-018 ./src/Catalogo/catalogo.xml ./src/Catalogo/catalogo.xsd ./src/Catalogo/salida.xml ./src/Recursos/ResultadosBusquedaP4.xsd ./src/Recursos/salidaJSON.json
 ********************************************************************/
public class P5_XPATH
{
	/**
	 * Clase principal de la aplicación de extracción de información del 
	 * Portal de Datos Abiertos del Ayuntamiento de Madrid
	 */
	private static final Class<?> bogus = new Object(){}.getClass();
	private static final Class<?> esta = bogus.getEnclosingClass();
	private static final String nombre = esta.getCanonicalName();
	private static final String USO = "uso: " + nombre + " <categoría> " +
		"<catálogo> <esqema_catálogo> <salida> <esquema_salida>\n\n" +
		"\t<categoría>: identificador de la categoría a buscar\n" +
		"\t<catálogo>: nombre del fichero que contiene el catálogo\n" +
		"\t<esquema_catálogo>: nombre del fichero que contiene el esquema " +
		"que debe satisfacer el fichero de entrada\n" +
		"\t<salida> nombre del fichero de salida\n" +
		"\t<esquema>: nombre del fichero que contiene el esquema " +
		"que debe satisfacer e l fichero de salida\n";

	public static void main ( String[] args ) throws Exception
	{
		try
		{
			trabajar ( args );
		}
		catch ( Exception e )
		{
			System.err.println ( e.getMessage() );
			e.printStackTrace(); // durante la depuración
		}
	}

	private static void trabajar ( String[] args ) throws Exception
	{		
/********************************************************************************************************
* 			                	PASO 1: VALIDACIÓN DE ARGUENTOS										*
******************************************************************************************************/	
				
		// 1. Verificar numero de argumentos correcto
		if (args.length!=6){
			System.out.println("[!] ERROR: Argumentos incorrectos.");
			throw new IllegalArgumentException ( USO );
			}		
		String codCategoria      = args[0]; // Criterio de búsqueda
		
		String rutaCatalogoXml   = args[1]; //Ruta al documento XML que contiene el catálogo a procesar
		
		String rutaCatalogoXSD   = args[2]; //Ruta al documento esquema contra el que se tendrá que validar el 
										    // documento XML de entrada.
		String rutaFicheroSalida = args[3]; //Ruta al documento XML de salida en el que se almacenará el resultado 
										    // de la búsqueda
		String rutaSchema        = args[4]; //Ruta al documento esquema contra el que se tendrá que validar
									        // el documento XML generado
		String rutaJSONSalida    = args[5]; //Ruta al documento JSON con el resultado de las búsquedas
											// realizadas usando expresiones XPath
		
		List<Propiedad> lPropiedades = new ArrayList<>();
		
		try {

			//Verificacion de las expresiones regulares
			comprobarArgumentosExpresionesRegulares(codCategoria, rutaCatalogoXml, rutaCatalogoXSD, rutaFicheroSalida, rutaSchema);
			
			//Comprobaci�n de que los ficheros existen y tienen permisos de escritura y lectura
			File catalogoXML = new File(rutaCatalogoXml);
			if (!(catalogoXML.exists() && catalogoXML.isFile() && catalogoXML.canRead())) {
					System.out.println("[!] No se ha abierto el archivo " + catalogoXML.getAbsolutePath());
					System.exit(1);
			}
			
			File catalogoXSD = new File(rutaCatalogoXSD);
			if (!(catalogoXSD.exists() && catalogoXSD.isFile() && catalogoXSD.canRead())) {
					System.out.println("[!] No se ha abierto el archivo " + catalogoXSD.getAbsolutePath());
					System.exit(1);
					
			}
			File ficheroXSD = new File(rutaSchema);
			if (!(ficheroXSD.exists() && ficheroXSD.isFile() && ficheroXSD.canRead())) {
					System.out.println("[!] No se ha abierto el archivo " + ficheroXSD.getAbsolutePath());
					System.exit(1);
			}

			
			File ficheroSalida = new File(rutaFicheroSalida);
			if (!(ficheroSalida.exists() && ficheroSalida.isFile() && ficheroSalida.canWrite())) {
					System.out.println("[!]No se ha abierto el archivo " + ficheroSalida.getAbsolutePath());
					System.exit(1);
			}
			File ficheroSalidaJSON = new File(rutaJSONSalida);
			if (!(ficheroSalida.exists() && ficheroSalida.isFile() && ficheroSalida.canWrite())) {
					System.out.println("[!]No se ha abierto el archivo " + ficheroSalida.getAbsolutePath());
					System.exit(1);
			}
			
			
			
			//Validadcion del catálogo frente al esquema correspondiente
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(catalogoXSD);
		    Validator validator = schema.newValidator();
		    Source source = new StreamSource(catalogoXML.getAbsoluteFile());
		    validator.validate(source);
		    System.out.println("[+] El catalogo ha sido validado correctamente");
			
		
/********************************************************************************************************
* 			                	PASO 2: CONFIGURACION ANALIZADOR 										*
* *******************************************************************************************************/
					
			//Creacion y configuracion de factoria
			SAXParserFactory factoria = SAXParserFactory.newInstance();
			factoria.setNamespaceAware(true);			// Soporta espacio de nombres XML
			factoria.setValidating(true);				// Permite validacion de documentos	

			//Creacion del SAX Parser y del Analizador
			SAXParser saxParser= factoria.newSAXParser();
			AnalizadorXML analizadorXML = new AnalizadorXML(codCategoria);
			
			//Se pone a funcionar el Parse
			saxParser.parse(catalogoXML, analizadorXML);
			System.out.println("[+] Fin del analisis.");
			
/********************************************************************************************************
* 			                	PASO 3: EXTRACCION DE LOS DATOS JSON 										*
* *******************************************************************************************************/
			
			//Realizamos el analisis de los ficheros JSON lanzando los hilos correspondientes
			final int numDeNucleos = Runtime.getRuntime().availableProcessors();
			System.out.println ("\nSe va a crear un pool de hilos analizadoresJSON para que como máximo haya " + numDeNucleos + " hilos en ejecución simultaneamente.");
			
			final ExecutorService ejecutor = Executors.newFixedThreadPool(numDeNucleos);
			int numTrabajadores=0;
			Map<String, List<Map<String,String>>> mDatasetConcepts = new HashMap<String, List< Map <String, String>>> ();
			
			for(String fich: analizadorXML.getDatasets().keySet()) {
				ejecutor.execute(new Trabajador(fich, analizadorXML.getConcepts(), mDatasetConcepts));
				numTrabajadores++;
			}
			
			System.out.print ("En total se van a ejecutar "+numTrabajadores+" trabajadores en el pool. Esperar a que terminen\n\n");
			
			// Esperar a que terminen todos los trabajadores
			ejecutor.shutdown();	// Cerrar el ejecutor cuando termine el último trabajador
			// Cada 10 segundos mostrar cuantos trabajadores se han ejecutado y los que quedan
			while (!ejecutor.awaitTermination(10, TimeUnit.SECONDS)) {
				System.out.print("Esperando a los trabajadores que quedan ");
			}
			// Mostrar todos los trabajadores que se han ejecutado. Debe coincidir con los creados
			System.out.println("\nYa han terminado los trabajadores");
			
			
			
/********************************************************************************************************
* 			                	PASO 4: CREACION DEL FICHERO DE SALIDA 									*
* *******************************************************************************************************/
			
			//Creacion del writer y escribimos en en el fichero lo obtenido en la busqueda
			FileWriter writer = new FileWriter(ficheroSalida);
			writer.write(GenerarXML.generar(analizadorXML.getDatasets(), analizadorXML.getConcepts(), codCategoria, mDatasetConcepts));
			writer.flush();
			writer.close();
			System.out.println("[+] Se ha rellenado el fichero de salida con los datos deseados");
			
			//Validadcion del fichero
			SchemaFactory schemaFactorySalida = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schemaSalida = schemaFactorySalida.newSchema(ficheroXSD);
		    Validator validatorSalida = schemaSalida.newValidator();
		    Source sourceSalida = new StreamSource(ficheroSalida.getAbsoluteFile());
		    validatorSalida.validate(sourceSalida);
		    System.out.println("[+] El fichero de salida ha sido validado correctamente");
			
/********************************************************************************************************
* 			                	PASO 5: EVALUACION XPATH Y GENERAR FICHERO JSON							*
* *******************************************************************************************************/
		   

 	   	  AnalizadorXPATH analizadorXPATH = new AnalizadorXPATH(rutaFicheroSalida);
		  analizadorXPATH.evaluarXPATH();
		  System.out.println("[+] El fichero XML ha sido evaluado correctamente");
		  lPropiedades = analizadorXPATH.getEvaluar(); GenerarJSON.generar(args[5], lPropiedades); 
		  System.out.println("[+] El fichero JSON ha sido generado correctamente");
		 
			
			
/********************************************************************************************************
* 			                				FIN EL MAIN													*
* *******************************************************************************************************/
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	// TODO: métodos privados auxiliares que se consideren necesarios
		private static void comprobarArgumentosExpresionesRegulares (String argumento0, String argumento1, String argumento2, String argumento3, String argumento4) {
			String regex1 = "^\\d{3,4}(-[A-Z0-9]{3,8})?$";
			String regex2 = ".*\\.xml$";
			String regex3 = ".*\\.xsd$";
			
	/**-------------------------------------------------------------- **/
			
			Pattern pattern = Pattern.compile(regex1);
			Matcher matcher = pattern.matcher(argumento0);

			if (!matcher.matches()) {
				System.out.println("[!] El argumento 0 no es un argumento valido. Valor: " + argumento0);
				System.exit(1);
			}
			
	/**-------------------------------------------------------------- **/

			pattern = Pattern.compile(regex2);
			matcher = pattern.matcher(argumento1);

			if (!matcher.matches()) {
				System.out.println("[!] El argumento 1 no es un argumento valido");
				System.exit(1);
			}
			
	/**-------------------------------------------------------------- **/

			pattern = Pattern.compile(regex3);
			matcher = pattern.matcher(argumento2);

			if (!matcher.matches()) {
				System.out.println("[!] El argumento 2 no es un argumento valido");
				System.exit(1);
			}
			
	/**-------------------------------------------------------------- **/

			pattern = Pattern.compile(regex2);
			matcher = pattern.matcher(argumento3);

			if (!matcher.matches()) {
				System.out.println("[!] El argumento 3 no es un argumento valido");
				System.exit(1);
			}
	/**-------------------------------------------------------------- **/

			pattern = Pattern.compile(regex3);
			matcher = pattern.matcher(argumento4);

			if (!matcher.matches()) {
				System.out.println("[!] El argumento 4 no es un argumento valido");
				System.exit(1);
			}
		}
}

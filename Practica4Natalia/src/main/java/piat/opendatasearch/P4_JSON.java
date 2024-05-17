package piat.opendatasearch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;
import java.util.regex.Matcher;


/**
 * @author Natalia Agüero Knauf 47230975S
 *
 */

/**
 * Clase principal de la aplicación de extracción de información del 
 * Portal de Datos Abiertos del Ayuntamiento de Madrid
 *
 */
public class P4_JSON {
	
	//ARGUMENTOS
	//018 src/main/java/catalogo/catalogo.xml src/main/java/files/ResultadosBusquedaP4.xsd src/main/java/files/salida.xml


	public static void main(String[] args) {
		
		// Verificar numero de argumentos correcto
		if (args.length!=4){
			String mensaje="ERROR: Argumentos incorrectos.";
			if (args.length>0)
				mensaje+=" He recibido estos argumentos: "+ Arrays.asList(args).toString()+"\n";
			mostrarUso(mensaje);
			System.exit(1);
		}		
		
		String codCategoria = args[0];
		String rutaDocXml = args[1];
		String rutaDocSchema = args[2];
		String rutaDocSalida = args[3];

		try {

//******************		VERIFICACION DE LOS ARGUMENTOS		*****************/

			//Verificacion del tipo de informacion que se espera de los argumentos
			comprobarArgumentosExpresionesRegulares(codCategoria, rutaDocXml, rutaDocSchema, rutaDocSalida);
			
			//Abro archivo XML y compruebo que sea un fichero de lectura
			File ficheroXML = new File(rutaDocXml);
			if (!(ficheroXML.exists() && ficheroXML.isFile() && ficheroXML.canRead())) {
					System.out.println("No se ha abierto el archivo " + ficheroXML.getAbsolutePath());
					System.exit(1);
			}
			//Abro el archivo XSD y compruebo que sea un fichero de lectura
			File ficheroXSD = new File(rutaDocSchema);
			if (!(ficheroXSD.exists() && ficheroXSD.isFile() && ficheroXSD.canRead())) {
					System.out.println("No se ha abierto el archivo " + ficheroXSD.getAbsolutePath());
					System.exit(1);
			}

			//Abro el fichero de salida XML y comruebo que sea un fichero de escritura
			
			File ficheroSalida = new File(rutaDocSalida);
			if (!(ficheroSalida.exists() && ficheroSalida.isFile() && ficheroSalida.canWrite())) {
					System.out.println("No se ha abierto el archivo " + ficheroSalida.getAbsolutePath());
					System.exit(1);
			}


			//CREO Y CONFIGURO UNA FACTORIA
			SAXParserFactory factoria = SAXParserFactory.newInstance();
			factoria.setNamespaceAware(true);	// Soporta espacio de nombres XML
			factoria.setValidating(true);		// Permite validacion de documentos
			

			//Creamos un SAX PARSER y un manejador que manejara los eventos SAX que se generan durante el procesamiento del archivo XML
			SAXParser saxParser= factoria.newSAXParser();
			ManejadorXML manejadorXML = new ManejadorXML(codCategoria);
			
			
	        //se realiza el parser del ficheroXML con el manejador
	        saxParser.parse(ficheroXML, manejadorXML); //permite encontrar los datos deseados y rellenar los mapas
			System.out.println("Fin del analisis.");
			
			//Realizamos el analisis de los ficheros JSON lanzando los hilos correspondientes
			final int numDeNucleos = Runtime.getRuntime().availableProcessors();
			System.out.println ("\nSe va a crear un pool de hilos analizadoresJSON para que como máximo haya " + numDeNucleos + " hilos en ejecución simultaneamente.");
			
			final ExecutorService ejecutor = Executors.newFixedThreadPool(numDeNucleos);
			int numTrabajadores=0;
			Map<String, List<Map<String,String>>> mDatasetConcepts = new HashMap<String, List< Map <String, String>>> ();
			
			for(String fich: manejadorXML.getDatasets().keySet()) {
				ejecutor.execute(new JSONDatasetParser(fich, manejadorXML.getConcepts(), mDatasetConcepts));
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
			
			
			//GENERAMOS EL FICHERO XML A PARTIR DEL PARSER (escribimos los datos de los mapas en el fichero xml)
			GenerarXML_P4 generarxml = new GenerarXML_P4();
			FileWriter writer = new FileWriter(ficheroSalida);
			writer.write(generarxml.generar(manejadorXML.getDatasets(), manejadorXML.getConcepts(), codCategoria, mDatasetConcepts));
			writer.flush();
			writer.close();
			System.out.println("Se ha rellenado el fichero de salida con los datos deseados");

			//VALIDACION DEL FICHERO DE SALIDA
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(ficheroXSD);
		    Validator validator = schema.newValidator();
		    Source source = new StreamSource(ficheroSalida.getAbsoluteFile());
		    validator.validate(source);
		    System.out.println("El fichero de salida ha sido validado correctamente");


		} catch (IOException e) {
			e.printStackTrace();
		}
		catch (SAXException e) {
			System.out.println("\nERROR: Probablemente el documento no se validó correctamente");
			e.printStackTrace();
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		// TODO
		/* 
		 * Validar los argumentos recibidos en main()
		 * Instanciar un objeto ManejadorXML pasando como parámetro el código de la categoría recibido en el primer argumento de main()
		 * Instanciar un objeto SAXParser e invocar a su método parse() pasando como parámetro un descriptor de fichero, cuyo nombre se recibió en el primer argumento de main(), y la instancia del objeto ManejadorXML 
		 * Invocar al método getConcepts() del objeto ManejadorXML para obtener un List<String> con las uris de los elementos <concept> cuyo elemento <code> contiene el código de la categoría buscado
		 * Invocar al método getDatasets() del objeto ManejadorXML para obtener un mapa con los datasets de la categoría buscada
		 * Crear el fichero de salida con el nombre recibido en el cuarto argumento de main()
		 * Volcar al fichero de salida los datos en el formato XML especificado por ResultadosBusquedaP3.xsd
		 * Validar el fichero generado con el esquema recibido en el tercer argumento de main()
		 */
		

		System.exit(0);
	}
	

	
	/**
	 * Muestra mensaje de los argumentos esperados por la aplicación.
	 * Deberá invocase en la fase de validación ante la detección de algún fallo
	 *
	 * @param mensaje  Mensaje adicional informativo (null si no se desea)
	 */
	private static void mostrarUso(String mensaje){
		Class<? extends Object> thisClass = new Object(){}.getClass();
		
		if (mensaje != null)
			System.err.println(mensaje+"\n");
		System.err.println(
				"Uso: " + thisClass.getEnclosingClass().getCanonicalName() + " <códigoCategoría> <ficheroCatalogo> <ficheroXSDsalida> <ficheroXMLSalida>\n" +
				"donde:\n"+
				"\t códigoCategoría:\t código de la categoría de la que se desea obtener datos\n" +
				"\t ficheroCatalogo:\t path al fichero XML con el catálogo de datos\n" +
				"\t ficheroXSDsalida:\t nombre del fichero que contiene el esquema contra el que se tiene que validar el documento XML de salida\n"	+
				"\t ficheroXMLSalida:\t nombre del fichero XML de salida\n"
				);				
	}
	
	private static void comprobarArgumentosExpresionesRegulares (String argumento0, String argumento1, String argumento2, String argumento3) {
		String regex1 = "^\\d{3,4}(-[A-Z0-9]{3,8})?$";
		String regex2 = ".*\\.xml$";
		String regex3 = ".*\\.xsd$";

		Pattern pattern = Pattern.compile(regex1);
		Matcher matcher = pattern.matcher(argumento0);

		if (!matcher.matches()) {
			System.out.println("El argumento 0 no es un argumento valido. Valor: " + argumento0);
			System.exit(1);
		}

		pattern = Pattern.compile(regex2);
		matcher = pattern.matcher(argumento1);

		if (!matcher.matches()) {
			System.out.println("El argumento 1 no es un argumento valido");
			System.exit(1);
		}

		pattern = Pattern.compile(regex3);
		matcher = pattern.matcher(argumento2);

		if (!matcher.matches()) {
			System.out.println("El argumento 2 no es un argumento valido");
			System.exit(1);
		}

		pattern = Pattern.compile(regex2);
		matcher = pattern.matcher(argumento3);

		if (!matcher.matches()) {
			System.out.println("El argumento 3 no es un argumento valido");
			System.exit(1);
		}
	}
}

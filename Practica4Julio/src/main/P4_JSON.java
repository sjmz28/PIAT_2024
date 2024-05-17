package main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

/**
 * @author Ruben Agustin Gonzalez, 52063864Y
 *
 */

/**
 * Clase principal de la aplicación de extracción de información del 
 * Portal de Datos Abiertos del Ayuntamiento de Madrid
 *
 */
public class P4_JSON {


	public static void main(String[] args) {
		
		// Verificar nº de argumentos correcto
		if (args.length!=4){
			String mensaje="ERROR: Argumentos incorrectos.";
			if (args.length>0)
				mensaje+=" He recibido estos argumentos: "+ Arrays.asList(args).toString()+"\n";
			mostrarUso(mensaje);
			System.exit(1);
		}		
		
		String codigoCategoria = args[0]; // 018
		String rutaAcatalogo   = args[1];// ./src/catalogo/catalogo.xml
		String rutaAesquema    = args[2];// ./src/ResultadosBusquedaP4.xsd
		String rutaAxmlSalida  = args[3];// ./src/salida.xml
		
		// Verificacion de argumentos
		if(!verificacionArgumentos(codigoCategoria, rutaAcatalogo, rutaAesquema, rutaAxmlSalida)) System.exit(1);
		
		List<String> concepts;
		Map<String, Map<String, String>> datasetsMap;
		
		ManejadorXML manejador;
		SAXParserFactory factory;
		SAXParser saxParser;
		
		
		try {
			manejador = new ManejadorXML(codigoCategoria);
			
			factory   = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true); 			// Support for XML namespaces
			saxParser = factory.newSAXParser();
			
			saxParser.parse(rutaAcatalogo, manejador);
			concepts    = manejador.getConcepts();
			datasetsMap = manejador.getDatasets();
			
			// P4: Crear mapa donde se almacenaran los concepts de los datasets. Thread-Safe
			var mDatasetConcepts = new ConcurrentHashMap<String, List<Map<String,String>>>();
			
			// P4: llamada a getDatasetConcepts()
			mDatasetConcepts = (ConcurrentHashMap<String, List<Map<String, String>>>) getDatasetConcepts(concepts, datasetsMap);
			
			String out = GenerarXML.generar(concepts, datasetsMap, codigoCategoria, mDatasetConcepts);
			Files.write(Path.of(rutaAxmlSalida), out.getBytes(), StandardOpenOption.WRITE);
			
			//Validar xml
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

			// create a schema object from the XSD file
			Schema schema = schemaFactory.newSchema(new File(rutaAesquema));

			// create a validator object from the schema
			Validator validator = schema.newValidator();

			// validate the XML file using the validator
			validator.validate(new StreamSource(new File(rutaAxmlSalida)));


		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Todo ok");
		
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
	/**
	 * Devuelve true si los argumentos son validos, false si no. Debe invocarse antes de utilizar los argumentos.
	 * Crea el archivo con el nombre que se pase como cuarto parametro si aun no existe.
	 * 
	 * @param codigoCategoria  Primer argumento. Debe empezar por 3 o 4 caracteres numericos, seguidos, opcionalmente, de un guion y de 3 a 8 caracteres numericos
	 * @param rutaAcatalogo    Segundo argumento. Debe acabar por ".xml" y tener permiso de lectura.
	 * @param rutaAesquema     Tercer argumento. Debe acabar por ".xsd" y tener permiso de lectura.
	 * @param rutaAxmlSalida   Cuarto argumento. Debe acabar por ".xml" y tener permiso de escritura.
	 * @return boolean		   Si son argumenos validos
	 */
	private static boolean verificacionArgumentos(String codigoCategoria, String rutaAcatalogo, String rutaAesquema, String rutaAxmlSalida) {
		boolean temp = true;
		
		String codigoCategoriaTipo = "(^[0-9]{3,4}-[0-9A-z]{3,8}$)|^[0-9]{3,4}$"; // 3 o 4 numeros, o 3 o 4 numeros y un guion y de 3 a 8 caracteres
		String archivoXml = ".*\\.xml$";										  // Acaba por xml
		String archivoXsd = ".*\\.xsd$";									  	  // Acaba por xsd
		
		temp = Pattern.matches(codigoCategoriaTipo, codigoCategoria) 
				&& Pattern.matches(archivoXml, rutaAcatalogo) && Pattern.matches(archivoXml, rutaAxmlSalida)
				&& Pattern.matches(archivoXsd, rutaAesquema);
		
		if (!temp) {
			mostrarUso("Error en alguno de los argumentos de entrada");
			return temp;
		}
		
		if(!Files.isReadable(Path.of(rutaAcatalogo))){
			System.out.println(rutaAcatalogo + " no tiene permisos de lectura");
			temp = false;
		}
		
		if(!Files.isReadable(Path.of(rutaAesquema))){
			System.out.println(rutaAesquema + " no tiene permisos de lectura");
			temp = false;
		}
		
		if(Files.exists(Path.of(rutaAxmlSalida))) {
			if(!Files.isWritable(Path.of(rutaAxmlSalida))){
				System.out.println(rutaAxmlSalida + " no tiene permisos de escritura");
				temp = false;
			} else {
				try {
					Files.delete(Path.of(rutaAxmlSalida));
					Files.createFile(Path.of(rutaAxmlSalida));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			try {
				Files.createFile(Path.of(rutaAxmlSalida));
			} catch (IOException e) {
				System.out.println("No se pudo crear el fichero" + rutaAxmlSalida);
				e.printStackTrace();
				temp = false;
			}
		}
		return temp;
	}
	
	private static Map<String, List<Map<String, String>>> getDatasetConcepts(List<String> lConcepts, Map<String, Map<String, String>> mDatasets){
		// Objeto que devolverá este método
		var mDatasetConcepts = new ConcurrentHashMap<String, List<Map<String,String>>>();
		// Num de nucleos del ordenador que ejecuta este código
		int numDeNucleos = Runtime.getRuntime().availableProcessors();
		// Crea un pool del tamaño del número de núcleos
		ExecutorService ejecutor = Executors.newFixedThreadPool(numDeNucleos);
		
		// Crea tantos hilos como entradas haya en el mapa de mDataset
		mDatasets.forEach((key, value) -> {
			Runnable trabajador = new JSONDatasetParser (key, lConcepts, mDatasetConcepts);
			ejecutor.execute(trabajador);
		});
		try {
			ejecutor.shutdown();
			ejecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return mDatasetConcepts;
	}
	
}

package piat;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import piat.XPATH_Evaluador.Propiedad;

/**
 * @author Sara Jiménez Muñoz 51512521L
 *
 * linea de argumentos
 * 0016-018 ./src/catalogo/catalogo.xml ./src/ResultadosBusquedaP4.xsd ./src/salida0016-018.xml ./src/salida0016-018.json
 */

public class P5_XPATH {

	public static void main(String[] args) {

		/********************************************************************************************************
		 * PASO 1: VERIFICACION DE ARGUMENTOS *
		 ******************************************************************************************************/
		if (args.length != 5) {
			System.out.println("[!] ERROR: Argumentos incorrectos.");
			mostrarUso(null);
			System.exit(1);
		}

		String codCategoria = args[0];
		String rutaXml = args[1];
		String rutaSchema = args[2];
		String xmlSalidaRuta = args[3];

		List<Propiedad> lPropiedades = new ArrayList<>();
		try {
			comprobarArgumentosExpresionesRegulares(codCategoria, rutaXml, rutaSchema, xmlSalidaRuta);

			File ficheroXML = new File(rutaXml);
			if (!(ficheroXML.exists() && ficheroXML.isFile() && ficheroXML.canRead())) {
				System.out.println("[!] No se ha abierto el archivo " + ficheroXML.getAbsolutePath());
				System.exit(1);
			}

			File ficheroXSD = new File(rutaSchema);
			if (!(ficheroXSD.exists() && ficheroXSD.isFile() && ficheroXSD.canRead())) {
				System.out.println("[!] No se ha abierto el archivo " + ficheroXSD.getAbsolutePath());
				System.exit(1);
			}

			File ficheroSalida = new File(xmlSalidaRuta);
			if (!(ficheroSalida.exists() && ficheroSalida.isFile() && ficheroSalida.canWrite())) {
				System.out.println("[!]No se ha abierto el archivo " + ficheroSalida.getAbsolutePath());
				System.exit(1);
			}

			/********************************************************************************************************
			 * PASO 2: CONFIGURACION MANEJADOR *
			 *******************************************************************************************************/

			List<String> listaConceptos; // Para facilitar la llamada a la fincion obtenerConceptos Dataset
			Map<String, Map<String, String>> mapaDatasets;

			ManejadorXML manejadorXML = new ManejadorXML(codCategoria);

			SAXParserFactory saxFactory = SAXParserFactory.newInstance();
			saxFactory.setNamespaceAware(true);
			SAXParser saxParser = saxFactory.newSAXParser();

			saxParser.parse(rutaXml, manejadorXML);
			System.out.println("[+] Fin del analisis.");

			listaConceptos = manejadorXML.getConcepts();
			mapaDatasets = manejadorXML.getDatasets();

			var mapaConceptosDataset = new ConcurrentHashMap<String, List<Map<String, String>>>();

			mapaConceptosDataset = (ConcurrentHashMap<String, List<Map<String, String>>>) obtenerConceptosDataset(
					listaConceptos, mapaDatasets);

			/********************************************************************************************************
			 * PASO 3: CREACION DEL FICHERO DE SALIDA *
			 ********************************************************************************************************/

			// Creacion del writer y escribimos en en el fichero lo obtenido en la busqueda
			GenerarXML generarxml = new GenerarXML();
			FileWriter writer = new FileWriter(ficheroSalida);
			writer.write(generarxml.generar(listaConceptos, mapaDatasets, codCategoria, mapaConceptosDataset));
			writer.flush();
			writer.close();
			System.out.println("[+] Se ha rellenado el fichero de salida con los datos deseados");

			// Validadcion del fichero
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(ficheroXSD);

			Validator validator = schema.newValidator();
			Source source = new StreamSource(ficheroSalida.getAbsoluteFile());
			validator.validate(source);
			System.out.println("[+] El fichero de salida ha sido validado correctamente");

			/********************************************************************************************************
			 * PASO 4: EVALUACION XPATH *
			 ********************************************************************************************************/
			lPropiedades = XPATH_Evaluador.evaluar(xmlSalidaRuta);
			GenerarJSON.generar(args[4], lPropiedades);

		} catch (Exception excepcion) {
			excepcion.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}

	/********************************************************************************************************
	 * FIN DEL MAIN *
	 ********************************************************************************************************/
	private static void mostrarUso(String mensaje) {
		Class<? extends Object> thisClass = new Object() {
		}.getClass();

		if (mensaje != null)
			System.err.println(mensaje + "\n");
		System.err.println(
				"Uso: " + thisClass.getEnclosingClass().getCanonicalName()
						+ " <códigoCategoría> <ficheroCatalogo> <ficheroXSDsalida> <ficheroXMLSalida>\n" +
						"donde:\n" +
						"\t códigoCategoría:\t código de la categoría de la que se desea obtener datos\n" +
						"\t ficheroCatalogo:\t path al fichero XML con el catálogo de datos\n" +
						"\t ficheroXSDsalida:\t nombre del fichero que contiene el esquema contra el que se tiene que validar el documento XML de salida\n"
						+
						"\t ficheroXMLSalida:\t nombre del fichero XML de salida\n");
	}

	private static void comprobarArgumentosExpresionesRegulares(String argumento0, String argumento1, String argumento2,
			String argumento3) {
		String regex1 = "^\\d{3,4}(-[A-Z0-9]{3,8})?$";
		String regex2 = ".*\\.xml$";
		String regex3 = ".*\\.xsd$";

		/** -------------------------------------------------------------- **/

		Pattern pattern = Pattern.compile(regex1);
		Matcher matcher = pattern.matcher(argumento0);

		if (!matcher.matches()) {
			System.out.println("[!] El argumento 0 no es un argumento valido. Valor: " + argumento0);
			System.exit(1);
		}

		/** -------------------------------------------------------------- **/

		pattern = Pattern.compile(regex2);
		matcher = pattern.matcher(argumento1);

		if (!matcher.matches()) {
			System.out.println("[!] El argumento 1 no es un argumento valido");
			System.exit(1);
		}

		/** -------------------------------------------------------------- **/

		pattern = Pattern.compile(regex3);
		matcher = pattern.matcher(argumento2);

		if (!matcher.matches()) {
			System.out.println("[!] El argumento 2 no es un argumento valido");
			System.exit(1);
		}

		/** -------------------------------------------------------------- **/

		pattern = Pattern.compile(regex2);
		matcher = pattern.matcher(argumento3);

		if (!matcher.matches()) {
			System.out.println("[!] El argumento 3 no es un argumento valido");
			System.exit(1);
		}
	}

	private static Map<String, List<Map<String, String>>> obtenerConceptosDataset(List<String> listaConceptos,
			Map<String, Map<String, String>> mapaDatasets) {
		var mapaConceptosDataset = new ConcurrentHashMap<String, List<Map<String, String>>>();
		int cantidadNucleos = Runtime.getRuntime().availableProcessors();
		ExecutorService gestor = Executors.newFixedThreadPool(cantidadNucleos);

		mapaDatasets.forEach((clave, valor) -> {
			Runnable tarea = new JSONDatasetParser(clave, listaConceptos, mapaConceptosDataset);
			gestor.execute(tarea);
		});
		try {
			gestor.shutdown();
			gestor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException excepcion) {
			excepcion.printStackTrace();
		}
		return mapaConceptosDataset;
	}

}

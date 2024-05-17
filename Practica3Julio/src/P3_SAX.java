

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
 * @author Sara Jimenez Muñoz 51512521L
 */

/**
 * linea de argumentos
 * 
 * 018 ./src/catalogo/catalogo.xml ./src/ResultadosBusquedaP3.xsd ./src/salida.xml
 */

/**
 * Clase principal de la aplicación de extracción de información del 
 * Portal de Datos Abiertos del Ayuntamiento de Madrid
 *
 */
public class P3_SAX {


	public static void main(String[] args) {
		
/********************************************************************************************************
 * 			                	PASO 1: VERIFICACION DE ARGUENTOS										*
******************************************************************************************************/	
		
		// Verificar numero de argumentos correcto
		if (args.length!=4){
			System.out.println("[!] ERROR: Argumentos incorrectos.");
			System.exit(1);
		}		
		
		String codCategoria  = args[0];
		String rutaXml       = args[1];
		String rutaSchema    = args[2];
		String rutaFicheroSalida = args[3];

	try {

			//Verificacion de las expresiones regulares
			comprobarArgumentosExpresionesRegulares(codCategoria, rutaXml, rutaSchema, rutaFicheroSalida);
			
			//Comprobaci�n de que los ficheros existen y tienen permisos de escritura y lectura
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

			
			File ficheroSalida = new File(rutaFicheroSalida);
			if (!(ficheroSalida.exists() && ficheroSalida.isFile() && ficheroSalida.canWrite())) {
					System.out.println("[!]No se ha abierto el archivo " + ficheroSalida.getAbsolutePath());
					System.exit(1);
			}

/********************************************************************************************************
* 			                	PASO 2: CONFIGURACION MANEJADOR 										*
* *******************************************************************************************************/
			
			//Creacion y configuracion de factoria
			SAXParserFactory factoria = SAXParserFactory.newInstance();
			factoria.setNamespaceAware(true);			// Soporta espacio de nombres XML
			factoria.setValidating(true);				// Permite validacion de documentos
				

			//Creacion del SAX Parser y del Manejador
			SAXParser saxParser= factoria.newSAXParser();
			ManejadorXML manejadorXML = new ManejadorXML(codCategoria);
			
			
	        //Se pone a funcionar el Parse
	        saxParser.parse(ficheroXML, manejadorXML);
			System.out.println("[+] Fin del analisis.");
			
/********************************************************************************************************
* 			                	PASO 3: CREACION DEL FICHERO DE SALIDA 									*
* *******************************************************************************************************/
			
			//Creacion del writer y escribimos en en el fichero lo obtenido en la busqueda
			GenerarXML generarxml = new GenerarXML();
			FileWriter writer = new FileWriter(ficheroSalida);
			writer.write(generarxml.generar(manejadorXML.getDatasets(), manejadorXML.getConcepts(), codCategoria));
			writer.flush();
			writer.close();
			System.out.println("[+] Se ha rellenado el fichero de salida con los datos deseados");

			//Validadcion del fichero
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(ficheroXSD);
		    Validator validator = schema.newValidator();
		    Source source = new StreamSource(ficheroSalida.getAbsoluteFile());
		    validator.validate(source);
		    System.out.println("[+] El fichero de salida ha sido validado correctamente");


		} catch (IOException e) {
			e.printStackTrace();
		}
		catch (SAXException e) {
			e.printStackTrace();
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		

		System.exit(0);
	}
	
	/********************************************************************************************************
	* 			                				FIN DEL MAIN												*
	* *******************************************************************************************************/
	
	/**
	 * Muestra mensaje de los argumentos esperados por la aplicación.
	 * Deberá invocase en la fase de validación ante la detección de algún fallo
	 *
	 * @param mensaje  Mensaje adicional informativo (null si no se desea)
	 */
	
	
	private static void comprobarArgumentosExpresionesRegulares (String argumento0, String argumento1, String argumento2, String argumento3) {
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
	}
	
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
}

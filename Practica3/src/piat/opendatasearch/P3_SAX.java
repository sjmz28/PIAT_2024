package piat.opendatasearch;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
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
 * 0016-018 ./src/Catalogo/catalogo.xml ./src/Catalogo/catalogo.xsd ./src/Catalogo/salida.xml ./src/Catalogo/ResultadosBusquedaP3.xsd
 ********************************************************************/
public class P3_SAX
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
		if (args.length!=5){
			System.out.println("[!] ERROR: Argumentos incorrectos.");
			throw new IllegalArgumentException ( USO );
			}		
		String codCategoria      = args[0]; // Criterio de búsqueda
		
		String rutaCatalogoXml   = args[1]; //Ruta al documento XML que contiene el catálogo a procesar
		
		String rutaCatalogoXSD   = args[2]; //Ruta al documento esquema contra el que se tendrá que validar el 
										    //documento XML de entrada.
		String rutaFicheroSalida = args[3]; //Ruta al documento XML de salida en el que se almacenará el resultado 
										    //de la búsqueda
		String rutaSchema        = args[4]; //Ruta al documento esquema contra el que se tendrá que validar
									        // el documento XML generado
		
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
			
			
			//Validadcion del catálogo frente al esquema correspondiente
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(catalogoXSD);
		    Validator validator = schema.newValidator();
		    Source source = new StreamSource(catalogoXML.getAbsoluteFile());
		    validator.validate(source);
		    System.out.println("[+] El catalogo ha sido validado correctamente");
			
		
/********************************************************************************************************
* 			                	PASO 2: CONFIGURACION MANEJADOR 										*
* *******************************************************************************************************/
					
			//Creacion y configuracion de factoria
			SAXParserFactory factoria = SAXParserFactory.newInstance();
			factoria.setNamespaceAware(true);			// Soporta espacio de nombres XML
			factoria.setValidating(true);				// Permite validacion de documentos	

			//Creacion del SAX Parser y del Manejador
			SAXParser saxParser= factoria.newSAXParser();
			AnalizadorXML analizadorXML = new AnalizadorXML(codCategoria);
			
			//Se pone a funcionar el Parse
			saxParser.parse(catalogoXML, analizadorXML);
			System.out.println("[+] Fin del analisis.");
			
			
/********************************************************************************************************
* 			                	PASO 3: CREACION DEL FICHERO DE SALIDA 									*
* *******************************************************************************************************/
			
			//Creacion del writer y escribimos en en el fichero lo obtenido en la busqueda
			GenerarXML generarxml = new GenerarXML();
			FileWriter writer = new FileWriter(ficheroSalida);
			writer.write(generarxml.generar(analizadorXML.getDatasets(), analizadorXML.getConcepts(), codCategoria));
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
			
			
		
		}catch (Exception e) {
			e.printStackTrace();
		}
		

		
		// TODO: validar el catálogo frente al esquema correspondiente : HECHO
		// TODO: procesar el catálogo usando un objeto AnalizadorXML: HECHO
		// TODO: recoger la información pertinente del AnalizadorXML: HECHO
		// TODO: generar el documento XML de salida por medio de GenerarXML
		// TODO: validar el documento XML generado frente al esquema
		// TODO: escribir el documento XML generado en el fichero de salida
		// En caso de detectar algún error, se debe lanzar una excepción
		// Se recomienda el uso de métodos privados auxiliares
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

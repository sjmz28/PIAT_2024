package piat.opendatasearch;

import java.util.Map;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;

/**
 * @author Rodrigo Vega Robles rodrigo.vega.robles@alumnos.upm.es
 */

/**
 * Clase que implementa el analizador XML usando un SAXParser.
 * Extiende la clase DefaultHandler para que pueda ser usada por un SAXParser,
 * por lo que tiene que sobreescribir sus métodos públicos.
 * Implementa la interfaz ParserCatálogo para que pueda ser usada por la
 * aplicación para obtener la información de concepts y datasets pertinentes.
 */
public final class AnalizadorXML extends DefaultHandler implements ParserCatálogo {
	// Analizador SAX que se usará para analizar el catálogo
	private final SAXParser saxParser;
	// Categoría que se usará para seleccionar la información pertinente
	private final String cat;
	// Stream para leer el catálogo
	private final InputStream doc;
	// Colección en la que anotar los concepts pertinentes a devolver
	private final Collection<String> concepts;
	// Mapa en el que registrar los datasets pertinentes a devolver
	private final Map<String,Map<String,String>> datasets;

	private StringBuilder valor;
	// TODO: añadir los campos que se necesiten
	int numConcepts = 0;
	boolean valido=false;
		//variables temporales para los concept
	private String tempURL;
			//variables temporales para los dataset
	private String tempId;
	private Map<String, String> tempMap;
	
	private boolean insideDataset = false;
	/**
	 * Constructor de la clase.
	 * @param cat la categoría que se usará para seleccionar la
	 * información pertinente.
	 * @param doc el documento XML que contiene el catálogo a analizar.
	 */
	public AnalizadorXML ( String cat, InputStream doc ) throws ParserConfigurationException, SAXException {
		// TODO: codificar el constructor
		tempMap = new HashMap <String, String> ();
		this.cat = cat;
        this.doc = doc;
        this.saxParser = SAXParserFactory.newInstance().newSAXParser();
        this.concepts = new ArrayList<>();
        this.datasets = new HashMap<>();
        this.valor = new StringBuilder();
	}

	/**
	 * Analiza el contenido del catálogo y obtiene la información
	 * pertinente. Debe ser invocado una sola vez.
	 */
	public void analizar () throws SAXException, IOException {
		// TODO: código adicional si se considera necesario
		saxParser.parse ( doc, this );	
	}

	//===========================================================
	// Métodos a implementar de la interfaz ParserCatálogo
	//===========================================================

	@Override
	public Collection<String> getConcepts ()
	{
		// TODO: código adicional si se considera necesario
		return concepts;
	}

	@Override
	public Map<String,Map<String,String>> getDatasets ()
	{
		// TODO: código adicional si se considera necesario
		return datasets;
	}

	//===========================================================
	// Métodos a implementar de SAX DocumentHandler
	//===========================================================

	@Override
	public final void startDocument() throws SAXException
	{
		super.startDocument();
		// TODO: código adicional si se considera necesario
		System.out.println("[+] Se ha comenzado a analizar el documento gracias al Analizador...");
	}
	
	@Override
	public final void endDocument() throws SAXException
	{
		super.endDocument();
		// TODO: código adicional si se considera necesario
		System.out.println("[+] Se ha terminado de analizar el documento gracias al Analizador...");
	}
		
	@Override
	public final void startElement (String nsURI, String localName, String qName, Attributes atts ) throws SAXException{
		super.startElement ( nsURI, localName, qName, atts );
		// TODO: código adicional si se considera necesario
		switch ( localName ){
			case "dataset":
	            tempMap.clear();
	            tempId = atts.getValue("id");
	            insideDataset = true;
	            break;
			case "concept":
	            tempURL = atts.getValue("id");
			numConcepts++;
	            break;
	        default:
	            borrarValor();
		}
	}
	
	@Override
	public final void endElement (String uri, String localName, String qName ) throws SAXException {
		super.endElement ( uri, localName, qName );
		// TODO: código adicional si se considera necesario
		switch ( localName ){
			case "dataset":
	            if (insideDataset) {
	                datasets.put(tempId, new HashMap<>(tempMap)); // Agregar tempMap al datasets
	            }
	            insideDataset = false;
	            break;
			case "title":
			case "description":
			case "keyword":
			case "theme":
			case "publisher":
			case "refConcepts":
				anotarDato ( localName ); 
				break;
				
	        case "concept":
	            concepts.add(tempURL); // Agregar tempURL a concepts
	            numConcepts--;
	            break;
	        case "category":
	        	validar(); 
	        	break;
	        case "label":
	        	anotarDato ( localName ); 
	        	break;
			default:;
		}
	}
	
	
	private void anotarDato ( String elem ){
		tempMap.put ( elem, valor.toString() );
	}

	private void validar ()
	{
		final String val = valor.toString();
		valido = cat.equals ( val );
	}
	@Override
	public final void characters (char chars[], int start, int length ) throws SAXException {
		super.characters ( chars, start, length );
		// TODO: código adicional si se considera necesario
		valor.append ( chars, start, length );
	}
	
	private void borrarValor ()
	{
		final int len = valor.length();
		final int fin = (len>0)?len:0;
		valor.delete ( 0, fin );
	}
}

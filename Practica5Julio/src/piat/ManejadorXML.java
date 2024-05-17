package piat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Arturo Salvador Mayor 51558282X
 * @author Sara Jiménez Muñoz 51512521L
 *
 */

/**
 * Clase estática, que debe implementar la interfaz ParserCatalogo 
 * Hereda de DefaultHandler por lo que se deben sobrescribir sus métodos para procesar documentos XML 
 *
 */
public class ManejadorXML extends DefaultHandler implements ParserCatalogo {
	private String sNombreCategoria;	// Nombre de la categoria
	private List<String> lConcepts; 	// Lista con los uris de los elementos <concept> que pertenecen a la categoria
	private Map <String, Map<String,String>> hDatasets;	// Mapa con informacion de los dataset que pertenecen a la categoria

	//variables temporales para los concept
	private String tmpURL;
	
	//variables temporales para los dataset
	private String tmpId;
	private Map<String, String> tmpMap;
	
	//variables internas
	private boolean insideCode = false;
	private boolean insideTitle = false;
	private boolean insideDescription = false;
	private boolean insideTheme = false;
	
	private Integer conceptCodigoEncontrado = null;
	private String codigo = null;
	private int numeroConcepts = 0;
	
	private boolean insideDataset = false;
	private boolean datasetValido = false;
	/**  
	 * @param sCodigoConcepto codigo de la categoria a procesar
	 * @throws SAXException, ParserConfigurationException 
	 */
	public ManejadorXML (String sCodigoConcepto) throws SAXException, ParserConfigurationException {
		sNombreCategoria = sCodigoConcepto;
		lConcepts = new ArrayList<String>();
		hDatasets = new HashMap <String, Map<String, String>> ();
		tmpMap = new HashMap <String, String> ();
	}

	 //===========================================================
	 // Métodos a implementar de la interfaz ParserCatalogo
	 //===========================================================

	/**
	 * <code><b>getConcepts</b></code>
	 *	Devuelve una lista con información de los <code><b>concepts</b></code> resultantes de la búsqueda. 
	 * <br> Cada uno de los elementos de la lista contiene la <code><em>URI</em></code> del <code>concept</code>
	 * 
	 * <br>Se considerarán pertinentes el <code><b>concept</b></code> cuyo código
	 *  sea igual al criterio de búsqueda y todos sus <code>concept</code> descendientes.
	 *  
	 * @return
	 * - List  con la <em>URI</em> de los concepts pertinentes.
	 * <br>
	 * - null  si no hay concepts pertinentes.
	 * 
	 */
	@Override	
	public List<String> getConcepts() {
		if (!lConcepts.isEmpty()) {
			return lConcepts;
		}
		return null;
		
	}

	/**
	 * <code><b>getDatasets</b></code>
	 * 
	 * @return Mapa con información de los <code>dataset</code> resultantes de la búsqueda.
	 * <br> Si no se ha realizado ninguna  búsqueda o no hay dataset pertinentes devolverá el valor <code>null</code>
	 * <br> Estructura de cada elemento del map:
	 * 		<br> . <b>key</b>: valor del atributo ID del elemento <code>dataset</code>con la cadena de la <code><em>URI</em></code>  
	 * 		<br> . <b>value</b>: Mapa con la información a extraer del <code>dataset</code>. Cada <code>key</code> tomará los valores <em>title</em>, <em>description</em> o <em>theme</em>, y <code>value</code> sus correspondientes valores.

	 * @return
	 *  - Map con información de los <code>dataset</code> resultantes de la búsqueda.
	 *  <br>
	 *  - null si no hay datasets pertinentes.  
	 */	
	@Override
	public Map<String, Map<String, String>> getDatasets() {
		if (!hDatasets.isEmpty()) {
			return hDatasets;
		}
		return null;
	}
	

	 //===========================================================
	 // Métodos a implementar de SAX DocumentHandler
	 //===========================================================
	
	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		System.out.println("Se ha comenzado a analizar el documento gracias al Manejador...");
	}

	
	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
		System.out.println("Se ha terminado de analizar el documento gracias al Manejador...");	
	}


	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		//super.startElement(uri, localName, qName, attributes);
		
		if (qName.equalsIgnoreCase("concept")) {
			//estamos dentro de un elemento concept, donde hay otro concept
			tmpURL = attributes.getValue("id");
			numeroConcepts++;
		} else if (qName.equalsIgnoreCase("dataset")) {
			tmpMap.clear();
			tmpId = attributes.getValue("id");
			insideDataset = true;
		} else if (qName.equalsIgnoreCase("title")) {
			insideTitle = true;
		} else if (qName.equalsIgnoreCase("description")) {
			insideDescription = true;
		} else if (qName.equalsIgnoreCase("theme")) {
			insideTheme = true;
		} else if (qName.equalsIgnoreCase("code")) {
			insideCode = true;			
		}
		
				
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		switch(qName) {
			case "concept":
				if (insideDataset) {
					for (String a : lConcepts) {
						if (a.equals(tmpURL)) {
							datasetValido = true;
						}
					}
				} else if ((!Objects.isNull(conceptCodigoEncontrado))) {
					if (numeroConcepts > conceptCodigoEncontrado && !lConcepts.contains(tmpURL)) {
					lConcepts.add(tmpURL);
					} else if (numeroConcepts == conceptCodigoEncontrado) {
						conceptCodigoEncontrado = null;
					}
				}
					numeroConcepts--;
				break;
			case "dataset":
				if (datasetValido) {
					hDatasets.put(tmpId, tmpMap);
				}
				insideDataset = false;
				datasetValido = false;
				break;
			case "code":
				if (codigo.equalsIgnoreCase(sNombreCategoria)) {
					conceptCodigoEncontrado = numeroConcepts;
					lConcepts.add(tmpURL);
				}
				insideCode = false;
				break;
				
			case "title":
				insideTitle = false;
				break;
			case "description":
				insideDescription = false;
				break;
			case "theme":
				insideTheme = false;
				break;
		}		
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		super.characters(ch, start, length);
		if (insideCode) {
			codigo = new String(ch, start, length);
		} else if (insideTitle)  {
			tmpMap.put("title", new String(ch, start, length));
		} else if (insideDescription) {
			tmpMap.put("description", new String(ch, start, length));
		} else if (insideTheme) {
			tmpMap.put("theme", new String(ch, start, length));
		}
	}

}

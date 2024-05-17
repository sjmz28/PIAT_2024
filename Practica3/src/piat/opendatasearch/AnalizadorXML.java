package piat.opendatasearch;



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
 * @author Sara Jimenez Munoz 51512521L
 *
 */

/**
 * Clase estática, que debe implementar la interfaz ParserCatalogo 
 * Hereda de DefaultHandler por lo que se deben sobrescribir sus métodos para procesar documentos XML 
 *
 */
public class AnalizadorXML extends DefaultHandler implements ParserCatalogo {
	private String sNomCategoria;    	// Nombre de la categoria
	private List<String> concepts; 	// Lista con los uris de los elementos <concept> que pertenecen a la categoria
										// Mapa con informacion de los dataset que pertenecen a la categoria
	private Map <String, Map<String,String>> datasets;	

										//variables temporales para los concept
	private String tempURL;
										//variables temporales para los dataset
	private String tempId;
	private Map<String, String> tempMap;
	
										//variables internas
	private boolean insideCode		  = false;
	private boolean insideTitle 	  = false;
	private boolean insideDescription = false;
	private boolean insideTheme 	  = false;
	
	private Integer conceptCodEncontrado 	= null;
	private String  code				    = null;
	private int 	numConcepts			    = 0;
	
	private boolean insideDataset = false;
	private boolean datasetValido = false;
	/**  
	 * @param sCodConcepto codigo de la categoria a procesar
	 * @throws SAXException, ParserConfigurationException 
	 */
	public AnalizadorXML (String sCodConcepto) throws SAXException, ParserConfigurationException {
		sNomCategoria = sCodConcepto;
		concepts = new ArrayList<String>();						// Lista de Concepts
		datasets = new HashMap <String, Map<String, String>> ();	// HashMap de lod Datasets
		tempMap = new HashMap <String, String> ();					// Mapa temporal para guardar 
	}

	/********************************************************************************************************
	* 			               			 	METODOS DEL PARSER CATALOGO 									*
	* *******************************************************************************************************/

	/********************************************************************************************************
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
	 *********************************************************************************************************/
	
	@Override	
	public List<String> getConcepts() {
		if (!concepts.isEmpty()) {
			return concepts;
		}
		return null;
		
	}

	/**********************************************************************************************************
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
	 **********************************************************************************************************/
	
	@Override
	public Map<String, Map<String, String>> getDatasets() {
		if (!datasets.isEmpty()) {
			return datasets;
		}
		return null;
	}
	

	/********************************************************************************************************
	* 			               			 	METODOS DEL SAX DOCUMENT HANDLER 								*
	* *******************************************************************************************************/
	
	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		System.out.println("[+] Se ha comenzado a analizar el documento gracias al Manejador...");
	}

	
	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
		System.out.println("[+] Se ha terminado de analizar el documento gracias al Manejador...");	
	}


	/**
	 *  Analizamos que elemento ha sido detectado, no se hacen distinciones entre mayusculas y minusculas
	 *  el objetivo de esta funcion es controlar en que nivel se encuentra e ir preparando los mapas para
	 *  a�adir los diferentes elementos
	 **/
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
		if (qName.equalsIgnoreCase("concept")) { // tengo un concept, me guardo el id por si aca
			tempURL = attributes.getValue("id");
			numConcepts++;					// para controlar el nivel en el que estamos
		
			//caso del refConcept
		} else if (qName.equalsIgnoreCase("refConcept"))
		{	tempURL = attributes.getValue("id");
							// para controlar el nivel en el que estamos
			
		}else if (qName.equalsIgnoreCase("category")) {
			insideCode = true;			
		}else if (qName.equalsIgnoreCase("dataset")) {
			tempMap.clear();
			tempId = attributes.getValue("id");
			insideDataset = true;
			
		} else if (qName.equalsIgnoreCase("title")) {
			insideTitle = true;
			
		} else if (qName.equalsIgnoreCase("description")) {
			insideDescription = true;
			
		} else if (qName.equalsIgnoreCase("theme")) {
			insideTheme = true;
			
		} 
		
				
	}
	
	/**
	 * Se analiza en primer lugar cual es elemento que ha terminado, sin distinguir entre mayusculas o minusculas
	 * Esta funcion se encarga de annadir los elementos a los mapas para luego pasarlos al generador
	 **/
	@Override
	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		
		switch(qName) {
		
			
			case "concept":
				if (insideDataset) {
					for (String a : concepts) {
						if (a.equals(tempURL)) {
							datasetValido = true;
						}
					}
				} else if ((!Objects.isNull(conceptCodEncontrado))) {
					if (numConcepts > conceptCodEncontrado && !concepts.contains(tempURL)) {
					concepts.add(tempURL);
					} else if (numConcepts == conceptCodEncontrado) {
						conceptCodEncontrado = null;
					}
				}
					numConcepts--;
				break;
				
			case "refConcept":
				
				if (insideDataset) {
					for (String a : concepts) {
						if (a.equals(tempURL)) {
							datasetValido = true;
						}
					}
				} else if ((!Objects.isNull(conceptCodEncontrado))) {
					if (numConcepts > conceptCodEncontrado && !concepts.contains(tempURL)) {
					concepts.add(tempURL);
					} else if (numConcepts == conceptCodEncontrado) {
						conceptCodEncontrado = null;
					}
				}
					numConcepts--;
			
				break;
				
			case "dataset":
				if (datasetValido) {
					datasets.put(tempId, tempMap);
				}
				insideDataset = false;
				datasetValido = false;
				break;
				
			case "category":
				if (code.equalsIgnoreCase(sNomCategoria)) { //?
					conceptCodEncontrado = numConcepts;
					concepts.add(tempURL);
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
			code = new String(ch, start, length);
		} else if (insideTitle)  {
			tempMap.put("title", new String(ch, start, length));
		} else if (insideDescription) {
			tempMap.put("description", new String(ch, start, length));
		} else if (insideTheme) {
			tempMap.put("theme", new String(ch, start, length));
		}
	}

}

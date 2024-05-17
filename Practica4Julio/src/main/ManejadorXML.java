package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Ruben Agustin Gonzalez 52063864Y
 *
 */

/**
 * Clase estática, que debe implementar la interfaz ParserCatalogo 
 * Hereda de DefaultHandler por lo que se deben sobrescribir sus métodos para procesar documentos XML 
 *
 */
public class ManejadorXML extends DefaultHandler implements ParserCatalogo {
	private String sNombreCategoria;	// Nombre de la categoría
	private List<String> lConcepts; 	// Lista con los uris de los elementos <concept> que pertenecen a la categoría
	private Map <String, Map<String,String>> hDatasets;	// Mapa con información de los dataset que pertenecen a la categoría
	
	private Map<String, String> infoDataset;	// Value de hDatasets. key: title, value: description o theme (extendible)
	
	private int iElementoNivel = 0;		// indica en que nivel nos encontramos
	private String sElementoNombre;		// nombre del elemento actual
	private List<String> lElementosPadre;	// Guarda el nombre de los elementos padre del elemento en el que nos encontramos.
	private int iPosicionCodeMin;		// La posicion del code mas pequeño que coincidio con el code que se paso como parametro. 
										// Ademas esta variable sirve como indice de la lista que contiene los elementos padre 
										// (posicion del concepts que contiene el code = este valor - 2, posicion del concept = este valor - 1). 
										// vale 0 si aun no ha coincidido.  P.ej: En caso de que se pase codigo 018 deberia valer 5. 
	
	private boolean datoInteresante = false;	// Indica si es pertinente o no
	private String uriActual;					// Solo se guarda si es pertiente
	private String urijson = "";
	
	private final String sCodigoConcepto;		// Llega como argumento
	
	private StringBuilder sb = new StringBuilder();
	/**  
	 * @param sCodigoConcepto código de la categoría a procesar
	 * @throws SAXException, ParserConfigurationException 
	 */
	public ManejadorXML (String sCodigoConcepto) throws SAXException, ParserConfigurationException {
		// TODO
		this.sCodigoConcepto = sCodigoConcepto;
		
		hDatasets = new HashMap<>();
		lConcepts = new ArrayList<>();
		lElementosPadre = new ArrayList<>();
				
		iPosicionCodeMin = 0;
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
		// TODO 
		if(lConcepts.size() == 0) return null;
		
		return lConcepts;
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
		// TODO 
		if(hDatasets.size() == 0) return null;
	
		return hDatasets;
	}
	

	 //===========================================================
	 // Métodos a implementar de SAX DocumentHandler
	 //===========================================================
	
	@Override
	public void startDocument() throws SAXException {
		super.startDocument();		
	}

	
	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		// TODO 
		iElementoNivel++;
		sElementoNombre = localName;
		lElementosPadre.add(localName);
		
		sb.setLength(0);
		
				
		if(localName.equalsIgnoreCase("concepts")) {
			// Abro concepts
			datoInteresante = false;
		}
		
		if(localName.equalsIgnoreCase("concept")) {
			// Abro concept
			uriActual = attributes.getValue("id");
		}
		
		if(localName.equals("datasets")) {
			// Abro datasets
			uriActual = new String();
		}
		
		if(localName.equals("dataset")) {
			// Abro dataset
			datoInteresante = false;
			urijson = attributes.getValue("id");
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		// TODO 
		iElementoNivel--;
		lElementosPadre.remove(lElementosPadre.size()-1);
		
		if(sb.toString().equals(sCodigoConcepto)) {				// Si se lee el codigo que se pasa como param
			iPosicionCodeMin = lElementosPadre.size();
			lConcepts.add(uriActual);
		} else
			if(localName.equals("code")) {	
				if(iPosicionCodeMin < lElementosPadre.size() && iPosicionCodeMin != 0) {
					// Si es un elemento hijo del elemento que me interesa
					// Guardar uri en el mapa
					lConcepts.add(uriActual);
				} else {
					if(localName.equals("code")) {
						iPosicionCodeMin = 0;
				}
			}
		}
		
		
		switch (localName) {
		case "title":
		//	hDatasets.put(uriActual, null)
			infoDataset = new HashMap<>();
			infoDataset.put(localName, sb.toString());
			break;
		case "description":
			infoDataset.put(localName, sb.toString());
			break;
		case "theme":
			infoDataset.put(localName, sb.toString());
			break;
		case "dataset":
			if(datoInteresante) {
				// Si es pertinente se guarda en el mapa
				hDatasets.putIfAbsent(urijson, infoDataset);
				System.out.println("URI JSON: " + urijson);
			}
			break;
		case "concept":
			if(lElementosPadre.contains("dataset")) {
				// Cuando cierro el concept de los dataset. Se comprueba si la uri es igual a alguna de la lista. 
				// En caso afirmativo, datoInteresante = true, sino sigue como estaba (false por defecto)
				for(String s : lConcepts) {
					if(s.equals(uriActual)) {
						datoInteresante = true;
					}
				}
			}
			break;
		default:
			break;
		}
		
		sb.setLength(0);
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		super.characters(ch, start, length);
		sb.append(ch,start,length);
	}

}

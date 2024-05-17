/* No modificar este fichero */
package piat.opendatasearch;

import java.util.Collection;
import java.util.Map;

/**
 * Interfaz que debe implementar la clase AnalizadorXML.
 */

public interface ParserCatalogo
{
	/**
	 * <code><b>getConcepts</b></code>
	 * Devuelve una colección con los <code><b>concepts</b></code>
	 * pertinentes.
	 * <br> Cada elemento de la colección contiene el
	 * <code><em>URI</em></code> de un <code>concept</code>.
	 * <br>Se considerarán pertinentes el <code><b>concept</b></code>
	 * cuyo código sea igual al criterio de búsqueda y todos sus
	 * <code>concept</code> descendientes.
	 * @return Una colección con los <em>URI</em> de los concepts
	 * pertinentes. Si no hay concepts pertinentes, la colección
	 * estará vacía.
	 */
	public Collection<String> getConcepts ();

	/**
	 * Devuelve un mapa con información de los <code>dataset</code>
	 * resultantes de la búsqueda.
	 * <br> Estructura de cada elemento del mapa:
	 * <ul><li><b>key</b>: valor del atributo <code><b>id</b></code>
	 * del elemento <code>dataset</code> que contiene el
	 * <code><em>URI</em></code> que lo identifica.
	 * <li><b>value</b>: mapa con la información extraída del
	 * <code>dataset</code>. Cada entrada de este mapa tien la
	 * siguiente estructura:
	 * <ul><li><b>key</b>: sus valores posibles son <em>title</em>,
	 * <em>description</em> y <em>theme</em>. y <code>value</code>.
	 * <li><b>value</b>: El valor correspondiente a dicho elemento.
	 * </ul></ul>
   	 * @return
	 * Mapa con información de los <code>dataset</code> pertinentes.
	 * Si no hay datasets pertinentes, el mapa estará vacío.
	 */
	public Map<String,Map<String,String>> getDatasets ();
}

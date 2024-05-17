package piat.opendatasearch;

import java.util.Collection;
import java.util.Map;

/**
 * Interfaz que debe implementar la clase AnalizadorSPATH.
 */
public interface ParserXPATH 
{
	/**
	 * <code><b>evaluarXPATH</b></code>
	 * Método que evalúa las expresiones XPath sobre un fichero XML
	 * @throws Exception si se produce una excepción o cualquier otro error
	 * durante el análisis.
	 */
	public void evaluarXPATH() throws Exception;

	/**
	 * <code><b>getQuery</b></code>
	 * Obtiene la categoría usada en la consulta. Corresponde al valor del
	 * campo <code>query</code> del documento XML.
	 * @return La cadena correspondiente a la categoría.
	 */
	public String getQuery ();

	/**
	 * <code><b>getNumRes</b></code>
	 * Obtiene el número de recursos que hay en el documento XML.
	 * @return el número de recursos del documento.
	 */
	public int getNumRes ();

	/**
	 * <code><b>getLocations</b></code>
	 * Obtiene los valores del campo <code>eventLocation</code> de cada
	 * recurso del documento. Elimina los duplicados que pudiera haber.
	 * @return Una colección con los nombres de las diferentes ubicaciones.
	 */
	public Collection<String> getLocations ();

	/**
	 * <code><b>getDatasetRes</b></code>
	 * Obtiene la relación de identificadores de <code>datasets</code>,
	 * junto con el número de recursos cuyo atributo <code>id</code> sea
	 * igual al atributo <code>id</code> del <code>dataset</code>
	 * correspondiente.
	 * @return Un mapa en el que, para cada dataset del documento XML, se
	 * da el número de recursos cuyo atributo <code>id</code> es igual al
	 * atributo <code>id</code> del <code>dataset</code> correspondiente.
	 */
	public Map<String,Integer> getDatasetRes ();
}

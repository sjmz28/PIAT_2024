/* No modificar este fichero */

package piat.opendatasearch;

import java.util.List;
import java.util.Map;

/**
 * Interfaz que debe implementar la clase AnalizadorJSON.
 */


public interface  ParserRecursos
{
	/**
	 * <code><b>analizarRecursos</b></code>
	 * Analiza un documento JSON que contiene recursos dentro del array "@graph".
	 * Únicamente debe obtener los 5 primeros recursos pertinentes.
	 * @throws Exception si se produce una excepción o cualquier otro error durante
	 * el análisis.
	 */	
	public void analizarRecursos () throws Exception;


	/**
	 * <code><b>getResurces</b></code>
	 * Devuelve una colección con los <code><b>resources</b></code>
	 * pertinentes.
	 * <br> Cada elemento de la colección contiene un
	 * <code><em>mapa</em></code> con la información necesaria para
	 * generar el documento XML de salida de acuerdo al esquema de
	 * salida.
	 * @return Una colección con los <em>mapas</em> de los recursos
	 * pertinentes. Si no hay recursos pertinentes, la colección
	 * estará vacía.
	 */
	public List<Map<String,String>> getRecursos();

}


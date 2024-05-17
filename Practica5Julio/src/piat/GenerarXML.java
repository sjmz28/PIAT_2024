package piat;


import java.text.Normalizer;
import java.util.List;
import java.util.Map;

/**
 * @author Arturo Salvador Mayor 51558282X
 * @author Sara Jimenez Munoz 51512521L
 *
 */

/**
 * Clase estática para crear un String que contenga el documento xml a partir de
 * la información almacenadas en las colecciones
 */
public class GenerarXML {

	private static final String sConceptPattern = "\n\t\t<concept>#ID#</concept>";
	private static final String sDatasetPattern = "\n\t\t<dataset id=\"#ID#\">" +
			"\n\t\t\t<title>#TEXT#</title>" +
			"\n\t\t\t<description>#TEXT#</description>" +
			"\n\t\t\t<theme>#TEXT#</theme>" +
			"\n\t\t</dataset>";

	/**
	 * Método que deberá ser invocado desde el programa principal
	 *
	 * @param lConcepts        Colecciones con la información obtenida del documento
	 *                         XML de entrada
	 * @param mDatasets        Mapa con la información de los datasets obtenida del
	 *                         documento XML de entrada
	 * @param sCode            Código de búsqueda
	 * @param mDatasetConcepts Mapa que relaciona los datasets con los conceptos
	 * @return String con el documento XML de salida
	 */
	public String generar(List<String> lConcepts, Map<String, Map<String, String>> mDatasets, String sCode,
			Map<String, List<Map<String, String>>> mDatasetConcepts) {
		StringBuilder salidaXML = new StringBuilder();

		// Inicio del documento XML
		salidaXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

		salidaXML.append("<searchResults xmlns=\"http://piat.dte.upm.es/practica4\"\n"
				+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ "xsi:schemaLocation=\"http://piat.dte.upm.es/practica4 ResultadosBusquedaP4.xsd\">\n"
				+ "");

		salidaXML.append("<summary>\n"
				+ "\t<query>" + sCode + "</query>\n"
				+ "\t<numConcepts>" + lConcepts.size() + "</numConcepts>\n"
				+ "\t<numDatasets>" + mDatasets.size() + "</numDatasets>\n"
				+ "</summary>");

		salidaXML.append("\n<results>");

		salidaXML.append("\n\t<concepts>");
		for (String unConcepto : lConcepts) {
			try {
				// Agrega cada concepto al documento XML
				salidaXML.append(sConceptPattern.replace("#ID#", unConcepto));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		salidaXML.append("\n\t</concepts>");

		salidaXML.append("\n\t<datasets>");

		String temp = "";
		for (Map.Entry<String, Map<String, String>> m : mDatasets.entrySet()) {
			try {
				temp = temp + sDatasetPattern.replace("#ID#", m.getKey());
			} catch (Exception e) {
				e.printStackTrace();
			}
			for (String v : m.getValue().values()) {
				temp = temp.replaceFirst("#TEXT#", v);
			}
		}
		salidaXML.append(temp);

		salidaXML.append("\n\t</datasets>");

		salidaXML.append(elementosResources(mDatasetConcepts));

		salidaXML.append("\n\t</results>\n");
		salidaXML.append("\n</searchResults>");

		String aux = cambiar_tildes(salidaXML.toString());

		return aux;

	}

	/**
	 * Crea los elementos de recursos dentro del documento XML a partir del mapa de
	 * conceptos de los datasets.
	 *
	 * @param mDatasetConcepts Mapa que relaciona los datasets con los conceptos
	 * @return String con los elementos de recursos en el documento XML
	 */
	private static String elementosResources(Map<String, List<Map<String, String>>> mDatasetConcepts) {
		StringBuilder sb = new StringBuilder();
		sb.append("\t<resources>");
		mDatasetConcepts.forEach((key, value) -> {
			for (Map<String, String> m : value) {
				sb.append(String.format(
						"<resource id=\"%s\">\n" +
								"    <concept id=\"%s\"/>\n" +
								"    <link><![CDATA[%s]]></link>\n" +
								"    <title>%s</title>\n" +
								"    <location>\n" +
								"        %s\n" +
								"        <address>\n" +
								"            %s\n" +
								"            %s\n" +
								"            %s\n" +
								"        </address>\n" +
								"        <timetable>\n" +
								"            %s\n" +
								"            %s\n" +
								"        </timetable>\n" +
								"        %s\n" +
								"    </location>\n" +
								"    %s\n" +
								"</resource>\n",
						key,
						m.get("@type"),
						m.get("link"),
						m.get("title"),
						formatIfNotNull("eventLocation", m.get("event-location")),
						formatIfNotNull("area", m.get("area")),
						formatIfNotNull("locality", m.get("locality")),
						formatIfNotNull("street", m.get("street-address")),
						formatIfNotNull("start", m.get("dtstart")),
						formatIfNotNull("end", m.get("dtend")),
						formatIfNotNull("georeference", m.get("latitude"), m.get("longitude")),
						formatIfNotNull("description", m.get("description")),
						"</resource>"));
			}

		});
		sb.append("\t</resources>");

		return sb.toString().replaceAll("(?m)^[ \t]*\r?\n", "");
	}

	/**
	 * Formatea el valor de una clave en un elemento XML si no es nulo.
	 *
	 * @param key    Nombre de la clave
	 * @param values Valores asociados a la clave
	 * @return String con el elemento XML formateado
	 */
	private static String formatIfNotNull(String key, String... values) {
		StringBuilder sb = new StringBuilder();
		if (values != null) {
			sb.append(String.format("<%s>", key));
			for (String value : values) {
				if (value == null || value.equals(""))
					return "";
				sb.append(value + " ");
			}
			sb.append(String.format("</%s>", key));
			return sb.toString().replaceFirst(" </" + key + ">", "</" + key + ">");
		} else {
			return "";
		}
	}

	/**
	 * Cambia los caracteres con tildes en la cadena de entrada por sus equivalentes
	 * sin tilde.
	 *
	 * @param entrada Cadena de entrada
	 * @return Cadena de salida sin tildes
	 */
	private static String cambiar_tildes(String entrada) {
		String sin_tildes = null;
		if (!(Normalizer.isNormalized(entrada, Normalizer.Form.NFKD))) {
			sin_tildes = Normalizer.normalize(entrada, Normalizer.Form.NFKD);
			sin_tildes = sin_tildes.replaceAll("\\p{M}", "");
			sin_tildes = sin_tildes.replaceAll("[^\\p{ASCII}]+", "");
		}
		return sin_tildes;
	}
}

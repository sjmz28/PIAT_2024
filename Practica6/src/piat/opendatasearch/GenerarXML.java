package piat.opendatasearch;

import java.util.List;
import java.util.Map;


/**
	@author Sara Jimenez Muñoz s.jmunoz@alumnos.upm.es
 */


/**
 * Clase estática para crear un String que contenga el documento xml a partir de la información almacenadas en las colecciones 
 *
 */	
public class GenerarXML {

	private static final String conceptPattern = "\n\t\t<concept>#ID#</concept>" ;
	private static final String datasetPattern = "\n\t\t<dataset id=\"#ID#\">" + 
			"\n\t\t\t<title>#TEXT#</title>" +
			"\n\t\t\t<description>#TEXT#</description>" +
			"\n\t\t\t<theme>#TEXT#</theme>" +
			"\n\t\t</dataset>";
	/**  
	 * Método que deberá ser invocado desde el programa principal
	 * 
	 * @param Colecciones con la información obtenida del documento XML de entrada
	 * @return String con el documento XML de salida
	 */	
	public static String generar (Map<String, Map<String,String>> mDatasets, List<String> lConcepts, String sCode, Map<String, List<Map<String,String>>> mDatasetConcepts){
		StringBuilder salidaXML= new StringBuilder();

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

		salidaXML.append("\n\t<concepts>" );
		for (String unConcepto : lConcepts){
			salidaXML.append (conceptPattern.replace("#ID#", unConcepto));
		}
		salidaXML.append("\n\t</concepts>");

		salidaXML.append("\n\t<datasets>" );

		String temp = "";
		for(Map.Entry<String, Map<String,String>> m : mDatasets.entrySet()) {
			// Escribe tantos datasets como tamaño del mapa
			temp = temp + datasetPattern.replace("#ID#", m.getKey());
			for(String v : m.getValue().values()) {
				// Para cada dataset realiza 3 cambios
				temp = temp.replaceFirst("#TEXT#", v);
			}
		}
		salidaXML.append(temp);

		salidaXML.append("\n\t</datasets>" );
		
		salidaXML.append(elementosResources(mDatasetConcepts));

		salidaXML.append("\n\t</results>\n");
		salidaXML.append("\n</searchResults>" );

		return salidaXML.toString();

	}

	private static String elementosResources(Map<String, List<Map<String,String>>> mDatasetConcepts) {
		StringBuilder sb = new StringBuilder();
		sb.append("\t<resources>");
		mDatasetConcepts.forEach((key, value)-> {
			for(Map<String,String> m : value) {
				sb.append(String.format("""
							<resource id=\"%s\">
								<concept id=\"%s\"/>
								<link><![CDATA[%s]]></link>
								<title>%s</title>
								<location>
									%s
									<address>
										%s
										%s
										%s
									</address>
									<timetable>
										%s
										%s
									</timetable>
									%s
								</location>
								%s
							%s
						""", key, m.get("@type"), m.get("link"), m.get("title"), formatIfNotNull("eventLocation", m.get("event-location")), 
						formatIfNotNull("area", m.get("area")), formatIfNotNull("locality", m.get("locality")), formatIfNotNull("street", m.get("street-address")),
						formatIfNotNull("start", m.get("dtstart")), formatIfNotNull("end", m.get("dtend")), 
						formatIfNotNull("georeference", m.get("latitude"), m.get("longitude")), formatIfNotNull("description", m.get("description")), "</resource>"));
			}
		});
		sb.append("\t</resources>");

		return sb.toString().replaceAll("(?m)^[ \t]*\r?\n", ""); // Elimina lineas en blanco
	}

	private static String formatIfNotNull(String key, String... values) {
		StringBuilder sb = new StringBuilder();
		if (values != null) {
			sb.append(String.format("<%s>", key));
			for(String value: values) {
				if(value == null || value.equals("")) return "";
				sb.append(value + " "); 
			}
			sb.append(String.format("</%s>", key));
			return sb.toString().replaceFirst(" </" + key + ">", "</" + key + ">");	// Elimina el ultimo espacio en blanco 
		} else {
			return "";
		}
	}


}


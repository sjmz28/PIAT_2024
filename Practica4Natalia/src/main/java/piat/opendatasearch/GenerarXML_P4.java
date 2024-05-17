package piat.opendatasearch;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;

/**
 * @author Natalia Agüero Knauf 47230975S
 *
 */


/**
 * Clase estÃ¡tica para crear un String que contenga el documento xml a partir de la informaciÃ³n almacenadas en las colecciones 
 *
 */	
public class GenerarXML_P4 {
	

	/**  
	 * MÃ©todo que deberÃ¡ ser invocado desde el programa principal
	 * 
	 * @param Colecciones con la informacion obtenida del documento XML de entrada
	 * @return String con el documento XML de salida
	 * 
	 * 
	 */	
	
	private String sXMLPattern = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"\n<tns:searchResults xmlns:tns =\"http://piat.dte.upm.es/practica4\">" + "\n" +
			"#SUMMARY#" + "\n" + 
			"#RESULTS#" + "\n" +
			"</tns:searchResults>";
			
	
	private String sXMLSummaryPattern = "\t<tns:summary>" + "\n" +
			"\t\t<tns:query> #QUERY# </tns:query>" + "\n" +
			"\t\t<tns:numConcepts> #NUMCONCEPTS# </tns:numConcepts>" + "\n" +
			"\t\t<tns:numDatasets> #NUMDATASETS# </tns:numDatasets>" + "\n" +
			"\t</tns:summary>" + "\n";
	
	private String sXMLResultsPattern = "\t<tns:results>" + "\n" +
			"\t\t<tns:concepts>\n" + "#CONCEPTS#" + "\t\t</tns:concepts>\n" +
			"\t\t<tns:datasets>\n" + "#DATASETS#" + "\t\t</tns:datasets>\n" +
			"\t\t<tns:resources>\n" + "#RESOURCES#" + "\t\t</tns:resources>\n" +
			"\t</tns:results>";
	
	private String sXMLConceptPattern = "\t\t\t<tns:concept> " + " #IDCONCEPT#" + " </tns:concept>\n";
	
	private String sXMLDataset = "\t\t\t<tns:dataset id = \"" + "#IDDATASET#" + "\">\n" +
			"#CONTENIDODATASET#" + 
			"\t\t\t</tns:dataset>\n";

	private String sXMLElementoPattern = "<#ELEMENTO#> #CONTENIDOELEMENTO# </#ELEMENTO#>\n";
	
	
	private String conceptsOuput(List<String> lConcepts) {
		// CONCEPTS OUTPUT
		StringBuilder sbSalida = new StringBuilder();
		for (String concepto : lConcepts) {
			sbSalida.append(sXMLConceptPattern.replace("#IDCONCEPT#", concepto));
		}

		return sbSalida.toString();
	}
	
	private String datasetOutput(Map<String, Map<String, String>> mDatasets) {
		// DATASETS OUTPUT
		StringBuilder sbTotalDataset = new StringBuilder();
		StringBuilder sbDataset = new StringBuilder();
		String contenido;

		for (String idDataset : mDatasets.keySet()) {
			// TITLE
			if (mDatasets.get(idDataset).containsKey("title")) {
				sbDataset.append("\t\t\t" + sXMLElementoPattern.replace("#ELEMENTO#", "tns:title").replace("#CONTENIDOELEMENTO#",
						mDatasets.get(idDataset).get("title")));
			} else {
				sbDataset.append(
						"\t\t" + sXMLElementoPattern.replace("#ELEMENTO#", "tns:title").replace("#CONTENIDOELEMENTO#", " "));
			}

			// DESCRIPTION
			if (mDatasets.get(idDataset).containsKey("description")) {
				sbDataset.append("\t\t\t" + sXMLElementoPattern.replace("#ELEMENTO#", "tns:description")
						.replace("#CONTENIDOELEMENTO#", mDatasets.get(idDataset).get("description")));
			}

			// THEME
			if (mDatasets.get(idDataset).containsKey("theme")) {
				sbDataset.append("\t\t\t" + sXMLElementoPattern.replace("#ELEMENTO#", "tns:theme").replace("#CONTENIDOELEMENTO#",
						mDatasets.get(idDataset).get("theme")));
			}

			contenido = sbDataset.toString();
			sbDataset.setLength(0);
			sbTotalDataset.append(sXMLDataset.replace("#IDDATASET#", idDataset).replace("#CONTENIDODATASET#", contenido));
		}

		return sbTotalDataset.toString();

	}
	
	private String resourcesOutPut(Map<String, List<Map<String,String>>> mDatasetConcepts) {
		StringBuilder sResource = new StringBuilder();
		StringBuilder sTotalResources = new StringBuilder();
		
		String sResourcePattern = "\t\t\t<tns:resource id = \"" + "#FICHEROJSON#" + "\">\n#RESOURCE#" + "\t\t\t</tns:resource>\n";
		String sConceptPattern = "\t\t\t\t<tns:concept id = \"" + "#CONCEPT#" + "\"/>\n";
		String sLinkPattern = "\t\t\t\t<tns:link> " + "<![CDATA[" + "#LINK#" + "]]>" + "</tns:link>\n";
		String sTitlePattern = "\t\t\t\t<tns:title> " + "#TITLE#" + "</tns:title>\n";
		String sLocationPatterns = "\t\t\t\t<tns:location>\n ";
		String sAddressPatterns = "\t\t\t\t\t<tns:address>\n ";
		String sAddressPatternf = "\t\t\t\t\t</tns:address>\n";
		String stimetablePattern = "\t\t\t\t\t<tns:timetable>\n " + 
					"\t\t\t\t\t\t<tns:start> " + "#START#" + "</tns:start>\n" + 
					"\t\t\t\t\t\t<tns:end> " + "#END#" + "</tns:end>\n" + 
				"\t\t\t\t\t</tns:timetable>\n";
		String sLocationPatternf = "\t\t\t\t</tns:location>\n";
		String sDescriptionPattern = "\t\t\t\t<tns:description> " + "#DESCRIPTION#" + "</tns:description>\n";
		
		String eventLocation = "";
		String area = "";
		String locality = "";
		String street = "";
		String tgeoref = "";
		
		for (String ficheroJSON: mDatasetConcepts.keySet()) {
			for (Map<String, String> m: mDatasetConcepts.get(ficheroJSON)) {
				sResource = new StringBuilder();
				sResource.append(sConceptPattern.replace("#CONCEPT#", m.get("concept")));
				sResource.append(sLinkPattern.replace("#LINK#", m.get("link")));
				sResource.append(sTitlePattern.replace("#TITLE#", m.get("title")));
				sResource.append(sLocationPatterns);
				if (m.containsKey("eventLocation")) {
					eventLocation = "\t\t\t\t\t<tns:eventLocation> " + m.get("eventLocation") + "</tns:eventLocation>\n";
				} else {
					eventLocation = "";
				}
				sResource.append(eventLocation);
				sResource.append(sAddressPatterns);
				if (m.containsKey("area")) {
					area = "\t\t\t\t\t\t<tns:area> " + m.get("area") + "</tns:area>\n";
				} else {
					area = "";
				}
				if (m.containsKey("locality")) {
					locality = "\t\t\t\t\t\t<tns:locality> " + m.get("locality") + "</tns:locality>\n";
				} else {
					locality = "";
				}
				if (m.containsKey("street")) {
					street = "\t\t\t\t\t\t<tns:street> " + m.get("street") + "</tns:street>\n";
				} else {
					street = "";
				}
				sResource.append(area).append(locality).append(street);
				sResource.append(sAddressPatternf);
				sResource.append(stimetablePattern.replace("#END#", m.get("end")).replace("#START#", m.get("start")));
				
				if (m.containsKey("latitude") && m.containsKey("longitude")) {
					tgeoref = "\t\t\t\t\t<tns:georeference>" + m.get("latitude") + " " + m.get("latitude") + "</tns:georeference>\n";
				} else {
					tgeoref = "";
				}
				sResource.append(tgeoref);
				sResource.append(sLocationPatternf);
				sResource.append(sDescriptionPattern.replace("#DESCRIPTION#", m.get("description")));
				sTotalResources.append(sResourcePattern.replace("#RESOURCE#", sResource).replace("#FICHEROJSON#", ficheroJSON));
			}
		}
		return sTotalResources.toString();
	}
	
	public String generar(Map<String, Map<String, String>> mDatasets, List<String> lConcepts, String codigo, Map<String, List<Map<String,String>>> mDatasetConcepts) {
		
		if (mDatasets == null | lConcepts == null | mDatasetConcepts == null) {
			System.out.println("Uno de los mapas es igual a null");
			System.exit(1);
		}
		String summary = sXMLSummaryPattern.replace("#QUERY#", codigo).replace("#NUMCONCEPTS#", Integer.toString(lConcepts.size())).replace("#NUMDATASETS#", Integer.toString(mDatasets.size()));
		String results = sXMLResultsPattern.replace("#CONCEPTS#", conceptsOuput(lConcepts)).replace("#DATASETS#", datasetOutput(mDatasets)).replace("#RESOURCES#", resourcesOutPut(mDatasetConcepts));
		String salida = sXMLPattern.replace("#SUMMARY#", summary).replace("#RESULTS#", results);
		salida = cambiar_tildes (salida);
		return salida;
	}
	
	private String cambiar_tildes (String entrada) {
		String sin_tildes = null;
		if (!(Normalizer.isNormalized(entrada, Normalizer.Form.NFKD))) {
		sin_tildes = Normalizer.normalize(entrada, Normalizer.Form.NFKD);
		sin_tildes.replaceAll("\\p{M}", "");
		sin_tildes.replaceAll("[^\\p{ASCII}]+", "");
		}
		return sin_tildes;
	}
}

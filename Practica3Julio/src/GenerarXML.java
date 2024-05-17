

import java.text.Normalizer;
import java.util.List;
import java.util.Map;

/**
 * @author Sara Jimenez Munoz		51512521L
 *
 */


	
public class GenerarXML {
	

	/***************************************************************************************  
	 * MÃ©todo que deberÃ¡ ser invocado desde el programa principal
	 * 
	 * @param Colecciones con la informacion obtenida del documento XML de entrada
	 * @return String con el documento XML de salida
	 * 
	 * 
	 ***************************************************************************************/	
	
	private String sXMLPattern = ""
			+ "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"\n<tns:searchResults xmlns:tns =\"http://piat.dte.upm.es/practica3\">" + "\n" +
			"#SUMMARY#" + "\n" + 
			"#RESULTS#" + "\n" +
			"</tns:searchResults>";
			
	
	private String sXMLSummaryPattern = "\t<tns:summary>" + "\n" +
			"\t\t	<tns:query> #QUERY# </tns:query>" + "\n" +
			"\t\t	<tns:numConcepts> #NUMCONCEPTS# </tns:numConcepts>" + "\n" +
			"\t\t	<tns:numDatasets> #NUMDATASETS# </tns:numDatasets>" + "\n" +
			"\t	</tns:summary>" + "\n";
	
	private String sXMLResultsPattern = ""
			+ "\t	<tns:results>" + "\n" +
			  "\t\t		<tns:concepts>\n" + "#CONCEPTS#" + "\t\t</tns:concepts>\n" +
			  "\t\t		<tns:datasets>\n" + "#DATASETS#" + "\t\t</tns:datasets>\n" +
			  "\t	</tns:results>";
	
	private String sXMLConceptPattern = "\t\t\t<tns:concept> " + " #IDCONCEPT#" + " </tns:concept>\n";
	
	private String sXMLDataset = ""
			+ "\t\t\t	<tns:dataset id = \"" + "#IDDATASET#" + "\">\n" +
			"#CONTENIDODATASET#" + 
			"\t\t\t		</tns:dataset>\n";

	private String sXMLElementoPattern = "<#ELEMENTO#> #CONTENIDOELEMENTO# </#ELEMENTO#>\n";
	
	
	private String conceptsOuput(List<String> lConcepts) {
		
		StringBuilder sbSalida = new StringBuilder();
		
		for (String concepto : lConcepts) {
			sbSalida.append(sXMLConceptPattern.replace("#IDCONCEPT#", concepto));
		}

		return sbSalida.toString();
	}
	
	private String datasetOutput(Map<String, Map<String, String>> mDatasets) {
		
		StringBuilder sbAllDataset = new StringBuilder();
		StringBuilder sbDataset = new StringBuilder();
		String contenido;

		for (String idDataset : mDatasets.keySet()) {
			// title
			if (mDatasets.get(idDataset).containsKey("title")) {
				sbDataset.append("\t\t\t" + sXMLElementoPattern.replace("#ELEMENTO#", "tns:title").replace("#CONTENIDOELEMENTO#",
						mDatasets.get(idDataset).get("title")));
			} else {
				sbDataset.append("\t\t" + sXMLElementoPattern.replace("#ELEMENTO#", "tns:title").replace("#CONTENIDOELEMENTO#", " "));
			}

			// description
			if (mDatasets.get(idDataset).containsKey("description")) {
				sbDataset.append("\t\t\t" + sXMLElementoPattern.replace("#ELEMENTO#", "tns:description")
						.replace("#CONTENIDOELEMENTO#", mDatasets.get(idDataset).get("description")));
			}

			// theme
			if (mDatasets.get(idDataset).containsKey("theme")) {
				sbDataset.append("\t\t\t" + sXMLElementoPattern.replace("#ELEMENTO#", "tns:theme").replace("#CONTENIDOELEMENTO#",
						mDatasets.get(idDataset).get("theme")));
			}

			contenido = sbDataset.toString();
			sbDataset.setLength(0);
			sbAllDataset.append(sXMLDataset.replace("#IDDATASET#", idDataset).replace("#CONTENIDODATASET#", contenido));
		}

		return sbAllDataset.toString();

	}
	
	public String generar(Map<String, Map<String, String>> mDatasets, List<String> lConcepts, String codigo) {
		
		if (mDatasets == null | lConcepts == null) {
			System.out.println("Uno de los mapas es igual a null");
			System.exit(1);
		}
		String summary = sXMLSummaryPattern.replace("#QUERY#", codigo).replace("#NUMCONCEPTS#", Integer.toString(lConcepts.size())).replace("#NUMDATASETS#", Integer.toString(mDatasets.size()));
		String results = sXMLResultsPattern.replace("#CONCEPTS#", conceptsOuput(lConcepts)).replace("#DATASETS#", datasetOutput(mDatasets));
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

package piat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Arturo Salvador Mayor 51558282X
 * @author Sara Jimenez Munoz 51512521L
 *
 */

/**
 * Clase para evaluar las expresiones XPath
 * Contiene un método estático, llamado evaluar(), que se encarga de realizar
 * las
 * consultas XPath al fichero XML que se le pasa como parámetro
 */
public class XPATH_Evaluador {

	/**
	 * Método que se encarga de evaluar las expresiones XPath sobre el fichero XML
	 * generado en la práctica 4
	 *
	 * @param ficheroXML Fichero XML a evaluar
	 * @return Una lista con la propiedad resultante de evaluar cada expresión XPath
	 * @throws IOException
	 * @throws XPathExpressionException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public static List<Propiedad> evaluar(String ficheroXML) throws IOException, XPathExpressionException {

		List<Propiedad> evaluar = new ArrayList<>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder build = factory.newDocumentBuilder();
			Document doc = build.parse(new File(ficheroXML));

			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPath xpath = xpathFactory.newXPath();

			// Evaluación de la expresión XPath "//searchResults/summary/query/text()"
			Object expr1 = xpath.compile("//searchResults/summary/query/text()").evaluate(doc, XPathConstants.STRING);
			Propiedad query = new Propiedad("query", expr1.toString());
			evaluar.add(query);

			// Evaluación de la expresión XPath
			// "//summary/child::numDatasets/descendant::text()"
			String numDataset = (String) xpath.evaluate("//summary/child::numDatasets/descendant::text()", doc,
					XPathConstants.STRING);
			Propiedad numDatasets = new Propiedad("numDataset", numDataset);
			evaluar.add(numDatasets);

			xpath.evaluate("/datasets", doc, XPathConstants.NODESET);
			NodeList lDatasets = (NodeList) xpath.evaluate("//dataset", doc, XPathConstants.NODESET);
			NodeList lEventLocation = (NodeList) xpath.evaluate(
					"//searchResults/results/resources/resource/location/eventLocation/text()", doc,
					XPathConstants.NODESET);

			// Evaluación de las expresiones XPath dentro del bucle "lDatasets"
			// Este ciclo recorre todos los elementos "dataset" de la lista "lDatasets"
			for (int i = 0; i < lDatasets.getLength(); i++) {
				// Se obtienen los atributos del elemento "dataset" actual
				NamedNodeMap attList = lDatasets.item(i).getAttributes();

				// Este ciclo recorre todos los atributos del elemento "dataset" actual
				for (int j = 0; j < attList.getLength(); j++) {
					// Se obtiene el atributo actual
					Node nodeAtt = attList.item(j);

					// Se crea una nueva propiedad con el nombre "id" y el valor del atributo actual
					Propiedad nodeAtt1 = new Propiedad("id", nodeAtt.getChildNodes().item(j).getNodeValue());

					// Se obtiene la cuenta de los recursos que tienen el mismo id que el atributo
					// actual
					Double num1 = (Double) xpath.evaluate(
							"count(//resources/resource[attribute::id=string(\""
									+ nodeAtt.getChildNodes().item(j).getNodeValue() + "\")])",
							doc, XPathConstants.NUMBER);

					// Se crea una nueva propiedad con el nombre "num" y el valor obtenido en el
					// paso anterior
					Propiedad num = new Propiedad("num", num1.toString());

					// Se añaden las propiedades a la lista "evaluar"
					evaluar.add(nodeAtt1);
					evaluar.add(num);
				}
			}

			// Evaluación de la expresión XPath "//resource/child::title/descendant::text()"
			NodeList ltitles = (NodeList) xpath.evaluate("//resource/child::title/descendant::text()", doc,
					XPathConstants.NODESET);
			for (int i = 0; i < ltitles.getLength(); i++) {
				String title1 = (String) ltitles.item(i).getNodeValue();
				Propiedad title = new Propiedad("title", title1);
				evaluar.add(title);
			}

			// Evaluación de la expresión XPath
			// "//searchResults/results/resources/resource/location/eventLocation/text()"
			for (int i = 0; i < lEventLocation.getLength(); i++) {
				String eventLocation = (String) lEventLocation.item(i).getNodeValue();
				Propiedad loc = new Propiedad("eventLocation", eventLocation);
				if (!containsElement(evaluar, loc)) {
					evaluar.add(loc);
				}
			}

			xpath.evaluate("//resource/child::title/descendant::text()", doc, XPathConstants.NODESET);
		} catch (ParserConfigurationException | SAXException e) {
			e.printStackTrace();
		}

		return evaluar;
	}

	/**
	 * Método para comprobar si una lista de Propiedades contiene un elemento
	 * específico
	 *
	 * @param evaluar Lista de Propiedades a comprobar
	 * @param loc     Propiedad a buscar
	 * @return true si la lista contiene el elemento, false en caso contrario
	 */
	private static boolean containsElement(List<Propiedad> evaluar, Propiedad loc) {
		for (Propiedad elem : evaluar) {
			if (elem.nombre.equals(loc.nombre) && elem.valor.equals(loc.valor)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Clase interna para representar una propiedad con nombre y valor
	 */
	public static class Propiedad {
		public final String nombre;
		public final String valor;

		public Propiedad(String nombre, String valor) {
			this.nombre = nombre;
			this.valor = valor;
		}

		@Override
		public String toString() {
			return this.nombre + ": " + this.valor;
		}
	}
}

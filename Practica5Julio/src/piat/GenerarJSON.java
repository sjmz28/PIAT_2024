package piat;


import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.stream.JsonWriter;

import piat.XPATH_Evaluador.Propiedad;

/**
 * @author Arturo Salvador Mayor 51558282X
 * @author Sara Jimenez Munoz 51512521L
 *
 */

public class GenerarJSON {

	/**
	 * Genera un archivo JSON a partir de una lista de propiedades.
	 *
	 * @param ficheroJSONSalida Ruta del archivo JSON de salida
	 * @param listaPropiedades  Lista de propiedades a incluir en el JSON
	 */
	public static void generar(String ficheroJSONSalida, List<Propiedad> listaPropiedades) {
		try {
			JsonWriter escritorArchivo = new JsonWriter(new FileWriter(ficheroJSONSalida));
			escritorArchivo.setIndent("  ");
			escritorArchivo.setHtmlSafe(true);
			escritorArchivo.beginObject();

			// Escribir query y su valor
			Propiedad queryElemento = listaPropiedades.get(0);
			escritorArchivo.name("query").value(queryElemento.valor);

			// Escribir numeroResources y su valor
			Propiedad numRecursosElemento = listaPropiedades.get(1);
			escritorArchivo.name("numeroResources").value(numRecursosElemento.valor);

			// Escribir infDatasets
			escritorArchivo.name("infDatasets").beginArray();
			for (int index = 2; index < listaPropiedades.size(); index += 2) {
				Propiedad elementoId = listaPropiedades.get(index);
				if (elementoId.nombre.equals("id")) {
					Propiedad elementoValor = listaPropiedades.get(index + 1);
					escritorArchivo.beginObject();
					escritorArchivo.name("id").value(elementoId.valor);
					escritorArchivo.name("num").value(elementoValor.valor);
					escritorArchivo.endObject();
				}
			}
			escritorArchivo.endArray();

			// Escribir ubicaciones
			escritorArchivo.name("ubicaciones").beginArray();
			for (int index = 2; index < listaPropiedades.size(); index++) {
				Propiedad elementoUbicacion = listaPropiedades.get(index);
				if (elementoUbicacion.nombre.equals("eventLocation")) {
					escritorArchivo.value(elementoUbicacion.valor);
				}
			}
			escritorArchivo.endArray();

			escritorArchivo.endObject();
			escritorArchivo.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Extrae un número entero de una cadena de texto.
	 *
	 * @param texto Cadena de texto de entrada
	 * @return Número entero extraído de la cadena (0 si no se encuentra ningún
	 *         número)
	 */
	public static int extraerNumero(String texto) {
		String numeroString = texto.replaceAll("[^0-9]", "");
		if (numeroString.isEmpty()) {
			return 0;
		}
		return Integer.parseInt(numeroString);
	}

	/**
	 * Extrae el texto de una cadena, eliminando los dígitos numéricos.
	 *
	 * @param texto Cadena de texto de entrada
	 * @return Texto extraído de la cadena
	 */
	public static String extraerTexto(String texto) {
		StringBuilder sb = new StringBuilder();
		for (char c : texto.toCharArray()) {
			if (!Character.isDigit(c)) {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * Extrae un número decimal de una cadena de texto.
	 *
	 * @param texto Cadena de texto de entrada
	 * @return Número decimal extraído de la cadena (0.0 si no se encuentra ningún
	 *         número decimal)
	 */
	public static double extraerNumeroDecimal(String texto) {
		Pattern patron = Pattern.compile("\\d+\\.\\d+");
		Matcher matcher = patron.matcher(texto);

		if (matcher.find()) {
			String numeroString = matcher.group();
			return Double.parseDouble(numeroString);
		} else {
			return 0;
		}
	}
}

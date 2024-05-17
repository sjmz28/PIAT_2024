package piat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.stream.JsonReader;

/**
 * @author Arturo Salvador Mayor 51558282X
 * @author Sara Jiménez Muñoz 51512521L
 *
 */

/* En esta clase se comportará como un hilo */

public class JSONDatasetParser implements Runnable {
	private String fichero;
	private List<String> lConcepts;
	private Map<String, List<Map<String,String>>> mDatasetConcepts;
	private String nombreHilo;
	
	
	public JSONDatasetParser (String fichero, List<String> lConcepts, Map<String, List<Map<String,String>>> mDatasetConcepts) { 
		this.fichero=fichero;
		this.lConcepts=lConcepts;
		this.mDatasetConcepts=mDatasetConcepts;
	}

	
	@Override
	public void run (){
		List<Map<String,String>> graphs=new ArrayList<Map<String,String>>();	// Aquí se almacenarán todos los graphs de un dataset cuyo objeto de nombre @type se corresponda con uno de los valores pasados en el la lista lConcepts
		boolean finProcesar=false;	// Para detener el parser si se han agregado a la lista graphs 5 graph
	
		Thread.currentThread().setName("JSON " + fichero);
		nombreHilo="["+Thread.currentThread().getName()+"] ";
	    try {
	    	InputStreamReader inputStream = new InputStreamReader(new URL(fichero).openStream(), "UTF-8");

			JsonReader jsonReader = new JsonReader(inputStream);
			jsonReader.beginObject();
			while (jsonReader.hasNext() && !finProcesar) {
				String name = jsonReader.nextName();
				if (name.equals("@graph")) {
					finProcesar = procesar_graph(jsonReader, graphs, lConcepts);
				} else {
					jsonReader.skipValue();
				}
			}
			if(!finProcesar) {
				jsonReader.endObject();
			}
			jsonReader.close();

			inputStream.close();

			//printGraphs(graphs);

		} catch (FileNotFoundException e) {
			System.out.println(nombreHilo+"El fichero no existe. Ignorándolo");
		} catch (IOException e) {
			System.out.println(nombreHilo+"Hubo un problema al abrir el fichero. Ignorándolo" + e);
		}
	    mDatasetConcepts.put(fichero, graphs); 	// Se añaden al Mapa de concepts de los Datasets
	    
	}

	/* 	procesar_graph()
	 * 	Procesa el array @graph
	 *  Devuelve true si ya se han añadido 5 objetos a la lista graphs
	 */
	private boolean procesar_graph(JsonReader jsonReader, List<Map<String, String>> graphs, List<String> lConcepts) throws IOException {
		boolean finProcesar=false;
		// TODO:
		//	- Consumir el primer "[" del array @graph
		//  - Procesar todos los objetos del array, hasta el final de fichero o hasta que finProcesar=true
		//  	- Consumir el primer "{" del objeto
		//  	- Procesar un objeto del array invocando al método procesar_un_graph()
		//  	- Consumir el último "}" del objeto
		// 		- Ver si se han añadido 5 graph a la lista, para en ese caso poner la variable finProcesar a true
		//	- Si se ha llegado al fin del array, consumir el último "]" del array

		int numProcesados = 0;

		jsonReader.beginArray();
		while (jsonReader.hasNext() && !finProcesar) {

			jsonReader.beginObject();
			procesar_un_graph(jsonReader, graphs, lConcepts);
			jsonReader.endObject();
			// el numero de graph procesados es el tamaño de la lista graphs y aumenta cada vez que se procesa un graph
			numProcesados = graphs.size();

			if (numProcesados == 5) finProcesar = true;
		}
		while (jsonReader.hasNext()) {
			
			switch (jsonReader.peek()) {

				case END_OBJECT -> jsonReader.endObject();
				case END_DOCUMENT -> jsonReader.endObject();
				case END_ARRAY -> jsonReader.endArray();
				default -> jsonReader.skipValue();
			}
		}
	
		jsonReader.endArray();
	    return finProcesar;
		
	}

	/*	procesar_un_graph()
	 * 	Procesa un objeto del array @graph y lo añade a la lista graphs si en el objeto de nombre @type hay un valor que se corresponde con uno de la lista lConcepts
	 */
	
	private void procesar_un_graph(JsonReader jsonReader, List<Map<String, String>> graphs, List<String> lConcepts) throws IOException {
		String key;
		Map<String, String> tempMap = new HashMap<>();
		while(jsonReader.hasNext()) {
			key = jsonReader.nextName();
			switch(jsonReader.peek()){
				case BEGIN_OBJECT -> comprobador(jsonReader, tempMap, key);
				case STRING -> tempMap.put(key, jsonReader.nextString());
				case NUMBER -> tempMap.put(key, jsonReader.nextString());
				default -> jsonReader.skipValue();
			}
		}
		lConcepts.forEach(c -> {
			if(c.equals(tempMap.get("@type"))){
				graphs.add(tempMap);
			}
		});

	}

	private String comprobador(JsonReader reader, Map<String, String> tmp, String nameId) throws IOException{
		String objectName;
		String keyId = null;
		reader.beginObject();

		while(reader.hasNext()){
			keyId = reader.nextName();
			switch(reader.peek()){
				case BEGIN_OBJECT -> {
					objectName = keyId;
					comprobador(reader, tmp, keyId);
				}

				case STRING -> {
					if(keyId.equals("@id")){
						tmp.put(nameId, reader.nextString());
					}
					else{
						tmp.put(keyId, reader.nextString());
					}
				}

				case NUMBER -> tmp.put(keyId, Double.toString(reader.nextDouble()));
				
				default -> reader.skipValue();
			}
		}
		reader.endObject();
		return keyId;
	}


	public static void printGraphs(List<Map<String, String>> graphs) {
		for (Map<String, String> map : graphs) {
			for (Map.Entry<String, String> entry : map.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				System.out.println(key + ": " + value);
			}
			System.out.println("---");
		}
	}

	public static void procesar_un_area() {

	}
	
	
}

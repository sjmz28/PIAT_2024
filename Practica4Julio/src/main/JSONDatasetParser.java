package main;

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
 * @author Ruben Agustin Gonzalez, 52063864Y
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
	    System.out.println(nombreHilo+"Empezar a descargar de internet el JSON");
	    try {
	    	InputStreamReader inputStream = new InputStreamReader(new URL(fichero).openStream(), "UTF-8"); 
	    	//TODO:
			//	- Crear objeto JsonReader a partir de inputStream
			//  - Consumir el primer "{" del fichero
			//  - Procesar los elementos del fichero JSON, hasta el final de fichero o hasta que finProcesar=true
			//		Si se encuentra el objeto @graph, invocar a procesar_graph()
			//		Descartar el resto de objetos
			//	- Si se ha llegado al fin del fichero, consumir el último "}" del fichero
			//  - Cerrar el objeto JsonReader
	    	JsonReader jsonReader = new JsonReader(inputStream);
	    	jsonReader.beginObject();
	    	
	    	while (jsonReader.hasNext() && !finProcesar){
	    		String s = jsonReader.nextName();
	    		if(s.equals("@graph")) procesar_graph(jsonReader, graphs, lConcepts); 
	    		else jsonReader.skipValue();
	    	}
	    	
	    	if(!jsonReader.hasNext()) jsonReader.endArray(); //jsonReader.endObject();
	    	jsonReader.close();
			inputStream.close();
		} catch (FileNotFoundException e) {
			System.out.println(nombreHilo+"El fichero no existe. Ignorándolo");
		} catch (IOException e) {
			System.out.println(nombreHilo+"Hubo un problema al abrir el fichero. Ignorándolo " + e);
		}
	    mDatasetConcepts.put(fichero, graphs); 	// Se añaden al Mapa de concepts de los Datasets
	}

	/* 	procesar_graph()
	 * 	Procesa el array @graph
	 *  Devuelve true si ya se han añadido 5 objetos a la lista graphs
	 */
	private boolean procesar_graph(JsonReader jsonReader, List<Map<String, String>> graph, List<String> lConcepts) throws IOException {
		// TODO:
		//	- Consumir el primer "[" del array @graph
		//  - Procesar todos los objetos del array, hasta el final de fichero o hasta que finProcesar=true
		//  	- Consumir el primer "{" del objeto
		//  	- Procesar un objeto del array invocando al método procesar_un_graph()
		//  	- Consumir el último "}" del objeto
		// 		- Ver si se han añadido 5 graph a la lista, para en ese caso poner la variable finProcesar a true
		//	- Si se ha llegado al fin del array, consumir el último "]" del array
		
		boolean finProcesar=false;
		int numGraphProcesados =0;
		
		jsonReader.beginArray();
		while(jsonReader.hasNext() && !finProcesar) {
			jsonReader.beginObject();
			procesar_un_graph(jsonReader, graph, lConcepts);
			jsonReader.endObject();
//			numGraphProcesados++;
			numGraphProcesados=graph.size();
			if(numGraphProcesados==5) finProcesar = true;
		}
		while(jsonReader.hasNext()) {
			switch (jsonReader.peek()) {			case END_OBJECT -> jsonReader.endObject();
			case END_DOCUMENT -> jsonReader.endObject();
			case END_ARRAY -> jsonReader.endArray();
			default -> jsonReader.skipValue();	
			}
		}
//		if(!jsonReader.hasNext()) jsonReader.endObject();
		
	    return finProcesar;
	}

	/*	procesar_un_graph()
	 * 	Procesa un objeto del array @graph y lo añade a la lista graphs si en el objeto de nombre @type hay un valor que se corresponde con uno de la lista lConcepts
	 */
	
	private void procesar_un_graph(JsonReader jsonReader, List<Map<String, String>> graphs, List<String> lConcepts) throws IOException {
		// TODO:
		//	- Procesar todas las propiedades de un objeto del array @graph, guardándolas en variables temporales
		//	- Una vez procesadas todas las propiedades, ver si la clave @type tiene un valor igual a alguno de los concept de la lista lConcepts. Si es así
		//	  guardar en un mapa Map<String,String> todos los valores de las variables temporales recogidas en el paso anterior y añadir este mapa al mapa graphs
		Map<String, String> tmp = new HashMap<>();
		String key;
		while(jsonReader.hasNext()) {			
			key = jsonReader.nextName();				// Consigue lo que será la clave del mapa temporal
			switch (jsonReader.peek()) {				
			case BEGIN_OBJECT -> objectInObject(jsonReader, tmp, key);
//			case BEGIN_ARRAY -> {}
			case STRING -> tmp.put(key, jsonReader.nextString());
			case NUMBER -> tmp.put(key, jsonReader.nextString()); // tmp.put(key, Double.toString(jsonReader.nextDouble()));
			default -> jsonReader.skipValue();
			}
		}
		lConcepts.forEach(c -> {
			if(c.equals(tmp.get("@type"))) graphs.add(tmp);});
	}
	
	/*
	 * Devuelve el nombre que tiene el objeto que dentro tiene el campo @id, o null si no habia campo @id
	 */
	private String objectInObject(JsonReader jsonReader, Map<String, String> tmp, String nameNoId) throws IOException {
		String objectName = null;
		String keyNoId = null;
		jsonReader.beginObject();
		
		while(jsonReader.hasNext()) {
			keyNoId = jsonReader.nextName();
			switch (jsonReader.peek()) {				
			case BEGIN_OBJECT -> {
				objectName = keyNoId;
				objectInObject(jsonReader, tmp, objectName);
			}
//			case BEGIN_ARRAY -> {}
			case STRING -> {if(keyNoId.equals("@id")) tmp.put(nameNoId, jsonReader.nextString());
							else tmp.put(keyNoId, jsonReader.nextString());
							}
			case NUMBER -> tmp.put(keyNoId, Double.toString(jsonReader.nextDouble())); // tmp.put(key, Double.toString(jsonReader.nextDouble()));
			default -> jsonReader.skipValue();
			}
		}
		jsonReader.endObject();
		return keyNoId;
	
		}
	
}

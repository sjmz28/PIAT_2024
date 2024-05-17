package piat.opendatasearch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.stream.JsonReader;


/* En esta clase se comportara como un hilo */

/**
 * @author Natalia Agüero Knauf 47230975S
 *
 */

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
		List<Map<String,String>> graphs=new ArrayList<Map<String,String>>();	// aqui se almacenaran todos los graphs de un dataset cuyo objeto de nombre @type se corresponda con uno de los valores pasados en el la lista lConcepts
		boolean finProcesar=false;	// Para detener el parser si se han agregado a la lista graphs 5 graph
	
		Thread.currentThread().setName("JSON " + fichero);
		nombreHilo="["+Thread.currentThread().getName()+"] ";
	    System.out.println(nombreHilo+"Empezar a descargar de internet el JSON");
	    try {
	    	//creamos el imput Stream donde esta el fichero xml de donde sacamos los datos
	    	InputStreamReader inputStream = new InputStreamReader(new URL(fichero).openStream(), "UTF-8"); 
			//creamos el JsonRreader
	    	JsonReader jsonReader = new JsonReader(inputStream);
			jsonReader.setLenient(true); //habilitamos que se puedan saltar valores con skipValue()
			
			//inicio del consumo de los eventos del fichero json
			String name;
			jsonReader.beginObject(); //consumo el "{" de apertura del documento
			
			//finProcesar indica que se han encontrado ya 5 graphs y se han incorporado en el array
			while(jsonReader.hasNext() && finProcesar == false)  { //si no se han encontrado, se siguen buscando
				name = jsonReader.nextName();
				switch (name) {
				  case "@graph": //IMPORTANTE: graph es una lista de graph con sus parametros (cuando se cierre, ya no hay mas graphs)
					  finProcesar = procesar_graph(jsonReader, graphs, lConcepts);
					  if (!finProcesar) { //si  no se han encontrado, se consume el endObject de graph, pues ya han analizado todo
						  jsonReader.endObject(); 
					  }
					  break;
				  default:
					  jsonReader.skipValue();
				}
			}
			
			jsonReader.close();
			inputStream.close();
			
	    	//TODO:
			//	- Crear objeto JsonReader a partir de inputStream
			//  - Consumir el primer "{" del fichero
			//  - Procesar los elementos del fichero JSON, hasta el final de fichero o hasta que finProcesar=true
			//		Si se encuentra el objeto @graph, invocar a procesar_graph()
			//		Descartar el resto de objetos
			//	- Si se ha llegado al fin del fichero, consumir el Ãºltimo "}" del fichero
			//  - Cerrar el objeto JsonReader

		} catch (FileNotFoundException e) {
			System.out.println(nombreHilo+"El fichero no existe. IgnorÃ¡ndolo");
		} catch (IOException e) {
			System.out.println(nombreHilo+"Hubo un problema al abrir el fichero. IgnorÃ¡ndolo" + e);
		}
	    mDatasetConcepts.put(fichero, graphs); 	// Se añaden al Mapa de concepts de los Datasets 
	}

	/* 	procesar_graph()
	 * 	Procesa el array @graph
	 *  Devuelve true si ya se han añadido 5 objetos a la lista graphs
	 */
	private boolean procesar_graph(JsonReader jsonReader, List<Map<String, String>> graphs, List<String> lConcepts) throws IOException {
		boolean finProcesar=false;
		jsonReader.beginArray();  //consumo el "[" de apertura del array
		while (jsonReader.hasNext() && graphs.size() < 5) {
			jsonReader.beginObject();  //consumo el "{" de apertura
			procesar_un_graph(jsonReader, graphs, lConcepts);
			jsonReader.endObject();
		}
		if (graphs.size() == 5) {
			finProcesar = true;
		} else {
		  jsonReader.endArray();
		}
		// TODO:
		//	- Consumir el primer "[" del array @graph
		//  - Procesar todos los objetos del array, hasta el final de fichero o hasta que finProcesar=true
		//  	- Consumir el primer "{" del objeto
		//  	- Procesar un objeto del array invocando al metodo procesar_un_graph()
		//  	- Consumir el ultimo "}" del objeto
		// 		- Ver si se han añadido 5 graph a la lista, para en ese caso poner la variable finProcesar a true
		//	- Si se ha llegado al fin del array, consumir el ultimo "]" del array
		return finProcesar;
	}

	/*	procesar_un_graph()
	 * 	Procesa un objeto del array @graph y lo añade a la lista graphs si en el objeto de nombre @type hay un valor que se corresponde con uno de la lista lConcepts
	 */
	
	private void procesar_un_graph(JsonReader jsonReader, List<Map<String, String>> graphs, List<String> lConcepts) throws IOException {
		// TODO:
		//	- Procesar todas las propiedades de un objeto del array @graph, guardandolas en variables temporales
		//	- Una vez procesadas todas las propiedades, ver si la clave @type tiene un valor igual a alguno de los concept de la lista lConcepts. Si es asi­
		//	  guardar en un mapa Map<String,String> todos los valores de las variables temporales recogidas en el paso anterior y añadir este mapa al mapa graphs
		Map<String, String> tmpMap = new HashMap<String, String>();
		String tmpName;
		String tmpValue;
		boolean graphAdd = false; 
		
		while (jsonReader.hasNext()) {
			tmpName = jsonReader.nextName();
			switch (tmpName) {
			  case "@type":
				tmpValue = jsonReader.nextString();
				for (String a: lConcepts) {
					if (a.equals(tmpValue) && !tmpValue.equals(lConcepts.get(0))) {
						graphAdd = true;
						tmpMap.put("concept", tmpValue);
					}
				}
				break;
			  case "@id":
				  tmpValue = jsonReader.nextString();
				  tmpMap.put("id", tmpValue);
				  break;
			  case "link":
				  tmpValue = jsonReader.nextString();
				  tmpMap.put(tmpName, tmpValue);
				  break;
			  case "references":
				  jsonReader.beginObject();
				  while (jsonReader.hasNext()) {
					  tmpName = jsonReader.nextName();
					  if (tmpName.equals("@id")) {
						  tmpValue = jsonReader.nextString();
						  if (!tmpMap.containsKey("link")) {
							  tmpMap.put("link", tmpValue);
						  }
					  } else {
						  jsonReader.skipValue();
					  }
				  }
				  jsonReader.endObject();
				  break;
			  case "title":
				  tmpValue = jsonReader.nextString();
				  tmpMap.put(tmpName, tmpValue);
				  break;
			  case "description":
				  tmpValue = jsonReader.nextString();
				  tmpMap.put(tmpName, tmpValue);
				  break;
			  case "event-location":
				  tmpValue = jsonReader.nextString();
				  tmpMap.put("eventLocation", tmpValue);
				  break;
			  case "address":
				  jsonReader.beginObject();
				  while (jsonReader.hasNext()) {
					  if (jsonReader.nextName().equals("area")) {
						  jsonReader.beginObject();
						  while (jsonReader.hasNext()) {
							  tmpName = jsonReader.nextName();
							  switch (tmpName) {
							    case "@id":
							    	tmpValue = jsonReader.nextString();
							    	tmpMap.put("area", tmpValue);
								  break;
							    case "locality":
							    	tmpValue = jsonReader.nextString();
							    	tmpMap.put("locality", tmpValue);
							    	break;
							    case "street-address":
							    	tmpValue = jsonReader.nextString();
							    	tmpMap.put("street", tmpValue);
							    	break;
							    default:
							    	jsonReader.skipValue();
							  }
						  }
						  jsonReader.endObject();
					  } else {
						  jsonReader.skipValue();
					  }
				  }
				  jsonReader.endObject();
				  break;
			  case "dtstart":
				  tmpValue = jsonReader.nextString();
				  tmpMap.put("start", tmpValue);
				  break;
			  case "dtend":
				  tmpValue = jsonReader.nextString();
				  tmpMap.put("end", tmpValue);
				  break;
			  case "location":
				  jsonReader.beginObject();
				  while (jsonReader.hasNext()) {
					  tmpName = jsonReader.nextName();
					  switch (tmpName) {
					  case "latitude":
						  tmpValue = jsonReader.nextString();
						  tmpMap.put(tmpName, tmpValue);
						  break;
					  case "longitude":
						  tmpValue = jsonReader.nextString();
						  tmpMap.put(tmpName, tmpValue);
						  break;
					  default:
						  jsonReader.skipValue();
					  }
				  }
				  jsonReader.endObject();
				  break;
			  default:
				jsonReader.skipValue();
			}
		}
		
		if (graphAdd) {
			graphs.add(tmpMap);
		}
	}
}

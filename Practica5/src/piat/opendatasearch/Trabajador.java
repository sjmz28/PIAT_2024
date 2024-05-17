package piat.opendatasearch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.stream.JsonReader;

/**
 * @author Sara Jimenez Muñoz s.jmunoz@alumnos.upm.es
 *
 */

/* En esta clase se comportará como un hilo */

public class Trabajador implements Runnable {
	private String fichero;
	private List<String> lConcepts;
	private Map<String, List<Map<String,String>>> mDatasetConcepts;
	private String nombreHilo;
	
	
	public Trabajador (String fichero, List<String> lConcepts, Map<String, List<Map<String,String>>> mDatasetConcepts) { 
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
	    	AnalizadorJSON analizadorJSON= new AnalizadorJSON(jsonReader, graphs, lConcepts);
	    	while (jsonReader.hasNext() && !finProcesar){
	    		String s = jsonReader.nextName();
	    		
	    		if(s.equals("@graph")) {
	    			analizadorJSON.analizarRecursos(); 
	    		}
	    		else jsonReader.skipValue();
	    	}
	    	
	    	if(!jsonReader.hasNext()) jsonReader.endArray();
	    	jsonReader.close();
			inputStream.close();
			
			mDatasetConcepts.put(fichero, analizadorJSON.getRecursos()); 
			
		} catch (FileNotFoundException e) {
			System.out.println(nombreHilo+"El fichero no existe. Ignorándolo");
		} catch (IOException e) {
			System.out.println(nombreHilo+"Hubo un problema al abrir el fichero. Ignorándolo " + e);
		}
	    	 
	}

	
	
}

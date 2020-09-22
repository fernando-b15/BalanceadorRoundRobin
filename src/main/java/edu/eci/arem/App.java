package edu.eci.arem;

import static spark.Spark.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;

import spark.Request;
import spark.Response;


/**
 * @author Fernando Barrera Barrera
 *
 */
public class App 
{
	private static int ports[]= {8001,8002,8003};
	private static int selected = 0 ; 
	
	/**
     * Este metodo main que  inicia el balancedor round robin y donde se definene dos servicios rest por medio de funciones lambda
     */
    public static void main( String[] args ) throws UnknownHostException
    {
    	port(getPort());
    	get("/", (req, res) ->  inputView(req, res));
    	post("/", (req, res) ->  register(req, res));
    	
    	   	
    }
    /**
     *Este metodo contruye la vista inputView del metodo get del balancedor con un pequeño formulario para adicionar nuevos logs y una tabla en base a los datos que le
     *retorne el logservice seleccionado
     *
     * @param req Tiene la informacion de la petición que llega al servidor.
     * @param res Tiene la información con la respuesta del servidor.
     * @return String con la informacion html de la vista de entrada.
     */
     
    private static String  inputView(Request req, Response res){
    	 String view="";
		 try {
			URL url = new URL("http://ec2-18-207-203-91.compute-1.amazonaws.com:"+String.valueOf(ports[selected])+"/consultlogs");
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			String inputLine;
			String datos="";
			while ((inputLine = reader.readLine()) != null) {
	                System.out.println(inputLine);
	                datos+=inputLine;
	        }
			String logs[]=datos.split(",");
			String logsToTable="";
			String server = "Esta Actualmente en el LogService No"+String.valueOf(selected+1);
			changePortBalance();
			for(String log:logs) {
				System.out.println("xd  "+log);
				String info[]=log.split("-");
				logsToTable+="<tr><td>"+info[0]+"</td><td>"+info[1]+"</td><td>"+info[2]+"</td></tr>";
				
			}
			view = "<!DOCTYPE html>"
	                 + "<html>"
	                 + "<body style=\"background-color:#32CD32;\">"
	                 + "<style>"
	                 + "table, th, td {"
	                 + "border: 1px solid black;"
	                 + "border-collapse: collapse;"
	                 + "}"
	                 + "</style>"
	                 +"<center>"
	                 +"<h1>Lista de Logs</h1>"
	                 +"<br/>"
	                 +"<h2>"+server+"</h2>"
	                 + "<form name='loginForm' method='post' action='/'>"
	                 +"Log: <input type='text' name='message'/> <br/>"
	                 +"<br/>"
	                 +"<input type='submit' value='submit' />"
	                 +"</form>"
	                 +"<br/>"
	 				 + "<Table>"
	 				 + "<tr>"
	 				+ "<th>No</th>"
	 			     + "<th>Message</th>"
	 			     + "<th>Date</th>"
	 			  	 + "</tr>"
	 			     + logsToTable
	 			     + "</Table>"
	                 +"</center>"
	                 + "</body>"
	                 + "</html>";
		 
	    } catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
    	return view;
    	
    }
    /**
     *Este metodo contruye  al metodo post del balancedor que simplemente se encarga de enviar una peticion get al logservice seleccionado
     *para adicionar un nuevo log y llama la vista inputview para actualizar la tabla de logs de la vista y se evidencia que se inserto el log
     *
     * @param req Tiene la informacion de la petición que llega al servidor.
     * @param res Tiene la información con la respuesta del servidor.
     * @return String con la informacion html actualizad de la vista iputview().
     */
    private static String  register(Request req, Response res){
    	try {
			URL url = new URL("http://ec2-18-207-203-91.compute-1.amazonaws.com:"+String.valueOf(ports[selected])+"/savelogs?message="+(req.queryParams("message").replace(' ','_')));
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			changePortBalance();
			String inputLine;
			while ((inputLine = reader.readLine()) != null) {
	                System.out.println(inputLine);
			}        
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return inputView(req,res);
    	
    }
    /**
     * Este metodo simplemenete es el metodo que se encarga de cambiar la variable estica selected que corresponda al indice del puerto del vector 
     * de puertos ports y este puerto hace referencia al logservice al cual se le solicitara  la siguiente operacion ya sea consulta o insercion
     */
    private static void changePortBalance() {
    	if(selected<2) {
    		selected+=1;
    	}
    	else {
    		selected  = 0;
    	}
    }
    /**
     *Este metodo se encarga de retonar el puerto por defecto que esta definido en una variable de entorno 
     *para correr el servidor web sobre ese puerto.
     */
    static int getPort() {
	   	 if (System.getenv("PORT") != null) {
	   		 return Integer.parseInt(System.getenv("PORT"));
	   	 }
	   	 return 4000; //returns default port if heroku-port isn't set
  }
}  

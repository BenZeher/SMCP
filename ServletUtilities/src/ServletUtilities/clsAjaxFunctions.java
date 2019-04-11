package ServletUtilities;

import java.sql.Connection;
import java.sql.Statement;

import javax.servlet.ServletContext;


public class clsAjaxFunctions {
	
	public static void ajax_Update_MySQL(
				ServletContext context, 
				String sDBID, 
				String sUserFullName,
				String SQLCommand
				) throws Exception{
				
				Connection conn = null;
				try {
					conn = clsDatabaseFunctions.getConnectionWithException(
						context, 
						sDBID, 
						"MySQL", 
						"clsAjaxFunctions.async_Update [1529689329] - user: " + sUserFullName);
				} catch (Exception e) {
					throw new Exception("Error [1529689330] getting connection - " + e.getMessage());
				}
				
				try {				
					ajax_Update(conn,SQLCommand);	
					
				} catch (Exception e) {
					clsDatabaseFunctions.freeConnection(context, conn, "[1546998927]");
					throw new Exception("Error [1529689042] saving - " + e.getMessage());
				}
				
				clsDatabaseFunctions.freeConnection(context, conn, "[]1547047424");
				return;
			}
	 
	 public static void ajax_Update(
			 	Connection conn,
				String SQLCommand
				) throws Exception{
		 
				try {
					Statement stmt = conn.createStatement();
					stmt.execute(SQLCommand);
				} catch (Exception e) {
					throw new Exception("Error [1529689042] updating with SQL: " + SQLCommand + " - " + e.getMessage() + ".");
				}				

				return;
			}
	 
	 public static String ajax_Request_Javascript(
			 String sRequestServlet, 
			 String sRequestParams, 
			 ServletContext context) {
			//call asyncRequest in javascript to send a request to a servlet
		 
			String s = "function asyncRequest() {\n"
	                + "var xhr = new XMLHttpRequest();\n\n" 
	                //Define how the response should be handled
	                + "xhr.onreadystatechange = function(){\n"  
	                		//If the response is ready then display it in the status
	                + "    if (this.readyState == 4 && this.status == 200){\n"
	                			//If there is a warning is the response
	                + "   		if (this.responseText.includes(\"Error\")){\n"
	                + "			     document.getElementById(\"updatenotificationtext\").style.color = 'red';\n"		
	                + "        		 document.getElementById(\"updatenotificationtext\").innerHTML = this.responseText; \n"
	                			//Otherwise display response as status message
	                + "    		}else{\n"
	                + "         	document.getElementById(\"updatenotificationtext\").innerHTML = this.responseText; \n"
	                + "         	setTimeout(function(){\n"
	               + " 				location.reload();\n"	
	                + "				}, 4000); \n"
	                + "			}\n" 
	                + "    }"
	                		//The request completely failed.  
	                + "     if (this.readyState == 4 && this.status != 200){\n"
	                + "        		 document.getElementById(\"updatenotificationtext\").innerHTML = this.responseText; \n"
	                + "	   }\n" 
	                +"};\n\n"
	                
	                //Send the request
	                + "xhr.open(\"POST\", \"" + clsServletUtilities.getURLLinkBase(context) + sRequestServlet + "\");\n" 
	                + "xhr.setRequestHeader(\"Content-Type\", \"application/x-www-form-urlencoded\");\n"
	                + "xhr.send(\"" + sRequestParams + "\");\n"
					+ "}\n\n"
					;
			
		 return s; 
	 }
}

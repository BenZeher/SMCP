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
}

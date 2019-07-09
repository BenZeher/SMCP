package smcontrolpanel;

import java.text.SimpleDateFormat;
import SMDataDefinition.*;

public class SMMySQLs extends SMClasses.MySQLs{
	static String SQL = "";
	static SimpleDateFormat USDateOnlyformatter = new SimpleDateFormat("MM/dd/yyyy");
		
	
	public static String Get_Sales_Contact_By_ID_SQL(int id){
		
		SQL = "SELECT * FROM " + SMTablesalescontacts.TableName + 
			  " WHERE" + 
			  	" " + SMTablesalescontacts.id + " = " + id;
		
		//System.out.println(SQL);
		return SQL;
	}
	
	public static String Get_Project_Type_SQL(){
		
		SQL = "SELECT * FROM " + SMTableprojecttypes.TableName;
		
		//System.out.println(SQL);
		return SQL;
	}

}

package TimeCardSystem;

import java.io.*;
import java.util.ArrayList;
import java.text.DecimalFormat;

import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;
import TCSDataDefinition.EmployeeAuxiliaryInfo;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;


/* Servlet that insert In-Time records into the the time entry table.*/

public class EmployeeGeneralSave extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	      
	    response.setContentType("text/html");
		HttpSession CurrentSession = request.getSession();
	    PrintWriter out = response.getWriter();
		
	    String title = "Time Card System";
	    String subtitle = "";
	    
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));
	    //out.println("Saving information for " + request.getParameter("EmployeeID") + "<BR>");
	    ArrayList<String> alSQLs = new ArrayList<String>(0);
	    
	    try {
	    	if (CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID).toString().compareTo("a0b0c0") == 0){
	    		//creating new employee, check to see employee id availability and prepare for insertion.
	    		String sSQL = TimeCardSQLs.Get_Employee_Info_SQL(request.getParameter("EmployeeID"));
	    		ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	    		if (rs.next()){
	    			//<BR>the EmployeeID is taken, don't save.");
		    		out.println ("<BR>");
		        	out.println ("<H4>The Employee ID is taken. please go back to the previous page and choose another one.</H4><BR><BR>");
	    		}else{
	    			alSQLs.add(Get_Insert_Employee_Info_SQL(request));
	    			String sInsertAuxInfoSQL;
					try {
						sInsertAuxInfoSQL = Get_Insert_Employee_Auxiliary_Info_SQL(request, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
					} catch (Exception e1) {
						throw new Exception("Error [1539616051] - " + e1.getMessage());
					}
	    			alSQLs.add(sInsertAuxInfoSQL);

	    			try {
						clsDatabaseFunctions.executeSQLsInTrans(alSQLs, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
					} catch (Exception e) {
						out.println ("<H4>Error [1539616041] adding new employee: " + e.getMessage() + "</H4><BR><BR>");
						out.println ("<A href=" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeList>Click here to return to employee list.</A>");
						out.println("</BODY></HTML>");
						rs.close();
						return;
					}
	    			
    				out.println ("<BR>");
    	        	out.println ("<H4>New employee was successfully added.</H4><BR><BR>");
    	        	out.println("<META http-equiv='Refresh' content='1;URL=" + TimeCardUtilities.URLDecode(request.getParameter("OriginalURL")) + "''>");
	    		}
	    		rs.close();
	    	}else{
	    		//editing existing employee. In this case, prepare for updating
	    		alSQLs.add(Get_Update_Employee_Info_SQL(request));

	    		//check to see if there is existing Auxi info for this employee
	    		//if not, do insert, if yes, do update.
	    		String sSQL = TimeCardSQLs.Get_Employee_Auxiliary_Info(request.getParameter("EmployeeID"));
	    		ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	    		if (rs.next()){
	    			alSQLs.add(Get_Update_Employee_Auxiliary_Info_SQL(request, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB)));
	    		}else{
	    			alSQLs.add(Get_Insert_Employee_Auxiliary_Info_SQL(request, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB)));
	    		}
	    		rs.close();
	    		
    			//for (int i=0;i<alSQLs.size();i++){
    			//	System.out.println("SQL in ArrayList"+ "(" + i + "): " + alSQLs.get(i).toString());
    			//}
    			
	    		try {
					clsDatabaseFunctions.executeSQLsInTrans(alSQLs, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
				} catch (Exception e) {
					out.println("<BR><BR>Error [1539616042] updating employee information - " + e.getMessage() + "<BR>");
			    	out.println ("<A href=" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeList>Click here to return to employee list.</A>");
			    	out.println("</BODY></HTML>");
			    	return;
				}
    				out.println ("<BR>");
    	        	out.println ("<H4>Information saved successfully.</H4><BR><BR>");
    	        	out.println("<META http-equiv='Refresh' content='1;URL=" + TimeCardUtilities.URLDecode(request.getParameter("OriginalURL")) + "'>");
	    	}
	    } catch (Exception ex) {
	        // Most of the time, it is session expired.
	    	out.println("Exception : " + ex.getMessage());
	    }
	
	    out.println("</BODY></HTML>");
	}
	
	private static String Get_Insert_Employee_Info_SQL(HttpServletRequest request){

		return TimeCardSQLs.Get_Insert_Employee_SQL(request.getParameter("EmployeeID"), 
												    request.getParameter("EmployeeFirstName"), 
												    request.getParameter("EmployeeMiddleName"), 
												    request.getParameter("EmployeeLastName"),  
												    request.getParameter("EmployeeDepartment"), 
												    request.getParameter("EmployeeExtension"),  
												    request.getParameter("EmployeeOfficePhoneAreaCode") + request.getParameter("EmployeeOfficePhoneSwitch") + request.getParameter("EmployeeOfficePhoneEndUnit"), 
												    request.getParameter("EmployeeCellPhoneAreaCode") + request.getParameter("EmployeeCellPhoneSwitch") + request.getParameter("EmployeeCellPhoneEndUnit"),  
												    request.getParameter("EmployeeHomePhoneAreaCode") + request.getParameter("EmployeeHomePhoneSwitch") + request.getParameter("EmployeeHomePhoneEndUnit"), 
												    request.getParameter("EmployeeEmail"),  
												     request.getParameter("AddressLine1"),
												     request.getParameter("AddressLine2"),
												     request.getParameter("AddressCity"),
												     request.getParameter("AddressState"),
												     request.getParameter("AddressZipCode"),
												     request.getParameter("AddressCountry"),
												    request.getParameter("NextelDirectCall"),  
												    Integer.parseInt(request.getParameter("IsPhoneService")), 
												    Integer.parseInt(request.getParameter("IsPhoneInsured")));
		
	}
	
	private static String Get_Update_Employee_Info_SQL(HttpServletRequest request){
		
		return TimeCardSQLs.Get_Update_Employee_SQL(request.getParameter("EmployeeID"), 
												    request.getParameter("EmployeeFirstName"), 
												    request.getParameter("EmployeeMiddleName"), 
												    request.getParameter("EmployeeLastName"), 
												    request.getParameter("EmployeeDepartment"), 
												    request.getParameter("EmployeeExtension"),  
												    request.getParameter("EmployeeOfficePhoneAreaCode") + request.getParameter("EmployeeOfficePhoneSwitch") + request.getParameter("EmployeeOfficePhoneEndUnit"), 
												    request.getParameter("EmployeeCellPhoneAreaCode") + request.getParameter("EmployeeCellPhoneSwitch") + request.getParameter("EmployeeCellPhoneEndUnit"),  
												    request.getParameter("EmployeeHomePhoneAreaCode") + request.getParameter("EmployeeHomePhoneSwitch") + request.getParameter("EmployeeHomePhoneEndUnit"), 
												    request.getParameter("EmployeeEmail"),  
												    request.getParameter("AddressLine1"),
												    request.getParameter("AddressLine2"),
												    request.getParameter("AddressCity"),
												    request.getParameter("AddressState"),
												    request.getParameter("AddressZipCode"),
												    request.getParameter("AddressCountry"),
												    request.getParameter("NextelDirectCall"),  
												    Integer.parseInt(request.getParameter("IsPhoneService")), 
												    Integer.parseInt(request.getParameter("IsPhoneInsured"))
												    );
		
	}
	
	private static String Get_Update_Employee_Auxiliary_Info_SQL(HttpServletRequest request, 
																 ServletContext context, 
																 String conf){
		
		String s;
		DecimalFormat df = new DecimalFormat("00");
		//find out what columns do we have right now.
		String sSQL = TimeCardSQLs.Get_Employee_Auxiliary_Info("ADMIN");
        
        s = "UPDATE EmployeeAuxiliaryInfo SET";
        try{
            ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, context, conf);
            ResultSetMetaData metaEAInfo = rs.getMetaData();
	        for (int i=3;i<=metaEAInfo.getColumnCount();i++){
	        	if (
		        	(metaEAInfo.getColumnName(i).compareToIgnoreCase(EmployeeAuxiliaryInfo.id) != 0) 
		        	&& (metaEAInfo.getColumnName(i).compareToIgnoreCase(EmployeeAuxiliaryInfo.sEmployeeID) != 0)
		        ){
		        	switch (metaEAInfo.getColumnType(i)){
		        		case Types.VARCHAR:	
		        			s = s + " `" + metaEAInfo.getColumnName(i) + "` = '" + 
		        			clsDatabaseFunctions.FormatSQLStatement(request.getParameter(metaEAInfo.getColumnName(i))) + "',";break;
		        		case Types.DATE:	
		        			s = s + " `" + metaEAInfo.getColumnName(i) + "` = '" + 
						  				  request.getParameter(metaEAInfo.getColumnName(i) + "SelectedYear") + "-" + 
						  				  df.format(Double.parseDouble(request.getParameter(metaEAInfo.getColumnName(i) + "SelectedMonth"))) + "-" +
						  				  df.format(Double.parseDouble(request.getParameter(metaEAInfo.getColumnName(i) + "SelectedDay"))) + 
						  				  "',";break;
		        		case Types.TIME:
		        			s = s + " `" + metaEAInfo.getColumnName(i) + "` = '" + 
		        			TimeCardUtilities.ConstructTimeString(df.format(Double.parseDouble(request.getParameter(metaEAInfo.getColumnName(i) + "SelectedHour"))),
		        												  df.format(Double.parseDouble(request.getParameter(metaEAInfo.getColumnName(i) + "SelectedMinute"))),
										        				  request.getParameter(metaEAInfo.getColumnName(i) + "SelectedAMPM") 
										        				  ) + "',";break;
		        		case Types.DOUBLE:
		        			s = s + " `" + metaEAInfo.getColumnName(i) + "` = " + request.getParameter(metaEAInfo.getColumnName(i)) + ",";break;
		        		case Types.INTEGER:
		        			s = s + " `" + metaEAInfo.getColumnName(i) + "` = " + request.getParameter(metaEAInfo.getColumnName(i)) + ",";break;
		        	}
	        	}
			}
	        //cut the last comma
	        s = s.substring(0, s.length() - 1) + " WHERE sEmployeeID = '" + request.getParameter("EmployeeID") + "'";
	        rs.close();
        }catch(Exception ex){
        	System.out.println("Error in Get_Update_Employee_Auxiliary_Info_SQL: " + ex.toString());
        }
		return s;
	}
	
	private static String Get_Insert_Employee_Auxiliary_Info_SQL(HttpServletRequest request, 
																 ServletContext context, 
																 String conf) throws Exception{
		
		//TJR - 10/15/2018 - This function was built to allow new fields to be added by an administrator.
		// So the SQL statement can't be built ahead of time.  If reads the table 'meta-data' to figure out how to construct the statement.
		
		//find out what columns do we have right now.
		String sSQL = TimeCardSQLs.Get_Employee_Auxiliary_Info("ADMIN");
		
        String sINSERTSegmentOfCommand = "INSERT INTO EmployeeAuxiliaryInfo (`sEmployeeID`,";
        String sVALUESSegmentOfCommand = " VALUES('" + request.getParameter("EmployeeID") + "',";
        DecimalFormat df = new DecimalFormat("00");        
        try{
            ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, context, conf);
            ResultSetMetaData metaEAInfo = rs.getMetaData();
            
            //for (int i=1;i<=metaEAInfo.getColumnCount();i++){
            //	System.out.println("[1539618856] i = '" + Integer.toString(i) + "' - metaEAInfo.getColumnName(i) = '" + metaEAInfo.getColumnName(i) + "'.");
            //}
            
	        for (int i=1;i<=metaEAInfo.getColumnCount();i++){
	        	//We don't need to read the 'id' (an autoincrement) or 'EmployeeID' (we're wiring this one in above) fields:
	        	if (
	        		(metaEAInfo.getColumnName(i).compareToIgnoreCase(EmployeeAuxiliaryInfo.id) != 0) 
	        		&& (metaEAInfo.getColumnName(i).compareToIgnoreCase(EmployeeAuxiliaryInfo.sEmployeeID) != 0)
	        	){
		        	switch (metaEAInfo.getColumnType(i)){
		        		case Types.VARCHAR:
		        			sINSERTSegmentOfCommand = sINSERTSegmentOfCommand + " `" + metaEAInfo.getColumnName(i) + "`,"; 
		        			sVALUESSegmentOfCommand = sVALUESSegmentOfCommand + " '" + clsDatabaseFunctions.FormatSQLStatement(request.getParameter(metaEAInfo.getColumnName(i))) + "',";break;
		        		case Types.DATE:	//print out an input box for user to type a string
		        			sINSERTSegmentOfCommand = sINSERTSegmentOfCommand + " `" + metaEAInfo.getColumnName(i) + "`,"; 
						  	sVALUESSegmentOfCommand = sVALUESSegmentOfCommand + " '" + request.getParameter(metaEAInfo.getColumnName(i) + "SelectedYear") + "-" + 
						  					 df.format(Double.parseDouble(request.getParameter(metaEAInfo.getColumnName(i) + "SelectedMonth"))) + "-" +
						  					 df.format(Double.parseDouble(request.getParameter(metaEAInfo.getColumnName(i) + "SelectedDay"))) + 
						  				  "',";break;
		        		case Types.TIME:
		        			sINSERTSegmentOfCommand = sINSERTSegmentOfCommand + " `" + metaEAInfo.getColumnName(i) + "`,"; 
		        			sVALUESSegmentOfCommand = sVALUESSegmentOfCommand + " '" + TimeCardUtilities.ConstructTimeString(df.format(Double.parseDouble(request.getParameter(metaEAInfo.getColumnName(i) + "SelectedHour"))),
													        					   df.format(Double.parseDouble(request.getParameter(metaEAInfo.getColumnName(i) + "SelectedMinute"))),
													        					   request.getParameter(metaEAInfo.getColumnName(i) + "SelectedAMPM") 
														        				   ) + "',";break;
		        		case Types.DOUBLE:
		        			sINSERTSegmentOfCommand = sINSERTSegmentOfCommand + " `" + metaEAInfo.getColumnName(i) + "`,"; 
							sVALUESSegmentOfCommand = sVALUESSegmentOfCommand + " " + clsDatabaseFunctions.FormatSQLStatement(request.getParameter(metaEAInfo.getColumnName(i))) + ",";break;
		        		case Types.INTEGER:
		        			sINSERTSegmentOfCommand = sINSERTSegmentOfCommand + " `" + metaEAInfo.getColumnName(i) + "`,"; 
							sVALUESSegmentOfCommand = sVALUESSegmentOfCommand + " " + request.getParameter(metaEAInfo.getColumnName(i)) + ",";break;
		        	}
	        	}
			}
	        rs.close();
	        
        }catch(Exception ex){
        	throw new Exception("Error [1539618192] inserting Employee Aux info - sINSERTSegmentOfCommand = '" 
        		+ sINSERTSegmentOfCommand + "', sVALUESSegmentOfCommand = '" + sVALUESSegmentOfCommand + "' - " + ex.getMessage());
        }

        //Now put the 'INSERT' segment and the 'VALUES' segment together to make a complete insert command:
        String sCompleteSQLCommand = sINSERTSegmentOfCommand.substring(0, sINSERTSegmentOfCommand.length() - 1) + ") " + sVALUESSegmentOfCommand.substring(0, sVALUESSegmentOfCommand.length() - 1) + ")";
        //System.out.println("[1539618520] - " + sCompleteSQLCommand);
        //throw new Exception("[1539618521]");
        return sCompleteSQLCommand;
	}
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}
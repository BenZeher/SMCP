package TimeCardSystem;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import TCSDataDefinition.SpecialEntryTypes;

import java.sql.*;

/** Servlet that insert In-Time records into the the time entry table.*/

public class TimeEntryRemove extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
   
	    response.setContentType("text/html");
	    PrintWriter out = response.getWriter();
	    HttpSession CurrentSession = request.getSession();
		
	    String title = "Time Card System";
	    String subtitle = "";
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));
	    
	    String sDBID = (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB);
	    String sUserID = (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID);
	    String sPunchTypeLabel = "";
	    String sEmployeeID = clsManageRequestParameters.get_Request_Parameter("EmployeeID", request);
	    String sSpecialEntryType = clsManageRequestParameters.get_Request_Parameter("SpecialEntryType", request);
	    
	    try {
	    	String sSQL;
	    	
	    	//out.println ("StartTimestamp: " + sqlStartTimeStamp.toString());
	    	//out.println ("EndTimestamp: " + sqlEndTimeStamp.toString());
	    	if (request.getParameter("DoubleCheck") == null){
	    		//don't delete, just go back.
	    		out.println("<META http-equiv='Refresh' content='0;URL=" + request.getParameter("OriginalURL") + "#" + request.getParameter("EmployeeID") + "'>");
	    	}else{
	    		//delete the selected entry
	    		if (Integer.parseInt(request.getParameter("Type")) == 0){
	    			sPunchTypeLabel  ="IN";
	    			sSQL = TimeCardSQLs.Get_Update_In_time_SQL(Integer.parseInt(request.getParameter("id")), 
						"0000-00-00 00:00:00", 
						1, //you remove it, must be modified. 
						0, //early start should be removed as well
						0, //late should be removed as well
						0, //late minute should be removed as well
						"KEEPTHENOTE", //the notes should stay
						Integer.parseInt(request.getParameter("SpecialEntryType"))); //retain old value
	    		}else{
	    			sPunchTypeLabel = "OUT";
	    			sSQL = TimeCardSQLs.Get_Update_Out_time_SQL(Integer.parseInt(request.getParameter("id")), 
						"0000-00-00 00:00:00", 
						1, //you remove it, must be modified.
						"KEEPTHENOTE", //the notes should stay 
						Integer.parseInt(request.getParameter("SpecialEntryType"))); 
	    		}
	    		if (clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB))){
		    		//check to see if both in time and out time are 0000, wipe the whole thing out.
	    			sSQL = TimeCardSQLs.Retieve_Specific_Time_Entry(Integer.parseInt(request.getParameter("id")));
	    			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
	    			
	    			if (rs.next()){
	    				if (rs.getString("dtInTime").compareTo("0000-00-00 00:00:00") == 0 && 
	    					rs.getString("dtOutTime").compareTo("0000-00-00 00:00:00") == 0){
	    					//remove the record
	    					sSQL = TimeCardSQLs.Remove_Specific_TimeEntry(Integer.parseInt(request.getParameter("id")));
	    					if (clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB))){
	    						out.println ("<META http-equiv='Refresh' content='0;URL=" + request.getParameter("OriginalURL") + "#" + request.getParameter("EmployeeID") + "'>");
	    						out.println ("<BR><H2>You've modified the time entry sucessfully.</H2>");
	    					}
	    				}else{
	    					out.println ("<META http-equiv='Refresh' content='0;URL=" + request.getParameter("OriginalURL") + "#" + request.getParameter("EmployeeID") + "'>");
    						out.println ("<BR><H2>You've modified the time entry sucessfully.</H2>");
	    				}
	    			}else{
	    				out.println("<BR><H2>Nullified the time entry successfully, but failed to wipe out the entry blank record.</H2><BR>");
	    				out.println ("<TD><A HREF=\"" + response.encodeURL(request.getParameter("OriginalURL")) + "#" + request.getParameter("EmployeeID") + "\"><IMG src=\"" 
							+ TCWebContextParameters.getInitImagePath(getServletContext()) 
							+ "return.gif\"></A></TD>");
	    			}
	    			rs.close();
	    			
		    	}else{
		    		out.println("<BR><H2>Failed to nullify the time entry.</H2><BR>");
		    		out.println ("<TD><A HREF=\"" + response.encodeURL(request.getParameter("OriginalURL")) + "#" + request.getParameter("EmployeeID") + "\"><IMG src=\"" 
						+ TCWebContextParameters.getInitImagePath(getServletContext()) 
						+ "return.gif\"></A></TD>");
		    	}
	    		
	    		String sSpecialEntryTypeDescription = "";
	    		String sSpecialEntryTypeSQL = "SELECT"
	    			+ " " + SpecialEntryTypes.sTypeDesc
	    			+ " FROM " + SpecialEntryTypes.TableName
	    			+ " WHERE ("
	    				+ "(" + SpecialEntryTypes.iTypeID + " = " + sSpecialEntryType + ")"
	    			+ ")"
	    		;
	    		ResultSet rsSpecialEntryType = clsDatabaseFunctions.openResultSet(
	    			sSpecialEntryTypeSQL, 
	    			getServletContext(), 
	    			sDBID, 
	    			"MySQL", 
	    			clsServletUtilities.getFullClassName(this.toString()) + ".getSpecialEntryTypes - user: " + sUserID
	    		);
	    		if (rsSpecialEntryType.next()){
	    			sSpecialEntryTypeDescription = rsSpecialEntryType.getString(SpecialEntryTypes.sTypeDesc);
	    		}
	    		rsSpecialEntryType.close();
	    		
	    		TCLogEntry log = new TCLogEntry(sDBID, getServletContext());
	    		log.writeEntry(
	    			sUserID, 
	    			TCLogEntry.LOG_OPERATION_ADMIN_REMOVED_TIME_ENTRY, 
	    			"Removed punch " + sPunchTypeLabel
	    				+ " for '" + sEmployeeID
	    				+ ", special entry type '" + sSpecialEntryTypeDescription + "'"
	    			, 
	    			sSQL, 
	    			"[1518031613]"
	    		);
	    	}
	    } catch (Exception ex) {
	        // handle any errors
	    	out.println("<BR><BR>Error!!<BR>");
	        out.println("SQLException: " + ex.getMessage() + "<BR>");
	    }
	
	    out.println("</BODY></HTML>");
	}
}
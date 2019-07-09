package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class CustomerCallLogListGenerate extends HttpServlet {
	//OBSOLETE?
	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMCustomerCallList))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "Customer Call List";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

    	//Calculate time period
	    SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");
    	String sStartingDate = clsManageRequestParameters.get_Request_Parameter("StartingDate", request);
    	String sEndingDate = clsManageRequestParameters.get_Request_Parameter("EndingDate", request);
    
    	/*if (!SMUtilities.IsValidDateString("M/d/yyyy", sStartingDate)){
    		out.println("Invalid start date.");
    		return;
    	}
    	if (!SMUtilities.IsValidDateString("M/d/yyyy", sEndingDate)){
    		out.println("Invalid start date.");
    		return;
    	}
*/
    	Date SelectedStartingDay = null;
		try {
			SelectedStartingDay = clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sStartingDate);
		} catch (ParseException e) {
			out.println("Invalid start date '" + sStartingDate + "' - " + e.getMessage());
    		return;
		}
    	Date SelectedEndingDay = null;
		try {
			SelectedEndingDay = clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sEndingDate);
		} catch (ParseException e) {
			out.println("Invalid end date '" + sEndingDate + "' - " + e.getMessage());
    		return;
		}
	    int iOrderSource = Integer.parseInt(request.getParameter("OrderSource"));
	    boolean bShowCallDetail = true;
	    
	    if (request.getParameter("ShowCallDetail") == null){
	    	//don't show call details
	    	bShowCallDetail = false;
	    }else{
	    	//show call details.
	    	bShowCallDetail = true;
	    }
	    
	    try{
	    	String sSQL = "";
	    	sSQL = "SELECT * FROM " + SMTablecustomercalllog.TableName + ", " + SMTableordersources.TableName + 
	    			" WHERE" +
	    			" " + SMTablecustomercalllog.TableName + "." + SMTablecustomercalllog.iOrderSourceID + " = " + 
	    			SMTableordersources.TableName + "." + SMTableordersources.iSourceID + 
	    			" AND" +
	    			" " + SMTablecustomercalllog.TableName + "." + SMTablecustomercalllog.datCallTime + " >= '" + SelectedStartingDay.toString() + " 00:00:00" + "'" +
	    			" AND" +
	    			" " + SMTablecustomercalllog.TableName + "." + SMTablecustomercalllog.datCallTime + " <= '" + SelectedEndingDay.toString() + " 23:59:59" + "'";
	    	if (iOrderSource != 0){
	    		sSQL = sSQL + " AND " + 
	    				" " + SMTablecustomercalllog.TableName + "." + SMTablecustomercalllog.iOrderSourceID + " = '" + iOrderSource + "'";
	    	}

	    	sSQL = sSQL + " ORDER BY " +
	    			SMTablecustomercalllog.TableName + "." + SMTablecustomercalllog.iOrderSourceID + "," + 
	    			SMTablecustomercalllog.TableName + "." + SMTablecustomercalllog.datCallTime;
			
		    ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
		    //get total call number
		    int iRowCount = 0;
		    while (rs.next()){
		    	iRowCount++;
		    }
		    rs.beforeFirst();
		    //print out column headers
		    out.println("<TABLE BORDER=1 WIDTH=100%>");
		    out.println("<TR>" +
		    				//"<TD ALIGN=CENTER WIDTH=10%><B>ID</B></TD>" +
		    				"<TD ALIGN=CENTER VALIGN=TOP WIDTH=25%><B>Call Time</B></TD>" +
		    				"<TD ALIGN=CENTER VALIGN=TOP WIDTH=15%><B>Customer Name</B></TD>" +
		    				"<TD ALIGN=CENTER VALIGN=TOP WIDTH=20%><B>Phone Number</B></TD>" +
		    				"<TD ALIGN=CENTER VALIGN=TOP WIDTH=15%><B>City</B></TD>" +
		    				"<TD ALIGN=CENTER VALIGN=TOP WIDTH=25%><B>Note</B></TD></TR>");
		    int iCurrentSource = 0;
		    String sCurrentSource = "";
		    int iCurrentCallCount = 0;
		    while (rs.next()){
		    	if (rs.getInt(SMTablecustomercalllog.iOrderSourceID) != iCurrentSource){
		    		if (iCurrentSource != 0 && iOrderSource == 0){
		    			//output total count and percentage
		    			if (bShowCallDetail){
		    				out.println("<TR><TD ALIGN=RIGHT VALIGN=TOP COLSPAN=5><FONT SIZE=3><B>Number of calls from \"" + sCurrentSource + "\": " + iCurrentCallCount + " (" + SMUtilities.RoundHalfUp(iCurrentCallCount * 100.0 / iRowCount, 2) + "%)</FONT></B></TD></TR>");
		    			}else{
		    				out.println("<TR><TD ALIGN=LEFT VALIGN=TOP COLSPAN=5><FONT SIZE=3><B>Number of calls from \"" + sCurrentSource + "\": " + iCurrentCallCount + " (" + SMUtilities.RoundHalfUp(iCurrentCallCount * 100.0 / iRowCount, 2) + "%)</FONT></B></TD></TR>");
		    			}
		    		}
		    		if (bShowCallDetail){
		    			//new source, print out a sector break
		    			out.println("<TR><TD ALIGN=LEFT VALIGN=TOP COLSPAN=5><FONT SIZE=4><B>" + rs.getInt(SMTableordersources.iSourceID) + " - " + rs.getString(SMTableordersources.sSourceDesc) + "</FONT></B></TD></TR>");
		    		}
		    		iCurrentSource = rs.getInt(SMTableordersources.iSourceID);
		    		sCurrentSource = rs.getString(SMTableordersources.sSourceDesc);
		    		iCurrentCallCount = 0;
		    	}
		    	iCurrentCallCount++;
		    	if (bShowCallDetail){
		    		out.println("<TR>");
		    		//out.println("<TD ALIGN=CENTER>" + rs.getInt(SMTablecustomercalllog.id) + "</TD>");
		    		out.println("<TD ALIGN=CENTER VALIGN=TOP>" + USDateformatter.format(rs.getTimestamp(SMTablecustomercalllog.datCallTime)) + "</TD>");
		    		out.println("<TD ALIGN=LEFT VALIGN=TOP>" + clsStringFunctions.FormatSQLResult(rs.getString(SMTablecustomercalllog.sCustomerName)) + "</TD>");
		    		if (rs.getString(SMTablecustomercalllog.sPhoneNumber) == null || rs.getString(SMTablecustomercalllog.sPhoneNumber).trim().compareTo("") == 0){
		    			out.println("<TD ALIGN=LEFT VALIGN=TOP>&nbsp;</TD>");
		    		}else{
		    			out.println("<TD ALIGN=LEFT VALIGN=TOP>" + SMUtilities.Format_PhoneNumber(rs.getString(SMTablecustomercalllog.sPhoneNumber)) + "</TD>");
		    		}
		    		if (rs.getString(SMTablecustomercalllog.sCity) == null || rs.getString(SMTablecustomercalllog.sCity).trim().compareTo("") == 0){
		    			out.println("<TD ALIGN=LEFT VALIGN=TOP>&nbsp;</TD>");
		    		}else{
		    			out.println("<TD ALIGN=LEFT VALIGN=TOP>" + clsStringFunctions.FormatSQLResult(rs.getString(SMTablecustomercalllog.sCity)) + "</TD>");	    			
		    		}
		    		if (rs.getString(SMTablecustomercalllog.mNote) == null || rs.getString(SMTablecustomercalllog.mNote).trim().compareTo("") == 0){
		    			out.println("<TD ALIGN=LEFT VALIGN=TOP>&nbsp;</TD>");
		    		}else{
		    			out.println("<TD ALIGN=LEFT VALIGN=TOP>" + clsStringFunctions.FormatSQLResult(rs.getString(SMTablecustomercalllog.mNote)) + "</TD>");
		    		}
		    	}
		    }
		    rs.close();
		    //printout statistic info for last source group
		    if (iCurrentSource != 0 && iOrderSource == 0){
    			//output total count and percentage
		    	if (bShowCallDetail){
		    		out.println("<TR><TD ALIGN=RIGHT VALIGN=TOP COLSPAN=5><FONT SIZE=3><B>Number of calls from " + sCurrentSource + ": " + iCurrentCallCount + " (" + SMUtilities.RoundHalfUp(iCurrentCallCount * 100.0 / iRowCount, 2) + "%)</FONT></B></TD></TR>");
		    	}else{
    				out.println("<TR><TD ALIGN=LEFT VALIGN=TOP COLSPAN=5><FONT SIZE=3><B>Number of calls from \"" + sCurrentSource + "\": " + iCurrentCallCount + " (" + SMUtilities.RoundHalfUp(iCurrentCallCount * 100.0 / iRowCount, 2) + "%)</FONT></B></TD></TR>");
    			}
    		}
		    out.println("<TR><TD ALIGN=RIGHT VALIGN=TOP COLSPAN=5><HR></TD></TR>");
		    out.println("<TR><TD ALIGN=RIGHT VALIGN=TOP COLSPAN=5><FONT SIZE=5><B>Total number of calls: " + iRowCount + " (100.00%)</FONT></B></TD></TR>");
		    out.println("</TABLE>");
	    }catch (SQLException ex){
	    	System.out.println("Error in CustomerCallLogListGenerate class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
	    }
	    
		out.println("</BODY></HTML>");
	}
	
	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
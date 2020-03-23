package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMLogEntry;
import SMDataDefinition.SMTablemechanics;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class SMJobCostDailyReportGenerate extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");
	
	private boolean bSelectByCategory = false;
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMJobCostDailyReport))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+  (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    String sWarning = "";
	    if (clsManageRequestParameters.get_Request_Parameter("SelectByCategory", request).compareTo("yes") == 0){
	    	bSelectByCategory = true;
	    }else{
	    	bSelectByCategory = false;
	    }
	    
	    String sStartingDate = "";
	    String sEndingDate = "";
	    
		sStartingDate = request.getParameter("StartingDate");
		sEndingDate = request.getParameter("EndingDate");

		try {
			sStartingDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sStartingDate),"yyyy-MM-dd");
		} catch (ParseException e1) {
			sWarning = "Invalid starting date - Error:[1423580936] - '" + sStartingDate + "' - " + e1.getMessage();
			return;
		}
		try {
			sEndingDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sEndingDate),"yyyy-MM-dd");
		} catch (ParseException e1) {
			sWarning = "Invalid ending date- Error:[1423580937] - '" + sEndingDate + "'- " + e1.getMessage();
			redirectAfterError(response, sCallingClass, sWarning, sDBID);			
            return;
		}

		ArrayList<String> sMechanics = new ArrayList<String>(0);
		Enumeration<String> paramNames = request.getParameterNames();
		ArrayList<String> sLocations = new ArrayList<String>(0);
		ArrayList<String> sServiceType = new ArrayList<String>(0);

		if (bSelectByCategory){
	    	//Get the list of selected mechanics:
		    String sLocationMarker = "LocationCheckBox";
		    String sServiceMarker = "ServiceTypeCheckbox";
		    while(paramNames.hasMoreElements()) {
		      String sParamName = paramNames.nextElement();
			  if (sParamName.contains(sLocationMarker)){
				  sLocations.add(
						  sParamName.substring(sParamName.indexOf(sLocationMarker) 
								  + sLocationMarker.length()));
			  }else if (sParamName.contains(sServiceMarker)){
				  sServiceType.add(
						  sParamName.substring(sParamName.indexOf(sServiceMarker) 
								  + sServiceMarker.length()));
			  }
		    }
			if(sLocations.size() == 0){
				sWarning = "You must select a technician location";
				redirectAfterError(response, sCallingClass, sWarning, sDBID);
				return;
	        }
			
	        String sLocationsString = "";
	        for (int i = 0; i < sLocations.size(); i++){
	        	sLocationsString += sLocations.get(i) + ",";
	        }
			String SQL = "SELECT"
				+ " " + SMTablemechanics.sMechInitial
				+ " FROM " + SMTablemechanics.TableName
				+ " WHERE ("
					+ "(INSTR('" + sLocationsString + "', " + SMTablemechanics.sMechLocation + ") > 0))"
				;
			
			System.out.println("In " + this.toString() + ".doGet, SQL = " + SQL);
			
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(
						SQL, 
						getServletContext(), 
						sDBID, 
						"MySQL", 
						SMUtilities.getFullClassName(this.toString()) + ".doGet, userID: " + sUserID
						+ " - "
						+ sUserFullName
						);
				
				while (rs.next()){
					sMechanics.add(rs.getString(SMTablemechanics.sMechInitial));
				}
				rs.close();
			} catch (SQLException e) {
				sWarning = "Could not read list of technicians: " + e.getMessage();
				redirectAfterError(response, sCallingClass, sWarning, sDBID);
				return;
			}
		}else{
	    	//Get the list of selected mechanics:
		    String sMarker = "MechCheckbox";
		    while(paramNames.hasMoreElements()) {
		      String sParamName = paramNames.nextElement();
		      //System.out.println("sParamname = " + sParamName);
			  if (sParamName.contains(sMarker)){
				  //System.out.println("sSalespersons.add: " + sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
				  sMechanics.add(sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
			  }
		    }
		}
	    Collections.sort(sMechanics);
		
	    if (sMechanics.size() == 0){
	    	if (bSelectByCategory){
	    		sWarning = "You selected a combination of locations and service types that includes no"
	    			+ " technicians.";
	    	}else{
	    		sWarning = "You must select at least one technician.";
	    	}
    		
    		redirectAfterError(response, sCallingClass, sWarning, sDBID);
    		return;
    	}
	    
	    boolean bSuppressDetails = false;
	    if (request.getParameter("SuppressDetails") != null){
	    	bSuppressDetails = true;
	    }
	    
    	//Customized title
    	String sReportTitle = "Job Cost Daily Report";

    	String sCriteria = "Starting with date <B>" + sStartingDate + "</B>"
    		+ ", ending with date <B>" + sEndingDate + "</B>";
    	
    	if (bSelectByCategory){
    		sCriteria += ", selected technician locations:&nbsp;";
    		for (int i = 0; i < sLocations.size(); i++){
    			if (i == 0){
    				sCriteria += "<B>" 
    					+ sLocations.get(i) + "</B>";
    			}else{
    				sCriteria += ", <B>" 
    					+ sLocations.get(i) + "</B>";
    			}
    		}
    		sCriteria += ", selected service types:&nbsp;";
    		for (int i = 0; i < sServiceType.size(); i++){
    			if (i == 0){
    				sCriteria += "<B>" 
    					+ sServiceType.get(i) + "</B>";
    			}else{
    				sCriteria += ", <B>" 
    					+ sServiceType.get(i) + "</B>";
    			}
    		}
    	}
    	
    	sCriteria += ", including technicians: ";
    	
    	for (int i = 0; i < sMechanics.size(); i++){
    		if (i == 0){
    			sCriteria += "<B>" + sMechanics.get(i) + "</B>";
    		}else{
    			sCriteria += ", <B>" + sMechanics.get(i) + "</B>";
    		}
    	}
   		sCriteria = sCriteria + ".";
   		String sColor = SMUtilities.getInitBackGroundColor(getServletContext(), sDBID);
   		out.println(SMUtilities.getMasterStyleSheetLink());
    	
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"#FFFFFF\">" +
		   "<TABLE BORDER=0 WIDTH=100% BGCOLOR = \"" + sColor +"\">" +
		   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
		   + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) 
		   + " Printed by " + SMUtilities.getFullNamebyUserID(sUserID, getServletContext(), sDBID, "SMJobCostDailyReportGenerate")
		   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>" +
		   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>" +
		   
		   "<TR><TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD></TR>");
				   
	   out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>" +
		   "</TD></TR></TABLE>");
	   
	    //log usage of this report
	    SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
	    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMJOBCOSTDAILYREPORT, "REPORT", "SMJobCostDailyReport", "[1376509324]");
    	
    	//Retrieve information
    	Connection conn = clsDatabaseFunctions.getConnection(
    		getServletContext(), 
    		sDBID, 
    		"MySQL", 
    		this.toString() 
    		+ " - userID: " 
    		+ sUserID
    		+ " - "
    		+ sUserFullName
    	);
    	if (conn == null){
    		sWarning = "Unable to get data connection.";
    		redirectAfterError(response, sCallingClass, sWarning, sDBID);
    		return;
    	}

    	SMJobCostDailyReport jcdr = new SMJobCostDailyReport();
    	if (!jcdr.processReport(
    			conn, 
    			sStartingDate, 
    			sEndingDate, 
    			sMechanics, 
    			sServiceType,
    			sDBID,
    			sUserID,
    			bSuppressDetails,
    			out,
    			getServletContext(),
    			(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
    		out.println("Could not print report - " + jcdr.getErrorMessage());
    	}
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080579]");
	    out.println("</BODY></HTML>");
	}
	private void redirectAfterError(HttpServletResponse res, String sCallingClass, String sWarning, String sDBID){
		
		String sRedirect =
			"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
			+ "Warning=" + sWarning
			;
		
		if (bSelectByCategory){
			sRedirect += "&SelectByCategory=true";
		}
		
		sRedirect += "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID;
		try {
			res.sendRedirect(sRedirect);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
        	return;
	}
}

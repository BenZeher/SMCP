package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.math.BigDecimal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMLogEntry;
import SMDataDefinition.*;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMBidFollowUpGenerate extends HttpServlet {
	
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
				SMSystemFunctions.SMBidFollowUpReport))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
    	//Calculate time period
    	SimpleDateFormat USDateOnlyformatter = new SimpleDateFormat("MM/dd/yyyy");
    	NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);

    	//get current URL
    	String sCurrentURL;
    	sCurrentURL = clsServletUtilities.URLEncode(request.getRequestURI().toString() + "?" + request.getQueryString());
    	
    	String title = "";
    	String subtitle = "";
    	String sSQL = "";
    	ResultSet rs = null;

	    /*************GET the PARAMETERs***************/
	    title = SMBidEntry.ParamObjectName + " Follow-Up List";
	    subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

	    out.println(
	    		"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMBidFollowUpCriteriaSelection"
	    		+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\"><B>New Search</B></A><BR><BR>");

    	//Display all selected criteria
    	ArrayList<String> alCriteria = new ArrayList<String>(0);
    	
	    String sSalespersonCode = request.getParameter("SelectedSalesperson");
	    if (sSalespersonCode.compareTo("ALLSP") == 0){
	    	alCriteria.add("<FONT SIZE=2><B>Salesperson:</B>&nbsp;All</FONT>");
	    }else{
	    	alCriteria.add("<FONT SIZE=2><B>Salesperson:</B>&nbsp;" + sSalespersonCode + "</FONT>");
	    }
	    int iProjectType = Integer.parseInt(request.getParameter("ProjectType"));
	    if (iProjectType == 0){
	    	alCriteria.add("<FONT SIZE=2><B>Project Type:</B>&nbsp;ALL</FONT>");
	    }else{
		    sSQL = SMMySQLs.Get_Project_Type_List_SQL();
		    try{
			    ResultSet rsProjectTypes = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
			    while (rsProjectTypes.next()){
			    	if (rsProjectTypes.getInt(SMTableprojecttypes.iTypeId) == iProjectType){
			    		alCriteria.add("<FONT SIZE=2><B>Project Type:</B>&nbsp;" + rsProjectTypes.getString(SMTableprojecttypes.sTypeDesc) + "</FONT>");
			    		break;
			    	}
			    }
			    rsProjectTypes.close();
		    }catch(SQLException ex){
		    	System.out.println("Error when getting project type information!!");
		        System.out.println("SQLException: " + ex.getMessage());
		        System.out.println("SQLState: " + ex.getSQLState());
		        System.out.println("SQL: " + ex.getErrorCode());
		    }
	    }
    	int iCheckLastContactDate = 0;
    	if (request.getParameter("CheckLastContactDate") != null){
    		iCheckLastContactDate = 1;
    	}
    	
    	String sLastContactStartDate = clsManageRequestParameters.get_Request_Parameter("LastContactStartDate", request);
    	if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sLastContactStartDate)){
    		out.println("Invalid last contact starting date - '" + sLastContactStartDate);
    		return;
    	}
    	String sLastContactEndDate = clsManageRequestParameters.get_Request_Parameter("LastContactEndDate", request);
    	if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sLastContactEndDate)){
    		out.println("Invalid last contact ending date - '" + sLastContactEndDate);
    		return;
    	}
    	
    	Timestamp datLastContactStartDate = clsDateAndTimeConversions.StringToTimestamp("M/d/yyyy", sLastContactStartDate);
    	Timestamp datLastContactEndDate = clsDateAndTimeConversions.StringToTimestamp(
    			"M/d/yyyy H:m:s", sLastContactEndDate + " 23:59:59");

	    if (iCheckLastContactDate == 1){
    		alCriteria.add("<FONT SIZE=2><B>Last Contact Date Range:</B>&nbsp;" + USDateOnlyformatter.format(new Date(datLastContactStartDate.getTime())) +
    																	  " - " + USDateOnlyformatter.format(new Date(datLastContactEndDate.getTime())) +
    					   "</FONT>");
	    }
    	int iCheckNextContactDate = 0;
    	if (request.getParameter("CheckNextContactDate") != null){
    		iCheckNextContactDate = 1;
    	}

    	String sNextContactStartDate = clsManageRequestParameters.get_Request_Parameter("NextContactStartDate", request);
    	if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sNextContactStartDate)){
    		out.println("Invalid next contact starting date - '" + sNextContactStartDate);
    		return;
    	}
    	String sNextContactEndDate = clsManageRequestParameters.get_Request_Parameter("NextContactEndDate", request);
    	if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sNextContactEndDate)){
    		out.println("Invalid next contact ending date - '" + sNextContactEndDate);
    		return;
    	}
    	
    	Timestamp datNextContactStartDate = clsDateAndTimeConversions.StringToTimestamp("M/d/yyyy", sNextContactStartDate);
    	Timestamp datNextContactEndDate = clsDateAndTimeConversions.StringToTimestamp(
    			"M/d/yyyy H:m:s", sNextContactEndDate + " 23:59:59");
	    if (iCheckNextContactDate == 1){
    		alCriteria.add("<FONT SIZE=2><B>Next Contact Date Range:</B>&nbsp;" + USDateOnlyformatter.format(new Date(datNextContactStartDate.getTime())) +
    																	  " - " + USDateOnlyformatter.format(new Date(datNextContactEndDate.getTime())) +
    					   "</FONT>");
	    }
	    String sSortBy1 = request.getParameter("SelectedSortOrder1");
	    //System.out.println("Order 1 = " + sSortBy1);
	    String sSortBy2 = request.getParameter("SelectedSortOrder2");
	    //System.out.println("Order 2 = " + sSortBy2);
	    alCriteria.add("<FONT SIZE=2><B>Sort Order:</B>&nbsp;" + sSortBy1 + "&nbsp;then&nbsp;" + sSortBy2 + "</FONT>");

	    //Status check
	    int iStatusPending = 0;
	    int iStatusSuccessful = 0;
	    int iStatusUnsuccessful = 0;
	    int iStatusInactive = 0;
	    
    	if (request.getParameter("StatusPending") != null){
    		iStatusPending = 1;
    		alCriteria.add("<FONT SIZE=2><B>Show Pending:</B>&nbsp;Yes</FONT>");
    	}else{
    		alCriteria.add("<FONT SIZE=2><B>Show Pending:</B>&nbsp;No</FONT>");
    	}
    	if (request.getParameter("StatusSuccessful") != null){
    		iStatusSuccessful = 1;
    		alCriteria.add("<FONT SIZE=2><B>Show Successful:</B>&nbsp;Yes</FONT>");
    	}else{
    		alCriteria.add("<FONT SIZE=2><B>Show Successful:</B>&nbsp;No</FONT>");
    	}
    	if (request.getParameter("StatusUnsuccessful") != null){
    		iStatusUnsuccessful = 1;
    		alCriteria.add("<FONT SIZE=2><B>Show Unsuccessful:</B>&nbsp;Yes</FONT>");
    	}else{
    		alCriteria.add("<FONT SIZE=2><B>Show Unsuccessful:</B>&nbsp;No</FONT>");
    	}
    	if (request.getParameter("StatusInactive") != null){
    		iStatusInactive = 1;
    		alCriteria.add("<FONT SIZE=2><B>Show Inactive:</B>&nbsp;Yes</FONT>");
    	}else{
    		alCriteria.add("<FONT SIZE=2><B>Show Inactive:</B>&nbsp;No</FONT>");
    	}
    	
	    //log usage of this report
	    SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
	    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_BIDFOLLOWUPREPORT, "REPORT", "SMBidFollowUp", "[1376509308]");

    	out.println(SMUtilities.Build_HTML_Table(3, 
			    								 alCriteria,
			    								 100,
			    								 0,
			    								 false,
			    								 false)
			    	);
	    sSQL = SMMySQLs.Get_Bid_Follow_Up_List_SQL(sSalespersonCode,
	    										   iProjectType,
	    										   iCheckLastContactDate,
	    										   datLastContactStartDate,
	    										   datLastContactEndDate,
	    										   iCheckNextContactDate,
	    										   datNextContactStartDate,
	    										   datNextContactEndDate,
	    										   sSortBy1,
	    										   sSortBy2,
												   iStatusPending,
												   iStatusSuccessful,
												   iStatusUnsuccessful,
												   iStatusInactive);
	    try{
	    	rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	    }catch (SQLException ex){
	    	System.out.println("Error when opening rs for sales lead followup list!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
	    }
	    /*************END of PARAMETER list***************/
	    
	    int iBidCount = 0;
	    BigDecimal iBidTotal = BigDecimal.ZERO;
	    try{
	    	if (rs != null){
			    
			    //print out column headers
			    out.println("<TABLE BORDER=1 WIDTH=1250>");
			    
			    out.println("<TR>");
			        //10 columns
			    	out.println("<TD ALIGN=CENTER VALIGN=TOP WIDTH=4%><B>ID</B></TD>");
			    	out.println("<TD ALIGN=CENTER VALIGN=TOP WIDTH=3%><B>SP</B></TD>");
			    	out.println("<TD ALIGN=CENTER VALIGN=TOP WIDTH=8%><B>Origination Date</B></TD>");
			    	out.println("<TD ALIGN=CENTER VALIGN=TOP WIDTH=5%><B>Follow Up Note</B></TD>");
			    	out.println("<TD ALIGN=CENTER VALIGN=TOP WIDTH=15%><B>Bill-to Name</B></TD>");
			    	out.println("<TD ALIGN=CENTER VALIGN=TOP WIDTH=25%><B>Ship-to Name</B></TD>");
			    	out.println("<TD ALIGN=CENTER VALIGN=TOP WIDTH=7%><B>Project Type</B></TD>");
			    	out.println("<TD ALIGN=CENTER VALIGN=TOP WIDTH=13%><B>Contact Name</B></TD>");
			    	out.println("<TD ALIGN=CENTER VALIGN=TOP WIDTH=10%><B>Phone Number</B></TD>");
			    	out.println("<TD ALIGN=CENTER VALIGN=TOP WIDTH=10%><B>Proposed Amount</B></TD>");
			    out.println("</TR>");
				
			    while (rs.next()){
				    out.println("<TR>");
				    iBidCount++;
				    iBidTotal = iBidTotal.add(rs.getBigDecimal(SMTablebids.dapproximateamount));
				    //id
				    out.println("<TD ALIGN=CENTER VALIGN=TOP><FONT SIZE=2>"
					    	+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditBidEntry"
					    	+ "?" + SMBidEntry.ParamID + "=" + rs.getInt(SMTablebids.lid) 
					    	+ "&OriginalURL=" + sCurrentURL 
					    	+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					    	+ "\">" 
					    	+ rs.getInt(SMTablebids.TableName + "." + SMTablebids.lid) + "</A></FONT></TD>");

				    //salesperson code
				    out.println("<TD ALIGN=CENTER VALIGN=TOP><FONT SIZE=2>" + rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode) + "</FONT></TD>");
				    //origination date
				    out.println("<TD ALIGN=CENTER VALIGN=TOP><FONT SIZE=2>" + USDateOnlyformatter.format(rs.getTimestamp(SMTablebids.dattimeoriginationdate)) + "</FONT></TD>");
				    //followup note
				    if (rs.getString(SMTablebids.mfollowupnotes).trim().length() > 0){
				    	String sImagePath = getServletContext().getInitParameter("imagepath");
				    	if (sImagePath == null){
				    		sImagePath = "../images/smcontrolpanel/";
				    	}
				    	out.println("<TD ALIGN=CENTER VALIGN=TOP><IMG src=\"" 
				    			+ sImagePath 
				    			+ "note.gif\" title=\"" 
				    			+ clsStringFunctions.FormatSQLResult(rs.getString(SMTablebids.mfollowupnotes)) + "\"></TD>");
				    }else{
				    	out.println("<TD ALIGN=CENTER VALIGN=TOP>&nbsp;</TD>");
				    }
				    //customer name
				    out.println("<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" + rs.getString(SMTablebids.scustomername) + "&nbsp;</FONT></TD>");
				    //project name
				    out.println("<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" + rs.getString(SMTablebids.sprojectname) + "</FONT></TD>");
				    //project type
				    out.println("<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" + rs.getString(SMTableprojecttypes.sTypeCode) + "</FONT></TD>");
				    //contact name
				    out.println("<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" + rs.getString(SMTablebids.scontactname) + "</FONT></TD>");
				    //phone number
				    out.println("<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" + SMUtilities.addPhoneNumberLink(rs.getString(SMTablebids.sphonenumber)) + "</FONT></TD>");
				    //proposed amount
				    out.println("<TD ALIGN=RIGHT VALIGN=TOP><FONT SIZE=2>" + rs.getBigDecimal(SMTablebids.dapproximateamount).setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "</FONT></TD>");
				    out.println("</TR>");
			    }
			    out.println("<TR><TD COLSPAN=10><HR></TD></TR>");
			    out.println("<TR><TD ALIGN=RIGHT COLSPAN=10><FONT SIZE=4><B>Total Follow-Up Calls: " + iBidCount + "</B></FONT></TD></TR>");
			    out.println("<TR><TD ALIGN =RIGHT COLSPAN=10><FONT SIZE=4><B> Total Proposed Amount : "+currency.format(iBidTotal)+"</B></FONT></TD></TR>");
			    out.println("</TABLE>");
			    rs.close();
	    	}else{
		    	//no report type is passed in, out blank.
		    	out.println("<BR>Error retrieving " + SMBidEntry.ParamObjectName + " information.");
		    }
		    
	    }catch (SQLException ex){
	    	System.out.println("Error in SMBidFollowUpGenerate!!");
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
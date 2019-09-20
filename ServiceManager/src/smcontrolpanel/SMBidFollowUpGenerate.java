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

import SMClasses.MySQLs;
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
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, "#FFFFFF", sCompanyName));
	    String sColor = SMUtilities.getInitBackGroundColor(getServletContext(), sDBID);

	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

	    out.println(
	    		"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMBidFollowUpCriteriaSelection"
	    		+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\"><B>New Search</A><BR><BR>");

    	//Display all selected criteria
    	ArrayList<String> alCriteria = new ArrayList<String>(0);
    	
	    String sSalespersonCode = request.getParameter("SelectedSalesperson");
	    if (sSalespersonCode.compareTo("ALLSP") == 0){
	    	alCriteria.add("<FONT SIZE=2><B>Salesperson:&nbsp;</B>All</FONT>");
	    }else{
	    	alCriteria.add("<FONT SIZE=2><B>Salesperson:&nbsp;</B>" + sSalespersonCode + "</FONT>");
	    }
	    int iProjectType = Integer.parseInt(request.getParameter("ProjectType"));
	    if (iProjectType == 0){
	    	alCriteria.add("<FONT SIZE=2><B>Project Type:&nbsp;</B>ALL</FONT>");
	    }else{
		    sSQL = MySQLs.Get_Project_Type_List_SQL();
		    try{
			    ResultSet rsProjectTypes = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
			    while (rsProjectTypes.next()){
			    	if (rsProjectTypes.getInt(SMTableprojecttypes.iTypeId) == iProjectType){
			    		alCriteria.add("<FONT SIZE=2><B>Project Type:&nbsp;</B>" + rsProjectTypes.getString(SMTableprojecttypes.sTypeDesc) + "</FONT>");
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
    		alCriteria.add("<FONT SIZE=2><B>Last Contact Date Range:&nbsp;" + USDateOnlyformatter.format(new Date(datLastContactStartDate.getTime())) +
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
    		alCriteria.add("<FONT SIZE=2><B>Next Contact Date Range:&nbsp;</B>" + USDateOnlyformatter.format(new Date(datNextContactStartDate.getTime())) +
    																	  " - " + USDateOnlyformatter.format(new Date(datNextContactEndDate.getTime())) +
    					   "</FONT>");
	    }
	    String sSortBy1 = request.getParameter("SelectedSortOrder1");
	    //System.out.println("Order 1 = " + sSortBy1);
	    String sSortBy2 = request.getParameter("SelectedSortOrder2");
	    //System.out.println("Order 2 = " + sSortBy2);
	    alCriteria.add("<FONT SIZE=2><B>Sort Order:&nbsp;</B>" + sSortBy1 + "&nbsp;then&nbsp;" + sSortBy2 + "</FONT>");

	    //Status check
	    int iStatusPending = 0;
	    int iStatusSuccessful = 0;
	    int iStatusUnsuccessful = 0;
	    int iStatusInactive = 0;
	    
    	if (request.getParameter("StatusPending") != null){
    		iStatusPending = 1;
    		alCriteria.add("<FONT SIZE=2><B>Show Pending:&nbsp;</B>Yes</FONT>");
    	}else{
    		alCriteria.add("<FONT SIZE=2><B>Show Pending:&nbsp;No</FONT>");
    	}
    	if (request.getParameter("StatusSuccessful") != null){
    		iStatusSuccessful = 1;
    		alCriteria.add("<FONT SIZE=2><B>Show Successful:&nbsp;</B>Yes</FONT>");
    	}else{
    		alCriteria.add("<FONT SIZE=2><B>Show Successful:&nbsp;</B>No</FONT>");
    	}
    	if (request.getParameter("StatusUnsuccessful") != null){
    		iStatusUnsuccessful = 1;
    		alCriteria.add("<FONT SIZE=2><B>Show Unsuccessful:&nbsp;</B>Yes</FONT>");
    	}else{
    		alCriteria.add("<FONT SIZE=2><B>Show Unsuccessful:&nbsp;</B>No</FONT>");
    	}
    	if (request.getParameter("StatusInactive") != null){
    		iStatusInactive = 1;
    		alCriteria.add("<FONT SIZE=2><B>Show Inactive:&nbsp;</B>Yes</FONT>");
    	}else{
    		alCriteria.add("<FONT SIZE=2><B>Show Inactive:&nbsp;</B>No</FONT>");
    	}
    	out.println("</TABLE>");
	    //log usage of this report
	    SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
	    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMBIDFOLLOWUPREPORT, "REPORT", "SMBidFollowUp", "[1376509308]");

    	out.println(SMUtilities.Build_HTML_Table(3, 
			    								 alCriteria,
			    								 100,
			    								 0,
			    								 false,
			    								 false,
			    								 sColor)
			    	);
    	out.println(SMUtilities.getMasterStyleSheetLink());
   
		sSQL = "SELECT * FROM " + SMTablebids.TableName + ", " + SMTablesalesperson.TableName + ", " + SMTableprojecttypes.TableName + 
				  " WHERE" + 
				  	" " + SMTablebids.TableName + "." + SMTablebids.ssalespersoncode + " =" + 
				  	" " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode + 
				  	" AND" + 
				  	" " + SMTablebids.TableName + "." + SMTablebids.iprojecttype + " =" +
				  	" " + SMTableprojecttypes.TableName + "." + SMTableprojecttypes.iTypeId;
			if (sSalespersonCode.compareTo("ALLSP") != 0){
				  sSQL = sSQL + " AND " + SMTablebids.TableName + "." + SMTablebids.ssalespersoncode + " = '" + sSalespersonCode + "'";
			}
			if (iProjectType > 0){
				  sSQL = sSQL + " AND " + SMTablebids.TableName + "." + SMTablebids.iprojecttype + " = " + iProjectType;
			}
				  
			if (iCheckLastContactDate == 1){
				  sSQL = sSQL + " AND " + SMTablebids.TableName + "." + SMTablebids.datlastcontactdate + " >= '" + datLastContactStartDate.toString() + "'" + 
				  			  " AND " + SMTablebids.TableName + "." + SMTablebids.datlastcontactdate + " <= '" + datLastContactEndDate.toString() + "'";
			}
			if (iCheckNextContactDate == 1){
				  sSQL = sSQL + " AND " + SMTablebids.TableName + "." + SMTablebids.datnextcontactdate + " >= '" + datNextContactStartDate.toString() + "'" + 
				  			  " AND " + SMTablebids.TableName + "." + SMTablebids.datnextcontactdate + " <= '" + datNextContactEndDate.toString() + "'";
			}
			
			if (iStatusPending == 0){
				sSQL = sSQL + " AND " + SMTablebids.TableName + "." + SMTablebids.sstatus + " <> '" + SMTablebids.STATUS_PENDING + "'";
			}
			if (iStatusSuccessful == 0){
				sSQL = sSQL + " AND " + SMTablebids.TableName + "." + SMTablebids.sstatus + " <> '" + SMTablebids.STATUS_SUCCESSFUL + "'";
			}
			if (iStatusUnsuccessful == 0){
				sSQL = sSQL + " AND " + SMTablebids.TableName + "." + SMTablebids.sstatus + " <> '" + SMTablebids.STATUS_UNSUCCESSFUL + "'";
			}
			if (iStatusInactive == 0){
				sSQL = sSQL + " AND " + SMTablebids.TableName + "." + SMTablebids.sstatus + " <> '" + SMTablebids.STATUS_INACTIVE + "'";
			}
			
			//default to sort by bidding date.
			sSQL = sSQL + " ORDER BY ";
			if (sSortBy1.compareTo("Salesperson") == 0 ){
				sSQL = sSQL + SMTablebids.TableName + "." + SMTablebids.ssalespersoncode;
			}else if (sSortBy1.compareTo("Origination Date") == 0 ){
				sSQL = sSQL + SMTablebids.TableName + "." + SMTablebids.dattimeoriginationdate;
			}else if (sSortBy1.compareTo("Customer Name") == 0 ){
				sSQL = sSQL + SMTablebids.TableName + "." + SMTablebids.scustomername;
			}else{
				sSQL = sSQL + SMTablebids.TableName + "." + SMTablebids.ssalespersoncode;
			}
			sSQL = sSQL + ", ";
			if (sSortBy2.compareTo("Last Contact Date") == 0 ){
				sSQL = sSQL + SMTablebids.TableName + "." + SMTablebids.datlastcontactdate;
			}else if (sSortBy2.compareTo("Next Contact Date") == 0 ){
				sSQL = sSQL + SMTablebids.TableName + "." + SMTablebids.datnextcontactdate;
			}else{
				sSQL = sSQL + SMTablebids.TableName + "." + SMTablebids.dattimebiddate;
			}
			
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
	    		out.println("<TABLE WIDTH=100% CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">");
	    		out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
	    		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">ID </TD>");
	    		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> SP</TD>");
	    		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> Origination Date</TD>");
	    		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> Follow Up Note</TD>");
	    		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> Bill-to Name</TD>");
	    		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> Ship-to Name</TD>");
	    		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> Project Type</TD>");
	    		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> Contact Name</TD>");
	    		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> Phone Number</TD>");
	    		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"> Proposed Amount</TD>");
	    		out.println("</TR>");
			    //print out column headers
			    while (rs.next()){
			    	if(iBidCount % 2 == 0) {
			    		out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
			    	}else {
			    		out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
			    	}
				    iBidCount++;
				    iBidTotal = iBidTotal.add(rs.getBigDecimal(SMTablebids.dapproximateamount));
				    //id
				    out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"
					    	+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditBidEntry"
					    	+ "?" + SMBidEntry.ParamID + "=" + rs.getInt(SMTablebids.lid) 
					    	+ "&OriginalURL=" + sCurrentURL 
					    	+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					    	+ "\">" 
					    	+ rs.getInt(SMTablebids.TableName + "." + SMTablebids.lid) + "</A></TD>");

				    //salesperson code
				    out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode) + "</TD>");
				    //origination date
				    out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + USDateOnlyformatter.format(rs.getTimestamp(SMTablebids.dattimeoriginationdate)) + "</TD>");
				    //followup note
				    if (rs.getString(SMTablebids.mfollowupnotes).trim().length() > 0){
				    	String sImagePath = getServletContext().getInitParameter("imagepath");
				    	if (sImagePath == null){
				    		sImagePath = "../images/smcontrolpanel/";
				    	}
				    	out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><IMG src=\"" 
				    			+ sImagePath 
				    			+ "note.gif\" title=\"" 
				    			+ clsStringFunctions.FormatSQLResult(rs.getString(SMTablebids.mfollowupnotes)) + "\"></TD>");
				    }else{
				    	out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">&nbsp;</TD>");
				    }
				    //customer name
				    out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + rs.getString(SMTablebids.scustomername) + "&nbsp;</TD>");
				    //project name
				    out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"  + rs.getString(SMTablebids.sprojectname) + "</TD>");
				    //project type
				    out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"  + rs.getString(SMTableprojecttypes.sTypeCode) + "</TD>");
				    //contact name
				    out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"  + rs.getString(SMTablebids.scontactname) + "</TD>");
				    //phone number
				    out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + SMUtilities.addPhoneNumberLink(rs.getString(SMTablebids.sphonenumber)) + "</TD>");
				    //proposed amount
				    out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"  + rs.getBigDecimal(SMTablebids.dapproximateamount).setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "</TD>");
				    out.println("</TR>");
			    }
			    out.println("<TR><TD COLSPAN=10 CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK + "\">&nbsp;</TD></TR>");
			    out.println("<TD COLSPAN=10 CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Total Follow-Up Calls: " + iBidCount + "</TD></TR>");
			    out.println("<TD COLSPAN=10 CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Total Proposed Amount : "+currency.format(iBidTotal)+"</TD></TR>");
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
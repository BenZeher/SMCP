package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
import java.util.Map.Entry;

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
import ServletUtilities.clsStringFunctions;

public class SMBidReportGenerate extends HttpServlet {
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
				SMSystemFunctions.SMSalesLeadReport))
			{
				return;
			}

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);

		//Calculate time period
		SimpleDateFormat USDateTimeformatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
		NumberFormat decimal = new DecimalFormat("##,###.00");

		//get current URL
		String sCurrentURL;
		sCurrentURL = clsServletUtilities.URLEncode(request.getRequestURI().toString() 
				+ "?" + request.getQueryString());

		String title = "";
		String subtitle = "";
		String sSQL = "";
		ResultSet rs = null;

		/*************GET the PARAMETERs***************/
		title = SMBidEntry.ParamObjectName + " Report";
		subtitle = "";
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

		//Display all selected criteria
		ArrayList<String> alCriteria = new ArrayList<String>(0);

		//Selected sales group
		ArrayList<String> alSelectedSalesGroups = new ArrayList<String>(0);
		Enumeration<String> e = request.getParameterNames();
		String sParam = "";
		String sGroup = "";
		//String sDisplay = "";
		while (e.hasMoreElements()){
			sParam = (String) e.nextElement();
			if (clsStringFunctions.StringLeft(sParam, "SALESGROUP".length()).compareToIgnoreCase("SALESGROUP") == 0){
				//select sales group
				if (request.getParameter(sParam) != null){
					sGroup = clsStringFunctions.StringRight(sParam, sParam.length() - "SALESGROUP".length());
					alSelectedSalesGroups.add(sGroup);
				}
			}
		}
		
		String s = "<TABLE BORDER=0><TR>" +
		"<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2><B>Sales Group(s):</B></FONT></TD>" +
		"<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" +
		getSelectedSalesGroups(alSelectedSalesGroups, sDBID) +
		"</FONT>" +
		"</TD>" +
		"</TR></TABLE>";
		alCriteria.add(s);

		/* LTO 20091102
		 * Drop down box is replaced with check boxes now. 
		 */
		/*
    	//selected salesperson
	    String sSalespersonCode = request.getParameter("SelectedSalesperson");
	    if (sSalespersonCode.compareTo("ALLSP") == 0){
	    	alCriteria.add("<FONT SIZE=2><B>Salesperson:</B>&nbsp;All</FONT>");
	    }else{
	    	alCriteria.add("<FONT SIZE=2><B>Salesperson:</B>&nbsp;" + sSalespersonCode + "</FONT>");
	    }
		 */


		//Get the list of selected salespersons:
		ArrayList<String> alSalespersons = new ArrayList<String>(0);
		Enumeration<String> paramNames = request.getParameterNames();
		String sMarker = "SALESPERSON";
		while(paramNames.hasMoreElements()) {
			String sParamName = paramNames.nextElement();
			//System.out.println("sParamname = " + sParamName);
			if (sParamName.contains(sMarker)){
				//System.out.println("sSalespersons.add: " + sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
				alSalespersons.add(sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
			}
		}
		Collections.sort(alSalespersons);

		int iProjectType = Integer.parseInt(request.getParameter("ProjectType"));
		if (iProjectType == 0){
			alCriteria.add("<FONT SIZE=2><B>Project Type:</B>&nbsp;ALL</FONT>");
		}else{
			sSQL = MySQLs.Get_Project_Type_List_SQL();
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

	    String sStartingDate = "";
	    String sEndingDate = "";
    	if(request.getParameter("DateRange").compareToIgnoreCase("CurrentMonth") == 0){
    		Calendar calendar = Calendar.getInstance();
    		Calendar calFirstDay = Calendar.getInstance();
    		calFirstDay.setTimeInMillis(SMUtilities.FindFirstDayOfMonth(calendar.getTimeInMillis()));
    		sStartingDate = clsDateAndTimeConversions.CalendarToString(calFirstDay, "yyyy-MM-dd");
    		Calendar calLastDay = Calendar.getInstance();
    		calLastDay.setTimeInMillis(SMUtilities.FindLastDayOfMonth(calendar.getTimeInMillis()));
    		sEndingDate = clsDateAndTimeConversions.CalendarToString(calLastDay, "yyyy-MM-dd");
    	}else{
    		if(request.getParameter("DateRange").compareToIgnoreCase("PreviousMonth") == 0){
        		Calendar calendar = Calendar.getInstance();
        		//Set it back a month:
        		calendar.add(Calendar.MONTH, -1);
        		Calendar calFirstDay = Calendar.getInstance();
        		calFirstDay.setTimeInMillis(SMUtilities.FindFirstDayOfMonth(calendar.getTimeInMillis()));
        		sStartingDate = clsDateAndTimeConversions.CalendarToString(calFirstDay, "yyyy-MM-dd");
        		Calendar calLastDay = Calendar.getInstance();
        		calLastDay.setTimeInMillis(SMUtilities.FindLastDayOfMonth(calendar.getTimeInMillis()));
        		sEndingDate = clsDateAndTimeConversions.CalendarToString(calLastDay, "yyyy-MM-dd");
    		}else{
    			//User entered dates:
    			sStartingDate = request.getParameter("StartingDate");
//    			if(!SMUtilities.IsValidDateString("M/d/yyyy", sStartingDate)){
//    				
//    			}
    			sEndingDate = request.getParameter("EndingDate");
//    			if(!SMUtilities.IsValidDateString("M/d/yyyy", sEndingDate)){
//    				
//    	        }
    			try {
					sStartingDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sStartingDate),"yyyy-MM-dd");
				} catch (ParseException e1) {
					
					out.println("Invalid starting date: '" + sStartingDate + "'- " + e1.getMessage());
   	            	return;
				}
    			try {
					sEndingDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sEndingDate),"yyyy-MM-dd");
				} catch (ParseException e1) {
					
					out.println("Invalid ending date: '" + sEndingDate + "'- " + e1.getMessage());
   	            	return;
				}
    		}
    	}
		alCriteria.add("<FONT SIZE=2><B>Actual Bid Date Range:</B>&nbsp;" + sStartingDate +
				" - " + sEndingDate + "</FONT>");

		//Status check
		int iStatusPending = 0;
		int iStatusSuccessful = 0;
		int iStatusUnsuccessful = 0;
		int iStatusInactive = 0;
		int iShowSalespersonTotalsOnly = 0;

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

		if (request.getParameter("OnlyShowSubtotals") != null){
			iShowSalespersonTotalsOnly = 1;
			alCriteria.add("<FONT SIZE=2><B>Show subtotals only:</B>&nbsp;Yes</FONT>");
		}else{
			alCriteria.add("<FONT SIZE=2><B>Show subtotals only:</B>&nbsp;No</FONT>");
		}

		out.println(SMUtilities.Build_HTML_Table(4, 
				alCriteria,
				100,
				0,
				false,
				false)
		);
		out.print("<FONT SIZE=2><B>Selected salesperson: ");
		for (int i=0;i<alSalespersons.size()-1;i++){
			out.print(alSalespersons.get(i).toString() + ", ");
		}
		try {
			out.println(alSalespersons.get(alSalespersons.size() - 1) + "<BR>");
		} catch (Exception e1) {
			out.println("&nbsp;" + "<BR>");
		}

		//log usage of this report
		SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
		log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMBIDREPORT, "REPORT", "SMBidReport", "[1376509309]");

		sSQL = Get_Bid_Report_SQL(alSelectedSalesGroups,
				alSalespersons,
				iProjectType,
				sStartingDate,
				sEndingDate,
				iStatusPending,
				iStatusSuccessful,
				iStatusUnsuccessful,
				iStatusInactive);
		try{
			rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
		}catch (SQLException ex){
			System.out.println("Error when opening rs for sales lead report!!");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("SQL: " + ex.getErrorCode());
		}
		/*************END of PARAMETER list***************/

		int iProjectTypeBidCount = 0;
		int iSalespersonBidCount = 0;
		int iGrandTotalBidCount = 0;
		//Group Total and Count 
		TreeMap<String, BigDecimal> hEachGroupTotal = new TreeMap<>();
		TreeMap<String,Integer> hEachGroupCount = new TreeMap<>();
		
		//Project Total and Count
		Map<String, BigDecimal> hEachProjectTotal = new TreeMap<>();
		Map<String, Integer> hEachProjectCount = new TreeMap<>();
		
		//Each SalesPerson Total and Count
		Map<String,BigDecimal> hEachSalePersonTotal = new TreeMap<>();
		Map<String,Integer> hEachSalePersonCount = new TreeMap<>();

		String sCurrentSalesperson = "NOSALESPERSON";
		String sCurrentSalespersonFullName = "";
		String sCurrentSalesGroup = "NONE";//The Current Sale group 
		String sNextSalesGroup = "";//The next sale group
		int iCurrentProjectType = -1;
		String sCurrentProjectTypeCode = "";
		String sCurrentProjectTypeDesc = "";

		BigDecimal bdProjectTypeTotal = BigDecimal.ZERO;
		BigDecimal bdSalespersonTotal = BigDecimal.ZERO;
		BigDecimal bdGrandTotal = BigDecimal.ZERO;

		try{
			if (rs != null){

				//print out column headers
				out.println("<TABLE style = \" border-collapse: collapse \" BORDER=0 WIDTH=1250 cellspacing=0 cellpadding=1>");

				while (rs.next()){

					if (sCurrentSalesperson.compareTo("NOSALESPERSON") == 0 &&
							iCurrentProjectType == -1 && 
							sCurrentSalesGroup.compareTo("NONE") == 0){
						//this is the beginning of the report.
						//print out headers
						//new salesperson, print out name
						out.println("<TR><TD ALIGN=LEFT COLSPAN=7><FONT SIZE=3><B>" + rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode) + "&nbsp;&nbsp;-&nbsp;&nbsp;" + 
								rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonLastName) + ", " + 
								rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonFirstName) + "</B></FONT></TD></TR>");
						sCurrentSalesperson = rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode);
						sCurrentSalespersonFullName = rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonFirstName) + " "+ rs.getString(SMTablesalesperson.sSalespersonLastName);
						
						//Checks to see if the next sale group is a null
						sNextSalesGroup = rs.getString(SMTablesalesgroups.TableName +"."+SMTablesalesgroups.sSalesGroupCode);
						if(sNextSalesGroup == null){
							sNextSalesGroup = "(BLANK)";//changes the null to blank
						}
						sCurrentSalesGroup = sNextSalesGroup;
						bdSalespersonTotal = BigDecimal.ZERO;
						iSalespersonBidCount = 0;

						if (iShowSalespersonTotalsOnly == 0){
							out.println("<TR style = \"background-color: #C2E0FF\">");
							//10 columns
							out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=5%><FONT SIZE=2><B>GRP/SP</B></FONT></TD>");
							out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=5%><FONT SIZE=2><B>ID</B></FONT></TD>");
							out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=15%><FONT SIZE=2><B>Actual Bid Date</B></FONT></TD>");
							out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=25%><FONT SIZE=2><B>Bill-to Name</B></FONT></TD>");
							out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=25%><FONT SIZE=2><B>Ship-to Name</B></FONT></TD>");
							out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=10%><FONT SIZE=2><B>Project Type</B></FONT></TD>");
							out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=15%><FONT SIZE=2><B>Proposed Amount</B></FONT></TD>");
							out.println("</TR>");
						}
						//new project type
						out.println("<TR><TD>&nbsp;</TD><TD ALIGN=LEFT COLSPAN=6><FONT SIZE=2><B>" + rs.getString(SMTableprojecttypes.TableName + "." + SMTableprojecttypes.sTypeCode) + "&nbsp;-&nbsp;" + 
								rs.getString(SMTableprojecttypes.TableName + "." + SMTableprojecttypes.sTypeDesc) + "</B></FONT></TD></TR>");
						iCurrentProjectType = rs.getInt(SMTablebids.TableName + "." + SMTablebids.iprojecttype); 
						sCurrentProjectTypeCode = rs.getString(SMTableprojecttypes.TableName + "." + SMTableprojecttypes.sTypeCode);
						sCurrentProjectTypeDesc = rs.getString(SMTableprojecttypes.sTypeDesc); 
						bdProjectTypeTotal = BigDecimal.ZERO;
						iProjectTypeBidCount = 0;
					}else{
						//Gets the next sales group
						sNextSalesGroup = rs.getString(SMTablesalesgroups.TableName +"."+SMTablesalesgroups.sSalesGroupCode);
						if(sNextSalesGroup == null){
							sNextSalesGroup = "(BLANK)";
						}
						if(sNextSalesGroup.compareTo(sCurrentSalesGroup) != 0){
							//finish last sales group
							//finish last project type
							out.println("<TR style = \"background-color: #FFCCFF;\">" +
									"<TD style=\"border: 1px solid black; border-spacing: 5em;\" ALIGN=RIGHT COLSPAN=5><FONT SIZE=2><B> " + sCurrentProjectTypeCode +" - "+sCurrentProjectTypeDesc+ " " + SMBidEntry.ParamObjectName.toLowerCase() + " count: " + iProjectTypeBidCount + "</B></FONT></TD>" +
									"<TD style=\"border: 1px solid black\" ALIGN=RIGHT COLSPAN=2><FONT SIZE=2><B> " + sCurrentProjectTypeCode +"  proposed total: " +  decimal.format(bdProjectTypeTotal) + "</B></FONT></TD>" +
							"</TR><BR>");
							
							out.println("<TR style = \"background-color: #FFCCFF\">" +
									"<TD style=\"border: 1px solid black \" ALIGN=RIGHT COLSPAN=5><FONT SIZE=3><B> GRP: "+sCurrentSalesGroup+", SP: " + sCurrentSalesperson + " - "+sCurrentSalespersonFullName+"  " + SMBidEntry.ParamObjectName.toLowerCase() + " count: " + iSalespersonBidCount + "</B></FONT></TD>" +
									"<TD style=\"border: 1px solid black\" ALIGN=RIGHT COLSPAN=2><FONT SIZE=3><B> SP: " + sCurrentSalesperson + " proposed total: " +  decimal.format(bdSalespersonTotal) + "</B></FONT></TD>" +
							"</TR>");
							out.println("<TR><TD COLSPAN=7><BR></TD></TR>");

							//get the next sales group
							sCurrentSalesGroup = sNextSalesGroup;
							//set the total for the next group
							bdSalespersonTotal = BigDecimal.ZERO;
							//set the count for the next group
							iSalespersonBidCount = 0;
							
							if (rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode).compareTo(sCurrentSalesperson) != 0){
								out.println("<TR><TD ALIGN=LEFT COLSPAN=7><FONT SIZE=3><B>" + rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode) + "&nbsp;&nbsp;-&nbsp;&nbsp;" + 
										rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonLastName) + ", " + 
										rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonFirstName) + "</B></FONT></TD></TR>");
								sCurrentSalesperson = rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode);
								sCurrentSalespersonFullName = rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonFirstName) + " "+ rs.getString(SMTablesalesperson.sSalespersonLastName);
								
							}
							
							if(iCurrentProjectType != -1 &&
									rs.getInt(SMTablebids.TableName + "." + SMTablebids.iprojecttype) != iCurrentProjectType){

								//finish last project type
								iCurrentProjectType = rs.getInt(SMTablebids.TableName + "." + SMTablebids.iprojecttype); 
								sCurrentProjectTypeCode = rs.getString(SMTableprojecttypes.TableName + "." + SMTableprojecttypes.sTypeCode);
								sCurrentProjectTypeDesc = rs.getString(SMTableprojecttypes.TableName + "."+ SMTableprojecttypes.sTypeDesc);
								bdProjectTypeTotal = BigDecimal.ZERO;
								iProjectTypeBidCount = 0;
							}
							

							out.println("<TR style = \"background-color: #C2E0FF\">");
							//10 columns
							if (iShowSalespersonTotalsOnly == 0){
								out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=5%><FONT SIZE=2><B>Grp/SP</B></FONT></TD>");
								out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid;\" VALIGN=TOP WIDTH=5%><FONT SIZE=2><B>ID</B></FONT></TD>");
								out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=15%><FONT SIZE=2><B>Actual Bid Date</B></FONT></TD>");
								out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=25%><FONT SIZE=2><B>Bill-to Name</B></FONT></TD>");
								out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=25%><FONT SIZE=2><B>Ship-to Name</B></FONT></TD>");
								out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=10%><FONT SIZE=2><B>Project Type</B></FONT></TD>");
								out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=15%><FONT SIZE=2><B>Proposed Amount</B></FONT></TD>");
								out.println("</TR>");
							}

						} else if (rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode).compareTo(sCurrentSalesperson) != 0){
							//finish last salesperson
							//finish last project type
							out.println("<TR style = \"background-color: #FFCCFF;\">" +
									"<TD style=\"border: 1px solid black; border-spacing: 5em;\" ALIGN=RIGHT COLSPAN=5><FONT SIZE=2><B> " + sCurrentProjectTypeCode + " - "+sCurrentProjectTypeDesc+" " + SMBidEntry.ParamObjectName.toLowerCase() + " count: " + iProjectTypeBidCount + "</B></FONT></TD>" +
									"<TD style=\"border: 1px solid black; border-spacing: 5em;\" ALIGN=RIGHT COLSPAN=2><FONT SIZE=2><B> " + sCurrentProjectTypeCode + " proposed total: " +  decimal.format(bdProjectTypeTotal) + "</B></FONT></TD>" +
							"</TR>");
							

							out.println("<TR style = \"background-color: #FFCCFF;\">" +
									"<TD style=\"border: 1px solid black; border-spacing: 5em;\" ALIGN=RIGHT COLSPAN=5><FONT SIZE=3><B> GRP: "+ sCurrentSalesGroup+", SP: " + sCurrentSalesperson + " - "+sCurrentSalespersonFullName+"  " + SMBidEntry.ParamObjectName.toLowerCase() + " count: " + iSalespersonBidCount + "</B></FONT></TD>" +
									"<TD style=\"border: 1px solid black; border-spacing: 5em;\" ALIGN=RIGHT COLSPAN=2><FONT SIZE=3><B> SP: " + sCurrentSalesperson + " proposed total: " +  decimal.format(bdSalespersonTotal) + "</B></FONT></TD>" +
							"</TR>");
							out.println("<TR><TD COLSPAN=7><BR></TD></TR>");

							//new salesperson, print out name
							out.println("<TR><TD ALIGN=LEFT COLSPAN=7><FONT SIZE=3><B>" + rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode) + "&nbsp;&nbsp;-&nbsp;&nbsp;" + 
									rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonLastName) + ", " + 
									rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonFirstName) + "</B></FONT></TD></TR>");
							sCurrentSalesperson = rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode);
							sCurrentSalespersonFullName = rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonFirstName) + " "+ rs.getString(SMTablesalesperson.sSalespersonLastName);
							bdSalespersonTotal = BigDecimal.ZERO;
							iSalespersonBidCount = 0;

							out.println("<TR style = \"background-color: #C2E0FF\">");
							//10 columns
							if (iShowSalespersonTotalsOnly == 0){
								out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=5%><FONT SIZE=2><B>Grp/SP</B></FONT></TD>");
								out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=5%><FONT SIZE=2><B>ID</B></FONT></TD>");
								out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=15%><FONT SIZE=2><B>Actual Bid Date</B></FONT></TD>");
								out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=25%><FONT SIZE=2><B>Bill-to Name</B></FONT></TD>");
								out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=25%><FONT SIZE=2><B>Ship-to Name</B></FONT></TD>");
								out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=10%><FONT SIZE=2><B>Project Type</B></FONT></TD>");
								out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=15%><FONT SIZE=2><B>Proposed Amount</B></FONT></TD>");
								out.println("</TR>");
							}
							//new project type
							out.println("<TR><TD>&nbsp;</TD><TD ALIGN=LEFT COLSPAN=6><FONT SIZE=2><B>" + rs.getString(SMTableprojecttypes.TableName + "." + SMTableprojecttypes.sTypeCode) + "&nbsp;-&nbsp;" + 
									rs.getString(SMTableprojecttypes.TableName + "." + SMTableprojecttypes.sTypeDesc) + "</B></FONT></TD></TR>");
							iCurrentProjectType = rs.getInt(SMTablebids.TableName + "." + SMTablebids.iprojecttype); 
							sCurrentProjectTypeCode = rs.getString(SMTableprojecttypes.TableName + "." + SMTableprojecttypes.sTypeCode);
							sCurrentProjectTypeDesc = rs.getString(SMTableprojecttypes.TableName + "." + SMTableprojecttypes.sTypeDesc);
							bdProjectTypeTotal = BigDecimal.ZERO;
							iProjectTypeBidCount = 0;

						}else if(iCurrentProjectType != -1 &&
								rs.getInt(SMTablebids.TableName + "." + SMTablebids.iprojecttype) != iCurrentProjectType){

							//finish last project type
							out.println("<TR style = \"background-color: #FFCCFF;\">" +
									"<TD style=\"border: 1px solid black; border-spacing: 5em;\" ALIGN=RIGHT COLSPAN=5><FONT SIZE=2><B> " + sCurrentProjectTypeCode + " - "+sCurrentProjectTypeDesc+" " + SMBidEntry.ParamObjectName.toLowerCase() + " count: " + iProjectTypeBidCount + "</B></FONT></TD>" +
									"<TD style=\"border: 1px solid black; border-spacing: 5em;\" ALIGN=RIGHT COLSPAN=2><FONT SIZE=2><B> " + sCurrentProjectTypeCode + " proposed total: " +  decimal.format(bdProjectTypeTotal) + "</B></FONT></TD>" +
							"</TR>");
							out.println("<TR><TD COLSPAN=7><BR></TD></TR>");
							//new project type
							out.println("<TR><TD>&nbsp;</TD><TD ALIGN=LEFT COLSPAN=6><FONT SIZE=2><B>" + rs.getString(SMTableprojecttypes.TableName + "." + SMTableprojecttypes.sTypeCode) + "&nbsp;-&nbsp;" + 
									rs.getString(SMTableprojecttypes.TableName + "." + SMTableprojecttypes.sTypeDesc) + "</B></FONT></TD></TR>");
							iCurrentProjectType = rs.getInt(SMTablebids.TableName + "." + SMTablebids.iprojecttype); 
							sCurrentProjectTypeCode = rs.getString(SMTableprojecttypes.TableName + "." + SMTableprojecttypes.sTypeCode);
							sCurrentProjectTypeDesc = rs.getString(SMTableprojecttypes.TableName + "." + SMTableprojecttypes.sTypeDesc);
							bdProjectTypeTotal = BigDecimal.ZERO;
							iProjectTypeBidCount = 0;
						}
					}

					iProjectTypeBidCount++;
					iSalespersonBidCount++;
					iGrandTotalBidCount++;
					bdProjectTypeTotal = bdProjectTypeTotal.add(rs.getBigDecimal(SMTablebids.dapproximateamount));
					bdSalespersonTotal = bdSalespersonTotal.add(rs.getBigDecimal(SMTablebids.dapproximateamount));
					bdGrandTotal = bdGrandTotal.add(rs.getBigDecimal(SMTablebids.dapproximateamount));
					
					
					//Group proposal total
					if(hEachGroupTotal.containsKey(sCurrentSalesGroup)){
						hEachGroupTotal.put(sCurrentSalesGroup, hEachGroupTotal.get(sCurrentSalesGroup).add(rs.getBigDecimal(SMTablebids.dapproximateamount)));
					}else{
						hEachGroupTotal.put(sCurrentSalesGroup, rs.getBigDecimal(SMTablebids.dapproximateamount));
					}
					
					//Group count
					if(hEachGroupCount.containsKey(sCurrentSalesGroup)){
						hEachGroupCount.put(sCurrentSalesGroup, hEachGroupCount.get(sCurrentSalesGroup) + 1);
					}else{
						hEachGroupCount.put(sCurrentSalesGroup, 1);
					}
					
					//Project proposal total
					if(hEachProjectTotal.containsKey(sCurrentProjectTypeDesc)){
						hEachProjectTotal.put(sCurrentProjectTypeDesc, hEachProjectTotal.get(sCurrentProjectTypeDesc).add(rs.getBigDecimal(SMTablebids.dapproximateamount)));
					}else{
						hEachProjectTotal.put(sCurrentProjectTypeDesc, rs.getBigDecimal(SMTablebids.dapproximateamount));
					}
					
					// Project proposal count
					if(hEachProjectCount.containsKey(sCurrentProjectTypeDesc)){
						hEachProjectCount.put(sCurrentProjectTypeDesc, hEachProjectCount.get(sCurrentProjectTypeDesc) + 1);
					}else{
						hEachProjectCount.put(sCurrentProjectTypeDesc,1);
					}
					
					// Sales Person Total
					if(hEachSalePersonTotal.containsKey(sCurrentSalespersonFullName)){
						hEachSalePersonTotal.put(sCurrentSalespersonFullName, hEachSalePersonTotal.get(sCurrentSalespersonFullName).add(rs.getBigDecimal(SMTablebids.dapproximateamount)));
					}else{
						hEachSalePersonTotal.put(sCurrentSalespersonFullName, rs.getBigDecimal(SMTablebids.dapproximateamount));
					}
					
					//Sales Person Count
					if(hEachSalePersonCount.containsKey(sCurrentSalespersonFullName)){
						hEachSalePersonCount.put(sCurrentSalespersonFullName, hEachSalePersonCount.get(sCurrentSalespersonFullName) + 1);
					}else{
						hEachSalePersonCount.put(sCurrentSalespersonFullName, 1);
					}
					
					
					//id

					if (iShowSalespersonTotalsOnly == 0){
						out.println("<TR>");
						//salesperson and salesgroup code
						String group = "";
						group = rs.getString(SMTablesalesgroups.TableName +"."+SMTablesalesgroups.sSalesGroupCode);
						if(group == null){
							group = "(BLANK)";
						}
						out.println("<TD ALIGN=CENTER VALIGN=TOP><FONT SIZE=2>"+group+ "/"+ rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode) + "</FONT></TD>");
						out.println("<TD ALIGN=CENTER VALIGN=TOP><FONT SIZE=2>"
								+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditBidEntry"
								+ "?" + SMBidEntry.ParamID + "=" + rs.getInt(SMTablebids.lid) 
								+ "&OriginalURL=" + sCurrentURL 
								+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
								+ "\">" 
								+ rs.getInt(SMTablebids.TableName + "." + SMTablebids.lid) + "</A></FONT></TD>");
						//actual date
						if (rs.getString(SMTablebids.TableName + "." + SMTablebids.dattimeactualbiddate).compareTo("0000-00-00 00:00:00") == 0){
							out.println("<TD ALIGN=CENTER VALIGN=TOP><FONT SIZE=2>N/A</FONT></TD>");
						}else{
							out.println("<TD ALIGN=CENTER VALIGN=TOP><FONT SIZE=2>" + USDateTimeformatter.format(rs.getTimestamp(SMTablebids.TableName + "." + SMTablebids.dattimeactualbiddate)) + "</FONT></TD>");
						}
						//customer name
						out.println("<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" + rs.getString(SMTablebids.scustomername) + "&nbsp;</FONT></TD>");
						//project name
						out.println("<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" + rs.getString(SMTablebids.sprojectname) + "</FONT></TD>");
						//project type
						out.println("<TD ALIGN=CENTER VALIGN=TOP><FONT SIZE=2>" + rs.getString(SMTableprojecttypes.sTypeCode) + "</FONT></TD>");
						//proposed amount
						out.println("<TD ALIGN=RIGHT VALIGN=TOP><FONT SIZE=2>" + decimal.format(rs.getBigDecimal(SMTablebids.dapproximateamount)) + "</FONT></TD>");

						out.println("</TR>");
					}
				}

				//finish last salesperson
				//finish last project type
				out.println("<TR style = \"background-color: #FFCCFF;\">" +
						"<TD style=\"border: 1px solid black; border-spacing: 5em;\" ALIGN=RIGHT COLSPAN=5><FONT SIZE=2><B> " + sCurrentProjectTypeCode + " - "+sCurrentProjectTypeDesc+" " + SMBidEntry.ParamObjectName.toLowerCase() + " count: " + iProjectTypeBidCount + "</B></FONT></TD>" +
						"<TD style=\"border: 1px solid black; border-spacing: 5em;\" ALIGN=RIGHT COLSPAN=2><FONT SIZE=2><B> " + sCurrentProjectTypeCode + " proposed total: " +  decimal.format(bdProjectTypeTotal) + "</B></FONT></TD>" +
				"</TR>");
				
				out.println("<TR style = \"background-color: #FFCCFF;\">" +
						"<TD style=\"border: 1px solid black; border-spacing: 5em;\" ALIGN=RIGHT COLSPAN=5><FONT SIZE=3><B> SG:  " +sCurrentSalesGroup+ ", SP:  "+ sCurrentSalesperson+" - "+sCurrentSalespersonFullName+" " + SMBidEntry.ParamObjectName.toLowerCase() + " count: " + iSalespersonBidCount + "</B></FONT></TD>" +
						"<TD style=\"border: 1px solid black; border-spacing: 5em;\" ALIGN=RIGHT COLSPAN=2><FONT SIZE=3><B> SP:" + sCurrentSalesperson + " proposed total: " +  decimal.format(bdSalespersonTotal) + "</B></FONT></TD>" +
				"</TR>");
				
				out.println("<TR><TD COLSPAN = 7><BR></TD></TR>");

				out.println("<TR><TD COLSPAN=10><HR></TD></TR>");
				
				
				//SUMMARY TABLE FOR SALES PERSON
				out.println("<TR><TD><U><B>SUMMARY</B></U></TD></TR>\n");
				out.println("<TR style =  \" background-color: #CCFF99\" >\n");
				out.println(   "<TD width = 15% class = \" rightjustifiedcell \" style = \" font-weight:bold; color:black; \" >SALESPERSON</TD>");
				out.println(   "<TD width = 15% class = \" rightjustifiedcell \" style = \" font-weight:bold; color:black; \" >NO. OF LEADS</TD>");
				out.println(   "<TD width = 15% class = \" rightjustifiedcell \" style = \" font-weight:bold; color:black; \" >TOTAL PROPOSED AMOUNT</TD>");
				int iTotalsaleslead = 0;
				BigDecimal iTotalproposedamount = BigDecimal.ZERO;
				//sorts the sales person alphabetically
				
				for(Entry<String, Integer> entry : hEachSalePersonCount.entrySet()){
					iTotalproposedamount.add(hEachSalePersonTotal.get(entry.getKey()));
					out.println("<TR ><TD>"+entry.getKey()+"</TD>\n");
					out.println("<TD align = \" right \">"+entry.getValue()+"</TD>\n");
					out.println("<TD align = \" right \">"+decimal.format(hEachSalePersonTotal.get(entry.getKey()))+"</TD></TR>");
					iTotalsaleslead += entry.getValue();
					iTotalproposedamount = iTotalproposedamount.add(hEachSalePersonTotal.get(entry.getKey()));
				}
				out.println("<TR style = \"background-color: #FFCCFF; font-weight:bold; color: black;\">\n");
				out.println("<TD style =  > TOTAL </TD>");
				out.println("<TD align = \" right \">"+iTotalsaleslead+"</TD>\n");
				out.println("<TD align = \" right \">"+decimal.format(iTotalproposedamount)+"</TD>\n");
				out.println("</TR>\n");
				out.println("<TR COLSPAN = 3><TD><BR></TD></TR>");
				
				
				
				//SUMMARY TABLE FOR PROJECT TYPE
				out.println("<TR style =  \" background-color: #CCFF99\" >\n");
				out.println(   "<TD width = 15% class = \" rightjustifiedcell \" style = \" font-weight:bold; color:black; \" >Project Type</TD>");
				out.println(   "<TD width = 15% class = \" rightjustifiedcell \" style = \" font-weight:bold; color:black; \" >NO. OF LEADS</TD>");
				out.println(   "<TD width = 15% class = \" rightjustifiedcell \" style = \" font-weight:bold; color:black; \" >TOTAL PROPOSED AMOUNT</TD>");
				iTotalsaleslead = 0;
				iTotalproposedamount = BigDecimal.ZERO;
				for(Entry<String, Integer> entry : hEachProjectCount.entrySet()){
					out.println("<TR ><TD>"+entry.getKey()+"</TD>\n");
					out.println("<TD align = \" right \">"+entry.getValue()+"</TD>\n");
					out.println("<TD align = \" right \">"+decimal.format(hEachProjectTotal.get(entry.getKey()))+"</TD></TR>");
					iTotalsaleslead += entry.getValue();
					iTotalproposedamount = iTotalproposedamount.add(hEachProjectTotal.get(entry.getKey()));
				}
				out.println("<TR style = \"background-color: #FFCCFF; font-weight:bold; color: black;\">\n");
				out.println("<TD style =  > TOTAL </TD>");
				out.println("<TD align = \" right \">"+iTotalsaleslead+"</TD>\n");
				out.println("<TD align = \" right \">"+decimal.format(iTotalproposedamount)+"</TD>\n");
				out.println("</TR>\n");
				out.println("<TR COLSPAN = 3><TD><BR></TD></TR>");
				
				//SUMMARY TABLE FOR GROUP
				out.println("<TR style =  \" background-color: #CCFF99\" >\n");
				out.println(   "<TD width = 15% class = \" rightjustifiedcell \" style = \" font-weight:bold; color:black; \" >GROUP</TD>");
				out.println(   "<TD width = 15% class = \" rightjustifiedcell \" style = \" font-weight:bold; color:black; \" >NO. OF LEADS</TD>");
				out.println(   "<TD width = 15% class = \" rightjustifiedcell \" style = \" font-weight:bold; color:black; \" >TOTAL PROPOSED AMOUNT</TD>");
				iTotalsaleslead = 0;
				iTotalproposedamount = BigDecimal.ZERO;
				for(Entry<String, Integer> entry : hEachGroupCount.entrySet()){
					iTotalproposedamount.add(hEachGroupTotal.get(entry.getKey()));
					out.println("<TR ><TD>"+entry.getKey()+"</TD>\n");
					out.println("<TD align = \" right \">"+entry.getValue()+"</TD>\n");
					out.println("<TD align = \" right \">"+decimal.format(hEachGroupTotal.get(entry.getKey()))+"</TD></TR>");
					iTotalsaleslead += entry.getValue();
					iTotalproposedamount = iTotalproposedamount.add(hEachGroupTotal.get(entry.getKey()));
				}
				out.println("<TR style = \"background-color: #FFCCFF; font-weight:bold; color: black;\">\n");
				out.println("<TD style =  > TOTAL </TD>");
				out.println("<TD align = \" right \">"+iTotalsaleslead+"</TD>\n");
				out.println("<TD align = \" right \">"+decimal.format(iTotalproposedamount)+"</TD>\n");
				out.println("</TR>\n");
				
				
				

				out.println("<TR>" +
						"<TD ALIGN=RIGHT COLSPAN=5><FONT SIZE=4><B>Total " + SMBidEntry.ParamObjectName.toLowerCase() + " count</B></FONT></TD>" +
						"<TD ALIGN=RIGHT COLSPAN=2><FONT SIZE=4><B>" + iGrandTotalBidCount + "</B></FONT></TD>" +
				"</TR>");
				out.println("<TR>" +
						"<TD ALIGN=RIGHT COLSPAN=5><FONT SIZE=4><B>Total Proposed Amount</B></FONT></TD>" +
						"<TD ALIGN=RIGHT COLSPAN=2><FONT SIZE=4><B>" + decimal.format(bdGrandTotal) + "</B></FONT></TD>" +
				"</TR>");

				out.println("</TABLE>");
				rs.close();
			}else{
				//no report type is passed in, out blank.
				out.println("<BR>Error retrieving " + SMBidEntry.ParamObjectName.toLowerCase() + " information.");
			}

		}catch (SQLException ex){
			System.out.println("Error in SMBidReportGenerate!!");
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
	

	private String Get_Bid_Report_SQL(ArrayList<String> arSalesGroups,
			ArrayList<String> alSalespersons,
			int iProjectType,
			String sStartingDate,
			String sEndingDate,
			int iStatusPending,
			int iStatusSuccessful,
			int iStatusUnsuccessful,
			int iStatusInactive){

		String SQL = "SELECT * FROM" + 
				" "+ SMTablebids.TableName+
				" LEFT JOIN "+ SMTablesalesperson.TableName +
				" ON "+SMTablebids.TableName+"."+SMTablebids.ssalespersoncode +
				" = "+SMTablesalesperson.TableName+"."+SMTablesalesperson.sSalespersonCode+
				" LEFT JOIN "+SMTableprojecttypes.TableName+
				" ON "+SMTablebids.TableName+"."+SMTablebids.iprojecttype+
				" = "+SMTableprojecttypes.TableName+"."+SMTableprojecttypes.iTypeId+
				" LEFT JOIN "+SMTablesalesgroups.TableName+
				" ON "+SMTablebids.TableName+"."+SMTablebids.lsalesgroupid+
				" = "+SMTablesalesgroups.TableName+"."+SMTablesalesgroups.iSalesGroupId+
				" WHERE 1 = 1 ";
				
				
		/*
			  	" AND" + 
			  	" " + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.iSalesGroupId + " =" +
			  	" " + SMTablebids.TableName + "." + SMTablesalesperson;

		if (arSalesGroups.size() > 0){
			SQL += " AND("; 
			for (int i = 0; i < arSalesGroups.size(); i++){
				SQL += " " + SMTableorderheaders.iSalesGroup + " = " + arSalesGroups.get(i) + " OR";
			}
			SQL = SQL.substring(0, SQL.length() - " OR".length()) + ")";
		}else{
			SQL += " AND " + SMTableorderheaders.iSalesGroup + " = 'NOGROUPSELECTED'";
		}

		if (sSalesperson.compareTo("ALLSP") != 0){
			SQL += " AND " + SMTablebids.TableName + "." + SMTablebids.ssalespersoncode + " = '" + sSalesperson + "'";
		}
		 */	 
		if(arSalesGroups.size() > 0){
			
			SQL = SQL + " AND (";
			for(int i = 0; i < arSalesGroups.size(); i++){
				int iarIndividualSaleGrouplength = arSalesGroups.get(i).toString().length();
				if(i > 0){
					SQL = SQL + " OR ";
				}
				SQL = SQL +  SMTablebids.TableName + "." + SMTablebids.lsalesgroupid
				+ " = " + arSalesGroups.get(i).substring(iarIndividualSaleGrouplength - 1) + "";
			}
			SQL = SQL + ")";
		}

		if (alSalespersons.size() > 0){
			SQL = SQL + " AND (";
			for (int i = 0; i < alSalespersons.size(); i++){
				if (i > 0){
					SQL = SQL + " OR ";
				}
				SQL = SQL + SMTablebids.TableName + "." + SMTablebids.ssalespersoncode 
				+ " = '" + alSalespersons.get(i) + "'";
			}
			SQL = SQL + ")";
		}
		if (iProjectType > 0){
			SQL += " AND " + SMTablebids.TableName + "." + SMTablebids.iprojecttype + " = " + iProjectType;
		}
		SQL += " AND " + SMTablebids.TableName + "." + SMTablebids.dattimeactualbiddate + " >= '" 
		+ sStartingDate + " 00:00:00'" + 
		" AND " + SMTablebids.TableName + "." + SMTablebids.dattimeactualbiddate + " <= '" 
		+ sEndingDate + " 23:59:59'";

		if (iStatusPending == 0){
			SQL += " AND " + SMTablebids.TableName + "." + SMTablebids.sstatus + " <> '" + SMTablebids.STATUS_PENDING + "'";
		}
		if (iStatusSuccessful == 0){
			SQL += " AND " + SMTablebids.TableName + "." + SMTablebids.sstatus + " <> '" + SMTablebids.STATUS_SUCCESSFUL + "'";
		}
		if (iStatusUnsuccessful == 0){
			SQL += " AND " + SMTablebids.TableName + "." + SMTablebids.sstatus + " <> '" + SMTablebids.STATUS_UNSUCCESSFUL + "'";
		}
		if (iStatusInactive == 0){
			SQL += " AND " + SMTablebids.TableName + "." + SMTablebids.sstatus + " <> '" + SMTablebids.STATUS_INACTIVE + "'";
		}

		//default to sort by salesperson.
		SQL += " ORDER BY" +
		" " + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupCode + ", " +
		" " + SMTablebids.TableName + "." + SMTablebids.ssalespersoncode + "," +
		" " + SMTablebids.TableName + "." + SMTablebids.iprojecttype + ", " +
		" " + SMTablebids.TableName + "." + SMTablebids.dattimeactualbiddate;

		//System.out.println("Get_Bid_Report_SQL:");
		return SQL;
	}

	private String getSelectedSalesGroups(ArrayList<String> arSalesGroups, String sDBID){

		String sDesc = "";

		String SQL = "SELECT * FROM " + SMTablesalesgroups.TableName 
		+ " ORDER BY " + SMTablesalesgroups.sSalesGroupCode;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID);
			int iGroup = 0;
			while(rs.next()){
				iGroup = rs.getInt(SMTablesalesgroups.iSalesGroupId);
				for (int i = 0; i < arSalesGroups.size(); i++){
					int iSaleGrouplength = arSalesGroups.get(i).toString().length();
					if(Integer.parseInt(arSalesGroups.get(i).toString().substring(iSaleGrouplength - 1)) == iGroup){
						sDesc = sDesc + rs.getString(SMTablesalesgroups.sSalesGroupDesc) + "<BR>";
					}
				}
			}
			sDesc = clsStringFunctions.StringLeft(sDesc, sDesc.length() - "<BR>".length());
			rs.close();
		}catch (SQLException e){
			sDesc = "COULD NOT READ SALESGROUPS";
		}

		return sDesc;
	}
}
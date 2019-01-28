package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMLogEntry;
import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class SMOrderSourceListingGenerate extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String sReportType = "";
	
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMOrderSourceListing))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
    	SimpleDateFormat USDateOnlyformatter = new SimpleDateFormat("MM/dd/yyyy");
	    
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
    	String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
    	String sWarning = "";
	    String sSQL;
	    
	    String sStartingDate = "";
	    String sEndingDate = "";
	    String sStartingStandardDate = "";
	    String sEndingStandardDate = "";
    	if(request.getParameter("DateRange").compareToIgnoreCase("CurrentMonth") == 0){
    		
    		Calendar calendar = Calendar.getInstance();
    		Calendar calFirstDay = Calendar.getInstance();
    		calFirstDay.setTimeInMillis(SMUtilities.FindFirstDayOfMonth(calendar.getTimeInMillis()));
    		sStartingDate = clsDateAndTimeConversions.CalendarToString(calFirstDay, "yyyy-MM-dd");
    	    sStartingStandardDate = clsDateAndTimeConversions.CalendarToString(calFirstDay, "M/d/yyyy");
    		Calendar calLastDay = Calendar.getInstance();
    		calLastDay.setTimeInMillis(SMUtilities.FindLastDayOfMonth(calendar.getTimeInMillis()));
    		sEndingDate = clsDateAndTimeConversions.CalendarToString(calLastDay, "yyyy-MM-dd");
    		sEndingStandardDate = clsDateAndTimeConversions.CalendarToString(calLastDay, "M/d/yyyy");
    	}else{
    		if(request.getParameter("DateRange").compareToIgnoreCase("PreviousMonth") == 0){
        		Calendar calendar = Calendar.getInstance();
        		//Set it back a month:
        		calendar.add(Calendar.MONTH, -1);
        		Calendar calFirstDay = Calendar.getInstance();
        		calFirstDay.setTimeInMillis(SMUtilities.FindFirstDayOfMonth(calendar.getTimeInMillis()));
        		sStartingDate = clsDateAndTimeConversions.CalendarToString(calFirstDay, "yyyy-MM-dd");
        		sStartingStandardDate = clsDateAndTimeConversions.CalendarToString(calFirstDay, "M/d/yyyy");
        		Calendar calLastDay = Calendar.getInstance();
        		calLastDay.setTimeInMillis(SMUtilities.FindLastDayOfMonth(calendar.getTimeInMillis()));
        		sEndingDate = clsDateAndTimeConversions.CalendarToString(calLastDay, "yyyy-MM-dd");
        		sEndingStandardDate = clsDateAndTimeConversions.CalendarToString(calLastDay, "M/d/yyyy");
    		}else{
    			//User entered dates:
    			sStartingDate = request.getParameter("StartingDate");
    			sStartingStandardDate = sStartingDate;
    			sEndingDate = request.getParameter("EndingDate");
    			sEndingStandardDate = sEndingDate;

    			try {
					sStartingDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sStartingDate),"yyyy-MM-dd");
				} catch (ParseException e) {
					sWarning = "Error:[1423578992] Invalid starting date '" + sStartingDate + "' - " + e.getMessage();
    	    		response.sendRedirect(
    	    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    	    				+ "Warning=" + sWarning
    	    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    	        		);			
    	            	return;
				}
    			try {
					sEndingDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sEndingDate),"yyyy-MM-dd");
				} catch (ParseException e) {
					sWarning = "Error:[1423578993] Invalid ending date '" + sEndingDate + "' - " + e.getMessage();
    	    		response.sendRedirect(
    	    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    	    				+ "Warning=" + sWarning
    	    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    	        		);			
    	            	return;
				}
    		}
    	}
    	
    	//Get the list of selected order types:
    	ArrayList<String> alServiceTypes = new ArrayList<String>(0);
	    Enumeration<String> paramNames = request.getParameterNames();
	    String sParamName = "";
	    String sMarker = "SERVICETYPE";
	    while(paramNames.hasMoreElements()) {
	      sParamName = paramNames.nextElement();
		  if (sParamName.contains(sMarker)){
			  alServiceTypes.add(sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
		  }
	    }
	    Collections.sort(alServiceTypes);
		
	    if (alServiceTypes.size() == 0){
    		sWarning = "You must select at least one order type.";
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
	    }

	    //Get the list of selected locations:
    	ArrayList<String> alLocations = new ArrayList<String>(0);
	    Enumeration<String> paramLocationNames = request.getParameterNames();
	    String sParamLocationName = "";
	    String sLocationMarker = SMOrderSourceListingCriteriaSelection.LOCATION_PARAM;
	    while(paramLocationNames.hasMoreElements()) {
	    	sParamLocationName = paramLocationNames.nextElement();
		  if (sParamLocationName.contains(sLocationMarker)){
			  alLocations.add(sParamLocationName.substring(sParamLocationName.indexOf(sLocationMarker) + sLocationMarker.length()));
		  }
	    }
	    Collections.sort(alLocations);
		
	    if (alLocations.size() == 0){
    		sWarning = "You must select at least one location.";
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
	    }
	    
    	String subtitle = "";
    	if(request.getParameter("ReportType").compareToIgnoreCase(SMBidEntry.ParamObjectName) == 0){
    		sReportType = SMBidEntry.ParamObjectName + "s";
    		subtitle = "Based on " + SMBidEntry.ParamObjectName + "s";
    	}else if(request.getParameter("ReportType").compareToIgnoreCase("Invoice") == 0){
    		sReportType = "Invoices";
    		subtitle = "Based on invoices";
    	}else{
    		sReportType = "Orders";
    		subtitle = "Based on orders";
    	}
    	
	    boolean bShowSummaryOnly;
	    String title;
	    if (request.getParameter(SMOrderSourceListingCriteriaSelection.SUMMARYONLY_PARAM) != null){
	    	bShowSummaryOnly = true;
	    	title = "Marketing Source Summary Report"; 
	    }else{
	    	bShowSummaryOnly = false;
	    	title = "Marketing Source Report"; 
	    }
	    boolean bShowPieChart;
	    if (request.getParameter(SMOrderSourceListingCriteriaSelection.PIECHART_PARAM) != null){
	    	bShowPieChart = true;
	    }else{
	    	bShowPieChart = false;
	    }
	    
	    ArrayList<String> alSources = new ArrayList<String>(0);
	    ArrayList<BigDecimal> alAmounts = new ArrayList<BigDecimal>(0);
	    
	    
	    out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">" +
			        "<HTML>" +
			        "<HEAD><TITLE>" + title + " - " + subtitle + "</TITLE></HEAD>\n<BR>" + 
				    "<BODY BGCOLOR=\"" 
			        + SMUtilities.getInitBackGroundColor(getServletContext(), sDBID) 
			        + "\">"
	    );
	    
 	    //log usage of this this report
 	    SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
 	    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMORDERSOURCELISTING, "REPORT", "SMOrderSourceListing", "[1376509333]");
	    
    	out.println("<TABLE BORDER=0 WIDTH=100%>");
	    out.println("<TR><TD ALIGN=LEFT><FONT SIZE=5><B>" 
	    	+ title 
	    	+ "</B></FONT> - <FONT SIZE=3>" 
	    	+ subtitle 
	    	+ "</FONT>&nbsp;&nbsp;-&nbsp;&nbsp;<BR><FONT SIZE=2>"
	    	+ "Printed by " + sUserName + "</FONT><BR>" 
	    	+ "<FONT SIZE=2>This report lists");
	    
	    //Locations:
	    String sLocationsString = ", for locations: <B>";
	    String SQL = "SELECT"
	    		+ " " + SMTablelocations.sLocation
	    		+ ", " + SMTablelocations.sLocationDescription
	    		+ " FROM " + SMTablelocations.TableName
	    		+ " ORDER BY " + SMTablelocations.sLocation
	    ;
	    ResultSet rs;
		try {
			rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(),
				sDBID, 
				"MySQL",
				this.toString() + ".reading locations - user: " + sUserID
				+ " - "
				+ sUserFullName
					);
			while (rs.next()){
				for (int i = 0; i < alLocations.size(); i++){
					if (rs.getString(SMTablelocations.sLocation).compareToIgnoreCase(alLocations.get(i)) == 0){
						sLocationsString += rs.getString(SMTablelocations.sLocation) + " - " 
							+ rs.getString(SMTablelocations.sLocationDescription) + ", ";
					}
				}
			}
			rs.close();
		} catch (SQLException e) {
			out.println("Error reading locations - " + e.getMessage());
		}
	    
	    try{
		    String sOrderTypes = " for order types: <B>";
		    if (
		    	(sReportType.compareTo("Orders") == 0)
		    	|| (sReportType.compareTo("Invoices") == 0)
		    ){
		    	out.println(sLocationsString + "</B>");
			    if (alServiceTypes.size() == 0){
			    	sOrderTypes += "NO ";
			    }
			    for (int i=0;i<alServiceTypes.size();i++){
			    	rs = (clsDatabaseFunctions.openResultSet("SELECT * FROM " + SMTableservicetypes.TableName 
			    			+ " WHERE " + SMTableservicetypes.sCode + " = '" + alServiceTypes.get(i) + "'", 
			    											  getServletContext(), 
			    											  sDBID));
			    	if (rs.next()){
		    			sOrderTypes += rs.getString(SMTableservicetypes.sName) + ", ";
			    	}else{
			    		sOrderTypes += "N/A, ";
			    	}
			    	rs.close();
			    }
			    out.println(sOrderTypes + "</B>");
		    }
		    
		    //Create date range string:
		    String sDateRangeString = 
		    	" from <B>" + sStartingStandardDate + "</B> up to <B>" + sEndingStandardDate + "</B>";
		    if (sReportType.compareTo("Orders") == 0){
		    	out.println("<B>ORDERS</B> with a line booked date" + sDateRangeString + ". <BR>The 'amount' is equal to the order unit price times the sum of the qty ordered plus the qty shipped.</FONT><BR>");
		    }else if (sReportType.compareTo("Invoices") == 0){
		    	out.println("<B>INVOICES</B> with an invoice date" + sDateRangeString + ". <BR>The 'amount' is equal to the invoice total amount.</FONT><BR>");
		    }else{
		    	out.println(" <B>" + SMBidEntry.ParamObjectName.toUpperCase() 
		    		+ "</B> with a " + SMBidEntry.ParamObjectName + " origination date" + sDateRangeString + ". <BR>The 'amount' is equal to the " + SMBidEntry.ParamObjectName + " approximate amount.</FONT><BR>");
		    }
		    out.println("</TD></TR>" +
						"<TR><TD><HR></TD></TR>");
		    
		    sSQL = SMMySQLs.Get_Order_Source_Listing_SQL(sStartingDate, 
		    											 sEndingDate,
		    											 sReportType,
		    											 alServiceTypes,
		    											 alLocations);
		    //out.println(sSQL);
		    rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
		    
		    boolean bHasRecord = false;

		    int iCurrentOrderSourceID = -1;
		    String sCurrentOrderSourceDesc = null;
		    String sCurrentDocNumber = null;
		    String sCurrentDocDate = "";
		    String sCurrentCustomerName = "";
		    String sCurrentCreatedBy = "";

		    //BigDecimal bdServiceTypeTotal = BigDecimal.ZERO;
		    BigDecimal bdOrderSourceTotal = BigDecimal.ZERO;
		    BigDecimal bdOrderTotal = BigDecimal.ZERO;
		    
		    boolean bFlipper = false;
		    String sbgcolor = "";
  
		    while (rs.next()){

				bFlipper = !bFlipper;
				
		    	if (bFlipper){
		    		sbgcolor = "\"#FFFFFF\"";
		    	}else{
		    		sbgcolor = "\"#EEEEEE\"";
		    	}
		    	
		    	if (iCurrentOrderSourceID == -1){
		    		//this is the start of the report
		    		sCurrentDocNumber = rs.getString("sDocNumber");
	    			sCurrentDocDate = USDateOnlyformatter.format(rs.getDate("datDocDate"));
	    			iCurrentOrderSourceID = rs.getInt("iOrderSourceID");
	    			if (iCurrentOrderSourceID != 0){
	    				sCurrentOrderSourceDesc = rs.getString("sOrderSourceDesc");
	    			}else{
	    				sCurrentOrderSourceDesc = "N/A";
	    			}
	    			sCurrentCustomerName = rs.getString("sCustomerName");
	    			sCurrentCreatedBy = rs.getString("sCreatedByFullName");
	    			
		    		bdOrderTotal = BigDecimal.ZERO;
		    		bdOrderSourceTotal = BigDecimal.ZERO;

			    	Print_Order_Source_Header(sCurrentOrderSourceDesc,
				  							  bShowSummaryOnly,
				  							  out);
		    		bHasRecord = true;
		    	}else if (rs.getInt("iOrderSourceID") != iCurrentOrderSourceID){ 

		    		if (!bShowSummaryOnly){
					    Print_Order_Info(sCurrentDocNumber,
					    				 sCurrentDocDate,
										 sCurrentCustomerName,
										 sCurrentCreatedBy,
										 bdOrderTotal,
										 out,
										 sCallingClass,
										 sDBID,
										 sbgcolor);
		    		}
	    			Print_Order_Source_Footer(sCurrentOrderSourceDesc,
				    						  bdOrderSourceTotal,
				    						  out);

	    			alSources.add(sCurrentOrderSourceDesc);
	    			alAmounts.add(bdOrderSourceTotal);
	    			sCurrentDocNumber = rs.getString("sDocNumber");
	    			sCurrentDocDate = USDateOnlyformatter.format(rs.getDate("datDocDate"));
	    			iCurrentOrderSourceID = rs.getInt("iOrderSourceID");
	    			if (iCurrentOrderSourceID != 0){
	    				sCurrentOrderSourceDesc = rs.getString("sOrderSourceDesc");
	    			}else{
	    				sCurrentOrderSourceDesc = "N/A";
	    			}
	    			sCurrentCustomerName = rs.getString("sCustomerName");
	    			sCurrentCreatedBy = rs.getString("sCreatedByFullName");
		    		bdOrderTotal = BigDecimal.ZERO;
		    		bdOrderSourceTotal = BigDecimal.ZERO;

			    	Print_Order_Source_Header(rs.getString("sOrderSourceDesc"),
				  							  bShowSummaryOnly,
				  							  out);
		    	}
		    	
		    	if (rs.getString("sDocNumber").compareTo(sCurrentDocNumber) != 0){ 
		    		if (!bShowSummaryOnly){
					    Print_Order_Info(sCurrentDocNumber,
					    				 sCurrentDocDate,
										 sCurrentCustomerName,
										 sCurrentCreatedBy,
										 bdOrderTotal,
										 out,
										 sCallingClass,
										 sDBID,
										 sbgcolor);
		    		}
	    			sCurrentDocDate = USDateOnlyformatter.format(rs.getDate("datDocDate"));
	    			sCurrentDocNumber = rs.getString("sDocNumber");
	    			sCurrentCustomerName = rs.getString("sCustomerName");
	    			sCurrentCreatedBy = rs.getString("sCreatedByFullName");
				    bdOrderTotal = BigDecimal.ZERO;
		    	}
		    	
		    	//accumulate order
		    	bdOrderTotal = bdOrderTotal.add(BigDecimal.valueOf(rs.getDouble("dAmount")));
		    	//bdServiceTypeTotal = bdServiceTypeTotal.add(BigDecimal.valueOf(rs.getDouble("Amount")));
		    	bdOrderSourceTotal = bdOrderSourceTotal.add(BigDecimal.valueOf(rs.getDouble("dAmount")));
		    }//end while
		    	
		    if (bHasRecord){
			    //print last order/order source/service type
		    	if (!bShowSummaryOnly){
				    Print_Order_Info(sCurrentDocNumber,
				    				 sCurrentDocDate,
									 sCurrentCustomerName,
									 sCurrentCreatedBy,
									 bdOrderTotal,
									 out,
									 sCallingClass,
									 sDBID,
									 sbgcolor);
		    	}
				Print_Order_Source_Footer(sCurrentOrderSourceDesc,
			    						  bdOrderSourceTotal,
			    						  out);

				alSources.add(sCurrentOrderSourceDesc);
    			alAmounts.add(bdOrderSourceTotal);
    			/*
				Print_Service_Type_Footer(sCurrentServiceTypeCodeDesc,
						   				  bdServiceTypeTotal,
						   				  alSources,
						   				  alAmounts,
						   				  bShowPieChart,
						   				  out);
				*/
		    }else{
		    	//no record returned.
		    }
		    if (bShowPieChart){
		    	//System.out.println("Print pie chart now.");
		    	out.println("<TR><TD ALIGN=LEFT COLSPAN=5>");
		    	Print_Pie_Chart(alSources,
		    					alAmounts,
		    					SMUtilities.getInitBackGroundColor(getServletContext(), sDBID).substring(1),
		    					out);
		    	out.println("</TD></TR>");
		    }
		    
		    out.println("</TABLE>");
		    rs.close();
		    
	    }catch (SQLException ex){
	    	System.out.println("Error in SMOrderSourceListingGenerate!!");
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
	
	private void Print_Order_Info(String sDocNumber,
								  String sOrderDate,
								  String sBillToName,
								  String sCreatedBy,
								  BigDecimal bdAmount,
								  PrintWriter out,
								  String sCallingClass,
								  String sDBID,
								  String sBackgroundColor
								  ){
		
		out.println("<TR BGCOLOR=" + sBackgroundColor + ">");
		if (sReportType.compareTo("Orders") == 0){
			out.println("<TD ALIGN=CENTER><FONT SIZE=2><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" + sDocNumber + "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + sDocNumber.trim() + "</A></FONT></TD>");
		}else if (sReportType.compareTo("Invoices") == 0){
			out.println("<TD ALIGN=CENTER><FONT SIZE=2><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMPrintInvoice?InvoiceNumberFrom=" + sDocNumber + "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + sDocNumber.trim() + "</A></FONT></TD>");
		}else{
			out.println("<TD ALIGN=CENTER><FONT SIZE=2><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditBidEntry" +
															"?lid=" + sDocNumber +
															"&CallingClass=" + sCallingClass +
															"&SubmitEdit=1" + 
															"&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + sDocNumber.trim() + "</A></FONT></TD>");
		}
			out.println("<TD ALIGN=CENTER><FONT SIZE=2>" + sOrderDate + "</FONT></TD>" +
						"<TD ALIGN=LEFT><FONT SIZE=2>" + sBillToName + "</FONT></TD>" +
						"<TD ALIGN=LEFT><FONT SIZE=2>" + sCreatedBy + "</FONT></TD>" +
						"<TD ALIGN=RIGHT><FONT SIZE=2>" + bdAmount.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "</FONT></TD>" + 
					"</TR>");
	}
	/*
	private void Print_Service_Type_Header(String sServiceTypeDescription,
										   PrintWriter out){
		out.println("<TR><TD ALIGN=LEFT><TABLE BORDER=2 WIDTH=100%><TR><TD><FONT SIZE=4><B>" + sServiceTypeDescription + "</B></FONT></TD></TR></TABLE><BR></TD></TR>");
	}
	
	private void Print_Service_Type_Footer(String sServiceTypeDescription,
										   BigDecimal bdServiceTypeTotal,
										   ArrayList<String> alSources,
										   ArrayList<BigDecimal> alAmounts, 
										   boolean bShowPieChart,
										   PrintWriter out){
		out.println("<TR><TD ALIGN=CENTER><TABLE BORDER=0><TR><TD>");
		if (bShowPieChart){
			Print_Pie_Chart(sServiceTypeDescription,
						    alSources,
							alAmounts,
							out);
		}else{
			out.println("&nbsp;");
		}
		
		out.println("</TD>" +
						"<TD ALIGN=RIGHT VALIGN=TOP><FONT SIZE=4><B>" + sServiceTypeDescription + " Total: </B></FONT></TD>" +
						"<TD ALIGN=RIGHT VALIGN=TOP><FONT SIZE=4>" + bdServiceTypeTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "</FONT></TD>" +
					"</TR></TABLE><BR><BR></TD></TR>");
	}
	*/
	private void Print_Order_Source_Header(String sOrderSourceDescription,
										   boolean bShowSummaryOnly,
										   PrintWriter out){

		String sIDHeader = "";
		String sDateHeader = "";
		if (sReportType.compareTo("Orders") == 0){
			sIDHeader = "Order #";
			sDateHeader = "Order Date";
		}else if (sReportType.compareTo("Invoices") == 0){
			sIDHeader = "Invoice #";
			sDateHeader = "Invoice Date";
		}else{
			sIDHeader = SMBidEntry.ParamObjectName + " #";
			sDateHeader = SMBidEntry.ParamObjectName + " Origination Date";
		}
		
		if (!bShowSummaryOnly){
			//printout order source header
			out.println("<TR><TD ALIGN=LEFT><FONT SIZE=3><B>" + sOrderSourceDescription + "</B></FONT></TD></TR>");
			out.println("<TR><TD><HR WIDTH=40% ALIGN=LEFT></TD></TR>");
			//print out the column header for details
		
			out.println("<TR><TD><TABLE BORDER=0 WIDTH=100%>" +
						"<TR>" +
							"<TD ALIGN=CENTER WIDTH=10%><FONT SIZE=2><B>" + sIDHeader + "</B></FONT></TD>" +
							"<TD ALIGN=CENTER WIDTH=15%><FONT SIZE=2><B>" + sDateHeader + "</B></FONT></TD>" +
							"<TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2><B>Customer Name</B></FONT></TD>" +
							"<TD ALIGN=CENTER WIDTH=20%><FONT SIZE=2><B>Created By</B></FONT></TD>" +
							"<TD ALIGN=RIGHT WIDTH=10%><FONT SIZE=2><B>Amount</B></FONT></TD>" + 
					   "</TR>" + 
						"<TR><TD COLSPAN=5><HR></TD></TR>");
		}
	}
	
	private void Print_Order_Source_Footer(String sOrderSourceDescription,
										   BigDecimal bdOrderSourceTotal,
										   PrintWriter out){
		
		out.println("</TABLE>");
		out.println("<TR><TD ALIGN=RIGHT><B>" + sOrderSourceDescription + "&nbsp;Total:&nbsp;&nbsp;&nbsp;</B>" + 
							bdOrderSourceTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + 
					"</TD></TR><TR><TD><HR></TD></TR>"
					);
	}
	
	private void Print_Pie_Chart(ArrayList<String> alOrderSourceList,
								 ArrayList<BigDecimal> alOrderSourceAmounts,
								 String sBackGroundColor,
								 PrintWriter out){

		//if nothing in the arrays, don't print
		if (alOrderSourceList.size() > 0){
			String sTitle = "Order source including";
			//System.out.println("sTitle = " + sTitle);
			BigDecimal bdTotal = BigDecimal.ZERO;
			String sLegends = "";
			String sValues = "";
	    	//System.out.println("alOrderSourceList.size() = " + alOrderSourceList.size());
	    	try{
				for (int i=0;i<alOrderSourceList.size();i++){
					//System.out.println(i + ":"); 
			    	//System.out.println("alOrderSourceList.get(i).toString() = " + (String) alOrderSourceList.get(i));
					//sLegends += (String) alOrderSourceList.get(i) + "|";
					bdTotal = bdTotal.add((BigDecimal) alOrderSourceAmounts.get(i));
					//System.out.println("((BigDecimal) alOrderSourceAmounts.get(i)).setScale(2, BigDecimal.ROUND_HALF_UP).toString() = " + ((BigDecimal) alOrderSourceAmounts.get(i)).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
					sValues += ((BigDecimal) alOrderSourceAmounts.get(i)).setScale(2, BigDecimal.ROUND_HALF_UP).toString() + ",";
				}
	    	}catch (Exception ex){
	    		System.out.println("Err: " + ex.getMessage());
	    	}
	    	
	    	if (bdTotal.compareTo(BigDecimal.ZERO) > 0){
		    	//System.out.println("2");
		    	//System.out.println("sLegends = " + sLegends);
				sValues = sValues.substring(0, sValues.length() - 1);
		    	//System.out.println("4");
				//System.out.println("sLegends = " + sLegends);
				//System.out.println("sValues = " + sValues);
				String sPercentages = "";
				for (int i=0;i<alOrderSourceAmounts.size();i++){
					String s = ((BigDecimal) alOrderSourceAmounts.get(i)).multiply(new BigDecimal(100)).divide(bdTotal, 2, BigDecimal.ROUND_HALF_UP).toString() + "%";
					sLegends += (String) alOrderSourceList.get(i) + "(" + s + ")|";
					sPercentages += s + "|";
				}
				sLegends = sLegends.substring(0, sLegends.length() - 1);
				sPercentages = sPercentages.substring(0, sPercentages.length() - 1);
				//System.out.println("sPercentages = " + sPercentages);
				
				out.println("<img src=\"https://chart.apis.google.com/chart" + 
						    "?chs=600x250" +
						    "&chf=bg,s," 
						    + sBackGroundColor 
						    + "&chco=FF0000|0000FF|00E300|AA0033|FF9900|49188F|008000|C2BDDD|80C65A|FFFF88|224499|C645DB|87A387|875305" + 
						    "&cht=p" +
						    "&chds=a" +
						    "&chd=t:" + sValues +
						    "&chdl=" + sLegends + 
						    "&chl=" + sPercentages + 
						    "&chtt=" + sTitle + "%3A" + 
						    "&chts=000000,11.5\"/>");
	    	}else{
				//Don't do anything
				out.println("&nbsp;");
	    	}
		}else{
			//Don't do anything
			out.println("&nbsp;");
		}
		
		/* JAVA SCRIPT VERSION
		out.println("    <script type=\"text/javascript\" src=\"http://www.google.com/jsapi\"></script>\n"
			      + "    <script type=\"text/javascript\">\n"
			      + "        google.load('visualization', '1', {packages: ['corechart']});\n"
			      + "    </script>"	        
			      + "    <script type=\"text/javascript\">\n"
			      + "    function drawOrderSourceChart() {\n"
			      + "        var data = new google.visualization.DataTable();\n"
			      + "        data.addColumn('string', 'Order Source');\n"
			      + "        data.addColumn('number', 'Total Billing');\n"
			      + "        data.addRows(" + Integer.toString(alOrderSourceList.size()) + ");\n"
		);
		
		for (int i = 0; i < alOrderSourceList.size(); i++){
			BigDecimal bdTotals = alOrderSourceAmounts.get(i);
			if (bdTotals.compareTo(BigDecimal.ZERO) < 0.00){
				bdTotals = BigDecimal.ZERO;
			}
			out.println(
			    "        data.setValue(" + Integer.toString(i) + ", 0, '" + alOrderSourceList.get(i).replace("'","") 
			    + "');\n"
			    + "        data.setValue(" + Integer.toString(i) + ", 1, " 
			    	+ SMUtilities.doubleTo2DecimalSTDFormat(bdTotals.doubleValue()).replace(",", "") + ");\n"
			);
		}
			    // Create and draw the visualization.
			    out.println("        new google.visualization.PieChart(document.getElementById('visualization_id')).\n"
					      + "            draw(data, {title:\"Billing By Order Source "
					      + " including: "
					      + "\"});\n"
					      + "    }\n"
					      
					      + "    google.setOnLoadCallback(drawOrderSourceChart);\n"
					      + "	 "
					      + "    </script>\n"
					      + "</head>\n"
					      + "<body style=\"font-family: " + SMUtilities.DEFAULT_FONT_FAMILY + ";border: 0 none;\">\n"
					      + "<div id=\"visualization_id\" style=\"width: 600px; height: 400px;\"></div>\n"
			    );*/
	}
}

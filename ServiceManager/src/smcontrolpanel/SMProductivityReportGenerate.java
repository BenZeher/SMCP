package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.math.BigDecimal;

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

public class SMProductivityReportGenerate extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMProductivityReport))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);

    	//Calculate time period
	    SimpleDateFormat USDateTimeformatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
    	SimpleDateFormat USDateOnlyformatter = new SimpleDateFormat("MM/dd/yyyy");

    	String title = "Productivity Report";
    	String sSQL = "";
    	ResultSet rs = null;

		
	 	   //log usage of this this report
	 	   SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
	 	   log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMPRODUCTIVITYREPORT, "REPORT", "SMProductivityReport", "[1376509350]");

		String sStartDate = clsManageRequestParameters.get_Request_Parameter("StartingDate", request);
		if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sStartDate)){
			out.println("Invalid start date - '" + sStartDate);
			return;
		}
		String sEndDate = clsManageRequestParameters.get_Request_Parameter("EndingDate", request);
		if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sEndDate)){
			out.println("Invalid end date - '" + sEndDate);
			return;
		}
		
		Timestamp datStartingDate = clsDateAndTimeConversions.StringToTimestamp("M/d/yyyy", sStartDate);
		Timestamp datEndingDate = clsDateAndTimeConversions.StringToTimestamp("M/d/yyyy", sEndDate);
	    
	    //Location & Service Types & Item Categories
	    boolean bCheckItemCategories = false;
	    if (request.getParameter("CheckItemCategories") != null){
	    	bCheckItemCategories = true;
    	}
	    
		Enumeration<?> paramNames = request.getParameterNames();
		ArrayList<String> alLocations = new ArrayList<String>(0);
		ArrayList<String> alServiceTypes = new ArrayList<String>(0);
		ArrayList<String> alItemCategories = new ArrayList<String>(0);
		while(paramNames.hasMoreElements()) {
			String s = paramNames.nextElement().toString();
			//System.out.println("paramNames.nextElement() = " + s);
			
			try{
				//System.out.println("s.subString(1, 2) = " + s.substring(1, 2));
				int iSwitch = Integer.parseInt(s.substring(1, 2));

    			switch (iSwitch) {
	                case 1: 
            			alLocations.add(s.substring(3));
        				break;
	                case 2: 
            			alServiceTypes.add(s.substring(3));
        				break;
	                case 3: 
            			if (bCheckItemCategories){
            				alItemCategories.add(s.substring(3));
            			}else{
            				alItemCategories.add("ALLIC");
            			}
        				break;
    			}
			}catch(Exception ex){
				//System.out.println("Not a parameter we care about at this point.");
			}
		}
	    
	    //Status check
	    boolean bShowSubtotalOnly = false;
	    if (request.getParameter("CheckSubtotalOnly") != null){
	    	bShowSubtotalOnly = true;
    	}
	    /*************END of PARAMETER list***************/
	    
	    boolean bHasRecord = false;
	    sSQL = "SELECT * FROM " + SMTableinvoiceheaders.TableName + ", " + 
	    		SMTableinvoicedetails.TableName + 
	    		" WHERE" +
	    		" " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber + " =" + 
	    		" " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber +
	    		" AND" +
	    		" " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate + " >= '" + datStartingDate.toString() + "'" +
	    		" AND" +
	    		" " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate + " <= '" + datEndingDate.toString() + "'";

	    //if there is any location selected, attach them
	    if (alLocations.size() == 0){
	    	//no location selected, make the SQL return nothing
	    	sSQL = sSQL + " AND" +
	    			" " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sLocation + " = '-1'";
	    }else{
	    	if (alLocations.get(0).toString().compareTo("ALLLOC") != 0){
	    		String sLocations = "";
	    		sSQL = sSQL + " AND (";
	    		for (int i=0;i<alLocations.size();i++){
	    			sLocations = sLocations + " OR" + 
	    					" " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sLocation + " = '" + alLocations.get(i) + "'";
	    		}
	    		//remove the leading OR
	    		sLocations = sLocations.substring(4);
	    		sSQL = sSQL + sLocations + ")";
	    	}
	    }

	    //if there is any service type selected, attach them
	    if (alServiceTypes.size() == 0){
	    	//no location selected, make the SQL return nothing
	    	sSQL = sSQL + " AND" +
	    			" " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sServiceTypeCode + " = 'SH9999'";
	    }else{
	    	if (alServiceTypes.get(0).toString().compareTo("ALLST") != 0){
	    		sSQL = sSQL + " AND (";
	    		String sServiceTypes = "";
	    		for (int i=0;i<alServiceTypes.size();i++){
	    			sServiceTypes = sServiceTypes + " OR" + 
	    					" " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sServiceTypeCode + " = '" + alServiceTypes.get(i) + "'";
	    		}
	    		//remove the leading OR
	    		sServiceTypes = sServiceTypes.substring(4);
	    		sSQL = sSQL + sServiceTypes + ")";
	    	}
	    }

	    //if there is any category type selected, attach them
	    if (alItemCategories.size() == 0){
	    	//no location selected, make the SQL return nothing
	    	sSQL = sSQL + " AND" +
	    			" " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sServiceTypeCode + " = ''";
	    }else{
	    	if (alItemCategories.get(0).toString().compareTo("ALLIC") != 0){
	    		sSQL = sSQL + " AND (";
	    		String sItemCategories = "";
	    		for (int i=0;i<alItemCategories.size();i++){
	    			sItemCategories = sItemCategories + " OR" + 
	    					" " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemCategory + " = '" + alItemCategories.get(i) + "'";
	    		}
	    		//remove the leading OR
	    		sItemCategories = sItemCategories.substring(4);
	    		sSQL = sSQL + sItemCategories + ")";
	    	}
	    }

	    sSQL = sSQL + " ORDER BY" + 
	    		" " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sMechInitial + ", " + 
	    		" " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber;


	    try{
	    	rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	    }catch (SQLException ex){
	    	System.out.println("[1579274753] Error when opening rs for productivity report!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
	    }

	    String sColor = SMUtilities.getInitBackGroundColor(getServletContext(), sDBID);
	    out.println(SMUtilities.getMasterStyleSheetLink());
	    try{
	    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
	    			   "Transitional//EN\">" +
	    		       "<HTML>" +
	    		       "<HEAD><TITLE>" + title + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>" + 
	    			   "<BODY BGCOLOR=\"#FFFFFF\">" +
	    			   "<TABLE BORDER=0 WIDTH=100% BGCOLOR = \"" + sColor + "\" >" +
	    			   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
	    			   + USDateTimeformatter.format(new Date(System.currentTimeMillis())) 
	    			   + " Printed by " + SMUtilities.getFullNamebyUserID(sUserID, getServletContext(), sDBID, "SMMonthlySalesReportGenerate") 
	    			   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>" +
	    			   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + title + "</B></FONT></TD></TR>" +
	    			   
	    			   "<TR><TD COLSPAN=2><FONT SIZE=2>Based on Invoice(s) From <B> " +
				 		USDateOnlyformatter.format(datStartingDate) + " to " + 
				 		USDateOnlyformatter.format(datEndingDate) + "</B></FONT></TD></TR>");
	    					   
	    		   out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
	    					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
	    					+ "\">Return to user login</A><BR>" +
	    			   "</TD></TR></TABLE>");
		    
		    Long lCurrentMechanicID = 0L;
		    String sCurrentMechanicName = null;
		    String sCurrentInvoiceNumber = null;
		    BigDecimal bdInvoiceTotal = BigDecimal.ZERO;
		    BigDecimal bdMechanicTotal = BigDecimal.ZERO;
		    
		    int iCount = 0;
		    out.println("<TABLE WIDTH = 100% CLASS=\""+ SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">" );
		    
		    while (rs.next()){	    
		    	bHasRecord = true;
		    	if (lCurrentMechanicID == 0 
		    		|| rs.getLong(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.imechid) != lCurrentMechanicID){
		    		//new mechanic, printout last mechanic's total.
		    		if (lCurrentMechanicID != 0){
		    			if (!bShowSubtotalOnly){
			    			//print out total for this invoice 
				    	out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\">");
			    		out.println("<TD COLSPAN=\"5\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK +"\">&nbsp</TD>");
			    		out.println("</TR>");
		    			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\">");
		    			out.println("<TD COLSPAN = \"4\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Total for Invoice #: <A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + SMUtilities.lnViewInvoice(sDBID, sCurrentInvoiceNumber ) + "\">" + sCurrentInvoiceNumber + "</A>: </TD>");
		    			out.println("<TD  CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">" + bdInvoiceTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString() +" </TD>");
		    			out.println("</TR>");
		    			}
		    			//print out total for this mechanic
				    	out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
			    		out.println("<TD COLSPAN=\"5\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK +"\">&nbsp</TD>");
			    		out.println("</TR>");
		    			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
		    			out.println("<TD COLSPAN = \"4\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Total For Technician " + sCurrentMechanicName + ": </TD>");
		    			out.println("<TD  CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">" + bdMechanicTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString() +" </TD>");
		    			out.println("</TR>");
				    	out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
			    		out.println("<TD COLSPAN=\"5\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK +"\">&nbsp</TD>");
			    		out.println("</TR>");
		    			iCount=0;
		    		}
		    		//print out new name
		    		if (rs.getLong(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.imechid) == 0){
		    			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		    			out.println("<TD COLSPAN = \"5\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK + "\">&nbsp; </TD>");
		    			out.println("</TR>");
		    			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		    			out.println("<TD COLSPAN = \"5\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Technician: N/A: </TD>");
		    			out.println("</TR>");
		    			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		    			out.println("<TD COLSPAN = \"5\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK + "\">&nbsp; </TD>");
		    			out.println("</TR>");
		    			iCount=0;
		    		}else{
		    			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		    			out.println("<TD COLSPAN = \"5\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK + "\">&nbsp; </TD>");
		    			out.println("</TR>");
		    			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		    			out.println("<TD COLSPAN = \"5\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Technician: " + rs.getString(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sMechFullName) + "</TD>");
		    			out.println("</TR>");
		    			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		    			out.println("<TD COLSPAN = \"5\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK + "\">&nbsp; </TD>");
		    			out.println("</TR>");
		    			iCount=0;
		    		}

		    		lCurrentMechanicID = rs.getLong(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.imechid);
		    		sCurrentMechanicName = rs.getString(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sMechFullName);
		    		sCurrentInvoiceNumber = rs.getString(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber).trim();
		    		//System.out.println("Inv: " + sCurrentInvoiceNumber);

	    			bdMechanicTotal = BigDecimal.ZERO;
		    		bdInvoiceTotal = BigDecimal.ZERO;
		    		if (!bShowSubtotalOnly){
		    			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		    			out.println("<TD COLSPAN=\"5\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Inv#:&nbsp;" 
		    					+ "&nbsp;<A HREF=\"" 
		    					+ SMUtilities.getURLLinkBase(getServletContext()) + "" 
		    					+ SMUtilities.lnViewInvoice(sDBID, rs.getString(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber)) + "\">" 
		    					+ rs.getString(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber).trim() + "</A>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; "
		    							+ "Date:</B>&nbsp;"+ USDateOnlyformatter.format(rs.getDate(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate)) +""
		    									+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Bill To:</B>&nbsp;" + rs.getString(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sBillToName)  
		    									+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Ship To:</B>&nbsp;" + rs.getString(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sShipToName) 
		    									+ "</TD>");
		    			out.println("</TR>");
		    			
		    			
		    			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		    			out.println("<TD COLSPAN = \"5\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK + "\">&nbsp; </TD>");
		    			out.println("</TR>");
		    			
		    			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		    			out.println("<TD  CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Item Number </TD>");
		    			out.println("<TD  CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Item Description </TD>");
		    			out.println("<TD  CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Qty.Shipped </TD>");
		    			out.println("<TD  CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">UOM </TD>");
		    			out.println("<TD  CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Ext. Price </TD>");
		    			out.println("</TR>");

		    		}
		    	}

			    if (sCurrentInvoiceNumber.compareTo(rs.getString(SMTableinvoiceheaders.sInvoiceNumber).trim()) != 0){
			    	if (!bShowSubtotalOnly){
		    			//print out total for this invoice 
			    		out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\">");
		    			out.println("<TR><TD COLSPAN=\"5\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK +"\">&nbsp</TD>");
		    			out.println("</TR>");
		    			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\">");
		    			out.println("<TD COLSPAN = \"4\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Total for Invoice #: <A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + SMUtilities.lnViewInvoice(sDBID, sCurrentInvoiceNumber ) + "\">" + sCurrentInvoiceNumber + "</A>: </TD>");
		    			out.println("<TD  CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">" + bdInvoiceTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString() +" </TD>");
		    			out.println("</TR>");
		    			iCount=0;
			    	}
	    			sCurrentInvoiceNumber = rs.getString(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber).trim();
	    			//System.out.println("Inv: " + sCurrentInvoiceNumber);
	    			//reset invoice total counter
	    			bdInvoiceTotal = BigDecimal.ZERO;
	    			if (!bShowSubtotalOnly){
		    			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		    			out.println("<TD COLSPAN=\"5\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Inv#:&nbsp;" 
		    					+ "&nbsp;<A HREF=\"" 
		    					+ SMUtilities.getURLLinkBase(getServletContext()) + "" 
		    					+ SMUtilities.lnViewInvoice(sDBID, rs.getString(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber)) + "\">" 
		    					+ rs.getString(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber).trim() + "</A>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; "
		    							+ "Date:</B>&nbsp;"+ USDateOnlyformatter.format(rs.getDate(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate)) +""
		    									+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Bill To:</B>&nbsp;" + rs.getString(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sBillToName)  
		    									+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Ship To:</B>&nbsp;" + rs.getString(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sShipToName) 
		    									+ "</TD>");
		    			out.println("</TR>");
		    			
		    			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		    			out.println("<TD COLSPAN = \"5\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK + "\">&nbsp; </TD>");
		    			out.println("</TR>");
		    			
		    			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		    			out.println("<TD  CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Item Number </TD>");
		    			out.println("<TD  CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Item Description </TD>");
		    			out.println("<TD  CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Qty.Shipped </TD>");
		    			out.println("<TD  CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">UOM </TD>");
		    			out.println("<TD  CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Ext. Price </TD>");
		    			out.println("</TR>");
	    			}
			    }

			    BigDecimal bdLine = BigDecimal.valueOf(rs.getDouble(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dExtendedPrice));
			    if (!bShowSubtotalOnly){
					if(iCount % 2 == 0) {
						out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
					}else {
						out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
					}
					out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + rs.getString(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemNumber).trim() +"</TD>");
					out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+ rs.getString(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sDesc).trim() +" </TD>");
					out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + rs.getDouble(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dQtyShipped) + "</TD>");
					out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+ rs.getString(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sUnitOfMeasure).trim() +" </TD>");
					out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+ bdLine.setScale(2, BigDecimal.ROUND_HALF_UP).toString() +" </TD>");
					out.println("</TR>");
				    iCount++;
			    }
			    bdInvoiceTotal = bdInvoiceTotal.add(bdLine);
			    bdMechanicTotal = bdMechanicTotal.add(bdLine);
			    
		    }
		    if (bHasRecord){
			    //print out last total
		    	if (!bShowSubtotalOnly){
			    	out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\">");
		    		out.println("<TD COLSPAN=\"5\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK +"\">&nbsp</TD>");
		    		out.println("</TR>");
	    			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\">");
	    			out.println("<TD COLSPAN = \"4\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Total for Invoice #: <A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + SMUtilities.lnViewInvoice(sDBID, sCurrentInvoiceNumber ) + "\">" + sCurrentInvoiceNumber + "</A>: </TD>");
	    			out.println("<TD  CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">" + bdInvoiceTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString() +" </TD>");
	    			out.println("</TR>");
		    	}
				//print out total for this mechanic
		    	out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
	    		out.println("<TD COLSPAN=\"5\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK +"\">&nbsp</TD>");
	    		out.println("</TR>");
    			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
    			out.println("<TD COLSPAN = \"4\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Total For Technician " + sCurrentMechanicName + ": </TD>");
    			out.println("<TD  CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">" + bdMechanicTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString() +" </TD>");
    			out.println("</TR>");
		    	out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
	    		out.println("<TD COLSPAN=\"5\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK +"\">&nbsp</TD>");
	    		out.println("</TR>");
		    }else{
		    	out.println("<TR><TD ALIGN=CENTER><B>No Record Found</B></TD></TR>");
		    	
		    }

		    out.println("</TABLE>");
		    rs.close();
		    
	    }catch (SQLException ex){
	    	System.out.println("[1579274760] Error in SMProductivityReportGenerate!!");
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
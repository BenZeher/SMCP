package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMClasses.SMLogEntry;
import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

public class SMOpenOrdersReportGenerate extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMOpenOrdersReport))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sUserFullName = SMUtilities.getFullNamebyUserID(sUserID, getServletContext(), sDBID, this.toString());
	    String sColor = SMUtilities.getInitBackGroundColor(getServletContext(), sDBID);

    	SimpleDateFormat USDateOnlyformatter = new SimpleDateFormat("MM/dd/yyyy");

    	String sReportTitle = "Open Orders Report";
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 "
	 		   + "Transitional//EN\">"
	 	       + "<HTML>"
	 	       + "<HEAD>"

	 	       + "<TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>" 
	 		   + "<BODY BGCOLOR=\"" 
	 		   + "#FFFFFF"
	 		   + "\""
	 		   + " style=\"font-family: " + SMUtilities.DEFAULT_FONT_FAMILY + "\";"
	 		   //Jump to the last edit:
	 		   + " onLoad=\"window.location='#LastEdit'\""
	 		   + ">"
	 		   + "<TABLE BORDER=0 WIDTH=100% BGCOLOR = \"" + sColor + "\">"
	 		   + "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
	 		   + clsDateAndTimeConversions.nowStdFormat() + " Printed by " + sUserFullName 
	 		   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>"
	 		   + "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>"
	 		   + "</TR>");
	 				   
	     	out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
	 			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
	 			+ "\">Return to user login</A><BR>");
	 	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.SMListOrdersForScheduling) 
	 	    		+ "\">Summary</A><BR>");

	 	    out.println("</TD></TR></TABLE>");
		
	 	    out.println(SMUtilities.getMasterStyleSheetLink());
		
		   
	 	   //log usage of this this report
	 	   SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
	 	   log.writeEntry(sUserID , SMLogEntry.LOG_OPERATION_SMOPENORDERSREPORT, "REPORT", "SMOpenOrdersReport", "[1376509331]");

	    try{

		    boolean bHasRecord = false;
			String sSQL = "SELECT DISTINCT" + "\n" +
					  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sLocation + ","  + "\n" +
					  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode + "," + "\n" + 
					  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCodeDescription + "," + "\n" +
					  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber + "," + "\n" +
					  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName + "," + "\n" +
					  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName + "," + "\n" + 
					  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderDate + "," + "\n" + 
					  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson + "\n" +
			
				  " FROM" + "\n" + 
					  " " + SMTableorderheaders.TableName + "\n" + 
			      " INNER JOIN" + 
					  " " + SMTableorderdetails.TableName + "\n" + 
				  " ON" + 
					  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.dOrderUniqueifier + " = " + 
					  " " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dUniqueOrderID + "\n" + 
				  " WHERE (" + "\n" + 
				  	  " (" + "\n" +
				  	  	"(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate + " < '1900-01-01')" + 
				  	" OR" +
				  	  " (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate + " IS NULL))" + "\n" +
				  	" AND (" +
				  	  " " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered + " > 0)" + "\n" +
				  	  
				  	"/* NO QUOTES! */" + "\n" +
				  	" AND (" + SMTableorderheaders.iOrderType + " != " + SMTableorderheaders.ORDERTYPE_QUOTE + ")" +
				  	")" + "\n" +
				  " ORDER BY" + 
				  	  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sLocation + "," +
				  	  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode + "," +
				  	  " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson + "\n";

		    ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
		    
			out.println("<TABLE WIDTH=100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\">");
		    String sCurrentLocation = "";
		    String sCurrentServiceType = "";
		    int iCount = 0;
		    while (rs.next()){	  
		    	
		    	bHasRecord = true;
		    	
		    	if (sCurrentLocation.compareTo(rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sLocation)) != 0){
		    		
		    		sCurrentLocation = rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sLocation);
		    		sCurrentServiceType = rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode);
		    		
		        	out.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		        	out.println("<TD COLSPAN=\"5\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Location:&nbsp;&nbsp; " + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sLocation) +"&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;Service Type:&nbsp;&nbsp;</B>  " + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCodeDescription) + "</TD>");
		    		out.println("</TR>"); 
		    		
		        	out.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		        	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Sales #</TD>");
		        	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Order Date</TD>");
		        	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Order Number</TD>");
		        	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Bill To Name</TD>");
		        	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Ship To Name</TD>");
		    		out.println("</TR>"); 
		        	out.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		        	out.println("<TD COLSPAN=\"8\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK + "\">&nbsp;</TD>");
		    		out.println("</TR>"); 
		    		iCount = 0;
		    	}

		    	if (sCurrentServiceType.compareTo(rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode)) != 0){
		    		sCurrentServiceType = rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode);
		    		
		        	out.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		        	out.println("<TD COLSPAN=\"5\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Location:&nbsp;&nbsp; " + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sLocation) +"&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;Service Type:&nbsp;&nbsp;</B>  " + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCodeDescription) + "</TD>");
		    		out.println("</TR>"); 
		    		
		        	out.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		        	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Sales #</TD>");
		        	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Order Date</TD>");
		        	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Order Number</TD>");
		        	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Bill To Name</TD>");
		        	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Ship To Name</TD>");
		    		out.println("</TR>"); 
		        	out.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		        	out.println("<TD COLSPAN=\"8\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK + "\">&nbsp;</TD>");
		    		out.println("</TR>"); 
		    		iCount = 0;
		    	} 
				if(iCount % 2 == 0) {
			    	out.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
				}else {
			    	out.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
				}
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson) + "</TD>");
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" + USDateOnlyformatter.format(rs.getDate(SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderDate)) + "</TD>");
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber) 
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" 
				+ rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber).trim() + "</A></TD>");
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName).trim() + "</TD>");
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName).trim() + "</TD>");
		    	iCount++;
		    }
		    if (!bHasRecord){
		    	out.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
	        	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">No Record Found</TD>");
	    		out.println("</TR>"); 
		    }
			
		    out.println("</TABLE>");
		    rs.close();
		    
	    }catch (SQLException ex){
	    	/*System.out.println("Error in SMOpenOrdersReportGenerate");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());*/
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
package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableicitemlocations;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

public class SMCustomPartsOnHandNotOnSalesOrderGenerate extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static boolean bDebugMode = false;
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMCustomPartsonHandNotonSalesOrders))
			{
				return;
			}

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sUserFullName = SMUtilities.getFullNamebyUserID(sUserID, getServletContext(), sDBID, this.toString());
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sReportTitle = "Custom Parts On Hand Not On Sales Order";
		String sColor = SMUtilities.getInitBackGroundColor(getServletContext(), sDBID);

	    
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
		log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMCUSTOMITEMSONHANDNOTONSALESORDER, "REPORT", "SMCustomPartsOnHandNotOnSalesOrder", "[1376509317]");

		try{

			boolean bHasRecord = false;
			String sCurrentItemCategory = null;
			/*
			ArrayList<String> alItemCategories = new ArrayList<String>(0);
			if (request.getParameter("CheckItemCategories") != null){
				//create a list of 
				Enumeration<?> paramNames = request.getParameterNames();
				while(paramNames.hasMoreElements()) {
					String s = paramNames.nextElement().toString();
					//System.out.println("paramNames.nextElement() = " + s);
					if (s.substring(0, 2).compareTo("!!") == 0){
						alItemCategories.add(s.substring(2));
					}
				}
			}else{
				alItemCategories.add("ALLIC");
			}
			*/
			boolean bAllowItemViewing = 
					SMSystemFunctions.isFunctionPermitted(
							SMSystemFunctions.ICDisplayItemInformation, 
							sUserID, 
							getServletContext(),
							sDBID,
							(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
					);
		    String sSQL = 
	    		"SELECT"
		    	+ " 'Custom Parts On Hand Not On Sales Order'  as REPORTNAME"
	    		+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber + " AS ITEM"
	    		+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sItemDescription + " AS DESCRIPTION"
	    		+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sCategoryCode + " AS CATEGORY"
	    		+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sLocation + " AS LOCATION"
	    		+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand + " AS OH"
	    		+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sTotalCost + " AS TOTALCOST"
	    		+ " from " + SMTableicitemlocations.TableName + " LEFT JOIN " + SMTableicitems.TableName
	    		+ " on " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber + " = " 
	    		+ SMTableicitems.TableName + "." + SMTableicitems.sItemNumber

				+ " LEFT JOIN"
				+ " (SELECT DISTINCT " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemNumber 
				+ " as ITEM from " + SMTableorderdetails.TableName + " LEFT JOIN " + SMTableorderheaders.TableName
				+ " ON " + SMTableorderdetails.TableName + "." + SMTableorderdetails.strimmedordernumber + " = " 
				+ SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber
				+ " where ("
				+ "(" + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered + " != 0.00)"
				+ " AND (NOT " + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate + " > '1900-01-01')"
				+ ")"
				+ ") AS DETAILQUERY"
				+ " on " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber + " = DETAILQUERY.ITEM"

				+ " WHERE ("
				+ "(" + SMTableicitems.TableName + "." + SMTableicitems.sDedicatedToOrderNumber + " != '')"
				+ " AND ("
				+ "(" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand + " != 0.00)"
				+ " OR (" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sTotalCost + " != 0.00)"
				+ ")"
				+ " AND (DETAILQUERY.ITEM IS NULL)"
				+ ")"
				+ " ORDER BY CATEGORY, ITEM"
		    ;
		    if (bDebugMode){
		    	System.out.println("[1579268025] In " + this.toString() + " - main SQL = " + sSQL);
		    }
			ResultSet rsItemOnHandList = clsDatabaseFunctions.openResultSet(
					sSQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					"smcontrolpanel.SMCustomPartsOnHandNotOnSalesOrderGenerate");
		  	out.println("<TABLE WIDTH=100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">");

			boolean bList = true;
			int iCount = 0;
			while (rsItemOnHandList.next()){	
				if (bList){
					if (sCurrentItemCategory == null ||
							rsItemOnHandList.getString("CATEGORY").compareTo(sCurrentItemCategory) != 0){
						sCurrentItemCategory = rsItemOnHandList.getString("CATEGORY");
			        	out.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
			        	out.println("<TD COLSPAN=\"6\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Item Category:&nbsp;&nbsp; " + sCurrentItemCategory + "</TD>");
			    		out.println("</TR>"); 
			    		
			        	out.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
			        	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Item Number</TD>");
			        	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Item Desc</TD>");
			        	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Category</TD>");
			        	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Location</TD>");
			        	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Qty On Hand</TD>");
			        	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Total Cost</TD>");
			    		out.println("</TR>"); 
			        	out.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
			        	out.println("<TD COLSPAN=\"8\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK + "\">&nbsp;</TD>");
			    		out.println("</TR>"); 
			    		iCount = 0;

					}
					//System.out.println("6");
					bHasRecord = true;
					if(iCount % 2 == 0) {
				    	out.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
					}else {
				    	out.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
					}
					
					String sItemNumber = rsItemOnHandList.getString("ITEM");
					String sItemNumberLink = sItemNumber;
					if (bAllowItemViewing){
						sItemNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
						+ "smic.ICDisplayItemInformation?ItemNumber=" 
						+ sItemNumber
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "\">" + sItemNumber + "</A>";
					}						
		        	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + sItemNumberLink + "</TD>");
		        	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + rsItemOnHandList.getString("DESCRIPTION") +"</TD>");
		        	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+ rsItemOnHandList.getString("CATEGORY") +"</TD>");
		        	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+ rsItemOnHandList.getString("LOCATION") +"</TD>");
		        	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+ rsItemOnHandList.getString("OH") +"</TD>");
		        	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+ rsItemOnHandList.getString("TOTALCOST") +"</TD>");
		    		out.println("</TR>"); 
				}
				bList = true;
				iCount++;
			}
			if (!bHasRecord){
				out.println("<TR>\n<TD ALIGN=CENTER COLSPAN=6><B>No Record Found</B></TD>\n</TR>\n");

			}
			out.println("</TABLE>");
			//out.println("End Time: " + USTimeOnlyformatter.format(new Date(System.currentTimeMillis())) + "<BR>");
			rsItemOnHandList.close();
		}catch (SQLException ex){
			System.out.println("[1579268030] Error in SMCustomPartsOnHandNotOnSalesOrderGenerate!!");
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
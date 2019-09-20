package smcontrolpanel;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import SMClasses.SMWorkOrderHeader;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTableworkorders;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsStringFunctions;

public class SMListOrdersForSchedulingReport extends java.lang.Object{

	private String m_sErrorMessage;
	
	public SMListOrdersForSchedulingReport(
			){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			String sDBID,
			ArrayList<String>arLocations,
			ArrayList<String>arServiceTypes,
			String sStartingDate,
			String sEndingDate,
			boolean bDateRangeChosen,
			boolean bDateRangeToday,
			boolean bDateRangeThisWeek,
			boolean bDateRangeNextWeek,
			String sUserID,
			String sUserFullName,
			PrintWriter out,
			ServletContext context,
			String sLicenseModuleLevel
			){
		
		//Make sure at least one service type is included:
		if(arServiceTypes.size() < 1){
			m_sErrorMessage = "You must choose at least one order type.";
			return false;
		}
    	
		boolean bAllowWorkOrderConfiguring = 
				SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.SMConfigureWorkOrders, 
						sUserID, 
						context,
						sDBID,
						sLicenseModuleLevel
				);
		boolean bAllowOrderViewing = 
				SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.SMViewOrderInformation, 
						sUserID, 
						context,
						sDBID,
						sLicenseModuleLevel
				);
		
    	//print out the column headers.
    	//Salesperson, date, job#, customer, amount, MU, truck days, avg MU/TD
    	out.println("<TABLE WIDTH=100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">");
    	out.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Exp. Date</TD>");
    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Order #</TD>");
    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Type</TD>");
    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Terms</TD>");
    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Bill to</TD>");
    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Ship To</TD>");
    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Work order notes</TD>");
		out.println("</TR>"); 
    	out.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
    	out.println("<TD COLSPAN=\"8\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK + "\">&nbsp;</TD>");
		out.println("</TR>"); 

    	String SQL = "SELECT "
    		+ SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber
    		+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.datExpectedShipDate
    		+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sTerms
    		+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName
    		+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName
    		+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress1
    		+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress2
    		+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress3
    		+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress4
    		+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToCity
    		+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToState
    		+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToZip
    		+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.mTicketComments
    		+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCodeDescription
    		+ " FROM " + SMTableorderheaders.TableName
    		//TODO - qualify this for ONLY orders with something left on order:
    		+ " LEFT JOIN " 
    		+ "(SELECT SUM(" + SMTableorderdetails.dQtyOrdered + ") AS ORDEREDQTY"
    		+ ", " + SMTableorderdetails.strimmedordernumber
    		+ " FROM " + SMTableorderdetails.TableName
    		+ " WHERE (" + SMTableorderdetails.dQtyOrdered + " > 0.0000)"
    		+ " GROUP BY " + SMTableorderdetails.strimmedordernumber + ") AS DETAILSQUERY"
    		+ " ON " + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber
    		+ " = DETAILSQUERY." + SMTableorderdetails.strimmedordernumber
    		//Link to schedule entries
    		+ " LEFT JOIN " + SMTableworkorders.TableName
    		+ " ON (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber + " = " 
    		+ SMTableworkorders.TableName + "." + SMTableworkorders.strimmedordernumber + ")"
    		+ " AND ("
    			+ SMTableorderheaders.TableName + "." + SMTableorderheaders.datExpectedShipDate + " = " 
        		+ SMTableworkorders.TableName + "." + SMTableworkorders.datscheduleddate
    		+ ")"
    		+ " WHERE ("
    			+ "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datExpectedShipDate + " >= '" + sStartingDate + "')"
    			+ " AND (" +  SMTableorderheaders.TableName + "." + SMTableorderheaders.datExpectedShipDate + " <= '" + sEndingDate + "')"
			 	//NO QUOTES!
				+ " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType + " != "
					+ SMTableorderheaders.ORDERTYPE_QUOTE + ")"
				//NO JOBS ALREADY SCHEDULED!
				+ " AND (" + SMTableworkorders.lid + " IS NULL)"
				//NO CANCELED ORDERS!
				+  " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate
					+ " < '1990-01-01')"
    			+ " AND (";
					for(int i = 0; i < arServiceTypes.size(); i++){
						SQL = SQL + "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode + " = '" + arServiceTypes.get(i) + "') OR ";
					}
					//Trim off the last 'OR':
					SQL = clsStringFunctions.StringLeft(SQL, SQL.length() - " OR".length());
					SQL = SQL + ")";
					
				    if (arLocations.size() > 0){
				    	SQL = SQL + " AND (";
					    for (int i = 0; i < arLocations.size(); i++){
					    	if (i > 0){
					    		SQL = SQL + " OR ";
					    	}
					    	SQL = SQL + "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sLocation
					    		+ " = '" + arLocations.get(i) + "')";
					    }
				    	SQL = SQL + ")";
				    }
			SQL += " AND (DETAILSQUERY.ORDEREDQTY > 0.00)";
    		SQL += ")"
    		+ " ORDER BY "
    			+ SMTableorderheaders.TableName + "." + SMTableorderheaders.datExpectedShipDate
    			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber
    		;
    	try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, context, sDBID, "MySQL", this.toString() + " - user: " 
															  + sUserID 
															  + " - "
															  + sUserFullName
															  + " [1332278012]");
			int iCount = 0;
			while(rs.next()){
				if(iCount % 2 == 0) {
			    	out.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
				}else {
			    	out.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
				}
    			String sScheduleDate = clsDateAndTimeConversions.utilDateToString(rs.getDate(
        				SMTableorderheaders.TableName + "." + SMTableorderheaders.datExpectedShipDate),"MM/dd/yyyy");
    			String sOrderLink = rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber);
    			if (bAllowWorkOrderConfiguring){
    				String sScheduleLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
						+ "smcontrolpanel.SMConfigWorkOrderEdit"
						+ "?" + SMWorkOrderHeader.Paramlid + "=-1"
						+ "&" + SMWorkOrderHeader.Paramscheduleddate + "=" + sScheduleDate
						+ "&" + SMWorkOrderHeader.Paramstrimmedordernumber + "=" + sOrderLink
						+ "&" + SMConfigWorkOrderEdit.REMOVE_WORK_ORDER_ATTRIBUTE_FROM_SESSION + "=Y"
						+ "&CallingClass=smcontrolpanel.SMListOrdersForSchedulingSelection"
						+ "&ReturnToTruckSchedule=N"
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "\">" + "Schedule this order" + "</A></FONT>";
    				
    		    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" + sScheduleLink + "</TD>");
    			}
				
				if (bAllowOrderViewing){
					sOrderLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" 
	    				+ sOrderLink 
	    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
	    				+ "\">" + sOrderLink + "</A>"
	    			;
				}
		    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" + sOrderLink + "</TD>");
		    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCodeDescription) + "</TD>");
		    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sTerms) + "</TD>");
		    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName) + "</TD>");

    			
    			String sShipTo = "";
    			if (rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress1).trim().compareToIgnoreCase("") != 0){
    				sShipTo += " " + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress1).trim();
    			}
    			if (rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress2).trim().compareToIgnoreCase("") != 0){
    				sShipTo += " " + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress2).trim();
    			}
    			if (rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress3).trim().compareToIgnoreCase("") != 0){
    				sShipTo += " " + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress3).trim();
    			}
    			if (rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress4).trim().compareToIgnoreCase("") != 0){
    				sShipTo += " " + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress4).trim();
    			}
    			if (rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToCity).trim().compareToIgnoreCase("") != 0){
    				sShipTo += " " + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToCity).trim();
    			}
    			if (rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToState).trim().compareToIgnoreCase("") != 0){
    				sShipTo += ", " + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToState).trim();
    			}
    			if (rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToZip).trim().compareToIgnoreCase("") != 0){
    				sShipTo += " " + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToZip).trim();
    			}
		    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"><A HREF=\"" 
						+ clsServletUtilities.createGoogleMapLink(sShipTo)
						+ "\">"
						+ rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName).trim() + " " + sShipTo
						+ "</A>" + "</TD>");
		    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" +  rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.mTicketComments) + "</TD>");
    			out.println("</TR>");
    			iCount++;
			}
			rs.close();
		    out.println("</TABLE>");
		    
		}catch(SQLException e){
			System.out.println("Error in " + this.toString() + ":processReport - " + e.getMessage());
			m_sErrorMessage = "Error in " + this.toString() + ":processReport - " + e.getMessage();
			return false;
		}
		out.println("</TABLE>");
		return true;
	}

	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}

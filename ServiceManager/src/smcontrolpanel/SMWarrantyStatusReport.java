package smcontrolpanel;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableorderheaders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsStringFunctions;

public class SMWarrantyStatusReport extends java.lang.Object{

	private String m_sErrorMessage;
	
	public SMWarrantyStatusReport(
			){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			String sStartingDate,
			String sEndingDate,
			ArrayList<String> arServiceTypes,
			ArrayList<String> arSalesPersons,
			PrintWriter out,
			String sDBID,
			ServletContext context
			){
		
		//Make sure at least one service type is included:
		if(arServiceTypes.size() < 1){
			m_sErrorMessage = "You must choose at least one order type.";
			return false;
		}
    	
    	//print out the column headers.
    	//Salesperson, date, job#, customer, amount, MU, truck days, avg MU/TD
    	out.println("<TABLE WIDTH=100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">");
    	out.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Loc</TD>");
    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Exp. Date</TD>");
    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Order #</TD>");
    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Bill to</TD>");
    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Ship to</TD>");
    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Project Mgr</TD>");
    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Phone</TD>");
    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Sales</TD>");
		out.println("</TR>"); 
    	out.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
    	out.println("<TD COLSPAN=\"8\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK + "\">&nbsp;</TD>");
		out.println("</TR>"); 

    	String SQL = "SELECT "
    		+ SMTableorderheaders.datwarrantyexpiration + " AS ExpirationDate"
    		+ ", TRIM(" + SMTableorderheaders.sOrderNumber + ") AS JobNumber"
    		+ ", " + SMTableorderheaders.sBillToName
    		+ ", " + SMTableorderheaders.sBillToContact
    		+ ", " + SMTableorderheaders.sBillToPhone
    		+ ", " + SMTableorderheaders.sShipToName
    		+ ", " + SMTableorderheaders.sLocation
    		+ ", " + SMTableorderheaders.sShipToContact + " AS ProjectManager"
    		+ ", " + SMTableorderheaders.sShipToPhone + " AS PhoneNumber"
    		+ ", " + SMTableorderheaders.sSalesperson
    		+ ", " + SMTableorderheaders.sServiceTypeCode
    		+ " FROM " + SMTableorderheaders.TableName
    		+ " WHERE ("
    			+ "(" + SMTableorderheaders.datwarrantyexpiration + " >= '" + sStartingDate + "')"
    			+ " AND (" +  SMTableorderheaders.datwarrantyexpiration + " <= '" + sEndingDate + "')"
			 	//NO QUOTES!
				+ " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType + " != "
					+ SMTableorderheaders.ORDERTYPE_QUOTE + ")"

    			+ " AND (";
					for(int i = 0; i < arServiceTypes.size(); i++){
						SQL = SQL + "(" + SMTableorderheaders.sServiceTypeCode + " = '" + arServiceTypes.get(i) + "') OR ";
					}
					//Trim off the last 'OR':
					SQL = clsStringFunctions.StringLeft(SQL, SQL.length() - " OR".length());
					SQL = SQL + ")";
					
				    if (arSalesPersons.size() > 0){
				    	SQL = SQL + " AND (";
					    for (int i = 0; i < arSalesPersons.size(); i++){
					    	if (i > 0){
					    		SQL = SQL + " OR ";
					    	}
					    	SQL = SQL + "(" + SMTableorderheaders.sSalesperson 
					    		+ " = '" + arSalesPersons.get(i) + "')";
					    }
				    	SQL = SQL + ")";
				    }
    		SQL += ")"
    		+ " ORDER BY "
    			+ SMTableorderheaders.sLocation
    			+ ", " + SMTableorderheaders.datwarrantyexpiration
    			+ ", " + SMTableorderheaders.sBillToName 
    			+ ", " + SMTableorderheaders.sOrderNumber
    		;
			//System.out.println("Warranty Status SQL = " + SQL);
    	try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			int iCount = 0;
			while(rs.next()){
				if(iCount % 2 == 0) {
			    	out.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
				}else {
			    	out.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
				}
				
		    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + rs.getString(SMTableorderheaders.sLocation) + "</TD>");
		    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsDateAndTimeConversions.utilDateToString(rs.getDate("ExpirationDate"),"MM/dd/yyyy") + "</TD>");
		    	
		    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" +  
		    	"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" + rs.getString("JobNumber") 
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">" + rs.getString("JobNumber") + "</A>"  + "</TD>");
		    	
		    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + rs.getString(SMTableorderheaders.sBillToName) + "</TD>");
		    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + rs.getString(SMTableorderheaders.sShipToName) + "</TD>");
		    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + rs.getString(SMTableorderheaders.sBillToContact) + "</TD>");
		    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + rs.getString(SMTableorderheaders.sBillToPhone) + "</TD>");
		    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + rs.getString(SMTableorderheaders.sSalesperson) + "</TD>");
    			out.println("</TR>");
    			iCount++;
			}
			rs.close();

		    out.println("</TABLE>");
		    
		}catch(SQLException e){
			System.out.println("[1579275500] Error in " + this.toString() + ":processReport - " + e.getMessage());
			m_sErrorMessage = "Error in " + this.toString() + ":processReport - " + e.getMessage();
			return false;
		}
		return true;
	}

	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}

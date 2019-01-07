package smcontrolpanel;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;

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
    	out.println("<TABLE BORDER=0 WIDTH=100%>");
		out.println("<TR>" + 
			"<TD ALIGN=LEFT VALIGN=BOTTOM><B><FONT SIZE=2>Loc</FONT></B></TD>" +
		    "<TD ALIGN=LEFT VALIGN=BOTTOM><B><FONT SIZE=2>Exp. Date</FONT></B></TD>" +
		    "<TD ALIGN=LEFT VALIGN=BOTTOM><B><FONT SIZE=2>Order #</FONT></B></TD>" +
		    "<TD ALIGN=LEFT VALIGN=BOTTOM><B><FONT SIZE=2>Bill to</FONT></B></TD>" +
		    "<TD ALIGN=LEFT VALIGN=BOTTOM><B><FONT SIZE=2>Ship To</FONT></B></TD>" +
		    "<TD ALIGN=LEFT VALIGN=BOTTOM><B><FONT SIZE=2>Project Mgr</FONT></B></TD>" +
		    "<TD ALIGN=LEFT VALIGN=BOTTOM><B><FONT SIZE=2>Phone</FONT></B></TD>" +
		    "<TD ALIGN=LEFT VALIGN=BOTTOM><B><FONT SIZE=2>Sales</FONT></B></TD>" +
		"</TR>" + 
   		"<TR><TD COLSPAN=8><HR></TD><TR>");

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
			while(rs.next()){
    			out.println("<TR>");
    			out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + rs.getString(SMTableorderheaders.sLocation) + "</FONT></TD>");
				out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + clsDateAndTimeConversions.utilDateToString(rs.getDate("ExpirationDate"),"MM/dd/yyyy") + "</FONT></TD>");
				
    			out.println("<TD ALIGN=LEFT><FONT SIZE=2>"
    					+ "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" + rs.getString("JobNumber") 
    					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
    					+ "\">" + rs.getString("JobNumber") + "</A>" 
    					+ "</FONT></TD>");
    			out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + rs.getString(SMTableorderheaders.sBillToName) + "</FONT></TD>");
    			out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + rs.getString(SMTableorderheaders.sShipToName) + "</FONT></TD>");
    			out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + rs.getString(SMTableorderheaders.sBillToContact) + "</FONT></TD>");
    			out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + rs.getString(SMTableorderheaders.sBillToPhone) + "</FONT></TD>");
    			out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + rs.getString(SMTableorderheaders.sSalesperson) + "</FONT></TD>");
    			out.println("</TR>");
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

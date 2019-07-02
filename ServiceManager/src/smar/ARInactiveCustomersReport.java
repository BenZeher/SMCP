package smar;

import java.io.PrintWriter;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTablearcustomer;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import smcontrolpanel.SMUtilities;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ARInactiveCustomersReport extends java.lang.Object{

	private String m_sErrorMessage;
	
	ARInactiveCustomersReport(
			){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			PrintWriter out
			){

    	int iCustomersPrinted = 0;
    	String SQL = "SELECT "
    		+ SMTablearcustomer.sCustomerNumber
    		+ ", " + SMTablearcustomer.sCustomerName
    		+ ", " + SMTablearcustomer.datStartDate

    		+ " FROM " + SMTablearcustomer.TableName
    		+ " WHERE ("
    			+ SMTablearcustomer.iActive + " = 0"
    		+ ")"
    		;
    		
    	int iLinesPrinted = 0;
    	out.println(SMUtilities.getMasterStyleSheetLink());
    	printTableHeader(out);
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()){
				/*if (iLinesPrinted == 50){
					out.println("</TABLE><BR>");
					printTableHeader(out);
					iLinesPrinted = 0;
				}*/
				if(iLinesPrinted%2 == 0) {
					out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD  + "\">");
				}else {
					out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN  + "\">");
				}
			   out.println( "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER  + "\">" + rs.getString(SMTablearcustomer.sCustomerNumber) +  "</TD>");
			   out.println( "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER  + "\">" + clsDateAndTimeConversions.utilDateToString(rs.getDate(SMTablearcustomer.datStartDate),"MM/dd/yyyy") +  "</TD>");
			   out.println( "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER  + "\">" + rs.getString(SMTablearcustomer.sCustomerName) +  "</TD>");
    			out.println("</TR>");
    			iLinesPrinted++;
    			iCustomersPrinted++;
			}
			rs.close();
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL  + "\">");
			out.println( "<TD COLSPAN = \"3\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD  + "\"> " + iCustomersPrinted +  " Inactive Customers Printed</TD>");
			out.println("</TR>");
			out.println("</TABLE>");

		}catch(SQLException e){
			System.out.println("Error in " + this.toString() + ":processReport - " + e.getMessage());
			m_sErrorMessage = "Error in " + this.toString() + ":processReport - " + e.getMessage();
			return false;
		}
		return true;
	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
	private void printTableHeader(PrintWriter out){
		out.println("<TABLE WIDTH = 100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER  + "\">");
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING  + "\">" + 
		    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD  + "\"><B>Customer #</B></TD>" +
		    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD  + "\"><B>Start Date #</B></TD>" +
		    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD  + "\"><B>Name</B></TD>" +
		    "</TR>"
		    );
	}

}

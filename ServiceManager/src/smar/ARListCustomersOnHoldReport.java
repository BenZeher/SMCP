package smar;

import java.io.PrintWriter;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTablearcustomer;
import ServletUtilities.clsDatabaseFunctions;
import smcontrolpanel.SMUtilities;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ARListCustomersOnHoldReport extends java.lang.Object{

	private String m_sErrorMessage;
	private boolean bShowComments;
	
	ARListCustomersOnHoldReport(
			){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			PrintWriter out,
			boolean bShowCustomerComments
			){

		bShowComments = bShowCustomerComments;
		
    	int iCustomersPrinted = 0;
    	String SQL = "SELECT "
    		+ SMTablearcustomer.sCustomerNumber
    		+ ", " + SMTablearcustomer.sCustomerName
    		+ ", " + SMTablearcustomer.datStartDate
    		+ ", " + SMTablearcustomer.mCustomerComments

    		+ " FROM " + SMTablearcustomer.TableName
    		+ " WHERE ("
    			+ SMTablearcustomer.iOnHold + " = 1"
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
					out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN  + "\">");
				}else {
					out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD  + "\">");
				}
			   out.println( "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER  + "\">" + rs.getString(SMTablearcustomer.sCustomerNumber) +  "</TD>");
			   out.println( "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER  + "\">" + rs.getString(SMTablearcustomer.sCustomerName) +  "</TD>");
    			if (bShowComments){
    				String sCustomerComments = rs.getString(SMTablearcustomer.mCustomerComments);
    				if (sCustomerComments == null){
    					sCustomerComments = "";
    				}else{
    					sCustomerComments = sCustomerComments.trim();
    				}
    				   out.println( "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER  + "\">"
    					+ sCustomerComments
    					+ "</TD>");
    			}
    			out.println("</TR>");
    			iLinesPrinted++;
    			iCustomersPrinted++;
			}
			rs.close();
			if(bShowComments) {
				out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL  + "\">");
				out.println( "<TD COLSPAN = \"3\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD  + "\"> " + iCustomersPrinted +  " Customers on Hold Printed</TD>");
				out.println("</TR>");
			}else {
				out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL  + "\">");
				out.println( "<TD COLSPAN = \"2\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD  + "\"> " + iCustomersPrinted +  " Customers on Hold Printed</TD>");
				out.println("</TR>");
			}

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
		if (!bShowComments){
			out.println("<TABLE WIDTH = 100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER_COLLAPSE  + "\">");
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING  + "\">" + 
					"<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD  + "\"><B>Acct #</B></TD>" +
					"<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD  + "\"><B>Name</B></TD>" +
					"</TR>"
					);
		}else {
			out.println("<TABLE WIDTH = 100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER  + "\">");
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING  + "\">" + 
					"<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD  + "\"><B>Acct #</B></TD>" +
					"<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD  + "\"><B>Name</B></TD>" +
					"<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD  + "\"><B>Comments</B></TD>" +
					"</TR>"
					);
		}
	}
}

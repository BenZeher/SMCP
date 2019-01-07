package smar;

import java.io.PrintWriter;

import SMDataDefinition.SMTablearcustomer;
import ServletUtilities.clsDatabaseFunctions;

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
    	printTableHeader(out);
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()){
				if (iLinesPrinted == 50){
					out.println("</TABLE><BR>");
					printTableHeader(out);
					iLinesPrinted = 0;
				}
    			out.println("<TR>");
				out.println("<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" + rs.getString(SMTablearcustomer.sCustomerNumber) + "</FONT></TD>");
    			out.println("<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" + rs.getString(SMTablearcustomer.sCustomerName) + "</FONT></TD>");
    			if (bShowComments){
    				String sCustomerComments = rs.getString(SMTablearcustomer.mCustomerComments);
    				if (sCustomerComments == null){
    					sCustomerComments = "";
    				}else{
    					sCustomerComments = sCustomerComments.trim();
    				}
    				out.println("<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" 
    					+ sCustomerComments
    					+ "</FONT></TD>");
    			}
    			out.println("</TR>");
    			iLinesPrinted++;
    			iCustomersPrinted++;
			}
			rs.close();
			out.println("</TABLE>");
		    out.println("<BR><B>" + iCustomersPrinted + " customers on hold printed</B>");

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
		out.println("<TABLE BORDER=0 WIDTH=100%>");
		
		if (!bShowComments){
			out.println("<TR>" + 
			    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=10%><B><FONT SIZE=2>Acct #</FONT></B></TD>" +
			    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=80%><B><FONT SIZE=2>Name</FONT></B></TD>" +
			    "</TR>"
			    );
		}else{
			out.println("<TR>" + 
			    "<TD ALIGN=LEFT VALIGN=BOTTOM><B><FONT SIZE=2>Acct #</FONT></B></TD>" +
			    "<TD ALIGN=LEFT VALIGN=BOTTOM><B><FONT SIZE=2>Name</FONT></B></TD>" +
			    "<TD ALIGN=LEFT VALIGN=BOTTOM><B><FONT SIZE=2>Comments</FONT></B></TD>" +
			    "</TR>"
		    );
		}
	}
}

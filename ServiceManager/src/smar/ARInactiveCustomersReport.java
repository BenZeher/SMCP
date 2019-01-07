package smar;

import java.io.PrintWriter;
import SMDataDefinition.SMTablearcustomer;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

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
				out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + rs.getString(SMTablearcustomer.sCustomerNumber) + "</FONT></TD>");
    			out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + clsDateAndTimeConversions.utilDateToString(rs.getDate(SMTablearcustomer.datStartDate),"MM/dd/yyyy") + "</FONT></TD>");
    			out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + rs.getString(SMTablearcustomer.sCustomerName) + "</FONT></TD>");
    			out.println("</TR>");
    			iLinesPrinted++;
    			iCustomersPrinted++;
			}
			rs.close();
			out.println("</TABLE>");
		    out.println("<BR><B>" + iCustomersPrinted + " inactive customers printed</B>");

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
		out.println("<TR>" + 
		    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=10%><B><FONT SIZE=2>Customer #</FONT></B></TD>" +
		    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=10%><B><FONT SIZE=2>Start date</FONT></B></TD>" +
		    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=80%><B><FONT SIZE=2>Name</FONT></B></TD>" +
		    "</TR>"
		    );
	}

}

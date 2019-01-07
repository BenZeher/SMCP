package smar;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import smcontrolpanel.SMUtilities;

public class ARActivityReport extends java.lang.Object{

	private String m_sErrorMessage;
	
	ARActivityReport(
			){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			String sStartingCustomer,
			String sEndingCustomer,
			java.sql.Date datStartingDate,
			java.sql.Date datEndingDate,
			boolean bIncludeFullyPaidTransactions,
			String sDBID,
			PrintWriter out,
			ServletContext context
			){

		long lStartingTime = System.currentTimeMillis();
		
    	if(!createTemporaryTables(
    			conn,
    			sStartingCustomer, 
    			sEndingCustomer, 
    			datStartingDate,
    			datEndingDate,
    			bIncludeFullyPaidTransactions
    			)){
        	return false;
    	}
		
    	//variables for customer total calculations
    	BigDecimal dCustomerTransactionTotal = new BigDecimal(0);
    	BigDecimal dCustomerBalanceTotal = new BigDecimal(0);

    	//variables for grand total calculations
    	BigDecimal dGrandTotalTransactionAmount = new BigDecimal(0);
    	BigDecimal dGrandTotalBalance = new BigDecimal(0);

    	String sCurrentCustomer = "";
    	int iCustomersPrinted = 0;
    	String SQL = ARSQLs.Get_Activity_Report();
    	int iLinesPrinted = 0;
    	printTableHeader(out);
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()){
				if (iLinesPrinted == 50){
					out.println("</TABLE>");
					printTableHeader(out);
					iLinesPrinted = 0;
				}
	    		//Print the header for any new customer:
	    		if (rs.getString("scustomer").compareToIgnoreCase(sCurrentCustomer) != 0){
	    			//Print the footer, if the record is for a new customer:
		    		if (sCurrentCustomer.compareToIgnoreCase("") != 0){
		    			out.println("<TR>");
		    			out.println("<TD ALIGN=RIGHT colspan=\"9\"><B><FONT SIZE=2>Customer total:</FONT></B></TD>");
		    			out.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dCustomerTransactionTotal) + "</B></FONT></TD>");
			    		out.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dCustomerBalanceTotal) + "</B></FONT></TD>");
		    			out.println("</TR>");
		    			
		    			//Reset the customer totals:
		    			dCustomerTransactionTotal = BigDecimal.ZERO;
		    			dCustomerBalanceTotal = BigDecimal.ZERO;
		    	    	iCustomersPrinted++;
		    		}

	    			//Print the customer header:
		    		String sCustomerNumber = rs.getString("scustomer");
		    		ARCustomer cust = new ARCustomer(sCustomerNumber);
		    		String sTerms = "N/A";
		    		if (cust.load(conn)){
		    			sTerms = cust.getM_sTerms();
		    		}
	    			out.println("<TR>");
	    			out.println("<TD><B><FONT SIZE=2>" + sCustomerNumber + "</FONT></B></TD>");
	    			out.println("<TD ALIGN=LEFT colspan=\"11\"><B><FONT SIZE=2>" 
	    					+ rs.getString("scustomername")+ " </B>,"
	    					+ " Current Balance: <B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(cust.getCurrentStoredBalance(conn)) + "</B>,"
	    					+ " Retainage Balance: <B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(cust.getCalculatedRetainageBalance(conn)) + "</B>,"
	    					+ " CR Limit: <B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal("dcreditlimit")) + "</B>,"
	    					+ " Terms: <B>" + sTerms + "</B>"
	    					+ "</FONT></TD>");
	    		}
    			out.println("<TR>");
				out.println("<TD ALIGN=LEFT><FONT SIZE=2>&nbsp;&nbsp;" + rs.getString("sdocappliedto") + "</FONT></TD>");
    			out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + getDocumentTypeLabel(rs.getInt("idoctype")) + "</FONT></TD>");
    			out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + rs.getString("sdocnumber") + "</FONT></TD>");
    			out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + Long.toString(rs.getLong("ldocid")) + "</FONT></TD>");
    			out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + clsDateAndTimeConversions.utilDateToString(rs.getDate("datdocdate"),"MM/dd/yyy") + "</FONT></TD>");
	    		out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + clsDateAndTimeConversions.utilDateToString(rs.getDate("datduedate"),"MM/dd/yyy") + "</FONT></TD>");
	    		
	    		String sOrderNumber = rs.getString("sordernumber").trim();
	    		out.println("<TD ALIGN=LEFT><FONT SIZE=2><A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" 
	    		+ sOrderNumber 
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
	    		+ "\">" + ARUtilities.Fill_In_Empty_String_For_HTML_Cell(sOrderNumber) + "</A></FONT></TD>");
	    		
	    		out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + Long.toString(rs.getLong("loriginalbatchnumber")) 
	    				+ "-" + Long.toString(rs.getLong("loriginalentrynumber")) + "</FONT></TD>");
	    		out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + Long.toString(rs.getLong("ldaysover")) + "</FONT></TD>");
	    		out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal("doriginalamt")) + "</FONT></TD>");
	    		out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal("dcurrentamt")) + "</FONT></TD>");
    			out.println("</TR>");
    			
    			//Set the totals:
    			dCustomerTransactionTotal = dCustomerTransactionTotal.add(rs.getBigDecimal("doriginalamt"));
    			dCustomerBalanceTotal = dCustomerBalanceTotal.add(rs.getBigDecimal("dcurrentamt"));
    	    	
    	    	//Accumulate the grand totals:
    	    	dGrandTotalTransactionAmount = dGrandTotalTransactionAmount.add(rs.getBigDecimal("doriginalamt"));
    	    	dGrandTotalBalance = dGrandTotalBalance.add(rs.getBigDecimal("dcurrentamt"));
    			//Reset:
    			sCurrentCustomer = rs.getString("scustomer");
    			iLinesPrinted++;
			}
			rs.close();
			
			//Print the last customer's totals, if at least one customer was listed:
    		if (sCurrentCustomer.compareToIgnoreCase("") != 0){
    			out.println("<TR>");
    			out.println("<TD ALIGN=RIGHT colspan=\"9\"><B><FONT SIZE=2>Customer total:</FONT></B></TD>");
    			out.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dCustomerTransactionTotal) + "</B></FONT></TD>");
	    		out.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dCustomerBalanceTotal) + "</B></FONT></TD>");
    			out.println("</TR>");
    	    	iCustomersPrinted++;
    		}

		    //Print the grand totals:
		    out.println("<TD colspan=\"11\">&nbsp;</TD>");
			out.println("<TR>");
			out.println("<TD ALIGN=RIGHT colspan=\"9\"><B><FONT SIZE=2>Report totals:</FONT></B></TD>");
			out.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dGrandTotalTransactionAmount) + "</B></FONT></TD>");
			out.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dGrandTotalBalance) + "</B></FONT></TD>");
			out.println("</TR>");
		    out.println("</TABLE>");
		    
		    //Print the legends:
		    out.println("<TABLE BORDER=0 WIDTH=100%>");
		    out.println("<TR>");
		    for (int i = 0;i <= 8; i++){
		    	out.println("<TD><FONT SIZE=2><I>" + ARDocumentTypes.Get_Document_Type_Label(i) + " = " + getDocumentTypeLabel(i) + "</I></FONT></TD>");
		    }
		    out.println("</TR>");
		    out.println("</TABLE>");
		    
		    out.println("<B>" + iCustomersPrinted + " customers printed</B>");
		    long lEndingTime = System.currentTimeMillis();
		    out.println("<BR>Processing took " + (lEndingTime - lStartingTime)/1000 + " seconds.");

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
	private String getDocumentTypeLabel(int lDocType){
		
		switch (lDocType){
		//Invoice
		case 0: return "IN";
		//Credit
		case 1: return "CR";
		//Payment
		case 2: return "PY";
		//Prepay
		case 3: return "PI";
		//Reversal
		case 4: return "RV";
		//Invoice adjustment
		case 5: return "IA";
		//Misc Receipt
		case 6: return "MR";
		//Cash adjustment
		case 7: return "CA";
		//Credit adjustment:
		case 8: return "RA";
		//Retainage transaction:
		case 9: return "RT";
		default: return "IN";
		}
	}
	private void printTableHeader(PrintWriter out){
		out.println("<TABLE BORDER=0 WIDTH=100%>");
		out.println("<TR>" + 
		    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=8%><B><FONT SIZE=2>Applied to</FONT></B></TD>" +
		    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=3%><B><FONT SIZE=2>Type</FONT></B></TD>" +
		    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=8%><B><FONT SIZE=2>Doc #</FONT></B></TD>" +
		    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=7%><B><FONT SIZE=2>Doc ID</FONT></B></TD>" +
		    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=7%><B><FONT SIZE=2>Doc. Date</FONT></B></TD>" + 
		    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=7%><B><FONT SIZE=2>Due Date</FONT></B></TD>" +
		    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=8%><B><FONT SIZE=2>Order #</FONT></B></TD>" +
		    "<TD ALIGN=RIGHT VALIGN=BOTTOM WIDTH=8%><B><FONT SIZE=2>Batch-Entry</FONT></B></TD>" +
		    "<TD ALIGN=RIGHT VALIGN=BOTTOM WIDTH=8%><B><FONT SIZE=2>Days<BR>Over</FONT></B></TD>" +
		    "<TD ALIGN=RIGHT VALIGN=BOTTOM WIDTH=8%><B><FONT SIZE=2>Amt</FONT></B></TD>" +
		    "<TD ALIGN=RIGHT VALIGN=BOTTOM WIDTH=9%><B><FONT SIZE=2>Balance</FONT></B></TD>" +
		"</TR>" + 
   		"<TR><TD COLSPAN=11><HR></TD><TR>");
	}
	private boolean createTemporaryTables(
			Connection conn,
			String sStartingCustomer, 
			String sEndingCustomer,
			java.sql.Date datStartingDate,
			java.sql.Date datEndingDate,
			boolean bIncludeFullyPaidTransactions
	){
		String SQL = ARSQLs.Drop_Temporary_Activity_Table();
		try {
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				//System.out.println("Error dropping temporary aging table");
				//sWarning = "Error dropping temporary aging table";
				//return false;
			}
		} catch (SQLException e) {
			// Don't choke over this
		}
		SQL = ARSQLs.Create_Temporary_Activity_Table(true);
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			m_sErrorMessage = "Error creating temporary activity table with SQL: " + SQL + " - " + e.getMessage();
			return false;
		}
		
		SQL = ARSQLs.Insert_Transactions_Into_Activity_Table(
				sStartingCustomer, 
				sEndingCustomer, 
				datStartingDate,
				datEndingDate,
				bIncludeFullyPaidTransactions
				);
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			m_sErrorMessage = "Error inserting transactions into activity table with SQL: " + SQL + " - " + e.getMessage();
			return false;
		}

		SQL = ARSQLs.Insert_Lines_Into_Activity_Table(
				sStartingCustomer, 
				sEndingCustomer, 
				datStartingDate,
				datEndingDate,
				bIncludeFullyPaidTransactions
				);
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			m_sErrorMessage = "Error inserting transaction lines into activity table with SQL: " + SQL + " - " + e.getMessage();
			return false;
		}
		
		if(!bIncludeFullyPaidTransactions){
			//Remove any lines which are applied to fully paid transactions:
			SQL = ARSQLs.Remove_Fully_Paid_TransactionLines_From_Activity_Table();
			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (Exception e) {
				m_sErrorMessage = "Error removing fully paid transaction lines into activity table with SQL: " + SQL + " - " + e.getMessage();
				return false;
			}
		}
		//Update the 'days over' on all transactions, based on their 'due' dates: 
		//applied-to documents:
		SQL = ARSQLs.Update_DaysOver_In_Activity_Table();
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			m_sErrorMessage = "Error days over in activity table with SQL: " + SQL + " - " + e.getMessage();
			return false;
		}
		return true;
	}
}

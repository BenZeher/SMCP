package smar;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
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
    	int iLinesPrinted = 1;
    	out.println(SMUtilities.getMasterStyleSheetLink());
    	out.println("<TABLE WIDTH = 100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\">");
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()){
				/*if (iLinesPrinted == 50){
					out.println("</TABLE>");
					printTableHeader(out);
					iLinesPrinted = 0;
				}*/
	    		//Print the header for any new customer:

	    		if (rs.getString(ARSQLs.scustomer).compareToIgnoreCase(sCurrentCustomer) != 0){
	    			//Print the footer, if the record is for a new customer:
		    		if (sCurrentCustomer.compareToIgnoreCase("") != 0){
		    			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTALS_HEADING + "\">");
		    			out.println("<TD COLSPAN = \"9\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"><B>Customer total:</B></TD>");
		    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dCustomerTransactionTotal) + "</B></FONT></TD>");
			    		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dCustomerBalanceTotal) + "</B></FONT></TD>");
		    			out.println("</TR>");
		    			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_BREAK + "\">");
		    			out.println("<TD COLSPAN = \"11\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">&nbsp</TD>");
		    			out.println("</TR>");
		    			
		    			//Reset the customer totals:
		    			dCustomerTransactionTotal = BigDecimal.ZERO;
		    			dCustomerBalanceTotal = BigDecimal.ZERO;
		    			iLinesPrinted = 1;
		    	    	iCustomersPrinted++;
		    		}

	    			//Print the customer header:
		    		String sCustomerNumber = rs.getString(ARSQLs.scustomer);
		    		ARCustomer cust = new ARCustomer(sCustomerNumber);
		    		String sTerms = "N/A";
		    		if (cust.load(conn)){
		    			sTerms = cust.getM_sTerms();
		    		}

		    		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_SUB_HEADING + "\">");
	    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_SUB_HEADING +  "\"><B>" + sCustomerNumber + "</B></TD>");
	    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_SUB_HEADING +  "\"> Current Balance: <B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(cust.getCurrentStoredBalance(conn)) + "</B></TD>"
	    					+ "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_SUB_HEADING +  "\"> Retainage Balance: <B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(cust.getCalculatedRetainageBalance(conn)) + "</B></TD>"
	    					+ "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_SUB_HEADING +  "\"> CR Limit: <B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(ARSQLs.dcreditlimit)) + "</B></TD>"
	    					+ "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_SUB_HEADING +  "\"> Terms: <B>" + sTerms + "</B></TD>"
	    					+ "</TR>");
		    		printTableHeader(out);
	    		}
	    		if( iLinesPrinted%2 == 0) {
		    		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
	    		}else {
		    		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
	    		}
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">&nbsp;&nbsp;" + rs.getString(ARSQLs.sdocappliedto) + "</TD>");
    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">" + getDocumentTypeLabel(rs.getInt(ARSQLs.idoctype)) + "</TD>");
    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">" + rs.getString(ARSQLs.sdocnumber) + "</TD>");
    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">" + Long.toString(rs.getLong(ARSQLs.ldocid)) + "</TD>");
    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">" + clsDateAndTimeConversions.utilDateToString(rs.getDate(ARSQLs.datdocdate),"MM/dd/yyy") + "</TD>");
	    		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">" + clsDateAndTimeConversions.utilDateToString(rs.getDate(ARSQLs.datduedate),"MM/dd/yyy") + "</FONT></TD>");
	    		
	    		String sOrderNumber = rs.getString(ARSQLs.sordernumber).trim();
	    		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\"><A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" 
	    		+ sOrderNumber 
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
	    		+ "\">" + ARUtilities.Fill_In_Empty_String_For_HTML_Cell(sOrderNumber) + "</A></FONT></TD>");
	    		
	    		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">"+ Long.toString(rs.getLong(ARSQLs.loriginalbatchnumber)) 
	    				+ "-" + Long.toString(rs.getLong(ARSQLs.loriginalentrynumber)) + "</FONT></TD>");
	    		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">" + Long.toString(rs.getLong(ARSQLs.ldaysover)) + "</FONT></TD>");
	    		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(ARSQLs.doriginalamt)) + "</FONT></TD>");
	    		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(ARSQLs.dcurrentamt)) + "</FONT></TD>");
    			out.println("</TR>");
    			
    			//Set the totals:
    			dCustomerTransactionTotal = dCustomerTransactionTotal.add(rs.getBigDecimal(ARSQLs.doriginalamt));
    			dCustomerBalanceTotal = dCustomerBalanceTotal.add(rs.getBigDecimal(ARSQLs.dcurrentamt));
    	    	
    	    	//Accumulate the grand totals:
    	    	dGrandTotalTransactionAmount = dGrandTotalTransactionAmount.add(rs.getBigDecimal(ARSQLs.doriginalamt));
    	    	dGrandTotalBalance = dGrandTotalBalance.add(rs.getBigDecimal(ARSQLs.dcurrentamt));
    			//Reset:
    			sCurrentCustomer = rs.getString(ARSQLs.scustomer);
    			iLinesPrinted++;
			}
			rs.close();
			
			//Print the last customer's totals, if at least one customer was listed:
    		if (sCurrentCustomer.compareToIgnoreCase("") != 0){
    			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTALS_HEADING + "\">");
    			out.println("<TD COLSPAN = \"9\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"><B>Customer total:</B></TD>");
    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dCustomerTransactionTotal) + "</B></FONT></TD>");
	    		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dCustomerBalanceTotal) + "</B></FONT></TD>");
    			out.println("</TR>");
    			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_BREAK + "\">");
    			out.println("<TD COLSPAN = \"11\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">&nbsp</TD>");
    			out.println("</TR>");
    	    	iCustomersPrinted++;
    		}

		    //Print the grand totals:
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
		    out.println("<TD colspan=\"11\">&nbsp;</TD>");
		    out.println("</TR>"); 
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
			out.println("<TD COLSPAN=\"9\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Report totals:</B></TD>");
			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dGrandTotalTransactionAmount) + "</B></FONT></TD>");
			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dGrandTotalBalance) + "</B></FONT></TD>");
			out.println("</TR>");
		    out.println("</TABLE>");
		    
		    //Print the legends:
		    out.println("<TABLE WIDTH = 100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\">");
		    out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_HIGHLIGHT + "\">");
		    for (int i = 0;i <= 8; i++){
		    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_CENTER_JUSTIFIED + "\"><I>" + ARDocumentTypes.Get_Document_Type_Label(i) + " = " + getDocumentTypeLabel(i) + "</I></TD>");
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
		/*out.println("<TABLE BORDER=0 WIDTH=100%>");*/
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_SUB_HEADING + "\">" + 
		    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"><U><B>Applied to</B></U></TD>" +
		    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"><U><B>Type</B></U></TD>" +
		    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"><U><B>Doc #</B></U></TD>" +
		    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"><U><B>Doc ID</B></U></TD>" +
		    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"><U><B>Doc. Date</B></U></TD>" +
		    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"><U><B>Due Date</B></U></TD>" +
		    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"><U><B>Order #</B></U></TD>" +
		    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"><U><B>Batch-Entry</B></U></TD>" +
		    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"><U><B>Days Over</B></U></TD>" +
		    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"><U><B>AMT</B></U></TD>" +
		    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\"><U><B>Balance</B></U></TD>" +
		"</TR>");
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

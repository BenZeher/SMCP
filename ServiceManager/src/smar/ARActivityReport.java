package smar;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTablearmatchingline;
import SMDataDefinition.SMTableartransactions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsServletUtilities;
import smcontrolpanel.SMUtilities;

public class ARActivityReport extends java.lang.Object{
    private static final String ARActivityLines = "aractivitylines";
    private static final String sCustomer = "scustomer";
    private static final String sCustomerName = "scustomername";
    private static final String sDocAppliedTo = "sdocappliedto";
    private static final String sSource = "ssource";
    private static final String datDocDate = "datdocdate";
    private static final String iDocType = "idoctype";
    private static final String sDocNumber = "sdocnumber";
    private static final String lDocId = "ldocid";
    private static final String datDueDate = "datduedate";
    private static final String sOrderNum = "sordernumber";
    private static final String dCreditLimit = "dcreditlimit";
    private static final String lOriginalBatchNumber = "loriginalbatchnumber";
    private static final String lOriginalEntryNumber = "loriginalentrynumber";
    private static final String  dOriginalAmmount = "doriginalamt";
    private static final String dCurrentAmmount = "dcurrentamt";
    private static final String lDaysOver = "ldaysover";
    private static final String lAppliedTo = "lappliedto";
    private static final String dApplyToDocCurrentamt = "dapplytodoccurrentamt";
    private static final String lParentTransactionId = "lparenttransactionid";
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
    	String SQL =  "SELECT * FROM "+ ARActivityLines + ""
    			+ " ORDER BY " + sCustomer +", "+ sDocAppliedTo + ", " + sSource + ",  " + datDocDate + "";
    	int iLinesPrinted = 1;
    	out.println(SMUtilities.getMasterStyleSheetLink());
    	out.println("<TABLE WIDTH = 100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">");
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()){
				/*if (iLinesPrinted == 50){
					out.println("</TABLE>");
					printTableHeader(out);
					iLinesPrinted = 0;
				}*/
	    		//Print the header for any new customer:

	    		if (rs.getString(sCustomer).compareToIgnoreCase(sCurrentCustomer) != 0){
	    			//Print the footer, if the record is for a new customer:
		    		if (sCurrentCustomer.compareToIgnoreCase("") != 0){
		    			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\">");
		    			out.println("<TD COLSPAN = \"9\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"><B>Customer total:</B></TD>");
		    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dCustomerTransactionTotal) + "</B></TD>");
			    		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dCustomerBalanceTotal) + "</B></TD>");
		    			out.println("</TR>");
		    			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\">");
		    			out.println("<TD COLSPAN = \"11\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK + "\">&nbsp</TD>");
		    			out.println("</TR>");
		    			
		    			//Reset the customer totals:
		    			dCustomerTransactionTotal = BigDecimal.ZERO;
		    			dCustomerBalanceTotal = BigDecimal.ZERO;
		    			iLinesPrinted = 1;
		    	    	iCustomersPrinted++;
		    		}

	    			//Print the customer header:
		    		String sCustomerNumber = rs.getString(sCustomer);
		    		ARCustomer cust = new ARCustomer(sCustomerNumber);
		    		String sTerms = "N/A";
		    		if (cust.load(conn)){
		    			sTerms = cust.getM_sTerms();
		    		}

		    		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
	    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD +  "\"><B>" + sCustomerNumber + "</B></TD>");
	    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD +  "\"> Current Balance: <B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(cust.getCurrentStoredBalance(conn)) + "</B></TD>"
	    					+ "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD +  "\"> Retainage Balance: <B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(cust.getCalculatedRetainageBalance(conn)) + "</B></TD>"
	    					+ "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD +  "\"> CR Limit: <B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(dCreditLimit)) + "</B></TD>"
	    					+ "<TD COLSPAN = \"7\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD +  "\"> Terms: <B>" + sTerms + "</B></TD>"
	    					+ "</TR>");
		    		printTableHeader(out);
	    		}
	    		if( iLinesPrinted%2 == 0) {
		    		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
	    		}else {
		    		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
	    		}
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">&nbsp;&nbsp;" + rs.getString(sDocAppliedTo) + "</TD>");
    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">" + getDocumentTypeLabel(rs.getInt(iDocType)) + "</TD>");
    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">" + rs.getString(sDocNumber) + "</TD>");
    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">" + Long.toString(rs.getLong(lDocId)) + "</TD>");
    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">" + clsDateAndTimeConversions.utilDateToString(rs.getDate(datDocDate),"MM/dd/yyy") + "</TD>");
	    		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">" + clsDateAndTimeConversions.utilDateToString(rs.getDate(datDueDate),"MM/dd/yyy") + "</TD>");
	    		
	    		String sOrderNumber = rs.getString(sOrderNum).trim();
	    		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\"><A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" 
	    		+ sOrderNumber 
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
	    		+ "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sOrderNumber) + "</A></TD>");
	    		
	    		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">"+ Long.toString(rs.getLong(lOriginalBatchNumber)) 
	    				+ "-" + Long.toString(rs.getLong(lOriginalEntryNumber)) + "</TD>");
	    		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">" + Long.toString(rs.getLong(lDaysOver)) + "</TD>");
	    		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(dOriginalAmmount)) + "</TD>");
	    		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(dCurrentAmmount)) + "</TD>");
    			out.println("</TR>");
    			
    			//Set the totals:
    			dCustomerTransactionTotal = dCustomerTransactionTotal.add(rs.getBigDecimal(dOriginalAmmount));
    			dCustomerBalanceTotal = dCustomerBalanceTotal.add(rs.getBigDecimal(dCurrentAmmount));
    	    	
    	    	//Accumulate the grand totals:
    	    	dGrandTotalTransactionAmount = dGrandTotalTransactionAmount.add(rs.getBigDecimal(dOriginalAmmount));
    	    	dGrandTotalBalance = dGrandTotalBalance.add(rs.getBigDecimal(dCurrentAmmount));
    			//Reset:
    			sCurrentCustomer = rs.getString(sCustomer);
    			iLinesPrinted++;
			}
			rs.close();
			
			//Print the last customer's totals, if at least one customer was listed:
    		if (sCurrentCustomer.compareToIgnoreCase("") != 0){
    			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\">");
    			out.println("<TD COLSPAN = \"9\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"><B>Customer total:</B></TD>");
    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dCustomerTransactionTotal) + "</B></TD>");
	    		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dCustomerBalanceTotal) + "</B></TD>");
    			out.println("</TR>");
    			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\">");
    			out.println("<TD COLSPAN = \"11\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK + "\">&nbsp</TD>");
    			out.println("</TR>");
    	    	iCustomersPrinted++;
    		}

		    //Print the grand totals:
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
		    out.println("<TD colspan=\"11\">&nbsp;</TD>");
		    out.println("</TR>"); 
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
			out.println("<TD COLSPAN=\"9\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Report totals:</B></TD>");
			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dGrandTotalTransactionAmount) + "</B></TD>");
			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dGrandTotalBalance) + "</B></TD>");
			out.println("</TR>");
		    out.println("</TABLE>");
		    
		    //Print the legends:
		    out.println("<TABLE WIDTH = 100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">");
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
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">" + 
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
		String SQL = "DROP TEMPORARY TABLE " + ARActivityLines;
		try {
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				//System.out.println("Error dropping temporary aging table");
				//sWarning = "Error dropping temporary aging table";
				//return false;
			}
		} catch (SQLException e) {
			// Don't choke over this
		}
		
		SQL = "CREATE TEMPORARY TABLE " + ARActivityLines +" ("
				+ sCustomer + " varchar(" + SMTablearcustomer.sCustomerNumberLength + ") NOT NULL default '',"
				+ sCustomerName +" varchar(" + SMTablearcustomer.sCustomerNameLength + ") NOT NULL default '',"
				+ lDocId + " int(11) NOT NULL default '0',"
				+ iDocType + " int(11) NOT NULL default '0',"
				+ sDocNumber + " varchar(" + SMTableartransactions.sdocnumberlength + ") NOT NULL default '',"
				+ datDocDate +" datetime NOT NULL default '0000-00-00 00:00:00',"
				+ datDueDate+ " datetime NOT NULL default '0000-00-00 00:00:00',"
				+ dOriginalAmmount + " decimal(17,2) NOT NULL default '0.00',"
				+ dCurrentAmmount +  " decimal(17,2) NOT NULL default '0.00',"
				+ sOrderNum + " varchar(22) NOT NULL default '',"
				+ sSource + " varchar(7) NOT NULL default '',"
				+ lAppliedTo + " int(11) NOT NULL default '0',"
				+ sDocAppliedTo + " varchar(" + SMTableartransactions.sdocnumberlength + ") NOT NULL default '',"
				+ lOriginalBatchNumber +" int(11) NOT NULL default '0',"
				+ lOriginalEntryNumber + " int(11) NOT NULL default '0',"
				+ lDaysOver +"  int(11) NOT NULL default '0',"
				+ dCreditLimit + " decimal(17,2) NOT NULL default '0.00',"
				+ dApplyToDocCurrentamt + " decimal(17,2) NOT NULL default '0.00',"
				+ lParentTransactionId + " int(11) NOT NULL default '0',"
				+ "KEY customerkey (" + sCustomer + "),"
				+ "KEY appliedtokey (" + lAppliedTo + "),"
				+ "KEY docnumberkey (" + sDocNumber + "),"
				+ "KEY parenttransactionkey ("  + lParentTransactionId + ")"
				+ ")";

		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			m_sErrorMessage = "Error creating temporary activity table with SQL: " + SQL + " - " + e.getMessage();
			return false;
		}

		
		SQL = "INSERT INTO " +  ARActivityLines +" ("
				+ sCustomer + " ,"
				+ lDocId + " ,"
				+ iDocType + " ,"
				+ sDocNumber + " ,"
				+ datDocDate +" ,"
				+ datDueDate+ " ,"
				+ dOriginalAmmount + " ,"
				+ dCurrentAmmount +  " ,"
				+ sOrderNum + " ,"
				+ sSource + " ,"
				+ lAppliedTo + " ,"
				+ sDocAppliedTo + " ,"
				+ lOriginalBatchNumber +" ,"
				+ lOriginalEntryNumber + " ,"
				+ lDaysOver +" ,"
				+ dApplyToDocCurrentamt + " ,"
				+ lParentTransactionId + " ,"
				+ sCustomerName +" ,"
				+ dCreditLimit
				
				+ ") SELECT"
				+ " " + SMTableartransactions.spayeepayor
				+ ", " + SMTableartransactions.lid
				+ ", " + SMTableartransactions.idoctype
				+ ", " + SMTableartransactions.sdocnumber
				+ ", " + SMTableartransactions.datdocdate
				+ ", " + SMTableartransactions.datduedate
				+ ", " + SMTableartransactions.doriginalamt
				+ ", " + SMTableartransactions.dcurrentamt
				+ ", " + SMTableartransactions.sordernumber
				+ ", 'CONTROL'"
				+ ", " + SMTableartransactions.lid
				+ ", " + SMTableartransactions.sdocnumber
				+ ", " + SMTableartransactions.loriginalbatchnumber
				+ ", " + SMTableartransactions.loriginalentrynumber
				+ ", 0"
				+ ", " + SMTableartransactions.dcurrentamt + " AS dapplytocurramt"
				+ ", " + SMTableartransactions.lid
				+ ", IF(" + SMTablearcustomer.sCustomerName + " IS NULL, '(NOT FOUND)', " 
					+ SMTablearcustomer.sCustomerName + ") AS scustomername"
				+ ", IF(" + SMTablearcustomer.dCreditLimit + " IS NULL, 0.00, " 
					+ SMTablearcustomer.dCreditLimit + ") AS dcreditlimit"
			+ " FROM " + SMTableartransactions.TableName + " LEFT JOIN " + SMTablearcustomer.TableName
			+ " ON " + SMTableartransactions.TableName + "." + SMTableartransactions.spayeepayor + " = "
			+ SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerNumber
			+ " WHERE ("
				+ "(" + SMTableartransactions.spayeepayor + ">='" + sStartingCustomer + "')"
				+ " AND (" + SMTableartransactions.spayeepayor + "<='" + sEndingCustomer + "')"
				+ " AND (" + SMTableartransactions.datdocdate + ">='" + clsDateAndTimeConversions.utilDateToString(datStartingDate, "yyyy-MM-dd") + "')"
				+ " AND (" + SMTableartransactions.datdocdate + "<='" + clsDateAndTimeConversions.utilDateToString(datEndingDate, "yyyy-MM-dd") + "')";
				if(!bIncludeFullyPaidTransactions){
					SQL = SQL + " AND (" + SMTableartransactions.dcurrentamt + " != 0.00)";
				}
			SQL = SQL + ")"
			;
			
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			m_sErrorMessage = "Error inserting transactions into activity table with SQL: " + SQL + " - " + e.getMessage();
			return false;
		}

		SQL ="INSERT INTO " +  ARActivityLines +" ("
				+ sCustomer + " ,"
				+ lDocId + " ,"
				+ sDocNumber + " ,"
				+ datDocDate +" ,"
				+ datDueDate+ " ,"
				+ dOriginalAmmount + " ,"
				+ dCurrentAmmount +  " ,"
				+ sOrderNum + " ,"
				+ sSource + " ,"
				+ lAppliedTo + " ,"
				+ sDocAppliedTo + " ,"
				+ dApplyToDocCurrentamt + " ,"
				+ lParentTransactionId + " ,"
				+ sCustomerName +" ,"
				+ dCreditLimit + " ,"
				+ lOriginalBatchNumber +" ,"
				+ lOriginalEntryNumber 
				
			+ ") SELECT"
				+ " " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.spayeepayor
				+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.lid
				+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.sdocnumber
				+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.dattransactiondate
				+ ", " + SMTableartransactions.TableName + "." + SMTableartransactions.datduedate
				//Applied amounts have the same sign as the apply-to amount, and so they must be negated:
				+ ", -1 * " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.damount
				+ ", -1 * " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.damount
				+ ", ''"
				+ ", 'DIST'"
				+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.ldocappliedtoid
				+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.sapplytodoc
				+ ", " + SMTableartransactions.TableName + "." + SMTableartransactions.dcurrentamt
				+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.lparenttransactionid
				+ ", IF(" + SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerName + " IS NULL, '(NOT FOUND)', " 
					+ SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerName + ") AS scustomername"
				+ ", IF(" + SMTablearcustomer.TableName + "." + SMTablearcustomer.dCreditLimit + " IS NULL, 0.00, " 
					+ SMTablearcustomer.TableName + "." + SMTablearcustomer.dCreditLimit + ") AS dcreditlimit"
				+ ", " + SMTableartransactions.TableName + "." + SMTableartransactions.loriginalbatchnumber
				+ ", " + SMTableartransactions.TableName + "." + SMTableartransactions.loriginalentrynumber
			+ " FROM " + SMTablearmatchingline.TableName + " LEFT JOIN " + SMTableartransactions.TableName
			+ " ON " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.ldocappliedtoid + " = "
			+  SMTableartransactions.TableName + "." + SMTableartransactions.lid
			+ " LEFT JOIN " + SMTablearcustomer.TableName + " ON " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.spayeepayor 
			+ "=" + SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerNumber
			+ " WHERE"
				+ " " + SMTableartransactions.TableName + "." + SMTableartransactions.spayeepayor + ">='" 
					+ sStartingCustomer + "'"
				+ " AND " + SMTableartransactions.TableName + "." + SMTableartransactions.spayeepayor + "<='" 
					+ sEndingCustomer + "'"
				+ " AND " + SMTableartransactions.TableName + "." + SMTableartransactions.datdocdate + ">='" 
					+ clsDateAndTimeConversions.utilDateToString(datStartingDate, "yyyy-MM-dd") + "'"
				+ " AND " + SMTableartransactions.TableName + "." + SMTableartransactions.datdocdate + "<='" 
					+ clsDateAndTimeConversions.utilDateToString(datEndingDate, "yyyy-MM-dd") + "'"
			;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			m_sErrorMessage = "Error inserting transaction lines into activity table with SQL: " + SQL + " - " + e.getMessage();
			return false;
		}
		
		if(!bIncludeFullyPaidTransactions){
			//Remove any lines which are applied to fully paid transactions:
			SQL ="DELETE FROM " + ARActivityLines
					+ " WHERE ("
					+ "(" + dCurrentAmmount +  " = 0.00)"
				+ ")"
				;
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
		SQL = "UPDATE " + ARActivityLines + " SET " 
				+ lDaysOver + "  = IF((TO_DAYS(NOW()) - TO_DAYS(" + datDueDate + "))>0,(TO_DAYS(NOW()) - TO_DAYS(" + datDueDate + ")),0)"
				+ " WHERE ("
					+ "(" + sSource + " = 'CONTROL')"
					+ " AND (" + dCurrentAmmount + " != 0.00)"
				+ ")"
				;
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

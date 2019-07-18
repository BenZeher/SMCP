package smar;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTablearmatchingline;
import SMDataDefinition.SMTableartransactionline;
import SMDataDefinition.SMTableartransactions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import smcontrolpanel.SMUtilities;

public class ARMiscCashReport extends java.lang.Object{
	private static final String ARMiscCashLines = "armisccashlines";
	private static final String iDocType = "idoctype";
	private static final String sDocAppliedTo = "sdocappliedto";
	private static final String sDocNumber = "sdocnumber";
	private static final String lDocId = "ldocid";
	private static final String datDocDate = "datdocdate";
	private static final String datDueDate = "datduedate";
	private static final String lOriginalBatchNumber = "loriginalbatchnumber";
	private static final String lOriginalEntryNumber = "loriginalentrynumber";
	private static final String  dOriginalAmmount = "doriginalamt";
	private static final String dCurrentAmmount = "dcurrentamt";
	private static final String sDescription = "sdesc";
	private static final String sSource = "ssource";
	private static final String lAppliedTo = "lappliedto";
	private static final String dApplyToDocCurrentAmt = "dapplytodoccurrentamt";
	private static final String lParentTransactionId = "lparenttransactionid";
	
	private String m_sErrorMessage;
	
	ARMiscCashReport(
			){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			PrintWriter out
			){

    	//variables for customer total calculations
    	BigDecimal dMiscCashOriginalTotal = new BigDecimal(0);
    	BigDecimal dMiscCashCurrentTotal = new BigDecimal(0);

    	if(!createTemporaryTables(conn)){
    		return false;
    	}
    	out.println(SMUtilities.getMasterStyleSheetLink());
    	//print out the column headers.
    	out.println("<TABLE WIDTH=100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\">"); 
    	int iPrintTransactionsIn = 0;
    	if (iPrintTransactionsIn == 0){
    		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">"  + 
			    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"> <B>Applied to</B></TD>" +
			    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"> <B>Type</B></TD>" +
			    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"> <B>Doc #</B></TD>" +
			    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"> <B>Doc ID</B></TD>" +
			    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"> <B>Doc Date</B></TD>" +
			    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"> <B>Due Date</B></TD>" +
			    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"> <B>Batch-Entry</B></TD>" +
			    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"> <B>Amt</B></TD>" +
			    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"> <B>Balance</B></TD>" +
			    "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"> <B>Description</B></TD>" +
			"</TR>" );
    	}
    	String SQL = "SELECT * FROM armisccashlines ORDER BY sdocappliedto, ssource, datdocdate";
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()){
				if(iPrintTransactionsIn%2 ==0) {
		    		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">" );
				}else {
		    		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">" );
				}
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">&nbsp;&nbsp;" + rs.getString(sDocAppliedTo) + "</TD>");
    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">" + getDocumentTypeLabel(rs.getInt(iDocType)) + "</TD>");
    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">" + rs.getString(sDocNumber) + "</TD>");
    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">" + Long.toString(rs.getLong(lDocId)) + "</TD>");
    			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">" + clsDateAndTimeConversions.utilDateToString(rs.getDate(datDocDate),"MM/dd/yyy") + "</TD>");
	    		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">" + clsDateAndTimeConversions.utilDateToString(rs.getDate(datDueDate),"MM/dd/yyy") + "</TD>");
	    		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">"+ Long.toString(rs.getLong(lOriginalBatchNumber)) 
				+ "-" + Long.toString(rs.getLong(lOriginalEntryNumber)) + "</TD>");
	    		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(dOriginalAmmount)) + "</TD>");
	    		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(dCurrentAmmount)) + "</TD>");
	    		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER +  "\">" + rs.getString(sDescription) + "</TD>");
    			out.println("</TR>");
    			iPrintTransactionsIn++;
    			//Set the totals:
    			dMiscCashOriginalTotal = dMiscCashOriginalTotal.add(rs.getBigDecimal("doriginalamt"));
    			dMiscCashCurrentTotal = dMiscCashCurrentTotal.add(rs.getBigDecimal("dcurrentamt"));
    	    	
			}
			rs.close();
		    //Print the grand totals:
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
		    out.println("<TD colspan=\"10\">&nbsp;</TD>");
			out.println("</TR>");
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
			out.println("<TD COLSPAN=\"7\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Report totals:</B></TD>");
			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dMiscCashOriginalTotal) + "</B></TD>");
			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dMiscCashCurrentTotal) + "</B></TD>");
		    out.println("<TD colspan=\"1\">&nbsp;</TD>");
			out.println("</TR>");
		    out.println("</TABLE>");
		    
		    //Print the legends:
		    out.println("<TABLE WIDTH = 100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\">");
		    out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_HIGHLIGHT + "\">");
		    for (int i = 0;i <= 4; i++){
		    	out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_CENTER_JUSTIFIED + "\"><I>" + ARDocumentTypes.Get_Document_Type_Label(i) + " = " + getDocumentTypeLabel(i) + "</I></TD>");
		    }
		    out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_CENTER_JUSTIFIED + "\"><I>" + ARDocumentTypes.Get_Document_Type_Label(6) + " = " + getDocumentTypeLabel(6) + "</I></TD>");
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
	private boolean createTemporaryTables(
			Connection conn
	){
		
		String SQL;
		
		try{
			SQL = "DROP TEMPORARY TABLE " + ARMiscCashLines;
			try {
				if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
					//System.out.println("Error dropping temporary aging table");
					//sWarning = "Error dropping temporary aging table";
					//return false;
				}
			} catch (Exception e) {
				// Don't choke over this
			}
		
			SQL = "CREATE TEMPORARY TABLE " + ARMiscCashLines +" ("
				+ lDocId + " int(11) NOT NULL default '0',"
				+ iDocType + " int(11) NOT NULL default '0',"
				+ sDocNumber + " varchar(" + SMTableartransactions.sdocnumberlength + ") NOT NULL default '',"
				+ datDocDate +" datetime NOT NULL default '0000-00-00 00:00:00',"
				+ datDueDate+ " datetime NOT NULL default '0000-00-00 00:00:00',"
				+ dOriginalAmmount + " decimal(17,2) NOT NULL default '0.00',"
				+ dCurrentAmmount +  " decimal(17,2) NOT NULL default '0.00',"
				+ sSource + " varchar(7) NOT NULL default '',"
				+ lAppliedTo + " int(11) NOT NULL default '0',"
				+ sDocAppliedTo + " varchar(" + SMTableartransactions.sdocnumberlength + ") NOT NULL default '',"
				+ lOriginalBatchNumber +" int(11) NOT NULL default '0',"
				+ lOriginalEntryNumber + " int(11) NOT NULL default '0',"
				+ dApplyToDocCurrentAmt + " decimal(17,2) NOT NULL default '0.00',"
				+ lParentTransactionId + " int(11) NOT NULL default '0',"
				+ sDescription + " varchar(" + SMTableartransactions.sdocdescriptionlength + ") NOT NULL default '',"
				+ "KEY appliedtokey (" + lAppliedTo + "),"
				+ "KEY docnumberkey (" + sDocNumber + "),"
				+ "KEY parenttransactionkey ("  + lParentTransactionId + ")"
				+ ") ENGINE = InnoDb"
				;
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				System.out.println("Error creating temporary misc cash lines table");
				m_sErrorMessage = "Error creating temporary misc cash lines table";
				return false;
			}
			SQL = "INSERT INTO " + ARMiscCashLines + " ("
					+ " " + lDocId +" ,"
					+ " " + iDocType +" ,"
					+ " " + sDocNumber +" ,"
					+ " " + datDocDate +" ,"
					+ " " + datDueDate +" ,"
	    			+ " " + dOriginalAmmount +" ,"
	    			+ " " + dCurrentAmmount +" ,"
	    			+ " " + sSource +" ,"
	    			+ " " + lAppliedTo +" ,"
	    			+ " " + sDocAppliedTo +" ,"
	    			+ " " + lOriginalBatchNumber +" ,"
	    			+ " " + lOriginalEntryNumber +" ,"
	    			+ " " + dApplyToDocCurrentAmt +" ,"
	    			+ " " + lParentTransactionId +" ,"
		    		+ " " + sDescription
				
				+ ") SELECT"
				+ " " + SMTableartransactions.lid
				+ ", " + SMTableartransactions.idoctype
				+ ", " + SMTableartransactions.sdocnumber
				+ ", " + SMTableartransactions.datdocdate
				+ ", " + SMTableartransactions.datduedate
				+ ", " + SMTableartransactions.doriginalamt
				+ ", " + SMTableartransactions.dcurrentamt
				+ ", 'CONTROL'"
				+ ", " + SMTableartransactions.lid
				+ ", " + SMTableartransactions.sdocnumber
				+ ", " + SMTableartransactions.loriginalbatchnumber
				+ ", " + SMTableartransactions.loriginalentrynumber
				+ ", " + SMTableartransactions.dcurrentamt + " AS dapplytocurramt"
				+ ", " + SMTableartransactions.lid
				+ ", " + SMTableartransactions.sdocdescription
			+ " FROM " + SMTableartransactions.TableName
			+ " WHERE ("
				+ "(" + SMTableartransactions.idoctype + " = " + ARDocumentTypes.MISCRECEIPT_STRING + ")"
			+ ")"
			;
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				System.out.println("Error inserting transactions into misc cash lines table");
				m_sErrorMessage = "Error inserting transactions into misc cash lines table";
				return false;
			}
			SQL = "INSERT INTO " + ARMiscCashLines + " ("
				+ " " + lDocId +" ,"
				+ " " + iDocType +" ,"
				+ " " + sDocNumber +" ,"
				+ " " + datDocDate +" ,"
				+ " " + datDueDate +" ,"
    			+ " " + dOriginalAmmount +" ,"
    			+ " " + dCurrentAmmount +" ,"
    			+ " " + sSource +" ,"
    			+ " " + lAppliedTo +" ,"
    			+ " " + sDocAppliedTo +" ,"
    			+ " " + dApplyToDocCurrentAmt +" ,"
    			+ " " + lParentTransactionId +" ,"
    			+ " " + lOriginalBatchNumber +" ,"
    			+ " " + lOriginalEntryNumber +" ,"
	    		+ " " + sDescription
	    			
			+ ") SELECT"
				+ " " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.lid
				+ ", " + "APPLYFROM." + SMTableartransactions.idoctype
				+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.sdocnumber
				+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.dattransactiondate
				+ ", " + SMTableartransactions.TableName + "." + SMTableartransactions.datduedate
				//Applied amounts have the same sign as the apply-to amount, and so they must be negated:
				+ ", -1 * " + SMTablearmatchingline.TableName + "." + SMTableartransactionline.damount
				+ ", -1 * " + SMTablearmatchingline.TableName + "." + SMTableartransactionline.damount
				+ ", 'DIST'"
				+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.ldocappliedtoid
				+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.sapplytodoc
				+ ", " + SMTableartransactions.TableName + "." + SMTableartransactions.dcurrentamt
				+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.lparenttransactionid
				+ ", " + SMTableartransactions.TableName + "." + SMTableartransactions.loriginalbatchnumber
				+ ", " + SMTableartransactions.TableName + "." + SMTableartransactions.loriginalentrynumber
				+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.sdescription
				
			+ " FROM " + SMTablearmatchingline.TableName 
				+ ", " + SMTableartransactions.TableName
				+ ", " + SMTableartransactions.TableName + " AS APPLYFROM"
			+ " WHERE ("
				+ "(" + SMTableartransactions.TableName + "." + SMTableartransactions.idoctype + " = " 
					+ ARDocumentTypes.MISCRECEIPT_STRING + ")"
				//Link the tables:
				+ " AND (" + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.ldocappliedtoid + "="
					+ SMTableartransactions.TableName + "." + SMTableartransactions.lid + ")"

				+ " AND (" + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.sdocnumber + "="
					+ "APPLYFROM." + SMTableartransactions.sdocnumber + ")"

				+ " AND (" + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.spayeepayor + "="
					+ "APPLYFROM." + SMTableartransactions.spayeepayor + ")"
					
				+ ")"
			;

			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				System.out.println("Error inserting transaction lines into misc cash lines table");
				m_sErrorMessage = "Error inserting transaction lines into misc cash lines table";
				return false;
			}

		}catch(SQLException e){
			m_sErrorMessage = "Unable to create temporary tables - " + e.getMessage();
			return false;
		}
		return true;
	}
}

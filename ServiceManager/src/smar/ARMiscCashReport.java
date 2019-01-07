package smar;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import SMDataDefinition.SMTablearmatchingline;
import SMDataDefinition.SMTableartransactionline;
import SMDataDefinition.SMTableartransactions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;

public class ARMiscCashReport extends java.lang.Object{

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
    	
    	//print out the column headers.
    	out.println("<TABLE BORDER=0 WIDTH=100%>");
    	int iPrintTransactionsIn = 0;
    	if (iPrintTransactionsIn == 0){
    		out.println("<TR>" + 
			    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=9%><B><FONT SIZE=2>Applied to</FONT></B></TD>" +
			    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=3%><B><FONT SIZE=2>Type</FONT></B></TD>" +
			    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=9%><B><FONT SIZE=2>Doc #</FONT></B></TD>" +
			    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=7%><B><FONT SIZE=2>Doc ID</FONT></B></TD>" +
			    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=7%><B><FONT SIZE=2>Doc. Date</FONT></B></TD>" + 
			    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=7%><B><FONT SIZE=2>Due Date</FONT></B></TD>" +
			    "<TD ALIGN=RIGHT VALIGN=BOTTOM WIDTH=8%><B><FONT SIZE=2>Batch-Entry</FONT></B></TD>" +
			    "<TD ALIGN=RIGHT VALIGN=BOTTOM WIDTH=10%><B><FONT SIZE=2>Amt</FONT></B></TD>" +
			    "<TD ALIGN=RIGHT VALIGN=BOTTOM WIDTH=11%><B><FONT SIZE=2>Balance</FONT></B></TD>" +
			    "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=31%><B><FONT SIZE=2>Description</FONT></B></TD>" +
			"</TR>" + 
	   		"<TR><TD COLSPAN=10><HR></TD><TR>");
    	}
    	String SQL = "SELECT * FROM armisccashlines ORDER BY sdocappliedto, ssource, datdocdate";
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()){
    			out.println("<TR>");
				out.println("<TD ALIGN=LEFT><FONT SIZE=2>&nbsp;&nbsp;" + rs.getString("sdocappliedto") + "</FONT></TD>");
    			out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + getDocumentTypeLabel(rs.getInt("idoctype")) + "</FONT></TD>");
    			out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + rs.getString("sdocnumber") + "</FONT></TD>");
    			out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + Long.toString(rs.getLong("ldocid")) + "</FONT></TD>");
    			out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + clsDateAndTimeConversions.utilDateToString(rs.getDate("datdocdate"),"MM/dd/yyy") + "</FONT></TD>");
	    		out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + clsDateAndTimeConversions.utilDateToString(rs.getDate("datduedate"),"MM/dd/yyy") + "</FONT></TD>");
	    		out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + Long.toString(rs.getLong("loriginalbatchnumber")) 
	    				+ "-" + Long.toString(rs.getLong("loriginalentrynumber")) + "</FONT></TD>");
	    		out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal("doriginalamt")) + "</FONT></TD>");
	    		out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal("dcurrentamt")) + "</FONT></TD>");
	    		out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + rs.getString("sdesc") + "</FONT></TD>");
    			out.println("</TR>");
    			
    			//Set the totals:
    			dMiscCashOriginalTotal = dMiscCashOriginalTotal.add(rs.getBigDecimal("doriginalamt"));
    			dMiscCashCurrentTotal = dMiscCashCurrentTotal.add(rs.getBigDecimal("dcurrentamt"));
    	    	
			}
			rs.close();
		    //Print the grand totals:
		    out.println("<TD colspan=\"7\">&nbsp;</TD>");
		    out.println("<TD COLSPAN=2><HR></TD>");
			out.println("<TR>");
			out.println("<TD ALIGN=RIGHT colspan=\"7\"><B><FONT SIZE=2>Report totals:</FONT></B></TD>");
			out.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dMiscCashOriginalTotal) + "</B></FONT></TD>");
			out.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dMiscCashCurrentTotal) + "</B></FONT></TD>");
			out.println("</TR>");
		    out.println("</TABLE>");
		    
		    //Print the legends:
		    out.println("<TABLE BORDER=0 WIDTH=100%>");
		    out.println("<TR>");
	    	out.println("<TD><FONT SIZE=2><I>" + ARDocumentTypes.Get_Document_Type_Label(0) + " = " + getDocumentTypeLabel(0) + "</I></FONT></TD>");
	    	out.println("<TD><FONT SIZE=2><I>" + ARDocumentTypes.Get_Document_Type_Label(1) + " = " + getDocumentTypeLabel(1) + "</I></FONT></TD>");
	    	out.println("<TD><FONT SIZE=2><I>" + ARDocumentTypes.Get_Document_Type_Label(2) + " = " + getDocumentTypeLabel(2) + "</I></FONT></TD>");
	    	out.println("<TD><FONT SIZE=2><I>" + ARDocumentTypes.Get_Document_Type_Label(3) + " = " + getDocumentTypeLabel(3) + "</I></FONT></TD>");
	    	out.println("<TD><FONT SIZE=2><I>" + ARDocumentTypes.Get_Document_Type_Label(4) + " = " + getDocumentTypeLabel(4) + "</I></FONT></TD>");
	    	out.println("<TD><FONT SIZE=2><I>" + ARDocumentTypes.Get_Document_Type_Label(6) + " = " + getDocumentTypeLabel(6) + "</I></FONT></TD>");
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
			SQL = "DROP TEMPORARY TABLE armisccashlines";
			try {
				if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
					//System.out.println("Error dropping temporary aging table");
					//sWarning = "Error dropping temporary aging table";
					//return false;
				}
			} catch (Exception e) {
				// Don't choke over this
			}
		
			SQL = "CREATE TEMPORARY TABLE armisccashlines ("
				+ "ldocid int(11) NOT NULL default '0',"
				+ "idoctype int(11) NOT NULL default '0',"
				+ "sdocnumber varchar(" + SMTableartransactions.sdocnumberlength + ") NOT NULL default '',"
				+ "datdocdate datetime NOT NULL default '0000-00-00 00:00:00',"
				+ "datduedate datetime NOT NULL default '0000-00-00 00:00:00',"
				+ "doriginalamt decimal(17,2) NOT NULL default '0.00',"
				+ "dcurrentamt decimal(17,2) NOT NULL default '0.00',"
				+ "ssource varchar(7) NOT NULL default '',"
				+ "lappliedto int(11) NOT NULL default '0',"
				+ "sdocappliedto varchar(" + SMTableartransactions.sdocnumberlength + ") NOT NULL default '',"
				+ "loriginalbatchnumber int(11) NOT NULL default '0',"
				+ "loriginalentrynumber int(11) NOT NULL default '0',"
				+ "dapplytodoccurrentamt decimal(17,2) NOT NULL default '0.00',"
				+ "lparenttransactionid int(11) NOT NULL default '0',"
				+ "sdesc varchar(" + SMTableartransactions.sdocdescriptionlength + ") NOT NULL default '',"
				+ "KEY appliedtokey (lappliedto),"
				+ "KEY docnumberkey (sdocnumber),"
				+ "KEY parenttransactionkey (lparenttransactionid)"
				+ ") ENGINE = InnoDb"
				;
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				System.out.println("Error creating temporary misc cash lines table");
				m_sErrorMessage = "Error creating temporary misc cash lines table";
				return false;
			}
			SQL = "INSERT INTO armisccashlines ("
				+ " ldocid,"
				+ " idoctype,"
				+ " sdocnumber,"
				+ " datdocdate,"
				+ " datduedate,"
				+ " doriginalamt,"
				+ " dcurrentamt,"
				+ " ssource,"
				+ " lappliedto,"
				+ " sdocappliedto,"
				+ " loriginalbatchnumber,"
				+ " loriginalentrynumber,"
				+ " dapplytodoccurrentamt,"
				+ " lparenttransactionid,"
				+ " sdesc"
				
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
			SQL = "INSERT INTO armisccashlines ("
				+ " ldocid,"
				+ " idoctype,"
				+ " sdocnumber,"
				+ " datdocdate,"
				+ " datduedate,"
				+ " doriginalamt,"
				+ " dcurrentamt,"
				+ " ssource,"
				+ " lappliedto,"
				+ " sdocappliedto,"
				+ " dapplytodoccurrentamt,"
				+ " lparenttransactionid,"
				+ " loriginalbatchnumber,"
				+ " loriginalentrynumber,"
				+ " sdesc"
				
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

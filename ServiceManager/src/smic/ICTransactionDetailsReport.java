package smic;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletContext;

import smar.ARUtilities;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableictransactiondetails;
import SMDataDefinition.SMTableictransactions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;

public class ICTransactionDetailsReport extends java.lang.Object{

	private String m_sErrorMessage;
	private boolean bDebugMode = false;
	
	public ICTransactionDetailsReport(
			){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			String sTransactionID,
			String sDBID,
			String sUserID,
			PrintWriter out,
			ServletContext context,
			String sLicenseModuleLevel
			){
	
		String SQL = 
			"SELECT " + SMTableictransactions.TableName + ".*"
			+ ", " + SMTableictransactiondetails.TableName + ".*"
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sItemDescription
			+ " FROM (" + SMTableictransactions.TableName 
			+ " LEFT JOIN " + SMTableicitems.TableName + " ON "
			+ SMTableictransactions.TableName + "." + SMTableictransactions.sitemnumber
			+ " = " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + ")"
			+ " LEFT JOIN " + SMTableictransactiondetails.TableName
			+ " ON " + SMTableictransactions.TableName + "." + SMTableictransactions.lid
			+ " = " + SMTableictransactiondetails.TableName + "." + SMTableictransactiondetails.ltransactionid
			+ " WHERE ("
				+ "(" + SMTableictransactions.TableName + "." 
				+ SMTableictransactions.lid + " = " + sTransactionID
				+ ")"
			+ ")"
			+ " ORDER BY"
			+ " " + SMTableictransactiondetails.TableName + "." + SMTableictransactiondetails.ldetailnumber
			
			;
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".processReport - main SQL = " + SQL);
		}
    	
		//Check permissions for viewing items:
		boolean bViewItemPermitted = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.ICDisplayItemInformation,
			sUserID,
			conn,
			sLicenseModuleLevel);
		
		BigDecimal bdTotalCost = new BigDecimal(0);
		BigDecimal bdTotalQty = new BigDecimal(0); 
    	try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			boolean bHeaderPrinted = false;
			while(rs.next()){
				
				if (!bHeaderPrinted){
					try {
						printTransactionInfo(rs, out, bViewItemPermitted, context, sDBID);
					} catch (SQLException e) {
						out.println("Error reading transaction info - " + e.getMessage() + "<BR>");
						return false;
					}
					bHeaderPrinted = true;
					printDetailsHeader(out);
				}
				//Now print each of the cost details:
				//Cost bucket ID:
				out.println("<TD ALIGN=RIGHT>");
				out.println("<FONT SIZE=2>" + Long.toString(
					rs.getLong(
					SMTableictransactiondetails.TableName + "." + SMTableictransactiondetails.lcostbucketid)));
				out.println("</FONT></TD>");
				
				//Creation date:
				out.println("<TD>");
				out.println("<FONT SIZE=2>" + clsDateAndTimeConversions.resultsetDateStringToString(
					rs.getString(SMTableictransactiondetails.TableName 
					+ "." + SMTableictransactiondetails.dattimecostbucketcreation)));
				out.println("</FONT></TD>");
				
				//Remark:
				out.println("<TD>");
				out.println("<FONT SIZE=2>" + rs.getString(SMTableictransactiondetails.TableName 
					+ "." + SMTableictransactiondetails.scostbucketremark));
				out.println("</FONT></TD>");
				
				BigDecimal bdQtyBeforeTransaction = new BigDecimal(0);
				BigDecimal bdCostBeforeTransaction = new BigDecimal(0);
				BigDecimal bdQtyAfterTransaction = new BigDecimal(0);
				BigDecimal bdCostAfterTransaction = new BigDecimal(0);
				BigDecimal bdQty = new BigDecimal(0);
				BigDecimal bdCost = new BigDecimal(0);
				
				bdQtyBeforeTransaction = rs.getBigDecimal(SMTableictransactiondetails.TableName + "." 
						+ SMTableictransactiondetails.bdcostbucketqtybeforetrans);
				if (bdQtyBeforeTransaction == null){
					bdQtyBeforeTransaction = BigDecimal.ZERO;
				}
				bdCostBeforeTransaction = rs.getBigDecimal(SMTableictransactiondetails.TableName + "." 
						+ SMTableictransactiondetails.bdcostbucketcostbeforetrans);
				if (bdCostBeforeTransaction == null){
					bdCostBeforeTransaction = BigDecimal.ZERO;
				}
				bdQty = rs.getBigDecimal(SMTableictransactiondetails.TableName + "." 
						+ SMTableictransactiondetails.bdqtychange);
				if (bdQty == null){
					bdQty = BigDecimal.ZERO;
				}
				bdCost = rs.getBigDecimal(SMTableictransactiondetails.TableName + "." 
						+ SMTableictransactiondetails.bdcostchange);
				if (bdCost == null){
					bdCost = BigDecimal.ZERO;
				}
				bdQtyAfterTransaction = bdQtyBeforeTransaction.add(bdQty);
				bdCostAfterTransaction = bdCostBeforeTransaction.add(bdCost);
				
				//Qty before transaction:
				out.println("<TD ALIGN=RIGHT>");
				out.println("<FONT SIZE=2>" + clsManageBigDecimals.BigDecimalToScaledFormattedString(
					SMTableictransactiondetails.bdcostbucketqtybeforetransScale, 
					bdQtyBeforeTransaction));
				out.println("</FONT></TD>");
				
				//Cost before transaction:
				out.println("<TD ALIGN=RIGHT>");
				out.println("<FONT SIZE=2>" + clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableictransactiondetails.bdcostbucketcostbeforetransScale, 
						bdCostBeforeTransaction));
				out.println("</FONT></TD>");
				
				//Qty after transaction:
				out.println("<TD ALIGN=RIGHT>");
				out.println("<FONT SIZE=2>" + clsManageBigDecimals.BigDecimalToScaledFormattedString(
					SMTableictransactiondetails.bdcostbucketqtybeforetransScale, 
					bdQtyAfterTransaction));
				out.println("</FONT></TD>");
				
				//Cost after transaction:
				out.println("<TD ALIGN=RIGHT>");
				out.println("<FONT SIZE=2>" + clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableictransactiondetails.bdcostbucketcostbeforetransScale, 
						bdCostAfterTransaction));
				out.println("</FONT></TD>");
				
				//Qty change:
				out.println("<TD ALIGN=RIGHT>");
				out.println("<FONT SIZE=2>" + clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableictransactiondetails.bdqtychangeScale, 
						bdQty));
				out.println("</FONT></TD>");
				
				//Cost change:
				out.println("<TD ALIGN=RIGHT>");
				out.println("<FONT SIZE=2>" + clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableictransactiondetails.bdcostchangeScale, 
						bdCost));
				out.println("</FONT></TD>");
				out.println("</TR>");
				
				//Accumulate the cost and qty for this transaction:
				bdTotalCost = bdTotalCost.add(bdCost);
				bdTotalQty = bdTotalQty.add(bdQty);
				
			}
			rs.close();
    	}catch (SQLException e){
    		m_sErrorMessage = "Error reading resultset - " + e.getMessage();
    		return false;
    	}

    	//Print the totals line:
    	out.println("<TR><TD ALIGN=RIGHT COLSPAN=7>&nbsp;TOTALS:</B></TD>");
    	out.println("<TD ALIGN=RIGHT><B><FONT SIZE=2>" 
    		+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
    		SMTableictransactiondetails.bdqtychangeScale, bdTotalQty) + "</FONT></TD>");
    	out.println("<TD ALIGN=RIGHT><B><FONT SIZE=2>" 
       		+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
       		SMTableictransactiondetails.bdcostchangeScale, bdTotalCost) + "</FONT></TD>");
    	out.println("</TD></TR>");
    	out.println("</TABLE>");
		return true;
	}
	private void printTransactionInfo(
			ResultSet rs, 
			PrintWriter out, 
			boolean bViewItemPermitted,
			ServletContext context,
			String sDBID) throws SQLException{
		
		printLineHeader(out);
		
		//Print the line:
		out.println("<TR>");
		
		try{
		//Transaction date:
		out.println("<TD><FONT SIZE=2>" + 
			clsDateAndTimeConversions.sqlDateToString(
				rs.getDate(SMTableictransactions.TableName + "." 
					+ SMTableictransactions.datpostingdate), "M/d/yyyy") + "</FONT></TD>");
		
		//Location:
		out.println("<TD><FONT SIZE=2>" 
			+ rs.getString(SMTableictransactions.TableName 
					+ "." + SMTableictransactions.slocation).trim() + "</FONT></TD>");
		
		//Item number:
		String sItemNumber = rs.getString(SMTableictransactions.TableName + "." 
			+ SMTableictransactions.sitemnumber);
		String sItemNumberLink = "";
		if (bViewItemPermitted){
			sItemNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
			+ "smic.ICDisplayItemInformation?ItemNumber=" 
		    		+ sItemNumber
		    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
		    		+ "\">" + ARUtilities.Fill_In_Empty_String_For_HTML_Cell(sItemNumber) + "</A>";
		}else{
			sItemNumberLink = sItemNumber;
		}
		out.println("<TD><FONT SIZE=2>" + sItemNumberLink + "</FONT></TD>");
		
		//Item description:
		out.println("<TD><FONT SIZE=2>" 
			+ rs.getString(SMTableicitems.TableName + "." 
				+ SMTableicitems.sItemDescription) + "</FONT></TD>");
		
		//Doc number:
		out.println("<TD><FONT SIZE=2>" + rs.getString(SMTableictransactions.TableName + "." 
				+ SMTableictransactions.sdocnumber)	+ "</FONT></TD>");

		//Type:
		String sType = "";
		BigDecimal bdQty = rs.getBigDecimal(SMTableictransactions.TableName + "." 
			+ SMTableictransactions.bdqty);
		int iType = rs.getInt(SMTableictransactions.TableName + "." 
			+ SMTableictransactions.ientrytype);
		if (iType == ICEntryTypes.ADJUSTMENT_ENTRY){
			sType = "Adjustment";
		}
		if (iType == ICEntryTypes.RECEIPT_ENTRY){
			if (bdQty.compareTo(BigDecimal.ZERO) < 0){
				sType = "Receipt Return";
			}else{
				sType = "Receipt";
			}
		}
		if (iType == ICEntryTypes.SHIPMENT_ENTRY){
			if (bdQty.compareTo(BigDecimal.ZERO) < 0){
				sType = "Shipment";
			}else{
				sType = "Shipment Return";
			}
		}
		if (iType == ICEntryTypes.TRANSFER_ENTRY){
			if (bdQty.compareTo(BigDecimal.ZERO) < 0){
				sType = "Transfer from";
			}else{
				sType = "Transfer to";
			}
		}
		if (iType == ICEntryTypes.PHYSICALCOUNT_ENTRY){
			sType = "Physical count";
		}
		out.println("<TD><FONT SIZE=2>" + sType + "</FONT></TD>");
		
		//Entry description:
		out.println("<TD><FONT SIZE=2>" 
			+ rs.getString(SMTableictransactions.TableName + "." 
				+ SMTableictransactions.sentrydescription) + "</FONT></TD>");
		
		//Line description:
		out.println("<TD><FONT SIZE=2>" 
			+ rs.getString(SMTableictransactions.TableName + "." 
				+ SMTableictransactions.slinedescription) + "</FONT></TD>");
		
		//Original batch number:
		out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" 
			+ Long.toString(rs.getLong(SMTableictransactions.TableName + "." 
				+ SMTableictransactions.loriginalbatchnumber)) 
			+ "</FONT></TD>");
		
		//Original entry number:
		out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" 
				+ Long.toString(rs.getLong(SMTableictransactions.TableName + "." 
					+ SMTableictransactions.loriginalentrynumber)) 
				+ "</FONT></TD>");
		
		//Original line number:
		out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" 
				+ Long.toString(rs.getLong(SMTableictransactions.TableName + "." 
					+ SMTableictransactions.loriginallinenumber)) 
				+ "</FONT></TD>");
		
		//Posted by:
		out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" 
				+ rs.getString(SMTableictransactions.TableName + "." 
					+ SMTableictransactions.spostedbyfullname) + "</FONT></TD>");
				
		//UOM:
		out.println("<TD><FONT SIZE=2>" 
				+ rs.getString(SMTableictransactions.TableName + "." 
					+ SMTableictransactions.sunitofmeasure) + "</FONT></TD>");
		
		//Qty:
		out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" 
			+ clsManageBigDecimals.BigDecimalToFormattedString("###,###,##0.0000", bdQty) 
			+ "</FONT></TD>");

		//Cost:
		out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" 
			+ clsManageBigDecimals.BigDecimalToFormattedString(
				"###,###,##0.0000", 
				rs.getBigDecimal(SMTableictransactions.TableName + "." + SMTableictransactions.bdcost)) 
			+ "</FONT></TD>");
		
		}catch(SQLException e){
			throw e;
		}
		out.println("</TR>");
		out.println("</TABLE>");

	}
	private void printLineHeader(PrintWriter out){
		out.println("<TABLE BORDER=0>");
		out.println("<TR>");
		out.println("<TD><B><FONT SIZE=2>Date</FONT></B></TD>");
		out.println("<TD><B><FONT SIZE=2>Location</FONT></B></TD>");
		out.println("<TD><B><FONT SIZE=2>Item</FONT></B></TD>");
		out.println("<TD><B><FONT SIZE=2>Item Desc.</FONT></B></TD>");
		out.println("<TD><B><FONT SIZE=2>Doc. #</FONT></B></TD>");
		out.println("<TD><B><FONT SIZE=2>Type</FONT></B></TD>");
		out.println("<TD><B><FONT SIZE=2>Entry desc.</FONT></B></TD>");
		out.println("<TD><B><FONT SIZE=2>Line desc.</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT><B><FONT SIZE=2>Batch</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT><B><FONT SIZE=2>Entry</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT><B><FONT SIZE=2>Line</FONT></B></TD>");
		out.println("<TD><B><FONT SIZE=2>Posted by</FONT></B></TD>");
		out.println("<TD><B><FONT SIZE=2>Unit</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT><B><FONT SIZE=2>Qty</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT><B><FONT SIZE=2>Cost</FONT></B></TD>");
		out.println("</TR>");
	}
	private void printDetailsHeader(PrintWriter out){

		out.println("<TABLE BORDER=0>");
		out.println("<TR>");
		out.println("<TD ALIGN=RIGHT VALIGN=BOTTOM><B><U><FONT SIZE=2>Cost<BR>bucket<BR>ID</FONT></U></B></TD>");
		out.println("<TD VALIGN=BOTTOM><B><U><FONT SIZE=2>Created</FONT></U></B></TD>");
		out.println("<TD VALIGN=BOTTOM><B><U><FONT SIZE=2>Remark</FONT></U></B></TD>");
		out.println("<TD VALIGN=BOTTOM><B><U><FONT SIZE=2>Qty&nbsp;before<BR>transaction</FONT></U></B></TD>");
		out.println("<TD VALIGN=BOTTOM><B><U><FONT SIZE=2>Cost&nbsp;before<BR>transaction</FONT></U></B></TD>");
		out.println("<TD VALIGN=BOTTOM><B><U><FONT SIZE=2>Qty&nbsp;after<BR>transaction</FONT></U></B></TD>");
		out.println("<TD VALIGN=BOTTOM><B><U><FONT SIZE=2>Cost&nbsp;after<BR>transaction</FONT></U></B></TD>");
		out.println("<TD ALIGN=RIGHT VALIGN=BOTTOM><B><U><FONT SIZE=2>Qty change</FONT></U></B></TD>");
		out.println("<TD ALIGN=RIGHT VALIGN=BOTTOM><B><U><FONT SIZE=2>Cost change</FONT></U></B></TD>");
		out.println("</TR>");
	}
	public String getErrorMessage(){
		return m_sErrorMessage;
	}
}

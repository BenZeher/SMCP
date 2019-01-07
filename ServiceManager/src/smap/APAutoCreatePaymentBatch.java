package smap;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;

import javax.servlet.ServletContext;

import SMClasses.SMBatchStatuses;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMTableapaccountsets;
import SMDataDefinition.SMTableapbatchentries;
import SMDataDefinition.SMTableapbatchentrylines;
import SMDataDefinition.SMTableapbatches;
import SMDataDefinition.SMTableaptransactions;
import SMDataDefinition.SMTableapvendorgroups;
import SMDataDefinition.SMTableapvendorremittolocations;
import SMDataDefinition.SMTablebkbanks;
import SMDataDefinition.SMTableicvendors;
import ServletUtilities.clsDBServerTime;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import smcontrolpanel.SMUtilities;

public class APAutoCreatePaymentBatch {

	public APBatch createPaymentBatch (
			Connection conn,
			String sPaymentDate,
			String sBatchDate,
			String sBankID,
			String sSelectDocumentsBy,
			String sDueDate,
			String sStartingDiscountDate,
			String sEndingDiscountDate,
			String sStartingVendorGroupName,
			String sEndingVendorGroupName,
			String sStartingAcctSetName,
			String sEndingAcctSetName,
			String sStartingInvoiceAmt,
			String sEndingInvoiceAmt,
			String sStartingVendor,
			String sEndingVendor,
			String sUserID,
			String sDBID,
			ServletContext context
			) throws Exception{
			
			//We don't want ANY payments that have unposted batch entries applying to them.
			
			//We ONLY want invoices listed - we're not creating payments for anything else.
		
			/*
			Here's the logic:
			
			In creating payments, we only care about 3 document types: invoices, debit notes, and credit notes
			If we have a credit balance with the vendor, then we don't want to create any payments for them.
			So if the total (net balance) of the vendor's credits is MORE than the total of their invoices and debits,
		 	we skip them.
			
			We'll get the 'net balance' for each vendor in the query, and if it's negative, we'll skip that record.
			  
			 */
		
		//The user can choose to select documents using different dates or combinations of dates.
		//He can either choose to select them by DUE DATE only, DISCOUNT DATE only, or BOTH DUE DATE AND DISCOUNT DATE.
		//Records have to qualified in the MAIN SQL 'WHERE' clause, and also in the subquery, so we're going to create
		//those qualifying clauses at this point, save them in string variables, then use them twice below to save building them
		//twice and to make it easier to maintain:
		
		//Select documents by:
		String sSelectDocumentsByDateQualifier = "";
		if (sSelectDocumentsBy.compareToIgnoreCase(APCreatePaymentsReportEdit.PARAM_SELECT_DOCUMENTS_BY_DISCOUNT_DATE) == 0){
			sSelectDocumentsByDateQualifier = "\n" + "/* User has chosen to select documents by DISCOUNT DATE: */" + "\n"
				+ "(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.datdiscountdate + " >= '" 
				+ clsDateAndTimeConversions.convertDateFormat(sStartingDiscountDate, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, "0000-00-00") + "')"
				+ " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.datdiscountdate + " <= '" 
				+ clsDateAndTimeConversions.convertDateFormat(sEndingDiscountDate, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, "0000-00-00") + "')"
				+ "\n"
			;
		}
		
		if (sSelectDocumentsBy.compareToIgnoreCase(APCreatePaymentsReportEdit.PARAM_SELECT_DOCUMENTS_BY_DUE_DATE) == 0){
			sSelectDocumentsByDateQualifier = "\n" + "/* User has chosen to select documents by DUE DATE */"+ "\n" 
				+ "(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.datduedate + " <= '" 
				+ clsDateAndTimeConversions.convertDateFormat(sDueDate, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, "0000-00-00") + "')" + "\n"
			;
		}
		
		if (sSelectDocumentsBy.compareToIgnoreCase(APCreatePaymentsReportEdit.PARAM_SELECT_DOCUMENTS_BY_DUE_OR_DISCOUNT_DATE) == 0){
			sSelectDocumentsByDateQualifier = "\n" + "/* User has chosen to select documents by EITHER DUE OR DISCOUNT DATE */" + "\n"
					+ "(" + "\n"
					
					//Start the 'OR' clause:
						// First part of OR clause:
						+ "        (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.datduedate + " <= '" 
						+ clsDateAndTimeConversions.convertDateFormat(sDueDate, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, "0000-00-00") + "')" + "\n"
					
					+ "    OR (" + "\n"
						// Second part of OR clause:
						+ "        (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.datdiscountdate + " >= '" 
						+ clsDateAndTimeConversions.convertDateFormat(sStartingDiscountDate, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, "0000-00-00") + "')"
						+ " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.datdiscountdate + " <= '" 
						+ clsDateAndTimeConversions.convertDateFormat(sEndingDiscountDate, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, "0000-00-00") + "')" + "\n"
						
					// End the OR clause
					+ "    )" + "\n"
						
				// Finish the 'AND' clause:
				+ ")" + "\n"
			;
		}
		
		/*
		// The following query is actually just used for testing - we'll save it, but it's not actually being used:
		
		//First, we are going to get a query that JUST returns the vendors who have any current amt due, since those are the only ones we will create payments for:
		String SQLVendorsDue = 
			"// This subquery will give us the current amt due, based on the date selections from the user: " + "\n"
			+ "SELECT"
			+ " SUM(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentamt 
				+ " - " + SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentdiscountavailable + ") AS CURRENTDUE" + "\n"
				+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor + " AS VENDOR" + "\n"
			+ " FROM " + SMTableaptransactions.TableName + "\n"
			
			+ " LEFT JOIN " + SMTableicvendors.TableName
				+ " ON " + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor + " = "
				+ SMTableicvendors.TableName + "." + SMTableicvendors.svendoracct + "\n"
			
			+ " LEFT JOIN " + SMTableapvendorgroups.TableName
				+ " ON " + SMTableicvendors.TableName + "." + SMTableicvendors.ivendorgroupid + " = "
				+ SMTableapvendorgroups.TableName + "." + SMTableapvendorgroups.lid + "\n"
			
			+ " LEFT JOIN " + SMTableapaccountsets.TableName
				+ " ON " + SMTableicvendors.TableName + "." + SMTableicvendors.iapaccountset + " = "
				+ SMTableapaccountsets.TableName + "." + SMTableapaccountsets.lid + "\n"
				
			+ "// This subquery will give us any open batch lines that might ALREADY be applying to these invoices: " + "\n"
			+ " LEFT JOIN" + "\n"
			+ " (SELECT"  + "\n"
				+ " " + SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lapplytodocid + " AS 'APPLYTODOCID'" + "\n"
				+ " FROM " + SMTableapbatchentrylines.TableName + "\n"
				
				+ " LEFT JOIN " + SMTableapbatchentries.TableName + "\n"
				+ " ON ("  + "\n"
					+ "(" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lbatchnumber + " = "
					+ SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lbatchnumber + ")"
					+ " AND (" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lentrynumber + " = "
					+ SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lentrynumber + ")"
				+ ")" + "\n"
			
				+ " LEFT JOIN " + SMTableapbatches.TableName + "\n"
				+ " ON " + SMTableapbatches.TableName + "." + SMTableapbatches.lbatchnumber + " = "
					+ SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lbatchnumber + "\n"
					
				+ " WHERE (" + "\n"
					+ "(" + SMTableapbatches.TableName + "." + SMTableapbatches.ibatchstatus + " = " + Integer.toString(SMBatchStatuses.ENTERED) + ")" + "\n"
					+ " OR (" + SMTableapbatches.TableName + "." + SMTableapbatches.ibatchstatus + " = " + Integer.toString(SMBatchStatuses.IMPORTED) + ")" + "\n"
				+ ")" + "\n" //End where

			+ ") AS APPLYINGBATCHLINES" + "\n"
			+ " ON APPLYINGBATCHLINES.APPLYTODOCID = " + SMTableaptransactions.TableName + "." + SMTableaptransactions.lid + "\n"
			+ "// End the 'applying batch lines' subquery " + "\n"
				
				
			+ " WHERE (" + "\n"
				+ "(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentamt + " != 0.00)" + "\n"
			
				//No ON HOLD invoices:
				+ "\n" + "// No ON HOLD invoices " + "\n"
				+ " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.ionhold + " = 0)" + "\n"
				+ "\n"
				
				//And the vendor is in the 'vendor range':
				+ "\n" + "// Include ONLY vendors starting with acct code '" + sStartingVendor + "' and ending with '" + sEndingVendor + "' " + "\n"
				+ " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor + " >= '" + sStartingVendor + "')" + "\n"
				+ " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor + " <= '" + sEndingVendor + "')" + "\n"
				+ "\n"
				
				//Vendor group names:
				+ "\n" + "// Include ONLY vendors with group IDs starting with '" + sStartingVendorGroupName + "' and ending with '" + sEndingVendorGroupName + "': " + "\n"
				+ " AND(" + SMTableapvendorgroups.TableName + "." + SMTableapvendorgroups.sgroupid + " >= '" + sStartingVendorGroupName + "')" + "\n"
				+ " AND (" + SMTableapvendorgroups.TableName + "." + SMTableapvendorgroups.sgroupid + " <= '" + sEndingVendorGroupName + "')" + "\n"
				+ "\n"
			
				//Account set names:
				+ "\n" + "// Include ONLY accounts sets starting with '" + sStartingAcctSetName + "' and ending with '" + sEndingAcctSetName + "' " + "\n"
				+ " AND (" + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.sacctsetname + " >= '" + sStartingAcctSetName + "')" + "\n"
				+ " AND (" + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.sacctsetname + " <= '" + sEndingAcctSetName + "')" + "\n"
				+ "\n"
				
				//Bank code:
				+ "// Include ONLY vendors having bank ID '" + sBankID + "' " + "\n"
				+ " AND (" + SMTableicvendors.TableName + "." + SMTableicvendors.ibankcode + " = " + sBankID + ")" + "\n"
				+ "\n"
				
				//Make sure that none of these transactions have any AP batch entry lines applied to them in any unposted batches:
				+ "// Make sure that none of these transactions have any OPEN AP batch entry lines applied to them: " + "\n"
				+ " AND (APPLYINGBATCHLINES.APPLYTODOCID IS NULL)" + "\n"
				+ "\n"
				
				//And it's EITHER a credit...
				+ " AND (" + "\n"
					+ "(" +SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype + " = " + SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_CREDITNOTE + ")" + "\n"
					//Or it's an invoice or debit note, in which case we qualify them by the date:
					+ " OR ("
					+ sSelectDocumentsByDateQualifier
					+ ")" + "\n"
				+ ")" + " // End THE AND CLAUSE " + "\n"
				+ "\n"	
				
			+ ")" + " // END THE WHERE CLAUSE " + "\n"
			
			+ " GROUP BY " + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor + "\n"
			
			//And make sure that the net due is NEGATIVE - i.e., that it's not a CREDIT balance:
			+ " HAVING (" + "CURRENTDUE" + " >= 0.00)" + "\n"
			+ "\n"
			
			+ " ORDER BY " + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor
		 + "\n"
		;
		
		*/
		
		//System.out.println("[1509041756] SQL = " + SQLVendorsDue);
		/*
		try {
			ResultSet rsVendorsWithCurrentAmts = SMUtilities.openResultSet(SQLVendorsDue, conn);
			while (rsVendorsWithCurrentAmts.next()){
				System.out.println("[1509041757]" + rsVendorsWithCurrentAmts.getString("VENDOR")
					+ ", " + rsVendorsWithCurrentAmts.getBigDecimal("CURRENTDUE")
				);
			}
			rsVendorsWithCurrentAmts.close();
		} catch (Exception e) {
			throw new Exception("Error [1509041392] reading vendors owed with SQL '" + SQLVendorsDue + "' - " + e.getMessage());
		}
		*/
		String SQL = "SELECT " + "\n"
		
			+ " " + SMTableaptransactions.TableName + "." + SMTableaptransactions.sdocnumber + "\n"
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentamt + "\n"
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentdiscountavailable + "\n"
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.datdiscountdate + "\n"
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.scontrolacct + "\n"
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype + "\n"
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.lid + "\n"
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.lapplytopurchaseorderid + "\n"
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor + "\n"

			+ ", " + SMTableicvendors.TableName + "." + SMTableicvendors.saddressline1 + "\n"
			+ ", " + SMTableicvendors.TableName + "." + SMTableicvendors.saddressline2 + "\n"
			+ ", " + SMTableicvendors.TableName + "." + SMTableicvendors.saddressline3 + "\n"
			+ ", " + SMTableicvendors.TableName + "." + SMTableicvendors.saddressline4 + "\n"
			+ ", " + SMTableicvendors.TableName + "." + SMTableicvendors.scity + "\n"
			+ ", " + SMTableicvendors.TableName + "." + SMTableicvendors.sstate + "\n"
			+ ", " + SMTableicvendors.TableName + "." + SMTableicvendors.scountry + "\n"
			+ ", " + SMTableicvendors.TableName + "." + SMTableicvendors.spostalcode + "\n"
			+ ", " + SMTableicvendors.TableName + "." + SMTableicvendors.svendoracct + "\n"
			+ ", " + SMTableicvendors.TableName + "." + SMTableicvendors.sname + "\n"
			
			+ ", " + SMTableicvendors.TableName + "." + SMTableicvendors.igenerateseparatepaymentsforeachinvoice + "\n"

			+ ", " + SMTableapvendorremittolocations.TableName + "." + SMTableapvendorremittolocations.sremittocode + "\n"
			+ ", " + SMTableapvendorremittolocations.TableName + "." + SMTableapvendorremittolocations.saddressline1 + "\n"
			+ ", " + SMTableapvendorremittolocations.TableName + "." + SMTableapvendorremittolocations.saddressline2 + "\n"
			+ ", " + SMTableapvendorremittolocations.TableName + "." + SMTableapvendorremittolocations.saddressline3 + "\n"
			+ ", " + SMTableapvendorremittolocations.TableName + "." + SMTableapvendorremittolocations.saddressline4 + "\n"
			+ ", " + SMTableapvendorremittolocations.TableName + "." + SMTableapvendorremittolocations.sremittoname + "\n"
			+ ", " + SMTableapvendorremittolocations.TableName + "." + SMTableapvendorremittolocations.scity + "\n"
			+ ", " + SMTableapvendorremittolocations.TableName + "." + SMTableapvendorremittolocations.sstate + "\n"
			+ ", " + SMTableapvendorremittolocations.TableName + "." + SMTableapvendorremittolocations.scountry + "\n"
			+ ", " + SMTableapvendorremittolocations.TableName + "." + SMTableapvendorremittolocations.spostalcode + "\n"
			
			+ ", " + SMTablebkbanks.TableName + "." + SMTablebkbanks.sglaccount + "\n"
			
			+ ", " + "AMTDUEQUERY.CURRENTDUE"
			+ ", " + "AMTDUEQUERY.VENDOR"

			+ " FROM " + "\n"
			+ " " + SMTableaptransactions.TableName + "\n"
			+ " LEFT JOIN " + SMTableicvendors.TableName
				+ " ON " + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor + " = "
				+ SMTableicvendors.TableName + "." + SMTableicvendors.svendoracct + "\n"
			+ " LEFT JOIN " + SMTableapvendorgroups.TableName
				+ " ON " + SMTableicvendors.TableName + "." + SMTableicvendors.ivendorgroupid + " = "
				+ SMTableapvendorgroups.TableName + "." + SMTableapvendorgroups.lid + "\n"
			+ " LEFT JOIN " + SMTableapaccountsets.TableName
				+ " ON " + SMTableicvendors.TableName + "." + SMTableicvendors.iapaccountset + " = "
				+ SMTableapaccountsets.TableName + "." + SMTableapaccountsets.lid + "\n"
			+ " LEFT JOIN " + SMTableapvendorremittolocations.TableName
				+ " ON (" + SMTableapvendorremittolocations.TableName + "." + SMTableapvendorremittolocations.svendoracct + " = "
				+ SMTableicvendors.TableName + "." + SMTableicvendors.svendoracct + ")"
				+ " AND (" + SMTableapvendorremittolocations.TableName + "." + SMTableapvendorremittolocations.sremittocode + " = "
				+ SMTableicvendors.TableName + "." + SMTableicvendors.sprimaryremittocode + ")" + "\n"
			+ " LEFT JOIN " + SMTablebkbanks.TableName
				+ " ON (" + SMTableicvendors.TableName + "." + SMTableicvendors.ibankcode + "=" + SMTablebkbanks.TableName + "." + SMTablebkbanks.lid + ")" + "\n"
				+ "\n"
				
			+ "/* This subquery will give us the current amt due, based on the date selections from the user: */" + "\n"
			+ " LEFT JOIN " + "\n"
				+ "(SELECT"
				+ " SUM(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentamt 
					+ " - " + SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentdiscountavailable + ") AS CURRENTDUE" + "\n"
					+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor + " AS VENDOR" + "\n"
				+ " FROM " + SMTableaptransactions.TableName + "\n"
				+ " WHERE (" + "\n"
					+ "(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentamt + " != 0.00)" + "\n"
				
					//And it's NOT on hold:
					+ " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.ionhold + " = 0)"
					
					//And it's EITHER a credit...
					+ " AND (" + "\n"
						+ "(" +SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype + " = " + SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_CREDITNOTE + ")" + "\n"
						//Or it's an invoice or debit note, in which case we qualify them by the date:
						+ " OR ("
						+ sSelectDocumentsByDateQualifier
						+ ")" + "\n"
					+ ")" + " /* End THE AND CLAUSE */" + "\n"
				+ ")" + " /* END THE WHERE CLAUSE */" + "\n"
				+ " GROUP BY " + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor + "\n"
				+ ") AS AMTDUEQUERY" + "\n"
			+ " ON " + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor + "=" + "AMTDUEQUERY.VENDOR" + "\n"
			 + "\n"
			
			+ "/* This subquery will give us any open batch lines that might ALREADY be applying to these invoices: */" + "\n"
			+ " LEFT JOIN" + "\n"
			+ " (SELECT"  + "\n"
				+ " " + SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lapplytodocid + " AS 'APPLYTODOCID'" + "\n"
				+ " FROM " + SMTableapbatchentrylines.TableName + "\n"
				
				+ " LEFT JOIN " + SMTableapbatchentries.TableName + "\n"
				+ " ON ("  + "\n"
					+ "(" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lbatchnumber + " = "
					+ SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lbatchnumber + ")"
					+ " AND (" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lentrynumber + " = "
					+ SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lentrynumber + ")"
				+ ")" + "\n"
			
				+ " LEFT JOIN " + SMTableapbatches.TableName + "\n"
				+ " ON " + SMTableapbatches.TableName + "." + SMTableapbatches.lbatchnumber + " = "
					+ SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lbatchnumber + "\n"
					
				+ " WHERE (" + "\n"
					+ "(" + SMTableapbatches.TableName + "." + SMTableapbatches.ibatchstatus + " = " + Integer.toString(SMBatchStatuses.ENTERED) + ")" + "\n"
					+ " OR (" + SMTableapbatches.TableName + "." + SMTableapbatches.ibatchstatus + " = " + Integer.toString(SMBatchStatuses.IMPORTED) + ")" + "\n"
				+ ")" + "\n" //End where

			+ ") AS APPLYINGBATCHLINES" + "\n"
			+ " ON APPLYINGBATCHLINES.APPLYTODOCID = " + SMTableaptransactions.TableName + "." + SMTableaptransactions.lid + "\n"
			+ "/* End the 'applying batch lines' subquery*/" + "\n"
			
			 + "\n"
			+ " WHERE (" + "\n"
			
				//No ON HOLD invoices:
				+ "\n" + "/* No ON HOLD invoices */" + "\n"
				+ "(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.ionhold + " = 0)" + "\n"
				+ "\n"
				
				//Vendor group names:
				+ "\n" + "/* Include ONLY vendors with group IDs starting with '" + sStartingVendorGroupName + "' and ending with '" + sEndingVendorGroupName + "': */" + "\n"
				+ " AND(" + SMTableapvendorgroups.TableName + "." + SMTableapvendorgroups.sgroupid + " >= '" + sStartingVendorGroupName + "')" + "\n"
				+ " AND (" + SMTableapvendorgroups.TableName + "." + SMTableapvendorgroups.sgroupid + " <= '" + sEndingVendorGroupName + "')" + "\n"
				+ "\n"
				
				//Account set names:
				+ "\n" + "/* Include ONLY accounts sets starting with '" + sStartingAcctSetName + "' and ending with '" + sEndingAcctSetName + "' */" + "\n"
				+ " AND (" + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.sacctsetname + " >= '" + sStartingAcctSetName + "')" + "\n"
				+ " AND (" + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.sacctsetname + " <= '" + sEndingAcctSetName + "')" + "\n"
				+ "\n"
				
				//Invoice current amts:
				// To make sure we also get CREDIT NOTES, we have to include any NEGATIVE amts also - so we'll use the ABS function to pick up those:
				+ "\n" + "/* Include ONLY invoice amts starting at '" + sStartingInvoiceAmt + "' and ending at '" + sEndingInvoiceAmt + "' */" + "\n"
				+ " AND (ABS(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentamt + ") >= " + sStartingInvoiceAmt.replaceAll(",", "") + ")" + "\n"
				+ " AND (ABS(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentamt + ") <= " + sEndingInvoiceAmt.replaceAll(",", "") + ")" + "\n"
				+ "\n"
				
				//Vendors:
				+ "\n" + "/* Include ONLY vendors starting with acct code '" + sStartingVendor + "' and ending with '" + sEndingVendor + "' */" + "\n"
				+ " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor + " >= '" + sStartingVendor + "')" + "\n"
				+ " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor + " <= '" + sEndingVendor + "')" + "\n"
				+ "\n"
				
				//Bank code:
				+ "/* Include ONLY vendors having bank ID '" + sBankID + "' */" + "\n"
				+ " AND (" + SMTableicvendors.TableName + "." + SMTableicvendors.ibankcode + " = " + sBankID + ")" + "\n"
				+ "\n"
				
				//And only for vendors with a current amt due:
				+ "/* Include ONLY vendors for whom we have a current amt due '" + sBankID + "' */" + "\n"
				+ " AND (" + "AMTDUEQUERY.CURRENTDUE" + " > 0.00)" + "\n"
				+ "\n"
		;

		//Make sure that none of these transactions have any AP batch entry lines applied to them in any unposted batches:
		SQL += "\n" + "/* Make sure that none of these transactions have any OPEN AP batch entry lines applied to them: */" + "\n"
			+ " AND (APPLYINGBATCHLINES.APPLYTODOCID IS NULL)" + "\n";

		//Qualify by the user's date choices:
		SQL += " AND " + sSelectDocumentsByDateQualifier;
		
		SQL += "\n" + "/* End the main 'WHERE' clause: */" + "\n" 
			+ ")" + "\n"  // End of WHERE clause
			+ " ORDER BY " 
				+ SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor
				
				//Get any CREDIT balances FIRST, so that they will appear at the top of the advice line for vendors who get a single check for ALL their due invoices.
				//In the case of a vendor who gets ONE check per invoice, we need the credit to appear first also, because they MAY have multiple invoices
				//linked to the credit, and in that case there will be multiple invoices on the one payment....
				
				//This formulation will give us either a NEGATIVE 1, a 0, or a POSITIVE 1, depending on the value of the current amt.  This is what we want, because we want any
				// credit notes, etc. FIRST, then the invoices, debits, etc....
				+ ", IF(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentamt + "=0.00, 0, (" 
					+ SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentamt + ") / (ABS(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentamt + "))"
				+ ")"
				
				//Then sort by document number....
				+ ", LPAD(" + SMTableaptransactions.sdocnumber + ", " + Integer.toString(SMTableaptransactions.sdocnumberlength) + ", ' ')"
				//+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.datdocdate + "\n"
		;
		
		//System.out.println("[1518460577] AP Create system generated check batch SQL = '" + SQL + "'");
		if (context != null){
			SMLogEntry log = new SMLogEntry(sDBID, context);
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_APGENERATECHECKBATCH, "AP System generated check", SQL, "[1507925257]");
		}
		
		//ArrayList<APBatchEntryLine>arrLines = new ArrayList<APBatchEntryLine>(0);
		APBatch batch = new APBatch("-1");
		batch.setsbatchdate(sBatchDate);
		batch.setsbatchdescription("Auto-created payment batch");
		batch.setsbatchstatus(Integer.toString(SMBatchStatuses.ENTERED));
		batch.setsbatchtype(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_PAYMENT));
		batch.setlcreatedby(sUserID);
		batch.setllasteditedby(sUserID);
		String sLastVendorRead = "";
		
		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		//System.out.println("[1512699187] - create payment report SQL = '" + SQL + "'");
		APBatchEntry entry = null;
		//Start reading the records:
		while (rs.next()){
			
			//If this vendor is NOT the last one read, then add the lines to this entry, and create a new entry:
			if (
				(sLastVendorRead.compareToIgnoreCase("") != 0)
				&& (rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor).compareToIgnoreCase(sLastVendorRead) != 0)
			){
				//Accumulate the line amounts into the entry amount:
				BigDecimal bdEntryTotal = new BigDecimal("0.00");
				for (int i = 0; i < entry.getLineArray().size(); i++){
					bdEntryTotal = bdEntryTotal.add(new BigDecimal(entry.getLineArray().get(i).getsbdamount().replaceAll(",", "").trim()));
				}
				entry.setsentryamount(clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapbatchentries.bdentryamountScale, bdEntryTotal));
				
				//Now add the entry to the batch:
				APBatchEntry storedentry = entry.copyEntry();
				batch.addBatchEntry(storedentry);
				
				//Clear the entry to load it again:
				entry = null;
			}
			
			//Populate the new entry:
			if (entry == null){
				entry = new APBatchEntry();
				entry.setsapplytoinvoicenumber("");
				entry.setsbdtaxrate("0.00");
				entry.setschecknumber("");
				entry.setscontrolacct(rs.getString(SMTablebkbanks.TableName + "." + SMTablebkbanks.sglaccount));
				entry.setsdatdiscount(SMUtilities.EMPTY_DATE_VALUE);
				entry.setsdatdocdate(sPaymentDate);
				entry.setsdatduedate(SMUtilities.EMPTY_DATE_VALUE);
				clsDBServerTime servertime = new clsDBServerTime(conn);
				entry.setsdatentrydate(servertime.getCurrentDateTimeInSelectedFormat(SMUtilities.DATE_FORMAT_FOR_DISPLAY));
				
				//In a payment batch, there is no discount on the payment document.  Each applying LINE might have a discount, but a discount amt on a payment is meaningless:
				entry.setsdiscountamt("0.00");
				entry.setsdocnumber(""); //This gets set when we save, automatically
				//entry.setsentryamount(sEntryAmount);  We set this when we add up the lines
				entry.setsentrydescription("Generated by automatic payment creation");
				entry.setsentrytype(Integer.toString(SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PAYMENT));
				entry.setsiprintcheck("1");
				entry.setslbankid(sBankID);
				
				//Remit-to addresses:
				if (rs.getString(SMTableapvendorremittolocations.TableName + "." + SMTableapvendorremittolocations.sremittocode) == null){
					entry.setsremittocode("");
					entry.setsremittoaddressline1(rs.getString(SMTableicvendors.TableName + "." + SMTableicvendors.saddressline1));
					entry.setsremittoaddressline2(rs.getString(SMTableicvendors.TableName + "." + SMTableicvendors.saddressline2));
					entry.setsremittoaddressline3(rs.getString(SMTableicvendors.TableName + "." + SMTableicvendors.saddressline3));
					entry.setsremittoaddressline4(rs.getString(SMTableicvendors.TableName + "." + SMTableicvendors.saddressline4));
					entry.setsremittocity(rs.getString(SMTableicvendors.TableName + "." + SMTableicvendors.scity));
					entry.setsremittocountry(rs.getString(SMTableicvendors.TableName + "." + SMTableicvendors.scountry));
					entry.setsremittoname(rs.getString(SMTableicvendors.TableName + "." + SMTableicvendors.sname));
					entry.setsremittopostalcode(rs.getString(SMTableicvendors.TableName + "." + SMTableicvendors.spostalcode));
					entry.setsremittostate(rs.getString(SMTableicvendors.TableName + "." + SMTableicvendors.sstate));
				}else{
					entry.setsremittocode(SMTableicvendors.TableName + "." + SMTableicvendors.sprimaryremittocode);
					entry.setsremittoaddressline1(rs.getString(SMTableapvendorremittolocations.TableName + "." + SMTableapvendorremittolocations.saddressline1));
					entry.setsremittoaddressline2(rs.getString(SMTableapvendorremittolocations.TableName + "." + SMTableapvendorremittolocations.saddressline2));
					entry.setsremittoaddressline3(rs.getString(SMTableapvendorremittolocations.TableName + "." + SMTableapvendorremittolocations.saddressline3));
					entry.setsremittoaddressline4(rs.getString(SMTableapvendorremittolocations.TableName + "." + SMTableapvendorremittolocations.saddressline4));
					entry.setsremittocity(rs.getString(SMTableapvendorremittolocations.TableName + "." + SMTableapvendorremittolocations.scity));
					entry.setsremittocountry(rs.getString(SMTableapvendorremittolocations.TableName + "." + SMTableapvendorremittolocations.scountry));
					entry.setsremittoname(rs.getString(SMTableapvendorremittolocations.TableName + "." + SMTableapvendorremittolocations.sremittoname));
					entry.setsremittopostalcode(rs.getString(SMTableapvendorremittolocations.TableName + "." + SMTableapvendorremittolocations.spostalcode));
					entry.setsremittostate(rs.getString(SMTableapvendorremittolocations.TableName + "." + SMTableapvendorremittolocations.sstate));
				}
				entry.setstaxjurisdiction("");
				entry.setstaxtype("0");
				entry.setsterms("");
				entry.setsvendoracct(rs.getString(SMTableicvendors.TableName + "." + SMTableicvendors.svendoracct));
				entry.setsvendorname(rs.getString(SMTableicvendors.TableName + "." + SMTableicvendors.sname));
			}

			//Populate a new line:
			APBatchEntryLine line = new APBatchEntryLine();
			line.setsapplytodocnumber(rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.sdocnumber));
			line.setslapplytodocid(Long.toString(rs.getLong(SMTableaptransactions.TableName + "." + SMTableaptransactions.lid)));
			
			BigDecimal bdTransactionCurrentAmt = rs.getBigDecimal(SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentamt);
			BigDecimal bdNetDiscountAvailable = rs.getBigDecimal(SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentdiscountavailable);
			
			//Determine if we are actually ELIGIBLE for the discount, based on the discount date and check date:
			String sPaymentDateAsSQLDate = clsDateAndTimeConversions.convertDateFormat(sPaymentDate, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE);
			String sDiscountDateAsSQLDate = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
				rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.datdiscountdate), SMUtilities.DATE_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE);
			
			//System.out.println("[1546724747] - sPaymentDateAsSQLDate = '" + sPaymentDateAsSQLDate + "', sDiscountDateAsSQLDate = '" + sDiscountDateAsSQLDate + "'.");
			//System.out.println("[1546724748] bdNetDiscountAvailable = '" + bdNetDiscountAvailable + "'.");
			
			//If the check date is past the discount date, then the real discount available should be ZERO:
			if (sPaymentDateAsSQLDate.compareToIgnoreCase(sDiscountDateAsSQLDate) > 0){
				bdNetDiscountAvailable = BigDecimal.ZERO;
			}
			
			//System.out.println("[1546724749] bdNetDiscountAvailable AFTER CHECKING = '" + bdNetDiscountAvailable + "'.");
			
			//Now set the line amount and discount amount:
			BigDecimal bdLineAmount = (bdTransactionCurrentAmt.subtract(bdNetDiscountAvailable)).negate();
			line.setsbdamount(clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapbatchentrylines.bdamountScale, bdLineAmount));
			line.setsbddiscountappliedamt(clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapbatchentries.bddiscountScale, bdNetDiscountAvailable.negate()));
			
			line.setscomment("");
			line.setsdescription("Auto-generated payment");
			line.setsdistributionacct(rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.scontrolacct));
			line.setsiapplytodoctype(Integer.toString(rs.getInt(SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype)));
			line.setslpoheaderid(Long.toString(rs.getLong(SMTableaptransactions.TableName + "." + SMTableaptransactions.lapplytopurchaseorderid)));
			
			//Add the line to the entry:
			APBatchEntryLine storedline = line.copyLine();
			entry.addLine(storedline);
			
			//If this vendor is supposed to get one check for EACH invoice, we still have to allow for the case where they have credits that might over-apply any one invoice.
			//So what we'll do is put the credits on the entry first, then keep applying the oldest invoices (sorted by document number, not date....because that's just how
			//the query works) until we've applied enough invoices to offset the credit amount:
			if (rs.getLong(SMTableicvendors.TableName + "." + SMTableicvendors.igenerateseparatepaymentsforeachinvoice) == 1){
				//Calculate the ENTRY and DISCOUNT total:
				BigDecimal bdEntryTotal = new BigDecimal("0.00");
				for (int i = 0; i < entry.getLineArray().size(); i++){
					bdEntryTotal = bdEntryTotal.add(new BigDecimal(entry.getLineArray().get(i).getsbdamount().replaceAll(",", "").trim()));
				}

				//If we've 'used up' any credits, then we're done with this entry:
				
				//Since the payment entries are normally negative, if this entry total has reached a 'negative' point, that means
				//we've exhausted any credits (positives):
				if (bdEntryTotal.compareTo(BigDecimal.ZERO) < 0){
					entry.setsentryamount(clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapbatchentries.bdentryamountScale, bdEntryTotal));
					APBatchEntry storedentry = entry.copyEntry();
					batch.addBatchEntry(storedentry);
					
					//Clear the lines and entry:
					entry = null;
					
					//Reset the vendor to blank, to start all over:
					sLastVendorRead = "";
				}
			}else{
				sLastVendorRead = rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor);
			}
		}
		rs.close();
		
		//IF there is an entry left to be added, add it now:
		if (entry != null){
			BigDecimal bdEntryTotal = new BigDecimal("0.00");
			for (int i = 0; i < entry.getLineArray().size(); i++){
				bdEntryTotal = bdEntryTotal.add(new BigDecimal(entry.getLineArray().get(i).getsbdamount().replaceAll(",", "").trim()));
			}
			entry.setsentryamount(clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableapbatchentries.bdentryamountScale, bdEntryTotal));
			APBatchEntry storedentry = entry.copyEntry();
			batch.addBatchEntry(storedentry);
		}
		
		//Update ALL the entry numbers and line numbers in the batch:
		for(int iEntry = 0; iEntry < batch.getBatchEntryArray().size(); iEntry++){
			batch.getBatchEntryArray().get(iEntry).setsentrynumber(Integer.toString(iEntry + 1));
			for(int iLine = 0; iLine < batch.getBatchEntryArray().get(iEntry).getLineArray().size(); iLine++){
				batch.getBatchEntryArray().get(iEntry).getLineArray().get(iLine).setsentrynumber(Integer.toString(iEntry + 1));
				batch.getBatchEntryArray().get(iEntry).getLineArray().get(iLine).setslinenumber(Integer.toString(iLine + 1));
			}
		}
		
		return batch;
			
	}
}

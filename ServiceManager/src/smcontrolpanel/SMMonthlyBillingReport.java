package smcontrolpanel;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import javax.servlet.ServletContext;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableinvoicedetails;
import SMDataDefinition.SMTableinvoiceheaders;
import SMDataDefinition.SMTablesalesgroups;
import SMDataDefinition.SMTablesalesperson;
import SMDataDefinition.SMTableservicetypes;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;

public class SMMonthlyBillingReport extends java.lang.Object{

	private static String NO_SALES_GROUP_MARKER = "***NOSALESGROUP***";
	private String m_sErrorMessage;
	private boolean bDebugMode = false;
	
	public SMMonthlyBillingReport(
			){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			String sStartingDate,
			String sEndingDate,
			ArrayList<String> arrSalesGroupList,
			ArrayList<String> arrServiceTypesList,
			boolean bShowDetail,
			String sDBID,
			String sUserID,
			PrintWriter out,
			ServletContext context,
			String sLicenseModuleLevel
			){
    	//SQL Statement:
        String SQL = "SELECT"
        	+ " " + "'Monthly Billing Report' AS REPORTNAME"
            + ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber
            + ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sOrderNumber
            + ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sServiceTypeCode
            + ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sServiceTypeCodeDescription
            + ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sLocation
            + ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dExtendedPriceAfterDiscount
            + ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sBillToName
            + ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate
            + ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iTransactionType
            
    	    + ", IF(" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sSalesperson + " = '', "
    		+ "'" + SMOrderHeader.UNLISTEDSALESPERSON_MARKER + "', " 
    		+ SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sSalesperson
    		+ ") AS " + SMTableinvoiceheaders.sSalesperson
    		
    		+ ", CONCAT(" + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonFirstName + ",' '," 
			+ SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonLastName + ") AS SALESPERSONNAME"
    		
            + ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sServiceTypeCode + " AS SERVICETYPE"
             + ", " + SMTableservicetypes.TableName + "." + SMTableservicetypes.sName + " AS SERVICETYPEDESC"

            
            + ", " + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupCode + " AS SALESGROUP"
            
            + " FROM"
            + " " + SMTableinvoicedetails.TableName + " LEFT JOIN " + SMTableinvoiceheaders.TableName + " ON "
            + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber + " = "
            + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber
            
            //Link sales groups table:
    	    + " LEFT JOIN " + SMTablesalesgroups.TableName + " ON " + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.iSalesGroupId
    	    + " = " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iSalesGroup
    	    
    	    //Link salesperson table:
    	    + " LEFT JOIN " + SMTablesalesperson.TableName + " ON " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sSalesperson
    	    	+ " = " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode
    	    
    	    //Link service types table:
    	    + " LEFT JOIN " + SMTableservicetypes.TableName + " ON " + SMTableservicetypes.TableName + "." + SMTableservicetypes.sCode
    	    + " = " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sServiceTypeCode
            
            + " WHERE ("
            
            + "(" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate + " >= '" + sStartingDate + " 00:00:00')"
            + " AND (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate + " <= '" + sEndingDate + " 23:59:59')";
            
    		//Get the service types:
		    if (arrServiceTypesList.size() > 0){
		    	SQL += " AND (";
	    		for (int i = 0; i < arrServiceTypesList.size(); i++){
	    			if (i == 0){
	    				SQL += "(" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sServiceTypeCode + " = '" + arrServiceTypesList.get(i) + "')";
	    			}else{
	    				SQL += " OR (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sServiceTypeCode + " = '" + arrServiceTypesList.get(i) + "')";
	    			}
	    		}
	    		SQL = SQL + ")";
		    }
		    
    		//Get the sales groups:
		    if (arrSalesGroupList.size() > 0){
		    	SQL += " AND (";
	    		for (int i = 0; i < arrSalesGroupList.size(); i++){
	    			if (i == 0){
	    				SQL += "(" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iSalesGroup + " = " 
	    					+ arrSalesGroupList.get(i).substring(arrSalesGroupList.get(i).indexOf(SMMonthlyBillingReportSelection.PARAM_SEPARATOR) + 1) + ")";
	    			}else{
	    				SQL += " OR (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iSalesGroup + " = " 
	    					+ arrSalesGroupList.get(i).substring(arrSalesGroupList.get(i).indexOf(SMMonthlyBillingReportSelection.PARAM_SEPARATOR) + 1) + ")";
	    			}
	    		}
	    		SQL = SQL + ")";
		    }
            
            SQL = SQL + ")"
            
            + " ORDER BY"
            + " SALESGROUP"
            + ", SERVICETYPE DESC"
            + ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sLocation
            + ", " + SMTableinvoiceheaders.sSalesperson
            + ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber
            ;
    	//end SQL statement

        if (bDebugMode){
        	System.out.println(" [1346073529] In " + this.getClass() + " - main SQL = " + SQL);
        }
            
        //System.out.println("In " + this.toString() + ".processReport - main SQL = " + SQL);
		String sCurrentServiceType = "";
		String sCurrentServiceTypeDescription = "";
    	String sCurrentLocation = "";
    	String sCurrentSalesperson = "";
    	String sCurrentSalespersonName = "";
    	String sCurrentInvoiceNumber = "";
    	String sCurrentSalesGroup = NO_SALES_GROUP_MARKER;
    	
    	//Variables for invoice footer:
		String sCurrentBillToName = "";
		String sCurrentTransactionType = "";
		java.sql.Date datCurrentInvoice = null;
		String sCurrentOrderNumber = "";
		BigDecimal bdCurrentInvoiceTotal = BigDecimal.ZERO;
		
		//Variables for salesperson footer:
		long lNumberOfInvoicesForSalesperson = 0L; 
		BigDecimal bdInvoiceAmountForSalesperson = BigDecimal.ZERO;
		long lNumberOfCreditNotesForSalesperson = 0L;
		BigDecimal bdCreditNoteAmountForSalesperson = BigDecimal.ZERO;
		BigDecimal bdTotalAmountForSalesperson = BigDecimal.ZERO;

		//Variables for location footer:
		long lNumberOfInvoicesForLocation = 0L; 
		BigDecimal bdInvoiceAmountForLocation = BigDecimal.ZERO;
		long lNumberOfCreditNotesForLocation = 0L;
		BigDecimal bdCreditNoteAmountForLocation = BigDecimal.ZERO;
		BigDecimal bdTotalAmountForLocation = BigDecimal.ZERO;

		//Variables for Service types:
		long lNumberOfInvoicesForServiceType = 0L;
		BigDecimal bdInvoiceAmountForServiceType = BigDecimal.ZERO;
		long lNumberOfCreditNotesForServiceType = 0l;
		BigDecimal bdCreditNoteAmountForServiceType = BigDecimal.ZERO;
		BigDecimal bdTotalAmountForServiceType = BigDecimal.ZERO;
		
		//Variables for sales group:
		long lNumberOfInvoicesForSalesGroup = 0L; 
		BigDecimal bdInvoiceAmountForSalesGroup = BigDecimal.ZERO;
		long lNumberOfCreditNotesForSalesGroup = 0L;
		BigDecimal bdCreditNoteAmountForSalesGroup = BigDecimal.ZERO;
		BigDecimal bdTotalAmountForSalesGroup = BigDecimal.ZERO;
		
		//Variables for report totals:
		long lTotalNumberOfInvoices = 0L; 
		BigDecimal bdTotalAverageInvoiceAmount = BigDecimal.ZERO;
		long lTotalNumberOfCreditNotes = 0L; 
		BigDecimal bdTotalAverageCreditNoteAmount = BigDecimal.ZERO;
		BigDecimal bdTotalAmount = BigDecimal.ZERO;
		BigDecimal bdTotalInvoiceAmount = BigDecimal.ZERO;
		BigDecimal bdTotalCreditNoteAmount = BigDecimal.ZERO;
		Map< String,BigDecimal> hmServiceTypeTotalAmmounts =  new HashMap< String,BigDecimal>();
    	
		//Check permissions for viewing invoices and orders:
		boolean bViewInvoicePermitted = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMPrintInvoice,
			sUserID,
			conn,
			sLicenseModuleLevel);
		boolean bViewOrderPermitted = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMViewOrderInformation,
			sUserID,
			conn,
			sLicenseModuleLevel);
		
    	try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()){
				
				//If the current invoice number is not blank, and the invoice number has changed, print the
				//invoice footer:
				if (
					(rs.getString(SMTableinvoiceheaders.sInvoiceNumber).compareToIgnoreCase(sCurrentInvoiceNumber) != 0)
					&& (sCurrentInvoiceNumber.compareToIgnoreCase("") != 0)
				){
					if (bShowDetail){
						printInvoiceFooter(
							sCurrentSalesGroup,
							sCurrentServiceTypeDescription,
							sCurrentLocation,
							sCurrentSalesperson,
							sCurrentInvoiceNumber.trim(),
							sCurrentBillToName,
							sCurrentTransactionType,
							datCurrentInvoice,
							sCurrentOrderNumber.trim(),
							bdCurrentInvoiceTotal,
							bViewInvoicePermitted,
							bViewOrderPermitted,
							sDBID,
							out,
							context
						);
					}
					bdCurrentInvoiceTotal = BigDecimal.ZERO;
					
					if (sCurrentTransactionType.compareToIgnoreCase("IN") == 0){
						lTotalNumberOfInvoices++;
						lNumberOfInvoicesForSalesperson++;
						lNumberOfInvoicesForLocation++;
						lNumberOfInvoicesForServiceType++;
						lNumberOfInvoicesForSalesGroup++;
					}
					if (sCurrentTransactionType.compareToIgnoreCase("CR") == 0){
						lTotalNumberOfCreditNotes++;
						lNumberOfCreditNotesForSalesperson++;
						lNumberOfCreditNotesForLocation++;
						lNumberOfCreditNotesForServiceType++;
						lNumberOfCreditNotesForSalesGroup++;
					}
				}
				
	    		//Print the salesperson header for any new service type OR location
				// OR salesperson:
				if (
					((rs.getString("ServiceType").compareToIgnoreCase(sCurrentServiceType) != 0)
					|| ((rs.getString(SMTableinvoiceheaders.sLocation)).compareToIgnoreCase(sCurrentLocation) != 0)
					|| ((rs.getString(SMTableinvoiceheaders.sSalesperson)).compareToIgnoreCase(sCurrentSalesperson) != 0))
					&& (sCurrentSalesperson.compareToIgnoreCase("") != 0)
				){
					BigDecimal bdAverageInvoiceAmountForSalesperson = BigDecimal.ZERO;
					BigDecimal bdAverageCreditNoteAmountForSalesperson = BigDecimal.ZERO;
					
					if (lNumberOfInvoicesForSalesperson > 0){
						bdAverageInvoiceAmountForSalesperson = bdInvoiceAmountForSalesperson.divide(BigDecimal.valueOf(lNumberOfInvoicesForSalesperson), 2, RoundingMode.HALF_UP);
					}else{
						bdAverageInvoiceAmountForSalesperson = BigDecimal.ZERO;
					}
					if (lNumberOfCreditNotesForSalesperson > 0){
						bdAverageCreditNoteAmountForSalesperson = bdCreditNoteAmountForSalesperson.divide(BigDecimal.valueOf(lNumberOfCreditNotesForSalesperson), 2, RoundingMode.HALF_UP);
					}else{
						bdAverageCreditNoteAmountForSalesperson = BigDecimal.ZERO;
					}
					printSalespersonFooter(
						lNumberOfInvoicesForSalesperson, 
						bdAverageInvoiceAmountForSalesperson,
						lNumberOfCreditNotesForSalesperson, 
						bdAverageCreditNoteAmountForSalesperson,
						sCurrentSalesperson,
						sCurrentSalespersonName,
						sCurrentLocation,
						sCurrentServiceTypeDescription,
						sCurrentSalesGroup,
						bdTotalAmountForSalesperson,
						bShowDetail,
						out,
						conn
					);
					
					//Initialize the salesperson variables:
					lNumberOfInvoicesForSalesperson = 0L;
					lNumberOfCreditNotesForSalesperson = 0L; 
					bdInvoiceAmountForSalesperson = BigDecimal.ZERO;
					bdCreditNoteAmountForSalesperson = BigDecimal.ZERO;
					bdTotalAmountForSalesperson = BigDecimal.ZERO;
				}

				//Process the location footer:
	    		//Print the location footer for any new service type OR location
				if (
					((rs.getString("ServiceType").compareToIgnoreCase(sCurrentServiceType) != 0)
					|| ((rs.getString(SMTableinvoiceheaders.sLocation)).compareToIgnoreCase(sCurrentLocation) != 0))
					&& (sCurrentLocation.compareToIgnoreCase("") != 0)
				){
					BigDecimal bdAverageInvoiceAmountForLocation = BigDecimal.ZERO;
					BigDecimal bdAverageCreditNoteAmountForLocation = BigDecimal.ZERO;
					
					if (lNumberOfInvoicesForLocation > 0){
						bdAverageInvoiceAmountForLocation = bdInvoiceAmountForLocation.divide(BigDecimal.valueOf(lNumberOfInvoicesForLocation), 2, RoundingMode.HALF_UP);
					}else{
						bdAverageInvoiceAmountForLocation = BigDecimal.ZERO;
					}
					if (lNumberOfCreditNotesForLocation > 0){
						bdAverageCreditNoteAmountForLocation = bdCreditNoteAmountForLocation.divide(BigDecimal.valueOf(lNumberOfCreditNotesForLocation), 2, RoundingMode.HALF_UP);
					}else{
						bdAverageCreditNoteAmountForLocation = BigDecimal.ZERO;
					}
					printLocationFooter(
							lNumberOfInvoicesForLocation, 
							bdAverageInvoiceAmountForLocation,
							lNumberOfCreditNotesForLocation, 
							bdAverageCreditNoteAmountForLocation,
							sCurrentLocation,
							sCurrentServiceTypeDescription,
							sCurrentSalesGroup,
							bdTotalAmountForLocation,
							out
							);
					//Initialize the location variables:
					lNumberOfInvoicesForLocation = 0L; 
					lNumberOfCreditNotesForLocation = 0L;
					bdInvoiceAmountForLocation = BigDecimal.ZERO;
					bdCreditNoteAmountForLocation = BigDecimal.ZERO;
					bdTotalAmountForLocation = BigDecimal.ZERO;
				}

				//Service Type footer
	    		//Print the sale type footer for any new service type
				if (((rs.getString("ServiceType").compareToIgnoreCase(sCurrentServiceType) != 0))
					&& (sCurrentServiceType.compareToIgnoreCase("") != 0)
				){
					BigDecimal bdAverageInvoiceAmountForServiceType = BigDecimal.ZERO;
					BigDecimal bdAverageCreditNoteAmountForServiceType = BigDecimal.ZERO;
					if (lNumberOfInvoicesForServiceType > 0){
						bdAverageInvoiceAmountForServiceType = bdInvoiceAmountForServiceType.divide(BigDecimal.valueOf(lNumberOfInvoicesForServiceType), 2, RoundingMode.HALF_UP);
					}else{
						bdAverageInvoiceAmountForServiceType = BigDecimal.ZERO;
					}
					if (lNumberOfCreditNotesForServiceType > 0){
						bdAverageCreditNoteAmountForServiceType = bdCreditNoteAmountForServiceType.divide(BigDecimal.valueOf(lNumberOfCreditNotesForServiceType), 2, RoundingMode.HALF_UP);
					}else{
						bdAverageCreditNoteAmountForServiceType = BigDecimal.ZERO;
					}
					printServiceTypeFooter(
							lNumberOfInvoicesForServiceType, 
							bdAverageInvoiceAmountForServiceType,
							lNumberOfCreditNotesForServiceType, 
							bdAverageCreditNoteAmountForServiceType,
							sCurrentServiceTypeDescription,
							sCurrentSalesGroup,
							bdTotalAmountForServiceType,
							out
							);
					//Initialize the service type variables:
					lNumberOfInvoicesForServiceType = 0L; 
					lNumberOfCreditNotesForServiceType = 0L; 
					bdInvoiceAmountForServiceType = BigDecimal.ZERO;
					bdCreditNoteAmountForServiceType = BigDecimal.ZERO;
					bdTotalAmountForServiceType = BigDecimal.ZERO;
				}
				
				//Sales Group footer
	    		//Print the sales group footer for any new sales group
				String sSalesGroupFromData = rs.getString("SALESGROUP");
				if (sSalesGroupFromData == null){
					sSalesGroupFromData = "";
				}
				if (
						(sSalesGroupFromData.compareToIgnoreCase(sCurrentSalesGroup) != 0)
						&& (sCurrentSalesGroup.compareToIgnoreCase(NO_SALES_GROUP_MARKER) != 0)
				){
					BigDecimal bdAverageInvoiceAmountForSalesGroup = BigDecimal.ZERO;
					BigDecimal bdAverageCreditNoteAmountForSalesGroup = BigDecimal.ZERO;
					
					if (lNumberOfInvoicesForSalesGroup > 0){
						bdAverageInvoiceAmountForSalesGroup = bdInvoiceAmountForSalesGroup.divide(BigDecimal.valueOf(lNumberOfInvoicesForSalesGroup), 2, RoundingMode.HALF_UP);
					}else{
						bdAverageInvoiceAmountForSalesGroup = BigDecimal.ZERO;
					}
					if (lNumberOfCreditNotesForSalesGroup > 0){
						bdAverageCreditNoteAmountForSalesGroup = bdCreditNoteAmountForSalesGroup.divide(BigDecimal.valueOf(lNumberOfCreditNotesForSalesGroup), 2, RoundingMode.HALF_UP);
					}else{
						bdAverageCreditNoteAmountForSalesGroup = BigDecimal.ZERO;
					}
					printSalesGroupFooter(
							lNumberOfInvoicesForSalesGroup, 
							bdAverageInvoiceAmountForSalesGroup,
							lNumberOfCreditNotesForSalesGroup, 
							bdAverageCreditNoteAmountForSalesGroup,
							sCurrentSalesGroup,
							bdTotalAmountForSalesGroup,
							out
							);
					//Initialize the sales group variables:
					lNumberOfInvoicesForSalesGroup = 0L; 
					lNumberOfCreditNotesForSalesGroup = 0L; 
					bdInvoiceAmountForSalesGroup = BigDecimal.ZERO;
					bdCreditNoteAmountForSalesGroup = BigDecimal.ZERO;
					bdTotalAmountForSalesGroup = BigDecimal.ZERO;
				}
				
				BigDecimal bdLinePrice = rs.getBigDecimal(SMTableinvoicedetails.dExtendedPriceAfterDiscount).setScale(2, BigDecimal.ROUND_HALF_UP);
				//Calculate the report totals:
				if (rs.getInt(SMTableinvoiceheaders.iTransactionType) == SMTableinvoiceheaders.TYPE_INVOICE){
					bdTotalInvoiceAmount = bdTotalInvoiceAmount.add(bdLinePrice);
					bdInvoiceAmountForSalesperson = bdInvoiceAmountForSalesperson.add(bdLinePrice);
					bdInvoiceAmountForLocation = bdInvoiceAmountForLocation.add(bdLinePrice);
					bdInvoiceAmountForServiceType = bdInvoiceAmountForServiceType.add(bdLinePrice);
					bdInvoiceAmountForSalesGroup = bdInvoiceAmountForSalesGroup.add(bdLinePrice);
					sCurrentTransactionType = "IN";
				}
				if (rs.getInt(SMTableinvoiceheaders.iTransactionType) == SMTableinvoiceheaders.TYPE_CREDIT){
					bdTotalCreditNoteAmount = bdTotalCreditNoteAmount.add(bdLinePrice);
					bdCreditNoteAmountForSalesperson = bdCreditNoteAmountForSalesperson.add(bdLinePrice);
					bdCreditNoteAmountForLocation = bdCreditNoteAmountForLocation.add(bdLinePrice);
					bdCreditNoteAmountForServiceType = bdCreditNoteAmountForServiceType.add(bdLinePrice);
					bdCreditNoteAmountForSalesGroup = bdCreditNoteAmountForSalesGroup.add(bdLinePrice);
					sCurrentTransactionType = "CR";
				}
				
				bdTotalAmount = bdTotalAmount.add(bdLinePrice);
				bdTotalAmountForSalesperson = bdTotalAmountForSalesperson.add(bdLinePrice);
				bdTotalAmountForLocation = bdTotalAmountForLocation.add(bdLinePrice);
				bdTotalAmountForServiceType = bdTotalAmountForServiceType.add(bdLinePrice);
				bdTotalAmountForSalesGroup = bdTotalAmountForSalesGroup.add(bdLinePrice);
				//Add the service type totals to hash map.
				try {
					if(sCurrentServiceType.compareToIgnoreCase("") != 0) {
						hmServiceTypeTotalAmmounts.put(sCurrentServiceTypeDescription , hmServiceTypeTotalAmmounts.get(sCurrentServiceTypeDescription).add(bdLinePrice));
					}	
				}catch (Exception e) {
					hmServiceTypeTotalAmmounts.put(sCurrentServiceTypeDescription, bdLinePrice);
				}
				
			
			
				//If service type or the location or the salesperson changes,
				//print a salesperson header:
				//If the current salesperson is blank, print it
				if (
						(sSalesGroupFromData.compareToIgnoreCase(sCurrentSalesGroup) != 0)
						|| (rs.getString("ServiceType").compareToIgnoreCase(sCurrentServiceType) != 0)
						|| ((rs.getString(SMTableinvoiceheaders.sLocation)).compareToIgnoreCase(sCurrentLocation) != 0)
						|| ((rs.getString(SMTableinvoiceheaders.sSalesperson)).compareToIgnoreCase(sCurrentSalesperson) != 0)
					){

					if (bShowDetail){
						printSalespersonHeader(out);
					}
				}
				
				//Update the variables:
				sCurrentServiceType = rs.getString("SERVICETYPE");
				sCurrentServiceTypeDescription = rs.getString("SERVICETYPEDESC");
		    	sCurrentSalesGroup = sSalesGroupFromData;
		    	sCurrentLocation = rs.getString(SMTableinvoiceheaders.sLocation);
		    	sCurrentSalesperson = rs.getString(SMTableinvoiceheaders.sSalesperson);
		    	sCurrentSalespersonName = rs.getString("SALESPERSONNAME");
		    	if (sCurrentSalespersonName == null){
		    		sCurrentSalespersonName = "";
		    	}
		    	sCurrentInvoiceNumber = rs.getString(SMTableinvoiceheaders.sInvoiceNumber);

		    	//Update the invoice variables:
				sCurrentBillToName = rs.getString(SMTableinvoiceheaders.sBillToName);
				datCurrentInvoice = rs.getDate(SMTableinvoiceheaders.datInvoiceDate);
				sCurrentOrderNumber = rs.getString(SMTableinvoiceheaders.sOrderNumber);
				bdCurrentInvoiceTotal = bdCurrentInvoiceTotal.add(bdLinePrice);
			
			}
			rs.close();
    	}catch (SQLException e){
    		m_sErrorMessage = "Error reading resultset - " + e.getMessage();
    		return false;
    	}
    	
		//Print the last invoice footer, if there was at least one invoice:
		if (sCurrentInvoiceNumber.compareToIgnoreCase("") != 0){
			if (bShowDetail){
				printInvoiceFooter(
					sCurrentSalesGroup,
					sCurrentServiceTypeDescription,
					sCurrentLocation,
					sCurrentSalesperson,
					sCurrentInvoiceNumber.trim(),
					sCurrentBillToName,
					sCurrentTransactionType,
					datCurrentInvoice,
					sCurrentOrderNumber.trim(),
					bdCurrentInvoiceTotal,
					bViewInvoicePermitted,
					bViewOrderPermitted,
					sDBID,
					out,
					context
				);
			}
			bdCurrentInvoiceTotal = BigDecimal.ZERO;
			
			if (sCurrentTransactionType.compareToIgnoreCase("IN") == 0){
				lTotalNumberOfInvoices++;
				lNumberOfInvoicesForSalesperson++;
				lNumberOfInvoicesForLocation++;
				lNumberOfInvoicesForServiceType++;
				lNumberOfInvoicesForSalesGroup++;
			}
			if (sCurrentTransactionType.compareToIgnoreCase("CR") == 0){
				lTotalNumberOfCreditNotes++;
				lNumberOfCreditNotesForSalesperson++;
				lNumberOfCreditNotesForLocation++;
				lNumberOfCreditNotesForServiceType++;
				lNumberOfCreditNotesForSalesGroup++;
			}
		}

		//Print the salesperson footer if there was at least one salesperson:
		if (sCurrentSalesperson.compareToIgnoreCase("") != 0){
			BigDecimal bdAverageInvoiceAmountForSalesperson = BigDecimal.ZERO;
			BigDecimal bdAverageCreditNoteAmountForSalesperson = BigDecimal.ZERO;
			
			if (lNumberOfInvoicesForSalesperson > 0){
				bdAverageInvoiceAmountForSalesperson = bdInvoiceAmountForSalesperson.divide(BigDecimal.valueOf(lNumberOfInvoicesForSalesperson), 2, RoundingMode.HALF_UP);
			}else{
				bdAverageInvoiceAmountForSalesperson = BigDecimal.ZERO;
			}
			if (lNumberOfCreditNotesForSalesperson > 0){
				bdAverageCreditNoteAmountForSalesperson = bdCreditNoteAmountForSalesperson.divide(BigDecimal.valueOf(lNumberOfCreditNotesForSalesperson), 2, RoundingMode.HALF_UP);
			}else{
				bdAverageCreditNoteAmountForSalesperson = BigDecimal.ZERO;
			}
			printSalespersonFooter(
				lNumberOfInvoicesForSalesperson, 
				bdAverageInvoiceAmountForSalesperson,
				lNumberOfCreditNotesForSalesperson, 
				bdAverageCreditNoteAmountForSalesperson,
				sCurrentSalesperson,
				sCurrentSalespersonName,
				sCurrentLocation,
				sCurrentServiceTypeDescription,
				sCurrentSalesGroup,
				bdTotalAmountForSalesperson,
				bShowDetail,
				out,
				conn
			);
		}

		//Process the location footer if there was at least one location:
		if (sCurrentLocation.compareToIgnoreCase("") != 0){
			BigDecimal bdAverageInvoiceAmountForLocation = BigDecimal.ZERO;
			BigDecimal bdAverageCreditNoteAmountForLocation = BigDecimal.ZERO;
			
			if (lNumberOfInvoicesForLocation > 0){
				bdAverageInvoiceAmountForLocation = bdInvoiceAmountForLocation.divide(BigDecimal.valueOf(lNumberOfInvoicesForLocation), 2, RoundingMode.HALF_UP);
			}else{
				bdAverageInvoiceAmountForLocation = BigDecimal.ZERO;
			}
			if (lNumberOfCreditNotesForLocation > 0){
				bdAverageCreditNoteAmountForLocation = bdCreditNoteAmountForLocation.divide(BigDecimal.valueOf(lNumberOfCreditNotesForLocation), 2, RoundingMode.HALF_UP);
			}else{
				bdAverageCreditNoteAmountForLocation = BigDecimal.ZERO;
			}
			printLocationFooter(
					lNumberOfInvoicesForLocation, 
					bdAverageInvoiceAmountForLocation,
					lNumberOfCreditNotesForLocation, 
					bdAverageCreditNoteAmountForLocation,
					sCurrentLocation,
					sCurrentServiceTypeDescription,
					sCurrentSalesGroup,
					bdTotalAmountForLocation,
					out
					);
		}

		//Print the service type footer for any new service type
		if (sCurrentServiceType.compareToIgnoreCase("") != 0){
			BigDecimal bdAverageInvoiceAmountForServiceType = BigDecimal.ZERO;
			BigDecimal bdAverageCreditNoteAmountForServiceType = BigDecimal.ZERO;
			if (lNumberOfInvoicesForServiceType > 0){
				bdAverageInvoiceAmountForServiceType = bdInvoiceAmountForServiceType.divide(BigDecimal.valueOf(lNumberOfInvoicesForServiceType), 2, RoundingMode.HALF_UP);
			}else{
				bdAverageInvoiceAmountForServiceType = BigDecimal.ZERO;
			}
			if (lNumberOfCreditNotesForServiceType > 0){
				bdAverageCreditNoteAmountForServiceType = bdCreditNoteAmountForServiceType.divide(BigDecimal.valueOf(lNumberOfCreditNotesForServiceType), 2, RoundingMode.HALF_UP);
			}else{
				bdAverageCreditNoteAmountForServiceType = BigDecimal.ZERO;
			}

			printServiceTypeFooter(
					lNumberOfInvoicesForServiceType, 
					bdAverageInvoiceAmountForServiceType,
					lNumberOfCreditNotesForServiceType, 
					bdAverageCreditNoteAmountForServiceType,
					sCurrentServiceTypeDescription,
					sCurrentSalesGroup,
					bdTotalAmountForServiceType,
					out
					);
		}
				
		//Print the sales group footer if there was at least one sales group:
		if (sCurrentSalesGroup.compareToIgnoreCase("") != 0){
			BigDecimal bdAverageInvoiceAmountForSalesGroup = BigDecimal.ZERO;
			BigDecimal bdAverageCreditNoteAmountForSalesGroup = BigDecimal.ZERO;
			
			if (lNumberOfInvoicesForSalesGroup > 0){
				bdAverageInvoiceAmountForSalesGroup = bdInvoiceAmountForSalesGroup.divide(BigDecimal.valueOf(lNumberOfInvoicesForSalesGroup), 2, RoundingMode.HALF_UP);
			}else{
				bdAverageInvoiceAmountForSalesGroup = BigDecimal.ZERO;
			}
			if (lNumberOfCreditNotesForSalesGroup > 0){
				bdAverageCreditNoteAmountForSalesGroup = bdCreditNoteAmountForSalesGroup.divide(BigDecimal.valueOf(lNumberOfCreditNotesForSalesGroup), 2, RoundingMode.HALF_UP);
			}else{
				bdAverageCreditNoteAmountForSalesGroup = BigDecimal.ZERO;
			}
			printSalesGroupFooter(
					lNumberOfInvoicesForSalesGroup, 
					bdAverageInvoiceAmountForSalesGroup,
					lNumberOfCreditNotesForSalesGroup, 
					bdAverageCreditNoteAmountForSalesGroup,
					sCurrentSalesGroup,
					bdTotalAmountForSalesGroup,
					out
					);
		}				
		
		//Print the grand totals:
		if (lTotalNumberOfInvoices > 0){
			bdTotalAverageInvoiceAmount = bdTotalInvoiceAmount.divide(BigDecimal.valueOf(lTotalNumberOfInvoices), 2, RoundingMode.HALF_UP);
		}else{
			bdTotalAverageInvoiceAmount = BigDecimal.ZERO;
		}
		if (lTotalNumberOfCreditNotes > 0){
			bdTotalAverageCreditNoteAmount = bdTotalCreditNoteAmount.divide(BigDecimal.valueOf(lTotalNumberOfCreditNotes), 2, RoundingMode.HALF_UP);
		}else{
			bdTotalAverageCreditNoteAmount = BigDecimal.ZERO;
		}

		printReportFooter(
				lTotalNumberOfInvoices, 
				bdTotalAverageInvoiceAmount,
				lTotalNumberOfCreditNotes, 
				bdTotalAverageCreditNoteAmount,
				bdTotalAmount,
				hmServiceTypeTotalAmmounts,
				out
				);
		return true;
	}
	private void printInvoiceFooter(
		String sSalesGroup,
		String sServiceType,
		String sLocation,
		String sSalesperson,
		String sInvoiceNumber,
		String sBillToName,
		String sTransactionType,
		java.sql.Date datInvoice,
		String sOrderNumber,
		BigDecimal bdInvoiceTotal,
		boolean bViewInvoicePermitted,
		boolean bViewOrderPermitted,
		String sDBID,
		PrintWriter out,
		ServletContext context
		){
		
		String sInvoiceNumberLink = "";
		if (bViewInvoicePermitted){
			sInvoiceNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMPrintInvoice?InvoiceNumberFrom=" + sInvoiceNumber 
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + sInvoiceNumber + "</A>";
		}else{
			sInvoiceNumberLink = sInvoiceNumber;
		}
		String sOrderNumberLink = "";
		if (bViewOrderPermitted){
			sOrderNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" + sOrderNumber 
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + sOrderNumber + "</A>";
		}else{
			sOrderNumberLink = sOrderNumber;
		}
		out.println(
				  "<TR>\n"
            	+     "<TD ALIGN=LEFT VALIGN=BOTTOM><FONT SIZE=2>" + sSalesGroup + "</FONT></TD>\n"
            	+     "<TD ALIGN=LEFT VALIGN=BOTTOM><FONT SIZE=2>" + sServiceType + "</FONT></TD>\n"
            	+     "<TD ALIGN=LEFT VALIGN=BOTTOM><FONT SIZE=2>" + sLocation + "</FONT></TD>\n"
            	+     "<TD ALIGN=LEFT VALIGN=BOTTOM><FONT SIZE=2>" + sSalesperson + "</FONT></TD>\n"
			    +     "<TD ALIGN=LEFT VALIGN=BOTTOM><FONT SIZE=2>" + sInvoiceNumberLink + "</FONT></TD>\n"
			    +     "<TD ALIGN=LEFT VALIGN=BOTTOM><FONT SIZE=2>" + sBillToName + "</FONT></TD>\n"
			    +     "<TD ALIGN=LEFT VALIGN=BOTTOM><FONT SIZE=2>" + sTransactionType + "</FONT></TD>\n"
			    +     "<TD ALIGN=LEFT VALIGN=BOTTOM><FONT SIZE=2>" 
			    +       clsDateAndTimeConversions.utilDateToString(datInvoice, "MM/dd/yyyy")+ "</FONT></TD>\n"
				+     "<TD ALIGN=LEFT VALIGN=BOTTOM><FONT SIZE=2>" + sOrderNumberLink + "</FONT></TD>\n"
				+     "<TD ALIGN=RIGHT VALIGN=BOTTOM><FONT SIZE=2>" 
				+       clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdInvoiceTotal) + "</FONT></TD>\n"
				+  "</TR>\n"
				);	
	}
	private void printSalespersonHeader(
			PrintWriter out
			){
		
		out.println(
				"<TABLE BORDER=0 WIDTH = 100%>\n"
				+   "<TR>\n"
						
				+     "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=6%><B><U><FONT SIZE=2>Sales&nbsp;Grp</FONT></U></B></TD>\n"
				+     "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=6%><B><U><FONT SIZE=2>Service Type</FONT></U></B></TD>\n"
				+     "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=3%><B><U><FONT SIZE=2>Loc</FONT></U></B></TD>\n"
				+     "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=4%><B><U><FONT SIZE=2>Sales</FONT></U></B></TD>\n"
			    +     "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=5%><B><U><FONT SIZE=2>Doc&nbsp;#</FONT></U></B></TD>\n"
			    +     "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=32%><B><U><FONT SIZE=2>Bill&nbsp;to</FONT></U></B></TD>\n"
			    +     "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=6%><B><U><FONT SIZE=2>Type</FONT></U></B></TD>\n"
			    +     "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=10%><B><U><FONT SIZE=2>Inv.&nbsp;date</FONT></U></B></TD>\n"
				+     "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=8%><B><U><FONT SIZE=2>Order&nbsp;#</FONT></U></B></TD>\n"
				+     "<TD ALIGN=RIGHT VALIGN=BOTTOM WIDTH=12%><B><U><FONT SIZE=2>Inv.&nbsp;amt</FONT></U></B></TD>\n"
				+   "</TR>\n"
				);
	}

	private void printSalespersonFooter(
			long lNumberOfInvoicesForSalesperson, 
			BigDecimal bdAverageInvoiceAmountForSalesperson,
			long lNumberOfCreditNotesForSalesperson, 
			BigDecimal bdAverageCreditNoteAmountForSalesperson,
			String sSalesperson,
			String sSalespersonName,
			String sLocation,
			String sServiceType,
			String sSalesGroup,
			BigDecimal bdTotalAmountForSalesperson,
			boolean bShowDetail,
			PrintWriter out,
			Connection conn
			){

		//First, end the salesperson header table:
		if (bShowDetail){
			out.println("</TABLE>\n");
		}
		
		out.println("<TABLE class = \""+SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER_COLLAPSE+"\" WIDTH = 100%>\n"
			+   "<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_GREY + " \" >\n"
			+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=45%><FONT SIZE=2><B>Invoices for sales group '" + sSalesGroup + "', " + sServiceType 
			+       ", location " + sLocation + ", salesperson " + sSalesperson + "&nbsp;" 
		    +       sSalespersonName + ":</B></FONT></TD>\n"
			+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=5%><FONT SIZE=2><B>" + Long.toString(lNumberOfInvoicesForSalesperson) + "</B></FONT></TD>\n"
			+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=40%><FONT SIZE=2><B>Avg. invoice amount:</B></FONT></TD>\n"
			+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=10%><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAverageInvoiceAmountForSalesperson) + "</B></FONT></TD>\n"
			+   "</TR>\n"
			+   "<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_GREY + " \" >\n"
			+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=45%><FONT SIZE=2><B>Credit notes for sales group '" + sSalesGroup + "', " + sServiceType
			+       ", location " + sLocation + ", salesperson " + sSalesperson + "&nbsp;"
			+       sSalespersonName + ":</B></FONT></TD>\n"
			+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=5%><FONT SIZE=2><B>" + Long.toString(lNumberOfCreditNotesForSalesperson) + "</B></FONT></TD>\n"
			+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=40%><FONT SIZE=2><B>Avg. credit note amount:</B></FONT></TD>\n"
			+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=10%><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAverageCreditNoteAmountForSalesperson) + "</B></FONT></TD>\n"
			+   "</TR>\n"
			+   "<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_GREY + " \" >\n"
			+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" colspan=\"3\" ALIGN=RIGHT><FONT SIZE=2><B>Total for " + sServiceType
			+       ", location " + sLocation + ", salesperson " + sSalesperson + "&nbsp;"
			+       sSalespersonName + ":</B></FONT></TD>\n"
			+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalAmountForSalesperson) + "</B></FONT></TD>\n"
			+   "</TR>\n"
			+ "</TABLE>\n" //Deleted an extra <TR> 
			);
	}
	private void printLocationFooter(
			long lNumberOfInvoicesForLocation, 
			BigDecimal bdAverageInvoiceAmountForLocation,
			long lNumberOfCreditNotesForLocation, 
			BigDecimal bdAverageCreditNoteAmountForLocation,
			String sLocation,
			String sServiceType,
			String sSalesGroup,
			BigDecimal bdTotalAmountForLocation,
			PrintWriter out
			){
		out.println("<TABLE class = \""+SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER_COLLAPSE+"\" WIDTH = 100%>\n"
				+   "<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_ORANGE + " \" >\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=45%><FONT SIZE=2><B>Invoices for sales group '" + sSalesGroup + "', " + sServiceType + ", location " + sLocation + "</B></FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=5%><FONT SIZE=2><B>" + Long.toString(lNumberOfInvoicesForLocation) + "</B></FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=40%><FONT SIZE=2><B>Avg. invoice amount</B>:</FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=10%><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAverageInvoiceAmountForLocation) + "</B></FONT></TD>\n"
				+   "</TR>\n"
				+   "<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_ORANGE + " \">\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\"ALIGN=RIGHT WIDTH=45%><FONT SIZE=2><B>Credit notes for sales group '" + sSalesGroup + "', " + sServiceType + ", location " + sLocation + "</B>:</FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=5%><FONT SIZE=2><B>" + Long.toString(lNumberOfCreditNotesForLocation) + "</B></FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=40%><FONT SIZE=2 ><B>Avg. credit note amount</B>:</FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=10%><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAverageCreditNoteAmountForLocation) + "</B></FONT></TD>\n"
				+   "</TR>\n"
				+   "<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_ORANGE + " \" >\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" colspan=\"3\" ALIGN=RIGHT><FONT SIZE=2><B>Total for " + sServiceType + ", location " + sLocation + "</B>:</FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalAmountForLocation) + "</B></FONT></TD>\n"
				+   "</TR>\n"
				+ "</TABLE>\n"//Deleted <TR>
				);
	}
	private void printServiceTypeFooter(
			long lNumberOfInvoicesForSeviceType, 
			BigDecimal bdAverageInvoiceAmountForServiceType,
			long lNumberOfCreditNotesForServiceType, 
			BigDecimal bdAverageCreditNoteAmountForServiceType,
			String sServiceType,
			String sSalesGroup,
			BigDecimal bdTotalAmountForServiceType,
			PrintWriter out
			){
		out.println("<TABLE class = \""+SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER_COLLAPSE+"\" WIDTH = 100%>\n"
				+   "<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTPINK + " \" >\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=45%><FONT SIZE=2 ><B>Invoices for sales group '" + sSalesGroup + "', " + sServiceType + "</B>: </FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=5%><FONT SIZE=2><B>" + Long.toString(lNumberOfInvoicesForSeviceType) + "</B></FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=4%><FONT SIZE=2><B>Avg. invoice amount</B>:</FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=10%><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAverageInvoiceAmountForServiceType) + "</B></FONT></TD>\n"
				+   "</TR>\n"
				+   "<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTPINK + " \" >\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=45%><FONT SIZE=2><B>Credit notes for sales group '" + sSalesGroup + "', " + sServiceType + "</B>:</FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=5%><FONT SIZE=2><B>" + Long.toString(lNumberOfCreditNotesForServiceType) + "</B></FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=40%><FONT SIZE=2><B>Avg. credit note amount</B>:</FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=10%><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAverageCreditNoteAmountForServiceType) + "</B></FONT></TD>\n"
				+   "</TR>\n"
				+   "<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTPINK + " \" >\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" colspan=\"3\" ALIGN=RIGHT><FONT SIZE=2><B>Total for " + sServiceType + "</B>:</FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalAmountForServiceType) + "</B></FONT></TD>\n"
				+   "</TR>\n"
				+ "</TABLE>\n" //Deleted <TR>
				);
	}

	private void printSalesGroupFooter(
			long lNumberOfInvoicesForSalesGroup, 
			BigDecimal bdAverageInvoiceAmountForSalesGroup,
			long lNumberOfCreditNotesForSalesGroup, 
			BigDecimal bdAverageCreditNoteAmountForSalesGroup,
			String sSalesGroup,
			BigDecimal bdTotalAmountForSalesGroup,
			PrintWriter out
			){
		out.println("<TABLE class = \""+SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER_COLLAPSE+"\" WIDTH = 100%>\n"
				+   "<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=45%><FONT SIZE=2><B>Invoices for sales group '" + sSalesGroup + "'</B>: </FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=5%><FONT SIZE=2><B>" + Long.toString(lNumberOfInvoicesForSalesGroup) + "</B></FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\"ALIGN=RIGHT WIDTH=40%><FONT SIZE=2><B>Avg. invoice amount</B>:</FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=10%><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAverageInvoiceAmountForSalesGroup) + "</B></FONT></TD>\n"
				+   "</TR>\n"
				+   "<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=45%><FONT SIZE=2><B>Credit notes for sales group '" + sSalesGroup + "'</B>:</FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=5%><FONT SIZE=2><B>" + Long.toString(lNumberOfCreditNotesForSalesGroup) + "</B></FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=40%><FONT SIZE=2><B>Avg. credit note amount</B>:</FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=10%><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAverageCreditNoteAmountForSalesGroup) + "</B></FONT></TD>\n"
				+   "</TR>\n"
				+   "<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" colspan=\"3\" ALIGN=RIGHT><FONT SIZE=2><B>Total for sales group '" + sSalesGroup + "'</B>:</FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalAmountForSalesGroup) + "</B></FONT></TD>\n"
				+   "</TR>\n"
				+   "</TABLE>\n" //Deleted <TR>
				);
	}
	private void printReportFooter(
			long lTotalNumberOfInvoices, 
			BigDecimal bdTotalAverageInvoiceAmount,
			long lTotalNumberOfCreditNotes, 
			BigDecimal bdTotalAverageCreditNoteAmount,
			BigDecimal bdTotalAmount,
			 Map< String,BigDecimal > hmTotalServiceTypeTotals,
			PrintWriter out
			){
		
		
		out.println("<TABLE class = \""+SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER_COLLAPSE+"\" BORDER=0 WIDTH = 100%>\n"
				+   "<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_YELLOW + " \" >\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=45%><FONT SIZE=2><B>Total Invoices:</B></FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=5%><FONT SIZE=2><B>" + Long.toString(lTotalNumberOfInvoices) + "</B></FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=40%><FONT SIZE=2><B>Avg. invoice amount:</B></FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=10%><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalAverageInvoiceAmount) + "</B></FONT></TD>\n"
				+   "</TR>\n"
				+   "<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_YELLOW + " \" >\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=45%><FONT SIZE=2><B>Total Credit notes:</B></FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=5%><FONT SIZE=2><B>" + Long.toString(lTotalNumberOfCreditNotes) + "</B></FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=40%><FONT SIZE=2><B>Avg. credit note amount:</B></FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=10%><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalAverageCreditNoteAmount) + "</B></FONT></TD>\n"
				+   "</TR>\n");
				
				for(String sServiceTypeDescription : hmTotalServiceTypeTotals.keySet()) {
					out.println("<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_YELLOW + " \" >\n"
						+   "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" colspan=\"3\" ALIGN=RIGHT><FONT SIZE=2><B>" + sServiceTypeDescription + " Total:</B></FONT></TD>\n"
						+   "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(hmTotalServiceTypeTotals.get(sServiceTypeDescription)) + "</B></FONT></TD>\n"
						+   "</TR>\n");
				}
		
				out.println( "<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_YELLOW + " \" >\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" colspan=\"3\" ALIGN=RIGHT><FONT SIZE=2><B>Company Total:</B></FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalAmount) + "</B></FONT></TD>\n"
				+   "</TR>\n"
				+ "</TABLE>\n"
				);
	}
	
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}

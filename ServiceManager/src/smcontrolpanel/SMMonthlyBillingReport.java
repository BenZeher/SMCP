package smcontrolpanel;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableinvoicedetails;
import SMDataDefinition.SMTableinvoiceheaders;
import SMDataDefinition.SMTablesalesgroups;
import SMDataDefinition.SMTablesalesperson;
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
			ArrayList<String> sSalesGroupList,
			boolean bIncludeSales,
			boolean bIncludeService,
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
    		
            + ", " + "IF((" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sServiceTypeCode
            + " = 'SH0001') OR (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sServiceTypeCode
            + " = 'SH0003'), 'Service', 'Sales') as SaleType"
            
            + ", " + "IF((" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sServiceTypeCode
            + " = 'SH0001') OR (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sServiceTypeCode
            + " = 'SH0002'), 'Residential', 'Commercial') as SiteType"
            
            + ", " + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupCode + " AS SALESGROUP"
            
            + " FROM"
            + " " + SMTableinvoicedetails.TableName + " LEFT JOIN " + SMTableinvoiceheaders.TableName + " ON "
            + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber + " = "
            + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber
            
    	    + " LEFT JOIN " + SMTablesalesgroups.TableName + " ON " + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.iSalesGroupId
    	    + " = " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iSalesGroup
    	    
    	    //Link salesperson table:
    	    + " LEFT JOIN " + SMTablesalesperson.TableName + " ON " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sSalesperson
    	    	+ " = " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode
            
            + " WHERE ("
            
            + "(" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate + " >= '" + sStartingDate + " 00:00:00')"
            + " AND (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate + " <= '" + sEndingDate + " 23:59:59')";
            
        	//SH0002 and SH0004 are sales
            if(!bIncludeSales){
                SQL = SQL + " AND (" + SMTableinvoiceheaders.TableName + "." 
                	+ SMTableinvoiceheaders.sServiceTypeCode + " != 'SH0002')"
                	+ " AND (" + SMTableinvoiceheaders.TableName + "." 
                	+ SMTableinvoiceheaders.sServiceTypeCode + " != 'SH0004')";
            }
            
            //SH0001 and SH0003 are service
            if(!bIncludeService){
                SQL = SQL + " AND (" + SMTableinvoiceheaders.TableName + "." 
                	+ SMTableinvoiceheaders.sServiceTypeCode + " != 'SH0001')"
                	+ " AND (" + SMTableinvoiceheaders.TableName + "." 
                	+ SMTableinvoiceheaders.sServiceTypeCode + " != 'SH0003')";
            }

    		//Get the sales groups:
		    if (sSalesGroupList.size() > 0){
		    	SQL += " AND (";
	    		for (int i = 0; i < sSalesGroupList.size(); i++){
	    			if (i == 0){
	    				SQL += "(" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iSalesGroup + " = " 
	    					+ sSalesGroupList.get(i).substring(sSalesGroupList.get(i).indexOf(SMMonthlyBillingReportSelection.SALESGROUP_PARAM_SEPARATOR) + 1) + ")";
	    			}else{
	    				SQL += " OR (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iSalesGroup + " = " 
	    					+ sSalesGroupList.get(i).substring(sSalesGroupList.get(i).indexOf(SMMonthlyBillingReportSelection.SALESGROUP_PARAM_SEPARATOR) + 1) + ")";
	    			}
	    		}
	    		SQL = SQL + ")";
		    }
            
            SQL = SQL + ")"
            
            + " ORDER BY"
            + " SALESGROUP"
            + ", SiteType"
            + ", SaleType"
            + ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sLocation
            + ", " + SMTableinvoiceheaders.sSalesperson
            + ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber
            ;
    	//end SQL statement

        if (bDebugMode){
        	System.out.println(" [1346073529] In " + this.getClass() + " - main SQL = " + SQL);
        }
            
        //System.out.println("In " + this.toString() + ".processReport - main SQL = " + SQL);
		String sCurrentSalesServiceType = "";
    	String sCurrentCommericalResidentialType = "";
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

		//Variables for Commercial/Residential type:
		long lNumberOfInvoicesForCommOrResiType = 0L;
		BigDecimal bdInvoiceAmountForCommOrResiType = BigDecimal.ZERO;
		long lNumberOfCreditNotesForCommOrResiType = 0l;
		BigDecimal bdCreditNoteAmountForCommOrResiType = BigDecimal.ZERO;
		BigDecimal bdTotalAmountForCommOrResiType = BigDecimal.ZERO;
		
		//Variables for Sale/Service type:
		long lNumberOfInvoicesForSaleOrServiceType = 0L; 
		BigDecimal bdInvoiceAmountForSaleOrServiceType = BigDecimal.ZERO;
		long lNumberOfCreditNotesForSaleOrServiceType = 0L;
		BigDecimal bdCreditNoteAmountForSaleOrServiceType = BigDecimal.ZERO;
		BigDecimal bdTotalAmountForSaleOrServiceType = BigDecimal.ZERO;
		
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
		BigDecimal bdTotalServiceAmount = BigDecimal.ZERO;
		BigDecimal bdTotalSalesAmount = BigDecimal.ZERO;
    	
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
							sCurrentCommericalResidentialType,
							sCurrentSalesServiceType,
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
						lNumberOfInvoicesForCommOrResiType++;
						lNumberOfInvoicesForSaleOrServiceType++;
						lNumberOfInvoicesForSalesGroup++;
					}
					if (sCurrentTransactionType.compareToIgnoreCase("CR") == 0){
						lTotalNumberOfCreditNotes++;
						lNumberOfCreditNotesForSalesperson++;
						lNumberOfCreditNotesForLocation++;
						lNumberOfCreditNotesForCommOrResiType++;
						lNumberOfCreditNotesForSaleOrServiceType++;
						lNumberOfCreditNotesForSalesGroup++;
					}
				}
				
	    		//Print the salesperson header for any new sales type OR site type OR location
				// OR salesperson:
				if (
					((rs.getString("SaleType").compareToIgnoreCase(sCurrentSalesServiceType) != 0)
					|| ((rs.getString("SiteType")).compareToIgnoreCase(sCurrentCommericalResidentialType) != 0)
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
						sCurrentCommericalResidentialType,
						sCurrentSalesServiceType,
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
	    		//Print the location footer for any new sales type OR site type OR location
				if (
					((rs.getString("SaleType").compareToIgnoreCase(sCurrentSalesServiceType) != 0)
					|| ((rs.getString("SiteType")).compareToIgnoreCase(sCurrentCommericalResidentialType) != 0)
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
							sCurrentCommericalResidentialType,
							sCurrentSalesServiceType,
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

				//Sale Type footer
	    		//Print the sale type footer for any new sales OR site type
				if (
						((rs.getString("SaleType").compareToIgnoreCase(sCurrentSalesServiceType) != 0)
						|| ((rs.getString("SiteType")).compareToIgnoreCase(sCurrentCommericalResidentialType) != 0))
					&& (sCurrentSalesServiceType.compareToIgnoreCase("") != 0)
				){
					BigDecimal bdAverageInvoiceAmountForSaleType = BigDecimal.ZERO;
					BigDecimal bdAverageCreditNoteAmountForSaleType = BigDecimal.ZERO;
					
					if (lNumberOfInvoicesForSaleOrServiceType > 0){
						bdAverageInvoiceAmountForSaleType = bdInvoiceAmountForSaleOrServiceType.divide(BigDecimal.valueOf(lNumberOfInvoicesForSaleOrServiceType), 2, RoundingMode.HALF_UP);
					}else{
						bdAverageInvoiceAmountForSaleType = BigDecimal.ZERO;
					}
					if (lNumberOfCreditNotesForSaleOrServiceType > 0){
						bdAverageCreditNoteAmountForSaleType = bdCreditNoteAmountForSaleOrServiceType.divide(BigDecimal.valueOf(lNumberOfCreditNotesForSaleOrServiceType), 2, RoundingMode.HALF_UP);
					}else{
						bdAverageCreditNoteAmountForSaleType = BigDecimal.ZERO;
					}
					printSaleTypeFooter(
							lNumberOfInvoicesForSaleOrServiceType, 
							bdAverageInvoiceAmountForSaleType,
							lNumberOfCreditNotesForSaleOrServiceType, 
							bdAverageCreditNoteAmountForSaleType,
							sCurrentSalesServiceType,
							sCurrentCommericalResidentialType,
							sCurrentSalesGroup,
							bdTotalAmountForSaleOrServiceType,
							out
							);
					//Initialize the sale type variables:
					lNumberOfInvoicesForSaleOrServiceType = 0L; 
					lNumberOfCreditNotesForSaleOrServiceType = 0L; 
					bdInvoiceAmountForSaleOrServiceType = BigDecimal.ZERO;
					bdCreditNoteAmountForSaleOrServiceType = BigDecimal.ZERO;
					bdTotalAmountForSaleOrServiceType = BigDecimal.ZERO;
				}
				
				//Site type footer
	    		//Print the site type footer for any new site type
				if (
					(rs.getString("SiteType").compareToIgnoreCase(sCurrentCommericalResidentialType) != 0)
					&& (sCurrentCommericalResidentialType.compareToIgnoreCase("") != 0)
				){
					BigDecimal bdAverageInvoiceAmountForSiteType = BigDecimal.ZERO;
					BigDecimal bdAverageCreditNoteAmountForSiteType = BigDecimal.ZERO;
					
					if (lNumberOfInvoicesForCommOrResiType > 0){
						bdAverageInvoiceAmountForSiteType = bdInvoiceAmountForCommOrResiType.divide(BigDecimal.valueOf(lNumberOfInvoicesForCommOrResiType), 2, RoundingMode.HALF_UP);
					}else{
						bdAverageInvoiceAmountForSiteType = BigDecimal.ZERO;
					}
					if (lNumberOfCreditNotesForCommOrResiType > 0){
						bdAverageCreditNoteAmountForSiteType = bdCreditNoteAmountForCommOrResiType.divide(BigDecimal.valueOf(lNumberOfCreditNotesForCommOrResiType), 2, RoundingMode.HALF_UP);
					}else{
						bdAverageCreditNoteAmountForSiteType = BigDecimal.ZERO;
					}
					printSiteTypeFooter(
							lNumberOfInvoicesForCommOrResiType, 
							bdAverageInvoiceAmountForSiteType,
							lNumberOfCreditNotesForCommOrResiType, 
							bdAverageCreditNoteAmountForSiteType,
							sCurrentCommericalResidentialType,
							sCurrentSalesGroup,
							bdTotalAmountForCommOrResiType,
							out
							);
					//Initialize the site type variables:
					lNumberOfInvoicesForCommOrResiType = 0L; 
					lNumberOfCreditNotesForCommOrResiType = 0L; 
					bdInvoiceAmountForCommOrResiType = BigDecimal.ZERO;
					bdCreditNoteAmountForCommOrResiType = BigDecimal.ZERO;
					bdTotalAmountForCommOrResiType = BigDecimal.ZERO;
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
					bdInvoiceAmountForCommOrResiType = bdInvoiceAmountForCommOrResiType.add(bdLinePrice);
					bdInvoiceAmountForSaleOrServiceType = bdInvoiceAmountForSaleOrServiceType.add(bdLinePrice);
					bdInvoiceAmountForSalesGroup = bdInvoiceAmountForSalesGroup.add(bdLinePrice);
					sCurrentTransactionType = "IN";
				}
				if (rs.getInt(SMTableinvoiceheaders.iTransactionType) == SMTableinvoiceheaders.TYPE_CREDIT){
					bdTotalCreditNoteAmount = bdTotalCreditNoteAmount.add(bdLinePrice);
					bdCreditNoteAmountForSalesperson = bdCreditNoteAmountForSalesperson.add(bdLinePrice);
					bdCreditNoteAmountForLocation = bdCreditNoteAmountForLocation.add(bdLinePrice);
					bdCreditNoteAmountForCommOrResiType = bdCreditNoteAmountForCommOrResiType.add(bdLinePrice);
					bdCreditNoteAmountForSaleOrServiceType = bdCreditNoteAmountForSaleOrServiceType.add(bdLinePrice);
					bdCreditNoteAmountForSalesGroup = bdCreditNoteAmountForSalesGroup.add(bdLinePrice);
					sCurrentTransactionType = "CR";
				}
				
				bdTotalAmount = bdTotalAmount.add(bdLinePrice);
				bdTotalAmountForSalesperson = bdTotalAmountForSalesperson.add(bdLinePrice);
				bdTotalAmountForLocation = bdTotalAmountForLocation.add(bdLinePrice);
				bdTotalAmountForCommOrResiType = bdTotalAmountForCommOrResiType.add(bdLinePrice);
				bdTotalAmountForSaleOrServiceType = bdTotalAmountForSaleOrServiceType.add(bdLinePrice);
				bdTotalAmountForSalesGroup = bdTotalAmountForSalesGroup.add(bdLinePrice);
				
				if (rs.getString("SaleType").compareToIgnoreCase("Service") == 0){
					bdTotalServiceAmount = bdTotalServiceAmount.add(bdLinePrice);
				}else{
					bdTotalSalesAmount = bdTotalSalesAmount.add(bdLinePrice);
				}
			
				//If either the group, sale type or the site type or the location or the salesperson changes,
				//print a salesperson header:
				//If the current salesperson is blank, print it
				if (
						(sSalesGroupFromData.compareToIgnoreCase(sCurrentSalesGroup) != 0)
						|| (rs.getString("SaleType").compareToIgnoreCase(sCurrentSalesServiceType) != 0)
						|| ((rs.getString("SiteType")).compareToIgnoreCase(sCurrentCommericalResidentialType) != 0)
						|| ((rs.getString(SMTableinvoiceheaders.sLocation)).compareToIgnoreCase(sCurrentLocation) != 0)
						|| ((rs.getString(SMTableinvoiceheaders.sSalesperson)).compareToIgnoreCase(sCurrentSalesperson) != 0)
					){

					if (bShowDetail){
						printSalespersonHeader(out);
					}
				}
				
				//Update the variables:
				sCurrentSalesServiceType = rs.getString("SaleType");
		    	sCurrentCommericalResidentialType = rs.getString("SiteType");
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
					sCurrentCommericalResidentialType,
					sCurrentSalesServiceType,
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
				lNumberOfInvoicesForCommOrResiType++;
				lNumberOfInvoicesForSaleOrServiceType++;
				lNumberOfInvoicesForSalesGroup++;
			}
			if (sCurrentTransactionType.compareToIgnoreCase("CR") == 0){
				lTotalNumberOfCreditNotes++;
				lNumberOfCreditNotesForSalesperson++;
				lNumberOfCreditNotesForLocation++;
				lNumberOfCreditNotesForCommOrResiType++;
				lNumberOfCreditNotesForSaleOrServiceType++;
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
				sCurrentCommericalResidentialType,
				sCurrentSalesServiceType,
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
					sCurrentCommericalResidentialType,
					sCurrentSalesServiceType,
					sCurrentSalesGroup,
					bdTotalAmountForLocation,
					out
					);
		}

		//Print the sale type footer for any new sales type
		if (sCurrentSalesServiceType.compareToIgnoreCase("") != 0){
			BigDecimal bdAverageInvoiceAmountForSaleType = BigDecimal.ZERO;
			BigDecimal bdAverageCreditNoteAmountForSaleType = BigDecimal.ZERO;
			
			if (lNumberOfInvoicesForSaleOrServiceType > 0){
				bdAverageInvoiceAmountForSaleType = bdInvoiceAmountForSaleOrServiceType.divide(BigDecimal.valueOf(lNumberOfInvoicesForSaleOrServiceType), 2, RoundingMode.HALF_UP);
			}else{
				bdAverageInvoiceAmountForSaleType = BigDecimal.ZERO;
			}
			if (lNumberOfCreditNotesForSaleOrServiceType > 0){
				bdAverageCreditNoteAmountForSaleType = bdCreditNoteAmountForSaleOrServiceType.divide(BigDecimal.valueOf(lNumberOfCreditNotesForSaleOrServiceType), 2, RoundingMode.HALF_UP);
			}else{
				bdAverageCreditNoteAmountForSaleType = BigDecimal.ZERO;
			}
			printSaleTypeFooter(
					lNumberOfInvoicesForSaleOrServiceType, 
					bdAverageInvoiceAmountForSaleType,
					lNumberOfCreditNotesForSaleOrServiceType, 
					bdAverageCreditNoteAmountForSaleType,
					sCurrentSalesServiceType,
					sCurrentCommericalResidentialType,
					sCurrentSalesGroup,
					bdTotalAmountForSaleOrServiceType,
					out
					);
		}
		
		//Print the site type footer if there was at least one site type:
		if (sCurrentCommericalResidentialType.compareToIgnoreCase("") != 0){
			BigDecimal bdAverageInvoiceAmountForSiteType = BigDecimal.ZERO;
			BigDecimal bdAverageCreditNoteAmountForSiteType = BigDecimal.ZERO;
			
			if (lNumberOfInvoicesForCommOrResiType > 0){
				bdAverageInvoiceAmountForSiteType = bdInvoiceAmountForCommOrResiType.divide(BigDecimal.valueOf(lNumberOfInvoicesForCommOrResiType), 2, RoundingMode.HALF_UP);
			}else{
				bdAverageInvoiceAmountForSiteType = BigDecimal.ZERO;
			}
			if (lNumberOfCreditNotesForCommOrResiType > 0){
				bdAverageCreditNoteAmountForSiteType = bdCreditNoteAmountForCommOrResiType.divide(BigDecimal.valueOf(lNumberOfCreditNotesForCommOrResiType), 2, RoundingMode.HALF_UP);
			}else{
				bdAverageCreditNoteAmountForSiteType = BigDecimal.ZERO;
			}
			printSiteTypeFooter(
					lNumberOfInvoicesForCommOrResiType, 
					bdAverageInvoiceAmountForSiteType,
					lNumberOfCreditNotesForCommOrResiType, 
					bdAverageCreditNoteAmountForSiteType,
					sCurrentCommericalResidentialType,
					sCurrentSalesGroup,
					bdTotalAmountForCommOrResiType,
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
				bdTotalServiceAmount,
				bdTotalSalesAmount,
				out
				);
		return true;
	}
	private void printInvoiceFooter(
		String sSalesGroup,
		String sCommOrResi,
		String sSalesOrService,
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
            	+     "<TD ALIGN=LEFT VALIGN=BOTTOM><FONT SIZE=2>" + sCommOrResi + "</FONT></TD>\n"
            	+     "<TD ALIGN=LEFT VALIGN=BOTTOM><FONT SIZE=2>" + sSalesOrService + "</FONT></TD>\n"
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
				+     "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=6%><B><U><FONT SIZE=2>Comm/Resi</FONT></U></B></TD>\n"
				+     "<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=8%><B><U><FONT SIZE=2>Sales/Service</FONT></U></B></TD>\n"
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
			String sSiteType,
			String sSaleType,
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
			+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=45%><FONT SIZE=2><B>Invoices for sales group '" + sSalesGroup + "', " + sSiteType + " " + sSaleType 
			+       ", location " + sLocation + ", salesperson " + sSalesperson + "&nbsp;" 
		    +       sSalespersonName + ":</B></FONT></TD>\n"
			+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=5%><FONT SIZE=2><B>" + Long.toString(lNumberOfInvoicesForSalesperson) + "</B></FONT></TD>\n"
			+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=40%><FONT SIZE=2><B>Avg. invoice amount:</B></FONT></TD>\n"
			+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=10%><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAverageInvoiceAmountForSalesperson) + "</B></FONT></TD>\n"
			+   "</TR>\n"
			+   "<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_GREY + " \" >\n"
			+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=45%><FONT SIZE=2><B>Credit notes for sales group '" + sSalesGroup + "', " + sSiteType + " " + sSaleType
			+       ", location " + sLocation + ", salesperson " + sSalesperson + "&nbsp;"
			+       sSalespersonName + ":</B></FONT></TD>\n"
			+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=5%><FONT SIZE=2><B>" + Long.toString(lNumberOfCreditNotesForSalesperson) + "</B></FONT></TD>\n"
			+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=40%><FONT SIZE=2><B>Avg. credit note amount:</B></FONT></TD>\n"
			+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=10%><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAverageCreditNoteAmountForSalesperson) + "</B></FONT></TD>\n"
			+   "</TR>\n"
			+   "<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_GREY + " \" >\n"
			+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" colspan=\"3\" ALIGN=RIGHT><FONT SIZE=2><B>Total for " + sSiteType + " " + sSaleType
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
			String sSiteType,
			String sSaleType,
			String sSalesGroup,
			BigDecimal bdTotalAmountForLocation,
			PrintWriter out
			){
		out.println("<TABLE class = \""+SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER_COLLAPSE+"\" WIDTH = 100%>\n"
				+   "<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_ORANGE + " \" >\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=45%><FONT SIZE=2><B>Invoices for sales group '" + sSalesGroup + "', " + sSiteType + " " + sSaleType + ", location " + sLocation + "</B></FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=5%><FONT SIZE=2><B>" + Long.toString(lNumberOfInvoicesForLocation) + "</B></FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=40%><FONT SIZE=2><B>Avg. invoice amount</B>:</FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=10%><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAverageInvoiceAmountForLocation) + "</B></FONT></TD>\n"
				+   "</TR>\n"
				+   "<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_ORANGE + " \">\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\"ALIGN=RIGHT WIDTH=45%><FONT SIZE=2><B>Credit notes for sales group '" + sSalesGroup + "', " + sSiteType +" "+ sSaleType + ", location " + sLocation + "</B>:</FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=5%><FONT SIZE=2><B>" + Long.toString(lNumberOfCreditNotesForLocation) + "</B></FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=40%><FONT SIZE=2 ><B>Avg. credit note amount</B>:</FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=10%><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAverageCreditNoteAmountForLocation) + "</B></FONT></TD>\n"
				+   "</TR>\n"
				+   "<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_ORANGE + " \" >\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" colspan=\"3\" ALIGN=RIGHT><FONT SIZE=2><B>Total for " + sSiteType + "   "+sSaleType + ", location " + sLocation + "</B>:</FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalAmountForLocation) + "</B></FONT></TD>\n"
				+   "</TR>\n"
				+ "</TABLE>\n"//Deleted <TR>
				);
	}
	private void printSaleTypeFooter(
			long lNumberOfInvoicesForSaleType, 
			BigDecimal bdAverageInvoiceAmountForSaleType,
			long lNumberOfCreditNotesForSaleType, 
			BigDecimal bdAverageCreditNoteAmountForSaleType,
			String sSaleType,
			String sSiteType,
			String sSalesGroup,
			BigDecimal bdTotalAmountForSaleType,
			PrintWriter out
			){
		out.println("<TABLE class = \""+SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER_COLLAPSE+"\" WIDTH = 100%>\n"
				+   "<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTPINK + " \" >\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=45%><FONT SIZE=2 ><B>Invoices for sales group '" + sSalesGroup + "', " + sSiteType + " " + sSaleType + "</B>: </FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=5%><FONT SIZE=2><B>" + Long.toString(lNumberOfInvoicesForSaleType) + "</B></FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=4%><FONT SIZE=2><B>Avg. invoice amount</B>:</FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=10%><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAverageInvoiceAmountForSaleType) + "</B></FONT></TD>\n"
				+   "</TR>\n"
				+   "<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTPINK + " \" >\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=45%><FONT SIZE=2><B>Credit notes for sales group '" + sSalesGroup + "', " + sSiteType + " " + sSaleType + "</B>:</FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=5%><FONT SIZE=2><B>" + Long.toString(lNumberOfCreditNotesForSaleType) + "</B></FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=40%><FONT SIZE=2><B>Avg. credit note amount</B>:</FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=10%><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAverageCreditNoteAmountForSaleType) + "</B></FONT></TD>\n"
				+   "</TR>\n"
				+   "<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTPINK + " \" >\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" colspan=\"3\" ALIGN=RIGHT><FONT SIZE=2><B>Total for " + sSiteType + " " + sSaleType + "</B>:</FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalAmountForSaleType) + "</B></FONT></TD>\n"
				+   "</TR>\n"
				+ "</TABLE>\n" //Deleted <TR>
				);
	}
	private void printSiteTypeFooter(
			long lNumberOfInvoicesForSiteType, 
			BigDecimal bdAverageInvoiceAmountForSiteType,
			long lNumberOfCreditNotesForSiteType, 
			BigDecimal bdAverageCreditNoteAmountForSiteType,
			String sSiteType,
			String sSalesGroup,
			BigDecimal bdTotalAmountForSiteType,
			PrintWriter out
			){
		out.println("<TABLE class = \""+SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER_COLLAPSE+"\" WIDTH = 100%>\n"
				+   "<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREEN + " \" >\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=45%><FONT SIZE=2><B>Invoices for sales group '" + sSalesGroup + "', " + sSiteType + "</B>: </FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=5%><FONT SIZE=2><B>" + Long.toString(lNumberOfInvoicesForSiteType) + "</B></FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=40%><FONT SIZE=2><B>Avg. invoice amount</B>:</FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=10%><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAverageInvoiceAmountForSiteType) + "</B></FONT></TD>\n"
				+   "</TR>\n"
				+   "<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREEN + " \" >\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=45%><FONT SIZE=2><B>Credit notes for sales group '" + sSalesGroup + "', " + sSiteType + "</B>:</FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=5%><FONT SIZE=2><B>" + Long.toString(lNumberOfCreditNotesForSiteType) + "</B></FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=40%><FONT SIZE=2><B>Avg. credit note amount</B>:</FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT WIDTH=10%><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAverageCreditNoteAmountForSiteType) + "</B></FONT></TD>\n"
				+   "</TR>\n"
				+   "<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREEN + " \" >\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" colspan=\"3\" ALIGN=RIGHT><FONT SIZE=2><B>Total for " + sSiteType + "</B>:</FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalAmountForSiteType) + "</B></FONT></TD>\n"
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
			BigDecimal bdServiceTotalAmount,
			BigDecimal bdSalesTotalAmount,
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
				+   "</TR>\n"
				
				+   "<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_YELLOW + " \" >\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" colspan=\"3\" ALIGN=RIGHT><FONT SIZE=2><B>Service Total:</B></FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdServiceTotalAmount) + "</B></FONT></TD>\n"
				+   "</TR>\n"
				
				+   "<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_YELLOW + " \" >\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" colspan=\"3\" ALIGN=RIGHT><FONT SIZE=2><B>Sales Total:</B></FONT></TD>\n"
				+     "<TD class = \""+SMMasterStyleSheetDefinitions.TABLE_CELL_COLLAPSE_BORDER+"\" ALIGN=RIGHT><FONT SIZE=2><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdSalesTotalAmount) + "</B></FONT></TD>\n"
				+   "</TR>\n"
								
				+   "<TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_YELLOW + " \" >\n"
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

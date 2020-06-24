package smcontrolpanel;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletContext;

import SMClasses.SMOrderHeader;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTablecostcenters;
import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableinvoicedetails;
import SMDataDefinition.SMTableinvoiceheaders;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTabletax;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsServletUtilities;

public class SMSalesTaxReport {
	
	//private static final String CELL_BORDER_COLOR = "#808080";
	private static final String SUBTOTAL_LINE_BG_COLOR = "#ccd9ff";
	private static final String SUBTOTAL_INVOICE__LINE_BG_COLOR = "#e6fffa";
	private static final String SUMMARY_TAX_JURISDICTION_TOTALS_BG_COLOR = "#e6fffa";
	private static final String SUMMARY_TAX_TYPE_TOTALS_BG_COLOR = "#ffe6f2";
	private static final String SUMMARY_COSTCENTER_TOTALS_BG_COLOR = "#f0f5f5";
	private static final String COSTCENTER_AND_TAX_SEPARATOR = "&nbsp;&nbsp;&nbsp;";
	private static final int MAX_LINE_BUFFER_COUNT = 100; //If we're just printing to the PrintWriter, we're not buffering
		//and we don't need this set to anything but one.  If we want to use it, we'll try it at 50.
	
	public void processReport(
		String sStartingDate,
		String sEndingDate,
		boolean bGroupByCostCenters,
		ArrayList<String>arrCostCenters,
		ArrayList<String>arrTaxJurisdictions,
		boolean bShowIndividualInvoiceLines,
		String sitemNumbertext,
		String sOrderNumbertext,
		ServletContext context,
		String sDBID,
		String sUserID,
		PrintWriter out,
		String sLicenseModuleLevel
	) throws Exception{
		
		//Test the dates:
		if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sStartingDate)){
			throw new Exception ("Invalid starting date: '" + sStartingDate + "'.");
		}
		if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sEndingDate)){
			throw new Exception ("Invalid clearing date: '" + sEndingDate + "'.");
		}
		
		//Test the cost centers:
		if (bGroupByCostCenters){
			if (arrCostCenters.size() == 0){
				throw new Exception("You've chosen to group by cost centers, but they are no cost centers selected");
			}
		}
		
		//Test the jurisdictions:
		if (arrTaxJurisdictions.size() == 0){
			throw new Exception("You have to select at least one tax jurisdiction");
		}
		
		out.println("\n" + sStyleScripts() + "\n");
		
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBID, 
				"MySQL", 
				clsServletUtilities.getFullClassName(this.toString()+ ".printInvoiceDetailReport - userID: " + sUserID)
			);
		} catch (Exception e1) {
			throw new Exception ("Error [1521833643] - could not get database connection - '" + e1.getMessage());
		}
		
		try {
			/*
			out.println(printInvoiceDetailReport(
				sStartingDate,
				sEndingDate,
				bGroupByCostCenters,
				bShowIndividualInvoiceLines,
				arrCostCenters,
				arrTaxJurisdictions,
				context,
				sDBID,
				sUser,
				out
				)
			);
			*/
			printInvoiceDetailReport(
					sStartingDate,
					sEndingDate,
					bGroupByCostCenters,
					bShowIndividualInvoiceLines,
					sitemNumbertext,
					sOrderNumbertext,
					arrCostCenters,
					arrTaxJurisdictions,
					context,
					sDBID,
					sUserID,
					out,
					sLicenseModuleLevel,
					conn
					);
			
		} catch (Exception e1) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080660]");
			throw new Exception(e1.getMessage());
		}
		
		clsDatabaseFunctions.freeConnection(context, conn, "[1547080661]");
		out.println("\n" + sJavaScripts() + "\n");
		return;
	}
	private void printInvoiceDetailReport(
		String sStartingDate,
		String sEndingDate,
		boolean bGroupByCostCenters,
		boolean bShowIndividualInvoiceLines,
		String sItemNumberText,
		String sOrderNumberText,
		ArrayList<String>arrCostCenters,
		ArrayList<String>arrTaxJurisdictions,
		ServletContext context,
		String sDBID,
		String sUserID,
		PrintWriter pwOut,
		String sLicenseModuleLevel,
		Connection conn
			) throws Exception{
		String s = "";
		boolean bOddRow = true;
		long iStartingTime = System.currentTimeMillis();
		
		int iLineBufferCount = 0;
		String sTempBuffer = "";
		
		//These array lists will hold the subtotals for the costs, etc:
		ArrayList<String>arrCostCenterLabels = new ArrayList<String>(0);
		ArrayList<String>arrTaxJurisdictionLabels = new ArrayList<String>(0);
		ArrayList<String>arrTaxTypeLabels = new ArrayList<String>(0);
		ArrayList<BigDecimal>arrbdPurchaseCost = new ArrayList<BigDecimal>(0);
		ArrayList<BigDecimal>arrbdTaxablePurchases = new ArrayList<BigDecimal>(0);
		ArrayList<BigDecimal>arrbdSalePriceAfterDiscount = new ArrayList<BigDecimal>(0);
		ArrayList<BigDecimal>arrbdTaxableSales = new ArrayList<BigDecimal>(0);
		ArrayList<BigDecimal>arrbdPurchaseTaxDue = new ArrayList<BigDecimal>(0);
		ArrayList<BigDecimal>arrbdSalesTaxDue = new ArrayList<BigDecimal>(0);
		ArrayList<BigDecimal>arrbdSalesTaxCollected = new ArrayList<BigDecimal>(0);
		
		//Variables to hold subtotals for each individual invoice
		BigDecimal bdInvoicePurchaseCost = new BigDecimal(0.00);
		BigDecimal bdInvoicePriceAfterDiscount = new BigDecimal(0.00);
		BigDecimal bdInvoiceTaxablePurchases =  new BigDecimal(0.00);
		BigDecimal bdInvoiceTaxableSales =  new BigDecimal(0.00);
		BigDecimal bdInvoicePurchaseTaxDue = new BigDecimal(0.00);
		BigDecimal bdInvoiceSalesTaxDue =  new BigDecimal(0.00);
		BigDecimal bdInvoiceSalesTaxCollected =  new BigDecimal(0.00);
		BigDecimal bdInvoiceTotalTaxDue = new BigDecimal(0.00);
		BigDecimal bdInvoiceTotalTaxOwed = new BigDecimal(0.00);
		
		
		String sCurrentInvoiceNumber = "";
		String sBillToName = "";
		String sShipToName = "";
		
		int iTotalColumnCount = 21;
		if (bGroupByCostCenters){
			iTotalColumnCount++;
		}
		
		//Establish the permissions for links:
		boolean bAllowInvoiceView = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMPrintInvoice, 
				sUserID, 
				conn,
				sLicenseModuleLevel);
		boolean bAllowOrderView = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMViewOrderInformation, 
				sUserID, 
				conn,
				sLicenseModuleLevel);
		boolean bAllowItemView = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.ICDisplayItemInformation, 
				sUserID, 
				conn,
				sLicenseModuleLevel);
		s += "<TABLE class = \"basic\">";
		
		String SQL = "SELECT"
			+ " " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.bdlinesalestaxamount
			+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dExtendedPriceAfterDiscount
			+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dQtyShipped
			+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.iTaxable
			+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sDesc
			+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber
			+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemCategory
			+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemNumber
			+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sUnitOfMeasure
			+ ", " + SMTableinvoicedetails.TableName  + "." + SMTableinvoicedetails.isuppressdetailoninvoice
			+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.bdtaxrate
			+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate
			+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.icalculatetaxoncustomerinvoice
			+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.icalculatetaxonpurchaseorsale
			+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sBillToName
			+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sShipToName
			+ ", " + SMTableinvoiceheaders.TableName  + "." + SMTableinvoiceheaders.staxjurisdiction
			+ ", " + SMTableinvoiceheaders.TableName  + "." + SMTableinvoiceheaders.staxtype
			+ ", " + SMTableinvoiceheaders.TableName  + "." + SMTableinvoiceheaders.strimmedordernumber
			+ ", " + SMTableicitems.TableName  + "." + SMTableicitems.inonstockitem
			+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.mInternalComments
			
			+ ", IF(" + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.iIsStockItem + " = 1" 
				+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dExtendedCost 
				+ ", ROUND(" + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.bdexpensedcost + " * " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dQtyShipped + ",2)" 
			+ ") AS EXTENDEDCOST"
			;
		
		if (bGroupByCostCenters){
			SQL += ", IF(" + SMTablecostcenters.TableName + "." + SMTablecostcenters.scostcentername + " IS NOT NULL, " + SMTablecostcenters.TableName + "." + SMTablecostcenters.scostcentername + ", '') AS COSTCENTER";
		}
		
		SQL += " FROM "
			+ SMTableinvoicedetails.TableName
			+" LEFT JOIN " + SMTableicitems.TableName
			+ " ON " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemNumber + "=" +  SMTableicitems.TableName + "." + SMTableicitems.sItemNumber	
			+ " LEFT JOIN " + SMTableinvoiceheaders.TableName
			+ " ON " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber + "=" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber
		
			+ " LEFT JOIN " + SMTableorderdetails.TableName
			+ " ON " 
				+ "(" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.strimmedordernumber + " = " + SMTableorderdetails.TableName + "." + SMTableorderdetails.strimmedordernumber + ")"
				+ " AND "
				+ "(" + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.iDetailNumber + " = " + SMTableorderdetails.TableName + "." + SMTableorderdetails.iDetailNumber + ")"
		;
		
		if (bGroupByCostCenters){
			SQL += " LEFT JOIN "
				+ SMTableglaccounts.TableName
				+ " ON " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sExpenseGLAcct + "=" + SMTableglaccounts.TableName + "." + SMTableglaccounts.sAcctID
				+ " LEFT JOIN "
				+ SMTablecostcenters.TableName
				+ " ON " + SMTableglaccounts.TableName + "." + SMTableglaccounts.iCostCenterID + "=" + SMTablecostcenters.TableName + "." + SMTablecostcenters.lid
			;
		}
/*		
		SQL += " LEFT JOIN " 
			+ "( SELECT"
				+ " " + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.sitemnumber + " AS ITEMNUM"
				+ ", AVG(" + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.bdamount + " / " + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.bdqtyreceived 
					+ ") AS AVGCOST"
				+ " FROM " + SMTableaptransactionlines.TableName
				+ " WHERE ("
					+ "(" + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.sitemnumber + " != '')"
				+ ")"
				+ " GROUP BY " + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.sitemnumber
			+ ") AS NONSTOCKCOSTSQUERY"
			+ " ON NONSTOCKCOSTSQUERY.ITEMNUM = " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemNumber
		;
*/
		SQL += " WHERE ("
			+ "(" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate + " >= '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(sStartingDate) + " 00:00:00')"
			+ " AND (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate + " <= '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(sEndingDate) + " 23:59:59')"
			+ " AND (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber + " IS NOT NULL)"
		;
		if(sItemNumberText.compareTo("") != 0){
			SQL += " AND ("+SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemNumber +"= '"+sItemNumberText+"')";
		}
		if(sOrderNumberText.compareTo("") != 0){
			SQL += " AND ("+SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.strimmedordernumber +"= '"+sOrderNumberText+"')";
		}
		SQL += " AND (";
		for (int i = 0; i < arrTaxJurisdictions.size(); i++){
			if (i == 0){
				SQL += "(" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.staxjurisdiction + " = '" + arrTaxJurisdictions.get(i) + "')";
			}else{
				SQL += " OR (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.staxjurisdiction + " = '" + arrTaxJurisdictions.get(i) + "')";
			}
		}
		SQL += ")";
		
		if (bGroupByCostCenters){
			SQL += " AND (";
				for (int i = 0; i < arrCostCenters.size(); i++){
					if (i == 0){
						if(arrCostCenters.get(i).equals("(Unassigned)"))
							SQL += "(" + SMTableglaccounts.TableName + "." + SMTableglaccounts.iCostCenterID+ " = 0 )";
						else
							SQL += "(" + SMTablecostcenters.TableName + "." + SMTablecostcenters.scostcentername + " = '" + arrCostCenters.get(i) + "')";
					}else{
						if(arrCostCenters.get(i).equals("(Unassigned)"))
							SQL += "OR (" + SMTableglaccounts.TableName + "." + SMTableglaccounts.iCostCenterID+ " = 0 )";
						else
							SQL += " OR (" + SMTablecostcenters.TableName + "." + SMTablecostcenters.scostcentername + " = '" + arrCostCenters.get(i) + "')";
					}
				}
			SQL += ")";
		}
		
		SQL += ") ORDER BY "; //End 'where' clause
				
		if (bGroupByCostCenters){
			SQL += SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.staxjurisdiction
				+ ", " + SMTablecostcenters.TableName + "." + SMTablecostcenters.scostcentername
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.staxtype
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate
				+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber 
				+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.iLineNumber
			;
		}else{ 
			SQL += SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.staxjurisdiction 
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.staxtype
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate
				+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber 
				+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.iLineNumber
			;
		}
		//s += "SQL = " + SQL;
		//Insert the line header:
		s += printInvoiceDetailHeading(bGroupByCostCenters, bShowIndividualInvoiceLines);
		pwOut.println(s);
		s = "";

		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			
			while(rs.next()){
				String sBackgroundColor = "";

				if (bOddRow){
					sBackgroundColor = SMMasterStyleSheetDefinitions.BACKGROUND_WHITE;
				}else{
					sBackgroundColor = SMMasterStyleSheetDefinitions.BACKGROUND_GREY;
				}
				
				BigDecimal bdExtendedCost = rs.getBigDecimal("EXTENDEDCOST");
				BigDecimal bdExtendedPriceAfterDiscount = new BigDecimal(
					clsManageBigDecimals.doubleToDecimalFormat(
						rs.getDouble(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dExtendedPriceAfterDiscount), SMTableinvoicedetails.bdlinetaxamountscale).replace(",", ""));
				BigDecimal bdTaxablePurchaseAmount = new BigDecimal("0.00");
				if (
					(rs.getInt(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.iTaxable) == 1)
					&& (rs.getInt(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.icalculatetaxonpurchaseorsale) == SMTabletax.TAX_CALCULATION_BASED_ON_PURCHASE_COST)
					&& (rs.getBigDecimal(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.bdtaxrate).compareTo(BigDecimal.ZERO) != 0)
				){
					bdTaxablePurchaseAmount = bdExtendedCost;
				}
				
				BigDecimal bdTaxableSalesAmount = new BigDecimal("0.00");
				if (
						(rs.getInt(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.iTaxable) == 1)
						&& (rs.getInt(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.icalculatetaxonpurchaseorsale) == SMTabletax.TAX_CALCULATION_BASED_ON_SELLING_PRICE)
						&& (rs.getBigDecimal(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.bdtaxrate).compareTo(BigDecimal.ZERO) != 0)
				){
					bdTaxableSalesAmount = BigDecimal.valueOf(rs.getDouble(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dExtendedPriceAfterDiscount));
				}
				BigDecimal bdOneHundred = new BigDecimal("100.00");
				BigDecimal bdTaxRate = rs.getBigDecimal(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.bdtaxrate);
				BigDecimal bdPurchaseTaxDue = bdTaxablePurchaseAmount.multiply(bdTaxRate.divide(bdOneHundred)).setScale(SMTableinvoicedetails.bdlinetaxamountscale, BigDecimal.ROUND_HALF_UP);
				BigDecimal bdRetailSalesTaxDue = bdTaxableSalesAmount.multiply(bdTaxRate.divide(bdOneHundred)).setScale(SMTableinvoicedetails.bdlinetaxamountscale, BigDecimal.ROUND_HALF_UP);
				BigDecimal bdTotalTaxDue = bdPurchaseTaxDue.add(bdRetailSalesTaxDue);
				BigDecimal bdTotalTaxCollected = rs.getBigDecimal(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.bdlinesalestaxamount);
				BigDecimal bdTotalTaxOwed = bdTotalTaxDue.subtract(bdTotalTaxCollected);
				
				if(sCurrentInvoiceNumber.compareToIgnoreCase("") == 0){
					sCurrentInvoiceNumber = rs.getString(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber);
					sBillToName = rs.getString(SMTableinvoiceheaders.sBillToName);
					sShipToName = rs.getString(SMTableinvoiceheaders.sShipToName);
				}
				//If the invoice is the same add running totals, Otherwise print totals then clear them.
				if(sCurrentInvoiceNumber.compareToIgnoreCase(rs.getString(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber)) == 0){

				}else{
					sTempBuffer += printInvoiceSubTotalsLine(
							sCurrentInvoiceNumber,					
							bdInvoicePurchaseCost,
							bdInvoiceTaxablePurchases, 
							bdInvoicePriceAfterDiscount,
							bdInvoiceTaxableSales,					
							bdInvoicePurchaseTaxDue,
							bdInvoiceSalesTaxDue,
							bdInvoiceTotalTaxDue,
							bdInvoiceSalesTaxCollected, 	
							bdInvoiceTotalTaxOwed, //7
							bShowIndividualInvoiceLines,
							bAllowInvoiceView,
							sBillToName,
							sShipToName,
							SUBTOTAL_INVOICE__LINE_BG_COLOR,
							context,
							iTotalColumnCount
							);
					//Since we've moved on to another invoice, reset these variables to zero:
					bdInvoicePurchaseCost = new BigDecimal("0.00");
					bdInvoiceTaxablePurchases = new BigDecimal("0.00");
					bdInvoicePriceAfterDiscount  = new BigDecimal("0.00");
					bdInvoiceTaxableSales = new BigDecimal("0.00");
					bdInvoicePurchaseTaxDue = new BigDecimal("0.00");
					bdInvoiceSalesTaxDue = new BigDecimal("0.00");
					bdInvoiceSalesTaxCollected = new BigDecimal("0.00");
					bdInvoiceTotalTaxDue = new BigDecimal("0.00");
					bdInvoiceTotalTaxOwed = new BigDecimal("0.00");
				}
				
				//Now accumulate the values in these variables on every record:
				bdInvoicePurchaseCost = bdInvoicePurchaseCost.add(bdExtendedCost);
				bdInvoiceTaxablePurchases = bdInvoiceTaxablePurchases.add(bdTaxablePurchaseAmount);
				bdInvoicePriceAfterDiscount = bdInvoicePriceAfterDiscount.add(bdExtendedPriceAfterDiscount);
				bdInvoiceTaxableSales = bdInvoiceTaxableSales.add(bdTaxableSalesAmount);
				bdInvoicePurchaseTaxDue = bdInvoicePurchaseTaxDue.add(bdPurchaseTaxDue);
				bdInvoiceSalesTaxDue = bdInvoiceSalesTaxDue.add(bdRetailSalesTaxDue);
				bdInvoiceTotalTaxDue = bdInvoiceTotalTaxDue.add(bdTotalTaxDue);
				bdInvoiceSalesTaxCollected = bdInvoiceSalesTaxCollected.add(bdTotalTaxCollected);
				bdInvoiceTotalTaxOwed = bdInvoiceTotalTaxOwed.add(bdTotalTaxOwed);
				
				//Store the current invoice number to compare to the next invoice number.
				sCurrentInvoiceNumber = rs.getString(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber);
				sBillToName = rs.getString(SMTableinvoiceheaders.sBillToName);
				sShipToName = rs.getString(SMTableinvoiceheaders.sShipToName);
				//Get the label that tells us this record is part of a new group:
				String sTaxJurisdiction = rs.getString(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.staxjurisdiction);
				String sTaxType = rs.getString(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.staxtype);
				String sCostCenterLabel = "";
				if (bGroupByCostCenters){
					sCostCenterLabel = rs.getString("COSTCENTER");
				}
				
				//If the label has changed, then we know we need to add a new group:
				
				//If nothing's been added yet, start a new group:
				if (arrTaxJurisdictionLabels.size() == 0){
					arrCostCenterLabels.add(sCostCenterLabel);
					arrTaxJurisdictionLabels.add(sTaxJurisdiction);
					arrTaxTypeLabels.add(sTaxType);
					arrbdPurchaseCost.add(bdExtendedCost);
					arrbdTaxablePurchases.add(bdTaxablePurchaseAmount);
					arrbdSalePriceAfterDiscount.add(bdExtendedPriceAfterDiscount);
					arrbdTaxableSales.add(bdTaxableSalesAmount);
					arrbdPurchaseTaxDue.add(bdPurchaseTaxDue);
					arrbdSalesTaxDue.add(bdRetailSalesTaxDue);
					arrbdSalesTaxCollected.add(bdTotalTaxCollected);
				} else{
					//If there IS already a group, and it matches, then add the current values to the collectors:
					if ((sTaxJurisdiction.compareToIgnoreCase(arrTaxJurisdictionLabels.get(arrTaxJurisdictionLabels.size() - 1)) == 0)
						&& (sTaxType.compareToIgnoreCase(arrTaxTypeLabels.get(arrTaxTypeLabels.size() - 1)) == 0)
						&& (sCostCenterLabel.compareToIgnoreCase(arrCostCenterLabels.get(arrCostCenterLabels.size() - 1)) == 0)
					){
						
						//Just accumulate the other values:
						arrbdPurchaseCost.set(arrbdPurchaseCost.size() - 1, arrbdPurchaseCost.get(arrbdPurchaseCost.size() - 1).add(bdExtendedCost));
						arrbdTaxablePurchases.set(arrbdTaxablePurchases.size() - 1, arrbdTaxablePurchases.get(arrbdTaxablePurchases.size() - 1).add(bdTaxablePurchaseAmount));
						arrbdSalePriceAfterDiscount.set(arrbdSalePriceAfterDiscount.size() - 1, arrbdSalePriceAfterDiscount.get(arrbdSalePriceAfterDiscount.size() - 1).add(bdExtendedPriceAfterDiscount));
						arrbdTaxableSales.set(arrbdTaxableSales.size() - 1, arrbdTaxableSales.get(arrbdTaxableSales.size() - 1).add(bdTaxableSalesAmount));
						arrbdPurchaseTaxDue.set(arrbdPurchaseTaxDue.size() - 1, arrbdPurchaseTaxDue.get(arrbdPurchaseTaxDue.size() - 1).add(bdPurchaseTaxDue));
						arrbdSalesTaxDue.set(arrbdSalesTaxDue.size() - 1, arrbdSalesTaxDue.get(arrbdSalesTaxDue.size() - 1).add(bdRetailSalesTaxDue));
						arrbdSalesTaxCollected.set(arrbdSalesTaxCollected.size() - 1, arrbdSalesTaxCollected.get(arrbdSalesTaxCollected.size() - 1).add(bdTotalTaxCollected));
						
					}else{
						//But IF the cost center or tax jurisdiction or tax type has changed, trigger the subtotal line, and add a new label for each:
						try {
							sTempBuffer += printSubTotalsLine(
								arrCostCenterLabels.get(arrCostCenterLabels.size() - 1),
								arrTaxJurisdictionLabels.get(arrTaxJurisdictionLabels.size() - 1),
								arrTaxTypeLabels.get(arrTaxTypeLabels.size() - 1),
								arrbdPurchaseCost.get(arrbdPurchaseCost.size() - 1),
								arrbdTaxablePurchases.get(arrbdTaxablePurchases.size() - 1),
								arrbdSalePriceAfterDiscount.get(arrbdSalePriceAfterDiscount.size() - 1),
								arrbdTaxableSales.get(arrbdTaxableSales.size() - 1),
								arrbdPurchaseTaxDue.get(arrbdPurchaseTaxDue.size() - 1),
								arrbdSalesTaxDue.get(arrbdSalesTaxDue.size() - 1),
								arrbdSalesTaxCollected.get(arrbdSalesTaxCollected.size() - 1),
								bGroupByCostCenters,
								bShowIndividualInvoiceLines,
								iTotalColumnCount
							);
						} catch (Exception e) {
							rs.close();
							throw new Exception(e.getMessage());
						}
						arrCostCenterLabels.add(sCostCenterLabel);
						arrTaxJurisdictionLabels.add(sTaxJurisdiction);
						arrTaxTypeLabels.add(sTaxType);
						arrbdPurchaseCost.add(bdExtendedCost);
						arrbdTaxablePurchases.add(bdTaxablePurchaseAmount);
						arrbdSalePriceAfterDiscount.add(bdExtendedPriceAfterDiscount);
						arrbdTaxableSales.add(bdTaxableSalesAmount);
						arrbdPurchaseTaxDue.add(bdPurchaseTaxDue);
						arrbdSalesTaxDue.add(bdRetailSalesTaxDue);
						arrbdSalesTaxCollected.add(bdTotalTaxCollected);
					}
				}			
				
				sTempBuffer += printInvoiceDetailLine(
					rs,
					clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, bdTaxablePurchaseAmount),
					clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, bdTaxableSalesAmount),
					clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, bdPurchaseTaxDue),
					clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, bdRetailSalesTaxDue),
					clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, bdTotalTaxDue),
					clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, bdTotalTaxCollected),
					clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, bdTotalTaxOwed),
					bGroupByCostCenters,
					bShowIndividualInvoiceLines,
					bAllowInvoiceView,
					bAllowOrderView,
					bAllowItemView,
					sBackgroundColor,
					context,
					conn,
					sDBID
				);

				bOddRow = !bOddRow;
				
				//This allows us to buffer each line into a temporary string which will never get too large, then copy it only every so often into the final string.  
				//This should minimize the amount of buffer reallocation, and should make it process faster.
				iLineBufferCount++;
				if (iLineBufferCount == MAX_LINE_BUFFER_COUNT){
					s += sTempBuffer;
					pwOut.println(s);
					s = "";
					sTempBuffer = "";
					iLineBufferCount = 0;
				}
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1455050609] reading invoice detail records - " + e.getMessage());
		}
		
		//Get anything that hasn't been transferred from the line buffer:
		s += sTempBuffer;

		//IF there were any records:
		if (arrCostCenterLabels.size() > 0){
			//Print the last subtotal line:
			try {
				s += printSubTotalsLine(
					arrCostCenterLabels.get(arrCostCenterLabels.size() - 1),
					arrTaxJurisdictionLabels.get(arrTaxJurisdictionLabels.size() - 1),
					arrTaxTypeLabels.get(arrTaxTypeLabels.size() - 1),
					arrbdPurchaseCost.get(arrbdPurchaseCost.size() - 1),
					arrbdTaxablePurchases.get(arrbdTaxablePurchases.size() - 1),
					arrbdSalePriceAfterDiscount.get(arrbdSalePriceAfterDiscount.size() - 1),
					arrbdTaxableSales.get(arrbdTaxableSales.size() - 1),
					arrbdPurchaseTaxDue.get(arrbdPurchaseTaxDue.size() - 1),
					arrbdSalesTaxDue.get(arrbdSalesTaxDue.size() - 1),
					arrbdSalesTaxCollected.get(arrbdSalesTaxCollected.size() - 1),
					bGroupByCostCenters,
					bShowIndividualInvoiceLines,
					iTotalColumnCount
				);
			} catch (Exception e) {
				throw new Exception(e.getMessage());
			}
	
			//Now print the summary totals:
			s += printSummaryTotals(
				arrCostCenterLabels,
				arrTaxJurisdictionLabels,
				arrTaxTypeLabels,
				arrbdPurchaseCost,
				arrbdTaxablePurchases,
				arrbdSalePriceAfterDiscount,
				arrbdTaxableSales,
				arrbdPurchaseTaxDue,
				arrbdSalesTaxDue,
				arrbdSalesTaxCollected,
				bGroupByCostCenters,
				iTotalColumnCount
			);
		}
		s += "</TABLE>";
		
		BigDecimal bdStartingTime = new BigDecimal(iStartingTime);
		BigDecimal bdEndingTime = new BigDecimal(System.currentTimeMillis());
		BigDecimal bdProcessingTime = (bdEndingTime.subtract(bdStartingTime)).divide(new BigDecimal("1000.00"));
		s += "<BR>Total processing time: <B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdProcessingTime) + "</B> seconds.";
		s += "<BR>" + printHeadingDescriptions();
		pwOut.println(s);
		
	}

	private String printInvoiceDetailLine(
		ResultSet rsInvoiceDetail,
		String sTaxablePurchaseAmount,
		String sTaxableSalesAmount,
		String sPurchaseTaxDue,
		String sRetailSalesTaxDue,
		String sTotalTaxDue,
		String sTaxCollected,
		String sTotalTaxOwed,
		boolean bShowCostCenter,
		boolean bShowIndividualInvoiceLines,
		boolean bAllowInvoiceView,
		boolean bAllowOrderView,
		boolean bAllowItemView,
		String sBackgroundColor,
		ServletContext context,
		Connection conn,
		String sDBID
		) throws Exception{
		String s = "";
		if (!bShowIndividualInvoiceLines){
			return s;
		}
		s += "<TR style = \" background-color: " + sBackgroundColor +  "; \">";
		String sTaxable = "N";
		if (rsInvoiceDetail.getInt(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.iTaxable) == 1){
			sTaxable = "Y";
		}
		
		//Create all the necessary links:
		String sInvoice = rsInvoiceDetail.getString(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber).trim();
		String sInvoiceLink = sInvoice;
		if (bAllowInvoiceView){
			sInvoiceLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "" 
				+ "smcontrolpanel.SMPrintInvoice?InvoiceNumberFrom=" + sInvoice
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "\">"
				+ sInvoice
				+ "</A>"
				;
		}
		
		String sOrder = rsInvoiceDetail.getString(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.strimmedordernumber).trim();
		String sOrderLink = sOrder;
		if (bAllowOrderView){
			sOrderLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
				+ "smcontrolpanel.SMDisplayOrderInformation"
				+ "?" + SMOrderHeader.ParamsOrderNumber + "=" + sOrder 
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "\">" + sOrder + "</A>"
				;
		}

		String sItem = rsInvoiceDetail.getString(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemNumber).trim();
		String sItemLink = sItem;
		if (bAllowItemView){
			sItemLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
				+ "smic.ICDisplayItemInformation?ItemNumber=" 
				+ sItem
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "\">" + sItem + "</A>"
				;
		}
		
		BigDecimal bdExtendedItemCost = rsInvoiceDetail.getBigDecimal("EXTENDEDCOST");
		if (rsInvoiceDetail.getInt(SMTableicitems.TableName + "." + SMTableicitems.inonstockitem) == 1){
			//Try to get the cost of the non-stock item from AP:
			//bdExtendedItemCost = getCostOfNonStockItem(sItem, rsInvoiceDetail.getDouble(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dQtyShipped), conn);
			bdExtendedItemCost = rsInvoiceDetail.getBigDecimal("EXTENDEDCOST");
		}
		
		String sDescriptionHoverText = rsInvoiceDetail.getString(SMTableorderdetails.TableName + "." + SMTableorderdetails.mInternalComments);
		if (sDescriptionHoverText == null)
			sDescriptionHoverText = "";
		try {
			if (bShowCostCenter){
				String sCostCenter = rsInvoiceDetail.getString("COSTCENTER");
				if(sCostCenter.equals(""))	
					s += "<TD class = \" leftjustifiedcell \">(Unassigned)</TD>";
				else
					s += "<TD class = \" leftjustifiedcell \">"+sCostCenter+"</TD>";
			}
			s += "<TD class = \" leftjustifiedcell \">"
				+ rsInvoiceDetail.getString(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.staxjurisdiction).trim()
				+ "&nbsp;" + rsInvoiceDetail.getString(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.staxtype).trim()
				+ "</TD>"
				+ "<TD class = \" leftjustifiedcell \">"
				+ clsDateAndTimeConversions.sqlDateToString(rsInvoiceDetail.getDate(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate), "MM/dd/YYYY")
				+ "</TD>"
				+ "<TD class = \" leftjustifiedcell \">"
				+ sInvoiceLink
				+ "</TD>"
				+ "<TD class = \" leftjustifiedcell \">"
				+ sOrderLink
				+ "</TD>"
				+ "<TD class = \" rightjustifiedcell \">"
				+ clsManageBigDecimals.doubleToDecimalFormat(rsInvoiceDetail.getDouble(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dQtyShipped), 4)
				+ "</TD>"
				+ "<TD class = \" leftjustifiedcell \">"
				+ sItemLink
				+ "</TD>"
				+ "<TD class = \" leftjustifiedcell \">"
			;
			if (sDescriptionHoverText.compareToIgnoreCase("") != 0){
				s += "<SPAN TITLE=\"" 
					+ sDescriptionHoverText
					+ "\">"
					+ "<B>" + rsInvoiceDetail.getString(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sDesc) + "</B>"
					+ "</SPAN>"
				;
			}else{
				s += rsInvoiceDetail.getString(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sDesc);
			}

			s += "</TD>"
				+ "<TD class = \" leftjustifiedcell \">"
				+ rsInvoiceDetail.getString(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sUnitOfMeasure)
				+ "</TD>"
				+ "<TD class = \" leftjustifiedcell \">";
				if(rsInvoiceDetail.getInt(SMTableicitems.TableName + "." + SMTableicitems.inonstockitem) == 1){
					s+= "Y";
				}else{
					s+= "N";
				}
				s +=  "</TD>"
				+ "<TD class = \" leftjustifiedcell \">";
				if(rsInvoiceDetail.getInt(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.isuppressdetailoninvoice) == 1){
					s+= "Y";
				}else{
					s+= "N";
				}
				s +=  "</TD>"
				+ "<TD class = \" centerjustifiedcell \">"
				+ sTaxable
				+ "</TD>"
				+ "<TD class = \" centerjustifiedcell \">"
				+ rsInvoiceDetail.getString(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemCategory)
				+ "</TD>"
				+ "<TD class = \" rightjustifiedcell \">"
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdExtendedItemCost)
				+ "</TD>"
				+ "<TD class = \" rightjustifiedcell \">"
				+ sTaxablePurchaseAmount
				+ "</TD>"
				+ "<TD class = \" rightjustifiedcell \">"
				+ clsManageBigDecimals.doubleToDecimalFormat(rsInvoiceDetail.getDouble(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dExtendedPriceAfterDiscount), 2)
				+ "</TD>"
				+ "<TD class = \" rightjustifiedcell \">"
				+ sTaxableSalesAmount
				+ "</TD>"
				+ "<TD class = \" rightjustifiedcell \">"
				+ sPurchaseTaxDue
				+ "</TD>"
				+ "<TD class = \" rightjustifiedcell \">"
				+ sRetailSalesTaxDue
				+ "</TD>"
				+ "<TD class = \" rightjustifiedcell \">"
				+ sTotalTaxDue
				+ "</TD>"
				+ "<TD class = \" rightjustifiedcell \">"
				+ sTaxCollected
				+ "</TD>"
				+ "<TD class = \" rightjustifiedcell \">"
				+ sTotalTaxOwed
				+ "</TD>"
			;
		} catch (Exception e) {
			throw new Exception("Error [1455053087] displaying invoice line - " + e.getMessage());
		}
		
		s += "</TR>";
		
		return s;
	}

	private String printSubTotalsLine(
		String sCostCenterLabel,
		String sTaxJurisdictionLabel,
		String sTaxTypeLabel,
		BigDecimal bdPurchaseCost,
		BigDecimal bdTaxablePurchases,
		BigDecimal bdSellPriceAfterDiscount,
		BigDecimal bdTaxableSales,
		BigDecimal bdPurchaseTaxDue,
		BigDecimal bdSalesTaxDue,
		BigDecimal bdSalesTaxCollected,
		boolean bShowCostCenter,
		boolean bShowIndividualInvoiceLines,
		int iTotalColumnCount) throws Exception{
		String s = "";
		if (!bShowIndividualInvoiceLines){
			return s;
		}

		String sBackgroundColor = SUBTOTAL_LINE_BG_COLOR;
		//Heading line for the totals:
		s += "<TR style = \" background-color: " + sBackgroundColor +  "; \" >"
			+ "<TD COLSPAN = " + Integer.toString(iTotalColumnCount - 9) + " ALIGN=RIGHT><B>" 
				+ "&nbsp;" 
				+ "</B></TD>"
				+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT><B><U>" 
				+ "INVENTORY<BR>COST" 
				+ "</U></B></TD>"
				+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT><B><U>" 
				+ "TAXABLE<BR>INVENTORY<BR>COST" 
				+ "</U></B></TD>"
				+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT><B><U>" 
				+ "SALES<BR>PRICE" 
				+ "</U></B></TD>"
				+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT><B><U>" 
				+ "TAXABLE<BR>SALES<BR>PRICE" 
				+ "</U></B></TD>"
				+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT><B><U>" 
				+ "PURCHASE<BR>TAX<BR>DUE" 
				+ "</U></B></TD>"
				+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT><B><U>" 
				+ "SALES<BR>TAX<BR>DUE" 
				+ "</U></B></TD>"
				+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT><B><U>" 
				+ "TOTAL<BR>TAX<BR>DUE" 
				+ "</U></B></TD>"
				+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT><B><U>" 
				+ "TAX<BR>COLLECTED" 
				+ "</U></B></TD>"
				+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT><B><U>" 
				+ "TAX<BR>OWED" 
				+ "</U></B></TD>"
			+ "</TR>"
		;

		try {
			s += "<TR style = \" background-color: " + sBackgroundColor +  "; \" >"
				+ "<TD COLSPAN=" + Integer.toString(iTotalColumnCount - 9) + "  class = \" rightjustifiedheading \" ALIGN=RIGHT><B>" 
					+ sCostCenterLabel + COSTCENTER_AND_TAX_SEPARATOR + sTaxJurisdictionLabel + "&nbsp;" + sTaxTypeLabel + ":</B></TD>"
				+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT>"
				+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, bdPurchaseCost)
				+ " </TD>"
				+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT>"
				+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, bdTaxablePurchases)
				+ " </TD>"
				+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT>"
				+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, bdSellPriceAfterDiscount)
				+ " </TD>"
				+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT>"
				+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, bdTaxableSales)
				+ " </TD>"
				+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT>"
				+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, bdPurchaseTaxDue)
				+ " </TD>"
				+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT>"
				+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, bdSalesTaxDue)
				+ " </TD>"
				+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT>"
				+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, bdPurchaseTaxDue.add(bdSalesTaxDue))
				+ " </TD>"
				+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT>"
				+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, bdSalesTaxCollected)
				+ " </TD>"
				+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT>"
				+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, (bdPurchaseTaxDue.add(bdSalesTaxDue)).subtract(bdSalesTaxCollected))
				+ " </TD>"
				+ "</TR>"
			;
		} catch (Exception e) {
			throw new Exception("Error [1455828472] printing invoice detail subtotals - " + e.getMessage());
		}
		
		s += "<TR style = \" background-color: " + sBackgroundColor +  "; \" >"
				+ "<TD COLSPAN=" + Integer.toString(iTotalColumnCount) + "  class = \" rightjustifiedheading \" ALIGN=RIGHT><B>" + "&nbsp;" + "</B></TD>"
				+ "</TR>"
			;
		return s;
	}
	
	private String printInvoiceSubTotalsLine(
			String sInvoiceNumber,
			BigDecimal bdPurchaseCost,
			BigDecimal sTaxablePurchaseAmount,
			BigDecimal bdInvoicePriceAfterDiscount,
			BigDecimal sTaxableSalesAmount,
			BigDecimal sUseTaxDue,
			BigDecimal sRetailSalesTaxDue,
			BigDecimal sTotalTaxDue,
			BigDecimal sTaxCollected,
			BigDecimal sTotalTaxOwed,
			boolean bShowIndividualInvoiceLines,
			boolean bAllowInvoiceView,
			String sBillToName,
			String sShipToName,
			String sBackgroundColor,
			ServletContext context,
			int iTotalColumnCount
			) throws Exception{
			String s = "";
			if (!bShowIndividualInvoiceLines){
				return s;
			}
			s += "<TR style = \" background-color: " + sBackgroundColor +  "; border-top-style: solid; \">";

			//Create all the necessary links:
			String sInvoiceLink = sInvoiceNumber;
			if (bAllowInvoiceView){
				sInvoiceLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "" 
					+ "smcontrolpanel.SMPrintInvoice?InvoiceNumberFrom=" + sInvoiceNumber
					+ "\">"
					+ sInvoiceNumber
					+ "</A>"
					;
			}
			
			String sAdditionalInvoiceInfo = " " + sBillToName + " - " + sShipToName + " ";
			
			try {
				s += "<TD  COLSPAN=" + Integer.toString(iTotalColumnCount - 9) + " class = \" rightjustifiedcell \">"
				    + sInvoiceLink + sAdditionalInvoiceInfo + "<B> Invoice " + " total: " + "</B></TD>"
					+ "<TD class = \" rightjustifiedcell \">"
					+clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdPurchaseCost)
					+ "</TD>"
					+ "<TD class = \" rightjustifiedcell \">"
					+clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(sTaxablePurchaseAmount)
					+ "</TD>"
					+ "<TD class = \" rightjustifiedcell \">"
					+clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdInvoicePriceAfterDiscount)
					+ "</TD>"
					+ "<TD class = \" rightjustifiedcell \">"
					+clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(sTaxableSalesAmount)
					+ "</TD>"
					+ "<TD class = \" rightjustifiedcell \">"
					+clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(sUseTaxDue)
					+ "</TD>"
					+ "<TD class = \" rightjustifiedcell \">"
					+clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(sRetailSalesTaxDue)
					+ "</TD>"
					+ "<TD class = \" rightjustifiedcell \">"
					+clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(sTotalTaxDue)
					+ "</TD>"
					+ "<TD class = \" rightjustifiedcell \">"
					+clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(sTaxCollected)
					+ "</TD>"
					+ "<TD class = \" rightjustifiedcell \">"
					+clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(sTotalTaxOwed)
					+ "</TD>"
				;
			} catch (Exception e) {
				throw new Exception("Error [1455053087] displaying invoice line - " + e.getMessage());
			}
			s += "</TR>";
			
			return s;
		}
	
	private String printInvoiceDetailHeading(boolean bShowCostCenter, boolean bShowIndividualInvoiceLines) throws Exception{
		String s = "";
		if (!bShowIndividualInvoiceLines){
			return s;
		}
		s += "<THEAD><TR id=\"tablehead\" style = \" background-color: black; color: white; \" >";
		
		if (bShowCostCenter){
			s += "<TD class = \" leftjustifiedheading \" >"
				+ "COST CENTER"
				+ " </TD>"
			; 
		}
		s += "<TD class = \" leftjustifiedheading \" >" + "TAX" + "</TD>"
			+ "<TD class = \" leftjustifiedheading \" >" + "INV DATE" + "</TD>"
			+ "<TD class = \" leftjustifiedheading \" >" + "INV #" + "</TD>"
			+ "<TD class = \" leftjustifiedheading \" >" + "ORDER" + "</TD>"
			+ "<TD class = \" rightjustifiedheading \" >" + "QTY" + "</TD>"
			+ "<TD class = \" leftjustifiedheading \" >" + "ITEM" + "</TD>"
			+ "<TD class = \" leftjustifiedheading \" >" + "DESCRIPTION <I>(Hover over BOLD lines to<BR>&nbsp;&nbsp;&nbsp;&nbsp;see order detail 'Internal Comment')</I>" + "</TD>"
			+ "<TD class = \" leftjustifiedheading \" >" + "U/M" + "</TD>"
			+ "<TD class = \" leftjustifiedheading \" >" + "NON-STOCK<BR>ITEM?" + "</TD>"
			+ "<TD class = \" leftjustifiedheading \" >" + "SUPPRESS<BR>ON<BR>INVOICE?" + "</TD>"
			+ "<TD class = \" leftjustifiedheading \" >" + "TAXABLE?" + "</TD>"
			+ "<TD class = \" leftjustifiedheading \" >" + "CATEGORY" + "</TD>"
			+ "<TD class = \" rightjustifiedheading \" >" + "INVENTORY<BR>COST<SUP><a href=\"#inventorycost\"><font color=\"#80ccff\">1</font></a></SUP>" + "</TD>"
			+ "<TD class = \" rightjustifiedheading \" >" + "TAXABLE<BR>INVENTORY<BR>COST<SUP><a href=\"#taxablecost\"><font color=\"#80ccff\">2</font></a></SUP>" + "</TD>"
			+ "<TD class = \" rightjustifiedheading \" >" + "SALES<BR>PRICE<SUP><a href=\"#saleprice\"><font color=\"#80ccff\">3</font></a></SUP>" + "</TD>"
			+ "<TD class = \" rightjustifiedheading \" >" + "TAXABLE<BR>SALES<BR>PRICE<SUP><a href=\"#taxablesalesprice\"><font color=\"#80ccff\">4</font></a></SUP>" + "</TD>" //
			+ "<TD class = \" rightjustifiedheading \" >" + "PURCHASE<BR>TAX<BR>DUE<SUP><a href=\"#purchasetaxdue\"><font color=\"#80ccff\">5</font></a></SUP>" + "</TD>"
			+ "<TD class = \" rightjustifiedheading \" >" + "SALES<BR>TAX<BR>DUE<SUP><a href=\"#salestaxdue\"><font color=\"#80ccff\">6</font></a></SUP>" + "</TD>"
			+ "<TD class = \" rightjustifiedheading \" >" + "TOTAL<BR>TAX<BR>DUE<SUP><a href=\"#totaltaxdue\"><font color=\"#80ccff\">7</font></a></SUP>" + "</TD>"
			+ "<TD class = \" rightjustifiedheading \" >" + "TAX<BR>COLLECTED<SUP><a href=\"#taxcollected\"><font color=\"#80ccff\">8</font></a></SUP>" + "</TD>"
			+ "<TD class = \" rightjustifiedheading \" >" + "TAX<BR>OWED<SUP><a href=\"#taxowed\"><font color=\"#80ccff\">9</font></a></SUP>" + "</TD>"
			+ "</TR></THEAD>"
		;
		//INVENTORY COST<BR>(TAXABLE COST)
		return s;
	}
	
	private String printSummaryTotals(
			ArrayList<String>arrCostCenterLabels,
			ArrayList<String>arrTaxJurisdictionLabels,
			ArrayList<String>arrTaxTypeLabels,
			ArrayList<BigDecimal>arrbdPurchaseCost,
			ArrayList<BigDecimal>arrbdTaxablePurchases,
			ArrayList<BigDecimal>arrbdSalePriceAfterDiscount,
			ArrayList<BigDecimal>arrbdTaxableSales,
			ArrayList<BigDecimal>arrbdPurchaseTaxDue,
			ArrayList<BigDecimal>arrbdSalesTaxDue,
			ArrayList<BigDecimal>arrbdSalesTaxCollected,
			boolean bShowCostCenter,
			int iTotalNumberOfColumns
		) throws Exception{
		String s = "";
		
		if (bShowCostCenter){
			s += printSummaryTotalsShowingCostCenters(
				arrCostCenterLabels,
				arrTaxJurisdictionLabels,
				arrTaxTypeLabels,
				arrbdPurchaseCost,
				arrbdTaxablePurchases,
				arrbdSalePriceAfterDiscount,
				arrbdTaxableSales,
				arrbdPurchaseTaxDue,
				arrbdSalesTaxDue,
				arrbdSalesTaxCollected,
				iTotalNumberOfColumns		
			);
		}else{
			s += printSummaryTotalsNotShowingCostCenters(
				arrTaxJurisdictionLabels,
				arrTaxTypeLabels,
				arrbdPurchaseCost,
				arrbdTaxablePurchases,
				arrbdSalePriceAfterDiscount,
				arrbdTaxableSales,
				arrbdPurchaseTaxDue,
				arrbdSalesTaxDue,
				arrbdSalesTaxCollected,
				iTotalNumberOfColumns
			);
		}
		return s;
	}
	private String printSummaryTotalsShowingCostCenters(
			ArrayList<String>arrCostCenterLabels,
			ArrayList<String>arrTaxJurisdictionLabels,
			ArrayList<String>arrTaxTypeLabels,
			ArrayList<BigDecimal>arrbdPurchaseCost,
			ArrayList<BigDecimal>arrbdTaxablePurchases,
			ArrayList<BigDecimal>arrbdSalePriceAfterDiscount,
			ArrayList<BigDecimal>arrbdTaxableSales,
			ArrayList<BigDecimal>arrbdPurchaseTaxDue,
			ArrayList<BigDecimal>arrbdSalesTaxDue,
			ArrayList<BigDecimal>arrbdSalesTaxCollected,
			int iTotalNumberOfColumns
			){
		HashMap<String,BigDecimal> hashTotals = new HashMap<String,BigDecimal>();
		String s = "";
		//Heading line for the cost centers:
		String sBackgroundColor = SUMMARY_COSTCENTER_TOTALS_BG_COLOR;
		//One separator line:
		s += printBlankSeparatorLine(sBackgroundColor, iTotalNumberOfColumns);
		
		//Heading:
		s += printSummaryHeadingLine("TOTALS FOR EACH TAX JURISDICTION, THEN COST CENTER:", sBackgroundColor, iTotalNumberOfColumns);
		
		s += printSummaryTotalColumnHeadings(iTotalNumberOfColumns, sBackgroundColor);
		
		//****************
		//Initialize arrays for each of the 'buckets':
		String sCurrentCostCenterLabel = "";
		String sCurrentTaxJurisdictionLabel = "";
		BigDecimal bdPurchaseCost = new BigDecimal("0.00");
		BigDecimal bdTaxablePurchases = new BigDecimal("0.00");
		BigDecimal bdSalePriceAfterDiscount = new BigDecimal("0.00");
		BigDecimal bdTaxableSales = new BigDecimal("0.00");
		BigDecimal bdPurchaseTaxDue = new BigDecimal("0.00");
		BigDecimal bdSalesTaxDue = new BigDecimal("0.00");
		BigDecimal bdSalesTaxCollected = new BigDecimal("0.00");
		BigDecimal bdTaxOwed = new BigDecimal("0.00");
		
		for (int i = 0; i < arrCostCenterLabels.size(); i++){
			//If we've already read a record - AND The next record is for a different jurisdiction, then print a line:
			if (
				(sCurrentCostCenterLabel.compareToIgnoreCase("") != 0)
				&& (sCurrentTaxJurisdictionLabel.compareToIgnoreCase("") != 0)
				&& (
					(arrCostCenterLabels.get(i).compareToIgnoreCase(sCurrentCostCenterLabel) != 0)
					|| (arrTaxJurisdictionLabels.get(i).compareToIgnoreCase(sCurrentTaxJurisdictionLabel) != 0)
				)
			){
				//Print a line for each tax jurisdiction:
				s += printSummaryTotalLine(
					sBackgroundColor,
					sCurrentTaxJurisdictionLabel + " - " + sCurrentCostCenterLabel,
					bdPurchaseCost,
					bdTaxablePurchases,
					bdSalePriceAfterDiscount,
					bdTaxableSales,
					bdPurchaseTaxDue,
					bdSalesTaxDue,
					bdSalesTaxCollected,
					bdTaxOwed,
					iTotalNumberOfColumns
				);
				bdTaxOwed = (bdPurchaseTaxDue.add(bdSalesTaxDue)).subtract(bdSalesTaxCollected);
				getTotal(bdPurchaseCost,
						bdTaxablePurchases,
						bdSalePriceAfterDiscount,
						bdTaxableSales,
						bdPurchaseTaxDue,
						bdSalesTaxDue, 
						bdSalesTaxCollected, 
						hashTotals,
						bdTaxOwed);
				//Now clear the totals and start over:
				bdPurchaseCost = new BigDecimal("0.00");
				bdTaxablePurchases = new BigDecimal("0.00");
				bdSalePriceAfterDiscount = new BigDecimal("0.00");
				bdTaxableSales = new BigDecimal("0.00");
				bdPurchaseTaxDue = new BigDecimal("0.00");
				bdSalesTaxDue = new BigDecimal("0.00");
				bdSalesTaxCollected = new BigDecimal("0.00");
			}
			sCurrentCostCenterLabel = arrCostCenterLabels.get(i);
			sCurrentTaxJurisdictionLabel = arrTaxJurisdictionLabels.get(i);
			//Accumulate the totals:
			bdPurchaseCost = bdPurchaseCost.add(arrbdPurchaseCost.get(i));
			bdTaxablePurchases = bdTaxablePurchases.add(arrbdTaxablePurchases.get(i));
			bdSalePriceAfterDiscount = bdSalePriceAfterDiscount.add(arrbdSalePriceAfterDiscount.get(i));
			bdTaxableSales = bdTaxableSales.add(arrbdTaxableSales.get(i));
			bdPurchaseTaxDue = bdPurchaseTaxDue.add(arrbdPurchaseTaxDue.get(i));
			bdSalesTaxDue = bdSalesTaxDue.add(arrbdSalesTaxDue.get(i));
			bdSalesTaxCollected = bdSalesTaxCollected.add(arrbdSalesTaxCollected.get(i));
		}
		//Now print the last section, if there is one:
		s += printSummaryTotalLine(
			sBackgroundColor,
			sCurrentTaxJurisdictionLabel + " - " + sCurrentCostCenterLabel,
			bdPurchaseCost,
			bdTaxablePurchases,
			bdSalePriceAfterDiscount,
			bdTaxableSales,
			bdPurchaseTaxDue,
			bdSalesTaxDue,
			bdSalesTaxCollected,
			bdTaxOwed,
			iTotalNumberOfColumns
		);
		bdTaxOwed = (bdPurchaseTaxDue.add(bdSalesTaxDue)).subtract(bdSalesTaxCollected);
		getTotal(bdPurchaseCost,
				bdTaxablePurchases,
				bdSalePriceAfterDiscount,
				bdTaxableSales,
				bdPurchaseTaxDue,
				bdSalesTaxDue, 
				bdSalesTaxCollected, 
				hashTotals,
				bdTaxOwed);
		
		s += printTotal(hashTotals,iTotalNumberOfColumns);
		
		//One separator line:
		s += printBlankSeparatorLine(sBackgroundColor, iTotalNumberOfColumns);
		return s;
	}
	private String printSummaryTotalsNotShowingCostCenters(
			ArrayList<String>arrTaxJurisdictionLabels,
			ArrayList<String>arrTaxTypeLabels,
			ArrayList<BigDecimal>arrbdPurchaseCost,
			ArrayList<BigDecimal>arrbdTaxablePurchases,
			ArrayList<BigDecimal>arrbdSalePriceAfterDiscount,
			ArrayList<BigDecimal>arrbdTaxableSales,
			ArrayList<BigDecimal>arrbdPurchaseTaxDue,
			ArrayList<BigDecimal>arrbdSalesTaxDue,
			ArrayList<BigDecimal>arrbdSalesTaxCollected,
			int iTotalNumberOfColumns
			){
		BigDecimal bdTaxOwed = new BigDecimal("0.00");
		HashMap<String,BigDecimal> hashEachTaxType = new HashMap<String,BigDecimal>();
		String s = "";
		String sBackgroundColor = "";
		//First, list a total for each tax type:
		sBackgroundColor = SUMMARY_TAX_JURISDICTION_TOTALS_BG_COLOR;
		//Separator line:
		//One separator line:
		s += printBlankSeparatorLine(sBackgroundColor, iTotalNumberOfColumns);
		
		//Heading line for the totals:
		s += printSummaryHeadingLine("TOTALS FOR EACH TAX TYPE:", sBackgroundColor, iTotalNumberOfColumns);
		
		//Headings for the columns:
		s += printSummaryTotalColumnHeadings(iTotalNumberOfColumns, sBackgroundColor);
		
		//String [] letter = {"INVENTORY COST", "TAXABLE INVENTORY COST", "SALES PRICE", "TAXABLE SALES PRICE", "PURCHASE TAX DUE", "SALES TAX DUE", "TOTAL TAX DUE"};
		
		
		for (int i = 0; i < arrTaxJurisdictionLabels.size(); i++){
			//Print a line for each tax type:
			s += printSummaryTotalLine(
				sBackgroundColor,
				arrTaxJurisdictionLabels.get(i) + " " + arrTaxTypeLabels.get(i),
				arrbdPurchaseCost.get(i),
				arrbdTaxablePurchases.get(i),
				arrbdSalePriceAfterDiscount.get(i),
				arrbdTaxableSales.get(i),
				arrbdPurchaseTaxDue.get(i),
				arrbdSalesTaxDue.get(i),
				arrbdSalesTaxCollected.get(i),
				bdTaxOwed,
				iTotalNumberOfColumns
			);
			bdTaxOwed = addTaxOwed(arrbdPurchaseTaxDue.get(i),arrbdSalesTaxDue.get(i),arrbdSalesTaxCollected.get(i));
			getTotal(arrbdPurchaseCost.get(i),
					arrbdTaxablePurchases.get(i),
					arrbdSalePriceAfterDiscount.get(i),
					arrbdTaxableSales.get(i),
					arrbdPurchaseTaxDue.get(i),
					arrbdSalesTaxDue.get(i), 
					arrbdSalesTaxCollected.get(i), 
					hashEachTaxType,
					bdTaxOwed);
			
			
			
			
		}
		s += printTotal(hashEachTaxType,iTotalNumberOfColumns);
		//One separator line:
		s += printBlankSeparatorLine(sBackgroundColor, iTotalNumberOfColumns);

		//*********************************************
		//Next list a total for each tax jurisdiction:
		//Heading line for the tax jurisdictions:
		sBackgroundColor = SUMMARY_TAX_TYPE_TOTALS_BG_COLOR;
		//One separator line:
		s += printBlankSeparatorLine(sBackgroundColor, iTotalNumberOfColumns);
		
		//Heading:
		s += printSummaryHeadingLine("TOTALS FOR EACH TAX JURISDICTION:", sBackgroundColor, iTotalNumberOfColumns);
		
		s += printSummaryTotalColumnHeadings(iTotalNumberOfColumns, sBackgroundColor);
		
		//Create arrays for each of the 'buckets':
		HashMap<String,BigDecimal> hashTaxJurisdictionTotal = new HashMap<String,BigDecimal>();
		String sCurrentTaxJurisdictionLabel = "";
		BigDecimal bdPurchases = new BigDecimal("0.00");
		BigDecimal bdTaxablePurchases = new BigDecimal("0.00");
		BigDecimal bdSalesAfterDiscount = new BigDecimal("0.00");
		BigDecimal bdTaxableSalesAfterDiscount = new BigDecimal("0.00");
		BigDecimal bdPurchaseTaxDue = new BigDecimal("0.00");
		BigDecimal bdSalesTaxDue = new BigDecimal("0.00");
		BigDecimal bdSalesTaxCollected = new BigDecimal("0.00");
		bdTaxOwed = new BigDecimal("0.00");
		for (int i = 0; i < arrTaxJurisdictionLabels.size(); i++){
			//If we've already read a record - AND The next record is for a different jurisdiction, then print a line:
			if (
				(sCurrentTaxJurisdictionLabel.compareToIgnoreCase("") != 0)
				&& (arrTaxJurisdictionLabels.get(i).compareToIgnoreCase(sCurrentTaxJurisdictionLabel) != 0)
			){
				//Print a line for each tax jurisdiction:
				s += printSummaryTotalLine(
					sBackgroundColor,
					sCurrentTaxJurisdictionLabel,
					bdPurchases,
					bdTaxablePurchases,
					bdSalesAfterDiscount,
					bdTaxableSalesAfterDiscount,
					bdPurchaseTaxDue,
					bdSalesTaxDue,
					bdSalesTaxCollected,
					bdTaxOwed,
					iTotalNumberOfColumns
				);
				bdTaxOwed = addTaxOwed(bdPurchaseTaxDue,bdSalesTaxDue,bdSalesTaxCollected);
				getTotal(bdPurchases,
						bdTaxablePurchases,
						bdSalesAfterDiscount,
						bdTaxableSalesAfterDiscount,
						bdPurchaseTaxDue,
						bdSalesTaxDue, 
						bdSalesTaxCollected, 
						hashTaxJurisdictionTotal,
						bdTaxOwed);
				//Now clear the totals and start over:
				bdPurchases = new BigDecimal("0.00");
				bdTaxablePurchases = new BigDecimal("0.00");
				bdSalesAfterDiscount = new BigDecimal("0.00");
				bdTaxableSalesAfterDiscount = new BigDecimal("0.00");
				bdPurchaseTaxDue = new BigDecimal("0.00");
				bdSalesTaxDue = new BigDecimal("0.00");
				bdSalesTaxCollected = new BigDecimal("0.00");
			}
			sCurrentTaxJurisdictionLabel = arrTaxJurisdictionLabels.get(i);
			//Accumulate the totals:
			bdPurchases = bdPurchases.add(arrbdPurchaseCost.get(i));
			bdTaxablePurchases = bdTaxablePurchases.add(arrbdTaxablePurchases.get(i));
			bdSalesAfterDiscount = bdSalesAfterDiscount.add(arrbdSalePriceAfterDiscount.get(i));
			bdTaxableSalesAfterDiscount = bdTaxableSalesAfterDiscount.add(arrbdTaxableSales.get(i));
			bdPurchaseTaxDue = bdPurchaseTaxDue.add(arrbdPurchaseTaxDue.get(i));
			bdSalesTaxDue = bdSalesTaxDue.add(arrbdSalesTaxDue.get(i));
			bdSalesTaxCollected = bdSalesTaxCollected.add(arrbdSalesTaxCollected.get(i));
		}
		
		//Now print the last section, if there is one:
		s += printSummaryTotalLine(
			sBackgroundColor,
			sCurrentTaxJurisdictionLabel,
			bdPurchases,
			bdTaxablePurchases,
			bdSalesAfterDiscount,
			bdTaxableSalesAfterDiscount,
			bdPurchaseTaxDue,
			bdSalesTaxDue,
			bdSalesTaxCollected,
			bdTaxOwed,
			iTotalNumberOfColumns
		);
		bdTaxOwed = addTaxOwed(bdPurchaseTaxDue,bdSalesTaxDue,bdSalesTaxCollected);
		getTotal(bdPurchases,
				bdTaxablePurchases,
				bdSalesAfterDiscount,
				bdTaxableSalesAfterDiscount,
				bdPurchaseTaxDue,
				bdSalesTaxDue, 
				bdSalesTaxCollected, 
				hashTaxJurisdictionTotal,
				bdTaxOwed);
		
		
		bdTaxOwed = addTaxOwed(bdPurchaseTaxDue,bdSalesTaxDue,bdSalesTaxCollected);
		s += printTotal(hashTaxJurisdictionTotal,iTotalNumberOfColumns);
		
		//One separator line:
		s += printBlankSeparatorLine(sBackgroundColor, iTotalNumberOfColumns);

		return s;
		
	}
	public BigDecimal addTaxOwed (BigDecimal bdPurchaseTaxDue, BigDecimal bdSalesTaxDue, BigDecimal bdSalesTaxCollected){
		if(bdPurchaseTaxDue.signum() == -1 || bdSalesTaxDue.signum() == -1)
			return (bdPurchaseTaxDue.subtract(bdSalesTaxDue)).subtract(bdSalesTaxCollected);
		if(bdSalesTaxCollected.signum() == -1)
			return (bdPurchaseTaxDue.add(bdSalesTaxDue)).subtract(bdSalesTaxCollected);
		
		return (bdPurchaseTaxDue.add(bdSalesTaxDue)).subtract(bdSalesTaxCollected);
	}
	
	private String printSummaryHeadingLine(String sHeading, String sBackgroundColor, int iTotalNumberOfColumns){
		String s = "";
		s += "<TR style = \" background-color: " + sBackgroundColor +  "; \" >"
			+ "<TD COLSPAN = " + Integer.toString(iTotalNumberOfColumns - 9) + " ALIGN=RIGHT><B><U>" 
				+ sHeading 
				+ "</U></B></TD>"
				+ "<TD COLSPAN = " + Integer.toString(iTotalNumberOfColumns - 1) + "&nbsp;" + "</TD>" 
			+ "</TR>"
		;
		return s;
	}
	private String printSummaryTotalLine(
			String sBackgroundColor,
			String sLabel,
			BigDecimal bdPurchaseCost,
			BigDecimal bdTaxablePurchases,
			BigDecimal bdSalePriceAfterDiscount,
			BigDecimal bdTaxableSales,
			BigDecimal bdPurchaseTaxDue,
			BigDecimal bdSalesTaxDue,
			BigDecimal bdSalesTaxCollected,
			BigDecimal bdTaxOwed,
			int iTotalNumberOfColumns
			){
		String s = "";
		s += "<TR style = \" background-color: " + sBackgroundColor +  "; \" >"
			+ "<TD COLSPAN=" + Integer.toString(iTotalNumberOfColumns - 9) + "  class = \" rightjustifiedheading \" ALIGN=RIGHT><B>" + sLabel + ":</B></TD>"
			+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT>"
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, bdPurchaseCost)
			+ " </TD>"
			+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT>"
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, bdTaxablePurchases)
			+ " </TD>"
			+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT>"
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, bdSalePriceAfterDiscount)
			+ " </TD>"
			+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT>"
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, bdTaxableSales)
			+ " </TD>"
			+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT>"
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, bdPurchaseTaxDue)
			+ " </TD>"
			+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT>"
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, bdSalesTaxDue)
			+ " </TD>"
			+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT>"
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, bdPurchaseTaxDue.add(bdSalesTaxDue))
			+ " </TD>"
			+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT>"
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, bdSalesTaxCollected)
			+ " </TD>"
			+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT>"
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, (bdPurchaseTaxDue.add(bdSalesTaxDue)).subtract(bdSalesTaxCollected))
			+ " </TD>"
			+ "</TR>"
		;
		//System.out.println("TAX OWED "+bdTaxOwed.toString());
		return s;
	}
	
	private void getTotal (
			BigDecimal bdPurchaseCost,
			BigDecimal bdTaxablePurchases,
			BigDecimal bdSalePriceAfterDiscount,
			BigDecimal bdTaxableSales,
			BigDecimal bdPurchaseTaxDue,
			BigDecimal bdSalesTaxDue,
			BigDecimal bdSalesTaxCollected,
			HashMap<String,BigDecimal> totals,
			BigDecimal bdTaxOwed
			){
		if(totals.containsKey("INVENTORY COST")){
			totals.put("INVENTORY COST", totals.get("INVENTORY COST").add(bdPurchaseCost));
		}else{
			totals.put("INVENTORY COST", bdPurchaseCost);
		}
		
		if(totals.containsKey("TAXABLE INVENTORY COST")){
			totals.put("TAXABLE INVENTORY COST", totals.get("TAXABLE INVENTORY COST").add(bdTaxablePurchases));
		}else{
			totals.put("TAXABLE INVENTORY COST", bdTaxablePurchases);
		}
		
		if(totals.containsKey("SALES PRICE")){
			totals.put("SALES PRICE", totals.get("SALES PRICE").add(bdSalePriceAfterDiscount));
		}else{
			totals.put("SALES PRICE", bdSalePriceAfterDiscount);
		}
		if(totals.containsKey("TAXABLE SALES PRICE")){
			totals.put("TAXABLE SALES PRICE", totals.get("TAXABLE SALES PRICE").add(bdTaxableSales));
		}else{
			totals.put("TAXABLE SALES PRICE", bdTaxableSales);
		}
		if(totals.containsKey("PURCHASE TAX DUE")){
			totals.put("PURCHASE TAX DUE", totals.get("PURCHASE TAX DUE").add(bdPurchaseTaxDue));
		}else{
			totals.put("PURCHASE TAX DUE", bdPurchaseTaxDue);
		}
		if(totals.containsKey("SALES TAX DUE")){
			totals.put("SALES TAX DUE", totals.get("SALES TAX DUE").add(bdSalesTaxDue));
		}else{
			totals.put("SALES TAX DUE", bdSalesTaxDue);
		}
		if(totals.containsKey("TOTAL TAX DUE")){
			totals.put("TOTAL TAX DUE", totals.get("TOTAL TAX DUE").add(bdPurchaseTaxDue.add(bdSalesTaxDue)));
		}else{
			totals.put("TOTAL TAX DUE", bdPurchaseTaxDue.add(bdSalesTaxDue));
		}
		if(totals.containsKey("TAX COLLECTED")){
			totals.put("TAX COLLECTED", totals.get("TAX COLLECTED").add(bdSalesTaxCollected));
		}else{
			totals.put("TAX COLLECTED", bdSalesTaxCollected);
		}
		if(totals.containsKey("TAX OWED")){
			totals.put("TAX OWED", totals.get("TAX OWED").add(bdTaxOwed));
		}else{
			totals.put("TAX OWED", bdTaxOwed);
		}
		
	}

	private String printBlankSeparatorLine(String sBackgroundColor, int iTotalNumberOfColumns){
		String s = "";
		
		s += "<TR style = \" background-color: " + sBackgroundColor +  "; \" >"
				+ "<TD COLSPAN=" + Integer.toString(iTotalNumberOfColumns) + "  class = \" rightjustifiedheading \" ALIGN=RIGHT><B>" + "&nbsp;" + "</B></TD>"
				+ "</TR>"
			;
		return s;
	}
	
	private String printTotal( HashMap<String,BigDecimal> hashtable, int iTotalNumberOfColumns){
		String s = "";
		s += "<TR style = \" background-color: yellow; \" >"
			+ "<TD COLSPAN=" + Integer.toString(iTotalNumberOfColumns - 9) + "  class = \" rightjustifiedheading \" ALIGN=RIGHT><B> TOTAL: </B></TD>"
			+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT>"
			+clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, hashtable.get("INVENTORY COST"))
			+ " </TD>"
			+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT>"
			+clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, hashtable.get("TAXABLE INVENTORY COST"))
			+ " </TD>"
			+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT>"
			+clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, hashtable.get("SALES PRICE"))
			+ " </TD>"
			+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT>"
			+clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, hashtable.get("TAXABLE SALES PRICE"))
			+ " </TD>"
			+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT>"
			+clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, hashtable.get("PURCHASE TAX DUE"))
			+ " </TD>"
			+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT>"
			+clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, hashtable.get("SALES TAX DUE"))
			+ " </TD>"
			+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT>"
			+clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, hashtable.get("TOTAL TAX DUE"))
			+ " </TD>"
			+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT>"
			+clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, hashtable.get("TAX COLLECTED"))
			+ " </TD>"
			+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT>"
			+clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableinvoicedetails.bdlinetaxamountscale, hashtable.get("TAX OWED"))
			+ " </TD>"
			+ "</TR>";
		return s;
	}
	
	private String printSummaryTotalColumnHeadings(int iTotalNumberOfColumns, String sBackGroundColor){
		String s = "";
		s += "<TR style = \" background-color: " + sBackGroundColor +  "; \" >"
		+ "<TD COLSPAN = " + Integer.toString(iTotalNumberOfColumns - 9) + " ALIGN=RIGHT><B>" 
			+ "&nbsp;" 
			+ "</B></TD>"
			+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT><B><U>" 
			+ "INVENTORY<BR>COST" 
			+ "</U></B></TD>"
			+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT><B><U>" 
			+ "TAXABLE<BR>INVENTORY<BR>COST" 
			+ "</U></B></TD>"
			+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT><B><U>" 
			+ "SALES<BR>PRICE" 
			+ "</U></B></TD>"
			+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT><B><U>" 
			+ "TAXABLE<BR>SALES<BR>PRICE" 
			+ "</U></B></TD>"
			+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT><B><U>" 
			+ "PURCHASE<BR>TAX<BR>DUE" 
			+ "</U></B></TD>"
			+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT><B><U>" 
			+ "SALES<BR>TAX<BR>DUE" 
			+ "</U></B></TD>"
			+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT><B><U>" 
			+ "TOTAL<BR>TAX<BR>DUE" 
			+ "</U></B></TD>"
			+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT><B><U>" 
			+ "TAX<BR>COLLECTED" 
			+ "</U></B></TD>"
			+ "<TD  class = \" rightjustifiedheading \" ALIGN=RIGHT><B><U>" 
			+ "TAX<BR>OWED" 
			+ "</U></B></TD>"
		+ "</TR>"
	;
		return s;
	}
	
	private String printHeadingDescriptions(){
		String s = "";
		s += "<br><a name=\"inventorycost\"><SUP>1</SUP></a><B>INVENTORY COST</B>: "
			+ " for <B>STOCK</B> items, this is the actual inventoried cost for this item, recorded through the"
			+ " purchase order and Accounts Payable system and costed at the time of sale."
			+ "  For <B>NON-STOCK</B> ('expensed') items, this is the average cost for this item"
			+ " found on invoices in the Accounts Payable system at the time the invoice was originally generated."
			;		
		s += "<br><a name=\"taxablecost\"><SUP>2</SUP></a><B>TAXABLE INVENTORY COST </B>: "
			+ " the cost basis for purchase tax (or 'use' tax).  If the item itself is not 'Taxable' "
			+ "(i.e., eligible for tax in the jurisdiction), the TAXABLE INVENTORY COST is zero.  "
			+ "And if the tax type is NOT based on the PURCHASE COST then this TAXABLE INVENTORY COST "
			+ "is also zero.  But if the tax type is based on PURCHASE COST, then the 'TAXABLE INVENTORY COST' "
			+ "is the actual inventory cost."
			;		
		s += "<br><a name=\"saleprice\"><SUP>3</SUP></a><B>SALES PRICE </B>: "
			+ "the effective selling price for each item - what the customer actually pays. "
			+ " This takes into account any discount that might be on the invoice: if there is a "
			+ "total discount on the invoice, the 'SALES PRICE' for each item is prorated to arrive "
			+ "at the net total after discount."
			;		
		s += "<br><a name=\"taxablesalesprice\"><SUP>4</SUP></a><B>TAXABLE SALES PRICE </B>: "
			+ "If the item itself is not 'Taxable' (i.e., eligible for tax in the jurisdiction), "
			+ "the TAXABLE SALES PRICE is zero.  And if the tax type is NOT based on the SALES PRICE "
			+ "then this TAXABLE SALES PRICE is also zero.  But if the tax type is based on SALES PRICE, "
			+ "then the TAXABLE SALES PRICE is the actual SALES PRICE (above)."
			;		
		s += "<br><a name=\"purchasetaxdue\"><SUP>5</SUP></a><B>PURCHASE TAX DUE </B>: "
			+ "this is the TAXABLE INVENTORY COST times the tax rate."
			;
		s += "<br><a name=\"salestaxdue\"><SUP>6</SUP></a><B>SALES TAX DUE </B>: "				
			+ "this is the TAXABLE SALES PRICE times the tax rate."
			;
		s += "<br><a name=\"totaltaxdue\"><SUP>7</SUP></a><B>TOTAL TAX DUE </B>: "
			+ "this is the sum of the PURCHASE TAX DUE and the SALES TAX DUE."
			;		
		s += "<br><a name=\"taxcollected\"><SUP>8</SUP></a><B>TAX COLLECTED</B>: "
			+ "this is the amount already collected from the customer for each sale (generally retail sales tax)."
			;		
		s += "<br><a name=\"taxowed\"><SUP>9</SUP></a><B>TAX OWED</B>: "
			+ "this is the tax still owed; the TOTAL TAX DUE less the TAX COLLECTED."
			;
		return s;
	}
	/*
	private BigDecimal getCostOfNonStockItem(
		String sItemNumber, 
		double dQtyShipped, 
		Connection conn) throws Exception{
		
		BigDecimal bdExtendedCost = new BigDecimal("0.00");
		
		String SQL = "SELECT"
			+ " AVG(" + SMTableaptransactionlines.bdamount + " / " + SMTableaptransactionlines.bdqtyreceived + ") AS AVGUNITCOST"
			+ " FROM " + SMTableaptransactionlines.TableName
			+ " WHERE ("
				+ "(" + SMTableaptransactionlines.sitemnumber + " = '" + sItemNumber + "')"
				+ " AND (" + SMTableaptransactionlines.bdqtyreceived + " > 0.0000)"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				if (rs.getBigDecimal("AVGUNITCOST") != null){
					bdExtendedCost = rs.getBigDecimal("AVGUNITCOST").multiply(bdExtendedCost);
				}
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1521837145] reading average item cost for item '" + sItemNumber + "' - " + e.getMessage());
		}
		
		return bdExtendedCost;
	}
	*/
	private String sStyleScripts(){
		String s = "";
		String sRowHeight = "22px";
		s += "<style type=\"text/css\">\n";
		
		//Layout table:
		s +=
			"table.basic {"
			//+ "border-width: 0px; "
			//+ "border-spacing: 2px; "
			//+ "border-style: outset; "
			//+ "border-style: solid; "
			//+ "border-style: none; "
			//+ "border-color: black; "
			+ "border-collapse: collapse; "
			+ "width: 100%; "
			+ "font-size: " + "small" + "; "
			+ "font-family : Arial; "
			+ "color: black; "
			//+ "background-color: white; "
			+ "}"
			+ "\n"
			;
		
		s +=
			"table.basicwithborder {"
			+ "border-width: 1px; "
			+ "border-spacing: 2px; "
			+ "border-style: outset; "
			+ "border-style: solid; "
			//+ "border-style: none; "
			+ "border-color: black; "
			+ "border-collapse: separate; "
			+ "width: 100%; "
			+ "font-size: " + "small" + "; "
			+ "font-family : Arial; "
			+ "color: black; "
			//+ "background-color: white; "
			+ "}"
			+ "\n"
			;
		/*
		//This is the def for a label field:
		s +=
			"td.fieldlabel {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			//+ "vertical-align: text-middle;"
			//+ "background-color: black; "
			+ "font-weight: bold; "
			+ "text-align: right; "
			//+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;
		*/
		//This is the def for a table cell, left justified:
		s +=
			"td.leftjustifiedcell {"
			+ "height: " + sRowHeight + "; "
			//+ "border: 0px solid; "
			+ "border-style: none; "
			+ "padding: 2px; "
			//+ "border-color: " + CELL_BORDER_COLOR + "; "
			+ "vertical-align: top;"
			+ "font-family : Arial; "
			+ "font-weight: normal; "
			+ "font-size: small; "
			+ "text-align: left; "
			+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;

	    //style= \" word-wrap:break-word; \"
	    //style= \" word-wrap:normal; white-space:pre-wrap; \" 
		s +=
			"td.leftjustifiedcellforcewrap {"
			+ "height: " + sRowHeight + "; "
			//+ "border: 0px solid; "
			+ "border-style: none; "
			+ "padding: 2px; "
			//+ "border-color: " + CELL_BORDER_COLOR + "; "
			+ "vertical-align: top;"
			+ "font-family : Arial; "
			+ "font-weight: normal; "
			+ "font-size: small; "
			+ "text-align: left; "
			+ "color: black; "
			+ "word-wrap:break-word; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;
		//This is the def for a table cell, right justified:
		s +=
			"td.rightjustifiedcell {"
			+ "height: " + sRowHeight + "; "
			//+ "border: 0px solid; "
			+ "border-style: none; "
			+ "padding: 2px; "
			//+ "border-color: " + CELL_BORDER_COLOR + "; "
			+ "vertical-align: top;"
			+ "font-family : Arial; "
			+ "font-weight: normal; "
			+ "font-size: small; "
			+ "text-align: right; "
			+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a table cell, center justified:
		s +=
			"td.centerjustifiedcell {"
			+ "height: " + sRowHeight + "; "
			//+ "border: 0px solid; "
			+ "border-style: none; "
			+ "padding: 2px; "
			//+ "border-color: " + CELL_BORDER_COLOR + "; "
			+ "vertical-align: top;"
			+ "font-family : Arial; "
			+ "font-weight: normal; "
			+ "font-size: small; "
			+ "text-align: center; "
			+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a left-aligned heading on a table:
		s +=
			"td.leftjustifiedheading {"
			//+ "border: 0px solid; "
			+ "border-style: none; "
			//+ "bordercolor: 000; "
			+ "padding: 2px; "
			+ "font-family : Arial; "
			+ "font-size: small; "
			+ "font-weight: bold; "
			+ "text-align: left; "
			+ "vertical-align:bottom; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a right-aligned heading on a table:
		s +=
			"td.rightjustifiedheading {"
			//+ "border: 0px solid; "
			+ "border-style: none; "
			//+ "bordercolor: 000; "
			+ "padding: 2px; "
			+ "font-family : Arial; "
			+ "font-size: small; "
			+ "font-weight: bold; "
			+ "text-align: right; "
			+ "vertical-align:bottom; "
			+ "}"
			+ "\n"
			;

		//This is the def for a center-aligned heading on a table:
		s +=
			"td.centerjustifiedheading {"
			//+ "border: 0px solid; "
			+ "border-style: none; "
			//+ "bordercolor: 000; "
			+ "padding: 2px; "
			+ "font-family : Arial; "
			+ "font-size: small; "
			+ "font-weight: bold; "
			+ "text-align: center; "
			+ "vertical-align:bottom; "
			+ "}"
			+ "\n"
			;
		
		s += "</style>"
			+ "\n"
			;

		return s;
	}

	private String sJavaScripts(){
		String s = "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.12.2/jquery.min.js\"></script>\n"
				+ "<script type=\"text/javascript\" src=\"scripts/jquery.floatThead.min.js\"></script>\n"
				+ "<script>\n"
				+ "var $table = $(\".basic\");\n"
				+ "$table.floatThead();\n"
				+ "</script>\n";
		
		return s;
		
	}
}

package smcontrolpanel;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import SMClasses.SMOrderDetail;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTablebids;
import SMDataDefinition.SMTabledefaultsalesgroupsalesperson;
import SMDataDefinition.SMTabledoingbusinessasaddresses;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableicitemlocations;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTableordermgrcomments;
import SMDataDefinition.SMTablesalesgroups;
import SMDataDefinition.SMTablesalesperson;
import SMDataDefinition.SMTabletax;
import SMDataDefinition.SMTableusers;
import SMDataDefinition.SMTableworkorders;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;

public class SMPreInvoiceReport extends java.lang.Object{

	private final static String CURRENT_USER_COMMENT_COLOR = "GREEN";
	private final static String OTHER_USER_COMMENT_COLOR = "#8F008F";
	private static final String NONSTOCK_ITEM_FLAG = "<DIV style = \" color:red; font-weight:bold; \" >N/A</DIV>";
	
	private String m_sErrorMessage;
	
	private ArrayList <String> sCommenters = new ArrayList <String> (0);
	private ArrayList <Integer> iCommentCount = new ArrayList <Integer> (0);
	private ArrayList <Integer> iCommentCounter = new ArrayList <Integer> (0);
	
	public SMPreInvoiceReport(
			){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			String sStartingDate,
			String sEndingDate,
			ArrayList <String> sOrderTypes,
			ArrayList <String> sLocations,
			String sDBID,
			String sUserID,
			boolean bSuppressDetail,
			String sLastOrderEdited,
			boolean bReprocessing,
			boolean bAllowItemViewing,
			boolean bAllowOrderEditing,
			String sURLLinkBase,
			PrintWriter out,
			ServletContext context,
			String sLicenseModuleLevel
			){
		//Get username for functions that do not use userID
		String sUserName = SMUtilities.getUserNamebyUserID(sUserID, conn);
		
		String sCurrentServiceType = "";
		String sLastServiceType = "";
		String sCurrentOrderNumber = "";
		String sLastOrderNumber = "";
		String sCurrentLineID = "";
		String sLastLineID = "";
		
		//Declare line variables:
		String sLastItemNumber = "";
		String sLastItemDesc = "";
		long lLastSuppressItemOnInvoice = 0;
		String sLastCategory = "";
		BigDecimal bdLastQty = new BigDecimal(0);
		boolean bLastNonStockItemFlag = false;
		BigDecimal bdLastQtyOnHand = new BigDecimal(0);
		String sLastUOM = "";
		BigDecimal bdLastCost = new BigDecimal(0);
		BigDecimal bdLastPrice = new BigDecimal(0);
		String sLastLocation = "";
		long lLastTaxable = 0;
		String sLastMechanicFullName = "";
		String sLastInvoiceDetailComment = "";
		String sLastInternalDetailComment = "";
		String sLastTicketDetailComment = "";
		String sLineNumber = "";
		String sDetailNumber = "";
		String sUniqueOrderID = "";
		
		//Declare order variables:
		BigDecimal bdPriceTotalForOrder = new BigDecimal(0);
		BigDecimal bdCostTotalForOrder = new BigDecimal(0);
		BigDecimal bdDiscountPercent = new BigDecimal(0);
		BigDecimal bdOrderDiscountAmount = new BigDecimal(0);
		String sDiscountDesc = "";
		BigDecimal bdOrderTaxAmount = new BigDecimal(0);
		BigDecimal bdOrderTotalWithoutTax = new BigDecimal(0);
		BigDecimal bdDepositAmt = new BigDecimal(0);
		
		//Declare service type total variables:
		long lTotalNumberOfOrdersForServiceType = 0;
		BigDecimal bdTotalOrderPriceForServiceType = new BigDecimal(0);
		//BigDecimal bdTotalOrderTaxForServiceType = new BigDecimal(0); //Comes from Get_Invoice_Header_Field(dINVHDRTaxAmount)
				
		//Declare report total variables:
		long lGrandTotalNumberOfOrders = 0;
		BigDecimal bdGrandTotalOrderCost = new BigDecimal(0);
		BigDecimal bdGrandTotalOrderPriceWithTax = new BigDecimal(0);
		//BigDecimal bdTotalOrderTax = new BigDecimal(0);

		//Create string of order types:
		//String sOrderTypesString = "";
		//for (int i = 0; i < sOrderTypes.size(); i++){
			//sOrderTypesString += "," + sOrderTypes.get(i);
		//}
		
    	//SQL Statement:
		String SQL = "";
        SQL = "SELECT "
        	+ " 'Pre-Invoice Report' AS REPORTNAME"
        	+ ", '" + sUserName + "' AS USERNAME"
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCreationDate
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.dPrePostingInvoiceDiscountPercentage
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sPrePostingInvoiceDiscountDesc
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.dPrePostingInvoiceDiscountAmount
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.bdtaxbase
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderDate
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.datLastPostingDate
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderCreatedByFullName
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.lOrderCreatedByID
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sCustomerCode
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.staxjurisdiction
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.mInvoiceComments
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.mInternalComments
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderSourceDesc
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.iCustomerDiscountLevel
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sDefaultPriceListCode
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sPONumber
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sTerms
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.mTicketComments
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.dEstimatedHour
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.bdordertaxamount
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.LASTEDITUSERFULLNAME
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.LASTEDITUSERID
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.datLastPostingDate
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.bddepositamount
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.iSalesGroup
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.idoingbusinessasaddressid
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.staxtype
        	
        	+ ", " + "DETAILQUERY" + "." + SMTableorderdetails.dExtendedOrderPrice
        	+ ", " + "DETAILQUERY" + "." + SMTableorderdetails.iLineNumber
        	+ ", " + "DETAILQUERY" + "." + SMTableorderdetails.iDetailNumber
        	+ ", " + "DETAILQUERY" + "." + SMTableorderdetails.dUniqueOrderID
        	+ ", " + "DETAILQUERY" + "." + SMTableorderdetails.mInvoiceComments
        	+ ", " + "DETAILQUERY" + "." + SMTableorderdetails.mInternalComments
        	+ ", " + "DETAILQUERY" + "." + SMTableorderdetails.mTicketComments
        	+ ", " + "DETAILQUERY" + "." + SMTableorderdetails.sMechFullName
        	+ ", " + "DETAILQUERY" + "." + SMTableorderdetails.iTaxable
        	+ ", " + "DETAILQUERY" + "." + SMTableorderdetails.sLocationCode
        	+ ", " + "DETAILQUERY" + "." + SMTableorderdetails.sOrderUnitOfMeasure
        	+ ", " + "DETAILQUERY" + "." + SMTableorderdetails.dQtyShipped
        	+ ", " + "DETAILQUERY" + "." + SMTableorderdetails.sItemCategory
        	+ ", " + "DETAILQUERY" + "." + SMTableorderdetails.sItemDesc
        	+ ", " + "DETAILQUERY" + "." + SMTableorderdetails.sItemNumber
        	+ ", " + "DETAILQUERY" + "." + SMTableorderdetails.isuppressdetailoninvoice

        	+ ", " + SMTableicitems.TableName + "." + SMTableicitems.bdmostrecentcost + " AS MRC"
        	+ ", " + SMTableicitems.TableName + "." + SMTableicitems.inonstockitem
        	+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand
        	
        	+ ", " + SMTableusers.TableName + "." + SMTableusers.sUserFirstName
        	+ ", " + SMTableusers.TableName + "." + SMTableusers.sUserLastName
        	
        	+ ", " + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupDesc
        	
        	+ ", " + SMTablearcustomer.TableName + "." + SMTablearcustomer.irequirespo
        	
        	+ ", " + SMTablelocations.TableName + "." + SMTablelocations.sLocationDescription
        	
        	+ ", " + SMTabletax.TableName + "." + SMTabletax.sdescription
        	
        	+ " FROM "
        	
        	+ " (SELECT" 
        	+ " " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dExtendedOrderPrice
        	+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.iLineNumber
        	+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.iDetailNumber
        	+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dUniqueOrderID
        	+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.mInvoiceComments
        	+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.mInternalComments
        	+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.mTicketComments
        	+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sMechFullName
        	+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.iTaxable
        	+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sLocationCode
        	+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sOrderUnitOfMeasure
        	+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyShipped
        	+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemCategory
        	+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemDesc
        	+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemNumber
        	+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.isuppressdetailoninvoice
        	+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.strimmedordernumber
        	+ " FROM " + SMTableorderdetails.TableName
        	+ " WHERE ("
        		+ "(" + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyShipped + " != 0.00)"
        	+ ")"
        	+ ") AS DETAILQUERY"

			+ " LEFT JOIN " + SMTableorderheaders.TableName
			+ " ON " + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber + " = DETAILQUERY." + SMTableorderdetails.strimmedordernumber
        	
        	+ " LEFT JOIN " + SMTableicitems.TableName + " ON " + "DETAILQUERY" + "."
        	+ SMTableorderdetails.sItemNumber + " = " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
        	
        	+ " LEFT JOIN " + SMTableicitemlocations.TableName + " ON (" 
        		+ "(" + "DETAILQUERY" + "." + SMTableorderdetails.sItemNumber + " = " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber + ")"
        		+ " AND "
        		+ "(" + "DETAILQUERY" + "." + SMTableorderdetails.sLocationCode + " = " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sLocation + ")"
        	+ ")"
        	
        	+ " LEFT JOIN " + SMTableusers.TableName + " ON " + SMTableorderheaders.TableName + "." + SMTableorderheaders.LASTEDITUSERID
        	+ " = " + SMTableusers.TableName + "." + SMTableusers.lid
        	
        	+ " LEFT JOIN " + SMTablesalesgroups.TableName + " ON " + SMTableorderheaders.TableName + "." + SMTableorderheaders.iSalesGroup
        	+ " = " + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.iSalesGroupId
        	
        	+ " LEFT JOIN " + SMTablearcustomer.TableName + " ON " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sCustomerCode
        	+ " = " + SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerNumber
        	
        	+ " LEFT JOIN " + SMTablelocations.TableName + " ON " + SMTablelocations.TableName + "." + SMTablelocations.sLocation
        	+ " = " + "DETAILQUERY" + "." + SMTableorderdetails.sLocationCode
        	
        	+ " LEFT JOIN " + SMTabletax.TableName + " ON " + SMTabletax.TableName + "." + SMTabletax.lid
        	+ " = " + SMTableorderheaders.TableName + "." + SMTableorderheaders.itaxid
        	
        	+ " WHERE ("
        		//Select by last posting dates
        		+ "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datLastPostingDate + " >= '" 
        			+ sStartingDate + "')"
        		+ " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datLastPostingDate + " <= '" 
        			+ sEndingDate + " 23:59:59')"
        	
        		//Order Types:
        		+ " AND ("
            	;
        		for (int i = 0; i < sOrderTypes.size(); i++){
        			if (i == 0){
        				SQL += "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode + "='" + sOrderTypes.get(i) + "')";
        			}else{
        				SQL += " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode + "='" + sOrderTypes.get(i) + "')";
        			}
        		}
            	SQL += ")"
        			
        		//Get the locations:
        		//TJR - 9/26/2014 - removed this to eliminate problems with similar location codes, like 'CH' and 'CHLDS'
                //+ " AND (INSTR('" + sLocationsString + "', " + SMTableorderheaders.TableName + "." 
            	//	+ SMTableorderheaders.sLocation + ") > 0)"
            		
            	+ " AND ("
            	;
        		for (int i = 0; i < sLocations.size(); i++){
        			if (i == 0){
        				SQL += "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sLocation + "='" + sLocations.get(i) + "')";
        			}else{
        				SQL += " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sLocation + "='" + sLocations.get(i) + "')";
        			}
        		}
            	SQL += ")"
                
                //No quotes:
                + " AND (" + SMTableorderheaders.TableName + "." 
                	+ SMTableorderheaders.iOrderType + " != " + Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE) + ")"
        			
        	+ ")"
        	+ " ORDER BY " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode
        		+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber
        		+ ", " + "DETAILQUERY" + "." + SMTableorderdetails.iLineNumber
        	;
			
		//Check permissions for viewing invoices and orders:
		boolean bViewOrderPermitted = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMViewOrderInformation,
			sUserID,
			conn,
			sLicenseModuleLevel);
		
		boolean bViewJobCostSummaryPermitted = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMJobCostSummaryReport,
				sUserID,
				conn,
				sLicenseModuleLevel);
		
		//System.out.println("In " + this.toString() + ".processReport - SQL = " + SQL);
		boolean bOddRow = true;
		
		out.println(
				"<FONT SIZE=2>"
				+ getPreviousCommenters(
						sStartingDate,
						sEndingDate,
						sOrderTypes,
						sLocations,
						conn
				)
				+ "</FONT><BR>"
		);
		
		//Place a link to the 'last edited' bookmark:
		if (sLastOrderEdited.trim().compareToIgnoreCase("") != 0){
			out.println("<a href=\"#LastEdit\">Go to last edit</a>");
		}
		
		out.println(sCommandScripts());
		
		//Set up the form:
		out.println ("<FORM ID='MAINFORM' NAME='MAINFORM' ACTION =\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMPrintPreInvoiceGenerate\" METHOD=\"POST\">");
		
		//Convert the starting and ending dates back to MM/dd/yyyy format:
		String sStart = null;
		try {
			sStart = clsDateAndTimeConversions.utilDateToString(
				clsDateAndTimeConversions.StringTojavaSQLDate("yyyy-MM-dd", sStartingDate), "MM/dd/yyyy");
		} catch (ParseException e1) {
			System.out.println("Error:[1423579524] Invalid Date '" + sStart +"' -" + e1.getMessage());
			return false;
		}
		String sEnd = null;
		try {
			sEnd = clsDateAndTimeConversions.utilDateToString(
					clsDateAndTimeConversions.StringTojavaSQLDate("yyyy-MM-dd", sEndingDate), "MM/dd/yyyy");
		} catch (ParseException e1) {
			System.out.println("Error:[1423579525] Invalid Date '" + sEnd +"' -" + e1.getMessage());
			return false;
		}
		
		printHiddenVariables(
				out,
				"smcontrolpanel.SMPrintPreInvoiceSelection",
				sStart,
				sEnd,
				sOrderTypes,
				sLocations,
				sDBID
			);
		
    	try{
    		//System.out.println("In " + this.toString() + " SQL start time = " + SMUtilities.now("hh:mm:ss"));
    		//System.out.println("Pre-Invoice SQL - [1500396042] = " + SQL);
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			//System.out.println("In " + this.toString() + " SQL end time = " + SMUtilities.now("hh:mm:ss"));
			while(rs.next()){
				BigDecimal bdMostRecentCost = new BigDecimal(0.00);
				if (rs.getBigDecimal("MRC") != null){
					bdMostRecentCost = rs.getBigDecimal("MRC");
				}
				//If it's a non-stock item, then we assume it will have no cost:
				if (rs.getInt(SMTableicitems.TableName + "." + SMTableicitems.inonstockitem) == 1){
					bdMostRecentCost = BigDecimal.ZERO;
				}
				sCurrentServiceType
					= rs.getString(SMTableorderheaders.TableName + "." 
					+ SMTableorderheaders.sServiceTypeCode);
				
				sCurrentOrderNumber
					= rs.getString(SMTableorderheaders.TableName + "." 
					+ SMTableorderheaders.sOrderNumber);
				

				sUniqueOrderID = Double.toString(rs.getDouble(
						"DETAILQUERY." + SMTableorderdetails.dUniqueOrderID));
				
				sCurrentLineID = sCurrentOrderNumber 
					+ Long.toString(rs.getLong("DETAILQUERY." 
							+ SMTableorderdetails.iLineNumber));

				//If it's a new line AND if there was a previous line, print it:
				if (
						(sCurrentLineID.compareToIgnoreCase(sLastLineID) != 0)
						&& (sLastLineID.compareToIgnoreCase("") != 0)
				){
		    		printLineFooter (
		        		sLastOrderNumber,
	        			sLastItemNumber, 
	        			sLastItemDesc,
	        			lLastSuppressItemOnInvoice,
	        			sLastCategory,
	        			bdLastQty,
	        			bLastNonStockItemFlag,
	        			bdLastQtyOnHand,
	        			sLastUOM,
	        			bdLastCost,
	        			bdLastPrice,
	        			sLastLocation,
	        			lLastTaxable,
	        			sLastMechanicFullName,
	        			sLastInvoiceDetailComment,
	        			sLastInternalDetailComment,
	        			sLastTicketDetailComment,
	        			sDetailNumber,
	        			sLineNumber,
	        			sUniqueOrderID,
	        			bOddRow,
	        			bAllowItemViewing,
	        			bAllowOrderEditing,
	        			sURLLinkBase,
	        			sDBID,
	        			out,
	        			context
	        			);
				}
				
				//If it's a new order number AND if there was a previous order number,
				//print the footer for it:
				if (
						(sCurrentOrderNumber.compareToIgnoreCase(sLastOrderNumber) != 0)
						&& (sLastOrderNumber.compareToIgnoreCase("") != 0)
					){
						//Last minute math for order totals here:
						bdOrderTotalWithoutTax = bdPriceTotalForOrder.subtract(bdOrderDiscountAmount);
											
				    	printOrderFooter(
				    			bdPriceTotalForOrder,
				    			bdCostTotalForOrder,
				    			bdPriceTotalForOrder,
				    			bdDiscountPercent,
				    			sDiscountDesc,
				    			bdOrderDiscountAmount,
				    			bdOrderTaxAmount,
				    			bdOrderTotalWithoutTax,
				    			bdDepositAmt,
				    			sLastOrderNumber,
				    			out,
				    			conn
				        );

				    	//Reset the order variables:
				    	bdCostTotalForOrder = BigDecimal.ZERO;
		    			bdPriceTotalForOrder = BigDecimal.ZERO;
		    			bdDiscountPercent = BigDecimal.ZERO;
		    			bdOrderDiscountAmount = BigDecimal.ZERO;
		    			bdOrderTaxAmount = BigDecimal.ZERO;
		    			bdOrderTotalWithoutTax = BigDecimal.ZERO;
		    			bdDepositAmt = BigDecimal.ZERO;
		    			sDiscountDesc = "";
				}

				//If it's a new service type AND If there was a previous service type,
				//print the footer for it:
				if (
					(sCurrentServiceType.compareToIgnoreCase(sLastServiceType) != 0)
					&& (sLastServiceType.compareToIgnoreCase("") != 0)
				){
			    	printServiceTypeFooter(
			    			sLastServiceType,
			    			lTotalNumberOfOrdersForServiceType,
			    			bdTotalOrderPriceForServiceType,
			    			out
			        );

			    	//Reset the service type variables:
	    			lTotalNumberOfOrdersForServiceType = 0;
	    			bdTotalOrderPriceForServiceType = BigDecimal.ZERO;
	    			//bdTotalOrderTaxForServiceType = BigDecimal.ZERO;
				}

				//If the service type is a new one, print the service type header:
				if (sCurrentServiceType.compareToIgnoreCase(sLastServiceType) != 0){
					printServiceTypeHeader(sCurrentServiceType, out);
				}
				
				//If it's a new order number, print the order header:
				if (sCurrentOrderNumber.compareToIgnoreCase(sLastOrderNumber) != 0){
					String sOrderCreationDateTime = clsDateAndTimeConversions.utilDateToString(rs.getDate(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.datOrderCreationDate), "MM/dd/yyyy");
					sOrderCreationDateTime = sOrderCreationDateTime + " " + clsDateAndTimeConversions.utilDateToString(rs.getTime(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.datOrderCreationDate), "h:mm a");

					String sLastPostingTime = clsDateAndTimeConversions.utilDateToString(rs.getDate(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.datLastPostingDate), "MM/dd/yyyy");
					sLastPostingTime = sLastPostingTime + " " + clsDateAndTimeConversions.utilDateToString(rs.getTime(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.datLastPostingDate), "h:mm a");
					
					bdDiscountPercent = BigDecimal.valueOf(
	    					rs.getDouble(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.dPrePostingInvoiceDiscountPercentage));
	    			sDiscountDesc = rs.getString(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.sPrePostingInvoiceDiscountDesc);
	    			bdOrderDiscountAmount = BigDecimal.valueOf(
	    					rs.getDouble(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.dPrePostingInvoiceDiscountAmount));
	    			bdOrderTaxAmount = BigDecimal.valueOf(
	    					rs.getDouble(SMTableorderheaders.TableName + "." 
	    					+ SMTableorderheaders.bdordertaxamount));
	    			bdDepositAmt = rs.getBigDecimal(SMTableorderheaders.TableName + "." 
	    					+ SMTableorderheaders.bddepositamount);

	    			printOrderHeader(
						sCurrentOrderNumber,
						clsDateAndTimeConversions.utilDateToString(
							rs.getDate(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.datOrderDate), "MM/dd/yyyy"),
						clsDateAndTimeConversions.utilDateToString(
							rs.getDate(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.datLastPostingDate), "MM/dd/yyyy"),
						sOrderCreationDateTime,
						bViewOrderPermitted,
						rs.getString(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.sCustomerCode).trim(),
						rs.getString(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.sBillToName).trim(),
						bViewJobCostSummaryPermitted,
						rs.getString(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.staxtype),
						rs.getString(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.staxjurisdiction).trim(),
						rs.getString(SMTabletax.TableName + "." 
							+ SMTabletax.sdescription).trim(),	
						rs.getString(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.sSalesperson).trim(),
						rs.getString(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.mInvoiceComments),
						rs.getString(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.mInternalComments),
						rs.getString(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.mTicketComments),
						rs.getString(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.sShipToName),
						rs.getString(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.sOrderSourceDesc),
						rs.getLong(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.iCustomerDiscountLevel),
						rs.getString(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.sDefaultPriceListCode),
						rs.getString(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.sPONumber),
						rs.getInt(SMTablearcustomer.TableName + "." + SMTablearcustomer.irequirespo) == 1,
						rs.getString(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.sTerms),
						rs.getString(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.sOrderCreatedByFullName).trim(),
//						sOrderCompletionDate,
						sLastOrderEdited,
						sUserID,
						rs.getDouble(SMTableorderheaders.TableName + "." 
								+ SMTableorderheaders.dEstimatedHour),
						rs.getString(SMTableusers.TableName + "." + SMTableusers.sUserFirstName) + " " 
								+ rs.getString(SMTableusers.TableName + "." + SMTableusers.sUserLastName),
						sLastPostingTime,
						rs.getBigDecimal(SMTableorderheaders.TableName + "." 
								+ SMTableorderheaders.bddepositamount),
						rs.getString(SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupDesc),
						rs.getLong(SMTableorderheaders.TableName + "." + SMTableorderheaders.iSalesGroup),
						Long.toString(rs.getLong(SMTableorderheaders.TableName + "." + SMTableorderheaders.idoingbusinessasaddressid)),
						bAllowOrderEditing,
						sDBID,
						out,
						conn,
						context,
						sUserName
					);
					
					//Accumulate the order totals:
					lGrandTotalNumberOfOrders++;
					lTotalNumberOfOrdersForServiceType++;
					
					//Subtract the discount amount from the order total for the service type:
					bdTotalOrderPriceForServiceType = 
						bdTotalOrderPriceForServiceType.subtract(bdOrderDiscountAmount);
					
					//Add the tax to the order total for the service type:
					bdTotalOrderPriceForServiceType = 
						bdTotalOrderPriceForServiceType.add(bdOrderTaxAmount);
					
					//Subtract the discount amount from the overall order total:
					bdGrandTotalOrderPriceWithTax = 
						bdGrandTotalOrderPriceWithTax.subtract(bdOrderDiscountAmount);
					//Add the tax to the order total:
					bdGrandTotalOrderPriceWithTax = 
						bdGrandTotalOrderPriceWithTax.add(bdOrderTaxAmount);
				}
				
				//If it's a new line, print any line header here:
				
				//Update the 'rememberers'
				sLastServiceType = sCurrentServiceType;
				sLastOrderNumber = sCurrentOrderNumber;
				sLastLineID = sCurrentLineID;
				
				//Line variables:
				sLastItemNumber = rs.getString(
						"DETAILQUERY." + SMTableorderdetails.sItemNumber).trim();
				
				sLastItemDesc = rs.getString(
						"DETAILQUERY." + SMTableorderdetails.sItemDesc).trim();
				
				lLastSuppressItemOnInvoice = rs.getLong(
						"DETAILQUERY." + SMTableorderdetails.isuppressdetailoninvoice);
				
				sLastCategory = rs.getString(
						"DETAILQUERY." + SMTableorderdetails.sItemCategory).trim();

				bdLastQty = BigDecimal.valueOf(
					rs.getDouble(
							"DETAILQUERY." + SMTableorderdetails.dQtyShipped));
				
				bLastNonStockItemFlag = rs.getBoolean(SMTableicitems.TableName + "." + SMTableicitems.inonstockitem);
				
				bdLastQtyOnHand = BigDecimal.valueOf(
						rs.getDouble(
								SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand));

				sLastUOM = rs.getString(
						"DETAILQUERY." + SMTableorderdetails.sOrderUnitOfMeasure).trim();

				bdLastCost = bdMostRecentCost;

				bdLastPrice = BigDecimal.valueOf(
					rs.getDouble(
							"DETAILQUERY." + SMTableorderdetails.dExtendedOrderPrice));
				bdLastPrice = bdLastPrice.setScale (2,
						BigDecimal.ROUND_HALF_UP);
				
				sLastLocation = rs.getString(
						"DETAILQUERY." + SMTableorderdetails.sLocationCode).trim();
				if (rs.getString(SMTablelocations.TableName + "." + SMTablelocations.sLocationDescription) != null){
					sLastLocation = sLastLocation + " - " 
					+ rs.getString(SMTablelocations.TableName + "." + SMTablelocations.sLocationDescription).trim();
				}
				
				lLastTaxable = rs.getLong(
						"DETAILQUERY." + SMTableorderdetails.iTaxable);
				
				sLastMechanicFullName = rs.getString(
						"DETAILQUERY." + SMTableorderdetails.sMechFullName);
				
				sLastInvoiceDetailComment = rs.getString(
						"DETAILQUERY." + SMTableorderdetails.mInvoiceComments);
				
				sLastInternalDetailComment = rs.getString(
						"DETAILQUERY." + SMTableorderdetails.mInternalComments);
				
				sLastTicketDetailComment = rs.getString(
						"DETAILQUERY." + SMTableorderdetails.mTicketComments);
				
				sLineNumber = Long.toString(rs.getLong(
						"DETAILQUERY." + SMTableorderdetails.iLineNumber));
				sDetailNumber = Long.toString(rs.getLong(
						"DETAILQUERY." + SMTableorderdetails.iDetailNumber));
				
				//Accumulate:
				bdPriceTotalForOrder = bdPriceTotalForOrder.add(
	    				BigDecimal.valueOf(rs.getDouble(
	    						"DETAILQUERY." 
	    						+ SMTableorderdetails.dExtendedOrderPrice)).setScale(2, BigDecimal.ROUND_HALF_UP));
				bdPriceTotalForOrder.setScale(2, BigDecimal.ROUND_HALF_UP);
				
				bdCostTotalForOrder = bdCostTotalForOrder.add(
					bdMostRecentCost.multiply(
	    				BigDecimal.valueOf(rs.getDouble("DETAILQUERY." + SMTableorderdetails.dQtyShipped))));
				//bdTotalOrderTax = 
				//	bdTotalOrderTax.add(
	    		//			BigDecimal.valueOf(rs.getDouble(SMTableorderdetails.TableName + "." 
	    		//				+ SMTableorderdetails.dLineTaxAmount)));

				bdGrandTotalOrderPriceWithTax = 
					bdGrandTotalOrderPriceWithTax.add(
	    					BigDecimal.valueOf(rs.getDouble("DETAILQUERY." 
	    						+ SMTableorderdetails.dExtendedOrderPrice)));

				bdGrandTotalOrderCost = 
					bdGrandTotalOrderCost.add(
						bdMostRecentCost.multiply(BigDecimal.valueOf(rs.getDouble("DETAILQUERY." + SMTableorderdetails.dQtyShipped))));
				
    			bdTotalOrderPriceForServiceType = 
    				bdTotalOrderPriceForServiceType.add(
    					BigDecimal.valueOf(rs.getDouble("DETAILQUERY." 
    						+ SMTableorderdetails.dExtendedOrderPrice)));

    			//bdTotalOrderTaxForServiceType = 	    				
    			//	bdTotalOrderTaxForServiceType.add(
    			//			BigDecimal.valueOf(rs.getDouble(SMTableorderdetails.TableName + "." 
	    		//				+ SMTableorderdetails.dLineTaxAmount)));
				bOddRow = !bOddRow;
			}
			rs.close();
    	}catch (SQLException e){
    		m_sErrorMessage = "Error reading resultset - " + e.getMessage();
    		return false;
    	}

    	if (sCurrentLineID.compareToIgnoreCase("") != 0){
    		printLineFooter (
    			sLastOrderNumber,
    			sLastItemNumber, 
    			sLastItemDesc,
    			lLastSuppressItemOnInvoice,
    			sLastCategory,
    			bdLastQty,
    			bLastNonStockItemFlag,
    			bdLastQtyOnHand,
    			sLastUOM,
    			bdLastCost,
    			bdLastPrice,
    			sLastLocation,
    			lLastTaxable,
    			sLastMechanicFullName,
    			sLastInvoiceDetailComment,
    			sLastInternalDetailComment,
    			sLastTicketDetailComment,
    			sDetailNumber,
    			sLineNumber,
    			sUniqueOrderID,
    			bOddRow,
    			bAllowItemViewing,
    			bAllowOrderEditing,
    			sURLLinkBase,
    			sDBID,
    			out,
    			context);
    	}
    	
    	//Print the order footer if there was at least one order:
    	if (sCurrentOrderNumber.compareToIgnoreCase("") != 0){
			//Last minute math for order totals here:
			bdOrderTotalWithoutTax = bdPriceTotalForOrder.subtract(bdOrderDiscountAmount);
    		printOrderFooter(
	    		bdPriceTotalForOrder,
	    		bdCostTotalForOrder,
	    		bdPriceTotalForOrder,
    			bdDiscountPercent,
    			sDiscountDesc,
    			bdOrderDiscountAmount,
    			bdOrderTaxAmount,
    			bdOrderTotalWithoutTax,
    			bdDepositAmt,
    			sLastOrderNumber,
    			out,
    			conn
    		);
    	}
    	
    	//Print the service type footer if there was at least one service type:
    	if (sCurrentServiceType.compareToIgnoreCase("") != 0){
	    	printServiceTypeFooter(
				sLastServiceType,
				lTotalNumberOfOrdersForServiceType,
				bdTotalOrderPriceForServiceType,
				out
	    	);
    	}
		printReportFooter(
			lGrandTotalNumberOfOrders,
			bdGrandTotalOrderCost,
			bdGrandTotalOrderPriceWithTax,
			out		
		);
		
		//End the form:
		out.println ("</FORM>");
		return true;
	}
	private void printLineFooter(
			String sOrderNum,
			String sItem,
			String sDesc,
			long iSupressItemOnInvoice,
			String sCategory,
			BigDecimal bdQty,
			boolean bNonStockItem,
			BigDecimal bdQtyOnHand,
			String sUnit,
			BigDecimal bdCost,
			BigDecimal bdPrice,
			String sLocation,
			long lTaxable,
			String sMechanicFullName,
			String sInvoiceDetailComment,
			String sInternalDetailComment,
			String sTicketDetailComment,
			String sDetailNumber,
			String sLineNumber,
			String sUniqueOrderID,
			boolean bIsOddRow,
			boolean bAllowItemViewing,
			boolean bAllowOrderEditing,
			String sURLLinkBase,
			String sDBID,
			PrintWriter pwOut,
			ServletContext context
	){
		
		String sBackgroundColor = "";
		String sItemNumberLink = "";
		if(bIsOddRow){
			sBackgroundColor = "\"#EEEEEE\"";
		}else{
			sBackgroundColor = "\"#FFFFFF\"";
		}
		pwOut.println("<TR bgcolor =" + sBackgroundColor + ">");
		
		String sLineNumberLink = "<A HREF=\"" 
				+ SMUtilities.getURLLinkBase(context) 
				+ "smcontrolpanel.SMEditOrderDetailEdit?"
				+ SMOrderDetail.Paramstrimmedordernumber + "=" + sOrderNum.trim()
				+ "&" + SMOrderDetail.ParamiDetailNumber + "=" + sDetailNumber
				+ "&" + SMOrderDetail.ParamiLineNumber + "=" + sLineNumber
				+ "&" + SMOrderDetail.ParamdUniqueOrderID + "=" + sUniqueOrderID
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "&CallingClass=" + this.getClass().getName()
				+ "\">" + sLineNumber + "</A>";
		if (bAllowOrderEditing){
			pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + sLineNumberLink + "&nbsp;</FONT></TD>");
		}else{
			pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + sLineNumber + "&nbsp;</FONT></TD>");
		}
		if (bAllowItemViewing){
			sItemNumberLink = "<A HREF=\"" + sURLLinkBase 
			+ "smic.ICDisplayItemInformation?ItemNumber=" 
			+ sItem
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "\">" + sItem + "</A>";
		}else{
			sItemNumberLink = sItem;
		}
			
		pwOut.println("<TD><FONT SIZE=2>" + sItemNumberLink + "</FONT></TD>");
		String sFinalDesc = sDesc;
		if (iSupressItemOnInvoice == 1){
			if (sDesc.substring(0, 2).compareToIgnoreCase("DNP") != 0){
				sFinalDesc = "(DNP) " + sDesc;
			}
		}
		pwOut.println("<TD><FONT SIZE=2>" + sFinalDesc + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2>" + sCategory + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2>" + sLocation + "</FONT></TD>");
		if (sMechanicFullName != null){
			if (sMechanicFullName.trim().compareToIgnoreCase("") != 0){
				pwOut.println("<TD><FONT SIZE=2>" + sMechanicFullName + "</FONT></TD>");
			}else{
				pwOut.println("<TD><FONT SIZE=2>" + "&nbsp;" + "</FONT></TD>");
			}
		}else{
			pwOut.println("<TD><FONT SIZE=2>" + "&nbsp;" + "</FONT></TD>");
		}

		pwOut.println(
				"<TD ALIGN=RIGHT><FONT SIZE=2>" 
				+ clsManageBigDecimals.BigDecimalToFormattedString("########0.0000", bdQty) + "</FONT></TD>");
		if (!bNonStockItem){
			BigDecimal bdUnderstocked = new BigDecimal("0.00");
			bdUnderstocked = bdQty.subtract(bdQtyOnHand);
			if (bdUnderstocked.compareTo(BigDecimal.ZERO) > 0.00){
				pwOut.println("<TD ALIGN=RIGHT><B><FONT SIZE=2 COLOR=RED>" 
					+ clsManageBigDecimals.BigDecimalToFormattedString("########0.0000", bdQtyOnHand) + "</FONT><B></TD>");
			}else{
				pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" 
					+ clsManageBigDecimals.BigDecimalToFormattedString("########0.0000", bdQtyOnHand) + "</FONT></TD>");
			}
		}else{
			pwOut.println(
					"<TD ALIGN=RIGHT><FONT SIZE=2>N/A</FONT></TD>");
		}
		pwOut.println("<TD><FONT SIZE=2>" + sUnit + "</FONT></TD>");
		
		//If it's a NON STOCK item, then don't show a cost:
		String sExtendedItemCost = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCost.multiply(bdQty));
		if (bNonStockItem){
			sExtendedItemCost = NONSTOCK_ITEM_FLAG;
		}
		pwOut.println(
			"<TD ALIGN=RIGHT><FONT SIZE=2>" 
			+ sExtendedItemCost + "</FONT></TD>");
		pwOut.println(
			"<TD ALIGN=RIGHT><FONT SIZE=2>" 
			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdPrice) + "</FONT></TD>");
		
		if (lTaxable > 0){
			pwOut.println("<TD ALIGN=CENTER><FONT SIZE=2>Y</FONT></TD>");
		}else{
			pwOut.println("<TD ALIGN=CENTER><FONT SIZE=2>N</FONT></TD>");
		}
				
		pwOut.println("</TR>");
		
		if (sTicketDetailComment != null){
			if (sTicketDetailComment.trim().compareToIgnoreCase("") != 0){
				pwOut.println("<TR><TD>&nbsp;</TD><TD ALIGN=LEFT COLSPAN=8><FONT SIZE=2><B>Work Order Detail Comments:</B></FONT></TD></TR>");
				pwOut.println("<TR><TD ALIGN=RIGHT><FONT SIZE=2>" + sLineNumber + "&nbsp;</FONT></TD><TD ALIGN=LEFT COLSPAN=8><FONT SIZE=2>" + sTicketDetailComment.trim() + "</FONT></TD></TR>");
			}
		}
		
		if (sInvoiceDetailComment != null){
			if (sInvoiceDetailComment.trim().compareToIgnoreCase("") != 0){
				pwOut.println("<TR><TD>&nbsp;</TD><TD ALIGN=LEFT COLSPAN=8><FONT SIZE=2><B>Invoice Detail Comments:</B></FONT></TD></TR>");
				pwOut.println(parseLineComment(sInvoiceDetailComment, sLineNumber));
			}
		}
		
		if (sInternalDetailComment != null){
			if (sInternalDetailComment.trim().compareToIgnoreCase("") != 0){
				pwOut.println("<TR><TD>&nbsp;</TD><TD ALIGN=LEFT COLSPAN=8><FONT SIZE=2><B>Internal Detail Comments:</B></FONT></TD></TR>");
				pwOut.println(parseLineComment(sInternalDetailComment, sLineNumber));
			}
		}		

	}
	private String parseLineComment(
			String sInvComment,
			String sLineNo
	){
		
		String sParsedComment = "";
		String sPrefix = 
			"<TR><TD ALIGN=RIGHT><FONT SIZE=2>" 
			+ sLineNo
			+ "&nbsp;</TD><TD>"
		;
		
		String sSuffix = "</FONT></TD></TR>";
		
		String sRemainingLine = sInvComment;
		int iCommentLineCounter = 97;
		while (sRemainingLine.length() > 0){
			sParsedComment = sParsedComment + sPrefix;
			
			sParsedComment = 
				sParsedComment
				//+ "(" 
				+ (char)iCommentLineCounter + ")"
				+ "</FONT></TD>"
				+ "<TD COLSPAN=9><FONT SIZE=2>"
				;
			
			if (sRemainingLine.contains("\n")){
				sParsedComment = sParsedComment 
				+ sRemainingLine.substring(0, sRemainingLine.indexOf("\n")).replace("\n", "");
				
				sParsedComment = sParsedComment + sSuffix;

				sRemainingLine =
					sRemainingLine.substring(sRemainingLine.indexOf("\n") + 1, sRemainingLine.length());

			}else{
				sParsedComment = sParsedComment 
				+ sRemainingLine;
				
				sParsedComment = sParsedComment + sSuffix;
				
				sRemainingLine = "";
			}
			sParsedComment = sParsedComment + sSuffix;
			
			iCommentLineCounter++;
		}
		
		return sParsedComment;
	}
	private void printOrderHeader(
			String sOrdNumber,
			String sOrderDate,
			String sLastPostingDate,
			String sCreationTime,
			boolean bViewOrder,
			String sCustCode,
			String sBillToName,
			boolean bViewJobCost,
			String sTaxType,
			String sTaxGroup,
			String sTaxDescription,
			String sSalesCode,
			String sInvoiceComment,
			String sInternalComment,
			String sTicketComment,
			String sShipToName,
			String sOrderSource,
			long lPriceLevel,
			String sPriceList,
			String sPONumber,
			boolean bRequiresPO,
			String sTerms,
			String sOrderCreatedByFullName,
//			String sOrderCompletionDate,
			String sLastOrderEdited,
			String sCurrentUserID,
			Double bdEstimatedTime,
			String sLastEditedBy,
			String sLastPostingTime,
			BigDecimal bdDepositAmt,
			String sSalesGroupDescription,
			long iSalesGroupID,
			String sDBAid,
			boolean bAllowOrderEditing,
			String sDBID,
			PrintWriter pwOut,
			Connection con,
			ServletContext context,
			String sUserName
	){
		
		String SQL = "";
		
		//System.out.println("[1379709142] SMPreInvoice.printOrderHeader: sOrdNumber = '" + sOrdNumber + "'");
		
		sOrdNumber = sOrdNumber.trim();
		
		//If this was the last order edited, place a bookmark here:
		if (sLastOrderEdited.trim().compareToIgnoreCase(sOrdNumber.trim()) == 0){
			pwOut.println("<a name=\"LastEdit\">");
		}
		
		pwOut.println("<B><FONT SIZE=2>Order");
		pwOut.println(" :&nbsp;</B>");

		if (bViewOrder){
			pwOut.println( 
					"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" 
		    		+ sOrdNumber 
		    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
		    		+ "\">" + sOrdNumber + "</A></TD>"
					);
		}else{
			pwOut.println(sOrdNumber);
		}
		
		if (bAllowOrderEditing){
			pwOut.println("<FONT SIZE=2>");
			pwOut.println("&nbsp;&nbsp;<A HREF=\"" 
						+ SMUtilities.getURLLinkBase(context) 
						+ "smcontrolpanel.SMEditOrderEdit?"
						+ SMOrderHeader.Paramstrimmedordernumber + "=" + sOrdNumber 
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "&CallingClass=" + this.getClass().getName()
						+ "\">Edit order header</A>");
			
			pwOut.println("&nbsp;&nbsp;<A HREF=\"" 
						+ SMUtilities.getURLLinkBase(context) 
						+ "smcontrolpanel.SMOrderDetailList?"
						+ SMOrderHeader.Paramstrimmedordernumber + "=" + sOrdNumber 
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "&CallingClass=" + this.getClass().getName()
						+ "\">Edit order details</A>");
			
			pwOut.println("&nbsp;&nbsp;<A HREF=\"" 
						+ SMUtilities.getURLLinkBase(context) 
						+ "smcontrolpanel.SMEditOrderTotalsEdit?"
						+ SMOrderHeader.Paramstrimmedordernumber + "=" + sOrdNumber 
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "&CallingClass=" + this.getClass().getName()
						+ "\">Edit order discount</A>");
			pwOut.println("</FONT>");
		}
		pwOut.println("<FONT SIZE=2>");
		
		if (bViewJobCost){
			//If there is no Job Cost info available, don't show this link:
			SQL = "SELECT"
				+ " " + SMTableworkorders.lid
				+ " FROM " + SMTableworkorders.TableName
				+ " WHERE ("
					+ "(" + SMTableworkorders.strimmedordernumber + " = '" + sOrdNumber + "')"
				+ ")"
			;
			boolean bJobCostExists = false;
			try{
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, con);
				if (rs.next()){
					bJobCostExists = true;
				}
				rs.close();
			}catch (SQLException e){
				System.out.println("In " + this.toString() 
					+ ".printOrderHeader - error getting job cost record: " + e.getMessage());
			}
			
			if (bJobCostExists){
				pwOut.println("&nbsp;&nbsp;" 
						+ "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel."
						+ "SMDisplayJobCostInformation?OrderNumber=" + sOrdNumber 
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + "Job Cost" + "</A>"
					);
			}else{
				pwOut.println("&nbsp;"+ "Job Cost: N/A");
			}
		}
		pwOut.println("<BR><B>Date:</B>&nbsp;" + sOrderDate);
		
		//Get the name of the 'created by':
	
		pwOut.println("&nbsp;&nbsp;&nbsp;&nbsp;<B>Created:</B>&nbsp;" + sCreationTime
			+ "&nbsp;by&nbsp;" + sOrderCreatedByFullName
		);
		
		pwOut.println("&nbsp;&nbsp;<B>Last posted :</B>&nbsp;" + sLastPostingTime
				+ "&nbsp;&nbsp;by:&nbsp;" + sLastEditedBy
			);
		
		String sTax = sTaxGroup + " - " + sTaxType + " - " + sTaxDescription;
		pwOut.println("&nbsp;&nbsp;&nbsp;&nbsp;<B>Tax:</B>&nbsp;"
			+ "</FONT><FONT COLOR=DARKORANGE  SIZE=2><B>"
			+ sTax
			+ "</B></FONT>"
		);
		
//		//Get the name of the 'tax status':
//		String sTaxStatusDesc =
//			"<FONT COLOR=DARKORANGE><B>"
//			+ sTaxType
//			+ "</B></FONT>"
//			;
//		try{
//			SQL = "SELECT " + SMTabletax.staxtype
//				+ " FROM " + SMTabletax.TableName
//				+ " WHERE ("
//					+ "(" + SMTabletax.staxjurisdiction + " = '" + sTaxGroup + "')"
//					+ " AND (" + SMTabletax.itaxtype + " = " + sTaxType + ")"
//				+ ")"
//				;
//			ResultSet rs = SMUtilities.openResultSet(SQL, con);
//			if (rs.next()){
//				sTaxStatusDesc = sTaxStatusDesc + " - " 
//					+ rs.getString(SMTabletax.staxtype);
//			}
//			rs.close();
//		}catch (SQLException e){
//			System.out.println("Error [1423663435] in " + this.toString() 
//				+ ".printOrderHeader - error getting name of tax status: " + e.getMessage());
//		}
//		pwOut.println("&nbsp;&nbsp&nbsp;&nbsp;<B>Tax type:</B>&nbsp;" + sTaxStatusDesc);
//		pwOut.println("</FONT>");
		
		pwOut.println("<BR>");
		
		pwOut.println("<B>Estimated time:</B>&nbsp;" 
				+ Double.toString(bdEstimatedTime));
		
		//Get the name of the 'salesperson':
		String sFullSalesName = sSalesCode;
		try{
			SQL = "SELECT " + SMTablesalesperson.sSalespersonFirstName + ", " + SMTablesalesperson.sSalespersonLastName
				+ " FROM " + SMTablesalesperson.TableName
				+ " WHERE ("
					+ SMTablesalesperson.sSalespersonCode + " = '" + sSalesCode + "'"
				+ ")"
				;
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, con);
			if (rs.next()){
				sFullSalesName = sFullSalesName + " - " 
					+ rs.getString(SMTablesalesperson.sSalespersonFirstName)
					+ " " + rs.getString(SMTablesalesperson.sSalespersonLastName);
			}
			rs.close();
		}catch (SQLException e){
			System.out.println("Error [1423663436] in " + this.toString() 
				+ ".printInvoiceHeader - error getting name of salesperson: " + e.getMessage());
		}
		pwOut.println("&nbsp;&nbsp;&nbsp;<B>Salesperson:</B>&nbsp;" + sFullSalesName);
		
		//Get the Doing Business As Name:
		String sDBADescription = sDBAid;
		try{
			SQL = "SELECT " + SMTabledoingbusinessasaddresses.sDescription 
				+ " FROM " + SMTabledoingbusinessasaddresses.TableName
				+ " WHERE ("
					+ SMTabledoingbusinessasaddresses.lid + " = " + sDBAid + ""
				+ ")"
				;
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, con);
			if (rs.next()){
				sDBADescription = rs.getString(SMTabledoingbusinessasaddresses.sDescription);
			}
			rs.close();
		}catch (SQLException e){
			System.out.println("Error [1423663546] in " + this.toString() 
				+ ".printInvoiceHeader - error getting DBA description: " + e.getMessage());
		}
		pwOut.println("&nbsp;&nbsp;&nbsp;<B>Doing business as:</B>&nbsp;" + sDBADescription);
		
		//Sales group:
		pwOut.println("&nbsp;&nbsp;&nbsp;<B>Sales group:</B>&nbsp;" + sSalesGroupDescription);
		
		String sSalespersonSetInAR = "";
		try{
			SQL = "SELECT " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonFirstName 
				+ ", " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonLastName
				+ ", " + SMTabledefaultsalesgroupsalesperson.TableName + "." + SMTabledefaultsalesgroupsalesperson.ssalespersoncode
				+ " FROM " + SMTabledefaultsalesgroupsalesperson.TableName + " LEFT JOIN "  + SMTablesalesperson.TableName 
				+ " ON " + SMTabledefaultsalesgroupsalesperson.TableName + "." + SMTabledefaultsalesgroupsalesperson.ssalespersoncode + " = "
				+ SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode
				+ " WHERE ("
					+ "(" + SMTabledefaultsalesgroupsalesperson.TableName + "." + SMTabledefaultsalesgroupsalesperson.scustomercode 
					+ " = '" + sCustCode + "')"
					+ " AND (" + SMTabledefaultsalesgroupsalesperson.TableName + "." + SMTabledefaultsalesgroupsalesperson.lsalesgroupid
					+ " = " + Long.toString(iSalesGroupID) + ")"
				+ ")"
				;
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, con);
			if (rs.next()){
				String sSalesCodeFromAR = rs.getString(SMTabledefaultsalesgroupsalesperson.TableName + "." 
						+ SMTabledefaultsalesgroupsalesperson.ssalespersoncode);
				
				if (sSalesCodeFromAR == null){
					sSalesCodeFromAR = "";
				}
				String sFirstName = rs.getString(SMTablesalesperson.sSalespersonFirstName);
				if (sFirstName == null){
					sFirstName = "";
				}
				String sLastName = rs.getString(SMTablesalesperson.sSalespersonLastName);
				if (sLastName == null){
					sLastName = "";
				}
				
				sSalespersonSetInAR = sSalesCodeFromAR.trim()
					+ " - " 
					+ sFirstName.trim()
					+ " " + sLastName.trim();
			}
			rs.close();
		}catch (SQLException e){
			System.out.println("Error [1423663437] in " + this.toString() 
				+ ".printInvoiceHeader - error getting name of salesperson: " + e.getMessage());
		}
		
		//Salesperson set as default in A/R:
		pwOut.println("&nbsp;&nbsp;&nbsp;<B>Default salesperson in A/R:</B>&nbsp;" + sSalespersonSetInAR);
		
		//iCustomerDiscountLevel
		pwOut.println("&nbsp;&nbsp;&nbsp;<B>Price level:</B>&nbsp;" + Long.toString(lPriceLevel));
		
		//sdefaultPriceListcode
		pwOut.println("&nbsp;&nbsp;&nbsp;<B>Price list:</B>&nbsp;" + sPriceList);
		
		//sterms
		pwOut.println("&nbsp;&nbsp;&nbsp;<B>Terms:</B>&nbsp;" + sTerms);
		
		pwOut.println("</FONT>");
		
		pwOut.println("<BR>");
		
		pwOut.println("<FONT SIZE=2><B>Bill to:&nbsp;" 
				+ "</FONT><FONT SIZE=2 COLOR=DARKORANGE>" + sCustCode.trim() 
				+ "&nbsp;-&nbsp;" 
				+ sBillToName + "</B></FONT>");

		pwOut.println("<FONT SIZE=2>&nbsp;<B>Ship to:</B>&nbsp;" + sShipToName);
		
		//sPONUMBER
		if ((sPONumber.trim().compareToIgnoreCase("") == 0) && bRequiresPO){
			pwOut.println("&nbsp;<B>PO:</B>&nbsp;" + "<FONT COLOR=RED><B>WARNING: PO IS REQUIRED!</B></FONT>");
		}else{
			pwOut.println("&nbsp;<B>PO:</B>&nbsp;" + sPONumber);
		}
		
		//Order source
		pwOut.println("&nbsp;<B>Source:</B>&nbsp;" + sOrderSource);
		
//		//Order completion date:
//		pwOut.println("&nbsp;<B>Order Completed:</B>&nbsp;" + sOrderCompletionDate);
		
		//Follow up sales lead	
		String sCreatedFromSalesLead = "";
		try{
			SQL = "SELECT " + SMTablebids.TableName + "." + SMTablebids.lid 
				+ " FROM " + SMTablebids.TableName 
				+ " WHERE ("
					+ "(" + SMTablebids.TableName + "." +  SMTablebids.screatedfromordernumber
					+ " = '" + sOrdNumber + "')"
				+ ")"
				;
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, con);
			while (rs.next()){
				String sSalesLead = Integer.toString(rs.getInt(SMTablebids.TableName + "." + SMTablebids.lid ));

				if (sSalesLead == null || sSalesLead.trim().compareToIgnoreCase("") == 0){
					sCreatedFromSalesLead = "";
				}else{
					sCreatedFromSalesLead += "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMEditBidEntry?"
						+ SMBidEntry.ParamID + "=" + sSalesLead
						+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "\">" + sSalesLead 
						+ "</A>&nbsp;";
				}

			}
			rs.close();
		}catch (SQLException e){
			System.out.println("Error [1493663437] in " + this.toString() 
				+ ".printInvoiceHeader - error getting created by sales lead numbers: " + e.getMessage());
		}
		pwOut.println("&nbsp;<B>Follow up sales lead(s):</B>&nbsp;" + sCreatedFromSalesLead);
		
		pwOut.println("</FONT>");
		pwOut.println("<BR>");
		if (sTicketComment != null){
			if (sTicketComment.trim().compareToIgnoreCase("") != 0){
				pwOut.println("<FONT SIZE=2>" 
					+ "<B>WORK ORDER NOTES:</B>&nbsp;" + sTicketComment.trim().replace("\n", "<BR>") + "</FONT><BR>");
			}
		}
		
		if (sInvoiceComment != null){
			if (sInvoiceComment.trim().compareToIgnoreCase("") != 0){
				pwOut.println("<FONT SIZE=2>" 
					+ "<B>INVOICE COMMENT:</B>&nbsp;" + sInvoiceComment.trim().replace("\n", "<BR>") + "</FONT><BR>");
			}
		}
		//TJR 11/10/2014 - this was put in by accident recently, but we are commenting it back out now...
		//if (sInternalComment != null){
		//	if (sInternalComment.trim().compareToIgnoreCase("") != 0){
		//		pwOut.println("<FONT SIZE=2>" 
		//			+ "<B>INTERNAL COMMENT:</B>&nbsp;" + sInternalComment.trim().replace("\n", "<BR>") + "</FONT><BR>");
		//	}
		//}

		//Display any order manager comments here:
		SQL = " SELECT * FROM " + SMTableordermgrcomments.TableName
			+ " WHERE ("
				+ "(" + SMTableordermgrcomments.sordernumber + " = '" + sOrdNumber + "')"
				+ " AND (" + SMTableordermgrcomments.sinvoicenumber + " = '')"
			+ ")"
			+ " ORDER BY " + SMTableordermgrcomments.datlastedited
			;
		//System.out.println("Order manager comments SQL (" + USDateTimeformatter.format(new Date(System.currentTimeMillis())) + " by " + sCurrentUser + "):");
		//System.out.println(SQL);
		boolean bCommentsTableConfigured = false;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, con);
			while (rs.next()){
				String sComment = rs.getString(SMTableordermgrcomments.TableName + "." 
						+ SMTableordermgrcomments.mcomment);
				String sCommenterFullName = rs.getString(SMTableordermgrcomments.TableName + "." 
						+ SMTableordermgrcomments.suserfullname);
				String sCommenterID = rs.getString(SMTableordermgrcomments.TableName + "." 
						+ SMTableordermgrcomments.luserid);
				
				if (sComment != null){
					if (sComment.trim().compareToIgnoreCase("") != 0){
						//If it's the first line, set up a table:
						if (!bCommentsTableConfigured){
							pwOut.println("<TABLE BORDER=0>");
							bCommentsTableConfigured = true;
						}

						//If the comment is from THIS user, show it in GREEN, otherwise, show it
						//in red:
						String sAlertColor = OTHER_USER_COMMENT_COLOR;
						if (sCurrentUserID.compareToIgnoreCase(sCommenterID) == 0){
							sAlertColor = CURRENT_USER_COMMENT_COLOR;
						}
						
						//Print a bookmark for this comment:
						
						//First get the index for this commenter:
						// iCurrentCommenterIndex indicates where in the list of commenters this particular commenter is:
						int iCurrentCommenterIndex = 0;
						try {
							iCurrentCommenterIndex = sCommenters.indexOf(sCommenterFullName);
						} catch (Exception e) {
							clsServletUtilities.sysprint(
								this.toString(), 
								sUserName, 
								"Error [1423663438] sCommenters.indexOf(" + sCommenterFullName + ") - " + e.getMessage());
						}
						
						//Next, increment the counter for this commenter:
						//If this commenter is not yet in the list of commenters, add him:
						if (iCurrentCommenterIndex == -1){
							sCommenters.add(sCommenterFullName);
							//Indicate that he now has one comment:
							iCommentCounter.add(1);
						}else{
							iCommentCounter.set(iCurrentCommenterIndex, 
								iCommentCounter.get(iCurrentCommenterIndex) + 1);
						}
						//Now, print a bookmark for this comment:
						String sCommentBookMark = 
							"<a name=\"" 
							+ sCommenterFullName.replace(" ", "") 
							+ Integer.toString(iCommentCounter.get(iCurrentCommenterIndex))
							+ "\">"
							;
						String sNextCommentLink = "";
						
						//If the 
						if(iCommentCount.get(iCurrentCommenterIndex).intValue()
								== iCommentCounter.get(iCurrentCommenterIndex).intValue()){
							sNextCommentLink = "<a href=\"#" 
								+ sCommenterFullName.replace(" ", "") 
								+ "0"
								+ "\">" // Back to the top
								;
						}else{
							sNextCommentLink = "<a href=\"#" 
								+ sCommenterFullName.replace(" ", "") 
								+ Integer.toString(iCommentCounter.get(iCurrentCommenterIndex) + 1)
								+ "\">" // Next comment"
								;
						}
						
						//Print the comment:
						pwOut.println("<TR>");
						pwOut.println("<TD VALIGN=TOP ALIGN=RIGHT><FONT COLOR=" + sAlertColor + " SIZE=2>"
							+ "<B>"
							+ sNextCommentLink
							+ sCommenterFullName.trim()
							+ sCommentBookMark
							//+ "</TD>"
							);
						
						pwOut.println(
							" @ "
							+ clsDateAndTimeConversions.utilDateToString(
								rs.getDate(SMTableordermgrcomments.datlastedited), "MM/dd/yyyy")
								+ " " + clsDateAndTimeConversions.utilDateToString(
										rs.getTime(SMTableordermgrcomments.datlastedited), "h:mm a")
							+ ":</B></TD>"
							);
						
						//If it's a comment by the current user, print a text box:
						//System.out.println("In " + this.toString() + ".printInvoiceHeader - sCurrentUser = " + sCurrentUser);
						//System.out.println("And sCommenter = " + sCurrentUser);
						if (sCurrentUserID.compareToIgnoreCase(sCommenterID) == 0){
							//Multiline text box:
							pwOut.println(
								"<TD>"
								+ "<FONT SIZE=2>"
								+ "<TEXTAREA NAME=\"" + "COMMENT" + sOrdNumber 
								+ "ID"
								+ Long.toString(rs.getLong(SMTableordermgrcomments.id))
								+ "\" rows=2 cols=50>"
								+ sComment.trim() + "</TEXTAREA>"
								+ "</FONT>"
								+ "<TD>"
							);
							/*
							pwOut.println("<button type=\"button\""
											+ " value=\"Update comment\""
											+ " name=\"1" + sOrdNumber + "\""
											+ " onClick=\"updateComment(this);\">LT Update comment</button>\n"
											);
							
							//Store which command button the user has chosen:
							pwOut.println("&nbsp;&nbsp;<INPUT TYPE=HIDDEN NAME=\"JSUPDATECOMMENT" + sOrdNumber + "\" VALUE=\"\""
											+ " id=\"JSUPDATECOMMENT" + sOrdNumber + "\""
											+ "\">");
							 
							pwOut.println("<button type=\"button\""
											+ " value=\"Remove comment\""
											+ " name=\"2" + sOrdNumber + "\""
											+ " onClick=\"removeComment(this);\">LT Remove comment</button>\n"
											);
							
							//Store which command button the user has chosen:
							pwOut.println("&nbsp;&nbsp;<INPUT TYPE=HIDDEN NAME=\"JSREMOVECOMMENT" + sOrdNumber + "\" VALUE=\"\""
											+ " id=\"JSREMOVECOMMENT" + sOrdNumber + "\""
											+ "\">");
							*/
							
							pwOut.println("<INPUT TYPE=\"SUBMIT\" NAME=\"" 
									+ "UPDATETHISCOMMENT" 
									+ sOrdNumber 
									+ "ID" 
									+ Long.toString(rs.getLong(SMTableordermgrcomments.id)) 
									+ "\" VALUE=\"Update comment\">");
							
							pwOut.println("<INPUT TYPE=\"SUBMIT\" NAME=\"" 
									+ "REMOVETHISCOMMENT" 
									+ sOrdNumber 
									+ "ID" 
									+ Long.toString(rs.getLong(SMTableordermgrcomments.id)) 
									+ "\" VALUE=\"Remove comment\">");
							
							
						}else{
							//Otherwise, just print the comment:
							pwOut.println("<TD><FONT COLOR=" + sAlertColor + ">" 
									+ sComment.trim().replace("\n", "<BR>") + "</FONT>"
									+ sCommentBookMark
									+ sNextCommentLink
									+ "</TD>"
								);
							}
						
						
						pwOut.println("</TR>");
					}
				}
			}
			rs.close();
		}catch(SQLException e){
			System.out.println("Error in " + this.toString() + ".printInvoiceHeader - " + e.getMessage());
		}
		
		//If there was a table configured, close it:
		if (bCommentsTableConfigured){
			pwOut.println("</TABLE>");
		}
		//Build the line table header:
		pwOut.println("<TABLE BORDER=0 WIDTH=100%>");
		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>LINE</B></FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>ITEM</B></FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>DESCRIPTION</B></FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>CAT.</B></FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>LOC.</B></FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>MECHANIC</B></FONT></TD>");
		pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>QTY</B></FONT></TD>");
		pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>QTY<BR>ON HAND</B></FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>UNIT</B></FONT></TD>");
		pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>LAST<BR>COST</B></FONT></TD>");
		pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>PRICE</B></FONT></TD>");
		pwOut.println("<TD ALIGN=CENTER><FONT SIZE=2><B>TAX?</B></FONT></TD>");
		
		pwOut.println("</TR>");
	}
	private void printOrderFooter(
			BigDecimal bdPriceBeforeTax,
			BigDecimal bdTotalCost,
			BigDecimal bdTotalPrice,
			BigDecimal bdDiscountPercentage,
			String sDiscountDescription,
			BigDecimal bdDiscountAmount,
			BigDecimal bdTaxAmount,
			BigDecimal bdNetTotalWithoutTax,
			BigDecimal bdDepositAmt,
			String sOrderNumber,
			PrintWriter pwOut,
			Connection conn
	){
		
		//Total cost and price:
		pwOut.println("<TD ALIGN=RIGHT COLSPAN=9><FONT SIZE=2>" 
				+ "<B>Subtotals:</B>" + "</FONT></TD>");
		
		pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" 
			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalCost) + "</FONT></TD>");
		
		//If the discount amt AND the tax are zero, underline the price total:
		if (
				(bdDiscountPercentage.compareTo(BigDecimal.ZERO) == 0)
				&& (bdTaxAmount.compareTo(BigDecimal.ZERO) == 0)
			){
			pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2><U>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalPrice) + "</U></FONT></TD>");
		}else{
			pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalPrice) + "</FONT></TD>");
		}
		pwOut.println("</TR>");
		
		//Discount line:
		//If the discount is NOT zero, print a 'discount line':
		if (
				(bdDiscountPercentage.compareTo(BigDecimal.ZERO) != 0)
				|| (bdDiscountAmount.compareTo(BigDecimal.ZERO) != 0)
			){
			pwOut.println("<TR>");
			pwOut.println("<TD ALIGN=RIGHT COLSPAN=9><FONT SIZE=2>" 
				+ "<B>" + sDiscountDescription + ":</B>" + "</FONT></TD>");
			pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>"
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdDiscountPercentage)
				+ "&nbsp;%</FONT></TD>");
			
			//If there is no tax, print an underline under the discount amount:
			if (bdTaxAmount.compareTo(BigDecimal.ZERO) == 0){
				pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2><U>"
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdDiscountAmount.negate())
						+ "</U></FONT></TD>");
			}else{
				pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>"
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdDiscountAmount.negate())
						+ "</FONT></TD>");
				}
			pwOut.println("</TR>");
		}
		
		//Tax line:
		if (bdTaxAmount.compareTo(BigDecimal.ZERO) != 0){
			pwOut.println("<TR>");
			pwOut.println("<TD ALIGN=RIGHT COLSPAN=9><FONT SIZE=2>" 
				+ "<B>Net amount without tax:</B>" + "</FONT></TD>");
			pwOut.println("<TD ALIGN=RIGHT COLSPAN=2><FONT SIZE=2>"
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdNetTotalWithoutTax)
					+ "</FONT></TD>");
			pwOut.println("</TR>");
			
			pwOut.println("<TR>");
			pwOut.println("<TD ALIGN=RIGHT COLSPAN=9><FONT SIZE=2>" 
					+ "<B>Tax amount:</B>" + "</FONT></TD>");
				pwOut.println("<TD ALIGN=RIGHT COLSPAN=2><FONT SIZE=2><U>+&nbsp;"
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTaxAmount)
						+ "</U></FONT></TD>");
				pwOut.println("</TR>");
				
			pwOut.println("</TR>");
		}
		
		//Total order line:
		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT COLSPAN=9><FONT SIZE=2>" 
				+ "<B>Order total:</B>" + "</FONT></TD>");
		pwOut.println("<TD ALIGN=RIGHT COLSPAN=2><FONT SIZE=2>"
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdNetTotalWithoutTax.add(bdTaxAmount))
				+ "</FONT></TD>");
		pwOut.println("</TR>");
			
		//add an extra line:
		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT COLSPAN=11><FONT SIZE=2>" 
				+ "&nbsp;" + "</FONT></TD>");
		pwOut.println("</TR>");
		
		//Deposit amount line:
		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT COLSPAN=9><FONT SIZE=2>" 
				+ "<B>Deposit amt:</B>" + "</FONT></TD>");
		pwOut.println("<TD ALIGN=RIGHT COLSPAN=2><FONT SIZE=2>"
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdDepositAmt)
				+ "</FONT></TD>");
		pwOut.println("</TR>");
		
		SMOrderHeader order = new SMOrderHeader();
		boolean bCalculationFailed = false;
		try {
			order.calculateBillingTotals(conn, sOrderNumber.trim());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			pwOut.println("<TR>");
			pwOut.println("<TD ALIGN=RIGHT COLSPAN=11><FONT SIZE=2; COLOR=RED>" 
					+ "<B>ERROR CALCULATING ORDER TOTALS - " + e.getMessage() + "</B>" + "</FONT></TD>");
			pwOut.println("</TR>");
			bCalculationFailed = true;
		}

		if (bCalculationFailed == false){
			/* - Commented these lines out, 10/23/2012 - TJR:
			//Original contract amount:
			pwOut.println("<TR>");
			pwOut.println("<TD ALIGN=RIGHT COLSPAN=9><FONT SIZE=2>" 
					+ "<B>Original contract amount:</B>" + "</FONT></TD>");
			pwOut.println("<TD ALIGN=RIGHT COLSPAN=2><FONT SIZE=2>"
					+ SMUtilities.BigDecimalTo2DecimalSTDFormat(order.getCalculatedOrderTotals_OriginalContractAmount())
					+ "</FONT></TD>");
			pwOut.println("</TR>");
			
			//Change order total:
			pwOut.println("<TR>");
			pwOut.println("<TD ALIGN=RIGHT COLSPAN=9><FONT SIZE=2>" 
					+ "<B>Change order total:</B>" + "</FONT></TD>");
			pwOut.println("<TD ALIGN=RIGHT COLSPAN=2><FONT SIZE=2>"
					+ SMUtilities.BigDecimalTo2DecimalSTDFormat(order.getCalculatedOrderTotals_ChangeOrderTotal())
					+ "</FONT></TD>");
			pwOut.println("</TR>");
			
			//TOTAL CONTRACT AMOUNT (ORIGINAL CONTRACT AMOUNT PLUS CHANGE ORDER TOTAL)
			pwOut.println("<TR>");
			pwOut.println("<TD ALIGN=RIGHT COLSPAN=9><FONT SIZE=2>" 
					+ "<B>Total contract amt (original contract amt plus change orders):</B>" + "</FONT></TD>");
			pwOut.println("<TD ALIGN=RIGHT COLSPAN=2><FONT SIZE=2>"
					+ SMUtilities.BigDecimalTo2DecimalSTDFormat(
					order.getCalculatedOrderTotals_OriginalContractAmount().add(order.getCalculatedOrderTotals_ChangeOrderTotal()))
					+ "</FONT></TD>");
			pwOut.println("</TR>");
			
			//Amt invoiced to date:
			pwOut.println("<TR>");
			pwOut.println("<TD ALIGN=RIGHT COLSPAN=9><FONT SIZE=2>" 
					+ "<B>Amount invoiced to date:</B>" + "</FONT></TD>");
			pwOut.println("<TD ALIGN=RIGHT COLSPAN=2><FONT SIZE=2>"
					+ SMUtilities.BigDecimalTo2DecimalSTDFormat(
					order.getCalculatedOrderTotals_TotalBilled())
					+ "</FONT></TD>");
			pwOut.println("</TR>");
			
			//Amt on order details currently in a 'shipped' state:
			pwOut.println("<TR>");
			pwOut.println("<TD ALIGN=RIGHT COLSPAN=9><FONT SIZE=2>" 
					+ "<B>Amount on order details currently being shipped:</B>" + "</FONT></TD>");
			pwOut.println("<TD ALIGN=RIGHT COLSPAN=2><FONT SIZE=2>"
					+ SMUtilities.BigDecimalTo2DecimalSTDFormat(
					order.getCalculatedOrderTotals_TotalAmtCurrentlyShipped())
					+ "</FONT></TD>");
			pwOut.println("</TR>");

			//Amt on order details currently in an 'ordered' state:
			pwOut.println("<TR>");
			pwOut.println("<TD ALIGN=RIGHT COLSPAN=9><FONT SIZE=2>" 
					+ "<B>Amount on order details remaining unshipped:</B>" + "</FONT></TD>");
			pwOut.println("<TD ALIGN=RIGHT COLSPAN=2><FONT SIZE=2>"
					+ SMUtilities.BigDecimalTo2DecimalSTDFormat(
					order.getCalculatedOrderTotals_TotalAmtStillOnOrder())
					+ "</FONT></TD>");
			pwOut.println("</TR>");
			
			//TOTAL ORDER AMOUNT (AMOUNT INVOICED TO DATE PLUS AMOUNT ON ORDER DETAILS REMAINING TO BE INVOICED):
			pwOut.println("<TR>");
			pwOut.println("<TD ALIGN=RIGHT COLSPAN=9><FONT SIZE=2>" 
					+ "<B>Total order amt (amt invoiced to date PLUS amt shipped PLUS amt left on order):</B>" + "</FONT></TD>");
			pwOut.println("<TD ALIGN=RIGHT COLSPAN=2><FONT SIZE=2>"
					+ SMUtilities.BigDecimalTo2DecimalSTDFormat(
						order.getCalculatedOrderTotals_TotalBilled().add(
						order.getCalculatedOrderTotals_TotalAmtCurrentlyShipped()).add(
						order.getCalculatedOrderTotals_TotalAmtStillOnOrder()))
					+ "</FONT></TD>");
			pwOut.println("</TR>");
			*/
			if (order.getCalculatedOrderTotals_OriginalContractAmount().compareTo(BigDecimal.ZERO) != 0){
				if (order.getCalculatedOrderTotals_RemainingAmtDifference().compareTo(BigDecimal.ZERO) > 0){
					pwOut.println("<TR>");
					pwOut.println("<TD ALIGN=RIGHT COLSPAN=11>"
						+ "<FONT COLOR=RED size=2><B><I>NOTE:</I> THE TOTAL ORDER AMOUNT IN THE BILLING SUMMARY IS "
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(order.getCalculatedOrderTotals_RemainingAmtDifference().abs())
						+ " <I>LESS</I> THAN THE TOTAL CONTRACT AMOUNT IN THE CHANGE ORDER LOG.</B></FONT></TD>"
					);
					pwOut.println("</TR>");
				}
				if (order.getCalculatedOrderTotals_RemainingAmtDifference().compareTo(BigDecimal.ZERO) < 0){
					pwOut.println("<TD ALIGN=RIGHT COLSPAN=11>"
							+ "<FONT COLOR=RED size=2><B><I>NOTE:</I> THE TOTAL ORDER AMOUNT IN THE BILLING SUMMARY IS "
							+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(order.getCalculatedOrderTotals_RemainingAmtDifference().abs())
							+ " <I>MORE</I> THAN THE TOTAL CONTRACT AMOUNT IN THE CHANGE ORDER LOG.</B></FONT></TD>"
						);
					pwOut.println("</TR>");
				}
			}
		}
		
		//End the line table:
		pwOut.println("</TABLE>");
		
		//Add a text box for comments:
		pwOut.println("<B>Comment: </B>" 
				
				//Regular text box:
				//+ SMUtilities.TDTextBox("COMMENT" + sInvNumber + "ID0", "", 100, 254, ""));
				
				//Multiline text box:
				+ "<TEXTAREA NAME=\"" + "COMMENT" + sOrderNumber + "ID0" + "\" rows=2 cols=80>"
				+ "" + "</TEXTAREA>"
				);
		
		// TODO: LTO 20130920 Add java script to prevent double clicking	
		pwOut.println("<button type=\"button\""
						+ " value=\"Save comment\""
						+ " name=\"" + sOrderNumber + "\""
						+ " onClick=\"saveComment(this);\">Save comment</button>\n"
						);
		//Store which command button the user has chosen:
		pwOut.println("&nbsp;&nbsp;<INPUT TYPE=HIDDEN NAME=\"JSSAVECOMMENT" + sOrderNumber + "\" VALUE=\"na\""
				+ " id=\"JSSAVECOMMENT" + sOrderNumber + "\""
				+ "\">");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"JSLASTEDITEDORDERNUMBER\" VALUE=\"0\""
				+ " id=\"JSLASTEDITEDORDERNUMBER\""
				+ "\">");
/*
		pwOut.println("&nbsp;&nbsp;<INPUT TYPE=\"SUBMIT\" NAME=\"" 
			+ "SAVEALLCOMMENTS" + sOrderNumber + "\" VALUE=\"Save comments\">");
	*/	
		pwOut.println("<HR>");
	}
	private void printServiceTypeHeader(String sServiceType, PrintWriter pwOut){
		
		pwOut.println("<BR><B><I><U>Service Type: " 
			+ sServiceType + "</U></I></B><BR>");
	}
	private void printServiceTypeFooter(
			String sServiceType,
			long lNumberOfOrdersForServiceType,
			BigDecimal bdTotalOrderPriceWithTaxForServiceType,
			PrintWriter pwOut
			){

		pwOut.println("<TABLE BORDER=1 WIDTH=100%>");
		
		//Orders
		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT>Number of orders in " 
			+ sServiceType + ":</TD>");
		pwOut.println("<TD ALIGN=RIGHT>" + Long.toString(lNumberOfOrdersForServiceType) + "</TD>");
		pwOut.println("<TD ALIGN=RIGHT>Avg. amount:</TD>");
		BigDecimal bdNumberOfOrders = BigDecimal.valueOf(lNumberOfOrdersForServiceType);
		if (lNumberOfOrdersForServiceType > 0){
			pwOut.println("<TD ALIGN=RIGHT>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
					(bdTotalOrderPriceWithTaxForServiceType).divide(
						bdNumberOfOrders, BigDecimal.ROUND_HALF_UP))
					+ "</TD>");
		}else{
			pwOut.println("<TD ALIGN=RIGHT>0.00</TD>");
		}
		pwOut.println("</TR>");

		pwOut.println("</TABLE>");
		
	}
	private void printReportFooter(
			long lTotalNumOfOrders,
			BigDecimal bdTotOrderCost,
			BigDecimal bdTotOrderPriceWithTax,
			PrintWriter pwOut
			){

		pwOut.println("<BR><TABLE BORDER=2 WIDTH=100%>");
		
		//Orders
		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT><B>Total number of orders:</B></TD>");
		pwOut.println("<TD ALIGN=RIGHT><B>" + Long.toString(lTotalNumOfOrders) + "</B></TD>");
		pwOut.println("<TD ALIGN=RIGHT><B>Avg. amount:</B></TD>");
		BigDecimal bdNumberOfOrders = BigDecimal.valueOf(lTotalNumOfOrders);
		if (lTotalNumOfOrders > 0){
			pwOut.println("<TD ALIGN=RIGHT><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
					bdTotOrderPriceWithTax.divide(
						bdNumberOfOrders, BigDecimal.ROUND_HALF_UP))
					+ "</B></TD>");
		}else{
			pwOut.println("<TD ALIGN=RIGHT><B>0.00</B></TD>");
		}
		pwOut.println("<TD ALIGN=RIGHT><B>Grand Total:</B></TD>");
		pwOut.println("<TD ALIGN=RIGHT><B>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotOrderPriceWithTax) + "</B></TD>");
		
		pwOut.println("</TR>");

		pwOut.println("</TABLE>");
		
	}
	private void printHiddenVariables(
			PrintWriter pwOut,
			String sCallingClass,
			String sStartDate,
			String sEndDate,
			ArrayList <String> sServiceTypes,
			ArrayList <String> sLocations,
			String sDBID
			){
		
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		
		pwOut.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='" + sCallingClass + "'>");

		pwOut.println("<INPUT TYPE=HIDDEN NAME='StartingDate' VALUE='" + sStartDate + "'>");
		
		pwOut.println("<INPUT TYPE=HIDDEN NAME='EndingDate' VALUE='" + sEndDate + "'>");
		
		//This will tell SMPrintInvoiceAuditGenerate that this request is a 're-processing'
		//of the report, so it won't record in the log that the user is running the report twice
		pwOut.println("<INPUT TYPE=HIDDEN NAME='Reprocess' VALUE='" + "TRUE" + "'>");
		
	    //SERVICETYPEs
		for (int i = 0; i < sServiceTypes.size(); i++){
			pwOut.println("<INPUT TYPE=HIDDEN NAME='SERVICETYPE" + sServiceTypes.get(i) + "' VALUE='ON'>");
		}
		
	    //LOCATIONs
		for (int i = 0; i < sLocations.size(); i++){
			pwOut.println("<INPUT TYPE=HIDDEN NAME='LOCATION" + sLocations.get(i) + "' VALUE='ON'>");
		}

	}
	private String getPreviousCommenters(
			String sStartDate,
			String sEndDate,
			ArrayList <String> sOrderTypesList,
			ArrayList <String> sLocationsList,
			Connection con
	){
		
		String sResult = "";
		
		//Create string of order types:
		String sListOfOrderTypes = "";
		for (int i = 0; i < sOrderTypesList.size(); i++){
			sListOfOrderTypes += "," + sOrderTypesList.get(i);
		}
		
		//Create string of order types:
		String sLocations = "";
		for (int i = 0; i < sLocationsList.size(); i++){
			sLocations += "," + sLocationsList.get(i);
		}
		
		String SQL = "SELECT Count(" 
			+ SMTableordermgrcomments.TableName + "." + SMTableordermgrcomments.suserfullname 
			+ ") AS CommentCount, "
			+ SMTableordermgrcomments.TableName + "." + SMTableordermgrcomments.suserfullname
			+ " FROM " + SMTableordermgrcomments.TableName + " INNER JOIN"
			+ " " + SMTableorderheaders.TableName + " ON"
			//TJROC
			+ " " + SMTableordermgrcomments.TableName + "." + SMTableordermgrcomments.sordernumber
			+ " = " + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber
			+ " WHERE ("
				+ "(" + SMTableorderheaders.TableName+ "." + SMTableorderheaders.datLastPostingDate 
				+ ">= '" + sStartDate + "')"
				+ " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datLastPostingDate
				+ " <= '" + sEndDate + " 23:59:59')"
				
				+ " AND (INSTR('" + sListOfOrderTypes + "', " + SMTableorderheaders.TableName 
				+ "." + SMTableorderheaders.sServiceTypeCode + ") > 0)"
				
				+ " AND (INSTR('" + sLocations + "', " + SMTableorderheaders.TableName 
				+ "." + SMTableorderheaders.sLocation + ") > 0)"
				
				+ " AND (" + SMTableordermgrcomments.TableName + "." + SMTableordermgrcomments.sinvoicenumber
					+ " = '')"
					
			+ ")"
			+ " GROUP BY " + SMTableordermgrcomments.TableName + "." + SMTableordermgrcomments.suserfullname
			;

		//System.out.println("In " + this.toString() + ".getPreviousCommenters - SQL = " + SQL);
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, con);
			while (rs.next()){
				String sCommenterFullName = rs.getString(SMTableordermgrcomments.suserfullname);
				if (sResult.compareToIgnoreCase("") == 0){
					sResult = "<B>PREVIOUS COMMENTS: </B>The following people have left at least"
						+ " one comment on this range of orders already (although they MAY NOT appear in the current"
						+ " selection because their comments may have been on previously shipped lines): "
						+ "<a href=\"#" + sCommenterFullName.replace(" ", "") + "1" + "\">"
						+ sCommenterFullName
						+ "</a>"
						//Add a bookmark here with a zero:
						+ "<a name=\"" + sCommenterFullName.replace(" ", "") + "0" + "\">"
						;
				}else{
					sResult = sResult + ", "
					+ "<a href=\"#" + sCommenterFullName.replace(" ", "") + "1" + "\">" 
					+ sCommenterFullName
					+ "</a>"
					//Add a bookmark here with a zero:
					+ "<a name=\"" + sCommenterFullName.replace(" ", "") + "0" + "\">"
					;
				}
				//Add to the commenters arrays:
				sCommenters.add(sCommenterFullName);
				iCommentCounter.add((Integer) 0);
				//Store the comment count for each commenter:
				iCommentCount.add((Integer) rs.getInt("CommentCount"));
				//System.out.println(sCommenterFullName + " has " + rs.getInt("CommentCount") + " comments");
			}
			rs.close();
		}catch (SQLException e){
			System.out.println("Error getting previous commenters - " + e.getMessage());
		}
		
		if (sResult.compareToIgnoreCase("") == 0){
			sResult = "<B>NO</B> comments have been made on this range of orders yet";
		}
		return sResult + ".";
	}
	
	private String sCommandScripts(){
		String s = "";
		
		s += "<NOSCRIPT>\n"
			+ "    <font color=red>\n"
			+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
			+ "    </font>\n"
			+ "</NOSCRIPT>\n"
		;
		s += "<script type=\"text/javascript\">\n";
		
		s += "function saveComment(button) {\n";
			//First change the label on the button and then disable it so user can't click it twice:
			s += "button.value = \"Please wait....\";\n";
			s += "button.disabled =true;\n";
			//Then submit the form
			s += "document.getElementById(\"JSSAVECOMMENT\" + button.name).value = \"1\";\n";
			s += "document.getElementById(\"JSLASTEDITEDORDERNUMBER\").value = button.name;\n";
			//s += "alert('[ADDING] Order Number =' + button.name);\n";
			s += "document.forms[\"MAINFORM\"].submit();\n";
			s += "}\n"; 
			/*
		s += "function updateComment(button) {\n";
			//First change the label on the button and then disable it so user can't click it twice:
			s += "button.value ='Please wait....';\n";
			s += "button.disabled =true;\n";
			//Then submit the form
			s += "document.getElementById(\"JSUPDATECOMMENT\" + button.name).value = \"1\";\n";
			s += "document.getElementById(\"JSLASTEDITEDORDERNUMBER\").value = button.name;\n";
			s += "alert('[UPDATING] Order Number =' + button.name);\n";
			s += "document.forms[\"MAINFORM\"].submit();\n";
			s += "}\n"; 
			
		s += "function removeComment(button) {\n";
			//First change the label on the button and then disable it so user can't click it twice:
			s += "button.value ='Please wait....';\n";
			s += "button.disabled =true;\n";
			//Then submit the form
			s += "document.getElementById(\"JSREMOVECOMMENT\" + button.name).value = \"1\";\n";
			s += "document.getElementById(\"JSLASTEDITEDORDERNUMBER\").value = button.name;\n";
			s += "alert('[REMOVING] Order Number =' + button.name);\n";
			s += "document.forms[\"MAINFORM\"].submit();\n";
			s += "}\n"; 
			 */
		s += "</script>\n";
		return s;
	}
	
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}

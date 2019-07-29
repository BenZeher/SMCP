package smcontrolpanel;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import SMClasses.SMOrderHeader;
import SMDataDefinition.SMTablebids;
import SMDataDefinition.SMTableinvoicedetails;
import SMDataDefinition.SMTableinvoiceheaders;
import SMDataDefinition.SMTableinvoicemgrcomments;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablesalesgroups;
import SMDataDefinition.SMTablesalesperson;
import SMDataDefinition.SMTabletax;
import SMDataDefinition.SMTableworkorders;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;

public class SMInvoiceAuditReport extends java.lang.Object{

	private static final String NONSTOCK_ITEM_FLAG = "<DIV style = \" color:red; font-weight:bold; \" >N/A</DIV>";
	private String m_sErrorMessage;
	
	private ArrayList <String> sCommenters = new ArrayList <String> (0);
	private ArrayList <Integer> iCommentCount = new ArrayList <Integer> (0);
	private ArrayList <Integer> iCommentCounter = new ArrayList <Integer> (0);
	
	private boolean bDebugMode = false;
	
	public SMInvoiceAuditReport(
			){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			String sStartingDate,
			String sEndingDate,
			String sStartingCreationDate,
			String sEndingCreationDate,
			ArrayList <String> sOrderTypes,
			ArrayList <String> sSalesGroups,
			String sDBID,
			String sUserID,
			boolean bSuppressDetail,
			String sLastInvoiceEdited,
			boolean bReprocessing,
			boolean bAllowItemViewing,
			String sURLLinkBase,
			PrintWriter out,
			ServletContext context,
			String sLicenseModuleLevel,
			String sOrderNumber,
			String sInvoiceNumber
			){

		String sCurrentServiceType = "";
		String sCurrentServiceTypeDescription = "";
		String sLastServiceType = "";
		String sLastServiceTypeDescription = "";
		String sCurrentInvoiceNumber = "";
		String sLastInvoiceNumber = "";
		String sCurrentLineID = "";
		String sLastLineID = "";
		String sLastInvoicingState = "";
		String sCurrentInvoicingState = "";
		
		//Declare line variables:
		String sLastItemNumber = "";
		String sLastItemDesc = "";
		long lLastSuppressItemOnInvoice = 0;
		String sLastCategory = "";
		BigDecimal bdLastQty = new BigDecimal(0);
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
		long lLastICTransactionID = -1;
		boolean bLastrecordIsNonStockItem = false;
		
		//Declare invoice variables:
		BigDecimal bdPriceTotalForInvoice = new BigDecimal(0);
		BigDecimal bdCostTotalForInvoice = new BigDecimal(0);
		BigDecimal bdDiscountPercent = new BigDecimal(0);
		BigDecimal bdInvoiceDiscountAmount = new BigDecimal(0);
		String sDiscountDesc = "";
		BigDecimal bdInvoiceTaxAmount = new BigDecimal(0);
		BigDecimal bdInvoiceTotalWithoutTax = new BigDecimal(0);
		
		//Declare service type total variables:
		long lTotalNumberOfInvoicesForServiceType = 0;
		BigDecimal bdTotalInvoicePriceForServiceType = new BigDecimal(0);
		BigDecimal bdTotalInvoiceTaxForServiceType = new BigDecimal(0); //Comes from Get_Invoice_Header_Field(dINVHDRTaxAmount)
		long lTotalNumberOfCreditsForServiceType = 0;
		BigDecimal bdTotalCreditPriceForServiceType = new BigDecimal(0);
		BigDecimal bdTotalCreditTaxForServiceType = new BigDecimal(0);
				
		//Declare report total variables:
		long lTotalNumberOfInvoices = 0;
		BigDecimal bdTotalInvoiceCost = new BigDecimal(0); //Get_Invoice_Detail_Field(dINVDETExtendedCost)
		BigDecimal bdTotalInvoicePrice = new BigDecimal(0); //Get_Invoice_Detail_Field(dINVDETExtendedPrice)
		BigDecimal bdTotalInvoiceTax = new BigDecimal(0); //Comes from Get_Invoice_Header_Field(dINVHDRTaxAmount)
		long lTotalNumberOfCredits = 0;
		BigDecimal bdTotalCreditCost = new BigDecimal(0);
		BigDecimal bdTotalCreditPrice = new BigDecimal(0);
		BigDecimal bdTotalCreditTax = new BigDecimal(0);

		//Create string of order types:
		String sOrderTypesString = "";
		for (int i = 0; i < sOrderTypes.size(); i++){
			sOrderTypesString += "," + sOrderTypes.get(i);
		}
		
    	//SQL Statement:
        String SQL = "SELECT "
        	+ SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iTransactionType
        	+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sServiceTypeCodeDescription
        	+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sServiceTypeCode
        	+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber
        	+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceCreationDate
        	+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.dDiscountPercentage
        	+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sDiscountDesc
        	+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.dDiscountAmount
        	+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.bdsalestaxamount
        	+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate
        	+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iDayEndNumber
        	+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sCreatedByFullName
        	+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.lCreatedByID
        	+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sOrderNumber
        	+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sCustomerCode
        	+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sBillToName
        	+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.staxjurisdiction
        	+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iSalesGroup
        	+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iinvoicingstate
        	
    	    + ", IF(" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sSalesperson + " = '', "
    		+ "'" + SMOrderHeader.UNLISTEDSALESPERSON_MARKER + "', " 
    		+ SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sSalesperson
    		+ ") AS " + SMTableinvoiceheaders.sSalesperson
        	
        	+ ", " + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupDesc
        	
        	+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.mInvoiceComments
        	+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sShipToName
        	+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sOrderSourceDesc
        	+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iCustomerDiscountLevel
        	+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sDefaultPriceListCode
        	+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sPONumber
        	+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sTerms
        	+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datDueDate
        	+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.itaxid
        	+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sdbadescription
        	
        	+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.bdlinesalestaxamount
        	+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dExtendedPrice
        	+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dExtendedPriceAfterDiscount
        	+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dExtendedCost
        	+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.iLineNumber
        	+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.mDetailInvoiceComment
        	+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sMechFullName
        	+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.iTaxable
        	+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sLocationCode
        	+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sUnitOfMeasure
        	+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dQtyShipped
        	+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemCategory
        	+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sDesc
        	+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemNumber
        	+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.lictransactionid
        	+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.isuppressdetailoninvoice
        	+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.iIsStockItem
        	
        	//+ ", " + SMTableinvoicemgrcomments.TableName + ".*"
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCreationDate
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderCreatedByFullName
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.lOrderCreatedByID
//        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.datCompletedDate
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderDate
        	+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.idoingbusinessasaddressid
        	
        	+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.mTicketComments
        	+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.mInternalComments
        	
        	+ " FROM ("
		        	+ "((" + SMTableinvoiceheaders.TableName + " INNER JOIN " + SMTableinvoicedetails.TableName 
		        	+ " ON " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber
		        	+ " = " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber + ")"
	        	+ " LEFT JOIN " + SMTableorderheaders.TableName 
		        + " ON " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sOrderNumber
	        		+ " = " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber
	        	+ ") LEFT JOIN " + SMTableorderdetails.TableName
		        + " ON (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.dOrderUniqueifier
        			+ " = " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dUniqueOrderID
        			+ " AND " 
        			+ SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.iDetailNumber
        			+ " = " + SMTableorderdetails.TableName + "." + SMTableorderdetails.iDetailNumber
	        	+ ")"
        	+ ")"
        	
        	+ " LEFT JOIN " + SMTablesalesgroups.TableName + " ON " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iSalesGroup
        	+ " = " + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.iSalesGroupId;
        	
        if(sOrderNumber!="") {
        	SQL += " WHERE ("
        			//Select by Order Number
        			+"("+SMTableinvoiceheaders.TableName+"."+SMTableinvoiceheaders.sOrderNumber +"=" 
        			+ sOrderNumber+")";
        }else if(sInvoiceNumber!=""){
        	SQL += " WHERE ("
        			//Select by Order Number
        			+"("+SMTableinvoiceheaders.TableName+"."+SMTableinvoiceheaders.sInvoiceNumber +"=" 
        			+ sInvoiceNumber+")";
        }else {
        	
        	SQL += " WHERE ("
        		//Select by invoice dates
        		+ "(" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate + " >= '" 
        			+ sStartingDate + "')"
        		+ " AND (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate + " <= '" 
        			+ sEndingDate + "')"
        	
        		//Select by invoice creation dates:
           		+ " AND (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceCreationDate + " >= '" 
        			+ sStartingCreationDate + "')"
        		+ " AND (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceCreationDate + " <= '" 
        			+ sEndingCreationDate + " 23:59:59'))";
        
        		//Get the order types:
            	SQL+= " AND (INSTR('" + sOrderTypesString + "', " + SMTableinvoiceheaders.TableName + "." 
        			+ SMTableinvoiceheaders.sServiceTypeCode + ") > 0)"
        			
        		//Get the sales groups:
        		+ " AND (";
        		for (int i = 0; i < sSalesGroups.size(); i++){
        			if (i == 0){
        				SQL += "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iSalesGroup + " = " 
        					+ sSalesGroups.get(i).substring(sSalesGroups.get(i).indexOf(SMPrintInvoiceAuditSelection.SALESGROUP_PARAM_SEPARATOR) + 1) + ")";
        			}else{
        				SQL += " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iSalesGroup + " = " 
        					+ sSalesGroups.get(i).substring(sSalesGroups.get(i).indexOf(SMPrintInvoiceAuditSelection.SALESGROUP_PARAM_SEPARATOR) + 1) + ")";
        			}
        		}
        }
        	SQL += ")"
        	+ " ORDER BY " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sServiceTypeCodeDescription + " DESC"
        		+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sOrderNumber
        		+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceCreationDate
        		//+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceCreationTime
        		+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber
        		+ ", " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.iLineNumber
        	;
        if (bDebugMode){
        	System.out.println("In " + this.toString() + ".processReport SQL = " + SQL);
        }
    	//end SQL statement

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
		
		boolean bViewJobCostSummaryPermitted = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMJobCostSummaryReport,
				sUserID,
				conn,
				sLicenseModuleLevel);
		
		boolean bViewICTransactionsPermitted = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.ICTransactionHistory,
				sUserID,
				conn,
				sLicenseModuleLevel);
		
		boolean bChangeInvoicingStatePermitted = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMSendInvoices,
				sUserID,
				conn,
				sLicenseModuleLevel);
		
		//System.out.println("In " + this.toString() + ".processReport - SQL = " + SQL);
		boolean bOddRow = true;
		int iTransactionType = 0;
		
		out.println(
				"<FONT SIZE=2>"
				+ getPreviousCommenters(
						sStartingDate,
						sEndingDate,
						sStartingCreationDate,
						sEndingCreationDate,
						sOrderTypes,
						sSalesGroups,
						sOrderNumber,
						sInvoiceNumber,
						conn
				)
				+ "</FONT><BR>"
		);
		
		//Place a link to the 'last edited' bookmark:
		//System.out.println("In " + this.toString() + " last invoice edited = " + sLastInvoiceEdited);
		if (sLastInvoiceEdited.trim().compareToIgnoreCase("") != 0){
			out.println("<a href=\"#LastEdit\">Go to last edit</a>");
		}
		
		//Set up the form:
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMPrintInvoiceAuditGenerate\" METHOD=\"POST\">");
		//Convert the starting and ending dates back to MM/dd/yyyy format:
		String sStart = null;
		try {
			sStart = clsDateAndTimeConversions.utilDateToString(
				clsDateAndTimeConversions.StringTojavaSQLDate("yyyy-MM-dd", sStartingDate), "MM/dd/yyyy");
		} catch (ParseException e1) {
			m_sErrorMessage = "Error reading starting date - '" + sStart +"' - " + e1.getMessage();
			return false;
		}
		String sEnd= null;
		try {
			sEnd = clsDateAndTimeConversions.utilDateToString(
					clsDateAndTimeConversions.StringTojavaSQLDate("yyyy-MM-dd", sEndingDate), "MM/dd/yyyy");
		} catch (ParseException e1) {
			m_sErrorMessage = "Error reading ending date - '" + sEnd +"' - " + e1.getMessage();
			return false;
		}
		
		String sStartCreation= null;
		try {
			sStartCreation = clsDateAndTimeConversions.utilDateToString(
					clsDateAndTimeConversions.StringTojavaSQLDate("yyyy-MM-dd", sStartingCreationDate), "MM/dd/yyyy");
		} catch (ParseException e1) {
			m_sErrorMessage = "Error reading starting creation date - '" + sStartCreation +"' - " + e1.getMessage();
			return false;
		}
		String sEndCreation= null;
		try {
			sEndCreation = clsDateAndTimeConversions.utilDateToString(
					clsDateAndTimeConversions.StringTojavaSQLDate("yyyy-MM-dd", sEndingCreationDate), "MM/dd/yyyy");
		} catch (ParseException e1) {
			m_sErrorMessage = "Error reading ending creation date - '" + sEndCreation +"' - " + e1.getMessage();
			return false;
		}
		
		//Get the service types into an array:
		//ArrayList <String> sServiceTypes = new ArrayList <String> (0);
		
		//for (int i = 0; i < sOrderTypes.size(); i++){
		//	sServiceTypes.add("SERVICETYPE" + sOrderTypes.get(i));
		//}
		
		printHiddenVariables(
				out,
				"smcontrolpanel.SMPrintInvoiceAuditSelection",
				sStart,
				sEnd,
				sStartCreation,
				sEndCreation,
				sOrderTypes,
				sSalesGroups,
				sDBID
			);
		
    	try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()){
				
				iTransactionType = rs.getInt(SMTableinvoiceheaders.TableName + "." 
					+ SMTableinvoiceheaders.iTransactionType);
				
				sCurrentServiceType
					= rs.getString(SMTableinvoiceheaders.TableName + "." 
					+ SMTableinvoiceheaders.sServiceTypeCode);
				
				sCurrentServiceTypeDescription
				= rs.getString(SMTableinvoiceheaders.TableName + "." 
				+ SMTableinvoiceheaders.sServiceTypeCodeDescription);
				
				sCurrentInvoiceNumber
					= rs.getString(SMTableinvoiceheaders.TableName + "." 
					+ SMTableinvoiceheaders.sInvoiceNumber);
				
				sCurrentLineID = sCurrentInvoiceNumber 
					+ Long.toString(rs.getLong(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.iLineNumber));
				
    			sCurrentInvoicingState = Integer.toString(rs.getInt(SMTableinvoiceheaders.TableName 
    					+ "." + SMTableinvoiceheaders.iinvoicingstate));
    			
				//If it's a new line AND if there was a previous line, print it:
				if (
						(sCurrentLineID.compareToIgnoreCase(sLastLineID) != 0)
						&& (sLastLineID.compareToIgnoreCase("") != 0)
				){
		    		printLineFooter (
	        			sLastItemNumber, 
	        			sLastItemDesc,
	        			lLastSuppressItemOnInvoice,
	        			sLastCategory,
	        			bdLastQty,
	        			sLastUOM,
	        			bdLastCost,
	        			lLastICTransactionID,
	        			bViewICTransactionsPermitted,
	        			sDBID,
	        			"smcontrolpanel.SMPrintInvoiceAuditGenerate",
	        			bdLastPrice,
	        			sLastLocation,
	        			lLastTaxable,
	        			sLastMechanicFullName,
	        			sLastInvoiceDetailComment,
	        			sLastInternalDetailComment,
	        			sLastTicketDetailComment,
	        			sLineNumber,
	        			bOddRow,
	        			bAllowItemViewing,
	        			sURLLinkBase,
	        			out,
	        			context,
	        			bLastrecordIsNonStockItem
	        			);
				}
				
				//If it's a new invoice number AND if there was a previous invoice number,
				//print the footer for it:
				if (
						(sCurrentInvoiceNumber.compareToIgnoreCase(sLastInvoiceNumber) != 0)
						&& (sLastInvoiceNumber.compareToIgnoreCase("") != 0)
					){
					
						//Last minute math for invoice totals here:
						bdInvoiceTotalWithoutTax = bdPriceTotalForInvoice.subtract(bdInvoiceDiscountAmount);
											
				    	printInvoiceFooter(
				    			bdPriceTotalForInvoice,
				    			bdCostTotalForInvoice,
				    			bdPriceTotalForInvoice,
				    			bdDiscountPercent,
				    			sDiscountDesc,
				    			bdInvoiceDiscountAmount,
				    			bdInvoiceTaxAmount,
				    			bdInvoiceTotalWithoutTax,
				    			sLastInvoiceNumber,
				    			sLastInvoicingState,
				    			bChangeInvoicingStatePermitted,
				    			out
				        );

				    	//Reset the invoice variables:
				    	bdCostTotalForInvoice = BigDecimal.ZERO;
		    			bdPriceTotalForInvoice = BigDecimal.ZERO;
		    			bdDiscountPercent = BigDecimal.ZERO;
		    			bdInvoiceDiscountAmount = BigDecimal.ZERO;
		    			bdInvoiceTaxAmount = BigDecimal.ZERO;
		    			bdInvoiceTotalWithoutTax = BigDecimal.ZERO;
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
			    			sLastServiceTypeDescription,
			    			lTotalNumberOfInvoicesForServiceType,
			    			bdTotalInvoicePriceForServiceType,
			    			lTotalNumberOfCreditsForServiceType,
			    			bdTotalCreditPriceForServiceType,
			    			out
			        );

			    	//Reset the service type variables:
	    			lTotalNumberOfInvoicesForServiceType = 0;
	    			bdTotalInvoicePriceForServiceType = BigDecimal.ZERO;
	    			bdTotalInvoiceTaxForServiceType = BigDecimal.ZERO;
	    			lTotalNumberOfCreditsForServiceType = 0;
	    			bdTotalCreditPriceForServiceType = BigDecimal.ZERO;
	    			bdTotalCreditTaxForServiceType = BigDecimal.ZERO;
				}

				//If the service type is a new one, print the service type header:
				if (sCurrentServiceType.compareToIgnoreCase(sLastServiceType) != 0){
					printServiceTypeHeader(sCurrentServiceTypeDescription, out);
				}
				
				//If it's a new invoice number, print the invoice header:
				if (sCurrentInvoiceNumber.compareToIgnoreCase(sLastInvoiceNumber) != 0){
					String sCreationDateTime = clsDateAndTimeConversions.resultsetDateTimeStringToString(rs.getString(SMTableinvoiceheaders.TableName + "." 
						+ SMTableinvoiceheaders.datInvoiceCreationDate));
					/*String sCreationTime = rs.getString(SMTableinvoiceheaders.TableName + "." 
						+ SMTableinvoiceheaders.sInvoiceCreationTime);*/
					/*if (sCreationTime.length() == 6){
						int iHour = Integer.parseInt(sCreationTime.substring(0, 2));
						if (iHour > 12){
							sCreationDateTime = sCreationDateTime + " " + Integer.toString((iHour - 12),2)
							+ ":" + sCreationTime.substring(2, 4) + ":" + sCreationTime.substring(4, 6)
							;
						}else{
							sCreationDateTime = sCreationDateTime + " " + sCreationTime.substring(0, 2)
							+ ":" + sCreationTime.substring(2, 4) + ":" + sCreationTime.substring(4, 6)
							;
						}
						if (iHour > 11){
							sCreationDateTime = sCreationDateTime + " PM";
						}else{
							sCreationDateTime = sCreationDateTime + " AM";
						}
					}*/
					
					String sOrderCreationDateTime = clsDateAndTimeConversions.utilDateToString(rs.getDate(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.datOrderCreationDate), "MM/dd/yyyy");
					sOrderCreationDateTime = sOrderCreationDateTime + " " + clsDateAndTimeConversions.utilDateToString(rs.getTime(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.datOrderCreationDate), "h:mm a");
					String sOrderDate = clsDateAndTimeConversions.utilDateToString(rs.getDate(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.datOrderDate), "MM/dd/yyyy");
					bdDiscountPercent = BigDecimal.valueOf(
	    					rs.getDouble(SMTableinvoiceheaders.TableName + "." 
							+ SMTableinvoiceheaders.dDiscountPercentage));
	    			sDiscountDesc = rs.getString(SMTableinvoiceheaders.TableName + "." 
							+ SMTableinvoiceheaders.sDiscountDesc);
	    			bdInvoiceDiscountAmount = BigDecimal.valueOf(
	    					rs.getDouble(SMTableinvoiceheaders.TableName + "." 
							+ SMTableinvoiceheaders.dDiscountAmount));
	    			bdInvoiceTaxAmount = rs.getBigDecimal(SMTableinvoiceheaders.TableName + "." 
	    					+ SMTableinvoiceheaders.bdsalestaxamount);
	    			
//	    			String sOrderCompletionDate = "";
//					if (rs.getString(SMTableorderheaders.datCompletedDate) == null || 
//						rs.getString(SMTableorderheaders.datCompletedDate).compareTo("1899-12-31 00:00:00")
//						<= 0
//						){
//						sOrderCompletionDate = "N/A";
//					}else{
//						sOrderCompletionDate = USDateOnlyformatter.format(
//							rs.getDate(SMTableorderheaders.datCompletedDate));
//					}
	    			
	    			printInvoiceHeader(
						sCurrentInvoiceNumber,
						bViewInvoicePermitted,
						iTransactionType, 
						clsDateAndTimeConversions.utilDateToString(
							rs.getDate(SMTableinvoiceheaders.TableName + "." 
							+ SMTableinvoiceheaders.datInvoiceDate), "MM/dd/yyyy"),
						Long.toString(rs.getLong(SMTableinvoiceheaders.TableName + "." 
							+ SMTableinvoiceheaders.iDayEndNumber)),
						sCreationDateTime,
						rs.getString(SMTableinvoiceheaders.TableName + "." 
							+ SMTableinvoiceheaders.sCreatedByFullName).trim(),
						rs.getString(SMTableinvoiceheaders.TableName + "." 
							+ SMTableinvoiceheaders.sOrderNumber).trim(),
						bViewOrderPermitted,
						rs.getString(SMTableinvoiceheaders.TableName + "." 
							+ SMTableinvoiceheaders.sCustomerCode).trim(),
						rs.getString(SMTableinvoiceheaders.TableName + "." 
							+ SMTableinvoiceheaders.sBillToName).trim(),
						bViewJobCostSummaryPermitted,
						rs.getLong(SMTableinvoiceheaders.TableName + "." 
							+ SMTableinvoiceheaders.itaxid),
						rs.getString(SMTableinvoiceheaders.TableName + "." 
							+ SMTableinvoiceheaders.staxjurisdiction).trim(),
						rs.getString(SMTableinvoiceheaders.sSalesperson).trim(),
						rs.getString(SMTablesalesgroups.sSalesGroupDesc),
						rs.getString(SMTableinvoiceheaders.TableName + "." 
							+ SMTableinvoiceheaders.mInvoiceComments),
						rs.getString(SMTableinvoiceheaders.TableName + "." 
							+ SMTableinvoiceheaders.sShipToName),
						rs.getString(SMTableinvoiceheaders.TableName + "." 
							+ SMTableinvoiceheaders.sOrderSourceDesc),
						rs.getLong(SMTableinvoiceheaders.TableName + "." 
							+ SMTableinvoiceheaders.iCustomerDiscountLevel),
						rs.getString(SMTableinvoiceheaders.TableName + "." 
							+ SMTableinvoiceheaders.sDefaultPriceListCode),
						rs.getString(SMTableinvoiceheaders.TableName + "." 
							+ SMTableinvoiceheaders.sPONumber),
						rs.getString(SMTableinvoiceheaders.TableName + "." 
							+ SMTableinvoiceheaders.sTerms),
						clsDateAndTimeConversions.utilDateToString(
							rs.getDate(SMTableinvoiceheaders.TableName + "." 
							+ SMTableinvoiceheaders.datDueDate), "MM/dd/yyyy"),
						rs.getString(SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.sOrderCreatedByFullName).trim(),
						rs.getString(SMTableinvoiceheaders.TableName + "." 
								+ SMTableinvoiceheaders.sdbadescription),
						//sOrderCompletionDate,
						sOrderCreationDateTime,
						sOrderDate,
						sLastInvoiceEdited,
						sUserID,
						sDBID,
						out,
						conn,
						context
					);
					
					//Accumulate the invoice totals:
					if (iTransactionType == SMTableinvoiceheaders.TYPE_INVOICE){
						lTotalNumberOfInvoices++;
						lTotalNumberOfInvoicesForServiceType++;
						//Add the tax to the invoice total for the service type:
						bdTotalInvoicePriceForServiceType = 
							bdTotalInvoicePriceForServiceType.add(bdInvoiceTaxAmount);
					}else{
						lTotalNumberOfCredits++;
						lTotalNumberOfCreditsForServiceType++;
						//Add the tax to the Credit total for the service type:
						bdTotalCreditPriceForServiceType = 
							bdTotalCreditPriceForServiceType.add(bdInvoiceTaxAmount);
					}
				}
				
				//If it's a new line, print any line header here:
				
				//Update the 'rememberers'
				sLastServiceType = sCurrentServiceType;
				sLastServiceTypeDescription = sCurrentServiceTypeDescription;
				sLastInvoiceNumber = sCurrentInvoiceNumber;
				sLastLineID = sCurrentLineID;
				sLastInvoicingState = sCurrentInvoicingState;
				
				//Line variables:
				sLastItemNumber = rs.getString(
						SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemNumber).trim();
				
				sLastItemDesc = rs.getString(
					SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sDesc).trim();
				
				lLastSuppressItemOnInvoice = rs.getLong(
					SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.isuppressdetailoninvoice);
				
				sLastCategory = rs.getString(
					SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemCategory).trim();

				bdLastQty = BigDecimal.valueOf(
					rs.getDouble(
						SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dQtyShipped));

				sLastUOM = rs.getString(
					SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sUnitOfMeasure).trim();

				bdLastCost = BigDecimal.valueOf(
					rs.getDouble(
						SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dExtendedCost));

				bdLastPrice = BigDecimal.valueOf(
					rs.getDouble(
						SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dExtendedPrice));

				sLastLocation = rs.getString(
					SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sLocationCode).trim();

				lLastTaxable = rs.getLong(
						SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.iTaxable);
				
				sLastMechanicFullName = rs.getString(
					SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sMechFullName);
				
				sLastInvoiceDetailComment = rs.getString(
						SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.mDetailInvoiceComment);
				
				sLastInternalDetailComment = rs.getString(
						SMTableorderdetails.TableName + "." + SMTableorderdetails.mInternalComments);
				
				sLastTicketDetailComment = rs.getString(
						SMTableorderdetails.TableName + "." + SMTableorderdetails.mTicketComments);
				
				sLineNumber = Long.toString(rs.getLong(
						SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.iLineNumber));
				
				lLastICTransactionID = rs.getLong(SMTableinvoicedetails.TableName + "." 
						+ SMTableinvoicedetails.lictransactionid);
				
				bLastrecordIsNonStockItem = rs.getInt(SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.iIsStockItem) == 0;
				
				//Accumulate:
				bdPriceTotalForInvoice = bdPriceTotalForInvoice.add(
	    				BigDecimal.valueOf(rs.getDouble(
   						SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dExtendedPrice)));
				
				bdCostTotalForInvoice = bdCostTotalForInvoice.add(
	    				BigDecimal.valueOf(rs.getDouble(
   						SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dExtendedCost)));
				
				if (iTransactionType == SMTableinvoiceheaders.TYPE_INVOICE){
					bdTotalInvoiceTax = 
						bdTotalInvoiceTax.add(rs.getBigDecimal(SMTableinvoicedetails.TableName + "." 
		    						+ SMTableinvoicedetails.bdlinesalestaxamount));

					bdTotalInvoicePrice = 
						bdTotalInvoicePrice.add(
		    					BigDecimal.valueOf(rs.getDouble(SMTableinvoicedetails.TableName + "." 
		    						+ SMTableinvoicedetails.dExtendedPriceAfterDiscount)));

						bdTotalInvoiceCost = 
						bdTotalInvoiceCost.add(
		    					BigDecimal.valueOf(rs.getDouble(SMTableinvoicedetails.TableName + "." 
		    						+ SMTableinvoicedetails.dExtendedCost)));
					
	    			bdTotalInvoicePriceForServiceType = 
	    				bdTotalInvoicePriceForServiceType.add(
	    					BigDecimal.valueOf(rs.getDouble(SMTableinvoicedetails.TableName + "." 
	    						+ SMTableinvoicedetails.dExtendedPriceAfterDiscount)));

	    			bdTotalInvoiceTaxForServiceType = 	    				
	    				bdTotalInvoiceTaxForServiceType.add(rs.getBigDecimal(SMTableinvoicedetails.TableName + "." 
		    						+ SMTableinvoicedetails.bdlinesalestaxamount));
				}else{
					bdTotalInvoiceTax = 
						bdTotalInvoiceTax.add(rs.getBigDecimal(SMTableinvoicedetails.TableName + "." 
		    						+ SMTableinvoicedetails.bdlinesalestaxamount));

					bdTotalCreditPrice = 
						bdTotalCreditPrice.add(
		    					BigDecimal.valueOf(rs.getDouble(SMTableinvoicedetails.TableName + "." 
		    						+ SMTableinvoicedetails.dExtendedPriceAfterDiscount)));

					bdTotalCreditCost = 
						bdTotalCreditCost.add(
		    					BigDecimal.valueOf(rs.getDouble(SMTableinvoicedetails.TableName + "." 
		    						+ SMTableinvoicedetails.dExtendedCost)));

					bdTotalCreditPriceForServiceType = 
	    				bdTotalCreditPriceForServiceType.add(
	    						BigDecimal.valueOf(rs.getDouble(SMTableinvoicedetails.TableName + "." 
	    						+ SMTableinvoicedetails.dExtendedPriceAfterDiscount)));
	    			bdTotalCreditTaxForServiceType = 	    				
	    				bdTotalCreditTaxForServiceType.add(
	    						BigDecimal.valueOf(rs.getDouble(SMTableinvoicedetails.TableName + "." 
		    						+ SMTableinvoicedetails.bdlinesalestaxamount)));
				}
				bOddRow = !bOddRow;
			}
			rs.close();
    	}catch (SQLException e){
    		m_sErrorMessage = "Error reading resultset - " + e.getMessage();
    		return false;
    	}

    	if (sCurrentLineID.compareToIgnoreCase("") != 0){
    		printLineFooter (
    			sLastItemNumber, 
    			sLastItemDesc,
    			lLastSuppressItemOnInvoice,
    			sLastCategory,
    			bdLastQty,
    			sLastUOM,
    			bdLastCost,
    			lLastICTransactionID,
    			bViewICTransactionsPermitted,
    			sDBID,
    			"smcontrolpanel.SMPrintInvoiceAuditGenerate",
    			bdLastPrice,
    			sLastLocation,
    			lLastTaxable,
    			sLastMechanicFullName,
    			sLastInvoiceDetailComment,
    			sLastInternalDetailComment,
    			sLastTicketDetailComment,
    			sLineNumber,
    			bOddRow,
    			bAllowItemViewing,
    			sURLLinkBase,
    			out,
    			context,
    			bLastrecordIsNonStockItem);
    	}
    	
    	//Print the invoice footer if there was at least one invoice:
    	if (sCurrentInvoiceNumber.compareToIgnoreCase("") != 0){
			//Last minute math for invoice totals here:
			bdInvoiceTotalWithoutTax = bdPriceTotalForInvoice.subtract(bdInvoiceDiscountAmount);
    		printInvoiceFooter(
	    		bdPriceTotalForInvoice,
	    		bdCostTotalForInvoice,
	    		bdPriceTotalForInvoice,
    			bdDiscountPercent,
    			sDiscountDesc,
    			bdInvoiceDiscountAmount,
    			bdInvoiceTaxAmount,
    			bdInvoiceTotalWithoutTax,
    			sLastInvoiceNumber,
    			sLastInvoicingState,
    			bChangeInvoicingStatePermitted,
    			out
    		);
    	}
    	
    	//Print the service type footer if there was at least one service type:
    	if (sCurrentServiceType.compareToIgnoreCase("") != 0){
	    	printServiceTypeFooter(
				sLastServiceType,
				sLastServiceTypeDescription,
				lTotalNumberOfInvoicesForServiceType,
				bdTotalInvoicePriceForServiceType,
				lTotalNumberOfCreditsForServiceType,
				bdTotalCreditPriceForServiceType,
				out
	    	);
    	}
		printReportFooter(
			lTotalNumberOfInvoices,
			bdTotalInvoiceCost,
			bdTotalInvoicePrice,
			bdTotalInvoiceTax,
			lTotalNumberOfCredits,
			bdTotalCreditCost,
			bdTotalCreditPrice,
			bdTotalCreditTax,
			out		
		);
		
		//End the form:
		out.println ("</FORM>");
		return true;
	}
	private void printLineFooter(
			String sItem,
			String sDesc,
			long iSupressItemOnInvoice,
			String sCategory,
			BigDecimal bdQty,
			String sUnit,
			BigDecimal bdCost,
			long lICTransactionID,
			boolean bViewICTransactionsPermitted,
			String sDBID,
			String sCallingClass,
			BigDecimal bdPrice,
			String sLocation,
			long lTaxable,
			String sMechanicFullName,
			String sInvoiceDetailComment,
			String sInternalDetailComment,
			String sTicketDetailComment,
			String sLineNumber,

			boolean bIsOddRow,
			boolean bAllowItemViewing,
			String sURLLinkBase,
			PrintWriter pwOut,
			ServletContext context,
			boolean bIsNonStockItem
	){
		
		String sBackgroundColor = "";
		String sItemNumberLink = "";
		
		if(bIsOddRow){
			sBackgroundColor = "\"#EEEEEE\"";
		}else{
			sBackgroundColor = "\"#FFFFFF\"";
		}
		pwOut.println("<TR bgcolor =" + sBackgroundColor + ">");
		
		pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + sLineNumber + "&nbsp;</FONT></TD>");
		
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
		pwOut.println("<TD><FONT SIZE=2>" + sUnit + "</FONT></TD>");
		
		//Item cost:
		String sItemCost = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCost);
		if (bIsNonStockItem){
			sItemCost = NONSTOCK_ITEM_FLAG;
		}
		
		//Print link to transactiondetail here:
		if (
			bViewICTransactionsPermitted
			&& (lICTransactionID > 0)
		){
			pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>"
				+ "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICTransactionDetailsDisplay"
	    		+ "?CallingClass=" + sCallingClass
	    		+ "&" + "ICTransactionID" + "=" + lICTransactionID
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "\">"
	    		+ sItemCost 
	    		+ "</A>"
	    		+ "</FONT></TD>"
			);		
			
		}else{
			pwOut.println(
				"<TD ALIGN=RIGHT><FONT SIZE=2>" 
				+ sItemCost + "</FONT></TD>");
		}
		
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
				pwOut.println("<TR><TD>&nbsp;</TD><TD ALIGN=LEFT COLSPAN=8><FONT SIZE=2>"
					+ "<B>Work Order Detail Comments:</B></FONT></TD></TR>");
				pwOut.println("<TR><TD ALIGN=RIGHT><FONT SIZE=2>" 
					+ sLineNumber + "&nbsp;</FONT></TD><TD ALIGN=LEFT COLSPAN=8><FONT SIZE=2>" 
					+ sTicketDetailComment.trim() + "</FONT></TD></TR>");
			}
		}
		
		if (sInvoiceDetailComment != null){
			if (sInvoiceDetailComment.trim().compareToIgnoreCase("") != 0){
				pwOut.println("<TR>\n<TD>&nbsp;</TD>\n<TD ALIGN=LEFT COLSPAN=8><FONT SIZE=2><B>Invoice Detail Comments:</B></FONT></TD>\n</TR>\n");
				pwOut.println(parseLineComment(sInvoiceDetailComment, sLineNumber));
			}
		}

		if (sInternalDetailComment != null){
			if (sInternalDetailComment.trim().compareToIgnoreCase("") != 0){
				pwOut.println("<TR>\n<TD>&nbsp;</TD>\n<TD ALIGN=LEFT COLSPAN=8><FONT SIZE=2><B>Internal Detail Comments:</B></FONT></TD>\n</TR>\n");
				pwOut.println(parseLineComment(sInternalDetailComment, sLineNumber));
			}
		}
		
		//if (sInternalDetailComment != null){
		//	if (sInternalDetailComment.trim().compareToIgnoreCase("") != 0){
		//		pwOut.println("<TR><TD>&nbsp;</TD><TD ALIGN=LEFT COLSPAN=8><FONT SIZE=2>"
		//				+ "<B>Internal Detail Comments:</B></FONT></TD></TR>");
		//		pwOut.println("<TR><TD ALIGN=RIGHT><FONT SIZE=2>" 
		//			+ sLineNumber + "&nbsp;</FONT></TD><TD ALIGN=LEFT COLSPAN=8><FONT SIZE=2>" 
		//			+ sInternalDetailComment.trim() + "</FONT></TD></TR>");
		//	}
		//}	

	}
	
	private String parseLineComment(
			String sInvComment,
			String sLineNo
	){
		
		String sParsedComment = "";
		String sPrefix = 
			"<TR>\n<TD ALIGN=RIGHT><FONT SIZE=2>" 
			+ sLineNo
			+ "&nbsp;</TD>\n<TD>\n"
		;
		
		String sSuffix = "</FONT></TD>\n</TR>\n";
		
		String sRemainingLine = sInvComment;
		int iCommentLineCounter = 97;
		while (sRemainingLine.length() > 0){
			sParsedComment = sParsedComment + sPrefix;
			
			sParsedComment = 
				sParsedComment
				//+ "(" 
				+ (char)iCommentLineCounter + ")"
				+ "</FONT></TD>\n"
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
	private void printInvoiceHeader(
			String sInvNumber,
			boolean bViewInvoice,
			int iTransType,
			String sInvoiceDate,
			String sDayEndNumber,
			String sCreationTime,
			String sCreatedByFullName,
			String sOrdNumber,
			boolean bViewOrder,
			String sCustCode,
			String sBillToName,
			boolean bViewJobCost,
			long itaxid,
			String sTaxGroup,
			String sSalesCode,
			String sSalesGroup,
			String sInvoiceComment,
			String sShipToName,
			String sOrderSource,
			long lPriceLevel,
			String sPriceList,
			String sPONumber,
			String sTerms,
			String sDueDate,
			String sOrderCreatedByFullName,
			String sDBADescription,
			//String sOrderCompletionDate,
			String sOrderCreationTime,
			String sOrderDate,
			String sLastInvEdited,
			String sCurrentUserID,
			String sDBID,
			PrintWriter pwOut,
			Connection con,
			ServletContext context
	){
		
		String SQL = "";
		
		//If this was the last invoice edited, place a bookmark here:
		if (sLastInvEdited.compareToIgnoreCase(sInvNumber) == 0){
			pwOut.println("<a name=\"LastEdit\">");
		}
		
		if (iTransType == SMTableinvoiceheaders.TYPE_INVOICE){
			pwOut.println("<B><FONT SIZE=2>Invoice");
		}else{
			pwOut.println("<B><FONT SIZE=2>Credit Note");
		}
		
		pwOut.println(" :&nbsp;</B>");
		
		if (bViewInvoice){
			pwOut.println( 
					"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "" 
					+ SMUtilities.lnViewInvoice(sDBID, sInvNumber )
		    		+ "\">"
		    		+ sInvNumber
		    		+ "</A>"
					);
		}else{
			pwOut.println(sInvNumber);
		}
		
		pwOut.println("&nbsp;&nbsp;&nbsp;&nbsp;<B>Date:</B>&nbsp;" + sInvoiceDate);
		
		//datduedate
		pwOut.println("&nbsp;&nbsp;&nbsp;<B>Due:</B>&nbsp;" + sDueDate);
		
		if (sDayEndNumber.compareToIgnoreCase("0") == 0){
			sDayEndNumber = "<B><U>NOT COSTED</U></B>";
		}
		pwOut.println("&nbsp;&nbsp;&nbsp;&nbsp;<B>Day End:</B>&nbsp;" + sDayEndNumber);
		
		pwOut.println("&nbsp;&nbsp;&nbsp;&nbsp;<B>Created:</B>&nbsp;" + sCreationTime
			+ "&nbsp;by&nbsp;" + sCreatedByFullName
		);
		
		String sTax = sTaxGroup;
		try{
			SQL = "SELECT *"
				+ " FROM " + SMTabletax.TableName
				+ " WHERE ("
					+ "(" + SMTabletax.lid + " = " + itaxid + ")"
				+ ")"
				;
			//System.out.println("SQL [1481237599]: " + SQL);
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, con);
			if (rs.next()){
				sTax += " - "
					+ rs.getString(SMTabletax.staxtype) + " - "
					+ rs.getString(SMTabletax.sdescription)
				;
			}
			rs.close();
		}catch (SQLException e){
			System.out.println("Error [1481237598] In " + this.toString() 
				+ ".printInvoiceHeader - error getting name of tax: " + e.getMessage());
		}

		pwOut.println("&nbsp;&nbsp;&nbsp;&nbsp;<B>Tax:</B>&nbsp;" 
			+ "</FONT><FONT COLOR=DARKORANGE SIZE=2><B>"
			+ sTax
			+ "</B></FONT>"
		);
		pwOut.println("<BR>");
		
		//Order information:
		pwOut.println("<B><FONT SIZE=2>Order:&nbsp;</B>");
		if (bViewOrder){
			pwOut.println( 
					"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" 
		    		+ sOrdNumber 
		    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
		    		+ "\">" + sOrdNumber + "</A>"
					);
		}else{
			pwOut.println(sOrdNumber);
		}
		pwOut.println("&nbsp;<B>Order Date:</B>&nbsp;" + sOrderDate);
		
		if (bViewJobCost){
			//If there is no Job Cost info available, don't show this link:
			SQL = "SELECT"
				+ " " + SMTableworkorders.lid
				+ " FROM " + SMTableworkorders.TableName
				+ " WHERE ("
					+ "(" + SMTableworkorders.strimmedordernumber + " = '" + sOrdNumber.trim() + "')"
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
					pwOut.println("&nbsp;" 
							+ "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel."
							+ "SMDisplayJobCostInformation?OrderNumber=" + sOrdNumber.trim() 
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + "Job Cost" + "</A>"
						);
				}else{
					pwOut.println("&nbsp;"+ "Job Cost: N/A");
				}
		}

		//Get the name of the 'ordercreated by':
		pwOut.println("&nbsp;&nbsp;&nbsp;&nbsp;<B>Order Created:</B>&nbsp;" + sOrderCreationTime
			+ "&nbsp;by&nbsp;" + sOrderCreatedByFullName
		);
		
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
			System.out.println("In " + this.toString() 
				+ ".printInvoiceHeader - error getting name of salesperson: " + e.getMessage());
		}
		pwOut.println("&nbsp;&nbsp;&nbsp;<B>Salesperson:</B>&nbsp;" + sFullSalesName);
		
		//Print the DBA description:
		pwOut.println("&nbsp;&nbsp;&nbsp;<B>Doing business as:</B>&nbsp;" + sDBADescription);
		
		//sSalesGroup
		if(sSalesGroup != null){ 
			pwOut.println("&nbsp;&nbsp;&nbsp;<B>Salesgroup:</B>&nbsp;" + sSalesGroup.trim());
		}else{
			pwOut.println("&nbsp;&nbsp;&nbsp;<B>Salesgroup:</B>&nbsp;N/A");
		}
		
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
		pwOut.println("&nbsp;<B>PO:</B>&nbsp;" + sPONumber);
		
		//Order source
		pwOut.println("&nbsp;<B>Source:</B>&nbsp;" + sOrderSource);
		
//		//Order completion date:
//		pwOut.println("&nbsp;<B>Order Completed:</B>&nbsp;" + sOrderCompletionDate);
		
		//Follow Up Sales Lead
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
			System.out.println("Error [1496063437] in " + this.toString() 
				+ ".printInvoiceHeader - error getting created by sales lead numbers: " + e.getMessage());
		}
		pwOut.println("&nbsp;<B>Follow up sales lead(s):</B>&nbsp;" + sCreatedFromSalesLead);
		pwOut.println("</FONT>");

		pwOut.println("<BR>");
		
		if (sInvoiceComment != null){
			if (sInvoiceComment.trim().compareToIgnoreCase("") != 0){
				pwOut.println("<FONT SIZE=2>" 
					+ sInvoiceComment.trim().replace("\n", "<BR>") + "</FONT><BR>");
			}
		}
		
		//Display any invoice manager comments here:
		SQL = " SELECT * FROM " + SMTableinvoicemgrcomments.TableName
			+ " WHERE ("
				+ SMTableinvoicemgrcomments.sinvoicenumber + " = '" + sInvNumber + "'"
			+ ")"
			+ " ORDER BY " + SMTableinvoicemgrcomments.datlastedited
			;
		boolean bCommentsTableConfigured = false;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, con);
			while (rs.next()){
				String sComment = rs.getString(SMTableinvoicemgrcomments.TableName + "." 
						+ SMTableinvoicemgrcomments.mcomment);
				String sCommenterFullName = rs.getString(SMTableinvoicemgrcomments.TableName + "." 
						+ SMTableinvoicemgrcomments.suserfullname);
				String sCommenterID = rs.getString(SMTableinvoicemgrcomments.TableName + "." 
						+ SMTableinvoicemgrcomments.luserid);
				
				if (sComment != null){
					if (sComment.trim().compareToIgnoreCase("") != 0){
						//If it's the first line, set up a table:
						if (!bCommentsTableConfigured){
							pwOut.println("<TABLE BORDER=0>");
							bCommentsTableConfigured = true;
						}

						//If the comment is from THIS user, show it ins GREEN, otherwise, show it
						//in red:
						String sAlertColor = "RED";
						if (sCurrentUserID.compareToIgnoreCase(sCommenterID) == 0){
							sAlertColor = "GREEN";
						}
						
						//Print a bookmark for this comment:
						
						//First get the index for this commenter:
						int iCurrentCommenterIndex = sCommenters.indexOf(sCommenterFullName);
						/*
						if (iCurrentCommenterIndex < 0){
							//Add to the commenters arrays:
							sCommenters.add(sCommenterFullName);
							iCommentCounter.add((Integer) 0);
							//Store the comment count for each commenter:
							iCommentCount.add(1);
							iCurrentCommenterIndex = 0;
						}
						*/
						//Next, increment the counter for this commenter:
						try {
							iCommentCounter.set(iCurrentCommenterIndex, iCommentCounter.get(iCurrentCommenterIndex) + 1);
						} catch (Exception e) {
							//Nothing to do - just trap this condition
						}
						
						//Now, print a bookmark for this comment:
						String sCommentBookMark = "";
						try {
							sCommentBookMark = "<a name=\"" 
							+ sCommenterFullName.replace(" ", "") 
							+ Integer.toString(iCommentCounter.get(iCurrentCommenterIndex))
							+ "\">";
						} catch (Exception e) {
							pwOut.println("<BR><FONT COLOR=RED><B>Error [1397678894] in SMInvoiceAuditReport - " + e.getMessage() + "</B></FONT>.");
						}
						String sNextCommentLink = "";
						
						//If the 
						try{
							//LTO 20140424
							//Catch the out of bound exception and see what's causing it.
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
						}catch(ArrayIndexOutOfBoundsException aioobe){
							System.out.println("[1398372899] iCommentCount.size() = " + iCommentCount.size());
							System.out.println("[1398372899] iCommentCounter.size() = " + iCommentCounter.size());
							System.out.println("[1398372899] iCurrentCommenterIndex = " + iCurrentCommenterIndex);
							System.out.println("[1398372899] Ex.msg = " +  aioobe.getMessage());
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
								rs.getDate(SMTableinvoicemgrcomments.datlastedited), "MM/dd/yyyy")
								+ " " + clsDateAndTimeConversions.utilDateToString(
										rs.getTime(SMTableinvoicemgrcomments.datlastedited), "h:mm a")
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
								+ "<TEXTAREA NAME=\"" + "COMMENT" + sInvNumber 
								+ "ID"
								+ Long.toString(rs.getLong(SMTableinvoicemgrcomments.id))
								+ "\" rows=2 cols=50>"
								+ sComment.trim() + "</TEXTAREA>"
								+ "</FONT>"
								+ "<TD>"
							);
							
							pwOut.println("<INPUT TYPE=\"SUBMIT\" NAME=\"" 
									+ "UPDATETHISCOMMENT" 
									+ sInvNumber 
									+ "ID" 
									+ Long.toString(rs.getLong(SMTableinvoicemgrcomments.id)) 
									+ "\" VALUE=\"Update comment\">");
							
							pwOut.println("<INPUT TYPE=\"SUBMIT\" NAME=\"" 
									+ "REMOVETHISCOMMENT" 
									+ sInvNumber 
									+ "ID" 
									+ Long.toString(rs.getLong(SMTableinvoicemgrcomments.id)) 
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
		pwOut.println("<TD><FONT SIZE=2><B>UNIT</B></FONT></TD>");
		pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>COST</B></FONT></TD>");
		pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>PRICE</B></FONT></TD>");
		pwOut.println("<TD ALIGN=CENTER><FONT SIZE=2><B>TAX?</B></FONT></TD>");
		
		pwOut.println("</TR>");
	}
	private void printInvoiceFooter(
			BigDecimal bdPriceBeforeTax,
			BigDecimal bdTotalCost,
			BigDecimal bdTotalPrice,
			BigDecimal bdDiscountPercentage,
			String sDiscountDescription,
			BigDecimal bdDiscountAmount,
			BigDecimal bdTaxAmount,
			BigDecimal bdNetTotalWithoutTax,
			String sInvNumber,
			String sInvoicingState,
			boolean bChangeInvoicingStatePermitted,
			PrintWriter pwOut
	){
		
		//Total cost and price:
		pwOut.println("<TD ALIGN=RIGHT COLSPAN=9><FONT SIZE=2>" 
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
		if ((bdDiscountPercentage.compareTo(BigDecimal.ZERO) != 0)
				|| (bdDiscountAmount.compareTo(BigDecimal.ZERO) != 0)
			){
			pwOut.println("<TR>");
			pwOut.println("<TD ALIGN=RIGHT COLSPAN=8><FONT SIZE=2>" 
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
			pwOut.println("<TD ALIGN=RIGHT COLSPAN=8><FONT SIZE=2>" 
				+ "<B>Net amount without tax:</B>" + "</FONT></TD>");
			pwOut.println("<TD ALIGN=RIGHT COLSPAN=2><FONT SIZE=2>"
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdNetTotalWithoutTax)
					+ "</FONT></TD>");
			pwOut.println("</TR>");
			
			pwOut.println("<TR>");
			pwOut.println("<TD ALIGN=RIGHT COLSPAN=8><FONT SIZE=2>" 
					+ "<B>Tax amount:</B>" + "</FONT></TD>");
				pwOut.println("<TD ALIGN=RIGHT COLSPAN=2><FONT SIZE=2><U>+&nbsp;"
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTaxAmount)
						+ "</U></FONT></TD>");
				pwOut.println("</TR>");
				
			pwOut.println("</TR>");
		}
		
		//Total invoice line:
		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT COLSPAN=8><FONT SIZE=2>" 
				+ "<B>Invoice total:</B>" + "</FONT></TD>");
		pwOut.println("<TD ALIGN=RIGHT COLSPAN=2><FONT SIZE=2>"
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdNetTotalWithoutTax.add(bdTaxAmount))
				+ "</FONT></TD>");
		pwOut.println("</TR>");
			
		//End the line table:
		pwOut.println("</TABLE>");
		
		//Add a drop down for invoiceing state:
		if(bChangeInvoicingStatePermitted){
			
		
			ArrayList<String> arrStateValues = new ArrayList<String>();
			ArrayList<String> arrStateDescriptions = new ArrayList<String>();
			
			for(int i = 0; i < SMTableinvoiceheaders.NUMBER_OF_INVOICING_STATES; i++){
				arrStateDescriptions.add(SMTableinvoiceheaders.getInvoicingStateDescription(i));
				arrStateValues.add(Integer.toString(i));
			}
		pwOut.println("<TABLE><TR><TD><B>Invoicing state: </B></TD><TD>" 
				
				//Regular text box:
				//+ SMUtilities.TDTextBox("COMMENT" + sInvNumber + "ID0", "", 100, 254, ""));
				
				//Multiline text box:
				+ clsCreateHTMLFormFields.Create_Edit_Form_List_Field(
							"STATE" + sInvoicingState  + "INV" + sInvNumber , 
							arrStateValues, 
							sInvoicingState, 
							arrStateDescriptions) 
				 + "</TD></TR>"
				 );
		}
		//Add a text box for comments:
		pwOut.println("<TR><TD align=\"right\"><B>Comment: </B></TD><TD>" 
				
				//Regular text box:
				//+ SMUtilities.TDTextBox("COMMENT" + sInvNumber + "ID0", "", 100, 254, ""));
				
				//Multiline text box:
				+ "<TEXTAREA NAME=\"" + "COMMENT" + sInvNumber + "ID0" + "\" rows=2 cols=80>"
				+ "" + "</TEXTAREA></TD></TR></TABLE>"
				);
		String sButtonLabel = "";
		if(bChangeInvoicingStatePermitted){
			sButtonLabel = "\"Save comments/states\"";
		}else{
			sButtonLabel = "\"Save comments\"";
		}
		pwOut.println("<BR>&nbsp;&nbsp;<INPUT TYPE=\"SUBMIT\" NAME=\"" 
			+ "SAVEALLCOMMENTS" + sInvNumber + "\" VALUE=" + sButtonLabel + ">");
		
		pwOut.println("<HR>");
	}
	private void printServiceTypeHeader(String sServiceTypeDescription, PrintWriter pwOut){
		
		pwOut.println("<BR><B><I><U>Service Type: " 
			+ sServiceTypeDescription + "</U></I></B><BR>");
	}
	private void printServiceTypeFooter(
			String sServiceType,
			String sServiceTypeDescription,
			long lNumberOfInvoicesForServiceType,
			BigDecimal bdTotalInvoicePriceWithTaxForServiceType,
			long lNumberOfCreditsForServiceType,
			BigDecimal bdTotalCreditPriceWithTaxForServiceType,
			PrintWriter pwOut
			){

		pwOut.println("<TABLE BORDER=1 WIDTH=100%>");
		
		//Invoices
		pwOut.println("<TR>");
		
		/*
		pwOut.println("<BR>DIAGNOSTIC: bdTotalInvoicePriceWithTaxForServiceType = " + SMUtilities.BigDecimalTo2DecimalSTDFormat(
				(bdTotalInvoicePriceWithTaxForServiceType)) + "<BR>");
		pwOut.println("<BR>DIAGNOSTIC: bdTotalCreditPriceWithTaxForServiceType = " + SMUtilities.BigDecimalTo2DecimalSTDFormat(
				(bdTotalCreditPriceWithTaxForServiceType)) + "<BR>");
		*/
		
		pwOut.println("<TD ALIGN=RIGHT>Number of invoices in " 
			+ sServiceTypeDescription + ":</TD>");
		pwOut.println("<TD ALIGN=RIGHT>" + Long.toString(lNumberOfInvoicesForServiceType) + "</TD>");
		pwOut.println("<TD ALIGN=RIGHT>Avg. amount:</TD>");
		BigDecimal bdNumberOfInvoices = BigDecimal.valueOf(lNumberOfInvoicesForServiceType);
		if (lNumberOfInvoicesForServiceType > 0){
			pwOut.println("<TD ALIGN=RIGHT>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
					(bdTotalInvoicePriceWithTaxForServiceType).divide(
						bdNumberOfInvoices, BigDecimal.ROUND_HALF_UP))
					+ "</TD>");
		}else{
			pwOut.println("<TD ALIGN=RIGHT>0.00</TD>");
		}
		pwOut.println("</TR>");

		//Credits
		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT>Number of credits in " 
			+ sServiceTypeDescription + ":</TD>");
		pwOut.println("<TD ALIGN=RIGHT>" + Long.toString(lNumberOfCreditsForServiceType) + "</TD>");
		pwOut.println("<TD ALIGN=RIGHT>Avg. amount:</TD>");
		BigDecimal bdNumberOfCredits = BigDecimal.valueOf(lNumberOfCreditsForServiceType);
		if (lNumberOfCreditsForServiceType > 0){
			pwOut.println("<TD ALIGN=RIGHT>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
					(bdTotalCreditPriceWithTaxForServiceType).divide(
							bdNumberOfCredits, BigDecimal.ROUND_HALF_UP))
					+ "</TD>");
		}else{
			pwOut.println("<TD ALIGN=RIGHT>0.00</TD>");
		}
		pwOut.println("</TR>");

		pwOut.println("</TABLE>");
		
	}
	private void printReportFooter(
			long lTotalNumOfInvoices,
			BigDecimal bdTotInvoiceCost,
			BigDecimal bdTotInvoicePrice,
			BigDecimal bdTotInvoiceTax,
			long lTotalNumOfCredits,
			BigDecimal bdTotCreditCost,
			BigDecimal bdTotCreditPrice,
			BigDecimal bdTotCreditTax,
			PrintWriter pwOut
			){

		pwOut.println("<BR><TABLE BORDER=2 WIDTH=100%>");
		
		//Invoices
		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT><B>Total number of invoices:</B></TD>");
		pwOut.println("<TD ALIGN=RIGHT><B>" + Long.toString(lTotalNumOfInvoices) + "</B></TD>");
		pwOut.println("<TD ALIGN=RIGHT><B>Avg. amount:</B></TD>");
		BigDecimal bdNumberOfInvoices = BigDecimal.valueOf(lTotalNumOfInvoices);
		if (lTotalNumOfInvoices > 0){
			pwOut.println("<TD ALIGN=RIGHT><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
					(bdTotInvoicePrice.add(bdTotInvoiceTax)).divide(
						bdNumberOfInvoices, BigDecimal.ROUND_HALF_UP))
					+ "</B></TD>");
		}else{
			pwOut.println("<TD ALIGN=RIGHT><B>0.00</B></TD>");
		}
		pwOut.println("</TR>");

		//Credits
		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT><B>Total number of credit notes:</B></TD>");
		pwOut.println("<TD ALIGN=RIGHT><B>" + Long.toString(lTotalNumOfCredits) + "</B></TD>");
		pwOut.println("<TD ALIGN=RIGHT><B>Avg. amount:</B></TD>");
		BigDecimal bdNumberOfCredits = BigDecimal.valueOf(lTotalNumOfCredits);
		if (lTotalNumOfCredits > 0){
			pwOut.println("<TD ALIGN=RIGHT><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
					(bdTotCreditPrice.add(bdTotCreditTax)).divide(
						bdNumberOfCredits, BigDecimal.ROUND_HALF_UP))
					+ "</B></TD>");
		}else{
			pwOut.println("<TD ALIGN=RIGHT><B>0.00</B></TD>");
		}
		pwOut.println("</TR>");
		pwOut.println("</TABLE><BR>");
		
		pwOut.println("<TABLE BORDER=0 WIDTH=100%>");
		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT><B><I>Total:</U></I></B></TD>");
		pwOut.println("<TD ALIGN=RIGHT><B><I><U>Cost:</U></I></B></TD>");
		pwOut.println("<TD ALIGN=RIGHT><B><I><U>Price:</U></I></B></TD>");
		pwOut.println("<TD ALIGN=RIGHT><B><I><U>Tax:</U></I></B></TD>");
		pwOut.println("<TD ALIGN=RIGHT><B><I><U>Invoice Amt:</U></I></B></TD>");
		pwOut.println("</TR>");

		//Invoice grand totals
		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT><B><I>Invoice:</I></B></TD>");
		pwOut.println("<TD ALIGN=RIGHT><B>" 
			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotInvoiceCost) + "</B></TD>");
		pwOut.println("<TD ALIGN=RIGHT><B>" 
			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotInvoicePrice) + "</B></TD>");
		pwOut.println("<TD ALIGN=RIGHT><B>" 
			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotInvoiceTax) + "</B></TD>");
		pwOut.println("<TD ALIGN=RIGHT><B>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotInvoicePrice.add(bdTotInvoiceTax))
				+ "</B></TD>");
		pwOut.println("</TR>");

		//Credit grand totals
		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT><B><I>Credit Note:</I></B></TD>");
		pwOut.println("<TD ALIGN=RIGHT><B>" 
			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotCreditCost) + "</B></TD>");
		pwOut.println("<TD ALIGN=RIGHT><B>" 
			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotCreditPrice) + "</B></TD>");
		pwOut.println("<TD ALIGN=RIGHT><B>" 
			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotCreditTax) + "</B></TD>");
		pwOut.println("<TD ALIGN=RIGHT><B>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotCreditPrice.add(bdTotCreditTax))
				+ "</B></TD>");
		pwOut.println("</TR>");

		//Overall grand totals:
		pwOut.println("<TR>");
		pwOut.println("<TD ALIGN=RIGHT><B><I>Grand Total:</I></B></TD>");
		pwOut.println("<TD ALIGN=RIGHT><B>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotInvoiceCost.add(bdTotCreditCost)) 
				+ "</B></TD>");
		pwOut.println("<TD ALIGN=RIGHT><B>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotInvoicePrice.add(bdTotCreditPrice)) 
				+ "</B></TD>");
		pwOut.println("<TD ALIGN=RIGHT><B>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotInvoiceTax.add(bdTotCreditTax)) 
				+ "</B></TD>");
		pwOut.println("<TD ALIGN=RIGHT><B>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
				(bdTotInvoicePrice.add(bdTotInvoiceTax)).add(bdTotCreditPrice.add(bdTotCreditTax))) 
				+ "</B></TD>");

		pwOut.println("</TR>");
		
		pwOut.println("</TABLE>");

	}
	private void printHiddenVariables(
			PrintWriter pwOut,
			String sCallingClass,
			String sStartDate,
			String sEndDate,
			String sStartCreationDate,
			String sEndCreationDate,
			ArrayList <String> sServiceTypes,
			ArrayList <String> sSalesGroups,
			String sDBID
			){
		
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		
		pwOut.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='" + sCallingClass + "'>");

		pwOut.println("<INPUT TYPE=HIDDEN NAME='StartingDate' VALUE='" + sStartDate + "'>");
		
		pwOut.println("<INPUT TYPE=HIDDEN NAME='EndingDate' VALUE='" + sEndDate + "'>");
		
		pwOut.println("<INPUT TYPE=HIDDEN NAME='StartingCreationDate' VALUE='" + sStartCreationDate + "'>");
		
		pwOut.println("<INPUT TYPE=HIDDEN NAME='EndingCreationDate' VALUE='" + sEndCreationDate + "'>");
		
		//This will tell SMPrintInvoiceAuditGenerate that this request is a 're-processing'
		//of the report, so it won't record in the log that the user is running the report twice
		pwOut.println("<INPUT TYPE=HIDDEN NAME='Reprocess' VALUE='" + "TRUE" + "'>");
		
	    //SERVICETYPEs
		for (int i = 0; i < sServiceTypes.size(); i++){
			pwOut.println("<INPUT TYPE=HIDDEN NAME='SERVICETYPE" + sServiceTypes.get(i) + "' VALUE='YES'>");
		}
	    //SALESGROUPs
		for (int i = 0; i < sSalesGroups.size(); i++){
			pwOut.println("<INPUT TYPE=HIDDEN NAME='SALESGROUPCODE" + sSalesGroups.get(i) + "' VALUE='YES'>");
		}
		
		//Sales Groups
		for (int i = 0; i < sSalesGroups.size(); i++){
			pwOut.println("<INPUT TYPE=HIDDEN NAME='" 
			+ SMPrintInvoiceAuditSelection.SALESGROUP_PARAM 
			//+ SMPrintInvoiceAuditSelection.SALESGROUP_PARAM_SEPARATOR
			+ sSalesGroups.get(i) 
			+ "' VALUE='YES'>");
		}		
		
	}
	private String getPreviousCommenters(
			String sStartDate,
			String sEndDate,
			String sStartCreationDate,
			String sEndCreationDate,
			ArrayList <String> sOrderTypesList,
			ArrayList <String> sSalesGroupList,
			String sOrderNumber,
			String sInvoiceNumber,
			Connection con
	){
		
		String sResult = "";
		
		//Create string of order types:
		String sListOfOrderTypes = "";
		for (int i = 0; i < sOrderTypesList.size(); i++){
			sListOfOrderTypes += "," + sOrderTypesList.get(i);
		}
		
		String SQL = "SELECT Count(" 
			+ SMTableinvoicemgrcomments.TableName + "." + SMTableinvoicemgrcomments.luserid 
			+ ") AS CommentCount, "
			+ SMTableinvoicemgrcomments.TableName + "." + SMTableinvoicemgrcomments.suserfullname
			+ " FROM " + SMTableinvoicemgrcomments.TableName + " INNER JOIN"
			+ " " + SMTableinvoiceheaders.TableName + " ON"
			+ " " + SMTableinvoicemgrcomments.TableName + "." + SMTableinvoicemgrcomments.sinvoicenumber
			+ " = " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber;
			
        if(sOrderNumber!="") {
        	SQL += " WHERE ("
        			//Select by Order Number
        			+"("+SMTableinvoiceheaders.TableName+"."+SMTableinvoiceheaders.sOrderNumber +"=" 
        			+ sOrderNumber+")";
        }else if(sInvoiceNumber!=""){
        	SQL += " WHERE ("
        			//Select by Order Number
        			+"("+SMTableinvoiceheaders.TableName+"."+SMTableinvoiceheaders.sInvoiceNumber +"=" 
        			+ sInvoiceNumber+")";
        }else {
        	
        	SQL += " WHERE ("
        		//Select by invoice dates
        		+ "(" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate + " >= '" 
        			+ sStartDate + "')"
        		+ " AND (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate + " <= '" 
        			+ sEndDate + "'))"
        	
        		//Select by invoice creation dates:
           		+ " AND (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceCreationDate + " >= '" 
        			+ sStartCreationDate + "')"
        		+ " AND (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceCreationDate + " <= '" 
        			+ sEndCreationDate + " 23:59:59')"
        
        		//Get the order types:
        		+ " AND (INSTR('" + sListOfOrderTypes + "', " + SMTableinvoiceheaders.TableName 
        	
        		+ "." + SMTableinvoiceheaders.sServiceTypeCode + ") > 0)"
        			
        		//Get the sales groups:
        		+ " AND (";
        		for (int i = 0; i < sSalesGroupList.size(); i++){
        			if (i == 0){
        				SQL += "(" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iSalesGroup + " = " 
        					+ sSalesGroupList.get(i).substring(sSalesGroupList.get(i).indexOf(SMPrintInvoiceAuditSelection.SALESGROUP_PARAM_SEPARATOR) + 1) + ")";
        			}else{
        				SQL += " OR (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iSalesGroup + " = " 
        					+ sSalesGroupList.get(i).substring(sSalesGroupList.get(i).indexOf(SMPrintInvoiceAuditSelection.SALESGROUP_PARAM_SEPARATOR) + 1) + ")";
        			}
        		}
        }
        		SQL += ")"
				
			+ " GROUP BY " + SMTableinvoicemgrcomments.TableName + "." + SMTableinvoicemgrcomments.suserfullname
			;
        if (bDebugMode){
        	System.out.println("In " + this.toString() + ".getPreviousCommenters - SQL = " + SQL);
        }
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, con);
			while (rs.next()){
				String sCommenterFullName = rs.getString(SMTableinvoicemgrcomments.suserfullname);
				if (sResult.compareToIgnoreCase("") == 0){
					sResult = "<B>PREVIOUS COMMENTS: </B>The following people currently have left at least one comment "
						+ "on this range of invoices: "
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
			sResult = "<B>NO</B> comments have been made on this range of invoices yet";
		}
		return sResult + ".";
	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}

package smcontrolpanel;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.ServletContext;

import SMClasses.SMInvoice;
import SMClasses.SMInvoiceDetail;
import SMClasses.SMLogEntry;
import SMClasses.SMOrderDetail;
import SMClasses.SMOrderHeader;
import SMClasses.SMTax;
import SMDataDefinition.SMTablearterms;
import SMDataDefinition.SMTableiccategories;
import SMDataDefinition.SMTableinvoicedetails;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTableordermgrcomments;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;
import smic.ICItem;

public class SMCreateInvoice extends java.lang.Object{

	private static boolean bDebugMode = false;
	public static SimpleDateFormat sdfTime = new SimpleDateFormat("HHmmss");
	public static SimpleDateFormat sdfNormalDate = new SimpleDateFormat("MM/dd/yyyy");
	private static Date datDueDate = new Date(System.currentTimeMillis());
	private static Date datDiscountDate = new Date(System.currentTimeMillis());
	private static double dDiscountPercentage = 0;
	private static String sExpenseGLAcct = "";
	private static String sRevenueGLAcct = "";
	private static String sInventoryGLAcct = "";

	public SMCreateInvoice(
			){
		datDueDate = new Date(System.currentTimeMillis());
		datDiscountDate = new Date(System.currentTimeMillis());
		dDiscountPercentage = 0;
		sExpenseGLAcct = "";
		sRevenueGLAcct = "";
		sInventoryGLAcct = "";
	}

	public void Create_Invoice(SMOrderHeader cOrder, 
			SMInvoice cInvoice, 
			String sInvoiceDate, 
			String sUserID, 
			String sUserFullName,
			Connection conn,
			String sDBID,
			ServletContext context) 
					throws Exception{
		int iInvLineNum = 0;
		BigDecimal bdTRate = new BigDecimal("0.0000");
		SMLogEntry log = new SMLogEntry(sDBID, context); 
		if (bDebugMode){
			System.out.println("[1540839462] - Starting creatInvoice function for invoice number " +cInvoice+ " from user " + sUserFullName + " at " + System.currentTimeMillis());
			log.writeEntry(
					sUserID,
					SMLogEntry.LOG_OPERATION_CREATEINVOICE,
					"Order number " + cOrder.getM_strimmedordernumber(),
					"Last edited by " + cOrder.getM_LASTEDITUSERFULLNAME()
					+ ", Disc Percent: " + cOrder.getM_dPrePostingInvoiceDiscountPercentage()
					+ ", Disc Amt: " + cOrder.getM_dPrePostingInvoiceDiscountAmount(),
					"[1376509314]"
					);
		}
		//STARTINVOICING:
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		
		try{
			cInvoice.setM_sOrderNumber(cOrder.getM_sOrderNumber());
			cInvoice.setM_strimmedordernumber(cOrder.getM_strimmedordernumber());
			cInvoice.setM_datInvoiceDate(sdf.parse(sInvoiceDate).getTime());
			
			//System.out.println("[1514902773] - sInvoiceDate = '" + sInvoiceDate + "'");
			//System.out.println("[1514902774] - cInvoice.getM_datInvoiceDate() = '" + cInvoice.getM_datInvoiceDate() + "'");
			
			cInvoice.setM_sCreatedByFullName(sUserFullName);
			cInvoice.setM_lCreatedByID(sUserID);
			cInvoice.setM_datOrderDate(sdf.parse(cOrder.getM_datOrderDate()).getTime());
			cInvoice.setM_sTerms(cOrder.getM_sTerms());
		}catch(Exception ex){
			throw new Exception ("Error setting invoice dates and times: " + ex.getMessage());
		}
		if (bDebugMode){
			System.out.println("[1540839462A] - In Create_Invoice calculating Invoice Due Date.");
		}
		try{
			Calculate_Invoice_Due_Date(cInvoice.getM_sTerms(), cInvoice.getM_datInvoiceDate(), conn);
		} catch (SQLException e){
			throw new Exception ("Failed to calculate invoice due date for order #" + cInvoice.getM_sOrderNumber()
					+ " - " + e.getMessage() + ".");
		}
		cInvoice.setM_datDueDate(datDueDate);
		cInvoice.setM_datTermsDiscountDate(datDiscountDate);
		cInvoice.setM_dTermsDiscountPercentage(BigDecimal.valueOf(dDiscountPercentage));
		cInvoice.setM_dDiscountAmount(new BigDecimal(cOrder.getM_dPrePostingInvoiceDiscountAmount().replace(",","")));
		cInvoice.setM_dDiscountPercentage(new BigDecimal(cOrder.getM_dPrePostingInvoiceDiscountPercentage().replace(",","")));
		cInvoice.setM_sDiscountDesc(cOrder.getM_sPrePostingInvoiceDiscountDesc());

		//Note: this has to come AFTER the invoice date is set:
		//cInvoice.setM_sFiscalYear("0");
		//cInvoice.setM_iFiscalPeriod(0);
		cInvoice.setM_sBillToAddressLine1(cOrder.getM_sBillToAddressLine1());
		cInvoice.setM_sBillToAddressLine2(cOrder.getM_sBillToAddressLine2());
		cInvoice.setM_sBillToAddressLine3(cOrder.getM_sBillToAddressLine3());
		cInvoice.setM_sBillToAddressLine4(cOrder.getM_sBillToAddressLine4());
		cInvoice.setM_sBillToCity(cOrder.getM_sBillToCity());
		cInvoice.setM_sBillToContact(cOrder.getM_sBilltoContact());
		cInvoice.setM_sBillToCountry(cOrder.getM_sBillToCountry());
		cInvoice.setM_sBillToFax(cOrder.getM_sBillToFax());
		cInvoice.setM_sBillToName(cOrder.getM_sBillToName());
		cInvoice.setM_sBillToPhone(cOrder.getM_sBilltoPhone());
		cInvoice.setM_sBillToState(cOrder.getM_sBillToState());
		cInvoice.setM_sBillToZip(cOrder.getM_sBillToZip());
		cInvoice.setM_sCustomerCode(cOrder.getM_sCustomerCode());
		cInvoice.setM_sCustomerControlAcctSet(cOrder.getM_sCustomerControlAcctSet());
		cInvoice.setM_sPONumber(cOrder.getM_sPONumber());
		cInvoice.setM_sInvoiceNumber("");
		cInvoice.setM_sSalesperson(cOrder.getM_sSalesperson());
		cInvoice.setM_sShipToAddress1(cOrder.getM_sShipToAddress1());
		cInvoice.setM_sShipToAddress2(cOrder.getM_sShipToAddress2());
		cInvoice.setM_sShipToAddress3(cOrder.getM_sShipToAddress3());
		cInvoice.setM_sShipToAddress4(cOrder.getM_sShipToAddress4());
		cInvoice.setM_sShipToCity(cOrder.getM_sShipToCity());
		cInvoice.setM_sShipToCode(cOrder.getM_sShipToCode());
		cInvoice.setM_sShipToContact(cOrder.getM_sShiptoContact());
		cInvoice.setM_sShipToCountry(cOrder.getM_sShipToCountry());
		cInvoice.setM_sShipToFax(cOrder.getM_sShipToFax());
		cInvoice.setM_sShipToName(cOrder.getM_sShipToName());
		cInvoice.setM_sShipToPhone(cOrder.getM_sShiptoPhone());
		cInvoice.setM_sShipToState(cOrder.getM_sShipToState());
		cInvoice.setM_sShipToZip(cOrder.getM_sShipToZip());
		cInvoice.setitaxid(cOrder.getitaxid());
		cInvoice.setstaxjurisdiction(cOrder.getstaxjurisdiction());
		cInvoice.setM_iCustomerDiscountLevel(Integer.parseInt(cOrder.getM_iCustomerDiscountLevel()));
		//cInvoice.setM_iTaxClass(0);
		cInvoice.setstaxtype(cOrder.getstaxtype());
		cInvoice.setM_iTransactionType(SMInvoice.TransactionTypeInvoice);
		cInvoice.setM_mInvoiceComments(cOrder.getM_mInvoiceComments());
		
		//System.out.println("[1545229555] - mInvoiceComments = '" + cInvoice.getM_mInvoiceComments());
		
		cInvoice.setM_iOrderSourceID(Integer.parseInt(cOrder.getM_iOrderSourceID()));
		cInvoice.setM_sOrderSourceDesc(cOrder.getM_sOrderSourceDesc());
		cInvoice.setM_iSalesGroup(Integer.parseInt(cOrder.getM_iSalesGroup()));

		//If there's no price list code, put one on it:
		if (cOrder.getM_sDefaultPriceListCode().trim().length() == 0){
			cInvoice.setM_sDefaultPriceListCode("1");
		}else{
			cInvoice.setM_sDefaultPriceListCode(cOrder.getM_sDefaultPriceListCode());
		}
		cInvoice.setM_sDesc("");
		cInvoice.setM_sLocation(cOrder.getM_sLocation());
		cInvoice.setM_sServiceTypeCode(cOrder.getM_sServiceTypeCode());
		cInvoice.setM_sServiceTypeCodeDescription(cOrder.getM_sServiceTypeCodeDescription());
		cInvoice.setM_sShipToCode(cOrder.getM_sShipToCode());
		cInvoice.setM_sShipToFax(cOrder.getM_sShipToFax());

		bdTRate = SMTax.Get_Tax_Rate(cOrder.getitaxid(), conn);
		if (bdTRate.compareTo(BigDecimal.ZERO) < 0){
			throw new Exception ("Failed to get tax rate for order #" + cInvoice.getM_sOrderNumber());
		}
		cInvoice.setM_dTaxRate(bdTRate);

		//Add the details:
		if (bDebugMode){
			System.out.println("[1540839462B] - In Create_Invoice: creating details.");
		}
		for (int i=0;i<cOrder.get_iOrderDetailCount();i++){
			//System.out.println("Processing detail " + i + " in Create_Invoice.");
			//If the detail has items shipped, then create an invoice detail
			if (Double.parseDouble(cOrder.getM_arrOrderDetails().get(i).getM_dQtyShipped().replace(",","")) != 0D){
				//First, populate the invoice detail
				SMOrderDetail cOrdDetail = cOrder.getM_arrOrderDetails().get(i);
				SMInvoiceDetail cInvDetail = new SMInvoiceDetail();
				iInvLineNum++;
				cInvDetail.setM_sDesc(cOrdDetail.getM_sItemDesc());
				cInvDetail.setM_sDetailInvoiceComment(cOrdDetail.getM_mInvoiceComments());
				cInvDetail.setM_iDetailNumber(Integer.parseInt(cOrdDetail.getM_iDetailNumber()));
				cInvDetail.setM_sItemCategory(cOrdDetail.getM_sItemCategory());
				cInvDetail.setM_sLocationCode(cOrdDetail.getM_sLocationCode());
				cInvDetail.setM_iSuppressDetailOnInvoice(Integer.parseInt(cOrdDetail.getM_isuppressdetailoninvoice()));

				try{
					Calculate_ER_GLAcct(cInvDetail.getM_sItemCategory(), conn);
				}catch(Exception e){
					throw new Exception ("Failed to calculate expense/revenue GL account for order #" 
							+ cInvoice.getM_sOrderNumber() + " - " + e.getMessage());
				}
				cInvDetail.setM_sExpenseGLAcct(sExpenseGLAcct);
				cInvDetail.setM_sRevenueGLAcct(sRevenueGLAcct);
				try {
					Calculate_I_GLAcct(cInvDetail.getM_sLocationCode(), conn);
				}catch(Exception e){
					throw new Exception ("Failed to calculate inventory GL account for order #" 
							+ cInvoice.getM_sOrderNumber() + " - " + e.getMessage());
				}
				cInvDetail.setM_sInventoryGLAcct(sInventoryGLAcct);
				cInvDetail.setM_dExtendedPrice(new BigDecimal(cOrdDetail.getM_dExtendedOrderPrice().replace(",", "")));
				cInvDetail.setM_iIsStockItem(Integer.parseInt(cOrdDetail.getM_iIsStockItem()));
				cInvDetail.setM_sItemNumber(cOrdDetail.getM_sItemNumber());
				cInvDetail.setM_iLineNumber(iInvLineNum);
				cInvDetail.setM_dQtyShipped(new BigDecimal(cOrdDetail.getM_dQtyShipped().replace(",", "")));
				cInvDetail.setM_iTaxable(Integer.parseInt(cOrdDetail.getM_iTaxable()));
				cInvDetail.setM_sUnitOfMeasure(cOrdDetail.getM_sOrderUnitOfMeasure().trim());
				cInvDetail.setM_dUnitPrice(new BigDecimal(cOrdDetail.getM_dOrderUnitPrice().replace(",", "")));
				cInvDetail.setM_sDetailInvoiceComment(cOrdDetail.getM_mInvoiceComments());
				cInvDetail.setM_sMechID(cOrdDetail.getM_sMechID());
				cInvDetail.setM_sMechInitial(cOrdDetail.getM_sMechInitial());
				cInvDetail.setM_sMechFullName(cOrdDetail.getM_sMechFullName());
				cInvDetail.setM_sLabel(cOrdDetail.getM_sLabel());
				
				//Get the labor flag from inventory:
				ICItem item = new ICItem(cInvDetail.getM_sItemNumber());
				if (!item.load(conn)){
					System.out.println("Error [1487109513] loading item '" + cInvDetail.getM_sItemNumber()
						+ " - " + item.getErrorMessageString()
					);
				}else{
					cInvDetail.set_iLaborItem(Integer.parseInt(item.getLaborItem()));
				}
				
				cInvoice.Add_Detail(cInvDetail);
			}
		}
		//Set the number of lines on the invoice:
		cInvoice.setM_iNumberOfLinesOnInvoice(iInvLineNum);
		//'Calculate the individual prices AFTER discount:
		if (bDebugMode){
			System.out.println("[1540839462C] - In Create_Invoice - Going to CalculatePriceAfterDiscount.");
		}
		try{
			cInvoice.CalculatePriceAfterDiscount();
		}catch (Exception e){
			throw new Exception ("Failed to calculate price after discount for order #" 
					+ cInvoice.getM_sOrderNumber() + " - " + e.getMessage());
		}

		//'Calculate the tax for this invoice - this calculates the tax base and the tax amount:
		if (bDebugMode){
			System.out.println("[1540839462D] - In Create_Invoice - Going to CalculateInvoiceTaxAmount.");
		}
		try {
			cInvoice.CalculateInvoiceTaxAmount(cOrder.getitaxid(), conn);
		} catch (Exception e) {
			throw new Exception ("Failed to calculate invoice sales tax amount for order #" + cInvoice.getM_sOrderNumber() + " - " + e.getMessage());
		}
		clsDatabaseFunctions.start_data_transaction(conn);
		if (bDebugMode){
			System.out.println("[1540839462E] - In Create_Invoice - Going into cInvoice.Save_Invoice. . .");
		}
		try{
			cInvoice.Save_Invoice(conn, sUserID, sUserFullName);
		}catch(Exception ex){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception ("Failed to save invoice for order #" + cInvoice.getM_sOrderNumber() + " "
					+ ex.getMessage());
		}
		if (bDebugMode){
			System.out.println("[1540839462F] - In Create_Invoice - saving invoice successful.");
		}

		//'Since several fields in the order are being changed, save the order too
		//'The discounts get re-set here so that they are not accidentally used on the next invoice:
		cOrder.setM_dPrePostingInvoiceDiscountAmount("0");
		cOrder.setM_dPrePostingInvoiceDiscountPercentage("0");
		cOrder.setM_sPrePostingInvoiceDiscountDesc("");
		cOrder.setM_sLastInvoiceNumber(cInvoice.getM_sInvoiceNumber());
		cOrder.setM_datLastPostingDate(sdfNormalDate.format(new Date(System.currentTimeMillis())));
		cOrder.setM_iNumberOfInvoices(String.valueOf(Integer.parseInt(cOrder.getM_iNumberOfInvoices()) + 1));

		//Go back and update the lines on the order:
		for (int i=0;i<cOrder.get_iOrderDetailCount();i++){
			//If the detail has items shipped, then there would be an invoice detail:
			//TJR - 2/19/2014 - special debugging lines:
			
			//if (sUserName.compareToIgnoreCase("new11") == 0){
			//	log.writeEntry(
			//			sUserName,
			//			"UPDATEINVLINE01",
			//			cOrder.getM_strimmedordernumber() + " - " + sDebugMarker,
			//			" cOrder.getM_arrOrderDetails().get(i).getM_sItemNumber() = " + cOrder.getM_arrOrderDetails().get(i).getM_sItemNumber()
			//			+ ", cOrder.getM_arrOrderDetails().get(i).getM_dQtyOrdered() = " + cOrder.getM_arrOrderDetails().get(i).getM_dQtyOrdered()
			//			+ ", cOrder.getM_arrOrderDetails().get(i).getM_dQtyShipped() = " + cOrder.getM_arrOrderDetails().get(i).getM_dQtyShipped()
			//			+ ", cOrder.getM_arrOrderDetails().get(i).getM_dQtyShippedToDate() = " + cOrder.getM_arrOrderDetails().get(i).getM_dQtyShippedToDate(),
			//			"[1392847060]"
			//			);
			//}
			
			if (Double.parseDouble(cOrder.getM_arrOrderDetails().get(i).getM_dQtyShipped().replace(",", "")) != 0D){
				//if (sUserName.compareToIgnoreCase("new11") == 0){
				//	log.writeEntry(
				//			sUserName,
				//			"UPDATEINVLINE02",
				//			cOrder.getM_strimmedordernumber() + " - " + sDebugMarker,
				//			"Qty shipped != 0.00",
				//			"[1392847061]"
				//			);
				//}

				//Update the line on the order:
				//Add the qty shipped to the qty shipped to date:
				cOrder.getM_arrOrderDetails().get(i).setM_dQtyShippedToDate(Double.toString(
						Double.parseDouble(cOrder.getM_arrOrderDetails().get(i).getM_dQtyShippedToDate().replace(",", "")) +
						Double.parseDouble(cOrder.getM_arrOrderDetails().get(i).getM_dQtyShipped().replace(",", ""))));

				//Now take the shipped items out of the qty ordered:
				cOrder.Upgrade_Qty_Ordered(i);

				//And finally set the qty shipped to zero:
				cOrder.getM_arrOrderDetails().get(i).setM_dQtyShipped("0");
				cOrder.getM_arrOrderDetails().get(i).setM_dExtendedOrderPrice("0");
				//cOrder.getM_arrOrderDetails().get(i).setM_dExtendedPriceAfterDiscount("0");
				
				//if (sUserName.compareToIgnoreCase("new11") == 0){
				//	log.writeEntry(
				//			sUserName,
				//			"UPDATEINVLINE03",
				//			cOrder.getM_strimmedordernumber() + " - " + sDebugMarker,
				//			" cOrder.getM_arrOrderDetails().get(i).getM_sItemNumber() = " + cOrder.getM_arrOrderDetails().get(i).getM_sItemNumber()
				//			+ ", cOrder.getM_arrOrderDetails().get(i).getM_dQtyOrdered() = " + cOrder.getM_arrOrderDetails().get(i).getM_dQtyOrdered()
				//			+ ", cOrder.getM_arrOrderDetails().get(i).getM_dQtyShipped() = " + cOrder.getM_arrOrderDetails().get(i).getM_dQtyShipped()
				//			+ ", cOrder.getM_arrOrderDetails().get(i).getM_dQtyShippedToDate() = " + cOrder.getM_arrOrderDetails().get(i).getM_dQtyShippedToDate(),
				//			"[1392847062]"
				//			);
				//}
				
			}
		}
		if (bDebugMode){
			System.out.println("[1540839462G] - In Create_Invoice - Going into cInvoice.Save_Order in Create_Invoice");
		}

		if (!cOrder.save_order_unprotected_by_transaction(conn, 
				sDBID,
				context,
				sUserID, 
				sUserFullName,
				false,
				"INVOICEDORDER")){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			if (bDebugMode){
				System.out.println("[1540839462H] - Unable to restore quantities on order.<BR>"
						+ cOrder.getErrorMessages());
			}
			throw new Exception("Unable to restore quantities on order.<BR>"
					+ cOrder.getErrorMessages());
		}
		if (bDebugMode){
			System.out.println("[1540839462I] - In Create_Invoice - cInvoice.Save_Order in Create_Invoice is successful.");
		}

		//TJR - 11/30/09 - added code to update ordermgrcomments table:
		String SQL = "UPDATE " + SMTableordermgrcomments.TableName + " SET"
				+ " " + SMTableordermgrcomments.sinvoicenumber + " = '" + cInvoice.getM_sInvoiceNumber() + "'"
				+ " WHERE"
				//TJROC
				+ " " + SMTableordermgrcomments.sordernumber + " = '" + cOrder.getM_strimmedordernumber() + "'";
		try{
			clsDatabaseFunctions.executeSQL(SQL, conn);
		}catch (SQLException ex){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error updating ordermgrcomments table when updating order status after invoicing!!"
					+ "<BR>" + ex.getMessage()
					+ "<BR>SQL: " + ex.getSQLState());
		}

		//last check to make sure invoice has lines.
		SQL = "SELECT * FROM "
				+ SMTableinvoicedetails.TableName
				+ " WHERE (" 
				+ "(" + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber + ") = "
				+ "'" + clsStringFunctions.PadLeft(cInvoice.getM_sInvoiceNumber().trim(), " ", 8) + "'"
				+ ")"	
				;

		ResultSet rsInvoiceDetails = clsDatabaseFunctions.openResultSet(SQL, conn);
		if (!rsInvoiceDetails.next()){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Invoice #" + cInvoice.getM_sInvoiceNumber() + " has no detail lines.");
		}

        //after invoice is created, reset deposit on order.
        String sSQL = "UPDATE " + SMTableorderheaders.TableName + 
        				" SET" +
        				" " + SMTableorderheaders.bddepositamount + " = 0" +
        				" WHERE" +
        				" " + SMTableorderheaders.strimmedordernumber + " = '" + cOrder.getM_sOrderNumber().trim() + "'";
        try{
        	clsDatabaseFunctions.executeSQL(sSQL, conn);
        }catch(SQLException ex){
        	if (bDebugMode){
        		System.out.println("[1540839462J] - Resetting order deposit amount failed.<BR>"
        	  		+ ex.getMessage());
        	}
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Resetting order deposit amount failed.<BR>" + ex.getMessage());
        }
		
		clsDatabaseFunctions.commit_data_transaction(conn);
		return;

	}
	public static void Calculate_Invoice_Due_Date(String sTermCode,
			Date datDocDate,
			Connection conn) throws SQLException{

		String sSQL = "";
		Date datTempDate = new Date(System.currentTimeMillis()); // Just using as an intermediate variable in ugly calculations
		Calendar calDocDate = Calendar.getInstance();
		calDocDate.setTimeInMillis(datDocDate.getTime());

		//Initialize the terms/discount factors:
		datDueDate = new Date(datDocDate.getTime());
		datDiscountDate = new Date(datDocDate.getTime());
		dDiscountPercentage = 0;

		try{
			sSQL = "SELECT" 
					+ " " + SMTablearterms.sTermsCode
					+ ", " + SMTablearterms.dDiscountPercent
					+ ", " + SMTablearterms.iDiscountNumberOfDays +
					", " + SMTablearterms.iDiscountDayOfTheMonth +
					", " + SMTablearterms.iDueNumberOfDays +
					", " + SMTablearterms.iDueDayOfTheMonth +
					" FROM" + 
					" " + SMTablearterms.TableName +	
					" WHERE" +
					" " + SMTablearterms.sTermsCode + " = '" + sTermCode + "'";
			ResultSet rsTerm = clsDatabaseFunctions.openResultSet(sSQL, conn);

			if (!rsTerm.next()){
				rsTerm.close();
				throw new SQLException("No terms record available for TermCode " + sTermCode + ".");
			}

			//Here's the logic to populate the terms/discount factors:
			//First, get the document (invoice) date into a Date variable:

			//Next, calculate the due date:
			if (rsTerm.getInt(SMTablearterms.iDueNumberOfDays) != 0){
				calDocDate.add(Calendar.DAY_OF_MONTH, rsTerm.getInt(SMTablearterms.iDueNumberOfDays));
				datDueDate.setTime(calDocDate.getTimeInMillis());
			}else if (rsTerm.getInt(SMTablearterms.iDueDayOfTheMonth) != 0){
				//Otherwise, calculate using the next due date
				//dARTERMBSDueDayOfTheMonth
				if (calDocDate.get(Calendar.DAY_OF_MONTH) > rsTerm.getInt(SMTablearterms.iDueDayOfTheMonth)){
					//Advance the invoice date by one month
					calDocDate.add(Calendar.MONTH, 1);
					calDocDate.set(Calendar.DAY_OF_MONTH, rsTerm.getInt(SMTablearterms.iDueDayOfTheMonth));
					datTempDate = new Date(calDocDate.getTimeInMillis());
				}else{               
					calDocDate.set(Calendar.DAY_OF_MONTH, rsTerm.getInt(SMTablearterms.iDueDayOfTheMonth));
					datTempDate = new Date(calDocDate.getTimeInMillis());
				}
				datDueDate.setTime(datTempDate.getTime());
			}

			//If there is a discount amount . . .
			if (rsTerm.getDouble(SMTablearterms.dDiscountPercent) != 0){
				//There is a discount to be calculated
				//If there is a discount number of days for the discount . . .
				if (rsTerm.getInt(SMTablearterms.iDiscountNumberOfDays) > 0){
					//Use the discount number of days to calculate the due date

					//Calculate the due date by adding the discount number of days to the
					//invoice date:
					calDocDate.add(Calendar.DAY_OF_MONTH, rsTerm.getInt(SMTablearterms.iDiscountNumberOfDays));
					datDiscountDate.setTime(calDocDate.getTimeInMillis());
				}else{
					//Otherwise, calculate using the next discount date
					if (calDocDate.get(Calendar.DAY_OF_MONTH) > rsTerm.getInt(SMTablearterms.iDiscountDayOfTheMonth)){
						//Advance the invoice date by one month
						calDocDate.add(Calendar.MONTH, 1);
						calDocDate.set(Calendar.DAY_OF_MONTH, rsTerm.getInt(SMTablearterms.iDiscountDayOfTheMonth));
						datTempDate = new Date(calDocDate.getTimeInMillis());
					}else{
						calDocDate.set(Calendar.DAY_OF_MONTH, rsTerm.getInt(SMTablearterms.iDiscountDayOfTheMonth));
						datTempDate = new Date(calDocDate.getTimeInMillis());
					}
					datDiscountDate.setTime(datTempDate.getTime());
				}
			}else{
				datDiscountDate.setTime(datDocDate.getTime());
			}

			dDiscountPercentage = rsTerm.getDouble(SMTablearterms.dDiscountPercent);
			rsTerm.close();

		}catch (SQLException ex){
			throw new SQLException("Error when calculating due date and discount date when creating invoice - "
					+ ex.getMessage() + "."
					);
		}
	}
	public static void Calculate_ER_GLAcct(String sItemCategory, Connection conn) throws Exception{

		String SQL = "SELECT * FROM " + SMTableiccategories.TableName
				+ " WHERE"
				+ " " + SMTableiccategories.sCategoryCode + " = '" + sItemCategory + "'";

		try{
			ResultSet rsCategory = clsDatabaseFunctions.openResultSet(SQL, conn);		    
			if (rsCategory.next()){
				sExpenseGLAcct = rsCategory.getString(SMTableiccategories.sCostofGoodsSoldAccount);
				sRevenueGLAcct = rsCategory.getString(SMTableiccategories.sSalesAccount);
			}
			rsCategory.close();
		}catch (SQLException ex){
			throw new Exception("Error in Calculate_ER_GLAcct: " + ex.getMessage());
		}
	}

	public static void Calculate_I_GLAcct(String sItemLocation, Connection conn) throws Exception{

		String SQL = "SELECT * FROM " + SMTablelocations.TableName
				+ " WHERE"
				+ " " + SMTablelocations.sLocation + " = '" + sItemLocation + "'";

		try{
			ResultSet rsLocation = clsDatabaseFunctions.openResultSet(SQL, conn);		    
			if (rsLocation.next()){
				sInventoryGLAcct = rsLocation.getString(SMTablelocations.sGLInventoryAcct);
			}
			rsLocation.close();
		}catch (SQLException ex){
			throw new Exception("Error in Calculate_I_GLAcct: " + ex.getMessage());
		}
	}
	/* - TJR - 1/27/2014 - This is being done in the 'validate_order_for_invoicing' function called previously
	private static void Validate_Order_For_Invoicing(SMOrderHeader cOrder, Connection conn) throws Exception{

		//Validate each line on the order:
		try{
			for (int i=0;i<cOrder.get_iOrderDetailCount();i++){
				if (Double.parseDouble(cOrder.getM_arrOrderDetails().get(i).getM_dQtyShipped().replace(",","")) != 0){
					try{
						Validate_Detail_Line(cOrder.getM_arrOrderDetails().get(i), conn);
					}catch (Exception e){
						throw new Exception("<BR>Can't validate order detail #" + i + " - " + e.getMessage());
					}
				}
			}
		}catch(Exception e){
			throw new Exception("<BR>Error in Validate_Order_For_Invoicing(): " + e.getMessage());
		}
	}

	private static void Validate_Detail_Line(SMOrderDetail cDetail, Connection conn) throws Exception {
		String sSQL = "";

		try{
			sSQL = "SELECT * FROM " + SMTableicitems.TableName
					+ " WHERE ("
					+ SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + " = '" 
					+ cDetail.getM_sItemNumber() + "'"
					+ ")";

			ResultSet rsItem = SMUtilities.openResultSet(sSQL, conn);
			if (rsItem.next()){
				//STEP 2: Check to see if the item is inactive or not.
				if (rsItem.getInt(SMTableicitems.TableName + "." + SMTableicitems.iActive) == 1){
					//STEP 3: Check to see if the UOM used in order matches the UOM used in icitems table 
					if (cDetail.getM_sOrderUnitOfMeasure().trim().compareTo(rsItem.getString(
							SMTableicitems.TableName + "." + SMTableicitems.sCostUnitOfMeasure).trim()) == 0){
						rsItem.close();	
						return;
					}else{
						rsItem.close();	
						throw new Exception("item " + cDetail.getM_sItemNumber() + " has an incorrect unit of measure.");
					}
				}else{
					rsItem.close();	
					throw new Exception("item " + cDetail.getM_sItemNumber() + " is not active.");
				}
			}else{
				rsItem.close();	
				throw new Exception("item " + cDetail.getM_sItemNumber() + " was not found in inventory.");
			}
		}catch(SQLException ex){
			throw new Exception("SQL Error validating invoice line - " + ex.getMessage());
		}
	}
	*/
}

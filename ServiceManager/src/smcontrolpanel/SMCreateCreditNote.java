package smcontrolpanel;

import java.math.BigDecimal;
import java.sql.Connection;
//import java.sql.Date;
import java.text.SimpleDateFormat;

import javax.servlet.ServletContext;

import SMClasses.SMInvoice;
import SMClasses.SMInvoiceDetail;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMTableinvoiceheaders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;

public class SMCreateCreditNote extends java.lang.Object{

	private static boolean bDebugMode = false;
	public static SimpleDateFormat sdfTime = new SimpleDateFormat("hhmmss");
	public static SimpleDateFormat sdfNormalDate = new SimpleDateFormat("MM/dd/yyyy");

	public String Create_Credit_Note(String sInvoiceNumber, 
								   String sCreditNoteDate, 
								   String sCreditNoteInfo, 
								   String sUserID,
								   String sUserFullName,
								   Connection conn, 
								   String sConf,
								   ServletContext context) 
							throws Exception{
		//int iInvLineNum = 0;
		//double dTRate;

		//Load invoice
		//SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		
		SMInvoice cInvoice = new SMInvoice();
		if (bDebugMode){
			System.out.println("Creating credit note for invoice# " + sInvoiceNumber);
		}
		cInvoice.setM_sInvoiceNumber(sInvoiceNumber);
		if (!cInvoice.load(conn)){
			throw new Exception("Failed to load invoice.<BR>" + 
								cInvoice.getErrorMessage());
		}

		//invoice can only be credited once, if it's already been credited, then inform user and exit.
        if (cInvoice.getM_iIsCredited() == 1){
            throw new Exception("This invoice has been credited already and cannot be credited again.");
        }
        
        //Can't credit an invoice which hasn't been processed through IC first:
        if (cInvoice.getM_iDayEndNumber() == 0){
            throw new Exception("Costing information for this invoice (number " 
            	+ cInvoice.getM_sInvoiceNumber().trim() + ") has not been processed through the Inventory Control "
            	+ "day end process - it cannot be credited yet.");
        }
		
        //Can't credit an invoice which hasn't been run through AR yet, either:
        if (cInvoice.getM_iExportedToAR() == 0){
            throw new Exception("This invoice (number " 
            	+ cInvoice.getM_sInvoiceNumber().trim() 
            	+ ") has not been posted to Accounts Receivable - it cannot be credited yet.");
        }
        
		//save original line numbers as matching line numbers in details
		for (int i=0;i<cInvoice.getM_iNumberOfLinesOnInvoice();i++){
			cInvoice.getDetailByIndex(i).setM_iMatchingInvoiceLineNumber(cInvoice.getDetailByIndex(i).getM_iLineNumber());
		}
		
        /*************************************
         * Here, we take the current invoice loaded into the invoice 
         * header class, and make the changes to it to turn it into 
         * a credit note.  Then we'll save this invoice, but it will 
         * be saved as a credit note:
         *************************************/
        
		//set transaction type to "Credit"
        cInvoice.setM_iTransactionType(SMInvoice.TransactionTypeCredit);
        //set Current InvoiceNumber as MatchingInvoiceNumber
        cInvoice.setM_sMatchingInvoiceNumber(cInvoice.getM_sInvoiceNumber());
        
        //If there's no price list code, put one on it:
        if (cInvoice.getM_sDefaultPriceListCode().trim() == ""){
        	cInvoice.setM_sDefaultPriceListCode("1");
        }
        cInvoice.setM_datInvoiceDate(clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", sCreditNoteDate));
        cInvoice.setM_datDueDate(cInvoice.getM_datInvoiceDate());
        cInvoice.setM_datTermsDiscountDate(cInvoice.getM_datInvoiceDate());
        cInvoice.setM_sCreatedByFullName(sUserFullName);
        cInvoice.setM_lCreatedByID(sUserID);
        //.dLineTotalAmount
        //.dNetTotalAmount
        cInvoice.setM_dPrePayment(BigDecimal.ZERO);
        cInvoice.setbdsalestaxamount(cInvoice.getbdsalestaxamount().negate());
        cInvoice.setbdsalestaxbase(cInvoice.getbdsalestaxbase().negate());
        cInvoice.setM_dTermsDiscountAvailable(BigDecimal.ZERO);
        cInvoice.setM_dTermsDiscountPercentage(BigDecimal.ZERO);
        //.dTotalAmountDue
        cInvoice.setM_iExportedToAR(0);
        cInvoice.setM_iExportedToIC(0);
        cInvoice.setM_iDayEndNumber(0);
        //cInvoice.setM_iDayEndPrinted(0);
        cInvoice.setM_dDiscountAmount(cInvoice.getM_dDiscountAmount().negate());
        
        //reason for credit note is saved in "invoice note" field
        cInvoice.setM_mInvoiceComments(sCreditNoteInfo);
        
        //We only need fiscal period info for use with ACCPAC AR:
        //cInvoice.setM_sFiscalYear("0");
        //cInvoice.setM_iFiscalPeriod(0);
        //cInvoice.setM_iPrintStatus(0); //set the print status to "not printed".
        
        //This will be set when the invoice is saved:
        cInvoice.setM_sInvoiceNumber("");
    
	    for (int i=0;i<cInvoice.getM_iNumberOfLinesOnInvoice();i++){
	        SMInvoiceDetail cDetail = cInvoice.getDetailByIndex(i);
            //reverse numbers to indicate returns
            cDetail.setM_dExtendedPriceAfterDiscount(cDetail.getM_dExtendedPriceAfterDiscount().negate());
            cDetail.setM_dExtendedCost(cDetail.getM_dExtendedCost().negate());
            cDetail.setM_dExtendedPrice(cDetail.getM_dExtendedPrice().negate());
            cDetail.setbdLineSalesTaxAmount(cDetail.getbdLineSalesTaxAmount().negate());
            //right now this is set to 1, means always return
            //cDetail.setM_iReturnToInventory(1);
            //set Current LineNumber as MatchingInvoiceLineNumber
            cDetail.setM_iMatchingInvoiceLineNumber(cDetail.getM_iLineNumber());
            cDetail.setM_dUnitPrice(cDetail.getM_dUnitPrice().negate());
	    }
	    
	    //TRANSACTION STARTS
		clsDatabaseFunctions.start_data_transaction(conn);
		if (bDebugMode){
			System.out.println("In Create_Credit_Note - Going into cInvoice.Save_Invoice. . .");
		}
		try{
			//here we save the invoice as a new credit note.
			cInvoice.Save_Invoice(conn, sUserID, sUserFullName);
		}catch(Exception ex){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception ("Failed to save credit note for invoice #" + sInvoiceNumber + " "
					+ ex.getMessage());
		}
		if (bDebugMode){
			System.out.println("In Create_Credit_Note - saving invoice successful.");
		}
		
	    //Update the invoice to indicate that it has been credited:
		try{
			clsDatabaseFunctions.executeSQL(Update_Invoice_As_Credited(sInvoiceNumber), conn);
		}catch(Exception ex){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception ("Failed to update invoice to 'Invoiced' status."
					+ ex.getMessage());
		}
		if (bDebugMode){
			System.out.println("In Create_Credit_Note - updating invoice status successful.");
		}
		
	    //modify corresponding order here:
	    try{ 
	    	Update_Order_Info_After_Credit(cInvoice, 
	    								   conn, 
	    								   sConf,
	    								   context,
	    								   sUserID,
	    								   sUserFullName);
	    }catch(Exception ex){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception ("Failed to update order #" + cInvoice.getM_sOrderNumber() + " after creation of credit note."
					+ ex.getMessage());
	    }
		if (bDebugMode){
			System.out.println("In Create_Credit_Note - updating order information successful.");
		}
		
		//TRANSACTION ENDS
		clsDatabaseFunctions.commit_data_transaction(conn);
		return cInvoice.getM_sInvoiceNumber();
	}
	
	private String Update_Invoice_As_Credited(String sInvoiceNumber){
		
		String SQL =  "UPDATE"+
					  	" " + SMTableinvoiceheaders.TableName + 
					  " SET" +
						" " + SMTableinvoiceheaders.iIsCredited + " = 1" +
					  " WHERE" + 
						" " + SMTableinvoiceheaders.sInvoiceNumber + " = '" + sInvoiceNumber + "'";
		if (bDebugMode){
			System.out.println("updating invoice status SQL:");
			System.out.println(SQL);
		}
		return SQL;
	}
	
	private void Update_Order_Info_After_Credit(SMInvoice cCreditNote, //this invoice has become credit note already
												Connection conn,
												String sConf,
												ServletContext context,
												String sUserID,
												String sUserFullName) throws Exception{
		
	    SMOrderHeader cOrder = new SMOrderHeader();
	       
	    cOrder.setM_sOrderNumber(cCreditNote.getM_sOrderNumber());
	    try{
	    	cOrder.load(conn);
	    }catch(Exception e){
	    	throw new Exception("Error loading order for updating credit note info." + 
	    			" " + cOrder.getErrorMessages());
	    }
	    
	    //modify the order here:
	    for (int i=0;i<cCreditNote.getM_iNumberOfLinesOnInvoice();i++){
	        for (int j=0;j<Integer.parseInt(cOrder.getM_iNumberOfLinesOnOrder());j++){
	            
	            //The detail number is the link between an order line and an invoice line:
	            if (cCreditNote.getDetailByIndex(i).getM_iDetailNumber() ==  
	            	Integer.parseInt(cOrder.getM_arrOrderDetails().get(j).getM_iDetailNumber())){
	                
	            		//return the Qty.shipped back into order
	            		cOrder.getM_arrOrderDetails().get(j).setM_dQtyOrdered(
            				clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(
	            				new BigDecimal(cOrder.getM_arrOrderDetails().get(j).getM_dQtyOrdered().replace(",", "")).add(
	            					cCreditNote.getDetailByIndex(i).getM_dQtyShipped()
	            				)
		            		)
		            	);
	            		//reset extended order price
	            		cOrder.getM_arrOrderDetails().get(j).setM_dExtendedOrderPrice(
            				clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(
	            				new BigDecimal(cOrder.getM_arrOrderDetails().get(j).getM_dQtyShipped().replace(",", "")).multiply( 
	            					new BigDecimal(cOrder.getM_arrOrderDetails().get(j).getM_dOrderUnitPrice().replace(",", ""))
	            				)
	            			)
		            	);
	            		/*cOrder.getM_arrOrderDetails().get(j).setM_dExtendedPriceAfterDiscount(
	            			SMUtilities.BigDecimalTo2DecimalSQLFormat(
		            			new BigDecimal(cOrder.getM_arrOrderDetails().get(j).getM_dExtendedPriceAfterDiscount().replace(",", "")).subtract(
		            				cCreditNote.getDetailByIndex(i).getM_dExtendedPriceAfterDiscount()
		            			)
	            			)
	                    );*/
	            		cOrder.getM_arrOrderDetails().get(j).setM_dQtyShippedToDate(
		            			clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(
				            		new BigDecimal(cOrder.getM_arrOrderDetails().get(j).getM_dQtyShippedToDate().replace(",", "")).subtract(
				            			cCreditNote.getDetailByIndex(i).getM_dQtyShipped()
				            		)
				            	)
		            		);
	                    //cOrder.getM_arrOrderDetails().get(j).setM_iOrderComplete("0");
	                    break;
	            }
	        } //end for j
	    } //end for i
	    
	    //save credited order back to database
	    if (!cOrder.save_order_unprotected_by_transaction(conn, 
	    												  sConf,
	    												  context,
	    												  sUserID,
	    												  sUserFullName,
	    												  false, 
	    												  "Creating_Credit_Note")){
	        throw new Exception("Failed to save order after credit note. " + 
	        					cOrder.getErrorMessages());
	    }	    
	}
}

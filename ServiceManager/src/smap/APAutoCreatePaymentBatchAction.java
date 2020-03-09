package smap;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ServletUtilities.clsDatabaseFunctions;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class APAutoCreatePaymentBatchAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	
	private static int MAX_ERROR_LENGTH = 1024;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.APEditBatches)){return;}
		
		//Get parameters to create the batch:
		String sPaymentDate = request.getParameter(APCreatePaymentsReportEdit.PARAM_PAYMENT_DATE);
		String sBatchDate = request.getParameter(APCreatePaymentsReportEdit.PARAM_BATCH_DATE);
		String sBankID = request.getParameter(APCreatePaymentsReportEdit.PARAM_BANK_ID);
		String sSelectDocumentsBy = request.getParameter(APCreatePaymentsReportEdit.PARAM_SELECT_DOCUMENTS_BY);
		String sDueDate = request.getParameter(APCreatePaymentsReportEdit.PARAM_DUE_DATE);
		String sStartingDiscountDate = request.getParameter(APCreatePaymentsReportEdit.PARAM_STARTING_DISCOUNT_DATE);
		String sEndingDiscountDate = request.getParameter(APCreatePaymentsReportEdit.PARAM_ENDING_DISCOUNT_DATE);
		String sStartingVendorGroupName = request.getParameter(APCreatePaymentsReportEdit.PARAM_STARTING_VENDOR_GROUP_NAME);
		String sEndingVendorGroupName = request.getParameter(APCreatePaymentsReportEdit.PARAM_ENDING_VENDOR_GROUP_NAME);
		String sStartingAcctSetName = request.getParameter(APCreatePaymentsReportEdit.PARAM_STARTING_ACCOUNT_SET_NAME);
		String sEndingAcctSetName = request.getParameter(APCreatePaymentsReportEdit.PARAM_ENDING_ACCOUNT_SET_NAME);
		String sStartingInvoiceAmt = request.getParameter(APCreatePaymentsReportEdit.PARAM_STARTING_INVOICE_AMT);
		String sEndingInvoiceAmt = request.getParameter(APCreatePaymentsReportEdit.PARAM_ENDING_INVOICE_AMT);
		String sStartingVendor = request.getParameter(APCreatePaymentsReportEdit.PARAM_STARTING_VENDOR);
		String sEndingVendor = request.getParameter(APCreatePaymentsReportEdit.PARAM_ENDING_VENDOR);

		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				smaction.getsDBID(), 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() + " - user: " + smaction.getUserName())
			);
		} catch (Exception e1) {
			String sWarning = "Failed to get data connection - " + e1.getMessage();
			if (sWarning.length() > MAX_ERROR_LENGTH){
				sWarning = sWarning.substring(0, MAX_ERROR_LENGTH) + "...";
			}
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smap.APEditBatchesSelect" + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			);
			return;
		}
		
		APAutoCreatePaymentBatch createbatch = new APAutoCreatePaymentBatch();
		APBatch autopaymentbatch = null;
		try {
			autopaymentbatch = createbatch.createPaymentBatch (
				conn,
				sPaymentDate,
				sBatchDate,
				sBankID,
				sSelectDocumentsBy,
				sDueDate,
				sStartingDiscountDate,
				sEndingDiscountDate,
				sStartingVendorGroupName,
				sEndingVendorGroupName,
				sStartingAcctSetName,
				sEndingAcctSetName,
				sStartingInvoiceAmt,
				sEndingInvoiceAmt,
				sStartingVendor,
				sEndingVendor,
				smaction.getUserID(),
				smaction.getsDBID(),
				getServletContext()
				)
			;
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1546998938]");
			String sWarning = "Failed to create payment batch - " + e.getMessage();
			if (sWarning.length() > MAX_ERROR_LENGTH){
				sWarning = sWarning.substring(0, MAX_ERROR_LENGTH) + "...";
			}
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smap.APEditBatchesSelect" + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			);
			return;
		}
		
		//Now try to save the batch:
		try {
			autopaymentbatch.save_with_data_transaction(getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName(), false);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1546998939]");
			String sWarning = SMUtilities.URLEncode("Could not SAVE payment batch - " + e.getMessage());
			if (sWarning.length() > MAX_ERROR_LENGTH){
				sWarning = sWarning.substring(0, MAX_ERROR_LENGTH) + "...";
			}
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smap.APEditBatchesSelect" + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			);
			return;

		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1546998940]");
		response.sendRedirect(
			"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smap.APEditBatchesSelect" + "?"
			+ "Status=" + "Successfully created payment batch " + autopaymentbatch.getsbatchnumber() + "."
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
		);
		return;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
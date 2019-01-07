package smar;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMUtilities;

public class ARAddEntry extends HttpServlet {

	private static final long serialVersionUID = 1L;
	/*
	 * Parameters in:
	 * BatchNumber - number of the batch to which this entry will be added
	 * CustomerNumber - null if it's being called for the first time, but NOT null if it's
	 * 		being called from the finder, or if it's being RE-called because the customer
	 * 		was not valid
	 * BatchType
	 * DocumentType
	 * Warning - this gets printed out to the page - if an invalid customer is entered,
	 * 		this page gets re-displayed, but with this warning.
	 *
	 * 	 * Parameters out:
	 * BatchNumber - number of the batch to which this entry will be added
	 * EntryNumber - normally -1 to indicate a new entry
	 * Editable - normally Yes, because it's going to be a new entry
	 * CustomerNumber - null if it's being called for the first time, but NOT null if it's
	 * 		being called from the finder, or if it's being RE-called because the customer
	 * 		was not valid
	 * BatchType
	 * Warning - this gets printed out to the page - if an invalid customer is entered,
	 * 		this page gets re-displayed, but with this warning.
	 * DocumentType - user selected
	 * SubmitEdit - value = 'Add entry for this customer'
	 * 
	 */
	private static String sDBID = "";
	private static String sCompanyName = "";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		//This page just accepts a customer number, and validates it or returns to itself . . . 
		
		PrintWriter out = response.getWriter();
	    if (!SMAuthenticate.authenticateSMCPCredentials(
	    	request, 
	    	response, 
	    	getServletContext(), 
	    	-1L)){
	    	return;
	    }
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    //Get the variables for the class:
	    String sBatchNumber = (String) request.getParameter("BatchNumber");
	    //Always -1 for a new entry:
	    String sEntryNumber = "-1";
	    String sEditable = "Yes";
	    String sCustomerNumber = "";
	    
	    if (request.getParameter("CustomerNumber") != null){
	    	sCustomerNumber = request.getParameter("CustomerNumber");
	    }
	    
	    String sBatchType = request.getParameter("BatchType");
	    String sWarning = "";
	    if (request.getParameter("Warning") != null){
	    	sWarning = request.getParameter("Warning");
	    }
	    String sDocumentType = request.getParameter("DocumentType");
	    int iDocType = 0;
		try {
			iDocType = Integer.parseInt(sDocumentType);
		} catch (NumberFormatException e) {
			out.println("<BR>Error [1418853360] - could not read document type from '" + sDocumentType + "'" + e.getMessage() + ".<BR>");
		}
	    String title = "Create new " + ARDocumentTypes.Get_Document_Type_Label(iDocType) + " entry";
	    String subtitle = sWarning;
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Receivable Main Menu</A><BR><BR>");
	    
	    //Set the name of the class which will handle the processing of the entry, depending on the doc type:
	    String sEntryProcessorClass = "";
	    // NOTE: Misc. adjustments are not handled by this class:	    
	    //If it's a new invoice:
	    if (sDocumentType.equalsIgnoreCase(ARDocumentTypes.INVOICE_STRING)){
	    	sEntryProcessorClass = "AREditInvoiceEntry";
	    }
	    
	    if (sDocumentType.equalsIgnoreCase(ARDocumentTypes.CREDIT_STRING)){
	    	sEntryProcessorClass = "AREditInvoiceEntry";
	    }
	    
	    // . . . a cash receipt:
	    else if(sDocumentType.equalsIgnoreCase(ARDocumentTypes.RECEIPT_STRING)){
	    	sEntryProcessorClass = "AREditCashEntry";
	    }
	    // . . . a cash prepay:
	    else if(sDocumentType.equalsIgnoreCase(ARDocumentTypes.PREPAYMENT_STRING)){
	    	sEntryProcessorClass = "AREditCashEntry";
	    }
	    // . . . a misc. receipt:
	    else if(sDocumentType.equalsIgnoreCase(ARDocumentTypes.MISCRECEIPT_STRING)){
	    	sEntryProcessorClass = "AREditMiscReceiptEntry";
	    }
	    // . . . an apply-to:
	    else if(sDocumentType.equalsIgnoreCase(ARDocumentTypes.APPLYTO_STRING)){
	    	sEntryProcessorClass = "AREditApplyToEntry";
	    }
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smar." 
	    		+ sEntryProcessorClass 
	    		+ "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"BatchNumber\" VALUE=\"" + sBatchNumber + "\">");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"EntryNumber\" VALUE=\"" + sEntryNumber + "\">");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"Editable\" VALUE=\"" + sEditable + "\">");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"BatchType\" VALUE=\"" + sBatchType + "\">");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"DocumentType\" VALUE=\"" + sDocumentType + "\">");
	    
	    //If we are dealing with invoices, credits, receipts, prepayments, we'll need customer information:
	    if (! (sDocumentType.equalsIgnoreCase(Integer.toString(ARDocumentTypes.MISCRECEIPT)))
	    	){
		    out.println(
				"<P>Enter customer number: <INPUT TYPE=TEXT NAME=\"" 
				+ "CustomerNumber" + "\""
				+ " VALUE = \"" + sCustomerNumber + "\""
				+ " SIZE=28 MAXLENGTH=" 
				+ Integer.toString(SMTableentries.spayeepayorLength) 
				+ " STYLE=\"width: 2.41in; height: 0.25in\">"
				);
	
			//Link to finder:
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
				+ "?ObjectName=Customer"
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&ResultClass=FinderResults"
				+ "&SearchingClass=smar.ARAddEntry"
				+ "&ParameterString="
					+ "*BatchNumber=" + sBatchNumber
					+ "*EntryNumber=" + sEntryNumber
					+ "*BatchType=" + sBatchType
					+ "*DocumentType=" + sDocumentType
					+ "*CallingURL=" + request.getRequestURI().toString()
					+ "*Editable=" + sEditable
					+ "*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&ReturnField=CustomerNumber"
				+ "&SearchField1=" + SMTablearcustomer.sCustomerName
				+ "&SearchFieldAlias1=Name"
				+ "&SearchField2=" + SMTablearcustomer.sCustomerNumber
				+ "&SearchFieldAlias2=Customer%20Code"
				+ "&SearchField3=" + SMTablearcustomer.sAddressLine1
				+ "&SearchFieldAlias3=Address%20Line%201"
				+ "&SearchField4=" + SMTablearcustomer.sPhoneNumber
				+ "&SearchFieldAlias4=Phone"
				+ "&ResultListField1="  + SMTablearcustomer.sCustomerNumber
				+ "&ResultHeading1=Customer%20Number"
				+ "&ResultListField2="  + SMTablearcustomer.sCustomerName
				+ "&ResultHeading2=Customer%20Name"
				+ "&ResultListField3="  + SMTablearcustomer.sAddressLine1
				+ "&ResultHeading3=Address%20Line%201"
				+ "&ResultListField4="  + SMTablearcustomer.sPhoneNumber
				+ "&ResultHeading4=Phone"
				+ "&ResultListField5="  + SMTablearcustomer.iActive
				+ "&ResultHeading5=Active"
				+ "&ResultListField6="  + SMTablearcustomer.iOnHold
				+ "&ResultHeading6=On%20Hold"
				+ "\""
				//+ "target=\"_blank\""
				+ "> Find customer</A></P>"
				);
	    }

	    //If we are creating a new misc. receipt, we'll need to get the default cash account:
	    //Create a drop down list for the account sets here:
	    //Add drop down list
	    if (sDocumentType.equalsIgnoreCase(Integer.toString(ARDocumentTypes.MISCRECEIPT))){
	    	out.println ("Select the Accounts Receivable Account Set:<BR>" );
	    	try{
		        String sSQL = ARSQLs.Get_AcctSets_List_SQL();
		        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
		     	out.println ("<SELECT NAME=\"" + "AccountSet" + "\">" );
	        	
	        	while (rs.next()){
	        		out.println ("<OPTION VALUE=\"" + rs.getString(SMTablearacctset.sAcctSetCode) + "\">");
	        		out.println (rs.getString(SMTablearacctset.sAcctSetCode) + " - " + rs.getString(SMTablearacctset.sDescription));
	        	}
	        	rs.close();
		        	//End the drop down list:
		        out.println ("</SELECT>");
		        out.println ("<BR>");
			}catch (SQLException ex){
		    	System.out.println("Error in " + this.toString() + " class - could not load account sets.");
		        System.out.println("SQLException: " + ex.getMessage());
		        System.out.println("SQLState: " + ex.getSQLState());
		        System.out.println("SQL: " + ex.getErrorCode());
				//return false;
			}
	    }	//End if it's an adjustment . . . 
	    out.println("<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Add entry' STYLE='width: 2.00in; height: 0.24in'></P>");
		out.println("</FORM>");
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
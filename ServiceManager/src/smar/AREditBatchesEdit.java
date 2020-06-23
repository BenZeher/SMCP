package smar;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMRecreateExportAction;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMClasses.SMBatchStatuses;
import SMClasses.SMBatchTypes;
import SMClasses.SMEntryBatch;
import SMClasses.SMModuleTypes;
import SMClasses.TRANSACTIONSQLs;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableentries;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;

public class AREditBatchesEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	/*
	 * Parameters:
	 * BatchNumber - batch number
	 * BatchType - batch type - an integer passed as a string
	 */
	public static final String AR_BATCH_POSTING_SESSION_WARNING_OBJECT = "AREDITBATCHWARNING";
	private static final String sObjectName = "Batch";
	//Button labels and values
		public static final String COMMAND_FLAG = "COMMANDFLAG";
		public static final String SAVE_COMMAND_VALUE = "SAVE";
		public static final String SAVE_BUTTON_LABEL = "Save " + sObjectName;
		public static final String DELETE_COMMAND_VALUE = "DELETE";
		public static final String DELETE_BUTTON_LABEL = "Delete " + sObjectName;
		public static final String POST_COMMAND_VALUE = "POST";
		public static final String POST_BUTTON_LABEL = "Post " + sObjectName;
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.AREditBatches))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME)
	    		+ (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
		String title = "";
		String subtitle = "";
		
    	//User has chosen to edit:
		if (request.getParameter("BatchNumber") != null){
			if (request.getParameter("BatchNumber").compareToIgnoreCase("-1") == 0){
				title = "Add new batch";
			}else{
				title = "Edit " + sObjectName + ": " + (String) request.getParameter("BatchNumber");
			}
		}
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
		
	    //Display any warnings:
	    String sWarning = (String)CurrentSession.getAttribute(AR_BATCH_POSTING_SESSION_WARNING_OBJECT);
	    CurrentSession.removeAttribute(AR_BATCH_POSTING_SESSION_WARNING_OBJECT);
		if (sWarning != null){
			out.println("<B><FONT COLOR=\"RED\">" + sWarning + "</FONT></B><BR>");
		}
	    //Display any status messages:
	    if (request.getParameter("Status") != null){
	    	String sStatus = request.getParameter("Status");
	    	if (!sStatus.equalsIgnoreCase("")){
	    		out.println("<B>" + sStatus + "</B><BR>");
	    	}
	    }
	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Receivable Main Menu</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditBatches?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Batch List</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.AREditBatches) 
	    		+ "\">Summary</A><BR>");
	    out.println(SMUtilities.getMasterStyleSheetLink());
	    
	    if (request.getParameter("BatchNumber") == null){
	    	out.println("Invalid batch number passed.");
	    	out.println("</BODY></HTML>");
	    	return;
	    }
	    ARBatch batch = new ARBatch((String) request.getParameter("BatchNumber"));

	    if (request.getParameter("BatchType") == null){
	    	out.println("Invalid batch type passed.");
	    	out.println("</BODY></HTML>");
	    	return;
	    }

	    if (! batch.sBatchType((String) request.getParameter("BatchType"))){
	    	out.println("Invalid batch type passed.");
	    	out.println("</BODY></HTML>");
	    	return;
	    }
	    
	    Edit_Record(batch, sUserName, out, request, sDBID, false, sUserID, sUserFullName);
	    out.println(getJavaScript());
		out.println("</BODY></HTML>");
	}
	
	private void Edit_Record(
			ARBatch batch,
			String sUserName,
			PrintWriter pwOut, 
			HttpServletRequest req,
			String sDBID,
			boolean bAddNew,
			String sUserID,
			String sUserFullName){
		
		if (batch.lBatchNumber() != -1){
			try {
				batch.load(getServletContext(), sDBID);
			} catch (Exception e) {
				pwOut.println("Could not load batch number " + batch.sBatchNumber() + " - " + e.getMessage());
			}
		}
		
		//Get the AR export type:
		AROptions aropt = new AROptions();
		try {
			aropt.load(sDBID, getServletContext(), sUserName);
		} catch (Exception e) {
			pwOut.println("<FONT COLOR=RED><B><BR>Error [1474644839] reading AR Options to get export type - " + aropt.getErrorMessageString()
					+ " <BR></B></FONT>");
		}
		
    	//If it's a posted batch, we need to add a button for creating a bank rec:
        if (batch.iBatchStatus() == SMBatchStatuses.POSTED){
        	pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMRecreateExportAction"
        		+ "?" + SMRecreateExportAction.BATCHNUMBER_PARAM + "=" + batch.sBatchNumber()
        		+ "&" + SMRecreateExportAction.BATCHLABEL_PARAM + "=" + SMBatchTypes.Get_Batch_Type(batch.iBatchType())
        		+ "&" + SMRecreateExportAction.SOURCELEDGER_PARAM + "=" + SMModuleTypes.AR
        		+ "&" + SMRecreateExportAction.EXPORTTYPE_PARAM + "=" + aropt.getExportTo()
        		+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
        		+ "&" + SMRecreateExportAction.OTHER_PARAMETERS_PARAM + "="
        			+ "*BatchNumber=" + batch.sBatchNumber()
        			+ "*BatchType=" + batch.sBatchType()
           		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">Re-create export file</A><BR><BR>");
        }
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditBatchesUpdate' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + COMMAND_FLAG + "\" VALUE=\"" + "" + "\"" + " id=\"" + COMMAND_FLAG + "\"" + "\">");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"BatchNumber\" VALUE=\"" + batch.sBatchNumber() + "\">");
		
		//Store the fields:
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"BatchType\" VALUE=\"" + batch.sBatchType() + "\">");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMEntryBatch.ibatchstatus + "\" VALUE=\"" + batch.sBatchStatus() + "\">");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMEntryBatch.lcreatedbyid + "\" VALUE=\"" + batch.sCreatedByID() + "\">");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMEntryBatch.screatedbyfullname + "\" VALUE=\"" + batch.sCreatedByFullName() + "\">");
		
        //Display fields:
        //Date last maintained:
        pwOut.println("<B>" + batch.sBatchTypeLabel() + "</B> batch");
        pwOut.println(" last edited <B>" + batch.sSQLLastEditDateTimeString() + "</B>");
        pwOut.println(" by user <B>" + batch.sLastEditedByFullName() + "</B><BR>");
        pwOut.println("Batch Date: <B>"+ batch.sStdBatchDateString() + "</B>");
        pwOut.println("     Status: <B>"+ batch.sBatchStatusLabel() + "</B>");
        
        if (batch.iBatchStatus() == SMBatchStatuses.POSTED){
        	pwOut.println(" on <B>"+ batch.getsPostingDate() + "</B>");
        }
        pwOut.println("<BR>");
        
        if (batch.bEditable()){
            //Description:
            pwOut.println(clsCreateHTMLFormFields.Create_Edit_Form_Text_Input_Field(
            		SMEntryBatch.sbatchdescription, 
            		clsStringFunctions.filter(batch.sBatchDescription()),  
            		SMEntryBatch.sBatchDescriptionLength, 
            		"Description: ", 
            		"")
            );
            
            //Add an editable cell for the batch date:
/*
            pwOut.println(clsCreateHTMLFormFields.Create_Edit_Form_Date_Input_Field(
            		"BatchDateYear", 
            		"BatchDateMonth", 
            		"BatchDateDay", 
            		batch.calendarBatchDate(),
            		"Batch date: ", 
            		""));
*/          
            pwOut.println(
            		"Batch date: " + clsCreateHTMLFormFields.TDTextBox(SMEntryBatch.datbatchdate, batch.sStdBatchDateString(),
            				10, 
            				10, 
            				"") 
                		+ SMUtilities.getDatePickerString(SMEntryBatch.datbatchdate, getServletContext()) 
                	);
            		
            //Here, we warn the user if we are near the end of the month:
            int iToday = Integer.parseInt(clsDateAndTimeConversions.now("d"));
	    	if ((iToday > 27) || (iToday < 4)
	    		){
	    		pwOut.println("<BR><B><FONT COLOR=\"RED\">" 
	    			+ "WARNING! It's after the 27th or before the 4 of the month - review the document dates below "
	    			+ "and make sure they are correct."
	    			+ "</FONT></B>");
	    	}
            
        	pwOut.println("<BR>");
        	pwOut.println(createSaveButton());     	
        	
        	if (
        			(batch.iBatchStatus() == SMBatchStatuses.ENTERED)
        			|| (batch.iBatchStatus() == SMBatchStatuses.IMPORTED)
        	){
        		pwOut.println(createPostButton());
        		pwOut.println("  Check to confirm posting: <INPUT TYPE=CHECKBOX NAME=\"ConfirmPost\">");
        	}
        	pwOut.println(createDeleteButton());
        	pwOut.println("  Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"ConfirmDelete\">");
        	
        	pwOut.println("</FORM>");
        	
        	//IF it's a new batch, we don't want to display all the batch details on it:
        	if (batch.lBatchNumber() == -1){
        		return;
        	}

        	//Add links to create new entries:
    		if (batch.iBatchType() == SMBatchTypes.AR_INVOICE){
        		pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARAddEntry?" 
	    	    		+ "BatchNumber=" + batch.sBatchNumber()
	    	    		+ "&BatchType=" + batch.sBatchType()
	    	    		+ "&DocumentType=" + ARDocumentTypes.INVOICE_STRING
	    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    	    		+ "\">"
	    	    		+ "Create invoice</A>"
	    	    		);
        		pwOut.println("&nbsp&nbsp");
        		pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARSelectDocID?" 
	    	    		+ "BatchNumber=" + batch.sBatchNumber()
	    	    		+ "&BatchType=" + batch.sBatchType()
	    	    		+ "&DocumentType=" + ARDocumentTypes.CREDIT_STRING
	    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    	    		+ "\">"
	    	    		+ "Create credit</A>"
	    	    		);
	    		pwOut.println("&nbsp&nbsp");
	    		pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARSelectDocID?" 
	    	    		+ "BatchNumber=" + batch.sBatchNumber()
	    	    		+ "&BatchType=" + batch.sBatchType()
	    	    		+ "&DocumentType=" + ARDocumentTypes.RETAINAGE_STRING
	    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    	    		+ "\">"
	    	    		+ "Create retainage</A>"
	    	    		);
    	}
        	if (batch.iBatchType() == SMBatchTypes.AR_CASH){
        		pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARAddEntry?" 
	    	    		+ "BatchNumber=" + batch.sBatchNumber()
	    	    		+ "&BatchType=" + batch.sBatchType()
	    	    		+ "&DocumentType=" + ARDocumentTypes.RECEIPT_STRING
	    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    	    		+ "\">"
	    	    		+ "Create receipt</A>"
	    	    		);
        		pwOut.println("&nbsp&nbsp");
        		pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARAddEntry?" 
	    	    		+ "BatchNumber=" + batch.sBatchNumber()
	    	    		+ "&BatchType=" + batch.sBatchType()
	    	    		+ "&DocumentType=" + ARDocumentTypes.PREPAYMENT_STRING
	    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    	    		+ "\">"
	    	    		+ "Create prepayment</A>"
	    	    		);
        		pwOut.println("&nbsp&nbsp");
        		pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARAddEntry?" 
	    	    		+ "BatchNumber=" + batch.sBatchNumber()
	    	    		+ "&BatchType=" + batch.sBatchType()
	    	    		+ "&DocumentType=" + ARDocumentTypes.MISCRECEIPT_STRING
	    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    	    		+ "\">"
	    	    		+ "Create miscellaneous receipt</A>"
	    	    		);
        		pwOut.println("&nbsp&nbsp");
        		pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARAddEntry?" 
	    	    		+ "BatchNumber=" + batch.sBatchNumber()
	    	    		+ "&BatchType=" + batch.sBatchType()
	    	    		+ "&DocumentType=" + ARDocumentTypes.APPLYTO_STRING
	    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    	    		+ "\">"
	    	    		+ "Create apply-to entry</A>"
	    	    		);
        		pwOut.println("&nbsp&nbsp");
        		pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARSelectDocID?" 
	    	    		+ "BatchNumber=" + batch.sBatchNumber()
	    	    		+ "&DocumentID="
	    	    		+ "&BatchType=" + batch.sBatchType()
	    	    		+ "&DocumentType=" + ARDocumentTypes.REVERSAL_STRING
	    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    	    		+ "\">"
	    	    		+ "Create cash reversal</A>"
	    	    		);
        	}

        	if (batch.iBatchType() == SMBatchTypes.AR_ADJUSTMENT){
 
        	}
        }
        else{
        	pwOut.println("Description: <B>"+ batch.sBatchDescription() + "</B><BR>");
        	pwOut.println(batch.sBatchStatusLabel() + " batches cannot be edited or deleted.");
        }

        //List out the entries on the screen as links to edit:
        //pwOut.println("<BR>");
        pwOut.println("<TABLE WIDTH=100% BGCOLOR=\"" + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "\" CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">");

    	pwOut.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
    	pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">");
    	pwOut.println("Entry #");
    	pwOut.println("</TD>");
    	
    	pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">");
    	pwOut.println("Doc. Type");
    	pwOut.println("</TD>");
    	
    	pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">");
    	pwOut.println("Doc. date");
    	pwOut.println("</TD>");

    	pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">");
    	pwOut.println("Doc #");
    	pwOut.println("</TD>");
    	
    	pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">");
    	pwOut.println("Customer");
    	pwOut.println("</TD>");
    	
    	pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">");
    	pwOut.println("Description");
    	pwOut.println("</TD>");
    	
    	pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">");
    	pwOut.println("Original amt.");
    	pwOut.println("</TD>");
    	
    	pwOut.println("</TR>");

    	if (batch.lBatchNumber() != -1){
    		BigDecimal bdOriginalAmtTotal = new BigDecimal(0);
    		try{
        		String sSQL = TRANSACTIONSQLs.Get_TransactionEntryList_By_BatchNumber(batch.sBatchNumber());
		        ResultSet rsEntries = clsDatabaseFunctions.openResultSet(
		        	sSQL, 
		        	getServletContext(), 
		        	sDBID,
		        	"MySQL",
		        	this.toString() + ".Edit_Record - User: " + sUserID
		        	+ " - "
		        	+ sUserFullName
		        		);
		        int iCount =0;
		        while(rsEntries.next()){
		        	if(iCount % 2 ==0) {
		            	pwOut.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
		        	}else {
		            	pwOut.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
		        	}
		        	//Entry number
		           	pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
		        	
		        	String sEditableBatch = "No";
		        	if(batch.bEditable()){
		        		sEditableBatch = "Yes";
		        	}
		        	
		    	    //Set the name of the class which will handle the processing of the entry, depending on the doc type:
		    	    String sEntryProcessorClass = "";
		    	    if (rsEntries.getString(SMTableentries.idocumenttype).equalsIgnoreCase(ARDocumentTypes.APPLYTO_STRING)){
		    	    	sEntryProcessorClass = "AREditApplyToEntry";
		    	    }
		    	    if (rsEntries.getString(SMTableentries.idocumenttype).equalsIgnoreCase(ARDocumentTypes.CREDIT_STRING)){
		    	    	sEntryProcessorClass = "AREditCreditEntry";
		    	    }
		    	    if (rsEntries.getString(SMTableentries.idocumenttype).equalsIgnoreCase(ARDocumentTypes.INVOICE_STRING)){
		    	    	sEntryProcessorClass = "AREditInvoiceEntry";
		    	    }
		    	    if (rsEntries.getString(SMTableentries.idocumenttype).equalsIgnoreCase(ARDocumentTypes.MISCRECEIPT_STRING)){
		    	    	sEntryProcessorClass = "AREditMiscReceiptEntry";
		    	    }
		    	    if (rsEntries.getString(SMTableentries.idocumenttype).equalsIgnoreCase(ARDocumentTypes.PREPAYMENT_STRING)){
		    	    	sEntryProcessorClass = "AREditCashEntry";
		    	    }
		    	    if (rsEntries.getString(SMTableentries.idocumenttype).equalsIgnoreCase(ARDocumentTypes.RECEIPT_STRING)){
		    	    	sEntryProcessorClass = "AREditCashEntry";
		    	    }
		    	    if (rsEntries.getString(SMTableentries.idocumenttype).equalsIgnoreCase(ARDocumentTypes.RETAINAGE_STRING)){
		    	    	sEntryProcessorClass = "AREditRetainageEntry";
		    	    }
		    	    if (rsEntries.getString(SMTableentries.idocumenttype).equalsIgnoreCase(ARDocumentTypes.REVERSAL_STRING)){
		    	    	sEntryProcessorClass = "AREditReversalEntry";
		    	    }

		    	    //if (batch.bEditable()){
		        	pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar." 
//		        			//Depending on the type of batch, we'll call the different 'edit' classes here:
		    	    		+ sEntryProcessorClass + "?" 
		    	    		+ "BatchNumber=" + batch.sBatchNumber() 
		    	    		+ "&EntryNumber=" + rsEntries.getString(SMTableentries.ientrynumber)
		    	    		+ "&Editable=" + sEditableBatch
		    	    		+ "&BatchType=" + batch.sBatchType()
		    	    		+ "&DocumentType=" + rsEntries.getString(SMTableentries.idocumenttype)
		    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    	    		+ "\">"
		    	    		+ clsStringFunctions.PadLeft(rsEntries.getString(SMTableentries.ientrynumber), "0", 5)
		    	    		+ "</A>"
		    	    		);
		    	    //} else{
		    	    //	pwOut.println(rsEntries.getString(SMTableentries.ientrynumber));
		    	    //}
		        	pwOut.println("</TD>");

		        	//Document type
		           	pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
		        	pwOut.println(ARDocumentTypes.Get_Document_Type_Label(rsEntries.getInt(SMTableentries.idocumenttype)));
		        	pwOut.println("</TD>");

		        	//Document date
		           	pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
		        	pwOut.println(clsDateAndTimeConversions.utilDateToString(rsEntries.getDate(SMTableentries.datdocdate),"MM-dd-yyyy"));
		        	pwOut.println("</TD>");
	
		        	//Doc Number
		           	pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
		        	pwOut.println(rsEntries.getString(SMTableentries.sdocnumber));
		        	pwOut.println("</TD>");
		        	
		        	//PayeePayor
		           	pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
		        	pwOut.println(clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(
		        					rsEntries.getString(SMTableentries.spayeepayor)));
		        	pwOut.println("</TD>");
		        	
		        	//Description
		           	pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
		        	pwOut.println(clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(
		        					rsEntries.getString(SMTableentries.sdocdescription)));
		        	pwOut.println("</TD>");	        	
		        	
		        	//Original amount - just display the absolute value, since we don't care about the arithmetic sign here:
		           	pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
		        	pwOut.println(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsEntries.getBigDecimal(SMTableentries.doriginalamount)));
		        	pwOut.println("</TD>");
		    		bdOriginalAmtTotal = bdOriginalAmtTotal.add(rsEntries.getBigDecimal(SMTableentries.doriginalamount));
		        	
		        	pwOut.println("</TR>");
		        	iCount++;
		        }
		        rsEntries.close();
		        //Print the total lines:
		        pwOut.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
		        pwOut.println("<TD>&nbsp;</TD>");
		        pwOut.println("<TD>&nbsp;</TD>");
		        pwOut.println("<TD>&nbsp;</TD>");
		        pwOut.println("<TD>&nbsp;</TD>");
		        pwOut.println("<TD>&nbsp;</TD>");
		        pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">TOTALS:</TD>");
		        pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdOriginalAmtTotal) + "</TD>");
		        pwOut.println("</TR>");
		        pwOut.println("</TABLE>");
		        pwOut.println("<BR>");
		        
			}catch (SQLException ex){
		    	System.out.println("[1579116339] Error in " + this.toString()+ " class!!");
		        System.out.println("SQLException: " + ex.getMessage());
		        System.out.println("SQLState: " + ex.getSQLState());
		        System.out.println("SQL: " + ex.getErrorCode());
				//return false;
			}
    	} //end if
	}
	public String getJavaScript() {
		String s = "<script>";
		 s +=  "   window.addEventListener(\"beforeunload\",function(){\n" 
		   +  "      document.documentElement.style.cursor = \"not-allowed\";\n "
		   +  "     document.documentElement.style.cursor = \"wait\";\n"
		   +  "      });\n";
			//Post:
			s += "function post(param){\n"
					+ "param.disabled=true;\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
					+ POST_COMMAND_VALUE + "\";\n"
				+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n"
			;
			
			s += "function save(param){\n"
					+ "param.disabled=true;\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
					+ SAVE_COMMAND_VALUE + "\";\n"
				+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n"
			;
			
			s += "function deleteBatch(param){\n"
					+ "param.disabled=true;\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
					+ DELETE_COMMAND_VALUE + "\";\n"
				+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n"
			;
		 s += "</script>";
		return s;
	}
	private String createPostButton(){
		return "<button type=\"button\""
			+ " value=\"" + POST_BUTTON_LABEL + "\""
			+ " name=\"" + POST_BUTTON_LABEL + "\""
			+ " onClick=\"post(this);\">"
			+ POST_BUTTON_LABEL
			+ "</button>\n"
			;
	}
	
	private String createSaveButton(){
		return "<button type=\"button\""
			+ " value=\"" + SAVE_BUTTON_LABEL + "\""
			+ " name=\"" + SAVE_BUTTON_LABEL + "\""
			+ " onClick=\"save(this);\">"
			+ SAVE_BUTTON_LABEL
			+ "</button>\n"
			;
	}
	
	private String createDeleteButton(){
		return "<button type=\"button\""
			+ " value=\"" + DELETE_BUTTON_LABEL + "\""
			+ " name=\"" + DELETE_BUTTON_LABEL + "\""
			+ " onClick=\"deleteBatch(this);\">"
			+ DELETE_BUTTON_LABEL
			+ "</button>\n"
			;
	}	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

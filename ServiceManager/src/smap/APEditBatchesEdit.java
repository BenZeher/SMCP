package smap;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMClasses.SMBatchStatuses;
import SMClasses.SMModuleTypes;
import SMDataDefinition.SMTableapbatchentries;
import SMDataDefinition.SMTableapbatches;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsStringFunctions;
import smar.SMOption;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMMasterEditSelect;
import smcontrolpanel.SMRecreateExportAction;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class APEditBatchesEdit extends HttpServlet {

	public static final String BATCH_EDITABLE_PARAMETER = "Editable";
	public static final String BATCH_EDITABLE_PARAMETER_VALUE_TRUE = "Yes";
	public static final String BUTTON_PRINT_CHECKS_NAME = "BUTTONPRINTCHECKS";
	public static final String BUTTON_PRINT_CHECKS_LABEL = "Print checks";
	
	private static final String ROW_BACKGROUND_HIGHLIGHT_COLOR = "YELLOW";
	private static final String TABLE_ROW_ODD_ROW_BACKGROUND_COLOR = "#DCDCDC";
	private static final String TABLE_ROW_EVEN_ROW_BACKGROUND_COLOR = "#FFFFFF";
	
	private static final long serialVersionUID = 1L;
	/*
	 * Parameters:
	 * BatchNumber - batch number
	 * BatchType - batch type - an integer passed as a string
	 */
	private static String sObjectName = "Batch";
	private static String sDBID = "";
	private static String sUserID = "";
	private static String sCompanyName = "";
	
	//public static String CREATEGLBATCH_BUTTON_NAME = "CREATEGLBATCH";
	//public static String CREATEGLBATCH_BUTTON_LABEL = "Re-create GL batch";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.APEditBatches))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
		String title = "";
		String subtitle = "";
		
		//First, load any batch data from the request:
		APBatch batch = new APBatch(request);
		
		//If there's a batch in the session, get that:
		if (CurrentSession.getAttribute(APBatch.OBJECT_NAME) != null){
			System.out.println("[1543592820] - batch in the session");
			batch = (APBatch)CurrentSession.getAttribute(APBatch.OBJECT_NAME);
			CurrentSession.removeAttribute(APBatch.OBJECT_NAME);
		}else{
			if (batch.getsbatchnumber().compareToIgnoreCase("-1") != 0){
				try {
					//System.out.println("[1543592821]");
					batch.load(getServletContext(), sDBID, sUserID);
					//System.out.println("[1543592822] - type = '" + batch.getsbatchtype());
				} catch (Exception e) {
					out.println("Error [1489358397] - Could not load batch number " + batch.getsbatchnumber() + " - " + e.getMessage() + "\n");
					return;
				}
			}
		}
		
    	//User has chosen to edit:
		String sFunction = "Edit";
		if (!batch.bEditable()){
			sFunction = "View";
		}
		
		if (request.getParameter(SMTableapbatches.lbatchnumber) != null){
			if (request.getParameter(SMTableapbatches.lbatchnumber).compareToIgnoreCase("-1") == 0){
				title = "Add new batch";
			}else{
				title = sFunction + " " + sObjectName + ": " + (String) request.getParameter(SMTableapbatches.lbatchnumber);
			}
		}
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
		out.println(sCommandScript());
		
	    //Display any warnings:
	    if (request.getParameter("Warning") != null){
	    	String sWarning = request.getParameter("Warning");
	    	if (!sWarning.equalsIgnoreCase("")){
	    		out.println("<BR><B><FONT COLOR=\"RED\">" + sWarning + "</FONT></B><BR>\n");
	    	}
	    }
	    //Display any status messages:
	    if (request.getParameter("Status") != null){
	    	String sStatus = request.getParameter("Status");
	    	if (!sStatus.equalsIgnoreCase("")){
	    		out.println("<B>" + sStatus + "</B><BR>\n");
	    	}
	    }
	    
	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>\n");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Payable Main Menu</A><BR>\n");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APEditBatchesSelect?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Batch List</A><BR>\n");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" 
				+ Long.toString(SMSystemFunctions.APEditBatches) + "\">Summary</A><BR>\n");
	    
		//Get the AP export type:
		//TODO - do we want this to export to a different type for AP??
		APOptions apopt = new APOptions();
		try {
			apopt.load(sDBID, getServletContext(), sUserID);
		} catch (Exception e) {
			out.println("<FONT COLOR=RED><B><BR>Error [1489249468] reading AP Options to get export type - " 
				+ e.getMessage()
				+ " <BR></B></FONT>\n");
		}
		
    	//If it's a posted batch, we need to add a button for creating a bank rec:
        if (batch.getsbatchstatus().compareToIgnoreCase(Integer.toString(SMBatchStatuses.POSTED)) == 0){
        	out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMRecreateExportAction"
        		+ "?" + SMRecreateExportAction.BATCHNUMBER_PARAM + "=" + batch.getsbatchnumber()
        		+ "&" + SMRecreateExportAction.BATCHLABEL_PARAM + "=" + SMTableapbatches.getBatchTypeLabel(Integer.parseInt(batch.getsbatchtype()))
        		+ "&" + SMRecreateExportAction.SOURCELEDGER_PARAM + "=" + SMModuleTypes.AP
        		+ "&" + SMRecreateExportAction.EXPORTTYPE_PARAM + "=" + apopt.getiexportoption()
        		+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
        		+ "&" + SMRecreateExportAction.OTHER_PARAMETERS_PARAM + "="
        			+ "*BatchNumber=" + batch.getsbatchnumber()
        			+ "*BatchType=" + batch.getsbatchtype()
           		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">Re-create export file</A><BR><BR>\n");
        }
		
	    Edit_Record(batch, sUserID, out, request, sDBID, false, (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
		out.println("</BODY></HTML>\n");
		
	}

	private void Edit_Record(
			APBatch batch,
			String sUserID,
			PrintWriter pwOut, 
			HttpServletRequest req,
			String sConf,
			boolean bAddNew,
			String sLicenseModuleLevel){
		
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APEditBatchesAction' METHOD='POST'>\n");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>\n");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatches.lbatchnumber + "\" VALUE=\"" + batch.getsbatchnumber() + "\">\n");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatches.ibatchtype + "\" VALUE=\"" + batch.getsbatchtype() + "\">\n");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatches.ibatchstatus + "\" VALUE=\"" + batch.getsbatchstatus() + "\">\n");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatches.lcreatedby + "\" VALUE=\"" + batch.getlcreatedby() + "\">\n");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatches.datlasteditdate + "\" VALUE=\"" + batch.getslasteditdate() + "\">\n");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatches.datpostdate + "\" VALUE=\"" + batch.getsposteddate() + "\">\n");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatches.lbatchlastentry + "\" VALUE=\"" + batch.getsbatchlastentry() + "\">\n");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTableapbatches.llasteditedby + "\" VALUE=\"" + batch.getllasteditedby() + "\">\n");
		
		pwOut.println("<INPUT TYPE=HIDDEN"
			+ " NAME=\"" + "CallingClass" + "\""
			+ " VALUE=\"" + SMUtilities.getFullClassName(this.toString()) + "\">\n"
		);
		
        //Display fields:
        //Date last maintained:
        pwOut.println("<B>" + SMTableapbatches.getBatchTypeLabel(Integer.parseInt(batch.getsbatchtype())) + "</B> batch\n");
        pwOut.println(" last edited <B>" + batch.getslasteditdate() + "</B>\n");
        pwOut.println(" by user <B>" + SMUtilities.getFullNamebyUserID(batch.getllasteditedby(), getServletContext(), sConf, "") + "</B><BR>\n");
        pwOut.println("Batch Date: <B>"+ batch.getsbatchdate() + "</B>\n");
        pwOut.println("     Status: <B>"+ batch.getsbatchstatuslabel() + "</B>\n");
        
        if (batch.getsbatchstatus().compareToIgnoreCase(Integer.toString(SMBatchStatuses.POSTED)) == 0){
        	pwOut.println(" on <B>"+ batch.getsposteddate() + "</B>\n");
        }
        pwOut.println("<BR>\n");
        
        if (batch.bEditable()){
            //Description:
        	pwOut.println(
        		"Description:&nbsp;"
        		+ "<INPUT TYPE=TEXT NAME=\"" + SMTableapbatches.sbatchdescription + "\""
        		+ " VALUE=\"" + clsStringFunctions.filter(batch.getsbatchdescription()) + "\""
        		+ " MAXLENGTH=" + Integer.toString(SMTableapbatches.sBatchDescriptionLength)
        		+ " SIZE = " + "30"
        		+ ">"
        		+ "\n"
        	);
 
            //Add an editable cell for the batch date:
        	pwOut.println(
        		"Batch&nbsp;date:&nbsp;"
        		+ "<INPUT TYPE=TEXT NAME=\"" + SMTableapbatches.datbatchdate + "\""
        		+ " VALUE=\"" + clsStringFunctions.filter(batch.getsbatchdate()) + "\""
        		+ " MAXLENGTH=" + "10"
        		+ " SIZE = " + "8"
        		+ ">"
        		+ "\n"
        	);
    		pwOut.println(SMUtilities.getDatePickerString(SMTableapbatches.datbatchdate, getServletContext()) + "\n");

            //Here, we warn the user if we are near the end of the month:
            int iToday = Integer.parseInt(clsDateAndTimeConversions.now("d"));
	    	if ((iToday > 27) || (iToday < 4)
	    		){
	    		pwOut.println("<BR><B><FONT COLOR=\"RED\">" 
	    			+ "WARNING! It's after the 27th or before the 4 of the month"
	    				+ " - review the document dates below "
	    			+ "and make sure they are correct."
	    			+ "</FONT></B>\n");
	    	}
            
        	pwOut.println("<BR><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Save "
        			+ sObjectName + "' STYLE='height: 0.24in'>\n");
        	
        	if (
        			(batch.getsbatchstatus().compareToIgnoreCase(Integer.toString(SMBatchStatuses.ENTERED)) == 0)
        			|| (batch.getsbatchstatus().compareToIgnoreCase(Integer.toString(SMBatchStatuses.IMPORTED)) == 0)
        	){
        		pwOut.println("  <INPUT TYPE=SUBMIT NAME='Post' VALUE='Post " + sObjectName + "' STYLE='height: 0.24in'>");
        		pwOut.println("  <LABEL>Check to confirm posting: <INPUT TYPE=CHECKBOX NAME=\"ConfirmPost\"></LABEL>\n");
        	}
        	pwOut.println("  <INPUT TYPE=SUBMIT NAME='Delete' VALUE='Delete " + sObjectName + "' STYLE='height: 0.24in'>");
        	pwOut.println("  <LABEL>Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"ConfirmDelete\"></LABEL>\n");
        	
        	//If this is a PAYMENT batch:
        	if (batch.getsbatchtype().compareToIgnoreCase(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_PAYMENT)) == 0){
        		
        		//If it's ENTERED or IMPORTED (not POSTED or DELETED), then:
        		if(
        			(batch.getsbatchstatus().compareToIgnoreCase(Integer.toString(SMBatchStatuses.ENTERED)) == 0)
            		|| (batch.getsbatchstatus().compareToIgnoreCase(Integer.toString(SMBatchStatuses.IMPORTED)) == 0)
        		){
        			//If all the checks have NOT been finalized, then allow the user to print the checks:
            		if (!batch.bAllChecksHaveBeenFinalized()){
            			pwOut.println("  <INPUT TYPE=SUBMIT NAME='" + BUTTON_PRINT_CHECKS_NAME + "' VALUE='" + BUTTON_PRINT_CHECKS_LABEL + "' STYLE='height: 0.24in'>");
            		}else{
            			pwOut.println("  (NOTE: Checks for this batch have already been printed successfully.)");
            		}
        		}
        	}
        	
        	pwOut.println("</FORM>\n");
        	
        	//IF it's a new batch, we don't want to display all the batch details on it:
        	if (batch.getsbatchnumber().compareToIgnoreCase("-1") == 0){
        		return;
        	}

       		if (batch.getsbatchtype().compareToIgnoreCase(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_INVOICE)) == 0){
       			for (int i = SMTableapbatchentries.ENTRY_TYPE_INV_INVOICE; i <= SMTableapbatchentries.ENTRY_TYPE_INV_CREDITNOTE; i++){
            		pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APEditInvoiceEdit" 
    	    	    		+ "?" + SMTableapbatches.lbatchnumber + "=" + batch.getsbatchnumber()
    	    	    		+ "&" + SMTableapbatches.ibatchtype + "=" + batch.getsbatchtype()
    	    	    		+ "&" + BATCH_EDITABLE_PARAMETER + "=" + BATCH_EDITABLE_PARAMETER_VALUE_TRUE
    	    	    		+ "&" + SMTableapbatchentries.lid + "=-1"
    	    	    		+ "&" + SMTableapbatchentries.ientrytype + "=" + Integer.toString(i)
    	    	    		+ "&" + "CallingClass=" + SMUtilities.getFullClassName(this.toString())
    	    	    		+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=Y"
    	    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    	    	    		+ "\">"
    	    	    		+ "Create " + SMTableapbatchentries.getDocumentTypeLabel(i) + "</A>"
    	    	    		+ "\n"
    	    	    		);
            		pwOut.println("&nbsp&nbsp");
       			}
    		}
       		if (batch.getsbatchtype().compareToIgnoreCase(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_PAYMENT)) == 0){
       			for (int i = SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PAYMENT; i <= SMTableapbatchentries.ENTRY_TYPE_PAYMENT_MISCPAYMENT; i++){
       				pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APEditPaymentEdit" 
	    	    		+ "?" + SMTableapbatches.lbatchnumber + "=" + batch.getsbatchnumber()
	    	    		+ "&" + SMTableapbatches.ibatchtype + "=" + batch.getsbatchtype()
	    	    		+ "&" + BATCH_EDITABLE_PARAMETER + "=" + BATCH_EDITABLE_PARAMETER_VALUE_TRUE
	    	    		+ "&" + SMTableapbatchentries.lid + "=-1"
	    	    		+ "&" + SMTableapbatchentries.ientrytype + "=" + Integer.toString(i)
	    	    		+ "&" + "CallingClass=" + SMUtilities.getFullClassName(this.toString())
	    	    		+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=Y"
	    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    	    		+ "\">"
	    	    		+ "Create " + SMTableapbatchentries.getDocumentTypeLabel(i) + "</A>"
	    	    		+ "\n"
	    	    		);
        		pwOut.println("&nbsp&nbsp");
       			}
       			
    		}
       		if (batch.getsbatchtype().compareToIgnoreCase(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_REVERSALS)) == 0){
        		pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APEditReversalEdit" 
    	    		+ "?" + SMTableapbatches.lbatchnumber + "=" + batch.getsbatchnumber()
    	    		+ "&" + SMTableapbatches.ibatchtype + "=" + batch.getsbatchtype()
    	    		+ "&" + BATCH_EDITABLE_PARAMETER + "=" + BATCH_EDITABLE_PARAMETER_VALUE_TRUE
    	    		+ "&" + SMTableapbatchentries.lid + "=-1"
    	    		+ "&" + SMTableapbatchentries.ientrytype + "=" + Integer.toString(SMTableapbatchentries.ENTRY_TYPE_REVERSAL)
    	    		+ "&" + "CallingClass=" + SMUtilities.getFullClassName(this.toString())
    	    		+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=Y"
    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    	    		+ "\">"
    	    		+ "Create " + SMTableapbatchentries.getDocumentTypeLabel(SMTableapbatchentries.ENTRY_TYPE_REVERSAL) + "</A>"
    	    		+ "\n"
    	    		);
        		pwOut.println("&nbsp&nbsp");
    		}
        }
        else{
        	pwOut.println("Description: <B>"+ batch.getsbatchdescription() + "</B><BR>");
        	//IF it's a payment batch and the checks are all finalized:
        	if (batch.bAllChecksHaveBeenFinalized() && (batch.getsbatchtype().compareToIgnoreCase(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_PAYMENT)) == 0)){
        		pwOut.println(batch.getsbatchstatuslabel() + "&nbsp;&nbsp;<B><I>NOTE: All checks for this payment batch have been finalized, so the payment entries cannot be edited.</I></B>\n");
        	}else{
        		pwOut.println(batch.getsbatchstatuslabel() + " batches cannot be edited or deleted.\n");
        	}
        }

		//If it's a posted payment batch, then add a link to print the check register:
        if (
        	(batch.getsbatchstatus().compareToIgnoreCase(Integer.toString(SMBatchStatuses.POSTED)) == 0)
        	&& (batch.getsbatchtype().compareToIgnoreCase(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_PAYMENT)) == 0)
        	
        ){
			pwOut.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APCheckRegisterGenerate" 
				+ "?" + SMTableapbatches.lbatchnumber + "=" + batch.getsbatchnumber()
				+ "&" + SMTableapbatches.ibatchtype + "=" + batch.getsbatchtype()
				+ "&" + "CallingClass=" + SMUtilities.getFullClassName(this.toString())
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "\">"
				+ "Print Check Register</A>"
				+ "\n"
		    );
		}
        
        int iColumnCount = 0;
        
        //List out the entries on the screen as links to edit:
        pwOut.println("<BR><I><B>NOTE:</B> Out-of-balance entry amounts and entry dates outside the posting period are displayed in RED.</I>");
        pwOut.println("<TABLE BORDER=1 CELLSPACING=2 style=\"font-size:75%\">\n");

    	pwOut.println("  <TR>\n");
    	pwOut.println("    <TD>");
    	pwOut.println("<B><U>Entry #</B></U>");
    	pwOut.println("</TD>\n");
    	iColumnCount++;
    	
    	pwOut.println("    <TD>");
    	pwOut.println("<B><U>Entry Type</B></U>");
    	pwOut.println("</TD>\n");
    	iColumnCount++;
    	
    	pwOut.println("    <TD>");
    	pwOut.println("<B><U>Entry date</B></U>");
    	pwOut.println("</TD>\n");
    	iColumnCount++;

    	pwOut.println("    <TD>");
    	pwOut.println("<B><U>Vendor</B></U>");
    	pwOut.println("</TD>\n");
    	iColumnCount++;

    	pwOut.println("    <TD>");
    	pwOut.println("<B><U>Doc #</B></U>");
    	pwOut.println("</TD>\n");
    	iColumnCount++;
    	
    	//If it's a payment batch, show the check number:
    	if (batch.getsbatchtype().compareToIgnoreCase(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_PAYMENT)) == 0){
        	pwOut.println("    <TD>");
        	pwOut.println("<B><U>Check number</B></U>");
        	pwOut.println("</TD>\n");
        	iColumnCount++;
    	}
    	
    	//If it's a payment batch, indicate if a check needs to be finalized:
    	if (batch.getsbatchtype().compareToIgnoreCase(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_PAYMENT)) == 0){
        	pwOut.println("    <TD>");
        	pwOut.println("<B><U>Check finalized?</B></U>");
        	pwOut.println("</TD>\n");
        	iColumnCount++;
    	}
    	
    	pwOut.println("    <TD>");
    	pwOut.println("<B><U>Description</B></U>");
    	pwOut.println("</TD>\n");
    	iColumnCount++;
    	
    	pwOut.println("    <TD>");
    	pwOut.println("<B><U>Number<BR>of&nbsp;lines</B></U>");
    	pwOut.println("</TD>\n");
    	iColumnCount++;
    	
    	pwOut.println("    <TD>");
    	pwOut.println("<B><U>Entry net amt</B></U>");
    	pwOut.println("</TD>\n");
    	iColumnCount++;
    	
    	pwOut.println("    <TD>");
    	pwOut.println("<B><U>Entry errors?</B></U>");
    	pwOut.println("</TD>\n");
    	iColumnCount++;
    	
    	pwOut.println("  </TR>\n");

	    //Set the name of the class which will handle the processing of the entry, depending on the doc type:
	    String sEntryProcessorClass = "";
	    if (batch.getsbatchtype().compareToIgnoreCase(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_ADJUSTMENT)) == 0){
	    	sEntryProcessorClass = "APEditAdjustmentEdit";
	    }
	    if (batch.getsbatchtype().compareToIgnoreCase(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_INVOICE)) == 0){
	    	sEntryProcessorClass = "APEditInvoiceEdit";
	    }
	    if (batch.getsbatchtype().compareToIgnoreCase(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_PAYMENT)) == 0){
	    	sEntryProcessorClass = "APEditPaymentEdit";
	    }
	    if (batch.getsbatchtype().compareToIgnoreCase(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_REVERSALS)) == 0){
	    	sEntryProcessorClass = "APEditReversalEdit";
	    }
    	
    	//Just read the entries out of the batch here:
    	if (batch.getsbatchnumber().compareToIgnoreCase("-1") != 0){
    		
    		Connection conn = null;
			try {
				conn = clsDatabaseFunctions.getConnectionWithException(
					getServletContext(), 
					sConf, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString() + ".Edit_Record - userID: " + sUserID)
				);
			} catch (Exception e) {
				pwOut.println("<BR><B><FONT COLOR=RED>Error [1494430078] getting connection - " + e.getMessage() + "</FONT></B><BR>");
			}
    		SMOption opt = new SMOption();
    		if(!opt.load(conn)){
    			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547047711]");
    			pwOut.println("<BR><B><FONT COLOR=RED>Error [1494430079] getting SM Options - " + opt.getErrorMessage() + "</FONT></B><BR>");
    		}
    		
    		BigDecimal bdCostTotal = new BigDecimal(0);
    		
    		boolean bViewVendorInfoAllowed = SMSystemFunctions.isFunctionPermitted(
    			SMSystemFunctions.APDisplayVendorInformation, 
    			sUserID, 
    			conn, 
    			sLicenseModuleLevel
    		);
    		boolean bOddRow = true;
    		for (int i = 0; i < batch.getBatchEntryArray().size(); i++){
    			APBatchEntry entry = batch.getBatchEntryArray().get(i);
				String sBackgroundColor = TABLE_ROW_EVEN_ROW_BACKGROUND_COLOR;
				if (bOddRow){
					sBackgroundColor = TABLE_ROW_ODD_ROW_BACKGROUND_COLOR;
				}
    			pwOut.println("  <TR style = \"  background-color:" + sBackgroundColor + ";  \""
					+ " onmouseout=\"setRowBackgroundColor(this, '" + sBackgroundColor + "');\""
					+ " onmousemove=\"setRowBackgroundColor(this, '" + ROW_BACKGROUND_HIGHLIGHT_COLOR + "');\""
					+ ">\n"
				);
	        	//Entry number
	        	pwOut.println("    <TD>");
	        	
	        	String sEditableBatch = "No";
	        	if(batch.bEditable()){
	        		sEditableBatch = "Yes";
	        	}
	        	
	    	    //if (batch.bEditable()){
	        	pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap." 
//	        			//Depending on the type of batch, we'll call the different 'edit' classes here:
	    	    		+ sEntryProcessorClass 
	    	    		+ "?" + SMTableapbatchentries.lbatchnumber + "=" + entry.getsbatchnumber() 
	    	    		+ "&" + SMTableapbatchentries.lentrynumber + "=" + entry.getsentrynumber()
	    	    		+ "&" + SMTableapbatchentries.lid + "=" + entry.getslid()
	    	    		+ "&" + SMTableapbatchentries.ientrytype + "=" + entry.getsentrytype()
	    	    		+ "&" + "Editable=" + sEditableBatch
	    	    		+ "&" + "CallingClass=" + SMUtilities.getFullClassName(this.toString())
	    	    		//+ "&" + SMTableapbatches.ibatchtype + "=" + batch.getsbatchtype()
	    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    	    		+ "\">"
	    	    		+ clsStringFunctions.PadLeft(
	    	    			entry.getsentrynumber(), "0", 5)
	    	    		+ "</A>"
	    	    		);
	    	    //} else{
	    	    //	pwOut.println(rsEntries.getString(SMTableentries.ientrynumber));
	    	    //}
	        	pwOut.println("</TD>\n");

	        	//Entry type
	        	pwOut.println("    <TD>");
	        	pwOut.println(SMTableapbatchentries.getDocumentTypeLabel(Integer.parseInt(entry.getsentrytype())));
	        	pwOut.println("</TD>\n");

	        	//Entry date
	        	pwOut.println("    <TD>");
	        	String sEntryDate = entry.getsdatentrydate();

	    		try {
	    			opt.checkDateForPosting(
	    				entry.getsdatentrydate(), 
	    				"ENTRY DATE", 
	    				conn, 
	    				sUserID
	    			);
	    		} catch (Exception e2) {
	    			sEntryDate = "<div style = \" color: red; font-weight: bold; \">" + sEntryDate + "</div>"; 
	    		}
	        	pwOut.println(sEntryDate);
	        	pwOut.println("</TD>\n");

	        	//Vendor
	        	String sVendorLink = entry.getsvendoracct();
	        	if(bViewVendorInfoAllowed){
	        		sVendorLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap." 
	    	    		+ "APDisplayVendorInformation" 
	    	    		+ "?" + "VendorNumber" + "=" + entry.getsvendoracct() 
	    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    	    		+ "\">"
	    	    		+ entry.getsvendoracct()
	    	    		+ "</A>";
	        	}
	        	pwOut.println("    <TD>");
	        	pwOut.println(sVendorLink);
	        	pwOut.println("</TD>\n");
	        	
	        	//Doc Number
	        	pwOut.println("    <TD>");
	        	pwOut.println(entry.getsdocnumber());
	        	pwOut.println("</TD>\n");
	        	
	        	//Check number:
	        	if (batch.getsbatchtype().compareToIgnoreCase(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_PAYMENT)) == 0){
		        	pwOut.println("    <TD>");
		        	pwOut.println(entry.getschecknumber());
		        	pwOut.println("</TD>\n");
	        	}
	        	
	        	//Check unfinalized?
	        	String sCheckFinalized = "N/A";
	        	
	        	//For payments, pre-pays, misc payments, the 'check finalized' is meaningful
	        	//But for apply-tos, etc., we just show an 'N/A':
	        	if (
	        		(entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_MISCPAYMENT)
	        		|| (entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PAYMENT)
	        		|| (entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PREPAYMENT)
	        	){
	        	
		        	if (entry.getsiprintcheck().compareToIgnoreCase("1") == 0){
		        		sCheckFinalized = "<B>Y</B>";
		        		if (entry.getsiprintingfinalized().compareToIgnoreCase("0") == 0){
		        			sCheckFinalized = "<DIV style = \" color: red; font-weight: bold; \">N</DIV>";
		        		}
		        	}
	        	}
	        	
	        	if (batch.getsbatchtype().compareToIgnoreCase(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_PAYMENT)) == 0){
		        	pwOut.println("    <TD ALIGN=CENTER>");
		        	pwOut.println(sCheckFinalized);
		        	pwOut.println("</TD>\n");
	        	}
	        	
	        	//Description
	        	pwOut.println("    <TD>");
	        	pwOut.println(clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(entry.getsentrydescription()));
	        	pwOut.println("</TD>\n");
	        	
	        	//Number of entries:
	        	pwOut.println("    <TD ALIGN=RIGHT>");
	        	pwOut.println(Integer.toString(entry.getLineArray().size()));
	        	pwOut.println("</TD>\n");
	        	
	        	//Entry amount - just display the absolute value, since we don't care about the arithmetic sign here:
	        	pwOut.println("    <TD ALIGN=RIGHT>");
	        	String sEntryAmt = entry.getsentryamount();
	        	if (!entry.entryIsInBalance(conn)){
	        		sEntryAmt = "<div style = \" color: red; font-weight: bold; \">" + sEntryAmt + "</div>";
	        	}
	        	pwOut.println(sEntryAmt);
	        	pwOut.println("</TD>\n");
	    		bdCostTotal = bdCostTotal.add(new BigDecimal(entry.getsentryamount().replaceAll(",", "")));
	        	
	        	//Entry error?
	    		String sCheckLinePostabilityResults = "";
	    		try {
					entry.checkLinePostability(conn);
				} catch (Exception e) {
					sCheckLinePostabilityResults = e.getMessage();
				}
	    		
	        	pwOut.println("    <TD>"); 
	        	pwOut.println(
	        		"<div style = \" color: red; font-weight: bold; \">"
	        		+ sCheckLinePostabilityResults
	        		+ "</div>"
	        	);
	        	pwOut.println("</TD>\n");
	    		
	        	pwOut.println(" </TR>\n");
	        	bOddRow = !bOddRow;
    		}
    		
        	//Print the 'totals' line:
        	pwOut.println("  <TR>\n"
        		+ "    <TD ALIGN=RIGHT COLSPAN=" + Integer.toString(iColumnCount - 2) + ">"
        		+ "<B>BATCH TOTAL:</B>"
        		+ "    </TD>\n"
        		+ "    <TD ALIGN=RIGHT >"
        		+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(batch.getBatchTotal())
        		+ "    </TD>\n"
        		+ "    <TD>"
        		+ "&nbsp;"
        		+ "    </TD>\n"
        		+ "  </TR>\n"
        	);
        	
    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547047712]");
    	} //end if
	}
	private String sCommandScript(){
		String s = "";
		s += "<NOSCRIPT>\n"
				+ "    <font color=red>\n"
				+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
				+ "    </font>\n"
				+ "</NOSCRIPT>\n"
			;
		s += "<script type='text/javascript'>\n";
		
		//Function for changing row backgroundcolor:
		s += "function setRowBackgroundColor(row, color) { \n"
			+ "    row.style.backgroundColor = color; \n"
    		+ "} \n"
		;
		
		 s +=  "   window.addEventListener(\"beforeunload\",function(){\n" 
				  +  "     document.body.setAttribute(\"style\",\"pointer-events: none; color: black; cursor: not-allowed; display: inline-block; text-decoration: none;\");\n"
				  +  "     document.documentElement.style.cursor = \"wait\";\n"
				  +"      });\n";
		 
		s += "</script>\n";
		return s;
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

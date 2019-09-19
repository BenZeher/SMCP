/*
 * Apply-to entries work like this:
 * 
 * The user picks a document (for example a prepay) that they wish to apply to one or more other documents.  
 * The entry amount defaults to the current amount of that 'apply-from' document.  The user can change it,
 * and if he does, then he is changing the amount of the apply-from that he wants to apply.
 * 
 * The apply-from document information is stored in the first line of the entry as the 'apply-to' doc and
 * apply-to doc ID.  The user cannot edit any information in this first line - they can edit the entry amount,
 * and that will change the amount of this first line, but they can't edit the first line directly.
 * 
 * The first line won't show among the lines on the entry at all.  Only the 'apply-to' lines will be listed
 * among the lines on the screen.
 * 
 * The apply-to doc and apply-to-doc ID will appear in the entry header as the 'Apply-from doc' and the 
 * 'Apply-from ID.' 
 * 
 * When the entry gets posted, the transaction gets created as normal.  But the armatchinglines work like this:
 * The first line holds the 'apply-from' document info.  So we create a matching line for every subsequent
 * line (i.e., not for line 0, but for every line after that one).  AND, also, for every line above zero,
 * we create a matching line that applies to the 'apply-from' doc, so there will be two armatchinglines for
 * line 1, and two armatchinglines for line 2, and so on.  ALL of the matching lines have the parent ID
 * of the created artransaction, however.
 * 
 * That first line will have a zero amount in the entry, and the entry will have an original amount.  But
 * when the entry is posted, the current amount is zero - apply-to entries never carry an open amount.
 * 
 * */

package smar;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMBatchTypes;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableartransactions;
import SMDataDefinition.SMTableentries;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class AREditApplyToEntry extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	private static String sObjectName = "Apply-to Entry";
	
	private AREntryInput m_EntryInput;
	private boolean m_bIsNewEntry = false;
	private boolean m_bEditable = false;

	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter m_pwOut = response.getWriter();
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
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    //If there is no EntryInput in the session, we'll get a null in m_EntryInput:
		m_EntryInput = (AREntryInput) CurrentSession.getAttribute("EntryInput");
		//Get rid of the session variable immediately:
		CurrentSession.removeAttribute("EntryInput");
		
	    //Get the request parameters:
		if (request.getParameter("EntryNumber") != null){
			if (clsManageRequestParameters.get_Request_Parameter("EntryNumber", request).equalsIgnoreCase("-1")){
				m_bIsNewEntry = true; 
			}else{
				m_bIsNewEntry = false;
			}
		}else{
			m_bIsNewEntry = false;
			//System.out.println("In " + this.toString() + " - didn't get parameter EntryNumber");
		}

		String m_sBatchNumber = clsManageRequestParameters.get_Request_Parameter("BatchNumber", request);
		String m_sEntryNumber = clsManageRequestParameters.get_Request_Parameter("EntryNumber", request);
		String m_sEditable = clsManageRequestParameters.get_Request_Parameter("Editable", request);
		if (m_sEditable.compareToIgnoreCase("Yes") ==0){
			m_bEditable = true;
		}else {
			m_bEditable = false;
		}
		String m_sBatchType = clsManageRequestParameters.get_Request_Parameter("BatchType", request);
		String m_sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		String m_sCustomerNumber = clsManageRequestParameters.get_Request_Parameter("CustomerNumber", request).toUpperCase();
		String m_sDocumentType = clsManageRequestParameters.get_Request_Parameter("DocumentType", request);
	    
		//Try to load an AREntryInput object from which to build the form:
		try {
			loadAREntryInput(
					sDBID,
					m_sBatchNumber,
					m_sEntryNumber,
					m_sBatchType,
					m_sDocumentType,
					m_sCustomerNumber
			);
		} catch (Exception e) {
    		m_pwOut.println(
					"<META http-equiv='Refresh' content='0;URL=" 
					+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARAddEntry"
					+ "?BatchNumber=" + m_sBatchNumber
					+ "&BatchType=" + m_sBatchType
					+ "&DocumentType=" + m_sDocumentType
					+ "&DocumentID="
					+ "&Warning=" + e.getMessage()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "'>"		
				);
			return;
		}
		
	    String title;
	    String subtitle = "";
	    if (m_bIsNewEntry){
	    	title = "Edit NEW " + sObjectName;
	    }else{
	    	title = "Edit " + sObjectName + ": " + m_EntryInput.getsEntryNumber();	
	    }

	    m_pwOut.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
		//If there is a warning from trying to input previously, print it here:
		if (! m_sWarning.equalsIgnoreCase("")){
			m_pwOut.println("<B><FONT COLOR=\"RED\">WARNING: " + m_sWarning + "</FONT></B><BR>");
		}
		
	    //Print a link to main menu:
		m_pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">Return to Accounts Receivable Main Menu</A><BR>");
	    
	    //Print a link to return to the 'edit batch' page:
		m_pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditBatchesEdit" 
	    		+ "?BatchNumber=" + m_EntryInput.getsBatchNumber()
	    		+ "&BatchType=" + m_EntryInput.getsBatchType()
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "\">Return to Edit Batch " + m_EntryInput.getsBatchNumber() + "</A><BR><BR>");

		//Try to construct the rest of the screen form from the AREntryInput object:
		if (!createFormFromAREntryInput(
				m_pwOut, 
				m_bEditable, 
				m_EntryInput, 
				getServletContext(), 
				sDBID,
				"AREditApplyToEntry"
		)){
			//System.out.println("In AREditApplyToEntry - error opening createFormFromAREntryInput");
    		m_pwOut.println(
					"<META http-equiv='Refresh' content='0;URL=" 
					+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARAddEntry"
					+ "?BatchNumber=" + m_EntryInput.getsBatchNumber()
					+ "&BatchType=" + m_EntryInput.getsBatchType()
					+ "&DocumentType=" + m_sDocumentType
					+ "&Warning=" + "Could not load customer"
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "'>"		
				);
			return;
		}
		
		//End the page:
		m_pwOut.println("</BODY></HTML>");
	}
	private void loadAREntryInput(String sDBID,
			String sBatchNumber,
			String sEntryNumber,
			String sBatchType,
			String sDocumentType,
			String sCustomerNumber) throws Exception{
		
		//If the class has NOT been passed an AREntryInput query string, we'll have to build it:
		if (m_EntryInput == null){
			//Have to construct the AREntryInput object here:
			m_EntryInput = new AREntryInput();
			if (m_bIsNewEntry){
				//If it's a new entry:
				m_EntryInput.setBatchNumber(sBatchNumber);
				m_EntryInput.setBatchType(sBatchType);
				m_EntryInput.setDocumentType(sDocumentType);
				//Load the transaction this adjustment will apply from:
				m_EntryInput.setCustomerNumber(sCustomerNumber);
				
				ARCustomer cust = new ARCustomer(sCustomerNumber);
				if (!cust.load(getServletContext(), sDBID)){
					throw new Exception("Could not load customer " + sCustomerNumber);
				}
				
				m_EntryInput.setControlAcct(cust.getARControlAccount(getServletContext(), sDBID));
				//Get the batch date as the default entry date:
				ARBatch batch = new ARBatch(m_EntryInput.getsBatchNumber());
				try {
					batch.load(getServletContext(), sDBID);
					m_EntryInput.setDocDate(batch.sStdBatchDateString());
				} catch (Exception e) {
					m_EntryInput.setDocDate(clsDateAndTimeConversions.now("MM/dd/yyyy"));
				}
				m_EntryInput.setDocDescription("Apply-to entry");
				m_EntryInput.setEntryNumber("-1");
				m_EntryInput.setEntryID("-1");
				m_EntryInput.setOriginalAmount("0.00");
			}else{
				//If it's an existing entry:
				//Load the existing entry:
				AREntry entry = new AREntry();
				entry = new AREntry();
				if (!entry.load(sBatchNumber, sEntryNumber, getServletContext(), sDBID)){
					throw new Exception("Could not load entry with batch number " + sBatchNumber + ", entry number " + sEntryNumber
			    		+ "\n" + entry.getErrorMessage());
				}
				if (!m_EntryInput.loadFromEntry(entry)){
					throw new Exception("Could not load entry input from entry with batch number " + sBatchNumber + ", entry number " + sEntryNumber);
				}
			}
		}
		return;
	}

	public static boolean createFormFromAREntryInput(
			PrintWriter pwOut,
			boolean bEditable,
			AREntryInput entryInput,
			ServletContext context,
			String sDBID,
			String sCallingClass
	){
		
		//Include the javascript:
		pwOut.println(SMUtilities.getDatePickerIncludeString(context));
		
	    //Start the entry edit form:
		pwOut.println("<FORM NAME='ENTRYEDIT' ACTION='" + SMUtilities.getURLLinkBase(context) + "smar.AREntryUpdate' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		//Record the hidden fields for the entry edit form:
	    storeHiddenFieldsOnForm (pwOut, bEditable, entryInput, sCallingClass);

        //Display the entry header fields:
	    try {
			displayEntryHeaderFields(pwOut, bEditable, entryInput, context, sDBID);
		} catch (Exception e) {
			pwOut.println("<BR><FONT COLOR=RED><B>" + e.getMessage() + "</B></FONT><BR>");
			return false;
		}
        
        if (bEditable){
        	displayEditableEntryFields (
        		pwOut, 
        		bEditable, 
        		entryInput,
        		context, 
        		sDBID);
        }
        //Else, if the record is NOT editable:
        else{
        	displayNonEditableEntryFields (pwOut, entryInput, context, sDBID);
        }
        
	    //Now display the transaction lines:
	    //If it's not editable, just show the current applied lines:
	    if (! bEditable){
	    	pwOut.println("<B>Line distribution:</B><BR>");
	    	Display_NONEditable_Lines(pwOut, entryInput, context, sDBID);
	    }else{
    		pwOut.println("<B>Line distribution:</B><BR>");
	        //Display the line header:
		    Display_Line_Header(pwOut, entryInput);

		    //Display all the current transaction lines:
		    if (!displayTransactionLines(
		    		pwOut, 
		    		entryInput, 
		    		context,
		    		sDBID
		    		)){
		    	return false;
		    }	    
		    pwOut.println("</TABLE>");
	    }
	    
	    if (bEditable){
	    	addCommandButtons(entryInput, pwOut);
	    }
	    //End the entry edit form:
	    pwOut.println("</FORM>");  

		return true;
	}
	private static void storeHiddenFieldsOnForm(
			PrintWriter pwOut,
			boolean bEditable,
			AREntryInput entryInput,
			String sCallingClass
	){
		
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AREntryInput.ParamBatchNumber + "\" VALUE='" + entryInput.getsBatchNumber() + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AREntryInput.ParamEntryNumber + "\" VALUE='" + entryInput.getsEntryNumber() + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AREntryInput.ParamCustomerNumber + "\" VALUE='" + entryInput.getsCustomerNumber() + "'>");
	    String sEditable;
	    if (bEditable){
	    	sEditable = "Yes";
	    }else{
	    	sEditable = "No";
	    }
	    pwOut.println("<INPUT TYPE=HIDDEN NAME=\"Editable\" VALUE='" + sEditable + "'>");
	    pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AREntryInput.ParamEntryID + "\" VALUE='" + entryInput.getsEntryID() + "'>");
	    pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AREntryInput.ParamBatchType + "\" VALUE='" + entryInput.getsBatchType() + "'>");
	    pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AREntryInput.ParamDocumentType + "\" VALUE='" + entryInput.getsDocumentType() + "'>");
	    pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AREntryInput.ParamDocNumber + "\" VALUE='" + entryInput.getsDocNumber() + "'>");
	    pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE='" + sCallingClass + "'>");
	}
	private static void displayEntryHeaderFields (
			PrintWriter pwOut,
			boolean bEditable,
			AREntryInput entryInput,
			ServletContext context,
			String sDBID
	)throws Exception{
		
		pwOut.println("<B>" + SMBatchTypes.Get_Batch_Type(Integer.parseInt(entryInput.getsBatchType())) + "</B>");
		pwOut.println(" batch number: <B>" + entryInput.getsBatchNumber() + "</B>;");
		//Get the batch total:
		ARBatch batch = new ARBatch(entryInput.getsBatchNumber());
		pwOut.println(" batch total: <B>" + batch.sTotalAmount(context, sDBID) + "</B>;");
		
        if (entryInput.getsEntryNumber().equalsIgnoreCase("-1")){
        	pwOut.println(" entry number: <B>NEW</B>.  ");
        }else{
        	pwOut.println(" entry number: <B>" + entryInput.getsEntryNumber() + "</B>.  ");
        }
        
        //If the entry has a customer number, display the number and name:
		//Get the customer name here:
        String sCustomerNumber = "";
        String sCustomerName = "";

    	sCustomerNumber = entryInput.getsCustomerNumber();
    	ARCustomer m_Customer = new ARCustomer(entryInput.getsCustomerNumber());
		if (! m_Customer.load(context, sDBID)){
			throw new Exception("Could not load customer: " + entryInput.getsCustomerNumber());
		}else{
			sCustomerName = m_Customer.getM_sCustomerName();
			
			//If it's a new entry, set the default control account:
			if (entryInput.getsEntryNumber().equalsIgnoreCase("-1")){
				entryInput.setControlAcct(m_Customer.getARControlAccount(context, sDBID));

			}
		}
		pwOut.println("For customer: " + "<B>" + sCustomerNumber);
    	
        //Get the customer name:
    	pwOut.println(" - " 
        		+ sCustomerName
        		+ "</B><BR>");

	    pwOut.println(" Document type: <B>" 
        		+ ARDocumentTypes.Get_Document_Type_Label(
        				Integer.parseInt(entryInput.getsDocumentType()))
        		+ "</B>.  ");

	    if (bEditable){
	    	addCommandButtons(entryInput, pwOut);
        }
	    return;
	}
	private static void displayEditableEntryFields(
			PrintWriter pwOut,
			boolean bEditable,
			AREntryInput entryInput,
			ServletContext context,
			String sDBID
	){
		
		pwOut.println("<TABLE BORDER=1 WIDTH=100% CELLSPACING=2 style=\"font-size:75%\">");
		
        //START ROW 1
		pwOut.println("<TR>");
        
        //Doc Number:
		pwOut.println("<TD>");
		pwOut.println("Doc. #:&nbsp;" + clsStringFunctions.filter(entryInput.getsDocNumber()));
		pwOut.println("</TD>");

        //Doc date:
		pwOut.println("<TD>");

		pwOut.println(clsCreateHTMLFormFields.Create_Edit_Form_Text_Input_Field(
        		AREntryInput.ParamDocDate, 
        		clsStringFunctions.filter(entryInput.getsDocDate()), 
        		10, 
        		"Doc&nbsp;date:", 
        		"",
        		8
        		)
        );
		
		pwOut.println(SMUtilities.getDatePickerString(AREntryInput.ParamDocDate, context));
		pwOut.println("</TD>");

        //Entry amount:
		pwOut.println("<TD>");
		pwOut.println("Entry&nbsp;amt:&nbsp;" + entryInput.getsOriginalAmount());
		pwOut.println("</TD>");

		pwOut.println("<TD>");

        //Control Acct:
		pwOut.println("Control&nbsp;Acct:");
    	pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
    			+ AREntryInput.ParamControlAcct
    			+ "\" VALUE=\"" + entryInput.getsControlAcct() + "\">");
		pwOut.println(entryInput.getsControlAcct());
        pwOut.println("</TD>");

        //END ROW 1
        pwOut.println("</TR>");
        
        //START ROW 2
		//Terms:
        pwOut.println("<TD>");
        pwOut.println("Terms:&nbsp;(N/A)");
        pwOut.println("</TD>");

        //Due date
        pwOut.println("<TD>");
        pwOut.println("Due&nbsp;Date:&nbsp;(N/A)");
        pwOut.println("</TD>");
        
        //Display the out-of-balance amount:
        pwOut.println("<TD>");
        pwOut.println("Out&nbsp;of&nbsp;Balance:");
        pwOut.println(entryInput.getsUndistributedAmount());
        pwOut.println("</TD>");
		
        //Description:
        pwOut.println("<TD>");
        pwOut.println(clsCreateHTMLFormFields.Create_Edit_Form_Text_Input_Field(
        		AREntryInput.ParamDocDescription, 
        		clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(clsStringFunctions.filter(entryInput.getsDocDescription())), 
        		SMTableentries.sdocdescriptionLength, 
        		"Description:", 
        		"",
        		40
        		)
        );
        pwOut.println("</TD>");
        
        //END ROW 2:
        pwOut.println("</TR>");
        
        //START ROW 3:
        pwOut.println("<TR>");
        
        //Order Number:
    	pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
    			+ AREntryInput.ParamOrderNumber
    			+ "\" VALUE=\"" + entryInput.getsOrderNumber() + "\">");
    	if(entryInput.getsOrderNumber().trim().compareTo("") != 0){
	    	pwOut.println("<TD>Order #: " 
	    			+ "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" 
	    			+ entryInput.getsOrderNumber() 
	    			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
	    			+ "\">" + entryInput.getsOrderNumber() + "</A></TD>");
    	}else{
    		pwOut.println("<TD>&nbsp;</TD>");
    	}
    	
    	pwOut.println("<TD></TD>");
    	pwOut.println("<TD></TD>");
    	
        //END ROW 3:
        pwOut.println("</TR>");
        
        pwOut.println("</TABLE>");
	}
	private static void displayNonEditableEntryFields (
			PrintWriter pwOut,
			AREntryInput entryInput,
			ServletContext context,
			String sDBID
	){

		pwOut.println("<TABLE BORDER=1 CELLSPACING=2 style=\"font-size:75%\">");
		
		//START ROW 1
        //Doc Number:
		pwOut.println("<TD>Doc #: <B>" 
				+ clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(clsStringFunctions.filter(entryInput.getsDocNumber()))+ "</B></TD>");
        //Doc date:
		pwOut.println("<TD>Doc. date: <B>" + entryInput.getsDocDate() + "</B></TD>");
        //Original amt:
		pwOut.println("<TD>Entry amt: <B>" + entryInput.getsOriginalAmount() + "</B></TD>");
        //Control Acct:
		pwOut.println("<TD>Control acct: <B>" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(entryInput.getsControlAcct()) + "</B></TD>");

        //END ROW 1
		pwOut.println("</TR>");

        //START ROW 2
		pwOut.println("<TR>");

        //Terms:
		pwOut.println("<TD>Terms: <B>(N/A)</B></TD>");
        //Due date:
		pwOut.println("<TD>Due date: <B>(N/A)</B></TD>");
        //Out of balance amt:
		pwOut.println("<TD>Out of balance: <B>" + entryInput.getsUndistributedAmount() + "</B></TD>");		
		//Description:
		pwOut.println("<TD>Description: <B>"
        		+ clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(clsStringFunctions.filter(entryInput.getsDocDescription())) 
        		+ "</B></TD>");
        
        //END ROW 2:
		pwOut.println("</TR>");
		
        //START ROW 3:
        pwOut.println("<TR>");
        
        if (entryInput.getsOrderNumber().trim().compareTo("") != 0){
        	pwOut.println("<TD>Order #: " 
        			+ "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" 
        			+ entryInput.getsOrderNumber() 
        			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
        			+ "\">" + entryInput.getsOrderNumber() + "</A></TD>");
        }else{
        	pwOut.println("<TD>&nbsp;</TD>");
        }
        
        pwOut.println("<TD></TD>");
        pwOut.println("<TD></TD>");
        
        //END ROW 3:
        pwOut.println("</TR>");
		pwOut.println("</TABLE>");
	}
	private static void Display_Line_Header(
			PrintWriter pwOut,
			AREntryInput entryInput
	){
		pwOut.println(SMUtilities.getMasterStyleSheetLink());
		pwOut.println("<TABLE WIDTH=100% BGCOLOR=\"#FFFFFF\" CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\">");
		pwOut.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		//Apply to doc
		pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Apply&nbsp;to<br>Doc #</TD>");
		//Apply to doc ID
		pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Apply&nbsp;to<br>Doc&nbsp;ID</TD>");
		//Doc type
		pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">");
		pwOut.println("Doc<br>type");
		pwOut.println("</TD>");
		//Order number
		pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">");
		pwOut.println("Order<br>number");
		pwOut.println("</TD>");
		//Original amount
		pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">");
		pwOut.println("Original<br>amount");
		pwOut.println("</TD>");
		//Current amount
		pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">");
		pwOut.println("Current<br>amount");
		pwOut.println("</TD>");
		//Net amount
		pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">");
		pwOut.println("Net<br>amount");
		pwOut.println("</TD>");
    	//Line amount
		pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">Amount</TD>");
		//Line desc
		pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Description</TD>");
		//Line comment
		pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Comment</TD>");
		pwOut.println("</TR>");

	}
	private static boolean Display_NONEditable_Lines(
			PrintWriter pwOut,
			AREntryInput entryInput,
			ServletContext context,
			String sDBID
	){
		
        //Display the line header:
		Display_Line_Header(pwOut, entryInput);
		//We DON'T show the first line (i = 0) because it is the 'apply-from' line:
        for (int i = 0; i < entryInput.getLineCount(); i++){
        	ARLineInput line = entryInput.getLine(i);

    		//Apply to doc
    		//Apply to doc ID
        	//Doc type
    		//Order number
    		//Original amount
    		//Current amount
    		//Net amount
        	//Line amount
    		//Line desc
    		//Line comment
        	
        	//Apply to doc #:
        	if(i % 2 == 0) {
            	pwOut.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
        	}else {
            	pwOut.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
        	}

        	pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
       		pwOut.println(clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(clsStringFunctions.filter(line.getDocAppliedTo())));
        	pwOut.println("</TD>");

        	//Apply to doc ID:
        	pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
        	pwOut.println(clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(clsStringFunctions.filter(line.getDocAppliedToID())));
        	pwOut.println("</TD>");
        	
			if (line.getDocAppliedToID().compareTo("-1") != 0){
				ARTransaction trans = new ARTransaction(line.getDocAppliedToID());
				if(!trans.load(context, sDBID)){
					pwOut.println("Error loading transaction with ID: " + line.getDocAppliedToID());
					//System.out.println("In AREditApplyToEntry - Error loading transaction with ID: " + line.getDocAppliedToID());
					return false;
				}
				
				//Doc type
	        	pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
				pwOut.println(ARDocumentTypes.getSourceTypes(trans.getiDocType()));
				pwOut.println("</TD>");
				
				//Order number
	        	pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
				String sOrderNumber = trans.getOrderNumber();
				if(sOrderNumber.length() > 0){
					pwOut.println(
							"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" 
							+ sOrderNumber 
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
							+ "\">" + sOrderNumber + "</A>"
					);
				}else{
					pwOut.println("&nbsp;");
				}
				pwOut.println("</TD>");
				//Original amount
	        	pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
				pwOut.println(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(trans.getdOriginalAmount()));
				pwOut.println("</TD>");
				//Current amount
	        	pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
				pwOut.println(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(trans.getdCurrentAmount()));
				pwOut.println("</TD>");
				//Net amount
	        	pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
				pwOut.println(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(trans.getNetAmount(context, sDBID)));
				pwOut.println("</TD>");
			}else{
				pwOut.println("&nbsp;");
				pwOut.println("</TD>");
				//Original amount
	        	pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
				pwOut.println("0.00");
				pwOut.println("</TD>");
				//Current amount
	        	pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
				pwOut.println("0.00");
				pwOut.println("</TD>");
				//Net amount
	        	pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
				pwOut.println("0.00");
				pwOut.println("</TD>");
			}
        	
        	//Amount:
        	pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
        	pwOut.println(line.getAmount());
        	pwOut.println("</TD>");
        	
        	//Description:
        	pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
        	pwOut.println(clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(clsStringFunctions.filter(line.getDescription())));
        	pwOut.println("</TD>");
        	
        	//Comment:
        	pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
        	pwOut.println(clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(clsStringFunctions.filter(line.getComment())));
        	pwOut.println("</TD>");
        	
        	pwOut.println("</TR>");
        }

        pwOut.println("</TABLE>");

		return true;
	}
	private static boolean displayTransactionLines(
			PrintWriter pwOut,
			AREntryInput entryInput,
			ServletContext context,
			String sDBID
	){

		//We want these to be empty in these lines:
		String m_sApplyToDocID = "-1";
		//  Get the lines by reading the database:
		//Show all the lines here:
		int iLineIndex = 0;
        for (int i = 0; i < entryInput.getLineCount(); i++){
        	ARLineInput line = entryInput.getLine(iLineIndex);
        	
        	pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
        			+ ARLineInput.ParamDocAppliedTo 
        			+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6) 
        			+ "\" VALUE=\"" + line.getDocAppliedTo() + "\">");
        	pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
        			+ ARLineInput.ParamLineDocAppliedToID 
        			+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6) 
        			+ "\" VALUE=\"" + line.getDocAppliedToID() + "\">");
        	
        	pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
        			+ ARLineInput.ParamLineApplyToOrderNumber
        			+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6) 
        			+ "\" VALUE=\"" + line.getApplyToOrderNumber() + "\">");

        	//GL Acct:
        	pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
        			+ ARLineInput.ParamDistAcct 
        			+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6) 
        			+ "\" VALUE=\"" + line.getLineAcct() + "\">");

        	pwOut.println("<TR>");

    		//Apply to doc
    		//Apply to doc ID
    		//Doc type
        	//Order number
    		//Original amount
    		//Current amount
    		//Net amount
        	//Line amount
    		//Line desc
    		//Line comment
        	
        	pwOut.println("<TD>");

			pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "" 
				+ SMUtilities.lnViewInvoice(sDBID, line.getDocAppliedTo() )
	    		+ "\">"
	    		+ line.getDocAppliedTo()
	    		+ "</A>");
        	pwOut.println("</TD>");

        	pwOut.println("<TD>");
        	pwOut.println(line.getDocAppliedToID());
        	pwOut.println("</TD>");
        	
			String sDocType = "";
			String sOrderNumber = "";
			String sOriginalAmount = "0.00";
			String sCurrentAmount = "0.00";
			String sNetAmount = "0.00";
			
			if(!line.getDocAppliedToID().equalsIgnoreCase("-1")){
				ARTransaction trans = new ARTransaction(line.getDocAppliedToID());
				if(!trans.load(context, sDBID)){
					pwOut.println("Error loading transaction with ID: " + line.getDocAppliedToID());
					//System.out.println("In AREditApplyToEntry - Error loading transaction with ID: " + line.getDocAppliedToID());
					return false;
				}
				sDocType = ARDocumentTypes.getSourceTypes(trans.getiDocType());
				sOrderNumber = trans.getOrderNumber();
				sOriginalAmount = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(trans.getdOriginalAmount());
				sCurrentAmount = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(trans.getdCurrentAmount());
				sNetAmount = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(trans.getNetAmount(context, sDBID));
			}
			
			//Doc type:
			pwOut.println("<TD>");
			pwOut.println(sDocType);
			pwOut.println("</TD>");
			
			//Order number
			pwOut.println("<TD>");
			if(sOrderNumber.length() > 0){
				pwOut.println(
						"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" 
						+ sOrderNumber 
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "\">" + sOrderNumber + "</A>"
				);
			}else{
				pwOut.println("&nbsp;");
			}
			pwOut.println("</TD>");
			//Original amount
			pwOut.println("<TD ALIGN=RIGHT>");
			pwOut.println(sOriginalAmount);
			pwOut.println("</TD>");
			//Current amount
			pwOut.println("<TD ALIGN=RIGHT>");
			pwOut.println(sCurrentAmount);
			pwOut.println("</TD>");
			//Net amount
			pwOut.println("<TD ALIGN=RIGHT>");
			pwOut.println(sNetAmount);
			pwOut.println("</TD>");
            
			pwOut.println("<TD ALIGN=RIGHT>");
        	//Amount:
            pwOut.println(clsCreateHTMLFormFields.Create_Edit_Form_Text_Input_Field(
	        			ARLineInput.ParamLineAmt 
	        				+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6), 
	        			line.getAmount(), 
	        			9, 
	        			"", 
	        			"",
	        			9
	        			)
	        	);
            pwOut.println("</TD>");

        	//Description:
            pwOut.println("<TD>");
            pwOut.println(clsCreateHTMLFormFields.Create_Edit_Form_Text_Input_Field(
        			ARLineInput.ParamLineDesc 
        				+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6), 
        				clsStringFunctions.filter(line.getDescription()), 
        			25, 
        			"", 
        			""
        			)
        	);
            pwOut.println("</TD>");

        	//Comment:
            pwOut.println("<TD>");
            pwOut.println(clsCreateHTMLFormFields.Create_Edit_Form_Text_Input_Field(
        			ARLineInput.ParamLineComment 
        				+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6), 
        				clsStringFunctions.filter(line.getComment()), 
        			25, 
        			"", 
        			""
        			)
        	);
            pwOut.println("</TD>");
        	
            pwOut.println("</TR>");
        	iLineIndex ++;
        }
        
        int iNumberOfBlankLines = 3;
        for (int iLines = 1; iLines <= iNumberOfBlankLines; iLines++){

        	pwOut.println("<TR>");
			pwOut.println("<TD>");
			
			//The GL account is normally the AR account, but COULD be the Customer Deposit account
			//if the line is a prepay - we'll leave it blank and let it be set when we save these lines:
        	pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
        			+ ARLineInput.ParamDistAcct 
        			+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6) 
        			+ "\" VALUE=\"" + "" + "\">");
			
    		//Apply to doc
    		//Apply to doc ID
        	//Doc type
    		//Order number
    		//Original amount
    		//Current amount
    		//Net amount
        	//Line amount
    		//Line desc
    		//Line comment
			
			//Apply to doc
            pwOut.println(clsCreateHTMLFormFields.Create_Edit_Form_Text_Input_Field(
        			ARLineInput.ParamDocAppliedTo
        				+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6), 
        				"", 
        			12, 
        			"", 
        			"",
        			12
        			)
        	);

			pwOut.println("</TD>");

			//Apply to ID:
			pwOut.println("<TD>");
			pwOut.println(m_sApplyToDocID);
			pwOut.println("</TD>");

			//Doc type
			pwOut.println("<TD>");
			pwOut.println("&nbsp;");
			pwOut.println("</TD>");
			//Order number
			pwOut.println("<TD>");
			pwOut.println("&nbsp;");
			pwOut.println("</TD>");
			//Original amount
			pwOut.println("<TD>");
			pwOut.println("&nbsp;");
			pwOut.println("</TD>");
			//Current amount
			pwOut.println("<TD>");
			pwOut.println("&nbsp;");
			pwOut.println("</TD>");
			//Net amount
			pwOut.println("<TD>");
			pwOut.println("&nbsp;");
			pwOut.println("</TD>");
			            
        	//Amount:
            pwOut.println("<TD ALIGN=\"RIGHT\">");
            pwOut.println(clsCreateHTMLFormFields.Create_Edit_Form_Text_Input_Field(
        			ARLineInput.ParamLineAmt 
        				+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6), 
        			"0.00", 
        			9, 
        			"", 
        			"",
        			9
        			)
        	);
        	
            pwOut.println("</TD>");

        	//Description:
            pwOut.println("<TD>");
            pwOut.println(clsCreateHTMLFormFields.Create_Edit_Form_Text_Input_Field(
        			ARLineInput.ParamLineDesc 
        				+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6), 
        				"", 
        			25, 
        			"", 
        			""
        			)
        	);
            pwOut.println("</TD>");

        	//Comment:
            pwOut.println("<TD>");
            pwOut.println(clsCreateHTMLFormFields.Create_Edit_Form_Text_Input_Field(
        			ARLineInput.ParamLineComment 
        				+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6), 
        				"", 
        			25, 
        			"", 
        			""
        			)
        	);
            pwOut.println("</TD>");
            pwOut.println("</TR>");	
        	iLineIndex ++;
		}
		//Now add lines for all the invoices or retainage or prepays or receipts with a current amount: 
		String sSQL = "SELECT *" 
			+ " FROM " + SMTableartransactions.TableName
			+ " WHERE ("
				+ "(" + SMTableartransactions.spayeepayor + " = '" + entryInput.getsCustomerNumber() + "')"
				+ " AND (" 
					+ "(" + SMTableartransactions.idoctype + " = " + ARDocumentTypes.INVOICE_STRING + ")"
					+ " OR (" + SMTableartransactions.idoctype + " = " + ARDocumentTypes.RETAINAGE_STRING + ")"
					+ " OR (" + SMTableartransactions.idoctype + " = " + ARDocumentTypes.PREPAYMENT_STRING + ")"
					+ " OR (" + SMTableartransactions.idoctype + " = " + ARDocumentTypes.RECEIPT_STRING + ")"
					+ " OR (" + SMTableartransactions.idoctype + " = " + ARDocumentTypes.CREDIT_STRING + ")"
				+ ")"
				+ " AND (" + SMTableartransactions.dcurrentamt + " != 0.00)"
				+ ")";
        try{
        	ResultSet rs = clsDatabaseFunctions.openResultSet(
        		sSQL, 
        		context, 
        		sDBID,
        		"MySQL",
        		"AREditApplyToEntry.displayTransactionLines (1)");
	        
			//Print out directly so that we don't waste time appending to string buffers:
	        while (rs.next()){
	        	int iDocType = rs.getInt(SMTableartransactions.idoctype);
	        	if (documentIsAlreadyListed(entryInput, rs.getString(SMTableartransactions.sdocnumber))){
	        	}else{
					pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
		        			+ ARLineInput.ParamDocAppliedTo 
		        			+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6) 
		        			+ "\" VALUE=\"" + rs.getString(SMTableartransactions.sdocnumber) + "\">");
					pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
		        			+ ARLineInput.ParamLineDocAppliedToID 
		        			+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6) 
		        			+ "\" VALUE=\"" + Long.toString(rs.getLong(SMTableartransactions.lid)) + "\">");
			
					//On an apply-to, the GL for the entry AND the lines is the AR account unless
					//it's a prepay.
					String sLineGLAcct = entryInput.getsControlAcct();
					if (iDocType == ARDocumentTypes.PREPAYMENT){
						ARCustomer cust = new ARCustomer(entryInput.getsCustomerNumber());
						if (!cust.load(context, sDBID)){
							return false;
						}
						sLineGLAcct = cust.getARPrepayLiabilityAccount(context, sDBID);
					}

		        	pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
		        			+ ARLineInput.ParamDistAcct 
		        			+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6) 
		        			+ "\" VALUE=\"" + sLineGLAcct + "\">");
		        	
					pwOut.println("<TR>");
					pwOut.println("<TD>");
					String sDocNumber = rs.getString(SMTableartransactions.sdocnumber);
					pwOut.println(
							"<INPUT TYPE=CHECKBOX NAME=\""
							+ ARLineInput.ParamLineApplyCashToChk
							+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6)
							+ "\" >" 
							+ "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "" 
							+ SMUtilities.lnViewInvoice(sDBID, sDocNumber )
				    		+ "\">"
				    		+ sDocNumber
				    		+ "</A>"
					);
					pwOut.println("</TD>");

					pwOut.println("<TD>");
					pwOut.println(Long.toString(rs.getLong(SMTableartransactions.lid)));
					pwOut.println("</TD>");

					//Doc type:
					pwOut.println("<TD>");
					pwOut.println(ARDocumentTypes.getSourceTypes(iDocType));
					pwOut.println("</TD>");
					
					//Order #
					pwOut.println("<TD>");
					String sOrderNumber = rs.getString(SMTableartransactions.sordernumber).trim();
					if(sOrderNumber.length() > 0){
						pwOut.println(
								"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" 
								+ sOrderNumber 
								+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
								+ "\">" + sOrderNumber + "</A>"
						);
					}else{
						pwOut.println("&nbsp;");
					}
					pwOut.println("</TD>");
					//Original amt:
					pwOut.println("<TD ALIGN=RIGHT>");
					pwOut.println(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableartransactions.doriginalamt)));
					pwOut.println("</TD>");
					
					//Current amt:
					pwOut.println("<TD ALIGN=RIGHT>");
					pwOut.println(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableartransactions.dcurrentamt)));
					pwOut.println("</TD>");
					
					//Net:
					pwOut.println("<TD ALIGN=RIGHT>");
					ARTransaction trans = new ARTransaction(Long.toString(rs.getLong(SMTableartransactions.lid)));
					if(!trans.load(context, sDBID)){
						pwOut.println("Error loading transaction with ID: " + trans.getsTransactionID());
						//System.out.println("In ARCreateEntryForm - Error loading existing invoices: transaction with ID: " + trans.getsTransactionID());
						return false;
					}
					pwOut.println(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(trans.getNetAmount(context, sDBID)));
					pwOut.println("</TD>");
					
		        	//Amount:
		            pwOut.println("<TD ALIGN=\"RIGHT\">");
		            pwOut.println(clsCreateHTMLFormFields.Create_Edit_Form_Text_Input_Field(
		        			ARLineInput.ParamLineAmt 
		        				+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6), 
		        			"0.00", 
		        			9, 
		        			"", 
		        			"",
		        			9
		        			)
		        	);
		        	
		            pwOut.println("</TD>");

		        	//Description:
		            pwOut.println("<TD>");
		            pwOut.println(clsCreateHTMLFormFields.Create_Edit_Form_Text_Input_Field(
		        			ARLineInput.ParamLineDesc 
		        				+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6), 
		        				"", 
		        			25, 
		        			"", 
		        			""
		        			)
		        	);
		            pwOut.println("</TD>");

		        	//Comment:
		            pwOut.println("<TD>");
		            pwOut.println(clsCreateHTMLFormFields.Create_Edit_Form_Text_Input_Field(
		        			ARLineInput.ParamLineComment 
		        				+ clsStringFunctions.PadLeft(Integer.toString(iLineIndex), "0", 6), 
		        				"", 
		        			25, 
		        			"", 
		        			""
		        			)
		        	);
		            pwOut.println("</TD>");
		            pwOut.println("</TR>");	
		        	iLineIndex ++;
	        	}
			}
	        rs.close();

			}catch (SQLException ex){
		    	System.out.println("Error in ARCreateEntryForm.loadGLList!!");
		        System.out.println("SQLException: " + ex.getMessage());
		        System.out.println("SQLState: " + ex.getSQLState());
		        System.out.println("SQL: " + ex.getErrorCode());
				return false;
		}			
		
    	//Record the number of lines:
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AREntryInput.ParamNumberOfLines + "\" VALUE='" + Integer.toString(iLineIndex) + "'>");
		
		return true;
	}
	private static boolean documentIsAlreadyListed(AREntryInput entryInput, String sDocNumber){
		
        for (int i = 0; i < entryInput.getLineCount(); i++){
        	ARLineInput line = entryInput.getLine(i);
        	if (line.getDocAppliedTo().equalsIgnoreCase(sDocNumber)){
        		return true;
        	}
        }
        //If we never get a match, just return false:
        return false;
	}
	private static void addCommandButtons(AREntryInput enInput, PrintWriter pwOut){
    	pwOut.println("<BR><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Save Entry' STYLE='height: 0.24in'>");
    	if(enInput.getsDocumentType().compareToIgnoreCase(ARDocumentTypes.RECEIPT_STRING) == 0){
    		pwOut.println("<INPUT TYPE=SUBMIT NAME='SaveAndAdd' VALUE='Save and add' STYLE='height: 0.24in'>");
    	}
    	pwOut.println("  <INPUT TYPE=SUBMIT NAME='Delete' VALUE='Delete Entry' STYLE='height: 0.24in'>");
    	pwOut.println("  Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"ConfirmDelete\">");
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

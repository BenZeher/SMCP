package smic;

import SMDataDefinition.*;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class ICEditReceiptEntry extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	private static String sObjectName = "Receipt Entry";
	
	private String m_sBatchNumber;
	private String m_sEntryNumber;
	private String m_sEditable;
	private String m_sBatchType;
	private String m_sWarning;
	private ICEntry m_Entry;
	private PrintWriter m_pwOut;
	private HttpServletRequest m_hsrRequest;
	private boolean m_bIsNewEntry = false;
	private boolean m_bEditable = false;

	private static String sDBID = "";
	private static String sUserID = "";
	private static String sUserFullName = "";
	private static String sCompanyName = "";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		m_pwOut = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICEditReceipts))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		
	    //If there is no EntryInput in the session, we'll get a null in m_EntryInput:
		m_Entry = (ICEntry) CurrentSession.getAttribute("EntryInput");
		//System.out.println("In " + this.toString() + " m_Entry = " + m_Entry);
		//Get rid of the session variable immediately:
		CurrentSession.removeAttribute("EntryInput");
	    
		//Also get rid of any lines in the session:
		CurrentSession.removeAttribute("EntryLine");
		m_hsrRequest = request;
	    get_request_parameters();
	    
		//Try to load an ICEntryInput object from which to build the form:
		if (!loadICEntryInput()){
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatchesEdit"
					+ "?BatchNumber=" + m_sBatchNumber
					+ "&BatchType=" + m_sBatchType
					+ "&Warning=" + m_sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=Could not process entry - " + m_sWarning
				);
			
			return;
		}

	    String title;
	    String subtitle = "";
	    if (m_bIsNewEntry){
	    	title = "Edit NEW " + sObjectName;
	    }else{
	    	title = "Edit " + sObjectName + ": " + m_Entry.sEntryNumber();	
	    }

	    m_pwOut.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    m_pwOut.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    
		//If there is a warning from trying to input previously, print it here:
		if (! m_sWarning.equalsIgnoreCase("")){
			m_pwOut.println("<B><FONT COLOR=\"RED\">WARNING: " + m_sWarning + "</FONT></B><BR>");
		}
		
	    //Print a link to main menu:
		m_pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">Return to Inventory Main Menu</A><BR>");
	    
	    //Print a link to return to the 'edit batch' page:
		m_pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatchesEdit" 
	    		+ "?BatchNumber=" + m_Entry.sBatchNumber()
	    		+ "&BatchType=" + m_Entry.sBatchType()
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "\">Return to Edit Batch " + m_Entry.sBatchNumber() + "</A><BR><BR>");

		//Try to construct the rest of the screen form from the AREntryInput object:
		if (!createFormFromEntryInput()){
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatchesEdit"
					+ "?BatchNumber=" + m_Entry.sBatchNumber()
					+ "&BatchType=" + m_sBatchType
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning
				);
			return;
		}
		
		//End the page:
		m_pwOut.println("</BODY></HTML>");
	}
	private boolean loadICEntryInput(){
		
		//If the class has NOT been passed an AREntryInput query string, we'll have to build it:
		if (m_Entry == null){

			//Have to construct the AREntryInput object here:
			m_Entry = new ICEntry();
			if (m_bIsNewEntry){
				//If it's a new entry:
				m_Entry.sBatchNumber(m_sBatchNumber);
				m_Entry.sBatchType(m_sBatchType);
				m_Entry.sEntryDescription("Receipt");
				m_Entry.sEntryType(Integer.toString(ICEntryTypes.RECEIPT_ENTRY));
				m_Entry.sEntryNumber("-1");
				m_Entry.slid("-1");
				
				//System.out.println("In " + this.toString() + ".loadICEntryInput - 01");
				//Get the batch date as the default entry date:
				ICEntryBatch batch = new ICEntryBatch(m_Entry.sBatchNumber());
				if(!batch.load(getServletContext(), sDBID)){
					//System.out.println("In " + this.toString() + ".loadICEntryInput - 02");
					m_Entry.sEntryDate(clsDateAndTimeConversions.now("MM/dd/yyyy"));
				}else{
					//System.out.println("In " + this.toString() + ".loadICEntryInput - 03");
					m_Entry.sEntryDate(batch.getBatchDateInStdFormat());
				}
			}else{
				
				//If it's an existing entry:
				//Load the existing entry:
				
				if (!m_Entry.load(m_sBatchNumber, m_sEntryNumber, getServletContext(), sDBID)){
			    	m_sWarning = "Could not load entry with batch number " + m_sBatchNumber + ", entry number " + m_sEntryNumber;
			    	m_sWarning += "\n" + m_Entry.getErrorMessage();
			    	return false;
				}
				//System.out.println("In " + this.toString() + ".loadICEntryInput - dump: " + m_Entry.read_out_debug_data());
			}
		}
		
		return true;
	}
	private boolean createFormFromEntryInput(){
		
	    //Start the entry edit form:
		m_pwOut.println("<FORM NAME='ENTRYEDIT' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEntryUpdate' METHOD='POST'>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		//Record the hidden fields for the entry edit form:
	    storeHiddenFieldsOnForm ();

        //Display the entry header fields:
	    displayEntryHeaderFields ();
   
        if (m_bEditable){
        	displayEditableEntryFields ();
        }
        //Else, if the record is NOT editable:
        else{
        	displayNonEditableEntryFields ();
        }
        
	    //Now display the transaction lines:
        //Display the line header:
	    Display_Line_Header();

	    //Display all the current transaction lines:
	    if (!displayLines(m_bEditable)){
	    	return false;
	    }
	    
        //Add a link for adding a line:
	    if(
	    	(m_Entry.lEntryNumber() > -1)
	    	&& (m_bEditable)
	    ){
			m_pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditReceiptLine" 
   	    		+ "?BatchNumber=" + m_Entry.sBatchNumber()
   	    		+ "&EntryNumber=" + m_Entry.sEntryNumber()
   	    		+ "&LineNumber=-1"
   	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
   	    		+ "\">" + "New line" + "</A>");
	    }

	    m_pwOut.println("</TABLE>");
	    //End the entry edit form:
	    m_pwOut.println("</FORM>");  
	    
		return true;
	}
	private void storeHiddenFieldsOnForm(){
		
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICEntry.ParamBatchNumber + "\" VALUE=\"" + m_Entry.sBatchNumber() + "\">");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICEntry.ParamEntryNumber + "\" VALUE=\"" + m_Entry.sEntryNumber() + "\">");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICEntry.ParamBatchType + "\" VALUE=\"" + m_Entry.sBatchType() + "\">");
	    String sEditable;
	    if (m_bEditable){
	    	sEditable = "Yes";
	    }else{
	    	sEditable = "No";
	    }
	    m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"Editable\" VALUE=\"" + sEditable + "\">");
	    m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICEntry.ParamEntryID + "\" VALUE=\"" + m_Entry.slid() + "\">");
	    m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICEntry.ParamBatchType + "\" VALUE=\"" + m_Entry.sBatchType() + "\">");
	    m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICEntry.ParamEntryType + "\" VALUE=\"" + m_Entry.sEntryType() + "\">");
	    m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICEntry.ParamEntryAmount + "\" VALUE=" + "0.00" + ">");
	    m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICEntry.ParamNumberOfLines + "\" VALUE=" 
	    	+ Long.toString(m_Entry.lNumberOfLines()) + ">");
	    m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" + "ICEditReceiptEntry" + "\">");
	}
	private void displayEntryHeaderFields (){
		
		m_pwOut.println("<B>" + ICBatchTypes.Get_Batch_Type(Integer.parseInt(m_Entry.sBatchType())) + "</B>");
		m_pwOut.println(" batch number: <B>" + m_Entry.sBatchNumber() + "</B>;");
        if (m_Entry.sEntryNumber().equalsIgnoreCase("-1")){
        	m_pwOut.println(" entry number: <B>NEW</B>.  ");
        }else{
        	m_pwOut.println(" entry number: <B>" + m_Entry.sEntryNumber() + "</B>.");
        }
    	    	
        m_pwOut.println(" Document type: <B>" 
        		+ ICEntryTypes.Get_Entry_Type(Integer.parseInt(m_Entry.sEntryType()))
        		+ "</B>.  ");

	    if (m_bEditable){
	    	m_pwOut.println("<BR><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Save " + sObjectName + "' STYLE='height: 0.24in'>");
	    	m_pwOut.println("  <INPUT TYPE=SUBMIT NAME='Delete' VALUE='Delete " + sObjectName + "' STYLE='height: 0.24in'>");
	    	m_pwOut.println("  Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"ConfirmDelete\">");
        }
	}
	private void displayEditableEntryFields(){
		
		m_pwOut.println("<TABLE BORDER=1 CELLSPACING=2 style=\"font-size:75%\">");
		
        //START ROW 1
		m_pwOut.println("<TR>");
        
        //Doc Number:
		m_pwOut.println("<TD>Document number:&nbsp;"
				+ "<INPUT TYPE=TEXT NAME=\"" + ICEntry.ParamDocNumber + "\""
				+ " VALUE=\"" + clsStringFunctions.filter(m_Entry.sDocNumber()) + "\""
				+ " MAXLENGTH=" + SMTableicbatchentries.sdocnumberLength + ">"
				+ "</TD>"
			);

        //Entry date:
		m_pwOut.println("<TD>Entry date:&nbsp;"
			+ "<INPUT TYPE=TEXT NAME=\"" + ICEntry.ParamEntryDate + "\""
			+ " VALUE=\"" + clsStringFunctions.filter(m_Entry.sStdEntryDate()) + "\""
			+ " MAXLENGTH=10>"
			+ SMUtilities.getDatePickerString(ICEntry.ParamEntryDate, getServletContext())
			+ "</TD>"
		);

		//Description:
		m_pwOut.println("<TD>Description:&nbsp;"
				+ "<INPUT TYPE=TEXT NAME=\"" + ICEntry.ParamEntryDescription + "\""
				+ " VALUE=\"" + clsStringFunctions.filter(m_Entry.sEntryDescription()) + "\""
				+ " MAXLENGTH=" + SMTableicbatchentries.sentrydescriptionLength + ">"
				+ "</TD>"
			);
		
		//Entry net amount:
		m_pwOut.println("<TD>Net cost total:&nbsp;"
				+ m_Entry.sEntryAmount()
				+ "</TD>"
			);
        
        //END ROW 1:
        m_pwOut.println("</TR>");
        
        m_pwOut.println("</TABLE>");
	}

	public void displayNonEditableEntryFields (){

		m_pwOut.println("<TABLE BORDER=1 CELLSPACING=2 style=\"font-size:75%\">");
		
		//START ROW 1
        //Doc Number:
		m_pwOut.println("<TD>Doc #: <B>" + clsStringFunctions.filter(m_Entry.sDocNumber())+ "</B></TD>");
        //Doc date:
		m_pwOut.println("<TD>Entry date:<B>" + m_Entry.sStdEntryDate() + "</B></TD>");
        //Original amt:
		m_pwOut.println("<TD>Entry amt: <B>" + m_Entry.sEntryAmount() + "</B></TD>");
        //Control Acct:

        //END ROW 1
		m_pwOut.println("</TR>");

        //START ROW 2
		m_pwOut.println("<TR>");

		//Description:
		m_pwOut.println("<TD>Description: <B>"
        	+ clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(clsStringFunctions.filter(m_Entry.sEntryDescription())) 
        	+ "</B></TD>");
        
        //END ROW 2:
		m_pwOut.println("</TR>");
		m_pwOut.println("</TABLE>");
	}
	private void Display_Line_Header(){
		m_pwOut.println("<TABLE BORDER=1 CELLSPACING=2>");
		m_pwOut.println("<TR>");
		m_pwOut.println("<TD><FONT SIZE=2><B><U>Line #</B></U></FONT></TD>");
		m_pwOut.println("<TD><FONT SIZE=2><B><U>Item #</B></U></FONT></TD>");
		m_pwOut.println("<TD><FONT SIZE=2><B><U>Item description</B></U></FONT></TD>");
		m_pwOut.println("<TD><FONT SIZE=2><B><U>Location</B></U></FONT></TD>");
		m_pwOut.println("<TD><FONT SIZE=2><B><U>Receipt number</B></U></FONT></TD>");
		m_pwOut.println("<TD><FONT SIZE=2><B><U>U/M</B></U></FONT></TD>");
		m_pwOut.println("<TD><FONT SIZE=2><B><U>Qty</B></U></FONT></TD>");
		m_pwOut.println("<TD><FONT SIZE=2><B><U>Cost</B></U></FONT></TD>");
		m_pwOut.println("<TD><FONT SIZE=2><B><U>Distribution Acct.</B></U></FONT></TD>");
		m_pwOut.println("<TD><FONT SIZE=2><B><U>Description</B></U></FONT></TD>");
		m_pwOut.println("<TD><FONT SIZE=2><B><U>Comment</B></U></FONT></TD>");
		m_pwOut.println("</TR>");
	}
	private boolean displayLines(boolean bEditable){
		
        //Display the line header:
        for (int i = 0; i < m_Entry.getLineCount(); i++){
        	ICEntryLine line = m_Entry.getLineByIndex(i);

        	//Line #:
        	m_pwOut.println("<TR>");
        	m_pwOut.println("<TD><FONT SIZE=2>");
        	if (bEditable){
        		m_pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditReceiptLine" 
       	    		+ "?BatchNumber=" + line.sBatchNumber()
       	    		+ "&EntryNumber=" + line.sEntryNumber()
       	    		+ "&LineNumber=" + line.sLineNumber()
       	    		+ "&BatchType=" + m_Entry.sBatchType()
       	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
       	    		+ "\">" + line.sLineNumber()+ "</A>");
        	}else{
        		m_pwOut.println(line.sLineNumber());
        	}
        	m_pwOut.println("</FONT></TD>");
        	
        	//Item #:
        	m_pwOut.println("<TD><FONT SIZE=2>");
        	m_pwOut.println(clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(clsStringFunctions.filter(line.sItemNumber())));
        	m_pwOut.println("</FONT></TD>");

        	//Description:
        	//First, read the item detail info:
        	line.getItemDetails(getServletContext(), sDBID, sUserID, sUserFullName);
        	m_pwOut.println("<TD><FONT SIZE=2>");
        	m_pwOut.println(line.sItemDescription());
        	m_pwOut.println("</FONT></TD>");
        	
        	//Location:
        	m_pwOut.println("<TD><FONT SIZE=2>");
        	m_pwOut.println(clsStringFunctions.filter(line.sLocation()));
        	m_pwOut.println("</FONT></TD>");
        	
        	//Receipt number:
        	m_pwOut.println("<TD><FONT SIZE=2>");
        	m_pwOut.println(line.sReceiptNum());
        	m_pwOut.println("</FONT></TD>");

        	//U/M:
        	m_pwOut.println("<TD><FONT SIZE=2>");
        	m_pwOut.println(line.sUnitOfMeasure());
        	m_pwOut.println("</FONT></TD>");
        	
        	//Qty:
        	m_pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>");
        	m_pwOut.println(line.sQtySTDFormat());
        	m_pwOut.println("</FONT></TD>");
        	
        	//Cost:
        	m_pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>");
        	m_pwOut.println(line.sCostSTDFormat());
        	m_pwOut.println("</FONT></TD>");

        	//Distribution acct:
        	m_pwOut.println("<TD><FONT SIZE=2>");
        	m_pwOut.println(clsStringFunctions.filter(line.sDistributionAcct()));
        	m_pwOut.println("</FONT></TD>");

        	//Description:
        	m_pwOut.println("<TD><FONT SIZE=2>");
        	m_pwOut.println(clsStringFunctions.filter(line.sDescription()));
        	m_pwOut.println("</FONT></TD>");

        	//Comment:
        	m_pwOut.println("<TD><FONT SIZE=2>");
        	m_pwOut.println(clsStringFunctions.filter(line.sComment()));
        	m_pwOut.println("</FONT></TD>");

        	m_pwOut.println("</TR>");
        }

        m_pwOut.println("</TABLE>");

		return true;
	}
	private void get_request_parameters(){
 
		if (m_hsrRequest.getParameter("EntryNumber") != null){
			//System.out.println("In " + this.toString() + " != null");
			if (clsManageRequestParameters.get_Request_Parameter("EntryNumber", m_hsrRequest).equalsIgnoreCase("-1")){
				//System.out.println("In " + this.toString() + " = -1");
				m_bIsNewEntry = true; 
			}else{
				//System.out.println("In " + this.toString() + " != -1");
				m_bIsNewEntry = false;
			}
		}else{
			m_bIsNewEntry = true;
			//System.out.println("In " + this.toString() + " - didn't get parameter EntryNumber");
		}

		m_sBatchNumber = clsManageRequestParameters.get_Request_Parameter("BatchNumber", m_hsrRequest);
		m_sEntryNumber = clsManageRequestParameters.get_Request_Parameter("EntryNumber", m_hsrRequest);
		m_sEditable = clsManageRequestParameters.get_Request_Parameter("Editable", m_hsrRequest);
		if (m_sEditable.compareToIgnoreCase("Yes") == 0){
			m_bEditable = true;
		}else {
			m_bEditable = false;
		}
		m_sBatchType = clsManageRequestParameters.get_Request_Parameter("BatchType", m_hsrRequest);
		m_sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", m_hsrRequest);
		
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

package smic;

import SMDataDefinition.*;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMUtilities;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ICEditShipmentEntry extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final String sObjectName = "Shipment Entry";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		PrintWriter m_pwOut;
		m_pwOut = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				-1L))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		
	    //local variables
	    String m_sBatchNumber;
		String m_sEntryNumber;
		String m_sEditable;
		String m_sBatchType;
		String m_sWarning;
		ICEntry m_Entry;
		
		HttpServletRequest m_hsrRequest = request;
		boolean m_bIsNewEntry = false;
		boolean m_bEditable = false;
		
	    //If there is no EntryInput in the session, we'll get a null in m_EntryInput:
		m_Entry = (ICEntry) CurrentSession.getAttribute("EntryInput");
		//System.out.println("In " + this.toString() + " m_Entry = " + m_Entry);
		//Get rid of the session variable immediately:
		CurrentSession.removeAttribute("EntryInput");
	    
		//Also get rid of any lines in the session:
		CurrentSession.removeAttribute("EntryLine");
		//get request parameters
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
	    
		//Try to load an ICEntryInput object from which to build the form:
		try {
			if (m_Entry == null){

				//Have to construct the ICEntryInput object here:
				m_Entry = new ICEntry();
				if (m_bIsNewEntry){
					//If it's a new entry:
					m_Entry.sBatchNumber(m_sBatchNumber);
					m_Entry.sBatchType(m_sBatchType);
					m_Entry.sEntryDescription("Shipment");
					m_Entry.sEntryType(Integer.toString(ICEntryTypes.SHIPMENT_ENTRY));
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
				    	throw new Exception("Could not load entry with batch number " + m_sBatchNumber + ", entry number " + m_sEntryNumber
				    	+ "\n" + m_Entry.getErrorMessage());
					}
					//System.out.println("In " + this.toString() + ".loadICEntryInput - dump: " + m_Entry.read_out_debug_data());
				}
			}
		}catch (Exception e) {
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatchesEdit"
					+ "?BatchNumber=" + m_sBatchNumber
					+ "&BatchType=" + m_sBatchType
					+ "&Warning=" + m_sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=Could not process entry - " + e.getMessage()
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
		if (!createFormFromEntryInput(m_Entry, m_pwOut, m_bEditable, sDBID, sUserID, sUserFullName)){
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

	private boolean createFormFromEntryInput(ICEntry m_Entry, PrintWriter m_pwOut, boolean m_bEditable, String sDBID, String sUserID, String sUserFullName){
		
	    //Start the entry edit form:
		m_pwOut.println("<FORM NAME='ENTRYEDIT' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEntryUpdate' METHOD='POST'>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		//Record the hidden fields for the entry edit form:
	    storeHiddenFieldsOnForm (m_Entry, m_pwOut, m_bEditable);

        //Display the entry header fields:
	    displayEntryHeaderFields (m_Entry, m_pwOut, m_bEditable);
   
        if (m_bEditable){
        	displayEditableEntryFields (m_Entry, m_pwOut);
        }
        //Else, if the record is NOT editable:
        else{
        	displayNonEditableEntryFields (m_Entry, m_pwOut);
        }
        
	    //Now display the transaction lines:
        //Display the line header:
	    Display_Line_Header(m_pwOut);

	    //Display all the current transaction lines:
	    if (!displayLines(m_Entry, m_pwOut, m_bEditable, sDBID, sUserID, sUserFullName)){
	    	return false;
	    }
	    
        //Add a link for adding a line:
	    if(
	    	(m_Entry.lEntryNumber() > -1)
	    	&& (m_bEditable)
	    ){
			m_pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditShipmentLine" 
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
	private void storeHiddenFieldsOnForm(ICEntry m_Entry, PrintWriter m_pwOut, boolean m_bEditable){
		
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
	    m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" + "ICEditShipmentEntry" + "\">");
	}
	private void displayEntryHeaderFields (ICEntry m_Entry, PrintWriter m_pwOut, boolean m_bEditable){
		
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
	private void displayEditableEntryFields(ICEntry m_Entry, PrintWriter m_pwOut){
		
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
        
        //END ROW 1:
        m_pwOut.println("</TR>");
        
        m_pwOut.println("</TABLE>");
	}

	public void displayNonEditableEntryFields (ICEntry m_Entry, PrintWriter m_pwOut){

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
	private void Display_Line_Header( PrintWriter m_pwOut){
		m_pwOut.println("<BR>");
		m_pwOut.println(SMUtilities.getMasterStyleSheetLink());
		m_pwOut.println("<TABLE WIDTH=100% BGCOLOR=\"#FFFFFF\" CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\">");
		m_pwOut.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_HEADING +"\">");
		m_pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED +"\">Line #</TD>");
		m_pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED +"\">Item #</TD>");
		m_pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED +"\">Item description</TD>");
		m_pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED +"\">Location</TD>");
		m_pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED +"\">U/M</TD>");
		m_pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED +"\">Qty Shipped/Returned</TD>");
		m_pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED +"\">Cost</TD>");
		m_pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED +"\">Price</TD>");
		m_pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED +"\">Category</TD>");
		m_pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED +"\">Description</TD>");
		m_pwOut.println("</TR>");
	}
	private boolean displayLines(ICEntry m_Entry, PrintWriter m_pwOut, boolean bEditable, String sDBID, String sUserID, String sUserFullName){
		
        //Display the line header:
        for (int i = 0; i < m_Entry.getLineCount(); i++){
        	ICEntryLine line = m_Entry.getLineByIndex(i);

        	if( i % 2 == 0) {
            	m_pwOut.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
        	}else {
            	m_pwOut.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
        	}
        	//Line #:

        	m_pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
        	if (bEditable){
        		m_pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditShipmentLine" 
       	    		+ "?BatchNumber=" + line.sBatchNumber()
       	    		+ "&EntryNumber=" + line.sEntryNumber()
       	    		+ "&LineNumber=" + line.sLineNumber()
       	    		+ "&BatchType=" + m_Entry.sBatchType()
       	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
       	    		+ "\">" + line.sLineNumber()+ "</A>");
        	}else{
        		m_pwOut.println(line.sLineNumber());
        	}
        	m_pwOut.println("</TD>");
        	
        	//Item #:
        	m_pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
        	m_pwOut.println(clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(clsStringFunctions.filter(line.sItemNumber())));
        	m_pwOut.println("</FONT></TD>");

        	//Get the item detail info:
        	line.getItemDetails(getServletContext(), sDBID, sUserID, sUserFullName);
        	//Description:
        	m_pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
        	m_pwOut.println(line.sItemDescription());
        	m_pwOut.println("</FONT></TD>");
        	
        	//Location:
        	m_pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
        	m_pwOut.println(clsStringFunctions.filter(line.sLocation()));
        	m_pwOut.println("</FONT></TD>");

        	//U/M:
        	m_pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
        	m_pwOut.println(line.sUnitOfMeasure());
        	m_pwOut.println("</FONT></TD>");
        	
        	//Qty:
        	m_pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
        	m_pwOut.println(line.sQtySTDFormat());
        	m_pwOut.println("</FONT></TD>");
        	
        	//Cost:
        	m_pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
        	//Shipments do not show a cost:
       		m_pwOut.println(line.sCostSTDFormat());
        	m_pwOut.println("</FONT></TD>");
        	
        	//Price:
        	m_pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
        	m_pwOut.println(line.sPriceSTDFormat());
        	m_pwOut.println("</FONT></TD>");

        	//Category:
        	m_pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
        	m_pwOut.println(clsStringFunctions.filter(line.sCategoryCode()));
        	m_pwOut.println("</FONT></TD>");

        	//Description:
        	m_pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">");
        	m_pwOut.println(clsStringFunctions.filter(line.sDescription()));
        	m_pwOut.println("</FONT></TD>");

        	m_pwOut.println("</TR>");
        }

        m_pwOut.println("</TABLE>");

		return true;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

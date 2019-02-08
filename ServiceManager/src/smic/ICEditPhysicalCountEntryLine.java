package smic;

import SMDataDefinition.*;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMFinderFunctions;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class ICEditPhysicalCountEntryLine extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String sObjectName = "Line";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		PrintWriter m_pwOut;
		m_pwOut = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICEditPhysicalInventory))
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
	    
		ICEntryLine m_line;
		HttpServletRequest m_hsrRequest = request;
		//Load parameters from request.
		String m_sBatchNumber = clsManageRequestParameters.get_Request_Parameter("BatchNumber", m_hsrRequest);
		String m_sEntryNumber = clsManageRequestParameters.get_Request_Parameter("EntryNumber", m_hsrRequest);
		String m_sLineNumber = clsManageRequestParameters.get_Request_Parameter("LineNumber", m_hsrRequest);
		String m_sBatchType = clsManageRequestParameters.get_Request_Parameter("BatchType", m_hsrRequest);
		String m_sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", m_hsrRequest);
		ArrayList<String> m_sLocationValues = new ArrayList<String>();
	    ArrayList<String> m_sLocationDescriptions = new ArrayList<String>();
	    ArrayList<String> m_sGLAccountValues = new ArrayList<String>();
	    ArrayList<String> m_sGLAccountDescriptions = new ArrayList<String>();
		
	    
	    //Try to load the line:
	    if (CurrentSession.getAttribute("EntryLine") != null){
	    	m_line = (ICEntryLine) CurrentSession.getAttribute("EntryLine");
	    	CurrentSession.removeAttribute("EntryLine");
	    }else{
	    	m_line = new ICEntryLine(m_sBatchNumber, m_sEntryNumber, m_sLineNumber);
	    	//If it's not a new line, try to load it:
	    	if (m_sLineNumber.compareToIgnoreCase("-1") != 0){
				if (!m_line.load(m_sBatchNumber, m_sEntryNumber, m_sLineNumber, getServletContext(), sDBID)){
					response.sendRedirect(
							"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPhysicalCountEntry"
							+ "?BatchNumber=" + m_sBatchNumber
							+ "&EntryNumber" + m_sEntryNumber
							+ "&BatchType=" + m_sBatchType
							+ "Editable=Yes"
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
							+ "&Warning=Could not load line."
						);
				return;
				}
	    	}
	    }

	    String title;
	    String subtitle = "";
	    if (m_sLineNumber.compareToIgnoreCase("-1") == 0){
	    	title = "Edit NEW " + sObjectName;
	    }else{
	    	title = "Edit " + sObjectName + ": " + m_sLineNumber;
	    }

	    m_pwOut.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
		//If there is a warning from trying to input previously, print it here:
		if (! m_sWarning.equalsIgnoreCase("")){
			m_pwOut.println("<B><FONT COLOR=\"RED\">WARNING: " + m_sWarning + "</FONT></B><BR>");
		}
		
	    //Print a link to main menu:
		m_pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">Return to Inventory Main Menu</A><BR>");
	    
	    //Print a link to return to the 'edit entry' page:
		m_pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPhysicalCountEntry" 
				+ "?BatchNumber=" + m_sBatchNumber
				+ "&EntryNumber=" + m_sEntryNumber
				+ "&BatchType=" + m_sBatchType
				+ "&Editable=Yes"
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&Warning="
	    		+ "\">Return to Edit Entry " + m_sEntryNumber + "</A><BR><BR>");
		
		//Load the locations
	    m_sLocationValues.clear();
	    m_sLocationDescriptions.clear();
	    	try{
	    		String sSQL = "SELECT "
	    			+ SMTablelocations.sLocation
		        	+ ", " + SMTablelocations.sLocationDescription
		        	+ " FROM " + SMTablelocations.TableName
		        	+ " ORDER BY " + SMTablelocations.sLocation;

		        ResultSet rsLocations = clsDatabaseFunctions.openResultSet(
		        		sSQL, 
		        		getServletContext(), 
			        	sDBID,
			        	"MySQL",
			        	this.toString() + ".loadLocationList (1) - User: " + sUserID
			        	+ " - "
			        	+ sUserFullName
		        		);
		        
				//Print out directly so that we don't waste time appending to string buffers:
		        while (rsLocations.next()){
		        	m_sLocationValues.add((String) rsLocations.getString(SMTablelocations.sLocation).trim());
		        	m_sLocationDescriptions.add(
		        		(String) rsLocations.getString(SMTablelocations.sLocation).trim() 
		        			+ " - " + (String) rsLocations.getString(SMTablelocations.sLocationDescription).trim());
				}
		        rsLocations.close();

			}catch (SQLException ex){
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPhysicalCountEntry"
						+ "?BatchNumber=" + m_sBatchNumber
						+ "&EntryNumber" + m_sEntryNumber
						+ "&BatchType=" + m_sBatchType
						+ "Editable=Yes"
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "&Warning=Could not load locations - " + ex.getMessage()
					);
				return;
			}
	        
	   //Load GL Accounts
	   m_sGLAccountValues.clear();
	   m_sGLAccountDescriptions.clear();
	   		try{
		        String sSQL = "SELECT "
		        	+ SMTableglaccounts.sAcctID
		        	+ ", " + SMTableglaccounts.sDesc
		        	+ " FROM " + SMTableglaccounts.TableName
		        	+ " WHERE ("
		        		+ SMTableglaccounts.lActive + " = 1"
		        	+ ")"
		        	+ " ORDER BY " + SMTableglaccounts.sAcctID;

		        ResultSet rsGLAccounts = clsDatabaseFunctions.openResultSet(
			        	sSQL, 
			        	getServletContext(), 
			        	sDBID,
			        	"MySQL",
			        	this.toString() + ".loadGLAccountList (1) - User: " + sUserID
			        	+ " - "
			        	+ sUserFullName
		        		);
		        
				//Print out directly so that we don't waste time appending to string buffers:
		        while (rsGLAccounts.next()){
		        	m_sGLAccountValues.add((String) rsGLAccounts.getString(SMTableglaccounts.sAcctID).trim());
		        	m_sGLAccountDescriptions.add(
		        		(String) rsGLAccounts.getString(SMTableglaccounts.sAcctID).trim() 
		        			+ " - " + (String) rsGLAccounts.getString(SMTableglaccounts.sDesc).trim());
				}
		        rsGLAccounts.close();

			}catch (SQLException ex){
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPhysicalCountEntry"
						+ "?BatchNumber=" + m_sBatchNumber
						+ "&EntryNumber" + m_sEntryNumber
						+ "&BatchType=" + m_sBatchType
						+ "Editable=Yes"
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "&Warning=Could not load gl acccounts - " + ex.getMessage()
					);
				return;
			}    
		
		//Try to construct the rest of the screen form from the AREntryInput object:
		if (!createFormFromLineInput(
				m_pwOut, 
				m_sBatchNumber,
				m_sEntryNumber, 
				m_sLineNumber, 
				m_sBatchType, 
				m_line,
				m_hsrRequest,
				m_sLocationValues,
				m_sLocationDescriptions,
				m_sGLAccountValues,
				m_sGLAccountDescriptions,
				sDBID,
				sUserID, 
				sUserFullName)){
			
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPhysicalCountEntry"
					+ "?BatchNumber=" + m_sBatchNumber
					+ "&BatchEntry=" + m_sEntryNumber
					+ "&BatchType=" + m_sBatchType
					+ "Editable=Yes"
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning
				);
			return;
		}
		//End the page:
		m_pwOut.println("</BODY></HTML>");
	}
	private boolean createFormFromLineInput(
			PrintWriter m_pwOut, 
			String m_sBatchNumber,
			String m_sEntryNumber,
			String m_sLineNumber,
			String m_sBatchType,
			ICEntryLine m_line,
			HttpServletRequest m_hsrRequest,
			ArrayList<String> m_sLocationValues,
			ArrayList<String> m_sLocationDescriptions,
			ArrayList<String> m_sGLAccountValues,
			ArrayList<String> m_sGLAccountDescriptions,
			String sDBID, 
			String sUserID, 
			String sUserFullName){
		
	    //Start the entry edit form:
		m_pwOut.println("<FORM NAME='ENTRYEDIT' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICPhysicalCountLineUpdate' METHOD='POST'>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME='BatchNumber' VALUE='" + m_sBatchNumber + "'>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME='EntryNumber' VALUE='" + m_sEntryNumber + "'>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME='LineNumber' VALUE='" + m_sLineNumber + "'>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME='BatchType' VALUE='" + m_sBatchType + "'>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='" + "ICEditPhysicalCountEntryLine" + "'>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME='" + ICEntryLine.ParamReceiptLineID 
				+ "' VALUE='" + m_line.sReceiptLineID() + "'>");

    	m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
    			+ ICEntryLine.ParamLineEntryID 
    			+ "\" VALUE=\"" + m_line.sEntryId() + "\">");
    	m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
    			+ ICEntryLine.ParamLineID 
    			+ "\" VALUE=\"" + m_line.sId() + "\">");
    	m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
    			+ ICEntryLine.ParamLineCost 
    			+ "\" VALUE=\"" + "0.00" + "\">");
    	m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
    			+ ICEntryLine.ParamLinePrice 
    			+ "\" VALUE=\"" + "0.00" + "\">");

    	m_pwOut.println("<TABLE BORDER=1>");
    	m_pwOut.println("<TR>");

    	//Item
    	String sEditCode = clsStringFunctions.filter(m_line.sItemNumber());
	    if (m_hsrRequest.getParameter(ICEntryLine.ParamLineItemNumber) != null){
	    	sEditCode = m_hsrRequest.getParameter(ICEntryLine.ParamLineItemNumber);
	    }
    	
    	m_pwOut.println("<TD ALIGN=RIGHT>Item number:</TD>");
    	m_pwOut.println("<TD>");
        m_pwOut.println(clsCreateHTMLFormFields.Create_Edit_Form_Text_Input_Field(
        		ICEntryLine.ParamLineItemNumber, 
        		sEditCode, 
    			25, 
    			"", 
    			""
    			)
    	);
        
		//Link to finder:
        m_pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&ObjectName=ACTIVE Item"
			+ "&ResultClass=FinderResults"
			+ "&SearchingClass=smic.ICEditPhysicalCountEntryLine"
			+ "&ReturnField=" + ICEntryLine.ParamLineItemNumber
			
			+ SMFinderFunctions.getStdITEMSearchAndResultString()
			
			/*
			+ "&SearchField1=" + SMTableicitems.sItemDescription
			+ "&SearchFieldAlias1=Description"
			+ "&SearchField2=" + SMTableicitems.sItemNumber
			+ "&SearchFieldAlias2=Item%20No."
			+ "&SearchField3=" + SMTableicitems.sComment1
			+ "&SearchFieldAlias3=Comment%201"
			+ "&SearchField4=" + SMTableicitems.sComment2
			+ "&SearchFieldAlias4=Comment%202"
			+ "&SearchField5=" + SMTableicitems.sComment3
			+ "&SearchFieldAlias5=Comment%203"
			+ "&ResultListField1="  + SMTableicitems.sItemNumber
			+ "&ResultHeading1=Item%20No."
			+ "&ResultListField2="  + SMTableicitems.sItemDescription
			+ "&ResultHeading2=Description"
			+ "&ResultListField3="  + SMTableicitems.sCostUnitOfMeasure
			+ "&ResultHeading3=Cost%20Unit"
			+ "&ResultListField4="  + SMTableicitems.inonstockitem
			+ "&ResultHeading4=Non-stock?"
			+ "&ResultListField5="  + SMTableicitems.sPickingSequence
			+ "&ResultHeading5=Picking%20Sequence"
			*/
						
			+ "&ParameterString=" 
				+ "*BatchNumber=" + m_sBatchNumber
				+ "*EntryNumber=" + m_sEntryNumber
				+ "*LineNumber=" + m_sLineNumber
				+ "*BatchType=" + m_sBatchType
			+ "\"> Find item</A>"
			);
        
    	m_pwOut.println("</TD>");
    	m_pwOut.println("<TD>");
    	m_pwOut.println("&nbsp;");
    	m_pwOut.println("</TD>");
    	m_pwOut.println("</TR>");

    	//Update the item info:
    	m_line.getItemDetails(getServletContext(), sDBID, sUserID, sUserFullName);
    	
    	//Description:
    	m_pwOut.println("<TR>");
    	m_pwOut.println("<TD ALIGN=RIGHT>Description:</TD>");
    	m_pwOut.println("<TD>");
        m_pwOut.println("<B>" + m_line.sItemDescription() + "</B>");
    	m_pwOut.println("</TD>");
    	m_pwOut.println("<TD>");
    	m_pwOut.println("&nbsp;");
    	m_pwOut.println("</TD>");
    	m_pwOut.println("</TR>");
    	
    	//Unit of measure:
    	m_pwOut.println("<TR>");
    	m_pwOut.println("<TD ALIGN=RIGHT>Unit of measure:</TD>");
    	m_pwOut.println("<TD>");
        m_pwOut.println("<B>" + m_line.sUnitOfMeasure() + "</B>");
    	m_pwOut.println("</TD>");
    	m_pwOut.println("<TD>");
    	m_pwOut.println("&nbsp;");
    	m_pwOut.println("</TD>");
    	m_pwOut.println("</TR>");
        
    	//Write off account:
    	m_pwOut.println("<TR>");
    	m_pwOut.println("<TD ALIGN=RIGHT>Write-off account:</TD>");
    	m_pwOut.println("<TD>");
    	m_pwOut.println("<SELECT NAME = \"" + ICEntryLine.ParamLineDistributionAccount + "\">");
    	
    	//add the first line as a default, so we can tell if they didn't pick a GL:
    	m_pwOut.println("<OPTION");
		m_pwOut.println(" VALUE=\"" + "" + "\">");
		m_pwOut.println(" - Select a write-off account - ");
    	
        //Read out the array list:
        for (int iGLAccountCount = 0; iGLAccountCount<m_sGLAccountValues.size();iGLAccountCount++){
        	m_pwOut.println("<OPTION");
			if (m_sGLAccountValues.get(iGLAccountCount).toString().compareToIgnoreCase(m_line.sDistributionAcct()) == 0){
				m_pwOut.println( " selected=yes ");
			}
			m_pwOut.println(" VALUE=\"" + m_sGLAccountValues.get(iGLAccountCount).toString() + "\">");
			m_pwOut.println(m_sGLAccountDescriptions.get(iGLAccountCount).toString());
        }
        m_pwOut.println("</SELECT>");
        m_pwOut.println("</TD>");
    	m_pwOut.println("<TD>");
    	m_pwOut.println("<INPUT TYPE=SUBMIT NAME='SubmitDefaultWriteOffAccount' "
    			+ "VALUE='Select default write-off account for this location' STYLE='height: 0.24in'>");
    	m_pwOut.println("</TD>");
        m_pwOut.println("</TR>");
    	
    	//Qty
    	m_pwOut.println("<TR>");
    	m_pwOut.println("<TD ALIGN=RIGHT>Quantity:</TD>");
    	m_pwOut.println("<TD>");
    	//Add drop down for positive/negative:
    	m_pwOut.println("<SELECT NAME = \"" + "QtyPositiveOrNegative" + "\">");
    	
    	//add the first line as a default, so we can tell if they didn't pick one:
    	m_pwOut.println("<OPTION");
		if (
				(m_line.sQtySTDFormat().trim().compareToIgnoreCase("0.0000") == 0)
				|| (m_line.sQtySTDFormat().trim().compareToIgnoreCase("") == 0)
			){
			m_pwOut.println(" selected = yes ");
		}
		m_pwOut.println(" VALUE=\"" + "UNSELECTED" + "\">");
		m_pwOut.println(" - Select DECREASE or INCREASE - ");
       	m_pwOut.println("<OPTION");
		if (
				(!m_line.sQtySTDFormat().startsWith("-"))
				&& (m_line.sQtySTDFormat().compareToIgnoreCase("0.0000") != 0)
				&& (m_line.sQtySTDFormat().compareToIgnoreCase("") != 0)
				){
			m_pwOut.println(" selected = yes ");
		}
		m_pwOut.println(" VALUE=\"" + "POSITIVE" + "\">");
		m_pwOut.println("INCREASED");
       	m_pwOut.println("<OPTION");
		if (m_line.sQtySTDFormat().startsWith("-")){
			m_pwOut.println(" selected = yes ");
		}
       	m_pwOut.println(" VALUE=\"" + "NEGATIVE" + "\">");
		m_pwOut.println("DECREASED");
        m_pwOut.println("</SELECT>");
        m_pwOut.println("&nbsp;");
    	
        //We have to replace any minus signs, because we don't want them appearing on the screen:
        m_pwOut.println(clsCreateHTMLFormFields.Create_Edit_Form_Text_Input_Field(
        		ICEntryLine.ParamLineQty,
   				clsStringFunctions.filter(m_line.sQtySTDFormat()).replace("-", ""), 
    			25, 
    			"", 
    			""
    			)
    	);

    	m_pwOut.println("</TD>");
    	m_pwOut.println("<TD>");
    	m_pwOut.println("&nbsp;");
    	m_pwOut.println("</TD>");
    	m_pwOut.println("</TR>");    	

    	//Location:
    	m_pwOut.println("<TR>");
    	m_pwOut.println("<TD ALIGN=RIGHT>Location:</TD>");
    	m_pwOut.println("<TD>");
    	m_pwOut.println("<SELECT NAME = \"" + ICEntryLine.ParamLineLocation + "\">");
    	
    	//add the first line as a default, so we can tell if they didn't pick one:
    	m_pwOut.println("<OPTION");
		m_pwOut.println(" VALUE=\"" + "" + "\">");
		m_pwOut.println(" - Select a location - ");
    	
        //Read out the array list:
        for (int iLocationCount = 0; iLocationCount<m_sLocationValues.size();iLocationCount++){
        	m_pwOut.println("<OPTION");
			if (m_sLocationValues.get(iLocationCount).toString().compareToIgnoreCase(m_line.sLocation()) == 0){
				m_pwOut.println( " selected=yes ");
			}
			m_pwOut.println(" VALUE=\"" + m_sLocationValues.get(iLocationCount).toString() + "\">");
			m_pwOut.println(m_sLocationDescriptions.get(iLocationCount).toString());
        }
        m_pwOut.println("</SELECT>");
        m_pwOut.println("</TD>");
    	m_pwOut.println("<TD>");
    	m_pwOut.println("&nbsp;");
    	m_pwOut.println("</TD>");
        m_pwOut.println("</TR>");
        
    	//Desc
        m_pwOut.println("<TR>");
        m_pwOut.println("<TD ALIGN=RIGHT>Description:</TD>");
        m_pwOut.println("<TD>");
        m_pwOut.println(clsCreateHTMLFormFields.Create_Edit_Form_Text_Input_Field(
        		ICEntryLine.ParamLineDesc,
   				clsStringFunctions.filter(m_line.sDescription()), 
    			25, 
    			"", 
    			""
    			)
    	);
        m_pwOut.println("</TD>");
    	m_pwOut.println("<TD>");
    	m_pwOut.println("&nbsp;");
    	m_pwOut.println("</TD>");
        m_pwOut.println("</TR>");

        m_pwOut.println("</TABLE>");
	    //End the entry edit form:
        
    	m_pwOut.println("<BR><INPUT TYPE=SUBMIT NAME='Save' VALUE='Save " + sObjectName + "' STYLE='height: 0.24in'>");
    	
    	if (m_line.sLineNumber().compareToIgnoreCase("-1") != 0){
    		m_pwOut.println("&nbsp;&nbsp;<INPUT TYPE=SUBMIT NAME='Delete' VALUE='Delete " + sObjectName + "' STYLE='height: 0.24in'>");
    		m_pwOut.println("&nbsp;&nbsp;Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"ConfirmDelete\">");
    	}
        
	    m_pwOut.println("</FORM>");  

		return true;
	}
	
		public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

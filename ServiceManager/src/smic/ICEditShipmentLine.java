package smic;

import SMDataDefinition.*;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMUtilities;

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

public class ICEditShipmentLine extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static String sObjectName = "Line";
	
	private ICEntryLine m_line;
	private String m_sBatchNumber;
	private String m_sEntryNumber;
	private String m_sLineNumber;
	private String m_sBatchType;
	private String m_sWarning;
	private PrintWriter m_pwOut;
	private HttpServletRequest m_hsrRequest;

	//We'll use these to store the location List, so we don't have to load it several times:
    private ArrayList<String> m_sLocationValues = new ArrayList<String>();
    private ArrayList<String> m_sLocationDescriptions = new ArrayList<String>();
    
    //Cost bucket array lists:
    private ArrayList<String> m_sCategoryValues = new ArrayList<String>();
    private ArrayList<String> m_sCategoryDescriptions = new ArrayList<String>();

	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

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
		
		m_hsrRequest = request;
	    get_request_parameters();
	    
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
							"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditShipmentEntry"
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
		m_pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditShipmentEntry" 
				+ "?BatchNumber=" + m_sBatchNumber
				+ "&EntryNumber=" + m_sEntryNumber
				+ "&BatchType=" + m_sBatchType
				+ "&Editable=Yes"
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&Warning="
	    		+ "\">Return to Edit Entry " + m_sEntryNumber + "</A><BR><BR>");
		
		//Try to construct the rest of the screen form from the AREntryInput object:
		if (!createFormFromLineInput(sDBID, sUserID, sUserFullName)){
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditShipmentEntry"
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
	private boolean createFormFromLineInput(String sDBID, String sUserID, String sUserFullName){
		
	    //Start the entry edit form:
		m_pwOut.println("<FORM NAME='ENTRYEDIT' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICShipmentLineUpdate' METHOD='POST'>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME='BatchNumber' VALUE='" + m_sBatchNumber + "'>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME='EntryNumber' VALUE='" + m_sEntryNumber + "'>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME='LineNumber' VALUE='" + m_sLineNumber + "'>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME='BatchType' VALUE='" + m_sBatchType + "'>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='" + "ICEditShipmentLine" + "'>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME='" + ICEntryLine.ParamReceiptLineID 
				+ "' VALUE='" + m_line.sReceiptLineID() + "'>");
	    if (!loadCategoryList(sDBID, sUserID, sUserFullName)){
	    	return false;
	    }
	    if (!loadLocationList(sDBID, sUserID, sUserFullName)){
	    	return false;
	    }

    	m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
    			+ ICEntryLine.ParamLineEntryID 
    			+ "\" VALUE=\"" + m_line.sEntryId() + "\">");
    	m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
    			+ ICEntryLine.ParamLineID 
    			+ "\" VALUE=\"" + m_line.sId() + "\">");

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
			+ "&SearchingClass=smic.ICEditShipmentLine"
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
    	
    	//Category:
    	m_pwOut.println("<TR>");
    	m_pwOut.println("<TD ALIGN=RIGHT>Category:</TD>");
    	m_pwOut.println("<TD>");
    	m_pwOut.println("<SELECT NAME = \"" + ICEntryLine.ParamLineCategory + "\">");
    	
    	//add the first line as a default, so we can tell if they didn't pick a GL:
    	m_pwOut.println("<OPTION");
		m_pwOut.println(" VALUE=\"" + "" + "\">");
		m_pwOut.println(" - Select a category - ");
    	
        //Read out the array list:
        for (int iCategoryCount = 0; iCategoryCount<m_sCategoryValues.size();iCategoryCount++){
        	m_pwOut.println("<OPTION");
			if (m_sCategoryValues.get(iCategoryCount).toString().compareToIgnoreCase(m_line.sCategoryCode()) == 0){
				m_pwOut.println( " selected=yes ");
			}
			m_pwOut.println(" VALUE=\"" + m_sCategoryValues.get(iCategoryCount).toString() + "\">");
			m_pwOut.println(m_sCategoryDescriptions.get(iCategoryCount).toString());
        }
        m_pwOut.println("</SELECT>");
        m_pwOut.println("</TD>");
    	m_pwOut.println("<TD>");
    	m_pwOut.println("<INPUT TYPE=SUBMIT NAME='SubmitDefaultCategory' VALUE='Select default category for this item' STYLE='height: 0.24in'>");
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
		m_pwOut.println(" - Select SHIPMENT or RETURN - ");
       	m_pwOut.println("<OPTION");
		if (
				(!m_line.sQtySTDFormat().startsWith("-"))
				&& (m_line.sQtySTDFormat().compareToIgnoreCase("0.0000") != 0)
				&& (m_line.sQtySTDFormat().compareToIgnoreCase("") != 0)
				){
			m_pwOut.println(" selected = yes ");
		}
		m_pwOut.println(" VALUE=\"" + "POSITIVE" + "\">");
		m_pwOut.println("RETURNED");
       	m_pwOut.println("<OPTION");
		if (m_line.sQtySTDFormat().startsWith("-")){
			m_pwOut.println(" selected = yes ");
		}
       	m_pwOut.println(" VALUE=\"" + "NEGATIVE" + "\">");
		m_pwOut.println("SHIPPED");
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
    	
    	//Cost
    	m_pwOut.println("<TR>");
    	m_pwOut.println("<TD ALIGN=RIGHT>Cost:</TD>");
    	m_pwOut.println("<TD>");
    	
        m_pwOut.println(clsCreateHTMLFormFields.Create_Edit_Form_Text_Input_Field(
        		ICEntryLine.ParamLineCost,
   				clsStringFunctions.filter(m_line.sCostSTDFormat().replace("-", "")), 
    			25, 
    			"", 
    			""
    			)
    	);

    	m_pwOut.println("</TD>");
    	m_pwOut.println("<TD>");
    	m_pwOut.println("TOTAL cost for the line, NOT unit price (used for returns only).");
    	m_pwOut.println("</TD>");
    	m_pwOut.println("</TR>");
    	
    	//Price
    	m_pwOut.println("<TR>");
    	m_pwOut.println("<TD ALIGN=RIGHT>Price:</TD>");
    	m_pwOut.println("<TD>");
    	
        m_pwOut.println(clsCreateHTMLFormFields.Create_Edit_Form_Text_Input_Field(
        		ICEntryLine.ParamLinePrice,
   				clsStringFunctions.filter(m_line.sPriceSTDFormat().replace("-", "")), 
    			25, 
    			"", 
    			""
    			)
    	);

    	m_pwOut.println("</TD>");
    	m_pwOut.println("<TD>");
    	m_pwOut.println("TOTAL price for the line, NOT unit price.");
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

	private void get_request_parameters(){
 
		m_sBatchNumber = clsManageRequestParameters.get_Request_Parameter("BatchNumber", m_hsrRequest);
		m_sEntryNumber = clsManageRequestParameters.get_Request_Parameter("EntryNumber", m_hsrRequest);
		m_sLineNumber = clsManageRequestParameters.get_Request_Parameter("LineNumber", m_hsrRequest);
		m_sBatchType = clsManageRequestParameters.get_Request_Parameter("BatchType", m_hsrRequest);
		m_sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", m_hsrRequest);
		
	}

	private boolean loadCategoryList(String sDBID, String sUserID, String sUserFullName){
        m_sCategoryValues.clear();
        m_sCategoryDescriptions.clear();
        try{
	        String sSQL = "SELECT "
	        	+ SMTableiccategories.sCategoryCode
	        	+ ", " + SMTableiccategories.sDescription
	        	+ " FROM " + SMTableiccategories.TableName
	        	+ " ORDER BY " + SMTableiccategories.sCategoryCode;

	        ResultSet rsCategories = clsDatabaseFunctions.openResultSet(
		        	sSQL, 
		        	getServletContext(), 
		        	sDBID,
		        	"MySQL",
		        	this.toString() + ".loadCategoryList (1) - User: " + sUserFullName);
	        
			//Print out directly so that we don't waste time appending to string buffers:
	        while (rsCategories.next()){
	        	m_sCategoryValues.add((String) rsCategories.getString(SMTableiccategories.sCategoryCode).trim());
	        	m_sCategoryDescriptions.add(
	        		(String) rsCategories.getString(SMTableiccategories.sCategoryCode).trim() 
	        			+ " - " + (String) rsCategories.getString(SMTableiccategories.sDescription).trim());
			}
	        rsCategories.close();

		}catch (SQLException ex){
	    	System.out.println("Error in " + this.toString()+ " class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}
		
		return true;
	}
	private boolean loadLocationList(String sDBID, String sUserID, String sUserFullName){
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
		        	this.toString() + ".loadLocationList (1) - User: " + sUserFullName);
	        
			//Print out directly so that we don't waste time appending to string buffers:
	        while (rsLocations.next()){
	        	m_sLocationValues.add((String) rsLocations.getString(SMTablelocations.sLocation).trim());
	        	m_sLocationDescriptions.add(
	        		(String) rsLocations.getString(SMTablelocations.sLocation).trim() 
	        			+ " - " + (String) rsLocations.getString(SMTablelocations.sLocationDescription).trim());
			}
	        rsLocations.close();

		}catch (SQLException ex){
	    	System.out.println("Error in " + this.toString()+ " class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}
		
		return true;
	}
		public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

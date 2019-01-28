package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMFinderFunctions;
import SMDataDefinition.SMTableicitemlocations;
import SMDataDefinition.SMTablelocations;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMUtilities;

public class ICEditTransferLine extends HttpServlet {
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
	public static final String DISPLAY_QTYS_PARAM = "DISPLAYITEMQTYS";
	//We'll use these to store the location List, so we don't have to load it several times:
    private ArrayList<String> m_sLocationValues = new ArrayList<String>();
    private ArrayList<String> m_sLocationDescriptions = new ArrayList<String>();
    private boolean m_bDisplayQtys = false;

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
							"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditTransferEntry"
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
		m_pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditTransferEntry" 
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
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditTransferEntry"
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
		m_pwOut.println("<FORM NAME='ENTRYEDIT' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICTransferLineUpdate' METHOD='POST'>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME='BatchNumber' VALUE='" + m_sBatchNumber + "'>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME='EntryNumber' VALUE='" + m_sEntryNumber + "'>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME='LineNumber' VALUE='" + m_sLineNumber + "'>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME='BatchType' VALUE='" + m_sBatchType + "'>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='" + "ICEditTransferLine" + "'>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME='" + ICEntryLine.ParamReceiptLineID 
				+ "' VALUE='" + m_line.sReceiptLineID() + "'>");
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
			+ "&SearchingClass=smic.ICEditTransferLine"
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
    	    	
    	//FROM Location:
    	m_pwOut.println("<TR>");
    	m_pwOut.println("<TD ALIGN=RIGHT>FROM Location:</TD>");
    	m_pwOut.println("<TD>");
    	m_pwOut.println("<SELECT NAME = \"" + ICEntryLine.ParamLineLocation + "\">");
    	
    	//add the first line as a default, so we can tell if they didn't pick one:
    	m_pwOut.println("<OPTION");
		m_pwOut.println(" VALUE=\"" + "" + "\">");
		m_pwOut.println(" - Select a FROM location - ");
    	
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
        
    	//TO Location:
    	m_pwOut.println("<TR>");
    	m_pwOut.println("<TD ALIGN=RIGHT>TO Location:</TD>");
    	m_pwOut.println("<TD>");
    	m_pwOut.println("<SELECT NAME = \"" + ICEntryLine.ParamLineTargetLocation + "\">");
    	
    	//add the first line as a default, so we can tell if they didn't pick one:
    	m_pwOut.println("<OPTION");
		m_pwOut.println(" VALUE=\"" + "" + "\">");
		m_pwOut.println(" - Select a TO location - ");
    	
        //Read out the array list:
        for (int iLocationCount = 0; iLocationCount<m_sLocationValues.size();iLocationCount++){
        	m_pwOut.println("<OPTION");
			if (m_sLocationValues.get(iLocationCount).toString().compareToIgnoreCase(m_line.sTargetLocation()) == 0){
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
        
    	//Qty
    	m_pwOut.println("<TR>");
    	m_pwOut.println("<TD ALIGN=RIGHT>Quantity:</TD>");
    	m_pwOut.println("<TD>");

    	//We have to replace any minus signs, because we don't want them appearing on the screen:
        m_pwOut.println(clsCreateHTMLFormFields.Create_Edit_Form_Text_Input_Field(
        		ICEntryLine.ParamLineQty,
   				clsStringFunctions.filter(m_line.sQtySTDFormat()), 
    			25, 
    			"", 
    			""
    			)
    	);

    	m_pwOut.println("</TD>");
    	m_pwOut.println("<TD>");
    	m_pwOut.println("Qty transferred must be positive.");
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
    	m_pwOut.println("&nbsp;&nbsp;<INPUT TYPE=SUBMIT NAME='SaveAndAddAnother' VALUE='Save and add new " + sObjectName + "' STYLE='height: 0.24in'>");
    	
    	if (m_line.sLineNumber().compareToIgnoreCase("-1") != 0){
    		m_pwOut.println("&nbsp;&nbsp;<INPUT TYPE=SUBMIT NAME='Delete' VALUE='Delete " + sObjectName + "' STYLE='height: 0.24in'>");
    		m_pwOut.println("&nbsp;&nbsp;Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"ConfirmDelete\">");
    	}
    	m_pwOut.println("&nbsp;&nbsp;<INPUT TYPE=SUBMIT NAME='" + DISPLAY_QTYS_PARAM 
    		+ "' VALUE='Display quantities on hand for this item' STYLE='height: 0.24in'>");
    	
	    m_pwOut.println("</FORM>");
	    
	    if (m_bDisplayQtys){
	    	try {
				displayQtys(sDBID, sUserID, sUserFullName);
			} catch (Exception e) {
				m_pwOut.println("<FONT COLOR=RED><B>" + e.getMessage() + "</B></FONT>");
			}
	    }
		return true;
	}

	private void get_request_parameters(){
 
		m_sBatchNumber = clsManageRequestParameters.get_Request_Parameter("BatchNumber", m_hsrRequest);
		m_sEntryNumber = clsManageRequestParameters.get_Request_Parameter("EntryNumber", m_hsrRequest);
		m_sLineNumber = clsManageRequestParameters.get_Request_Parameter("LineNumber", m_hsrRequest);
		m_sBatchType = clsManageRequestParameters.get_Request_Parameter("BatchType", m_hsrRequest);
		m_sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", m_hsrRequest);
		m_bDisplayQtys = clsManageRequestParameters.get_Request_Parameter(DISPLAY_QTYS_PARAM, m_hsrRequest).compareToIgnoreCase("") != 0;
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
	private void displayQtys(String sDBID, String sUserID, String sUserFullName) throws Exception{
		ICItem item = new ICItem(m_line.sItemNumber());
		if (!item.load(getServletContext(), sDBID)){
			m_pwOut.println("<FONT COLOR=RED><B>Can't display quantities for item number '" + m_line.sItemNumber() 
				+ "' - this item cannot be loaded.</B></FONT><BR>");
			return;
		}
		m_pwOut.println("<B>Quantities on hand for item number " + m_line.sItemNumber() + ":</B><BR>");
		m_pwOut.println("<TABLE BORDER=1>");
		m_pwOut.println("<TR>"
			+ "<TD>"
			+ "<B>Location</B>"
			+ "</TD>"
			+ "<TD ALIGN=RIGHT>"
			+ "<B>Quantity</B>"
			+ "</TD>"
			+ "</TR>"
		);

		String SQL = "SELECT"
			+ " " + SMTablelocations.TableName + "." + SMTablelocations.sLocation
			+ ", " + SMTablelocations.TableName + "." + SMTablelocations.sLocationDescription
			+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand
			+ " FROM " + SMTablelocations.TableName + " LEFT JOIN " + SMTableicitemlocations.TableName
			+ " ON " + SMTablelocations.TableName + "." + SMTablelocations.sLocation + " = "
			+ SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sLocation
			+ " WHERE ("
				+ "(" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber + " = '" + m_line.sItemNumber() + "')"
				+ " OR (" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber + " IS NULL)"
			+ ") ORDER BY " + SMTablelocations.TableName + "." + SMTablelocations.sLocationDescription
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(),
				sDBID, 
				"MySQL", 
				this.toString() + ".displayQtys - user: " + sUserFullName);
			while (rs.next()){
				BigDecimal bdQty = rs.getBigDecimal(SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand);
				if (bdQty == null){
					bdQty = BigDecimal.ZERO;
				}
				m_pwOut.println("<TR>"
						+ "<TD>"
						+ rs.getString(SMTablelocations.TableName + "." + SMTablelocations.sLocationDescription)
						+ "</TD>"
						+ "<TD ALIGN=RIGHT>"
						+ clsManageBigDecimals.BigDecimalToScaledFormattedString(4, bdQty)
						+ "</TD>"
						+ "</TR>"
					);
			}
			rs.close();
		} catch (SQLException e) {
			throw new Exception("Error reading qtys on hand - " + e.getMessage());
		} 
		m_pwOut.println("</TABLE>");
	}
		public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

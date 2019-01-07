package smap;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class APConvertACCPAC extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	public static final String CONVERSION_BUTTON_NAME = "CONVERSION";
	public static final String CONVERSION_BUTTON_LABEL = "----Execute selected function----";
	public static final String CONFIRM_CONVERSION_LABEL = "  Check to confirm data conversion: ";
	public static final String CONFIRM_CONVERSION_CHECKBOX_NAME = "CONFIRMCONVERSION";
	
	public static final String RADIO_FIELD_NAME = "CONVERSIONPHASE";
	
	public static final int NUMBER_OF_CONVERSION_STEPS = 1;
	
	public static final int ROLLBACK_OPTION_VALUE = 1;
	public static final String ROLLBACK_BUTTON_LABEL = Integer.toString(ROLLBACK_OPTION_VALUE) + ") Rollback previous data conversion";
	public static final int PROCESS_MASTER_TABLES_OPTION_VALUE = 2;
	public static final String PROCESS_MASTER_TABLES_BUTTON_LABEL = Integer.toString(PROCESS_MASTER_TABLES_OPTION_VALUE) + ") Convert Account Sets, Distribution Codes, 1099/CPRS Codes, and Banks";
	public static final int PROCESS_VENDOR_TABLES_OPTION_VALUE = 3;
	public static final String PROCESS_VENDOR_TABLES_BUTTON_LABEL = Integer.toString(PROCESS_VENDOR_TABLES_OPTION_VALUE) + ") Convert Vendor Groups, Vendor Remit-to's, and Vendors";
	public static final int PROCESS_VENDOR_STATISTICS_OPTION_VALUE = 4;
	public static final String PROCESS_VENDOR_STATISTICS_BUTTON_LABEL = Integer.toString(PROCESS_VENDOR_STATISTICS_OPTION_VALUE) + ") Convert Vendor Statistics";
	public static final int PROCESS_VENDOR_TRANSACTIONS_OPTION_VALUE = 5;
	public static final String PROCESS_VENDOR_TRANSACTIONS_BUTTON_LABEL = Integer.toString(PROCESS_VENDOR_TRANSACTIONS_OPTION_VALUE) + ") Convert Vendor Transactions";
	public static final int PROCESS_VENDOR_TRANSACTION_LINES_INSERT_OPTION_VALUE = 6;
	public static final String PROCESS_VENDOR_TRANSACTION_LINES_INSERT_BUTTON_LABEL = Integer.toString(PROCESS_VENDOR_TRANSACTION_LINES_INSERT_OPTION_VALUE) + ") Convert Vendor Transaction Lines";
	public static final int PROCESS_VENDOR_TRANSACTION_LINES_UPDATE_OPTION_VALUE = 7;
	public static final String PROCESS_VENDOR_TRANSACTION_LINES_UPDATE_BUTTON_LABEL = Integer.toString(PROCESS_VENDOR_TRANSACTION_LINES_UPDATE_OPTION_VALUE) + ") Update Transaction Lines";
	public static final int PROCESS_INSERT_VENDOR_MATCHING_LINES_OPTION_VALUE = 8;
	public static final String PROCESS_VENDOR_MATCHING_LINES_BUTTON_LABEL = Integer.toString(PROCESS_INSERT_VENDOR_MATCHING_LINES_OPTION_VALUE) + ") Convert Vendor Transaction Matching Lines";
	public static final int PROCESS_UPDATE_APPLY_FROM_MATCHING_LINES_OPTION_VALUE = 9;
	public static final String PROCESS_UPDATE_APPLY_FROM_MATCHING_LINES_BUTTON_LABEL = Integer.toString(PROCESS_UPDATE_APPLY_FROM_MATCHING_LINES_OPTION_VALUE) + ") Update APPLY-FROM Matching Lines";
	public static final int PROCESS_UPDATE_APPLY_TO_MATCHING_LINES_OPTION_VALUE = 10;
	public static final String PROCESS_UPDATE_APPLY_TO_MATCHING_LINES_BUTTON_LABEL = Integer.toString(PROCESS_UPDATE_APPLY_TO_MATCHING_LINES_OPTION_VALUE) + ") Update APPLY-TO Matching Lines";
	public static final int PROCESS_UPDATE_VENDOR_ADDRESSES_OPTION_VALUE = 11;
	public static final String PROCESS_UPDATE_VENDOR_ADDRESSES_BUTTON_LABEL = Integer.toString(PROCESS_UPDATE_VENDOR_ADDRESSES_OPTION_VALUE) + ") OPTIONAL: Update ALL Vendor Addresses From ACCPAC";
	
	
	public static final int LAST_FUNCTION_IN_SEQUENCE = PROCESS_UPDATE_VENDOR_ADDRESSES_OPTION_VALUE;
	
	private static final String HIGHLIGHT_ROW_BACKGROUND_COLOR = "YELLOW";
	private static final String DEFAULT_ROW_BACKGROUND_COLOR = "WHITE";
	
	//Conversion steps:
	
	
	private String sDBID = "";
	private String sCompanyName = "";
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.APConvertACCPACData))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "Convert ACCPAC AP Data";
	    String subtitle = "";
	    
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    
	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		
	    String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>***** NOTE: " + sStatus + "</B><BR>");
		}
	    
		//Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Payable Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.APConvertACCPACData) 
	    		+ "\">Summary</A><BR>");
	    
	    out.println("<BR>This function will read all of the ACCPAC AP data and convert it into the SMCP format, to be used in the SMCP AP module."
	    		+ "  Once this is run, and the SMCP AP functions are first used it CAN NOT be run again.<BR>"
	    );
	    
    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APConvertACCPACAction\">");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
    	out.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='" + this.getClass().getName() + "'>");
    	
    	//Use radio buttons here for each of the conversion phases:
    	ArrayList<String>arrConversionPhaseNames = new ArrayList<String> (0);
    	ArrayList<String>arrConversionPhaseValues = new ArrayList<String> (0);
    	ArrayList<String>arrConfirmingLabels = new ArrayList<String> (0);
    	arrConversionPhaseValues.add(Integer.toString(ROLLBACK_OPTION_VALUE));
    	arrConversionPhaseNames.add(ROLLBACK_BUTTON_LABEL);
    	arrConfirmingLabels.add("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_CONVERSION_CHECKBOX_NAME + "\"> <B><I>Confirm</B></I></LABEL>");
    	
    	arrConversionPhaseValues.add(Integer.toString(PROCESS_MASTER_TABLES_OPTION_VALUE));
    	arrConversionPhaseNames.add(PROCESS_MASTER_TABLES_BUTTON_LABEL);
    	arrConfirmingLabels.add("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_CONVERSION_CHECKBOX_NAME + "\"> <B><I>Confirm</B></I></LABEL>");
    	
    	arrConversionPhaseValues.add(Integer.toString(PROCESS_VENDOR_TABLES_OPTION_VALUE));
    	arrConversionPhaseNames.add(PROCESS_VENDOR_TABLES_BUTTON_LABEL);
    	arrConfirmingLabels.add("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_CONVERSION_CHECKBOX_NAME + "\"> <B><I>Confirm</B></I></LABEL>");
    	
    	arrConversionPhaseValues.add(Integer.toString(PROCESS_VENDOR_STATISTICS_OPTION_VALUE));
    	arrConversionPhaseNames.add(PROCESS_VENDOR_STATISTICS_BUTTON_LABEL);
    	arrConfirmingLabels.add("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_CONVERSION_CHECKBOX_NAME + "\"> <B><I>Confirm</B></I></LABEL>");
    	
    	arrConversionPhaseValues.add(Integer.toString(PROCESS_VENDOR_TRANSACTIONS_OPTION_VALUE));
    	arrConversionPhaseNames.add(PROCESS_VENDOR_TRANSACTIONS_BUTTON_LABEL);
    	arrConfirmingLabels.add("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_CONVERSION_CHECKBOX_NAME + "\"> <B><I>Confirm</B></I></LABEL>");
    	
    	arrConversionPhaseValues.add(Integer.toString(PROCESS_VENDOR_TRANSACTION_LINES_INSERT_OPTION_VALUE));
    	arrConversionPhaseNames.add(PROCESS_VENDOR_TRANSACTION_LINES_INSERT_BUTTON_LABEL);
    	arrConfirmingLabels.add("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_CONVERSION_CHECKBOX_NAME + "\"> <B><I>Confirm</B></I></LABEL>");
    	
    	arrConversionPhaseValues.add(Integer.toString(PROCESS_VENDOR_TRANSACTION_LINES_UPDATE_OPTION_VALUE));
    	arrConversionPhaseNames.add(PROCESS_VENDOR_TRANSACTION_LINES_UPDATE_BUTTON_LABEL);
    	arrConfirmingLabels.add("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_CONVERSION_CHECKBOX_NAME + "\"> <B><I>Confirm</B></I></LABEL>");
    	
    	arrConversionPhaseValues.add(Integer.toString(PROCESS_INSERT_VENDOR_MATCHING_LINES_OPTION_VALUE));
    	arrConversionPhaseNames.add(PROCESS_VENDOR_MATCHING_LINES_BUTTON_LABEL);
    	arrConfirmingLabels.add("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_CONVERSION_CHECKBOX_NAME + "\"> <B><I>Confirm</B></I></LABEL>");

    	arrConversionPhaseValues.add(Integer.toString(PROCESS_UPDATE_APPLY_FROM_MATCHING_LINES_OPTION_VALUE));
    	arrConversionPhaseNames.add(PROCESS_UPDATE_APPLY_FROM_MATCHING_LINES_BUTTON_LABEL);
    	arrConfirmingLabels.add("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_CONVERSION_CHECKBOX_NAME + "\"> <B><I>Confirm</B></I></LABEL>");

    	arrConversionPhaseValues.add(Integer.toString(PROCESS_UPDATE_APPLY_TO_MATCHING_LINES_OPTION_VALUE));
    	arrConversionPhaseNames.add(PROCESS_UPDATE_APPLY_TO_MATCHING_LINES_BUTTON_LABEL);
    	arrConfirmingLabels.add("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_CONVERSION_CHECKBOX_NAME + "\"> <B><I>Confirm</B></I></LABEL>");
    	
    	arrConversionPhaseValues.add(Integer.toString(PROCESS_UPDATE_VENDOR_ADDRESSES_OPTION_VALUE));
    	arrConversionPhaseNames.add(PROCESS_UPDATE_VENDOR_ADDRESSES_BUTTON_LABEL);
    	arrConfirmingLabels.add("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_CONVERSION_CHECKBOX_NAME + "\"> <B><I>Confirm</B></I></LABEL>");

    	String sDefaultPhaseValue = Integer.toString(ROLLBACK_OPTION_VALUE);
    	if (clsManageRequestParameters.get_Request_Parameter(RADIO_FIELD_NAME, request).compareToIgnoreCase("") != 0){
    		sDefaultPhaseValue = clsManageRequestParameters.get_Request_Parameter(RADIO_FIELD_NAME, request);
    	}
    	
    	out.println ("<BR>");
    	//out.print(SMUtilities.Create_Edit_Form_RadioButton_Input_Field(RADIO_FIELD_NAME, arrConversionPhaseNames, arrConversionPhaseValues, sDefaultPhaseValue));
    	
    	out.println(clsCreateHTMLTableFormFields.Create_Edit_Form_RadioButton_Input_Rows (
    		RADIO_FIELD_NAME,
    		arrConversionPhaseNames,
    		arrConversionPhaseValues,
    		arrConfirmingLabels,
    		sDefaultPhaseValue,
    		sDefaultPhaseValue,
    		HIGHLIGHT_ROW_BACKGROUND_COLOR,
    		DEFAULT_ROW_BACKGROUND_COLOR
    		)
    	);
    	
    	out.println ("<BR>");
    	
    	out.println ("<INPUT TYPE=\"SUBMIT\" NAME=\"" + CONVERSION_BUTTON_NAME + "\"VALUE=\"" + CONVERSION_BUTTON_LABEL + "\">");
    	
    	out.println ("</FORM>");
	    out.println("</BODY></HTML>");
	}
}

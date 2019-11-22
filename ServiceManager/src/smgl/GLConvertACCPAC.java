package smgl;

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

public class GLConvertACCPAC extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	public static final String CONVERSION_BUTTON_NAME = "CONVERSION";
	public static final String CONVERSION_BUTTON_LABEL = "----Execute selected function----";
	public static final String CONFIRM_CONVERSION_LABEL = "  Check to confirm data conversion: ";
	public static final String CONFIRM_CONVERSION_CHECKBOX_NAME = "CONFIRMCONVERSION";
	
	public static final String RADIO_FIELD_NAME = "CONVERSIONPHASE";
	
	public static final int NUMBER_OF_CONVERSION_STEPS = 1;
	
	public static final int ROLLBACK_OPTION_VALUE = 1;
	public static final String ROLLBACK_BUTTON_LABEL = Integer.toString(ROLLBACK_OPTION_VALUE) + ") Rollback previous data conversion";
	public static final int PROCESS_GL_SEGMENTS_VALUE = 2;
	public static final String PROCESS_GL_SEGMENTS_BUTTON_LABEL = Integer.toString(PROCESS_GL_SEGMENTS_VALUE) + ") Convert GL Segments";
	public static final int PROCESS_GL_SEGMENT_VALUES_VALUE = 3;
	public static final String PROCESS_GL_SEGMENT_VALUES_BUTTON_LABEL = Integer.toString(PROCESS_GL_SEGMENT_VALUES_VALUE) + ") Convert GL Segment Values";
	public static final int PROCESS_GL_ACCOUNT_STRUCTURES_VALUE = 4;
	public static final String PROCESS_GL_ACCOUNT_STRUCTURES_BUTTON_LABEL = Integer.toString(PROCESS_GL_ACCOUNT_STRUCTURES_VALUE) + ") Convert GL Account Structures";
	public static final int PROCESS_GL_ACCOUNT_GROUPS_VALUE = 5;
	public static final String PROCESS_GL_ACCOUNT_GROUPS_BUTTON_LABEL = Integer.toString(PROCESS_GL_ACCOUNT_GROUPS_VALUE) + ") Convert GL Account Groups";
	public static final int PROCESS_GL_ACCOUNT_MASTER_VALUE = 6;
	public static final String PROCESS_GL_ACCOUNT_MASTER_BUTTON_LABEL = Integer.toString(PROCESS_GL_ACCOUNT_MASTER_VALUE) + ") Convert GL Accounts";
	public static final int PROCESS_GL_FISCAL_CALENDAR_VALUE = 7;
	public static final String PROCESS_GL_FISCAL_CALENDAR_LABEL = Integer.toString(PROCESS_GL_FISCAL_CALENDAR_VALUE) + ") Convert GL Fiscal Calendar";
	public static final int PROCESS_GL_FISCAL_SETS_VALUE = 8;
	public static final String PROCESS_GL_FISCAL_SETS_LABEL = Integer.toString(PROCESS_GL_FISCAL_SETS_VALUE) + ") Convert GL Fiscal Sets";
	public static final int PROCESS_GL_FINANCIALDATA_VALUE = 9;
	public static final String PROCESS_GL_FINANCIALDATA_LABEL = Integer.toString(PROCESS_GL_FINANCIALDATA_VALUE) + ") Convert GL Financial Statement Data";
	public static final int PROCESS_GL_POSTEDTRANSACTIONS_VALUE = 10;
	public static final String PROCESS_GL_POSTEDTRANSACTIONS_LABEL = Integer.toString(PROCESS_GL_POSTEDTRANSACTIONS_VALUE) + ") Convert GL Posted Transactions";

	public static final int LAST_FUNCTION_IN_SEQUENCE = PROCESS_GL_POSTEDTRANSACTIONS_VALUE;
	
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
				SMSystemFunctions.GLConvertACCPACData))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "Convert ACCPAC GL Data";
	    String subtitle = "";
	    
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    
	    String sWarning = (String)CurrentSession.getAttribute(GLConvertACCPACAction.SESSION_ATTRIBUTE_WARNING);
	    CurrentSession.removeAttribute(GLConvertACCPACAction.SESSION_ATTRIBUTE_WARNING);
	    if (sWarning == null){
	    	sWarning = "";
	    }
		if (sWarning.compareToIgnoreCase("") != 0){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		
	    String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>***** NOTE: " + sStatus + "</B><BR>");
		}
	    
		String sResult = (String)CurrentSession.getAttribute(GLConvertACCPACAction.SESSION_ATTRIBUTE_RESULT);
		CurrentSession.removeAttribute(GLConvertACCPACAction.SESSION_ATTRIBUTE_RESULT);
		if (sResult == null){
			sResult = "";
		}
		if (sResult.compareToIgnoreCase("") != 0){
			out.println("<B>***** RESULTS:<BR>" + sResult + "</B><BR>");
		}
		
		//Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to General Ledger Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.GLConvertACCPACData) 
	    		+ "\">Summary</A><BR>");
	    
	    out.println("<BR>This function will read all of the ACCPAC GL data and convert it into the SMCP format, to be used in the SMCP GL module."
	    		+ "  Once this is run, and the SMCP GL functions are first used it CAN NOT be run again.<BR>"
	    );
	    
    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLConvertACCPACAction\">");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
    	out.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='" + this.getClass().getName() + "'>");
    	
    	//Use radio buttons here for each of the conversion phases:
    	ArrayList<String>arrConversionPhaseNames = new ArrayList<String> (0);
    	ArrayList<String>arrConversionPhaseValues = new ArrayList<String> (0);
    	ArrayList<String>arrConfirmingLabels = new ArrayList<String> (0);
    	arrConversionPhaseValues.add(Integer.toString(ROLLBACK_OPTION_VALUE));
    	arrConversionPhaseNames.add(ROLLBACK_BUTTON_LABEL);
    	arrConfirmingLabels.add("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_CONVERSION_CHECKBOX_NAME + "\"> <B><I>Confirm</B></I></LABEL>");
    	
    	arrConversionPhaseValues.add(Integer.toString(PROCESS_GL_SEGMENTS_VALUE));
    	arrConversionPhaseNames.add(PROCESS_GL_SEGMENTS_BUTTON_LABEL);
    	arrConfirmingLabels.add("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_CONVERSION_CHECKBOX_NAME + "\"> <B><I>Confirm</B></I></LABEL>");
    	
    	arrConversionPhaseValues.add(Integer.toString(PROCESS_GL_SEGMENT_VALUES_VALUE));
    	arrConversionPhaseNames.add(PROCESS_GL_SEGMENT_VALUES_BUTTON_LABEL);
    	arrConfirmingLabels.add("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_CONVERSION_CHECKBOX_NAME + "\"> <B><I>Confirm</B></I></LABEL>");
    	
    	arrConversionPhaseValues.add(Integer.toString(PROCESS_GL_ACCOUNT_STRUCTURES_VALUE));
    	arrConversionPhaseNames.add(PROCESS_GL_ACCOUNT_STRUCTURES_BUTTON_LABEL);
    	arrConfirmingLabels.add("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_CONVERSION_CHECKBOX_NAME + "\"> <B><I>Confirm</B></I></LABEL>");
    	
    	arrConversionPhaseValues.add(Integer.toString(PROCESS_GL_ACCOUNT_GROUPS_VALUE));
    	arrConversionPhaseNames.add(PROCESS_GL_ACCOUNT_GROUPS_BUTTON_LABEL);
    	arrConfirmingLabels.add("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_CONVERSION_CHECKBOX_NAME + "\"> <B><I>Confirm</B></I></LABEL>");
    	
    	arrConversionPhaseValues.add(Integer.toString(PROCESS_GL_ACCOUNT_MASTER_VALUE));
    	arrConversionPhaseNames.add(PROCESS_GL_ACCOUNT_MASTER_BUTTON_LABEL);
    	arrConfirmingLabels.add("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_CONVERSION_CHECKBOX_NAME + "\"> <B><I>Confirm</B></I></LABEL>");
    	
    	arrConversionPhaseValues.add(Integer.toString(PROCESS_GL_FISCAL_CALENDAR_VALUE));
    	arrConversionPhaseNames.add(PROCESS_GL_FISCAL_CALENDAR_LABEL);
    	arrConfirmingLabels.add("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_CONVERSION_CHECKBOX_NAME + "\"> <B><I>Confirm</B></I></LABEL>");
    	
    	arrConversionPhaseValues.add(Integer.toString(PROCESS_GL_FISCAL_SETS_VALUE));
    	arrConversionPhaseNames.add(PROCESS_GL_FISCAL_SETS_LABEL);
    	arrConfirmingLabels.add("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_CONVERSION_CHECKBOX_NAME + "\"> <B><I>Confirm</B></I></LABEL>");
    	
    	arrConversionPhaseValues.add(Integer.toString(PROCESS_GL_FINANCIALDATA_VALUE));
    	arrConversionPhaseNames.add(PROCESS_GL_FINANCIALDATA_LABEL);
    	arrConfirmingLabels.add("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_CONVERSION_CHECKBOX_NAME + "\"> <B><I>Confirm</B></I></LABEL>");
    	
    	arrConversionPhaseValues.add(Integer.toString(PROCESS_GL_POSTEDTRANSACTIONS_VALUE));
    	arrConversionPhaseNames.add(PROCESS_GL_POSTEDTRANSACTIONS_LABEL);
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

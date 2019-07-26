package smar;

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
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTablearterms;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsManageRequestParameters;

public class AREditTermsEdit extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Terms";
	private static String sCalledClassName = "AREditTermsAction";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.AREditTerms))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		//Load the input class from the request object - if it's a 'resubmit', then this will contain
	    //all the values typed from the previous screen.  If it's a 'first time' edit, then this will only
	    //contain the terms code
		ARTerms terms = new ARTerms("");
		terms.loadFromHTTPRequest(request);
	    //First process if it's a 'delete':
	    if(request.getParameter("SubmitDelete") != null){
		    if (request.getParameter("ConfirmDelete") == null){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditTerms"
					+ "?" + ARTerms.ParamsTermsCode + "=" + terms.getM_sTermsCode()
					+ "&Warning=You must check the 'confirming' check box to delete."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
		    }
		    if (terms.getM_sTermsCode().compareToIgnoreCase("") == 0){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditTerms"
					+ "?" + ARTerms.ParamsTermsCode + "=" + terms.getM_sTermsCode()
					+ "&Warning=You must select a terms code to delete."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
		    }
		    
		    else{

			    if (!terms.delete(terms.getM_sTermsCode(), getServletContext(), sDBID)){
    				response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditTerms"
    					+ "?" + ARTerms.ParamsTermsCode + "=" + terms.getM_sTermsCode()
        				+ "&Warning=Error deleting terms."
        				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    				);
						return;
			    }
		    }
	    }
	    
		String title = "";
		String subtitle = "";		
		if(request.getParameter("SubmitAdd") != null){
			terms.setM_sTermsCode("");
			terms.setM_bNewRecord("1");
		}
		//If we are coming here for the first time to edit (i.e., it's not a re-submit), then we need to 
		//load the input class from the database if possible:
		if(request.getParameter("SubmitEdit") != null){

			if(!terms.load(getServletContext(), sDBID)){
				response.sendRedirect(
        			"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditTerms"
        			+ "?" + ARTerms.ParamsTermsCode + "=" + terms.getM_sTermsCode()
					+ "&Warning=Could not load terms " + terms.getM_sTermsCode() + "."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
	    	}
		}
		
		//In any other case, such as the possibility that this is a 'resubmit', we need to edit the customer:
    	title = "Edit " + sObjectName;
	    subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Receivable Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.AREditTerms) 
	    		+ "\">Summary</A><BR><BR>");

		//If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
	    if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    Edit_Record(terms, out, sDBID, sUserName);
		
		out.println("</BODY></HTML>");
		return;
	}
	private void Edit_Record(
			ARTerms terms, 
			PrintWriter pwOut, 
			String sDBID,
			String sUser
			){
	    
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smar." + sCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		if(terms.getM_iNewRecord().compareToIgnoreCase("0") == 0){
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ARTerms.ParamsTermsCode + "\" VALUE=\"" + terms.getM_sTermsCode() + "\">");
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ARTerms.ParamsAddingNewRecord + "\" VALUE=0>");
		}else{
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ARTerms.ParamsAddingNewRecord + "\" VALUE=1>");
		}
	    pwOut.println("<TABLE BORDER=12 CELLSPACING=2>");

        //Terms code:
	    if(terms.getM_iNewRecord().compareToIgnoreCase("1") == 0){
	        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		ARTerms.ParamsTermsCode, 
	        		terms.getM_sTermsCode().replace("\"", "&quot;"),  
	        		SMTablearterms.sTermsCodeLength, 
	        		"Terms code:", 
	        		"Up to " + SMTablearterms.sTermsCodeLength + " characters.",
	        		"" + SMTablearterms.sTermsCodeLength
	        	)
	        );
	    	
	    }else{
	    	pwOut.println("<TD ALIGN=RIGHT><B>Terms code:</B></TD><TD>" 
	    		+ terms.getM_sTermsCode().replace("\"", "&quot;") + "</TD><TD>&nbsp;</TD>");
	    }
	    
		//Active?
	    String sTrueOrFalse = "true";
	    if (terms.getM_iActive().compareToIgnoreCase("1") == 0){
	    	sTrueOrFalse = "true";
	    }else{
	    	sTrueOrFalse = "false";
	    }
	    pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
			ARTerms.ParamiActive, 
			sTrueOrFalse, 
			"Active terms?", 
			"Uncheck to de-activate these terms."
			)
		);
	    
        //Description:
		pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				ARTerms.ParamsDescription, 
        		terms.getM_sDescription().replace("\"", "&quot;"), 
        		SMTablearterms.sDescriptionLength, 
        		"Terms description:", 
        		"Up to " + SMTablearterms.sDescriptionLength + " characters.",
        		"" + SMTablearterms.sDescriptionLength
        	)
        );

		//Discount percent:
        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
        		ARTerms.ParamdDiscountPercent,
        		terms.getM_dDiscountPercent().replace("\"", "&quot;"), 
        		18, 
        		"Discount percentage:", 
        		"Enter WITHOUT percent sign - enter a zero if no discount is offered.",
        		"18"
        	)
        );

		//iDiscountNumberOfDays
        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
        		ARTerms.ParamiDiscountNumberOfDays,
        		terms.getM_iDiscountNumberOfDays().replace("\"", "&quot;"), 
        		3, 
        		"Discount number of days:", 
        		"Used if discount is offered after a specified number of days; otherwise, enter a zero.",
        		"3"
        	)
        );
		
		//iDiscountDayOfTheMonth
        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
        		ARTerms.ParamiDiscountDayOfTheMonth,
        		terms.getM_iDiscountDayOfTheMonth().replace("\"", "&quot;"), 
        		3, 
        		"Discount day of the month:", 
        		"Used if discount is offered by payment on a particular day of the month; otherwise, enter a zero.",
        		"3"
        	)
        );
		
		//iDueNumberOfDays
        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
        		ARTerms.ParamiDueNumberOfDays,
        		terms.getM_iDueNumberOfDays().replace("\"", "&quot;"), 
        		3, 
        		"Due number of days:", 
        		"Used if payment is due after a specified number of days; otherwise, enter a zero.",
        		"3"
        	)
        );
		
		//iDueDayOfTheMonth
        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
        		ARTerms.ParamiDueDayOfTheMonth,
        		terms.getM_iDueDayOfTheMonth().replace("\"", "&quot;"), 
        		3, 
        		"Due day of the month:", 
        		"Used if payment is due on a specified day of the month; otherwise, enter a zero.",
        		"3"
        	)
        );
        
        pwOut.println("</TABLE>");
        pwOut.println("<BR>");
        pwOut.println("<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Update " + sObjectName + "' STYLE='height: 0.24in'></P>");
        pwOut.println("</FORM>");
		
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

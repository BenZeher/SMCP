package smfa;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsManageRequestParameters;

public class FAPeriodEndProcessingSelect extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String sFAPeriodEndProcessingActionCalledClassName = "FAPeriodEndProcessingAction";

	private SimpleDateFormat sdfDateOnly = new SimpleDateFormat("MM/dd/yyyy");
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.FAPeriodEndProcessing)){
	    	return;
	    }
	    String title = "Period End Processing.";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    
		//If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
	    if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
	    if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>STATUS: " + sStatus + "</B><BR>");
		}
	    
	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Fixed Assets Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.FAPeriodEndProcessing) 
	    		+ "\">Summary</A><BR><BR>");
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa." + sFAPeriodEndProcessingActionCalledClassName + "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    try{
	    	List_Criteria(out, sDBID, sUserName, request);
	    }catch (Exception e){
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAMainMenu"
					+ "?Warning=" + clsServletUtilities.URLEncode("Error displaying period end criteria selecting screen : " + e.getMessage())
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
	    }
	    
		out.println("</FORM>");
    	
		out.println ("<NOSCRIPT>\n"
 			   + "    <font color=red>\n"
 			   + "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
 			   + "    </font>\n"
 			   + "</NOSCRIPT>\n"
				
 			   + "<script type=\"text/javascript\">\n"
 			   
			   + "function confirmation(){\n"
			   + "    if (confirm('This will generate all the depreciation for the selected fiscal year and period - are you sure you want to continue?')) {\n"
			   + "      document.MAINFORM.submit();\n"
			   + " 	  }\n"
			   + "}\n"
			   +"   window.addEventListener(\"beforeunload\",function(){\n" 
			   +  "      document.documentElement.style.cursor = \"not-allowed\";\n "
			   +  "     document.documentElement.style.cursor = \"wait\";\n"
			   +"      });\n"
			   
			+ "</script>\n");
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
	
	private void List_Criteria(PrintWriter pwOut, 
							   String sDBIB,
							   String sUser,
							   HttpServletRequest req) throws Exception{

	    pwOut.println("<TABLE BORDER=12 CELLSPACING=2>");
        
        ArrayList<String> sValues = new ArrayList<String>();
        ArrayList<String> sDescriptions = new ArrayList<String>();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        
	    try{
		    //Fiscal year:
	        sValues.clear();
	        sDescriptions.clear();
	        //First, add a blank to make sure the user selects one:
	        sValues.add("");
	        sDescriptions.add("--- Select a fiscal year ---");
	        for (int i=0;i<100;i++){
	        	sValues.add(String.valueOf(1970 + i));
	        	sDescriptions.add(String.valueOf(1970 + i));
	        }
	        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
	        		"FISCALYEAR", 
	        		sValues, 
	        		String.valueOf(c.get(Calendar.YEAR)), 
	        		sDescriptions, 
	        		"Depreciate all assets for fiscal year:", 
	        		"&nbsp;"
	        	)
	        ); 

		    //Fiscal period:
	        sValues.clear();
	        sDescriptions.clear();
	        //First, add a blank to make sure the user selects one:
	        sValues.add("");
	        sDescriptions.add("-- Select a fiscal period --");
	        for (int i=1;i<=13;i++){
	        	sValues.add(String.valueOf(i));
	        	sDescriptions.add(String.valueOf(i));
	        }
	        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
	        		"FISCALPERIOD", 
	        		sValues, 
	        		String.valueOf(c.get(Calendar.MONTH) + 1), 
	        		sDescriptions, 
	        		"Depreciate all assets for period:", 
	        		"&nbsp;"
	        	)
	        ); 
	        
	    }catch (Exception e){
	    	throw new Exception ("Error: " + e.getMessage());
	    }

		//Transaction Date:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT>" + "Transaction date:</TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"TRANSACTIONDATE\""
        		+ " VALUE=\"" + sdfDateOnly.format(new Date(c.getTimeInMillis())).replace("\"", "&quot;") + "\""
        		+ " SIZE=20"
        		+ " MAXLENGTH=" + "10"
        		+ " STYLE=\"height: 0.25in\""
        		+ ">"
        		+ SMUtilities.getDatePickerString("TRANSACTIONDATE", getServletContext())
        		+ "</TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "In <B>mm/dd/yyyy</B> format."
        		+ "</TD>"
        		+ "</TR>"
        		);
	    
        
        //checkboxes
        
        /* TJR - 1/19/2017 - removed this:
        //Provisional posting?
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT>Provisional posting? </TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=CHECKBOX NAME=\"PROVISIONALPOSTING\""
        		+ SMUtilities.CHECKBOX_UNCHECKED_STRING
        		+ " STYLE=\"height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>&nbsp;</TD>"
        		+ "</TR>"
        		);
        */
        //Consolidate GL batch details?
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT>Consolidate GL batch details? </TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=CHECKBOX NAME=\"CONSOLIDATEGLBATCHDETAILS\""
        		+ clsServletUtilities.CHECKBOX_CHECKED_STRING
        		+ " STYLE=\"height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>&nbsp;</TD>"
        		+ "</TR>"
        		);
       
        pwOut.println("</TABLE>");
        
        //pwOut.println("<BR>");
        pwOut.println("<P><input type=button value=\"----Process----\" onClick=\"confirmation()\"></P>");
  
	}
}

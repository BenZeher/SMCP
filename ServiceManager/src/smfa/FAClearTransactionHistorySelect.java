package smfa;

import java.io.IOException;
import java.io.PrintWriter;
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

public class FAClearTransactionHistorySelect extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String FA_TRANSACTION_HISTORY_CONFIRMCLEARING = "CONFIRMCLEARING";
	//private static String sObjectName = "Transaction";

	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCalledClassName = "FAClearTransactionHistoryAction";
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.FAClearTransactionHistory)){
	    	return;
	    }

	    String title = "Clear Transaction History.";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

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
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.FAClearTransactionHistory) 
	    		+ "\">Summary</A><BR><BR>");
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa." + sCalledClassName + "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");

	    try{
	    	List_Criteria(out, sDBID, sUserName, request);
	    }catch (Exception e){
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAMainMenu"
					+ "?Warning=" + clsServletUtilities.URLEncode("Error displaying clear transaction history criteria selecting screen : " + e.getMessage())
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
			   + "    if (confirm('This will clear all transactions up to and including the selected fiscal year and period - are you sure you want to continue?')) {\n"
			   + "      document.MAINFORM.submit();\n"
			   + " 	  }\n"
			   + "}\n"
			   
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
	        		"Clear transactions UP TO AND INCLUDING fiscal year:", 
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
	        		"And UP TO AND INCLUDING period:", 
	        		"&nbsp;"
	        	)
	        ); 
	        
	    }catch (Exception e){
	    	throw new Exception ("Error: " + e.getMessage());
	    }
        pwOut.println("</TABLE>");
        
        //pwOut.println("<BR>");
        pwOut.println("<P><input type=button value=\"----Clear History----\" onClick=\"confirmation()\"></P>");
        
  
	}
	    
	    
}
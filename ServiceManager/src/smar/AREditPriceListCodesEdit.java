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
import SMDataDefinition.SMTablepricelistcodes;

public class AREditPriceListCodesEdit extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final String sObjectName = "Price List Code";
	private static final String sCalledClassName = "AREditPriceListCodesAction";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.AREditPriceListCodes))
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
	    //contain the code
		SMPriceListCode plc = new SMPriceListCode("");
		plc.loadFromHTTPRequest(request);
	    //First process if it's a 'delete':
	    if(request.getParameter("SubmitDelete") != null){
		    if (request.getParameter("ConfirmDelete") == null){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditPriceListCodes"
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + SMPriceListCode.ParamsPriceListCode + "=" + plc.getM_sPriceListCode()
					+ "&Warning=You must check the 'confirming' check box to delete."
					
				);
				return;
		    }
		    if (plc.getM_sPriceListCode().compareToIgnoreCase("") == 0){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditPriceListCodes"
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + SMPriceListCode.ParamsPriceListCode + "=" + plc.getM_sPriceListCode()
					+ "&Warning=You must select a price list code to delete."
				);
				return;
		    }else{
		    	try{
		    		plc.delete(plc.getM_sPriceListCode(), getServletContext(), sDBID);
		    	}catch (Exception e){
    				response.sendRedirect(
       						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditPriceListCodes"
        					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
       						+ "&" + SMPriceListCode.ParamsPriceListCode + "=" + plc.getM_sPriceListCode()
            				+ "&Warning=" + e.getMessage()
        				);
   						return;
		    	}
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditPriceListCodes"
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + SMPriceListCode.ParamsPriceListCode + "=" + plc.getM_sPriceListCode()
					+ "&Status=Successfully deleted price list code " + plc.getM_sPriceListCode() + "."
					
				);
				return;
		    }
	    }
	    
		String title = "";
		String subtitle = "";		
		if(request.getParameter("SubmitAdd") != null){
			plc.setM_sPriceListCode("");
			plc.setM_iNewRecord("1");
		}
		//If we are coming here for the first time to edit (i.e., it's not a re-submit), then we need to 
		//load the input class from the database if possible:
		if(request.getParameter("SubmitEdit") != null){

			if(!plc.load(getServletContext(), sDBID)){
				response.sendRedirect(
  					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditPriceListCodes"
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
  					+ "&" + SMPriceListCode.ParamsPriceListCode + "=" + plc.getM_sPriceListCode()
					+ "&Warning=Could not load price list code " + plc.getM_sPriceListCode() + "."
					
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
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.AREditPriceListCodes) 
	    		+ "\">Summary</A><BR><BR>");

		//If there is a warning from trying to input previously, print it here:
		String sWarning = ARUtilities.get_Request_Parameter("Warning", request);
	    if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    Edit_Record(plc, out, sDBID, sUserName);
		
		out.println("</BODY></HTML>");
		return;
	}
	private void Edit_Record(
			SMPriceListCode plc, 
			PrintWriter pwOut, 
			String sDBID,
			String sUser
			){
	    
        //Date last maintained:
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smar." + sCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		if(plc.getM_iNewRecord().compareToIgnoreCase("0") == 0){
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMPriceListCode.ParamsPriceListCode + "\" VALUE=\"" + plc.getM_sPriceListCode() + "\">");
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMPriceListCode.ParamsAddingNewRecord + "\" VALUE=0>");
		}else{
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMPriceListCode.ParamsAddingNewRecord + "\" VALUE=1>");
		}
	    pwOut.println("<TABLE BORDER=12 CELLSPACING=2>");

        //Code:
	    if(plc.getM_iNewRecord().compareToIgnoreCase("1") == 0){
	        pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Row(
	        		SMPriceListCode.ParamsPriceListCode, 
	        		plc.getM_sPriceListCode().replace("\"", "&quot;"),  
	        		SMTablepricelistcodes.spricelistcodeLength, 
	        		"Price list code:", 
	        		"Up to " + SMTablepricelistcodes.spricelistcodeLength + " characters.",
	        		"1.6"
	        	)
	        );
	    	
	    }else{
	    	pwOut.println("<TD ALIGN=RIGHT><B>Price list code:</B></TD><TD>" 
	    		+ plc.getM_sPriceListCode().replace("\"", "&quot;") + "</TD><TD>&nbsp;</TD>");
	    }
	    
        //Description:
		pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Row(
				SMPriceListCode.ParamsDescription, 
				plc.getM_sDescription().replace("\"", "&quot;"), 
				SMTablepricelistcodes.sdescriptionLength, 
        		"Code description:", 
        		"Up to " + SMTablepricelistcodes.sdescriptionLength + " characters.",
        		"3.2"
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

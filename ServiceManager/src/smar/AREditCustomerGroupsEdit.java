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
import SMDataDefinition.SMTablearcustomergroups;

public class AREditCustomerGroupsEdit extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Customer Group";
	private static String sCalledClassName = "AREditCustomerGroupsAction";
	private static String sDBID = "";
	private static String sUserFullName = "";
	private static String sCompanyName = "";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.AREditCustomerGroups))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME)
	    		 + " " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		//Load the input class from the request object - if it's a 'resubmit', then this will contain
	    //all the values typed from the previous screen.  If it's a 'first time' edit, then this will only
	    //contain the gl acct
		ARCustomerGroup group = new ARCustomerGroup("");
		group.loadFromHTTPRequest(request);
	    //First process if it's a 'delete':
	    if(request.getParameter("SubmitDelete") != null){
		    if (request.getParameter("ConfirmDelete") == null){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomerGroups"
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + ARCustomerGroup.ParamsGroupCode + "=" + group.getM_sGroupCode()
					+ "&Warning=You must check the 'confirming' check box to delete."
					
				);
				return;
		    }
		    if (group.getM_sGroupCode().compareToIgnoreCase("") == 0){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomerGroups"
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + ARCustomerGroup.ParamsGroupCode + "=" + group.getM_sGroupCode()
					+ "&Warning=You must enter a customer group to delete."
					
				);
				return;
		    }
		    
		    else{
			    if (!group.delete(group.getM_sGroupCode(), getServletContext(),sDBID)){
    				response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomerGroups"
    					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    					+ "&" + ARCustomerGroup.ParamsGroupCode + "=" + group.getM_sGroupCode()
    					+ "&Warning=Error deleting customer group."
    				);
						return;
			    }else{
    				response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomerGroups"
    					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    					+ "&" + ARCustomerGroup.ParamsGroupCode + "=" + group.getM_sGroupCode()
    					+ "&Status=Successfully deleted customer group " + group.getM_sGroupCode() + "."
    				);
					return;
			    }
		    }
	    }
	    
		String title = "";
		String subtitle = "";		
		if(request.getParameter("SubmitAdd") != null){
			group.setM_sGroupCode("");
			group.setM_bNewRecord("1");
		}
		//If we are coming here for the first time to edit (i.e., it's not a re-submit), then we need to 
		//load the input class from the database if possible:
		if(request.getParameter("SubmitEdit") != null){
			if(!group.load(getServletContext(), sDBID)){
				response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomerGroups"
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + ARCustomerGroup.ParamsGroupCode + "=" + group.getM_sGroupCode()
					+ "&Warning=Could not load customer group " + group.getM_sGroupCode() + "."
					
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
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.AREditCustomerGroups) 
	    		+ "\">Summary</A><BR><BR>");

		//If there is a warning from trying to input previously, print it here:
		String sWarning = ARUtilities.get_Request_Parameter("Warning", request);
	    if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    Edit_Record(group, out, sDBID, sUserFullName);
		
		out.println("</BODY></HTML>");
		return;
	}
	private void Edit_Record(
			ARCustomerGroup group, 
			PrintWriter pwOut, 
			String sConf,
			String sUserFullName
			){
	    
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smar." + sCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		if(group.getM_iNewRecord().compareToIgnoreCase("0") == 0){
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ARCustomerGroup.ParamsGroupCode + "\" VALUE=\"" + group.getM_sGroupCode() + "\">");
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ARCustomerGroup.ParamsAddingNewRecord + "\" VALUE=0>");
		}else{
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ARCustomerGroup.ParamsAddingNewRecord + "\" VALUE=1>");
		}
		
        //Date last maintained:
		pwOut.println("Date last maintained: " + group.getM_datLastMaintained());
		pwOut.println(" by user: " + group.getM_sLastEditUserFullName() + "<BR>");
        
	    pwOut.println("<TABLE BORDER=12 CELLSPACING=2>");

        //Group code:
	    if(group.getM_iNewRecord().compareToIgnoreCase("1") == 0){
	        pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Row(
	        		ARCustomerGroup.ParamsGroupCode, 
	        		group.getM_sGroupCode().replace("\"", "&quot;"),  
	        		SMTablearcustomergroups.sGroupCodeLength, 
	        		"Group code:", 
	        		"Up to " + SMTablearcustomergroups.sGroupCodeLength + " characters.",
	        		"1.6"
	        	)
	        );
	    	
	    }else{
	    	pwOut.println("<TD ALIGN=RIGHT><B>Group code:</B></TD><TD>" 
	    			+ group.getM_sGroupCode().replace("\"", "&quot;") + "</TD><TD>&nbsp;</TD>");
	    }
	    
		//Active?
	    String sTrueOrFalse = "true";
	    if (group.getM_iActive().compareToIgnoreCase("1") == 0){
	    	sTrueOrFalse = "true";
	    }else{
	    	sTrueOrFalse = "false";
	    }
	    pwOut.println(ARUtilities.Create_Edit_Form_Checkbox_Row(
	    	ARCustomerGroup.ParamiActive, 
			sTrueOrFalse, 
			"Active group?", 
			"Uncheck to de-activate this group."
			)
		);
	    
        //Description:
		pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Row(
				ARCustomerGroup.ParamsDescription, 
        		group.getM_sDescription().replace("\"", "&quot;"), 
        		SMTablearcustomergroups.sDescriptionLength, 
        		"Group description:", 
        		"Up to " + SMTablearcustomergroups.sDescriptionLength + " characters.",
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

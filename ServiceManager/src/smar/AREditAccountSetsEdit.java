package smar;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTablearacctset;
import SMDataDefinition.SMTableglaccounts;
import ServletUtilities.clsDatabaseFunctions;

public class AREditAccountSetsEdit extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static final String sObjectName = "Account Set";
	private static final String sCalledClassName = "AREditAccountSetsAction";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.AREditAccountSets))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		//Load the input class from the request object - if it's a 'resubmit', then this will contain
	    //all the values typed from the previous screen.  If it's a 'first time' edit, then this will only
	    //contain the code
		ARAccountSet set = new ARAccountSet("");
		set.loadFromHTTPRequest(request);
	    //First process if it's a 'delete':
	    if(request.getParameter("SubmitDelete") != null){
		    if (request.getParameter("ConfirmDelete") == null){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditAccountSets"
					+ "?" + ARAccountSet.ParamsAcctSetCode + "=" + set.getM_sAcctSetCode()
					+ "&Warning=You must check the 'confirming' check box to delete."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
		    }
		    if (set.getM_sAcctSetCode().compareToIgnoreCase("") == 0){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditAccountSets"
					+ "?" + ARAccountSet.ParamsAcctSetCode + "=" + set.getM_sAcctSetCode()
					+ "&Warning=You must select an account set to delete."
					+ "&SESSIO" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "NTAG=" + sDBID
				);
				return;
		    }
		    
		    else{
			    if (!set.delete(set.getM_sAcctSetCode(), getServletContext(), sDBID)){
    				response.sendRedirect(
   						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditAccountSets"
   						+ "?" + ARAccountSet.ParamsAcctSetCode + "=" + set.getM_sAcctSetCode()
        				+ "&Warning=Error deleting account set."
        				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    				);
						return;
			    }else{
    				response.sendRedirect(
   						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditAccountSets"
   						+ "?" + ARAccountSet.ParamsAcctSetCode + "=" + set.getM_sAcctSetCode()
    					+ "&Status=Successfully" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    				);
					return;
			    }
		    }
	    }
	    
		String title = "";
		String subtitle = "";		
		if(request.getParameter("SubmitAdd") != null){
			set.setM_sAcctSetCode("");
			set.setM_iNewRecord("1");
		}
		//If we are coming here for the first time to edit (i.e., it's not a re-submit), then we need to 
		//load the input class from the database if possible:
		if(request.getParameter("SubmitEdit") != null){
			if(!set.load(getServletContext(), sDBID)){
				response.sendRedirect(
  					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditAccountSets"
  					+ "?" + ARAccountSet.ParamsAcctSetCode + "=" + set.getM_sAcctSetCode()
					+ "&Warning=Could not load account set " + set.getM_sAcctSetCode() + "."
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
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.AREditAccountSets) 
	    		+ "\">Summary</A><BR><BR>");

		//If there is a warning from trying to input previously, print it here:
		String sWarning = ARUtilities.get_Request_Parameter("Warning", request);
	    if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    Edit_Record(set, out, sDBID, sUserName, sUserFullName);
		
		out.println("</BODY></HTML>");
		return;
	}
	private void Edit_Record(
			ARAccountSet set, 
			PrintWriter pwOut, 
			String sDBID,
			String sUserID,
			String sUserFullName
			){
	    
        //Date last maintained:
		pwOut.println("Date last maintained: " + set.getM_datLastMaintained() + ".<BR>");
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smar." + sCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		if(set.getM_iNewRecord().compareToIgnoreCase("0") == 0){
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ARAccountSet.ParamsAcctSetCode + "\" VALUE=\"" + set.getM_sAcctSetCode() + "\">");
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ARAccountSet.ParamsAddingNewRecord + "\" VALUE=0>");
		}else{
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ARAccountSet.ParamsAddingNewRecord + "\" VALUE=1>");
		}
	    pwOut.println("<TABLE BORDER=12 CELLSPACING=2>");

        //Account set code:
	    if(set.getM_iNewRecord().compareToIgnoreCase("1") == 0){
	        pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Row(
	        		ARAccountSet.ParamsAcctSetCode, 
	        		set.getM_sAcctSetCode().replace("\"", "&quot;"),  
	        		SMTablearacctset.sAcctSetCodeLength, 
	        		"Acct set code:", 
	        		"Up to " + SMTablearacctset.sAcctSetCodeLength + " characters.",
	        		"1.6"
	        	)
	        );
	    	
	    }else{
	    	pwOut.println("<TD ALIGN=RIGHT><B>Account set code:</B></TD><TD>" 
	    		+ set.getM_sAcctSetCode().replace("\"", "&quot;") + "</TD><TD>&nbsp;</TD>");
	    }
	    
		//Active?
	    String sTrueOrFalse = "true";
	    if (set.getM_iActive().compareToIgnoreCase("1") == 0){
	    	sTrueOrFalse = "true";
	    }else{
	    	sTrueOrFalse = "false";
	    }
	    pwOut.println(ARUtilities.Create_Edit_Form_Checkbox_Row(
	    		ARAccountSet.ParamiActive, 
			sTrueOrFalse, 
			"Active acct set?", 
			"Uncheck to de-activate this account set."
			)
		);
	    
        //Description:
		pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Row(
				ARAccountSet.ParamsDescription, 
        		set.getM_sDescription().replace("\"", "&quot;"), 
        		SMTablearacctset.sDescriptionLength, 
        		"Acct set description:", 
        		"Up to " + SMTablearacctset.sDescriptionLength + " characters.",
        		"3.2"
        	)
        );

        //Select from list of GL Accts:
        ArrayList<String> sValues = new ArrayList<String>();
        ArrayList<String> sDescriptions = new ArrayList<String>();
        try{
			//Get the record to edit:
	        String sSQL = SMClasses.MySQLs.Get_GL_Account_List_SQL(true);
	        ResultSet rsGLAccts = clsDatabaseFunctions.openResultSet(
	        	sSQL, 
	        	getServletContext(), 
	        	sDBID,
	        	"MySQL",
	        	this.toString() + ".Edit_Record - User: " + sUserID
	        	+ " - "
	        	+ sUserFullName
	        		);
	        
			//Print out directly so that we don't waste time appending to string buffers:
	        sValues.add("");
	        sDescriptions.add("*** Select a GL Account ***");
	        while (rsGLAccts.next()){
	        	sValues.add((String) rsGLAccts.getString(SMTableglaccounts.sAcctID).trim());
	        	sDescriptions.add((String) rsGLAccts.getString(SMTableglaccounts.sAcctID).trim() + " - " + (String) rsGLAccts.getString(SMTableglaccounts.sDesc).trim());
			}
	        rsGLAccts.close();
		}catch (SQLException ex){
	    	System.out.println("Error in " + this.toString()+ " class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			//return false;
		}
		
	    //A/R Control Acct:
        pwOut.println(ARUtilities.Create_Edit_Form_List_Row(
        		ARAccountSet.ParamdsAcctsReceivableControlAcct, 
        		sValues, 
        		set.getM_sAcctsReceivableControlAcct().replace("\"", "&quot;"),  
        		sDescriptions, 
        		"Accounts Receivable Control:", 
        		"Select the A/R Control Account."
        		)
        );

		//ReceiptDiscountsAcct
        pwOut.println(ARUtilities.Create_Edit_Form_List_Row(
        		ARAccountSet.ParamsReceiptDiscountsAcct, 
        		sValues, 
        		set.getM_sReceiptDiscountsAcct().replace("\"", "&quot;"),  
        		sDescriptions, 
        		"Receipt Discount:", 
        		"Select the Receipt Discount Account."
        		)
        );
		//PrepaymentLiabilityAcct
        pwOut.println(ARUtilities.Create_Edit_Form_List_Row(
        		ARAccountSet.ParamsPrepaymentLiabilityAcct, 
        		sValues, 
        		set.getM_sPrepaymentLiabilityAcct().replace("\"", "&quot;"),  
        		sDescriptions, 
        		"Prepayment Liability:", 
        		"Select the Prepayment Liability Account."
        		)
        );
        
		//WriteOffAcct
        pwOut.println(ARUtilities.Create_Edit_Form_List_Row(
        		ARAccountSet.ParamsWriteOffAcct, 
        		sValues, 
        		set.getM_sWriteOffAcct().replace("\"", "&quot;"),  
        		sDescriptions, 
        		"Write-Off:", 
        		"Select the Write-Off Account."
        		)
        );
		//RetainageAcct
        pwOut.println(ARUtilities.Create_Edit_Form_List_Row(
        		ARAccountSet.ParamsRetainageAcct, 
        		sValues, 
        		set.getM_sRetainageAcct().replace("\"", "&quot;"),  
        		sDescriptions, 
        		"Retainage:", 
        		"Select the Retainage Account."
        		)
        );       
		//CashAcct
        pwOut.println(ARUtilities.Create_Edit_Form_List_Row(
        		ARAccountSet.ParamsCashAcct, 
        		sValues, 
        		set.getM_sCashAcct().replace("\"", "&quot;"),  
        		sDescriptions, 
        		"Cash:", 
        		"Select the Cash Account."
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

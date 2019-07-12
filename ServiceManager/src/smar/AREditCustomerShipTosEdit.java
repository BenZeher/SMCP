package smar;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTablearcustomershiptos;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class AREditCustomerShipTosEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String sObjectName = "Customer ship-to";
	private static final String sCalledClassName = "AREditCustomerShipTosAction";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.AREditCustomerShipToLocations))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		//Load the input class from the request object - if it's a 'resubmit', then this will contain
	    //all the values typed from the previous screen.  If it's a 'first time' edit, then this will only
	    //contain the customer number
	    ARCustomerShipTo shipto = new ARCustomerShipTo("","");
	    shipto.loadFromHTTPRequest(request);
	    //First process if it's a 'delete':
	    if(request.getParameter("SubmitDelete") != null){
		    if (request.getParameter("ConfirmDelete") == null){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomerShipTos"
					+ "?Warning=You must check the 'confirming' check box to delete."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
		    }
		    //Get the customer code and the ship-to code:
			shipto.setM_sCustomerNumber(
				clsStringFunctions.StringLeft(
					clsManageRequestParameters.get_Request_Parameter("EditCode", request),
						SMTablearcustomershiptos.sCustomerNumberLength));
			shipto.setM_sShipToCode(
					clsStringFunctions.StringRight(
					clsManageRequestParameters.get_Request_Parameter("EditCode", request),
						SMTablearcustomershiptos.sShipToCodeLength));
		    if (shipto.getM_sCustomerNumber().compareToIgnoreCase("") == 0){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomerShipTos"
					+ "?Warning=You must enter a customer code to delete."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
		    }
		    if (shipto.getM_sShipToCode().compareToIgnoreCase("") == 0){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomerShipTos"
					+ "?Warning=You must enter a ship-to code to delete."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
		    }
		    else{
		    	//Need a connection for the delete here:
		    	Connection conn = clsDatabaseFunctions.getConnection(
		    		getServletContext(), 
		    		sDBID,
		    		"MySQL",
		    		this.toString() + ".doPost - USer: " 
		    		+ sUserID
		    		+ " - "
		    		+ sUserFullName
		    			);
		    	if(conn == null){
    				response.sendRedirect(
        					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomerShipTos"
        					+ "?Warning=Error deleting customer."
        					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
        				);
    						return;
		    	}
		    	clsDatabaseFunctions.start_data_transaction(conn);
			    if (!shipto.delete(shipto.getM_sCustomerNumber(), shipto.getM_sShipToCode(), conn)){
			    	clsDatabaseFunctions.rollback_data_transaction(conn);
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067538]");
    				response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomerShipTos"
    					+ "?Warning=Error deleting customer ship-to."
    					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    				);
						return;
			    }else{
			    	clsDatabaseFunctions.commit_data_transaction(conn);
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067539]");
    				response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomerShipTos"
    					+ "?Status=Successfully deleted customer ship-to"
    						+ shipto.getM_sCustomerNumber() + " - " + shipto.getM_sShipToCode()
    					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    				);
					return;
			    }
		    }
	    }
	    
		String title = "";
		String subtitle = "";
		
		if(request.getParameter("SubmitAdd") != null){
			//Validate the customer number:
			ARCustomer cust = new ARCustomer(shipto.getM_sCustomerNumber());
			if(!cust.load(getServletContext(), sDBID)){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomerShipTos"
					+ "?" + ARCustomerShipTo.ParamsCustomerNumber + "=" + shipto.getM_sCustomerNumber()
					+ "&" + ARCustomerShipTo.ParamsShipToCode + "=" + shipto.getM_sShipToCode()
					+ "&Warning=Could not load customer " 
						+ shipto.getM_sCustomerNumber()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					);
						return;
			}
			shipto.setM_sShipToCode("");
			shipto.setM_bNewRecord("1");
		}
		//If we are coming here for the first time to edit (i.e., it's not a re-submit), then we need to 
		//load the input class from the database if possible:
		if(request.getParameter("SubmitEdit") != null){
			shipto.setM_sCustomerNumber(
					clsStringFunctions.StringLeft(
					clsManageRequestParameters.get_Request_Parameter("EditCode", request),
						SMTablearcustomershiptos.sCustomerNumberLength));
			shipto.setM_sShipToCode(
					clsStringFunctions.StringRight(
						clsManageRequestParameters.get_Request_Parameter("EditCode", request),
							SMTablearcustomershiptos.sShipToCodeLength));
			if(!shipto.load(getServletContext(), sDBID)){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomerShipTos"
					+ "?" + ARCustomerShipTo.ParamsCustomerNumber + "=" + shipto.getM_sCustomerNumber()
					+ "&" + ARCustomerShipTo.ParamsShipToCode + "=" + shipto.getM_sShipToCode()
					+ "&Warning=Could not load customer ship-to " 
						+ shipto.getM_sCustomerNumber() + " - " + shipto.getM_sShipToCode()
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
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.AREditCustomerShipToLocations) 
	    		+ "\">Summary</A><BR><BR>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");

		//If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
	    if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    
		//If there is a status from previous input, print it here:
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
	    if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>Status: " + sStatus + "</B><BR>");
		}

	    Edit_Record(shipto, out, sDBID, sUserName);
		
		out.println("</BODY></HTML>");
	}
	
	private void Edit_Record(
			ARCustomerShipTo shipto, 
			PrintWriter pwOut, 
			String sDBID,
			String sUser
			){
	    
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smar." + sCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ARCustomerShipTo.ParamsCustomerNumber + "\" VALUE=\"" + shipto.getM_sCustomerNumber() + "\">");
		if(shipto.getM_iNewRecord().compareToIgnoreCase("0") == 0){
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ARCustomerShipTo.ParamsShipToCode + "\" VALUE=\"" + shipto.getM_sShipToCode() + "\">");
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ARCustomerShipTo.ParamsAddingNewRecord + "\" VALUE=0>");
		}else{
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ARCustomerShipTo.ParamsAddingNewRecord + "\" VALUE=1>");
		}

		pwOut.println("<TABLE BORDER=12 CELLSPACING=2>");
        //Customer number:
    	pwOut.println("<TR><TD ALIGN=RIGHT><B>Customer number:</B></TD><TD>" + shipto.getM_sCustomerNumber() + "</TD><TD>&nbsp;</TD></TR>");
	    
        //Ship to code:
	    if(shipto.getM_iNewRecord().compareToIgnoreCase("1") == 0){
	        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		ARCustomerShipTo.ParamsShipToCode, 
	        		shipto.getM_sShipToCode().replace("\"", "&quot;"),  
	        		SMTablearcustomershiptos.sShipToCodeLength, 
	        		"Ship-to code:", 
	        		"Up to " + SMTablearcustomershiptos.sShipToCodeLength + " characters.",
	        		"1.6"
	        	)
	        );
	    	
	    }else{
	    	pwOut.println("<TR><TD ALIGN=RIGHT><B>Ship-to code:</B></TD><TD>" 
	    		+ shipto.getM_sShipToCode().replace("\"", "&quot;") + "</TD><TD>&nbsp;</TD></TR>");
	    }

        //Description:
        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
        		ARCustomerShipTo.ParamsDescription, 
        		shipto.getM_sDescription().replace("\"", "&quot;"), 
        		SMTablearcustomershiptos.sDescriptionLength, 
        		"Description:", 
        		"Up to " + SMTablearcustomershiptos.sAddressLine1Length + " characters.",
        		"3.2"
        		)
        );
		
        //Address Line 1:
        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
        		ARCustomerShipTo.ParamsAddressLine1, 
        		shipto.getM_sAddressLine1().replace("\"", "&quot;"), 
        		SMTablearcustomershiptos.sAddressLine1Length, 
        		"Address Line 1:", 
        		"First line of the address, up to " + SMTablearcustomershiptos.sAddressLine1Length + " characters.",
        		"3.2"
        		)
        );

        //Address Line 2:
        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
        		ARCustomerShipTo.ParamsAddressLine2, 
        		shipto.getM_sAddressLine2().replace("\"", "&quot;"), 
        		SMTablearcustomershiptos.sAddressLine2Length, 
        		"Address Line 2:", 
        		"Second line of the address, up to " + SMTablearcustomershiptos.sAddressLine2Length + " characters.",
        		"3.2"
        		)
        );
        		
        //Address Line 3:
        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
        		ARCustomerShipTo.ParamsAddressLine3, 
        		shipto.getM_sAddressLine3().replace("\"", "&quot;"), 
        		SMTablearcustomershiptos.sAddressLine3Length, 
        		"Address Line 3:", 
        		"Third line of the address, up to " + SMTablearcustomershiptos.sAddressLine3Length + " characters.",
        		"3.2"
        		)
        );

        //Address Line 4:
        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
        		ARCustomerShipTo.ParamsAddressLine4, 
        		shipto.getM_sAddressLine4().replace("\"", "&quot;"), 
        		SMTablearcustomershiptos.sAddressLine4Length, 
        		"Address Line 4:", 
        		"Fourth line of the address, up to " + SMTablearcustomershiptos.sAddressLine4Length + " characters.",
        		"3.2"
        		)
        );

        //City:
        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
        		ARCustomerShipTo.ParamsCity, 
        		shipto.getM_sCity().replace("\"", "&quot;"),  
        		SMTablearcustomershiptos.sCityLength, 
        		"City:", 
        		"Name of city, up to " + SMTablearcustomershiptos.sCityLength + " characters.",
        		"3.2"
        	)
		);

        //State:
        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
        		ARCustomerShipTo.ParamsState, 
        		shipto.getM_sState().replace("\"", "&quot;"), 
        		SMTablearcustomershiptos.sStateLength, 
        		"State Or Province:", 
        		"Up to " + SMTablearcustomershiptos.sStateLength + " characters.",
        		"3.2"
        	)
		);
        
        //Country:
        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
        		ARCustomerShipTo.ParamsCountry, 
        		shipto.getM_sCountry().replace("\"", "&quot;"), 
        		SMTablearcustomershiptos.sCountryLength, 
        		"Country:", 
        		"Up to " + SMTablearcustomershiptos.sCountryLength + " characters.",
        		"3.2"
        	)
		);

        //Postal code:
        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
        		ARCustomerShipTo.ParamsPostalCode, 
        		shipto.getM_sPostalCode().replace("\"", "&quot;"), 
        		SMTablearcustomershiptos.sPostalCodeLength, 
        		"Zip Or Postal Code:", 
        		"Up to " + SMTablearcustomershiptos.sPostalCodeLength + " characters.",
        		"3.2"
        	)
		);
        
        //Contact name:
        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
        		ARCustomerShipTo.ParamsContactName, 
        		shipto.getM_sContactName().replace("\"", "&quot;"),  
        		SMTablearcustomershiptos.sContactNameLength, 
        		"Contact name:", 
        		"Up to " + SMTablearcustomershiptos.sContactNameLength + " characters.",
        		"3.2"
        	)
		);
        
        //Phone:
        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
        		ARCustomerShipTo.ParamsPhoneNumber, 
        		shipto.getM_sPhoneNumber().replace("\"", "&quot;"), 
        		SMTablearcustomershiptos.sPhoneNumberLength, 
        		"Phone:", 
        		"Up to " + SMTablearcustomershiptos.sPhoneNumberLength + " characters.",
        		"3.2"
        	)
		);
        
        //Fax:
        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
        		ARCustomerShipTo.ParamsFaxNumber, 
        		shipto.getM_sFaxNumber().replace("\"", "&quot;"),  
        		SMTablearcustomershiptos.sFaxNumberLength, 
        		"Fax:", 
        		"Up to " + SMTablearcustomershiptos.sFaxNumberLength + " characters.",
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

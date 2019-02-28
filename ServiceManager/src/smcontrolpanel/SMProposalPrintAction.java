package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTableproposals;
import SMDataDefinition.SMTablesalesperson;
import SMDataDefinition.SMTablesmoptions;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsEmailInlineHTML;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMProposalPrintAction extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private boolean bDebugMode = false;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMEditProposals
			)
		){
			return;
		}
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " " 
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    
		SMUtilities.sysprint(
				this.toString(), 
				sUserName, 
				"[1551280488] - sDBID = '" 
					+ sDBID 
					+ "', req.parameters = " 
					+ ServletUtilities.clsManageRequestParameters.getAllRequestParameters(request)
					+ " - SESSION ATTRIBUTES: "
					+ ServletUtilities.clsServletUtilities.getSessionAttributes(CurrentSession)
			);

	    String  sRedirect = SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
			+ SMProposal.Paramstrimmedordernumber + "=" + clsManageRequestParameters.get_Request_Parameter(SMProposal.Paramstrimmedordernumber, request)
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&" + SMProposalPrintSelection.EMAIL_BUTTON_NAME + "=" + clsManageRequestParameters.get_Request_Parameter(SMProposalPrintSelection.EMAIL_BUTTON_NAME, request)
			+ "&" + SMProposalPrintSelection.EMAIL_ADDRESSES_PARAM + "=" + clsManageRequestParameters.get_Request_Parameter(SMProposalPrintSelection.EMAIL_ADDRESSES_PARAM, request)
			+ "&" + SMProposalPrintSelection.EMAIL_MESSAGE_PARAM + "=" + clsManageRequestParameters.get_Request_Parameter(SMProposalPrintSelection.EMAIL_MESSAGE_PARAM, request)
			+ "&" + SMProposalPrintSelection.EMAIL_TO_SELF_PARAM + "=" + clsManageRequestParameters.get_Request_Parameter(SMProposalPrintSelection.EMAIL_TO_SELF_PARAM, request)
			//+ "&" + SMProposalPrintSelection.INCLUDE_SIGNATURE_PARAM + "=" + SMUtilities.get_Request_Parameter(SMProposalPrintSelection.INCLUDE_SIGNATURE_PARAM, request)
			+ "&" + SMProposalPrintSelection.NUMBER_OF_PROPOSAL_COPIES + "=" + clsManageRequestParameters.get_Request_Parameter(SMProposalPrintSelection.NUMBER_OF_PROPOSAL_COPIES, request)
			+ "&" + SMProposalPrintSelection.PRINT_BUTTON_NAME + "=" + clsManageRequestParameters.get_Request_Parameter(SMProposalPrintSelection.PRINT_BUTTON_NAME, request)
			+ "&" + SMProposalPrintSelection.PRINT_LOGO_PARAM + "=" + clsManageRequestParameters.get_Request_Parameter(SMProposalPrintSelection.PRINT_LOGO_PARAM, request)
		;
	    
	    //First get the command value that was passed in:
	    String sCommandValue = clsManageRequestParameters.get_Request_Parameter(SMProposalPrintSelection.COMMAND_FLAG, request);

    	//If it's a request to email:
    	if (sCommandValue.compareToIgnoreCase(SMProposalPrintSelection.EMAILRECEIPTCOMMAND_VALUE) == 0){
    		//Go to email:
	    	try {
				emailProposal(request, sDBID, sUserName, sUserID, sUserFullName);
			} catch (Exception e) {
				sRedirect += "&Warning=Email failed: " + e.getMessage();
				response.sendRedirect(sRedirect);
				return;
			}
	    	sRedirect += "&Status=Email successfully sent.";
	    	response.sendRedirect(sRedirect);
	    	return;
		}
	    //If it's a request to print:
    	if (sCommandValue.compareToIgnoreCase(SMProposalPrintSelection.PRINTRECEIPTCOMMAND_VALUE) == 0){
    		if (bDebugMode){
    			System.out.println("[1547142813] In " + this.toString() + " into print mode");
    		}
    		try {
				printProposal(request, response, out, sDBID, sUserName);
			} catch (Exception e) {
				sRedirect += "&Warning=Print failed: " + e.getMessage();
				response.sendRedirect(sRedirect);
		    	return;
			}
    		return;
    	}
    	return;
	}
	private void emailProposal(
			HttpServletRequest req, 
			String sDBID, 
			String sUserName,
			String sUserID,
			String sUserFullName
			) throws Exception{

		String sProposalNumber = clsManageRequestParameters.get_Request_Parameter(SMTableproposals.strimmedordernumber, req);
		String sShipToName = "";
		String sEmailBody = " THIS IS A TEST";
		String sSMTPServer = "";
		//String sSMTPPort = "";
		//String sSMTPSourceServerName = "";
		String sSMTPUserName = "";
		String sSMTPPassword = "";
		String sSMTPReplyToAddress = "";
		String sSendingEmail = "";
		//boolean bUsesSMTPAuthentication = false;
		String sEmailAddresses = clsManageRequestParameters.get_Request_Parameter(SMProposalPrintSelection.EMAIL_ADDRESSES_PARAM, req).replace(";", ",");
		System.out.println("[1551280950] sEmailAddresses = " + sEmailAddresses);
		if (sEmailAddresses.trim().length() == 0){
			throw new Exception("You must enter at least one email address.");
		}
		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				this.toString() + ".emailProposal - user: " 
				+ sUserID
				+ " - "
				+ sUserFullName
				);
		
		if (conn == null){
			throw new Exception("Could not get data connection to read email info.");
		}
		String SQL = "SELECT " + SMTablesmoptions.TableName + ".*"
			//+ ", DATE_FORMAT(NOW(),'%c/%e/%Y %h:%i:%s %p')"
			//+ " AS CURRENTTIME"
			+ " FROM " 
			+ SMTablesmoptions.TableName;
		try {
			ResultSet rsOptions = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rsOptions.next()){
				sSMTPServer = rsOptions.getString(SMTablesmoptions.ssmtpserver).trim();
				//sSMTPPort = rsOptions.getString(SMTablesmoptions.ssmtpport).trim();
				//sSMTPSourceServerName = rsOptions.getString(SMTablesmoptions.ssmtpsourceservername).trim();
				sSMTPUserName = rsOptions.getString(SMTablesmoptions.ssmtpusername).trim();
				sSMTPPassword = rsOptions.getString(SMTablesmoptions.ssmtppassword).trim();
				sSMTPReplyToAddress = rsOptions.getString(SMTablesmoptions.ssmtpreplytoname);
				//bUsesSMTPAuthentication = (rsOptions.getInt(SMTablesmoptions.iusesauthentication) == 1);
				rsOptions.close();
			}else{
				rsOptions.close();
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080646]");
				throw new Exception("Could not get SM Options record with email info.");
			}
		} catch (Exception e1) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080647]");
			throw new Exception("Error reading SM Options record - " + e1.getMessage());
		}
		String sSystemRootPath = SMUtilities.getAbsoluteRootPath(req, getServletContext());
	
		//Get the salesperson's email address and ship to for the proposal:
		SQL = "SELECT"
			+ " " + SMTableusers.TableName + "." + SMTableusers.semail
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName
			+ " FROM " + SMTableorderheaders.TableName + " LEFT JOIN " + SMTableusers.TableName
			+ " ON " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson + " = "
			+ SMTableusers.TableName + "." + SMTableusers.sDefaultSalespersonCode
			+ " WHERE ("
				+ "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber + " = '" + sProposalNumber + "')"
			+ ")"
		;
		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		if (rs.next()){
			sSendingEmail = rs.getString(SMTableusers.TableName + "." + SMTableusers.semail).trim();
			sShipToName = rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName).trim();
		}else{
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080648]");
			throw new Exception("Could not read salesperson's email address.");
		}
		
		SQL = "SELECT"
				+ " " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sDirectDial
				+ ", " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonEmail
				+ ", " + SMTablesalesperson.TableName + "." + SMTablesalesperson.mSignature
				+ ", " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonFirstName
				+ ", " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonLastName
				+ ", " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonTitle
				+ " FROM " + SMTableorderheaders.TableName + " LEFT JOIN " + SMTablesalesperson.TableName
				+ " ON " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson + " = "
				+ SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode
				+ " WHERE ("
					+ "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber + " = '" + sProposalNumber + "')"
				+ ")"
			;
		//First get the signature in a variable:
		String sSignature = "";
		rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		if (rs.next()){
			sSignature = rs.getString(SMTablesalesperson.TableName + "." + SMTablesalesperson.mSignature);
			if (sSignature == null){
				throw new Exception("No salesperson found for this order.");
			}
		}else{
			throw new Exception("No salesperson data found for this salesperson.");
		}
		
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080649]");
		
		if (sSendingEmail.compareToIgnoreCase("") == 0){
			throw new Exception("The salesperson's email address is blank.");
		}
		
		sEmailBody = clsManageRequestParameters.get_Request_Parameter(SMProposalPrintSelection.EMAIL_MESSAGE_PARAM, req) 
			+ "\n\n" 
			+ getHTMLProposalForm(
				1, 
				sProposalNumber, 
				SMProposalForm.REQUEST_TYPE_EMAIL,
				sDBID,
				clsManageRequestParameters.get_Request_Parameter(SMProposalPrintSelection.PRINT_LOGO_PARAM, req).compareToIgnoreCase("") != 0,
				sUserName
			)
		;
		try{
			 clsEmailInlineHTML.emailEmbeddedHTMLWithSignature(
				sSystemRootPath,
				sSignature,
	        	Integer.parseInt(SMTableproposals.SIGNATURE_CANVAS_WIDTH),
	    		Integer.parseInt(SMTableproposals.SIGNATURE_CANVAS_HEIGHT),
	    		Integer.parseInt(SMTableproposals.SIGNATURE_PEN_WIDTH),
	    		SMTableproposals.SIGNATURE_PEN_R_COLOUR,
	    		SMTableproposals.SIGNATURE_PEN_G_COLOUR,
	    		SMTableproposals.SIGNATURE_PEN_B_COLOUR,
	    		sSMTPServer, 
	        	sSMTPUserName, 
	        	sSMTPPassword, 
	        	sEmailAddresses, 
	        	sSMTPReplyToAddress,
	  		    "Proposal No. " + sProposalNumber + " for " + sShipToName, 
	  		    sEmailBody, 
	        	getServletContext());
		} catch (Exception e1) {
			throw new Exception(e1.getMessage());
		}
		return;
		/*
		SMUtilities.sendEmail(sSMTPServer, 
		  sSMTPUserName, 
		  sSMTPPassword, 
		  sSMTPPort, 
		  "Proposal No. " + sProposalNumber
		  	+ " for " + sShipToName, //sSubject, 
		  sEmailBody, 
		  sSendingEmail,
		  sSMTPSourceServerName, 
		  sEmails, 
		  bUsesSMTPAuthentication,
		  true // use html?
		  );
		  */
	}
	private void printProposal(HttpServletRequest req,HttpServletResponse res, PrintWriter out, String sDBID, String sUserName) throws Exception{
	    String sNumberOfCopies = "";
	    String sProposalNumber = req.getParameter(SMProposal.Paramstrimmedordernumber);
	    //Get the number of copies:
	    sNumberOfCopies = req.getParameter(SMProposalPrintSelection.NUMBER_OF_PROPOSAL_COPIES);
	    int iNumberOfCopies = 0;
	    try {
			iNumberOfCopies = Integer.parseInt(sNumberOfCopies);
		} catch (NumberFormatException e) {
			throw new Exception("Invalid number of copies: " + sNumberOfCopies + ".");
		}
	    out.println(getHTMLProposalForm(
	    	iNumberOfCopies, 
	    	sProposalNumber, 
	    	SMProposalForm.REQUEST_TYPE_PRINT,
	    	sDBID,
	    	clsManageRequestParameters.get_Request_Parameter(SMProposalPrintSelection.PRINT_LOGO_PARAM, req).compareToIgnoreCase("") != 0,
	    	sUserName
	    	)
	    );
	}
	private String getHTMLProposalForm(
			int iNumberOfCopies, 
			String sProposalNumber, 
			int iRequestType,
			String sDBID,
			boolean bPrintLogo,
			String sUserName) throws Exception{
		String s = 
			"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"><HTML><HEAD>" 
		   		+ "<STYLE TYPE=\"text/css\">P.breakhere {page-break-before: always}\n"
		   		+ "H1.western { font-family: \"Arial\", sans-serif; font-size: 16pt; }\n"
		   		+ "H2.western { font-family: \"Arial\", sans-serif; font-size: 14pt; }\n"
		   		+ "H3.western { font-family: \"Arial\", sans-serif; font-size: 12pt; }\n"
		   		+ "H4.western { font-family: \"Arial\", sans-serif; font-size: 10pt; }\n"
		   		+ "@page { size:8.5in 11in; margin: 0.4in }\n"
		   		+ "</STYLE>"
		   		;
		//For printing signature:
		s += "<!--[if lt IE 9]><script src=\"scripts/flashcanvas.js\"></script><![endif]-->"
					+ "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js\"></script>";
		s += "</HEAD><BODY BGCOLOR=\"#FFFFFF\">"
		;
	 	//Retrieve information
	 	Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
					getServletContext(), 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".printProposal - user: " + sUserName
					);
		} catch (Exception e1) {
	 		s += "<FONT COLOR=RED>Unable to get data connection - " + e1.getMessage() + ".</FONT>"
		 			+ "</BODY></HTML>"
		 			;
		 		return s;
		}
	 	
	 	SMProposalForm form = new SMProposalForm();
	 	try {
			s += form.processReport(
				conn, 
				sProposalNumber, 
				sDBID,
				sUserName,
				iNumberOfCopies,
				iRequestType,
				getServletContext(),
				bPrintLogo
			);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080650]");
	 		s += "<FONT COLOR=RED>Error - " + e.getMessage() + ".</FONT>"
		 			+ "</BODY></HTML>"
		 			;
		 		return s;
		}
	 	
	 	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080651]");
	 	s += "</BODY></HTML>";
	 	return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}

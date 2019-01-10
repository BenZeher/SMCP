package smic;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMUtilities;

import java.sql.Connection;

public class ICConvertFromACCPACAction extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private String sWarning = "";
	private String sStatus = "";
	private String sCallingClass = "";
	private static String sDBID = "";
	private static String sUserID = "";
	private static String sUserFullName = "";
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		if(!SMAuthenticate.authenticateSMCPCredentials(
				request,	
				response,
				getServletContext(),
				-1L
			)
		){
			return;
		}
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    		+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    //sCallingClass will look like: smar.ARAgedTrialBalanceReport
	    sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    /**************Get Parameters**************/

	    /*
	    //TEST:
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
    			   "Transitional//EN\">" +
    		       "<HTML>" +
    		       "<HEAD><TITLE>" + "TEST1" + "</TITLE></HEAD>\n<BR>" + 
    			   "<BODY BGCOLOR=\"#FFFFFF\">" +
    			   "</BODY>" + 
    			   "TEST1" +
    			   "</HTML>"
    			   );
    	out.flush();
    	response.flushBuffer();
    	response.
    	//timer code here:
    	long lStartTime = System.currentTimeMillis();
    	while (System.currentTimeMillis() < lStartTime + 2000){
    		//just loop
    	}
    	
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
 			   "Transitional//EN\">" +
 		       "<HTML>" +
 		       "<HEAD><TITLE>" + "TEST2" + "</TITLE></HEAD>\n<BR>" + 
 			   "<BODY BGCOLOR=\"#FFFFFF\">" +
 			   "</BODY>" + 
			   "TEST2" +
 			   "</HTML>"
 			   );
    	response.flushBuffer();
    	return;
    	/*
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
 			   "Transitional//EN\">" +
 		       "<HTML>" +
 		       "<HEAD><TITLE>" + "TEST3" + "</TITLE></HEAD>\n<BR>" + 
 			   "<BODY BGCOLOR=\"#FFFFFF\">" +
 			   "</BODY>" + 
 			   "</HTML>"
 			   );

	    */
	    
    	//Customized title
    	String sTitle = "IC Convert ACCPAC Data";
    	
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sTitle + " - " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME) + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"#FFFFFF\">" +
		   "<TABLE BORDER=0 WIDTH=100%>" +
		   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>"
		   + clsDateAndTimeConversions.nowStdFormat()
		   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME).toString() + "</B></FONT></TD></TR>" +
		   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sTitle + "</B></FONT></TD></TR>"
		   );
		
    	boolean bConvertIC = false;
    	boolean bConvertPOHeaders = false;
    	boolean bConvertPOLines = false;
    	boolean bConvertPOReceipts = false;
    	boolean bConvertAll = false;
    	if ((request.getParameter("CONVERT_IC") != null)){
    		bConvertIC = true;
        	if (request.getParameter("ConfirmICConversion") == null){
        		sWarning = "You chose to convert IC from ACCPAC, but you did not check the 'Confirm' checkbox.";
        		response.sendRedirect(
        				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
        				+ "Warning=" + sWarning
        				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
        		);			
            	return;
        	}
    	}
    	if ((request.getParameter("CONVERT_POHEADER") != null)){
    		bConvertPOHeaders = true;
        	if (request.getParameter("ConfirmPOHeadConversion") == null){
        		sWarning = "You chose to convert PO Headers from ACCPAC, but you did not check the 'Confirm' checkbox.";
        		response.sendRedirect(
        				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
        				+ "Warning=" + sWarning
        				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
        		);			
            	return;
        	}
    	}

    	if ((request.getParameter("CONVERT_POLINE") != null)){
    		bConvertPOLines = true;
        	if (request.getParameter("ConfirmPOLineConversion") == null){
        		sWarning = "You chose to convert PO Lines from ACCPAC, but you did not check the 'Confirm' checkbox.";
        		response.sendRedirect(
        				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
        				+ "Warning=" + sWarning
        				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
        		);			
            	return;
        	}
    	}
    	
    	if ((request.getParameter("CONVERT_PORECEIPTS") != null)){
    		bConvertPOReceipts = true;
        	if (request.getParameter("ConfirmPOReceiptsConversion") == null){
        		sWarning = "You chose to convert PO Receipts from ACCPAC, but you did not check the 'Confirm' checkbox.";
        		response.sendRedirect(
        				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
        				+ "Warning=" + sWarning
        				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
        		);			
            	return;
        	}
    	}
    	
    	if ((request.getParameter("CONVERT_ALL") != null)){
    		bConvertAll = true;
        	if (request.getParameter("ConfirmConvertAll") == null){
        		sWarning = "You chose to convert ALL the IC data from ACCPAC, but you did not check the 'Confirm' checkbox.";
        		response.sendRedirect(
        				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
        				+ "Warning=" + sWarning
        				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
        		);			
            	return;
        	}
    	}

    	
    	Connection conn = clsDatabaseFunctions.getConnection(
    			getServletContext(), 
    			sDBID, 
    			"MySQL", 
    			"In " + this.toString() + ".doGet - user: " + sUserID
    			+ " - "
    			+ sUserFullName
    	);

    	if (conn == null){
    		sWarning = "Could not get connection to Service Manager data.";
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "Status=" + sStatus
    				+ "&Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID    		);			
        	return;    		
    	}
    	
    	Connection conACCPAC = clsDatabaseFunctions.getConnection(
    			getServletContext(), 
    			sDBID, 
    			"PERVASIVE", 
    			"In " + this.toString() + ".doGet - user: " + sUserID
    			+ " - " + sUserFullName
    	);
    	
    	if (conACCPAC == null){
    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080805]");
    		sWarning = "Could not get connection to Service Manager data.";
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "Status=" + sStatus
    				+ "&Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID    		
    		);			
        	return;    		
    	}

    	ICConvertACCPAC conv = new ICConvertACCPAC();
    	if (bConvertIC){
	    	if (!conv.convertICData(conn, conACCPAC, sUserID, out)){
	    		sWarning = "Error converting IC data: " + conv.getErrorMessage();
	    		sStatus = "";
	    	}else{
	    		sStatus = "Successfully converted IC data from ACCPAC<BR>Events completed:";
	    		for (int i = 0; i < conv.getStatusMessages().size(); i++){
	    			sStatus += conv.getStatusMessages().get(i) + "<BR>";
	    		}
	    		sWarning = "";
	    	}
    	}
    	if (bConvertPOHeaders){
	    	if (!conv.convertPOHeaderData(conn, conACCPAC, sUserID, out)){
	    		sWarning = "Error converting PO data: " + conv.getErrorMessage();
	    		sStatus = "";
	    	}else{
	    		sStatus = "Successfully converted PO headerdata from ACCPAC<BR>Events completed:";
	    		for (int i = 0; i < conv.getStatusMessages().size(); i++){
	    			sStatus += conv.getStatusMessages().get(i) + "<BR>";
	    		}
	    		sWarning = "";
	    	}
    	}
    	if (bConvertPOLines){
	    	if (!conv.convertPOLineData(conn, conACCPAC, sUserID, out)){
	    		sWarning = "Error converting PO line data: " + conv.getErrorMessage();
	    		sStatus = "";
	    	}else{
	    		sStatus = "Successfully converted PO line data from ACCPAC<BR>Events completed:";
	    		for (int i = 0; i < conv.getStatusMessages().size(); i++){
	    			sStatus += conv.getStatusMessages().get(i) + "<BR>";
	    		}
	    		sStatus = clsServletUtilities.URLEncode(sStatus);
	    		sWarning = "";
	    	}
    	}

    	if (bConvertPOReceipts){
	    	if (!conv.convertPOReceiptData(conn, conACCPAC,  sUserID, out)){
	    		sWarning = "Error converting PO receipt data: " + conv.getErrorMessage();
	    		sStatus = "";
	    	}else{
	    		sStatus = "Successfully converted PO receipt data from ACCPAC<BR>Events completed:";
	    		for (int i = 0; i < conv.getStatusMessages().size(); i++){
	    			sStatus += conv.getStatusMessages().get(i) + "<BR>";
	    		}
	    		sStatus = clsServletUtilities.URLEncode(sStatus);
	    		sWarning = "";
	    	}
    	}
    	
    	if (bConvertAll){
	    	if (!convertAll(conv, conn, conACCPAC,  sUserID, out)){
	    	}else{
	    		sStatus = "Successfully converted IC data from ACCPAC<BR>Events completed:";
	    		for (int i = 0; i < conv.getStatusMessages().size(); i++){
	    			sStatus += conv.getStatusMessages().get(i) + "<BR>";
	    		}
	    		sWarning = "";
	    	}
    	}
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080806]");
    	clsDatabaseFunctions.freeConnection(getServletContext(), conACCPAC, "[1547080807]");
		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Status=" + sStatus
				+ "&Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID    		
		);			
    	return;    		
	}
	private boolean convertAll(
			ICConvertACCPAC conv,
			Connection conn, 
			Connection conACCPAC, 
			String sUserID, 
			PrintWriter out
			){
		
		if (!conv.convertICData(conn, conACCPAC,  sUserID, out)){
    		sWarning = "Error converting IC data: " + conv.getErrorMessage();
    		sStatus = "";
    		return false;
    	}
		
		if (!conv.convertPOHeaderData(conn, conACCPAC, sUserID, out)){
    		sWarning = "Error converting po header data: " + conv.getErrorMessage();
    		sStatus = "";
    		return false;
    	}
		
		if (!conv.convertPOLineData(conn, conACCPAC, sUserID, out)){
    		sWarning = "Error converting PO line data: " + conv.getErrorMessage();
    		sStatus = "";
    		return false;
    	}

		if (!conv.convertPOReceiptData(conn, conACCPAC, sUserID, out)){
    		sWarning = "Error converting po receipt data: " + conv.getErrorMessage();
    		sStatus = "";
    		return false;
    	}

		return true;
	}
}

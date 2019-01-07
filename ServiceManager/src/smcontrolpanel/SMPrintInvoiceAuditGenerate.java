package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMLogEntry;
import SMDataDefinition.SMTableinvoiceheaders;
import SMDataDefinition.SMTableinvoicemgrcomments;
import SMDataDefinition.SMTableservicetypes;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class SMPrintInvoiceAuditGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");
	
	private String sWarning = "";
	private String sCallingClass = "";
	private String sDBID = "";
	private String sUserID = "";
	private String sUserFirstName = "";
	private String sUserLastName = "";
	private String sCompanyName = "";
	//private static SimpleDateFormat USTimeOnlyformatter = new SimpleDateFormat("hh:mm:ss a");
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMInvoiceAuditList))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFirstName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
	    sUserLastName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    
    	//Retrieve information
    	Connection conn = clsDatabaseFunctions.getConnection(
    		getServletContext(), 
    		sDBID, 
    		"MySQL", 
    		"smcontrolpanel.SMPrintInvoiceAuditGenerate - user: " 
    		+ sUserID
    		+ " - "
    		+ sUserFirstName
    		+ " "
    		+ sUserLastName
    	);
    	if (conn == null){
    		sWarning = "Unable to get data connection.";
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + clsServletUtilities.URLEncode(sWarning)
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
    	}
	    
    	//Get the user's full name here:
    	String sFullName = SMUtilities.getFullNamebyUserID(sUserID, conn);

	    //Get the last invoice edited, if there was one, and save an 'edited' comment, if there was one:
	    String sLastInvoiceEdited = "";
	    
	    /*
	    //DIAGNOSTIC:
		Enumeration<String> paramtestNames = request.getParameterNames();
	    
		while(paramtestNames.hasMoreElements()) {
			String stestParamName = paramtestNames.nextElement();
			System.out.println("stestParamName = " + stestParamName + ", value = '" + request.getParameter(stestParamName) + "'");
		}
		*/

	    //First, determine what command was issued: are we 1) saving all the comments, or 2) updating
	    //a comment, or 3) removing a comment?
	    Enumeration<String> paramSubmitButtonNames = request.getParameterNames();
	    String sSaveAllCommentsMarker = "SAVEALLCOMMENTS";
	    String sUpdateThisCommentMarker = "UPDATETHISCOMMENT";
	    String sDeleteThisCommentMarker = "REMOVETHISCOMMENT";

	    while(paramSubmitButtonNames.hasMoreElements()) {
		      String sParamName = paramSubmitButtonNames.nextElement();
		      //System.out.println("sParamName = '" + sParamName + "' = '" + request.getParameter(sParamName));
			  if (sParamName.contains(sSaveAllCommentsMarker)){
				  sLastInvoiceEdited = 
					  (sParamName.substring(sParamName.indexOf(sSaveAllCommentsMarker) 
							  + sSaveAllCommentsMarker.length()));
				  if (!saveAll(
						  	sUserID,
							sFullName,
							request,
							conn  
				  	)
				  ){
					  clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			    		response.sendRedirect(
								"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
								+ "Warning=" + clsServletUtilities.URLEncode(sWarning)
								+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				    		);			
				        	return;
				  }
				  break;
			  }
			  if (sParamName.contains(sUpdateThisCommentMarker)){
				  //System.out.println("contains Update Marker - sParamName = " + sParamName);
				  sLastInvoiceEdited = 
					  (sParamName.substring(sParamName.indexOf(sUpdateThisCommentMarker) 
							  + sUpdateThisCommentMarker.length()));
				  sLastInvoiceEdited = 
					  sLastInvoiceEdited.substring(0, sLastInvoiceEdited.indexOf("ID"));
				  //System.out.println("sLastInvoiceEdited = " + sLastInvoiceEdited);
				  //System.out.println("going into updateThisComment");
				  if (!updateThisComment(
						  sParamName,
						  sUpdateThisCommentMarker,
						  sUserID,
						  sFullName,
						  request,
						  conn  
				  	)
				  ){
					  clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			    		response.sendRedirect(
								"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
								+ "Warning=" + clsServletUtilities.URLEncode(sWarning)
								+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				    		);			
				        	return;
				  }
				  break;
			  }
			  if (sParamName.contains(sDeleteThisCommentMarker)){
				  sLastInvoiceEdited = 
					  (sParamName.substring(sParamName.indexOf(sDeleteThisCommentMarker) 
							  + sDeleteThisCommentMarker.length()));
				  sLastInvoiceEdited = 
					  sLastInvoiceEdited.substring(0, sLastInvoiceEdited.indexOf("ID"));
				  if (!deleteComment(
						  sParamName,
						  sDeleteThisCommentMarker,
						  sUserID,
						  sFullName,
						  request,
						  conn  
				  	)
				  ){
					  clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			    		response.sendRedirect(
								"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
								+ "Warning=" + clsServletUtilities.URLEncode(sWarning)
								+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				    		);			
				        	return;
				  }
				  break;
			  }
	    } //end while

	    String sStartingDate = "";
	    String sEndingDate = "";
	    
		sStartingDate = request.getParameter("StartingDate");
		sEndingDate = request.getParameter("EndingDate");
		
		try {
			sStartingDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sStartingDate),"yyyy-MM-dd");
		} catch (ParseException e) {
			sWarning = "Error:[1423581474] Invalid starting date '" + sStartingDate + "' - " + e.getMessage();
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "Warning=" +clsServletUtilities.URLEncode(sWarning)
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
        		);			
            	return;
		}
		try {
			sEndingDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sEndingDate),"yyyy-MM-dd");
		} catch (ParseException e) {
			sWarning = "Error:[1423581475] Invalid ending date '" + sEndingDate + "' - " + e.getMessage();
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "Warning=" + clsServletUtilities.URLEncode(sWarning)
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
        		);			
            	return;
		}
		
		//Invoice creation dates:
	    String sStartingCreationDate = "";
	    String sEndingCreationDate = "";
		
	    sStartingCreationDate = request.getParameter("StartingCreationDate");
		sEndingCreationDate = request.getParameter("EndingCreationDate");
		
		try {
			sStartingCreationDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sStartingCreationDate),"yyyy-MM-dd");
		} catch (ParseException e) {
			sWarning = "Error:[1423580600] Invalid starting creation date '" + sStartingCreationDate + "' - " + e.getMessage();
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "Warning=" + clsServletUtilities.URLEncode(sWarning)
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
        		);			
            	return;
		}
		try {
			sEndingCreationDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sEndingCreationDate),"yyyy-MM-dd");
		} catch (ParseException e) {
			sWarning = "Error:[1423580601] Invalid ending creation date '" + sEndingCreationDate + "' - " + e.getMessage();
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "Warning=" + clsServletUtilities.URLEncode(sWarning)
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
        		);			
            	return;
		}

    	//Get the list of selected order types:
    	ArrayList<String> sOrderTypes = new ArrayList<String>(0);
	    Enumeration<String> paramNames = request.getParameterNames();
	    String sParamName = "";
	    String sMarker = "SERVICETYPE";
	    while(paramNames.hasMoreElements()) {
	      sParamName = paramNames.nextElement();
		  if (sParamName.contains(sMarker)){
			  sOrderTypes.add(sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
		  }
	    }
	    Collections.sort(sOrderTypes);
		
	    if (sOrderTypes.size() == 0){
    		sWarning = "You must select at least one order type.";
    		clsDatabaseFunctions.freeConnection(getServletContext(), conn);
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + clsServletUtilities.URLEncode(sWarning)
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
	    }
		
    	//Get the list of selected sales groups:
    	ArrayList<String> sSalesGroups = new ArrayList<String>(0);
	    paramNames = request.getParameterNames();
	    sParamName = "";
	    sMarker = SMPrintInvoiceAuditSelection.SALESGROUP_PARAM;
	    while(paramNames.hasMoreElements()) {
	      sParamName = paramNames.nextElement();
		  if (sParamName.contains(sMarker)){
			  sSalesGroups.add(sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
		  }
	    }
	    Collections.sort(sSalesGroups);
		
	    if (sSalesGroups.size() == 0){
    		sWarning = "You must select at least one sales group.";
    		clsDatabaseFunctions.freeConnection(getServletContext(), conn);
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + clsServletUtilities.URLEncode(sWarning)
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
	    }
	    
	    //Determine whether this is a 're-processing' of this report, or if the user is running it
	    //for the first time:
	    boolean bReprocessing = false;
	    if (request.getParameter("Reprocess") != null){
	    	bReprocessing = true;
	    }
	    
    	//Customized title
    	String sReportTitle = "Invoice Audit List";

    	String sCriteria = "Starting with invoice date <B>" + sStartingDate + "</B>"
    		+ ", ending with invoice date <B>" + sEndingDate + "</B>"
    		+ ", starting with invoice creation date <B>" + sStartingCreationDate + "</B>"
    		+ ", ending with invoice creation date <B>" + sEndingCreationDate + "</B>"
    		+ ", including order types: ";
    	
    	for (int i = 0; i < sOrderTypes.size(); i++){
    		if (i == 0){
    			sCriteria += "<B>" + SMTableservicetypes.getServiceTypeLabel(sOrderTypes.get(i)) + "</B>";
    		}else{
    			sCriteria += ", <B>" + SMTableservicetypes.getServiceTypeLabel(sOrderTypes.get(i)) + "</B>";
    		}
    	}
   		sCriteria = sCriteria + ", including sales groups: ";
    	for (int i = 0; i < sSalesGroups.size(); i++){
    		if (i == 0){
    			sCriteria += "<B>" + sSalesGroups.get(i).substring(
   					0, sSalesGroups.get(i).indexOf(SMPrintInvoiceAuditSelection.SALESGROUP_PARAM_SEPARATOR)) + "</B>";
    		}else{
    			sCriteria += ", <B>" + sSalesGroups.get(i).substring(
       					0, sSalesGroups.get(i).indexOf(SMPrintInvoiceAuditSelection.SALESGROUP_PARAM_SEPARATOR)) + "</B>";
    		}
    	}
    	
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD>"
	       + "<TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE>"
	       
	       //Jump to last edit - must be between 'HEAD' tags:
	       + "<body onLoad=\"window.location='#LastEdit'\">"
	       
	       + "</HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"#FFFFFF\">" +
		   "<TABLE BORDER=0 WIDTH=100%>" +
		   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
		   + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) + " Printed by " + sFullName 
		   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>" +
		   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>" +
		   
		   "<TR><TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD></TR>");
				   
	   out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>" +
		   "</TD></TR></TABLE>");
	   
	    //log usage of this report
	   if (!bReprocessing){
		   SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
		   log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_PRINTINVOICEAUDIT, "REPORT", "SMPrintInvoiceAuditReport", "[1376509336]");
	   }

	   boolean bAllowItemViewing = 
	   SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.ICDisplayItemInformation, 
											 sUserID, 
											 getServletContext(),
											 sDBID,
											 (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
											 );
	   String sURLLinkBase = SMUtilities.getURLLinkBase(getServletContext());
	   
    	SMInvoiceAuditReport iar = new SMInvoiceAuditReport();
    	
    	if (!iar.processReport(
    			conn, 
    			sStartingDate, 
    			sEndingDate,
    			sStartingCreationDate, 
    			sEndingCreationDate,
    			sOrderTypes,
    			sSalesGroups,
    			sDBID,
    			sUserID,
    			true,
    			sLastInvoiceEdited,
    			bReprocessing,
    			bAllowItemViewing,
    			sURLLinkBase,
    			out,
    			getServletContext(),
    			(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
    		out.println("Could not print report - " + iar.getErrorMessage());
    	}
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn);
    	
    	out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
    			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    			+ "\">Return to user login</A>");
    	    	
    	out.println("<BR><A HREF=#> Back to Top</A>");

    	out.println("</BODY></HTML>");
	}
	private boolean deleteComment (
			String sDeleteParameterName,
			String sDeleteCommentMarker,
			String sUsersID,
			String sUserFullName,
			HttpServletRequest req,
			Connection con
			){
			
		//The edited text box will have a name like:
		//"COMMENT" + sInvNumber + "ID" + SMTableinvoicemgrcomments.id
		String sEditBoxName = "COMMENT" 
			+ sDeleteParameterName.substring(sDeleteParameterName.indexOf(sDeleteCommentMarker) 
					+ sDeleteCommentMarker.length());

		//sEditBoxName looks like: "COMMENT  604464ID162"
		
		//Get the ID:
		//System.out.println("sEditBoxName = '" + sEditBoxName + "'");
		String sID = sEditBoxName.substring(sEditBoxName.indexOf("ID") + "ID".length(),
				sEditBoxName.length());
		//System.out.println("sID = '" + sID + "'");
		
		String SQL = "";
		SQL = "DELETE FROM " + SMTableinvoicemgrcomments.TableName
			+ " WHERE("
				+ SMTableinvoicemgrcomments.id + " = " + sID
			+ ")"
			;
		try{
			clsDatabaseFunctions.executeSQL(SQL, con);
		}catch (SQLException e){
			sWarning = "Error deleting comment " + sEditBoxName + " - " + e.getMessage();
			return false;
		}
		return true;
	}
	
	private boolean saveComment (
			String sInvoiceAndID,
			String sComment,
			String sUsersID,
			String sUserFullName,
			Connection con
			){
	
		if (sComment.trim().compareToIgnoreCase("") == 0){
			return true;
		}
		
		String sInvoice = sInvoiceAndID.substring(0, sInvoiceAndID.indexOf("ID"));
		String sID = sInvoiceAndID.substring(sInvoiceAndID.indexOf("ID") + "ID".length(), sInvoiceAndID.length());
		String SQL = "";
		if (Long.parseLong(sID) == 0){
			//It's a new comment, do an insert
			SQL = "INSERT INTO " + SMTableinvoicemgrcomments.TableName
				+ "("
				+ SMTableinvoicemgrcomments.datlastedited
				+ ", " + SMTableinvoicemgrcomments.mcomment
				+ ", " + SMTableinvoicemgrcomments.sinvoicenumber
				+ ", " + SMTableinvoicemgrcomments.luserid
				+ ", " + SMTableinvoicemgrcomments.suserfullname
				
				+ ") VALUES ("
				+ "NOW()"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sComment) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sInvoice) + "'"
				+ ", " + clsDatabaseFunctions.FormatSQLStatement(sUsersID) 
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
				+ ")"
				;
		}else{
			//It's an existing comment, update it:
			SQL = "UPDATE " + SMTableinvoicemgrcomments.TableName
			+ " SET "
			+ SMTableinvoicemgrcomments.datlastedited
			+ " = NOW()"
			+ ", " + SMTableinvoicemgrcomments.mcomment
			+ " = '" + clsDatabaseFunctions.FormatSQLStatement(sComment) + "'"
			+ " WHERE ("
				+ SMTableinvoicemgrcomments.id + " = " + sID
			+ ")"
			;
		}

		try{
			clsDatabaseFunctions.executeSQL(SQL, con);
		}catch (SQLException e){
			sWarning += "Error saving comment for invoice " + sInvoice + " - " + e.getMessage();
			//System.out.println("Error saving comment for invoice " + sInvoice + " - " + e.getMessage());
			return false;
		}
		
		return true;
	}
	private boolean saveAll(
			String sUsersID,
			String sUserFullName,
			HttpServletRequest req,
			Connection con		
	){
		
	    //Process any comments from SMInvoiceAuditReport if it is the calling class:
	    //COMMENT" + sInvNumber + "ID0"
	    Enumeration<String> parameterNames = req.getParameterNames();
	    String sCommentMarker = "COMMENT";
	    String sStateMarker = "STATE";
	    while(parameterNames.hasMoreElements()) {
	    	String sParamName = parameterNames.nextElement();
	    	//System.out.println("In " + this.toString() + "sParamName = " + sParamName
	    	//		+ ", request.getParameter(sParamName) = " + request.getParameter(sParamName)
	    	//);
	    	if (sParamName.contains(sCommentMarker)){
	    		//If there's a valid comment:
	    		if (
	    				(req.getParameter(sParamName) != null)
	    				&& (req.getParameter(sParamName).trim().compareToIgnoreCase("") != 0)
	    				//AND ONLY if it's a new comment, indicated by having no ID 
	    				//(We'll save edited comments next):
	    				&& (sParamName.contains("ID0"))
	    		){
	    			if (!saveComment (
	    					sParamName.substring(sParamName.indexOf(sCommentMarker) + sCommentMarker.length()),
	    					req.getParameter(sParamName),
	    					sUsersID,
	    					sUserFullName,
	    					con)){
	    				sWarning += " Error saving comment " + sParamName;
	    				return false;
	    			}
	    		}
	    	}
	    	if (sParamName.contains(sStateMarker)){	
	    		if((req.getParameter(sParamName) != null) 
	    			&& (req.getParameter(sParamName).trim().compareToIgnoreCase("") != 0)
	    			&& (sParamName.contains("STATE"))){
	  
	    			String sPreviouseSate = sParamName.substring(sParamName.indexOf(sStateMarker) + sStateMarker.length(),
	    					sParamName.indexOf(sStateMarker) + sStateMarker.length() + 1 );
	    			String sCurrentSate = req.getParameter(sParamName);
	    			
	    			if(sPreviouseSate.compareToIgnoreCase(sCurrentSate) != 0){
	    				if (!saveInvoicingState(
	    					sParamName.substring(sParamName.indexOf("INV") + "INV".length()).trim(),
	    					req.getParameter(sParamName),
	    					sUsersID,
	    					sUserFullName,
	    					con
	    					)){
	    				sWarning = "Error saving invoicing state on invoice " + sParamName.substring(sParamName.indexOf(sStateMarker) + sStateMarker.length()).trim();
	    				return false;
	    				}
	    			}
	    		}	
	    	}
	    }

		
		return true;
	}

	private boolean saveInvoicingState(
			String sInvoiceNumber, 
			String sInvoicingState,
			String sUsersID, 
			String sUserFullName, 
			Connection con) {
		
		String SQL = "UPDATE " + SMTableinvoiceheaders.TableName
				+ " SET "
				+ SMTableinvoiceheaders.iinvoicingstate + " =" + sInvoicingState 
				+ " WHERE ("
					+ "TRIM(" + SMTableinvoiceheaders.sInvoiceNumber + ") = '" + sInvoiceNumber + "'"
				+ ")"
				;
		
		try{
			clsDatabaseFunctions.executeSQL(SQL, con);
		}catch (SQLException e){
			sWarning = "Error saving invoicing state for invoice " + sInvoiceNumber + " - " + e.getMessage();
			System.out.println("Error saving invoicing state for invoice " + sInvoiceNumber + " - " + e.getMessage());
			return false;
		}
		
		return true;
	}
	//updateThisComment
	private boolean updateThisComment(
			String sUpdateParameterName,
			String sUpdateCommentMarker,
			String sUsersID,
			String sUserFullName,
			HttpServletRequest req,
			Connection con		
	){
		
		//System.out.println("sUpdateParameterName = " + sUpdateParameterName);
		//System.out.println("sUpdateCommentMarker = " + sUpdateCommentMarker);
		
		//"sUpdateParameterName" is the name of the SUBMIT button, so we have to pick off
		//the invoicenumber and get the COMMENT parameter's value to save as the actual 
		//comment:
		
		//Need to find and read the text box with this ID:
		Enumeration<String> paramUpdateNames = req.getParameterNames();
		    
		//The edited text box will have a name like:
		//"COMMENT" + sInvNumber + "ID" + SMTableinvoicemgrcomments.id
		// or "COMMENT  604464ID167"
		String sCommentMarker = "COMMENT";
		String sEditBoxName = sCommentMarker 
			+ sUpdateParameterName.substring(sUpdateParameterName.indexOf(sUpdateCommentMarker) 
			+ sUpdateCommentMarker.length());

		while(paramUpdateNames.hasMoreElements()) {
			String sUpdateParamName = paramUpdateNames.nextElement();
			//System.out.println("sUpdateParamName = " + sUpdateParamName + ", value = '" + req.getParameter(sUpdateParamName) + "'");
			if (sUpdateParamName.compareToIgnoreCase(sEditBoxName) == 0){
				if (!saveComment(
						sUpdateParamName.substring(sUpdateParamName.indexOf(sCommentMarker) 
							+ sCommentMarker.length()),
						req.getParameter(sUpdateParamName),
						sUsersID,
						sUserFullName,
						con
					)
				){
					
					return false;
				}
				break;
			}
		}
		return true;
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

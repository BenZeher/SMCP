package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
//import java.math.BigDecimal;
import java.sql.ResultSet;
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
import SMDataDefinition.SMTableordermgrcomments;
import SMDataDefinition.SMTableservicetypes;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class SMPrintPreInvoiceGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");
	
	private String sWarning = "";
	private String sCallingClass = "";
	private String sDBID = "";
	private String sUserFirstName = "";
	private String sUserLastName = "";
	//private String sUserName = "";
	private String sUserID = "";
	private String sCompanyName = "";
	private boolean bDebugMode = false;
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
				SMSystemFunctions.SMPreInvoice))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserFirstName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
	    sUserLastName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    //sUserName = (String) CurrentSession.getAttribute(SMUtilities.SESSION_PARAM_USERNAME);
	    sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    
    	//Retrieve information
    	Connection conn = clsDatabaseFunctions.getConnection(
    		getServletContext(), 
    		sDBID, 
    		"MySQL", 
    		"smcontrolpanel.SMPrintPreInvoiceGenerate - user: " 
    		+ sUserID
    		+ " - "
    		+ sUserFirstName
    		+ " "
    		+ sUserLastName
    	);
    	if (conn == null){
    		sWarning += "Unable to get data connection.";
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + clsServletUtilities.URLEncode(sWarning)
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
    	}    	
	    
    	//Get the user's full name here:
    	String sFullName = "";
		try{
			String SQL = "SELECT " + SMTableusers.sUserFirstName + ", " + SMTableusers.sUserLastName
				+ " FROM " + SMTableusers.TableName
				+ " WHERE ("
					+ SMTableusers.lid + " = " + sUserID + ""
				+ ")"
				;
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				sFullName = rs.getString(SMTableusers.sUserFirstName)
					+ " " + rs.getString(SMTableusers.sUserLastName);
			}
			rs.close();
		}catch (SQLException e){
			System.out.println("In " + this.toString() 
				+ ".doGet - error getting full name of user " + e.getMessage());
		}

	    //Get the last order edited, if there was one, and save an 'edited' comment, if there was one:
	    String sLastOrderEdited = "";
	    
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
			  if (
					  (sParamName.contains(sSaveAllCommentsMarker))
					  && (request.getParameter(sParamName) != null)
					  
				){
				  //System.out.println("[1379706741]: save using regular button");
				  sLastOrderEdited = 
					  (sParamName.substring(sParamName.indexOf(sSaveAllCommentsMarker) 
							  + sSaveAllCommentsMarker.length()));
				  if (!saveAllComments(
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
			  //TODO: LTO 20130920 save using button with JS
			  if (
					  (sParamName.contains("JSSAVECOMMENT"))
					  && (request.getParameter(sParamName).compareTo("na") != 0)
					  
				){
				  //System.out.println("[1379706741]: save using button with JS");
				  sLastOrderEdited = request.getParameter("JSLASTEDITEDORDERNUMBER");
				  if (!saveAllComments(
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
			  
			  if (
					  (sParamName.contains(sUpdateThisCommentMarker))
					  && (request.getParameter(sParamName) != null 
					  && (request.getParameter("JSLASTEDITEDORDERNUMBER").compareTo("0") == 0))
					  
			  ){
				  //System.out.println("contains Update Marker - sParamName = " + sParamName);
				  //System.out.println("[1379709692]: update using regular button");
				  sLastOrderEdited = 
					  (sParamName.substring(sParamName.indexOf(sUpdateThisCommentMarker) 
							  + sUpdateThisCommentMarker.length()));
				  sLastOrderEdited = 
					  sLastOrderEdited.substring(0, sLastOrderEdited.indexOf("ID"));
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

			  if (
					  (sParamName.contains(sDeleteThisCommentMarker))
					  && (request.getParameter(sParamName) != null 
					  && (request.getParameter("JSLASTEDITEDORDERNUMBER").compareTo("0") == 0))
			  ){
				  //System.out.println("[1379709659]: delete using regular button");
				  sLastOrderEdited = 
					  (sParamName.substring(sParamName.indexOf(sDeleteThisCommentMarker) 
							  + sDeleteThisCommentMarker.length()));
				  sLastOrderEdited = 
					  sLastOrderEdited.substring(0, sLastOrderEdited.indexOf("ID"));
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
			sWarning += "Error:[1423580794] Invalid starting date '" + sStartingDate + "' - " + e.getMessage();
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "Warning=" + clsServletUtilities.URLEncode(sWarning)
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
        		);			
            	return;
		}
		try {
			sEndingDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sEndingDate),"yyyy-MM-dd");
		} catch (ParseException e) {
			sWarning += "Error:[1423580795] Invalid ending date '" + sEndingDate + "' - " + e.getMessage();
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
    		sWarning += "You must select at least one order type.";
    		clsDatabaseFunctions.freeConnection(getServletContext(), conn);
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + clsServletUtilities.URLEncode(sWarning)
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
	    }
		
    	//Get the list of selected locations:
    	ArrayList<String> sLocations = new ArrayList<String>(0);
	    Enumeration<String> paramLocationNames = request.getParameterNames();
	    String sParamLocationName = "";
	    String sLocationMarker = "LOCATION";
	    while(paramLocationNames.hasMoreElements()) {
	    	sParamLocationName = paramLocationNames.nextElement();
		  if (sParamLocationName.contains(sLocationMarker)){
			  sLocations.add(sParamLocationName.substring(
					  sParamLocationName.indexOf(sLocationMarker) + sLocationMarker.length()));
		  }
	    }
	    Collections.sort(sLocations);
		
	    if (sLocations.size() == 0){
    		sWarning += "You must select at least one location.";
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
    	String sReportTitle = "Pre-Invoice Report";

    	String sCriteria = "Starting with order posting date <B>" + sStartingDate + "</B>"
    		+ ", ending with order posting date <B>" + sEndingDate + "</B>"
    		+ ", including order types: ";
    	
    	for (int i = 0; i < sOrderTypes.size(); i++){
    		if (i == 0){
    			sCriteria += "<B>" + SMTableservicetypes.getServiceTypeLabel(sOrderTypes.get(i)) + "</B>";
    		}else{
    			sCriteria += ", <B>" + SMTableservicetypes.getServiceTypeLabel(sOrderTypes.get(i)) + "</B>";
    		}
    	}
   		sCriteria = sCriteria + ", for locations: ";
    	for (int i = 0; i < sLocations.size(); i++){
    		if (i == 0){
    			sCriteria += "<B>" + sLocations.get(i) + "</B>.";
    		}else{
    			sCriteria += ", <B>" + sLocations.get(i) + "</B>.";
    		}
    	}
    	String sHeading = 
    			"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
    			"Transitional//EN\">" +
    			"<HTML>" +
    			"<HEAD>"
    			+ "<TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE>"
    			;
    	if (sLastOrderEdited.compareToIgnoreCase("") != 0){
    		//Jump to last edit - must be between 'HEAD' tags:
    		sHeading += "<body onLoad=\"window.location='#LastEdit'\">";
    	}
    	sHeading += 
    			"</HEAD>\n<BR>" + 
    			"<BODY BGCOLOR=\"#FFFFFF\">" +
    			"<TABLE BORDER=0 WIDTH=100%>" +
    			"<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
    			+ USDateformatter.format((new Timestamp(System.currentTimeMillis()))) + " Printed by " + sFullName 
    			+ "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>" +
    			"<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>" +
    			"<TR><TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD></TR>"
    			+ "<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
    			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>" +
				"</TD></TR></TABLE>";
	   out.println(sHeading);
	    //log usage of this report
	   if (!bReprocessing){
		   SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
		   log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_PRINTPREINVOICE, "REPORT", "SMPrintPreInvoiceList", "[1376509338]");
	   }

	   if (bDebugMode){
		   System.out.println(this.toString() + " .get connection to Pervasive database - sDBID = " + sDBID);
	   }
	   Connection ICconn = clsDatabaseFunctions.getConnection(    		
			   getServletContext(), 
			   sDBID, 
			   "MySQL", 
			   "smcontrolpanel.SMPrintPreInvoiceGenerate - user: " + sUserID
	   );

	   if (ICconn == null){
		   clsDatabaseFunctions.freeConnection(getServletContext(), conn);
		   sWarning += "Could not open connection to IC database.";
		   response.sendRedirect(
				   "" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				   + "Warning=" + clsServletUtilities.URLEncode(sWarning)
				   + "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		   );
	   }

	   boolean bAllowItemViewing = 
	   SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.ICDisplayItemInformation, 
			   								 sUserID, 
											 getServletContext(),
											 sDBID,
											 (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
											 );
	   boolean bAllowOrderEditing = 
		   SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMEditOrders, 
				   								sUserID, 
												 getServletContext(),
												 sDBID,
												 (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
												 );
	   String sURLLinkBase = SMUtilities.getURLLinkBase(getServletContext());
	   clsDatabaseFunctions.freeConnection(getServletContext(), ICconn);
    	SMPreInvoiceReport pir = new SMPreInvoiceReport();
		long lStartingTime = System.currentTimeMillis();
    	if (!pir.processReport(
    			conn, 
    			sStartingDate, 
    			sEndingDate,
    			sOrderTypes,
    			sLocations,
    			sDBID,
    			sUserID,
    			true,
    			sLastOrderEdited.trim(),
    			bReprocessing,
    			bAllowItemViewing,
    			bAllowOrderEditing,
    			sURLLinkBase,
    			out,
    			getServletContext(),
    			(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
    		out.println("Could not print report - " + pir.getErrorMessage());
    	}
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn);
    	long lEndingTime = System.currentTimeMillis();
    	
    	out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
		+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
		+ "\">Return to user login</A>");
    	
    	out.println("<BR><A HREF=#> Back to Top</A>");
    	
		out.println("<BR>Processing took " + (lEndingTime - lStartingTime)/1000L + " seconds (" + Long.toString(lEndingTime - lStartingTime) + "ms) on database server.");
		
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
		SQL = "DELETE FROM " + SMTableordermgrcomments.TableName
			+ " WHERE("
				+ SMTableordermgrcomments.id + " = " + sID
			+ ")"
			;
		SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
		if (bDebugMode){
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_OTHER, "ORDERMGRCOMMENTS", "Preinvoice - " + SQL, "[1376509337]");
		}
		try{
			clsDatabaseFunctions.executeSQL(SQL, con);
		}catch (SQLException e){
			sWarning += "Error deleting comment " + sEditBoxName + " - " + e.getMessage();
			return false;
		}
		return true;
	}
	
	private boolean saveComment (
			String sOrderAndID,
			String sComment,
			String sUsersID,
			String sUserFullName,
			Connection con
			){
	
		if (sComment.trim().compareToIgnoreCase("") == 0){
			return true;
		}
		
		String sOrder = sOrderAndID.substring(0, sOrderAndID.indexOf("ID"));
		String sID = sOrderAndID.substring(sOrderAndID.indexOf("ID") + "ID".length(), sOrderAndID.length());
		String SQL = "";
		if (Long.parseLong(sID) == 0){
			//It's a new comment, do an insert
			SQL = "INSERT INTO " + SMTableordermgrcomments.TableName
				+ "("
				+ SMTableordermgrcomments.datlastedited
				+ ", " + SMTableordermgrcomments.mcomment
				+ ", " + SMTableordermgrcomments.sordernumber
				+ ", " + SMTableordermgrcomments.luserid
				+ ", " + SMTableordermgrcomments.suserfullname
				
				+ ") VALUES ("
				+ "NOW()"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sComment) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sOrder.trim()) + "'"
				+ ", " + clsDatabaseFunctions.FormatSQLStatement(sUsersID) + ""
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
				+ ")"
				;
			//System.out.println("Order manager comments SQL (" + USDateTimeformatter.format(new Date(System.currentTimeMillis())) + " by " + sUserName + "):");
			//System.out.println(SQL);
		}else{
			//It's an existing comment, update it:
			SQL = "UPDATE " + SMTableordermgrcomments.TableName
			+ " SET "
			+ SMTableordermgrcomments.datlastedited
			+ " = NOW()"
			+ ", " + SMTableordermgrcomments.mcomment
			+ " = '" + clsDatabaseFunctions.FormatSQLStatement(sComment) + "'"
			+ " WHERE ("
				+ SMTableordermgrcomments.id + " = " + sID
			+ ")"
			;
			//System.out.println("Order manager comments SQL (" + USDateTimeformatter.format(new Date(System.currentTimeMillis())) + " by " + sUserName + "):");
			//System.out.println(SQL);
		}
		SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
		if (bDebugMode){		
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_OTHER, "ORDERMGRCOMMENTS", "Preinvoice - " + SQL, "[1376509339]");
		}
		try{
			clsDatabaseFunctions.executeSQL(SQL, con);
		}catch (SQLException e){
			sWarning += "Error saving comment for order " + sOrder + " - " + e.getMessage();
		   log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_OTHER, "ORDERMGRCOMMENTS", "Preinvoice - Error saving comment for order " + sOrder + " - " + e.getMessage(), "[1376509340]");
			return false;
		}
		
		return true;
	}
	private boolean saveAllComments(
			String sUsersID,
			String sUserFullName,
			HttpServletRequest req,
			Connection con		
	){
		
	    //Process any comments from SMInvoiceAuditReport if it is the calling class:
	    //COMMENT" + sInvNumber + "ID0"
	    Enumeration<String> parameterNames = req.getParameterNames();
	    String sCommentMarker = "COMMENT";
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
	    				sWarning += "Error saving comment " + sParamName;
	    				return false;
	    			}
	    		}
	    	}
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
		//"COMMENT" + sOrdNumber + "ID" + SMTableordermgrcomments.id
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

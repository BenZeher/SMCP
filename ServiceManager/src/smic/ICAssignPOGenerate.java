package smic;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMTableicpoheaders;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class ICAssignPOGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private String sICAssignPOGenerateWarning = "";
	private String sICAssignPOGenerateAssignedDate = "";
	private String sICAssignPOGenerateAssignedNumber = "";
	//private static SimpleDateFormat USTimeOnlyformatter = new SimpleDateFormat("hh:mm:ss a");
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.ICAssignPO)){
			return;
		}
	    response.setContentType("text/html");
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    			  + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);  
	    
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    String sAssignedDate = "";
	    String sPOComment = "";
		sPOComment = clsManageRequestParameters.get_Request_Parameter(ICPOHeader.Paramscomment, request).trim();
		if(sPOComment.compareToIgnoreCase("") == 0){
			sICAssignPOGenerateWarning = "PO Comments cannot be blank.";
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + clsServletUtilities.URLEncode(sICAssignPOGenerateWarning)
				+ "&" + ICPOHeader.Paramscomment + "=" + clsServletUtilities.URLEncode(sPOComment)
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
		}
		
    	//Retrieve information
    	Connection conn = clsDatabaseFunctions.getConnection(
    			getServletContext(), 
    			sDBID, 
    			"MySQL", 
    			SMUtilities.getFullClassName(this.toString()) 
    			+ " - userID: " 
    			+ sUserID
    			+ " - "
    			+ sUserFullName
    	);
    	if (conn == null){
    		sICAssignPOGenerateWarning = "Unable to get data connection.";
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "Warning=" + clsServletUtilities.URLEncode(sICAssignPOGenerateWarning)
    				+ "&" + ICPOHeader.Paramscomment + "=" + clsServletUtilities.URLEncode(sPOComment)
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
        		);			
            	return;    	
        }

		SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(conn);
		
		//If we determine that this is a duplicate request, don't display anything on the screen, just
		//return:
		if (checkForDuplicateRequest(sUserID, sUserFullName, sPOComment, conn, log)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080769]");
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "Warning=" + clsServletUtilities.URLEncode(sICAssignPOGenerateWarning)
    				+ "&" + ICPOHeader.Paramscomment + "=" + clsServletUtilities.URLEncode(sPOComment)
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
        		);
			return;
		}
		
		if (!insertPOHeader(sUserFullName, sUserID, sPOComment, conn)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080770]");
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "Warning=" + clsServletUtilities.URLEncode(sICAssignPOGenerateWarning)
    				+ "&" + ICPOHeader.Paramscomment + "=" + clsServletUtilities.URLEncode(sPOComment)
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
        		);			
            	return;    	
		}
    	
	    //Log the usage:
		log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_POASSIGNMENT, "POASSIGNMENT", "PO Number " + sICAssignPOGenerateAssignedNumber 
				+ " was assigned to " + sUserFullName + " on " + sAssignedDate,
				"[1376509373]"
		);
		
		//Determine if this user has rights to edit a PO:
		boolean bAllowPOEditing = 
			SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.ICEditPurchaseOrders, 
					sUserID, 
					conn,
					(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
			);
		String sAssignedPOLink = sICAssignPOGenerateAssignedNumber;
		if (bAllowPOEditing){
			sAssignedPOLink = 
				"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPOEdit"
				+ "?" + ICPOHeader.Paramlid + "=" + sICAssignPOGenerateAssignedNumber
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + sICAssignPOGenerateAssignedNumber + "</A>";
		}
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080771]");
		String sStatus = "PO Number " + sAssignedPOLink + " was assigned to " 
			+ sUserFullName + " on " + sAssignedDate + ".";
		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Status=" + clsServletUtilities.URLEncode(sStatus)
				+ "&" + ICPOHeader.Paramscomment + "=" + clsServletUtilities.URLEncode(sPOComment)
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        return;    	
	}
	private boolean checkForDuplicateRequest(
			String sUserID, 
			String sFullName, 
			String sPOComment, 
			Connection conn,
			SMLogEntry log){
		//First check to make sure this exact request wasn't recently completed:
		String SQL = "SELECT"
			+ " " + SMTableicpoheaders.datassigned
			+ ", NOW() AS CURRENTDATE"
			+ " FROM " + SMTableicpoheaders.TableName
			+ " WHERE ("
				+ "(" + SMTableicpoheaders.sassignedtofullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(sFullName) + "')"
				+ " AND (" + SMTableicpoheaders.scomment + " = '" + sPOComment + "')"
			+ ")"
			+ " ORDER BY " + SMTableicpoheaders.lid + " DESC"
		;
		Timestamp tcAssigned = new Timestamp(0);
		Timestamp tcNow = new Timestamp(0);
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				tcAssigned = rs.getTimestamp(SMTableicpoheaders.datassigned);
				tcNow = rs.getTimestamp("CURRENTDATE");
			}
			rs.close();
		} catch (SQLException e1) { 
			//System.out.println("ERROR checking for dupes with SQL:" + SQL + " - " + e1.getMessage());
			sICAssignPOGenerateWarning = "Could not verify PO information - " + e1.getMessage();
			//In this case, we just assume it's not a duplicate and let it go on.
			return false;
		}

		//If tcNow is still zero, there was no similar record and we can just return false:
		if (tcNow.getTime() == 0L){
			return false;
		}
		//System.out.println("now = " + tcNow.getTime() + ", assigned time = " + tcAssigned.getTime());
		long lTimeDifferenceInMinutes = tcNow.getTime() - tcAssigned.getTime();
		//System.out.println("lTimeDifferenceInMinutes = " + lTimeDifferenceInMinutes);
		
		// Calculate difference in minutes
		long diffMinutes = lTimeDifferenceInMinutes / (60 * 1000);
		//System.out.println("diffMinutes = " + diffMinutes);
		if (diffMinutes < 5){
			sICAssignPOGenerateWarning = "This request appears to be a duplicate.  Clicking the 'Back' button in your browser"
				+ " may show you the PO number that was already assigned.";
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_ICASSIGNPO, 
					"PO requested twice within " + diffMinutes + " minutes", 
					"Comment was: '" + sPOComment + "'",
					"[1376509372]")
			;
			return true;
		}
		return false;
	}
	private boolean insertPOHeader( String sUserFullName, String sUserID, String sPOComment, Connection conn){
		
    	String SQL = "INSERT INTO " + SMTableicpoheaders.TableName + "("
		+ SMTableicpoheaders.sassignedtofullname
		+ ", " + SMTableicpoheaders.lassignedtouserid
		+ ", " + SMTableicpoheaders.datassigned
		+ ", " + SMTableicpoheaders.scomment
		//+ ", " + SMTableicpoheaders.datexpecteddate
		+ ", " + SMTableicpoheaders.datpodate
		+ ", " + SMTableicpoheaders.screatedbyfullname
		+ ", " + SMTableicpoheaders.lcreatedbyuserid
		+ ") VALUES ("
		+ "'" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
		+ "," + clsDatabaseFunctions.FormatSQLStatement(sUserID) + ""
		+ ", NOW()"
		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sPOComment) + "'"
		+ ", NOW()"
		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
		+ "," + clsDatabaseFunctions.FormatSQLStatement(sUserID) + ""
		+ ")"
		;

    	//System.out.println("[1395093960] SQL = " + SQL);
		try {
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				sICAssignPOGenerateWarning = "Could not insert PO to get a number.";
			}
		} catch (SQLException e) {
			sICAssignPOGenerateWarning = "Could not insert PO record - " + e.getMessage() + ".";
			return false;
		}

		//Get the po number:
		SQL = "SELECT LAST_INSERT_ID()";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (!rs.next()){
				sICAssignPOGenerateWarning = "Could not read PO ID.";
				rs.close();
				return false;
			}else{
				sICAssignPOGenerateAssignedNumber = Long.toString(rs.getLong(1));
				rs.close();
			}
		} catch (SQLException e) {
			sICAssignPOGenerateWarning = "Could not read PO ID - " + e.getMessage();
			return false;
		}
		
		//Finally, read the time from the assigned PO:
		SQL = "SELECT"
			+ " " + SMTableicpoheaders.datassigned
			+ " FROM " + SMTableicpoheaders.TableName
			+ " WHERE ("
				+ "(" + SMTableicpoheaders.lid + " = " + sICAssignPOGenerateAssignedNumber + ")"
			+ ")"
			;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (!rs.next()){
				sICAssignPOGenerateWarning = "Could not read PO assigned date.";
				rs.close();
				return false;
			}else{
				sICAssignPOGenerateAssignedDate = clsDateAndTimeConversions.resultsetDateTimeStringToString(
					rs.getString(SMTableicpoheaders.datassigned));
				rs.close();
			}
		} catch (SQLException e) {
			sICAssignPOGenerateWarning = "Could not read PO assigned date - " + e.getMessage();
			return false;
		}
		
		return true;
	}
}

package smcontrolpanel;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import SMDataDefinition.SMTableservicetypes;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMEditServiceTypeAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.SMViewAppointmentCalendar)){return;}

	    //Get request parameters
		long lID = Long.parseLong(request.getParameter(SMTableservicetypes.id));
		String sServiceTypeName = request.getParameter(SMTableservicetypes.sName);
		String sServiceTypeCode = request.getParameter(SMTableservicetypes.sCode);
		String sWorkOrderTerms = request.getParameter(SMTableservicetypes.mworkorderterms);
		String sWorkOrderReceiptComment = request.getParameter(SMTableservicetypes.mworeceiptcomment);
		
		//Get connection to database
		Connection conn = null;
		try {
			 conn = clsDatabaseFunctions.getConnection(
			    		getServletContext(), 
			    		smaction.getsDBID(), 
			    		"MySQL", 
			    		this.toString() 
			    		+ ".doPost - user: " 
			    		+ smaction.getUserID()
			    		+ " - "
			    		+ smaction.getFullUserName());

		}catch (Exception e) {
			smaction.redirectAction(
	    			"Could connect to database: " + e.getMessage(), 
	    			"", 
	    			"&" + SMTableservicetypes.id + "=" + Long.toString(lID)
	    			);
			return;
		}
		
	    if (conn == null){
	    	smaction.redirectAction(
	    			"Error connecting to database", 
	    			"", 
	    			"&" + SMTableservicetypes.id + "=" + Long.toString(lID)
	    			);
			return;
	    }
	    

	    
		//If it is a request to save
	    if(clsManageRequestParameters.get_Request_Parameter(
	    		"COMMANDFLAG", request).compareToIgnoreCase(SMEditAppointmentEdit.COMMAND_VALUE_SAVE) == 0){
	    	
		    //Validate entries.
		    try{
		    	validateEntries(
		    			lID, 
		    			sServiceTypeName, 
		    			sServiceTypeCode, 
		    			conn);
		    }catch (Exception e) {
		    	smaction.redirectAction(
		    			e.getMessage(), 
		    			"", 
		    			"&" + SMTableservicetypes.id + "=" + Long.toString(lID)
		    			);
				return;
		    }
		    
	    	String sSQL = "";
	    	//If this is not a new record update it
	    	if(lID != -1) {
	    		sSQL = "UPDATE " + SMTableservicetypes.TableName
		        		+ " SET" 
		        	    + " " + SMTableservicetypes.mworkorderterms + " = '" + clsDatabaseFunctions.FormatSQLStatement(sWorkOrderTerms) + "'"
		        	    + ", " + SMTableservicetypes.mworeceiptcomment + " = '" + clsDatabaseFunctions.FormatSQLStatement(sWorkOrderReceiptComment) + "'" 	
		        	    + ", " + SMTableservicetypes.sName + " = '" + clsDatabaseFunctions.FormatSQLStatement(sServiceTypeName) + "'" 
		        	    + ", " + SMTableservicetypes.sCode + " = '" + clsDatabaseFunctions.FormatSQLStatement(sServiceTypeCode) + "'" 
		        		+ " WHERE" 
		        		+ " " + SMTableservicetypes.id + " = " + lID 
		        		;
	        //Otherwise insert a new reocrd
	    	}else {
	    		sSQL = "INSERT INTO " + SMTableservicetypes.TableName
		        		+ " SET" 
		        	    + " " + SMTableservicetypes.mworkorderterms + " = '" + clsDatabaseFunctions.FormatSQLStatement(sWorkOrderTerms) + "'"
		        	    + ", " + SMTableservicetypes.mworeceiptcomment + " = '" + clsDatabaseFunctions.FormatSQLStatement(sWorkOrderReceiptComment) + "'" 	
		        	    + ", " + SMTableservicetypes.sName + " = '" + clsDatabaseFunctions.FormatSQLStatement(sServiceTypeName) + "'" 
		        	    + ", " + SMTableservicetypes.sCode + " = '" + clsDatabaseFunctions.FormatSQLStatement(sServiceTypeCode) + "'" 
		        		;
	    	}
			    try{
			    	clsDatabaseFunctions.executeSQL(sSQL, conn);
			    	if(lID == -1) {
			    		try {
				    		lID = clsDatabaseFunctions.getLastInsertID(conn);
				    	}catch (Exception e) {
				    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080541]");
					    	smaction.redirectAction(
					    			e.getMessage(), 
					    			"", 
					    			"&" + SMTableservicetypes.id + "=" + Long.toString(lID)
					    			);
							return;
				    	}	
			    	}
			    }catch (Exception e){
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080541]");
			    	smaction.redirectAction(
			    			"Error saving record: " + e.getMessage(), 
			    			"", 
			    			"&" + SMTableservicetypes.id + "=" + Long.toString(lID)
			    			);
					return;
			    }
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080542]");
			    	smaction.redirectAction(
			    			"", 
			    			"Saved successfully.", 
			    			"&" + SMTableservicetypes.id + "=" + Long.toString(lID)
			    			);
					return;
	    }
	    
		//If it is a request to delete the entry
	    if(clsManageRequestParameters.get_Request_Parameter(
	    		"COMMANDFLAG", request).compareToIgnoreCase(SMEditAppointmentEdit.COMMAND_VALUE_DELETE) == 0){
	    	
	    	String sSQL = "DELETE FROM " + SMTableservicetypes.TableName	    
	        		+ " WHERE " 
	        		+ SMTableservicetypes.id + " = " + lID 
	        		;

			    try{
			    	clsDatabaseFunctions.executeSQL(sSQL, conn);
			    }catch (Exception e){
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080544]");
			    	smaction.redirectAction(
			    			"Error delete record: " + e.getMessage(), 
			    			"", 
			    			"&" + SMTableservicetypes.id + "=" + Long.toString(lID)
			    			);
					return;
			    }

			    clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080545]");
			    smaction.setCallingClass("smcontrolpanel.SMEditServiceTypeSelect");
			    smaction.redirectAction(
		    			"", 
		    			"", 
		    			"" 
		    			);
				return;
	    }
	}

	private void validateEntries(long lID, String sServiceTypeName, String sServiceTypeCode, Connection conn) throws Exception {
		
		String sInvalidEntryWarning = "";
		if(sServiceTypeName.compareToIgnoreCase("") == 0) {
			sInvalidEntryWarning += "Service type name is required. ";
		}
		if(sServiceTypeCode.compareToIgnoreCase("") == 0) {
			sInvalidEntryWarning += "Service type code is required. ";
		}
		
		String SQL = "SELECT * FROM " + SMTableservicetypes.TableName 
					+ " WHERE " + SMTableservicetypes.id + " != " + lID;
			
		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		
		while(rs.next()) {
			if(rs.getString(SMTableservicetypes.sCode).trim().compareToIgnoreCase(sServiceTypeCode.trim()) == 0) {
				sInvalidEntryWarning += "This service type code '" + sServiceTypeCode + "' already exists. ";
			}
			if(rs.getString(SMTableservicetypes.sName).trim().compareToIgnoreCase(sServiceTypeName.trim()) == 0) {
				sInvalidEntryWarning += "This service type name '" + sServiceTypeName + "'already exists. ";
			}
		}
		rs.close();
		
		if(sInvalidEntryWarning.compareToIgnoreCase("") != 0) {
			throw new Exception(sInvalidEntryWarning);
		}
		return;
	}

	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

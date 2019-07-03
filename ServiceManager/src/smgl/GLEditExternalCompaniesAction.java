package smgl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMDataDefinition.SMTableglexternalcompanies;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;

public class GLEditExternalCompaniesAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.GLEditBatches)){return;}
	    //Read the entry fields from the request object:
	    
	    //First get the command value that was passed in:
	    String sCommandValue = clsManageRequestParameters.get_Request_Parameter(GLEditEntryEdit.COMMAND_FLAG, request);
	    
		//If it's an edit, process that:
    	if (sCommandValue.compareToIgnoreCase(GLEditEntryEdit.COMMAND_VALUE_SAVE) == 0){
    		try {
    			updateCompanies(smaction, request);
			} catch (Exception e) {
				smaction.redirectAction(
					e.getMessage(), 
					"", 
    	    		"&" + GLEditEntryEdit.RECORDWASCHANGED_FLAG + "=" 
	    			+ clsManageRequestParameters.get_Request_Parameter(GLEditExternalCompaniesEdit.RECORDWASCHANGED_FLAG, request)
				);
				return;
			}
			smaction.redirectAction(
				"", 
				"Companies were successfully updated.",
   	    		""
			);
			return;
	    }
    	
    	//Process remove line
    	if (sCommandValue.compareToIgnoreCase(GLEditEntryEdit.COMMAND_VALUE_REMOVELINE) == 0){
    		String sLid = clsManageRequestParameters.get_Request_Parameter(GLEditExternalCompaniesEdit.LID_TO_DELETE_PARAM, request);
    		
    		try {
    			removeCompany(smaction, sLid);
			} catch (Exception e) {
				smaction.redirectAction(
					"Could not remove company with ID# '" + sLid + "' - " + e.getMessage(), 
					"", 
    	    		"&" + GLEditEntryEdit.RECORDWASCHANGED_FLAG + "=" 
	    			+ clsManageRequestParameters.get_Request_Parameter(GLEditExternalCompaniesEdit.RECORDWASCHANGED_FLAG, request)
				);
				return;
			}
			smaction.redirectAction(
				"", 
				"Company ID# " + sLid + " was successfully removed.",
   	    		""
			);
			return;
    	}
    	
		return;
	}
	private void updateCompanies(SMMasterEditAction sm, HttpServletRequest req) throws Exception{
		
		//Read the external company lines:
		String sErrorString = "";
    	Enumeration <String> eParams = req.getParameterNames();
    	String sLineParam = "";
    	String sLid = "";
    	long lLid = 0;
    	
    	Connection conn = null;
    	try {
			conn = ServletUtilities.clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", 
				this.toString() + ".updateCompanies - user: " + sm.getFullUserName()
			);
		} catch (Exception e1) {
			throw new Exception("Error [20191791712174] " + "Could not get datas connection - " + e1.getMessage());
		}
    	
    	while (eParams.hasMoreElements()){
    		sLineParam = eParams.nextElement();
    		//System.out.println("[1490711688] sLineParam = '" + sLineParam +"'");
    		//If it contains an lid parameter, then it's an GLTransactionBatchLine field:
    		if (sLineParam.startsWith(GLEditExternalCompaniesEdit.PARAM_DB_PREFIX)){
    			//System.out.println("[1490711588] sLineParam = '" + sLineParam +"'");
    			sLid = sLineParam.substring(
    				GLEditExternalCompaniesEdit.PARAM_DB_PREFIX.length(),
    				GLEditExternalCompaniesEdit.PARAM_DB_PREFIX.length() + GLEditExternalCompaniesEdit.LID_PADDING_LENGTH).trim();
    			lLid = Integer.parseInt(sLid);
    			
    			//If the lid is zero, this is a new company being added:
    			//System.out.println("[1490711589] sLineNumber = '" + sLineNumber +"'");
    			String sDBName = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(sLineParam, req).trim();
    			String sCompanyNameParameter = sLineParam.replace(GLEditExternalCompaniesEdit.PARAM_DB_PREFIX, GLEditExternalCompaniesEdit.PARAM_COMPANYNAME_PREFIX);
    			sCompanyNameParameter = sCompanyNameParameter.replace(SMTableglexternalcompanies.sdbname, SMTableglexternalcompanies.scompanyname);
    			String sCompanyName = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(sCompanyNameParameter, req).trim();
    			
    			//If it's the 'blank' line, and nothing has been added on it, then just keep going...
    			if ( (lLid == 0L) && (sDBName.compareToIgnoreCase("") == 0) && (sCompanyName.compareToIgnoreCase("") == 0) ){
    				continue;
    			}
    			
    			//Validate the fields:
    			if (sDBName.length() > SMTableglexternalcompanies.sdbnameLength){
    				sErrorString += "Database name '" + sDBName + " cannot be longer than " + Integer.toString(SMTableglexternalcompanies.sdbnameLength) + " characters.  ";
    			}
    			if (sDBName.compareToIgnoreCase("") == 0){
    				sErrorString += "Database name cannot be blank on ID# " + sLid + ".  ";
    			}
    			if (sCompanyName.length() > SMTableglexternalcompanies.scompanynameLength){
    				sErrorString += "Company name '" + sDBName + " cannot be longer than " + Integer.toString(SMTableglexternalcompanies.scompanynameLength) + " characters.  ";
    			}
    			if (sCompanyName.compareToIgnoreCase("") == 0){
    				sErrorString += "Company name cannot be blank on ID# " + sLid + ".  ";
    			}
    			
    			//Try to update/insert the record:
    			String SQL = "";
    			if (lLid == 0L){
	    			SQL = "INSERT INTO " + SMTableglexternalcompanies.TableName
	    				+ " ("
	    				+ SMTableglexternalcompanies.sdbname
	    				+ ", " + SMTableglexternalcompanies.scompanyname
	    				+ ") VALUES ("
	    				+ "'" + ServletUtilities.clsDatabaseFunctions.FormatSQLStatement(sDBName) + "'"
	    				+ ", '" + ServletUtilities.clsDatabaseFunctions.FormatSQLStatement(sCompanyName) + "'"
	    				+ ")"
	    			;
    			}else{
    				SQL = "UPDATE " + SMTableglexternalcompanies.TableName
    				+ " SET " + SMTableglexternalcompanies.scompanyname + " = '" + clsDatabaseFunctions.FormatSQLStatement(sCompanyName) + "'"
    				+ ", " + SMTableglexternalcompanies.sdbname + " = '" + ServletUtilities.clsDatabaseFunctions.FormatSQLStatement(sDBName) + "'"
    				+ " WHERE ("
    					+ "(" + SMTableglexternalcompanies.lid + " = " + sLid + ")"
    				+ ")"
    			;
    			}

    			//System.out.println("[20191791648519] " + "SQL = '" + SQL + "'");
    			try {
    				Statement stmt = conn.createStatement();
    				stmt.execute(SQL);
    			} catch (Exception e) {
    				ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1561756575]");
    				throw new Exception("Error [2019179162391] " + "could not insert or update company with database '" + sDBName + "'"
    					+ " SQL = '"+ SQL + "' - " + e.getMessage());
    			}
    		}
    	}
    	if (sErrorString.compareToIgnoreCase("") != 0){
    		ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1561756577]");
    		throw new Exception("Error [20191791624441] saving companies - " + sErrorString);
    	}
    	ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1561756576]");
		return;
	}

	private void removeCompany(SMMasterEditAction sm, String slid) throws Exception{
		
		String SQL = "DELETE FROM " + SMTableglexternalcompanies.TableName
			+ " WHERE ("
				+ "(" + SMTableglexternalcompanies.lid + " = " + slid + ")"
			+ ")"
		;
		try {
			ServletUtilities.clsDatabaseFunctions.executeSQLWithException(
				SQL, 
				sm.getsDBID(), 
				"MySQL", 
				this.toString() + ".removeCompany - user: " + sm.getFullUserName(), 
				getServletContext()
			);
		} catch (Exception e) {
			throw new Exception("Error [20191791540330] " + "Could not remove company with ID# '" + slid + "' - " + e.getMessage());
		}
		
		return;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
package smic;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class ICSetInactiveItemsAction extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private String sWarning = "";
	private static String sDBID = "";
	private static final boolean bDebugMode = false;
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    if (!SMAuthenticate.authenticateSMCPCredentials(
	    	request, 
	    	response, 
	    	getServletContext(), 
	    	SMSystemFunctions.ICSetInactiveItems)){
	    	return;
	    }
	    
    	//Get connection:
    	Enumeration <String> paramNames = request.getParameterNames();
	    String sItemNumber = "";
	    String SQL = "";
	    String sSetActiveFlagTo = clsManageRequestParameters.get_Request_Parameter("SETACTIVEFLAGTO", request);
	    if (sSetActiveFlagTo.trim().compareToIgnoreCase("") == 0){
	    	sWarning = "SETACTIVEFLAGTO is blank";
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) 
    				+ "smic.ICSetInactiveItemsSelection" + "?"
    				+ "Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;	    	
	    }
    	long lStartTime = System.currentTimeMillis();
    	long lItemCounter = 0;
    	String sAction = "";
    	if (sSetActiveFlagTo.compareTo("1") == 0){
    		sAction = "active";
    	}else{
    		sAction = "inactive";
    	}
    	//System.out.println("[1394133501] sAction = " + sAction);
	    while(paramNames.hasMoreElements()) {
			String sParamName = paramNames.nextElement();
			if (sParamName.contains(ICSetInactiveItemsGenerate.ITEM_CHECKBOX_NAME)){
				if (request.getParameter(sParamName) != null){
					if (bDebugMode){lItemCounter++;}
					sItemNumber = sParamName.replace(ICSetInactiveItemsGenerate.ITEM_CHECKBOX_NAME, "");
					//sItemNumber = request.getParameter(sParamName);
					SQL = "UPDATE "
						+ SMTableicitems.TableName
						+ " SET " + SMTableicitems.iActive
						+ " = " + sSetActiveFlagTo
						;
						if (sSetActiveFlagTo.compareToIgnoreCase("1") == 0){
							SQL += ", " + SMTableicitems.datInactive + " = '0000-00-00 00:00:00'";
						}else{
							SQL += ", " + SMTableicitems.datInactive + " = NOW()";
						}
						
						SQL += " WHERE " + SMTableicitems.sItemNumber
							+ " = '" + sItemNumber + "'"
					;
					//if (bDebugMode){
					//	System.out.println("In " + this.toString() + " - SQL = " + SQL);
					//}
					try{
						clsDatabaseFunctions.executeSQL(SQL, getServletContext(), sDBID);
					}catch(SQLException e){
						sWarning = "Could not set item " + sItemNumber + " to " + sAction + " using SQL: " + SQL 
						+ " - error:" + e.getMessage() + ".";
			    		response.sendRedirect(
			    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smic.ICSetInactiveItemsSelection" + "?"
			    				+ "Warning=" + sWarning
			    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			    		);			
			        	return;
					}
				}
			}
	    }
	    if (bDebugMode){
	    	System.out.println("In " + this.toString() + " - number of items updated = " + Long.toString(lItemCounter)
	    		+ " in " + Long.toString(((System.currentTimeMillis() - lStartTime) / 1000))
	    	);
	    }
		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smic.ICSetInactiveItemsSelection" + "?"
				+ "Warning=" + "Selected items were successfully set to " + sAction + "."
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		);			
    	return;
	}

	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doGet(request, response);
	}
}

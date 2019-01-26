package smcontrolpanel;

import SMDataDefinition.SMTablelocations;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SMEditLocationsAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static final String sObjectName = "Location";
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
	PrintWriter out = response.getWriter();
	if (!SMAuthenticate.authenticateSMCPCredentials(
			request, 
			response, 
			getServletContext(), 
			SMSystemFunctions.SMEditLocations))
	{
		return;
	}

    //Get the session info:
    HttpSession CurrentSession = request.getSession(true);
    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
    
    String sEditCode = clsStringFunctions.filter(request.getParameter("EditCode"));
    String title = "Updating " + sObjectName + "'" + sEditCode + "'";
    String subtitle = "";
    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	
    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

    String sOutPut = "";
 
    String iShowInTruckSchedule = "1";
	if(request.getParameter(SMTablelocations.ishowintruckschedule) == null){
		iShowInTruckSchedule = "0";
	}
    String SQL = "UPDATE " + SMTablelocations.TableName
		+ " SET " 
		+ SMTablelocations.sAddress1 + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sAddress1)) + "', "
		+ SMTablelocations.sAddress2 + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sAddress2)) + "', "
		+ SMTablelocations.sAddress3 + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sAddress3)) + "', "
		+ SMTablelocations.sAddress4 + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sAddress4)) + "', "
		+ SMTablelocations.sCity + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sCity)) + "', "
		+ SMTablelocations.sCompanyDescription + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sCompanyDescription)) + "', "
		+ SMTablelocations.sContact + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sContact)) + "', "
		+ SMTablelocations.sCountry + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sCountry)) + "', "
		+ SMTablelocations.sFax + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sFax)) + "', "
		+ SMTablelocations.sRemitToAddress1 + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sRemitToAddress1)) + "', "
		+ SMTablelocations.sRemitToAddress2 + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sRemitToAddress2)) + "', "
		+ SMTablelocations.sRemitToAddress3 + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sRemitToAddress3)) + "', "
		+ SMTablelocations.sRemitToAddress4 + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sRemitToAddress4)) + "', "
		+ SMTablelocations.sRemitToCity + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sRemitToCity)) + "', "
		+ SMTablelocations.sRemitToState + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sRemitToState)) + "', "
		+ SMTablelocations.sRemitToZip + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sRemitToZip)) + "', "
		+ SMTablelocations.sRemitToCompanyDescription + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sRemitToCompanyDescription)) + "', "
		+ SMTablelocations.sRemitToContact + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sRemitToContact)) + "', "
		+ SMTablelocations.sRemitToCountry + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sRemitToCountry)) + "', "
		+ SMTablelocations.sRemitToPhone + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sRemitToPhone)) + "', "
		+ SMTablelocations.sRemitToFax + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sRemitToFax)) + "', "
		+ SMTablelocations.sLocationDescription + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sLocationDescription)) + "', "
		+ SMTablelocations.sLogo + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sLogo)) + "', "
		+ SMTablelocations.sPhone + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sPhone)) + "', "
		+ SMTablelocations.sSecondOfficeName + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sSecondOfficeName)) + "', "
		+ SMTablelocations.sSecondOfficePhone + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sSecondOfficePhone)) + "', "
		+ SMTablelocations.sState + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sState)) + "', "
		+ SMTablelocations.sTollFreeNumber + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sTollFreeNumber)) + "', "
		+ SMTablelocations.sWebSite + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sWebSite)) + "', "
		+ SMTablelocations.sZip + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sZip)) + "', "
		+ SMTablelocations.sGLInventoryAcct + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sGLInventoryAcct)) + "', "
		+ SMTablelocations.sGLPayableClearingAcct + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sGLPayableClearingAcct)) + "', "
		+ SMTablelocations.sGLWriteOffAcct + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sGLWriteOffAcct)) + "', "
		+ SMTablelocations.sAdditionalNotes + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sAdditionalNotes)) + "', "	
		+ SMTablelocations.sGLTransferClearingAcct + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablelocations.sGLTransferClearingAcct)) + "', "
		+ SMTablelocations.ishowintruckschedule + " = " + iShowInTruckSchedule
		+ " WHERE " + SMTablelocations.sLocation + " = '" + sEditCode + "'";
    try{
    	if (clsDatabaseFunctions.executeSQL(SQL, getServletContext(), sDBID) == false){
    		sOutPut = "Could not complete update transaction - " + sObjectName + " was not updated.<BR>";
    	}else{
    		sOutPut = "Successfully updated " + sObjectName + ": " + sEditCode + ".";
    	}
    }catch (SQLException ex){
		//System.out.println("Error in " + this.toString() + " class!!");
	    //System.out.println("SQLException: " + ex.getMessage());
	    //System.out.println("SQLState: " + ex.getSQLState());
	    //System.out.println("SQL: " + ex.getErrorCode());
	    sOutPut = "Error [1420467789] - could not update " + sObjectName + " with SQL: '" + SQL + "' - " + ex.getMessage() + ".<BR>";
	}
    
    out.println(sOutPut);
    out.println("</BODY></HTML>");
	}

	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

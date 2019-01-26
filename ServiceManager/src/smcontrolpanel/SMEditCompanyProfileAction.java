package smcontrolpanel;

import SMDataDefinition.SMTablecompanyprofile;
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

public class SMEditCompanyProfileAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Company Profile";
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
	String sEditCode = clsStringFunctions.filter(request.getParameter("EditCode"));
	
	PrintWriter out = response.getWriter();
	if(!SMAuthenticate.authenticateSMCPCredentials(
			request, 
			response, 
			getServletContext(), 
			SMSystemFunctions.SMEditCompanyProfile))
		{
			return;
		}

    //Get the session info:
    HttpSession CurrentSession = request.getSession(true);
    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
    String title = "Updating " + sObjectName;
    String subtitle = "";
    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

    String sOutPut = "";

    String sSQL = SMMySQLs.Update_CompanyProfile_By_ID_SQL(
    		sEditCode,
    		clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablecompanyprofile.sAddress01)),
    		clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablecompanyprofile.sAddress02)),
    		clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablecompanyprofile.sAddress03)),
    		clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablecompanyprofile.sAddress04)),
    		clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablecompanyprofile.sCity)),
    		clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablecompanyprofile.sCompanyName)),
    		clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablecompanyprofile.sContactName)),
    		clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablecompanyprofile.sCountry)),
    		clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablecompanyprofile.sFaxNumber)),
    		clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablecompanyprofile.sPhoneNumber)),
    		clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablecompanyprofile.sState)),
    		clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablecompanyprofile.sZipCode))
    	//	SMUtilities.FormatSQLStatement(request.getParameter(SMTablecompanyprofile.sWOReceiptHeaderComment))
    		);
    
    try{
    	if (clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), sDBID) == false){
    		sOutPut = "Could not complete update transaction - " + sObjectName + " was not updated.<BR>";
    	}else{
    		sOutPut = "Successfully updated " + sObjectName + ": " + sEditCode + ".";
    	}
    }catch (SQLException ex){
		System.out.println("Error in " + this.toString() + " class!!");
	    System.out.println("SQLException: " + ex.getMessage());
	    System.out.println("SQLState: " + ex.getSQLState());
	    System.out.println("SQL: " + ex.getErrorCode());
	    sOutPut = "Error - could not update " + sObjectName + ".<BR>";
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
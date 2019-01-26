package smcontrolpanel;

import SMDataDefinition.SMTabledefaultitemcategories;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTableservicetypes;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SMEditDefaultItemCategoriesAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Default Item Category Records";
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
	PrintWriter out = response.getWriter();
	if (!SMAuthenticate.authenticateSMCPCredentials(
			request, 
			response, 
			getServletContext(), 
			SMSystemFunctions.SMEditDefaultItemCategories))
	{
		return;
	}

    //Get the session info:
    HttpSession CurrentSession = request.getSession(true);
    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
    String sUser = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	
    String title = "Updating " + sObjectName;
    String subtitle = "";
    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	
    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

    String sOutPut = "<BR>Successfully updated Default Item Categories.";
    String SQL = 
		"SELECT"
		+ " " + SMTablelocations.TableName + "." + SMTablelocations.sLocation
		+ ", " + SMTableservicetypes.TableName + "." + SMTableservicetypes.sCode
		+ " FROM " + SMTablelocations.TableName + ", " + SMTableservicetypes.TableName
    ;
    try {
		ResultSet rs = clsDatabaseFunctions.openResultSet(
			SQL, 
			getServletContext(),
			sDBID, 
			"MySQL", 
			this.toString() + ".doPost - user: " + sUserID
			+ " - "
			+ sUserFullName	);
		while (rs.next()){
			String sUpdateSQL = 
				"INSERT INTO " + SMTabledefaultitemcategories.TableName + "("
				+ SMTabledefaultitemcategories.DefaultItemCategory
				+ ", " + SMTabledefaultitemcategories.InitialItem
				+ ", " + SMTabledefaultitemcategories.LocationCode
				+ ", " + SMTabledefaultitemcategories.ServiceTypeCode
				+ ") VALUES ("
				+ "'" + clsManageRequestParameters.get_Request_Parameter(SMEditDefaultItemCategories.DEFAULT_CATEGORY_MARKER
					+ rs.getString(SMTablelocations.TableName + "." + SMTablelocations.sLocation) 
					+ rs.getString(SMTableservicetypes.TableName + "." + SMTableservicetypes.sCode), request) + "'"
				+ ", '" + clsManageRequestParameters.get_Request_Parameter(SMEditDefaultItemCategories.INITIAL_ITEM_MARKER
					+ rs.getString(SMTablelocations.TableName + "." + SMTablelocations.sLocation) 
					+ rs.getString(SMTableservicetypes.TableName + "." + SMTableservicetypes.sCode), request) + "'"
				+ ", '" + rs.getString(SMTablelocations.TableName + "." + SMTablelocations.sLocation) + "'"
				+ ", '" + rs.getString(SMTableservicetypes.TableName + "." + SMTableservicetypes.sCode) + "'"
				+ ")"
				+ " ON DUPLICATE KEY UPDATE"
				+ " " + SMTabledefaultitemcategories.DefaultItemCategory + " = '" 
					+ clsManageRequestParameters.get_Request_Parameter(SMEditDefaultItemCategories.DEFAULT_CATEGORY_MARKER
					+ rs.getString(SMTablelocations.TableName + "." + SMTablelocations.sLocation) 
					+ rs.getString(SMTableservicetypes.TableName + "." + SMTableservicetypes.sCode), request) + "'"
				+ ", " + SMTabledefaultitemcategories.InitialItem + " = '" 
					+ clsManageRequestParameters.get_Request_Parameter(SMEditDefaultItemCategories.INITIAL_ITEM_MARKER
					+ rs.getString(SMTablelocations.TableName + "." + SMTablelocations.sLocation) 
					+ rs.getString(SMTableservicetypes.TableName + "." + SMTableservicetypes.sCode), request) + "'"
				;
			try {
				clsDatabaseFunctions.executeSQL(
					sUpdateSQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					this.toString() + ".Updating - user: " + sUser
				);
			} catch (Exception e) {
				rs.close();
				sOutPut = "<BR>Error updating default item categories with SQL: " + sUpdateSQL + " - " + e.getMessage() + ".";
				break;
			}
		}
		rs.close();
	} catch (SQLException e) {
		sOutPut = "<BR>Error listing locations and service types - " + e.getMessage() + ".";
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

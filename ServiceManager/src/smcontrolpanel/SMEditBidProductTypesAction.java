package smcontrolpanel;

import SMDataDefinition.SMTablebidproducttypes;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SMEditBidProductTypesAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static String sObjectName = SMBidEntry.ParamObjectName + " Product Types";
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMEditBidProductTypes
			)
		){
			return;
		}
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sBidProductType = clsStringFunctions.filter(request.getParameter(SMTablebidproducttypes.sProductType));
		String sBidProductTypeID = clsManageRequestParameters.get_Request_Parameter(SMTablebidproducttypes.lID, request);
		
	    String title = "Updating " + sObjectName + "'" + sBidProductType + "'";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	
	    String sSQL = "UPDATE " + SMTablebidproducttypes.TableName
	    	+ " SET " + SMTablebidproducttypes.sProductType + " = '" + sBidProductType + "'"
	    	+ " WHERE ("
	    		+ SMTablebidproducttypes.lID + " = " + sBidProductTypeID 
	    	+ ")"
	    	;
		
	    try{
	    	clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), sDBID);
	    }catch (SQLException ex){
	    	out.println("Error updating product type with SQL: " + sSQL
	    		+ " - " + ex.getMessage());
			System.out.println("Error updating product type with SQL: " + sSQL
		    		+ " - " + ex.getMessage());
			out.println("</BODY></HTML>");
			return;
		}
	    out.println("Successfully updated product type " + sBidProductType + ".");
	    out.println("</BODY></HTML>");
	}

	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

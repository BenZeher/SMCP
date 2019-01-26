package smcontrolpanel;

import SMDataDefinition.SMTablesalesgroups;
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

public class SMEditSalesGroupsAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Sales Group";
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditSalesGroups))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sID = clsStringFunctions.filter(request.getParameter("EditCode"));
		
	    String title = "Updating " + sObjectName + "'" + sID + "'";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	
	    String sOutPut = "";
	    //System.out.println("Code = " + SMUtilities.FormatSQLStatement(request.getParameter(SMTablesalesgroups.sSalesGroupCode)));
	    //System.out.println("Desc = " + SMUtilities.FormatSQLStatement(request.getParameter(SMTablesalesgroups.sSalesGroupDesc)));
	    String sSQL = Update_Sales_Group_SQL(sID,
								    	     clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablesalesgroups.sSalesGroupCode)),
								    	     clsDatabaseFunctions.FormatSQLStatement(request.getParameter(SMTablesalesgroups.sSalesGroupDesc))
	    									 );
	    
	    try{
	    	if (clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), sDBID) == false){
	    		sOutPut = "Could not complete update transaction - " + sObjectName + " was not updated.<BR>";
	    	}else{
	    		sOutPut = "Successfully updated " + sObjectName + ": " + sID + ".";
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
	
	public static String Update_Sales_Group_SQL(String iID,
												 String sGroupCode,
												 String sGroupDesc
											  	 ){
			
			String SQL = "UPDATE " + SMTablesalesgroups.TableName
			+ " SET " 
			+ SMTablesalesgroups.sSalesGroupCode + " = '" + sGroupCode + "', "
			+ SMTablesalesgroups.sSalesGroupDesc + " = '" + sGroupDesc + "'"
			
			+ " WHERE " + SMTablesalesgroups.iSalesGroupId + " = " + iID;

			System.out.println ("Update_Sales_Group_SQL = " + SQL);
			return SQL;
	}
}

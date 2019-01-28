package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTablesecurityfunctions;
import SMDataDefinition.SMTablesecuritygroupfunctions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;

public class SMManageSecurityGroupsAction extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		String sGroupName = clsStringFunctions.filter(request.getParameter("GroupName"));
		
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMManageSecurityGroups))
		{
			return;
		}

	    String title = "Updating Security Group '" + sGroupName + "'";
	    String subtitle = "";
		
	    HttpSession CurrentSession = request.getSession(true);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

	    String sOutPut = "";

	    //First, start a transaction
	    ArrayList<String> sSQLList = new ArrayList<String>(0);
	    //First, delete all the SecurityGroupFunctions:
	    sSQLList.add(SMMySQLs.Delete_Group_Functions_SQL(sGroupName));
	    
	    //Next, delete all SecurityUserGroups:
	    sSQLList.add(SMMySQLs.Delete_Group_Users_SQL(sGroupName));
	    
	    //Now add back in all the SecurityGroupFunctions for this group:
		Enumeration<?> paramNames = request.getParameterNames();
	    String sFunctionMarker = "Function***Update";
	    String sUserMarker = "User***Update";
	    while(paramNames.hasMoreElements()) {
	      String sParamName = (String)paramNames.nextElement();
	      
		  if (sParamName.contains(sFunctionMarker)){
			  String sFunctionID = (sParamName.substring(sParamName.indexOf(sFunctionMarker) + sFunctionMarker.length()));

			  //Now add an insert statement for each function:
			  sSQLList.add(SMMySQLs.Insert_Security_Group_Function_SQL(sGroupName, sFunctionID));
		  }
		  if (sParamName.contains(sUserMarker)){
			  String sUserID = (sParamName.substring(sParamName.indexOf(sUserMarker) + sUserMarker.length()));

			  //Now add an insert statement for each function:
			  sSQLList.add(SMMySQLs.Insert_Security_Group_User_SQL(sGroupName, sUserID));
		  }
	    }
		//Update the function names in SecurityFunctionGroups:
	    //NOTE - this can be removed if code in Service Manager no longer needs to lookup the 
	    //function from the securitygroupfunctions table.
	    String SQL = "UPDATE "
	    		+ SMTablesecurityfunctions.TableName
	    		+ " INNER JOIN " + SMTablesecuritygroupfunctions.TableName + " ON " 
	    		+ SMTablesecurityfunctions.TableName + "." + SMTablesecurityfunctions.iFunctionID 
	    		+ " = " + SMTablesecuritygroupfunctions.TableName + "." + SMTablesecuritygroupfunctions.ifunctionid
	    		+ " SET " + SMTablesecuritygroupfunctions.TableName + "." + SMTablesecuritygroupfunctions.sFunction 
	    		+ " = " + SMTablesecurityfunctions.TableName + "." + SMTablesecurityfunctions.sFunctionName;
	    sSQLList.add(SQL);
		  
	    try{
	    	if (clsDatabaseFunctions.executeSQLsInTransaction(sSQLList, getServletContext(), sDBID) == false){
	    		sOutPut = "Could not complete update transaction - group was not updated.<BR>";
	    	}else{
	    		sOutPut = "Successfully updated group " + sGroupName + ".";
	    	}
	    }catch (SQLException ex){
			System.out.println("Error in SMUtilities.commitTransaction class!!");
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("SQL: " + ex.getErrorCode());
		    sOutPut = "Error - could not update group.<BR>";
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
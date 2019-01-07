package TimeCardSystem;

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

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;

public class ManageACGroupsAction extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		response.setContentType("text/html");
		
		String sGroupName = clsStringFunctions.filter(request.getParameter("GroupName"));
		
		PrintWriter out = response.getWriter();
	    String title = "Updating Access Control Group '" + sGroupName + "'";
	    String subtitle = "";
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");
	    HttpSession CurrentSession = request.getSession(true);
	    String sConfFile = (String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB); 

	    String sOutPut = "";

	    //First, start a transaction
	    ArrayList<String> sSQLList = new ArrayList<String>(0);
	    //First, delete all the SecurityGroupFunctions:
	    sSQLList.add(TimeCardSQLs.Delete_Group_Functions_SQL(sGroupName));
	    
	    //Next, delete all SecurityUserGroups:
	    sSQLList.add(TimeCardSQLs.Delete_Group_Users_SQL(sGroupName));
	    
	    //Now add back in all the SecurityGroupFunctions for this group:
		Enumeration<?> paramNames = request.getParameterNames();
	    String sFunctionMarker = "Function***Update";
	    String sUserMarker = "User***Update";
	    while(paramNames.hasMoreElements()) {
	      String sParamName = (String)paramNames.nextElement();
	      
		  if (sParamName.contains(sFunctionMarker)){
			  String sFunction = (sParamName.substring(sParamName.indexOf(sFunctionMarker) + sFunctionMarker.length()));

			  //Now add an insert statement for each function:
			  sSQLList.add(TimeCardSQLs.Insert_Access_Control_Group_Function_SQL(sGroupName, sFunction));
		  }
		  if (sParamName.contains(sUserMarker)){
			  String sUser = (sParamName.substring(sParamName.indexOf(sUserMarker) + sUserMarker.length()));

			  //Now add an insert statement for each function:
			  sSQLList.add(TimeCardSQLs.Insert_Access_Control_Group_User_SQL(sGroupName, sUser));
		  }
	    }
		//TODO - Update the users here
		  
	    try{
	    	if (clsDatabaseFunctions.executeSQLsInTransaction(sSQLList, getServletContext(), sConfFile) == false){
	    		sOutPut = "Could not complete update transaction - group was not updated.<BR>";
	    	}else{
	    		sOutPut = "Successfully updated group " + sGroupName + ".";
		    	out.println("<META http-equiv='Refresh' content='2;URL=" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.ManageACGroups'>");
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

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
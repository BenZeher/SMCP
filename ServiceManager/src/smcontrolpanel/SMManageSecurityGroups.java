package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;

public class SMManageSecurityGroups extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static String sDBID = "";
	private static String sCompanyName = "";
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMManageSecurityGroups
			)
		){
			return;
		}
		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);

	    String title = "Manage Security Groups";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    
	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	    
	    out.println("<FORM NAME=\"MAINFORM\" ACTION=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMManageSecurityGroupsEdit\" METHOD=\"POST\">");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "\" VALUE=\"" + sDBID + "\">");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"CallingClass\" VALUE=\"" + SMUtilities.getFullClassName(this.toString()) + "\">");
	    String sOutPut = "";
	    
	    //Add drop down list
		try{
	        String sSQL = SMMySQLs.Get_Security_Group_List_SQL();
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	     	out.println ("<SELECT NAME=\"Security Groups\">" );
        	
        	while (rs.next()){
        		//flag that there are multiple entries.
        		sOutPut = "<OPTION VALUE=\"" + rs.getString("sSecurityGroupName") + "\">";
        		sOutPut = sOutPut + rs.getString("sSecurityGroupName");
	        	out.println (sOutPut);
        	}
        	rs.close();
	        	//End the drop down list:
	        out.println ("</SELECT>");
	        out.println ("<BR>");
		}catch (SQLException ex){
	    	System.out.println("Error in SMManageSecurityGroups class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			//return false;
		}
		//Display text boxes for the new password and a confirmation:
		
		sOutPut = "<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Edit Selected Group' STYLE='width: 2.00in; height: 0.24in'></P>";
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='SubmitDelete' VALUE='Delete Selected Group' STYLE='width: 2.00in; height: 0.24in'>";
		sOutPut = sOutPut + "  Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"ConfirmDelete\">";
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='SubmitAdd' VALUE='Add New Group' STYLE='width: 2.00in; height: 0.24in'></P>";
		sOutPut = sOutPut + "<P>New Group To Be Added: <INPUT TYPE=TEXT NAME=\"NewGroupName\" SIZE=28 MAXLENGTH=50 STYLE=\"width: 2.41in; height: 0.25in\"></P>";
		sOutPut = sOutPut + "</FORM>";
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

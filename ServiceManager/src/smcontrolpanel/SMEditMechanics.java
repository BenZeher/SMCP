package smcontrolpanel;

import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTablemechanics;
import ServletUtilities.clsDatabaseFunctions;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SMEditMechanics extends HttpServlet {
	
	public static final String NEW_MECHANIC_ID = "-1";
	
	private static final long serialVersionUID = 1L;
	@Override
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditMechanics))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "Manage Technicians";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	    try {
	    	
	        String sSQL = "SELECT" 
	        	+ " " + SMTablemechanics.TableName + "." + SMTablemechanics.lid + "," 
	        	+ " " + SMTablemechanics.TableName + "." + SMTablemechanics.sMechFullName + "," 
	        	+ " " + SMTablelocations.TableName + "." + SMTablelocations.sLocationDescription 
	        	+ " FROM " + SMTablemechanics.TableName + " LEFT JOIN " + SMTablelocations.TableName 
	        	+ " ON " + SMTablemechanics.TableName + "." + SMTablemechanics.sMechLocation 
	        	+ " = " + SMTablelocations.TableName + "." + SMTablelocations.sLocation 
	        	+ " ORDER BY " + SMTablemechanics.TableName + "." + SMTablemechanics.sMechFullName 
	        	 + ", " + SMTablelocations.TableName + "." + SMTablelocations.sLocation
	        ;
	        //System.out.println (sSQL);
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	        out.println("<H3>Technician(s) in system:</H3>");
        	//print the first line here. 
        	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditMechanicsEdit\">");
        	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
        	out.println("<INPUT TYPE=HIDDEN NAME=\"RESTRICTED\" VALUE=\"NO\">");
        	out.println("Location and Technician:<BR>");
        	out.println ("<SELECT NAME=\"MECHANIC\">" );
        	out.println ("<OPTION VALUE=\"" + NEW_MECHANIC_ID + "\">----Create New Technician---- ");
        	while (rs.next()){
        		String sLocationDescription = rs.getString(SMTablelocations.TableName + "." + SMTablelocations.sLocationDescription);
        		if (sLocationDescription == null){
        			sLocationDescription = "(NONE)";
        		}
        		//flag that there are multiple entries.
	        	out.println ("<OPTION VALUE=\"" + Long.toString(rs.getLong(SMTablemechanics.lid)) + "\" CHECKED>"
	        	//+ Long.toString(rs.getLong(SMTablemechanics.lid)) + " "
        		+ sLocationDescription + " - " 
	        	+ rs.getString(SMTablemechanics.TableName + "." + SMTablemechanics.sMechFullName));
        	}
        	rs.close();
	        	//finish the table.
	        out.println ("</SELECT>");
	        out.println ("<BR>");
        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Edit----\">");
        	out.println ("</FORM>");

	    } catch (SQLException ex) {
		    // handle any errors
			out.println("<BR><BR>Error in SMEditMechanics!!<BR>");
		    out.println("SQLException: " + ex.getMessage() + "<BR>");
		    out.println("SQLState: " + ex.getSQLState() + "<BR>");
		    out.println("SQL: " + "<BR>");
		}
	    
		out.println("</BODY></HTML>");
	}
}
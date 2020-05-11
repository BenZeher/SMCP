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

import SMDataDefinition.SMTableproposalphrasegroups;
import SMDataDefinition.SMTableproposalphrases;
import ServletUtilities.clsDatabaseFunctions;

public class SMProposalPhrasesSelect extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	private static final String sObjectName = "Proposal Phrase";
	private static final String sCalledClassName = "SMProposalPhrasesEdit";
	
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMEditProposalPhrases //add new rights if necessary
			)
		){
			return;
		}
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "Manage " + sObjectName + "s";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(
	    		title, 
	    		subtitle, 
	    		SMUtilities.getInitBackGroundColor(getServletContext(), sDBID),
	    		sCompanyName));

	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	    
	    
	    out.println("<FORM NAME='MAINFORM' ACTION='" 
	    		+ SMUtilities.getURLLinkBase(getServletContext()) 
	    		+ "smcontrolpanel." + sCalledClassName + "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    String sOutPut = "";
	    
	    //Add drop down list
		try{
	        String sSQL = "SELECT * FROM " 
	        	+ SMTableproposalphrases.TableName
	        	+ " LEFT JOIN " + SMTableproposalphrasegroups.TableName
	        	+ " ON " + SMTableproposalphrases.TableName + "." + SMTableproposalphrases.iphrasegroupid
	        	+ " = " + SMTableproposalphrasegroups.TableName + "." + SMTableproposalphrasegroups.sid 
	        	+ " ORDER BY " + SMTableproposalphrasegroups.TableName + "." + SMTableproposalphrasegroups.sgroupname 
	        	+ ", " + SMTableproposalphrases.TableName + "." + SMTableproposalphrases.sproposalphrasename;
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	     	out.println ("<SELECT NAME=\"" + SMTableproposalphrases.sproposalphrasename + "\">" );
        	
        	while (rs.next()){
        		out.println ("<OPTION VALUE=\"" + rs.getString(SMTableproposalphrases.TableName + "." + SMTableproposalphrases.sid) + "\">"
        			+ rs.getString(SMTableproposalphrasegroups.TableName + "." + SMTableproposalphrasegroups.sgroupname) + " - "
        			+ rs.getString(SMTableproposalphrases.TableName + "." + SMTableproposalphrases.sproposalphrasename)
        		); 
        	}
        	rs.close();
	        	//End the drop down list:
	        out.println ("</SELECT>");
	        out.println ("<BR>");
		}catch (SQLException ex){
			out.println("Error getting list of proposal phrases - " + ex.getMessage());
		}
		sOutPut = "<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Edit Selected " + sObjectName + "'" 
			+ "></P>";
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='SubmitDelete' VALUE='Delete Selected " 
			+ sObjectName + "'>";
		sOutPut = sOutPut + "  Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"ConfirmDelete\">";
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='SubmitAdd' VALUE='Add New " 
			+ sObjectName + "'></P>";
		sOutPut = sOutPut + 
			" New Proposal Phrase To Be Added: <INPUT TYPE=TEXT NAME=\"New" + sObjectName + "\" SIZE=28 MAXLENGTH=" + 
			Integer.toString(SMTableproposalphrases.sproposalphrasenameLength) + 
			" STYLE=\"width: 2.41in; height: 0.25in\"> Proposal phrase name maximum length is " + 
			Integer.toString(SMTableproposalphrases.sproposalphrasenameLength) + ".</P>";
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
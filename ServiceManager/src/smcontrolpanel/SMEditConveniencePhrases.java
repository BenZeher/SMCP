package smcontrolpanel;

import SMDataDefinition.SMTableconveniencephrases;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SMEditConveniencePhrases extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Convenience Phrase";
	private static String sCalledClassName = "SMEditConveniencePhrasesEdit";
	private String sDBID = "";
	private String sCompanyName = "";
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
			request, 
			response, 
			getServletContext(), SMSystemFunctions.SMEditConveniencePhrases)){
			return;
			
		}
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		
	    String title = "Manage " + sObjectName + "s";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	    
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel." + sCalledClassName + "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    String sOutPut = "";
	    
	    //Add drop down list
		try{
	        String sSQL = SMMySQLs.Get_ConveniencePhrase_List_SQL();
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	     	out.println ("<SELECT NAME=\"" + sObjectName + "\">" );
        	
        	while (rs.next()){
        		String sPhrase = "";
        		if (rs.getString(SMTableconveniencephrases.mPhraseText) != null){
        			sPhrase = rs.getString(SMTableconveniencephrases.mPhraseText);
        		}
        		if (sPhrase.length() > 75){
        			sPhrase = clsStringFunctions.StringLeft(sPhrase, 75) + "...";
        		}
        		
        		sOutPut = "<OPTION VALUE=\"" + rs.getString(SMTableconveniencephrases.lPhraseID) + "\">";
        		//Convenience phrases can be very long, so we truncate it to fit in
        		//the list:
        		sOutPut += rs.getString(SMTableconveniencephrases.lPhraseID) +
        		//SPECIAL CODE HERE - trimmed the value of the phrase:
        		" - " + sPhrase;
	        	out.println (sOutPut);
        	}
        	rs.close();
	        	//End the drop down list:
	        out.println ("</SELECT>");
	        out.println ("<BR>");
		}catch (SQLException ex){
	    	System.out.println("Error in " + this.toString() + " class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			//return false;
		}
		//Display text boxes for the new password and a confirmation:
		
		sOutPut = "<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Edit Selected " + sObjectName + "' STYLE='height: 0.24in'></P>";
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='SubmitDelete' VALUE='Delete Selected " + sObjectName + "' STYLE='height: 0.24in'>";
		sOutPut = sOutPut + "  Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"ConfirmDelete\">";
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='SubmitAdd' VALUE='Add New " + sObjectName + "' STYLE='height: 0.24in'></P>";
		sOutPut = sOutPut + 
			" New Convenience Phrase Code To Be Added: <INPUT TYPE=TEXT NAME=\"New" + sObjectName + "\" SIZE=28 MAXLENGTH=" + 
			"4" + 
			" STYLE=\"width: 2.41in; height: 0.25in\"> Code must be integer no greater than 9999" + 
			 ".</P>";
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
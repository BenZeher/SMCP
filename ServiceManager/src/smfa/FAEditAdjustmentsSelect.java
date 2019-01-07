package smfa;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTablefamaster;
import ServletUtilities.clsManageRequestParameters;

public class FAEditAdjustmentsSelect extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Asset adjustment";
	private static String sCalledClassName = "FAEditAdjustmentsEdit";
	private static String sCompanyName = "";
	private String sDBID = "";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.FAEnterAdjustments)){
	    	return;
	    }

	    String title = "Manage " + sObjectName + "s.";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		//If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
	    if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
	    if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>STATUS: " + sStatus + "</B><BR>");
		}
	    
	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Fixed Assets Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICEditItems) 
	    		+ "\">Summary</A><BR><BR>");
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa." + sCalledClassName + "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    String sOutPut = "";
	    
	    String sEditCode = "";
	    if (request.getParameter(FAAsset.ParamAssetNumber) != null){
	    	sEditCode = request.getParameter(FAAsset.ParamAssetNumber);
	    }
	    
		sOutPut = 
			"<P>Enter Asset Number: <INPUT TYPE=TEXT NAME=\"" 
			+ FAAsset.ParamAssetNumber + "\""
			+ " VALUE = \"" + sEditCode + "\""
			+ " SIZE=32 MAXLENGTH=" 
			+ Integer.toString(SMTablefamaster.sAssetNumberLength) 
			+ " STYLE=\"width: 2.41in; height: 0.25in\">&nbsp;";
		
		//Link to finder:
		sOutPut += "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&ObjectName=Asset"
			+ "&ResultClass=FinderResults"
			+ "&SearchingClass=smfa.FAEditAssetsSelect"
			+ "&ReturnField=" + FAAsset.ParamAssetNumber
			+ "&SearchField1=" + SMTablefamaster.sDescription
			+ "&SearchFieldAlias1=Description"
			+ "&SearchField2=" + SMTablefamaster.sAssetNumber
			+ "&SearchFieldAlias2=Asset%20No."
			+ "&ResultListField1="  + SMTablefamaster.sAssetNumber
			+ "&ResultHeading1=Item%20No."
			+ "&ResultListField2="  + SMTablefamaster.sDescription
			+ "&ResultHeading2=Description"
			+ "&ParameterString="
			+ "\"> Find asset</A></P>";
		
		sOutPut += "<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Edit Selected " + sObjectName + "' STYLE='width: 2.00in; height: 0.24in'></P>";
		sOutPut = sOutPut + "</FORM>";
		out.println(sOutPut);
		
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
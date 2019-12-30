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

public class FAEditAssetsSelect extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String sAssetObjectName = "Asset";
	private static final String sFAEditAssestSelectCalledClassName = "FAEditAssetsEdit";

	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.FAManageAssets)){
	    	return;
	    }
	    String title = "Manage " + sAssetObjectName + "s.";
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
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.FAManageAssets) 
	    		+ "\">Summary</A><BR><BR>");
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa." + sFAEditAssestSelectCalledClassName + "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    String sOutPut = "";
	    
	    String sEditCode = "";
	    if (request.getParameter(FAAsset.ParamAssetNumber) != null){
	    	sEditCode = request.getParameter(FAAsset.ParamAssetNumber);
	    }
	    
		sOutPut = 
			"<P>Enter " + sAssetObjectName + " Number: <INPUT TYPE=TEXT NAME=\"" 
			+ FAAsset.ParamAssetNumber + "\""
			+ " VALUE = \"" + sEditCode + "\""
			+ " SIZE=32 MAXLENGTH=" 
			+ Integer.toString(SMTablefamaster.sAssetNumberLength) 
			+ " STYLE=\"width: 2.41in; height: 0.25in\">&nbsp;";
		
		//Link to finder:
		sOutPut += "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "SMClasses.ObjectFinder"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&ObjectName=Asset"
			+ "&ResultClass=FinderResults"
			+ "&SearchingClass=smfa.FAEditAssetsSelect"
			+ "&ReturnField=" + FAAsset.ParamAssetNumber
			+ "&SearchField1=" + SMTablefamaster.sDescription
			+ "&SearchFieldAlias1=Description"
			+ "&SearchField2=" + SMTablefamaster.sAssetNumber
			+ "&SearchFieldAlias2=Asset%20No."
			+ "&SearchField3=" + SMTablefamaster.sTruckNumber
			+ "&SearchFieldAlias3=Truck"
			+ "&SearchField4=" + SMTablefamaster.sGaragedLocation
			+ "&SearchFieldAlias4=Garaged%20Location"
			+ "&SearchField5=" + SMTablefamaster.sLicenseTagNumber
			+ "&SearchFieldAlias5=License"
			+ "&SearchField6=" + SMTablefamaster.sSerialNumber
			+ "&SearchFieldAlias6=Serial%20No."

			+ "&ResultListField1="  + SMTablefamaster.sAssetNumber
			+ "&ResultHeading1=Asset%20No."
			+ "&ResultListField2="  + SMTablefamaster.sDescription
			+ "&ResultHeading2=Description"
			+ "&ResultListField3="  + SMTablefamaster.sTruckNumber
			+ "&ResultHeading3=Truck%20No."
			+ "&ResultListField4="  + SMTablefamaster.sGaragedLocation
			+ "&ResultHeading4=Garaged"
			+ "&ResultListField5="  + SMTablefamaster.sLicenseTagNumber
			+ "&ResultHeading5=License"
			+ "&ResultListField6="  + SMTablefamaster.sSerialNumber
			+ "&ResultHeading6=Serial%20No."
			
			+ "&ParameterString="
			+ "\"> Find asset</A></P>";
		
		sOutPut += "<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Edit/Delete Selected " + sAssetObjectName + "' STYLE='width: 2.00in; height: 0.24in'></P>";
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='SubmitAdd' VALUE='Add New " + sAssetObjectName + "' STYLE='width: 2.00in; height: 0.24in'></P>";
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
package smic;

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
import SMClasses.SMFinderFunctions;
import SMDataDefinition.SMTableicitems;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsManageRequestParameters;

public class ICDisplayItemSelection  extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.ICDisplayItemInformation
			)
		){
			return;
		}
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String title = "View Item Information";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	
	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Control Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICDisplayItemInformation) 
	    		+ "\">Summary</A><BR><BR>");
		out.println ("<FORM NAME=MAINFORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICDisplayItemInformation\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
		out.println("<TABLE WIDTH=100% CELLPADDING=10 border=4>");
		out.println("<TR>");
		
		//Item number:
		out.println("<TD>" + "<B>View item:</B> " 
				+ clsCreateHTMLFormFields.TDTextBox(
					"ItemNumber", 
					clsManageRequestParameters.get_Request_Parameter("ItemNumber", request), 
					SMTableicitems.sItemNumberLength, 
					SMTableicitems.sItemNumberLength, 
					""
					));
		
		//Link to finder:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "SMClasses.ObjectFinder"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&ObjectName=Item"
			+ "&ResultClass=FinderResults"
			+ "&SearchingClass=smic.ICDisplayItemSelection"
			+ "&ReturnField=" + ICItem.ParamItemNumber
			
			+ SMFinderFunctions.getStdITEMSearchAndResultString()
			
			/*
			+ "&SearchField1=" + SMTableicitems.sItemDescription
			+ "&SearchFieldAlias1=Description"
			+ "&SearchField2=" + SMTableicitems.sItemNumber
			+ "&SearchFieldAlias2=Item%20No."
			+ "&SearchField3=" + SMTableicitems.sComment1
			+ "&SearchFieldAlias3=Comment%201"
			+ "&SearchField4=" + SMTableicitems.sComment2
			+ "&SearchFieldAlias4=Comment%202"
			+ "&SearchField5=" + SMTableicitems.sComment3
			+ "&SearchFieldAlias5=Comment%203"
			+ "&ResultListField1="  + SMTableicitems.sItemNumber
			+ "&ResultHeading1=Item%20No."
			+ "&ResultListField2="  + SMTableicitems.sItemDescription
			+ "&ResultHeading2=Description"
			+ "&ResultListField3="  + SMTableicitems.sCostUnitOfMeasure
			+ "&ResultHeading3=Cost%20Unit"
			+ "&ResultListField4="  + SMTableicitems.inonstockitem
			+ "&ResultHeading4=Non-stock?"
			+ "&ResultListField5="  + SMTableicitems.sPickingSequence
			+ "&ResultHeading5=Picking%20Sequence"
			*/
						
			//+ "&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "\"> Find item</A></P>");
		
		out.println("</TR></TABLE>");
		
		out.println("<BR>");
		out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----View----\">");
		out.println("</FORM>");
		//Set the default focus:
		out.println("<script language=\"JavaScript\">");
		out.println("document.MAINFORM.ItemNumber.focus();");
		out.println("</script>");
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

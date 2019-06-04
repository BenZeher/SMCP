package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smar.FinderResults;
import SMClasses.SMOrderHeader;
import SMDataDefinition.*;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class SMWageScaleReportSelect  extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMWageScaleReport))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "Wage Scale Report";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    
	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>Status: " + sStatus + "</B><BR>");
		}
	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMWageScaleReportGenerate\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
		out.println("<TABLE WIDTH=100% CELLPADDING=10 border=4>");

		//Order Number
		out.println("<TR><TD>" + "<B>Order number:</B> " 
				+ "<INPUT TYPE=TEXT ID='OrderNumber' NAME=\"ORDERNUMBER\" VALUE=\"" + clsManageRequestParameters.get_Request_Parameter("ORDERNUMBER", request) + "\""
				+ " class = \"text\""
				+ " style=\"width:100px;\"" 
				+ " MAXLENGTH = 10" 
				+ ">"
				);

		//Link to finder:
		out.println("&nbsp;" + "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
				+ "?ObjectName=Order"
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&ResultClass=FinderResults"
				+ "&SearchingClass=" + "smcontrolpanel.SMEditOrderSelection"
				+ "&ReturnField=" + SMOrderHeader.ParamsOrderNumber
				+ "&SearchField1=" + SMTableorderheaders.sBillToName
				+ "&SearchFieldAlias1=Bill%20To%20Name"
				+ "&SearchField2=" + SMTableorderheaders.sShipToName
				+ "&SearchFieldAlias2=Ship%20To%20Name"
				+ "&SearchField3=" + SMTableorderheaders.sBillToAddressLine1
				+ "&SearchFieldAlias3=Bill%20To%20Address%20Line%201"
				+ "&SearchField4=" + SMTableorderheaders.sShipToAddress1
				+ "&SearchFieldAlias4=Ship%20To%20Address%20Line%201"
				+ "&ResultListField1="  + SMTableorderheaders.sOrderNumber
				+ "&ResultHeading1=Order%20Number"
				+ "&ResultListField2="  + SMTableorderheaders.sBillToName
				+ "&ResultHeading2=Bill%20To%20Name"
				+ "&ResultListField3="  + SMTableorderheaders.sShipToName
				+ "&ResultHeading3=Ship%20To%20Name"
				+ "&ResultListField4="  + SMTableorderheaders.sServiceTypeCodeDescription
				+ "&ResultHeading4=Service%20Type"
				+ "&ResultListField5="  + SMTableorderheaders.sSalesperson
				+ "&ResultHeading5=Salesperson"
				+ "&ResultListField6="  + SMTableorderheaders.datOrderDate
				+ "&ResultHeading6=Order%20Date"
				+ "&ResultListField7="
					+ clsServletUtilities.URLEncode("IF(" + SMTableorderheaders.datOrderCanceledDate + "<'1950-01-01','N/A'," 
					+ "CONCAT('<FONT COLOR=RED>', DATE_FORMAT(" + SMTableorderheaders.datOrderCanceledDate + ",'%c/%e/%Y'), '</FONT>')) AS 'CANCELEDDATE'")
				+ "&" + FinderResults.RESULT_FIELD_ALIAS + "7=CANCELEDDATE"
				+ "&ResultHeading7=Canceled"
				//+ "&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "\"> Find order</A>"

				//Add EXTENDED order find:
				+ "&nbsp;&nbsp;"
				+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
				+ "?ObjectName=" + FinderResults.OBJECT_ORDER_EXTENDED
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&ResultClass=FinderResults"
				+ "&SearchingClass=" + "smcontrolpanel.SMEditOrderSelection"
				+ "&ReturnField=" + SMOrderHeader.ParamsOrderNumber
				+ "&SearchField1=" + SMTableorderheaders.sBillToName
				+ "&SearchFieldAlias1=Bill%20To%20Name"
				+ "&SearchField2=" + SMTableorderheaders.sShipToName
				+ "&SearchFieldAlias2=Ship%20To%20Name"
				+ "&SearchField3=" + SMTableorderheaders.sCustomerCode
				+ "&SearchFieldAlias3=Customer%20Acct."
				+ "&SearchField4=" + FinderResults.COMPLETE_BILL_TO_ADDRESS
				+ "&SearchFieldAlias4=Complete%20Bill%20To%20Address"
				+ "&SearchField5=" + FinderResults.COMPLETE_SHIP_TO_ADDRESS
				+ "&SearchFieldAlias5=Complete%20Ship%20To%20Address"
				+ "&SearchField6=" + SMTableorderheaders.mTicketComments
				+ "&SearchFieldAlias6=Ticket%20Comments"
				+ "&SearchField7=" + SMTableorderheaders.sBillToContact
				+ "&SearchFieldAlias7=Bill%20To%20Contact"
				+ "&SearchField8=" + SMTableorderheaders.sBillToPhone
				+ "&SearchFieldAlias8=Bill%20To%20Phone"
				+ "&SearchField9=" + SMTableorderheaders.sShipToContact
				+ "&SearchFieldAlias9=Ship%20To%20Contact"
				+ "&SearchField10=" + SMTableorderheaders.sShipToPhone
				+ "&SearchFieldAlias10=Ship%20To%20Phone"
				+ "&SearchField11=" + SMTableorderheaders.sPONumber
				+ "&SearchFieldAlias11=PO%20Number"
				+ "&SearchField12=" + SMTableorderheaders.sOrderCreatedByFullName
				+ "&SearchFieldAlias12=Created%20By%20Full%20Name"
				+ "&ResultListField1="  + SMTableorderheaders.sOrderNumber
				+ "&ResultHeading1=Order%20Number"
				+ "&ResultListField2="  + SMTableorderheaders.sBillToName
				+ "&ResultHeading2=Bill%20To%20Name"
				+ "&ResultListField3="  + SMTableorderheaders.sCustomerCode
				+ "&ResultHeading3=Customer%20Acct."
				+ "&ResultListField4="  + "CompleteBillToAddress"
				+ "&ResultHeading4=Bill%20To%20Address"
				+ "&ResultListField5="  + SMTableorderheaders.sShipToName
				+ "&ResultHeading5=Ship%20To%20Name"
				+ "&ResultListField6="  + "CompleteShipToAddress"
				+ "&ResultHeading6=Ship%20To%20Address"
				+ "&ResultListField7="  + SMTableorderheaders.sBillToContact
				+ "&ResultHeading7=Bill%20To%20Contact"
				+ "&ResultListField8="  + SMTableorderheaders.sBillToPhone
				+ "&ResultHeading8=Bill%20To%20Phone"
				+ "&ResultListField9="  + SMTableorderheaders.sShipToContact
				+ "&ResultHeading9=Ship%20To%20Contact"
				+ "&ResultListField10="  + SMTableorderheaders.sShipToPhone
				+ "&ResultHeading10=Ship%20To%20Phone"
				+ "&ResultListField11="  + SMTableorderheaders.sPONumber
				+ "&ResultHeading11=PO%20Number"
				+ "&ResultListField12="  + SMTableorderheaders.mTicketComments
				+ "&ResultHeading12=Ticket%20Comments"
				+ "&ResultListField13="  + SMTableorderheaders.sServiceTypeCodeDescription
				+ "&ResultHeading13=Service%20Type"
				+ "&ResultListField14="  + SMTableorderheaders.sSalesperson
				+ "&ResultHeading14=Salesperson"
				+ "&ResultListField15="  + SMTableorderheaders.datOrderDate
				+ "&ResultHeading15=Order%20Date"
				+ "&ResultListField16="
					+ clsServletUtilities.URLEncode("IF(" + SMTableorderheaders.datOrderCanceledDate + "<'1950-01-01','N/A'," 
							+ "CONCAT('<FONT COLOR=RED>', DATE_FORMAT(" + SMTableorderheaders.datOrderCanceledDate + ",'%c/%e/%Y'), '</FONT>')) AS 'CANCELEDDATE'")
					+ "&" + FinderResults.RESULT_FIELD_ALIAS + "16=CANCELEDDATE"
				+ "&ResultHeading16=Canceled"
				+ "&ResultListField17="  + SMTableorderheaders.sOrderCreatedByFullName
				+ "&ResultHeading15=Created%20By"
				//+ "&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "\"> Find order (extended search)</A>"
				+ "</TD></TR>");

		//Period End Date:
		String sDefaultDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.nowAsSQLDate(), "M/d/yyyy");
		out.println("<TR>");
		out.println("<TD><B>Period End Date:</B>");
		out.println(
			"&nbsp;" 
				+ clsCreateHTMLFormFields.TDTextBox("PeriodEndDate", sDefaultDate, 10, 10, "")
				+ SMUtilities.getDatePickerString("PeriodEndDate", getServletContext())
			
		);
		out.println("</TD>");
		out.println("</TR>");
		
		//Enter Encryption Key:
		out.println("<TR>");
		out.println("<TD><B>Enter Encryption Key:</B>");
		out.println(
			"&nbsp;<INPUT TYPE=TEXT NAME=" + "\"" + SMWageScaleDataEntry.ParamEncryptionKey +"\">");
		out.println("</TD>");
		out.println("</TR>");
		
		
		out.println("</TABLE>");
		
		out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Process report----\">");
		out.println("&nbsp<INPUT TYPE=\"SUBMIT\" NAME=\"" + SMWageScaleDataEntry.DELETE_BUTTON_LABEL 
				+ "\" VALUE=\"" + SMWageScaleDataEntry.DELETE_BUTTON_VALUE + "\">");
		out.println("<INPUT TYPE='CHECKBOX' NAME='" + SMWageScaleDataEntry.CONFIRM_DELETE_CHECKBOX 
		        + "' VALUE='" + SMWageScaleDataEntry.CONFIRM_DELETE_CHECKBOX + "' > Check to confirm before deleting");
		out.println("</FORM>");
	    	
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

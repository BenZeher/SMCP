package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import SMDataDefinition.*;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsManageRequestParameters;

public class SMPrintInvoiceCriteriaSelection extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String sCompanyName = "";
	private String sDBID = "";
	public static final String NOOFINVOICECOPIES_NAME = "NOOFINVOICECOPIES";
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMPrintInvoice
			)
		){
			return;
		}
	    response.setContentType("text/html");
	    
		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String title = "Print Invoice";
	    String subtitle = "";
	    
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

    	//out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMPrintInvoice\">");
    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMPrintInvoice\">");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
    	out.println("<TABLE CELLPADDING=10 BORDER=1>");
    	
    	boolean bPrintMultiple = false;
    	if (clsManageRequestParameters.get_Request_Parameter("PrintMultipleInvoices", request).compareTo("true") == 0){
    		bPrintMultiple = true;
    	}
    	out.println("<TR><TD ALIGN=CENTER><H3>Invoice Number </H3></TD>");
    	out.println("<TD>" + clsCreateHTMLFormFields.TDTextBox(
							"InvoiceNumberFrom", 
							clsManageRequestParameters.get_Request_Parameter("InvoiceNumberFrom", request), 
							10, 
							10, 
							""
							) + "&nbsp;"

    					//Link to finder:
    				    + "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
	    				+ "?ObjectName=Invoice"
	    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    				+ "&ResultClass=FinderResults"
	    				+ "&SearchingClass=" + "smcontrolpanel.SMPrintInvoiceCriteriaSelection"
	    				+ "&ReturnField=" + "InvoiceNumberFrom"
	    				+ "&SearchField1=" + SMTableinvoiceheaders.sBillToName
	    				+ "&SearchFieldAlias1=Bill%20To%20Name"
	    				+ "&SearchField2=" + SMTableinvoiceheaders.sShipToName
	    				+ "&SearchFieldAlias2=Ship%20To%20Name"
	    				+ "&SearchField3=" + SMTableinvoiceheaders.sBillToAddressLine1
	    				+ "&SearchFieldAlias3=Bill%20To%20Address%20Line%201"
	    				+ "&SearchField4=" + SMTableinvoiceheaders.sShipToAddress1
	    				+ "&SearchFieldAlias4=Ship%20To%20Address%20Line%201"
	    				+ "&SearchField5=" + SMTableinvoiceheaders.strimmedordernumber
	    				+ "&SearchFieldAlias5=Order%20Number"
	    				+ "&ResultListField1="  + SMTableinvoiceheaders.sInvoiceNumber
	    				+ "&ResultHeading1=Invoice%20Number"
	    				+ "&ResultListField2="  + SMTableinvoiceheaders.sBillToName
	    				+ "&ResultHeading2=Bill%20To%20Name"
	    				+ "&ResultListField3="  + SMTableinvoiceheaders.sShipToName
	    				+ "&ResultHeading3=Ship%20To%20Name"
	    				+ "&ResultListField4="  + SMTableinvoiceheaders.strimmedordernumber
	    				+ "&ResultHeading4=Order%20Number"
	    				+ "&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    				+ "\"> Find invoice</A><HR>" +
	    				clsCreateHTMLFormFields.TDCheckBox("PrintMultipleInvoices", 
	    									   bPrintMultiple, 
	    									   "Print a range of invoices starting with the invoice above, and going through the invoice number below:") + 
	    				clsCreateHTMLFormFields.TDTextBox(
								"InvoiceNumberTo", 
								clsManageRequestParameters.get_Request_Parameter("InvoiceNumberTo", request), 
								10, 
								10, 
								""
								) +
    			
    	"</TD></TR>");

    	out.println("<TR><TD ALIGN=CENTER><H3>Options </H3></TD>");
    	out.println("<TD>");
    	//Create a drop down for number of copies':
    	out.println("Number of copies: <SELECT "
						+ " NAME = \"" + NOOFINVOICECOPIES_NAME + "\""
						+ " ID = \"" + NOOFINVOICECOPIES_NAME + "\""
						+ ">");
		
		for (long l = 0; l < 5; l++){
			out.println("<OPTION VALUE = \"" + Long.toString(l + 1) + "\""
						+ ">" + Long.toString(l + 1)
						+ "</OPTION>");
		}
		
		out.println("</SELECT>");
		out.println("<HR>");
    	out.println("<INPUT TYPE=\"CHECKBOX\" NAME=\"ShowExtendedPriceForEachItem\" VALUE=1>Show extended price for each item<BR>" + 
					"<INPUT TYPE=\"CHECKBOX\" NAME=\"ShowLaborAndMaterialSubtotals\" VALUE=1>Show labor and material subtotals<BR>" +
					"<INPUT TYPE=\"CHECKBOX\" NAME=\"ShowTaxBreakdown\" VALUE=1>Show tax breakdown<BR>" +
					"<INPUT TYPE=\"CHECKBOX\" NAME=\"ShowALLItemsOnInvoiceIncludingDNP\" VALUE=1>Show all items on invoice (including 'DNP' items)<BR>" +
					"<INPUT TYPE=\"CHECKBOX\" NAME=\"SuppressDetailsPageBreak\" VALUE=1>Start details immediately below totals (no page break before the details)<BR>" +
    				"</TD></TR>");
    	out.println("</TABLE>");
    	
    	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Show----\">");
    	out.println ("</FORM>");
 
	    out.println("</BODY></HTML>");
	}
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		doGet(request, response);
	}
	
}
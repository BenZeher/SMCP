package smcontrolpanel;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smar.ARCustomer;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTableorderheaders;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;

public class SMChangeCustomerEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String COPYBILLTO_CHECKBOX = "COPYBILLTOCHECKBOX";
	public static final String STRIMMEDORDERNUMBER_PARAM = "STRIMMEDORDERNUMBER";
	public static final String CUSTOMERNUMBER_PARAM = "CUSTOMERNUMBER";
	public static final String CHANGECUSTOMER_COMMAND = "CHANGECUSTOMERCOMMAND";
	private boolean bDebugMode = false;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		//PrintWriter debug_out = new PrintWriter(System.out);
		//SMUtilities.printRequestParameters(debug_out, request);
		
		SMOrderHeader order = new SMOrderHeader(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				"customer on sales order",
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMEditOrderAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMChangeCustomerOnOrders
				);
		
		if (!smedit.processSession(getServletContext(), -1)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		//If this is a 'resubmit', meaning it's being called by another class, then
		//the session will have an entry object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		currentSession.removeAttribute(SMOrderHeader.ParamObjectName);
	    
	    smedit.printHeaderTable();
		
		//Add a link to go back to the order:
		smedit.getPWOut().println(
				"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
			+ "smcontrolpanel.SMEditOrderEdit"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			+ "&" + SMTableorderheaders.strimmedordernumber + "=" 
			+ clsManageRequestParameters.get_Request_Parameter(STRIMMEDORDERNUMBER_PARAM, smedit.getRequest())
				+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString()) + "\">" + "Return to order header" + "</A>"
		);
		
		smedit.setbIncludeUpdateButton(true);
		smedit.setbIncludeDeleteButton(false);
		if (bDebugMode){
			System.out.println("In " + this.toString() + " just before createEditPage.");
		}
	    try {
			smedit.createEditPage(getEditHTML(smedit, order), "");
		} catch (SQLException e) {
			//Put the order header back into the session:
			currentSession.setAttribute(SMOrderHeader.ParamObjectName, order);
    		String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smcontrolpanel.SMEditOrderEdit"
				+ "?Warning=Error building customer change screen - " + sError
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
				return;
		}
		if (bDebugMode){
			System.out.println("In " + this.toString() + " just after createEditPage.");
		}
	    return;
	}
	private String getEditHTML(
			SMMasterEditEntry sm, 
			SMOrderHeader order
	) throws SQLException{
		String s = "";
		s += "<BR><B>NOTE:</B> Changing the customer on this order will NOT change any of the other order information.  "
			+ "You CAN choose to copy the customer's bill to information over the bill to information on the order by"
			+ " checking below, but you will have to check and update all of the other order information manually.<BR>"
		;

		//Store the order number:
		//Store whether or not the actual proposal data has been changed - this doesn't include the approval status:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + STRIMMEDORDERNUMBER_PARAM + "\" VALUE=\"" 
				+ clsManageRequestParameters.get_Request_Parameter(STRIMMEDORDERNUMBER_PARAM, sm.getRequest()) + "\"" + ">";
		//This will tell the called class where we are coming from:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + CHANGECUSTOMER_COMMAND + "\" VALUE=\"" + "Y" + "\"" + ">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "\" VALUE=\"" + sm.getsDBID() + "\"" + ">";
		
        s += "<BR><B>Change to customer:</B>"
        	+ "<INPUT TYPE=TEXT NAME=\"" + CUSTOMERNUMBER_PARAM + "\""
	    	+ " VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(CUSTOMERNUMBER_PARAM, sm.getRequest()) + "\""
	    	+ " SIZE=" + "15"
	    	+ " MAXLENGTH=" + SMTablearcustomer.sCustomerNumberLength
	    	+ ">"
    	;
        
		//Link to finder:
		s += "&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID()
			+ "&ObjectName=Customer"
			+ "&ResultClass=FinderResults"
			+ "&SearchingClass=" + SMUtilities.getFullClassName(this.toString())
			+ "&ReturnField=" + CUSTOMERNUMBER_PARAM
			+ "&SearchField1=" + SMTablearcustomer.sCustomerName
			+ "&SearchFieldAlias1=Name"
			+ "&SearchField2=" + SMTablearcustomer.sCustomerNumber
			+ "&SearchFieldAlias2=Customer%20Code"
			+ "&SearchField3=" + SMTablearcustomer.sAddressLine1
			+ "&SearchFieldAlias3=Address%20Line%201"
			+ "&SearchField4=" + SMTablearcustomer.sPhoneNumber
			+ "&SearchFieldAlias4=Phone"
			+ "&ResultListField1="  + SMTablearcustomer.sCustomerNumber
			+ "&ResultHeading1=Customer%20Number"
			+ "&ResultListField2="  + SMTablearcustomer.sCustomerName
			+ "&ResultHeading2=Customer%20Name"
			+ "&ResultListField3="  + SMTablearcustomer.sAddressLine1
			+ "&ResultHeading3=Address%20Line%201"
			+ "&ResultListField4="  + SMTablearcustomer.sPhoneNumber
			+ "&ResultHeading4=Phone"
			+ "&ResultListField5="  + SMTablearcustomer.iActive
			+ "&ResultHeading5=Active"
			+ "&ResultListField6="  + SMTablearcustomer.iOnHold
			+ "&ResultHeading6=On%20Hold"
			+ "&ResultListField7="  + SMTablearcustomer.sCustomerGroup
			+ "&ResultHeading7=Customer%20Group"
			//+ "&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID()
			+ "&ParameterString=*" + STRIMMEDORDERNUMBER_PARAM + "=" + clsManageRequestParameters.get_Request_Parameter(STRIMMEDORDERNUMBER_PARAM, sm.getRequest())
			+ "\"> Find customer</A>"
			;
		
		//Add a link to add a new customer, if the user has permission:
		if(SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.AREditCustomers, 
			sm.getUserID(),
			getServletContext(),
			sm.getsDBID(),
			(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
			s += "&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smar.AREditCustomersEdit?" + ARCustomer.ParamsCustomerNumber + "=''" 
				+ "&SubmitAdd=Y"
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID() + "\">" + "Add a new customer" + "</A>"
			;
		}
		s += "</P>";
        
		String sCheckBoxChecked = "";
		if (sm.getRequest().getParameter(COPYBILLTO_CHECKBOX) != null){
			sCheckBoxChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}
		s += ""
				+ " Copy customer's default bill-to information to order?&nbsp;"
				+ "<INPUT TYPE=CHECKBOX "
				+ sCheckBoxChecked
				+ " NAME=\"" + COPYBILLTO_CHECKBOX + "\""
				+ " id = \"" + COPYBILLTO_CHECKBOX + "\""
				+ " width=0.25>"
				;
        
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

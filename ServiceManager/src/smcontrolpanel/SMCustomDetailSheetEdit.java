package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.SMOrderHeader;
import SMClasses.SMWorkOrderHeader;
import SMDataDefinition.SMTableworkorderdetailsheets;
import SMDataDefinition.SMTableworkorders;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMCustomDetailSheetEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String CALLED_CLASS_NAME = "SMCustomDetailSheetAction";
	public static final String SAVE_BUTTON_NAME = "SAVEBUTTON";
	public static final String SAVE_BUTTON_LABEL = "Save";
	public static final String CANCEL_BUTTON_NAME = "CANCELBUTTON";
	public static final String CANCEL_BUTTON_LABEL = "Cancel";
	public static final String VIEW_RESULTS_BUTTON_NAME = "VIEWRESULTSBUTTON";
	public static final String VIEW_RESULTS_BUTTON_LABEL = "View results text";
	public static final String PARAM_DETAIL_SHEET_NAME = "DETAILSHEETNAME";
	
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMEditWorkOrders
		)
		){
			return;
		}

		String sDetailSheetID = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderHeader.ADD_DETAIL_SHEET_DROPDOWN_NAME, request);
		String sWorkOrderID = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderHeader.Paramlid, request);
		String sCalledClassName = CALLED_CLASS_NAME;
		String sBillToName = "Some Customer";
		String sShipToName = "Some Address";
		boolean bTestOnly = sWorkOrderID.compareToIgnoreCase("") == 0;
		//if (bTestOnly){
		//	sCalledClassName = SMUtilities.get_Request_Parameter("CallingClass", request);
		//	sWorkOrderID = "(TEST)";
		//}
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), -1, request)){return;}

		String sViewPricingFlagValue = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.VIEW_PRICING_FLAG, request);
		String sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + clsManageRequestParameters.get_Request_Parameter("CallingClass", request)
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&" + SMWorkOrderHeader.Paramlid + "=" + sWorkOrderID
				+ "&" + SMWorkOrderEdit.VIEW_PRICING_FLAG + "=" + sViewPricingFlagValue
				//In case it's being called from the Detail Sheet edit as a 'test':
				+ "&" + SMDetailSheetEdit.DETAIL_SHEET_ID + "=" + sDetailSheetID
			;
		
		SMWorkOrderHeader workorder = null;
		SMOrderHeader order = null;
		if (!bTestOnly){
			
			workorder = new SMWorkOrderHeader();
			workorder.setlid(sWorkOrderID);
			try {
				if (!workorder.load(smaction.getsDBID(), smaction.getUserName(), getServletContext())){
					sRedirectString += "&Warning=Error [1436964161] loading work order #: '" + sWorkOrderID + "' - " + workorder.getErrorMessages() + "."; 
					redirectProcess(sRedirectString, response);
					return;
				}
			} catch (Exception e1) {
				sRedirectString += "&Warning=Error [1436964162] loading work order #: '" + sWorkOrderID + "' - " + e1.getMessage() + "."; 
				redirectProcess(sRedirectString, response);
				return;
			}
			
			order = new SMOrderHeader();
			order.setM_strimmedordernumber(workorder.getstrimmedordernumber());
			if (!order.load(getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName())){
				sRedirectString += "&Warning=Error [1436964164] loading order #: '" + workorder.getstrimmedordernumber() + "' - " + order.getErrorMessages() + "."; 
				redirectProcess(sRedirectString, response);
				return;
			}
			sBillToName = order.getM_sBillToName();
			sShipToName = order.getM_sShipToName();
		}
		
		try {
			smaction.getCurrentSession().removeAttribute(SMTableworkorders.ObjectName);
		} catch (Exception e2) {
			clsServletUtilities.sysprint(this.toString(), smaction.getUserName(), "Error [1436908311]  - " + e2.getMessage() + ".");
		}

		//Get the detail sheet record:
		String sDetailSheetName = "";
		String sDesc = "";
		String sText = "";
		String SQL = "SELECT"
			+ " * FROM " + SMTableworkorderdetailsheets.TableName
			+ " WHERE ("
				+ "(" + SMTableworkorderdetailsheets.lid + " = " + sDetailSheetID + ")"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				smaction.getsDBID(), 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() + ".get detail sheet record - user: " + smaction.getUserID()
				+ " - "
				+ smaction.getFullUserName()));
			
			if (rs.next()){
				sDetailSheetName = rs.getString(SMTableworkorderdetailsheets.sname);
				sDesc = rs.getString(SMTableworkorderdetailsheets.sdescription);
				sText = rs.getString(SMTableworkorderdetailsheets.mtext);
			}else{
				sRedirectString += "&Warning=Error - no detail sheet with ID: '" + sDetailSheetID + "'."; 
				redirectProcess(sRedirectString, response);
				return;
			}
			rs.close();
		} catch (SQLException e) {
			sRedirectString += "&Warning=Error [1436964160] reading detail sheet with SQL: '" + SQL + "' - " + e.getMessage() + "."; 
			redirectProcess(sRedirectString, response);
			return;
		}
		
		String title = sDetailSheetName;
		String subtitle = sDesc;
		
		out.println(SMUtilities.SMCPTitleSubBGColor(
			title, subtitle, SMUtilities.getInitBackGroundColor(
				getServletContext(), 
				smaction.getsDBID()), 
				(String) smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME)
			)
		);

		//Print a link to the first page after login:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID() 
			+ "\">Return to user login</A><BR><BR>");

		out.println("Work order #: " + sWorkOrderID + "&nbsp;&nbsp;" + sBillToName + " - " + sShipToName);
		
		Edit_Record(sDetailSheetID, sDetailSheetName, sText, sViewPricingFlagValue, workorder, out, smaction, sCalledClassName, bTestOnly);
		out.println("</BODY></HTML>");
		
	}

	private void Edit_Record(
			String sDetailSheetID,
			String sDetailSheetName,
			String sDetailSheetText,
			String sViewPricingFlag, 
			SMWorkOrderHeader wo, 
			PrintWriter pwOut,
			SMMasterEditAction smaction,
			String sCalledClassName,
			boolean bTestOnly){

		pwOut.println("<FORM NAME='MAINFORM' ACTION='" 
				+ SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel." + sCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + smaction.getsDBID() + "'>");
		if (!bTestOnly){
			pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMWorkOrderHeader.Paramlid + "' VALUE='" + wo.getlid() + "'>");
			pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMWorkOrderEdit.VIEW_PRICING_FLAG + "' VALUE='" + sViewPricingFlag + "'>");
			pwOut.println("<INPUT TYPE=HIDDEN NAME='" + PARAM_DETAIL_SHEET_NAME + "' VALUE='" + sDetailSheetName + "'>");
		}else{
			pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMDetailSheetEdit.DETAIL_SHEET_ID + "' VALUE='" + sDetailSheetID + "'>");
			pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMDetailSheetEdit.BUTTON_SUBMIT_EDIT + "' VALUE='" + "Y" + "'>");
			pwOut.println("<INPUT TYPE=HIDDEN NAME='" + PARAM_DETAIL_SHEET_NAME + "' VALUE='" + sDetailSheetName + "'>");
		}
		pwOut.println("<BR>");
		
		//Display the detail sheet text, which consists of various HTML fields:
		pwOut.println(sDetailSheetText);
		pwOut.println("<BR><P>");
		if (bTestOnly){
			pwOut.println("&nbsp;&nbsp;");
			pwOut.println("<INPUT TYPE=SUBMIT NAME='" + VIEW_RESULTS_BUTTON_NAME + "' VALUE='" + VIEW_RESULTS_BUTTON_LABEL + "' STYLE='height: 0.24in'>");
		}else{
			pwOut.println("<INPUT TYPE=SUBMIT NAME='" + SAVE_BUTTON_NAME + "' VALUE='" + SAVE_BUTTON_LABEL + "' STYLE='height: 0.24in'>");
			pwOut.println("&nbsp;&nbsp;");
			pwOut.println("<INPUT TYPE=SUBMIT NAME='" + CANCEL_BUTTON_NAME + "' VALUE='" + CANCEL_BUTTON_LABEL + "' STYLE='height: 0.24in'>");			
		}
		pwOut.println("</P></FORM>");
	}

	private void redirectProcess(String sRedirectString, HttpServletResponse res ){
		try {
			res.sendRedirect(sRedirectString);
		} catch (IOException e1) {
			System.out.println("Error [1395238124] in " + this.toString() + ".redirectAction - IOException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
			return;
		} catch (IllegalStateException e1) {
			System.out.println("Error [1395238125] in " + this.toString() + ".redirectAction - IllegalStateException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
			return;
		}
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}

package smcontrolpanel;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smap.APVendor;
import smic.ICPOHeader;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTableicpolines;
import SMDataDefinition.SMTableorderheaders;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMCreatePOFromOrderEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String FOUND_VENDOR_PARAMETER = "FOUND" + ICPOHeader.Paramsvendor;
	public static final String FIND_VENDOR_BUTTON_NAME = "FINDVENDOR";
	public static final String PARAM_ORDERNUMBER = "ORDERNUMBER";
	public static final String PARAM_DETAILNUMBERSTRING = "DETAILNUMBERSTRING";
	public static final String PARAM_DETAILNUMBERDELIMITER = ",";
	public static final String PARAM_VENDORCODE = "VENDORCODE";
	public static final String PARAM_VENDORNAME = "VENDORNAME";
	public static final String PARAM_EXPECTEDSHIPDATE = "EXPECTEDSHIPDATE";
	public static final String PARAM_ITEMNUMBERSTUB = "ITEMNUMBERSTUB";
	public static final String PARAM_ITEMDESCSTUB = "ITEMDESCSTUB";
	public static final String PARAM_UNITCOSTSTUB = "UNITCOSTSTUB";
	public static final String PARAM_UNITOFMEASURESTUB = "UMSTUB";
	public static final String PARAM_QUANTITYSTUB = "QTYSTUB";

	private boolean bDebugMode = false;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		//PrintWriter debug_out = new PrintWriter(System.out);
		//SMUtilities.printRequestParameters(debug_out, request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				"New Purchase Order",
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMCreatePOFromOrderAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.ICEditPurchaseOrders
		);

		String sCurrentCompleteURL = clsServletUtilities.URLEncode(request.getRequestURI().toString() 
				+ clsManageRequestParameters.getQueryStringFromPost(request)).replace("&", "*");
		if (bDebugMode){
			System.out.println("In " + this.toString() 
					+ "sCurrentCompleteURL = " + sCurrentCompleteURL);
		}

		if (!smedit.processSession(getServletContext(), SMSystemFunctions.ICEditPurchaseOrders)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		SMCreatePO createpo = (SMCreatePO)smedit.getCurrentSession().getAttribute(SMCreatePO.CREATE_PO_OBJECT_NAME);
		smedit.getCurrentSession().removeAttribute(SMCreatePO.CREATE_PO_OBJECT_NAME);
		String m_sTrimmedOrderNumber = "";
		String m_sDetailNumberString  = "";
		
		if (createpo == null){
			createpo = new SMCreatePO();
		//	Get the order number and list of detail numbers, if they are passed in:
			m_sTrimmedOrderNumber = clsManageRequestParameters.get_Request_Parameter(PARAM_ORDERNUMBER, request);
			m_sDetailNumberString  = clsManageRequestParameters.get_Request_Parameter(PARAM_DETAILNUMBERSTRING, request);
			//Try to read the order:
			SMOrderHeader order = new SMOrderHeader();
			order.setM_strimmedordernumber(m_sTrimmedOrderNumber);
			if (!order.load(getServletContext(), smedit.getsDBID(), smedit.getUserID(), smedit.getFullUserName())){
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
						+ "?" + SMTableorderheaders.strimmedordernumber + "=" + m_sTrimmedOrderNumber
						+ "&Warning=" + order.getErrorMessages()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
				);
				return;
			}
			createpo.setOrderNumber(m_sTrimmedOrderNumber);
			createpo.setDetailNumberListString(m_sDetailNumberString);
			//Get the lines from the order that we need:
			String[] sDetailNumbers = new String[0];
			sDetailNumbers = m_sDetailNumberString.split(PARAM_DETAILNUMBERDELIMITER);
			for (int i = 0; i < sDetailNumbers.length; i++){
				for (int j = 0; j < order.getM_arrOrderDetails().size(); j++){
					if (order.getM_arrOrderDetails().get(j).getM_iDetailNumber().compareToIgnoreCase(sDetailNumbers[i]) == 0){
						createpo.addItemNumber(order.getM_arrOrderDetails().get(j).getM_sItemNumber());
						createpo.addItemDescription(order.getM_arrOrderDetails().get(j).getM_sItemDesc());
						createpo.addUnitCost(order.getM_arrOrderDetails().get(j).getM_bdEstimatedUnitCost());
						createpo.addUnitOfMeasure(order.getM_arrOrderDetails().get(j).getM_sOrderUnitOfMeasure());
						createpo.addQuantity(order.getM_arrOrderDetails().get(j).getM_dQtyOrdered());
					}
				}
			}
		}	
		
		//If we are returning from finding a vendor, update that vendor and vendor info:
		if (request.getParameter(FOUND_VENDOR_PARAMETER) != null){
			createpo.setVendorCode(request.getParameter(FOUND_VENDOR_PARAMETER));
			APVendor ven = new APVendor();
			ven.setsvendoracct(createpo.getVendorCode());
			if (!ven.load(getServletContext(), smedit.getsDBID(), smedit.getUserID(), smedit.getFullUserName())){
				//Don't choke on this . . . 
			}
			createpo.setVendorName(ven.getsname());
		}

		smedit.printHeaderTable();
		smedit.setbIncludeDeleteButton(false);
		smedit.setIncludeSMCP_CSS_Script(true);
		smedit.setUpdateButtonLabel("Create Purchase Order");
		
		//Add a link to go back to the order:
		smedit.getPWOut().println(
				"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
			+ "smcontrolpanel.SMOrderDetailList"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			+ "&" + SMTableorderheaders.strimmedordernumber + "=" + createpo.getOrderNumber()
				+ "&CallingClass=smcontrolpanel.SMCreatePOFromOrder" + "\">" + "Return to order details" + "</A>"
		);
		
		try {
			smedit.createEditPage(getEditHTML(smedit, createpo), "");
		} catch (SQLException e) {
			String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
					+ "?" + SMTableorderheaders.strimmedordernumber + "=" + createpo.getOrderNumber()
					+ "&Warning=" + sError
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
			return;
		}
		return;
	}
	private String getEditHTML(SMMasterEditEntry sm, SMCreatePO protopo) throws SQLException{

		String s = "<INPUT TYPE=HIDDEN NAME=\"" + PARAM_ORDERNUMBER 
			+ "\" VALUE=\"" + protopo.getOrderNumber().trim().replace("\"", "&quot;") + "\">"
			+ "<INPUT TYPE=HIDDEN NAME=\"" + PARAM_DETAILNUMBERSTRING 
			+ "\" VALUE=\"" + protopo.getDetailNumberListString().trim().replace("\"", "&quot;") + "\">";

		s += "<B>Vendor number:</B>&nbsp;"
		+ "<INPUT TYPE=TEXT NAME=\"" + PARAM_VENDORCODE + "\""
		+ " VALUE=\"" + protopo.getVendorCode().replace("\"", "&quot;") + "\""
		+ " SIZE=" + "20"
		+ " MAXLENGTH=" + Integer.toString(SMTableicpoheaders.svendorLength)
		+ ">"
		;
		s += "<INPUT TYPE=SUBMIT NAME='" + FIND_VENDOR_BUTTON_NAME + "'" 
		+ " VALUE='Find vendor'" 
		+ " STYLE='height: 0.24in'>"
		;			

		s += "&nbsp;&nbsp;<B>Vendor name:</B>&nbsp;" + protopo.getVendorName().replace("\"", "&quot;");
		
		s += "<BR><B>Expected ship date:</B>&nbsp;<INPUT TYPE=TEXT NAME=\"" + PARAM_EXPECTEDSHIPDATE + "\""
		+ " VALUE=\"" + protopo.getExpectedShipDate() + "\""
		+ " SIZE=10"
		+ " MAXLENGTH=10"
		+ " STYLE=\"width: " + ".75" + " in; height: 0.25in\""
		+ ">"
		+ SMUtilities.getDatePickerString(PARAM_EXPECTEDSHIPDATE, getServletContext())
		;

		//Start a table for the po lines:
		//s += "<table class=noborder WIDTH=100%>";
		s += "<table class=noborder>";
		s += "<tr>";
		s += "<tr class=dark>" +
				"<th ALIGN=LEFT VALIGN=VTOP><B>Item #</B></th>" +
				"<th ALIGN=LEFT VALIGN=VTOP><B>Quantity</B></th>" +
				"<th ALIGN=LEFT VALIGN=VTOP><B>Description</B></th>" +
				"<th ALIGN=LEFT VALIGN=VTOP><B>U/M</B></th>" +
				"<th ALIGN=LEFT VALIGN=VTOP><B>Unit cost</B></th>" +
			"</tr>";
		
		//Load all the lines here:
		for (int i = 0; i < protopo.getNumberOfLines(); i++){
			s += "<tr class=normal>";
			s += "<td class=normal>" 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + PARAM_ITEMNUMBERSTUB + clsStringFunctions.PadLeft(Integer.toString(i), "0", 4) 
				+ "\" VALUE=\"" + protopo.getItemNumber(i).replace("\"", "&quot;") + "\">"
				+ "<B>" + protopo.getItemNumber(i) + "</B>"
				+ "</td>";
			
			s += "<td class=normal>" 
					+ "<INPUT TYPE=TEXT NAME=\"" + PARAM_QUANTITYSTUB + clsStringFunctions.PadLeft(Integer.toString(i), "0", 4) + "\""
					+ " VALUE=\"" + protopo.getQuantity(i).replace("\"", "&quot;") + "\""
					+ " SIZE=" + "10"
					+ " MAXLENGTH=" + "17"
					+ ">"
					+ "</td>";
			
			s += "<td class=normal>" 
					+ "<INPUT TYPE=TEXT NAME=\"" + PARAM_ITEMDESCSTUB + clsStringFunctions.PadLeft(Integer.toString(i), "0", 4) + "\""
					+ " VALUE=\"" + protopo.getItemDescription(i).replace("\"", "&quot;") + "\""
					+ " SIZE=" + "40"
					+ " MAXLENGTH=" + Integer.toString(SMTableicpolines.sitemdescriptionLength)
					+ ">"
					+ "</td>";

			s += "<td class=normal>" 
					+ "<INPUT TYPE=TEXT NAME=\"" + PARAM_UNITOFMEASURESTUB + clsStringFunctions.PadLeft(Integer.toString(i), "0", 4) + "\""
					+ " VALUE=\"" + protopo.getUnitOfMeasure(i).replace("\"", "&quot;") + "\""
					+ " SIZE=" + "10"
					+ " MAXLENGTH=" + Integer.toString(SMTableicpolines.sunitofmeasureLength)
					+ ">"
					+ "</td>";

			s += "<td class=normal>" 
					+ "<INPUT TYPE=TEXT NAME=\"" + PARAM_UNITCOSTSTUB + clsStringFunctions.PadLeft(Integer.toString(i), "0", 4) + "\""
					+ " VALUE=\"" + protopo.getUnitCost(i).replace("\"", "&quot;") + "\""
					+ " SIZE=" + "10"
					+ " MAXLENGTH=" + "17"
					+ ">"
					+ "</td>";

			s += "</tr>";
		}

		s += "</table>";
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}

package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMOrderHeader;
import SMDataDefinition.SMTablechangeorders;
import SMDataDefinition.SMTableorderheaders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMEditChangeOrdersEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static String NUMBER_OF_LINES = "NUMBEROFLINES";
	public static String CHANGE_ORDER_ARRAY_ATTRIBUTE = "ChangeOrderArray";
	public static String CO_LINE_PARAM_PREFIX = "COLine";
	public static String UPDATE_BUTTON_NAME = "UPDATE";
	public static String UPDATE_BUTTON_LABEL = "Update";
	public static String DELETE_BUTTON_NAME_PREFIX = "Delete";
	public static String DELETE_BUTTON_LABEL = "Delete";
	public static String DELETE_CONFIRM_CHECKBOX_NAME = "CONFIRMDELETE";
	private static String sCompanyName;
	private static String sDBID;
	private static String sUserName;
	private static String sUserID;
	private static String sUserFullName;
	private ArrayList<SMChangeOrder> arrChangeOrders;

	@SuppressWarnings("unchecked")
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditChangeOrders)){

			return;
		}

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
		sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
						+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);

		String sOrderNumber = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsOrderNumber, request);
		String title = "Edit Change Orders";
		String subtitle = "";
		PrintWriter out = response.getWriter();
		out.println(SMUtilities.SMCPTitleSubBGColor(
				title, 
				subtitle, 
				SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), 
				sCompanyName)
		);
		out.println(SMUtilities.getDatePickerIncludeString (getServletContext()));
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>Status: " + sStatus + "</B><BR>");
		}
		//Print a link to the first page after login:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");

		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMURLHistoryList?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ sDBID + "\">Return to...</A><BR>");

		//Print a link to the order editing menu:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMEditOrderSelection?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "&" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber 
				+ "\">Edit order " + sOrderNumber + "</A>");

		//Print a link to the display order function:
		out.println("&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMDisplayOrderInformation?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "&" + "OrderNumber" + "=" + sOrderNumber 
				+ "\">View order " + sOrderNumber + "</A><BR>");

		out.println ("<FORM NAME=MAINFORM ACTION =\"" 
				+ SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMEditChangeOrdersAction\" METHOD=\"POST\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");

		//If there is an array of change orders in the session, grab that, otherwise, read them into the array:
		arrChangeOrders = (ArrayList<SMChangeOrder>)CurrentSession.getAttribute(CHANGE_ORDER_ARRAY_ATTRIBUTE);
		//Make sure we get rid of the session object here:
		CurrentSession.removeAttribute(CHANGE_ORDER_ARRAY_ATTRIBUTE);
		//But if there is NO array list in the session, we'll have to read the change orders into our array:
		if (arrChangeOrders == null){
			arrChangeOrders = new ArrayList<SMChangeOrder>(0);
			try {
				loadChangeOrders(sOrderNumber, sDBID, sUserName);
			} catch (SQLException e) {
				out.println(e.getMessage());
				return;
			}
		}

		out.println(displayContractData(sOrderNumber, sDBID, sUserName));
		
		out.println(listChangeOrderLines(sOrderNumber));

		out.println("<P><INPUT TYPE=SUBMIT NAME='" + UPDATE_BUTTON_NAME
				+ "' VALUE='" + UPDATE_BUTTON_LABEL + "' STYLE='height: 0.24in'>");
		out.println("</FORM>");
		out.println("</BODY></HTML>");
		return;
	}
	private void loadChangeOrders(
			String sOrderNum, 
			String sConf, 
			String sUser) throws SQLException{

		//arrChangeOrders.clear();
		String SQL = "SELECT"
			+ " * FROM " + SMTablechangeorders.TableName
			+ " WHERE ("
			+ "(" + SMTablechangeorders.sJobNumber + " = '" + sOrderNum + "')"
			+ ")"
			+ " ORDER BY " + SMTablechangeorders.dChangeOrderNumber
			;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sConf, 
					"MySQL",
					this.toString() + ".loadChangeOrders - user: " + sUserID
					+ " - "
					+ sUserFullName
			);
			while (rs.next()){
				SMChangeOrder co = new SMChangeOrder();
				co.setM_dAmount(clsManageBigDecimals.doubleToDecimalFormat(
						rs.getDouble(SMTablechangeorders.dAmount.replace("`", "")), SMTablechangeorders.dAmountScale));
				co.setM_datChangeOrderDate(clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTablechangeorders.datChangeOrderDate.replace("`", ""))));
				co.setM_dChangeOrderNumber(clsManageBigDecimals.doubleToDecimalFormat(
						rs.getDouble(SMTablechangeorders.dChangeOrderNumber.replace("`", "")), 0));
				co.setM_dTotalMarkUp(clsManageBigDecimals.doubleToDecimalFormat(
						rs.getDouble(SMTablechangeorders.dTotalMarkUp.replace("`", "")), SMTablechangeorders.dTotalMarkUPScale));
				co.setM_dTruckDays(clsManageBigDecimals.doubleToDecimalFormat(
						rs.getDouble(SMTablechangeorders.dTruckDays.replace("`", "")), SMTablechangeorders.dTruckDaysScale));
				co.setM_iID(Long.toString(rs.getLong(SMTablechangeorders.iID.replace("`", ""))));
				co.setM_sDesc(rs.getString(SMTablechangeorders.sDesc.replace("`", "")));
				co.setM_sJobNumber(sOrderNum);
				arrChangeOrders.add(co);
			}
			rs.close();
		} catch (SQLException e) {
			throw new SQLException("Error reading change orders - " + e.getMessage());
		}
		//Now add one more for the 'blank' line:
		SMChangeOrder co = new SMChangeOrder();
		//co.setM_iID(SMUtilities.PadLeft("", "0", 6));
		co.setM_sJobNumber(sOrderNum);
		arrChangeOrders.add(co);
	}
	private String displayContractData(
			String sOrderNum, 
			String sConf, 
			String sUser){

		String sResult = "";
		String SQL = "SELECT"
			+ " " + SMTableorderheaders.bdtotalcontractamount
			+ ", " + SMTableorderheaders.bdtotalmarkup
			+ ", " + SMTableorderheaders.bdtruckdays
			+ " FROM " + SMTableorderheaders.TableName
			+ " WHERE ("
				+ "(" + SMTableorderheaders.strimmedordernumber + " = '" + sOrderNum + "')"
			+ ")"
			;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sConf, 
					"MySQL",
					this.toString() + ".loadChangeOrders - user: " + sUserID
					+ " - "
					+ sUserFullName
			);
			if (rs.next()){
				sResult = "<BR>Original truck days:&nbsp;<B>"
					+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
							SMTableorderheaders.bdtruckdaysScale, rs.getBigDecimal(
									SMTableorderheaders.bdtruckdays))
					+ "</B>&nbsp;&nbsp;Original MU:&nbsp;<B>"
						+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
							SMTableorderheaders.bdtotalmarkupScale, rs.getBigDecimal(
									SMTableorderheaders.bdtotalmarkup))
					+ "</B>&nbsp;&nbsp;Original contract amt:&nbsp;<B>"
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(
							SMTableorderheaders.bdtotalcontractamount))
					+ "</B>"
				;
			}
			rs.close();
		} catch (SQLException e) {
			sResult = "Error reading contract information - " + e.getMessage();
		}
		return sResult;
	}

	private String listChangeOrderLines(String sOrderNum) {
		String s = "";

		//Layout the table:
		s += layoutEditTable();

		s += "<table class = \"main\">";
		s += printHeader();

		//This is used to tell us if we have a line that's not saved - for example, this could be 
		//a 'resubmit' from a failed save, and in that case, we don't want to add another blank line
		//at the bottom:
		int iNumberOfLines = 0;
		//boolean bUnsavedLineExists = false;

		//boolean bAdditionalLineHeadingPrinted = false;
		for (int i = 0; i < arrChangeOrders.size(); i++) {
			iNumberOfLines++;
			//if (arrChangeOrders.get(i).getM_iID().compareToIgnoreCase("-1") == 0){
			//	bUnsavedLineExists = true;
			//}
			s += "<tr>";

			s += "<INPUT TYPE=HIDDEN NAME=\"" + CO_LINE_PARAM_PREFIX + SMChangeOrder.ParamiID
			+ clsStringFunctions.PadLeft(Integer.toString(i + 1), "0", 6)
			+ "\" VALUE=\"" + arrChangeOrders.get(i).getM_iID().trim() + "\">";

			s += "<INPUT TYPE=HIDDEN NAME=\"" + CO_LINE_PARAM_PREFIX + SMChangeOrder.ParamsJobNumber
			+ clsStringFunctions.PadLeft(Integer.toString(i + 1), "0", 6)
			+ "\" VALUE=\"" + arrChangeOrders.get(i).getM_sJobNumber().trim() + "\">";

			s += "<td class=\"r\">";
			if (arrChangeOrders.get(i).getM_iID().trim().compareToIgnoreCase("-1") == 0){
				s += "<INPUT TYPE=TEXT NAME=\"" 
					+ CO_LINE_PARAM_PREFIX + SMChangeOrder.ParamdChangeOrderNumber 
					+ clsStringFunctions.PadLeft(Integer.toString(i + 1), "0", 6)
					+ "\""
					+ " VALUE=\"" + "" + "\""
					//+ " SIZE=4"
					+ " MAXLENGTH=" + "5"
					+ " STYLE=\"width: " + ".4" + " in; text-align:right; height: 0.25in\""
					+ ">"
					;
			}else{
				s += "<INPUT TYPE=HIDDEN NAME=\"" + CO_LINE_PARAM_PREFIX + SMChangeOrder.ParamdChangeOrderNumber
				+ clsStringFunctions.PadLeft(Integer.toString(i + 1), "0", 6)
				+ "\" VALUE=\"" + arrChangeOrders.get(i).getM_dChangeOrderNumber().trim() + "\">";
				s += arrChangeOrders.get(i).getM_dChangeOrderNumber().trim();
			}
			s += "</td>";

			s += "<td class=\"l\">" 
				+ "<INPUT TYPE=TEXT NAME=\"" 
				+ CO_LINE_PARAM_PREFIX + SMChangeOrder.ParamdatChangeOrderDate 
				+ clsStringFunctions.PadLeft(Integer.toString(i + 1), "0", 6)
				+ "\""
				+ " VALUE=\"" + arrChangeOrders.get(i).getM_datChangeOrderDate() + "\""
				//+ " SIZE=8"
				+ " MAXLENGTH=" + "10"
				+ " STYLE=\"width: " + ".8" + " in; height: 0.25in\""
				+ ">"
				+ SMUtilities.getDatePickerString(CO_LINE_PARAM_PREFIX + SMChangeOrder.ParamdatChangeOrderDate 
						+ clsStringFunctions.PadLeft(Integer.toString(i + 1), "0", 6), getServletContext())
						+ "</td>"
						;

			s += "<td class=\"l\">" 
				+"<INPUT TYPE=TEXT NAME=\"" 
				+ CO_LINE_PARAM_PREFIX + SMChangeOrder.ParamsDesc 
				+ clsStringFunctions.PadLeft(Integer.toString(i + 1), "0", 6)
				+ "\""
				+ " VALUE=\"" + arrChangeOrders.get(i).getM_sDesc().trim().replace("\"", "&quot;") + "\""
				//+ " SIZE=18"
				+ " MAXLENGTH=" + Integer.toString(SMTablechangeorders.sDescriptionLength)
				+ " STYLE=\"width: " + "3.5" + " in; height: 0.25in\""
				+ ">"
				+ "</td>"
				;

			s += "<td class=\"r\">" 
				+"<INPUT TYPE=TEXT NAME=\"" 
				+ CO_LINE_PARAM_PREFIX + SMChangeOrder.ParamdAmount 
				+ clsStringFunctions.PadLeft(Integer.toString(i + 1), "0", 6)
				+ "\""
				+ " VALUE=\"" + arrChangeOrders.get(i).getM_dAmount() + "\""
				//+ " SIZE=8"
				+ " MAXLENGTH=" + "13"
				+ " STYLE=\"width: " + ".8" + " in; text-align:right; height: 0.25in\""
				+ ">"
				+ "</td>"
				;

			s += "<td class=\"r\">" 
				+"<INPUT TYPE=TEXT NAME=\"" 
				+ CO_LINE_PARAM_PREFIX + SMChangeOrder.ParamdTotalMarkUp
				+ clsStringFunctions.PadLeft(Integer.toString(i + 1), "0", 6)
				+ "\""
				+ " VALUE=\"" + arrChangeOrders.get(i).getM_dTotalMarkUp() + "\""
				//+ " SIZE=8"
				+ " MAXLENGTH=" + "13"
				+ " STYLE=\"width: " + ".8" + " in; text-align:right; height: 0.25in\""
				+ ">"
				+ "</td>"
				;

			s += "<td class=\"r\">" 
				+"<INPUT TYPE=TEXT NAME=\"" 
				+ CO_LINE_PARAM_PREFIX + SMChangeOrder.ParamdTruckDays
				+ clsStringFunctions.PadLeft(Integer.toString(i + 1), "0", 6)
				+ "\""
				+ " VALUE=\"" + arrChangeOrders.get(i).getM_dTruckDays() + "\""
				//+ " SIZE=8"
				+ " MAXLENGTH=" + "13"
				+ " STYLE=\"width: " + ".7" + " in; text-align:right; height: 0.25in\""
				+ ">"
				+ "</td>"
				;				

			//Put a 'delete' button here if it's a saved line:
			if (arrChangeOrders.get(i).getM_iID().trim().compareToIgnoreCase("-1") != 0){
				s += "<td class=\"l\">"
					+ "<INPUT TYPE=SUBMIT NAME='" 
					+ DELETE_BUTTON_NAME_PREFIX + arrChangeOrders.get(i).getM_iID()
					+ "' VALUE='" + DELETE_BUTTON_LABEL
					+ "' >"
					//+ "</td>"
					//;
					+ "&nbsp;<INPUT TYPE=CHECKBOX NAME=\"" + DELETE_CONFIRM_CHECKBOX_NAME 
					+ arrChangeOrders.get(i).getM_iID()
					+ "\"> Confirm delete</P>"
					;
			}
		} //end loop

		/*
		//If there is NO unsaved line in the array, we can add a blank line for a new insert:
		if (!bUnsavedLineExists){
			iNumberOfLines++;
			//Add a line for an additional change order:
			s += "<tr>";
			//We flag this unsaved line with a -1 ID:
			s += "<INPUT TYPE=HIDDEN NAME=\"" + CO_LINE_PARAM_PREFIX + SMChangeOrder.ParamiID
			+ SMUtilities.PadLeft("", "0", 6)
			+ "\" VALUE=\"" + "-1" + "\">";

			s += "<INPUT TYPE=HIDDEN NAME=\"" + CO_LINE_PARAM_PREFIX + SMChangeOrder.ParamsJobNumber
			+ SMUtilities.PadLeft("", "0", 6)
			+ "\" VALUE=\"" + sOrderNum + "\">";

			s += "<td class=\"r\">" 
				+"<INPUT TYPE=TEXT NAME=\"" 
				+ CO_LINE_PARAM_PREFIX + SMChangeOrder.ParamdChangeOrderNumber 
				+ SMUtilities.PadLeft("", "0", 6)
				+ "\""
				+ " VALUE=\"" + "" + "\""
				//+ " SIZE=4"
				+ " MAXLENGTH=" + "5"
				+ " STYLE=\"width: " + ".4" + " in; text-align:right; height: 0.25in\""
				+ ">"
				+ "</td>"
				;

			s += "<td class=\"l\">" 
				+ "<INPUT TYPE=TEXT NAME=\"" 
				+ CO_LINE_PARAM_PREFIX + SMChangeOrder.ParamdatChangeOrderDate 
				+ SMUtilities.PadLeft("", "0", 6)
				+ "\""
				+ " VALUE=\"" + SMMasterEntry.EMPTY_DATE_STRING + "\""
				//+ " SIZE=8"
				+ " MAXLENGTH=" + "10"
				+ " STYLE=\"width: " + ".8" + " in; height: 0.25in\""
				+ ">"
				+ SMUtilities.getDatePickerString(CO_LINE_PARAM_PREFIX + SMChangeOrder.ParamdatChangeOrderDate 
						+ SMUtilities.PadLeft("", "0", 6), getServletContext())
						+ "</td>"
						;

			s += "<td class=\"l\">" 
				+"<INPUT TYPE=TEXT NAME=\"" 
				+ CO_LINE_PARAM_PREFIX + SMChangeOrder.ParamsDesc 
				+ SMUtilities.PadLeft("", "0", 6)
				+ "\""
				+ " VALUE=\"" + "" + "\""
				//+ " SIZE=18"
				+ " MAXLENGTH=" + Integer.toString(SMTablechangeorders.sDescriptionLength)
				+ " STYLE=\"width: " + "3.5" + " in; height: 0.25in\""
				+ ">"
				+ "</td>"
				;

			s += "<td class=\"r\">" 
				+"<INPUT TYPE=TEXT NAME=\"" 
				+ CO_LINE_PARAM_PREFIX + SMChangeOrder.ParamdAmount 
				+ SMUtilities.PadLeft("", "0", 6)
				+ "\""
				+ " VALUE=\"" + "0.00" + "\""
				//+ " SIZE=8"
				+ " MAXLENGTH=" + "13"
				+ " STYLE=\"width: " + ".8" + " in; text-align:right; height: 0.25in\""
				+ ">"
				+ "</td>"
				;

			s += "<td class=\"r\">" 
				+"<INPUT TYPE=TEXT NAME=\"" 
				+ CO_LINE_PARAM_PREFIX + SMChangeOrder.ParamdTotalMarkUp
				+ SMUtilities.PadLeft("", "0", 6)
				+ "\""
				+ " VALUE=\"" + "0.00" + "\""
				//+ " SIZE=8"
				+ " MAXLENGTH=" + "13"
				+ " STYLE=\"width: " + ".8" + " in; text-align:right; height: 0.25in\""
				+ ">"
				+ "</td>"
				;

			s += "<td class=\"r\">" 
				+"<INPUT TYPE=TEXT NAME=\"" 
				+ CO_LINE_PARAM_PREFIX + SMChangeOrder.ParamdTruckDays
				+ SMUtilities.PadLeft("", "0", 6)
				+ "\""
				+ " VALUE=\"" + "0.0000" + "\""
				//+ " SIZE=8"
				+ " MAXLENGTH=" + "13"
				+ " STYLE=\"width: " + ".7" + " in; text-align:right; height: 0.25in\""
				+ ">"
				+ "</td>"
				;				

			s += "<td class=\"l\">&nbsp;</td>";
			s += "</tr>";
		}
		*/
		//Record the number of lines:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + NUMBER_OF_LINES
		+ "\" VALUE=\"" + Integer.toString(iNumberOfLines) + "\">";

		s += "</table>";
		return s;
	}
	private String printHeader(){
		return
		"<tr>"
		+ "<td class=\"r\">CO #</TD>"
		+ "<td class=\"l\">Date</TD>"
		+ "<td class=\"l\">Description</TD>"
		+ "<td class=\"r\">Amt.</TD>"
		+ "<td class=\"r\">Total MU</TD>"
		+ "<td class=\"r\">Truck days</TD>"
		+ "<td class=\"l\">&nbsp;</TD>"
		+ "</tr>"
		;

	}
	public static String layoutEditTable(){
		String s = "";
		String sBorderSize = "2";
		String sFontSize = "small";
		s += "<style type=\"text/css\">\n";

		//Set hyperlink style:
		//s += "a {font-family : Arial; Font-size : 12px; text-decoration : none}\n";

		//s += "amenu {font-family : Arial; text-decoration : none; font-weight: 900}\n";
		//s += "amenu:link {color : white}\n";
		//s += "amenu:visited {color : #99FFFF}\n";
		//s += "amenu:active {color : #99FFFF}\n";
		//s += "amenu:hover {color : white}\n";

		//s += "a {font-family : Arial; text-decoration : none; font-weight: 900}\n";
		//s += "a:link {color : #99FFFF}\n";
		//s += "a:visited {color : #99FFFF}\n";
		//s += "a:active {color : #99FFFF}\n";
		//s += "a:hover {color : white}\n";

		//Layout table:
		s +=
			"table.main {"
			+ "border-width: " + sBorderSize + "px; "
			+ "border-spacing: 2px; "
			+ "border-style: outset; "
			+ "border-style: solid; "
			+ "border-color: black; "
			+ "border-collapse: separate; "
			+ "width: 100%; "
			+ "font-size: " + sFontSize + "; "
			+ "font-family : Arial; "
			//+ "background-color: white; "
			+ "}"
			+ "\n"
			;

		s +=
			"table.main th {"
			+ "border-width: " + sBorderSize + "px; "
			+ "padding: 2px; "
			//+ "border-style: inset; "
			+ "border-style: none; "
			+ "border-color: white; "
			//+ "background-color: white; "
			+ "color: black; "
			+ "font-family : Arial; "
			+ "vertical-align: text-middle; "
			+ "text-align: ; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;

		//s +=
		//	"tr.d0 td {"
		//	+ "background-color: #FFFFFF; "
		//	+"}"
		//	;
		//s +=
		//	"tr.d1 td {"
		//	+ "background-color: #EEEEEE; "
		//	+ "}"
		//	+ "\n"
		//	;

		s +=
			"td.r {"
			+ "border-width: " + sBorderSize + "px; "
			+ "padding: 2px; "
			//+ "border-style: solid; "
			//+ "border-color: black; "
			+ "vertical-align: text-middle;"
			//+ "background-color: black; "
			+ "text-align: right; "
			+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;

		s +=
			"td.l {"
			+ "border-width: " + sBorderSize + "px; "
			+ "padding: 2px; "
			//+ "border-style: solid; "
			//+ "border-color: black; "
			+ "vertical-align: text-middle;"
			//+ "background-color: black; "
			+ "text-align: left; "
			+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;

		s += "</style>"
			+ "\n"
			;

		return s;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}

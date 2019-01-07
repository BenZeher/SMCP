package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableicitemprices;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTablepricelistcodes;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;

public class ICItemPriceEdit extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private String sDBID = "";
	private String sUserName = "";
	private String sCompanyName = "";
	
	private String sBasePrice = "";
	private String sPriceLevel1 = "";
	private String sPriceLevel2 = "";
	private String sPriceLevel3 = "";
	private String sPriceLevel4 = "";
	private String sPriceLevel5 = "";
		
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICEditItemPricing))
		{
			return;
		}
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "IC Edit Item Pricing";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    
	    //Get the item price fields:
	    String sPriceListCode = clsManageRequestParameters.get_Request_Parameter("PriceListCode", request);
	    String sPriceListCodeDesc = clsManageRequestParameters.get_Request_Parameter("PriceListCodeDesc", request);
	    String sItemNumber = clsManageRequestParameters.get_Request_Parameter("ItemNumber", request);

	    if (!validateItem(sItemNumber, sDBID, sUserName)){
	    	out.println("<B><FONT COLOR=\"RED\">WARNING: " + "Invalid item number: '" 
	    		+ sItemNumber + "'" + "</FONT></B><BR>");
	    	out.println("</BODY></HTML>");
	    	return;
	    }

	    if (!validatePriceCode(sPriceListCode, sDBID, sUserName)){
	    	out.println("<B><FONT COLOR=\"RED\">WARNING: " + "Invalid price list code: '" 
	    		+ sPriceListCode + "'" + "</FONT></B><BR>");
	    	out.println("</BODY></HTML>");
	    	return;
	    }

	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>STATUS: " + sStatus + "</B><BR>");
		}
	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Control Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICItemPricing) 
	    	+ "\">Summary</A><BR><BR>");
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICItemPriceEditAction\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" 
			+ SMUtilities.getFullClassName(this.toString()) + "\">");
		out.println("<INPUT TYPE=HIDDEN NAME='PriceListCode' VALUE='" + sPriceListCode + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME='ItemNumber' VALUE='" + sItemNumber + "'>");
		
		out.println("<TABLE WIDTH=100% CELLPADDING=5 border=1>");
		out.println("<TR><TD ALIGN=RIGHT><B>Price list code:</B></TD><TD><B>" 
				+ sPriceListCode + " - " + sPriceListCodeDesc + "</B></TD><TD>&nbsp;</TD></TR>");
		out.println("<TR><TD ALIGN=RIGHT><B>Item number:</B></TD><TD><B>" 
				+ sItemNumber + "</B></TD><TD>&nbsp;</TD></TR>");

		//Begin editable fields:
		//If it's a 'reprocess', meaning it's returning from the 'Action' page, then display the 
		//previously entered fields:
		if (clsManageRequestParameters.get_Request_Parameter("Reprocess", request).compareToIgnoreCase("") != 0){
		    sBasePrice = clsManageRequestParameters.get_Request_Parameter("BasePrice", request);
		    if (sBasePrice.trim().compareToIgnoreCase("") == 0){
		    	sBasePrice = "0.00";
		    }
		    sPriceLevel1 = clsManageRequestParameters.get_Request_Parameter("PriceLevel1", request);
		    if (sPriceLevel1.trim().compareToIgnoreCase("") == 0){
		    	sPriceLevel1 = "0.00";
		    }
		    sPriceLevel2 = clsManageRequestParameters.get_Request_Parameter("PriceLevel2", request);
		    if (sPriceLevel2.trim().compareToIgnoreCase("") == 0){
		    	sPriceLevel2 = "0.00";
		    }
		    sPriceLevel3 = clsManageRequestParameters.get_Request_Parameter("PriceLevel3", request);
		    if (sPriceLevel3.trim().compareToIgnoreCase("") == 0){
		    	sPriceLevel3 = "0.00";
		    }
		    sPriceLevel4 = clsManageRequestParameters.get_Request_Parameter("PriceLevel4", request);
		    if (sPriceLevel4.trim().compareToIgnoreCase("") == 0){
		    	sPriceLevel4 = "0.00";
		    }
		    sPriceLevel5 = clsManageRequestParameters.get_Request_Parameter("PriceLevel5", request);
		    if (sPriceLevel5.trim().compareToIgnoreCase("") == 0){
		    	sPriceLevel5 = "0.00";
		    }
		}else{
			//Try to get an existing record, if there is one, display it
			try {
				if (!readItemPriceRecord(sItemNumber, sPriceListCode, sDBID, sUserName)){
				}
			} catch (SQLException e) {
				out.println("<B><FONT COLOR=\"RED\">WARNING: " + "Error reading item price record for item: '" 
			    		+ sItemNumber + "', price list code: '" + sPriceListCode +  "' - "
			    		+ e.getMessage() + "</FONT></B><BR>");
			    out.println("</BODY></HTML>");
			    return;
			}
		}
		buildEditFields(out);
		
		out.println("</TABLE>");
		out.println("<BR>");
		out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"Save\">");
		out.println("</FORM>");
	    	
		out.println("</BODY></HTML>");
	}
	private boolean readItemPriceRecord(
			String sItem, 
			String sPriceCode, 
			String sConf, 
			String sUser) throws SQLException{
		
		String SQL = "SELECT"
			+ " * FROM " + SMTableicitemprices.TableName
			+ " WHERE ("
				+ "(" + SMTableicitemprices.sItemNumber + " = '" + sItem + "')"
				+ " AND (" + SMTableicitemprices.sPriceListCode + " = '" + sPriceCode + "')"
			+ ")"
			;
		//System.out.println(SQL);
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sConf, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".validateItem - user: " + sUser
					);
			if (rs.next()){
				sBasePrice = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableicitemprices.bdBasePrice));
				sPriceLevel1 = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableicitemprices.bdLevel1Price));
				sPriceLevel2 = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableicitemprices.bdLevel2Price));
				sPriceLevel3 = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableicitemprices.bdLevel3Price));
				sPriceLevel4 = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableicitemprices.bdLevel4Price));
				sPriceLevel5 = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableicitemprices.bdLevel5Price));
				rs.close();
				return true;
			}else{
				sBasePrice = "0.00";
				sPriceLevel1 = "0.00";
				sPriceLevel2 = "0.00";
				sPriceLevel3 = "0.00";
				sPriceLevel4 = "0.00";
				sPriceLevel5 = "0.00";
				rs.close();
				return false;
			}
		} catch (SQLException e) {
			throw e;
		}
	}
	private void buildEditFields(PrintWriter pwOut){
		
		pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
			"BasePrice", 
			sBasePrice, 
			14, 
			"Base price:", 
			"&nbsp;", 
			"14")
			);
		
		pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
			"PriceLevel1", 
			sPriceLevel1, 
			14, 
			"Level 1 price:", 
			"&nbsp;", 
			"14")
			);
		
		pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				"PriceLevel2", 
				sPriceLevel2, 
				14, 
				"Level 2 price:", 
				"&nbsp;", 
				"14")
				);
		
		pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				"PriceLevel3", 
				sPriceLevel3, 
				14, 
				"Level 3 price:", 
				"&nbsp;", 
				"14")
				);
		
		pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				"PriceLevel4", 
				sPriceLevel4, 
				14, 
				"Level 4 price:", 
				"&nbsp;", 
				"14")
				);
		
		pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				"PriceLevel5", 
				sPriceLevel5, 
				14, 
				"Level 5 price:", 
				"&nbsp;", 
				"14")
				);
		
	}
	private boolean validateItem(String sItem, String sConf, String sUser){
		
		String SQL = "SELECT"
			+ " " + SMTableicitems.sItemNumber
			+ " FROM " + SMTableicitems.TableName
			+ " WHERE ("
				+ SMTableicitems.sItemNumber + " = '" + sItem + "'"
			+ ")"
			;
		//System.out.println(SQL);
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sConf, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".validateItem - user: " + sUser
					);
			if (rs.next()){
				rs.close();
				return true;
			}else{
				rs.close();
				return false;
			}
		} catch (SQLException e) {
			return false;
		}
	}
	private boolean validatePriceCode(String sPriceCode, String sConf, String sUser){
		
		String SQL = "SELECT"
			+ " " + SMTablepricelistcodes.spricelistcode
			+ " FROM " + SMTablepricelistcodes.TableName
			+ " WHERE ("
				+ SMTablepricelistcodes.spricelistcode + " = '" + sPriceCode + "'"
			+ ")"
			;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sConf, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".validatePriceCode - user: " + sUser
					);
			if (rs.next()){
				rs.close();
				return true;
			}else{
				rs.close();
				return false;
			}
		} catch (SQLException e) {
			return false;
		}
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

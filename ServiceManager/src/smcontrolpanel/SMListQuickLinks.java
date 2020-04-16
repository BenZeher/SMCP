package smcontrolpanel;

import java.io.*;

//	import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTablesecurityfunctions;
import SMDataDefinition.SMTablesecuritygroupfunctions;
import SMDataDefinition.SMTablesecurityusergroups;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

import java.sql.*;
import java.util.ArrayList;

public class SMListQuickLinks extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sLicenseModuleLevel = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL);
		String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
		String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sUserFullName = SMUtilities.getFullNamebyUserID(sUserID, getServletContext(), sDBID, "SMListQuickLinks");
		//LTO 20111010
		//Password is not saved in session anymore. Therefore is it read from last page.
		String sPassword = clsManageRequestParameters.get_Request_Parameter("PASSWORD", request);
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		String SQL = "";
		String title = "List Customized Quick Links";
		String subtitle = "";

		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		//Print a link to the first page after login:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

		out.println("<B>Quick Links for " + sUserFullName + "</B><BR>");

		String sDbString = "";
		sDbString += "db=" + sDBID;  

		//check password
		try{
			SQL = "SELECT " + SMTableusers.sHashedPw + " from " + SMTableusers.TableName + " where " + SMTableusers.lid + " = " + sUserID + "";
			//System.out.println("SQL = " + SQL);
			ResultSet rsHashedPw = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID, "MySQL", this.toString() + ".doGet" + " User: " 
																	  + sUserID
																	  + " - "
																	  + sUserFullName);
			if (!rsHashedPw.next()){
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
						+ "?Warning=" + clsServletUtilities.URLEncode("The user " + sUserName + "can't be found in database.")
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					);
					return;
			}
			String sDBPw = rsHashedPw.getString(SMTableusers.sHashedPw);
			SQL = "SELECT SHA('" + sPassword + "') AS Passwd";
			ResultSet rsEnteredPw = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID, "MySQL", this.toString() + ".doGet" + " User: " 
																	  + sUserID
																	  + " - "
																	  + sUserFullName);
			rsEnteredPw.next();
			String sEtrPw = rsEnteredPw.getString("Passwd");
			if (sEtrPw.compareTo(sDBPw) != 0){
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
						+ "?Warning=" + clsServletUtilities.URLEncode("The password you entered doesn't match your current password.")
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					);
					return;
			}
		}catch (SQLException ex){
			out.println("Error verifying password - " + ex.getMessage());
			out.println("SQL = " + ex.getSQLState());
			return;
		}

		//Get the list of links for the functions we want:
		ArrayList<Long> arPermittedFunctions = new ArrayList<Long>(0);
		ArrayList<String> arPermittedFunctionNames = new ArrayList<String>(0);
		ArrayList<String> arPermittedFunctionLinks = new ArrayList<String>(0);
		//Permission values for custom links (using negative numbers)
		Long lViewTodaysAppointments = (long) -1;
		
		//First, get a list of the permitted functions:
		try{
			SQL = "SELECT DISTINCT "
				+ SMTablesecurityfunctions.TableName  + "." + SMTablesecurityfunctions.iFunctionID
				+ ", " + SMTablesecurityfunctions.TableName + "." + SMTablesecurityfunctions.slink
				+ ", " + SMTablesecurityfunctions.TableName + "." + SMTablesecurityfunctions.sFunctionName
			  + " FROM"
			  + " " + SMTablesecuritygroupfunctions.TableName 
			  + ", " + SMTablesecurityusergroups.TableName
			  + ", " + SMTablesecurityfunctions.TableName
			  + " WHERE (" 
			  	+ "(" + SMTablesecuritygroupfunctions.TableName + "." 
			  		+ SMTablesecuritygroupfunctions.sGroupName 
			  		+ " = " + SMTablesecurityusergroups.TableName + "." 
			  		+ SMTablesecurityusergroups.sSecurityGroupName + ")" 

			  	+ " AND (" + SMTablesecuritygroupfunctions.TableName + "." 
			  		+ SMTablesecuritygroupfunctions.ifunctionid 
			  		+ " = " + SMTablesecurityfunctions.TableName + "." 
			  		+ SMTablesecurityfunctions.iFunctionID + ")" 

			  		+ " AND (" + SMTablesecurityusergroups.TableName + "." 
			  		+ SMTablesecurityusergroups.luserid + "=" + sUserID + ")"
			  		
			  + " AND ((" + SMTablesecurityfunctions.imodulelevelsum + " & " + sLicenseModuleLevel + ") > 0)"
			  		
			  + ")";
			//System.out.println("SQL = " + SQL);
			
			
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sDBID, 
				"MySQL", 
				this.toString() + ".buildMenus" + " User: " 
				+ sUserID
				+ " - "
				+ sUserFullName
					);
			
			while(rs.next()){
				arPermittedFunctions.add(rs.getLong(SMTablesecurityfunctions.iFunctionID));
				arPermittedFunctionNames.add(rs.getString(SMTablesecurityfunctions.sFunctionName));
				arPermittedFunctionLinks.add(rs.getString(SMTablesecurityfunctions.slink));
			}
			//Add custom class names that are not tied to permissions
			arPermittedFunctions.add(lViewTodaysAppointments);
			arPermittedFunctionNames.add("View Todays Appointments");
			arPermittedFunctionLinks.add("smcontrolpanel.SMViewAppointmentCalendarGenerate");
			
			rs.close();
		}catch(SQLException e){
			out.println("Error getting list of permitted functions - " + e.getMessage());
			return;
		}
		
		//This array will contain the list of 'quick-linkable' functions:
		ArrayList<Long> arQuikLinkFunctions = new ArrayList<Long>(0);
		ArrayList<String> arQuikLinkOtherParams = new ArrayList<String>(0);
		//Add the 'quick-linkable' functions to the array:

		//SM Administration:
		arQuikLinkFunctions.add(SMSystemFunctions.SMQuerySelector);
		arQuikLinkOtherParams.add("");
		arQuikLinkFunctions.add(SMSystemFunctions.SMManageSecurityGroups);
		arQuikLinkOtherParams.add("");
		
		//SM General functions:
		arQuikLinkFunctions.add(SMSystemFunctions.SMViewOrderInformation);
		arQuikLinkOtherParams.add("");
		arQuikLinkFunctions.add(SMSystemFunctions.SMManageOrders);
		arQuikLinkOtherParams.add("");
		arQuikLinkFunctions.add(SMSystemFunctions.SMPrintInvoice);
		arQuikLinkOtherParams.add("");
		
		//Management reports:
		arQuikLinkFunctions.add(SMSystemFunctions.SMAverageMUpertruckday);
		arQuikLinkOtherParams.add("");
		arQuikLinkFunctions.add(SMSystemFunctions.SMIndividualAverageMUpertruckday);
		arQuikLinkOtherParams.add("");
		arQuikLinkFunctions.add(SMSystemFunctions.SMMonthlySales);
		arQuikLinkOtherParams.add("");
		arQuikLinkFunctions.add(SMSystemFunctions.SMIndividualMonthlySales);
		arQuikLinkOtherParams.add("");
		arQuikLinkFunctions.add(SMSystemFunctions.SMEditBids);
		arQuikLinkOtherParams.add("");
		arQuikLinkFunctions.add(SMSystemFunctions.SMSalesLeadReport);
		arQuikLinkOtherParams.add("");
		arQuikLinkFunctions.add(SMSystemFunctions.SMPendingBidsReport);
		arQuikLinkOtherParams.add("");
		arQuikLinkFunctions.add(SMSystemFunctions.SMJobCostDailyReport);
		arQuikLinkOtherParams.add("");
		arQuikLinkFunctions.add(SMSystemFunctions.SMUnbilledOrdersReport);
		arQuikLinkOtherParams.add("");
		arQuikLinkFunctions.add(SMSystemFunctions.SMViewTruckSchedules);
		arQuikLinkOtherParams.add("");
		arQuikLinkFunctions.add(SMSystemFunctions.SMViewAppointmentCalendar);
		arQuikLinkOtherParams.add("");
		//This is a custom link not tied to a system function
		arQuikLinkFunctions.add(lViewTodaysAppointments);
		arQuikLinkOtherParams.add("DateRange=DateRangeToday"
				+ "&StartingDate=&EndingDate="
				+ "&AllowAppointmentEditing=on&GENERATE_REPORT=----View----&"
				+ "USER*" + sUserID + "=on#LastEdit");
		arQuikLinkFunctions.add(SMSystemFunctions.SMCriticaldatesreport);
		arQuikLinkOtherParams.add("");
		arQuikLinkFunctions.add(SMSystemFunctions.SMOrderSourceListing);
		arQuikLinkOtherParams.add("");

		//IC:
		arQuikLinkFunctions.add(SMSystemFunctions.ICOnHandByDescriptionSelection);
		arQuikLinkOtherParams.add("");
		arQuikLinkFunctions.add(SMSystemFunctions.ICDisplayItemInformation);
		arQuikLinkOtherParams.add("");
		arQuikLinkFunctions.add(SMSystemFunctions.ICPrintUPCLabels);
		arQuikLinkOtherParams.add("");
		arQuikLinkFunctions.add(SMSystemFunctions.ICEditPurchaseOrders);
		arQuikLinkOtherParams.add("");
		arQuikLinkFunctions.add(SMSystemFunctions.ICEnterInvoices);
		arQuikLinkOtherParams.add("");
		arQuikLinkFunctions.add(SMSystemFunctions.ICTransactionHistory);
		arQuikLinkOtherParams.add("");
		arQuikLinkFunctions.add(SMSystemFunctions.ICPOReceivingReport);
		arQuikLinkOtherParams.add("");
		arQuikLinkFunctions.add(SMSystemFunctions.ICAssignPO);
		arQuikLinkOtherParams.add("");
		//arQuikLinkFunctions.add(SMSystemFunctions.ICClearPostedBatches);
		//arQuikLinkOtherParams.add("");
		arQuikLinkFunctions.add(SMSystemFunctions.ICEditBatches);
		arQuikLinkOtherParams.add("");
		arQuikLinkFunctions.add(SMSystemFunctions.ICListItemsReceived);
		arQuikLinkOtherParams.add("");
		arQuikLinkFunctions.add(SMSystemFunctions.ICListUnusedPOs);
		arQuikLinkOtherParams.add("");
		//arQuikLinkFunctions.add(SMSystemFunctions.ICEditPhysicalInventory);
		//arQuikLinkOtherParams.add("");
		arQuikLinkFunctions.add(SMSystemFunctions.ICPrintPurchaseOrders);
		arQuikLinkOtherParams.add("");
		arQuikLinkFunctions.add(SMSystemFunctions.ICPrintReceivingLabels);
		arQuikLinkOtherParams.add("");
		arQuikLinkFunctions.add(SMSystemFunctions.ICSetInactiveItems);
		arQuikLinkOtherParams.add("");
		
		//AP:
		arQuikLinkFunctions.add(SMSystemFunctions.APEditVendors);
		arQuikLinkOtherParams.add("");
		arQuikLinkFunctions.add(SMSystemFunctions.APEditVendorTerms);
		arQuikLinkOtherParams.add("");
		
		//AS:
		arQuikLinkFunctions.add(SMSystemFunctions.ASActivateAlarmSequences);
		arQuikLinkOtherParams.add("");
		arQuikLinkFunctions.add(SMSystemFunctions.ASAlarmFunctions);
		arQuikLinkOtherParams.add("");
		
		//AR:
		arQuikLinkFunctions.add(SMSystemFunctions.ARDisplayCustomerInformation);
		arQuikLinkOtherParams.add("");
		//arQuikLinkFunctions.add(SMSystemFunctions.AREditBatches);
		//arQuikLinkOtherParams.add("");

		
		String sMobile = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_MOBILE);
		String sAdditionalParams = "";
		if (sMobile.compareToIgnoreCase("Y") == 0){
			sAdditionalParams = "mobile=Y";
		}
		
		//First add one for the main menu:
		out.println(printLink(
				getServletContext(), 
				"smcontrolpanel.SMUserLogin", 
				"Main Menu",
				sUserName,
				sPassword,
				sDbString,
				"",
				sAdditionalParams,
				request,
				sCompanyName
		) + "<BR>");
		
		for (int i = 0; i < arQuikLinkFunctions.size(); i++){
			long iQuikLinkFunctionID = arQuikLinkFunctions.get(i);
			for (int j = 0; j < arPermittedFunctions.size(); j++){
				long iPermittedFunctionID = arPermittedFunctions.get(j);
				if (iQuikLinkFunctionID == iPermittedFunctionID){
					out.println(printLink(
							getServletContext(), 
							arPermittedFunctionLinks.get(j),
							arPermittedFunctionNames.get(j), 
							sUserName,
							sPassword,
							sDbString,
							arQuikLinkOtherParams.get(i),
							sAdditionalParams,
							request,
							sCompanyName
					) + "<BR>");
				}
			}
		}
		
		
		out.println("</BODY></HTML>");
	}

	private String printLink(
			ServletContext context, 
			String sClass, 
			String sLinkDescription,
			String sUser,
			String sPw,
			String sDbString,
			String sQuickLinkParams,
			String sOtherParams,
			HttpServletRequest req,
			String sCompanyName){

		String sLinkBase = "";
		try {
			sLinkBase = clsServletUtilities.getServerURL(req, context);
		} catch (Exception e) {
			System.out.println("[1543612623] - Error getting server URL - " + e.getMessage());
		}
		
		String sLinkString = "<A HREF=\"" 
			+ sLinkBase
			+ "/"
			+ WebContextParameters.getInitWebAppName(getServletContext()) 
			+ "/"
			+ sClass
		;
		
		//If the link already has parameters, we just add an '&'
		if (sLinkString.contains("?")){
			sLinkString += "&";
		}else{
			sLinkString += "?";
		}		
		sLinkString +=
			"user="
			+ sUser
			+ "&pw="
			+ sPw
			+ "&"
			+ sDbString
			;
		
		if (sQuickLinkParams.compareToIgnoreCase("") != 0){
			sLinkString += "&" + sQuickLinkParams;
		}
		if (sOtherParams.compareToIgnoreCase("") != 0){
			sLinkString += "&" + sOtherParams;
		}
		
		sLinkString +=
			"\">"
			+ sCompanyName + " " + sLinkDescription
			+ "</A><BR>";
		
		return sLinkString;
	}
}

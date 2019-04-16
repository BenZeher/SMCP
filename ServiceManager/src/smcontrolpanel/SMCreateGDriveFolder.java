package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smap.APVendor;
import smar.ARCustomer;
import smic.ICPOHeader;
import SMClasses.SMLogEntry;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMCreateGoogleDriveFolderParamDefinitions;
import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTablebids;
import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTableicvendors;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTableworkorders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMCreateGDriveFolder extends HttpServlet{
	
	public static String FOLDER_URL_PARAM = "folderURL";
	public static String RECORD_TYPE_PARAM = "recordtype";
	public static String KEY_VALUE_PARAM = "keyvalue";
	public static String ASYNC_VALUE_PARAM = "asyncrequest";
	
	private static final long serialVersionUID = 1L;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		boolean bAsyncRequest = clsManageRequestParameters.get_Request_Parameter(ASYNC_VALUE_PARAM, request).compareToIgnoreCase("") != 0;
		
		PrintWriter out = response.getWriter();
		
		String s = "<HTML>";
		String sRecordType = clsManageRequestParameters.get_Request_Parameter(RECORD_TYPE_PARAM, request);
		long lFunctionID = 0L;
		

		String sFolderURL = clsManageRequestParameters.get_Request_Parameter(FOLDER_URL_PARAM, request);
		if (sFolderURL.compareToIgnoreCase("") == 0){
			s += "<BR>Error [Error [1439824291] - Folder URL passed in '" + FOLDER_URL_PARAM + "' was blank - cannot continue.</HTML>";
			if(bAsyncRequest) { response.getWriter().write(s); }
			out.println(s);
			return;
		}
		
		String sKeyValue = clsManageRequestParameters.get_Request_Parameter(KEY_VALUE_PARAM, request);
		if (sKeyValue.compareToIgnoreCase("") == 0){
			s += "<BR>Error [Error [1439824292] - Invalid record key value '" + sKeyValue + "' - '" + sKeyValue + "' - cannot continue.</HTML>";
			if(bAsyncRequest) { response.getWriter().write(s); }
			out.println(s);
			return;
		}
		
		HttpSession CurrentSession = request.getSession();
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		
		//Create the necessary SQL statement:
		String SQL = "";
		String sRedirectString = "";
		
		if (sRecordType.compareToIgnoreCase(SMCreateGoogleDriveFolderParamDefinitions.AR_CUSTOMER_RECORD_TYPE_PARAM_VALUE) == 0){
			lFunctionID = SMSystemFunctions.SMCreateGDriveARFolders;
			SQL = "UPDATE " + SMTablearcustomer.TableName + " SET " + SMTablearcustomer.sgdoclink + " = '" + sFolderURL + "'"
				+ " WHERE (" + SMTablearcustomer.sCustomerNumber + " = '" + sKeyValue + "')";
			sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
				+ "smar.AREditCustomersEdit"
				+ "?" + ARCustomer.ParamsCustomerNumber + "=" + sKeyValue
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&SubmitEdit=Y"
				+ "&Status=Google Drive folder was updated successfully."
				;
		}
		if (sRecordType.compareToIgnoreCase(SMCreateGoogleDriveFolderParamDefinitions.AR_DISPLAYED_CUSTOMER_TYPE_PARAM_VALUE) == 0){
			lFunctionID = SMSystemFunctions.SMCreateGDriveARFolders;
			SQL = "UPDATE " + SMTablearcustomer.TableName + " SET " + SMTablearcustomer.sgdoclink + " = '" + sFolderURL + "'"
				+ " WHERE (" + SMTablearcustomer.sCustomerNumber + " = '" + sKeyValue + "')";
			sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
				+ "smar.ARDisplayCustomerInformation"
				+ "?" + ARCustomer.ParamsCustomerNumber + "=" + sKeyValue
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&CallingClass=smcontrolpanel.ARDisplayCustomerSelect"
				+ "&Status=Google Drive folder was updated successfully."
				;
		}
		
		if (sRecordType.compareToIgnoreCase(SMCreateGoogleDriveFolderParamDefinitions.RENAMED_ORDER_FOLDER_URL_PARAM_VALUE) == 0){
			lFunctionID = SMSystemFunctions.SMCreateGDriveOrderFolders;
			sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditOrderEdit"
				+ "?" + SMOrderHeader.ParamsOrderNumber + "=" + sKeyValue
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&CallingClass=smcontrolpanel.SMEditOrderSelection"
				+ "&Status=Google Drive folder was updated successfully."
			;
	    	CurrentSession.removeAttribute(SMOrderHeader.ParamObjectName);
		}
		if (sRecordType.compareToIgnoreCase(SMCreateGoogleDriveFolderParamDefinitions.ORDER_RECORD_TYPE_PARAM_VALUE) == 0){
			lFunctionID = SMSystemFunctions.SMCreateGDriveOrderFolders;
			SQL = "UPDATE " + SMTableorderheaders.TableName + " SET " + SMTableorderheaders.sgdoclink + " = '" + sFolderURL + "'"
				+ " WHERE (" + SMTableorderheaders.strimmedordernumber + " = '" + sKeyValue + "')";
			sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditOrderEdit"
				+ "?" + SMOrderHeader.ParamsOrderNumber + "=" + sKeyValue
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&CallingClass=smcontrolpanel.SMEditOrderSelection"
				+ "&Status=Google Drive folder was updated successfully."
			;
			//Make sure there's no 'Order' entry left in the session so the entry screen can load the order
			//fresh from the data:
	    	CurrentSession.removeAttribute(SMOrderHeader.ParamObjectName);
		}
		if (sRecordType.compareToIgnoreCase(SMCreateGoogleDriveFolderParamDefinitions.DISPLAYED_ORDER_TYPE_PARAM_VALUE) == 0){
			lFunctionID = SMSystemFunctions.SMCreateGDriveOrderFolders;
			SQL = "UPDATE " + SMTableorderheaders.TableName + " SET " + SMTableorderheaders.sgdoclink + " = '" + sFolderURL + "'"
				+ " WHERE (" + SMTableorderheaders.strimmedordernumber + " = '" + sKeyValue + "')";
			sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMDisplayOrderInformation"
				+ "?" + SMOrderHeader.ParamsOrderNumber + "=" + sKeyValue
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&CallingClass=smcontrolpanel.SMDisplayOrderSelect"
				+ "&Status=Google Drive folder was updated successfully."
			;
			//Make sure there's no 'Order' entry left in the session so the entry screen can load the order
			//fresh from the data:
	    	CurrentSession.removeAttribute(SMOrderHeader.ParamObjectName);
		}
		if (sRecordType.compareToIgnoreCase(SMCreateGoogleDriveFolderParamDefinitions.WORK_ORDER_TYPE_PARAM_VALUE) == 0){
			lFunctionID = SMSystemFunctions.SMCreateGDriveWorkOrderFolders;
			SQL = "UPDATE " + SMTableworkorders.TableName   
				+ " SET " + SMTableworkorders.TableName + "." + SMTableworkorders.sgdoclink + " = '" + sFolderURL + "'"
				+ " WHERE (" + SMTableworkorders.lid + " = '" + sKeyValue + "')";
			sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMWorkOrderEdit"
				+ "?" + SMTableworkorders.lid + "=" + sKeyValue
				+ "&CallingClass=smcontrolpanel.SMWorkOrderEdit"
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&Status=Google Drive folder was updated successfully."
			;
			//Make sure there's no 'Work Order' entry left in the session so the entry screen can load the order
			//fresh from the data:
	    	CurrentSession.removeAttribute(SMTableworkorders.ObjectName);
		}
		if (sRecordType.compareToIgnoreCase(SMCreateGoogleDriveFolderParamDefinitions.PO_RECORD_TYPE_PARAM_VALUE) == 0){
			lFunctionID = SMSystemFunctions.SMCreateGDrivePOFolders;
			SQL = "UPDATE " + SMTableicpoheaders.TableName + " SET " + SMTableicpoheaders.sgdoclink + " = '" + sFolderURL + "'"
				+ " WHERE (" + SMTableicpoheaders.lid + " = " + sKeyValue + ")";
			sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPOEdit"
				+ "?" + ICPOHeader.Paramlid + "=" + sKeyValue
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&Status=Google Drive folder was updated successfully."
				;
			//Make sure there's no 'PO Entry' left in the session so the entry screen can load the PO
			//fresh from the data:
	    	CurrentSession.removeAttribute(ICPOHeader.ParamObjectName);
		}
		if (sRecordType.compareToIgnoreCase(SMCreateGoogleDriveFolderParamDefinitions.SALESLEAD_RECORD_TYPE_PARAM_VALUE) == 0){
			lFunctionID = SMSystemFunctions.SMCreateGDriveSalesLeadFolders;
			SQL = "UPDATE " + SMTablebids.TableName + " SET " + SMTablebids.sgdoclink + " = '" + sFolderURL + "'"
				+ " WHERE (" + SMTablebids.lid + " = " + sKeyValue + ")";
			sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditBidEntry"
				+ "?" + SMBidEntry.ParamID + "=" + sKeyValue
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&Status=Google Drive folder was updated successfully."
				;
			//Make sure there's no 'sales lead entry' left in the session so the entry screen can load the sales lead
			//fresh from the data:
	    	CurrentSession.removeAttribute("BidEntry");
		}
		
		if (sRecordType.compareToIgnoreCase(SMCreateGoogleDriveFolderParamDefinitions.AP_VENDOR_RECORD_TYPE_PARAM_VALUE) == 0){
			lFunctionID = SMSystemFunctions.SMCreateGDriveVendorFolders;
			SQL = "UPDATE " + SMTableicvendors.TableName + " SET " + SMTableicvendors.sgdoclink + " = '" + sFolderURL + "'"
				+ " WHERE (" + SMTableicvendors.svendoracct + " = '" + sKeyValue + "')";
			sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "smap.APEditVendorsEdit"
				+ "?" + APVendor.Paramsvendoracct + "=" + sKeyValue
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&Status=Google Drive folder was updated successfully."
				;
			//Make sure there's no 'vendor' left in the session so the entry screen can load the vendor
			//fresh from the data:
	    	CurrentSession.removeAttribute(APVendor.ParamObjectName);
		}
		
		if (sRecordType.compareToIgnoreCase(SMCreateGoogleDriveFolderParamDefinitions.AP_DISPLAY_VENDOR_TYPE_PARAM_VALUE) == 0){
			lFunctionID = SMSystemFunctions.SMCreateGDriveVendorFolders;
			SQL = "UPDATE " + SMTableicvendors.TableName + " SET " + SMTableicvendors.sgdoclink + " = '" + sFolderURL + "'"
				+ " WHERE (" + SMTableicvendors.svendoracct + " = '" + sKeyValue + "')";
			sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "smap.APDisplayVendorInformation"
				+ "?" + APVendor.Paramsvendoracct + "=" + sKeyValue
				+ "&CallingClass=smap.APDisplayVendorSelection"
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&Status=Google Drive folder was updated successfully."
				;
			//Make sure there's no 'vendor' left in the session so the entry screen can load the vendor
			//fresh from the data:
	    	CurrentSession.removeAttribute(APVendor.ParamObjectName);
		}
		
		//If we didn't get a valid record type parameter, then warn and exit;
		if (lFunctionID == 0L){
			s += "<BR>Error [Error [1439824290] - Invalid '" + RECORD_TYPE_PARAM + "' - '" + sRecordType + "' - cannot continue.</HTML>";
			if(bAsyncRequest) { response.getWriter().write(s); }
			out.println(s);
			return;
		}
		
		//Now update the record:
   		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				this.toString() + ".updating record - " + "user: " + sUserID + " [1439824957]");
		} catch (Exception e) {
			s += "<BR>Error [Error [1439824294] - Could not get data connection - " + e.getMessage() + " - cannot continue.</HTML>";
			if(bAsyncRequest) { response.getWriter().write(s); }
			out.println(s);
			return;
		}
		
		try {
			Statement stmt = conn.createStatement();
			if(SQL.compareToIgnoreCase("") != 0) {
				stmt.execute(SQL);
			}
		} catch (SQLException e1) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080421]");
			s += "<BR>Error [Error [1439824295] - Could not update Google Drive link with SQL: '" + SQL + " - " + e1.getMessage() + ".</HTML>";
			if(bAsyncRequest) { response.getWriter().write(s); }
			out.println(s);
			return;
		}
		
		SMLogEntry log = new SMLogEntry(conn);
		log.writeEntry(
			sUserID, 
			SMLogEntry.LOG_OPERATION_SMCREATENEWDOCUMENTFOLDER, 
			"Added folder for " + sRecordType + ", key: " + sKeyValue, 
			"Folder URL: '" + sFolderURL + "'", 
			"[1440028151]"
		);
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080422]");
		
		//if ASYNC request 
		if(bAsyncRequest) {
			response.getWriter().write("Update google link succesfully.");
			return;
		}	
		//Assuming all went well, now we can return the user to the appropriate screen:
		redirectProcess(sRedirectString, response);
		return;
	}
	private void redirectProcess(String sRedirectString, HttpServletResponse res ){
		try {
			res.sendRedirect(sRedirectString.replace(" ", "+"));
		} catch (IOException e1) {
			System.out.println("In " + this.toString() + ".redirectAction - error redirecting with string: "
					+ sRedirectString);
			return;
		}
		
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
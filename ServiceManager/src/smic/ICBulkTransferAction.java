package smic;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTableicbatchentries;
import SMDataDefinition.SMTableicentrylines;
import SMDataDefinition.SMTableicitems;
import ServletUtilities.clsDBServerTime;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class ICBulkTransferAction extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public static final String PARAM_BULK_TANSFER_UOMMARKER = "UNITOFMEASURE";
	public static final String PARAM_BULK_TANSFER_ITEMDESCMARKER = "ITEMDESC";
	public static final String PARAM_BULK_TANSFER_FROMLOCATIONQTYONHANDMARKER = "FROMLOCQTYOH";
	public static final String PARAM_BULK_TANSFER_TOLOCATIONQTYONHANDMARKER = "TOLOCQTYOH";
	public static final String PARAM_BULK_TANSFER_ADD_ROWS = "ADDROWS";
	public static final int BULK_TANSFER_NUMBER_OF_ROWS_TO_ADD = 10;
	public static final String PARAM_BULK_TANSFER_TOTAL_NUMBER_OF_ROWS = "TOTALNUMBEROFROWS";
	public static final String PARAM_BULK_TANSFER_CALLINGCLASS = "CALLINGCLASS";
	public static final String PARAM_BULK_TANSFER_SHOWINFOFIELDS = "SHOWINFOFIELDS";
	public static final String SESSION_OBJECT_TRANSFER_ENTRY = "BULKTRANSFERENTRY";
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICBulkTransfers))
		{
			return;
		}

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		CurrentSession.removeAttribute(SESSION_OBJECT_TRANSFER_ENTRY);
		ICEntry entry = new ICEntry(request);
		//Now remove any lines with blank item numbers:
		for (int i = 0; i < entry.getLineCount(); i++){
			if (entry.getLineByIndex(i).sItemNumber().compareToIgnoreCase("") == 0){
				entry.getLineByIndex(i).setQtyString("0.0000");
			}
		}
		entry.remove_zero_amount_lines();
		
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME)
				+ " " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		//sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CALLINGCLASS", request);
		
		String sCommand = clsManageRequestParameters.get_Request_Parameter(ICBulkTransferEdit.PARAM_COMMAND, request);
		
		try {
		} catch (NumberFormatException e1) {
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass + "?"
					+ "Warning=Invalid number of lines"
					+ "&" + PARAM_BULK_TANSFER_TOTAL_NUMBER_OF_ROWS + "=" + clsManageRequestParameters.get_Request_Parameter(PARAM_BULK_TANSFER_TOTAL_NUMBER_OF_ROWS, request)
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
		}

		String sStatus = "";
		//Process a CREATE:
		if (sCommand.compareToIgnoreCase(ICBulkTransferEdit.PARAM_COMMAND_CREATE) == 0){
			//Create the batch here:
			String sBatchNumber = "";
			try {
				sBatchNumber = createBatch(entry, sDBID, sUserID, sUserFullName);
			} catch (Exception e) {
				CurrentSession.setAttribute(ICBulkTransferAction.SESSION_OBJECT_TRANSFER_ENTRY, entry);
				String sWarning = e.getMessage();
				if (sWarning.length() > 512){
					sWarning = sWarning.substring(0, 512) + "...";
				}
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass + "?"
						+ "Warning=" + sWarning
						+ "&" + PARAM_BULK_TANSFER_TOTAL_NUMBER_OF_ROWS + "=" + clsManageRequestParameters.get_Request_Parameter(PARAM_BULK_TANSFER_TOTAL_NUMBER_OF_ROWS, request)
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
			}
			sStatus = "Batch successfully created.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatchesEdit" + "?"
					+ "BatchNumber=" + sBatchNumber
					+ "&BatchType=" + Integer.toString(ICBatchTypes.IC_TRANSFER)
					+ "&Status=Batch number " + sBatchNumber + " successfully created"
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
		}
		
		//Process a VALIDATE:
		if (sCommand.compareToIgnoreCase(ICBulkTransferEdit.PARAM_COMMAND_VALIDATE) == 0){
			//Do the validation here:
			try {
				validateEntryTransfers(entry, sDBID);
				
			} catch (Exception e) {
				CurrentSession.setAttribute(ICBulkTransferAction.SESSION_OBJECT_TRANSFER_ENTRY, entry);
				String sWarning = e.getMessage();
				if (sWarning.length() > 512){
					sWarning = sWarning.substring(0, 512) + "...";
				}
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass + "?"
						+ PARAM_BULK_TANSFER_TOTAL_NUMBER_OF_ROWS + "=" + clsManageRequestParameters.get_Request_Parameter(PARAM_BULK_TANSFER_TOTAL_NUMBER_OF_ROWS, request)
						+ "&" + "Warning=" + sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
			}
			CurrentSession.setAttribute(ICBulkTransferAction.SESSION_OBJECT_TRANSFER_ENTRY, entry);
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass + "?"
					+ "Status=" + "Validated transfers"
					+ "&" + PARAM_BULK_TANSFER_SHOWINFOFIELDS + "=Y"
					+ "&" + PARAM_BULK_TANSFER_TOTAL_NUMBER_OF_ROWS + "=" + clsManageRequestParameters.get_Request_Parameter(PARAM_BULK_TANSFER_TOTAL_NUMBER_OF_ROWS, request)
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
		}
		
		//Process an ADD_ROWS command:
		if (sCommand.compareToIgnoreCase(ICBulkTransferEdit.PARAM_COMMAND_ADD_TEN_ROWS) == 0){
			sStatus = "Added " + Integer.toString(BULK_TANSFER_NUMBER_OF_ROWS_TO_ADD) + " rows";
			int iNewNumberOfRows;
			try {
				iNewNumberOfRows = Integer.parseInt(clsManageRequestParameters.get_Request_Parameter(PARAM_BULK_TANSFER_TOTAL_NUMBER_OF_ROWS, request));
			} catch (NumberFormatException e) {
				iNewNumberOfRows = ICBulkTransferEdit.INITIAL_NUMBER_OF_ROWS;
			}
			iNewNumberOfRows += BULK_TANSFER_NUMBER_OF_ROWS_TO_ADD;
			CurrentSession.setAttribute(ICBulkTransferAction.SESSION_OBJECT_TRANSFER_ENTRY, entry);
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass + "?"
					+ "Status=" + sStatus
					+ "&" + PARAM_BULK_TANSFER_TOTAL_NUMBER_OF_ROWS + "=" + Integer.toString(iNewNumberOfRows)
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
		}
		
		//Process a 'restock' command:
		if (sCommand.compareToIgnoreCase(ICBulkTransferEdit.PARAM_COMMAND_RESTOCK_FROM_SHIPMENTS) == 0){
			String sStartingDate = clsDateAndTimeConversions.stdDateStringToSQLDateString(
					clsManageRequestParameters.get_Request_Parameter(
							ICBulkTransferEdit.STARTING_DATE_FIELD, request).trim()		
			) + " 00:00:00";
			String sRestockingFromLocation = clsManageRequestParameters.get_Request_Parameter(
					ICBulkTransferEdit.RESTOCK_FROM_LOCATION_PARAMETER, request).trim();
			ArrayList<String>arrLocations = new ArrayList<String>(0);
		    Enumeration<String> paramLocationNames = request.getParameterNames();
		    String sParamLocationName = "";
		    String sLocationMarker = ICBulkTransferEdit.LOCATION_PARAMETER;
		    while(paramLocationNames.hasMoreElements()) {
		    	sParamLocationName = paramLocationNames.nextElement();
			  if (sParamLocationName.contains(sLocationMarker)){
				  arrLocations.add(sParamLocationName.substring(
						  sParamLocationName.indexOf(sLocationMarker) + sLocationMarker.length()));
			  }
		    }
		    Collections.sort(arrLocations);
		    String sLocationQueryString = "";
		    for (int i = 0; i < arrLocations.size(); i++){
		    	sLocationQueryString += "&" + ICBulkTransferEdit.LOCATION_PARAMETER + arrLocations.get(i);
		    }
		     
			//Do the restock here:
			try {
				restockLocations(entry, sDBID, sStartingDate, sRestockingFromLocation, arrLocations);
				
			} catch (Exception e) {
				CurrentSession.setAttribute(ICBulkTransferAction.SESSION_OBJECT_TRANSFER_ENTRY, entry);
				String sWarning = e.getMessage();
				if (sWarning.length() > 512){
					sWarning = sWarning.substring(0, 512) + "...";
				}
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass + "?"
						+ PARAM_BULK_TANSFER_TOTAL_NUMBER_OF_ROWS + "=" + clsManageRequestParameters.get_Request_Parameter(PARAM_BULK_TANSFER_TOTAL_NUMBER_OF_ROWS, request)
						+ "&" + ICBulkTransferEdit.STARTING_DATE_FIELD + " = " 
							+ clsManageRequestParameters.get_Request_Parameter(ICBulkTransferEdit.STARTING_DATE_FIELD, request)
						+ sLocationQueryString
						+ "&" + "Warning=" + sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
			}
			CurrentSession.setAttribute(ICBulkTransferAction.SESSION_OBJECT_TRANSFER_ENTRY, entry);
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass + "?"
					+ "Status=" + "Restocked location(s)"
					+ "&" + PARAM_BULK_TANSFER_SHOWINFOFIELDS + "=Y"
					+ "&" + PARAM_BULK_TANSFER_TOTAL_NUMBER_OF_ROWS + "=" + clsManageRequestParameters.get_Request_Parameter(PARAM_BULK_TANSFER_TOTAL_NUMBER_OF_ROWS, request)
					+ "&" + ICBulkTransferEdit.STARTING_DATE_FIELD + " = " 
						+ clsManageRequestParameters.get_Request_Parameter(ICBulkTransferEdit.STARTING_DATE_FIELD, request)
					+ sLocationQueryString
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
		}
		return;
	}
	private String createBatch(ICEntry entry, String sDBID, String sUserID, String sUserFullName) throws Exception{
		
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(getServletContext(), sDBID, "MySQL", sDBID);
		} catch (Exception e) {
			throw new Exception("Error [1475626193] getting connection - " + e.getMessage());
		}
		
		ICEntryBatch batch = new ICEntryBatch("-1");
		batch.sBatchDescription("Bulk transfers");
		batch.iBatchType(ICBatchTypes.IC_TRANSFER);
		batch.sSetCreatedByFullName(sUserFullName);
		batch.sSetCreatedByID(sUserID);
		
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080772]");
			throw new Exception("Error [1475168409] starting data transaction.");
		}
		
		if (!batch.save_without_data_transaction(conn, sUserFullName, sUserID)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080773]");
			throw new Exception("Error [1475168408] - could not save transfer batch - " + batch.getErrorMessages());
		}
		
		entry.lBatchNumber(batch.lBatchNumber());
		entry.sBatchType(batch.sBatchType());
		clsDBServerTime st = new clsDBServerTime(conn);
		entry.sDocNumber(st.getCurrentDateTimeInSelectedFormat("yyyyMMdd HH:mm:ss") + "-BULK");
		entry.sEntryDescription("Bulk transfer - " + sUserFullName);
		//entry.sEntryNumber("1");
		entry.sEntryType(Integer.toString(ICEntryTypes.TRANSFER_ENTRY));

		if(!entry.save_without_data_transaction(conn, sUserID)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080774]");
			throw new Exception("Error [1475177439] saving entry - " + entry.getErrorMessage());
		}
		//System.out.println("[1475181785] got here");
		if(!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080775]");
			throw new Exception("Error [1475177449] committing transaction.");
		}
		//System.out.println("[1475181786] got here");
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080776]");
		return batch.sBatchNumber();
		
	}

	private void validateEntryTransfers(ICEntry entry, String sDBID) throws Exception{
		
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".validateEntryTransfers")
			;
		} catch (Exception e) {
			throw new Exception("Error [1475607063] getting connection - " + e.getMessage());
		}
		
		String sLineErrors = "";
		entry.remove_zero_amount_lines();
		for (int i = 0; i < entry.getLineCount(); i++){
			if (!entry.validateSingleLine(entry.getLineByIndex(i), conn)){
				sLineErrors += entry.getErrorMessage() + ", ";
			}
		}
		
		if (sLineErrors.compareToIgnoreCase("") != 0){
			throw new Exception(sLineErrors);
		}
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080777]");
		return;
		
	}
	
	private void restockLocations(
		ICEntry entry, 
		String sDBID, 
		String sStartingDate, 
		String sRestockFromLocation,
		ArrayList <String>sLocations) throws Exception{
		
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".restockLocations")
			;
		} catch (Exception e) {
			throw new Exception("Error [1475783185] getting connection - " + e.getMessage());
		}
		
		//System.out.println("[1475783285] - sStartingDate = '" + sStartingDate + "'");
		//System.out.println("[1475783286] - sLocationsString = '" + sLocationsString + "'");
		
		entry.remove_zero_amount_lines();

		String SQL = "";
		//For each location selected, read what was shipped since the start date:
		for (int i = 0; i < sLocations.size(); i++){
			SQL = "SELECT"
				+ " " + SMTableicentrylines.TableName + "." + SMTableicentrylines.bdqty
				+ ", " + SMTableicentrylines.TableName + "." + SMTableicentrylines.sitemnumber
				+ ", " + SMTableicentrylines.TableName + "." + SMTableicentrylines.slocation
				+ " FROM " + SMTableicentrylines.TableName
				+ " LEFT JOIN " + SMTableicbatchentries.TableName
				+ " ON " + SMTableicentrylines.TableName + "." + SMTableicentrylines.lentryid + "=" + SMTableicbatchentries.TableName + "." + SMTableicbatchentries.lid
				+ " LEFT JOIN " + ICEntryBatch.TableName
				+ " ON " + SMTableicentrylines.TableName + "." + SMTableicentrylines.lbatchnumber
				+ "=" + ICEntryBatch.TableName + "." + ICEntryBatch.lbatchnumber
				+ " LEFT JOIN " + SMTableicitems.TableName
				+ " ON " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + " = "
				+ SMTableicentrylines.TableName + "." + SMTableicentrylines.sitemnumber
				+ " WHERE ("
					+ "(" + ICEntryBatch.TableName + "." + ICEntryBatch.datpostdate + " > '" + sStartingDate + "')"
					+ " AND (" + SMTableicentrylines.TableName + "." + SMTableicentrylines.slocation + " = '" + sLocations.get(i) + "')"
					+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.ilaboritem + " = 0)"
					+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.inonstockitem + " = 0)"
					+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sDedicatedToOrderNumber + " = '')"
					//We only want negative qtys (shipments/invoices) not returns/credit notes)):
					+ " AND (" + SMTableicentrylines.TableName + "." + SMTableicentrylines.bdqty + " < 0.0000)"
					//We only want entry types that are SHIPMENTS:
					+ " AND (" + SMTableicbatchentries.TableName + "." + SMTableicbatchentries.ientrytype + " = " + Integer.toString(ICEntryTypes.SHIPMENT_ENTRY) + ")"
				+ ")"
				+ " ORDER BY " + ICEntryBatch.TableName + "." + ICEntryBatch.datpostdate 
				+ ", " +  SMTableicentrylines.TableName + "." + SMTableicentrylines.lentrynumber
				+ ", " +  SMTableicentrylines.TableName + "." + SMTableicentrylines.llinenumber
			;
			//System.out.println("[1475892433] - SQL = '" + SQL + "'");
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				while (rs.next()){
					ICEntryLine line = new ICEntryLine();
					line.sItemNumber(rs.getString(SMTableicentrylines.TableName + "." + SMTableicentrylines.sitemnumber));
					//The shipments have negative qtys, but we want the transfers that replace them to have POSITIVE qtys -
					//so we reverse the sign on them:
					line.setQtyString(clsManageBigDecimals.BigDecimalToScaledFormattedString(
						4, rs.getBigDecimal(SMTableicentrylines.TableName + "." + SMTableicentrylines.bdqty).multiply(new BigDecimal("-1"))));
					line.sLocation(sRestockFromLocation);
					line.sTargetLocation(sLocations.get(i));
					entry.add_line(line);
				}
				rs.close();
			} catch (Exception e) {
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080778]");
				throw new Exception("Error [1475787304] reading shipments with SQL '" + SQL + "' - " + e.getMessage());
			}
		}
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080779]");
		return;
		
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}

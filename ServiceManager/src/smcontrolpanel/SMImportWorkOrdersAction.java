package smcontrolpanel;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smic.ICItem;
import SMClasses.SMLogEntry;
import SMClasses.SMOrderDetail;
import SMClasses.SMOrderHeader;
import SMClasses.SMWorkOrderHeader;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTableworkorderdetails;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMImportWorkOrdersAction extends HttpServlet{
	public static final String WORK_ORDER_EMAIL_SUBJECT = "Work Order Receipt";
	private static final long serialVersionUID = 1L;
	private boolean bDebugMode = false;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.SMEditWorkOrders, request)){return;}
		
	    String sWarning = "";
	    String sStatus = "Work orders were successfully imported";

	    //Get the ship location and category for the work order(s):
	    String sLocation = clsManageRequestParameters.get_Request_Parameter(SMImportWorkOrdersEdit.LOCATION_LIST, request);
	    String sCategory = clsManageRequestParameters.get_Request_Parameter(SMImportWorkOrdersEdit.CATEGORY_LIST, request);
	    //Ship after importing?
	    boolean bDoNotShipExistingItems = request.getParameter(SMImportWorkOrdersEdit.DO_NOT_SHIP_EXISTING_ITEMS_CHECKBOX_NAME) != null;
	    ArrayList<String>arrWorkOrders = new ArrayList<String>(0);
	    //Get the work orders we want to import into a string array:
		Enumeration<String> paramNames = request.getParameterNames();
	    String sMarker = SMImportWorkOrdersEdit.CHECKBOX_BASE;
	    while(paramNames.hasMoreElements()) {
	      String sParamName = paramNames.nextElement();
		  if (sParamName.contains(sMarker)){
			  String sWorkOrderID = sParamName.substring(sMarker.length() + SMImportWorkOrdersEdit.LENGTH_OF_LINE_COUNTER_PADDING);
			  arrWorkOrders.add(sWorkOrderID);
			  if (bDebugMode){
				  System.out.println("[1395410918] sWorkOrderID = " + sWorkOrderID);
			  }
		  }
	    }
	    Collections.sort(arrWorkOrders);
	    for (int i = 0; i < arrWorkOrders.size();i++){
	    	try {
				importWorkOrder(arrWorkOrders.get(i), sLocation, sCategory, smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName(), bDoNotShipExistingItems);
			} catch (Exception e) {
				sWarning = "Error importing work order number " + Long.toString(Long.parseLong(arrWorkOrders.get(i))) + " - " + e.getMessage();
				sStatus = "";
				break;
			}
	    }
		smaction.redirectAction(
			sWarning, 
			sStatus,
			SMTableorderheaders.strimmedordernumber + "=" 
				+ clsManageRequestParameters.get_Request_Parameter(SMTableorderheaders.strimmedordernumber, request)
		);
	    return;
	}
	private void importWorkOrder(
		String sWorkOrderID, 
		String sLocation, 
		String sCategory,
		String sConf, 
		String sUserID,
		String sUserFullName,
		boolean bDoNotShipExistingItems) throws Exception{
		
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				sConf, 
				"MySQL", 
				this.toString() + ".importWorkOrder - user: " + sUserFullName
			);
		} catch (Exception e) {
			throw new Exception("Error [1395411913] - " + e.getMessage());
		}

		//Start a data transaction:
		if(!clsDatabaseFunctions.start_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080575]");
			throw new Exception ("Error [1395411914] starting data transaction.");
		}
		try {
			processImport(conn, sConf, sUserID, sUserFullName, sWorkOrderID, sLocation, sCategory, bDoNotShipExistingItems);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080576]");
			throw new Exception(e.getMessage());
		}
		
		if(!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080577]");
			throw new Exception("Error [1395411918] committing data transaction.");
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080578]");
		return;
	}
	private void processImport(
			Connection conn, 
			String sConf, 
			String sUserID,
			String sUserFullName,
			String sWorkOrderID,
			String sLocation,
			String sCategory,
			boolean bDoNotShipExistingItems
			) throws Exception{
		
		if (bDebugMode){
			System.out.println("[1395431097] - 1");
		}
		SMLogEntry log = new SMLogEntry(sConf, getServletContext());
		//Import the work order:
		SMWorkOrderHeader wo = new SMWorkOrderHeader();
		wo.setlid(sWorkOrderID);
		try {
			wo.load(conn);
		} catch (Exception e) {
			throw new Exception("Error [1395411915] loading work order " + sWorkOrderID + " - " + e.getMessage());
		}
		if (bDebugMode){
			System.out.println("[1395431098] - 2");
		}
		SMOrderHeader order = new SMOrderHeader();
		order.setM_strimmedordernumber(wo.getstrimmedordernumber());
		if(!order.load(conn)){
			throw new Exception("Error [1395411916] loading order " + wo.getstrimmedordernumber() + " - " + order.getErrorMessages());
		}
		if (bDebugMode){
			System.out.println("[1395431099] - 3");
		}
		
		//Get the work performed codes from the work order:
		String sWorkPerformedCodes = "";
		for (int i = 0; i < wo.getDetailCount(); i++){
			if (
				(wo.getDetailByIndex(i).getsworkperformed().compareToIgnoreCase("") != 0)
				&& (wo.getDetailByIndex(i).getsdetailtype().compareToIgnoreCase(
					Integer.toString(SMTableworkorderdetails.WORK_ORDER_DETAIL_TYPE_WORKPERFORMED)) == 0)
			){
				sWorkPerformedCodes += wo.getDetailByIndex(i).getsworkperformed() + "\n";
			}
		}
		//Strip off the last newline character:
		if (sWorkPerformedCodes.compareToIgnoreCase("") != 0){
			sWorkPerformedCodes = sWorkPerformedCodes.substring(0, sWorkPerformedCodes.length() - "\n".length());
		}
		
		//First, update the items that are already on the order:
		ArrayList<String>arrDetailNumbers = new ArrayList<String>(0);
		ArrayList<String>arrQtys = new ArrayList<String>(0);
		
		//We only need to do this IF we are shipping the existing items:
		if (!bDoNotShipExistingItems){
			for(int i = 0;i <wo.getDetailCount(); i++){
				//If it's an existing line on the order, update the shipped qty:
				int iDetailNumber;
				try {
					iDetailNumber = Integer.parseInt(wo.getDetailByIndex(i).getsorderdetailnumber());
				} catch (Exception e) {
					throw new Exception("Error [1395411917] parsing detail number: " + wo.getDetailByIndex(i).getsorderdetailnumber());
				}
				if (
					(iDetailNumber > 0)
					&& (wo.getDetailByIndex(i).getsdetailtype().compareToIgnoreCase(
							Integer.toString(SMTableworkorderdetails.WORK_ORDER_DETAIL_TYPE_ITEM)) == 0)
				){
					arrDetailNumbers.add(wo.getDetailByIndex(i).getsorderdetailnumber());
					arrQtys.add(wo.getDetailByIndex(i).getsbdquantity());
				}
			}
			if (bDebugMode){
				System.out.println("[1395431100] - 4");
			}
			//If we DID have any lines that matched the order details, then ship those order details:
			if (arrDetailNumbers.size() > 0){
				try {
					order.shipWorkOrderLinesForExistingDetails(
						arrDetailNumbers,
						arrQtys,
						wo,
						wo.getmechid(),
						wo.getmechanicsname(),
						wo.getmechanicsinitials(),
						sWorkPerformedCodes,
						sLocation,
						sCategory,
						conn, 
						sConf,
						getServletContext(), 
						sUserID,
						sUserFullName
					);
				} catch (Exception e1) {
					throw new Exception ("Error shipping work order lines - " + e1.getMessage() + ".");
				}
			}
			if (bDebugMode){
				System.out.println("[1395431101] - 5");
			}
		}
		//Now add the new lines to the order:
		boolean bWPCsHaveAlreadyBeenAdded;
		if (arrDetailNumbers.size() > 0){
			bWPCsHaveAlreadyBeenAdded = true;
		}else{
			bWPCsHaveAlreadyBeenAdded = false;
		}
		for(int i = 0;i <wo.getDetailCount(); i++){
			//If it's NOT an existing line on the order, update the shipped qty:
			int iDetailNumber;
			try {
				iDetailNumber = Integer.parseInt(wo.getDetailByIndex(i).getsorderdetailnumber());
			} catch (Exception e) {
				throw new Exception("Error [1395411927] parsing detail number: " + wo.getDetailByIndex(i).getsorderdetailnumber());
			}
			//If we are using the location from the work order details on every line, then we need to
			//get the location from every line:
			String sLineLocation = "";
			if (sLocation.compareToIgnoreCase(SMImportWorkOrdersEdit.LOCATION_OPTION_USE_WORK_ORDER_LOCATIONS) == 0){
				sLineLocation = wo.getDetailByIndex(i).getslocationcode();
			//But if the person importing the work order chose a particular location, then all of the work order
			//lines will carry THAT location when they are placed on the order:
			}else
				sLineLocation = sLocation;
			if (
				(iDetailNumber <= 0) && 
				(wo.getDetailByIndex(i).getsdetailtype().compareToIgnoreCase(
					Integer.toString(SMTableworkorderdetails.WORK_ORDER_DETAIL_TYPE_ITEM)) == 0)){
				SMOrderDetail line = new SMOrderDetail();
				ICItem item = new ICItem(wo.getDetailByIndex(i).getsitemnumber());
				if (!item.load(conn)){
					throw new Exception("Error [1395420406] loading item number '" + wo.getDetailByIndex(i).getsitemnumber() 
						+ "' - " + item.getErrorMessageString());
				}
				line.setM_bdEstimatedUnitCost("0.00");
				line.setM_datDetailExpectedShipDate(wo.getdattimedone());
				line.setM_datLineBookedDate(wo.getdattimedone());
				line.setM_dExtendedOrderCost("0.00");
				line.setM_dOrderUnitCost("0.00");
				line.setM_dOriginalQty(wo.getDetailByIndex(i).getsbdquantity().replace(",", ""));
				line.setM_dQtyOrdered(wo.getDetailByIndex(i).getsbdquantity().replace(",", ""));
				line.setM_dQtyShipped(wo.getDetailByIndex(i).getsbdquantity().replace(",", ""));
				line.setM_dQtyShippedToDate("0.00");
				if (item.getNonStockItem().compareToIgnoreCase("1") == 0){
					line.setM_iIsStockItem("0");
				}else{
					line.setM_iIsStockItem("1");
				}
				line.setM_iprintondeliveryticket("0");
				line.setM_isuppressdetailoninvoice("0");
				line.setM_iTaxable(item.getTaxable());
				if (bWPCsHaveAlreadyBeenAdded == false){
					line.setM_mInvoiceComments(sWorkPerformedCodes);
					bWPCsHaveAlreadyBeenAdded = true;
				}else{
					line.setM_mInvoiceComments("");
				}
				line.setM_mTicketComments("");
				line.setM_sItemCategory(sCategory);
				line.setM_sItemDesc(wo.getDetailByIndex(i).getsitemdesc());
				line.setM_sItemNumber(wo.getDetailByIndex(i).getsitemnumber());
				line.setM_sLabel("");
				line.setM_sLocationCode(sLineLocation);
				line.setM_sMechFullName(wo.getmechanicsname());
				line.setM_sMechInitial(wo.getmechanicsinitials());
				//line.setM_sMechSSN(wo.get_ssn()); //MechSSNSCO
				line.setM_sMechID(wo.getmechid());
				line.setM_sOrderUnitOfMeasure(item.getCostUnitOfMeasure());
				line.setM_strimmedordernumber(wo.getstrimmedordernumber());
				//this sets the unit AND the extended price:
				order.updateLinePrice(line, conn);
				if(wo.getDetailByIndex(i).getssetpricetozero().compareToIgnoreCase("1") == 0) {
					line.setM_dOrderUnitPrice("0.00");
					line.setM_dExtendedOrderPrice("0.00");
				}			
				try {
					order.addNewDetailLine_wo_transaction(line, sUserID, sUserFullName, conn);
				} catch (Exception e) {
					throw new Exception(e.getMessage());
				}
			}
		}
		if (bDebugMode){
			System.out.println("[1395431104] - 8");
		}
		wo.setsimported("1");
		try {
			wo.save_import_without_data_transaction(conn, sUserID, sUserFullName, log, getServletContext());
		} catch (Exception e) {
			throw new Exception("Error [1395689582] - " + e.getMessage());
		}
		return;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

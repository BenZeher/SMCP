package smcontrolpanel;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.SMInvoice;
import SMClasses.SMOrderDetail;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTableproposals;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMOrderDetailListAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.SMEditOrders)){return;}
	    //Read the entry fields from the request object:
		SMOrderHeader entry = new SMOrderHeader(request);
		//smaction.getCurrentSession().setAttribute(SMOrderHeader.ParamObjectName, entry);
		smaction.getCurrentSession().removeAttribute(SMOrderHeader.ParamObjectName);
    	//If it's a request to add a line:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMOrderDetailList.LINECOMMAND_FLAG, request).compareToIgnoreCase(
					SMOrderDetailList.LINECOMMANDADDLINE_LABEL) == 0){
    		
    		//Get info from the last line:
    		String SQL = "SELECT"
    			+ " " + SMTableorderdetails.sLabel
    			+ " FROM " + SMTableorderdetails.TableName
    			+ " WHERE ("
    				+ "(" + SMTableorderdetails.strimmedordernumber + " = '" + entry.getM_strimmedordernumber() + "')"
    			+ ") ORDER BY " + SMTableorderdetails.iLineNumber + " DESC LIMIT 1"
    		;
    		String sLastSiteLabel = "";
    		try {
    			ResultSet rsDetails = clsDatabaseFunctions.openResultSet(
    				SQL, 
    				getServletContext(), 
    				smaction.getsDBID(),
    				"MySQL",
    				smaction.getCallingClass() + " - adding line"
    				+ "[1331737072]"
    			);
				if (rsDetails.next()){
					sLastSiteLabel = rsDetails.getString(SMTableorderdetails.sLabel).trim();
				}
				rsDetails.close();
			} catch (SQLException e) {
				smaction.redirectAction(
						"Error reading label from last line - " + e.getMessage(), 
						"", 
						SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
					);
				return;
			}
    		
    		String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
			+ "smcontrolpanel.SMEditOrderDetailEdit"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			+ "&" + SMOrderDetail.ParamiDetailNumber + "=-1"
			+ "&" + SMOrderDetail.Paramstrimmedordernumber + "=" 
				+ clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramstrimmedordernumber, request)
			+ "&" + SMOrderDetail.ParamiLineNumber + "=-1"
			+ "&" + SMEditOrderDetailEdit.LASTSITELABEL_PARAM + "=" + clsServletUtilities.URLEncode(sLastSiteLabel)
			+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=Y"
			+ "&" + "CallingClass=" + smaction.getCallingClass()
			;
			redirectProcess(sRedirectString, response);
			
			return;
    	}
    	//If it's a request to delete lines:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMOrderDetailList.LINECOMMAND_FLAG, request).compareToIgnoreCase(
					SMOrderDetailList.LINECOMMANDDELETE_LABEL) == 0){

			//First, get the list of detail numbers to be deleted into an array:
			ArrayList<String>arrDetailNumbersToDelete = new ArrayList<String>(0);
			Enumeration<String> paramNames = request.getParameterNames();
		    String sMarker = SMOrderDetailList.LINEDETAILBASE;
		    while(paramNames.hasMoreElements()) {
		      String sParamName = paramNames.nextElement();
			  if (sParamName.contains(sMarker)){
				  arrDetailNumbersToDelete.add(sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
			  }
		    }
		    SMOrderHeader order = new SMOrderHeader();
		    try {
				order.processLineDeletions(
					clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramstrimmedordernumber, request),
					arrDetailNumbersToDelete, 
					smaction.getsDBID(), 
					getServletContext(), 
					smaction.getUserID(),
					smaction.getFullUserName()
					
						);
			} catch (SQLException e) {
				smaction.redirectAction(
						"Error deleting lines - " + e.getMessage(), 
						"", 
						SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
					);
				return;
			}
			smaction.redirectAction(
				"", 
				"Successfully deleted order lines", 
				SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
			);
			return;
    	}

    	//If it's a request to go to the header:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMOrderDetailList.LINECOMMAND_FLAG, request).compareToIgnoreCase(
					SMOrderDetailList.LINECOMMANDHEADER_LABEL) == 0){
    	
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
			+ "smcontrolpanel.SMEditOrderEdit"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			+ "&" + SMTableorderheaders.strimmedordernumber + "=" + entry.getM_strimmedordernumber()
			+ "&CallingClass=" + smaction.getCallingClass()
			;
			redirectProcess(sRedirectString, response);
			return;
    	}
    	
    	//If it's a request to invoice:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMOrderDetailList.LINECOMMAND_FLAG, request).compareToIgnoreCase(
					SMOrderDetailList.ORDERINVOICE_BUTTON_LABEL) == 0){
    		
       		Connection conn = clsDatabaseFunctions.getConnection(
       			getServletContext(), 
       			smaction.getsDBID(), 
       			"MySQL", 
       			SMUtilities.getFullClassName(this.toString() + ".invoicing - user: " + smaction.getUserID()
       			+ " - " + smaction.getFullUserName()));
    		if (conn == null){
    			smaction.getCurrentSession().setAttribute(SMOrderHeader.ParamObjectName, entry);
				smaction.redirectAction(
						"Could not get data connection.", 
						"", 
						SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
				);
				return;
    		}
    		if (entry.validate_for_invoicing(conn)){
    			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080594]");
				String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
				+ "smcontrolpanel.SMCreateMultipleInvoicesSelection"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&" + SMCreateMultipleInvoicesSelection.CREATE_SINGLE_INVOICE_PARAM + "=Y"
				+ "&" + SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
				+ "&" + SMCreateMultipleInvoicesSelection.LIST_ORDERS_TO_INVOICE_PARAM + "=Y"
				+ "&CallingClass=" + smaction.getCallingClass()
				;
				redirectProcess(sRedirectString, response);
				return;
    		}else{
    			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080595]");
				smaction.getCurrentSession().setAttribute(SMOrderHeader.ParamObjectName, entry);
				smaction.redirectAction(
				entry.getErrorMessages(), 
				"", 
				SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
				);
				return;
    		}
    	}
    	
    	//If it's a request to go to the totals:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMOrderDetailList.LINECOMMAND_FLAG, request).compareToIgnoreCase(
					SMOrderDetailList.LINECOMMANDTOTALS_LABEL) == 0){
    	
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
			+ "smcontrolpanel.SMEditOrderTotalsEdit"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			+ "&" + SMTableorderheaders.strimmedordernumber + "=" + entry.getM_strimmedordernumber()
			+ "&CallingClass=" + smaction.getCallingClass()
			;
			redirectProcess(sRedirectString, response);
			return;
    	}
    	
    	//If it's a request to go to import work orders:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMOrderDetailList.LINECOMMAND_FLAG, request).compareToIgnoreCase(
					SMOrderDetailList.LINECOMMANDIMPORTWORKORDERS_LABEL) == 0){
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
			+ "smcontrolpanel.SMImportWorkOrdersEdit"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			+ "&" + SMTableorderheaders.strimmedordernumber + "=" + entry.getM_strimmedordernumber()
			+ "&CallingClass=" + smaction.getCallingClass()
			;
			redirectProcess(sRedirectString, response);
			return;
    	}
    	
    	//If it's a request to go to a proposal:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMOrderDetailList.LINECOMMAND_FLAG, request).compareToIgnoreCase(
    					SMOrderDetailList.LINECOMMANDGOTOPROPOSAL_LABEL) == 0){
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
			+ "smcontrolpanel.SMProposalEdit"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			+ "&" + SMTableproposals.strimmedordernumber + "=" + entry.getM_strimmedordernumber()
			+ "&CallingClass=" + smaction.getCallingClass()
			;
			redirectProcess(sRedirectString, response);
			return;
    	}
    	
    	//If it's a request to insert a line:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMOrderDetailList.LINECOMMAND_FLAG, request).compareToIgnoreCase(
					SMOrderDetailList.LINECOMMANDINSERTLINE_LABEL) == 0){

			//First, get the list of detail numbers which are selected (there should only be one):
			ArrayList<String>arrSelectedDetailNumbers = new ArrayList<String>(0);
			Enumeration<String> paramNames = request.getParameterNames();
		    String sMarker = SMOrderDetailList.LINEDETAILBASE;
		    while(paramNames.hasMoreElements()) {
		      String sParamName = paramNames.nextElement();
			  if (sParamName.contains(sMarker)){
				  arrSelectedDetailNumbers.add(sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
			  }
		    }
    		
		    //We have to get the door label from the last line BEFORE the line we are inserting above:
    		//Get info from the last line:
    		String SQL = "SELECT"
    			+ " " + SMTableorderdetails.iLineNumber
    			+ ", " + SMTableorderdetails.iDetailNumber
    			+ ", " + SMTableorderdetails.sLabel
    			+ " FROM " + SMTableorderdetails.TableName
    			+ " WHERE ("
    				+ "(" + SMTableorderdetails.strimmedordernumber + " = '" + entry.getM_strimmedordernumber() + "')"
    			+ ") ORDER BY " + SMTableorderdetails.iLineNumber
    		;
    		String sLastSiteLabel = "";
    		try {
    			ResultSet rsDetails = clsDatabaseFunctions.openResultSet(
    				SQL, 
    				getServletContext(), 
    				smaction.getsDBID(),
    				"MySQL",
    				smaction.getCallingClass() + " - inserting line"
    				+ " [1331737163]"
    			);
    			String sLabelOnLastRecord = "";
				while (rsDetails.next()){
					if (Integer.toString(rsDetails.getInt(SMTableorderdetails.iDetailNumber)).compareToIgnoreCase(
						arrSelectedDetailNumbers.get(0)) == 0){
						sLastSiteLabel = sLabelOnLastRecord;
						break;
					}
					sLabelOnLastRecord = rsDetails.getString(SMTableorderdetails.sLabel).trim();
				}
				rsDetails.close();
			} catch (SQLException e) {
				smaction.redirectAction(
						"Error reading label using line to insert above - " + e.getMessage(), 
						"", 
						SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
					);
				return;
			}
		    
    		String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
			+ "smcontrolpanel.SMEditOrderDetailEdit"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			+ "&" + SMOrderDetail.ParamiDetailNumber + "=-1"
			+ "&" + SMOrderDetail.Paramstrimmedordernumber + "=" 
				+ clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramstrimmedordernumber, request)
			+ "&" + SMOrderDetail.ParamiLineNumber + "=-1"
			+ "&" + SMEditOrderDetailEdit.INSERTNEWLINEABOVEDETAILNUMBER + "=" + arrSelectedDetailNumbers.get(0)
			+ "&" + SMEditOrderDetailEdit.LASTSITELABEL_PARAM + "=" + clsServletUtilities.URLEncode(sLastSiteLabel)
			+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=Y"
			+ "&" + "CallingClass=" + smaction.getCallingClass()
			;
			redirectProcess(sRedirectString, response);
			
			return;
    	}
       	//If it's a request to move lines up:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMOrderDetailList.LINECOMMAND_FLAG, request).compareToIgnoreCase(
					SMOrderDetailList.LINECOMMANDMOVEABOVE_LABEL) == 0){

			//First, get the list of detail numbers to be deleted into an array:
			ArrayList<String>arrDetailNumbersToMove = new ArrayList<String>(0);
			Enumeration<String> paramNames = request.getParameterNames();
		    String sMarker = SMOrderDetailList.LINEDETAILBASE;
		    while(paramNames.hasMoreElements()) {
		      String sParamName = paramNames.nextElement();
			  if (sParamName.contains(sMarker)){
				  arrDetailNumbersToMove.add(sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
			  }
		    }
		    String sLineToMoveAbove = clsManageRequestParameters.get_Request_Parameter(SMOrderDetailList.MOVEABOVESELECT_NAME, request);
		    SMOrderHeader order = new SMOrderHeader();
		    try {
				order.processLineMoves(
					clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramstrimmedordernumber, request),
					arrDetailNumbersToMove,
					sLineToMoveAbove,
					smaction.getsDBID(), 
					getServletContext(), 
					smaction.getUserName(),
					smaction.getUserID(),
					smaction.getFullUserName()
					
						);
			} catch (SQLException e) {
				smaction.redirectAction(
						"Error moving lines up - " + e.getMessage(), 
						"", 
						SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
					);
				return;
			}
			smaction.redirectAction(
				"", 
				"Successfully moved order lines up", 
				SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
			);
			return;
    	}

    	//If it's a request to move lines for drag and drop:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMOrderDetailList.LINECOMMAND_FLAG, request).compareToIgnoreCase(
					SMOrderDetailList.LINECOMMANDMOVE_VALUE) == 0){
    		//TODO move lines
		    try {
				processLineNumbersAfterSorting(
					entry.getM_strimmedordernumber(),
					request,
					smaction.getsDBID(), 
					getServletContext(), 
					smaction.getUserID());
			} catch (Exception e) {
				smaction.redirectAction(
						"Error moving line - " + e.getMessage(), 
						"", 
						SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
					);
				return;
			}
			smaction.redirectAction(
				"", 
				"Successfully moved order line", 
				SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
			);
			return;
    	}

    	//If it's a request to copy certain lines:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMOrderDetailList.LINECOMMAND_FLAG, request).compareToIgnoreCase(
					SMOrderDetailList.LINECOMMANDCOPYABOVE_LABEL) == 0){

			//First, get the list of detail numbers to be deleted into an array:
			ArrayList<String>arrDetailNumbersToCopy = new ArrayList<String>(0);
			Enumeration<String> paramNames = request.getParameterNames();
		    String sMarker = SMOrderDetailList.LINEDETAILBASE;
		    while(paramNames.hasMoreElements()) {
		      String sParamName = paramNames.nextElement();
			  if (sParamName.contains(sMarker)){
				  arrDetailNumbersToCopy.add(sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
			  }
		    }
		    String sLineToCopyAbove = clsManageRequestParameters.get_Request_Parameter(SMOrderDetailList.COPYABOVESELECT_NAME, request);
		    SMOrderHeader order = new SMOrderHeader();
		    try {
				order.processLineCopy(
					clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramstrimmedordernumber, request),
					arrDetailNumbersToCopy,
					sLineToCopyAbove,
					smaction.getsDBID(), 
					getServletContext(), 
					smaction.getUserName(),
					smaction.getUserID(),
					smaction.getFullUserName()
						);
			} catch (SQLException e) {
				smaction.redirectAction(
						"Error copying lines - " + e.getMessage(), 
						"", 
						SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
					);
				return;
			}
			smaction.redirectAction(
				"", 
				"Successfully copied order lines", 
				SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
			);
			return;
    	}

    	//If it's a request to set mechanic:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMOrderDetailList.LINECOMMAND_FLAG, request).compareToIgnoreCase(
					SMOrderDetailList.LINECOMMANDSETMECHANIC_LABEL) == 0){

			//First, get the list of detail numbers to be deleted into an array:
			ArrayList<String>arrDetailNumbers = new ArrayList<String>(0);
			Enumeration<String> paramNames = request.getParameterNames();
		    String sMarker = SMOrderDetailList.LINEDETAILBASE;
		    while(paramNames.hasMoreElements()) {
		      String sParamName = paramNames.nextElement();
			  if (sParamName.contains(sMarker)){
				  arrDetailNumbers.add(sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
			  }
		    }
		    String sSelectedMechanicID = clsManageRequestParameters.get_Request_Parameter(SMOrderDetailList.SETMECHANICIDSELECT_NAME, request);
		    SMOrderHeader order = new SMOrderHeader();
		    try {
				order.setMechanic(clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramstrimmedordernumber, request),
								  arrDetailNumbers,
								  sSelectedMechanicID,
								  smaction.getsDBID(), 
								  getServletContext(), 
								  smaction.getUserName(),
								  smaction.getUserID(),
								  smaction.getFullUserName()
						);
			} catch (SQLException e) {
				smaction.redirectAction(
						"Error setting mechanics - " + e.getMessage(), 
						"", 
						SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
					);
				return;
			}
			smaction.redirectAction(
				"", 
				"Successfully set detail mechanics", 
				SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
			);
			return;
    	}

    	//If it's a request to set mechanic and ship:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMOrderDetailList.LINECOMMAND_FLAG, request).compareToIgnoreCase(
					SMOrderDetailList.LINECOMMANDSETMECHANICNSHIP_LABEL) == 0){

			//First, get the list of detail numbers to be deleted into an array:
			ArrayList<String>arrDetailNumbers = new ArrayList<String>(0);
			Enumeration<String> paramNames = request.getParameterNames();
		    String sMarker = SMOrderDetailList.LINEDETAILBASE;
		    while(paramNames.hasMoreElements()) {
		      String sParamName = paramNames.nextElement();
			  if (sParamName.contains(sMarker)){
				  arrDetailNumbers.add(sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
			  }
		    }
		    String sSelectedMechanicID = clsManageRequestParameters.get_Request_Parameter(SMOrderDetailList.SETMECHANICIDSELECT_NAME, request);
		    SMOrderHeader order = new SMOrderHeader();
		    try {
				order.setMechanic(clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramstrimmedordernumber, request),
								  arrDetailNumbers,
								  sSelectedMechanicID,
								  smaction.getsDBID(), 
								  getServletContext(), 
								  smaction.getUserName(),
								  smaction.getUserID(),
								  smaction.getFullUserName()
						);
			} catch (SQLException e) {
				smaction.redirectAction(
						"Error setting mechanics - " + e.getMessage(), 
						"", 
						SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
					);
				return;
			}
		    try {
				order.processLineShips(
					clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramstrimmedordernumber, request),
					arrDetailNumbers,
					smaction.getsDBID(), 
					getServletContext(), 
					smaction.getUserName(),
					smaction.getUserID(),
					smaction.getFullUserName()
						);
			} catch (SQLException e) {
				smaction.redirectAction(
						"Error shipping lines - " + e.getMessage(), 
						"", 
						SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
					);
				return;
			}
			
			smaction.redirectAction(
				"", 
				"Successfully set detail mechanics and shipped order lines", 
				SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
			);
			return;
    	}

    	//If it's a request to reprice:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMOrderDetailList.LINECOMMAND_FLAG, request).compareToIgnoreCase(
					SMOrderDetailList.LINECOMMANDREPRICE_BUTTON) == 0){

			//First, get the list of detail numbers to be repriced into an array:
			ArrayList<String>arrDetailNumbers = new ArrayList<String>(0);
			Enumeration<String> paramNames = request.getParameterNames();
		    String sMarker = SMOrderDetailList.LINEDETAILBASE;
		    while(paramNames.hasMoreElements()) {
		      String sParamName = paramNames.nextElement();
			  if (sParamName.contains(sMarker)){
				  arrDetailNumbers.add(sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
			  }
		    }
		    String sSelectedRepriceMethod = clsManageRequestParameters.get_Request_Parameter(SMOrderDetailList.REPRICESELECT_NAME, request);
		    String sRepriceAmt = clsManageRequestParameters.get_Request_Parameter(SMOrderDetailList.REPRICEAMOUNT_PARAM, request);
		    SMOrderHeader order = new SMOrderHeader();
		    try {
				order.repriceQuote(clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramstrimmedordernumber, request),
					  arrDetailNumbers,
					  sSelectedRepriceMethod,
					  sRepriceAmt,
					  smaction.getsDBID(), 
					  getServletContext(), 
					  smaction.getUserName(),
					  smaction.getUserID(),
					  smaction.getFullUserName()
						);
			} catch (SQLException e) {
				smaction.redirectAction(
					"Error repricing quote - " + e.getMessage(), 
					"", 
					SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
					);
				return;
			}
			smaction.redirectAction(
				"", 
				"Successfully repriced quote", 
				SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
			);
			return;
    	}

       	//If it's a request to ship lines:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMOrderDetailList.LINECOMMAND_FLAG, request).compareToIgnoreCase(
					SMOrderDetailList.LINECOMMANDSHIP_LABEL) == 0){

			//First, get the list of detail numbers to be shipped into an array:
			ArrayList<String>arrDetailNumbersToShip = new ArrayList<String>(0);
			Enumeration<String> paramNames = request.getParameterNames();
		    String sMarker = SMOrderDetailList.LINEDETAILBASE;
		    while(paramNames.hasMoreElements()) {
		      String sParamName = paramNames.nextElement();
			  if (sParamName.contains(sMarker)){
				  arrDetailNumbersToShip.add(sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
			  }
		    }
		    SMOrderHeader order = new SMOrderHeader();
		    try {
				order.processLineShips(
					clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramstrimmedordernumber, request),
					arrDetailNumbersToShip,
					smaction.getsDBID(), 
					getServletContext(), 
					smaction.getUserName(),
					smaction.getUserID(),
					smaction.getFullUserName()
						);
			} catch (SQLException e) {
				smaction.redirectAction(
						"Error shipping lines - " + e.getMessage(), 
						"", 
						SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
					);
				return;
			}
			smaction.redirectAction(
				"", 
				"Successfully shipped selected order lines", 
				SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
			);
			return;
    	}

       	//If it's a request to unship lines:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMOrderDetailList.LINECOMMAND_FLAG, request).compareToIgnoreCase(
					SMOrderDetailList.LINECOMMANDUNSHIP_LABEL) == 0){

			//First, get the list of detail numbers to be unshipped into an array:
			ArrayList<String>arrDetailNumbersToUnShip = new ArrayList<String>(0);
			Enumeration<String> paramNames = request.getParameterNames();
		    String sMarker = SMOrderDetailList.LINEDETAILBASE;
		    while(paramNames.hasMoreElements()) {
		      String sParamName = paramNames.nextElement();
		      //System.out.println("sParamname = " + sParamName);
			  if (sParamName.contains(sMarker)){
				  //System.out.println("sSalespersons.add: " + sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
				  arrDetailNumbersToUnShip.add(sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
			  }
		    }
		    SMOrderHeader order = new SMOrderHeader();
		    try {
				order.processLineUnships(
					clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramstrimmedordernumber, request),
					arrDetailNumbersToUnShip, 
					smaction.getsDBID(), 
					getServletContext(), 
					smaction.getUserName(),
					smaction.getUserID(),
					smaction.getFullUserName());
			} catch (SQLException e) {
				smaction.redirectAction(
						"Error unshipping lines - " + e.getMessage(), 
						"", 
						SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
					);
				return;
			}
			smaction.redirectAction(
				"", 
				"Successfully unshipped selected order lines", 
				SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
			);
			return;
		}
    	
       	//If it's a request to create items:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMOrderDetailList.LINECOMMAND_FLAG, request).compareToIgnoreCase(
					SMOrderDetailList.LINECOMMANDCREATEITEM_LABEL) == 0){
			//First, get the list of detail numbers to be created into an array:
			ArrayList<String>arrDetailNumbersToCreateItemsFor = new ArrayList<String>(0);
			Enumeration<String> paramNames = request.getParameterNames();
		    String sMarker = SMOrderDetailList.LINEDETAILBASE;
		    while(paramNames.hasMoreElements()) {
		      String sParamName = paramNames.nextElement();
			  if (sParamName.contains(sMarker)){
				arrDetailNumbersToCreateItemsFor.add(
					clsStringFunctions.PadLeft(sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()), " ", 13)
				);
			  }
		    }
		    Collections.sort(arrDetailNumbersToCreateItemsFor);
		    SMOrderHeader order = new SMOrderHeader();
		    try {
				order.processLineItemCreations(
					clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramstrimmedordernumber, request),
					arrDetailNumbersToCreateItemsFor, 
					smaction.getsDBID(), 
					getServletContext(), 
					smaction.getUserID(),
					smaction.getFullUserName()
						);
			} catch (SQLException e) {
				smaction.redirectAction(
						"Error creating items for lines - " + e.getMessage(), 
						"", 
						SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
					);
				return;
			}
			smaction.redirectAction(
				"", 
				"Successfully created items for selected order lines", 
				SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
			);
			return;
    	}

       	//If it's a request to create a purchase order:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMOrderDetailList.LINECOMMAND_FLAG, request).compareToIgnoreCase(
					SMOrderDetailList.LINECOMMANDCREATEPO_LABEL) == 0){
			//First, get the list of detail numbers into an array:
			Enumeration<String> paramNames = request.getParameterNames();
		    String sMarker = SMOrderDetailList.LINEDETAILBASE;
		    String sDetailNumberList = "";
		    ArrayList <Long> arrDetailNumbers = new ArrayList<Long>(0);
		    while(paramNames.hasMoreElements()) {
		      String sParamName = paramNames.nextElement();
			  if (sParamName.contains(sMarker)){
				  try {
					arrDetailNumbers.add(Long.parseLong(sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()).trim()));
				} catch (NumberFormatException e1) {
					smaction.redirectAction(
						"Error parsing line number - '" +  
						sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()) + ".","",
						SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
						);
					return;
				}
				  //sDetailNumberList += sParamName.substring(sParamName.indexOf(sMarker) 
				//		 + sMarker.length()) + SMCreatePOFromOrderEdit.PARAM_DETAILNUMBERDELIMITER;
				  
			  }
		    }
		    Collections.sort(arrDetailNumbers);
		    for (int i = 0; i < arrDetailNumbers.size(); i++){
		    	sDetailNumberList += Long.toString(arrDetailNumbers.get(i)) + SMCreatePOFromOrderEdit.PARAM_DETAILNUMBERDELIMITER;
		    }
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
				+ "smcontrolpanel.SMCreatePOFromOrderEdit"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&" + SMCreatePOFromOrderEdit.PARAM_ORDERNUMBER + "=" + entry.getM_strimmedordernumber()
				+ "&" + SMCreatePOFromOrderEdit.PARAM_DETAILNUMBERSTRING + "=" + sDetailNumberList
				+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
			;
			redirectProcess(sRedirectString, response);
			return;
    	}

       	//If it's a request to do direct entry:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMOrderDetailList.LINECOMMAND_FLAG, request).compareToIgnoreCase(
					SMOrderDetailList.LINECOMMANDDIRECTENTRY_LABEL) == 0){
    		//First make sure there isn't a leftover direct entry object in the session:
    		if (smaction.getCurrentSession().getAttribute(SMDirectOrderDetailEntry.ParamObjectName) != null){
    			//Remove the object from the session:
    			smaction.getCurrentSession().removeAttribute(SMDirectOrderDetailEntry.ParamObjectName);
    		}
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
				+ "smcontrolpanel.SMEditDirectOrderDetailEntry"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&" + SMDirectOrderDetailEntry.Paramsordernumber + "=" + entry.getM_strimmedordernumber()
				+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
			;
			redirectProcess(sRedirectString, response);
			return;
    	}
    	
       	//If it's a request to paste items into a proposal:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMOrderDetailList.LINECOMMAND_FLAG, request).compareToIgnoreCase(
					SMOrderDetailList.LINECOMMANDPASTEINTOPROPOSAL_LABEL) == 0){

			//First, get the list of detail numbers to be shipped into an array:
			ArrayList<String>arrDetailNumbersToPaste = new ArrayList<String>(0);
			Enumeration<String> paramNames = request.getParameterNames();
		    String sMarker = SMOrderDetailList.LINEDETAILBASE;
		    while(paramNames.hasMoreElements()) {
		      String sParamName = paramNames.nextElement();
			  if (sParamName.contains(sMarker)){
				  arrDetailNumbersToPaste.add(sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
			  }
		    }
		    SMProposal proposal = new SMProposal(clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramstrimmedordernumber, request));
		    try {
		    	proposal.pasteOrderLines(
					clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramstrimmedordernumber, request),
					arrDetailNumbersToPaste, 
					smaction.getsDBID(), 
					getServletContext(), 
					smaction.getUserID(),
					smaction.getFullUserName());
			} catch (Exception e) {
				smaction.redirectAction(
						"Error pasting into proposal - " + e.getMessage(), 
						"", 
						SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
					);
				return;
			}
			smaction.redirectAction(
				"", 
				"Successfully pasted lines into proposal", 
				SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
			);
			return;
    	}
    	
    	
       	//If it's a request to update the invoiceing state:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMOrderDetailList.LINECOMMAND_FLAG, request).contains(
					SMOrderDetailList.INVOICECOMMANDUPDATESTATE_LABEL)){

			//First, get the invoice number from the parameter:
    		String sButtonLabel = clsManageRequestParameters.get_Request_Parameter(SMOrderDetailList.LINECOMMAND_FLAG, request);
    		String sInvoiceToUpdate = "";
    		sInvoiceToUpdate = sButtonLabel.substring(SMOrderDetailList.INVOICECOMMANDUPDATESTATE_LABEL.length(),
    				sButtonLabel.lastIndexOf('*'));
    		String sInvoicingState = sButtonLabel
    				.substring(sButtonLabel.lastIndexOf('*') + 1);  		
    		//System.out.println("Invoice: " + sInvoiceToUpdate + " State: " + sInvoicingState);
		    SMInvoice invoice = new SMInvoice();
		    
		    try {
		    	invoice.updateInvoicingState(
					sInvoiceToUpdate,
					sInvoicingState,
					smaction.getsDBID(), 
					getServletContext(), 
					smaction.getUserID(),
					smaction.getFullUserName());
			} catch (Exception e) {
				smaction.redirectAction(
						"Error updating invoicing state on invoice '" + sInvoiceToUpdate + "' - " + e.getMessage(), 
						"", 
						SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
					);
				return;
			}
			smaction.redirectAction(
				"", 
				"Successfully updated invoice " + sInvoiceToUpdate + " invoicing state.", 
				SMOrderHeader.Paramstrimmedordernumber + "=" + entry.getM_strimmedordernumber()
			);
			return;
    	}
	}
	private void redirectProcess(String sRedirectString, HttpServletResponse res ){
		try {
			res.sendRedirect(sRedirectString);
		} catch (IOException e1) {
			System.out.println("In " + this.toString() + ".redirectAction - error redirecting with string: "
					+ sRedirectString);
			return;
		}
		
	}
	
	public void processLineNumbersAfterSorting(
			String strimmedordernumber,
			HttpServletRequest request,
			String sDBID, 
			ServletContext context, 
			String sUserID) throws Exception{
		
		//Get DB connection
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".updateLineNumbersAfterSorting - userID: " + sUserID);
		} catch (Exception e) {
			throw new Exception("Error [1523639832] getting connection - " + e.getMessage());
			
		}
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080596]");
			throw new Exception("Error [1523639833] - could not start data transaction.");
			
		}	
		String SQL ="";
		
		//Clear all line numbers by making them negative values. 
		//This is so we can check to make sure they were all updated later.
		 SQL = "UPDATE " + SMTableorderdetails.TableName 
				+ " SET " + SMTableorderdetails.iLineNumber + " = " + SMTableorderdetails.iLineNumber + " * -1"
				+ " WHERE " + "(" + SMTableorderdetails.strimmedordernumber + " ='" + strimmedordernumber + "')";
		
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		} catch (Exception ex) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080597]");
			throw new Exception("Error updating order line number with SQL: " + SQL + " - " + ex.getMessage());
		}
		
		// Get the Detail Number and update with new Line Number:
		String sOrginalDetailNumber = "";
		Enumeration<String> paramNames = request.getParameterNames();
		String sMarker = SMOrderDetailList.LINENUMBERMOVEMARKER;
		
		while (paramNames.hasMoreElements()) {
			String sParamName = paramNames.nextElement();

			if (sParamName.contains(sMarker)) {

				sOrginalDetailNumber = sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length());

				//Update line numbers
				 SQL = "UPDATE " + SMTableorderdetails.TableName 
						+ " SET " + SMTableorderdetails.iLineNumber + " = " + request.getParameter(sParamName) 
						+ " WHERE (" 
						+ "(" + SMTableorderdetails.iDetailNumber + " = " + sOrginalDetailNumber + ")"
						+ " AND "
						+ "(" + SMTableorderdetails.strimmedordernumber + " ='" + strimmedordernumber + "')"
						+ ")"
						;
		
				try {
					Statement stmt = conn.createStatement();
					stmt.executeUpdate(SQL);
				} catch (Exception ex) {
					clsDatabaseFunctions.rollback_data_transaction(conn);
					clsDatabaseFunctions.freeConnection(context, conn, "[1547080598]");
					throw new Exception("Error updating po line number with SQL: " + SQL + " - " + ex.getMessage());
				}
			}
		}

		//Make sure all line number have been updated. (no line numbers should still be negative)
		 SQL = "SELECT"
			+ " " + SMTableorderdetails.iLineNumber
			+ " FROM " + SMTableorderdetails.TableName
			+ " WHERE ("
			+ "(" + SMTableorderdetails.strimmedordernumber + " ='" + strimmedordernumber + "')"
			+ " AND "
			+ "(" + SMTableorderdetails.iLineNumber + " <= " + "0" + ")"
			+ ")"
		;
		try {
			ResultSet rsLineNumbers = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rsLineNumbers.next()){
				rsLineNumbers.close();
				clsDatabaseFunctions.rollback_data_transaction(conn);
				clsDatabaseFunctions.freeConnection(context, conn, "[1547080599]");
				throw new Exception("Error updating order line number with SQL: " + SQL);
			}
			rsLineNumbers.close();
		} catch (SQLException e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080600]");
			throw new Exception("Error updating order line number with SQL: " + SQL + " - " + e.getMessage());
		}

		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080601]");
			throw new Exception("Error committing data transaction to update order line numbers");
		}
		
		clsDatabaseFunctions.freeConnection(context, conn, "[1547080602]");	
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
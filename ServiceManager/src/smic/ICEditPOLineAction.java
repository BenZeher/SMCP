package smic;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.FinderResults;
import SMClasses.SMFinderFunctions;
import SMClasses.SMOrderDetail;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTableicpolines;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMMasterEditSelect;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import smgl.GLAccount;

public class ICEditPOLineAction extends HttpServlet{

	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		//String sPONumber = SMUtilities.get_Request_Parameter(ICPOHeader.Paramsponumber, request);
		
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.ICEditPurchaseOrders)){return;}
	    //Read the entry fields from the request object:
		ICPOLine entry = new ICPOLine(request);
		//System.out.println("In " + this.toString() + " line dump = " + entry.read_out_debug_data());
		smaction.getCurrentSession().setAttribute(ICPOLine.ParamObjectName, entry);
		
		//*******************************************************
		//SPECIAL CASES:
		if (request.getParameter(ICEditPOLineEdit.FIND_ITEM_BUTTON) != null){
			//Then call the finder to search for items:
			String sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "SMClasses.ObjectFinder"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&ObjectName=" + FinderResults.SEARCH_ACTIVE_ITEM
				+ "&ResultClass=FinderResults"
				+ "&SearchingClass=" + smaction.getCallingClass()
				+ "&ReturnField=" + "FOUND" + ICPOLine.Paramsitemnumber
				
				+ SMFinderFunctions.getStdITEMSearchAndResultString()
				
				/*
				+ "&SearchField1=" + SMTableicitems.sItemDescription
				+ "&SearchFieldAlias1=Description"
				+ "&SearchField2=" + SMTableicitems.sItemNumber
				+ "&SearchFieldAlias2=Item%20No."
				+ "&SearchField3=" + SMTableicitems.sComment1
				+ "&SearchFieldAlias3=Comment%201"
				+ "&SearchField4=" + SMTableicitems.sComment2
				+ "&SearchFieldAlias4=Comment%202"
				+ "&ResultListField1="  + SMTableicitems.sItemNumber
				+ "&ResultHeading1=Item%20No."
				+ "&ResultListField2="  + SMTableicitems.sItemDescription
				+ "&ResultHeading2=Description"
				+ "&ResultListField3="  + SMTableicitems.sCostUnitOfMeasure
				+ "&ResultHeading3=Cost%20Unit"
				+ "&ResultListField4="  + SMTableicitems.inonstockitem
				+ "&ResultHeading4=Non-stock?"
				+ "&ResultListField5="  + SMTableicitems.sPickingSequence
				+ "&ResultHeading5=Picking%20Sequence"				+ "&ParameterString="
				*/
				
				+ "*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "*RETURNINGFROMFINDER=TRUE"
				+ entry.getQueryString().replace("&", "*")
			;
			//System.out.println("sRedirectString = " + sRedirectString);
			try {
				response.sendRedirect(sRedirectString);
			} catch (IOException e) {
				System.out.println("In " + this.toString() 
					+ "After FINDITEM - error redirecting with string: " + sRedirectString);
				smaction.redirectAction(
					"After FINDITEM - error redirecting with string: " + sRedirectString, 
					"", 
					ICPOLine.Paramlid + "=" + entry.getsID()
					+ "&" + ICPOLine.Paramlpoheaderid + "=" + entry.getspoheaderid()
				);
				return;
			}
			return;
		}
		
		//If it was a request to CREATE a new item:
		if (request.getParameter(ICEditPOLineEdit.CREATE_ITEM_BUTTON) != null){
			if (clsManageRequestParameters.get_Request_Parameter(
				ICPOLine.Paramsitemnumber, request).compareToIgnoreCase("") == 0){
		    	smaction.redirectAction(
		    			"You need to enter an item number to create a new item.", 
		    			"", 
		    			ICPOLine.Paramlid + "=" + entry.getsID()
		    			+ "&" + ICPOLine.Paramlpoheaderid + "=" + entry.getspoheaderid()
		    			);
				return;
			}
			//Go to CREATE a new item:
			String sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditItemsEdit"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&" + ICEditPOLineEdit.CREATE_ITEM_BUTTON + "=Y"
    			+ "&" + ICPOLine.Paramlid + "=" + entry.getsID()
    			+ "&" + ICPOLine.Paramlpoheaderid + "=" + entry.getspoheaderid()
				+ "&" + ICItem.ParamItemNumber + "=" 
					+ clsManageRequestParameters.get_Request_Parameter(ICPOLine.Paramsitemnumber, request)
				+ "&" + ICItem.ParamItemDescription + "=" 
					+ clsManageRequestParameters.get_Request_Parameter(ICPOLine.Paramsitemdescription, request)
				;
			try {
				response.sendRedirect(sRedirectString);
			} catch (IOException e) {
				//System.out.println("In " + this.toString() 
				//	+ "After FINDGLACCOUNT - error redirecting with string: " + sRedirectString);
				smaction.redirectAction(
					"After FINDGLACCOUNT - error redirecting with string: " + sRedirectString, 
					"", 
					ICPOLine.Paramlid + "=" + entry.getsID()
					+ "&" + ICPOLine.Paramlpoheaderid + "=" + entry.getspoheaderid()
				);
				return;
			}
		}
		
		//If it was a request to ADD THE ITEM TO AN ORDER:
		if (request.getParameter(ICEditPOLineEdit.ADD_ITEM_TO_ORDER_BUTTON) != null){
			//Then update the item cost and return to the ICPOEditLine screen:
			String sWarning = "";
			String sResult = "";
			Connection conn = clsDatabaseFunctions.getConnection(
					getServletContext(), 
					smaction.getsDBID(), 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".doPost - user: " 
					+ smaction.getUserName()
					+ " - "
					+ smaction.getFullUserName()
			);
			try {
				sResult = addItemToOrder(entry, smaction.getUserID(), smaction.getFullUserName(), conn);
			} catch (Exception e) {
				sWarning = "Could not add item to order - " + e.getMessage();
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080840]");
			smaction.getCurrentSession().setAttribute(ICPOLine.ParamObjectName, entry);
			smaction.redirectAction(
				sWarning, 
				sResult, 
				ICPOLine.Paramlid + "=" + entry.getsID()
    				+ "&" + ICPOLine.Paramlpoheaderid + "=" + entry.getspoheaderid()
				);
			return;
		}
		
		if (request.getParameter("FINDGLEXPENSEACCT") != null){
			//Then call the finder to search for items:
			String sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "SMClasses.ObjectFinder"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&ObjectName=" + "ACTIVE " + GLAccount.Paramobjectname //We only want ACTIVE accounts listed:
				+ "&ResultClass=FinderResults"
				+ "&SearchingClass=" + smaction.getCallingClass()
				+ "&ReturnField=" + "FOUND" + GLAccount.Paramobjectname
				+ "&SearchField1=" + SMTableglaccounts.sDesc
				+ "&SearchFieldAlias1=Description"
				+ "&SearchField2=" + SMTableglaccounts.sAcctID
				+ "&SearchFieldAlias2=Account%20Number"
				+ "&ResultListField1="  + SMTableglaccounts.sAcctID
				+ "&ResultHeading1=Account%20Number."
				+ "&ResultListField2="  + SMTableglaccounts.sDesc
				+ "&ResultHeading2=Description"
				+ "&ParameterString="
				+ "*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "*RETURNINGFROMFINDER=TRUE"
				+ entry.getQueryString().replace("&", "*")
			;
			//System.out.println("sRedirectString = " + sRedirectString);
			try {
				response.sendRedirect(sRedirectString);
			} catch (IOException e) {
				System.out.println("In " + this.toString() 
					+ "After FINDGLACCOUNT - error redirecting with string: " + sRedirectString);
				smaction.redirectAction(
					"After FINDGLACCOUNT - error redirecting with string: " + sRedirectString, 
					"", 
					ICPOLine.Paramlid + "=" + entry.getsID()
					+ "&" + ICPOLine.Paramlpoheaderid + "=" + entry.getspoheaderid()
				);
				return;
			}
			return;
		}
		
		if (clsManageRequestParameters.get_Request_Parameter(
			ICEditPOLineEdit.GET_MOST_RECENT_COST_BUTTON, request).compareToIgnoreCase("") != 0){
			//Then update the item cost and return to the ICPOEditLine screen:
			Connection conn = clsDatabaseFunctions.getConnection(
					getServletContext(), 
					smaction.getsDBID(), 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".doPost - user: " + smaction.getUserName()
			);
			entry.updateMostRecentCost(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080841]");
			smaction.getCurrentSession().setAttribute(ICPOLine.ParamObjectName, entry);
			smaction.redirectAction(
				"", 
				"", 
				ICPOLine.Paramlid + "=" + entry.getsID()
    				+ "&" + ICPOLine.Paramlpoheaderid + "=" + entry.getspoheaderid()
				);
			return;
		}
		
		//If it's a request to get the vendor's item number:
		if (clsManageRequestParameters.get_Request_Parameter(
				ICEditPOLineEdit.GET_VENDOR_ITEM_NO_BUTTON, request).compareToIgnoreCase("") != 0){
				//Then update the item cost and return to the ICPOEditLine screen:
				Connection conn = clsDatabaseFunctions.getConnection(
						getServletContext(), 
						smaction.getsDBID(), 
						"MySQL", 
						SMUtilities.getFullClassName(this.toString()) + ".doPost - user: " + smaction.getUserName()
				);
				entry.updateVendorItem(conn);
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080842]");
				smaction.getCurrentSession().setAttribute(ICPOLine.ParamObjectName, entry);
				smaction.redirectAction(
					"", 
					"", 
					ICPOLine.Paramlid + "=" + entry.getsID()
	    				+ "&" + ICPOLine.Paramlpoheaderid + "=" + entry.getspoheaderid()
					);
				return;
			}
		
		//Switching between inventory and non-inventory items:
		if (request.getParameter(ICEditPOLineEdit.TOGGLE_NONINVENTORY_BUTTON_NAME) != null){
			//Then set the line to the required value:
			String sToggleLabel = request.getParameter(ICEditPOLineEdit.TOGGLE_NONINVENTORY_BUTTON_NAME);
			String sSwitchStatus = "";
			if (sToggleLabel.compareToIgnoreCase(ICEditPOLineEdit.TOGGLE_TO_NONINVENTORY_LABEL) == 0){
				entry.setsnoninventoryitem("1");
				sSwitchStatus = "Switched to NON-INVENTORY item";
			}else{
				entry.setsnoninventoryitem("0");
				sSwitchStatus = "Switched to INVENTORY item";
			}
			entry.setsextendedordercost("0.00");
			entry.setsextendedreceivedcost("0.00");
			entry.setsglexpenseacct("");
			entry.setsitemdescription("");
			entry.setsitemnumber("");
			entry.setsitemdescription("");
			entry.setsqtyordered("0.0000");
			entry.setsqtyreceived("0.0000");
			entry.setsunitcost("0.00");
			entry.setsunitofmeasure("");
			entry.setsvendorsitemnumber("");

			smaction.getCurrentSession().setAttribute(ICPOLine.ParamObjectName, entry);
			smaction.redirectAction(
				"", 
				sSwitchStatus, 
				ICPOLine.Paramlid + "=" + entry.getsID()
    				+ "&" + ICPOLine.Paramlpoheaderid + "=" + entry.getspoheaderid()
				);
			return;
		}	
		//End special cases
		//*********************************************
		
	    if(smaction.isDeleteRequested()){
		    if (smaction.isDeleteConfirmed()){
			    //Save this now so it's not lost after the delete:
			    String sPOLine = entry.getslinenumber();
			    if (!entry.delete(getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName())){
			    	//System.out.println("In " + this.toString() + " !entry.delete, error = '" + entry.getErrorMessages());
			    	smaction.redirectAction(
			    			"Could not delete: " + entry.getErrorMessages(), 
			    			"", 
			    			ICPOLine.Paramlid + "=" + entry.getsID()
			    			+ "&" + ICPOLine.Paramlpoheaderid + "=" + entry.getspoheaderid()
			    			);
					return;
			    }else{
			    	//If the delete succeeded, the entry will be initialized:
			    	//Re-set the job number in the new, blank entry:
					if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
						smaction.returnToOriginalURL();
					}else{
						smaction.getCurrentSession().setAttribute(ICPOLine.ParamObjectName, entry);
						smaction.redirectAction(
							"", 
							entry.getObjectName() 
							+ ": " + sPOLine + " was successfully deleted.", "");
					}
					return;
			    }
	    	}
	    }
		
		//If it's an update, process that:
	    if(smaction.isEditRequested()){
			if(!entry.save_without_data_transaction(
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserName(),
					smaction.getUserID(),
					smaction.getFullUserName()
					)){
				smaction.getCurrentSession().setAttribute(ICPOLine.ParamObjectName, entry);
				smaction.redirectAction(
					"Could not save: " + entry.getErrorMessages(), 
					"", 
					ICPOLine.Paramlid + "=" + entry.getsID()
	    				+ "&" + ICPOLine.Paramlpoheaderid + "=" + entry.getspoheaderid()
					);
				return;
			}else{
				//If the save succeeded, force the called function to reload it by NOT
				//putting the entry object in the current session, but by passing it
				//in the query string instead:
				if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
					smaction.returnToOriginalURL();
				}else{
					smaction.redirectAction(
						"", 
						entry.getObjectName() + ": " + entry.getslinenumber() + " was successfully saved.",
						entry.getQueryString()
					);
				}
			}
	    }

		//If it's an 'UPDATE AND ADD', process that:
	    if (request.getParameter(ICEditPOLineEdit.UPDATEANDADD_BUTTON) != null){
			if(!entry.save_without_data_transaction(
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserName(),
					smaction.getUserID(),
					smaction.getFullUserName())){
				smaction.getCurrentSession().setAttribute(ICPOLine.ParamObjectName, entry);
				smaction.redirectAction(
					"Could not save: " + entry.getErrorMessages(), 
					"", 
					ICPOLine.Paramlid + "=" + entry.getsID()
	    				+ "&" + ICPOLine.Paramlpoheaderid + "=" + entry.getspoheaderid()
					);
				return;
			}else{
				//If the save succeeded, in this case, give the user the chance to add another PO line:
				smaction.getCurrentSession().removeAttribute(ICPOLine.ParamObjectName);
				response.sendRedirect(
					SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPOLineEdit"
					+ "?" + ICPOLine.Paramlpoheaderid + "=" + entry.getspoheaderid()
					+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=TRUE" //Set this to indicate it's an 'add'
					+ "&CallingClass=" + "ICEditPOEdit"
					+ "&" + ICPOHeader.Paramlid + "=" + entry.getspoheaderid()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				);
				return;
			}
	    }
	    
		//If it's an 'UPDATE AND GO TO NEXT LINE', process that:
	    if (request.getParameter(ICEditPOLineEdit.UPDATEANDGOTONEXT_BUTTON) != null){
			if(!entry.save_without_data_transaction(
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserName(),
					smaction.getUserID(),
					smaction.getFullUserName())){
				smaction.getCurrentSession().setAttribute(ICPOLine.ParamObjectName, entry);
				smaction.redirectAction(
					"Could not save: " + entry.getErrorMessages(), 
					"", 
					ICPOLine.Paramlid + "=" + entry.getsID()
	    				+ "&" + ICPOLine.Paramlpoheaderid + "=" + entry.getspoheaderid()
					);
				return;
			}else{
				//If the save succeeded, in this case, go to edit the next PO line:
				smaction.getCurrentSession().removeAttribute(ICPOLine.ParamObjectName);
				
				int iNextLineNumber = Integer.parseInt(entry.getslinenumber()) + 1;
				//Get the line ID of the next line if there is one:
				String sNextLineID = "0";
				String SQL = "SELECT"
					+ " " + SMTableicpolines.lid
					+ " FROM " + SMTableicpolines.TableName
					+ " WHERE ("
						+ "(" + SMTableicpolines.lpoheaderid + " = " + entry.getspoheaderid() + ")"
						+ " AND (" + SMTableicpolines.llinenumber + " = " + Integer.toString(iNextLineNumber) + ")"
					+ ")"
				;
				try {
					ResultSet rs = ServletUtilities.clsDatabaseFunctions.openResultSet(
						SQL, 
						getServletContext(), 
						smaction.getsDBID(), 
						"MySQL", 
						this.toString() + ", reading next PO line number - user: " + smaction.getFullUserName()
					);
					if (rs.next()){
						sNextLineID = Long.toString(rs.getLong(SMTableicpolines.lid));
					}
					rs.close();
				} catch (SQLException e) {
					//Error reading the next line:
					smaction.getCurrentSession().setAttribute(ICPOLine.ParamObjectName, entry);
					smaction.redirectAction(
						"Error [1572379067] reading next PO line ID - " + e.getMessage(), 
						"", 
						ICPOLine.Paramlid + "=" + entry.getsID()
		    				+ "&" + ICPOLine.Paramlpoheaderid + "=" + entry.getspoheaderid()
						);
					return;
				}
				
				if (sNextLineID.compareToIgnoreCase("0") == 0){
					smaction.getCurrentSession().setAttribute(ICPOLine.ParamObjectName, entry);
					smaction.redirectAction(
						"The line was saved, but there is no next line to edit", 
						"", 
						ICPOLine.Paramlid + "=" + entry.getsID()
		    				+ "&" + ICPOLine.Paramlpoheaderid + "=" + entry.getspoheaderid()
						);
					return;
				}
				
				response.sendRedirect(
					SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPOLineEdit"
					+ "?" + ICPOLine.Paramlpoheaderid + "=" + entry.getspoheaderid()
					+ "&" + ICPOLine.Paramlid + "=" + sNextLineID
					+ "&CallingClass=" + "ICEditPOEdit"
					+ "&" + ICPOHeader.Paramlid + "=" + entry.getspoheaderid()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				);
				return;
			}
	    }

	    //Special case - it's a request to 'Validate' the line:
		//If it's an edit, process that:
	    if(request.getParameter("VALIDATE") != null){
			if(!entry.validate_entry_fields(
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserID(),
					smaction.getFullUserName())){
						smaction.getCurrentSession().setAttribute(ICPOLine.ParamObjectName, entry);
						smaction.redirectAction(
							entry.getErrorMessages(), 
							"", 
							ICPOLine.Paramlid + "=" + entry.getsID()
								+ "&" + ICPOLine.Paramlpoheaderid + "=" + entry.getspoheaderid()
						);
				return;
			}else{
				//If the save succeeded, force the called function to reload it by NOT
				//putting the entry object in the current session, but by passing it
				//in the query string instead:
				smaction.getCurrentSession().setAttribute(ICPOLine.ParamObjectName, entry);
				smaction.redirectAction(
					"",
					"Purchase order line validated successfully.",
					ICPOLine.Paramlid + "=" + entry.getsID()
						+ "&" + ICPOLine.Paramlpoheaderid + "=" + entry.getspoheaderid()
				);
			}
	    	return;
	    }
		return;
	}
	private String addItemToOrder(ICPOLine line, String sUserID, String sUserFullName, Connection conn) throws Exception{
		
		if (line.getsnoninventoryitem().compareToIgnoreCase("1") == 0){
			throw new Exception ("Cannot add a non-inventory item to an order.");
		}
		
		ICItem item = new ICItem(line.getsitemnumber());
		if (!item.load(conn)){
			throw new Exception ("Could not load item number '" + line.getsitemnumber() + "' - " + item.getErrorMessageString());
		}
		
		String sDedicatedToOrder = item.getDedicatedToOrderNumber().trim();
		if (sDedicatedToOrder.compareToIgnoreCase("") == 0){
			throw new Exception ("Item number '" + line.getsitemnumber() + "' is not dedicated to an order.");
		}
		
		SMOrderHeader order = new SMOrderHeader();
		order.setM_strimmedordernumber(sDedicatedToOrder);
		if (!order.load(conn)){
			throw new Exception ("Order number '" + sDedicatedToOrder + "' could not be loaded - " + order.getErrorMessages());
		}
		
		SMOrderDetail detail = new SMOrderDetail();
		//detail.setM_dMostRecentCost(item.getMostRecentCost());
		detail.setM_dOriginalQty(line.getsqtyordered());
		detail.setM_dQtyOrdered(line.getsqtyordered());
		//detail.setM_sICItemComment1(item.getComment1());
		//detail.setM_sICItemComment2(item.getComment2());
		detail.setM_sItemDesc(line.getsitemdescription());
		detail.setM_sItemNumber(line.getsitemnumber());
		detail.setM_sOrderUnitOfMeasure(item.getCostUnitOfMeasure());

		if (!order.addAndSaveOrderDetailFromPO(detail, sUserID, sUserFullName, conn)){
			throw new Exception (order.getErrorMessages());
		}
		return "Item '" + line.getsitemnumber() + "' was successfully added to order number '" 
			+ sDedicatedToOrder + "'.";
		
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
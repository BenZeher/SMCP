package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smap.APVendor;
import smic.ICEditPOEdit;
import smic.ICPOHeader;
import smic.ICPOLine;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

public class SMCreatePOFromOrderAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.ICEditPurchaseOrders)){return;}
	    //Read the entry fields from the request object:
		SMCreatePO createpo = new SMCreatePO(request);
		//Remove any createpo objects in the session:
		smaction.getCurrentSession().removeAttribute(SMCreatePO.CREATE_PO_OBJECT_NAME);
		//Load this createpo object into the session again:
		smaction.getCurrentSession().setAttribute(SMCreatePO.CREATE_PO_OBJECT_NAME, createpo);
		String sWarning = "";
		
		//If this class has been called from the 'submit vendor' button:
		if (request.getParameter(SMCreatePOFromOrderEdit.FIND_VENDOR_BUTTON_NAME) != null){
			//First, save all the create po info in the session:
			smaction.getCurrentSession().setAttribute(SMCreatePO.CREATE_PO_OBJECT_NAME, createpo);
			//Then call the finder to search for vendors:
			String sRedirectString = 
				APVendor.getFindVendorLink(
					smaction.getCallingClass(), 
					SMCreatePOFromOrderEdit.FOUND_VENDOR_PARAMETER, 
					SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID(), 
					getServletContext(),
					smaction.getsDBID()
				)
				+ "*RETURNINGFROMFINDER=TRUE"
				;

			try {
				response.sendRedirect(sRedirectString);
			} catch (IOException e1) {
				sWarning = "after " + ICEditPOEdit.FIND_VENDOR_BUTTON_NAME + " - error redirecting with string: " + sRedirectString;
				smaction.redirectAction(
						sWarning, 
						"", 
						""
						);
					return;
			}
			return;
		}
		
		//Validate the createpo object here:
		try {
			createpo.validate_entry(smaction.getsDBID(),getServletContext(), smaction.getUserID(), smaction.getFullUserName());
		} catch (Exception e2) {
			smaction.getCurrentSession().setAttribute(SMCreatePO.CREATE_PO_OBJECT_NAME, createpo);
			sWarning = e2.getMessage();
			smaction.redirectAction(
					sWarning, 
					"", 
					""
					);
			return;
		}
		
		//System.out.println("[1361812314] - OK to here");
		
		//Otherwise, create a new PO:
		ICPOHeader pohead = new ICPOHeader();
		SMOrderHeader order = new SMOrderHeader();
		order.setM_strimmedordernumber(createpo.getOrderNumber().trim());
		if (!order.load(getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName())){
			smaction.redirectAction(
					"Error loading order number '" + createpo.getOrderNumber() + "' - " + order.getErrorMessages(), 
					"", 
					""
					);
				return;
		}
		String SQL = "SELECT * FROM " + SMTablelocations.TableName
				+ " WHERE ("
					+ "(" + SMTablelocations.sLocation + " = '" + order.getM_sLocation() + "')"
				+ ")"
		;
		try {
			ResultSet rsLocation = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				smaction.getsDBID(),
				"MySQL",
				this.toString() + ".readLocation - user: " + smaction.getUserID()
				+ " - "
				+ smaction.getFullUserName()
					);
			if (rsLocation.next()){
				pohead.setsbilladdress1(rsLocation.getString(SMTablelocations.sRemitToAddress1));
				pohead.setsbilladdress2(rsLocation.getString(SMTablelocations.sRemitToAddress2));
				pohead.setsbilladdress3(rsLocation.getString(SMTablelocations.sRemitToAddress3));
				pohead.setsbilladdress4(rsLocation.getString(SMTablelocations.sRemitToAddress4));
				pohead.setsbillcity(rsLocation.getString(SMTablelocations.sRemitToCity));
				pohead.setsbillcode(rsLocation.getString(SMTablelocations.sLocation));
				pohead.setsbillcountry(rsLocation.getString(SMTablelocations.sRemitToCountry));
				pohead.setsbillfax(rsLocation.getString(SMTablelocations.sRemitToFax));
				pohead.setsbillname(rsLocation.getString(SMTablelocations.sRemitToCompanyDescription));
				pohead.setsbillphone(rsLocation.getString(SMTablelocations.sRemitToPhone));
				pohead.setsbillpostalcode(rsLocation.getString(SMTablelocations.sRemitToZip));
				pohead.setsbillstate(rsLocation.getString(SMTablelocations.sRemitToState));
				rsLocation.close();
			}else{
				pohead.setsbillname("(NOT SELECTED");
				rsLocation.close();
			}
		} catch (SQLException e1) {
			sWarning = "Error reading location data for remit to -" + e1.getMessage();
			smaction.redirectAction(
					sWarning, 
					"", 
					""
					);
				return;
		}
		SQL = "SELECT * FROM " + SMTableusers.TableName
			+ " WHERE ("
				+ "(" + SMTableusers.lid + " = " + smaction.getUserID() + ")"
			+ ")"
		;
		try {
			ResultSet rsUser = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					smaction.getsDBID(),
					"MySQL",
					this.toString() + ".readUserName - user: " + smaction.getUserID()
					+ " - "
					+ smaction.getFullUserName()
					);
			if (rsUser.next()){
				pohead.setsbillcontactname(rsUser.getString(SMTableusers.sUserFirstName) + " " + rsUser.getString(SMTableusers.sUserLastName));
			}else{
				pohead.setsbillcontactname("");
			}
			rsUser.close();
		} catch (SQLException e) {
			//Don't choke over this
			pohead.setsbillcontactname("");
		}
		
		pohead.setscomment(order.getM_strimmedordernumber() + " " 
				+ order.getM_sBillToName() + " - " + order.getM_sShipToName());
		pohead.setsdatexpecteddate(createpo.getExpectedShipDate());
		pohead.setsdescription("");
		pohead.setsgdoclink("");
		pohead.setspodate(clsDateAndTimeConversions.now("MM/dd/yyyy"));
		pohead.setsponumber("");
		pohead.setsreference("");
		
		//We set the ship to location address to the location of the first line we are trying to order:
		String sDetailNumberString[] = createpo.getDetailNumberListString().split(SMCreatePOFromOrderEdit.PARAM_DETAILNUMBERDELIMITER);
		String sShipToLocation = "";
		for (int i = 0; i < order.get_iOrderDetailCount(); i++){
			if (order.getM_arrOrderDetails().get(i).getM_iDetailNumber().compareToIgnoreCase(sDetailNumberString[0]) == 0){
				sShipToLocation = order.getM_arrOrderDetails().get(i).getM_sLocationCode();
				break;
			}
		}
		
		SQL = "SELECT * FROM " + SMTablelocations.TableName
				+ " WHERE ("
					+ "(" + SMTablelocations.sLocation + " = '" + sShipToLocation + "')"
				+ ")"
		;
		try {
			ResultSet rsLocation = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				smaction.getsDBID(),
				"MySQL",
				this.toString() + ".readShipToLocation - user: " + smaction.getUserID()
				+ " - "
				+ smaction.getFullUserName()
					);
			if (rsLocation.next()){
				pohead.setsshipaddress1(rsLocation.getString(SMTablelocations.sAddress1));
				pohead.setsshipaddress2(rsLocation.getString(SMTablelocations.sAddress2));
				pohead.setsshipaddress3(rsLocation.getString(SMTablelocations.sAddress3));
				pohead.setsshipaddress4(rsLocation.getString(SMTablelocations.sAddress4));
				pohead.setsshipcity(rsLocation.getString(SMTablelocations.sCity));
				pohead.setsshipcode(rsLocation.getString(SMTablelocations.sLocation));
				pohead.setsshipcontactname("");
				pohead.setsshipcountry(rsLocation.getString(SMTablelocations.sCountry));
				pohead.setsshipfax(rsLocation.getString(SMTablelocations.sFax));
				pohead.setsshipname(rsLocation.getString(SMTablelocations.sCompanyDescription));
				pohead.setsshipphone(rsLocation.getString(SMTablelocations.sPhone));
				pohead.setsshippostalcode(rsLocation.getString(SMTablelocations.sZip));
				pohead.setsshipstate(rsLocation.getString(SMTablelocations.sState));
				rsLocation.close();
			}else{
				pohead.setsshipname("(NOT SELECTED");
				rsLocation.close();
			}
		} catch (SQLException e1) {
			sWarning = "Error reading location data for ship to -" + e1.getMessage();
			smaction.redirectAction(
					sWarning, 
					"", 
					""
					);
				return;
		}

		pohead.setsshipviacode("");
		pohead.setsshipvianame("");
		pohead.setsstatus("0");
		pohead.setsvendor(createpo.getVendorCode());
		APVendor ven = new APVendor();
		ven.setsvendoracct(createpo.getVendorCode());
		if (!ven.load(getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName())){
			sWarning = "Error reading information for vendor '" + createpo.getVendorCode() + "' -" + ven.getErrorMessages();
			smaction.redirectAction(
					sWarning, 
					"", 
					""
					);
				return;
		}

		pohead.setsvendorname(ven.getsname());
		if (!pohead.save_without_data_transaction(
			getServletContext(), 
			smaction.getsDBID(), 
			smaction.getUserID(),
			smaction.getFullUserName(),
			false
			)){
			sWarning = "Error saving PO header - " + pohead.getErrorMessages();
			smaction.redirectAction(
					sWarning, 
					"", 
					""
					);
				return;
		}
		
		//Now add the lines to the PO:
		String sStatus = "";
		for (int i = 0; i < createpo.getNumberOfLines(); i++){
			ICPOLine poline = new ICPOLine();
			//Get the line from the order:
			for (int j = 0; j < order.get_iOrderDetailCount(); j++){
				if (order.getM_arrOrderDetails().get(j).getM_iDetailNumber().compareToIgnoreCase(sDetailNumberString[i]) == 0){
					poline.setslocation(order.getM_arrOrderDetails().get(j).getM_sLocationCode());
					break;
				}
			}
			
			poline.setnumberoflabels("1");
			poline.setsdatexpected(pohead.getsdatexpecteddate());
			poline.setsextendedordercost(
				new BigDecimal(createpo.getQuantity(i)).multiply(new BigDecimal(createpo.getUnitCost(i))).toString().replace(",", "")
			);
			poline.setsextendedreceivedcost("0.00");
			poline.setsglexpenseacct(""); //This gets set when we validate the PO Line, before saving.
			poline.setsinstructions("");
			poline.setsitemdescription(createpo.getItemDescription(i));
			poline.setsitemnumber(createpo.getItemNumber(i));
			poline.setslinenumber(Integer.toString(i + 1));
			poline.setsnoninventoryitem("0");  //This IS an inventory item
			poline.setspoheaderid(pohead.getsID());
			poline.setsqtyordered(createpo.getQuantity(i));
			poline.setsqtyreceived("0.0000");
			poline.setsunitcost(createpo.getUnitCost(i));
			poline.setsunitofmeasure(createpo.getUnitOfMeasure(i));
			poline.setsvendorsitemcomment("");
			poline.setsvendorsitemnumber("");
			if (!poline.save_without_data_transaction(getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserName(),
					smaction.getUserID(),
					smaction.getFullUserName()
					)){
				sStatus += " Item '" + createpo.getItemNumber(i) + "' could not be added - " + poline.getErrorMessages() + ".";
				//Let's not choke on any one line, if it doesn't save:
			}
		}

		//Assuming everything went ok, now display the created PO:
		//Remove the createpo object from the session - we won't need it now:
		smaction.getCurrentSession().removeAttribute(SMCreatePO.CREATE_PO_OBJECT_NAME);
		String sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smic.ICEditPOEdit"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&lid=" + pohead.getsID()
				+ "&Status=" + sStatus
				;
		PrintWriter out = response.getWriter();
		try {
			response.sendRedirect(sRedirectString);
		} catch (IOException e) {
			
			out.println("In " + this.toString() 
					+ ".redirectAction - IOException error redirecting with string: "
					+ clsServletUtilities.URLEncode(sRedirectString + " - " + e.getMessage())
			);
		} catch (IllegalStateException e) {
			out.println("In " + this.toString() 
					+ ".redirectAction - IllegalStateException error redirecting with string: "
					+ clsServletUtilities.URLEncode(sRedirectString + " - " + e.getMessage())
			);
		}

		return;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
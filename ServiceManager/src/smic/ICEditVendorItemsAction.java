package smic;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class ICEditVendorItemsAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;

	public static String VENDORITEMARRAY = "VENDORITEMARRAY";
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICEditVendorItems
		)){
			return;
		}
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName  = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		//If the vendor item array is stored in the session, remove it:
		CurrentSession.removeAttribute(VENDORITEMARRAY);
		
	    String sItemNumber = clsManageRequestParameters.get_Request_Parameter(ICEditVendorItems.ITEMNUMBER, request);
	    //Set up an arraylist to hold all the vendor items:
	    String sNumberOfCurrentRecords = clsManageRequestParameters.get_Request_Parameter(ICEditVendorItems.NUMBEROFCURRENTRECORDS_PARAMETER, request);
	    //Size the array for the number of current vendor item records plus one for a 'new' entry:
	    ArrayList<ICVendorItem>arrVendorItems = new ArrayList<ICVendorItem>(0);
	    for (int i = 0; i < Integer.parseInt(sNumberOfCurrentRecords) + 1; i++){
	    	ICVendorItem venitem = new ICVendorItem();
	    	venitem.setsitemnumber(sItemNumber);
	    	arrVendorItems.add(venitem);
	    }
	    //Collect the strings here:
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    String sEditingFromPO = clsManageRequestParameters.get_Request_Parameter(ICEditVendorItems.INITIATED_FROM_PO_PARAM, request);
	    
	    //If we're adding a new vendor item, these will have values:
	    arrVendorItems.get(0).setscost(clsManageRequestParameters.get_Request_Parameter(ICEditVendorItems.NEWVENDORCOST_NAME, request));
	    arrVendorItems.get(0).setsvendor(clsManageRequestParameters.get_Request_Parameter(ICEditVendorItems.NEWVENDOR_NAME, request));
	    arrVendorItems.get(0).setsvendoritemnumber(clsManageRequestParameters.get_Request_Parameter(ICEditVendorItems.NEWVENDORITEM_NAME, request));
	    arrVendorItems.get(0).setscomment(clsManageRequestParameters.get_Request_Parameter(ICEditVendorItems.NEWVENDORITEMCOMMENT_NAME, request));
	    //So the first item in the array will always be any new record we want to add - if it's blank,
	    //we know the user didn't add one.

	    //Read all the current values for any existing vendor items:
    	Enumeration <String> e = request.getParameterNames();
    	String sParam = "";
    	while (e.hasMoreElements()){
    		sParam = e.nextElement();
    		if (sParam.contains(ICEditVendorItems.VENDOR_PARAMETER_MARKER)){
    			String sRecordNumber = sParam.substring(
    				ICEditVendorItems.VENDOR_PARAMETER_MARKER.length(), sParam.length());
    			arrVendorItems.get(Integer.parseInt(sRecordNumber)).setsvendor(
    				clsManageRequestParameters.get_Request_Parameter(sParam, request));
    		}
    		if (sParam.contains(ICEditVendorItems.VENDORITEM_PARAMETER_MARKER)){
    			String sRecordNumber = sParam.substring(
    				ICEditVendorItems.VENDORITEM_PARAMETER_MARKER.length(), sParam.length());
    			arrVendorItems.get(Integer.parseInt(sRecordNumber)).setsvendoritemnumber(
    				clsManageRequestParameters.get_Request_Parameter(sParam, request));
    		}
    		if (sParam.contains(ICEditVendorItems.VENDORCOST_PARAMETER_MARKER)){
    			String sRecordNumber = sParam.substring(
    				ICEditVendorItems.VENDORCOST_PARAMETER_MARKER.length(), sParam.length());
    			arrVendorItems.get(Integer.parseInt(sRecordNumber)).setscost(
    				clsManageRequestParameters.get_Request_Parameter(sParam, request));
    		}
    		if (sParam.contains(ICEditVendorItems.VENDORITEMCOMMENT_PARAMETER_MARKER)){
    			String sRecordNumber = sParam.substring(
    				ICEditVendorItems.VENDORITEMCOMMENT_PARAMETER_MARKER.length(), sParam.length());
    			arrVendorItems.get(Integer.parseInt(sRecordNumber)).setscomment(
    				clsManageRequestParameters.get_Request_Parameter(sParam, request));
    		}
    	}

    	//Store the array list in the session:
    	CurrentSession.setAttribute(VENDORITEMARRAY, arrVendorItems);
    	
		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				sDBID,
				"MySQL",
				this.toString() + ".doPost - User: " + sUserID
				+ " - "
				+ sUserFullName
				);

		if (conn == null){
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
					+ "?" + ICEditVendorItems.ITEMNUMBER + "=" + sItemNumber
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&WARNING=Could not get data connection."
			);
			return;
		}
		
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080843]");
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
					+ "?" + ICEditVendorItems.ITEMNUMBER + "=" + sItemNumber
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&WARNING=Could not start data transaction."
			);
			return;
		}
		
		for (int i = 0; i < arrVendorItems.size();i++){
	    	//System.out.println("*************************** record " + i);
	    	//System.out.println(arrVendorItems.get(i).read_out_debug_data());
			
			//Don't try to save the 'new' item, if it has no vendor selected:
			if (
					(i == 0)
					&& (arrVendorItems.get(i).getsvendor().compareToIgnoreCase("") == 0) 
				){
			}else{
		    	if (!arrVendorItems.get(i).save_without_data_transaction(conn, sUserName)){
					clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080844]");
					response.sendRedirect(
							"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
							+ "?" + ICEditVendorItems.ITEMNUMBER + "=" + sItemNumber
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
							+ "&WARNING=Could not save information for vendor " + arrVendorItems.get(i).getsvendor()
								+ " - " + arrVendorItems.get(i).getErrorMessages()
					);
					return;
		    	}
			}
	    }
		
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080845]");
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
					+ "?" + ICEditVendorItems.ITEMNUMBER + "=" + sItemNumber
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&WARNING=Could not commit data transaction."
			);
			return;
		}
   
		//If the save is successful, remove the session attribute:
		CurrentSession.removeAttribute(VENDORITEMARRAY);
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080846]");
		
		//If the edit was initiated from a PO, then return to the PO Edit screen:
		if (sEditingFromPO.trim().compareToIgnoreCase("") != 0){
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smic.ICEditPOEdit"
				+ "?" + ICPOHeader.Paramlid + "=" + sEditingFromPO
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&STATUS=Vendor item successfully updated."
			);
			return;
		}else{
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
				+ "?" + ICEditVendorItems.ITEMNUMBER + "=" + sItemNumber
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&STATUS=Vendor items successfully updated."
			);
			return;
		}
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

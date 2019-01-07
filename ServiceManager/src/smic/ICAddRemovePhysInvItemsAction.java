package smic;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;

public class ICAddRemovePhysInvItemsAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.ICEditPhysicalInventory)){return;}
	    //Read the entry fields from the request object:
		String sAddOrRemove = clsManageRequestParameters.get_Request_Parameter(ICAddRemovePhysInvItemsEdit.PARAM_ADD_OR_REMOVE, request);
		String sStartingItemNumber = clsManageRequestParameters.get_Request_Parameter(ICAddRemovePhysInvItemsEdit.PARAM_STARTING_ITEM_NUMBER, request);
		String sEndingItemNumber = clsManageRequestParameters.get_Request_Parameter(ICAddRemovePhysInvItemsEdit.PARAM_ENDING_ITEM_NUMBER, request);
		String sStartingReportGroup1 = clsManageRequestParameters.get_Request_Parameter(ICAddRemovePhysInvItemsEdit.PARAM_STARTING_REPORTGROUP1, request);
		String sEndingReportGroup1 = clsManageRequestParameters.get_Request_Parameter(ICAddRemovePhysInvItemsEdit.PARAM_ENDING_REPORTGROUP1, request);
		String sStartingReportGroup2 = clsManageRequestParameters.get_Request_Parameter(ICAddRemovePhysInvItemsEdit.PARAM_STARTING_REPORTGROUP2, request);
		String sEndingReportGroup2 = clsManageRequestParameters.get_Request_Parameter(ICAddRemovePhysInvItemsEdit.PARAM_ENDING_REPORTGROUP2, request);
		String sStartingReportGroup3 = clsManageRequestParameters.get_Request_Parameter(ICAddRemovePhysInvItemsEdit.PARAM_STARTING_REPORTGROUP3, request);
		String sEndingReportGroup3 = clsManageRequestParameters.get_Request_Parameter(ICAddRemovePhysInvItemsEdit.PARAM_ENDING_REPORTGROUP3, request);
		String sStartingReportGroup4 = clsManageRequestParameters.get_Request_Parameter(ICAddRemovePhysInvItemsEdit.PARAM_STARTING_REPORTGROUP4, request);
		String sEndingReportGroup4 = clsManageRequestParameters.get_Request_Parameter(ICAddRemovePhysInvItemsEdit.PARAM_ENDING_REPORTGROUP4, request);
		String sStartingReportGroup5 = clsManageRequestParameters.get_Request_Parameter(ICAddRemovePhysInvItemsEdit.PARAM_STARTING_REPORTGROUP5, request);
		String sEndingReportGroup5 = clsManageRequestParameters.get_Request_Parameter(ICAddRemovePhysInvItemsEdit.PARAM_ENDING_REPORTGROUP5, request);

		//Process the request:
		ICPhysicalInventoryEntry entry = new ICPhysicalInventoryEntry(request);
		if (!entry.load(getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName())){
			smaction.redirectAction("Could not load physical inventory: " + entry.getErrorMessages(), "", "");
			return;
		}
		//TODO - add variables to query string before returning:
		if(!entry.addorremoveItemsFromPhysicalInventory(
				sAddOrRemove,
				sStartingItemNumber,
				sEndingItemNumber,
				sStartingReportGroup1,
				sEndingReportGroup1,
				sStartingReportGroup2,
				sEndingReportGroup2,
				sStartingReportGroup3,
				sEndingReportGroup3,
				sStartingReportGroup4,
				sEndingReportGroup4,
				sStartingReportGroup5,
				sEndingReportGroup5,
				getServletContext(),
				smaction.getsDBID(),
				smaction.getUserName())
				)
		{
			smaction.redirectAction("Could not update physical inventory: " + entry.getErrorMessages(), "", "");
			return;
		}else{
			//If the update succeeded, force the called function to reload it by NOT
			//putting the entry object in the current session, but by passing it
			//in the query string instead:
			if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			}else{
				smaction.redirectAction(
					"", 
					"Physical Count ID: " + entry.slid() + " was successfully updated.",
					entry.getQueryString()
				);
			}
		}
		return;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
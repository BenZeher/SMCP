package smic;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;

public class ICPostPhysicalInventory extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.ICEditPhysicalInventory)){return;}
	    //Read the entry fields from the request object:
		ICPhysicalInventoryEntry entry = new ICPhysicalInventoryEntry(request);
		smaction.getCurrentSession().setAttribute(ICPhysicalInventoryEntry.ParamObjectName, entry);
		
		//If the posting is not confirmed, return and warn the user:
		if (request.getParameter("ConfirmPosting") == null){
			smaction.redirectAction("You chose to post this physical inventory, but "
					+ "you did not check the confirming checkbox.", "", ""
			);
			return;
		}
		
		//First, see if we can load the physical inventory:
		if(!entry.load(getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName())){
			smaction.redirectAction("Could not load physical inventory: " + entry.getErrorMessages(), "", "");
			return;
		}
		
		if (!entry.postToBatch(smaction.getsDBID(), 
							   getServletContext(), 
							   smaction.getUserID(),
							   smaction.getFullUserName()
				)){
	    	smaction.redirectAction(
	    		"Could not post physical inventory: " + entry.getErrorMessages(), 
	    		"", 
	    		ICPhysicalInventoryEntry.ParamID + "=" + entry.slid());
		}else{
			
			
			/*System.out.println("[1543507221]"
				+ " sRedirectString = '"
				+ SMUtilities.getURLLinkBase(getServletContext()) + "" 
				+ smaction.getCallingClass()
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "'"
			);*/
				
	    	smaction.redirectAction(
	    		"",
	    		"Physical inventory successfully posted to batch " + entry.getsBatchNumber() + "."
	    		, ICPhysicalInventoryEntry.ParamID + "=" + entry.slid()
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
package smcontrolpanel;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import SMClasses.SMLaborBackCharge;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsManageRequestParameters;

public class SMLaborBackChargeAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.SMEditLaborBackCharges)){return;}
		smaction.getCurrentSession().removeAttribute(SMLaborBackCharge.ParamObjectName);
	    //Read the entry fields from the request object:
		SMLaborBackCharge entry = new SMLaborBackCharge(request);
		
		//If it's a delete, process that:
	    if(clsManageRequestParameters.get_Request_Parameter(
	    		SMLaborBackChargeEdit.COMMAND_FLAG, request).compareToIgnoreCase(
	    				SMLaborBackChargeEdit.DELETE_COMMAND_VALUE) == 0){
		    if (smaction.isDeleteConfirmed()){
			    //Save this now so it's not lost after the delete:
			    String sLid = entry.getlid();
			    String sOrderNumber = entry.getstrimmedordernumber();
			    try {
					entry.delete(getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName());
				} catch (Exception e) {
			    	smaction.redirectAction(
			    		"Could not delete: " + e.getMessage(), 
			    		"", 
			    		SMLaborBackCharge.Paramlid + "=" + entry.getlid()
			    		);
					return;
				}

		    	//If the delete succeeded, redirect to Edit screen with a new entry:
					String sRedirectString = 
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMLaborBackChargeEdit"
						+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
						+ "&Status=" + entry.getObjectName() + ": " + sLid + " was successfully deleted."
						+ "&" + SMLaborBackCharge.Paramlid + "=-1"
					    + "&" + SMLaborBackCharge.Paramstrimmedordernumber + "=" + sOrderNumber ;
					try {
						response.sendRedirect(sRedirectString);
					} catch (IOException e) {
						smaction.getPwOut().println("In " + this.toString() 
							+ ".redirectAction - IOException error redirecting with string: "
							+ clsServletUtilities.URLEncode(sRedirectString + " - " + e.getMessage())
						);
					} catch (IllegalStateException e) {
						smaction.getPwOut().println("In " + this.toString() 
							+ ".redirectAction - IllegalStateException error redirecting with string: "
							+ clsServletUtilities.URLEncode(sRedirectString + " - " + e.getMessage())
						);
					}
				
				return;
	    	}else{
	    		smaction.getCurrentSession().setAttribute(SMLaborBackCharge.ParamObjectName, entry);
				smaction.redirectAction(
					"You chose to delete, but did not check the CONFIRM checkbox.", 
					"", 
					SMLaborBackCharge.Paramlid + "=" + entry.getlid()
				);
				return;
	    	}
	    }
		
		//If it's an Update, process that:
	    if(clsManageRequestParameters.get_Request_Parameter(
	    		SMLaborBackChargeEdit.COMMAND_FLAG, request).compareToIgnoreCase(
	    				SMLaborBackChargeEdit.UPDATE_COMMAND_VALUE) == 0){
	    	try {
				entry.save_without_data_transaction(
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserID(),
					smaction.getFullUserName()
				);
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(SMLaborBackCharge.ParamObjectName, entry);
				smaction.redirectAction(
					"Could not save: " + e.getMessage(), 
					"", 
					SMLaborBackCharge.Paramlid + "=" + entry.getlid()
				);
				return;
			}
	    	//TODO Generate a Critical date here
	    	
			//If the save succeeded, force the called function to reload it by NOT
			//putting the entry object in the current session, but by passing it
			//the lid instead:
			if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			}else{
				smaction.redirectAction(
					"", 
					entry.getObjectName() + ": " + entry.getlid() + " was successfully saved.",
					SMLaborBackCharge.Paramlid + "=" + entry.getlid()
				);
				return;
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

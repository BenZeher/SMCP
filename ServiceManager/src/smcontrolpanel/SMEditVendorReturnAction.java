package smcontrolpanel;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.SMVendorReturn;
import ServletUtilities.clsServletUtilities;

public class SMEditVendorReturnAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.SMEditVendorReturns)){return;}
		smaction.getCurrentSession().removeAttribute(SMVendorReturn.ParamObjectName);
	    //Read the entry fields from the request object:
		SMVendorReturn entry = new SMVendorReturn(request);
		
	    if(smaction.isDeleteRequested()){
		    if (smaction.isDeleteConfirmed()){
			    //Save this now so it's not lost after the delete:
			    String sLid = entry.getslid();
			    try {
					entry.delete(getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName());
				} catch (Exception e) {
			    	smaction.redirectAction(
				    		"Could not delete: " + e.getMessage(), 
				    		"", 
				    		SMVendorReturn.Paramlid + "=" + entry.getslid()
				    		);
						return;
				}

		    	//If the delete succeeded, the entry will be initialized:
				if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
					smaction.returnToOriginalURL();
				}else{
					String sRedirectString = 
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditVendorReturnSelect"
						+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
						+ "&Status=" + entry.getObjectName() + ": " + sLid + " was successfully deleted."
						;
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
				}
				return;
	    	}else{
	    		smaction.getCurrentSession().setAttribute(SMVendorReturn.ParamObjectName, entry);
				smaction.redirectAction(
					"You chose to delete, but did not check the CONFIRM checkbox.", 
					"", 
					SMVendorReturn.Paramlid + "=" + entry.getslid()
				);
				return;
	    	}
	    }
		
		//If it's an edit, process that:
	    if(smaction.isEditRequested()){
	    	try {
				entry.save_without_data_transaction(
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserID(),
					smaction.getFullUserName()
				);
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(SMVendorReturn.ParamObjectName, entry);
				smaction.redirectAction(
					"[1571767615] Could not save: " + e.getMessage(), 
					"  ", 
					SMVendorReturn.Paramlid + "=" + entry.getslid()
				);
				return;
			}
			//If the save succeeded, force the called function to reload it by NOT
			//putting the entry object in the current session, but by passing it
			//the lid instead:
			if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			}else{
				smaction.redirectAction(
					"", 
					entry.getObjectName() + ": " + entry.getslid() + " was successfully saved.",
					SMVendorReturn.Paramlid + "=" + entry.getslid()
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
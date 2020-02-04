package smcontrolpanel;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;

public class SMEditPriceLevelLabelsAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.SMEditPriceLevelLabels)){return;}
	    //Read the entry fields from the request object:
		SMPriceLevelLabels entry = new SMPriceLevelLabels(request);
		smaction.getCurrentSession().removeAttribute(SMPriceLevelLabels.ParamObjectName);
		

		//If it's an edit, process that:
		try {
			entry.save(getServletContext(), smaction.getsDBID(), smaction.getUserName());
		} catch (Exception e) {
			smaction.getCurrentSession().setAttribute(SMPriceLevelLabels.ParamObjectName, entry);
			smaction.redirectAction(
				"Could not save: " + e.getMessage(), 
				"", 
				""
				);
			return;
		}
		if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
			smaction.returnToOriginalURL();
		}else{
			smaction.redirectAction(
				"", 
				SMPriceLevelLabels.ParamObjectName + " were successfully saved.",
				""
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
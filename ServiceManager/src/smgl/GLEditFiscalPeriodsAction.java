package smgl;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMDataDefinition.SMTableglfiscalperiods;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;

public class GLEditFiscalPeriodsAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.GLEditFiscalPeriods)){return;}
	    //Read the entry fields from the request object:
		GLFiscalPeriod entry = new GLFiscalPeriod(request);
		smaction.getCurrentSession().removeAttribute(GLFiscalPeriod.ParamObjectName);
		
		//If it's an edit, process that:
	    //if(smaction.isEditRequested()){
	    	try {
				entry.save(
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserID(),
					smaction.getFullUserName()
				);
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(GLFiscalPeriod.ParamObjectName, entry);
				smaction.redirectAction(
					"Could not save: " + e.getMessage(), 
					"", 
					SMTableglfiscalperiods.ifiscalyear + "=" + entry.get_sifiscalyear()
					);
				return;
			}
	    //}
	    //If it succeeds, then just return:
		smaction.redirectAction(
			"", 
			"Fiscal year " + entry.get_sifiscalyear() + " was updated successfully", 
			SMTableglfiscalperiods.ifiscalyear + "=" + entry.get_sifiscalyear()
			);
		return;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
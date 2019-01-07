package smas;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;

public class ASEditSSOptionsAction extends HttpServlet{

	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.ASEditSSOptions)){return;}
		//Read the entry fields from the request object:
		SSOptions entry = new SSOptions(request);
		smaction.getCurrentSession().removeAttribute(entry.getobjectname());
		try {
			entry.save(
					getServletContext(), 
					smaction.getConfFile(), 
					smaction.getUserName()
					);
		} catch (Exception e) {
			smaction.getCurrentSession().setAttribute(entry.getobjectname(), entry);
			smaction.redirectAction(
					"Could not save: " + e.getMessage(), 
					"", 
					""
					);
			return;
		}
		//If the save succeeded, force the called function to reload it by NOT
		//putting the entry object in the current session
		if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
			smaction.returnToOriginalURL();
		}else{
			smaction.redirectAction(
					"", 
					entry.getobjectname() + " was successfully saved.",
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
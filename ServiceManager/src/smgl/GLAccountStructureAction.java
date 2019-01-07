package smgl;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMDataDefinition.SMTableglaccountstructures;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;

public class GLAccountStructureAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.GLEditAccountStructures)){return;}
	    //Read the entry fields from the request object:
		
		GLAccountStructure struct = new GLAccountStructure(request);

		try {
			struct.saveEditableFields(getServletContext(), smaction.getsDBID(), smaction.getUserName());
		} catch (Exception e) {
			smaction.redirectAction(
				"Error [1525722348] - Could not save structure - " + e.getMessage(), 
				"", 
				SMTableglaccountstructures.lid + "=" + struct.getlid()
				);
			return;
		}
		
		smaction.redirectAction(
			"", 
			"Structure was successfully changed", 
			SMTableglaccountstructures.lid + "=" + struct.getlid()
		);
		
		return;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
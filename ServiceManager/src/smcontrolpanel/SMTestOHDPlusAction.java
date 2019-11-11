package smcontrolpanel;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SMTestOHDPlusAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
	    //Read the entry fields from the request object:
	    //String sExternalCompanyID = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(GLPullIntoConsolidationSelect.RADIO_BUTTONS_NAME, request);
		

		return;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
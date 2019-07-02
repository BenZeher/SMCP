package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class ICPOUnpostedInvoiceGenerate extends HttpServlet {
	
	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");
	private static final long serialVersionUID = 1L;
	private static final String sCallingClass = "smic.ICEditBatches?";
	private static final String sReportTitle = "I/C Unposted PO Invoices";
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {
		
		long lStartingTime = 0;
		PrintWriter out = response.getWriter();
		
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.ICEditBatches)){
	    	return;
	    }
	    
	    HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " 
			+ "Transitional//EN\">\n" 
			+ "<HTML>\n" 
			+ "  <HEAD>\n"
			+ "    <TITLE>" + sReportTitle + " - " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME) + "</TITLE>\n"
			+ "  </HEAD>\n"
			+ "<BR>" 
			+ "  <BODY BGCOLOR=\"#FFFFFF\">\n" 
			+ "    <TABLE BORDER=0 WIDTH=100%>\n" 
			+ "      <TR>\n"
			+ "        <TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) + "</FONT></TD>\n"
			+ "        <TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME).toString() + "</B></FONT></TD>\n"
			+ "      </TR>\n" 
			+ "      <TR>\n"
			+ "        <TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD>\n"
			+ "      </TR>\n" 
		);
		out.println( "  <TR>\n"
				+ "    <TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>" 
				+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Edit Batches</A></TD>\n"
				+ "  </TR>\n"
				+ "</TABLE>");
		out.println(SMUtilities.getMasterStyleSheetLink());
		out.println("<BR>\n");
		out.println("<TABLE BORDER=0>\n");
		
		lStartingTime = System.currentTimeMillis();
		ServletContext context = getServletContext();
		ICPOUnpostedInvoiceReport rpt = new ICPOUnpostedInvoiceReport();
		try {
			out.println(rpt.processReport(context, sDBID, sCallingClass));
		} catch (Exception e) {
			response.sendRedirect("" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + e.getMessage() + "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID);
			return;
		}

		long lEndingTime = System.currentTimeMillis();
		out.println("<BR>Processing took " + (lEndingTime - lStartingTime) / 1000L + " seconds.\n");
		out.println("  </BODY>\n" + "    </HTML>\n");
		return;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response) 
			throws ServletException, IOException {
		doPost(request, response);
	}

}

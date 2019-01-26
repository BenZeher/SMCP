package smcontrolpanel;

import SMDataDefinition.SMTablelabelprinters;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SMEditLabelPrintersAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Label Printers";
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMEditLabelPrinters
			)
		){
			return;
		}
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sLabelPrinter = clsStringFunctions.filter(request.getParameter(SMTablelabelprinters.sName));
		String sDescription = clsStringFunctions.filter(request.getParameter(SMTablelabelprinters.sDescription));
		String sHost = clsStringFunctions.filter(request.getParameter(SMTablelabelprinters.sHost));
		String sPort = clsStringFunctions.filter(request.getParameter(SMTablelabelprinters.iport));
		String sTopMargin = clsStringFunctions.filter(request.getParameter(SMTablelabelprinters.iTopMargin));
		String sLeftMargin = clsStringFunctions.filter(request.getParameter(SMTablelabelprinters.iLeftMargin));
		String sFont = clsStringFunctions.filter(request.getParameter(SMTablelabelprinters.sFont));
		String sBarCodeHeight = clsStringFunctions.filter(request.getParameter(SMTablelabelprinters.iBarCodeHeight));
		String sBarCodeWidth = clsStringFunctions.filter(request.getParameter(SMTablelabelprinters.iBarCodeWidth));
		String sDarkness = clsStringFunctions.filter(request.getParameter(SMTablelabelprinters.iDarkness));
		String sPrinterLanguage = clsStringFunctions.filter(request.getParameter(SMTablelabelprinters.iprinterlanguage));
		
		try {
			long lPort = Long.parseLong(sPort);
			if (
				(lPort < 1)
				|| (lPort > 65000)
			){
				out.println("Invalid port number - click 'Back' to correct.");
				out.println("</BODY></HTML>");
				return;
			}
		} catch (NumberFormatException e) {
			out.println("Invalid port number - click 'Back' to correct.");
			out.println("</BODY></HTML>");
			return;
		}
		try {
			long lTopMargin = Long.parseLong(sTopMargin);
			if (
				(lTopMargin < 0)
				|| (lTopMargin > 32000)
			){
				out.println("Invalid top margin number - click 'Back' to correct.");
				out.println("</BODY></HTML>");
				return;
			}
		} catch (NumberFormatException e) {
			out.println("Invalid top margin - click 'Back' to correct.");
			out.println("</BODY></HTML>");
			return;
		}
		try {
			long lLeftMargin = Long.parseLong(sLeftMargin);
			if (
				(lLeftMargin < 0)
				|| (lLeftMargin > 32000)
			){
				out.println("Invalid left margin number - click 'Back' to correct.");
				out.println("</BODY></HTML>");
				return;
			}
		} catch (NumberFormatException e) {
			out.println("Invalid left margin - click 'Back' to correct.");
			out.println("</BODY></HTML>");
			return;
		}
		try {
			long lBarCodeWidth = Long.parseLong(sBarCodeWidth);
			if (
				(lBarCodeWidth < 1)
				|| (lBarCodeWidth > 10)
			){
				out.println("Invalid bar code width - click 'Back' to correct.");
				out.println("</BODY></HTML>");
				return;
			}
		} catch (NumberFormatException e) {
			out.println("Invalid bar code width - click 'Back' to correct.");
			out.println("</BODY></HTML>");
			return;
		}
		try {
			long lBarCodeHeight = Long.parseLong(sBarCodeHeight);
			if (
				(lBarCodeHeight < 1)
				|| (lBarCodeHeight > 32000)
			){
				out.println("Invalid bar code height - click 'Back' to correct.");
				out.println("</BODY></HTML>");
				return;
			}
		} catch (NumberFormatException e) {
			out.println("Invalid bar code height - click 'Back' to correct.");
			out.println("</BODY></HTML>");
			return;
		}

		try {
			long lDarkness = Long.parseLong(sDarkness);
			if (
				(lDarkness < 1)
				|| (lDarkness > 30)
			){
				out.println("Invalid darkness setting - click 'Back' to correct.");
				out.println("</BODY></HTML>");
				return;
			}
		} catch (NumberFormatException e) {
			out.println("Invalid darkness setting - click 'Back' to correct.");
			out.println("</BODY></HTML>");
			return;
		}
		long lPrinterLanguage = 0;
		try {
			lPrinterLanguage = Long.parseLong(sPrinterLanguage);
			if (
				(lPrinterLanguage < 0)
				|| (lPrinterLanguage > 2)
			){
				out.println("Invalid printer language - click 'Back' to correct.");
				out.println("</BODY></HTML>");
				return;
			}
		} catch (NumberFormatException e) {
			out.println("Invalid printer language - click 'Back' to correct.");
			out.println("</BODY></HTML>");
			return;
		}
		
		if (sLabelPrinter.trim().compareToIgnoreCase("") == 0){
			out.println("Name cannot be blank - click 'Back' to correct.");
			out.println("</BODY></HTML>");
			return;
		}

		if (sDescription.trim().compareToIgnoreCase("") == 0){
			out.println("Description cannot be blank - click 'Back' to correct.");
			out.println("</BODY></HTML>");
			return;
		}
		
		if (sHost.trim().compareToIgnoreCase("") == 0){
			out.println("Host cannot be blank - click 'Back' to correct.");
			out.println("</BODY></HTML>");
			return;
		}
		if (sFont.trim().compareToIgnoreCase("") == 0){
			out.println("Font cannot be blank - click 'Back' to correct.");
			out.println("</BODY></HTML>");
			return;
		}
		//check that fonts are valid for the printer language:
		if (
				(lPrinterLanguage == SMTablelabelprinters.PRINTER_LANGUAGE_ZPL)
				&& (!"PQRSTUV".contains(sFont))
			){
			out.println("Font '" + sFont + "' is not valid for printer language '" 
				+ SMTablelabelprinters.getPrinterLanguageDescription(Integer.parseInt(sPrinterLanguage)) + "' - click 'Back' to correct.");
			out.println("</BODY></HTML>");
			return;
		}
		if (
				(lPrinterLanguage == SMTablelabelprinters.PRINTER_LANGUAGE_EPL)
				&& (!"12345".contains(sFont))
			){
			out.println("Font '" + sFont + "' is not valid for printer language '" 
				+ SMTablelabelprinters.getPrinterLanguageDescription(Integer.parseInt(sPrinterLanguage)) + "' - click 'Back' to correct.");
			out.println("</BODY></HTML>");
			return;
		}
		
	    String title = "Updating " + sObjectName + "'" + sLabelPrinter + "'";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	
	    String sSQL = "INSERT INTO " + SMTablelabelprinters.TableName
	    	+ " (" + SMTablelabelprinters.iport
	    	+ ", " + SMTablelabelprinters.sDescription
	    	+ ", " + SMTablelabelprinters.sHost
	    	+ ", " + SMTablelabelprinters.sName
	    	+ ", " + SMTablelabelprinters.iTopMargin
	    	+ ", " + SMTablelabelprinters.iLeftMargin
	    	+ ", " + SMTablelabelprinters.sFont
	    	+ ", " + SMTablelabelprinters.iBarCodeHeight
	    	+ ", " + SMTablelabelprinters.iBarCodeWidth
	    	+ ", " + SMTablelabelprinters.iDarkness
	    	+ ", " + SMTablelabelprinters.iprinterlanguage
	    	+ ") VALUES ("
	    	+ sPort
	    	+ ", '" + sDescription + "'"
	    	+ ", '" + sHost + "'"
	    	+ ", '" + sLabelPrinter + "'"
	    	+ ", " + sTopMargin
	    	+ ", " + sLeftMargin
	    	+ ", '" + sFont + "'"
	    	+ ", " + sBarCodeHeight
	    	+ ", " + sBarCodeWidth
	    	+ ", " + sDarkness
	    	+ ", " + sPrinterLanguage
	    	+ ")"
	    	+ " ON DUPLICATE KEY UPDATE"
	    	+ " " + SMTablelabelprinters.sDescription + " = '" + sDescription + "'"
	    	+ ", " + SMTablelabelprinters.sName + " = '" + sLabelPrinter + "'"
	    	+ ", " + SMTablelabelprinters.sHost + " = '" + sHost + "'"
	    	+ ", " + SMTablelabelprinters.iport + " = " + sPort
	    	+ ", " + SMTablelabelprinters.iTopMargin + " = " + sTopMargin
	    	+ ", " + SMTablelabelprinters.iLeftMargin + " = " + sLeftMargin
	    	+ ", " + SMTablelabelprinters.sFont + " = '" + sFont + "'"
	    	+ ", " + SMTablelabelprinters.iBarCodeHeight + " = " + sBarCodeHeight
	    	+ ", " + SMTablelabelprinters.iBarCodeWidth + " = " + sBarCodeWidth
	    	+ ", " + SMTablelabelprinters.iDarkness + " = " + sDarkness
	    	+ ", " + SMTablelabelprinters.iprinterlanguage + " = " + sPrinterLanguage
	    	;
		
	    try{
	    	clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), sDBID);
	    }catch (SQLException ex){
	    	out.println("Error updating label printer with SQL: " + sSQL
	    		+ " - " + ex.getMessage());
			System.out.println("Error updating label printer with SQL: " + sSQL
		    		+ " - " + ex.getMessage());
			out.println("</BODY></HTML>");
			return;
		}
	    out.println("Successfully updated label printer " + sLabelPrinter + ".");
	    out.println("</BODY></HTML>");
	}

	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

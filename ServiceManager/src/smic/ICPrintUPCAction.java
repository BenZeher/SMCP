package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class ICPrintUPCAction extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private String sWarning = "";

	public static String PARAM_NUMBEROFDIFFERENTLABELS = "NUMBEROFDIFFERENTLABELS";
	public static String PARAM_ITEMNUMMARKER = "ITEMNUM";
	public static String PARAM_QTYMARKER = "QTY";
	public static String PARAM_NUMPIECESMARKER = "NUMPIECES";

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICPrintUPCLabels))
		{
			return;
		}

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);

		//sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);

		//System.out.println("[1539289073] - Param = '" + clsManageRequestParameters.get_Request_Parameter(ICPrintUPCSelection.BUTTON_POPULATE_ITEMS, request) + "'");
		//System.out.println("[1539289074] - Param = '" + clsManageRequestParameters.get_Request_Parameter(ICPrintUPCSelection.BUTTON_POPULATE_ITEMS_LABEL, request) + "'");
		
		//If the user chose to POPULATE items:
		if (clsManageRequestParameters.get_Request_Parameter(ICPrintUPCSelection.BUTTON_POPULATE_ITEMS, request).compareTo("") != 0){
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "smic." + sCallingClass
				+ "?" + ICPrintUPCSelection.BUTTON_POPULATE_ITEMS + "=Y"
				+ "&" + ICPrintUPCSelection.PARAM_STARTINGITEM + "=" 
					+ clsManageRequestParameters.get_Request_Parameter(ICPrintUPCSelection.PARAM_STARTINGITEM, request)
				+ "&" + ICPrintUPCSelection.PARAM_ENDINGITEM + "=" 
					+ clsManageRequestParameters.get_Request_Parameter(ICPrintUPCSelection.PARAM_ENDINGITEM, request)
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				;
			//System.out.println("[1539289072] - sRedirectString = '" + sRedirectString + "'");
			response.sendRedirect(sRedirectString);
			return;
		}
		
		//Otherwise, assume the user just chose to PRINT:
		//Are we printing to the screen or directly to a printer?:
		String sLabelPrinterID = clsManageRequestParameters.get_Request_Parameter(ICPrintUPCItemLabel.LABELPRINTER_LIST, request);
		boolean bPrintToPrinter = false;
		if (sLabelPrinterID.compareToIgnoreCase("0") != 0){
			bPrintToPrinter = true;
		}

		ArrayList <String> sItemNumbers = new ArrayList<String>(0);
		ArrayList<Integer> iLabelQuantities = new ArrayList<Integer>(0);
		ArrayList<Integer> iPieceQuantities = new ArrayList<Integer>(0);
		//ArrayList <String> sComments = new ArrayList<String>(0);

		//System.out.println("In " + this.toString() + ".doPost - before loading array");
		int iNumberOfDifferentLabels = 0;
		try {
			iNumberOfDifferentLabels 
			= Integer.parseInt(clsManageRequestParameters.get_Request_Parameter(PARAM_NUMBEROFDIFFERENTLABELS, request));
		} catch (NumberFormatException e1) {
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + sCallingClass + "?"
					+ "Warning=Invalid NUMBEROFDIFFERENTLABELS"
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
		}

		for (int i = 1; i < iNumberOfDifferentLabels + 1; i++){
			sItemNumbers.add(clsManageRequestParameters.get_Request_Parameter(
					PARAM_ITEMNUMMARKER + Integer.toString(i), request).toUpperCase());

			String sQty = clsManageRequestParameters.get_Request_Parameter(
					PARAM_QTYMARKER + Integer.toString(i), request);

			if (sQty.compareToIgnoreCase("") == 0){
				iLabelQuantities.add((int) 0);
			}else{
				try{
					iLabelQuantities.add(Integer.parseInt(sQty));
				}catch (NumberFormatException e){
					iLabelQuantities.add(-1);
				}
			}

			String sPieceQty = clsManageRequestParameters.get_Request_Parameter(
					PARAM_NUMPIECESMARKER + Integer.toString(i), request);
			if (sPieceQty.compareToIgnoreCase("") == 0){
				iPieceQuantities.add((int) 0);
			}else{
				try{
					iPieceQuantities.add(Integer.parseInt(sPieceQty));
				}catch (NumberFormatException e){
					iPieceQuantities.add(-1);
				}
			}
			//sComments.add(SMUtilities.get_Request_Parameter(
			//		"Comment" + Integer.toString(i), request));
		}
		//System.out.println("In " + this.toString() + ".doPost - after loading array");
		if (!bPrintToPrinter){
			out.println(
					"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 "
					+ "Transitional//EN\">"
					+ "<HTML>"

					//Add a page break definition here:
					+ "<HEAD><BODY BGCOLOR=\"#FFFFFF\">" 
					+ "<STYLE TYPE=\"text/css\">P.breakhere {page-break-before: always}</STYLE>"
					+ "</HEAD>"

					+ "<BODY BGCOLOR=\"#FFFFFF\">"
			);
		}
		String sQueryList = "";

		//System.out.println("In " + this.toString() + ".doPost - before building QueryList");
		for (int i = 0; i < iNumberOfDifferentLabels; i++){
			sQueryList = 
				sQueryList
				+ "&ItemNumber"
				+ Integer.toString(i + 1)
				+ "="
				+ sItemNumbers.get(i)
				+ "&Quantity"
				+ Integer.toString(i + 1)
				+ "="
				+ Integer.toString(iLabelQuantities.get(i))
				+ "&PieceQuantity"
				+ Integer.toString(i + 1)
				+ "="
				+ Integer.toString(iPieceQuantities.get(i))
				;

		}

		//Retrieve information
		String sDatabaseType = "";
		if (SMUtilities.getICImportFlag(sDBID, getServletContext())){
			sDatabaseType = "MySQL";
		}
		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), sDBID, sDatabaseType, "smic.ICPrintUPCAction");
		if (conn == null){
			sWarning = "Unable to get data connection.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + sCallingClass + "?"
					+ "Warning=" + sWarning
					+ sQueryList
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);			
			return;
		}

		//Process the labels here:
		ICPrintUPCItemLabel pupc = new ICPrintUPCItemLabel();

		if (!pupc.printLabels(
				conn,
				sDatabaseType,
				sItemNumbers,
				iLabelQuantities,
				iPieceQuantities,
				sLabelPrinterID,
				bPrintToPrinter,
				false,
				out,
				request,
				getServletContext())
		){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080957]");
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + sCallingClass + "?"
					+ "Warning=" + pupc.getErrorMessage()
					+ sQueryList
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
		}

		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080958]");

		if(!bPrintToPrinter){
			out.println("</BODY></HTML>");
		}else{
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + sCallingClass + "?"
					+ "Status=" + "Labels successfully sent to printer"
					+ sQueryList
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
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

package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTableicitems;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class ICPrintLabelScanSheetAction extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private String sWarning = "";
	private String sCallingClass = "";
	private String sDBID = "";
	private String sUser = "";

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICPrintLabelScanSheets)){
			return;
		}
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		sUser = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
		
		//sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
		sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		String sStartingItemNumber = clsManageRequestParameters.get_Request_Parameter(
			ICPrintLabelScanSheetSelect.PARAM_STARTINGITEM, request);
		String sEndingItemNumber = clsManageRequestParameters.get_Request_Parameter(
			ICPrintLabelScanSheetSelect.PARAM_ENDINGITEM, request);

		ArrayList <String> sItemNumbers = new ArrayList<String>(0);
		ArrayList <Integer> iLabelQty = new ArrayList<Integer>(0);		
		ArrayList <Integer> iPieceQty = new ArrayList<Integer>(0);
		
		String SQL = "SELECT"
			+ " " + SMTableicitems.sItemNumber
			+ " FROM " + SMTableicitems.TableName
			+ " WHERE ("
				+ "(" + SMTableicitems.sItemNumber + " >= '" + sStartingItemNumber + "')"
				+ " AND (" + SMTableicitems.sItemNumber + " <= '" + sEndingItemNumber + "')"
			+ ")"
			+ " ORDER BY " + SMTableicitems.sItemNumber
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					this.toString() + ".doPost - user: " + sUser);
			
			while (rs.next()){
				sItemNumbers.add(rs.getString(SMTableicitems.sItemNumber));
				iLabelQty.add(1);
				iPieceQty.add(1);
			}
			rs.close();
		} catch (SQLException e2) {
			response.sendRedirect(
				SMUtilities.getURLLinkBase(getServletContext()) + "smic." + sCallingClass + "?"
				+ ICPrintLabelScanSheetSelect.PARAM_STARTINGITEM + "=" + sStartingItemNumber
				+ "&" + ICPrintLabelScanSheetSelect.PARAM_ENDINGITEM + "=" + sEndingItemNumber
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&Warning=Error reading item numbers - " + e2.getMessage()
			);
			return;
		}
		
		out.println(
				"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 "
				+ "Transitional//EN\">"
				+ "<HTML>"
				+ "<HEAD><BODY BGCOLOR=\"#FFFFFF\">" 
				+ "</HEAD>"
				+ "<BODY BGCOLOR=\"#FFFFFF\">"
		);

		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), sDBID, "MySQL", "smic.ICPrintLabelScanSheetAction");
		if (conn == null){
			sWarning = "Unable to get data connection.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + sCallingClass + "?"
					+ ICPrintLabelScanSheetSelect.PARAM_STARTINGITEM + "=" + sStartingItemNumber
					+ "&" + ICPrintLabelScanSheetSelect.PARAM_ENDINGITEM + "=" + sEndingItemNumber
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + sWarning
			);			
			return;
		}

		//Process the labels here:
		ICPrintUPCItemLabel pupc = new ICPrintUPCItemLabel();
		if (!pupc.printLabels(
				conn,
				"MySQL",
				sItemNumbers,
				iLabelQty,
				iPieceQty,
				"",
				false,
				true,
				out,
				request,
				getServletContext())
		){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			response.sendRedirect(
				SMUtilities.getURLLinkBase(getServletContext()) + "smic." + sCallingClass + "?"
				+ ICPrintLabelScanSheetSelect.PARAM_STARTINGITEM + "=" + sStartingItemNumber
				+ "&" + ICPrintLabelScanSheetSelect.PARAM_ENDINGITEM + "=" + sEndingItemNumber
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&Warning=" + pupc.getErrorMessage()
			);
			return;
		}

		clsDatabaseFunctions.freeConnection(getServletContext(), conn);
		out.println("</BODY></HTML>");
		return;
		
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}

package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableicitems;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class ICPrintReceivingLabelsAction extends HttpServlet{

	private static final long serialVersionUID = 1L;
	public static final String PARAM_NUMBEROFDIFFERENTLABELS = "NUMBEROFDIFFERENTLABELS";
	public static final String PARAM_ITEMNUMMARKER = "ITEMNUM";
	public static final String PARAM_QTYMARKER = "QTY";
	public static final String PARAM_NUMPIECESMARKER = "NUMPIECES";
	public static final String PARAM_PONUMBER = "PONUMBER";
	
	private boolean bDebugMode = false;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request,	
				response,
				getServletContext(),
				SMSystemFunctions.ICPrintReceivingLabels
			)
		){
			return;
		}
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    
		String sCallingClass = "";
	    //sCallingClass will look like: smar.ARAgedTrialBalanceReport
	    sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    
	    //Get the parameters:
		String sStartingVendor = clsManageRequestParameters.get_Request_Parameter("StartingVendor", request);
		String sEndingVendor = clsManageRequestParameters.get_Request_Parameter("EndingVendor", request);
		String sStartPODate = request.getParameter("StartingPODate");
		String sEndPODate = request.getParameter("EndingPODate");
		String sStartDate = request.getParameter("StartingDate");
		String sEndDate = request.getParameter("EndingDate");
		String sPONumber = clsManageRequestParameters.get_Request_Parameter(PARAM_PONUMBER, request);
	    
		//Are we printing to the screen or directly to a printer?:
		String sLabelPrinterID = clsManageRequestParameters.get_Request_Parameter(ICPrintUPCItemLabel.LABELPRINTER_LIST, request);
		boolean bPrintToPrinter = false;
		if (sLabelPrinterID.compareToIgnoreCase("0") != 0){
			bPrintToPrinter = true;
		}
		
    	//Get the list of selected locations:
    	ArrayList<String> sLocations = new ArrayList<String>(0);
	    Enumeration<String> paramLocationNames = request.getParameterNames();
	    String sParamLocationName = "";
	    String sLocationMarker = "LOCATION";
	    while(paramLocationNames.hasMoreElements()) {
	    	sParamLocationName = paramLocationNames.nextElement();
		  if (sParamLocationName.contains(sLocationMarker)){
			  sLocations.add(sParamLocationName.substring(
					  sParamLocationName.indexOf(sLocationMarker) + sLocationMarker.length()));
		  }
	    }
	    Collections.sort(sLocations);
	    
	    //Get the item numbers and qtys:
    	ArrayList<String> arrItemNumbers = new ArrayList<String>(0);
    	ArrayList<String> arrNumberOfLabels = new ArrayList<String>(0);
	    Enumeration<String> paramPiecesPerItemNumber = request.getParameterNames();
	    String sParamPiecesPerItemNumber = "";
	    String sItemMarker = PARAM_NUMPIECESMARKER;
	    while(paramPiecesPerItemNumber.hasMoreElements()) {
	    	sParamPiecesPerItemNumber = paramPiecesPerItemNumber.nextElement();
		  if (sParamPiecesPerItemNumber.contains(sItemMarker)){
			  //The incoming parameter name has the PARAM_NUMPIECESMARKER, plus 6 spaces for the line number,
			  //then, finally, the item number.  It carries the number of pieces as its value:
			  
			  if (bDebugMode){
				  
			  }
			  arrItemNumbers.add(sParamPiecesPerItemNumber.substring(
					  sParamPiecesPerItemNumber.indexOf(sItemMarker)+ 6 + sItemMarker.length()));
			  arrNumberOfLabels.add(clsManageRequestParameters.get_Request_Parameter(sParamPiecesPerItemNumber, request));
		  }
	    }
	    
	    String sQueryString = 
	    	"&" + "StartingVendor=" + sStartingVendor
	    	+ "&" + "EndingVendor=" + sEndingVendor
	    	+ "&" + "StartingPODate=" + sStartPODate
	    	+ "&" + "EndingPODate=" + sEndPODate
	    	+ "&" + "StartingDate=" + sStartDate
	    	+ "&" + "EndingDate=" + sEndDate
	    	+ "&" + PARAM_PONUMBER + sPONumber
			+ "&" + ICPrintReceivingLabelsSelection.STARTING_RECEIPT_DATE_PARAMETER + "=" 
	    		+ clsManageRequestParameters.get_Request_Parameter(ICPrintReceivingLabelsSelection.STARTING_RECEIPT_DATE_PARAMETER, request)
			+ "&" + ICPrintReceivingLabelsSelection.ENDING_RECEIPT_DATE_PARAMETER + "=" 
	    		+ clsManageRequestParameters.get_Request_Parameter(ICPrintReceivingLabelsSelection.ENDING_RECEIPT_DATE_PARAMETER, request)
			+ "&" + ICPrintReceivingLabelsSelection.RECEIVED_BY_PARAMETER + "=" 
	    		+ clsManageRequestParameters.get_Request_Parameter(ICPrintReceivingLabelsSelection.RECEIVED_BY_PARAMETER, request)
			+ "&" + ICPrintReceivingLabelsSelection.PRINT_RECEIVED_OR_UNRECEIVED_ITEMS + "=" 
	    		+ clsManageRequestParameters.get_Request_Parameter(ICPrintReceivingLabelsSelection.PRINT_RECEIVED_OR_UNRECEIVED_ITEMS, request)
	    	+ "&" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    	;

	    for (int i = 0; i < sLocations.size(); i++){
	    	sQueryString = sQueryString + "&LOCATION" + sLocations.get(i);
	    }
	    
	    //Special case - it's a request to 'Validate' the line:
		//If it's an edit, process that:
	    if(request.getParameter(ICPrintReceivingLabelsGenerate.BUTTONSAVE_NAME) != null){
	    	try {
	    		processUpdate(request, sDBID, sUserName, arrItemNumbers, arrNumberOfLabels);
	    		response.sendRedirect(
	    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
	    				+ "Status=" + "Number of labels on items successfully updated."
	    				+ sQueryString
	    			);
	    			return;	
	    	}catch(Exception e) {
	    		response.sendRedirect(
        				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smic.ICPrintReceivingLabelsSelection" + "?"
        				+ "Warning=" + clsServletUtilities.URLEncode(e.getMessage())
        				+ sQueryString
        		);			
            	return;
	    	}		
	    }
	   
	    if(request.getParameter(ICPrintReceivingLabelsGenerate.BUTTONPRINT_NAME) != null){
	    	try {
	    		printLabels(sLabelPrinterID, bPrintToPrinter, request, sDBID, sUserName, response);
	    		
	    	}catch(Exception e){
	    		response.sendRedirect(
		    			"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
		    			+ "Warning=" + "Error printing labels: " + clsServletUtilities.URLEncode(e.getMessage())
		    			+ sQueryString
		    		);
		    		return;
	    	}

	    	if (bPrintToPrinter){
        		response.sendRedirect(
        				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smic.ICPrintReceivingLabelsSelection" + "?"
        				+ "Status=Labels successfully printed."
        				+ sQueryString
        		);			
            	return;
	    	}
	    	return;
	    }
	    return;
	}
	private void processUpdate(
			HttpServletRequest req, 
			String sDBID, 
			String sUser, 
			ArrayList<String>arrItemNumbers,
			ArrayList<String>arrNumberOfLabels
			) throws Exception{
		Connection conn = clsDatabaseFunctions.getConnection(getServletContext(), sDBID, "MySQL", sUser);
		if (conn == null){
			throw new Exception("Could not open data connection");
		}
		
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080952]");
			throw new Exception("Could not open data connection");
		}

		//Process updates here:
		//Verify the number of labels:
		for (int i = 0; i < arrNumberOfLabels.size(); i++){
			try{
				arrNumberOfLabels.set(i, arrNumberOfLabels.get(i).trim().replace(",", ""));
			}catch(NumberFormatException e){
				throw new Exception("Qty '" + arrNumberOfLabels.get(i).trim().replace(",", "") + "' is not valid "
					+ "for item number '" + arrItemNumbers.get(i) + "'.  <BR>");
			}
		}
		
		String SQL = "";
		for (int i = 0; i < arrItemNumbers.size(); i++){
			SQL = "UPDATE"
				+ " " + SMTableicitems.TableName
				+ " SET " + SMTableicitems.bdnumberoflabels + " = " + arrNumberOfLabels.get(i)
				+ " WHERE ("
					+ "(" + SMTableicitems.sItemNumber + " = '" + arrItemNumbers.get(i) + "')" 
				+ ")"
			;
			
			try{
				Statement stmt = conn .createStatement();
				stmt.execute(SQL);
			}catch (SQLException e){
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080953]");
				throw new Exception("Error with SQL: " + SQL + " - " + e.getMessage());
				
			}
		}
		
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080954]");
			throw new Exception("Could not commit data transaction");
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080955]");
	}
	
	private void printLabels(
			String sLabelPrinterID,
			boolean bPrintToPrinter,
			HttpServletRequest req, 
			String sDBID, 
			String sUser, 
			HttpServletResponse res) throws Exception{
	    ArrayList <String> sItemNumbers = new ArrayList<String>(0);
	    ArrayList<Integer> iLabelQuantities = new ArrayList<Integer>(0);
	    ArrayList<Integer> iPieceQuantities = new ArrayList<Integer>(0);
	    //ArrayList <String> sComments = new ArrayList<String>(0);
	    
	    //System.out.println("In " + this.toString() + ".doPost - before loading array");
	    int iNumberOfDifferentLabels = 0;
	    try {
			iNumberOfDifferentLabels 
				= Integer.parseInt(clsManageRequestParameters.get_Request_Parameter(PARAM_NUMBEROFDIFFERENTLABELS, req));
		} catch (NumberFormatException e1) {
			throw new Exception(PARAM_NUMBEROFDIFFERENTLABELS + " parameter value '" 
				+ clsManageRequestParameters.get_Request_Parameter(PARAM_NUMBEROFDIFFERENTLABELS, req)
				+ "' is invalid.");
		}
	    //number of different labels.
	    for (int i = 1; i < iNumberOfDifferentLabels + 1; i++){
	    		sItemNumbers.add(clsManageRequestParameters.get_Request_Parameter(
	    			PARAM_ITEMNUMMARKER + Integer.toString(i), req).toUpperCase());

	    		if (bDebugMode){
	    			clsServletUtilities.sysprint(this.toString(), sUser, "QTYMARKER param = '" 
	    				+ clsManageRequestParameters.get_Request_Parameter(
	    	    		PARAM_QTYMARKER + Integer.toString(i), req));
	    		}
	    		String sQty = clsManageRequestParameters.get_Request_Parameter(
	    			PARAM_QTYMARKER + Integer.toString(i), req);
	    		
	    		BigDecimal bdQty = new BigDecimal(0);
	    		if (sQty.compareToIgnoreCase("") == 0){
	    			iLabelQuantities.add((int) 0);
	    		}else{
	    			try{
	    				bdQty = new BigDecimal(sQty.replace(",", ""));
	    			}catch (NumberFormatException e){
	    				throw new Exception("Invalid qty on item '" 
	    				+ clsManageRequestParameters.get_Request_Parameter(
	    		    		PARAM_ITEMNUMMARKER + Integer.toString(i), req).toUpperCase()
	    		    	+ "'.")
	    				;
	    			}
	    		}

	    		
	    		//If the number of pieces is less than zero, that indicates that the items come several in 
	    		//a box, of in packs of some kind, and the user wants to print only one label for every
	    		//so many items.  In THAT case, we re-calculate to print ONE piece, but a QTY of
	    		// Qty X NoOfPieces and round it up:
	    		if (bDebugMode){
	    			clsServletUtilities.sysprint(this.toString(), sUser, "PIECEMARKER param = '" 
	    				+ clsManageRequestParameters.get_Request_Parameter(PARAM_NUMPIECESMARKER + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6) 
	    		    			+ clsManageRequestParameters.get_Request_Parameter(
	    		    	    			PARAM_ITEMNUMMARKER + Integer.toString(i), req),
	    		    			req));
	    		}
	    		String sPieceQty = clsManageRequestParameters.get_Request_Parameter(
	    			PARAM_NUMPIECESMARKER + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6) 
	    			+ clsManageRequestParameters.get_Request_Parameter(
	    	    			PARAM_ITEMNUMMARKER + Integer.toString(i), req),
	    			req);
	    		BigDecimal bdPieceQty = new BigDecimal(0);
	    		if (sPieceQty.compareToIgnoreCase("") == 0){
	    			iPieceQuantities.add((int) 0);
	    		}else{
	    			try{
	    				bdPieceQty = new BigDecimal(sPieceQty.replace(",", ""));
	    			}catch (NumberFormatException e){
	    				throw new Exception("Invalid label qty on item '" 
	    				+ clsManageRequestParameters.get_Request_Parameter(
	    		    		PARAM_ITEMNUMMARKER + Integer.toString(i), req).toUpperCase()
	    		    	+ "'.")
	    				;
	    			}
	    		}

	    		if (bdPieceQty.compareTo(BigDecimal.ONE) < 0){
	    			bdQty = bdQty.multiply(bdPieceQty);
	    			bdPieceQty = BigDecimal.ONE;
	    		}
	    		
	    		//Add the final qty and number of pieces here:
	    		//If the qty includes a fraction part (like 1.25), round up the quantity:
	    		BigDecimal bdRoundedQty = bdQty.divide(BigDecimal.ONE, 0, BigDecimal.ROUND_UP);
	    		try {
					iLabelQuantities.add(
						Integer.parseInt(clsManageBigDecimals.BigDecimalToFormattedString("#########", bdRoundedQty)));
				} catch (NumberFormatException e1) {
					throw new Exception("Invalid rounded qty: '" +  bdRoundedQty + "' on item '" 
	    				+ clsManageRequestParameters.get_Request_Parameter(
	    		    		PARAM_ITEMNUMMARKER + Integer.toString(i), req).toUpperCase()
	    		    	+ "'.")
	    				;
				}

	    		try {
					iPieceQuantities.add(
						Integer.parseInt(clsManageBigDecimals.BigDecimalToFormattedString("#########", bdPieceQty)));
				} catch (NumberFormatException e1) {
					throw new Exception("Invalid label qty: '" +  bdPieceQty + "' on item '" 
	    				+ clsManageRequestParameters.get_Request_Parameter(
	    		    		PARAM_ITEMNUMMARKER + Integer.toString(i), req).toUpperCase()
	    		    	+ "'.")
	    				;
				}

	    }
    	//Retrieve information
    	String sDatabaseType = "";
    	if (SMUtilities.getICImportFlag(sDBID, getServletContext())){
    		sDatabaseType = "MySQL";
    	}
    	//Find out if we are using SMIC live - if so, we will be opening a MySQL connection, otherwise, a 
    	Connection conn = clsDatabaseFunctions.getConnection(
        	getServletContext(), sDBID, sDatabaseType, "smic.ICPrintUPCAction");
    	if (conn == null){
    		throw new Exception("Unable to get data connection.");
    	}
    	
    	//Print the first line of the HTML:
	    res.setContentType("text/html");
		PrintWriter out;
		try {
			out = res.getWriter();
		} catch (IOException e) {
			throw new Exception("Unable to initialize PrintWriter - " + e.getMessage() + ".");
        }
    	
		//Print the beginning of the HTML:
    	out.println(
    		"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 "
		   + "Transitional//EN\">"
	       + "<HTML>"
	       
	       //Add a page break definition here:
	       + "<HEAD><BODY BGCOLOR=\"" + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "\">" 
	       		+ "<STYLE TYPE=\"text/css\">P.breakhere {page-break-before: always}</STYLE>"
	       + "</HEAD>"
	       
		   + "<BODY BGCOLOR=\"" + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "\">"
    	);
		
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
    			req,
    			getServletContext())
    	){
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080950]");
				out.println("Error printing labels - " + pupc.getErrorMessage() + ".");
				throw new Exception(pupc.getErrorMessage());
	    }else{
	    	//out.println("NO Error printing labels.");
	    }
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080951]");
	    out.println("</BODY></HTML>");
		
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
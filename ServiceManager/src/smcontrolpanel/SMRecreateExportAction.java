package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smar.SMGLExport;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMExportTypes;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMRecreateExportAction extends HttpServlet{
	public static final String BATCHNUMBER_PARAM = "BATCH_NUMBER";
	public static final String SOURCELEDGER_PARAM = "SOURCE_LEDGER";
	public static final String BATCHLABEL_PARAM = "BATCH_LABEL";
	public static final String OTHER_PARAMETERS_PARAM = "OTHER_PARAMETERS";
	public static final String EXPORTTYPE_PARAM = "EXPORT_TYPE";
	private static final long serialVersionUID = 1L;
	private boolean bDebugMode = false;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), -1)){return;}
		smaction.getCurrentSession().removeAttribute(SMOrderHeader.ParamObjectName);
    	//Get the batchnumber and source ledger type:
	    String sBatchNumber = clsManageRequestParameters.get_Request_Parameter(
	    	BATCHNUMBER_PARAM, request);
	    String sSourceLedger = clsManageRequestParameters.get_Request_Parameter(
	    	SOURCELEDGER_PARAM, request);
	    String sBatchTypeLabel = clsManageRequestParameters.get_Request_Parameter(
	    	BATCHLABEL_PARAM, request);
	    String sOtherParameters = clsManageRequestParameters.get_Request_Parameter(
	    	OTHER_PARAMETERS_PARAM, request);
		Connection conn = clsDatabaseFunctions.getConnection(
			getServletContext(), 
			smaction.getsDBID(), 
			"MySQL", 
			this.toString() + " - " +
					"user: " + smaction.getUserID()
					+" - "
					+ smaction.getFullUserName()
					+ 
					" [1341349818]");
		if (conn == null){
			smaction.redirectAction(
					"Could not get data connection.", 
					"", 
					sOtherParameters.replace("*", "&")
			);
			return;
		}
		SMGLExport glex = new SMGLExport();
		try{
			glex.loadExport(sBatchNumber, sSourceLedger, conn);
		}catch (Exception ex){
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn);
	    	if (bDebugMode){
	    		System.out.println("In " + this.toString() + "Error loading GL export data for batchnumber '" + sBatchNumber 
	    			+ "', source ledger '" + sSourceLedger + " - " 
	    			+ ex.getMessage());
	    	}
			smaction.redirectAction( 
			"Error loading GL export data for batchnumber '" + sBatchNumber + "', source ledger '" + sSourceLedger + " - " 
			+ ex.getMessage(), 
			"",
			sOtherParameters.replace("*", "&")
			);
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn);		
		//Now create the download and send it to the browser:
		response.setContentType("text/csv");
	    String sDBID = (String) smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sDatabaseName = sDBID;
        String disposition = "attachment; fileName= " + glex.getExportFileName(sSourceLedger, sBatchTypeLabel, sBatchNumber, sDatabaseName);
        response.setHeader("Content-Disposition", disposition);
        PrintWriter out = response.getWriter();

		String sExportType = clsManageRequestParameters.get_Request_Parameter(EXPORTTYPE_PARAM, request).trim();
        try {
        	if (sExportType.compareToIgnoreCase(Integer.toString(SMExportTypes.EXPORT_TO_MAS200)) == 0){
        		glex.writeMAS200ExportDownload(sSourceLedger, sBatchTypeLabel, sBatchNumber, out);
        	}else{
        		glex.writeACCPACExportDownload(sSourceLedger, sBatchTypeLabel, sBatchNumber, out);
        	}
		} catch (Exception e) {
			smaction.redirectAction( 
			"Error creating export for batchnumber '" + sBatchNumber + "', source ledger '" + sSourceLedger + " - " 
			+ e.getMessage(), 
			"",
			sOtherParameters.replace("*", "&")
			);
			return;
		}
		smaction.redirectAction( 
			"", 
			"Successfully re-created export for batch number '" + sBatchNumber + "'.",
			sOtherParameters.replace("*", "&")
		);
		return;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
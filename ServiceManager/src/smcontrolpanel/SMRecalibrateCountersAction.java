package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableinvoiceheaders;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablesmoptions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;

public class SMRecalibrateCountersAction extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String EDIT_PROCESS_NAME = "RECALIBRATE COUNTERS";
	private static char [] cCharacterArray = "abcdefghijklmnopqrstuvwxyz-!@#$%^&*+=-".toCharArray();
	private String SQL;
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME)
	    		+ " " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    Connection conn = clsDatabaseFunctions.getConnection(
	    		getServletContext(), 
	    		sDBID, 
	    		"MySQL", 
	    		"smcontrolpanel.SMPrintPreInvoiceGenerate - user: " 
	    	);
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMRecalibrateOrderAndInvoiceCounters)){
			return;
		}
	    
	    //sCallingClass will look like: smar.ARAgedTrialBalanceReport
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		String sWarning = "";
	    /**************Get Parameters**************/

    	//Customized title
    	String sTitle = "SM Recalibrate Order And Invoice Number Counters";
    	
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sTitle + " - " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME) + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"" + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "\">" +
		   "<TABLE BORDER=0 WIDTH=100%>" +
		   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>"
		   + clsDateAndTimeConversions.nowStdFormat()
		   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME).toString() + "</B></FONT></TD></TR>" +
		   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sTitle + "</B></FONT></TD></TR>"
		   );
		   
    	if (request.getParameter("Confirm") == null){
    		sWarning = "You chose to recalibrate the counters, but you did not check the 'Confirm' checkbox.";
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
    	}
    	if (conn == null){
    		sWarning += "Unable to get data connection.";
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + clsServletUtilities.URLEncode(sWarning)
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
    	} 
    	//Get the Invoice Number
    	String m_sInvoiceNumber = "";
    	try{
    		
    		SQL =  "SELECT " + 
    				SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber + 
    				" FROM  " + SMTableinvoiceheaders.TableName + 
    				" WHERE ( NOT "+ SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber +" REGEXP '[a-z]')" + 
    				" ORDER BY CAST(" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber + " as signed) DESC" + 
    				" LIMIT 1";
    		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID);
    		if(rs.next()){
    			m_sInvoiceNumber = rs.getString(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber);
    			m_sInvoiceNumber =  m_sInvoiceNumber.replaceAll("[^0-9.]", "");
    			int addOneInvoiceNumber = Integer.parseInt(m_sInvoiceNumber) + 1;
    			m_sInvoiceNumber = String.valueOf(addOneInvoiceNumber);
    		}
    	}catch(SQLException e){
    		System.out.println(" Error [1531855779] "+e.getMessage());
    		sWarning = "Error retrieving Invoice Number [1531242877] "+e.getMessage();
    	}
    	//Get the Order Number 
    	String m_sOrderNumber = "";
    	try{
    		String sOrderNumberColumn = "sOrderNumberColumn";
    		SQL =  "SELECT"
        			+ " "+sOrderNumberColumn+"  FROM ("+Select_Replace_String(SMTableorderheaders.sOrderNumber)+""+Alphabet_String()+""+From_Statement_String(sOrderNumberColumn,SMTableorderheaders.TableName)+") as b"
        			+ " ORDER BY CAST("+sOrderNumberColumn+" as signed) DESC"
        			+ " LIMIT 1";
    		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID);
    		if(rs.next()){
    			m_sOrderNumber = rs.getString(sOrderNumberColumn);
    			m_sOrderNumber = m_sOrderNumber.replaceAll("[^0-9.]", "");
    			int addOneOrderNumber = Integer.parseInt(m_sOrderNumber) + 1;
    			m_sOrderNumber = String.valueOf(addOneOrderNumber);
    		}
    	}catch(SQLException e){
    		System.out.println(" Error [1531855779] "+e.getMessage());
    		sWarning = "Error retrieving Invoice Number [1531242877] "+e.getMessage();
    	}
    	SMLogEntry log = new SMLogEntry(sDBID, getServletContext());
    	log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMRECALIBRATECOUNTERS, "Recalibration attempted", "", "[1389802121]");
    	//Reset flag:
    	boolean bResetSuccessful = true;
    	try{
    		String SQL = "UPDATE " + SMTablesmoptions.TableName
    			+ " SET " + SMTablesmoptions.lnextordernumber + " = "
    			+ m_sOrderNumber
    			+ ", " + SMTablesmoptions.snextinvoicenumber + " = "
    			+m_sInvoiceNumber
    			+ " , " + SMTablesmoptions.lnextorderuniquifier + " = "
    			+ " ("
	    			+ "SELECT"
	    			+ " " + SMTableorderheaders.dOrderUniqueifier + " + 1"
	    			+ " FROM " + SMTableorderheaders.TableName
	    			+ " ORDER BY " + SMTableorderheaders.dOrderUniqueifier + " DESC"
	    			+ " LIMIT 1"
    			+ ")"
	    		+ ", " + SMTablesmoptions.slasteditdate + " = DATE_FORMAT(NOW(), '%Y%m%d')"
	    		+ ", " + SMTablesmoptions.slastedittime + " = DATE_FORMAT(NOW(), '%H%i%s')"
	    		+ ", " + SMTablesmoptions.slasteditprocess + " = '" + EDIT_PROCESS_NAME + "'"
	    		+ ", " + SMTablesmoptions.llastedituserid + " = " + sUserID 
	    		+ ", " + SMTablesmoptions.slastedituserfullname + " = '" + sUserFullName + "'"
	    	;
	    		
    		if (!clsDatabaseFunctions.executeSQL(
    				SQL, 
    				getServletContext(), 
    				sDBID,
    				"MySQL",
    				this.toString() + ".doGet - User: " + sUserFullName)){
    			bResetSuccessful = false;
    			sWarning = "Could not execute update statement.";
    		}
    	}catch (SQLException e){
    		bResetSuccessful = false;
    		sWarning = "Error updating flag - " + e.getMessage();
    	}
    	
    	//If there was no error message, simply advise that the delete process was successful:
    	if (bResetSuccessful){
    		sWarning = "Recalibration was successful.";
    		log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMRECALIBRATECOUNTERS, "Recalibration was successful", SQL, "[1389802122]");
    	}else{
    		sWarning = sWarning + "<BR>recalibration failed.";
    		log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMRECALIBRATECOUNTERS, "Recalibration failed", SQL + " - " + sWarning, "[1389802123]");
    	}
		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		);			
    	return;
	}
	
	public String Select_Replace_String (String sOriginalColumnName){
		String SQL = "SELECT";
		for(int i = 0; i < cCharacterArray.length; i++){
			SQL += " REPLACE(";
		}
		SQL += " LOWER("+ sOriginalColumnName +")";
		return SQL;
	}
	public String Alphabet_String(){
		String SQL = "";
		for(int i = 0; i < cCharacterArray.length; i++){
			SQL += ",'"+cCharacterArray[i]+"','')";
		}
		return SQL;
	}
	public String From_Statement_String(String sNewColumnName, String sTableName){
		String SQL = "AS "+sNewColumnName+"  FROM " + sTableName +"";
		return SQL;
		}
	
	public static int stringToint(String str){
		int i_sum = 0;
		int i_ZeroAsciiValue = '0';
		for(char letter :str.toCharArray()){
			int tmpAscii = letter;
			i_sum = (i_sum*10)+(tmpAscii-i_ZeroAsciiValue);
		}
		return i_sum;
    }   
}


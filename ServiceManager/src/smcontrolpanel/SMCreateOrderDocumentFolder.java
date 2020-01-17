package smcontrolpanel;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTablesmoptions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMCreateOrderDocumentFolder extends HttpServlet{
	//OBSOLETE? - We no longer create folders for order documents
	private static final long serialVersionUID = 1L;

	//HttpServletRequest parameters:
	private String m_sWarning;
	
	private static String sDBID = "";
	private static String sUserID = "";
	private static String sUserFullName = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				-1L))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    m_sWarning = "";
	    
	    String sOrderNumber = clsManageRequestParameters.get_Request_Parameter("OrderNumber", request).trim();
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request).trim();
	    String sOriginalCallingClass = clsManageRequestParameters.get_Request_Parameter("OriginalCallingClass", request).trim();
	    
    	try {
			if (!createDocumentsFolder(request, sOrderNumber)){
			}
		} catch (SQLException e) {
			m_sWarning = e.getMessage();
		}
		
		response.sendRedirect(
			"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
			+ "?OrderNumber=" + sOrderNumber
			+ "&CallingClass=" + sOriginalCallingClass
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&Warning=" + m_sWarning
		);
    	return;
	}
	private boolean createDocumentsFolder(HttpServletRequest req, String sOrderNumber) throws SQLException{
		
		String sOrderDocPath = "";
		String SQL = "SELECT * FROM " + SMTablesmoptions.TableName;
		
		try{
			ResultSet rsOptions = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() + ".createDocumentsFolder - user: " +sUserID
				+ " - "
				+ sUserFullName)
			);
			
			if(rsOptions.next()){
				//sOrderDocPath = rsOptions.getString(SMTablesmoptions.sorderdocspath);
			}else{
				m_sWarning = "Could not read order documents path.";
			}
			rsOptions.close();
		}catch (SQLException e){
			System.out.println("[1579267836] Error reading order docs path in " + this.toString() + " - " + e.getMessage());
			m_sWarning = "Error reading order documents path: " + e.getMessage();
			throw e;
		}
		
		if (sOrderDocPath.trim().compareToIgnoreCase("") == 0){
			return false;
		}
		String sAbsoluteOrderDocPath = 
			SMUtilities.getAbsoluteRootPath(req, getServletContext())
			+ sOrderDocPath
			;
		//System.out.println("SMDisplayOrderInformation.ListOrderDocs: sAbsoluteOrderDocPath = '" + sAbsoluteOrderDocPath + "'");
	    File dir = new File(
	    		sAbsoluteOrderDocPath
	    		+ System.getProperty("file.separator")
	    		+ sOrderNumber
	    		+ System.getProperty("file.separator")
	    		);
	    if (dir.exists()) {
	    	m_sWarning = "Documents folder for order number " + sOrderNumber + " already exists.";
	      return false;
	    }
		
	    //Try to create the folder:
	    if (!new File(
	    		sAbsoluteOrderDocPath
	    		+ System.getProperty("file.separator")
	    		+ sOrderNumber
	    		+ System.getProperty("file.separator")
	    		).mkdirs()){
	    	
	    	m_sWarning = "Documents folder for order number " + sOrderNumber + " could not be created.";
		}
	    
	    String sCommand = "";
	    if (getOSName().contains("indows")){
	    	sCommand = "attrib -R " + dir;
	    }else{
	    	sCommand = "chmod 777 " + dir;
	    }
	    
	    try {
			Process p = Runtime.getRuntime().exec(sCommand);
			p.waitFor();
		} catch (IOException e) {
			m_sWarning = "Could not execute permissions command: '" + sCommand + "' - " + e.getMessage();
			return false;
		} catch (InterruptedException e) {
			m_sWarning = "Could not execute permissions command: '" + sCommand + "' - " + e.getMessage();
			return false;
		}

	    /*
	    setWritable and other methods are not implemented correctly in Java:
	    dir = new File(
	    		sAbsoluteOrderDocPath
	    		+ System.getProperty("file.separator")
	    		+ sOrderNumber
	    		+ System.getProperty("file.separator")
	    		);
	    if (!dir.setWritable(true, false)){
	    	m_sWarning = "Could not set folder for universal write access.";
	    	return false;
	    }
	    if (!dir.setExecutable(true, false)){
	    	m_sWarning = "Could not set folder for universal execute access.";
	    	return false;
	    }
	    if (!dir.setReadable(true, false)){
	    	m_sWarning = "Could not set folder for universal read access.";
	    	return false;
	    }
	    */
		return true;	
	}
	private String getOSName()
	  {
		String osName = "";
	    try{
	      osName= System.getProperty("os.name");
	      System.out.println("[1579267846] Operating system name =>"+ osName);
	    }catch (Exception e){
	      System.out.println("[1579267852] Exception caught ="+e.getMessage());
	    }
	    return osName;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
package smcontrolpanel;

import java.io.PrintWriter;
import javax.servlet.ServletContext;

public class SMPrintEstimateSummary extends java.lang.Object {
	
	private String m_sErrorMessage;
	public SMPrintEstimateSummary(
			){
		m_sErrorMessage = "";
	}
	
	public boolean processReport(
			SMEstimateSummary summary,
			String sDBID,
			String sUserID,
			String sUserFullName,
			PrintWriter out,
			ServletContext context,
			String sLicenseModuleLevel
			){
		
		
				return false;
	}
	
}

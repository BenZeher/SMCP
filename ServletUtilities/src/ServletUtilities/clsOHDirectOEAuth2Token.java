package ServletUtilities;

import java.sql.Connection;

public class clsOHDirectOEAuth2Token extends java.lang.Object{

	private static String m_sToken;
	private static String m_sOHDirectRequestURLBase;
	
	public clsOHDirectOEAuth2Token() {
			
	}
	
	public void refreshToken(
		Connection conn
	) throws Exception{
		
		clsOHDirectSettings objsettings = new clsOHDirectSettings();
		try {
			objsettings.load(conn);
		} catch (Exception e1) {
			throw new Exception("Error [202004233541] - " + e1.getMessage());
		}
		
		m_sOHDirectRequestURLBase = objsettings.getsrequesturlbase();		
		try {
			m_sToken = clsOEAuthFunctions.getOHDirectToken(
				objsettings.getstokenusername(), 
				objsettings.getstokenuserpassword(), 
				objsettings.getstokenurl(), 
				objsettings.getsclientid(), 
				objsettings.getsclientsecret()
			);
		} catch (Exception e) {
			throw new Exception("Error [202004233218] - could not get token for authorization - " + e.getMessage());
		}
		
		return;
	}
	
	public String getToken() {
		return m_sToken;
	}
	public String getOHDirectRequestURLBase() {
		return m_sOHDirectRequestURLBase;
	}
}

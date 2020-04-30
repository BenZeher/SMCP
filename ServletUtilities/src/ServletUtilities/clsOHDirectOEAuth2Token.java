package ServletUtilities;

import java.sql.Connection;
import java.util.ArrayList;

public class clsOHDirectOEAuth2Token extends java.lang.Object{

	private static ArrayList<String>arrTokens;
	private static ArrayList<String>arrDBIDs;
	private static ArrayList<String>arrURLBases;
	
	public clsOHDirectOEAuth2Token() {
			if (arrDBIDs == null) {
				arrTokens = new ArrayList<String>(0);
				arrDBIDs = new ArrayList<String>(0);
				arrURLBases = new ArrayList<String>(0);
			}
	}
	
	public void refreshToken(
		Connection conn,
		String sDBID,
		String sUserID
	) throws Exception{
		
		int iIndex = -1;
		for(int i = 0; i < arrDBIDs.size(); i++) {
			if (sDBID.compareToIgnoreCase(arrDBIDs.get(i)) == 0) {
				iIndex = i;
			}
		}
		if (iIndex == -1) {
			//We'll need to add a new array item for this new DBID:
			arrDBIDs.add(sDBID);
			arrTokens.add("");
			arrURLBases.add("");
			iIndex = arrDBIDs.size() - 1;
		}
		
		clsOHDirectSettings objsettings = new clsOHDirectSettings();
		try {
			objsettings.load(conn);
		} catch (Exception e1) {
			throw new Exception("Error [202004233541] - " + e1.getMessage());
		}
		
		arrURLBases.set(iIndex, objsettings.getsrequesturlbase());		
		try {
			arrTokens.set(iIndex, clsOEAuthFunctions.getOHDirectToken(
				objsettings.getstokenusername(), 
				objsettings.getstokenuserpassword(), 
				objsettings.getstokenurl(), 
				objsettings.getsclientid(), 
				objsettings.getsclientsecret(),
				conn,
				sUserID,
				sDBID
			)
			);
		} catch (Exception e) {
			throw new Exception("Error [202004233218] - could not get token for authorization for DBID '" 
				+ sDBID + "' - " + e.getMessage());
		}
		
		return;
	}
	
	public String getToken(String sDBID) {
		for(int i = 0; i < arrDBIDs.size(); i++) {
			if (sDBID.compareToIgnoreCase(arrDBIDs.get(i)) == 0) {
				return arrTokens.get(i);
			}
		}
		return "";
	}
	public String getOHDirectRequestURLBase(String sDBID) {
		for(int i = 0; i < arrDBIDs.size(); i++) {
			if (sDBID.compareToIgnoreCase(arrDBIDs.get(i)) == 0) {
				return arrURLBases.get(i);
			}
		}
		return "";
	}
}

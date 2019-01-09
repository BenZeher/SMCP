package ServletUtilities;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.servlet.ServletContext;

public class clsDBServerTime {

	private long m_lCurrentTimeInMinutes = 0;
	private long m_lCurrentTimeInSeconds = 0;
	private int m_iCurrentDayOfWeek = 0;  //Sunday is '1', Monday is '2', etc.
	private long m_lLastMidnightInSeconds = 0;
	private long m_lLastMidnightInMinutes = 0;
	private java.sql.Date m_datCurrentTime = null;
	
	public clsDBServerTime(Connection conn) throws Exception{
		readTimeFromDB(conn);
	}
	
	public clsDBServerTime(String sDBID, String sUserFullName, ServletContext context) throws Exception{
		
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBID, 
				"MySQL", 
				this.toString() + ".clsDBServerTime - user: " + sUserFullName
			);
		} catch (Exception e) {
			throw new Exception("Error [1494245341] getting connection to read DB server time - " + e.getMessage());
		}
		readTimeFromDB(conn);
		clsDatabaseFunctions.freeConnection(context, conn, "[1546998928]");
	}
	
	private void readTimeFromDB(Connection conn) throws Exception{
		
		Timestamp sqlTimeStamp = new Timestamp(System.currentTimeMillis());
		String sTime = "NOW()";
		
		String SQL = "SELECT"
			+ " ROUND(UNIX_TIMESTAMP(" + sTime + ") / (60)) AS CURRENTTIMEINMINUTES"
			+ ", UNIX_TIMESTAMP(" + sTime + ") AS CURRENTTIMEINSECONDS"
			+ ", DAYOFWEEK(" + sTime + ") AS CURRENTDAYOFWEEK"
			+ ", UNIX_TIMESTAMP(DATE(" + sTime + ")) AS LASTMIDNIGHTINSECONDS"
			+ ", ROUND((UNIX_TIMESTAMP(DATE(" + sTime + "))) / (60)) AS LASTMIDNIGHTINMINUTES"
			+ ", LOCALTIME() AS LOCALTS"
		;

		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				m_lCurrentTimeInMinutes = rs.getLong("CURRENTTIMEINMINUTES");
				m_lCurrentTimeInSeconds = rs.getLong("CURRENTTIMEINSECONDS");
				m_iCurrentDayOfWeek = rs.getInt("CURRENTDAYOFWEEK");
				m_lLastMidnightInSeconds = rs.getLong("LASTMIDNIGHTINSECONDS");
				m_lLastMidnightInMinutes = rs.getLong("LASTMIDNIGHTINMINUTES");
				sqlTimeStamp = rs.getTimestamp("LOCALTS");
				m_datCurrentTime = new Date(sqlTimeStamp.getTime());
	
				//System.out.println("[1528981875] m_lCurrentTimeInMinutes = " + Long.toString(m_lCurrentTimeInMinutes));
				//System.out.println("[1528981875] m_lCurrentTimeInSeconds = " + Long.toString(m_lCurrentTimeInSeconds));
				//System.out.println("[1528981875] m_iCurrentDayOfWeek = " + Long.toString(m_iCurrentDayOfWeek));
				//System.out.println("[1528981875] m_lLastMidnightInSeconds = " + Long.toString(m_lLastMidnightInSeconds));
				//System.out.println("[1528981875] m_lLastMidnightInMinutes = " + Long.toString(m_lLastMidnightInMinutes));
				//System.out.println("[1528981875] m_datCurrentTime = " + m_datCurrentTime);
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1494244859] getting DB Server Time with SQL: '" + SQL + "' - " + e.getMessage());
		}
	}
	
	public long getCurrentTimeInMinutes(){
		return m_lCurrentTimeInMinutes;
	}
	public long getCurrentTimeInSeconds(){
		return m_lCurrentTimeInSeconds;
	}
	public int getCurrentDayOfWeek(){
		//Sunday is '1', Monday is '2', etc.
		return m_iCurrentDayOfWeek;
	}
	public long getLastMidnightInSeconds(){
		return m_lLastMidnightInSeconds;
	}
	public long getLastMidnightInMinutes(){
		return m_lLastMidnightInMinutes;
	}
	public java.sql.Date getCurrentTimeAsJavaSQLDate(){
		return m_datCurrentTime;
	}
	public String getCurrentDateTimeInSelectedFormat(String sFormat){
		SimpleDateFormat sdf = new SimpleDateFormat(sFormat);
		return sdf.format(getCurrentTimeAsJavaSQLDate());
	}
}

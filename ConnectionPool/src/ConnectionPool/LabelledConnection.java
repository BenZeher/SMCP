package ConnectionPool;

import java.sql.*;

public class LabelledConnection{
	private String sLabel;
	private String sType;
	private String sCallingClass;
	private String sDatabaseURL;
	private Connection conn;
	private Timestamp tsCreationTime;
	private long m_lConnectionID;
	
	public String Get_Label(){
		return sLabel;
	}
	
	public void Set_Label(String L){
		sLabel = L;
	}
	
	public String Get_Type(){
		return sType;
	}
	
	public void Set_Type(String T){
		sType = T;
	}

	public String Get_CallingClass(){
		return sCallingClass;
	}
	
	public void Set_CallingClass(String CC){
		sCallingClass = CC;
	}
	public String Get_sDatabaseURL(){
		return sDatabaseURL;
	}
	public void Set_sDatabaseURL(String sDbURL){
		sDatabaseURL = sDbURL;
	}

	public Connection Get_Connection(){
		return conn;
	}
	
	public void Set_Connection(Connection inConn){
		conn = inConn;
	}

	public Timestamp get_Creation_Timestamp(){
		return tsCreationTime;
	}
	
	public void set_Creation_Timestamp(Timestamp inTS){
		tsCreationTime = inTS;
	}
	
	public long get_Connection_ID(){
		return m_lConnectionID;
	}
	
	public void set_Connection_ID(long lConnectionID){
		m_lConnectionID = lConnectionID;
	}
	
	public LabelledConnection(Connection oriConn, 
							  String sL, 
							  String sT, 
							  String sCC,
							  String sDbURL,
							  Timestamp ts,
							  long lConnectionID){
		sLabel = sL;
		sType = sT;
		sCallingClass = sCC;
		sDatabaseURL = sDbURL;
		conn = oriConn;
		tsCreationTime = ts;
		m_lConnectionID = lConnectionID;
		
		//System.out.println("[1500322894] - " + System.currentTimeMillis() + " connection - processID = " + Long.toString(lConnectionID) + ", creation timestamp = " + ts);
	}
	
	public LabelledConnection(){
		sLabel = "";
		sType = "";
		sCallingClass = "";
		sDatabaseURL = "";
		conn = null;
		tsCreationTime = null;
		m_lConnectionID = 0;
	}
}

package TimeCardSystem;

import java.sql.Timestamp;

public class LeaveTypeTotal {
	
	private int id;
	private String sTitle;
	private String sDesc;
	private double dTotalLogged;
	private double dTotalCredit;
	private Timestamp tsStartDate;
	private boolean bIsCarriedOver;
	private double dMaxHour;
	
	public int getID(){
		return id;
	}
	
	public String getTitle(){
		return sTitle;
	}
	
	public String getDesc(){
		return sDesc;
	}
	
	public double getTotalLogged(){
		return dTotalLogged;
	}

	public void setTotalLogged(double d){
		dTotalLogged = d;
	}
	
	public double getTotalCredit(){
		return dTotalCredit;
	}

	public void setTotalCredit(double d){
		dTotalCredit = d;
	}
	
	public Timestamp getStartDate(){
		return tsStartDate;
	}

	public void setStartDate(Timestamp ts){
		tsStartDate = ts;
	}
	
	public boolean IsCarriedOver(){
		return bIsCarriedOver;
	}
	
	public double getMaxHour(){
		return dMaxHour;
	}
	
	public LeaveTypeTotal(){
		id = 0;
		sTitle = "";
		sDesc = "";
		dTotalLogged = 0;
		dTotalCredit = 0;
		tsStartDate = null;
		bIsCarriedOver = false;
		dMaxHour = 0;
	}
	
	public LeaveTypeTotal(int i, 
						  String sT, 
						  String sD, 
						  double dLogged, 
						  double dCredit, 
						  Timestamp tsStart,
						  boolean bCarriedOver,
						  double dMaximumHours){
		id = i;
		sTitle = sT;
		sDesc = sD;
		dTotalLogged = dLogged;
		dTotalCredit = dCredit;
		tsStartDate = tsStart;
		bIsCarriedOver = bCarriedOver;
		dMaxHour = dMaximumHours;
	}
}
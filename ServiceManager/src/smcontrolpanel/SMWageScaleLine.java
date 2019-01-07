package smcontrolpanel;

import java.sql.Date;

import ServletUtilities.clsMasterEntry;

public class SMWageScaleLine extends clsMasterEntry{
	
	public static final String ParamObjectName = "Wage Scale Line";
	
	
	//private static final Date ParamDate = new Date(System.currentTimeMillis()); //job date
	//Member variables for the data:
	private String m_sEmployeeName;
	private String m_sEmployeeSSN;
	private String m_sEmployeeAddress;
	private String m_sEmployeeCity; 
	private String m_sEmployeeState;
	private String m_sEmployeeZipCode;
	private String m_sEmployeeTitle;
	private String m_sDay; //job day
	private Date m_datDate; //job date
	private String m_sOrderNumber; //order number
	private double m_dRegHours;
	private double m_dOTHours;
	private double m_dDTHours;
	private double m_dPayRate;	
	private double m_dHolidayHours;
	private double m_dPersonalHours;
	private double m_dVacHours;
	private double m_dGross;
	private double m_dFederal;
	private double m_dSS;
	private double m_dMedicare;
	private double m_dState;
	private double m_dMiscDed;
	private double m_dNetPay;
	private double m_dVacAllowed;
	
	public SMWageScaleLine() {
		super();
		initBidVariables();
        }


    public void addErrorMessage(String sMsg){
    	super.addErrorMessage(sMsg);
    }
    
	public String getM_EmployeeName() {
		return m_sEmployeeName;
	}
	public void setM_EmployeeName(String s) {
		m_sEmployeeName = s;
	}
    
	public String getM_EmployeeSSN() {
		return m_sEmployeeSSN;
	}
	public void setM_EmployeeSSN(String s) {
		m_sEmployeeSSN = s;
	}
    
	public String getM_EmployeeAddress() {
		return m_sEmployeeAddress;
	}
	public void setM_EmployeeAddress(String s) {
		m_sEmployeeAddress = s;
	}
    
	public String getM_EmployeeCity() {
		return m_sEmployeeCity;
	}
	public void setM_EmployeeCity(String s) {
		m_sEmployeeCity = s;
	}
    
	public String getM_EmployeeState() {
		return m_sEmployeeState;
	}
	public void setM_EmployeeState(String s) {
		m_sEmployeeState = s;
	}
    
	public String getM_EmployeeZipCode() {
		return m_sEmployeeZipCode;
	}
	public void setM_EmployeeZipCode(String s) {
		m_sEmployeeZipCode = s;
	}
    
	public String getM_EmployeeTitle() {
		return m_sEmployeeTitle;
	}
	public void setM_EmployeeTitle(String s) {
		m_sEmployeeTitle = s;
	}
    
	public String getM_Day() {
		return m_sDay;
	}
	public void setM_Day(String s) {
		m_sDay = s;
	}
    
	public Date getM_Date() {
		return m_datDate;
	}
	public void setM_Date(Date d) {
		m_datDate = d;
	}
    
	public String getM_OrderNumber() {
		return m_sOrderNumber;
	}
	public void setM_OrderNumber(String s) {
		m_sOrderNumber = s;
	}
    
	public double getM_RegHours() {
		return m_dRegHours;
	}
	public void setM_RegHours(double d) {
		m_dRegHours = d;
	}
    
	public double getM_OTHours() {
		return m_dOTHours;
	}
	public void setM_OTHours(double d) {
		m_dOTHours = d;
	}
    
	public double getM_DTHours() {
		return m_dDTHours;
	}
	public void setM_DTHours(double d) {
		m_dDTHours = d;
	}
    
	public double getM_PayRate() {
		return m_dPayRate;
	}
	public void setM_PayRate(double d) {
		m_dPayRate = d;
	}
    
	public double getM_HolidayHours() {
		return m_dHolidayHours;
	}
	public void setM_HolidayHours(double d) {
		m_dHolidayHours = d;
	}
    
	public double getM_PersonalHours() {
		return m_dPersonalHours;
	}
	public void setM_PersonalHours(double d) {
		m_dPersonalHours = d;
	}
    
	public double getM_VacHours() {
		return m_dVacHours;
	}
	public void setM_VacHours(double d) {
		m_dVacHours = d;
	}
    
	public double getM_Gross() {
		return m_dGross;
	}
	public void setM_Gross(double d) {
		m_dGross = d;
	}
    
	public double getM_Federal() {
		return m_dFederal;
	}
	public void setM_Federal(double d) {
		m_dFederal = d;
	}
    
	public double getM_SS() {
		return m_dSS;
	}
	public void setM_SS(double d) {
		m_dSS = d;
	}
    
	public double getM_Medicare() {
		return m_dMedicare;
	}
	public void setM_Medicare(double d) {
		m_dMedicare = d;
	}
    
	public double getM_State() {
		return m_dState;
	}
	public void setM_State(double d) {
		m_dState = d;
	}
    
	public double getM_MiscDed() {
		return m_dMiscDed;
	}
	public void setM_MiscDed(double d) {
		m_dMiscDed = d;
	}
    
	public double getM_NetPay() {
		return m_dNetPay;
	}
	public void setM_NetPay(double d) {
		m_dNetPay = d;
	}
    
	public double getM_VacAllowed() {
		return m_dVacAllowed;
	}
	public void setM_VacAllowed(double d) {
		m_dVacAllowed = d;
	}
	
    private void initBidVariables(){
    	
    	m_sEmployeeName = "";
    	m_sEmployeeSSN = "";
    	m_sEmployeeAddress = "";
    	m_sEmployeeCity = ""; 
    	m_sEmployeeState = "";
    	m_sEmployeeZipCode = "";
    	m_sEmployeeTitle = "";
    	m_sDay = "";
    	m_datDate = new Date(System.currentTimeMillis());
    	m_sOrderNumber = "";
    	m_dRegHours = 0;
    	m_dOTHours = 0;
    	m_dDTHours = 0;
    	m_dPayRate = 0;	
    	m_dHolidayHours = 0;
    	m_dPersonalHours = 0;
    	m_dVacHours = 0;
    	m_dGross = 0;
    	m_dFederal = 0;
    	m_dSS = 0;
    	m_dMedicare = 0;
    	m_dState = 0;
    	m_dMiscDed = 0;
    	m_dNetPay = 0;
    	m_dVacAllowed = 0;
		super.initVariables();
		super.setObjectName(ParamObjectName);
    }
}
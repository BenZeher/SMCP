package smic;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsManageRequestParameters;

public class ICOptionInput extends java.lang.Object{

	public static final String ParamBatchPostingInProcess = "BatchPostingInProcess";
	public static final String ParamPostingUserFullName = "PostingUserFullName";
	public static final String ParamPostingProcess = "PostingProcess";
	public static final String ParamPostingStartDate = "PostingStartDate";
	public static final String ParamCostingMethod = "CostingMethod";
	public static final String ParamAllowNegativeQtys = "AllowNegativeQtys";
	public static final String Paramiexportto = "iexportto";
	public static final String Paramifeedgl = "ifeedgl";
	public static final String Paramisuppressbarcodesonnonstockitems = "SuppressBarCodesOnNonStockItems";
	
	//Fields for creating new purchase orders folders in Google Drive:
	public static final String Paramsgdrivepurchaseordersparentfolderid = "gdrivepurchaseordersparentfolderid";
	public static final String Paramsgdrivepurchaseordersfolderprefix = "gdrivepurchaseordersfolderprefix";
	public static final String Paramsgdrivepurchaseordersfoldersuffix = "gdrivepurchaseordersfoldersuffix";
	
	private String m_sBatchPostingInProcess;
	private String m_sPostingUserFullName;
	private String m_sPostingProcess;
	private String m_sPostingStartDate;
	private String m_sCostingMethod;
	private String m_sAllowNegativeQtys;
	private String m_sExportTo;
	private String m_isuppressbarcodesonnonstockitems;
	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String> (0);
	private String m_sgdrivepurchaseordersparentfolderid;
	private String m_sgdrivepurchaseordersfolderprefix;
	private String m_sgdrivepurchaseordersfoldersuffix;
	private String m_ifeedgl;

	public ICOptionInput(){
		m_sBatchPostingInProcess = "";
		m_sPostingUserFullName = "";
		m_sPostingProcess = "";
		m_sPostingStartDate = "";
		m_sCostingMethod = "";
		m_sAllowNegativeQtys = "";
		m_sExportTo = "";
		m_isuppressbarcodesonnonstockitems = "";
		m_sgdrivepurchaseordersparentfolderid = "";
		m_sgdrivepurchaseordersfolderprefix = "";
		m_sgdrivepurchaseordersfoldersuffix = "";
		m_ifeedgl = "0";
		m_sErrorMessageArray = new ArrayList<String> (0);
	}
	
	public ICOptionInput (HttpServletRequest req){

		m_sBatchPostingInProcess = clsManageRequestParameters.get_Request_Parameter(ICOptionInput.ParamBatchPostingInProcess, req).trim();
		m_sPostingUserFullName = clsManageRequestParameters.get_Request_Parameter(ICOptionInput.ParamPostingUserFullName, req).trim();
		m_sPostingProcess = clsManageRequestParameters.get_Request_Parameter(ICOptionInput.ParamPostingProcess, req).trim();
		m_sPostingStartDate = clsManageRequestParameters.get_Request_Parameter(ICOptionInput.ParamPostingStartDate, req).trim();
		m_ifeedgl = clsManageRequestParameters.get_Request_Parameter(ICOptionInput.Paramifeedgl, req).trim();
		m_sCostingMethod = clsManageRequestParameters.get_Request_Parameter(ICOptionInput.ParamCostingMethod, req).trim();
		if (req.getParameter(ParamAllowNegativeQtys) != null){
			m_sAllowNegativeQtys = req.getParameter(ParamAllowNegativeQtys);
		}else{
			m_sAllowNegativeQtys = "0";
		}
		
		m_sExportTo = clsManageRequestParameters.get_Request_Parameter(ICOptionInput.Paramiexportto, req).trim();
		if (req.getParameter(Paramisuppressbarcodesonnonstockitems) != null){
			m_isuppressbarcodesonnonstockitems = req.getParameter(Paramisuppressbarcodesonnonstockitems);
		}else{
			m_isuppressbarcodesonnonstockitems = "0";
		}
		m_sErrorMessageArray = new ArrayList<String> (0);
		m_sgdrivepurchaseordersparentfolderid = clsManageRequestParameters.get_Request_Parameter(ICOptionInput.Paramsgdrivepurchaseordersparentfolderid, req).trim();
		m_sgdrivepurchaseordersfolderprefix  = clsManageRequestParameters.get_Request_Parameter(ICOptionInput.Paramsgdrivepurchaseordersfolderprefix, req).trim();
		m_sgdrivepurchaseordersfoldersuffix = clsManageRequestParameters.get_Request_Parameter(ICOptionInput.Paramsgdrivepurchaseordersfoldersuffix, req).trim();
	}
	
	public void clearErrorMessages(){
		m_sErrorMessageArray.clear();
	}
	public ArrayList<String> getErrorMessages(){
		return m_sErrorMessageArray;
	}
	public String getErrorMessageString(){
		String s = "";
		for (int i = 0; i < m_sErrorMessageArray.size(); i ++){
			s += "\n" + m_sErrorMessageArray.get(i);
		}
		return s;
	}
	public boolean loadToICOptionClass (ICOption icoption){
		
		boolean bEntriesAreValid = true;
		//clearErrorMessages();
		//m_sErrorMessageArray = new ArrayList<String>(0);
		//Set the entry values:
		
		//Costing method:
		try{
			if (!icoption.setCostingMethod(Long.parseLong(m_sCostingMethod))){
				bEntriesAreValid = false;
				m_sErrorMessageArray.add(icoption.getErrorMessage());
			}
		}catch(NumberFormatException e){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid costing method - '" + m_sCostingMethod + "'.");
		}

		//Allow negative qtys:
		try{
			if (!icoption.setAllowNegativeQtys(Long.parseLong(m_sAllowNegativeQtys))){
				bEntriesAreValid = false;
				m_sErrorMessageArray.add(icoption.getErrorMessage());
			}
		}catch(NumberFormatException e){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid allow negative qtys value - '" + m_sAllowNegativeQtys + "'.");
		}
		
		if (!icoption.setPostingProcess(m_sPostingProcess)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid posting process value - '" + m_sPostingProcess + "'.");
		}
		
		icoption.setPostingStartDate(m_sPostingStartDate);
		
		if (!icoption.setPostingUserFullName(m_sPostingUserFullName)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid posting user - '" + m_sPostingUserFullName + "'.");
		}
		
		if (!icoption.setExportTo(m_sExportTo)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid export to - '" + m_sExportTo + "'.");
		}
		if (!icoption.setSuppressBarCodesOnNonStockItems(m_isuppressbarcodesonnonstockitems)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid value for 'Suppress bar codes on non-stock items - '" + m_isuppressbarcodesonnonstockitems + "'.");
		}
		icoption.setfeedgl(m_ifeedgl);
		icoption.setgdrivepurchaseordersparentfolderid(m_sgdrivepurchaseordersparentfolderid);
		icoption.setgdrivepurchaseordersfolderprefix(m_sgdrivepurchaseordersfolderprefix);
		icoption.setgdrivepurchaseordersfoldersuffix(m_sgdrivepurchaseordersfoldersuffix);
		
		return bEntriesAreValid;
	}
	
	public boolean loadFromICOptionClass(ICOption icoption){
		
		m_sBatchPostingInProcess = Long.toString(icoption.getBatchPostingInProcess());
		m_sPostingUserFullName = icoption.getPostingUserFullName();
		m_sPostingProcess = icoption.getPostingProcess();
		m_sPostingStartDate = icoption.getPostingStartDate();
		
		m_sCostingMethod = Long.toString(icoption.getCostingMethod());
		m_sAllowNegativeQtys = Long.toString(icoption.getAllowNegativeQtys());
		m_sExportTo = Long.toString(icoption.getExportTo());
		m_isuppressbarcodesonnonstockitems = Long.toString(icoption.getSuppressBarCodesOnNonStockItems());
		m_sgdrivepurchaseordersparentfolderid = icoption.getgdrivepurchaseordersparentfolderid();
		m_sgdrivepurchaseordersfolderprefix = icoption.getgdrivepurchaseordersfolderprefix();
		m_sgdrivepurchaseordersfoldersuffix = icoption.getgdrivepurchaseordersfoldersuffix();
		m_ifeedgl = icoption.getfeedgl();
		return true;
	}
	
	public String getBatchPostingInProcess() {
		return m_sBatchPostingInProcess;
	}

	public void setBatchPostingInProcess(String sBatchPostingInProcess) {
		this.m_sBatchPostingInProcess = sBatchPostingInProcess;
	}

	public String getPostingUserFullName() {
		return m_sPostingUserFullName;
	}

	public void setPostingUserFullName(String sPostingUserFullName) {
		this.m_sPostingUserFullName = sPostingUserFullName;
	}

	public String getPostingProcess() {
		return m_sPostingProcess;
	}

	public void setPostingProcess(String sPostingProcess) {
		this.m_sPostingProcess = sPostingProcess;
	}

	public String getPostingStartDate() {
		return m_sPostingStartDate;
	}

	public void setPostingStartDate(String sPostingStartDate) {
		this.m_sPostingStartDate = sPostingStartDate;
	}

	public String getCostingMethod() {
		return m_sCostingMethod;
	}

	public void setCostingMethod(String sCostingMethod) {
		this.m_sCostingMethod = sCostingMethod;
	}
	public String getAllowNegativeQtys() {
		return m_sAllowNegativeQtys;
	}

	public void setAllowNegativeQtys(String sAllowNegativeQtys) {
		this.m_sAllowNegativeQtys = sAllowNegativeQtys;
	}
	public String getExportTo() {
		return m_sExportTo;
	}

	public void setExportTo(String sExportTo) {
		this.m_sExportTo = sExportTo;
	}
	
	public String getfeedgl() {
		return m_ifeedgl;
	}

	public void setfeedgl(String sfeedgl) {
		this.m_ifeedgl = sfeedgl;
	}
	
	public String getSuppressBarCodesOnNonStockItems() {
		return m_isuppressbarcodesonnonstockitems;
	}

	public void setSuppressBarCodesOnNonStockItems(String sSuppressBarCodesOnNonStockItems) {
		this.m_isuppressbarcodesonnonstockitems = sSuppressBarCodesOnNonStockItems;
	}
	
	 public String getgdrivepurchaseordersparentfolderid(){
	    	return m_sgdrivepurchaseordersparentfolderid;
	}
	 public void setgdrivepurchaseordersparentfolderid(String sgdrivepurchaseordersparentfolderid){
	    	m_sgdrivepurchaseordersparentfolderid = sgdrivepurchaseordersparentfolderid;
	}
	 public String getgdrivepurchaseordersfolderprefix(){
	    	return m_sgdrivepurchaseordersfolderprefix;
	}
	public void setgdrivepurchaseordersfolderprefix(String sgdrivepurchaseordersfolderprefix){
	    	m_sgdrivepurchaseordersfolderprefix = sgdrivepurchaseordersfolderprefix;
	}
	public String getgdrivepurchaseordersfoldersuffix(){
	    	return m_sgdrivepurchaseordersfoldersuffix;
	}
	public void setgdrivepurchaseordersfoldersuffix(String sgdrivepurchaseordersfoldersuffix){
	    	m_sgdrivepurchaseordersfoldersuffix = sgdrivepurchaseordersfoldersuffix;
	}
	public String getQueryString(){
	
		String sQueryString = "";
		sQueryString += ParamBatchPostingInProcess + "=" + clsServletUtilities.URLEncode(m_sBatchPostingInProcess);
		sQueryString += "&" + ParamPostingUserFullName + "=" + clsServletUtilities.URLEncode(m_sPostingUserFullName);
		sQueryString += "&" + ParamPostingProcess + "=" + clsServletUtilities.URLEncode(m_sPostingProcess);
		sQueryString += "&" + ParamPostingStartDate + "=" + clsServletUtilities.URLEncode(m_sPostingStartDate);
		sQueryString += "&" + ParamCostingMethod + "=" + clsServletUtilities.URLEncode(m_sCostingMethod);
		sQueryString += "&" + ParamAllowNegativeQtys + "=" + clsServletUtilities.URLEncode(m_sAllowNegativeQtys);
		sQueryString += "&" + Paramiexportto + "=" + clsServletUtilities.URLEncode(m_sExportTo);
		sQueryString += "&" + Paramifeedgl + "=" + clsServletUtilities.URLEncode(m_ifeedgl);
		sQueryString += "&" + Paramisuppressbarcodesonnonstockitems + " = " + clsServletUtilities.URLEncode(m_isuppressbarcodesonnonstockitems);
		sQueryString += "&" + Paramsgdrivepurchaseordersparentfolderid + "=" + clsServletUtilities.URLEncode(m_sgdrivepurchaseordersparentfolderid);
		sQueryString += "&" + Paramsgdrivepurchaseordersfolderprefix + "=" + clsServletUtilities.URLEncode(m_sgdrivepurchaseordersfolderprefix);
		sQueryString += "&" + Paramsgdrivepurchaseordersfoldersuffix + "=" + clsServletUtilities.URLEncode(m_sgdrivepurchaseordersfoldersuffix);
		return sQueryString;
	}
	public String getDataDump(){
	
		String s = "";
		s += ParamBatchPostingInProcess + "=" + clsServletUtilities.URLEncode(m_sBatchPostingInProcess);
		s += "\n" + ParamPostingUserFullName + "=" + clsServletUtilities.URLEncode(m_sPostingUserFullName);
		s += "\n" + ParamPostingProcess + "=" + clsServletUtilities.URLEncode(m_sPostingProcess);
		s += "\n" + ParamPostingStartDate + "=" + clsServletUtilities.URLEncode(m_sPostingStartDate);
		s += "\n" + ParamCostingMethod + "=" + clsServletUtilities.URLEncode(m_sCostingMethod);
		s += "\n" + ParamAllowNegativeQtys + "=" + clsServletUtilities.URLEncode(m_sAllowNegativeQtys);
		s += "\n" + Paramiexportto + "=" + clsServletUtilities.URLEncode(m_sExportTo);
		s += "\n" + Paramifeedgl + "=" + clsServletUtilities.URLEncode(m_ifeedgl);
		s += "\n" + Paramisuppressbarcodesonnonstockitems + " = " + clsServletUtilities.URLEncode(m_isuppressbarcodesonnonstockitems);
		s += "\n" + Paramsgdrivepurchaseordersparentfolderid + "=" + clsServletUtilities.URLEncode(m_sgdrivepurchaseordersparentfolderid);
		s += "\n" + Paramsgdrivepurchaseordersfolderprefix + "=" + clsServletUtilities.URLEncode(m_sgdrivepurchaseordersfolderprefix);
		s += "\n" + Paramsgdrivepurchaseordersfoldersuffix + "=" + clsServletUtilities.URLEncode(m_sgdrivepurchaseordersfoldersuffix);
		return s;
	}
}
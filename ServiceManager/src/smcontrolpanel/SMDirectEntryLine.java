package smcontrolpanel;

import java.math.BigDecimal;
import java.sql.Connection;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import smic.ICCategory;
import SMDataDefinition.SMTableiccategories;
import SMDataDefinition.SMTableicitems;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;

public class SMDirectEntryLine extends clsMasterEntry{
	
	public static final String ParamObjectName = "Direct Entry Line";
	
	//Particular to the specific class
	public static final String Paramllinenumber = "llinenumber";
	public static final String Paramsquantity = "squantity";
	public static final String Paramsunitofmeasure = "sunitofmeasure";
	public static final String Paramsitemnumber = "sitemnumber";
	public static final String Paramsestimatedextendedmaterialcost = "sestimatedextendedmaterialcost";
	public static final String Paramsestimatedextendedlaborcost = "sestimatedextendedlaborcost";
	public static final String Paramsitemdescription = "sitemdescription";
	public static final String Paramsextendedmaterialbillingvalue = "sextendedmaterialbillingvalue";
	public static final String Paramsextendedlaborbillingvalue = "sextendedlaborbillingvalue";
	public static final String Paramsgrossprofit = "sgrossprofit";
	public static final String Paramsextendedlaborunits = "sextendedlaborunits";
	public static final String Paramsitemcategory = "sitemcategory";

	public static final int squantityScale = 4;
	public static final int sestimatedextendedmaterialcostScale = 2;
	public static final int sestimatedextendedlaborcostScale = 2;
	public static final int sextendedmaterialbillingvalueScale = 2;
	public static final int sextendedlaborbillingvalueScale = 2;
	public static final int sgrossprofitScale = 2;
	public static final int sextendedlaborunitsScale = 2;
	
	public static final int sunitofmeasureLength = SMTableicitems.sCostUnitOfMeasureLength;
	public static final int sitemnumberLength = SMTableicitems.sItemNumberLength;
	public static final int sitemdescriptionLength = SMTableicitems.sItemDescriptionLength;
	public static final int scategorycodeLength = SMTableiccategories.sCategoryCodeLength;
	
	private static final String DEFAULT_UNIT_OF_MEASURE = "EA";
	
	private String m_llinenumber;
	private String m_sitemnumber;
	private String m_sitemdescription;
	private String m_sunitofmeasure;
	private String m_scategorycode;
	private String m_squantity;
	private String m_sestimatedextendedmaterialcost;
	private String m_sestimatedextendedlaborcost;
	private String m_sextendedmaterialbillingvalue;
	private String m_sextendedlaborbillingvalue;
	private String m_sgrossprofit;
	private String m_sextendedlaborunits;
	
	public SMDirectEntryLine() {
		super();
		initBidVariables();
        }

	SMDirectEntryLine (HttpServletRequest req){
		super(req);
		initBidVariables();
		
		m_llinenumber = clsManageRequestParameters.get_Request_Parameter(SMDirectEntryLine.Paramllinenumber, req);
		m_sitemnumber = clsManageRequestParameters.get_Request_Parameter(SMDirectEntryLine.Paramsitemnumber, req);
		m_sitemdescription = clsManageRequestParameters.get_Request_Parameter(SMDirectEntryLine.Paramsitemdescription, req);
		m_scategorycode = clsManageRequestParameters.get_Request_Parameter(SMDirectEntryLine.Paramsitemcategory, req);
		m_sunitofmeasure = clsManageRequestParameters.get_Request_Parameter(SMDirectEntryLine.Paramsunitofmeasure, req);
		m_squantity = clsManageRequestParameters.get_Request_Parameter(SMDirectEntryLine.Paramsquantity, req).trim();
		if (m_squantity.compareToIgnoreCase("") == 0){
			m_squantity = clsManageBigDecimals.BigDecimalToScaledFormattedString(
				squantityScale, BigDecimal.ZERO);
		}
		m_sestimatedextendedmaterialcost = clsManageRequestParameters.get_Request_Parameter(
				SMDirectEntryLine.Paramsestimatedextendedmaterialcost, req).trim();
		if (m_sestimatedextendedmaterialcost.compareToIgnoreCase("") == 0){
			m_sestimatedextendedmaterialcost = clsManageBigDecimals.BigDecimalToScaledFormattedString(
				sestimatedextendedmaterialcostScale, BigDecimal.ZERO);
		}
		m_sestimatedextendedlaborcost = clsManageRequestParameters.get_Request_Parameter(
				SMDirectEntryLine.Paramsestimatedextendedlaborcost, req).trim();
		if (m_sestimatedextendedlaborcost.compareToIgnoreCase("") == 0){
			m_sestimatedextendedlaborcost = clsManageBigDecimals.BigDecimalToScaledFormattedString(
				sestimatedextendedlaborcostScale, BigDecimal.ZERO);
		}
		m_sextendedmaterialbillingvalue = clsManageRequestParameters.get_Request_Parameter(
				SMDirectEntryLine.Paramsextendedmaterialbillingvalue, req).trim();
		if (m_sextendedmaterialbillingvalue.compareToIgnoreCase("") == 0){
			m_sextendedmaterialbillingvalue = clsManageBigDecimals.BigDecimalToScaledFormattedString(
				sextendedmaterialbillingvalueScale, BigDecimal.ZERO);
		}
		m_sextendedlaborbillingvalue = clsManageRequestParameters.get_Request_Parameter(
				SMDirectEntryLine.Paramsextendedlaborbillingvalue, req).trim();
		if (m_sextendedlaborbillingvalue.compareToIgnoreCase("") == 0){
			m_sextendedlaborbillingvalue = clsManageBigDecimals.BigDecimalToScaledFormattedString(
				sextendedlaborbillingvalueScale, BigDecimal.ZERO);
		}
		m_sgrossprofit = clsManageRequestParameters.get_Request_Parameter(
				SMDirectEntryLine.Paramsgrossprofit, req).trim();
		if (m_sgrossprofit.compareToIgnoreCase("") == 0){
			m_sgrossprofit = clsManageBigDecimals.BigDecimalToScaledFormattedString(
				sgrossprofitScale, BigDecimal.ZERO);
		}
		m_sextendedlaborunits = clsManageRequestParameters.get_Request_Parameter(
				SMDirectEntryLine.Paramsextendedlaborunits, req).trim();
		if (m_sextendedlaborunits.compareToIgnoreCase("") == 0){
			m_sextendedlaborunits = clsManageBigDecimals.BigDecimalToScaledFormattedString(
				sextendedlaborunitsScale, BigDecimal.ZERO);
		}
	}

    public boolean validate_entry_fields(
    		ServletContext context, 
			String sDBID, 
			String sUser,
			String sUserID,
			String sUserFullName
    		){
    	
    	boolean bResult = true;
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBID, 
    			"MySQL", 
    			SMUtilities.getFullClassName(this.toString() + ".validate_entry_fields - user: " + sUserID + " - " + sUserFullName)
    	);
    	if (conn == null){
    		super.addErrorMessage("Could not get connection to validate entry fields.");
    		return false;
    	}
    	
    	bResult = validate_entry_fields(conn);
    	clsDatabaseFunctions.freeConnection(context, conn);
    	
    	return bResult;
    	
    }
    public boolean validate_entry_fields (Connection conn){
        //Validate the entries here:
    	boolean bEntriesAreValid = true;

    	long lID;
		try {
			lID = Long.parseLong(m_llinenumber);
		} catch (NumberFormatException e) {
        	super.addErrorMessage("Invalid line number: '" + m_llinenumber + "'.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
		}
    	if (lID < -1){
        	super.addErrorMessage("Invalid line number: '" + m_llinenumber + "'.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
    	}
    	
		//Qty ordered:
		m_squantity = m_squantity.replace(",", "");
        if (m_squantity.compareToIgnoreCase("") == 0){
        	m_squantity = clsManageBigDecimals.BigDecimalToScaledFormattedString(
        		squantityScale, BigDecimal.ZERO);
        }
		BigDecimal bdQty = new BigDecimal(0);
        try{
        	bdQty = new BigDecimal(m_squantity);
        	//If the qty is anything but a positive number, set it to zero:
            if (bdQty.compareTo(BigDecimal.ZERO) <= 0){
            	bdQty = new BigDecimal("0.00");
            }else{
            	m_squantity = clsManageBigDecimals.BigDecimalToScaledFormattedString(
                		squantityScale, bdQty);
            }
        }catch(NumberFormatException e){
    		super.addErrorMessage("Invalid quantity: '" + m_squantity + "'.  ");
    		bEntriesAreValid = false;
        }
        //If a line has no qty on it, then nothing else needs to be validated:
    	if (bdQty.compareTo(BigDecimal.ZERO) == 0){
    		return bEntriesAreValid;
    	}
        //Item number:
    	m_sitemnumber = m_sitemnumber.trim();
    	//Item number CAN be blank if it hasn't been created yet:
        //if (m_sitemnumber.compareToIgnoreCase("") == 0){
        //	super.addErrorMessage("Item number cannot be blank.");
        //	bEntriesAreValid = false;
        //}
        if (m_sitemnumber.length() > sitemnumberLength){
        	super.addErrorMessage("Item number is too long.");
        	bEntriesAreValid = false;
        }
        //Item description:
    	m_sitemdescription = m_sitemdescription.trim();
        if (m_sitemdescription.compareToIgnoreCase("") == 0){
        	super.addErrorMessage("Item description cannot be blank.");
        	bEntriesAreValid = false;
        }
        if (m_sitemdescription.length() > sitemdescriptionLength){
        	super.addErrorMessage("Item description is too long.");
        	bEntriesAreValid = false;
        }
        
        if (m_scategorycode.compareToIgnoreCase("") == 0){
        	super.addErrorMessage("Category code cannot be blank.");
        	bEntriesAreValid = false;
        }
        if (m_scategorycode.length() > scategorycodeLength){
        	super.addErrorMessage("Category code is too long.");
        	bEntriesAreValid = false;
        }
        
        ICCategory cat = new ICCategory(m_scategorycode);
        if (!cat.load(conn)){
        	super.addErrorMessage("Category code '" + m_scategorycode + "' is invalid.");
        	bEntriesAreValid = false;
        }
        
        //Unit of measure:
    	m_sunitofmeasure = m_sunitofmeasure.trim();
        if (m_sunitofmeasure.compareToIgnoreCase("") == 0){
        	super.addErrorMessage("Unit of measure cannot be blank.");
        	bEntriesAreValid = false;
        }
        if (m_sunitofmeasure.length() > sunitofmeasureLength){
        	super.addErrorMessage("Unit of measure is too long.");
        	bEntriesAreValid = false;
        }
        
        //Estimated material cost:
		m_sestimatedextendedmaterialcost = m_sestimatedextendedmaterialcost.replace(",", "");
        if (m_sestimatedextendedmaterialcost.compareToIgnoreCase("") == 0){
        	m_sestimatedextendedmaterialcost = clsManageBigDecimals.BigDecimalToScaledFormattedString(
        			sestimatedextendedmaterialcostScale, BigDecimal.ZERO);
        }
		BigDecimal bdEstimatedMaterialCost = new BigDecimal(0);
        try{
        	bdEstimatedMaterialCost = new BigDecimal(m_sestimatedextendedmaterialcost);
            if (bdEstimatedMaterialCost.compareTo(BigDecimal.ZERO) <= 0){
            	super.addErrorMessage("Estimated material cost must be a positive number: " 
            			+ m_sestimatedextendedmaterialcost + ".  ");
        		bEntriesAreValid = false;
            }else{
            	m_sestimatedextendedmaterialcost = clsManageBigDecimals.BigDecimalToScaledFormattedString(
            			sestimatedextendedmaterialcostScale, bdEstimatedMaterialCost);
            }
        }catch(NumberFormatException e){
    		super.addErrorMessage("Invalid estimated material cost: '" + m_sestimatedextendedmaterialcost + "'.  ");
    		bEntriesAreValid = false;
        }

        //Estimated labor cost:
		m_sestimatedextendedlaborcost = m_sestimatedextendedlaborcost.replace(",", "");
        if (m_sestimatedextendedlaborcost.compareToIgnoreCase("") == 0){
        	m_sestimatedextendedlaborcost = clsManageBigDecimals.BigDecimalToScaledFormattedString(
        		sestimatedextendedlaborcostScale, BigDecimal.ZERO);
        }
		BigDecimal bdEstimatedLaborCost = new BigDecimal(0);
        try{
        	bdEstimatedLaborCost = new BigDecimal(m_sestimatedextendedlaborcost);
            if (bdEstimatedLaborCost.compareTo(BigDecimal.ZERO) < 0){
            	super.addErrorMessage("Estimated labor cost must be a positive number: " 
            			+ m_sestimatedextendedlaborcost + ".  ");
        		bEntriesAreValid = false;
            }else{
            	m_sestimatedextendedlaborcost = clsManageBigDecimals.BigDecimalToScaledFormattedString(
                		sestimatedextendedlaborcostScale, bdEstimatedLaborCost);
            }
        }catch(NumberFormatException e){
    		super.addErrorMessage("Invalid estimated labor cost: '" + m_sestimatedextendedlaborcost + "'.  ");
    		bEntriesAreValid = false;
        }
        
        //Material billing value:
		m_sextendedmaterialbillingvalue = m_sextendedmaterialbillingvalue.replace(",", "");
        if (m_sextendedmaterialbillingvalue.compareToIgnoreCase("") == 0){
        	m_sextendedmaterialbillingvalue = clsManageBigDecimals.BigDecimalToScaledFormattedString(
        		sextendedmaterialbillingvalueScale, BigDecimal.ZERO);
        }
		BigDecimal bdMaterialBillingValue = new BigDecimal(0);
        try{
        	bdMaterialBillingValue = new BigDecimal(m_sextendedmaterialbillingvalue);
            //if (bdMaterialBillingValue.compareTo(BigDecimal.ZERO) <= 0){
            //	super.addErrorMessage("Material billing value must be a positive number: " 
            //			+ m_smaterialbillingvalue + ".  ");
        	//	bEntriesAreValid = false;
            //}else{
            	m_sextendedmaterialbillingvalue = clsManageBigDecimals.BigDecimalToScaledFormattedString(
                		sextendedmaterialbillingvalueScale, bdMaterialBillingValue);
            //}
        }catch(NumberFormatException e){
    		super.addErrorMessage("Invalid material billing value: '" + m_sextendedmaterialbillingvalue + "'.  ");
    		bEntriesAreValid = false;
        }
        
        //Labor billing value:
		m_sextendedlaborbillingvalue = m_sextendedlaborbillingvalue.replace(",", "");
        if (m_sextendedlaborbillingvalue.compareToIgnoreCase("") == 0){
        	m_sextendedlaborbillingvalue = clsManageBigDecimals.BigDecimalToScaledFormattedString(
        		sextendedlaborbillingvalueScale, BigDecimal.ZERO);
        }
		BigDecimal bdLaborBillingValue = new BigDecimal(0);
        try{
        	bdLaborBillingValue = new BigDecimal(m_sextendedlaborbillingvalue);
            //if (bdLaborBillingValue.compareTo(BigDecimal.ZERO) <= 0){
            //	super.addErrorMessage("Labor billing value must be a positive number: " 
            //			+ m_slaborbillingvalue + ".  ");
        	//	bEntriesAreValid = false;
            //}else{
            	m_sextendedlaborbillingvalue = clsManageBigDecimals.BigDecimalToScaledFormattedString(
                		sextendedlaborbillingvalueScale, bdLaborBillingValue);
            //}
        }catch(NumberFormatException e){
    		super.addErrorMessage("Invalid labor billing value: '" + m_sextendedlaborbillingvalue + "'.  ");
    		bEntriesAreValid = false;
        }
        
        //Gross profit:
		m_sgrossprofit = m_sgrossprofit.replace(",", "");
        if (m_sgrossprofit.compareToIgnoreCase("") == 0){
        	m_sgrossprofit = clsManageBigDecimals.BigDecimalToScaledFormattedString(
        		sextendedlaborbillingvalueScale, BigDecimal.ZERO);
        }
		BigDecimal bdGrossProfit = new BigDecimal(0);
        try{
        	bdGrossProfit = new BigDecimal(m_sgrossprofit);
            //if (bdGrossProfit.compareTo(BigDecimal.ZERO) <= 0){
            //	super.addErrorMessage("Gross profit must be a positive number: " 
            //			+ m_sgrossprofit + ".  ");
        	//	bEntriesAreValid = false;
            //}else{
            	m_sgrossprofit = clsManageBigDecimals.BigDecimalToScaledFormattedString(
                		sgrossprofitScale, bdGrossProfit);
            //}
        }catch(NumberFormatException e){
    		super.addErrorMessage("Invalid gross profit: '" + m_sgrossprofit + "'.  ");
    		bEntriesAreValid = false;
        }
        //Labor units:
		m_sextendedlaborunits = m_sextendedlaborunits.replace(",", "");
        if (m_sextendedlaborunits.compareToIgnoreCase("") == 0){
        	m_sextendedlaborunits = clsManageBigDecimals.BigDecimalToScaledFormattedString(
        		sextendedlaborunitsScale, BigDecimal.ZERO);
        }
		BigDecimal bdLaborUnits = new BigDecimal(0);
        try{
        	bdLaborUnits = new BigDecimal(m_sextendedlaborunits);
            if (bdLaborUnits.compareTo(BigDecimal.ZERO) < 0){
            	super.addErrorMessage("Labor units must be a positive number: " 
            			+ m_sextendedlaborunits + ".  ");
        		bEntriesAreValid = false;
            }else{
            	m_sextendedlaborunits = clsManageBigDecimals.BigDecimalToScaledFormattedString(
                		sextendedlaborunitsScale, bdLaborUnits);
            }
        }catch(NumberFormatException e){
    		super.addErrorMessage("Invalid gross profit: '" + m_sextendedlaborunits + "'.  ");
    		bEntriesAreValid = false;
        }
        
    	return bEntriesAreValid;
    }

    public void addErrorMessage(String sMsg){
    	super.addErrorMessage(sMsg);
    }
	public String getM_llinenumber() {
		return m_llinenumber;
	}

	public void setM_llinenumber(String mLlinenumber) {
		m_llinenumber = mLlinenumber;
	}

	public String getM_sitemnumber() {
		return m_sitemnumber;
	}

	public void setM_sitemnumber(String mSitemnumber) {
		m_sitemnumber = mSitemnumber;
	}

	public String getM_sitemdescription() {
		return m_sitemdescription;
	}

	public void setM_sitemdescription(String mSitemdescription) {
		m_sitemdescription = mSitemdescription;
	}

	public String getM_sunitofmeasure() {
		return m_sunitofmeasure;
	}

	public void setM_sunitofmeasure(String mSunitofmeasure) {
		m_sunitofmeasure = mSunitofmeasure;
	}
	
	public String getM_scategorycode() {
		return m_scategorycode;
	}

	public void setM_scategorycode(String mScategorycode) {
		m_scategorycode = mScategorycode;
	}

	public String getM_squantity() {
		return m_squantity;
	}

	public void setM_squantity(String mSquantity) {
		m_squantity = mSquantity;
	}
	public BigDecimal getbdQuantity() throws NumberFormatException{
		try {
			return new BigDecimal(this.getM_squantity().replace(",", ""));
		} catch (NumberFormatException e) {
			throw e;
		}
	}
	public void set_bdQuantity(BigDecimal bdQuantity){
		m_squantity = clsManageBigDecimals.BigDecimalToScaledFormattedString(squantityScale, bdQuantity);
	}
	public String getM_sestimatedextendedmaterialcost() {
		return m_sestimatedextendedmaterialcost;
	}

	public void setM_sestimatedextendedmaterialcost(String mSestimatedextendedmaterialcost) {
		m_sestimatedextendedmaterialcost = mSestimatedextendedmaterialcost;
	}
	public BigDecimal getbdEstimatedExtendedMaterialCost() throws NumberFormatException{
		try {
			return new BigDecimal(this.getM_sestimatedextendedmaterialcost().replace(",", ""));
		} catch (NumberFormatException e) {
			throw e;
		}
	}
	public void setbdEstimateExtendedMaterialCost(BigDecimal bdEstimatedExtendedMaterialCost){
		m_sestimatedextendedmaterialcost = clsManageBigDecimals.BigDecimalToScaledFormattedString(
				sestimatedextendedmaterialcostScale, bdEstimatedExtendedMaterialCost);
	}
	public String getM_sestimatedextendedlaborcost() {
		return m_sestimatedextendedlaborcost;
	}

	public void setM_sestimatedextendedlaborcost(String mSestimatedextendedlaborcost) {
		m_sestimatedextendedlaborcost = mSestimatedextendedlaborcost;
	}
	public BigDecimal getbdEstimatedExtendedLaborCost() throws NumberFormatException{
		try {
			return new BigDecimal(this.getM_sestimatedextendedlaborcost().replace(",", ""));
		} catch (NumberFormatException e) {
			throw e;
		}
	}
	public void setbdEstimateExtendedLaborCost(BigDecimal bdEstimatedExtendedLaborCost){
		m_sestimatedextendedlaborcost = clsManageBigDecimals.BigDecimalToScaledFormattedString(
			sestimatedextendedlaborcostScale, bdEstimatedExtendedLaborCost);
	}
	public String getM_sextendedmaterialbillingvalue() {
		return m_sextendedmaterialbillingvalue;
	}

	public void setM_sextendedmaterialbillingvalue(String mSextendedmaterialbillingvalue) {
		m_sextendedmaterialbillingvalue = mSextendedmaterialbillingvalue;
	}
	public BigDecimal getbdEstimatedMaterialBillingValue() throws NumberFormatException{
		try {
			return new BigDecimal(this.getM_sextendedmaterialbillingvalue().replace(",", ""));
		} catch (NumberFormatException e) {
			throw e;
		}
	}
	public void setbdExtendedMaterialBillingValue(BigDecimal bdExtendedMaterialBillingValue){
		m_sextendedmaterialbillingvalue = clsManageBigDecimals.BigDecimalToScaledFormattedString(
			sextendedmaterialbillingvalueScale, bdExtendedMaterialBillingValue);
	}
	public String getM_sextendedlaborbillingvalue() {
		return m_sextendedlaborbillingvalue;
	}

	public void setM_sextendedlaborbillingvalue(String mSextendedlaborbillingvalue) {
		m_sextendedlaborbillingvalue = mSextendedlaborbillingvalue;
	}
	public BigDecimal getbdExtendedLaborBillingValue() throws NumberFormatException{
		try {
			return new BigDecimal(this.getM_sextendedlaborbillingvalue().replace(",", ""));
		} catch (NumberFormatException e) {
			throw e;
		}
	}
	public void setbdExtendedLaborBillingValue(BigDecimal bdExtendedLaborBillingValue){
		m_sextendedlaborbillingvalue = clsManageBigDecimals.BigDecimalToScaledFormattedString(
			sextendedlaborbillingvalueScale, bdExtendedLaborBillingValue);
	}
	public String getM_sgrossprofit() {
		return m_sgrossprofit;
	}

	public void setM_sgrossprofit(String mSgrossprofit) {
		m_sgrossprofit = mSgrossprofit;
	}
	public BigDecimal getbdGrossProfit() throws NumberFormatException{
		try {
			return new BigDecimal(this.getM_sgrossprofit().replace(",", ""));
		} catch (NumberFormatException e) {
			throw e;
		}
	}
	public String getM_sextendedlaborunits() {
		return m_sextendedlaborunits;
	}

	public void setM_sextendedlaborunits(String mSextendedlaborunits) {
		m_sextendedlaborunits = mSextendedlaborunits;
	}
	public BigDecimal getbdExtendedLaborUnits() throws NumberFormatException{
		try {
			return new BigDecimal(this.getM_sextendedlaborunits().replace(",", ""));
		} catch (NumberFormatException e) {
			throw e;
		}
	}
	public void set_bdExtendedLaborUnits(BigDecimal bdExtendedLaborUnits){
		m_sextendedlaborunits = clsManageBigDecimals.BigDecimalToScaledFormattedString(sextendedlaborunitsScale, bdExtendedLaborUnits);
	}
	
    private void initBidVariables(){
    	
    	m_llinenumber = "-1";
    	m_sitemnumber = "";
    	m_sitemdescription = "";
    	m_sunitofmeasure = DEFAULT_UNIT_OF_MEASURE;
    	m_scategorycode = "";
    	m_squantity = "0.0000";
    	m_sestimatedextendedmaterialcost = "0.00";
    	m_sestimatedextendedlaborcost = "0.00";
    	m_sextendedmaterialbillingvalue = "0.00";
    	m_sextendedlaborbillingvalue = "0.00";
    	m_sgrossprofit = "0.00";
    	m_sextendedlaborunits = "0.00";
		super.initVariables();
		super.setObjectName(ParamObjectName);
    }
    public String read_out_debug_data(){
    	String sResult = "\n  ** Direct entry lines read out: ";
    	sResult += "\nLine number: " + this.getM_llinenumber();
    	sResult += "\nItem number: " + this.getM_sitemnumber();
    	sResult += "\nItem description: " + this.getM_sitemdescription();
    	sResult += "\nCategory code:" + this.getM_scategorycode();
    	sResult += "\nUnit of measure: " + this.getM_sunitofmeasure();
    	sResult += "\nQty: " + this.getM_squantity();
    	sResult += "\nEstimated material cost: " + this.getM_sestimatedextendedmaterialcost();
    	sResult += "\nEstimated labor cost: " + this.getM_sestimatedextendedlaborcost();
    	sResult += "\nMaterial billing value: " + this.getM_sextendedmaterialbillingvalue();
    	sResult += "\nLabor billing value: " + this.getM_sextendedlaborbillingvalue();
    	sResult += "\nGross profit: " + this.getM_sgrossprofit();
    	sResult += "\nLabor units: " + this.getM_sextendedlaborunits();
    	return sResult;
    }
	public String getQueryString(){
		//Particular to the specific class
		String sQueryString = "";
		sQueryString += "&" + ParamObjectName + "=" 
			+ clsServletUtilities.URLEncode(getObjectName());
		sQueryString += "&" + Paramllinenumber + "=" 
			+ clsServletUtilities.URLEncode(getM_llinenumber());
		sQueryString += "&" + Paramsestimatedextendedlaborcost + "=" 
			+ clsServletUtilities.URLEncode(getM_sestimatedextendedlaborcost());
		sQueryString += "&" + Paramsestimatedextendedmaterialcost + "=" 
			+ clsServletUtilities.URLEncode(getM_sestimatedextendedmaterialcost());
		sQueryString += "&" + Paramsgrossprofit + "=" 
			+ clsServletUtilities.URLEncode(getM_sgrossprofit());
		sQueryString += "&" + Paramsitemdescription + "=" 
			+ clsServletUtilities.URLEncode(getM_sitemdescription());
		sQueryString += "&" + m_sitemnumber + "=" 
			+ clsServletUtilities.URLEncode(getM_sitemnumber());
		sQueryString += "&" + Paramsextendedlaborbillingvalue + "=" 
			+ clsServletUtilities.URLEncode(getM_sextendedlaborbillingvalue());
		sQueryString += "&" + Paramsextendedlaborunits + "=" 
			+ clsServletUtilities.URLEncode(getM_sextendedlaborunits());
		sQueryString += "&" + Paramsextendedmaterialbillingvalue + "=" 
			+ clsServletUtilities.URLEncode(getM_sextendedmaterialbillingvalue());
		sQueryString += "&" + Paramsquantity + "=" 
			+ clsServletUtilities.URLEncode(getM_squantity());
		sQueryString += "&" + Paramsunitofmeasure + "=" 
			+ clsServletUtilities.URLEncode(getM_sunitofmeasure());
		sQueryString += "&" + Paramsitemcategory + "=" 
				+ clsServletUtilities.URLEncode(getM_scategorycode());
		return sQueryString;
	}
}
package smcontrolpanel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import smic.ICItem;
import SMClasses.SMOrderDetail;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMTableiccategories;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTablelabortypes;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMIncorporateSummary extends clsMasterEntry{
	public static final String ParamObjectName = "Summary Incorporation";
	
	//Particular to the specific class
	public static final String Paramsordernumber = "ordernumber";
	public static final String Paramtotalbillingamount = "stotalbillingamount";
	public static final String Paramsunitlaborcost = "sunitlaborcost";
	public static final String Paramslabortype = "slabortype";
	public static final String Paramslocation = "slocation";
	public static final String Paramstaxjurisdiction = "staxjurisdiction";
	public static final String Paramsitemcategory = "sitemcategory";
	public static final String Paramsnonstockmaterialitem = "snonstockmaterialitem";
	public static final String Paramssummaryid = "ssummaryid";
	//public static final String Paramsunitlabormarkup = "sunitlabormarkup";

	public static final int scontractamountScale = 2;
	public static final int sunitlaborcostScale = 2;
	public static final int sunitlabormarkupScale = 2;
	
	public static final int sordernumberLength = SMTableorderheaders.sOrderNumberLength;
	public static final int slocationLength = SMTablelocations.sLocationLength;
	public static final int sitemcategoryLength = SMTableiccategories.sCategoryCodeLength;
	public static final int sdescriptionLength = 128;
	
	//private static final String DEFAULT_UNIT_LABOR_AMOUNT = "200.00";
	private static final int NUMBER_OF_INITIAL_LINES = 5;
	
	private String m_strimmedordernumber;
	private String m_stotalbillingamount;
	private String m_sunitlaborcost;
	private String m_slabortype;
	private String m_slocation;
	private String m_staxjurisdiction;
	private String m_sitemcategory;
	private String m_sunitlabormarkup;
	private String m_sdescription;
	private String m_snonstockmaterialitem;
	private String m_ssummaryid;
	private ArrayList<SMSummaryIncorporationLine> m_arrLines;
	
	private boolean bDebugMode =false;
	
	public SMIncorporateSummary() {
		super();
		initVariables();
		initSummaryIncorporationVariables();
        }

	SMIncorporateSummary (HttpServletRequest req){
		super(req);
		initVariables();
		initSummaryIncorporationVariables();
		
		m_stotalbillingamount = clsManageRequestParameters.get_Request_Parameter(Paramtotalbillingamount, req);
		if (m_stotalbillingamount.compareToIgnoreCase("") == 0){
			m_stotalbillingamount = clsManageBigDecimals.BigDecimalToScaledFormattedString(
				scontractamountScale, BigDecimal.ZERO);
		}
		m_sitemcategory = clsManageRequestParameters.get_Request_Parameter(Paramsitemcategory, req);
		m_slabortype = clsManageRequestParameters.get_Request_Parameter(Paramslabortype, req);
		m_slocation = clsManageRequestParameters.get_Request_Parameter(Paramslocation, req);
		m_strimmedordernumber = clsManageRequestParameters.get_Request_Parameter(Paramsordernumber, req);
		m_staxjurisdiction = clsManageRequestParameters.get_Request_Parameter(Paramstaxjurisdiction, req);
		m_sunitlaborcost = clsManageRequestParameters.get_Request_Parameter(Paramsunitlaborcost, req);
		if (m_sunitlaborcost.compareToIgnoreCase("") == 0){
			m_sunitlaborcost = clsManageBigDecimals.BigDecimalToScaledFormattedString(
				sunitlaborcostScale, BigDecimal.ZERO);
		}
		m_snonstockmaterialitem = clsManageRequestParameters.get_Request_Parameter(Paramsnonstockmaterialitem, req);
		m_ssummaryid = clsManageRequestParameters.get_Request_Parameter(Paramssummaryid, req);

		//Now read each line of the entry:
		m_arrLines.clear();
		int iCounter = 0;
		while (clsManageRequestParameters.get_Request_Parameter(
				SMSummaryIncorporationLine.Paramllinenumber 
			+ clsStringFunctions.PadLeft(Integer.toString(iCounter), "0", 6), req).compareToIgnoreCase("") != 0){
			//Read all the values into the lines:
			SMSummaryIncorporationLine line = new SMSummaryIncorporationLine();
			line.setM_llinenumber(clsManageRequestParameters.get_Request_Parameter(SMDirectEntryLine.Paramllinenumber 
					+ clsStringFunctions.PadLeft(Integer.toString(iCounter), "0", 6), req));
			line.setM_sestimatedextendedlaborcost(clsManageRequestParameters.get_Request_Parameter(SMDirectEntryLine.Paramsestimatedextendedlaborcost
					+ clsStringFunctions.PadLeft(Integer.toString(iCounter), "0", 6), req));
			line.setM_sestimatedextendedmaterialcost(clsManageRequestParameters.get_Request_Parameter(SMDirectEntryLine.Paramsestimatedextendedmaterialcost
					+ clsStringFunctions.PadLeft(Integer.toString(iCounter), "0", 6), req));
			line.setM_sgrossprofit(clsManageRequestParameters.get_Request_Parameter(SMDirectEntryLine.Paramsgrossprofit
					+ clsStringFunctions.PadLeft(Integer.toString(iCounter), "0", 6), req));
			line.setM_sitemdescription(clsManageRequestParameters.get_Request_Parameter(SMDirectEntryLine.Paramsitemdescription
					+ clsStringFunctions.PadLeft(Integer.toString(iCounter), "0", 6), req));
			line.setM_sitemnumber(clsManageRequestParameters.get_Request_Parameter(SMDirectEntryLine.Paramsitemnumber
					+ clsStringFunctions.PadLeft(Integer.toString(iCounter), "0", 6), req));
			line.setM_scategorycode(clsManageRequestParameters.get_Request_Parameter(SMDirectEntryLine.Paramsitemcategory
					+ clsStringFunctions.PadLeft(Integer.toString(iCounter), "0", 6), req));
			//If there is no category code for the separate line, then use the header category code:
			if (line.getM_scategorycode().compareToIgnoreCase("") == 0){
				line.setM_scategorycode(m_sitemcategory);
			}
			line.setM_sextendedlaborbillingvalue(clsManageRequestParameters.get_Request_Parameter(SMDirectEntryLine.Paramsextendedlaborbillingvalue
					+ clsStringFunctions.PadLeft(Integer.toString(iCounter), "0", 6), req));
			line.setM_sextendedlaborunits(clsManageRequestParameters.get_Request_Parameter(SMDirectEntryLine.Paramsextendedlaborunits 
				+ clsStringFunctions.PadLeft(Integer.toString(iCounter), "0", 6), req));
			line.setM_sextendedmaterialbillingvalue(clsManageRequestParameters.get_Request_Parameter(SMDirectEntryLine.Paramsextendedmaterialbillingvalue
					+ clsStringFunctions.PadLeft(Integer.toString(iCounter), "0", 6), req));
			line.setM_squantity(clsManageRequestParameters.get_Request_Parameter(SMDirectEntryLine.Paramsquantity
					+ clsStringFunctions.PadLeft(Integer.toString(iCounter), "0", 6), req));
			line.setM_sunitofmeasure(clsManageRequestParameters.get_Request_Parameter(SMDirectEntryLine.Paramsunitofmeasure
					+ clsStringFunctions.PadLeft(Integer.toString(iCounter), "0", 6), req));
			m_arrLines.add(line);
			iCounter++;
		}
		//We always want a minimum number of lines in the entry, so here we'll add more if we need them:
		for (int i = m_arrLines.size(); i < NUMBER_OF_INITIAL_LINES; i++){
			m_arrLines.add(new SMSummaryIncorporationLine());
			m_arrLines.get(m_arrLines.size() - 1).setM_llinenumber(Integer.toString(m_arrLines.size()));
		}
		if (bDebugMode){
			for (int i = 0; i < m_arrLines.size(); i++){
				System.out.println("[1368539621] direct entry line description = '" + m_arrLines.get(i).getM_llinenumber() + " - " + m_arrLines.get(i).getM_sitemdescription());
			}
		}
	}

    public void validate_entry_fields(
    		ServletContext context, 
			String sDBID, 
			String sUser) throws Exception{
    	
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBID, 
    			"MySQL", 
    			SMUtilities.getFullClassName(this.toString() + ".validate_entry_fields - user: " + sUser)
    	);
    	if (conn == null){
    		throw new Exception("Could not get connection to validate entry fields.");
    	}
    	
    	try {
			validate_entry_fields(conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080692]");
			throw new Exception(e.getMessage());
		}
    }
    public void validate_entry_fields (Connection conn) throws Exception{
        //Validate the entries here:
    	boolean bEntriesAreValid = true;

    	m_strimmedordernumber = m_strimmedordernumber.trim();
        if (m_strimmedordernumber.compareToIgnoreCase("") == 0){
        	super.addErrorMessage("Order number cannot be blank.");
        	bEntriesAreValid = false;
        }
        if (m_strimmedordernumber.length() > sordernumberLength){
        	super.addErrorMessage("Order number is too long.");
        	bEntriesAreValid = false;
        }
    	
    	String SQL = "SELECT"
    		+ " " + SMTableorderheaders.sOrderNumber
    		+ " FROM " + SMTableorderheaders.TableName
    		+ " WHERE ("
    			+ SMTableorderheaders.strimmedordernumber + " = '" 
    			+ m_strimmedordernumber.trim() + "'" 
    			//+ " AND (" + SMTableorderheaders.iOrderType + " != " + SMTableorderheaders.ORDERTYPE_QUOTE + ")"
    		+ ")"
    		;
    	if (bDebugMode){
    		System.out.println("[1579268713] In " + this.toString() + ".validate_entry_fields - SQL = " + SQL);
    	}

    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (!rs.next()){
			   	super.addErrorMessage("Order number is not valid.");
				bEntriesAreValid = false;
			}
			rs.close();
		} catch (SQLException e1) {
		   	super.addErrorMessage("Error validating order number - " + e1.getMessage());
			bEntriesAreValid = false;
		}
    	
		//Contract amount:
		m_stotalbillingamount = m_stotalbillingamount.replace(",", "");
        if (m_stotalbillingamount.compareToIgnoreCase("") == 0){
        	m_stotalbillingamount = clsManageBigDecimals.BigDecimalToScaledFormattedString(
        		scontractamountScale, BigDecimal.ZERO);
        }
		BigDecimal bdContractAmount = new BigDecimal(0);
        try{
        	bdContractAmount = new BigDecimal(m_stotalbillingamount);
            if (bdContractAmount.compareTo(BigDecimal.ZERO) <= 0){
            	super.addErrorMessage("Total billing amount must be a positive number: " + m_stotalbillingamount + ".  ");
        		bEntriesAreValid = false;
            }else{
            	m_stotalbillingamount = clsManageBigDecimals.BigDecimalToScaledFormattedString(
                		scontractamountScale, bdContractAmount);
            }
        }catch(NumberFormatException e){
    		super.addErrorMessage("Invalid total billing amount: '" + m_stotalbillingamount + "'.  ");
        }

        //Unit labor cost:
		m_sunitlaborcost = m_sunitlaborcost.replace(",", "");
        if (m_sunitlaborcost.compareToIgnoreCase("") == 0){
        	m_sunitlaborcost = clsManageBigDecimals.BigDecimalToScaledFormattedString(
        		sunitlaborcostScale, BigDecimal.ZERO);
        }
		BigDecimal bdUnitLaborCost = new BigDecimal(0);
        try{
        	bdUnitLaborCost = new BigDecimal(m_sunitlaborcost);
            if (bdUnitLaborCost.compareTo(BigDecimal.ZERO) <= 0){
            	super.addErrorMessage("Unit labor cost must be a positive number: " + m_sunitlaborcost + ".  ");
        		bEntriesAreValid = false;
            }else{
            	m_sunitlaborcost = clsManageBigDecimals.BigDecimalToScaledFormattedString(
                		sunitlaborcostScale, bdUnitLaborCost);
            }
        }catch(NumberFormatException e){
    		super.addErrorMessage("Invalid unit labor cost: '" + m_sunitlaborcost + "'.  ");
    		bEntriesAreValid = false;
        }
        
		m_sunitlabormarkup = m_sunitlabormarkup.replace(",", "");
        if (m_sunitlabormarkup.compareToIgnoreCase("") == 0){
        	m_sunitlabormarkup = clsManageBigDecimals.BigDecimalToScaledFormattedString(
        		sunitlabormarkupScale, BigDecimal.ZERO);
        }
		BigDecimal bdUnitLaborMarkupAmount = new BigDecimal(0);
        try{
        	bdUnitLaborMarkupAmount = new BigDecimal(m_sunitlabormarkup);
            //if (bdUnitLaborMarkupAmount.compareTo(BigDecimal.ZERO) <= 0){
            //	super.addErrorMessage("Unit labor markup must be a positive number: " + m_sunitlabormarkup + ".  ");
        	//	bEntriesAreValid = false;
            //}else{
            	m_sunitlabormarkup = clsManageBigDecimals.BigDecimalToScaledFormattedString(
                		scontractamountScale, bdUnitLaborMarkupAmount);
            //}
        }catch(NumberFormatException e){
    		super.addErrorMessage("Invalid unit labor markup amount: '" + m_sunitlabormarkup + "'.  ");
    		bEntriesAreValid = false;
        }
        
        //Labor type:
        m_slabortype = m_slabortype.trim();
        if (m_slabortype.compareToIgnoreCase("") == 0){
        	super.addErrorMessage("You must select a labor type.");
        	bEntriesAreValid = false;
        }

        //Location:
        m_slocation = m_slocation.trim();
        if (m_slocation.compareToIgnoreCase("") == 0){
        	super.addErrorMessage("You must select a location.");
        	bEntriesAreValid = false;
        }

        //Tax
        m_staxjurisdiction = m_staxjurisdiction.trim();
        if (m_staxjurisdiction.compareToIgnoreCase("") == 0){
        	super.addErrorMessage("You must select a tax type.");
        	bEntriesAreValid = false;
        }
        
        //Item category
        //m_sitemcategory = m_sitemcategory.trim();
        //if (m_sitemcategory.compareToIgnoreCase("") == 0){
        //	super.addErrorMessage("You must select an item category.");
        //	bEntriesAreValid = false;
        //}
        
        //Non-stock material item:
        m_snonstockmaterialitem = m_snonstockmaterialitem.trim();
        if (m_snonstockmaterialitem.compareToIgnoreCase("") != 0){
        	ICItem item = new ICItem(m_snonstockmaterialitem);
        	if (!item.load(conn)){
        		super.addErrorMessage("Item '" + m_snonstockmaterialitem + "' is not a valid item number.");
        		bEntriesAreValid = false;
        	}
        	if (item.getLaborItem().compareToIgnoreCase("1") == 0){
        		super.addErrorMessage("Item '" + m_snonstockmaterialitem + "' is a labor item and cannot be used.");
        		bEntriesAreValid = false;
        	}
        	if (item.getActive().compareToIgnoreCase("0") == 0){
        		super.addErrorMessage("Item '" + m_snonstockmaterialitem + "' is inactive and cannot be used.");
        		bEntriesAreValid = false;
        	}
        	if (item.getNonStockItem().compareToIgnoreCase("0") == 0){
        		super.addErrorMessage("Item '" + m_snonstockmaterialitem + "' is a NOT a non-stock item and cannot be used.");
        		bEntriesAreValid = false;
        	}
        }
        
        for (int i = 0; i < m_arrLines.size(); i++){
        	if (!m_arrLines.get(i).validate_entry_fields(conn)){
        		super.addErrorMessage("Line " + m_arrLines.get(i).getM_llinenumber() + " - " + m_arrLines.get(i).getErrorMessages());
        		bEntriesAreValid = false;
        		break;
        	}
        }
        if (!bEntriesAreValid){
        	throw new Exception(this.getErrorMessages());
        }
    }
    private void removeZeroQtyLines(){
    	ArrayList<SMSummaryIncorporationLine> m_arrTempLines = new ArrayList<SMSummaryIncorporationLine> (0);
    	for (int i = 0; i < m_arrLines.size(); i++){
    		if (m_arrLines.get(i).getbdQuantity().compareTo(BigDecimal.ZERO) > 0){
    			SMSummaryIncorporationLine line = m_arrLines.get(i);
    			line.setM_llinenumber(Integer.toString(i + 1));
    			m_arrTempLines.add(line);
    		}
    	}
    	m_arrLines.clear();
    	for (int i = 0; i < m_arrTempLines.size(); i++){
    		SMSummaryIncorporationLine line = m_arrTempLines.get(i);
			m_arrLines.add(line);
    	}
    	//Now fill out the object to it has a minimum number of lines:
		for (int i = m_arrLines.size(); i < NUMBER_OF_INITIAL_LINES; i++){
			m_arrLines.add(new SMSummaryIncorporationLine());
			m_arrLines.get(m_arrLines.size() - 1).setM_llinenumber(Integer.toString(m_arrLines.size()));
		}
    }
    public void calculateBillingValues(ServletContext context, String sDBID, String sUser, String sUserID, String sUserFullName) throws Exception{

    	int iLastRow;
    	int iLastMaterialRow;
    	int iLastLaborRow;
    	BigDecimal bdLaborPriceSubtotal;
    	BigDecimal bdMaterialPriceSubtotal;
    	BigDecimal bdTotalBillingValueWithoutLabor;
    	BigDecimal bdMaterialCostProportion;
    	BigDecimal bdContractAmountRemaining;
    	
    	if (bDebugMode){
    		System.out.println("[1579268720] In " + this.toString() + ".calculateValues - before validate_entry_fields");
    	}
		Connection conn = clsDatabaseFunctions.getConnection(
				context, sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() + ".calculateBillingValues - user: " + sUserID + " - " + sUserFullName));
		if (conn == null){
			throw new Exception("Could not get data connection");
		}
    	try {
			validate_entry_fields(conn);
		} catch (Exception e) {
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547080442]");
    		throw new Exception(e.getMessage());
		}
    	if (bDebugMode){
    		System.out.println("[1579268723] In " + this.toString() + ".calculateValues - after validate_entry_fields");
    	}
    	
    	removeZeroQtyLines();
    	
    	//Check to make sure that the contract amount, gross profit, and labor unit cost are not zero.
    	if (this.getbdTotalBillingAmount().compareTo(BigDecimal.ZERO) == 0){
    		super.addErrorMessage("Contract amount cannot be zero.");
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547080443]");
    		throw new Exception(this.getErrorMessages());
    	}

    	if (this.getbdUnitLaborCost().compareTo(BigDecimal.ZERO) == 0){
    		super.addErrorMessage("Unit labor cost cannot be zero.");
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547080444]");
    		throw new Exception(this.getErrorMessages());
    	}
        
    	if (bDebugMode){
    		System.out.println("[1579268728] In " + this.toString() + ".calculateValues - up to 01");
    	}
    	
        //Get the TOTAL EXTENDED MATERIAL COST by adding the extended material cost for each line.
        BigDecimal m_dTotalExtendedMaterialCost = new BigDecimal(0);

        for (int i = 0; i < m_arrLines.size(); i++){
            m_dTotalExtendedMaterialCost = m_dTotalExtendedMaterialCost.add(m_arrLines.get(i).getbdEstimatedExtendedMaterialCost());
        }
        //System.out.println("[1380920548] m_dTotalExtendedMaterialCost = " + m_dTotalExtendedMaterialCost);
        //For each line in the breakdown, set the estimated extended labor cost by multiplying the labor unit cost times the labor unit qty:
        for (int i = 0; i < m_arrLines.size(); i++){
            if (m_arrLines.get(i).getbdQuantity().compareTo(BigDecimal.ZERO) > 0){
                //Calculate the number of labor units per item per unit
                m_arrLines.get(i).setbdEstimateExtendedLaborCost(m_arrLines.get(i).getbdExtendedLaborUnits().multiply(getbdUnitLaborCost()));
            } else {
                m_arrLines.get(i).setbdEstimateExtendedLaborCost(BigDecimal.ZERO);
            }
        }

    	if (bDebugMode){
    		System.out.println("[1579268733] In " + this.toString() + ".calculateValues - up to 02");
    	}
    	
    	//Next loop through the lines to find the LASTLINENUMBERWITHMATERIAL and the LASTLINENUMBERWITHLABOR.
        //Find the last row used, the last row with material, and the last row with labor
        iLastRow = m_arrLines.size() - 1;

        iLastMaterialRow = -1;
        iLastLaborRow = -1;
        
        //'For i = iLastRow To 0 Step -1
        for (int i = iLastRow; i >= 0; i--){
            //Don't even check unless the last row hasn't yet been found
            //'If iLastMaterialRow = -1 Then
            if (iLastMaterialRow == -1){
                //Don't count it if there is no quantity
                //'If m_arrQuoteItems(i).ItemQuantity > 0 Then
            	if (m_arrLines.get(i).getbdQuantity().compareTo(BigDecimal.ZERO) > 0){
                    //'If m_arrQuoteItems(i).EstimatedMaterialCost > 0 Then
            		if (m_arrLines.get(i).getbdEstimatedExtendedMaterialCost().compareTo(BigDecimal.ZERO) > 0){
                        iLastMaterialRow = i;
            		}
            	}
            }

            //'If iLastLaborRow = -1 Then
            if (iLastLaborRow == -1){
                //Don't count it if there is no quantity
                //'If m_arrQuoteItems(i).ItemQuantity > 0 Then
            	if (m_arrLines.get(i).getbdQuantity().compareTo(BigDecimal.ZERO) > 0){
                    //'If m_arrQuoteItems(i).EstimatedLaborCost > 0 Then
            		if (m_arrLines.get(i).getbdEstimatedExtendedLaborCost().compareTo(BigDecimal.ZERO) > 0){
                        iLastLaborRow = i;
            		}
            	}
            }
        }

    	if (bDebugMode){
    		System.out.println("[1579268737] In " + this.toString() + ".calculateValues - up to 03");
    	}
    	
    	//Calculate the labor billing values first: For each line, calculate the selling price by adding the estimated labor cost
    	//to (the labor unit markup times the number of labor units).
        //Now calculate the labor
        bdLaborPriceSubtotal = BigDecimal.ZERO;

        //Get the markup per labor unit:
        try {
			setUnitLaborMarkup(conn);
		} catch (Exception e) {
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547080445]");
    		throw new Exception(e.getMessage());
		}
        
        //Assign all of the labor billing values first:
        //'For i = 0 To UBound(m_arrQuoteItems)
        for (int i = 0; i < m_arrLines.size(); i++){
            //'If m_arrQuoteItems(i).ItemQuantity > 0 Then
        	if (m_arrLines.get(i).getbdQuantity().compareTo(BigDecimal.ZERO) > 0){
                //'m_arrQuoteItems(i).LaborBillingValue = CDbl(m_arrQuoteItems(i).EstimatedLaborCost + (m_dDailyLaborMarkUp * m_arrQuoteItems(i).LaborUnit))
        		BigDecimal bdCalculatedLaborDetailPrice = m_arrLines.get(i).getbdEstimatedExtendedLaborCost().add(getbdUnitLaborMarkup().multiply(m_arrLines.get(i).getbdExtendedLaborUnits()));
        		//Round this to the nearest dollar:
        		m_arrLines.get(i).setbdExtendedLaborBillingValue(bdCalculatedLaborDetailPrice.setScale(0, RoundingMode.HALF_UP));
        		
                //'dLaborPriceSubtotal = dLaborPriceSubtotal + (m_arrQuoteItems(i).LaborBillingValue * m_arrQuoteItems(i).ItemQuantity)
        		bdLaborPriceSubtotal = bdLaborPriceSubtotal.add(m_arrLines.get(i).getbdExtendedLaborBillingValue());
        	}else{
        		m_arrLines.get(i).setbdExtendedLaborBillingValue(BigDecimal.ZERO);
        	}
        }

    	if (bDebugMode){
    		System.out.println("[1579268742] In " + this.toString() + ".calculateValues - up to 04");
    	}
    	//Next subtract the total labor billing value from the total contract amount to get the total material billing value:
        //First, determine how much billing value there is left in the contract for the material,
        //after the labor billing values have been subtracted:
        bdTotalBillingValueWithoutLabor = this.getbdTotalBillingAmount().subtract(bdLaborPriceSubtotal);
        //System.out.println("[202004105455] - bdLaborPriceSubtotal = " + bdLaborPriceSubtotal);
        //System.out.println("[202004105424] - bdTotalBillingValueWithoutLabor = " + bdTotalBillingValueWithoutLabor);

        //Assign the material billing values
        bdMaterialPriceSubtotal = BigDecimal.ZERO;
        
        //Get a proportion which represents the material cost divided by the total material billing value:
        //'If dTotalBillingValueWithoutLabor <= 0 Then
        if (bdTotalBillingValueWithoutLabor.compareTo(BigDecimal.ZERO) <= 0){
    		super.addErrorMessage(
    			"The labor billing value is more than or equal to the contract amount - "
    				+ "cannot calculate material billing values.");
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547080446]");
    		throw new Exception(this.getErrorMessages());
        }
        
    	if (bDebugMode){
    		System.out.println("[1579268750] In " + this.toString() + ".calculateValues - up to 05");
    	}
        bdMaterialCostProportion = m_dTotalExtendedMaterialCost.divide(bdTotalBillingValueWithoutLabor, 8, RoundingMode.HALF_UP);
        //System.out.println("[202004105329] - bdMaterialCostProportion = " + bdMaterialCostProportion);
        //System.out.println("[1380920550] bdMaterialCostProportion = " + bdMaterialCostProportion);
        
        //Don't even bother if there is no material
        //'If m_dTotalMaterialCost > 0 Then
        if (m_dTotalExtendedMaterialCost.compareTo(BigDecimal.ZERO) > 0){
        //First assign all of the rows but the last material row with rounded
        //proportionate values
            //'For i = 0 To iLastMaterialRow - 1
        	for (int i = 0; i < iLastMaterialRow; i++){
                //'If m_arrQuoteItems(i).ItemQuantity > 0 Then
        		if (m_arrLines.get(i).getbdQuantity().compareTo(BigDecimal.ZERO) > 0){
                    //'If m_arrQuoteItems(i).EstimatedMaterialCost > 0 Then
        			if (m_arrLines.get(i).getbdEstimatedExtendedMaterialCost().compareTo(BigDecimal.ZERO) > 0){
                        //Calculate the unit material price by multiplying the
                        //'dMaterialCostProportion' times the 'dTotalBillingValueWithoutLabor'.
                        //Finally truncate to an integer
                        //'m_arrQuoteItems(i).MaterialBillingValue = CDbl(Int(m_arrQuoteItems(i).EstimatedMaterialCost / dMaterialCostProportion))
        				BigDecimal bdExtendedMaterialBillingPerLine = 
            					m_arrLines.get(i).getbdEstimatedExtendedMaterialCost().divide(
                						bdMaterialCostProportion, 
                						SMDirectEntryLine.sextendedmaterialbillingvalueScale, 
                						RoundingMode.HALF_UP);
        				//Round this to an even dollar:
        				
        				m_arrLines.get(i).setbdExtendedMaterialBillingValue(bdExtendedMaterialBillingPerLine.setScale(0, RoundingMode.HALF_UP));
        		       // System.out.println("[1380920551] m_arrLines.get(i).getbdEstimatedMaterialBillingValue() = " + m_arrLines.get(i).getbdEstimatedMaterialBillingValue());
                        bdMaterialPriceSubtotal = bdMaterialPriceSubtotal.add(m_arrLines.get(i).getbdEstimatedMaterialBillingValue());
        		        //System.out.println("[1380920552] bdMaterialPriceSubtotal = " + bdMaterialPriceSubtotal);
        			}
        		}else{
        			m_arrLines.get(i).setbdExtendedMaterialBillingValue(BigDecimal.ZERO);
        		}
        	}

            //To calculate the material price, first determine the amount of
            //the contract remaining by subtracting LaborPriceSubtotal and the
            //dMaterialPriceSubtotal from the total contract amount
            bdContractAmountRemaining = getbdTotalBillingAmount().subtract(
            	bdLaborPriceSubtotal.add(bdMaterialPriceSubtotal));
            
            //System.out.println("[202004104859] - bdContractAmountRemaining = " + bdContractAmountRemaining + ".");
            
            //Next, divide that remaining amount by the quantity on the
            //'LastMaterialRow' to get the 'LastMaterialRow' billing value
            m_arrLines.get(iLastMaterialRow).setbdExtendedMaterialBillingValue(bdContractAmountRemaining);
        }
        //Now set the calculated gross profit on each line:
        for (int i = 0; i < m_arrLines.size(); i++){
        	if (m_arrLines.get(i).getbdQuantity().compareTo(BigDecimal.ZERO) == 0){
        		m_arrLines.get(i).setM_sgrossprofit("0.00");
        	}else{
        		m_arrLines.get(i).setM_sgrossprofit(
        			clsManageBigDecimals.BigDecimalToScaledFormattedString(SMDirectEntryLine.sgrossprofitScale, 
        			(m_arrLines.get(i).getbdEstimatedMaterialBillingValue().add(m_arrLines.get(i).getbdExtendedLaborBillingValue()))
        			.subtract(m_arrLines.get(i).getbdEstimatedExtendedMaterialCost().add(m_arrLines.get(i).getbdEstimatedExtendedLaborCost()))
        			)
        		);
        	}
        }
    	if (bDebugMode){
    		System.out.println("[1579268756] In " + this.toString() + ".calculateValues - up to 06");
    	}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080447]");
    }
    private void setUnitLaborMarkup(
    		Connection conn
    		) throws SQLException {
    	
    	String SQL = "SELECT"
    		+ " " + SMTablelabortypes.dMarkupAmount
    		+ " FROM " + SMTablelabortypes.TableName
    		+ " WHERE ("
    			+ "(" + SMTablelabortypes.sID + " = " + this.getM_slabortype() + ")"
    		+ ")"
    		;
    		
    	if (bDebugMode){
    		System.out.println("[1579268759] In " + this.toString() + " .setUnitLaborMarkup - SQL = " + SQL);
    	}
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn); 
			if (rs.next()){
				this.setM_sunitlabormarkup(Double.toString(rs.getDouble(SMTablelabortypes.dMarkupAmount)));
		    	if (bDebugMode){
		    		System.out.println("[1579268763] In " + this.toString() + " .setUnitLaborMarkup, unit labor markup = " + m_sunitlabormarkup);
		    	}
			}else{
		    	if (bDebugMode){
		    		System.out.println("[1579268766] In " + this.toString() + " .setUnitLaborMarkup, Could not read mark up for labor type ID: " + this.getM_slabortype());
		    	}
				throw new SQLException("Could not read mark up for labor type ID: " + this.getM_slabortype());
			}
			rs.close();
		} catch (SQLException e) {
	    	if (bDebugMode){
	    		System.out.println("[1579268770] In " + this.toString() + " .setUnitLaborMarkup, SQLException: " + e.getMessage());
	    	}
			throw new SQLException("Error reading labor unit markup = " + e.getMessage());
		}
    }
    public void addNewLine(){
    	m_arrLines.add(new SMSummaryIncorporationLine());
    	m_arrLines.get(m_arrLines.size() - 1).setM_llinenumber(Integer.toString(m_arrLines.size()));
    }
    public void createItemsAndAddToOrder(String sDBID, 
    		ServletContext context,  
    		String sUserName,
    		String sUserID, 
    		String sUserFullName) throws Exception{
    	
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBID, 
    			"MySQL", 
    			SMUtilities.getFullClassName(this.toString()) + ".createItemsAndAddToOrder - user: " + sUserID + " - " + sUserFullName);
    	if (conn == null){
    		throw new Exception("Could not get data connection.");
    	}
 
    	//If there is a labor item, get that now:
    	String SQL = "SELECT"
    		+ " * FROM " + SMTablelabortypes.TableName
    		+ " WHERE ("
    			+ "(" + SMTablelabortypes.sID + " = " + getM_slabortype() + ")"
    		+ ")"
    	;
    	String sLaborItem = "";
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				sLaborItem = rs.getString(SMTablelabortypes.sItemNumber);
			}else{
				clsDatabaseFunctions.freeConnection(context, conn, "[1547080448]");
				throw new Exception("Could not read labor type information for '" + getM_slabortype() + "'.");
			}
			rs.close();
		} catch (Exception e1) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080449]");
			throw new Exception("Error reading labor type information - " + e1.getMessage() + ".");
		}
    	ICItem laboritem = new ICItem(sLaborItem);
    	if (!laboritem.load(conn)){
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547080450]");
    		throw new Exception("Cannot read labor item '" + sLaborItem + "' - " + laboritem.getErrorMessageString());
    	}
    	if (!clsDatabaseFunctions.start_data_transaction(conn)){
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547080451]");
    		throw new Exception("Could not start data transaction.");
    	}
    	SMOrderHeader order = new SMOrderHeader();
    	order.setM_strimmedordernumber(getM_strimmedordernumber().trim());
    	if (!order.load(conn)){
    		clsDatabaseFunctions.rollback_data_transaction(conn);
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547080452]");
    		throw new Exception("Could not load order - " + order.getErrorMessages());
    	}
    	ArrayList <SMOrderDetail> arrOrderDetails = new ArrayList<SMOrderDetail>(0);
    	//System.out.println("[1363286132] - creating items:" + read_out_debug_data());
    	//Create the items:
    	
    	try {
			for (int i = 0; i < m_arrLines.size(); i++){
				BigDecimal bdQty = new BigDecimal(m_arrLines.get(i).getM_squantity().replace(",", ""));
				if (bdQty.compareTo(BigDecimal.ZERO) > 0){
					//If we are NOT using an existing non-stock item, build a new item now:
					BigDecimal bdExtendedCost = new BigDecimal(m_arrLines.get(i).getM_sestimatedextendedmaterialcost().replace(",", ""));
					BigDecimal bdUnitCost = bdExtendedCost.divide(bdQty, 2, RoundingMode.HALF_UP);
					ICItem item;
					if (getM_snonstockmaterialitem().compareToIgnoreCase("") == 0){
				    	item = new ICItem("");
				    	String sNewItemNumber = item.getNextDedicatedItemNumberForOrder(conn, getM_strimmedordernumber().trim());
				    	m_arrLines.get(i).setM_sitemnumber(sNewItemNumber);
				    	item.setItemNumber(sNewItemNumber);
						item.setActive("1");
						item.setCategoryCode(m_arrLines.get(i).getM_scategorycode());
						item.setCostUnitOfMeasure(m_arrLines.get(i).getM_sunitofmeasure());
						item.setDedicatedToOrderNumber(getM_strimmedordernumber().trim());
						item.setDefaultPriceListCode(order.getM_sDefaultPriceListCode());
						item.setHideOnInvoiceDefault("0");
						item.setItemDescription(m_arrLines.get(i).getM_sitemdescription());
						item.setLaborItem("0");
						item.setLastEditUserFullName(sUserFullName);
						item.setMostRecentCost(clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableicitems.bdmostrecentcostScale, bdUnitCost));
						item.setNewRecord("1");
						item.setNonStockItem("0");
						item.setNumberOfLabels(m_arrLines.get(i).getM_squantity().replace(",", ""));
						item.setSuppressItemQtyLookup("0");
						item.setTaxable("1");
						if (!item.save(sUserFullName, sUserID, conn)){
				    		clsDatabaseFunctions.rollback_data_transaction(conn);
				    		clsDatabaseFunctions.freeConnection(context, conn, "[1547080453]");
							throw new SQLException("Error adding item '" + sNewItemNumber + "': " + item.getErrorMessageString());
						}
					}else{
						item = new ICItem(getM_snonstockmaterialitem());
						if (!item.load(conn)){
							throw new Exception("Error loading non stock item '" + getM_snonstockmaterialitem() + "' - " + item.getErrorMessageString() + ".");
						}
					}
					//Now build the order detail:
			    	SMOrderDetail detail = new SMOrderDetail();
			    	detail.setM_bdEstimatedUnitCost(clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableorderdetails.bdEstimatedUnitCostScale, bdUnitCost).replace(",", ""));
			    	detail.setM_datDetailExpectedShipDate("00/00/0000");
			    	detail.setM_datLineBookedDate(clsDateAndTimeConversions.now("MM/dd/yyyy"));
			    	detail.setM_dExtendedOrderCost("0.00");
			    	detail.setM_dExtendedOrderPrice(m_arrLines.get(i).getM_sextendedmaterialbillingvalue().replace(",", ""));
			    	//detail.setM_dLineTaxAmount("0.00");
			    	detail.setM_dOrderUnitCost("0.00");
			    	BigDecimal bdUnitPrice = new BigDecimal ("0.00");
			    	BigDecimal bdExtendedPrice = new BigDecimal(m_arrLines.get(i).getM_sextendedmaterialbillingvalue().replace(",", ""));
			    	bdUnitPrice = bdExtendedPrice.divide(bdQty, SMTableorderdetails.dOrderUnitPriceScale, RoundingMode.HALF_UP);
			    	//System.out.println("[1363286129] - bdExtendedPrice = " + bdExtendedPrice);
			    	//System.out.println("[1363286129] - bdUnitPrice = " + bdUnitPrice);
			    	detail.setM_dOrderUnitPrice(clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableorderdetails.dOrderUnitPriceScale, bdUnitPrice));
			    	detail.setM_dOriginalQty(m_arrLines.get(i).getM_squantity().replace(",", ""));
			    	detail.setM_dQtyOrdered(m_arrLines.get(i).getM_squantity().replace(",", ""));
			    	detail.setM_dQtyShipped(clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableorderdetails.dQtyShippedScale, BigDecimal.ZERO));
			    	detail.setM_dQtyShippedToDate(clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableorderdetails.dQtyShippedScale, BigDecimal.ZERO));
			    	detail.setM_iIsStockItem("1");
			    	detail.setM_iprintondeliveryticket("0");
			    	detail.setM_isuppressdetailoninvoice("0");
			    	detail.setM_iTaxable(item.getTaxable());
			    	detail.setM_sItemCategory(m_arrLines.get(i).getM_scategorycode());
			    	detail.setM_sItemDesc(m_arrLines.get(i).getM_sitemdescription());
			    	detail.setM_sItemNumber(item.getItemNumber());
			    	detail.setM_sLabel("");
			    	detail.setM_sLocationCode(getM_slocation());
			    	detail.setM_sMechFullName("");
			    	detail.setM_sMechInitial("");
			    	detail.setM_sMechID("0");
			    	detail.setM_sOrderUnitOfMeasure(item.getCostUnitOfMeasure());
			    	detail.setM_strimmedordernumber(getM_strimmedordernumber().trim());
			    	arrOrderDetails.add(detail);

			    	//Now add the labor item for this line, if there is labor in it:
			    	if (m_arrLines.get(i).getbdExtendedLaborBillingValue().compareTo(BigDecimal.ZERO) > 0){
				    	SMOrderDetail labordetail = new SMOrderDetail();
				    	BigDecimal bdExtendedLaborCost = m_arrLines.get(i).getbdEstimatedExtendedLaborCost();
				    	labordetail.setM_bdEstimatedUnitCost(clsManageBigDecimals.BigDecimalToScaledFormattedString(
				    			SMTableorderdetails.bdEstimatedUnitCostScale, bdExtendedLaborCost.divide(
				    		bdQty, SMTableorderdetails.bdEstimatedUnitCostScale, RoundingMode.HALF_UP)));
				    	labordetail.setM_datDetailExpectedShipDate("00/00/0000");
				    	labordetail.setM_datLineBookedDate(clsDateAndTimeConversions.now("MM/dd/yyyy"));
				    	labordetail.setM_dExtendedOrderCost("0.00");
				    	labordetail.setM_dExtendedOrderPrice(m_arrLines.get(i).getM_sextendedlaborbillingvalue().replace(",", ""));
				    	labordetail.setM_dOrderUnitCost("0.00");
				    	BigDecimal bdLaborUnitPrice = new BigDecimal ("0.00");
				    	BigDecimal bdLaborExtendedPrice = new BigDecimal(m_arrLines.get(i).getM_sextendedlaborbillingvalue().replace(",", ""));
				    	bdLaborUnitPrice = bdLaborExtendedPrice.divide(bdQty, SMTableorderdetails.dOrderUnitPriceScale, RoundingMode.HALF_UP);
				    	labordetail.setM_dOrderUnitPrice(clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableorderdetails.dOrderUnitPriceScale, bdLaborUnitPrice));
				    	labordetail.setM_dOriginalQty(m_arrLines.get(i).getM_squantity().replace(",", ""));
				    	labordetail.setM_dQtyOrdered(m_arrLines.get(i).getM_squantity().replace(",", ""));
				    	labordetail.setM_dQtyShipped(clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableorderdetails.dQtyShippedScale, BigDecimal.ZERO));
				    	labordetail.setM_dQtyShippedToDate(clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableorderdetails.dQtyShippedScale, BigDecimal.ZERO));
				    	labordetail.setM_iIsStockItem("0");
				    	labordetail.setM_iprintondeliveryticket("0");
				    	labordetail.setM_isuppressdetailoninvoice(laboritem.getHideOnInvoiceDefault());
				    	labordetail.setM_iTaxable(laboritem.getTaxable());
				    	labordetail.setM_sItemCategory(m_arrLines.get(i).getM_scategorycode());
				    	labordetail.setM_sItemDesc(laboritem.getItemDescription());
				    	labordetail.setM_sItemNumber(laboritem.getItemNumber());
				    	labordetail.setM_sLabel("");
				    	labordetail.setM_sLocationCode(getM_slocation());
				    	labordetail.setM_sMechFullName("");
				    	labordetail.setM_sMechInitial("");
				    	labordetail.setM_sMechID("0");
				    	labordetail.setM_sOrderUnitOfMeasure(laboritem.getCostUnitOfMeasure());
				    	labordetail.setM_strimmedordernumber(getM_strimmedordernumber().trim());
				    	arrOrderDetails.add(labordetail);
			    	}
				}
			}
		} catch (Exception e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080454]");
			throw new Exception(e1.getMessage());
		}
    	if (bDebugMode){
    		System.out.println("[1363285364] listing order details from direct entry:");
    		for (int i = 0; i < arrOrderDetails.size(); i++){
    			System.out.println("order detail from direct entry: " + arrOrderDetails.get(i).getM_sItemDesc());
    		}
    	}
    	try {
			order.addMultipleDetails(arrOrderDetails, conn, sUserID, sUserFullName);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080455]");
			throw new Exception(e.getMessage());
		}
    	
    	if (!clsDatabaseFunctions.commit_data_transaction(conn)){
    		clsDatabaseFunctions.rollback_data_transaction(conn);
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547080456]");
    		throw new Exception("Could not commit data transaction.");
    	}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080457]");
    	return;
    }

    public void addErrorMessage(String sMsg){
    	super.addErrorMessage(sMsg);
    }
    public String getM_strimmedordernumber() {
		return m_strimmedordernumber;
	}

	public void setM_strimmedordernumber(String mOrdernumber) {
		m_strimmedordernumber = mOrdernumber;
	}

    public String getM_sdescription() {
		return m_sdescription;
	}

	public void setM_sdescription(String sDescription) {
		m_sdescription = sDescription;
	}
	
	public String getM_stotalbillingamount() {
		return m_stotalbillingamount;
	}

	public void setM_stotalbillingamount(String mStotalbillingamount) {
		m_stotalbillingamount = mStotalbillingamount;
	}
	public BigDecimal getbdTotalBillingAmount() throws NumberFormatException{
		BigDecimal bdTotalBillingAmt = new BigDecimal("0.00");
		try {
			bdTotalBillingAmt = new BigDecimal(getM_stotalbillingamount().replace(",", ""));
		} catch (Exception e) {
			throw new NumberFormatException("Error [1416603769] " + e.getMessage());
		}
		return bdTotalBillingAmt;
	}
	public void setbdTotalBillingAmount (BigDecimal bdValue){
		m_stotalbillingamount = clsManageBigDecimals.BigDecimalToScaledFormattedString(scontractamountScale, bdValue);
	}

	public String getM_sunitlaborcost() {
		return m_sunitlaborcost;
	}

	public void setM_sunitlaborcost(String mSunitlaborcost) {
		m_sunitlaborcost = mSunitlaborcost;
	}
	public BigDecimal getbdUnitLaborCost() throws NumberFormatException{
		try {
			return new BigDecimal(this.getM_sunitlaborcost().replace(",", ""));
		} catch (NumberFormatException e) {
			throw e;
		}
	}
	public void setbdUnitLaborCost (BigDecimal bdValue){
		m_sunitlaborcost = clsManageBigDecimals.BigDecimalToScaledFormattedString(sunitlaborcostScale, bdValue);
	}
	
	public String getM_slabortype() {
		return m_slabortype;
	}

	public void setM_slabortype(String mSlabortype) {
		m_slabortype = mSlabortype;
	}

	public String getM_slocation() {
		return m_slocation;
	}

	public void setM_slocation(String mSlocation) {
		m_slocation = mSlocation;
	}

	public String getstaxjurisdiction() {
		return m_staxjurisdiction;
	}

	public void setstaxjurisdiction(String mStax) {
		m_staxjurisdiction = mStax;
	}

	public String getM_sitemcategory() {
		return m_sitemcategory;
	}

	public void setM_sitemcategory(String mSitemcategory) {
		m_sitemcategory = mSitemcategory;
	}
	
	public String getM_ssummaryid() {
		return m_ssummaryid;
	}
	
	public String getM_sunitlabormarkup() {
		return m_sunitlabormarkup;
	}

	public void setM_sunitlabormarkup(String mSunitlabormarkup) {
		m_sunitlabormarkup = mSunitlabormarkup;
	}
	public BigDecimal getbdUnitLaborMarkup(){
		try {
			return new BigDecimal(this.getM_sunitlabormarkup().replace(",", ""));
		} catch (NumberFormatException e) {
			throw e;
		}
	}
	public String getM_snonstockmaterialitem(){
		return m_snonstockmaterialitem;
	}
	public void setM_snonstockmaterialitem(String sNonStockMaterialItem){
		m_snonstockmaterialitem = sNonStockMaterialItem;
	}
	public SMSummaryIncorporationLine getLine(int iLineNumber){
		try {
			return m_arrLines.get(iLineNumber);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	public int getNumberOfLines(){
		return m_arrLines.size();
	}
	
	public void loadSummaryValues(String sSummaryID, Connection conn) throws Exception{
		//First load the summary:
		SMEstimateSummary summary = new SMEstimateSummary();
		summary.setslid(sSummaryID);
		try {
			summary.load(conn);
		} catch (Exception e) {
			throw new Exception("Error [202007011803] - could not load summary - " + e.getMessage());
		}
		
		m_arrLines.clear();
		//Now load all the summary lines into this entry:
		for (int iEstimateCounter = 0; iEstimateCounter < summary.getEstimateArray().size(); iEstimateCounter++ ) {
			
			SMEstimate estimate = summary.getEstimateArray().get(iEstimateCounter);
			
			//First, if there's a 'prefix label' to be added, add that now:
			if (estimate.getsprefixlabelitem().compareToIgnoreCase("") != 0) {
				SMSummaryIncorporationLine line = new SMSummaryIncorporationLine();
				line.setM_sestimateid(estimate.getslid());
				line.set_bdExtendedLaborUnits(BigDecimal.ZERO);
				line.set_bdQuantity(BigDecimal.ONE);
				line.setbdEstimateExtendedLaborCost(BigDecimal.ZERO);
				line.setbdEstimateExtendedMaterialCost(BigDecimal.ZERO);
				line.setbdExtendedLaborBillingValue(BigDecimal.ZERO);
				line.setbdExtendedMaterialBillingValue(BigDecimal.ZERO);
				line.setM_llinenumber(Integer.toString(m_arrLines.size() + 1));
				line.setM_scategorycode("");
				line.setM_sitemdescription(estimate.getsdescription());
				line.setM_sitemnumber(estimate.getsprefixlabelitem());
				line.setM_sunitofmeasure("EA");
				line.setM_slinetype("Prefix Label");
				m_arrLines.add(line);
			}
			
			boolean bShowLaborBillingValue = true;
			if (estimate.getsbdlaborsellpriceperunit().compareToIgnoreCase("0.00") == 0) {
				bShowLaborBillingValue = false;
			}
			
			//Add a material line for the product:
			SMSummaryIncorporationLine line = new SMSummaryIncorporationLine();
			line.setM_sestimateid(estimate.getslid());
			line.setbdEstimateExtendedMaterialCost(new BigDecimal(estimate.getsbdextendedcost().replace(",", "")));
			BigDecimal bdLaborSellPricePerUnit = new BigDecimal(estimate.getsbdlaborsellpriceperunit().replace(",", ""));
			BigDecimal bdLaborQuantity = new BigDecimal(estimate.getsbdlaborquantity().replace(",", ""));
			BigDecimal bdTotalLaborBillingValue = bdLaborQuantity.multiply(bdLaborSellPricePerUnit);
			BigDecimal bdTotalMaterialBillingValue = estimate.getTotalPrice(conn).subtract(bdTotalLaborBillingValue);
			line.setbdExtendedMaterialBillingValue(bdTotalMaterialBillingValue);
			line.setM_llinenumber(Integer.toString(m_arrLines.size() + 1));
			line.setM_scategorycode("");
			line.setM_sitemdescription(estimate.getsproductdescription());
			line.setM_sitemnumber(estimate.getsitemnumber());
			line.setM_squantity(estimate.getsbdquantity());
			line.setM_sunitofmeasure(estimate.getsunitofmeasure());
			if (bShowLaborBillingValue) {
				line.set_bdExtendedLaborUnits(bdLaborQuantity);
				line.setbdEstimateExtendedLaborCost(new BigDecimal(estimate.getsbdlaborcostperunit()).multiply(bdLaborQuantity));
			}
			line.setbdExtendedLaborBillingValue(BigDecimal.ZERO);
			line.setM_slinetype("Product");
			//System.out.println("[202007033922] - iEstimateCounter = " + iEstimateCounter + ", line dump: " + line.read_out_debug_data());
			m_arrLines.add(line);
			
			//Add a labor line, if called for:
			if (bShowLaborBillingValue) {
				//Then add a line for labor:
				line = new SMSummaryIncorporationLine();
				line.setM_sestimateid(estimate.getslid());
				bdLaborQuantity = new BigDecimal(estimate.getsbdlaborquantity().replace(",", ""));
				line.set_bdExtendedLaborUnits(bdLaborQuantity);
				line.setbdEstimateExtendedLaborCost(new BigDecimal(estimate.getsbdlaborcostperunit()).multiply(bdLaborQuantity));
				line.setbdEstimateExtendedMaterialCost(new BigDecimal(estimate.getsbdlaborcostperunit()).multiply(bdLaborQuantity));
				bdLaborSellPricePerUnit = new BigDecimal(estimate.getsbdlaborsellpriceperunit().replace(",", ""));
				bdTotalLaborBillingValue = bdLaborQuantity.multiply(bdLaborSellPricePerUnit);
				line.setbdExtendedLaborBillingValue(BigDecimal.ZERO);
				line.setbdExtendedMaterialBillingValue(bdTotalLaborBillingValue);
				line.setM_llinenumber(Integer.toString(m_arrLines.size() + 1));
				line.setM_scategorycode("");
				line.setM_squantity(estimate.getsbdquantity());
				//Now get the labor item number from the labor type:
				line.setM_sitemnumber(summary.getLaborItemFromLaborType(conn));
				ICItem item = new ICItem(line.getM_sitemnumber());
				if (!item.load(conn)){
					throw new Exception("Error [202007085454] - could not load labor item '" + line.getM_sitemnumber() + "' - " + item.getErrorMessageString());
				}
				line.setM_sitemdescription(item.getItemDescription());
				line.setM_sunitofmeasure(item.getCostUnitOfMeasure());
				line.setM_slinetype("Labor");
				m_arrLines.add(line);
			}
			//Now add the estimate options If they are flagged as 'include on order':
			for (int iEstimateOptioncounter = 0; iEstimateOptioncounter < estimate.getLineArray().size(); iEstimateOptioncounter++) {
				SMEstimateLine option = estimate.getLineArray().get(iEstimateOptioncounter);
				if (option.getsiincludeonorder().compareToIgnoreCase("1") == 0) {
					line = new SMSummaryIncorporationLine();
					line.setM_sestimateid(estimate.getslid());
					line.set_bdExtendedLaborUnits(BigDecimal.ZERO);
					line.set_bdQuantity(new BigDecimal(option.getsbdquantity().replace(",", "")));
					line.setbdEstimateExtendedLaborCost(BigDecimal.ZERO);
					line.setbdEstimateExtendedMaterialCost(new BigDecimal(option.getsbdextendedcost().replace(",", "")));
					line.setbdExtendedLaborBillingValue(BigDecimal.ZERO);
					line.setbdExtendedMaterialBillingValue(BigDecimal.ZERO);
					line.setM_llinenumber(Integer.toString(m_arrLines.size() + 1));
					line.setM_scategorycode("");
					line.setM_sitemdescription(option.getslinedescription());
					line.setM_sitemnumber(option.getsitemnumber());
					line.setM_sunitofmeasure(option.getsunitofmeasure());
					line.setM_slinetype("Option");
					m_arrLines.add(line);
				}
			}
		}
	}
	
	private void initSummaryIncorporationVariables(){
		m_strimmedordernumber = "";
		m_stotalbillingamount = "0.00";
		m_sunitlaborcost = "0.00";
		m_slabortype = "";
		m_slocation = "";
		m_staxjurisdiction = "";
		m_sitemcategory = "";
		m_arrLines = new ArrayList<SMSummaryIncorporationLine> (0);
		for (int i = 0; i < NUMBER_OF_INITIAL_LINES; i++){
			m_arrLines.add(new SMSummaryIncorporationLine());
			m_arrLines.get(m_arrLines.size() - 1).setM_llinenumber(Integer.toString(m_arrLines.size()));
		}
		m_sunitlabormarkup = "";
		m_sdescription = "";
		m_snonstockmaterialitem = "";
		m_ssummaryid = "0";
		super.initVariables();
		super.setObjectName(ParamObjectName);
    }
    public String read_out_debug_data(){
    	String sResult = "\n  ** Direct Entry read out: ";
    	sResult += "\nOrder number: " + this.getM_strimmedordernumber();
    	sResult += "\nSummary ID: " + this.getM_ssummaryid();
    	sResult += "\nTotal billing amount: " + this.getM_stotalbillingamount();
    	sResult += "\nUnit labor cost: " + this.getM_sunitlaborcost();
    	sResult += "\nLabor type: " + this.getM_slabortype();
    	sResult += "\nLocation: " + this.getM_slocation();
    	sResult += "\nTax: " + this.getstaxjurisdiction();
    	sResult += "\nItem category: " + this.getM_sitemcategory();
    	sResult += "\nUnit labor markup: " + this.getM_sunitlabormarkup();
    	sResult += "\nDescription: " + this.getM_sdescription();
    	sResult += "\nNon-stock material item: " + this.getM_snonstockmaterialitem();
    	for (int i = 0; i < m_arrLines.size(); i++){
    		sResult += m_arrLines.get(i).read_out_debug_data();
    	}
    	return sResult;
    }
}
package ACCPACDataDefinition;

public class ICITEM {

	public static String TableName = ACCPACTables.IC_ITEMS;
	
	//Field names:
    public static String sItemNumber = "ITEMNO";            //CHAR         24     Unformatted Item Number
    public static String lAlternateItemSet = "ALTSET";             //LONG         4      Alternate Item Set Number
    public static String sItemDesc = "DESC";               //CHAR         40     Description
    public static String adDateLastMaintained = "DATELASTMN";         //DATE         5      Date Last Maintained
    public static String bInactive = "INACTIVE";           //BOOL         2      Status
    public static String sItemStructureCode = "ITEMBRKID";          //CHAR         6      Structure Code
    public static String sFormattedItemNumber = "FMTITEMNO";          //CHAR         24     Item Number
    public static String sCategory = "CATEGORY";           //CHAR         6      Category
    public static String sAccountSetCode = "CNTLACCT";           //CHAR         6      Account Set Code
    public static String bStockitem = "STOCKITEM";          //BOOL         2      Stock Item
    public static String sStockUnitMeasure = "STOCKUNIT";          //CHAR         10     Stocking Unit of Measure
    public static String sDefaultPriceListCode = "DEFPRICLST";         //CHAR         6      Default Price List Code
    public static String dUnitWeight = "UNITWGT";            //BCD          10     Unit Weight
    public static String sPickingSequence = "PICKINGSEQ";         //CHAR         10     Default Picking Sequence
    public static String bSerialNumbers = "SERIALNO";           //BOOL         2      Serial Numbers
    public static String sCommodityNumber = "COMMODIM";           //CHAR         16     Commodity Number
    public static String adDateInactive = "DATEINACTV";         //DATE         5     Date Inactive
    public static String sSegment1 = "SEGMENT1";           //CHAR         24     Segment 1
    public static String sSegment2 = "SEGMENT2";           //CHAR         24     Segment 2
    public static String sSegment3 = "SEGMENT3";           //CHAR         24     Segment 3
    public static String sSegment4 = "SEGMENT4";           //CHAR         24     Segment 4
    public static String sSegment5 = "SEGMENT5";           //CHAR         24     Segment 5
    public static String sSegment6 = "SEGMENT6";           //CHAR         24     Segment 6
    public static String sSegment7 = "SEGMENT7";           //CHAR         24     Segment 7
    public static String sSegment8 = "SEGMENT8";           //CHAR         24     Segment 8
    public static String sSegment9 = "SEGMENT9";           //CHAR         24     Segment 9
    public static String sSegment10 = "SEGMENT10";          //CHAR         24     Segment 10
    public static String sComment1 = "COMMENT1";           //CHAR         75     Comment 1
    public static String sComment2 = "COMMENT2";           //CHAR         75     Comment 2
    public static String sComment3 = "COMMENT3";           //CHAR         75     Comment 3
    public static String sComment4 = "COMMENT4";           //CHAR         75     Comment 4
    
    //Added in ACCPAC Version 5.4:
    public static String iSellable = "SELLABLE";
	
	//Field Sizes:
    public static int sItemNumberLength = 24;
    public static int lAlternateItemSetLength = 4;
    public static int sItemDescLength = 60;
    public static int sItemStructureCodeLength = 6;
    public static int sFormattedItemNumberLength = 24;
    public static int sCategoryLength = 6;
    public static int sAccountSetCodeLength = 6;
    public static int sStockUnitMeasureLength = 10;
    public static int sDefaultPriceListCodeLength = 6;
    public static int sPickingSequenceLength = 10;
    public static int sCommodityNumberLength = 16;
    public static int sSegment1Length = 24;
    public static int sSegment2Length = 24;
    public static int sSegment3Length = 24;
    public static int sSegment4Length = 24;
    public static int sSegment5Length = 24;
    public static int sSegment6Length = 24;
    public static int sSegment7Length = 24;
    public static int sSegment8Length = 24;
    public static int sSegment9Length = 24;
    public static int sSegment10Length = 24;
    public static int sComment1Length = 80;
    public static int sComment2Length = 80;
    public static int sComment3Length = 80;
    public static int sComment4Length = 80;
    
}

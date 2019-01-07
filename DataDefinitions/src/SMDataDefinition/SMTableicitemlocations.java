package SMDataDefinition;

public class SMTableicitemlocations {
	public static String TableName = "icitemlocations";
	
	//Field names:
    public static String sLocation = "slocation";
    public static String sItemNumber = "sitemnumber";
    public static String sQtyOnHand = "bdqtyonhand";
    public static String sTotalCost = "bdtotalcost";
    //LTO 20111028 minimum quantity on-hand
    public static String sMinQtyOnHand = "bdminqtyonhand";
    
    
    /*
     * In ACCPAC's ICILOC, QTYOH = (ICCOST.QTY - ICCOST.QTYSHIPPED) + QTYOFFEST 
     * Cost works the same way.
     * In ICCOST, there may be more than one record for an item/location combination, so we have to
     * do a SUM statement to get the correct amount
     */
    
    
    /* ACCPAC Fields:
    sICILOCItemNumber = 1                     'ITEMNO    CHAR    24        Item Number
    sICILOCLocation = 2                       'LOCATION    CHAR    6        Location
    sICILOCPickingSequence = 3                'PICKINGSEQ    CHAR    10        Picking Sequence
    bICILOCAllowed = 4                        'ACTIVE    BOOL    2        Allowed
    adICILOCDateLocationActivated = 5         'DATEACTIVE    DATE    5        Date Location Activated
    bICILOCInUse = 6                          'USED    BOOL    2        In Use
    adICILOCDateLastUsed = 7                  'LASTUSED    DATE    5        Date Last Used
    dICILOCQuantityonHand = 8                 'QTYONHAND    BCD    10    4    Quantity on Hand (Last Day End)
    dICILOCQuantityonPO = 9                   'QTYONORDER    BCD    10    4    Quantity on P/O
    dICILOCQuantityonSO = 10                  'QTYSALORDR    BCD    10    4    Quantity on S/O
    dICILOCQuantityNotinCostFile = 11         'QTYOFFSET    BCD    10    4    Quantity Not in Cost File
    dICILOCQuantityShippedNotCosted = 12      'QTYSHNOCST    BCD    10    4    Quantity Shipped Not Costed
    dICILOCQuantityReceivedNotCosted = 13     'QTYRENOCST    BCD    10    4    Quantity Received Not Costed
    dICILOCQuantityAdjustedNotCosted = 14     'QTYADNOCST    BCD    10    4    Quantity Adjusted Not Costed
    lICILOCNumberofUncostedTransactions = 15  'NUMNOCST    LONG    4        Number of Uncosted Transactions
    dICILOCTotalCost = 16                     'TOTALCOST    BCD    10    3    Total Cost
    dICILOCCostNotinCostFile = 17             'COSTOFFSET    BCD    10    3    Cost Not in Cost File
    sICILOCCostUnitofMeasure = 18             'COSTUNIT    CHAR    10        Cost Unit of Measure
    dICILOCCostUnitConversionFactor = 19      'COSTCONV    BCD    10    6    Cost Unit Conversion Factor
    dICILOCStandardCost = 20                  'STDCOST    BCD    10    6    Standard Cost
    dICILOCLastStandardCost = 21              'LASTSTDCST    BCD    10    6    Last Standard Cost
    adICILOCLastStandardCostDate = 22         'LASTSTDDAT    DATE    5        Last Standard Cost Date
    adICILOCLastShipmentDate = 23             'LASTSHIPDT    DATE    5        Last Shipment Date
    dICILOCAverageDaysToShip = 24             'DAYSTOSHIP    BCD    10    4    Average Days To Ship
    dICILOCAverageUnitsShipped = 25           'UNITSSHIP    BCD    10    4    Average Units Shipped
    iICILOCShipmentsUsedInCalculation = 26    'SHIPMENTS    INT    2        Shipments Used In Calculation
    adICILOCLastReceiptDate = 27              'LASTRCPTDT    DATE    5        Last Receipt Date
    dICILOCMostRecentCost = 28                'RECENTCOST    BCD    10    6    Most Recent Cost
    dICILOCUserDefinedCost1 = 29              'COST1    BCD    10    6    User Defined Cost 1
    dICILOCUserDefinedCost2 = 30              'COST2    BCD    10    6    User Defined Cost 2
    dICILOCLastUnitCost = 31                  'LASTCOST    BCD    10    6    Last Unit Cost
    */
	//Field Lengths:
    public static int sLocationLength = 6;
    public static int sItemNumberLength = 24;
}

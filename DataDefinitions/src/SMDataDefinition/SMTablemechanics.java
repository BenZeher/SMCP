package SMDataDefinition;

public class SMTablemechanics {

	public static String TableName = "mechanics";
	
	//Field names:
    public static final String sMechInitial = "sMechInitial";
    public static final String sMechFullName = "sMechFullName";
    public static final String iMechType = "iMechType";
    public static final String sMechLocation = "sMechLocation";
    public static final String sAssistant = "sAssistant";
    public static final String sVehicleLabel = "sVehicleLabel";
    public static final String sstartingtime = "sstartingtime";
    public static final String sMechColorCodeRow = "smechcolorrow";
    public static final String sMechColorCodeCol = "smechcolorcol";
    public static final String lid = "lid";
    public static final String semployeeid = "semployeeid";

    //Field lengths:
	public static final int sMechInitialLength = 4;
	public static final int sMechFullNameLength = 50;
	public static final int sMechLocationLength = 6;
	public static final int sAssistantLength = 60;
	public static final int sVehicleLabelLength = 10;
	public static final int sstartingtimeLength = 10;
	public static final int semployeeidLength = 32;
	
	/*
	Public Function Check_Mechanic_Type(lMT As Long, iServiceTypeID As Integer) As Boolean
	'given the sum of service types this mechanic is capable of and the particular type we wanna find out.
	    Dim i As Integer
	    
	    Check_Mechanic_Type = False
	    
	    For i = 24 To iServiceTypeID + 1 Step -1
	    
	        If lMT >= 2 ^ i Then
	            lMT = lMT - 2 ^ i
	        End If
	    
	    Next i
	    
	    If lMT >= 2 ^ iServiceTypeID Then
	        Check_Mechanic_Type = True
	    End If
	    
	    Exit Function
	    
	CLEAR_ALL:

	End Function
	*/
	
	/*
	 *         'if there is any service type selected, then put in service type as criteria
        If (tServiceTypes(0).sCode <> "SH9999") Then
        
            'construct all possible combination of service type
            Dim iTypes() As Integer
            
            For i = 0 To UBound(tServiceTypes)
            
                Increase_Array iTypes
                iTypes(UBound(iTypes)) = 2 ^ tServiceTypes(i).iTypeID
                
                'fill in all the combinations of current types
                iCurrentUBound = UBound(iTypes)
                For j = 0 To iCurrentUBound - 1
                    
                   Increase_Array iTypes
                   iTypes(UBound(iTypes)) = iTypes(j) + 2 ^ tServiceTypes(i).iTypeID
                   
                Next j
            Next i
           
            SQL = SQL & " AND "
            'put in the first selected servicetype, the reason to separate the first one is because
            'i cannot put "OR" in front of the first one.
            SQL = SQL & " (" & DATA_TABLE_SM_MECHANICS & "." & Get_Mechanic_Field(iMechType) & " = '" & iTypes(0) & "'"
            If UBound(iTypes) > 0 Then
                For i = 1 To UBound(iTypes)
                    SQL = SQL & " OR " & DATA_TABLE_SM_MECHANICS & "." & Get_Mechanic_Field(iMechType) & " = '" & iTypes(i) & "'"
                Next i
            End If
            SQL = SQL & ")"
	 */
}

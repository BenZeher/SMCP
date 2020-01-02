package TimeCardSystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import TCSDataDefinition.TCSTableEmployeeTypes;
import TCSDataDefinition.TCSTableMilestones;


public class TCEditMilestonesEdit extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String MAIN_FORM_NAME = "MilestoneForm";
	private static final String CALLED_CLASS = "TimeCardSystem.TCEditMilestonesAction";
	
	public static final String SUBMIT_EDIT_BUTTON_NAME = "SUBMITEDIT";
	public static final String SUBMIT_EDIT_BUTTON_LABEL = "Update";
	public static final String SUBMIT_DELETE_BUTTON_NAME = "SUBMITDELETE";
	public static final String SUBMIT_DELETE_BUTTON_LABEL = "Delete";
	public static final String CONFIRM_DELETE_CHECKBOX_NAME = "CONFIRMDELETE";

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB);
	    String sUser = (String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID);
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    
		TCMilestones entry = new TCMilestones(request);

		//If the user chose to ADD A NEW MILESTONE, then we just initialize the entry, and go from there:
		if (clsManageRequestParameters.get_Request_Parameter(TCEditMilestonesSelection.BUTTON_ADD_NEW_NAME, request).compareToIgnoreCase(TCEditMilestonesSelection.BUTTON_ADD_NEW_LABEL) == 0){
			entry.set_slid("-1");
		}

		//If this is a 'resubmit', meaning it's being called by the 'Action' class, then
		//the session will have an entry object in it, and that's what we'll pick up.
		
	    if (CurrentSession.getAttribute(TCMilestones.ParamObjectName) != null){
	    	entry = (TCMilestones) CurrentSession.getAttribute(TCMilestones.ParamObjectName);
	    	CurrentSession.removeAttribute(TCMilestones.ParamObjectName);
	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the ID or key from the request and try to load the entry:
	    }else{
	    	//Unless we are trying to create a NEW type, load the existing type:
	    	if (entry.get_slid().compareToIgnoreCase("-1") != 0){
	    		try {
					entry.load(sDBID, getServletContext(), sUser);
				} catch (Exception e) {
					response.sendRedirect(
							"" + TimeCardUtilities.getURLLinkBase(getServletContext()) + sCallingClass
							+ "?Warning=" + e.getMessage()
						);
						return;
				}
	    	}
	    }
	    
	    String sTitle = "Edit Milestones";
	    String sSubtitle = "";
	    out.println(TimeCardUtilities.TCTitleSubBGColorWithFont(sTitle, sSubtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS, TimeCardUtilities.BASE_FONT_FAMILY));
	    out.println(TimeCardUtilities.getDatePickerIncludeString(getServletContext()));
	    
	    out.println("<BR><A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) 
	    	+ "TimeCardSystem.AdminMain\">Return to main menu</A><BR>");
	    
	    out.println("<BR><A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) 
    		+ "TimeCardSystem.TCEditMilestonesSelection\">Edit another Milestone</A><BR>");
	    
    	String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
    	if (sWarning.compareToIgnoreCase("") != 0){
    		out.println("<BR><B><FONT COLOR=RED>Error editing " + TCMilestones.ParamObjectName + " - " + sWarning + ".</FONT></B><BR>");
    	}
    	out.println("<BR>");
    	String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
    	if (sStatus.compareToIgnoreCase("") != 0){
    		out.println("<B><FONT COLOR=YELLOW>" + sStatus + "</FONT></B><BR>");
    	}

	    out.println("<FORM ID='\"" + MAIN_FORM_NAME + "'\" NAME=\"" + MAIN_FORM_NAME + "\" ACTION=\"" 
			+ TimeCardUtilities.getURLLinkBase(getServletContext()) + CALLED_CLASS + "\""
			+ " METHOD=\"POST\">"
		);
			
	    out.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" + this.getClass().getName() + "\">");

	    out.println("<TABLE BORDER=1>");

	    String sID = "NEW";
	    if (entry.get_slid().compareToIgnoreCase("-1") != 0){
	    	sID = entry.get_slid();
	    }
	    
		out.println("<TR>"
			+ "<TD ALIGN=RIGHT><B>" + "ID:" + " </B></TD>"
			+ "<TD ALIGN=LEFT>" + sID + "</TD>"
			+ "<TD ALIGN=LEFT>" 
			+ "&nbsp;" 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + TCSTableMilestones.lid + "\" VALUE=\"" + entry.get_slid() + "\">"
			+ "</TD>"
			+ "</TR>"
		);
		
	    //Name
	    out.println(TimeCardUtilities.Create_Edit_Form_Text_Input_Row(
	    	TCSTableMilestones.sName,
	    	entry.get_sName(), 
	    	TCSTableMilestones.sNameLength, 
	    	"Name:", 
	    	"Milestone Name", 
	    	"40", 
	    	"")
	    );
	    
	    //Description
	    out.println(TimeCardUtilities.Create_Edit_Form_MultilineText_Input_Row(
	    	TCSTableMilestones.sDescription,
	    	entry.get_sDescription(), 
	    	"Description:", 
	    	"Enter milestone description", 
	    	3,
	    	40,
	    	"")
	    );

	    //Employee Type ID
	    ArrayList<String> sValues = new ArrayList<String>();
	    ArrayList<String> sDescriptions = new ArrayList<String>();
	    sValues.add("0");
	    sDescriptions.add("***SELECT EMPLOYEE TYPE***");
	    String sSQL = "SELECT * FROM EmployeeTypes ORDER BY sName";
		ResultSet rs;
		try {
			rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));	
			while (rs.next()){
				sValues.add(Long.toString(rs.getLong(TCSTableEmployeeTypes.lid)));
				sDescriptions.add(Long.toString(rs.getLong(TCSTableEmployeeTypes.lid)) 
						+ " - " + rs.getString(TCSTableEmployeeTypes.sName));
			}
        rs.close();
		} catch (SQLException e) {
	        out.println ("Error getting Employee Type IDs.");
		}
	    
	    out.println(TimeCardUtilities.Create_Edit_Form_List_Row(
	    	TCSTableMilestones.sEmployeeTypeID,
	    	sValues,
	    	entry.get_sEmployeeTypeID(), 
	    	sDescriptions, 
	    	"Employee type:", 
	    	"Select the type of employee this milestone is for",
	    	"")
	    );
		out.println("</TABLE>");
	    
		out.println("<P><INPUT TYPE=SUBMIT NAME='" 
			+ SUBMIT_EDIT_BUTTON_NAME 
			+ "' VALUE='" + SUBMIT_EDIT_BUTTON_LABEL + "' STYLE='height: 0.24in'>"
		);
	    
		out.println("<INPUT TYPE=SUBMIT NAME='" 
			+ SUBMIT_DELETE_BUTTON_NAME 
			+ "' VALUE=\"" + SUBMIT_DELETE_BUTTON_LABEL + "\"" + TCMilestones.ParamObjectName 
			+ "' STYLE='height: 0.24in'>"
			+ "  Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"" 
			+ CONFIRM_DELETE_CHECKBOX_NAME + "\"></P>"
		);
			
	    return;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

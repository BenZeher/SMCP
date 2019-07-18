package ServletUtilities;

import SMDataDefinition.SMMasterStyleSheetDefinitions;

public class clsNotifications {

	public static String success(String sNotificationText){
		String s = "";
		
		return s;
	}
	
	public static String info(String sNotificationText){
		String s = "";
		
		return s;
	}
	
	public static String warning(String sNotificationText){
		String s = "";
		
		return s;
	}
	
	public static String success_center_fade(String sNotificationText){
		String s = "";
		s +="<div class=\""+ SMMasterStyleSheetDefinitions.NOTIFICATION_CENTER +"\">\n" 
				+ "<div class=\""+SMMasterStyleSheetDefinitions.NOTIFICATION_SUCCESS_CENTER+"\""
					+ " id=\"" + SMMasterStyleSheetDefinitions.SUCCESS_NOTIFICATION_TEXT_ID + "\">\n"
				+ sNotificationText + "\n"
				+ "</div>\n" 
			+ "</div>\n";
		
		s += "<script>\n"
				+ "$(function() {\n" 
				+ "    setTimeout(function() {\n" 
				+ "    $(\"." + SMMasterStyleSheetDefinitions.NOTIFICATION_SUCCESS_CENTER + "\").fadeOut(1250);\n"
				+ "    }, 3000);\n" 
				+ "});\n"
				+ ""
		+ "</script>\n";
		
		return s;
	}
	
	public static String info_center(String sNotificationText){
		String s = "";
		
		return s;
	}
	
	public static String info_center_overlay(String sNotificationText){
		String s = "";
		s += "<div class=\""+ SMMasterStyleSheetDefinitions.SCREEN_OVERLAY +"\">\n"
				+ "<div class=\""+ SMMasterStyleSheetDefinitions.NOTIFICATION_CENTER +"\">\n"
				+ "<div class=\""+SMMasterStyleSheetDefinitions.NOTIFICATION_INFO_CENTER +"\""
			    	+ " id=\""+SMMasterStyleSheetDefinitions.INFO_NOTIFICATION_TEXT_ID+"\">\n"
				+ sNotificationText + "\n"
				+ "</div>\n" 
		+ "  </div>\n"
		+ "  </div>\n";
		return s;
	}
	
	public static String warning_center(String sNotificationText){
		String s = "";
		
		return s;
	}
}

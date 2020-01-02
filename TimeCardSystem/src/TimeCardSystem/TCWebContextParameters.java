package TimeCardSystem;

import javax.servlet.ServletContext;

public class TCWebContextParameters {
		
    private static final String webappname = "webappname";
    private static final String imagepath = "imagepath";
    private static final String scriptpath = "scriptpath";
    private static final String programtitle = "programtitle";
    
	 public static String getURLLinkBase(ServletContext context){
		 String sSMAppName = context.getInitParameter(TCWebContextParameters.webappname);
		 String sURLBase = "";
		 if ((sSMAppName == null) || (sSMAppName.compareToIgnoreCase("")) == 0){
			 	sURLBase = "/servlet/";
			}else{
				sURLBase = "/" + sSMAppName + "/";
			}
			return sURLBase;
	 }

	 public static String getInitImagePath(ServletContext context){
		 String sInitParam = context.getInitParameter(TCWebContextParameters.imagepath);
		 if ((sInitParam == null) || (sInitParam.compareToIgnoreCase("")) == 0){
			 sInitParam = "images";
			}
			return sInitParam;
	 }
	 public static String getInitProgramTitle(ServletContext context){
		 String sInitParam = context.getInitParameter(TCWebContextParameters.programtitle);
		 if ((sInitParam == null) || (sInitParam.compareToIgnoreCase("")) == 0){
			 sInitParam = "SM Control Panel";
			}
			return sInitParam;
	 }
	 public static String getInitScriptPath(ServletContext context){
		 String sInitParam = context.getInitParameter(TCWebContextParameters.scriptpath);
		 if ((sInitParam == null) || (sInitParam.compareToIgnoreCase("")) == 0){
			 sInitParam = "scripts";
			}
			return sInitParam;
	 }
	 public static String getInitWebAppName(ServletContext context){
		 String sInitParam = context.getInitParameter(TCWebContextParameters.webappname);
		 if ((sInitParam == null) || (sInitParam.compareToIgnoreCase("")) == 0){
			 sInitParam = "sm";
			}
			return sInitParam;
	 }	 
}
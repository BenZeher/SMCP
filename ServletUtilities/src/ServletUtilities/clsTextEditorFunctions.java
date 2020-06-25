package ServletUtilities;

import SMDataDefinition.SMMasterStyleSheetDefinitions;

public class clsTextEditorFunctions {

	//NOTE: To add an editable text box add the getJavascriptTextEditToolBarFunctions() function to the top of the page
	public static String Create_Edit_Form_Editable_MultilineText_Input_Row_with_Style(
			String sFieldName,
			String sValue,
			String sLabel,
			String sRemark,
			int iHeight_px,
			int iWidth_px,
			String onchange,
			boolean bIncludeToolBar,
			boolean bAutoWrapText,
			String sStyle
	){
	
		String sRow = "<TR>\n";
		sRow += "<TD ALIGN=RIGHT><B>" + sLabel  + " </B></TD>\n";
	
		sRow += "<TD ALIGN=LEFT>";
		sRow += Create_Editable_Form_MultilineText_Input_Field(
				sFieldName,
				sValue,
				iHeight_px,
				iWidth_px,
				onchange,
				bIncludeToolBar,
				bAutoWrapText
				);
		sRow += "</TD>\n";
	
		sRow += "<TD ALIGN=LEFT "+sStyle+">" + sRemark + "</TD>\n";
		sRow += "</TR>\n";
		return sRow;
	
	}
	
	
	
	
	
	public static String Create_Edit_Form_Editable_MultilineText_Input_Row (
			String sFieldName,
			String sValue,
			String sLabel,
			String sRemark,
			int iHeight_px,
			int iWidth_px,
			String onchange,
			boolean bIncludeToolBar,
			boolean bAutoWrapText
	){
	
		String sRow = "<TR>\n";
		sRow += "<TD ALIGN=RIGHT><B>" + sLabel  + " </B></TD>\n";
	
		sRow += "<TD ALIGN=LEFT>";
		sRow += Create_Editable_Form_MultilineText_Input_Field(
				sFieldName,
				sValue,
				iHeight_px,
				iWidth_px,
				onchange,
				bIncludeToolBar,
				bAutoWrapText
				);
		sRow += "</TD>\n";
	
		sRow += "<TD ALIGN=LEFT>" + sRemark + "</TD>\n";
		sRow += "</TR>\n";
		return sRow;
	
	}
	
	public static String Create_Editable_Form_MultilineText_Input_Field (
			String sFieldName,
			String sValue,
			int iHeight_px,
			int iWidth_px,
			String onChange,
			boolean bIncludeShowHTMLOption,
			boolean bAutoWrapText
	){
	
		String s = "";

		s += "<TABLE>";
		s += "<TR>\n<TD>\n";
		s += clsTextEditorFunctions.createTextEditToolBar(sFieldName, bIncludeShowHTMLOption);
		s+= "</TD>\n</TR>\n";
		
		s+=	 "<TR>\n<TD>\n";
		//Create normal TEXTAREA, but do not display it
		s += "<textarea name=\"" + sFieldName + "\" id=\"" + sFieldName + "\""				
			+ "style=\"display:none; "
			+ "width:" + iWidth_px + "px; "
			+ "height:" + iHeight_px + "px; "
			+ "resize: none;\""
			+ " onchange=\"" + onChange + "\">";
		if (sValue != null){
			s +=  sValue.replace("\"", "&quot;") ;
		}
		s += "</textarea>\n";
		
		//Create the iFrame to edit text.
		s+="<iframe name=\"" + sFieldName + "iFrame\" id=\"" + sFieldName + "iFrame\""
		    + " tabindex=\"-1\""
			+ " style=\"background-color:white;"
			+ " border:" + SMMasterStyleSheetDefinitions.BACKGROUND_BLACK + " 1px solid;"
			+ " resize: both; "
			+ " min-width:" + iWidth_px + "px; "
			+ " min-height:" + iHeight_px+ "px;\""
			+ ">"
			+ "</iframe>\n";		
		s+= "</TD>\n</TR>\n";
		s+= "</TABLE>";
				
		//Create script to turn design mode on the iFrame and copy what's load the iFrame with data. 
		//Update TEXTAREA with event listener.
		String sAutoWrapText = "";
		if(bAutoWrapText) {
			sAutoWrapText = "doc" + sFieldName + ".body.style.wordWrap = 'break-word';\n";
		}
		s+="<script>\n"
			+ "var doc" + sFieldName + "= window.frames['" + sFieldName + "iFrame'].document;\n"			
			+ "doc" + sFieldName + ".designMode = 'On';\n"
			+ "doc" + sFieldName + ".open();\n"
			+ "doc" + sFieldName + ".close();\n"
			+ sAutoWrapText		
			+ "doc" + sFieldName + ".body.innerHTML = document.getElementById(\"" + sFieldName + "\").value;\n"
			
			//Update the text area any time focus comes off the frame.
			+ "doc" + sFieldName + ".body.addEventListener('blur',function(e) {\n"			
			+ "		document.getElementById(\""+ sFieldName + "\").value = doc" + sFieldName + ".body.innerHTML.replace(\"<br>\",\"<br/>\").replace(\"\\n\",\"\").replace(\"\\r\",\"\");\n"			
			+ "});\n"
			
			//Execute onChange when anything is entered into the frame
			+ "doc" + sFieldName + ".body.addEventListener('input',function() {\n"			
			+  	onChange + "\n"		
			+ "});\n"
			
			//Only paste into the frame as plain text.
			+ "doc" + sFieldName + ".body.addEventListener('paste',function(e) {\n"
			+ "e.preventDefault();\n" 
			+ "    var text = '';\n" 
			+ "    if (e.clipboardData || e.originalEvent.clipboardData) {\n" 
			+ "      text = (e.originalEvent || e).clipboardData.getData('text/plain');\n" 
			+ "    } else if (window.clipboardData) {\n" 
			+ "      text = window.clipboardData.getData('Text');\n" 
			+ "    }\n"  
			+ "    if (document.queryCommandSupported('insertText')) {\n"  
			+ "       doc" + sFieldName + ".execCommand('insertText', false, text);\n"  
			+ "    } else {\n"  
			+ "      doc" + sFieldName + ".execCommand('paste', false, text);\n" 
			+ "    }"
			+ "});\n"
			+ "</script>\n";
		return s;
	
	}
	
	public static String createTextEditToolBar(String sFieldName, boolean bShowHTMLOption) {
		String s =  createUndoButtonIcon(sFieldName) 
				+ "&nbsp;\n" + createRemoveFromattingButtonIcon(sFieldName) 
				+ "&nbsp;\n" + createBoldButtonIcon(sFieldName) 
				+ "&nbsp;\n" + createItalicButtonIcon(sFieldName)
				+ "&nbsp;\n" + createUnderlineButtonIcon(sFieldName) 
				+ "&nbsp;\n" + createFontSizeSelection(sFieldName) 
				+ "&nbsp;\n" + createFontColorSelection(sFieldName) 
				+ "&nbsp;\n" + createLinkButtonIcon(sFieldName) ;				
				if(bShowHTMLOption) {
					s += "&nbsp;\n" + createToggleHTMLButtonIcon(sFieldName);
				}	
				s += "<BR/>";
		return s;	
	}
	
	public static String getJavascriptTextEditToolBarFunctions() {
		String s =" "
			+ "<script>\n"
				
			+ "function updateTextAreaValue(sPramName){\n" 
			+ " var checkBox = document.getElementById(sPramName + 'toggleHTML');"
			+ "if (checkBox !== null){ "
			+ "	if(!document.getElementById(sPramName + 'toggleHTML').checked){\n"
			+ "    document.getElementById(sPramName).value = window.frames[sPramName + 'iFrame'].document.body.innerHTML.replace(\"<br>\",\"<br/>\");\n"
			+ "	}\n"
			+ "}else{\n"
			+ "    document.getElementById(sPramName).value = window.frames[sPramName + 'iFrame'].document.body.innerHTML.replace(\"<br>\",\"<br/>\");\n"
			+ "}\n"
			+ "}\n"
			
			+ "function iBold(sPramName){\n" 
			+ "     this[sPramName + 'iFrame'].document.execCommand('bold',false,null);\n"
			+ "	    updateTextAreaValue(sPramName);\n"  
			+ " }\n" 
			
			+ "function iUnderline(sPramName){\n" 
			+ "     this[sPramName + 'iFrame'].document.execCommand('underline',false,null);\n" 
			+ "	    updateTextAreaValue(sPramName);\n" 
			+ " }\n" 
			
			+ "function iItalic(sPramName){\n" 
			+ "     this[sPramName + 'iFrame'].document.execCommand('italic',false,null);\n" 
			+ "	    updateTextAreaValue(sPramName);\n" 
			+ " }\n"
			
			+ " function iLink(sPramName){\n" 
			+ "    var link = prompt('Enter the URL for this link:',\"http://\");\n" 
			+ "    this[sPramName + 'iFrame'].document.execCommand('CreateLink',false,link)\n" 
			+ "	   updateTextAreaValue(sPramName);\n"
			+ " }\n"
			
			+ " function iUnlink(sPramName){\n"  
			+ "    this[sPramName + 'iFrame'].document.execCommand('Unlink',false,null);\n"
			+ "	   updateTextAreaValue(sPramName);\n"
			+ " }\n" 
			
			+ " function iUndo(sPramName){\n"  
			+ "    this[sPramName + 'iFrame'].document.execCommand('Undo',false,null);\n"
			+ "	   updateTextAreaValue(sPramName);\n"
			+ " }\n" 
			
			+ " function iFontSize(sPramName, size){\n" 
			+ "    this[sPramName + 'iFrame'].document.execCommand('FontSize',false,size);\n" 
			+ "	   updateTextAreaValue(sPramName);\n"
			+ " }\n"
			
			+ " function iFontColor(sPramName, color){\n" 
			+ "    this[sPramName + 'iFrame'].document.execCommand('forecolor',false,color);\n"  
			+ "	   updateTextAreaValue(sPramName);\n"
			+ " }\n"  
			
			+ " function iRemoveFormat(sPramName){\n"  
			+ "    this[sPramName + 'iFrame'].document.execCommand('removeFormat',false,null);\n"
			+ "	   updateTextAreaValue(sPramName);\n"
			+ " }\n" 
			
			+ " function iStrikeThrough(sPramName){\n"  
			+ "    this[sPramName + 'iFrame'].document.execCommand('strikeThrough',false,null);\n"
			+ "	   updateTextAreaValue(sPramName);\n"
			+ " }\n" 
			
			+ " function iToggleHTML(sPramName, bIsChecked){\n"
			+ "    if(bIsChecked){\n"
			+ "    		document.getElementById(sPramName).value = window.frames[sPramName + 'iFrame'].document.body.innerHTML.replace(\"<br>\",\"<br/>\");\n"
			+ "    		document.getElementById(sPramName + 'iFrame').style.display = \"none\";\n"
			+ "    		document.getElementById(sPramName).style.display = \"inline\";\n"
			+ "    }else{\n"
			+"     		window.frames[sPramName + 'iFrame'].document.body.innerHTML = document.getElementById(sPramName).value;\n"
			+ "    		document.getElementById(sPramName + 'iFrame').style.display = \"inline\";\n"
			+ "    		document.getElementById(sPramName).style.display = \"none\";\n"
			+ "		}\n"
			+ "	   updateTextAreaValue(sPramName);\n"
			+ " }\n"
			
			+ "</script>"
			;
		
		return s;	
	}
	
	private static String createBoldButtonIcon(String sParamName){
		String s = "<img class=\"intLink\" title=\"Bold\" onclick=\"iBold('" + sParamName + "');\" "
				+ "src=\"data:image/gif;base64,R0lGODlhFgAWAID/AMDAwAAAACH5BAEAAAAALAAAAAAWABYAQAInhI+pa+H9mJy0LhdgtrxzDG5WGFVk6aXqyk6Y9kXvKKNuLbb6zgMFADs=\" />";
		return s;
	}
	private static String createItalicButtonIcon(String sParamName){
		String s = "<img class=\"intLink\" title=\"Italic\" onclick=\"iItalic('" + sParamName + "');\" "
			+ "src=\"data:image/gif;base64,R0lGODlhFgAWAKEDAAAAAF9vj5WIbf///yH5BAEAAAMALAAAAAAWABYAAAIjnI+py+0Po5x0gXvruEKHrF2BB1YiCWgbMFIYpsbyTNd2UwAAOw==\" />";
		return s;
	}
	
	private static String createUnderlineButtonIcon(String sParamName){
		String s = "<img class=\"intLink\" title=\"Underline\" onclick=\"iUnderline('" + sParamName + "');\" "
				+ "src=\"data:image/gif;base64,R0lGODlhFgAWAKECAAAAAF9vj////////yH5BAEAAAIALAAAAAAWABYAAAIrlI+py+0Po5zUgAsEzvEeL4Ea"
				+ "15EiJJ5PSqJmuwKBEKgxVuXWtun+DwxCCgA7\" />";
		return s;
	}
	
	private static String createLinkButtonIcon(String sParamName){
		String s = "<img class=\"intLink\" title=\"Hyperlink\" onclick=\"iLink('" + sParamName + "');\" "
				+ "src=\"data:image/gif;base64,R0lGODlhFgAWAOMKAB1ChDRLY19vj3mOrpGjuaezxrCztb/I19Ha7Pv8/f///////////////////////yH5BAE"
				+ "KAA8ALAAAAAAWABYAAARY8MlJq7046827/2BYIQVhHg9pEgVGIklyDEUBy/RlE4FQF4dCj2AQXAiJQDCWQCAEBwIioEMQBgSAFhDAGghGi9XgHAhMNoSZgJkJei33UESv2+/4vD4TAQA7\" />";
		return s;
	}
	
	private static String createUndoButtonIcon(String sParamName){
		String s = "<img class=\"intLink\" title=\"Undo\" onclick=\"iUndo('" + sParamName + "');\" "
				+ "src=\"data:image/gif;base64,R0lGODlhFgAWAOMKADljwliE33mOrpGjuYKl8aezxqPD+7/I19DV3NHa7P///////////////////////yH5BA"
				+ "EKAA8ALAAAAAAWABYAAARR8MlJq7046807TkaYeJJBnES4EeUJvIGapWYAC0CsocQ7SDlWJkAkCA6ToMYWIARGQF3mRQVIEjkkSVLIbSfEwhdRIH4fh/DZMICe3/C4nBQBADs=\" />";
		return s;
	}
	//TODO fix strike through icon
	/*
	private static String createStrikthroughButtonIcon(String sParamName){
		String s = "<span style=\"text-align:bottom;\" class=\"intLink\" title=\"Strike\" onclick=\"iStrikeThrough('" + sParamName + "');\" "
				+ "><font size=\"x-small\"><b><strike>T</strike></b></font></span>";
		return s;
	}
	*/
	private static String createRemoveFromattingButtonIcon(String sParamName){
		String s = "<img class=\"intLink\" title=\"Remove formatting\" onclick=\"iRemoveFormat('" + sParamName + "');\" "
				+ "src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABYAAAAWCAYAAADEtGw7AAAABGdBTUEAALGPC/xhBQAAAAZiS0dEAP8A/wD/oL2nkwAAAA"
				+ "lwSFlzAAAOxAAADsQBlSsOGwAAAAd0SU1FB9oECQMCKPI8CIIAAAAIdEVYdENvbW1lbnQA9syWvwAAAuhJREFUOMtjYBgFxAB501ZWBvVaL2nHnlmk6mXCJbF69z"
				+ "U+Hz/9fB5O1lx+bg45qhl8/fYr5it3XrP/YWTUvvvk3VeqGXz70TvbJy8+Wv39+2/Hz19/mGwjZzuTYjALuoBv9jImaXHeyD3H7kU8fPj2ICML8z92dlbtMzdeiG3f"
				+ "co7J08foH1kurkm3E9iw54YvKwuTuom+LPt/BgbWf3//sf37/1/c02cCG1lB8f//f95DZx74MTMzshhoSm6szrQ/a6Ir/Z2RkfEjBxuLYFpDiDi6Af///2ckaHBp7+"
				+ "7wmavP5n76+P2ClrLIYl8H9W36auJCbCxM4szMTJac7Kza////R3H1w2cfWAgafPbqs5g7D95++/P1B4+ECK8tAwMDw/1H7159+/7r7ZcvPz4fOHbzEwMDwx8GBgaGn"
				+ "NatfHZx8zqrJ+4VJBh5CQEGOySEua/v3n7hXmqI8WUGBgYGL3vVG7fuPK3i5GD9/fja7ZsMDAzMG/Ze52mZeSj4yu1XEq/ff7W5dvfVAS1lsXc4Db7z8C3r8p7Qjf///"
				+ "2dnZGxlqJuyr3rPqQd/Hhyu7oSpYWScylDQsd3kzvnH738wMDzj5GBN1VIWW4c3KDon7VOvm7S3paB9u5qsU5/x5KUnlY+eexQbkLNsErK61+++VnAJcfkyMTIwffj0Qw"
				+ "ZbJDKjcETs1Y8evyd48toz8y/ffzv//vPP4veffxpX77z6l5JewHPu8MqTDAwMDLzyrjb/mZm0JcT5Lj+89+Ybm6zz95oMh7s4XbygN3Sluq4Mj5K8iKMgP4f0////fv77/"
				+ "/8nLy+7MCcXmyYDAwODS9jM9tcvPypd35pne3ljdjvj26+H2dhYpuENikgfvQeXNmSl3tqepxXsqhXPyc666s+fv1fMdKR3TK72zpix8nTc7bdfhfkEeVbC9KhbK/9iYWHiE"
				+ "rbu6MWbY/7//8/4//9/pgOnH6jGVazvFDRtq2VgiBIZrUTIBgCk+ivHvuEKwAAAAABJRU5ErkJggg==\">";
		return s;
	}
	
	private static String createFontSizeSelection(String sParamName){
		String s = "<select style=\"vertical-align: text-top; font-size:10px;\" "
				+ "onchange=\"iFontSize('" + sParamName + "', this[this.selectedIndex].value);this.selectedIndex=0;\">\n" 
				+ "<option class=\"heading\" selected>- size -</option>\n"  
				+ "<option value=\"1\">1</option>\n" 
				+ "<option value=\"2\">2</option>\n" 
				+ "<option value=\"3\">3 default</option>\n" 
				+ "<option value=\"4\">4</option>\n" 
				+ "<option value=\"5\">5</option>\n" 
				+ "<option value=\"6\">6</option>\n" 
				+ "<option value=\"7\">7</option>\n"
				+ "</select>";
		return s;
	}
	
	private static String createFontColorSelection(String sParamName){
		String s = "<select style=\"vertical-align: text-top; font-size:10px;\" "
				+ "onchange=\"iFontColor('" + sParamName + "', this[this.selectedIndex].value);this.selectedIndex=0;\">\n"
				+ "<option class=\"heading\" selected>- color -</option>\n" 
				+ "<option style=\"color:red;\" value=\"red\">Red</option>\n" 
				+ "<option style=\"color:blue;\" value=\"blue\">Blue</option>\n" 
				+ "<option style=\"color:green;\" value=\"green\">Green</option>\n"  
				+ "<option style=\"color:black;\" value=\"black\">Black</option>\n" 
				+ "</select>";
		return s;
	}
	
	private static String createToggleHTMLButtonIcon(String sParamName){
		String s = "<input type=\"checkbox\" name=\"" + sParamName + "toggleHTML\" id=\"" + sParamName + "toggleHTML\" "
				+ "onchange=\"iToggleHTML('" + sParamName + "', this.checked);\" /> "
				+ "<label for=\"" + sParamName + "toggleHTML\">Plain text</label>";
		return s;
	}
	
//	public static String getJavascriptTextEditor() {
//		String s = " ";	
//		return s;
//	}
}

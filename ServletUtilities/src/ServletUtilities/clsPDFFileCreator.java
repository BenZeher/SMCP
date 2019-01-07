package ServletUtilities;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.codec.Base64;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.html.CssAppliers;
import com.itextpdf.tool.xml.html.CssAppliersImpl;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.AbstractImageProvider;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;

public class clsPDFFileCreator extends Object{
	
	private String m_sFullFileoutputfilename;
	StringBuilder m_stringBuilderHtml;
	
    public clsPDFFileCreator(String sFullFileName){
		initEntryVariables();
		m_sFullFileoutputfilename = sFullFileName;
		m_stringBuilderHtml = new StringBuilder();
    }
    public void createPDFFromString(String sHTML) throws Exception{
    	
    	InputStream is = new ByteArrayInputStream(sHTML.getBytes());
    	
    	//writeTextToFile(sHTML);
    	
    	createPdfFile(is);
    	//InputStream is;
		//try {
		//	is = new ByteArrayInputStream(stringBuilderHtml.toString().getBytes());
		//} catch (Exception e) {
		//	throw new Exception("Error [1487005991] creating InputStream - " + e.getMessage());
		//}
    }
    
    // TJR - For testing ONLY:
    @SuppressWarnings("unused")
	private void writeTextToFile(String sText){
    	
		BufferedWriter bw = null;
		FileWriter fw = null;

		try {

			String content = "This is the content to write into file\n";

			fw = new FileWriter("/home/tom/Desktop/invoice.html");
			bw = new BufferedWriter(fw);
			bw.write(sText);

			System.out.println("Done");

		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			try {

				if (bw != null)
					bw.close();

				if (fw != null)
					fw.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}

		}

	}

    private void createPdfFile(InputStream sInputStream) throws Exception {

    	Document document;
		try {
			document = new Document();
		} catch (Exception e) {
			throw new Exception("Error [1487005982] opening Document object - " + e.getMessage());
		}
		
        PdfWriter writer;
		try {
			writer = PdfWriter.getInstance(document, new FileOutputStream(m_sFullFileoutputfilename));
		} catch (Exception e) {
			throw new Exception("Error [1487005983] creating PDFWriter - " + e.getMessage());
		}
        
        writer.setInitialLeading(12.5f);
        
        try {
			document.open();
		} catch (Exception e) {
			throw new Exception("Error [1487005984] opening document - " + e.getMessage());
		}
 
        // CSS
        CSSResolver cssResolver;
		try {
			cssResolver = XMLWorkerHelper.getInstance().getDefaultCssResolver(false);
		} catch (Exception e) {
			throw new Exception("Error [1487005985] creating XMLWorkerHelper - " + e.getMessage());
		}
        
        //This is done to speed up the PDF creation - otherwise it takes 20+ seconds to create one PDF....
        XMLWorkerFontProvider fontProvider;
		try {
			fontProvider = new XMLWorkerFontProvider(XMLWorkerFontProvider.DONTLOOKFORFONTS);
		} catch (Exception e) {
			throw new Exception("Error [1487005986] creating XMLWorkerFontProvider - " + e.getMessage());
		}
		// TJR - keeping this commented code in here because it will be required if we need to 
		// use actual fonts in the web pages:
		//fontProvider.register("resources/fonts/Cardo-Regular.ttf");
        //fontProvider.register("resources/fonts/Cardo-Bold.ttf");
        //fontProvider.register("resources/fonts/Cardo-Italic.ttf");
        //fontProvider.addFontSubstitute("lowagie", "cardo");
		
        CssAppliers cssAppliers;
		try {
			cssAppliers = new CssAppliersImpl(fontProvider);
		} catch (Exception e) {
			throw new Exception("Error [1487005987] creating CssAppliersImpl - " + e.getMessage());
		}
        
        // HTML
        HtmlPipelineContext htmlContext;
		try {
			htmlContext = new HtmlPipelineContext(cssAppliers);
			htmlContext.setTagFactory(Tags.getHtmlTagProcessorFactory());
			htmlContext.setImageProvider(new Base64ImageProvider());
			htmlContext.autoBookmark(false);
		} catch (Exception e) {
			throw new Exception("Error [1487005988] creating htmlContext - " + e.getMessage());
		}
        
        // Pipelines
        CssResolverPipeline css;
		try {
			PdfWriterPipeline pdf = new PdfWriterPipeline(document, writer);
			HtmlPipeline html = new HtmlPipeline(htmlContext, pdf);
			css = new CssResolverPipeline(cssResolver, html);
		} catch (Exception e) {
			throw new Exception("Error [1487005989] creating pipelines - " + e.getMessage());
		}
 
        // XML Worker
		XMLParser p;
		try {
			XMLWorker worker = new XMLWorker(css, true);
			p = new XMLParser(worker);
		} catch (Exception e) {
			throw new Exception("Error [1487005990] creating XMLWorker - " + e.getMessage());
		}
        
        try {
			p.parse(sInputStream);
		} catch (Exception e) {
			throw new Exception("Error [1487005992] parsing - " + e.getMessage());
		}
        
        document.close();
        
    }
    class Base64ImageProvider extends AbstractImageProvider {

    	@Override
    	public Image retrieve(String src) {
    		int pos = src.indexOf("base64,");
    		try {
    			if (src.startsWith("data") && pos > 0) {
    				byte[] img = Base64.decode(src.substring(pos + 7));
    				return Image.getInstance(img);
    			}
    			else {
    				return Image.getInstance(src);
    			}
    		} catch (BadElementException ex) {
    			return null;
    		} catch (IOException ex) {
    			return null;
    		}
    	}

    	@Override
    	public String getImageRootPath() {
    		return null;
    	}
    }
    
    //This function is now currently used, but could be useful if we have a need to build an HTML document line by line:
    public void appendLine(String sLine){
    	m_stringBuilderHtml.append(sLine);
    }
    private void initEntryVariables(){
    	m_sFullFileoutputfilename = "";
	}
    public void removePDFFile() throws Exception{
    	//Try to delete the PDF file previously created:
    	try {
			File file = new File(m_sFullFileoutputfilename);
			if(file.delete()){
			}else{
				throw new Exception("Error [1487713502] - couldn't delete PDF file '" + m_sFullFileoutputfilename + "'.");
			}
		} catch (Exception e) {
			throw new Exception("Error [1487713503] deleting PDF file '" + m_sFullFileoutputfilename + "' - " + e.getMessage());
		}
    }
}

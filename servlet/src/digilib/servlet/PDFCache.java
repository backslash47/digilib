package digilib.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A class for handling user requests for pdf documents from digilib images.  
 * 
 * If a document does not already exist, it will be enqueued for generation; if it does exist, it is sent
 * to the user.
 * 
 * @author cmielack
 *
 */

@SuppressWarnings("serial")
public class PDFCache extends RequestHandler {

	private DigilibConfiguration dlConfig = null;
	
	public static String instanceKey = "digilib.servlet.PDFCache";
	
	private DigilibJobCenter<OutputStream> pdfJobCenter = null;
	
	private File cache_directory = new File("cache");  
	
	private File temp_directory = new File("pdf_temp");
	
	private static String JSP_WIP = "/pdf/wip.jsp";
	
	private static String JSP_ERROR = "/pdf/error.jsp";
	
	public static Integer STATUS_DONE = 0;  	// document exists in cache

	public static Integer STATUS_WIP = 1;  		// document is "work in progress"
	
	public static Integer STATUS_NONEXISTENT = 2;  	// document does not exist in cache and is not in progress
	
	public static Integer STATUS_ERROR = 3;     // an error occurred while processing the request
	
	public static String version = "0.2";
	
	private ServletContext context = null;

	
	// TODO ? functionality for the pre-generation of complete books/chapters using default values
	
	
	public void init(ServletConfig config) throws ServletException{
		super.init(config);
		
        System.out.println("***** Digital Image Library Image PDF-Cache Servlet (version "
                + version + ") *****");
        // say hello in the log file
        logger.info("***** Digital Image Library Image PDF-Cache Servlet (version "
                + version + ") *****");

		context = config.getServletContext();
		dlConfig = (DigilibConfiguration) context.getAttribute("digilib.servlet.configuration");
		if (dlConfig == null) {
			// no Configuration
			throw new ServletException("No Configuration!");
		}
	
		String temp_fn = dlConfig.getAsString("pdf-temp-dir");
		temp_directory = new File(temp_fn);
		if (!temp_directory.isDirectory()) {
		    throw new ServletException("Configuration error: problem with pdf-temp-dir="+temp_fn);
		}
        // rid the temporary directory of possible incomplete document files
        emptyDirectory(temp_directory);
        
		String cache_fn = dlConfig.getAsString("pdf-cache-dir");
       	cache_directory = new File(cache_fn);
        if (!cache_directory.isDirectory()) {
            throw new ServletException("Configuration error: problem with pdf-cache-dir="+cache_fn);
        }

        pdfJobCenter = (DigilibJobCenter<OutputStream>) dlConfig.getValue("servlet.worker.pdfexecutor");
        
		// register this instance globally
		context.setAttribute(instanceKey, this);
		
	}
	
	/** 
	 * clean up any broken and unfinished files from the temporary directory.
	 */
	public void emptyDirectory(File temp_dir){
		File[] temp_files = temp_dir.listFiles();
		
		for (File f: temp_files){
			f.delete();
		}
	}
	
	
	
	@Override
	public void processRequest(HttpServletRequest request,
			HttpServletResponse response) {

		// evaluate request ( make a PDFJobDeclaration , get the DocumentId)
		PDFJobInformation pdfji = new PDFJobInformation(dlConfig); 
		pdfji.setWithRequest(request);
		
		String docid = pdfji.getDocumentId();
		
		// if some invalid data has been entered ...
		if(!pdfji.checkValidity()) {
			notifyUser(STATUS_ERROR, docid, request, response);
			return;
		}
		
		int status = getStatus(docid);
		
        if (status == STATUS_NONEXISTENT) {
            createNewPdfDocument(pdfji, docid);
            notifyUser(status, docid, request, response);
        } else if (status == STATUS_DONE) {
            try {
                sendFile(docid, getDownloadFilename(pdfji), response);
            } catch (IOException e) {
                e.printStackTrace();
                logger.error(e.getMessage());
            }
        } else {
            notifyUser(status, docid, request, response);
        }
	}

	/**
	 * depending on the documents status, redirect the user to an appropriate waiting- or download-site
	 * 
	 * @param status
	 * @param documentid
	 * @param request
	 * @param response
	 */
	public void notifyUser(int status, String documentid, HttpServletRequest request, HttpServletResponse response){
		
		String jsp=null;
		
		if(status == STATUS_NONEXISTENT){
			// tell the user that the document has to be created before he/she can download it
			logger.debug("PDFCache: "+documentid+" has STATUS_NONEXISTENT.");
			jsp = JSP_WIP;
		} else if(status == STATUS_WIP){
			logger.debug("PDFCache: "+documentid+" has STATUS_WIP.");
			jsp = JSP_WIP;

			// estimate remaining work time
			// tell the user he/she has to wait
		} else if(status == STATUS_DONE){
			logger.debug("PDFCache: "+documentid+" has STATUS_DONE.");
		} else {
			logger.debug("PDFCache: "+documentid+" has STATUS_ERROR.");
			jsp = JSP_ERROR;
		}

		RequestDispatcher dispatch = context.getRequestDispatcher(jsp);
		
		try {
			dispatch.forward(request, response);
		} catch (ServletException e) {
			logger.debug(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.debug(e.getMessage());
			e.printStackTrace();
		}
		
	}

	
	/** check the status of the document corresponding to the documentid */
	public Integer getStatus(String documentid){
		// looks into the cache and temp directory in order to find out the status of the document
		File cached = new File(cache_directory, documentid);
		File wip = new File(temp_directory, documentid);
		if(cached.exists()){
			return STATUS_DONE;
		} else if (wip.exists()){
			return STATUS_WIP;
		} else {
			return STATUS_NONEXISTENT;
		}
	}

	/** 
	 * create new thread for pdf generation.
	 * 
	 * @param pdfji
	 * @param filename
	 */
	public void createNewPdfDocument(PDFJobInformation pdfji, String filename){
		// start new worker
		PDFMaker pdf_maker = new PDFMaker(context, pdfji,filename);
		new Thread(pdf_maker, "PDFMaker").start();
	}
	
	
	/**
	 * generate the filename the user is going to receive the pdf as
	 * 
	 * @param pdfji
	 * @return
	 */
	public String getDownloadFilename(PDFJobInformation pdfji){
		// filename example: digilib_example_pgs1-3.pdf
		String filename;
		filename =  "digilib_";
		filename += pdfji.getImageJobInformation().getAsString("fn");
		filename += "_pgs" + pdfji.getAsString("pgs");
		filename += ".pdf";
		
		return filename;
	}
	
	/**
	 *  sends a document to the user
	 * 
	 * @param cachefile  The filename of the  document in cache.
	 * @param filename  The filename used for downloading.
	 * @param response  
	 * @throws IOException
	 */
	public void sendFile(String cachefile, String filename, HttpServletResponse response) throws IOException{
		File cached_file = null;
		FileInputStream fis = null;
		ServletOutputStream sos = null;
		BufferedInputStream bis = null;

		try {
			// get file handle
			cached_file = new File(cache_directory, cachefile);
			// create necessary streams
			fis = new FileInputStream(cached_file);
			sos = response.getOutputStream();
			bis = new BufferedInputStream(fis);

			int bytes = 0;

			// set http headers
			response.setContentType("application/pdf");
			response.addHeader("Content-Disposition", "attachment; filename="+filename);
			response.setContentLength( (int) cached_file.length());

			// send the bytes
			while ((bytes = bis.read()) != -1){ 
				sos.write(bytes);
			}
			}
			catch(Exception e){
				logger.error(e.getMessage());
			}
			finally{
				// close all streams
				if (fis != null)
					fis.close();
				if (bis != null)
					bis.close();
				if (sos != null)
					sos.close();
			}

	}
	
	public File getCacheDirectory(){
		return cache_directory;
	}
	
	public File getTempDirectory(){
		return temp_directory;
	}
	
	/** 
	 * returns a File object based on filename in the temp directory.
	 * @param filename
	 * @return
	 */
	public File getTempFile(String filename) {
	    return new File(temp_directory, filename);
	}

	/** 
     * returns a File object based on filename in the cache directory.
     * @param filename
     * @return
     */
    public File getCacheFile(String filename) {
        return new File(cache_directory, filename);
    }
}

/*
 * DigilibRequest.java
 *
 * lightweight class carrying all parameters for a request to digilib
 *

  Digital Image Library servlet components

  Copyright (C) 2001, 2002 Robert Casties (robcast@mail.berlios.de),
                           Christian Luginbuehl

  This program is free software; you can redistribute  it and/or modify it
  under  the terms of  the GNU General  Public License as published by the
  Free Software Foundation;  either version 2 of the  License, or (at your
  option) any later version.
   
  Please read license.txt for the full details. A copy of the GPL
  may be found at http://www.gnu.org/copyleft/lgpl.html

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 * Created on 27. August 2002, 19:43
 */

package digilib.servlet;

import java.io.*;
import java.util.*;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import com.hp.hpl.mesa.rdf.jena.mem.ModelMem;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.common.*;

import digilib.image.DocuImage;

/** Class holding the parameters of a digilib user request.
 * The parameters are mostly named like the servlet parameters:<br>
 * request_path: url of the page/document.<br> fn: url of the page/document.
 * <br> pn: page number.<br> dw: width of result window in pixels.<br> dh:
 * height of result window in pixels.<br> wx: left edge of image area (float
 * from 0 to 1).<br> wy: top edge of image area (float from 0 to 1).<br> ww:
 * width of image area(float from 0 to 1).<br> wh: height of image area(float
 * from 0 to 1).<br> ws: scale factor.<br> mo: special options like 'fit' for
 * gifs.<br> mk: list of marks.<br> pt: total number of pages (generated by
 * sevlet).<br> baseURL: base URL (from http:// to below /servlet).
 * 
 * @author casties
 *
 */
public class DigilibRequest {

	private String request_path; // url of the page/document
	private String fn; // url of the page/document
	private int pn; // page number
	private String pn_s;
	private int dw; // width of client in pixels
	private String dw_s;
	private int dh; // height of client in pixels
	private String dh_s;
	private float wx; // left edge of image (float from 0 to 1)
	private String wx_s;
	private float wy; // top edge in image (float from 0 to 1)
	private String wy_s;
	private float ww; // width of image (float from 0 to 1)
	private String ww_s;
	private float wh; // height of image (float from 0 to 1)
	private String wh_s;
	private float ws; // scale factor
	private String ws_s;
	private String mo; // special options like 'fit' for gifs
	private String mk; // marks    
	private int pt; // total number of pages (generated by sevlet)
	private String pt_s;
	private String baseURL; // base URL (from http:// to below /servlet)
	private float rot; // rotation angle in degrees
	private String rot_s;
	private float cont; // contrast enhancement factor
	private String cont_s;
	private float brgt; // brightness enhancement factor
	private String brgt_s;
	private float[] rgbm; // color multiplicative factors
	private String rgbm_s;
	private float[] rgba; // color additive factors
	private String rgba_s;
	private int lv; // level of digilib (0 = just image, 1 = one HTML page
	                //                   2 = in frameset, 3 = XUL-'frameset'
	                //                   4 = XUL-Sidebar )
	private String lv_s;

	private DocuImage image; // internal DocuImage instance for this request
	private ServletRequest servletRequest; // internal ServletRequest
        private boolean boolRDF=false; // uses RDF Parameters


	/** Creates a new instance of DigilibRequest and sets default values. */
	public DigilibRequest() {
		setToDefault();
	}

	/** Creates a new instance of DigilibRequest with parameters from a
	 * ServletRequest.
	 * All undefined parameters are set to default values.
	 * 
	 * @param request
	 */
	public DigilibRequest(ServletRequest request) {
		reset();
		setWithRequest(request);
		
	}

	/** Populate the request object with data from a ServletRequest.
	 * 
	 * 
	 * @param request
	 */
	public void setWithRequest(ServletRequest request) {
		servletRequest = request;
		// decide if it's old-style or new-style
		String qs = ((HttpServletRequest) request).getQueryString();
		if (request.getParameter("rdf") != null) {
			boolRDF=true;
		        setWithRDF(request);                        
		} else if (request.getParameter("fn") != null) {
			setWithParamRequest(request);
		} else if ((qs != null) && (qs.indexOf("&") > -1)) {
			setWithParamRequest(request);
		} else {
			setWithOldString(qs);
		}
		// set the baseURL
		setBaseURL((HttpServletRequest) request);
	}

	/** Populate a request from a string in the old "++++" parameter form.
	 *
	 * @param queryString String with paramters in the old "+++" form.
	 */
	public void setWithOldString(String queryString) {

		if (queryString == null) {
			return;
		}

		// enable the passing of delimiter to get empty parameters
		StringTokenizer query = new StringTokenizer(queryString, "+", true);
		String token;

		// first parameter FN
		if (query.hasMoreTokens()) {
			token = query.nextToken();
			if (!token.equals("+")) {
				fn = token;
				request_path = null;
				if (query.hasMoreTokens()) {
					query.nextToken();
				}
			}
		}
		// second parameter PN
		if (query.hasMoreTokens()) {
			token = query.nextToken();
			if (!token.equals("+")) {
				try {
					int i = Integer.parseInt(token);
					pn = i;
					pn_s = token;
				} catch (Exception e) {
					//util.dprintln(4, "trytoGetParam(int) failed on param "+s);
				}
				if (query.hasMoreTokens()) {
					query.nextToken();
				}
			}
		}
		// third parameter WS
		if (query.hasMoreTokens()) {
			token = query.nextToken();
			if (!token.equals("+")) {
				try {
					float f = Float.parseFloat(token);
					ws = f;
					ws_s = token;
				} catch (Exception e) {
					//util.dprintln(4, "trytoGetParam(int) failed on param "+s);
				}
				if (query.hasMoreTokens()) {
					query.nextToken();
				}
			}
		}
		// fourth parameter MO
		if (query.hasMoreTokens()) {
			token = query.nextToken();
			if (!token.equals("+")) {
				mo = token;
				if (query.hasMoreTokens()) {
					query.nextToken();
				}
			}
		}
		// fifth parameter MK
		if (query.hasMoreTokens()) {
			token = query.nextToken();
			if (!token.equals("+")) {
				mk = token;
				if (query.hasMoreTokens()) {
					query.nextToken();
				}
			}
		}
		// sixth parameter WX
		if (query.hasMoreTokens()) {
			token = query.nextToken();
			if (!token.equals("+")) {
				try {
					float f = Float.parseFloat(token);
					wx = f;
					wx_s = token;
				} catch (Exception e) {
					//util.dprintln(4, "trytoGetParam(int) failed on param "+s);
				}
				if (query.hasMoreTokens()) {
					query.nextToken();
				}
			}
		}
		// seventh parameter WY
		if (query.hasMoreTokens()) {
			token = query.nextToken();
			if (!token.equals("+")) {
				try {
					float f = Float.parseFloat(token);
					wy = f;
					wy_s = token;
				} catch (Exception e) {
					//util.dprintln(4, "trytoGetParam(int) failed on param "+s);
				}
				if (query.hasMoreTokens()) {
					query.nextToken();
				}
			}
		}
		// eigth parameter WW
		if (query.hasMoreTokens()) {
			token = query.nextToken();
			if (!token.equals("+")) {
				try {
					float f = Float.parseFloat(token);
					ww = f;
					ww_s = token;
				} catch (Exception e) {
					//util.dprintln(4, "trytoGetParam(int) failed on param "+s);
				}
				if (query.hasMoreTokens()) {
					query.nextToken();
				}
			}
		}
		// ninth parameter WH
		if (query.hasMoreTokens()) {
			token = query.nextToken();
			if (!token.equals("+")) {
				try {
					float f = Float.parseFloat(token);
					wh = f;
					wh_s = token;
				} catch (Exception e) {
					//util.dprintln(4, "trytoGetParam(int) failed on param "+s);
				}
				if (query.hasMoreTokens()) {
					query.nextToken();
				}
			}
		}
	}

	/** Return the request parameters as a String in the parameter form
	 * 'fn=/icons&amp;pn=1'. Empty (undefined) fields are not included.
	 *
	 * @return String of request parameters in parameter form.
	 */
	public String getAsString() {
		String s = "";
		// request_path adds to fn 
		if ((fn != null) || (request_path != null)) {
			s += "fn="
				+ ((request_path != null) ? request_path : "")
				+ ((fn != null) ? fn : "");
		}
		if (pn_s != null) {
			s += "&pn=" + pn_s;
		}
		if (dw_s != null && dw_s != "0") {
			s += "&dw=" + dw_s;
		}
		if (dh_s != null && dh_s != "0") {
			s += "&dh=" + dh_s;
		}
		if (wx_s != null) {
			s += "&wx=" + wx_s;
		}
		if (wy_s != null) {
			s += "&wy=" + wy_s;
		}
		if (ww_s != null) {
			s += "&ww=" + ww_s;
		}
		if (wh_s != null) {
			s += "&wh=" + wh_s;
		}
		if (ws_s != null) {
			s += "&ws=" + ws_s;
		}
		if ((mo != null)&&(mo.length() > 0)) {
			s += "&mo=" + mo;
		}
		if ((mk != null)&&(mk.length() > 0)) {
			s += "&mk=" + mk;
		}
		if (rot_s != null) {
			s += "&rot=" + rot_s;
		}
		if (cont_s != null) {
			s += "&cont=" + cont_s;
		}
		if (brgt_s != null) {
			s += "&brgt=" + brgt_s;
		}
		if (rgbm_s != null) {
			s += "&rgbm=" + rgbm_s;
		}
		if (rgba_s != null) {
			s += "&rgba=" + rgba_s;
		}
		if (pt_s != null) {
			s += "&pt=" + pt_s;
		}
		if (lv_s != null) {
			s += "&lv=" + lv_s;
		}

		return s;
	}

	/** Returns request parameters in old '++++' form.
	 *
	 * @return String with parameters in old '++++' form.
	 */
	public String getAsOldString() {
		String s = "";
		s += (request_path != null) ? request_path : "";
		s += (fn != null) ? fn : "";
		s += "+" + ((pn_s != null) ? pn_s : "");
		s += "+" + ((ws_s != null) ? ws_s : "");
		s += "+" + ((mo != null) ? mo : "");
		s += "+" + ((mk != null) ? mk : "");
		s += "+" + ((wx_s != null) ? wx_s : "");
		s += "+" + ((wy_s != null) ? wy_s : "");
		s += "+" + ((ww_s != null) ? ww_s : "");
		s += "+" + ((wh_s != null) ? wh_s : "");
		return s;
	}

	/** Set request parameters from javax.servlet.ServletRequest. Uses the Requests
	 * getParameter methods for 'fn=foo' style parameters.
	 *
	 * @param request ServletRequest to get parameters from.
	 */
	public void setWithParamRequest(ServletRequest request) {
		String s;
		s = request.getParameter("fn");
		if (s != null) {
			setFn(s);
		}
		s = request.getParameter("pn");
		if (s != null) {
			setPn(s);
		}
		s = request.getParameter("ws");
		if (s != null) {
			setWs(s);
		}
		s = request.getParameter("mo");
		if (s != null) {
			setMo(s);
		}
		s = request.getParameter("mk");
		if (s != null) {
			setMk(s);
		}
		s = request.getParameter("wx");
		if (s != null) {
			setWx(s);
		}
		s = request.getParameter("wy");
		if (s != null) {
			setWy(s);
		}
		s = request.getParameter("ww");
		if (s != null) {
			setWw(s);
		}
		s = request.getParameter("wh");
		if (s != null) {
			setWh(s);
		}
		s = request.getParameter("dw");
		if (s != null) {
			setDw(s);
		}
		s = request.getParameter("dh");
		if (s != null) {
			setDh(s);
		}
		s = request.getParameter("rot");
		if (s != null) {
			setRot(s);
		}
		s = request.getParameter("cont");
		if (s != null) {
			setCont(s);
		}
		s = request.getParameter("brgt");
		if (s != null) {
			setBrgt(s);
		}
		s = request.getParameter("rgbm");
		if (s != null) {
			setRgbm(s);
		}
		s = request.getParameter("rgba");
		if (s != null) {
			setRgba(s);
		}
		s = request.getParameter("pt");
		if (s != null) {
			setPt(s);
		}
		s = request.getParameter("lv");
		if (s != null) {
			setLv(s);
		}
		s = ((HttpServletRequest) request).getPathInfo();
		if (s != null) {
			setRequestPath(s);
		}
	}

/**
 *
 *
 */
    public void setWithRDF(ServletRequest request) {
	String strRDF;
	strRDF = request.getParameter("rdf");
	if (strRDF != null) {
	    //System.out.println(strRDF);
	    Hashtable hashRDF=rdf2hash(strRDF);
	    //System.out.println(hashRDF.toString());
	    String s;
	    s = (String)hashRDF.get("fn");
	    if (s != null) {
		setFn(s);
	    }
	    s = (String)hashRDF.get("pn");
	    if (s != null) {
		setPn(s);
	    }
	    s = (String)hashRDF.get("ws");
	    if (s != null) {
		setWs(s);
	    }
	    s = (String)hashRDF.get("mo");
	    if (s != null) {
		setMo(s);
	    }
	    s = (String)hashRDF.get("mk");
	    if (s != null) {
		setMk(s);
	    }
	    s = (String)hashRDF.get("wx");
	    if (s != null) {
		setWx(s);
	    }
	    s = (String)hashRDF.get("wy");
	    if (s != null) {
		setWy(s);
	    }
	    s = (String)hashRDF.get("ww");
	    if (s != null) {
		setWw(s);
	    }
	    s = (String)hashRDF.get("wh");
	    if (s != null) {
		setWh(s);
	    }
	    s = (String)hashRDF.get("dw");
	    if (s != null) {
		setDw(s);
	    }
	    s = (String)hashRDF.get("dh");
	    if (s != null) {
		setDh(s);
	    }
	    s = (String)hashRDF.get("rot");
	    if (s != null) {
		setRot(s);
	    }
	    s = (String)hashRDF.get("cont");
	    if (s != null) {
		setCont(s);
	    }
	    s = (String)hashRDF.get("brgt");
	    if (s != null) {
		setBrgt(s);
	    }
	    s = (String)hashRDF.get("rgbm");
	    if (s != null) {
		setRgbm(s);
	    }
	    s = (String)hashRDF.get("rgba");
	    if (s != null) {
		setRgba(s);
	    }
	    s = (String)hashRDF.get("pt");
	    if (s != null) {
		setPt(s);
	    }
	    s = (String)hashRDF.get("lv");
	    if (s != null) {
		setLv(s);
	    }
	    s = ((HttpServletRequest) request).getPathInfo();
	    if (s != null) {
		setRequestPath(s);
	    }
	}
    }
    
    private Hashtable rdf2hash(String strRDF){
	Hashtable hashParams=new Hashtable();
	try {
	    // create an empty model
	    Model model = new ModelMem();
	    StringReader sr=new StringReader(strRDF);
	    model.read(sr,"");
            // get Property fn -> digilib
            Property p=model.getProperty("http://echo.unibe.ch/digilib/rdf#","fn");
	    //System.out.println(p.toString());
            if (p!=null){
                // get URI
                String strURI=null;
                NodeIterator i=model.listObjectsOfProperty(p);
                if (i.hasNext()){
                    strURI="urn:echo:"+i.next().toString();
                    Resource r=model.getResource(strURI);
		    //System.out.println(r.toString());
                    Selector selector = new SelectorImpl(r,null,(RDFNode)null);
                    // list the statements in the graph
                    StmtIterator iter = model.listStatements(selector);
                    // add predicate and object to Hashtable
                    while (iter.hasNext()) {
                        Statement stmt      = iter.next();         // get next statement
                        Resource  subject   = stmt.getSubject();   // get the subject
                        Property  predicate = stmt.getPredicate(); // get the predicate
                        RDFNode   object    = stmt.getObject();    // get the object
			
                        String strKey=predicate.toString();
                        String strValue="";
			
                        if (object instanceof Resource) {
                            strValue=object.toString();
                        } else {
                            // object is a literal
                            strValue=object.toString();
                        }
			String strDigilibKey=strKey.substring(strKey.indexOf("#")+1,strKey.length());
                        hashParams.put(strDigilibKey,strValue);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Failed: " + e);
        }
        return hashParams;
    }
    

	/** Reset all request parameters to null. */
	public void reset() {
		request_path = null; // url of the page/document

		lv = 0; // level of digilib cf. variable declaration
		lv_s = null;
		fn = null; // url of the page/document
		pn = 0; // page number
		pn_s = null;
		dw = 0; // width of client in pixels
		dw_s = null;
		dh = 0; // height of client in pixels
		dh_s = null;
		wx = 0f; // left edge of image (float from 0 to 1)
		wx_s = null;
		wy = 0f; // top edge in image (float from 0 to 1)
		wy_s = null;
		ww = 0f; // width of image (float from 0 to 1)
		ww_s = null;
		wh = 0f; // height of image (float from 0 to 1)
		wh_s = null;
		ws = 0f; // scale factor
		ws_s = null;
		mo = null; // special options like 'fit' for gifs
		mk = null; // marks
		pt = 0; // total number of pages
		pt_s = null;
		rot = 0;
		rot_s = null;
		cont = 0;
		cont_s = null;
		brgt = 0;
		brgt_s = null;
		rgbm = null;
		rgbm_s = null;
		rgba = null;
		rgba_s = null;
		baseURL = null;
		image = null;
		servletRequest = null;
	}

	/** Reset all request parameters to default values. */
	public void setToDefault() {
		lv = 2; // default level
		lv_s = "2";
		request_path = ""; // url of the page/document
		fn = ""; // url of the page/document
		pn = 1; // page number
		pn_s = "1";
		dw = 0; // width of client in pixels
		dw_s = "0";
		dh = 0; // height of client in pixels
		dh_s = "0";
		wx = 0f; // left edge of image (float from 0 to 1)
		wx_s = "0";
		wy = 0f; // top edge in image (float from 0 to 1)
		wy_s = "0";
		ww = 1f; // width of image (float from 0 to 1)
		ww_s = "1";
		wh = 1f; // height of image (float from 0 to 1)
		wh_s = "1";
		ws = 1f; // scale factor
		ws_s = "1";
		mo = ""; // special options like 'fit' for gifs
		mk = ""; // marks
		pt = 0; // total number of pages
		pt_s = null;
		rot = 0;
		rot_s = null;
		cont = 0;
		cont_s = null;
		brgt = 0;
		brgt_s = null;
		rgbm = null;
		rgbm_s = null;
		rgba = null;
		baseURL = null;
		image = null;
		servletRequest = null;
	}

	/** Test if option string <code>opt</code> is set.
	 * Checks if the substring <code>opt</code> is contained in the options
	 * string <code>mo</code>.
	 * 
	 * @param opt Option string to be tested.
	 * @return boolean
	 */
	public boolean isOption(String opt) {
		if ((mo != null) && (mo.indexOf(opt) >= 0)) {
			return true;
		}
		return false;
	}

	/** The image file path to be accessed.
	 * 
	 * The mage file path is assembled from the servlets RequestPath and
	 * Parameter fn. The file path never starts with a directory separator.
	 * 
	 * @return String the effective filepath.
	 */
	public String getFilePath() {
		String s = getRequestPath();
		s += getFn();
		if (s.startsWith(File.separator)) {
			return s.substring(1);
		} else {
			return s;
		}
	}

	/* Property getter and setter */

// lugi - begin

	/** Getter for property lv.
	 * @return Value of property lv.
	 *
	 */
	public int getLv() {
		return lv;
	}

	/** Setter for property lv.
	 * @param lv New value of property lv.
	 *
	 */
	public void setLv(int lv) {
		this.lv = lv;
		lv_s = Integer.toString(lv);
	}
	public void setLv(String lv) {
		try {
			int i = Integer.parseInt(lv);
			this.lv = i;
			this.lv_s = lv;
		} catch (Exception e) {
			//util.dprintln(4, "trytoGetParam(int) failed on param "+s);
		}
	}

// lugi - end

	/** Getter for property dh.
	 * @return Value of property dh.
	 *
	 */
	public int getDh() {
		return dh;
	}

	/** Setter for property dh.
	 * @param dh New value of property dh.
	 *
	 */
	public void setDh(int dh) {
		this.dh = dh;
		dh_s = Integer.toString(dh);
	}
	public void setDh(String dh) {
		try {
			int i = Integer.parseInt(dh);
			this.dh = i;
			this.dh_s = dh;
		} catch (Exception e) {
			//util.dprintln(4, "trytoGetParam(int) failed on param "+s);
		}
	}

	/** Getter for property dw.
	 * @return Value of property dw.
	 *
	 */
	public int getDw() {
		return dw;
	}

	/** Setter for property dw.
	 * @param dw New value of property dw.
	 *
	 */
	public void setDw(int dw) {
		this.dw = dw;
		dw_s = Integer.toString(dw);
	}
	public void setDw(String dw) {
		try {
			int i = Integer.parseInt(dw);
			this.dw = i;
			this.dw_s = dw;
		} catch (Exception e) {
			//util.dprintln(4, "trytoGetParam(int) failed on param "+s);
		}
	}

	/** Getter for property fn.
	 * @return Value of property fn.
	 *
	 */
	public String getFn() {
		return (fn != null) ? fn : "";
	}

	/** Setter for property fn.
	 * @param fn New value of property fn.
	 *
	 */
	public void setFn(String fn) {
		this.fn = fn;
	}

	/** Getter for property mo.
	 * @return Value of property mo.
	 *
	 */
	public String getMo() {
		return (mo != null) ? mo : "";
	}

	/** Setter for property mo.
	 * @param mo New value of property mo.
	 *
	 */
	public void setMo(String mo) {
		this.mo = mo;
	}

	/** Getter for property pn.
	 * @return Value of property pn.
	 *
	 */
	public int getPn() {
		return pn;
	}

	/** Setter for property pn.
	 * @param pn New value of property pn.
	 *
	 */
	public void setPn(int pn) {
		this.pn = pn;
		pn_s = Integer.toString(pn);
	}
	public void setPn(String pn) {
		try {
			int i = Integer.parseInt(pn);
			this.pn = i;
			this.pn_s = pn;
		} catch (Exception e) {
			//util.dprintln(4, "trytoGetParam(int) failed on param "+s);
		}
	}

	/** Getter for property request_path.
	 * @return Value of property request_path.
	 *
	 */
	public String getRequestPath() {
		return (request_path != null) ? request_path : "";
	}

	/** Setter for property request_path.
	 * @param request_path New value of property request_path.
	 *
	 */
	public void setRequestPath(String request_path) {
		this.request_path = request_path;
	}

	/** Getter for property wh.
	 * @return Value of property wh.
	 *
	 */
	public float getWh() {
		return wh;
	}

	/** Setter for property wh.
	 * @param wh New value of property wh.
	 *
	 */
	public void setWh(float wh) {
		this.wh = wh;
		wh_s = Float.toString(wh);
	}
	public void setWh(String wh) {
		try {
			float f = Float.parseFloat(wh);
			this.wh = f;
			this.wh_s = wh;
		} catch (Exception e) {
			//util.dprintln(4, "trytoGetParam(int) failed on param "+s);
		}
	}

	/** Getter for property ws.
	 * @return Value of property ws.
	 *
	 */
	public float getWs() {
		return ws;
	}

	/** Setter for property ws.
	 * @param ws New value of property ws.
	 *
	 */
	public void setWs(float ws) {
		this.ws = ws;
		ws_s = Float.toString(ws);
	}
	public void setWs(String ws) {
		try {
			float f = Float.parseFloat(ws);
			this.ws = f;
			this.ws_s = ws;
		} catch (Exception e) {
			//util.dprintln(4, "trytoGetParam(int) failed on param "+s);
		}
	}

	/** Getter for property ww.
	 * @return Value of property ww.
	 *
	 */
	public float getWw() {
		return ww;
	}

	/** Setter for property ww.
	 * @param ww New value of property ww.
	 *
	 */
	public void setWw(float ww) {
		this.ww = ww;
		ww_s = Float.toString(ww);
	}
	public void setWw(String ww) {
		try {
			float f = Float.parseFloat(ww);
			this.ww = f;
			this.ww_s = ww;
		} catch (Exception e) {
			//util.dprintln(4, "trytoGetParam(int) failed on param "+s);
		}
	}

	/** Getter for property wx.
	 * @return Value of property wx.
	 *
	 */
	public float getWx() {
		return wx;
	}

	/** Setter for property wx.
	 * @param wx New value of property wx.
	 *
	 */
	public void setWx(float wx) {
		this.wx = wx;
		wx_s = Float.toString(wx);
	}
	public void setWx(String wx) {
		try {
			float f = Float.parseFloat(wx);
			this.wx = f;
			this.wx_s = wx;
		} catch (Exception e) {
			//util.dprintln(4, "trytoGetParam(int) failed on param "+s);
		}
	}

	/** Getter for property wy.
	 * @return Value of property wy.
	 *
	 */
	public float getWy() {
		return wy;
	}

	/** Setter for property wy.
	 * @param wy New value of property wy.
	 *
	 */
	public void setWy(float wy) {
		this.wy = wy;
		wy_s = Float.toString(wy);
	}
	public void setWy(String wy) {
		try {
			float f = Float.parseFloat(wy);
			this.wy = f;
			this.wy_s = wy;
		} catch (Exception e) {
			//util.dprintln(4, "trytoGetParam(int) failed on param "+s);
		}
	}

	/** Getter for property mk.
	 * @return Value of property mk.
	 *
	 */
	public String getMk() {
		return (mk != null) ? mk : "0/0";
	}

	/** Setter for property mk.
	 * @param mk New value of property mk.
	 *
	 */
	public void setMk(String mk) {
		this.mk = mk;
	}

	/** Getter for property pt.
	 * @return Value of property pt.
	 *
	 */
	public int getPt() {
		return pt;
	}

	/** Setter for property pt.
	 * @param pt New value of property pt.
	 *
	 */
	public void setPt(int pt) {
		this.pt = pt;
		pt_s = Integer.toString(pt);
	}
	public void setPt(String pt) {
		try {
			int i = Integer.parseInt(pt);
			this.pt = i;
			this.pt_s = pt;
		} catch (Exception e) {
			//util.dprintln(4, "trytoGetParam(int) failed on param "+s);
		}
	}

	/** Returns the base URL (from http:// up to the base directory without file name or
	 * /servlet). Base URL has to be set from a request via setBaseURL or
	 * setWithRequest.
	 * @return String with the base URL.
	 */
	public String getBaseURL() {
		return (baseURL != null) ? baseURL : "";
	}

	/** Set the requests base URL parameter from a javax.sevlet.http.HttpServletRequest.
	 * @param request HttpServletRequest to set the base URL.
	 */
	public void setBaseURL(javax.servlet.http.HttpServletRequest request) {
		// calculate base URL string from request (minus last part)
		String s = request.getRequestURL().toString();
		int eop = s.lastIndexOf("/");
		if (eop > 0) {
			baseURL = s.substring(0, eop);
		} else {
			// fall back
			baseURL =
				"http://"
					+ request.getServerName()
					+ "/docuserver/digitallibrary";
		}
	}

	/**
	 * Returns the image.
	 * @return DocuImage
	 */
	public DocuImage getImage() {
		return image;
	}

	/**
	 * Sets the image.
	 * @param image The image to set
	 */
	public void setImage(DocuImage image) {
		this.image = image;
	}

	/**
	 * Returns the servletRequest.
	 * @return ServletRequest
	 */
	public ServletRequest getServletRequest() {
		return servletRequest;
	}

	/**
	 * Sets the servletRequest.
	 * @param servletRequest The servletRequest to set
	 */
	public void setServletRequest(ServletRequest servletRequest) {
		this.servletRequest = servletRequest;
	}

	/**
	 * Returns the rot.
	 * @return float
	 */
	public float getRot() {
		return rot;
	}

	/**
	 * Sets the rot.
	 * @param rot The rot to set
	 */
	public void setRot(float rot) {
		this.rot = rot;
		this.rot_s = Float.toString(rot);        // lugi - cleanup : war rot_s statt this.rot_s
	}
	public void setRot(String rot) {
		try {
			float f = Float.parseFloat(rot);
			this.rot = f;
			this.rot_s = rot;
		} catch (Exception e) {
			//util.dprintln(4, "trytoGetParam(int) failed on param "+s);
		}
	}

	/**
	 * Returns the rot.
	 * @return float
	 */
	public float getCont() {
		return cont;
	}

	/**
	 * Sets the rot.
	 * @param rot The rot to set
	 */
	public void setCont(float cont) {
		this.cont = cont;
		this.cont_s = Float.toString(cont);        // lugi - bugfix : war rot_s statt this.cont_s
	}
	public void setCont(String cont) {
		try {
			float f = Float.parseFloat(cont);
			this.cont = f;
			this.cont_s = cont;
		} catch (Exception e) {
			//util.dprintln(4, "trytoGetParam(int) failed on param "+s);
		}
	}

	/**
	 * Returns the brgt.
	 * @return float
	 */
	public float getBrgt() {
		return this.brgt;
	}

	/**
	 * Sets the brgt.
	 * @param brgt The brgt to set
	 */
	public void setBrgt(float brgt) {
		this.brgt = brgt;
		this.brgt_s = Float.toString(brgt);
	}
	public void setBrgt(String brgt) {
		try {
			float f = Float.parseFloat(brgt);
			this.brgt = f;
			this.brgt_s = brgt;
		} catch (Exception e) {
			//util.dprintln(4, "trytoGetParam(int) failed on param "+s);
		}
	}

	/**
	 * @return float[]
	 */
	public float[] getRgba() {
		return this.rgba;
	}

	/**
	 * @return float[]
	 */
	public float[] getRgbm() {
		return this.rgbm;
	}

// lugi - begin

	/** 
	 * @return string property Rgba.
	 */
	public String getRgba_s() {
    if (rgba_s != null) {
      return this.rgba_s;
    } else {
      return "";
    }
	}

	/** 
	 * @return string property Rgbm.
	 */
	public String getRgbm_s() {
		if (rgbm_s != null) {
		  return this.rgbm_s;
		} else {
		  return "";
		}
	}

// lugi - end

	/**
	 * Sets the rgba.
	 * @param rgba The rgba to set
	 */
	public void setRgba(float[] rgba) {
		this.rgba = rgba;
		this.rgba_s = rgba[0] + "/" + rgba[1] + "/" + rgba[2];      // lugi - bugfix : save string representation was missing
	}
	public void setRgba(String s) {
		try {
			String[] sa = s.split("/");
			float[] fa = new float[3];
			for (int i = 0; i < 3; i++) {
				float f = Float.parseFloat(sa[i]);
				fa[i] = f;
			}
			this.rgba_s = s;
			this.rgba = fa;
		} catch (Exception e) {
			//util.dprintln(4, "trytoGetParam(int) failed on param "+s);
		}
	}

	/**
	 * Sets the rgbm.
	 * @param rgbm The rgbm to set
	 */
	public void setRgbm(float[] rgbm) {
		this.rgbm = rgbm;
		this.rgbm_s = rgbm[0] + "/" + rgbm[1] + "/" + rgbm[2];      // lugi - bugfix : save string representation was missing
	}
	public void setRgbm(String s) {
		try {
			String[] sa = s.split("/");
			float[] fa = new float[3];
			for (int i = 0; i < 3; i++) {
				float f = Float.parseFloat(sa[i]);
				fa[i] = f;
			}
			this.rgbm_s = s;
			this.rgbm = fa;
		} catch (Exception e) {
			//util.dprintln(4, "trytoGetParam(int) failed on param "+s);
		}
	}

	public boolean isRDF(){
	  return boolRDF;
        }

}

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

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import digilib.image.DocuImage;
import digilib.io.FileOps;

/**
 * Class holding the parameters of a digilib user request. The parameters are
 * mostly named like the servlet parameters: <br>
 * request_path: url of the page/document. <br>
 * fn: url of the page/document. <br>
 * pn: page number. <br>
 * dw: width of result window in pixels. <br>
 * dh: height of result window in pixels. <br>
 * wx: left edge of image area (float from 0 to 1). <br>
 * wy: top edge of image area (float from 0 to 1). <br>
 * ww: width of image area(float from 0 to 1). <br>
 * wh: height of image area(float from 0 to 1). <br>
 * ws: scale factor. <br>
 * mo: special options like 'fit' for gifs. <br>
 * mk: list of marks. <br>
 * pt: total number of pages (generated by sevlet). <br>
 * baseURL: base URL (from http:// to below /servlet). <br>
 * ...et alii
 * 
 * @author casties
 * 
 */
public class DigilibRequest extends ParameterMap {

	private static final long serialVersionUID = -4707707539569977901L;

	private final static String ECHO = "http://echo.unibe.ch/digilib/rdf#";

	private final static String DIGILIB = "Digilib";

	private Logger logger = Logger.getLogger(this.getClass());

	private boolean boolRDF = false; // use RDF Parameters

	private DocuImage image; // internal DocuImage instance for this request

	private ServletRequest servletRequest; // internal ServletRequest

	/** Creates a new instance of DigilibRequest and sets default values. */
	public DigilibRequest() {
		// create HashMap(20)
		super(30);

		/*
		 * Definition of parameters and default values. Parameter of type 's'
		 * are for the servlet.
		 */

		// url of the page/document (second part)
		newParameter("fn", "", null, 's');
		// page number
		newParameter("pn", new Integer(1), null, 's');
		// width of client in pixels
		newParameter("dw", new Integer(0), null, 's');
		// height of client in pixels
		newParameter("dh", new Integer(0), null, 's');
		// left edge of image (float from 0 to 1)
		newParameter("wx", new Float(0), null, 's');
		// top edge in image (float from 0 to 1)
		newParameter("wy", new Float(0), null, 's');
		// width of image (float from 0 to 1)
		newParameter("ww", new Float(1), null, 's');
		// height of image (float from 0 to 1)
		newParameter("wh", new Float(1), null, 's');
		// scale factor
		newParameter("ws", new Float(1), null, 's');
		// special options like 'fit' for gifs
		newParameter("mo", "", null, 's');
		// rotation angle (degree)
		newParameter("rot", new Float(0), null, 's');
		// contrast enhancement factor
		newParameter("cont", new Float(0), null, 's');
		// brightness enhancement factor
		newParameter("brgt", new Float(0), null, 's');
		// color multiplicative factors
		newParameter("rgbm", "0/0/0", null, 's');
		// color additive factors
		newParameter("rgba", "0/0/0", null, 's');
		// display dpi resolution (total)
		newParameter("ddpi", new Float(0), null, 's');
		// display dpi X resolution
		newParameter("ddpix", new Float(0), null, 's');
		// display dpi Y resolution
		newParameter("ddpiy", new Float(0), null, 's');
		// scale factor for mo=ascale
		newParameter("scale", new Float(1), null, 's');

		/*
		 * Parameters of type 'i' are not exchanged between client and server,
		 * but are for the servlets or JSPs internal use.
		 */

		// url of the page/document (first part, may be empty)
		newParameter("request.path", "", null, 'i');
		// base URL (from http:// to below /servlet)
		newParameter("base.url", null, null, 'i');
		// DocuImage instance for this request
		newParameter("docu.image", image, null, 'i');
		image = null;
		// HttpServletRequest for this request
		newParameter("servlet.request", servletRequest, null, 'i');
		servletRequest = null;

		/*
		 * Parameters of type 'c' are for the clients use
		 */

		// "real" filename
		newParameter("img.fn", "", null, 'c');
		// image dpi x
		newParameter("img.dpix", new Integer(0), null, 'c');
		// image dpi y
		newParameter("img.dpiy", new Integer(0), null, 'c');
		// hires image size x
		newParameter("img.pix_x", new Integer(0), null, 'c');
		// hires image size y
		newParameter("img.pix_y", new Integer(0), null, 'c');
		// total number of pages
		newParameter("pt", new Integer(0), null, 'c');
		// display level of digilib (0 = just image, 1 = one HTML page
		// 2 = in frameset, 3 = XUL-'frameset'
		// 4 = XUL-Sidebar )
		newParameter("lv", new Integer(2), null, 'c');
		// marks
		newParameter("mk", "", null, 'c');

	}

	/**
	 * Creates a new instance of DigilibRequest with parameters from a
	 * ServletRequest. All undefined parameters are set to default values.
	 * 
	 * @param request
	 */
	public DigilibRequest(ServletRequest request) {
		this();
		setWithRequest(request);
	}

	/**
	 * Populate the request object with data from a ServletRequest.
	 * 
	 * 
	 * @param request
	 */
	public void setWithRequest(ServletRequest request) {
		servletRequest = request;
		// decide if it's old-style or new-style
		String qs = ((HttpServletRequest) request).getQueryString();
		if (qs != null) {
			if (qs.indexOf("&amp;") > -1) {
				// &amp; separator
				setWithParamString(qs, "&amp;");
			} else if (qs.indexOf(";") > -1) {
				// ; separator
				setWithParamString(qs, ";");
			} else if (request.getParameter("fn") != null) {
				// standard '&' parameters
				setWithParamRequest(request);
			} else {
				setWithOldString(qs);
			}
		}
		setValue("servlet.request", request);
		// add path from request
		setValue("request.path", ((HttpServletRequest) request).getPathInfo());
		// set the baseURL
		setBaseURL((HttpServletRequest) request);
	}

	/**
	 * Populate a request from a string in the old "++++" parameter form.
	 * 
	 * @param queryString
	 *            String with paramters in the old "+++" form.
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
				setValueFromString("fn", token);
				if (query.hasMoreTokens()) {
					query.nextToken();
				}
			}
		}
		// second parameter PN
		if (query.hasMoreTokens()) {
			token = query.nextToken();
			if (!token.equals("+")) {
				setValueFromString("pn", token);
				if (query.hasMoreTokens()) {
					query.nextToken();
				}
			}
		}
		// third parameter WS
		if (query.hasMoreTokens()) {
			token = query.nextToken();
			if (!token.equals("+")) {
				setValueFromString("ws", token);
				if (query.hasMoreTokens()) {
					query.nextToken();
				}
			}
		}
		// fourth parameter MO
		if (query.hasMoreTokens()) {
			token = query.nextToken();
			if (!token.equals("+")) {
				setValueFromString("mo", token);
				if (query.hasMoreTokens()) {
					query.nextToken();
				}
			}
		}
		// fifth parameter MK
		if (query.hasMoreTokens()) {
			token = query.nextToken();
			if (!token.equals("+")) {
				setValueFromString("mk", token);
				if (query.hasMoreTokens()) {
					query.nextToken();
				}
			}
		}
		// sixth parameter WX
		if (query.hasMoreTokens()) {
			token = query.nextToken();
			if (!token.equals("+")) {
				setValueFromString("wx", token);
				if (query.hasMoreTokens()) {
					query.nextToken();
				}
			}
		}
		// seventh parameter WY
		if (query.hasMoreTokens()) {
			token = query.nextToken();
			if (!token.equals("+")) {
				setValueFromString("wy", token);
				if (query.hasMoreTokens()) {
					query.nextToken();
				}
			}
		}
		// eigth parameter WW
		if (query.hasMoreTokens()) {
			token = query.nextToken();
			if (!token.equals("+")) {
				setValueFromString("ww", token);
				if (query.hasMoreTokens()) {
					query.nextToken();
				}
			}
		}
		// ninth parameter WH
		if (query.hasMoreTokens()) {
			token = query.nextToken();
			if (!token.equals("+")) {
				setValueFromString("wh", token);
				if (query.hasMoreTokens()) {
					query.nextToken();
				}
			}
		}
	}

	/**
	 * Return the request parameters as a String in the parameter form
	 * 'fn=/icons&amp;pn=1'. Empty (undefined) fields are not included.
	 * 
	 * @return String of request parameters in parameter form.
	 */
	public String getAsString() {
		return getAsString(0);
	}

	/**
	 * Return the request parameters of a given type type as a String in the
	 * parameter form 'fn=/icons&amp;pn=1'. Empty (undefined) fields are not
	 * included.
	 * 
	 * @return String of request parameters in parameter form.
	 */
	public String getAsString(int type) {
		StringBuffer s = new StringBuffer(50);
		// go through all values
		for (Iterator i = this.values().iterator(); i.hasNext();) {
			Parameter p = (Parameter) i.next();
			if ((type > 0) && (p.getType() != type)) {
				// skip the wrong types
				continue;
			}
			String name = p.getName();
			/*
			 * handling special cases
			 */
			// request_path adds to fn
			if (name.equals("fn")) {
				s.append("&fn=" + getAsString("request.path")
						+ getAsString("fn"));
				continue;
			}
			/*
			 * the rest is sent with its name
			 */
			// parameters that are not set or internal are not sent
			if ((!p.hasValue()) || (p.getType() == 'i')) {
				continue;
			}
			s.append("&" + name + "=" + p.getAsString());
		}
		// kill first "&"
		s.deleteCharAt(0);
		return s.toString();
	}

	/**
	 * Returns request parameters in old '++++' form.
	 * 
	 * @return String with parameters in old '++++' form.
	 */
	public String getAsOldString() {
		StringBuffer s = new StringBuffer(50);
		s.append(getAsString("request.path"));
		s.append(getAsString("fn"));
		s.append("+" + getAsString("pn"));
		s.append("+" + getAsString("ws"));
		s.append("+" + getAsString("mo"));
		s.append("+" + getAsString("mk"));
		s.append("+" + getAsString("wx"));
		s.append("+" + getAsString("wy"));
		s.append("+" + getAsString("ww"));
		s.append("+" + getAsString("wh"));
		return s.toString();
	}

	/**
	 * Set request parameters from javax.servlet.ServletRequest. Uses the
	 * Requests getParameter methods for 'fn=foo' style parameters.
	 * 
	 * @param request
	 *            ServletRequest to get parameters from.
	 */
	public void setWithParamRequest(ServletRequest request) {
		setValue("servlet.request", request);
		// go through all request parameters
		for (Enumeration i = request.getParameterNames(); i.hasMoreElements();) {
			String name = (String) i.nextElement();
			// is this a known parameter?
			if (this.containsKey(name)) {
				Parameter p = (Parameter) this.get(name);
				// internal parameters are not set
				if (p.getType() == 'i') {
					continue;
				}
				p.setValueFromString(request.getParameter(name));
				continue;
			}
			// unknown parameters are just added with type 'r'
			newParameter(name, null, request.getParameter(name), 'r');
		}
		// add path from request
		setValue("request.path", ((HttpServletRequest) request).getPathInfo());
	}

	/**
	 * Set request parameters from query string. Uses the separator string qs to
	 * get 'fn=foo' style parameters.
	 * 
	 * @param qs
	 *            query string
	 * @param sep
	 *            parameter-separator string
	 */
	public void setWithParamString(String qs, String sep) {
		// go through all request parameters
		String[] qa = qs.split(sep);
		for (int i = 0; i < qa.length; i++) {
			// split names and values on "="
			String[] nv = qa[i].split("=");
			try {
				String name = URLDecoder.decode(nv[0], "UTF-8");
				String val = URLDecoder.decode(nv[1], "UTF-8");
				// is this a known parameter?
				if (this.containsKey(name)) {
					Parameter p = (Parameter) this.get(name);
					// internal parameters are not set
					if (p.getType() == 'i') {
						continue;
					}
					p.setValueFromString(val);
					continue;
				}
				// unknown parameters are just added with type 'r'
				newParameter(name, null, val, 'r');
			} catch (UnsupportedEncodingException e) {
				// this shouldn't happen anyway
				e.printStackTrace();
			}
		}
	}

	/**
	 * Test if option string <code>opt</code> is set. Checks if the substring
	 * <code>opt</code> is contained in the options string <code>param</code>.
	 * 
	 * @param opt
	 *            Option string to be tested.
	 * @return boolean
	 */
	public boolean hasOption(String param, String opt) {
		String s = getAsString(param);
		if (s != null) {
			StringTokenizer i = new StringTokenizer(s, ",");
			while (i.hasMoreTokens()) {
				if (i.nextToken().equals(opt)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * The image file path to be accessed.
	 * 
	 * The mage file path is assembled from the servlets RequestPath and
	 * Parameter fn and normalized.
	 * 
	 * @return String the effective filepath.
	 */
	public String getFilePath() {
		String s = getAsString("request.path");
		s += getAsString("fn");
		return FileOps.normalName(s);
	}

	/* Property getter and setter */

	/**
	 * Set the requests base URL parameter from a
	 * javax.sevlet.http.HttpServletRequest.
	 * 
	 * @param request
	 *            HttpServletRequest to set the base URL.
	 */
	public void setBaseURL(javax.servlet.http.HttpServletRequest request) {
		String baseURL = null;
		// calculate base URL string from request (minus last part)
		String s = request.getRequestURL().toString();
		int eop = s.lastIndexOf("/");
		if (eop > 0) {
			baseURL = s.substring(0, eop);
		} else {
			// fall back
			baseURL = "http://" + request.getServerName()
					+ "/docuserver/digitallibrary";
		}
		setValue("base.url", baseURL);
	}

	/**
	 * Returns the image.
	 * 
	 * @return DocuImage
	 */
	public DocuImage getImage() {
		return image;
	}

	/**
	 * Sets the image.
	 * 
	 * @param image
	 *            The image to set
	 */
	public void setImage(DocuImage image) {
		this.image = image;
		setValue("docu.image", image);
	}

	public boolean isRDF() {
		return boolRDF;
	}

	/**
	 * @return
	 */
	public ServletRequest getServletRequest() {
		return servletRequest;
	}

}

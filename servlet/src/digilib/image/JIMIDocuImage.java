/* JAIDocuImage -- Image class implementation using JIMI toolkit

  Digital Image Library servlet components

  Copyright (C) 2001, 2002 Robert Casties (robcast@mail.berlios.de)

  This program is free software; you can redistribute  it and/or modify it
  under  the terms of  the GNU General  Public License as published by the
  Free Software Foundation;  either version 2 of the  License, or (at your
  option) any later version.
   
  Please read license.txt for the full details. A copy of the GPL
  may be found at http://www.gnu.org/copyleft/lgpl.html

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

*/

package digilib.image;

import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.OutputStream;

import com.sun.jimi.core.Jimi;
import com.sun.jimi.core.JimiException;
import com.sun.jimi.core.filters.AreaAverageScaleFilter;
import com.sun.jimi.core.filters.ReplicatingScaleFilter;
import com.sun.jimi.core.raster.JimiRasterImage;

import digilib.Utils;
import digilib.io.FileOpException;

/** Implementation of DocuImage using the JIMI image Library. */
public class JIMIDocuImage extends DocuImageImpl {

	private JimiRasterImage img;
	private ImageProducer imgp;
	private int imgWidth = 0;
	private int imgHeight = 0;

	public JIMIDocuImage() {
	}

	public JIMIDocuImage(Utils u) {
		util = u;
	}

	/**
	 *  load image file
	 */
	public void loadImage(File f) throws FileOpException {
		System.gc();
		try {
			img = Jimi.getRasterImage(f.toURL());
		} catch (java.net.MalformedURLException e) {
			util.dprintln(3, "ERROR(loadImage): MalformedURLException");
		} catch (JimiException e) {
			util.dprintln(3, "ERROR(loadImage): JIMIException");
			throw new FileOpException("Unable to load File!" + e);
		}
		if (img == null) {
			util.dprintln(3, "ERROR(loadImage): unable to load file");
			throw new FileOpException("Unable to load File!");
		}
		imgp = img.getImageProducer();
		imgWidth = img.getWidth();
		imgHeight = img.getHeight();
	}

	/**
	 *  write image of type mt to Stream
	 */
	public void writeImage(String mt, OutputStream ostream)
		throws FileOpException {
		try {
			// render output
			Jimi.putImage(mt, imgp, ostream);

		} catch (JimiException e) {
			throw new FileOpException("Error writing image!" + e);
		}
	}

	public int getWidth() {
		return imgWidth;
	}

	public int getHeight() {
		return imgHeight;
	}

	public void scale(double scale) throws ImageOpException {

		ImageFilter scaleFilter;
		int destWidth = (int) (scale * (float) imgWidth);
		int destHeight = (int) (scale * (float) imgHeight);

		// setup scale and interpolation quality
		if (quality > 0) {
			util.dprintln(4, "quality q1");
			scaleFilter = new AreaAverageScaleFilter(destWidth, destHeight);
		} else {
			util.dprintln(4, "quality q0");
			scaleFilter = new ReplicatingScaleFilter(destWidth, destHeight);
		}

		ImageProducer scaledImg = new FilteredImageSource(imgp, scaleFilter);

		if (scaledImg == null) {
			util.dprintln(2, "ERROR(cropAndScale): error in scale");
			throw new ImageOpException("Unable to scale");
		}

		imgp = scaledImg;
		imgWidth = destWidth;
		imgHeight = destHeight;
	}

	public void crop(int x_off, int y_off, int width, int height)
		throws ImageOpException {
		// setup Crop
		ImageProducer croppedImg =
			img.getCroppedImageProducer(x_off, y_off, width, height);
		//util.dprintln(3, "CROP:"+croppedImg.getWidth()+"x"+croppedImg.getHeight()); //DEBUG

		if (croppedImg == null) {
			util.dprintln(2, "ERROR(cropAndScale): error in crop");
			throw new ImageOpException("Unable to crop");
		}
		imgp = croppedImg;
		imgWidth = width;
		imgHeight = height;
	}

}

/* ImageFile.java -- digilib image file class.

  Digital Image Library servlet components

  Copyright (C) 2003 Robert Casties (robcast@mail.berlios.de)

  This program is free software; you can redistribute  it and/or modify it
  under  the terms of  the GNU General  Public License as published by the
  Free Software Foundation;  either version 2 of the  License, or (at your
  option) any later version.
   
  Please read license.txt for the full details. A copy of the GPL
  may be found at http://www.gnu.org/copyleft/lgpl.html

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 * Created on 25.02.2003
 */
 
package digilib.io;

import java.io.File;

import digilib.image.ImageSize;

/**
 * @author casties
 */
public class ImageFile extends DocuDirent implements ImageInput {
	
	// file name
	private String filename = null;
	// parent ImageSet
	private ImageSet parent = null;
	// parent directory
	private Directory dir = null;
    // mime file type
    protected String mimetype = null;
    // image size in pixels
    protected ImageSize pixelSize = null;


	public ImageFile(String fn, ImageSet parent, Directory dir) {
		this.filename = fn;
		this.parent = parent;
		this.dir = dir;
	}
	
	public ImageFile(String fn) {
		File f = new File(fn);
		this.dir = new Directory(f.getParentFile());
		this.filename = f.getName();
	}
	
	
	/** Returns the file name (without path).
	 * 
	 * @return
	 */
	public String getName() {
		return filename;
	}


	/**
	 * @return File
	 */
	public File getFile() {
		if (dir == null) {
			return null;
		}
		File f = new File(dir.getDir(), filename);
		return f;
	}

	/* (non-Javadoc)
	 * @see digilib.io.ImageInput#setSize(digilib.image.ImageSize)
	 */
	public void setSize(ImageSize imageSize) {
		this.pixelSize = imageSize;
		// pass on to parent
		if (this.parent != null) {
			this.parent.setAspect(imageSize);
		}
	}
    /* (non-Javadoc)
     * @see digilib.io.ImageInput#getSize()
     */
    public ImageSize getSize() {
        return pixelSize;
    }

    /* (non-Javadoc)
     * @see digilib.io.ImageInput#getMimetype()
     */
    public String getMimetype() {
        return mimetype;
    }

    /* (non-Javadoc)
     * @see digilib.io.ImageInput#setMimetype(java.lang.String)
     */
    public void setMimetype(String filetype) {
        this.mimetype = filetype;
    }

    /* (non-Javadoc)
     * @see digilib.io.ImageInput#isChecked()
     */
    public boolean isChecked() {
        return (pixelSize != null);
    }
    
    /* (non-Javadoc)
     * @see digilib.io.ImageInput#getAspect()
     */
    public float getAspect() {
        return (pixelSize != null) ? pixelSize.getAspect() : 0;
    }

    @Override
    public ImageSet getParentSet() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setParentSet(ImageSet parent) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void checkMeta() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public File getInput() {
        // TODO Auto-generated method stub
        return null;
    }

}

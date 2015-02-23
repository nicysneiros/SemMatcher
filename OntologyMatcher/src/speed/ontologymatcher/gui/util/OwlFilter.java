package speed.ontologymatcher.gui.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class OwlFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		boolean accept = false;
		if(f != null)
		{
			String path = f.getAbsolutePath();
			int lastindex = path.lastIndexOf(".");
			
			if(lastindex > 0)
			{
				String extension = path.substring(lastindex, path.length());
				
				if(extension.equalsIgnoreCase(".owl") ||
						extension.equalsIgnoreCase(".rdf") ||
						extension.equalsIgnoreCase(".rdfs"))
				{
					accept = true;				
				}
			}
			else
			{
				//Pastas
				accept = true;
			}
		}
		return accept;
	}

	@Override
	public String getDescription() {		
		return "Ontology files (owl, rdf, rdfs)";
	}

}

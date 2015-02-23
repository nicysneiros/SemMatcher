package speed.ontologymatcher.util;

import speed.ontologymatcher.lematching.enums.EMappingCardinality;
import mapping.MappingManager;
import mapping.MappingType;

/**
 * Classe auxiliar para geração de arquivos do HMatch.
 * @author Thiago Pachêco Andrade Pereira
 *
 */
public class FileHandler {

	public static void createMatchingFile(String fileOut, EMappingCardinality mappingType,
			Integer id)
	{		
		MappingManager mm = MappingManager.getInstance();
		mm.createRDFAlignment(id.intValue(), fileOut, MappingType.stringToType(mappingType.toString()));		
	}
}

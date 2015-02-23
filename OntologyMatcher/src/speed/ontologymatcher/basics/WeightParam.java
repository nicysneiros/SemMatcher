package speed.ontologymatcher.basics;

/**
 * Enum para listar os tipos de pesos que podem ser configurados na ferramenta
 * @author nicolle
 *
 */
public enum WeightParam {
	LE_MATCH,
	SEMANTIC_MATCH,
	EQUIVALENT,
	SUB_CONCEPT,
	SUPER_CONCEPT,
	PART_OF,
	WHOLE_OF,
	CLOSE_TO,
	DISJOINT;
}

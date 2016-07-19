package io.gromit.geonames.export;

/**
 * The Interface Transformer.
 */
public interface Transformer {

	/**
	 * Transform.
	 *
	 * @param row the row
	 */
	void transform(String[] row);
	
}

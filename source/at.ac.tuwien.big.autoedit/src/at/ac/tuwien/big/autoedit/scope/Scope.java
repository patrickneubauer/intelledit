package at.ac.tuwien.big.autoedit.scope;

import java.util.Iterator;
import java.util.Map.Entry;

import at.tuwien.big.virtmod.datatype.IteratorUtils;

/**A scope contains a set of solutions*/
public interface Scope<Solution,Quality extends Comparable<Quality>> extends Iterable<Solution> {

	
	public boolean contains(Solution sol);
	
	public Quality getQuality(Solution sol);
	
	/**Should give solutions in decreating quality if possible*/
	public Iterator<Solution> iterator();
	
	/**Should give solutions in decreating quality if possible*/
	public default Iterator<ComparablePair<Solution,Quality>> qualityIterate() {
		return IteratorUtils.convert(iterator(), (x)->new ComparablePairImpl<>(x,getQuality(x)));
	}
	
	public boolean isFinite();
	
	/**Should not be deterministic and infinite, favor good solutions*/
	public Iterator<Solution> sample();
	
	
	
}

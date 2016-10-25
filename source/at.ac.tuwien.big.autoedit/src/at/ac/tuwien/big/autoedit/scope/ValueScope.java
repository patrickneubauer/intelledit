package at.ac.tuwien.big.autoedit.scope;

import java.net.Socket;
import java.util.Iterator;

import at.ac.tuwien.big.autoedit.transfer.EcoreTransferFunction;

/*A value-scope has solutions which are single values to be placed somewhere*/
public interface ValueScope<Solution,Quality extends Comparable<Quality>> extends Scope<Solution,Quality> {

	default Solution sampled() {
		Iterator<Solution> iter = sample();
		if (iter.hasNext()) {
			return sample().next();
		}
		return null;
	}

	void transfer(EcoreTransferFunction transferFunc);
}

package at.ac.tuwien.big.autoedit.change.primitive;

import java.util.Iterator;

import at.ac.tuwien.big.autoedit.scope.ValueScope;
import at.ac.tuwien.big.autoedit.transfer.EcoreTransferFunction;

public class LogLongChangeScope implements ValueScope<Long, Boolean> {
	private LogLongScope base = LogLongScope.INSTANCE;
	
	private long curValue;
	
	public LogLongChangeScope(long curValue)  {
		this.curValue = curValue;
	}

	@Override
	public boolean contains(Long sol) {
		return true;
	}

	@Override
	public Boolean getQuality(Long sol) {
		return true;
	}

	private Iterator<Long> addIter(Iterator<Long> base) {
		return new Iterator<Long>() {

			@Override
			public boolean hasNext() {
				return base.hasNext();
			}

			@Override
			public Long next() {
				return curValue+base.next();
			}
			
		};
	}
	
	@Override
	public Iterator<Long> iterator() {
		return addIter(base.iterator());
	}

	@Override
	public boolean isFinite() {
		return false;
	}

	@Override
	public Iterator<Long> sample() {
		return addIter(base.sample());
	}
	
	@Override
	public void transfer(EcoreTransferFunction transferFunc) {
	}

}

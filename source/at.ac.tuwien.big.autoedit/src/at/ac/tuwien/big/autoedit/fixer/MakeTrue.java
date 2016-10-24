package at.ac.tuwien.big.autoedit.fixer;

import at.ac.tuwien.big.autoedit.fixer.impl.MakeTrueImpl;

public interface MakeTrue extends FixAttempt {

	FixAttempt INSTANCE = MakeTrueImpl.INSTANCE;


	@Override
	public default boolean isFulfilled(Object obj) {
		return (obj instanceof Boolean) && ((Boolean)obj);
	}
}
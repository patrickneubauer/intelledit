package at.ac.tuwien.big.autoedit.oclvisit.fixinggenerators;

import java.util.Collection;

import org.eclipse.ocl.expressions.OperationCallExp;

import at.ac.tuwien.big.autoedit.fixer.Decrease;
import at.ac.tuwien.big.autoedit.fixer.FixAttempt;
import at.ac.tuwien.big.autoedit.fixer.Increase;
import at.ac.tuwien.big.autoedit.fixer.MakeDifferent;
import at.ac.tuwien.big.autoedit.fixer.MakeEqual;
import at.ac.tuwien.big.autoedit.fixer.impl.SetAddAnyImpl;
import at.ac.tuwien.big.autoedit.fixer.impl.SetRemoveImpl;
import at.ac.tuwien.big.autoedit.oclvisit.AbstractSelectiveEvaluator;
import at.ac.tuwien.big.autoedit.oclvisit.EvalResult;
import at.ac.tuwien.big.autoedit.oclvisit.ExpressionResult;
import at.ac.tuwien.big.autoedit.oclvisit.FixingGenerator;

public class NumberToSetPropagation extends AbstractSelectiveEvaluator<OperationCallExp, Number> implements FixingGenerator<OperationCallExp, Number> {

	public NumberToSetPropagation() {
		super(OperationCallExp.class, Number.class, true, "size");
	}
	
	public static NumberToSetPropagation INSTANCE = new NumberToSetPropagation();

	@Override
	public boolean addFixingPossibilitiesLocal(FixAttempt singleAttemptForThis, ExpressionResult res, OperationCallExp expr,
			Number result, Collection<FixAttempt>[] fixAttemptsPerSub) {
		if (result == null) {
			return false;
		}
		
		boolean shouldIncrease = false;
		boolean shouldDecrease = false;
		if (singleAttemptForThis instanceof Increase) {
			shouldIncrease = true;
			shouldDecrease = false;
		} else if (singleAttemptForThis instanceof Decrease) {
			shouldIncrease = false;
			shouldDecrease = true;
		} else if (singleAttemptForThis instanceof MakeEqual) {
			MakeEqual eq = (MakeEqual)singleAttemptForThis;
			Object border = eq.border();
			if (border instanceof Number) {
				Number bord = (Number)border;
				double left = result.doubleValue();
				double right = bord.doubleValue();
				if (left < right) {
					shouldDecrease = false;
					shouldIncrease = true;
				} else if (left > right) {
					shouldDecrease = true;
					shouldIncrease = false;
				} else {
					//Nothing to do
					return true;
				}
			} else {
				return false;
			}
		} else if (singleAttemptForThis instanceof MakeDifferent) {
			MakeEqual eq = (MakeEqual)singleAttemptForThis;
			Object border = eq.border();
			if (border instanceof Number) {
				Number bord = (Number)border;
				double left = result.doubleValue();
				double right = bord.doubleValue();
				if (left == right) {
					shouldDecrease = true;
					shouldIncrease = true;
				} else {
					//Nothing to do
					return true;
				}
			}
		} else {
			//Don't know what I should do
			return false;
		}
		
		EvalResult first = res.getSubResults().get(0);
		if (first == null) {
			return false;
		}
		Object subresult = first.getResult();
		
		if (!(subresult instanceof Collection)) {
			System.err.println("Strangely size is called on a non-set: "+result);
			return false;
		}
		Collection subCol = (Collection)subresult;
		if (shouldDecrease) {
			//Delete elements from the set			
			for (Object obj: subCol) {
				first.addPossibleFix(new SetRemoveImpl(obj));
			}
		}
		
		if (shouldIncrease) {
			//Add elements to the set, but which ones?
			first.addPossibleFix(new SetAddAnyImpl(result.intValue()));
		}
		
		return true;
	}

}
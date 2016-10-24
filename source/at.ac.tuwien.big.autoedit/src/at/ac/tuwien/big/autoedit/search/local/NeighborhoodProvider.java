package at.ac.tuwien.big.autoedit.search.local;

import java.util.Iterator;
import java.util.List;

import at.ac.tuwien.big.autoedit.change.Change;
import at.ac.tuwien.big.autoedit.change.ChangeType;
import at.ac.tuwien.big.autoedit.oclvisit.EvalResult;
import at.ac.tuwien.big.autoedit.search.local.impl.LocalSearchPartialSolution;
import at.tuwien.big.virtmod.datatype.IteratorUtils;
import at.tuwien.big.virtmod.datatype.IteratorUtils.SimpleFunction;

public interface NeighborhoodProvider {

	
	static NeighborhoodProvider DEFAULT_DIRECTFIX = new NeighborhoodProvider() {
		
		@Override
		public Iterable<? extends Change<?>> getNeighbours(LocalSearchPartialSolution x) {
			Iterator<ChangeType<?,? extends Change<?>>> ctIter = x.getDirectFixes().iterator();
			SimpleFunction<ChangeType<?,? extends Change<?>>, Iterator<? extends Change<?>>> iterateFunc = new SimpleFunction<ChangeType<?,? extends Change<?>>,  Iterator<? extends Change<?>>>() {

				@Override
				public Iterator<? extends Change<?>> applyTo(ChangeType<?, ? extends Change<?>> x) {
					return x.iterateWithMissing();
				}
			};
			return ()->IteratorUtils.<ChangeType<?,?>,Change<?>>balancedIterate(x.getDirectFixes().iterator(),iterateFunc);
		}
		
		@Override
		public List<ChangeType<?,? extends Change<?>>> getBaseFixes(EvalResult res) {
			return res.getAllDirectFixingActions();
		}
	};
	
	static NeighborhoodProvider DEFAULT_LOCALSEARCH = new NeighborhoodProvider() {
		
		@Override
		public Iterable<? extends Change<?>> getNeighbours(LocalSearchPartialSolution x) {
			Iterator<Change<?>> iter = IteratorUtils.<ChangeType<?,?>,Change<?>>balancedIterate(x.getDirectFixes().iterator(), (y)->y.sampleWithMissing());
			return ()->(Iterator)iter;
		}
		
		@Override
		public List<ChangeType<?,? extends Change<?>>> getBaseFixes(EvalResult res) {
			return res.getAllFixingActions();
		}
	};
	

	public Iterable<? extends Change<?>> getNeighbours(LocalSearchPartialSolution curSol);
	
	public List<ChangeType<?,? extends Change<?>>> getBaseFixes(EvalResult res);
}
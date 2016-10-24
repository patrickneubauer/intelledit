package at.ac.tuwien.big.autoedit.change.basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;

import at.ac.tuwien.big.autoedit.change.BasicChange;
import at.ac.tuwien.big.autoedit.change.Change;
import at.ac.tuwien.big.autoedit.change.CostProvider;
import at.ac.tuwien.big.autoedit.change.EObjectChangeMap;
import at.ac.tuwien.big.autoedit.change.Undoer;
import at.ac.tuwien.big.autoedit.ecore.util.MyEcoreUtil;
import at.ac.tuwien.big.autoedit.oclvisit.FixAttemptFeatureReferenceImpl;
import at.ac.tuwien.big.autoedit.oclvisit.FixAttemptReference;
import at.ac.tuwien.big.autoedit.transfer.EcoreTransferFunction;

public class BasicDeleteConstantChange extends AbstractFeatureChange<BasicDeleteConstantChange> implements FeatureChange<BasicDeleteConstantChange> {

	private Object value;
	
	public BasicDeleteConstantChange(Resource res, EObject toObj, EStructuralFeature feat, Object value) {
		super(toObj,feat,res);
		this.value = value;
		if (value == null) {
			System.out.println("Deleting null ...");
		}
	}

	@Override
	public BasicDeleteConstantChange clone() {
		return new BasicDeleteConstantChange(forResource(),forObject(), forFeature(), value);
	}

	
	@Override
	public void transfer(EcoreTransferFunction func) {
		super.transfer(func);
		Object oldValue = value;
		value = func.transfer(value);
		if (value == null && oldValue != null) {
			System.out.println("Value now null!! from "+oldValue);
		}
	}
	
	private double costs = 0.0;
	
	@Override
	public Undoer execute() {
		if (forObject() == null) {
			return ()->{};
		}
		costs = 0.0;
		if (forFeature() instanceof EReference) {
			EReference ref = (EReference)forFeature();
			if (ref.isContainment()) {
				//Delete instead
				List<Undoer> allUndoers = new ArrayList<Undoer>();
				Collection col = new ArrayList<>(MyEcoreUtil.getAsCollection(forObject(), ref));
				for (Object o: col) {
					if (o == null) {continue;}
					DeleteObjectChange doc = new DeleteObjectChange((EObject)o,forResource());
					
					allUndoers.add(doc.execute());
					costs+= doc.getCosts();
				}
				return ()->{
					for (int i = allUndoers.size()-1; i >= 0; --i) {
						allUndoers.get(i).undo();
					}
				};
			}
			
		}
		
		costs = costProvider().getFunction(value).getCosts(value, null);
		int index = MyEcoreUtil.removeValueGetIndex(forObject(), forFeature(), value);
		return ()->{
			if (index != -1) {
				MyEcoreUtil.addValue(forObject(), forFeature(), index, value);
			}
		};
	}

	@Override
	protected String getSimpleName() {
		return "Delete from ";
	}

	@Override
	protected String getAdditionalValueName() {
		return ""+value;
	}

	public int hashCode() {
		return super.hashCode()+Objects.hashCode(value);
	}
	

	@Override
	public boolean equals(BasicDeleteConstantChange o) {
		return Objects.equals(value, o.value); 
	}


	@Override
	public void addFixReferencesLocal(Collection<FixAttemptReference> refs) {
		refs.add(new FixAttemptFeatureReferenceImpl(forObject(), forFeature(),
				-1, value));
	}
	

	@Override
	public void normalizeMap(EObjectChangeMap map) {
		List<BasicChange<?>> baseChanges = map.getFeatureChanges(forObject(), forFeature());
		for (BasicChange<?> bc: baseChanges) {
			if (bc instanceof BasicAddConstantChange) {
				BasicAddConstantChange bac = (BasicAddConstantChange)bc;
				Object obj = bac.getValue();
				if(Objects.equals(obj, bac.getValue())) {
					//Do nothing, it might be risky, but maybe you could delete it
				}
			}
		}
		baseChanges.add(this);
	}
	


	@Override
	public double getCosts() {
		return costs;
	}
	

	@Override
	public boolean isIdentity() {
		return !MyEcoreUtil.getAsCollection(forObject(), forFeature()).contains(value);
	}
	
}
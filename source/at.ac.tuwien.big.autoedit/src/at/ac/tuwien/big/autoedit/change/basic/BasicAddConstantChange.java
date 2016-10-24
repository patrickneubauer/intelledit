package at.ac.tuwien.big.autoedit.change.basic;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;

import at.ac.tuwien.big.autoedit.change.Change;
import at.ac.tuwien.big.autoedit.change.CostProvider;
import at.ac.tuwien.big.autoedit.change.EObjectChangeMap;
import at.ac.tuwien.big.autoedit.change.Undoer;
import at.ac.tuwien.big.autoedit.ecore.util.MyEcoreUtil;
import at.ac.tuwien.big.autoedit.ecore.util.MyResource;
import at.ac.tuwien.big.autoedit.oclvisit.FixAttemptFeatureReferenceImpl;
import at.ac.tuwien.big.autoedit.oclvisit.FixAttemptReference;
import at.ac.tuwien.big.autoedit.transfer.EcoreTransferFunction;

public class BasicAddConstantChange extends AbstractFeatureChange<BasicAddConstantChange> implements FeatureChange<BasicAddConstantChange> {

	private Object value;
	private int index;
	
	public BasicAddConstantChange(Resource res, EObject toObj, EStructuralFeature feat, int index, Object value) {
		super(toObj,feat,res);
		this.value = value;
		this.index = index;
	}
	
	public BasicAddConstantChange(Resource res, EObject toObj, EStructuralFeature feat, Object value) {
		this(res,toObj,feat,-1,value);
	}
	
	public Object getValue() {
		return value;
	}

	@Override
	public BasicAddConstantChange clone() {
		return new BasicAddConstantChange(forResource(), forObject(), forFeature(), index, value);
	}

	
	@Override
	public void transfer(EcoreTransferFunction func) {
		super.transfer(func);
		value = func.transfer(value);
	}
	
	private double costs;
	
	@Override
	public Undoer execute() {
		try {
		costs = 0.0;
		if (forObject() == null || value == null) {
			return ()->{};
		}
		
		boolean needSetVal = false;
		Object oldTargetVal = null;
		int prevIndex = -1;
		EObject prevContainer = null;
		EStructuralFeature contFeat = null;
		int prevContIndex = -1;
		int prevResourceIndex = -1;
		if (forFeature() instanceof EReference) {
			EReference ref = (EReference)forFeature();
			if (ref.isContainment()) {
				if (value instanceof EObject) {
					EObject target = (EObject)value;
					prevContainer = target.eContainer();
					contFeat = target.eContainingFeature();
					if (prevContainer != null) {
						prevContIndex = ((List)MyEcoreUtil.getAsCollection(prevContainer,contFeat)).indexOf(target);
					}
					prevResourceIndex = target.eResource()==null?-1:target.eResource().getContents().indexOf(target);
					if (prevContainer == forObject() && contFeat == forFeature()) {
						return ()->{};
					}
				}
				 
			}			
			if (ref.getEOpposite() != null && value instanceof EObject) {
				EObject target = (EObject)value;
				if (!ref.getEOpposite().isMany()) {
					//You need to store the old Value
					oldTargetVal = target.eGet(ref.getEOpposite());
					needSetVal = true;
				} else {
					prevIndex = ((List)MyEcoreUtil.getAsCollection(target, ref.getEOpposite())).indexOf(forObject());
				}
			}
		} 
		int prevIndexF = prevIndex;
		boolean needSetValF = needSetVal;
		Object oldTargetValF = oldTargetVal;
		int prevContIndexF = prevContIndex;
		int prevResourceIndexF = prevResourceIndex;
		EObject prevContainerF = prevContainer;
		EStructuralFeature contFeatF = contFeat;
		costs = costProvider().getFunction(value).getCosts(null, value);
		if (needSetValF) {
			costs+= costProvider().getFunction(oldTargetVal).getCosts(oldTargetValF, forObject());
		}
		if (index == -1) {
			int ind = MyEcoreUtil.getAsCollectionSize(forObject(), forFeature());
			if (MyEcoreUtil.addValue(forObject(), forFeature(), value)) {
				return ()->{
					if (prevContIndexF != -1) {
						MyEcoreUtil.addValue(prevContainerF, contFeatF, prevContIndexF, (EObject)value );
					} else {
						MyEcoreUtil.removeValueAtIndex(forObject(), forFeature(), ind);
					}
					if (needSetValF) {
						((EObject)value).eSet(((EReference)forFeature()).getEOpposite(), oldTargetValF);
					} else if (prevIndexF != -1) {
						MyEcoreUtil.addValue((EObject)value, ((EReference)forFeature()).getEOpposite(), prevIndexF, forObject() );
					}
				};
			} else {
				return ()->{};
			}
		} else {
			if (forFeature().isUnique() && MyEcoreUtil.getAsCollection(forObject(), forFeature()).contains(value)) {
				return ()->{};
			} else {
				MyEcoreUtil.addValue(forObject(), forFeature(), index, value);
				return ()->{
					if (prevContIndexF != -1) {
						MyEcoreUtil.addValue(prevContainerF, contFeatF, prevContIndexF, (EObject)value );
					} else {
						MyEcoreUtil.removeValueAtIndex(forObject(), forFeature(), index);
					}
					if (needSetValF) {
						((EObject)value).eSet(((EReference)forFeature()).getEOpposite(), oldTargetValF);
					}
				};
			}
			
		}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return ()->{};
		}
	}
 	

	@Override
	protected String getSimpleName() {
		return "Add to ";
	}

	@Override
	protected String getAdditionalValueName() {
		return ""+MyEcoreUtil.getEObjName(value);
	}
	

	public int hashCode() {
		return super.hashCode()+Objects.hashCode(value);
	}
	

	@Override
	public boolean equals(BasicAddConstantChange o) {
		return 	Objects.equals(value, o.value); 
	}
	

	@Override
	public void addFixReferencesLocal(Collection<FixAttemptReference> refs) {
		refs.add(new FixAttemptFeatureReferenceImpl(forObject(), forFeature(),
				-1, value));
	}

	@Override
	public void normalizeMap(EObjectChangeMap map) {
		map.getFeatureChanges(forObject(), forFeature()).add(this);
	}
	

	@Override
	public double getCosts() {
		return costs;
	}
	

	@Override
	public boolean isIdentity() {
		return value == null || (forFeature().isUnique() && MyEcoreUtil.getAsCollection(forObject(), forFeature()).contains(value));
	}
	
}

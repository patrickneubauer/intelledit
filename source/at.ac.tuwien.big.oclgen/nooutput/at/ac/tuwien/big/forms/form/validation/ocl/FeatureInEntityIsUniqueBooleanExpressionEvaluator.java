package at.ac.tuwien.big.forms.form.validation.ocl;

import java.util.Map;
import java.util.Set;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import at.ac.tuwien.big.forms.Entity;

public class FeatureInEntityIsUniqueBooleanExpressionEvaluator implements at.ac.tuwien.big.oclgen.OCLBooleanExpressionEvaluator<Entity> {
	
	public static final at.ac.tuwien.big.oclgen.OCLBooleanExpressionEvaluator<Entity> INSTANCE = new FeatureInEntityIsUniqueBooleanExpressionEvaluator();
	
	@Override
	public boolean isValid(Entity self) {
		final EPackage ePackage = self.eClass().getEPackage();
		final org.eclipse.ocl.EvaluationEnvironment evalEnv = org.eclipse.ocl.ecore.EcoreEnvironmentFactory.INSTANCE.createEvaluationEnvironment();
		final Map<EClass, Set<EObject>> extents = new org.eclipse.ocl.LazyExtentMap<EClass, EObject>((EObject) self) {
		
			// implements the inherited specification
			@Override
			protected boolean isInstance(EClass cls, EObject element) {
				return cls.isInstance(element);
			}
		};
final java.util.Collection<at.ac.tuwien.big.forms.Feature> var0 = self.getFeatures();
final java.lang.Boolean var1 = at.ac.tuwien.big.oclgen.OCL2JavaSupport.forAll(var0, 2, var4 -> {
final at.ac.tuwien.big.forms.Feature var2 = var4.get(0);
final at.ac.tuwien.big.forms.Feature var3 = var4.get(1);
final java.lang.Boolean var5 = !at.ac.tuwien.big.oclgen.OCL2JavaSupport.equals(var2, var3);
final java.lang.String var6 = var2.getName();
final java.lang.String var7 = var3.getName();
final java.lang.Boolean var8 = !at.ac.tuwien.big.oclgen.OCL2JavaSupport.equals(var6, var7);
final boolean var9 = !var5 || var8;
return var9;
});
return var1;
	}
	
	@Override
	public EStructuralFeature findErrorFeature(Entity self) {
		return self.eClass().getEStructuralFeature("features");
	}
	
}
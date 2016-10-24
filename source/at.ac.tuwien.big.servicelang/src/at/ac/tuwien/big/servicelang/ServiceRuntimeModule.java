/*
 * generated by Xtext
 */
package at.ac.tuwien.big.servicelang;

import org.eclipse.emf.ecore.EValidator;

import com.google.inject.Binder;

import at.ac.tuwien.big.oclgen.CustomRegistry;

/**
 * Use this class to register components to be used at runtime / without the Equinox extension registry.
 */
public class ServiceRuntimeModule extends at.ac.tuwien.big.servicelang.AbstractServiceRuntimeModule {

	

	@Override
	public void configure(Binder binder) {
		super.configure(binder);
		CustomRegistry.INSTANCE.registerCustom("http://www.example.org/serviceexample", "at.ac.tuwien.big.servicelang.");
		binder.requestInjection(CustomRegistry.INSTANCE);
		binder.bindListener(CustomRegistry.INSTANCE, CustomRegistry.INSTANCE);
	}

	@Override
	public EValidator.Registry bindEValidatorRegistry() {
		return CustomRegistry.INSTANCE;
	}

}

/*
 * generated by Xtext
 */
package at.ac.tuwien.big.servicelang.ui;

import org.eclipse.xtext.ui.guice.AbstractGuiceAwareExecutableExtensionFactory;
import org.osgi.framework.Bundle;

import com.google.inject.Injector;

import at.ac.tuwien.big.servicelang.ui.internal.ServiceActivator;

/**
 * This class was generated. Customizations should only happen in a newly
 * introduced subclass. 
 */
public class ServiceExecutableExtensionFactory extends AbstractGuiceAwareExecutableExtensionFactory {

	@Override
	protected Bundle getBundle() {
		return ServiceActivator.getInstance().getBundle();
	}
	
	@Override
	protected Injector getInjector() {
		return ServiceActivator.getInstance().getInjector(ServiceActivator.AT_AC_TUWIEN_BIG_SERVICELANG_SERVICE);
	}
	
}
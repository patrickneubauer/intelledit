/*
 * generated by Xtext
 */
package at.ac.tuwien.big.servicelang;

/**
 * Initialization support for running Xtext languages 
 * without equinox extension registry
 */
public class ServiceStandaloneSetup extends ServiceStandaloneSetupGenerated{

	public static void doSetup() {
		new ServiceStandaloneSetup().createInjectorAndDoEMFRegistration();
	}
}

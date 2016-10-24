package at.ac.tuwien.big.autoedit.global;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Stack;

import org.eclipse.emf.ecore.resource.Resource;
import org.moeaframework.Executor;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.Variation;
import org.moeaframework.core.spi.OperatorFactory;
import org.moeaframework.core.spi.OperatorProvider;
import org.moeaframework.problem.AbstractProblem;

import at.ac.tuwien.big.autoedit.change.Change;
import at.ac.tuwien.big.autoedit.change.CostProvider;
import at.ac.tuwien.big.autoedit.change.Undoer;
import at.ac.tuwien.big.autoedit.change.composite.CompositeChangeImpl;
import at.ac.tuwien.big.autoedit.ecore.util.MyResource;
import at.ac.tuwien.big.autoedit.search.local.SimpleStream;
import at.ac.tuwien.big.autoedit.search.local.impl.Evaluation;
import at.ac.tuwien.big.autoedit.search.local.impl.ResourceEvaluator;
import at.ac.tuwien.big.autoedit.search.local.impl.ViolatedConstraintsEvaluator;
import at.ac.tuwien.big.autoedit.transfer.EcoreTransferFunction;
import at.ac.tuwien.big.autoedit.xtext.DynamicValidator;
import at.ac.tuwien.big.autoedit.xtext.DynamicValidatorIFace;

public class GlobalSearch {
	
	private DynamicValidatorIFace valid;
	private SimpleStream<Change<?>> stream;
	private Resource resource;
	
	static {
		OperatorFactory opfact = OperatorFactory.getInstance();
		opfact.addProvider(new OperatorProvider() {
			
			@Override
			public String getVariationHint(Problem problem) {
				return "variatechange";
			}
			
			@Override
			public Variation getVariation(String name, Properties properties, Problem problem) {
				if ("variatechange".equals(name)) {
				return new Variation() {
					
					private Random random = new Random();
					
					@Override
					public int getArity() {
						return 2;
					}
					
					@Override
					public Solution[] evolve(Solution[] parents) {
						Solution first = parents[0];
						Solution second = parents[1];
						int border = random.nextInt(Math.min(first.getNumberOfVariables(),second.getNumberOfVariables()));
						
						Solution[] ret = new Solution[2];
						ret[0] = new Solution(first.getNumberOfVariables(), first.getNumberOfObjectives());
						ret[1] = new Solution(second.getNumberOfVariables(), first.getNumberOfObjectives());
						for (int i = 0; i < border; ++i) {
							ret[1].setVariable(i, first.getVariable(i).copy());
							ret[0].setVariable(i, second.getVariable(i).copy());
						}
						for (int i = border; i < first.getNumberOfVariables(); ++i) {
							ret[0].setVariable(i, first.getVariable(i).copy());
							ret[1].setVariable(i, second.getVariable(i).copy());
						}
						return ret;
					}
				};
				} else if ("mutatechange".equals(name)) {
					return new Variation() {
						
						private Random random = new Random();
						
						@Override
						public int getArity() {
							return 1;
						}
						
						@Override
						public Solution[] evolve(Solution[] parents) {
							Solution first = parents[0];								
							int randInd = random.nextInt(first.getNumberOfVariables());
							MyResourceContainer container = (MyResourceContainer)first.getAttribute("container");
							EcoreTransferFunction tf = container.pullResource();
							Stack<Undoer> undos = new Stack<Undoer>();
							for (int i = 0; i < randInd; ++i) {
								MOEAChangeVariable var = (MOEAChangeVariable)first.getVariable(randInd);
								if (var == null || var.getCurChange() == null) {continue;}
								undos.push(var.getCurChange().transfered(tf).execute());
							}
							((MOEAChangeVariable)first.getVariable(randInd)).randomChange(tf);
							while (!undos.isEmpty()) {
								undos.pop().undo();
							}
							return parents;
						}
					};
				}
				return null;
			}
			
			@Override
			public String getMutationHint(Problem problem) {
				return "mutatechange";
			}
		});
	}
	
	public GlobalSearch(Resource resource, DynamicValidatorIFace valid, SimpleStream<Change<?>> stream) {
		
		this.valid = valid;
		this.stream = stream;
		setResource(resource);
	}
	
	public Resource getResource() {
		return resource;
	}
	
	private MyResourceContainer container;
	
	public MyResourceContainer getContainer() {
		return container;
	}
	
	public void setResource(Resource res) {
		try {
		if (resource != null && resource.equals(res)) {
			return;
		}
		
		this.resource = res;
		this.container = new MyResourceContainer(MyResource.get(resource));
		abortSearch();
		startSearch();
		} catch (Exception e) {
			String str = Arrays.toString(e.getStackTrace());
			System.out.println(str);
		}
	}
	
	public void changedSomething() {
		if (resource != null) {
			container = new MyResourceContainer(MyResource.get(resource));
		}
	}

	public void startSearch() {
		Thread t = new Thread(()->{
		Problem problem = new AbstractProblem(20,3) {			
			
			@Override
			public void close() {
				
				super.close();
			}
			
			@Override
			public Solution newSolution() {
				try {
					Solution sol = new Solution(getNumberOfVariables(), getNumberOfObjectives());
					sol.setAttribute("container", container);
					for (int i = 0; i < getNumberOfVariables(); ++i) {
						MOEAChangeVariable var = new MOEAChangeVariable(valid);
						sol.setVariable(i, var);
					}
					return sol;
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
			
			@Override
			public void evaluate(Solution sol) {
				try {
				EcoreTransferFunction tf = container.pullResource();
				MyResource trg = MyResource.get(tf.forwardResource());
				List<Change<?>> chs = new ArrayList<Change<?>>();
				
				for (int i = 0; i < getNumberOfVariables(); ++i)  {
					Variable  var = sol.getVariable(i);
					if (var == null) {
						continue;
					}
					if (var instanceof MOEAChangeVariable) {
						MOEAChangeVariable mvar = (MOEAChangeVariable)var;
						if (mvar.getCurChange() != null) {
							chs.add(mvar.getCurChange().transfered(tf));
						}
					} else {
						throw new RuntimeException("I can only work with my variables!");
					}
				}
				CompositeChangeImpl cci = new CompositeChangeImpl(trg.getResource(), chs);
				ViolatedConstraintsEvaluator eval = new ViolatedConstraintsEvaluator();
				double[] constraintsViolated = eval.evaluate(cci, new Evaluation());
				if (!Double.isNaN(constraintsViolated[0])) {
					sol.setObjective(0, -constraintsViolated[0]);
				} else {
					sol.setObjective(0, 99999999);
				}
				if (!Double.isNaN(constraintsViolated[1])) {
					sol.setObjective(1, constraintsViolated[1]);
				}  else {
					sol.setObjective(1, 99999999);
				}
				if (!Double.isNaN(constraintsViolated[2])) {
					sol.setObjective(2, -constraintsViolated[2]);
				} else {
					sol.setObjective(2, 99999999);
				}
				stream.add(cci, cci.transfered(tf.inverse()), constraintsViolated[0], constraintsViolated[1], constraintsViolated[2]);
				container.pushResource(tf);
				} catch (Exception e) {
					String str = Arrays.toString(e.getStackTrace()).replace(",","\n");
					System.err.println(str);
				}
			}
		};
		//problem = new DistributedProblem(problem, Executors.newCachedThreadPool());
		
		exec = new Executor().
				withAlgorithm("NSGAII").withProblem(problem).withMaxTime(1000*1000*1000L).withProperty("populationSize", 100);
		exec.run();
		});
		t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				System.out.println(e.getMessage());
				String s = Arrays.toString(e.getStackTrace()).replace(",", "\n");
				System.out.println(s);
			}
		});
		t.start();
		
	}
	
	public void abortSearch() {
		if (exec != null) {
			exec.cancel();
		}
	}
	
	Executor exec;

	



}
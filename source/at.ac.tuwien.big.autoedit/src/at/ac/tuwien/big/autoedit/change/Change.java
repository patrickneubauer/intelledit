package at.ac.tuwien.big.autoedit.change;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.model.XtextDocument;
import org.eclipse.xtext.util.DiffUtil;

import at.ac.tuwien.big.autoedit.change.basic.CreateObjectChange;
import at.ac.tuwien.big.autoedit.change.basic.DeleteObjectChange;
import at.ac.tuwien.big.autoedit.ecore.util.MyResource;
import at.ac.tuwien.big.autoedit.fixer.SetAdd;
import at.ac.tuwien.big.autoedit.oclvisit.FixAttemptReference;
import at.ac.tuwien.big.autoedit.transfer.EcoreTransferFunction;
import at.ac.tuwien.big.autoedit.transfer.TransferFunction;
import at.tuwien.big.virtmod.datatype.IteratorUtils;
import at.tuwien.big.virtmod.datatype.IteratorUtils.SimpleFunction;
import difflib.Chunk;
import difflib.Delta;
import difflib.Delta.TYPE;
import difflib.DiffAlgorithm;
import difflib.DiffUtils;
import difflib.Patch;
import difflib.myers.MyersDiff;


/**A concrete change which can be executed*/
public interface Change<Type extends Change<Type>> {

	public Iterable<? extends Change<?>> getSubChanges();
	
	public Type clone();
	
	public Resource forResource();
	
	public default List<Change<?>> unbox() {
		return Collections.singletonList(this);
	}
	
	public double getCosts();
	
	public default MyResource forMyResource() {
		return MyResource.get(forResource());
	}
	
	public default void addFixReferences(Collection<FixAttemptReference> refs) {
		addFixReferencesLocal(refs);
		for (Change<?> c: getSubChanges()) {
			c.addFixReferences(refs);
		}
		
	}
	
	public void addFixReferencesLocal(Collection<FixAttemptReference> refs);
	
	public default Set<FixAttemptReference> getFixReferences() {
		Set<FixAttemptReference> ret = new HashSet<FixAttemptReference>();
		addFixReferences(ret);
		return ret;
	}
		
	
	public void transfer(EcoreTransferFunction func);
	
	public default Iterable<BasicChange<?>> getBaseChanges() {
		return ()->IteratorUtils.multiIteratorIt(IteratorUtils.convert(
				getSubChanges().iterator(),
				(x)->x.getBaseChanges().iterator()));
	}
	
	public default List<BasicChange<?>> getReducedBasicChanges() {
		Iterable<BasicChange<?>> baseChanges = getBaseChanges();
		EObjectChangeMap changeMap = new EObjectChangeMap();
		for (BasicChange<?> ch: baseChanges) {
			ch.normalizeMap(changeMap);
		}
		return changeMap.getChangeList();
	}
	
	public default void augmentBasicChangeMap(Map<BasicChange<?>, Set<Change<?>>> container, Set<Change<?>> additionalUnnecessary) {
		int initSize = container.size();
		for (Change<?> subC: getSubChanges()) {
			subC.augmentBasicChangeMap(container,additionalUnnecessary);
		}
		for (BasicChange<?> bc: getBaseChanges()) {
			Set<Change<?>> chs = container.get(bc);
			if (chs == null) {
				container.put(bc, chs = new HashSet<Change<?>>());
			}
			chs.add(this);
		}
		int thenSize = container.size();
		if (initSize == thenSize) {
			//This is unnecessary
			additionalUnnecessary.add(this);
		}
	}
	
	public default void reduceMap(Map<BasicChange<?>, Set<Change<?>>> container, List<BasicChange<?>> reducedChanges) {
		Set<BasicChange<?>> removeChanges = new HashSet<BasicChange<?>>(container.keySet());
		Set<BasicChange<?>> reducedChangeSet = new HashSet<BasicChange<?>>(reducedChanges);
		removeChanges.removeAll(reducedChangeSet);
		for (BasicChange<?> bc: removeChanges) {
			container.remove(bc);
		}
	}
	
	public default Set<Change<?>> getUnionSet(Map<BasicChange<?>, Set<Change<?>>> map) {
		Set<Change<?>> ret = new HashSet<Change<?>>();
		for (Set<Change<?>> set: map.values()) {
			ret.addAll(set);
		}
		return ret;
	}
	
	public  void removeNonretained(Set<Change<?>> retain, Set<Change<?>> removeSet);
	
	public default void removeUnnecessarySubchanges() {
		Map<BasicChange<?>, Set<Change<?>>> container = new HashMap<BasicChange<?>, Set<Change<?>>>();
		Set<Change<?>> removeSet = new HashSet<Change<?>>();
		augmentBasicChangeMap(container,removeSet);
		List<BasicChange<?>> reducedChanges = getReducedBasicChanges();
		reduceMap(container, reducedChanges);
		Set<Change<?>> retainChanges = getUnionSet(container);
		removeNonretained(retainChanges,removeSet);
	}
	
	
	public boolean isIdentity();
	
	public default Undoer execute() {
		List<Undoer> undoerList = new ArrayList<>();		
		getSubChanges().forEach((x)->undoerList.add(x.execute()));
		return ListUndoer.from(undoerList);
	}
	
	public default CostProvider costProvider() {
		return MyResource.get(forResource()).getCostProvider();
	}
	
		
	public static class EmptyChange implements Change<EmptyChange>{
		
		private Resource forResource;
		
		public EmptyChange(Resource forResource) {
			this.forResource = forResource;
		}

		@Override
		public Iterable getSubChanges() {
			return Collections.emptyList();
		}

		@Override
		public EmptyChange clone() {
			return new EmptyChange(forResource);
		}

		@Override
		public Resource forResource() {
			return forResource;
		}

		@Override
		public void addFixReferencesLocal(Collection refs) {				
		}

		@Override
		public void transfer(EcoreTransferFunction func) {
			forResource = func.forwardResource();
		}

		@Override
		public void removeNonretained(Set retain, Set remove) {
		}

		@Override
		public double getCosts() {
			return 0;
		}
		
		public String toString(EObject context) {
			return "Do nothing";
		}
		
		public String toString(String context) {
			return "Do nothing";
		}
		
		public String toString() {
			return "Do nothing";
		}

		@Override
		public String getName(EObject context) {
			return "No Change";
		}
		
		@Override
		public String getName(String context) {
			return "No Change";
		}
		
		@Override
		public boolean isIdentity() {
			return true;
		}
		
	}; 
	
	public static String intToInvisible(long num, int bytes, boolean minimize) {
		StringBuilder ret = new StringBuilder();
		boolean[] b = new boolean[bytes];
		for (int i = 0; i < bytes; ++i) {
			b[b.length-1-i] = (num%2)==1;
			num/=2;
		}		
		//U+180E Mongolian Vovel Separator
		//U+200B No width"
		//U+FEFF No width
		for (int i = 0; i < b.length; ++i) {
			if (b[i] != minimize) {
				ret.append("\u180E");
			} else {
				ret.append("\uFEFF");
			}
		}
		return ret.toString();
	}
	
	public static String costToInvisible(double d, boolean minimize) {
		String str = String.valueOf(d);
		String exp = "";
		boolean minus = false;
		if (str.startsWith("-")) {
			minus = true;
			str = str.substring(1);
		}
		if (str.contains("E")) {
			exp = str.substring(str.indexOf("E")+1);
			str = str.substring(0,str.indexOf("E"));
		}
		String afterComma = "";
		if (str.contains(".")) {
			afterComma = str.substring(str.indexOf('.')+1);
			str = str.substring(0,str.indexOf('.'));
		}
		//U+180E Mongolian Vovel Separator
		//U+200B No width"
		//U+FEFF No width
		int expNum = (exp.equals(""))?0:Integer.valueOf(exp);
		String ret = intToInvisible(minus?0:1, 1, minimize)+intToInvisible(expNum+256, 9, minimize^minus)+intToInvisible(Long.valueOf(str),24,minimize^minus);
		for (int i = 0; i < afterComma.length(); ++i) {
			ret+= intToInvisible(Integer.valueOf(""+afterComma.charAt(i)), 4, minimize^minus);
		}
		char[] retAr = ret.toCharArray();
		for (int i = 0; i < retAr.length; ++i) {
			System.out.println("RetAr: " +Character.getName(retAr[i]));
		}
		return ret;
	}
	
	static WeakHashMap<Resource, Change> weakEmptyChanges = new WeakHashMap<Resource, Change>();

	public static Change<?> empty(Resource res) {
		Change<?> ret = weakEmptyChanges.get(res);
		if (ret == null) {
			weakEmptyChanges.put(res, ret = new EmptyChange(res));
		}
		return ret;
	}

	public default Change<?> transfered(EcoreTransferFunction transferFunc) {
		Change<?> ret = clone();
		ret.transfer(transferFunc);
		return ret;
	}
	
	public String toString(EObject context);

	public static String safeToString(Change<?> change, EObject context) {
		if (change == null) {
			return "Do nothing";
		} else {
			return change.toString(context);
		}
	}
	
	public String getName(EObject context);

	public default String getName(String contextUri) {
		return getName((EObject)null);
	}

	public default String toString(String contextUri) {
		return toString();
	}
}
 
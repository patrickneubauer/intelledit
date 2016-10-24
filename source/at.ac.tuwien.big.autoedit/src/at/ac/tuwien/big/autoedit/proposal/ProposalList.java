package at.ac.tuwien.big.autoedit.proposal;

import java.util.Collection;
import java.util.Iterator;

public interface ProposalList<T extends Comparable<T>, U extends Proposal<T,U>> extends Iterable<U> {
	
	public void addProposal(U prob);
	
	public void clearProposals();
	
	public Iterable<U> getProposals();
	
	public default Iterator<U> iterator() {
		return getProposals().iterator();
	}
	
	public int getMaxProposals();
	
	public void setMaxProposals(int proposals);

}

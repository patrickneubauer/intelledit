package at.ac.tuwien.big.autoedit.change;

public interface BasicChangeType<Self extends ChangeType<Self,BC>, BC  extends BasicChange<BC>> extends ChangeType<Self,BC> {

}

package entity.inter;

public interface IDocChangeEvent {
    IPortalDocument getDocument();

    int getOffset();

    int getOldLength();

    int getNewLength();

    CharSequence getOldFragment();

    CharSequence getNewFragment();

    long getOldTimeStamp();

}

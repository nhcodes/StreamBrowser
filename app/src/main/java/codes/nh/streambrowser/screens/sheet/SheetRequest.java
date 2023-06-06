package codes.nh.streambrowser.screens.sheet;

public class SheetRequest {

    private final Class<? extends SheetFragment> fragmentClass;

    public SheetRequest(Class<? extends SheetFragment> fragmentClass) {
        this.fragmentClass = fragmentClass;
    }

    public Class<? extends SheetFragment> getFragmentClass() {
        return fragmentClass;
    }

}

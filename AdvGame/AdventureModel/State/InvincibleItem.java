package AdventureModel.State;

import AdventureModel.AdventureObject;
import AdventureModel.State.State;
import views.BossView;

/**
 * The Invincible Item State of Adventure Object
 */
public class InvincibleItem implements State {

    BossView view; // to direct us to the appropriate action for using this object

    /**
     * Invincible Item Constructor.
     */
    public InvincibleItem() {

    }

    /**
     * Makes player invincible in boss room
     */
    @Override
    public void execute() {
        this.view.invincible();
    }

    /**
     * Set the view to delegate the execute task to
     *
     * @param view the boss view for this game
     */
    public void setView(BossView view) {
        this.view = view;
    }

}

package scripts.Utils;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Player;

/**
 * Created by James on 16/10/2016.
 */
public class CombatUtils {

    public static boolean isInCombat(){
        return Player.getRSPlayer().getInteractingCharacter() != null;
    }

    //region CONDITIONAL SLEEPING

    public static void waitToLeaveCombat(){
        Timing.waitCondition(new Condition() {
            @Override
            public boolean active() {
                General.sleep(100, 300);
                return Player.getRSPlayer().getInteractingCharacter() == null;
            }
        }, General.random(60000, 120000));
    }

    public static void waitToEnterCombat(){
        Timing.waitCondition(new Condition() {
            @Override
            public boolean active() {
                General.sleep(100, 300);
                return Player.getRSPlayer().getInteractingCharacter() != null;
            }
        }, General.random(3000, 5000));
    }

    //endregion
}

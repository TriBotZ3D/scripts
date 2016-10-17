package scripts.Utils;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.RSTile;

/**
 * Created by James on 16/10/2016.
 */
public class WalkingUtils {


    //region SLEEP METHODS

    public static void waitToStopWalking(){
        Timing.waitCondition(new Condition() {
            @Override
            public boolean active() {
                General.sleep(100, 300);
                return Player.isMoving();
            }
        }, General.random(3000, 5000));

        Timing.waitCondition(new Condition() {
            @Override
            public boolean active() {
                General.sleep(100, 300);
                return !Player.isMoving();
            }
        }, General.random(15000, 20000));
    }

    public static void waitToMoveTile(final RSTile startTile){
        Timing.waitCondition(new Condition() {
            @Override
            public boolean active() {
                General.sleep(300);
                return !Player.getPosition().equals(startTile);
            }
        }, General.random(10000, 20000));
    }

    //endregion
}

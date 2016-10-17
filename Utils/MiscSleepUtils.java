package scripts.Utils;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Game;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.RSTile;
import scripts.AccountStarter.Variables.ASVariables;

/**
 * Created by James on 23/09/2016.
 */
public class MiscSleepUtils {


    public static void waitForIdle(int minTimeout, int maxTimeout){
        Timing.waitCondition(new Condition() {
            @Override
            public boolean active() {
                General.sleep(100, 300);
                return Player.getAnimation() == -1;
            }
        }, General.random(minTimeout, maxTimeout));
    }

    public static void waitUntilNotIdle(){
        Timing.waitCondition(new Condition() {
            @Override
            public boolean active() {
                General.sleep(100, 300);
                return Player.getAnimation() != -1;
            }
        }, General.random(3000, 5000));
    }


}

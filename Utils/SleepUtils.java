package scripts.Utils;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Player;

/**
 * Created by James on 23/09/2016.
 */
public class SleepUtils {

    public static void waitToStopWalking(){
        Timing.waitCondition(new Condition() {
            @Override
            public boolean active() {
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

    public static void waitForBankToOpen(){
        Timing.waitCondition(new Condition() {
            @Override
            public boolean active() {
                General.sleep(100, 300);
                return Banking.isBankScreenOpen();
            }
        }, General.random(4000, 6000));
    }

    public static void waitForBankToClose(){
        Timing.waitCondition(new Condition() {
            @Override
            public boolean active() {
                General.sleep(100, 300);
                return !Banking.isBankScreenOpen();
            }
        }, General.random(4000, 6000));
    }


}

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
public class SleepUtils {

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

    public static void waitToMoveTile(final RSTile startTile){
        Timing.waitCondition(new Condition() {
            @Override
            public boolean active() {
                General.sleep(300);
                return !Player.getPosition().equals(startTile);
            }
        }, General.random(10000, 20000));
    }

    public static void waitForInventChange(){
        final int inventCount = Inventory.getAll().length;
        Timing.waitCondition(new Condition() {
            @Override
            public boolean active() {
                General.sleep(100, 300);
                return inventCount != Inventory.getAll().length;
            }
        }, General.random(2000, 3000));
    }

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

    public static void waitForEmptyInvent(){
        Timing.waitCondition(new Condition() {
            @Override
            public boolean active() {
                General.sleep(100, 300);
                return InventoryUtils.inventIsEmpty();
            }
        }, General.random(3000, 5000));
    }

    public static void waitForItemSelect(){
        Timing.waitCondition(new Condition() {
            @Override
            public boolean active() {
                General.sleep(100, 300);
                return Game.isUptext("->");
            }
        }, General.random(2000, 4000));
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

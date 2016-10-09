package scripts.AccountStarter.Firemaking;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Mouse;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.*;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSPlayer;
import org.tribot.api2007.types.RSTile;
import scripts.Utils.SleepUtils;

import java.util.Timer;

/**
 * Created by James on 07/10/2016.
 */
public class ASFiremaking {

    public static void lightLogs(String inLogName){
        while (true) {
            RSTile nearestValidTile = getNearestEmptyTile(Player.getPosition());
            if (Player.getPosition().equals(nearestValidTile)) {
                if (Player.getAnimation() == -1) {
                    RSItem[] Logs = Inventory.find(inLogName);
                    RSItem[] Tinderbox = Inventory.find("Tinderbox");
                    if (Game.isUptext("Tinderbox ->")) {
                        if (Logs.length > 0) {
                            if (Game.isUptext("Tinderbox -> Oak logs") || Game.isUptext("Tinderbox -> Logs")) {
                                Mouse.click(1);
                                SleepUtils.waitUntilNotIdle();
                                if (Logs.length > 1)
                                    Tinderbox[0].hover();
                            } else {
                                Logs[0].hover();
                                Timing.waitCondition(new Condition() {
                                    @Override
                                    public boolean active() {
                                        General.sleep(300);
                                        return Game.isUptext("Tinderbox -> Oak logs") || Game.isUptext("Tinderbox -> Logs");
                                    }
                                }, General.random(2000, 4000));
                            }
                        } else {
                            System.out.println("No Logs Found!");
                            General.sleep(1000);
                            break;
                        }
                    } else {
                        if (Tinderbox.length > 0) {
                            if (Tinderbox[0].click("Use Tinderbox")) {
                                SleepUtils.waitForItemSelect();
                            }
                        } else {
                            System.out.println("No Tinderbox Found!");
                            General.sleep(1000);
                            break;
                        }
                    }
                } else {
                    final RSTile startTile = Player.getPosition();
                    SleepUtils.waitToMoveTile(startTile);
                    if (!Player.getPosition().equals(startTile))
                        break;
                }
            } else {
                if (Game.isUptext("->")) {
                    Mouse.click(1);
                    General.sleep(300, 600);
                }
                Walking.walkScreenPath(Walking.generateStraightScreenPath(nearestValidTile));
                SleepUtils.waitToStopWalking();
            }
        }
    }

    private static RSTile getNearestEmptyTile(RSTile inCentreTile){
        for (int i = 0; i < 10; i++){
            RSTile[] allTiles = getPerimeterTiles(i, inCentreTile);
            for (int j = 0; j < allTiles.length; j++){
                RSObject[] objectsOnTile = Objects.getAt(allTiles[j]);
                if (objectsOnTile.length == 0 || objectsOnTile[0].getDefinition().getName().equals("null")){
                    if (PathFinding.isTileWalkable(allTiles[j]))
                        return allTiles[j];
                }
            }
        }
        return null;
    }


    private static RSTile[] getPerimeterTiles(int inLayer, RSTile inCentreTile){

        if (inLayer == 0)
            return new RSTile[]{inCentreTile};

        int sideLength = 1 + (2 * inLayer);
        int perimeterLength = (sideLength - 1) * 4;

        RSTile topLeftTile = inCentreTile.translate((-1 * inLayer), (-1 * inLayer));
        RSTile[] outputTileArray = new RSTile[perimeterLength];

        int[] modifierX = { 1, 0, -1, 0};
        int[] modifierY = { 0, 1, 0, -1};
        RSTile currentTile = topLeftTile;
        int perimeterCount = 0;
        for (int sideNum = 0; sideNum < 4; sideNum++){
            for (int tileNum = 0; tileNum < sideLength - 1; tileNum++){
                outputTileArray[perimeterCount] = currentTile;
                currentTile = currentTile.translate(modifierX[sideNum], modifierY[sideNum]);
                perimeterCount++;
            }
        }

        return outputTileArray;
    }
}

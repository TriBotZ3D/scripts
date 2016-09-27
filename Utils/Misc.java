package scripts.Utils;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Mouse;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.GameTab;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSTile;

import java.awt.*;

/**
 * Created by James on 15/09/2016.
 */
public class Misc {

    public static Point generateRandomPoint(Rectangle inRec) {
        //Will generate a random point inside a given rectangle
        return new Point(inRec.x + General.random(0, inRec.width), inRec.y + General.random(0, inRec.height));
    }

    public static RSTile getCentreTile(RSArea inArea){
        return inArea.getAllTiles()[inArea.getAllTiles().length / 2];
    }

    public static boolean arrayContainsString (String[] inArray, String inKey){
        for (int i = 0; i < inArray.length; i++){
            if (inArray[i].equals(inKey)){
                return true;
            }
        }
        return false;
    }

}

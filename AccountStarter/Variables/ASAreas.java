package scripts.AccountStarter.Variables;

import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSTile;

/**
 * Created by James on 22/09/2016.
 */
public class ASAreas {


    //new RSArea(new RSTile(), new RSTile())

    private static RSArea bankArea = new RSArea(new RSTile(3207, 3215, 2), new RSTile(3210, 3222, 2));
    public static RSArea getBankArea(){
        return bankArea;
    }

    private static RSArea combatArea = new RSArea(new RSTile(3239, 3253, 0), new RSTile(3266, 3222, 0));
    public static RSArea getCombatArea(){
        return combatArea;
    }

    private static RSArea goblinHouseArea = new RSArea(new RSTile(3243, 3248, 0), new RSTile(3248, 3244, 0));
    public static RSArea getGoblinHouseArea(){
        return goblinHouseArea;
    }

    private static RSArea fishingArea = new RSArea(new RSTile(3237, 3164, 0), new RSTile(3244, 3140, 0));
    public static RSArea getFishingArea(){
        return fishingArea;
    }

    private static RSArea miningArea = new RSArea(new RSTile(3220, 3153, 0), new RSTile(3235, 3143, 0));
    public static RSArea getMiningArea(){
        return miningArea;
    }

    private static RSArea normalTreesArea = new RSArea(new RSTile(3139, 3260, 0), new RSTile(3208, 3237, 0));
    public static RSArea getNormalTreesArea(){
        return normalTreesArea;
    }

    private static RSArea oakTreesArea = new RSArea(new RSTile(3200, 3250, 0), new RSTile(3210, 3237, 0));
    public static RSArea getOakTreesArea(){return oakTreesArea;}
}

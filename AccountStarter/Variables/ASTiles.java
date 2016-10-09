package scripts.AccountStarter.Variables;

import org.tribot.api2007.types.RSTile;

/**
 * Created by James on 22/09/2016.
 */
public class ASTiles {

    private static RSTile lumbyStairsTile = new RSTile(3208, 3210);
    public static RSTile getLumbyStairsTile(){return lumbyStairsTile;}

    private static RSTile centreCombatTile = new RSTile(3252, 3242);
    public static RSTile getCentreCombatTile(){return centreCombatTile;}

    private static RSTile bankTile = new RSTile(3208, 3220, 2);
    public static RSTile getBankTile() {return bankTile;}


}

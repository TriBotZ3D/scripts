package scripts;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Keyboard;
import org.tribot.api.input.Mouse;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.*;
import org.tribot.api2007.ext.Filters;
import org.tribot.api2007.types.*;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.MessageListening07;
import scripts.Utils.ChatUtils;

/**
 * Created by James on 14/09/2016.
 */

@ScriptManifest(authors = {"Z3D"}, name = "Test Script", category = "Test")
public class TEST_SCRIPT extends Script implements MessageListening07{


    @Override
    public void run() {

        Combat.selectIndex(2);

    }


   private void tradePlayer(String inPlayerName){
       //if (Trade window not open){
       RSPlayer[] Mule = Players.find(inPlayerName);
       if (Mule.length > 0){
           if (Mule[0].isOnScreen()){
               if (Mule[0].click("Trade with " + inPlayerName)){
                   //Clicked trade with, wait for trade window to open
               }
           }else{
               //Mule found but not on screen
           }
       }else{
           //No Mule found!
       }
       //}else{
       //   Trade screen open, handle trade stuff
       //}
   }

    private void Pickpocket(String inNPCName){

        if (Player.getAnimation() == -1) {

            if (Game.getUptext() != null && Game.getUptext().contains("Pickpocket")){
                Mouse.sendClick(Mouse.getPos(), 1);
                General.sleep(400, 1300);
            }else {

                final RSNPC[] validNPCs = NPCs.findNearest(Filters.NPCs.actionsContains("Pickpocket").combine(Filters.NPCs.nameEquals(inNPCName), true));

                if (validNPCs.length > 0) {
                    if (validNPCs[0].isOnScreen()) {
                        if (validNPCs[0].click("Pickpocket")) {
                            //Clicked pickpocket
                            Timing.waitCondition(new Condition() {
                                @Override
                                public boolean active() {
                                    General.sleep(100, 300);
                                    return Player.getAnimation() != -1;
                                }
                            }, General.random(3000, 8000));
                        }
                    } else {
                        //NPC not on screen, walk to them
                        Walking.blindWalkTo(validNPCs[0].getPosition());
                        Timing.waitCondition(new Condition() {
                            @Override
                            public boolean active() {
                                General.sleep(100, 300);
                                return !Player.isMoving() || validNPCs[0].isOnScreen();
                            }
                        }, General.random(5000, 1000));

                        //Walked to the NPC but still not on screen :/
                        if (!validNPCs[0].isOnScreen())
                            Camera.turnToTile(validNPCs[0].getPosition());
                    }
                } else {
                    //No NPCs currently found.
                }
            }
        }else{
            //Wait until we're done pickpocketing.
            Timing.waitCondition(new Condition() {
                @Override
                public boolean active() {
                    General.sleep(100, 300);
                    return Player.getAnimation() == -1;
                }
            }, General.random(5000, 10000));
        }
    }



    @Override
    public void serverMessageReceived(String s) {
        if (s.contains("Congratulations, you just advanced a")){
            ChatUtils.waitForChatInterface();
            while(ChatUtils.interfaceCoveringChat()){
                Keyboard.pressKeys(Keyboard.getKeyCode(' '));
                General.sleep(1000);
            }

        }
    }

    @Override
    public void playerMessageReceived(String s, String s1) {

    }

    @Override
    public void duelRequestReceived(String s, String s1) {

    }

    @Override
    public void clanMessageReceived(String s, String s1) {

    }

    @Override
    public void tradeRequestReceived(String s) {

    }

    @Override
    public void personalMessageReceived(String s, String s1) {

    }
}

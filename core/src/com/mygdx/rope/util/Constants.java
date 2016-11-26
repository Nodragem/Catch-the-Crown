package com.mygdx.rope.util;

import com.badlogic.gdx.utils.ArrayMap;

public class Constants {
	public static final float VIEWPORT_WIDTH = 32.0f;
	public static final float VIEWPORT_HEIGHT = 17.0f;
	public static final float TILES_SIZE = 32.0f; //  one meter equal 32 pixels
	public static final float VIEWPORT_GUI_WIDTH = 1920.0f;
	public static final float VIEWPORT_GUI_HEIGHT = 1080.0f;
	public static final float CROWNGOLDRATE = 0.333f;
    public static final float MARKPENALTY = 3f ;

    public enum ANNOUNCEMENT {KO, LONG_TERM_KO, CROWN, GOLDEN_PRABBIT, TIMER_ALMOST_OFF, WEAK_PRABBIT, NONE}


    public static enum GAME_STATE {PLAYED, PAUSED, ROUND_END, TOURNAMENT_END}
    public static enum MENU_STATE {MAIN_MENU, STORY_MENU, TOURNAMENT_MENU, OPTION_MENU, CONTROL_MENU, QUIT_REQUEST}
    public static enum GUI_STATE {DISPLAY_GUI, DISPLAY_END, previousGUIState, DISPLAY_PAUSE}
    public static final int MOVESTOFREE = 2;
    public static final int MOVESTOTHROW = 4;
    public static final int MAXMARKS = 5;
    public static final float BONUSCROWN = 0;
    public static final float GOLDENDLEVEL = 3500;//500000;
    public static final float GOLDVALUE = 10;
	public static final String TEXTURE_ATLAS_OBJECTS = "nothing-yet.pack";
	public static final String LEVEL_01= "tournament_levels/level_jungle01.tmx";
	public static final String TOURNAMENT_LEVEL_PATH= "tournament_levels";
	public static final String FEET_TAG= "feet";
    public static final float TIMEATTACK1 = 0.5f;
    public static final float RESPAWNTIME = 5.0f;
    public static final float SLAPDAMAGE = 8.4f;
    public static final float COINSTIME = - 20.0f;
    public static final float STARTTIMER = 5*60;//2*60; //10*60; //2*60; // 2*60; // 2 minutes


    public static enum INPUTSTATE {MOVE_X, MOVE_Y, AIMING_X, AIMING_Y, ATTACK1, ATTACK2, JUMP, PICKUP}
    public static enum TRAPSTATE {ON, OFF, OBSTRUCTED, RELOADING}
    public static enum SWITCHSTATE {ON, OFF, ACTIVATED}
    public static enum VIEW_DIRECTION { LEFT, RIGHT, UP, DOWN}
    public static enum PLATFORM_STATE {MOVING, STOPPED, LEAVING_STOP, GOING_TO_STOP}
    public static enum ACTIVE_STATE { ACTIVATION, ACTIVATED, DESACTIVATION, DESACTIVATED }
    public static enum DAMAGE_STATE { IMMUNE,  NOT_IMMUNE}
    public static enum MOVE_STATE {IDLE, GROUNDED, FALLING, RISING, PANICKING, THROWED, THROWING_CHARACTER}
    public static enum PICKUP_STATE {PICKINGUP, CHALLENGED, CHALLENGER, NORMAL} // note that if you are picking up a character, you are in picked-up challenge.
    public static enum ATTACK_STATE{SHORTATTACK, LONGATTACK, NOTATTACKING, CARRYING, AIMING, CHARGING, CHARGE_READY, THROWING}
    public static enum AWAKE_STATE {AWAKE, SLEEPING, DEAD}
    public static enum COLLIDER_TYPE {STICKY, SENSOR, HURTABLE, ONEWAY, CROWN}
    public static enum MAPOBJECT_TYPE {TRAP, LAUNCHER, PLATFORM, SWITCH}


    public static final ArrayMap<String, Short> CATEGORY = new ArrayMap<String, Short>();
        static {
            CATEGORY.put("Player",(short)0x0001);
            CATEGORY.put("Sensor",(short)0x0002);
            CATEGORY.put("Object",(short)0x0004);
            CATEGORY.put("Weapon",(short)0x0008);
            CATEGORY.put("Collectable",(short)0x0010);
            CATEGORY.put("AttachedObject",(short)0x0020);
            CATEGORY.put("Scenery", (short) 0x0040);
        }

    public static final ArrayMap<String, Short> MASK = new ArrayMap<String, Short>();
    static {
        MASK.put("Player", (short) (CATEGORY.get("Weapon") | CATEGORY.get("Sensor") | CATEGORY.get("Collectable") | CATEGORY.get("Scenery") | CATEGORY.get("AttachedObject") ) );
        //MASK.put("Sensor", (short)(CATEGORY.get("Player") | CATEGORY.get("Scenery")) );
        MASK.put("Sensor", (short) (CATEGORY.get("Player" )|CATEGORY.get("Scenery")));
        MASK.put("Object",(short)(CATEGORY.get("Sensor") | CATEGORY.get("Object") | CATEGORY.get("Scenery") ) );
        //MASK.put("Weapon",  (short) ~( CATEGORY.get("Object") & CATEGORY.get("Weapon") & CATEGORY.get("Sensor")) );
        MASK.put("Weapon",  (short) ~( CATEGORY.get("Object") & CATEGORY.get("Weapon") & CATEGORY.get("Sensor")) );
        //MASK.put("Weapon",  (short) (CATEGORY.get("Player") | CATEGORY.get("Scenery")) );
        //MASK.put("Weapon",  (short) 0 );
        MASK.put("Collectable", CATEGORY.get("Player"));
        MASK.put("AttachedObject",(short)(CATEGORY.get("Player") | CATEGORY.get("Sensor")) );
        MASK.put("Scenery", (short) ~CATEGORY.get("Scenery") );
        MASK.put("Ghost", (short) 0 );
    }
}

//    public static final short CATEGORY_PLAYER = 0x0001;  // 0000000000000001 in binary
//    public static final short CATEGORY_SENSOR = 0x0002; // 0000000000000010 in binary
//    public static final short CATEGORY_OBJECT = 0x0008;
//    public static final short CATEGORY_WEAPON = 0x0010;
//    public static final short CATEGORY_COLLECTABLE = 0x0020;
//    public static final short CATEGORY_ATTACHEDOBJECT = 0x0040;
//    public static final short CATEGORY_SCENERY = 0x0004; //
//
//    // attempt to make things better, simpler (does not work):
////    public static final short MASK_PLAYER = CATEGORY_SENSOR |  CATEGORY_WEAPON | CATEGORY_COLLECTABLE | CATEGORY_ATTACHEDOBJECT | CATEGORY_SCENERY;
////    public static final short MASK_SENSOR = CATEGORY_PLAYER | CATEGORY_SCENERY;
////    public static final short MASK_OBJECT = CATEGORY_OBJECT | CATEGORY_SCENERY;
////    public static final short MASK_WEAPON = CATEGORY_PLAYER | CATEGORY_SCENERY;
////    public static final short MASK_COLLECTABLE = CATEGORY_PLAYER;
////    public static final short MASK_ATTACHEDOBJECT = CATEGORY_PLAYER;
////    public static final short MASK_SCENERY = CATEGORY_PLAYER| CATEGORY_SENSOR|CATEGORY_OBJECT|CATEGORY_WEAPON;
//
//    public static final short MASK_PLAYER = CATEGORY_WEAPON | CATEGORY_SENSOR | CATEGORY_COLLECTABLE | CATEGORY_SCENERY | CATEGORY_ATTACHEDOBJECT; // or ~CATEGORY_PLAYER
//    //public static final short MASK_MONSTER = CATEGORY_PLAYER | CATEGORY_SCENERY; // or ~CATEGORY_MONSTER
//    public static final short MASK_WEAPON = ~(CATEGORY_OBJECT & CATEGORY_WEAPON & CATEGORY_SENSOR);
//    public static final short MASK_ATTACHEDOBJECT = CATEGORY_PLAYER | CATEGORY_SENSOR; // | CATEGORY_WEAPON; // or ~CATEGORY_MONSTER
//    public static final short MASK_COLLECTABLE = CATEGORY_PLAYER;
//    public static final short MASK_SENSOR = CATEGORY_PLAYER | CATEGORY_SCENERY;
//
//    public static final short MASK_OBJECT = CATEGORY_SENSOR | CATEGORY_OBJECT | CATEGORY_SCENERY; // or ~CATEGORY_MONSTER
//    public static final short MASK_GHOST = 0;
//    public static final short MASK_SCENERY = ~CATEGORY_SCENERY;

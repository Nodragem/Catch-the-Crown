package com.mygdx.rope.objects.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Array;
import com.mygdx.rope.objects.ControlProcessor;
import com.mygdx.rope.objects.Updatable;
import com.mygdx.rope.objects.weapon.LanceManager;
import com.mygdx.rope.screens.GameScreenTournament;
import com.mygdx.rope.screens.Window;
import com.mygdx.rope.util.Constants.JUMP_STATE;
import com.mygdx.rope.util.Constants.MOVE_STATE;
import com.mygdx.rope.util.Constants.AWAKE_STATE;
import com.mygdx.rope.util.Constants.VIEW_DIRECTION;
import com.mygdx.rope.util.Constants;
import com.mygdx.rope.util.ContactData;
import com.mygdx.rope.util.InputHandler.InputProfile;

public class Player implements ControlProcessor, Updatable  {
    /**
     * Player Controller need a Body which implements 2 fixtures:
     * - one which is the actual shape of the object/character use for collision and physics
     * - one which is a sensor at the bottom of the object to detect if it is grounded
     */
    public final String TAG = "PlayerController";
    public String name;
    public float score;
    public GameScreenTournament gameScreen;
	private Body objBody;
    private Character character; // only character has feetsensor
    private InputProfile inputProfile;
    private ContactData sensorBuffer;
    private boolean jumpButtonPressed = false;
    private boolean pickUpButtonPressed = false;
    private LanceManager weapon;
    private int challengePressCount = 0; // the players compete in pressing the action buttons when they are picking or picked up.
    private Vector2 bodyVel =  new Vector2(0,0);
    private Vector2 bodyPos = new Vector2(0,0);
    private Float currentAimingAngle = 0.0f;
    private Vector2 currentMovingVector = new Vector2(0,0);
    private int jumpButtonCount;
    private boolean challengePressed = false;
    private Sound soundJump;
    private boolean wasPausePressed = false;
    private Window currentWindow;
    private Window previousWindow;
    public float inputCoolDown;

    public Player(String name, Character character, InputProfile inputProfile, GameScreenTournament gameScreen) {
        this.gameScreen = gameScreen;
        this.inputProfile = inputProfile;
        this.name = name;
        setCurrentWindow(null);
        setCharacter(character);
        soundJump = gameScreen.assetManager.getRandom("jump");
    }

    public void setCharacter(Character character) {
        // we should check the character has feet...
		this.character = character;
        this.objBody = character.body;
        character.setPlayer(this);
        weapon = new LanceManager(character.getGamescreen(), character, character.position, character.color_texture); // that weird t have that here! (I agree)
    }



    @Override
    public Character getCharacter() {
        return character;
    }

    public InputProfile getInputProfile() {
        return inputProfile;
    }

    public String getName() {
        return name;
    }

    public void setName(String playerName) {
        this.name = playerName;
    }

    public boolean hasTheCrown(){ return character.hasTheCrown();}

    public float getScore() {
        return score;
    }

    public void addScore(float ascore){
        this.score += ascore;
    }

    public float getCharacterLife() {
        return character.getLife();
    }

    public void processInputs(float deltaTime) {
        if(gameScreen.getStateGame() == Constants.GAME_STATE.PLAYED && inputCoolDown ==0) {
            if (character.awakeState == AWAKE_STATE.SLEEPING ||
                    character.moveState == MOVE_STATE.THROWED ||
                    character.awakeState == AWAKE_STATE.DEAD ||
                    character == null)
                return;
            bodyVel = objBody.getLinearVelocity();
            bodyPos = objBody.getPosition();
            currentMovingVector = inputProfile.getMovingVector();
            currentAimingAngle = inputProfile.getAimingAngle(new Vector2(character.position)
                    .add(character.dimension.x * 0.5f, character.dimension.y * 0.5f)); // try to provide the center of the object
            boolean playerExplicitAiming = currentAimingAngle != null;
            processAimingInput();
            processMovingInput();
            processJumpInput(inputProfile.getButtonState("Jump"));
            processLongAttackInput((inputProfile.getButtonState("LongAttack1") || inputProfile.getButtonState("LongAttack2")), playerExplicitAiming, deltaTime);
            processShortAttackInput((inputProfile.getButtonState("ShortAttack")), playerExplicitAiming, deltaTime);
            processActionInput(inputProfile.getButtonState("PickUp"));
            processPauseInput(inputProfile.getButtonState("Start"));
        } else if (inputCoolDown > 0){
            inputCoolDown -= 3.0*deltaTime;
        } else if (inputCoolDown < 0){
            inputCoolDown = 0;
        }
    }



    private void processPauseInput(boolean isPressed) {
        if (isPressed && !wasPausePressed) {
            wasPausePressed = true;
            if(gameScreen.getStateGame() != Constants.GAME_STATE.PAUSED) {
                gameScreen.openPauseWindow(name);
            }
        } else if (!isPressed && wasPausePressed) {
            wasPausePressed = false;
        }
    }

    private void processAimingInput() {
        if (currentAimingAngle == null)
            currentAimingAngle = MathUtils.PI * (character.viewDirection == VIEW_DIRECTION.RIGHT?0:1);
        character.viewDirection = (currentAimingAngle < MathUtils.PI/2 | currentAimingAngle > 3*MathUtils.PI/2)? VIEW_DIRECTION.RIGHT: VIEW_DIRECTION.LEFT;
        //character.viewDirection = (currentAimingAngle + MathUtils.PI/2 <MathUtils.PI)? VIEW_DIRECTION.RIGHT: VIEW_DIRECTION.LEFT;
    }

    private void processMovingInput(){
        if (currentMovingVector.len()>0.5) {
            accelerateToVelX( 5 * currentMovingVector.x, bodyVel.x );
        }
        else {
            accelerateToVelX( character.lastGroundedBody!=null? character.lastGroundedBody.getLinearVelocity().x : 0, bodyVel.x );
        }
    }

    private void processActionInput(boolean isPressed) {
        if(character.moveState == MOVE_STATE.PICKUPCHALLENGER){
            processChallenge(isPressed);
            return;
        }
        if(isPressed && !pickUpButtonPressed) { // basically, here you want a push request and not to pull.
            pickUpButtonPressed = true;
            //if (!character.hasCarriedObject()) { // is that not equivalent to "is  not MOVE_STATE.PICKINGUP" ?
            if (character.moveState == MOVE_STATE.NORMAL){
                sensorBuffer = (ContactData) character.currentHand.getUserData();
                Array<Fixture> touched = sensorBuffer.getTouchedFixtures();
                for (Fixture f : touched) {
                    if (f.getUserData() != null) {
                        character.setCarriedObject(f.getBody()); // this will change the movestate to PICKINGUP or PICKUPCHALLENGER / PICKUPCHALLENGED
                    }
                }
                sensorBuffer = null;
            } else if (character.moveState == MOVE_STATE.PICKINGUP){
                character.useCarriedBody(currentAimingAngle, 260.0f);
            }
        }
        else if(!isPressed){
            pickUpButtonPressed = false;
        }
    }

    private void processChallenge(boolean isPressed) {
        if(isPressed && !challengePressed) {
            challengePressed = true;
            challengePressCount += 1;
            Gdx.app.debug("Press Challenge", "nb: " + challengePressCount);
            if(character.moveState == MOVE_STATE.PICKUPCHALLENGED){
                if (challengePressCount >= Constants.MOVESTOFREE-2) {
//                    character.getCarrier().moveState = MOVE_STATE.THROWED;
                    character.setCarriedObject(character.getCarrier().getBody());
                    character.getCarrier().setCarriedObject(null);
                    character.moveState = MOVE_STATE.NORMAL;
//                    character.getCarrier().getBody().applyLinearImpulse(MathUtils.cos(currentAimingAngle) * 260.0f,
//                            MathUtils.sin(currentAimingAngle) * 260.0f, 0.5f, 0.5f, true);
                    character.useCarriedBody(Math.abs(currentAimingAngle)%MathUtils.PI+1 ,260.0f);
                    challengePressCount = 0;
                }
                Gdx.app.debug("jumpEscape", "nb " + jumpButtonCount);
            }
            else {
                if (challengePressCount >= Constants.MOVESTOFREE) {
                    character.moveState = MOVE_STATE.NORMAL;
                    character.useCarriedBody(currentAimingAngle ,260.0f ); // take care of throwing and changing the state of the other;
                    challengePressCount = 0;
                }
                Gdx.app.debug("smash it!", "nb " + jumpButtonCount);
            }
        }
        else if (!isPressed && challengePressed){
            challengePressed = false;
        }

    }

    private void processShortAttackInput(boolean isPressed, boolean isAiming, float deltaTime) {
        if (character.moveState == MOVE_STATE.PICKINGUP){
            return;
        }
        weapon.shortDistanceAttack(isPressed, deltaTime);
    }

    private void processLongAttackInput(boolean isPressed, boolean isAiming, float deltaTime) {
        if (character.moveState == MOVE_STATE.PICKINGUP){
            return;
        }
        weapon.longDistanceAttack(isPressed, bodyPos, currentAimingAngle, isAiming, deltaTime);
        /* the PlayerController is doing the job of a Child System, it is positionning the weapon on the character
        but it is not all, the LanceManager itself is applying a shift of (0.5, 0.5) units for aligning the Lance to the Player center.
        That's not readable/easy to maintain.
        */
    }

    private void processJumpInput(boolean isPressed) {
        if(character.moveState == MOVE_STATE.PICKUPCHALLENGED){
            processChallenge(isPressed);
            return;
        }
        if(isPressed){
            if (character.jumpState == JUMP_STATE.GROUNDED | character.jumpState == JUMP_STATE.IDLE) { // here we want the state.
                if(character.previousJumpState == JUMP_STATE.RISING)
                    return;
                bodyVel.y = 0; // we cancel any force on Y before to apply our force.
                // (especially useful for jumping immediately after a FALLINGSTATE, the grounded sensor is activated while we are still falling
                objBody.setLinearVelocity(bodyVel);
                character.getBody().applyLinearImpulse(new Vector2(0.0f, 116.0f), new Vector2(0,0), true );
                soundJump.play();
            }
        }
        else { // Jump Button is not Pressed
            if(jumpButtonPressed)
                jumpButtonPressed = false;
            if(character.jumpState == JUMP_STATE.RISING ) { // this block may should be outside the condition "if (jumpButtonPressed)", especially if you want to respect a minimal jump size.
                bodyVel.y = 0; // like that we come back to Falling state
                objBody.setLinearVelocity(bodyVel);
            }
        }
    }

    // weird to have that here:
    public void accelerateToVelX(float tv, float cv){ // tv: desired velocity, cv: current velocity
        float impulse = objBody.getMass() * (tv - cv);
        objBody.applyLinearImpulse(new Vector2(impulse, 0), objBody.getWorldCenter(), true);
    }

    public void accelerateToVelY(float tv, float cv){ // Those two functions should be in a kind of Utils library, or in the Move Component
        float impulse = objBody.getMass() * (tv - cv);
        objBody.applyLinearImpulse(new Vector2(0, impulse), objBody.getWorldCenter(), true);

    }

    @Override
    public boolean update(float deltaTime) {
        processInputs(deltaTime);
        if (inputCoolDown != 0)
            inputCoolDown -= deltaTime*3;
        if (inputCoolDown <0)
            inputCoolDown = 0;
        return false;
    }

    public void setCurrentWindow(Window currentWindow) {
        this.previousWindow = this.currentWindow;
        this.currentWindow = currentWindow;
    }

    public boolean toPreviousWindow(){
        currentWindow = previousWindow;
        if (currentWindow == null)
            return false;
        else
            return true;
    }

    public void savePreviousWindow(){
        previousWindow = currentWindow;
    }
}

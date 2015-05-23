package com.mygdx.rope.objects.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Array;
import com.mygdx.rope.objects.ControlProcessor;
import com.mygdx.rope.objects.Updatable;
import com.mygdx.rope.objects.weapon.LanceManager;
import com.mygdx.rope.screens.GameScreen;
import com.mygdx.rope.util.Constants.JUMP_STATE;
import com.mygdx.rope.util.Constants.MOVE_STATE;
import com.mygdx.rope.util.Constants.AWAKE_STATE;
import com.mygdx.rope.util.Constants.VIEW_DIRECTION;
import com.mygdx.rope.util.Constants.INPUTSTATE;
import com.mygdx.rope.util.Constants;
import com.mygdx.rope.util.ContactData;
import com.mygdx.rope.util.InputHandler.InputProfile;
import com.mygdx.rope.util.Xbox360Map;

import java.util.HashMap;

public class Player implements ControlProcessor, Updatable  {
    /**
     * Player Controller need a Body which implements 2 fixtures:
     * - one which is the actual shape of the object/character use for collision and physics
     * - one which is a sensor at the bottom of the object to detect if it is grounded
     */
    public final String TAG = "PlayerController";
    public String name;
    public float score;
    public GameScreen gameScreen;
	private Body objBody;
    private Character character; // only character has feetsensor
    private InputProfile inputProfile;
    private ContactData sensorBuffer;
    private boolean jumpButtonPressed = false;
    private boolean pickUpButtonPressed = false;
    private LanceManager weapon;
    private Vector2 bodyVel =  new Vector2(0,0);
    private Vector2 bodyPos = new Vector2(0,0);
    private Float currentAimingAngle = 0.0f;
    private Vector2 currentMovingVector = new Vector2(0,0);
    private int jumpButtonCount;

    public Player(String name, Character character, InputProfile inputProfile, GameScreen gameScreen)  {
        this.gameScreen =gameScreen;
        this.inputProfile = inputProfile;
        this.name = name;
        setCharacter(character);
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
        if (character.awakeState == AWAKE_STATE.SLEEPING ||
                character.moveState == MOVE_STATE.THROWED ||
                character.awakeState == AWAKE_STATE.DEAD  ||
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
        processAttackInput((inputProfile.getButtonState("Attack1") || inputProfile.getButtonState("Attack2")), playerExplicitAiming, deltaTime);
        processActionInput(inputProfile.getButtonState("PickUp"));
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
        if(character.moveState == MOVE_STATE.PICKEDUP){
            return;
        }
        if(isPressed && !pickUpButtonPressed) { // basically, here you want a push request and not to pull.
            pickUpButtonPressed = true;
            if (!character.hasCarriedObject()) { // is that not equivalent to "is  not MOVE_STATE.PICKINGUP"
                sensorBuffer = (ContactData) character.currentHand.getUserData();
                Array<Fixture> touched = sensorBuffer.getTouchedFixtures();
                for (Fixture f : touched) {
                    if (f.getUserData() != null) {
                        character.setCarriedObject(f.getBody());
                    }
                }
                sensorBuffer = null;
            } else {
                character.useCarriedBody(currentAimingAngle, 260.0f);
            }
        }
        else if(!isPressed){
            pickUpButtonPressed = false;
        }
    }

    private void processAttackInput(boolean isPressed, boolean isAiming, float deltaTime) {
        if (character.moveState == MOVE_STATE.PINCKINGUP){
            return;
        }
        weapon.setAttack(isPressed, bodyPos, currentAimingAngle, isAiming, deltaTime);
        /* the PlayerController is doing the job of a Child System, it is positionning the weapon on the character
        but it is not all, the LanceManager itself is applying a shift of (0.5, 0.5) units for aligning the Lance to the Player center.
        That's not readable/easy to maintain.
        */
    }

    private void processJumpInput(boolean isPressed) {
        if(isPressed){
            if(character.moveState == MOVE_STATE.PICKEDUP){
                if (!jumpButtonPressed) { // here we would like to get a push request (we don't care of following the button state)
                    jumpButtonPressed = true;
                    jumpButtonCount += 1;
                }
                if(jumpButtonCount>Constants.MOVESTOFREE){
                    character.getCarrier().useCarriedBody(90.0f*MathUtils.degreesToRadians, 50.0f);
                    jumpButtonCount = 0;
                }
                Gdx.app.debug("jumpEscape", "nb "+jumpButtonCount);
            }
            else if (character.jumpState == JUMP_STATE.GROUNDED | character.jumpState == JUMP_STATE.IDLE) { // here we want the state.
                bodyVel.y = 0; // we cancel any force on Y before to apply our force.
                // (especially useful for jumping immediately after a FALLINGSTATE, the grounded sensor is activated while we are still falling
                objBody.setLinearVelocity(bodyVel);
                character.getBody().applyLinearImpulse(new Vector2(0.0f, 116.0f), new Vector2(0,0), true );
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
        return false;
    }
}

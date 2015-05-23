package com.mygdx.rope.objects.collectable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.rope.objects.GameObject;
import com.mygdx.rope.objects.characters.Character;
import com.mygdx.rope.objects.traps.HubInterface;
import com.mygdx.rope.objects.traps.SimpleHub;
import com.mygdx.rope.objects.traps.Triggerable;
import com.mygdx.rope.screens.GameScreen;
import com.mygdx.rope.util.Constants;
import com.mygdx.rope.util.ContactData;

/**
 * Created by Nodragem on 08/06/2014.
 */
public class Coins extends GameObject implements Triggerable {
    private Array<Vector2> positionCollectables = new Array<Vector2>();
    //private Array<Boolean> activated = new Array();
    private Array<Float> respawnTimeCollectables = new Array<Float>();
    private Array<Float> sizeCollectables = new Array<Float>();
    private Array<Animation> animationCollectables=new Array<Animation>();
    private Array <Float> valueCollectables = new Array<Float>();
    private FixtureDef coinFixture;
    private boolean activationAllowed;
    private PolygonShape coinBox;
    private Character characterWithCrown;
    private Filter coinFilter;
    private Crown linkedCrown;
    private float coinsRespawnTime; // time before to respawn
    private boolean defaultON;
    final static JsonValue collectableInfo = new JsonReader().parse(Gdx.files.internal("object_types.json")).get("collectable_type");
    static ArrayMap<String, Animation> animOfCollectables;

    // we may want it to be compatible with the trap system
    public Coins(GameScreen game, Vector2 position, TiledMapTileLayer collectableMap, ArrayMap<String, HubInterface> HubList) {
        super(game, position, new Vector2(0.5f,0.5f));
        activationAllowed = false;
        coinsRespawnTime = Constants.COINSTIME;
        defaultON = Boolean.valueOf(collectableMap.getProperties().get("defaultON", "true", String.class));
        if (HubList != null && collectableMap.getProperties().get("Parent", null, String.class) != null){
            Gdx.app.debug("Coin Debug: ", "HubList: " + HubList);
            HubInterface hub = HubList.get(collectableMap.getProperties().get("Parent", null, String.class));
            if (hub != null) {
                hub.addTriggerable(this);
                Gdx.app.debug("Coin Debug: ", "Coin Layer added to Hub: ");
            }
        }

        if (animOfCollectables == null){
            animOfCollectables = new ArrayMap<String, Animation>();
            Array<TextureAtlas.AtlasRegion> regions = null;
            for (JsonValue collectable : collectableInfo) {
                Gdx.app.debug("Coins Class:", "collectable.name():  " + collectable.name());
                regions = atlas.findRegions(collectable.name()+"_main");
                animOfCollectables.put(collectable.name(), new Animation(1.0f/2.0f, regions, Animation.PlayMode.LOOP));
            }
        } else {
            Gdx.app.debug("Coin Debug: ", ""+animOfCollectables);
            Gdx.app.debug("Coin Debug: ", ""+collectableInfo);
        }
        Gdx.app.debug("Coin Debug: ", "  DefaultON: " + defaultON);
        stateTime = 0;

        for (int i = 0; i < collectableMap.getWidth(); i++) {
            for (int j = 0; j < collectableMap.getHeight(); j++) {
                TiledMapTileLayer.Cell cell = collectableMap.getCell(i,j);
                if (cell != null && cell.getTile() != null){
                    this.addCoin(i, j, cell.getTile().getProperties().get("type", "coin", String.class));
                }
            }
        }
    }

    public void initFilter() {
        coinFilter = new Filter();
        coinFilter.categoryBits = Constants.CATEGORY.get("Collectable");
        coinFilter.maskBits = Constants.MASK.get("Collectable");
    }

    public void initFixture() {
        body.setType(BodyDef.BodyType.StaticBody);
        coinBox = new PolygonShape();
        coinFixture = new FixtureDef();
        coinFixture.shape = coinBox;
        coinFixture.density = 50;
        coinFixture.restitution = 0.0f;
        coinFixture.friction = 0.5f;
        coinFixture.isSensor = true;
    }

    public void setLinkedCrown(Crown linkedCrown) {
        this.linkedCrown = linkedCrown;
    }

    public Crown getLinkedCrown() {
        return linkedCrown;
    }

    public void dispose(){
        coinBox.dispose();
    }

    public void initAnimation() {
    }

    public void addCoin(int x, int y, String typeName){
        JsonValue infoType = collectableInfo.get(typeName);
        Gdx.app.debug("addCoin: ", "infoType: "+ infoType);
        float size = infoType.getFloat("scalex", 0.5f);
        float corrected_x = x + (1 - size)/2f;
        float corrected_y = y + (1 - size)/2f;
        positionCollectables.add(new Vector2(corrected_x, corrected_y));
        sizeCollectables.add(size);
        respawnTimeCollectables.add(0.0f);
        animationCollectables.add(animOfCollectables.get(typeName));
        valueCollectables.add(infoType.getFloat("value", 10));
        coinBox.setAsBox(size / 2.0f, size / 2.0f, new Vector2(corrected_x+size/2.0f, corrected_y+size/2.0f), 0);
        coinFixture.shape = coinBox;
        Fixture f = this.body.createFixture(coinFixture);
        f.setFilterData(coinFilter);
        new ContactData(1, f);
    }

    private void addSensor(float x, float y) {

    }

    public void removeCoin(int index){
        positionCollectables.removeIndex(index);
        //activated.removeIndex(index);
        respawnTimeCollectables.removeIndex(index);
        animationCollectables.removeIndex(index);
        valueCollectables.removeIndex(index);

    }

    public void allowActivation(boolean b){
//        for (int i = 0; i < activated.size; i++) {
//            activated.set(i, switcher);
//        }
        activationAllowed = b;
    }

    public boolean update(float deltaTime){
        characterWithCrown = linkedCrown.getCarrier();
        if ( characterWithCrown!= null) {
            for (int i = 0; i < respawnTimeCollectables.size; i++) {
                if (respawnTimeCollectables.get(i) > 0 ) {
                    ContactData data = (ContactData) body.getFixtureList().get(i).getUserData();
                    if (data.isTouchedBy(characterWithCrown.getBody().getFixtureList().get(0)) == true) {
                        respawnTimeCollectables.set(i, coinsRespawnTime);
                        distributeGold(valueCollectables.get(i));
                    }
                }
                else {
                    respawnTimeCollectables.set(i, respawnTimeCollectables.get(i)+1.0f*deltaTime);
                }
            }
        }
        stateTime += deltaTime;
        return false;

    }


    public void distributeGold(float gold){
        linkedCrown.addGoldValue(gold * Constants.CROWNGOLDRATE); // the part of gold which is not really to the player
        characterWithCrown.getPlayer().addScore(gold); // we remove the extraScore when he drops it
    }

    public void render(SpriteBatch batch) {
        if(isVisible) {
            for (int i = 0; i < positionCollectables.size; i++) {
                if (respawnTimeCollectables.get(i) > 0) {
                    TextureRegion reg = null;
                    //Gdx.app.debug(TAG, "Key Frame: " + stateTime);
                    reg = animationCollectables.get(i).getKeyFrame(stateTime);
                    batch.draw(reg.getTexture(), // we called this long draw() method just to get access the flip argument...
                            positionCollectables.get(i).x, positionCollectables.get(i).y,
                            0, 0,
                            sizeCollectables.get(i), sizeCollectables.get(i),
                            1, 1,
                            rotation,
                            reg.getRegionX(), reg.getRegionY(),
                            reg.getRegionWidth(), reg.getRegionHeight(),
                            viewDirection == Constants.VIEW_DIRECTION.LEFT, false);
                    //batch.setColor(1, 1, 1, 1);
                }
            }
        }
    }

    @Override
    public void reset() {
        setActivate(defaultON);
    }

    @Override
    public boolean isDefaultON() {
        return defaultON;
    }

    @Override
    public void triggerONActions(HubInterface hub) {
        goToActivation();
    }

    @Override
    public void triggerOFFActions(HubInterface hub) {
        goToDesactivation();
    }
}

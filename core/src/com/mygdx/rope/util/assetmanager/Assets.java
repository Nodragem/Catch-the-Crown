package com.mygdx.rope.util.assetmanager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AssetLoader;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.JsonValue.JsonIterator;


public class Assets implements Disposable, AssetErrorListener {

	private static final String TAG = "Assets";
	private AssetManager manager;
	private ObjectMap<String, Array<Asset>> groups;

	public Assets() {

		manager = new AssetManager();
		manager.setErrorListener(this);
		// manager.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
		//manager.setLoader(SpriteAnimationData.class, new SpriteAnimationLoader(new InternalFileHandleResolver()));


	}

	public AssetLoader<?, ?> getLoader(Class<?> type) {
		return manager.getLoader(type);
	}

	public void loadGroup(String groupName) {
		Gdx.app.log(TAG, "loading group " + groupName);

		Array<Asset> assets = groups.get(groupName, null);

		if (assets != null) {
			for (Asset asset : assets) {
				Gdx.app.log(TAG, "loading..." + asset.path);
				//manager.load(asset.path, asset.type); //, asset.parameters);
				manager.load(asset.path, asset.type, asset.parameters); // you have to avoid to write: parameters: {} in the json file.
			}
		}
		else {
			Gdx.app.log(TAG, "error loading group " + groupName + ", not found");
		}
	}

	public void unloadGroup(String groupName) {
		Gdx.app.log(TAG, "unloading group " + groupName);

		Array<Asset> assets = groups.get(groupName, null);

		if (assets != null) {
			for (Asset asset : assets) {
				if (manager.isLoaded(asset.path, asset.type)) {
					manager.unload(asset.path);
				}
			}
		}
		else {
			Gdx.app.log(TAG, "error unloading group " + groupName + ", not found");
		}
	}

    public synchronized <T> T get(String groupName, int ID) {
        Asset asset = groups.get(groupName).get(ID);
        return (T) manager.get(asset.path, asset.type);
    }

    public synchronized <T> T getRandom(String groupName) {
        Asset asset = groups.get(groupName).random();
        return (T) manager.get(asset.path, asset.type);
    }

	public synchronized <T> T get(String fileName) {
		return manager.get(fileName);
	}

	public synchronized <T> T get(String fileName, Class<T> type) {
        return manager.get(fileName, type);
	}

	public <T> boolean isLoaded(String fileName, Class<T> type) {
		return manager.isLoaded(fileName, type);
	}

	public boolean update() {
		return manager.update();
	}

	public void finishLoading() {
		manager.finishLoading();
	}

	public float getProgress() {
		return manager.getProgress();
	}

	@Override
	public void dispose() {
		Gdx.app.log(TAG, "shutting down");
		manager.dispose();
	}

	@Override
	public void error(AssetDescriptor asset, Throwable throwable) {
		Gdx.app.log(TAG, "error loading " + asset.fileName + " message: " + throwable.getMessage());
	}

	public void loadGroups(String assetFile) {
		groups = new ObjectMap<String, Array<Asset>>();


		Gdx.app.log(TAG, "loading file " + assetFile);

		try {
			Json json = new Json();
			JsonReader reader = new JsonReader();
			JsonValue root = reader.parse(Gdx.files.internal(assetFile).reader("UTF-8")); // modified
//            Gdx.app.log(TAG, "group file has been parsed!");
			JsonIterator groupIt = root.iterator();

			while (groupIt.hasNext()) {
				JsonValue groupValue = groupIt.next();

				if (groups.containsKey(groupValue.name)) {
					Gdx.app.log(TAG, "group " + groupValue.name + " already exists, skipping");
					continue;
				}
				
				Gdx.app.log(TAG, "registering group " + groupValue.name);

				Array<Asset> assets = new Array<Asset>();

				JsonIterator assetIt = groupValue.iterator();

				while (assetIt.hasNext()) {
					JsonValue assetValue = assetIt.next();
//                    Gdx.app.log(TAG, "Adding AssetValue: \n" + assetValue.prettyPrint(JsonWriter.OutputType.json, 0)); // modified
					Asset asset = json.fromJson(Asset.class, assetValue.prettyPrint(JsonWriter.OutputType.json, 0)); // modified
//                    Gdx.app.log(TAG, "AssetValue added...");
					assets.add(asset);
//                    Gdx.app.log(TAG, "Adding Asset...");
				}

				groups.put(groupValue.name, assets);
//                Gdx.app.log(TAG, "Adding Asset...");
				loadGroup(groupValue.name); // modified from original
			}
		}
		catch (Exception e) {
			Gdx.app.log(TAG, "error loading file " + assetFile + " " + e.getMessage());
		}
	}
}
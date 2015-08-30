package com.mygdx.rope.util.assetmanager;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

public class Asset implements Json.Serializable  {
	public Integer id;
	public Class<?> type;
	public String path;
	public AssetLoaderParameters parameters;
	
	@Override
	public void write(Json json) {
		json.writeValue("id", id);
		json.writeValue("assetType", type.getName());
		json.writeValue("path", path);
		json.writeValue("parameters", parameters);
	}
	
	@Override
	public void read(Json json, JsonValue jsonData) {

		id = jsonData.get("id").asInt();
		try {
			type = Class.forName(jsonData.get("type").asString());
            //Gdx.app.log("ME", ""+type);
		} catch (Exception e) {
			type = null;
		}
		
		path = jsonData.get("path").asString();
		
		JsonValue parametersValue = jsonData.get("parameters");
//		parameters = parametersValue != null ? json.fromJson(AssetLoaderParameters.class, parametersValue.toString()) : null;
		parameters = parametersValue != null ? json.fromJson(AssetLoaderParameters.class,
				parametersValue.prettyPrint(JsonWriter.OutputType.json, 0)) : null; // modified
		//Gdx.app.debug("TAG", ""+parameters);
	}
}
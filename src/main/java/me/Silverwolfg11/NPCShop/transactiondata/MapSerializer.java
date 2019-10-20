package me.Silverwolfg11.NPCShop.transactiondata;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class MapSerializer{

    public static HashMap<Material, ItemTransaction> deserialize(String json) {
        JsonElement element = new JsonParser().parse(json);

        HashMap<Material, ItemTransaction> map = new HashMap<>();

        JsonObject jsonObj = (JsonObject) element;
        for (Map.Entry<String, JsonElement> entry : jsonObj.entrySet()) {
            ItemTransaction transaction = new ItemTransaction();

            JsonArray array = entry.getValue().getAsJsonArray();

            transaction.bought(array.get(0).getAsInt());
            transaction.sold(array.get(1).getAsInt());

            map.put(Material.valueOf(entry.getKey()), transaction);
        }

        return map;
    }

    public static String serialize(Map<Material, ItemTransaction> map) {
        JsonObject mapObject = new JsonObject();

        for (Map.Entry<Material, ItemTransaction> entry : map.entrySet()) {
            JsonArray array = new JsonArray();
            array.add(entry.getValue().getBuys());
            array.add(entry.getValue().getSells());

            mapObject.add(entry.getKey().name(), array);
        }

        return mapObject.toString();
    }
}

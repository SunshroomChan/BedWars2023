package com.tomkeuper.bedwars.connectionmanager.redis;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.arena.Arena;
import com.tomkeuper.bedwars.connectionmanager.LoadedUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.JedisPubSub;

public class RedisPubSubListener extends JedisPubSub {
    private final String BW_CHANNEL;
    public RedisPubSubListener(String channel) {
        this.BW_CHANNEL = channel;
    }

    @Override
    public void onMessage(String channel, String message) {
        if(channel.equals(BW_CHANNEL)) {
            final JsonObject json;
            try {
                json = new JsonParser().parse(message).getAsJsonObject();
                BedWars.debug("incoming json message: " + json.toString());
            } catch (JsonSyntaxException e) {
                BedWars.plugin.getLogger().warning("Received bad data from redis message channel " + BW_CHANNEL);
                return;
            }
            if (!json.has("type")) return;
            switch (json.get("type").getAsString().toUpperCase()) {
                case "PLD":
                    new LoadedUser(json.get("uuid").getAsString(), json.get("arena_identifier").getAsString(), json.get("lang_iso").getAsString(), json.get("target").getAsString());
                    break;
                case "Q":
                    Player p = Bukkit.getPlayer(json.get("name").getAsString());
                    if (p != null && p.isOnline()){
                        IArena a = Arena.getArenaByPlayer(p);
                        if (a != null) {
                            JsonObject jo = new JsonObject();
                            jo.addProperty("type", "Q");
                            jo.addProperty("name", p.getName());
                            jo.addProperty("requester", json.get("requester").getAsString());
                            jo.addProperty("server_name", BedWars.config.getString(ConfigPath.GENERAL_CONFIGURATION_BUNGEE_OPTION_SERVER_ID));
                            jo.addProperty("arena_id", a.getWorldName());
                        }
                    }
                    break;
            }
        }
    }
}

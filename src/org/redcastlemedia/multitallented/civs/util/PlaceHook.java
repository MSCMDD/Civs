package org.redcastlemedia.multitallented.civs.util;


import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

public class PlaceHook extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return "civs";
    }

    @Override
    public String getAuthor() {
        return "Multitallented";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        if (player == null) {
            return "";
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        return getReplacement(civilian);
    }
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        return getReplacement(civilian);
    }

    private String getReplacement(Civilian civilian) {
        Town town = TownManager.getInstance().isOwnerOfATown(civilian);
        if (town != null) {
            return town.getName();
        } else {
            int highestPopulation = 0;
            Town highestTown = null;
            for (Town to : TownManager.getInstance().getTowns()) {
                if (!to.getPeople().containsKey(civilian.getUuid())) {
                    continue;
                }
                int pop = to.getPopulation();
                if (pop > highestPopulation) {
                    highestTown = to;
                    highestPopulation = pop;
                }
            }
            return highestTown == null ? "" : highestTown.getName();
        }
    }
}

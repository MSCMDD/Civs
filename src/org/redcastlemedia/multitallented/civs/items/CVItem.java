package org.redcastlemedia.multitallented.civs.items;

import lombok.Getter;
import lombok.Setter;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.NBTItem;
import org.apache.commons.lang.ObjectUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Multi
 */
public class CVItem {
    private Material mat;
    private int qty;
    private final double chance;
    private String displayName = null;

    @Getter @Setter
    private String mmoItemType = null;

    @Setter @Getter
    private String mmoItemName = null;

    @Getter @Setter
    private String group = null;
    private List<String> lore = new ArrayList<>();

    public CVItem(Material mat, int qty, int chance, String displayName, List<String> lore) {
        this.mat = mat;
        this.qty = qty;
        this.chance = ((double) chance) / 100;
        this.displayName = displayName;
        this.lore = lore;
    }

    public CVItem(Material mat, int qty, int chance, String displayName) {
        this.mat = mat;
        this.qty = qty;
        this.chance = ((double) chance) / 100;
        this.displayName = displayName;
    }

    public CVItem(Material mat, int qty, int chance) {
        this.mat = mat;
        this.qty = qty;
        this.chance = ((double) chance) / 100;
    }

    public CVItem(Material mat, int qty) {
        this.mat = mat;
        this.qty = qty;
        this.chance = 1;
    }

    public static CVItem createCVItemFromString(String materialString) {
        return createCVItemFromString(ConfigManager.getInstance().getDefaultLanguage(), materialString);
    }

    public static CVItem createCVItemFromString(String locale, String materialString)  {
        boolean isMMOItem = materialString.contains("mi:");
        if (isMMOItem) {
            materialString = materialString.replace("mi:", "");
        }

        String quantityString = "1";
        String chanceString = "100";
        String nameString = null;
        String mmoType = "";
        Material mat;

        String[] splitString;


        for (;;) {
            int asteriskIndex = materialString.indexOf("*");
            int percentIndex = materialString.indexOf("%");
            int nameIndex = materialString.indexOf(".");
            if (asteriskIndex != -1 && asteriskIndex > percentIndex && asteriskIndex > nameIndex) {
                splitString = materialString.split("\\*");
                quantityString = splitString[splitString.length - 1];
                materialString = splitString[0];
            } else if (percentIndex != -1 && percentIndex > asteriskIndex && percentIndex > nameIndex) {
                splitString = materialString.split("%");
                chanceString = splitString[splitString.length - 1];
                materialString = splitString[0];
            } else if (nameIndex != -1 && nameIndex > percentIndex && nameIndex > asteriskIndex) {
                splitString = materialString.split("\\.");
                nameString = splitString[splitString.length -1];
                materialString = splitString[0];
            } else {
                if (isMMOItem) {
                    mmoType = materialString;
                    mat = Material.STONE;
                } else {
                    mat = getMaterialFromString(materialString);
                }
                break;
            }
        }


        if (mat == null) {
            mat = Material.STONE;
            Civs.logger.severe(Civs.getPrefix() + "Unable to parse material " + materialString);
        }
        int quantity = Integer.parseInt(quantityString);
        int chance = Integer.parseInt(chanceString);

        if (isMMOItem) {
            if (Civs.mmoItems == null) {
                Civs.logger.severe(Civs.getPrefix() + "Unable to create MMOItem because MMOItems is disabled");
                return new CVItem(mat, quantity, chance);
            }
            Type mmoItemType = Civs.mmoItems.getTypes().get(mmoType);
            if (mmoItemType == null) {
                Civs.logger.severe(Civs.getPrefix() + "MMOItem type " + mmoType + " not found");
                return new CVItem(mat, quantity, chance);
            }
            if (nameString == null) {
                Civs.logger.severe(Civs.getPrefix() + "Invalid MMOItem " + mmoType + " did not provide item name");
                return new CVItem(mat, quantity, chance);
            }
            MMOItem mmoItem = Civs.mmoItems.getItems().getMMOItem(mmoItemType, nameString);
            ItemStack item = mmoItem.newBuilder().build();
            CVItem cvItem = new CVItem(item.getType(), quantity, chance, item.getItemMeta().getDisplayName(),
                    item.getItemMeta().getLore());
            cvItem.mmoItemName = nameString;
            cvItem.mmoItemType = mmoType;
            return cvItem;
        }
        if (nameString == null) {
            return new CVItem(mat, quantity, chance);
        } else {
            String displayName = LocaleManager.getInstance().getTranslation(locale, "item-" + nameString + "-name");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.BLACK + nameString);
            lore.addAll(Util.textWrap(ConfigManager.getInstance().getCivsItemPrefix() +
                    LocaleManager.getInstance().getTranslation(locale, "item-" + nameString + "-desc")));
            return new CVItem(mat, quantity, chance, displayName, lore);
        }
    }

    public static boolean isCustomItem(ItemStack itemStack) {
        return itemStack != null && itemStack.hasItemMeta() &&
                itemStack.getItemMeta() != null &&
                isCustomItem(itemStack.getItemMeta().getLore());
    }

    public static void translateItem(String locale, ItemStack itemStack) {
        String itemDisplayName = itemStack.getItemMeta().getLore().get(1);
        String nameString = ChatColor.stripColor(itemDisplayName
                .replace(ChatColor.stripColor(ConfigManager.getInstance().getCivsItemPrefix()), "").toLowerCase());
        String displayName = LocaleManager.getInstance().getTranslation(locale, "item-" + nameString + "-name");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.BLACK + nameString);
        lore.addAll(Util.textWrap(
                LocaleManager.getInstance().getTranslation(locale, "item-" + nameString + "-desc")));
        itemStack.getItemMeta().setDisplayName(displayName);
        itemStack.getItemMeta().setLore(lore);
    }

    public boolean isCustomItem() {
        return isCustomItem(lore);
    }

    private static boolean isCustomItem(List<String> lore) {
        return lore != null && !lore.isEmpty() &&
                LocaleManager.getInstance().hasTranslation(
                        ConfigManager.getInstance().getDefaultLanguage(),
                        "item-" + ChatColor.stripColor(lore.get(0)) + "-name");
    }

    private static Material getMaterialFromString(String materialString) {
        return Material.valueOf(materialString.replaceAll(" ", "_").toUpperCase());
    }

    public boolean equivalentItem(ItemStack iss) {
        return equivalentItem(iss, false);
    }

    public static boolean isCivsItem(ItemStack is) {
        if (is == null || !is.hasItemMeta()) {
            return false;
        }
        ItemMeta im = is.getItemMeta();
        if (im == null || im.getDisplayName() == null) {
            return false;
        }
        if (im.getLore() == null || im.getLore().size() < 2 || ItemManager.getInstance().getItemType(im.getLore().get(1)) == null) {
            return false;
        }
        return true;
    }

    public CivItem getCivItem() {
        if (lore.size() < 2) {
            return null;
        }
        return ItemManager.getInstance().getItemType(lore.get(1));
    }

    public static CVItem createFromItemStack(ItemStack is) {
        if (is.hasItemMeta() && is.getItemMeta().getDisplayName() != null &&
                !"".equals(is.getItemMeta().getDisplayName())) {
            if (is.getItemMeta().getLore() != null) {
                return new CVItem(is.getType(),is.getAmount(), 100, is.getItemMeta().getDisplayName(), is.getItemMeta().getLore());
            } else {
                return new CVItem(is.getType(),is.getAmount(), 100, is.getItemMeta().getDisplayName());
            }
        }
        return new CVItem(is.getType(),is.getAmount());
    }
    public static List<CVItem> createListFromString(String input) {
        String groupName = null;
        group: if (input.contains("g:")) {
            String itemGroup = null;
            String params = null;
            for (String currKey : ConfigManager.getInstance().getItemGroups().keySet()) {
                if (input.matches("g:" + currKey + "\\*.*")) {
                    groupName = currKey;
                    itemGroup = ConfigManager.getInstance().getItemGroups().get(groupName);
                    params = input.replaceAll("g:" + currKey + "(?=\\*)", "");
                }
            }
            if (groupName == null || itemGroup == null || params == null) {
                break group;
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (String chunk : itemGroup.split(",")) {
                stringBuilder.append(chunk);
                stringBuilder.append(params);
                stringBuilder.append(",");
            }
            stringBuilder.substring(stringBuilder.length() - 1);
            input = stringBuilder.toString();
        }
        List<CVItem> reqs = new ArrayList<>();
        for (String req : input.split(",")) {
            CVItem cvItem = createCVItemFromString(req);
            if (groupName != null) {
                cvItem.setGroup(groupName);
            }
            reqs.add(cvItem);
        }
        return reqs;
    }

    public ItemStack createItemStack() {
        if (mmoItemType != null && mmoItemName != null && Civs.mmoItems != null) {
            Type mmoType = Civs.mmoItems.getTypes().get(mmoItemType);
            MMOItem mmoItem = Civs.mmoItems.getItems().getMMOItem(mmoType, mmoItemName);
            return mmoItem.newBuilder().build();
        }

        ItemStack is = new ItemStack(mat, qty);
        if (displayName != null || (lore != null && !lore.isEmpty())) {
            if (!is.hasItemMeta()) {
                is.setItemMeta(Bukkit.getItemFactory().getItemMeta(is.getType()));
            }
            ItemMeta im = is.getItemMeta();
            if (displayName != null) {
                im.setDisplayName(displayName);
            }
            if (lore == null) {
                lore = new ArrayList<>();
            } else if (!lore.isEmpty()) {
                im.setLore(lore);
            }
            im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            is.setItemMeta(im);
        }
        return is;
    }

    public boolean equivalentItem(ItemStack iss, boolean useDisplayName) {
        return equivalentItem(iss, useDisplayName, false);
    }

    public boolean equivalentItem(ItemStack iss, boolean useDisplayName, boolean lore) {
        if (mmoItemType != null && mmoItemName != null) {
            NBTItem nbtItem = NBTItem.get(iss);
            if (!nbtItem.hasType()) {
                return false;
            }
            if (!mmoItemType.equalsIgnoreCase(nbtItem.getString("MMOITEMS_ITEM_TYPE"))) {
                return false;
            }
            if (!mmoItemName.equalsIgnoreCase(nbtItem.getString("MMOITEMS_ITEM_ID"))) {
                return false;
            }
            return true;
        }
        if (useDisplayName) {
            boolean nullComparison = getDisplayName() == null;
            boolean hasItemMeta = iss.hasItemMeta();
            boolean issNullName = hasItemMeta && iss.getItemMeta().getDisplayName() == null;
            boolean nullName = !hasItemMeta || issNullName;

            boolean equivalentNames = (nullComparison && nullName) || ((!nullComparison && !nullName) && iss.getItemMeta().getDisplayName().equals(getDisplayName()));

            return iss.getType() == getMat() &&
                    equivalentNames;
        } else {
            return iss.getType() == getMat();
        }
    }

    public boolean equivalentCVItem(CVItem iss) {
        return equivalentCVItem(iss, false);
    }

    public boolean equivalentCVItem(CVItem iss, boolean useDisplayName) {
        if (!ObjectUtils.equals(mmoItemName, iss.getMmoItemName()) ||
                !ObjectUtils.equals(mmoItemType, iss.getMmoItemType())) {
            return false;
        }
        if (mmoItemType != null) {
            return true;
        }

        if (useDisplayName) {
            return iss.getMat() == getMat() &&
                    ((getDisplayName() == null && iss.getDisplayName() == null) || getDisplayName().equals(iss.getDisplayName()));
        } else {
            return iss.getMat() == getMat();
        }
    }

    public List<String> getLore() {
        return lore;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setMat(Material mat) {
        this.mat = mat;
    }

    public Material getMat() {
        return mat;
    }
    public int getQty() {
        return qty;
    }
    public double getChance() {
        return chance;
    }
    public void setQty(int qty) {
        this.qty = qty;
    }
    public void setDisplayName(String name) {
        this.displayName = name;
    }
    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    @Override
    public CVItem clone() {
        CVItem cvItem = new CVItem(mat, qty, (int) chance, displayName, new ArrayList<>(lore));
        cvItem.mmoItemName = mmoItemName;
        cvItem.mmoItemType = mmoItemType;
        cvItem.setGroup(group);
        return cvItem;
    }
}

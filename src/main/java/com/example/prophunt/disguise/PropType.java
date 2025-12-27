package com.example.prophunt.disguise;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.util.Objects;

/**
 * Represents a type of prop that players can disguise as.
 */
public class PropType {

    private final Material material;
    private final BlockData blockData;
    private final PropSize size;
    private final String displayName;

    /**
     * Creates a new PropType with default block data.
     *
     * @param material the block material
     * @param size the prop size category
     */
    public PropType(Material material, PropSize size) {
        this(material, material.createBlockData(), size);
    }

    /**
     * Creates a new PropType with specific block data.
     *
     * @param material the block material
     * @param blockData the specific block data
     * @param size the prop size category
     */
    public PropType(Material material, BlockData blockData, PropSize size) {
        this.material = material;
        this.blockData = blockData;
        this.size = size;
        this.displayName = formatMaterialName(material);
    }

    /**
     * Gets the material.
     *
     * @return the material
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Gets the block data.
     *
     * @return the block data
     */
    public BlockData getBlockData() {
        return blockData;
    }

    /**
     * Gets the size category.
     *
     * @return the prop size
     */
    public PropSize getSize() {
        return size;
    }

    /**
     * Gets the display name.
     *
     * @return formatted display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the health for this prop type.
     *
     * @return health points
     */
    public int getHealth() {
        return size.getHealth();
    }

    /**
     * Formats a material name for display.
     *
     * @param material the material
     * @return formatted name
     */
    private static String formatMaterialName(Material material) {
        String name = material.name().toLowerCase().replace('_', ' ');
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : name.toCharArray()) {
            if (c == ' ') {
                result.append(c);
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    /**
     * Determines the size category for a material based on its properties.
     *
     * @param material the material to categorize
     * @return the appropriate PropSize
     */
    public static PropSize determineSizeForMaterial(Material material) {
        String name = material.name();

        // Small props
        if (name.contains("BUTTON") || name.contains("TORCH") || name.contains("CANDLE") ||
            name.contains("FLOWER") || name.contains("SAPLING") || name.contains("LEVER") ||
            name.contains("PRESSURE_PLATE") || name.contains("TRIPWIRE") ||
            name.equals("DEAD_BUSH") || name.equals("FERN") || name.equals("GRASS") ||
            name.contains("CORAL") && !name.contains("BLOCK") || name.contains("PICKLE") ||
            name.contains("MUSHROOM") && !name.contains("BLOCK") && !name.contains("STEM")) {
            return PropSize.SMALL;
        }

        // Large props
        if (name.contains("BARREL") || name.contains("CHEST") || name.contains("FURNACE") ||
            name.contains("CRAFTING") || name.contains("CAULDRON") || name.contains("COMPOSTER") ||
            name.contains("LECTERN") || name.contains("SMOKER") || name.contains("BLAST") ||
            name.contains("ANVIL") || name.contains("BREWING") || name.contains("ENCHANTING") ||
            name.contains("HOPPER") || name.contains("DISPENSER") || name.contains("DROPPER") ||
            name.contains("OBSERVER") || name.contains("PISTON") || name.contains("BED") ||
            name.contains("SHULKER") || name.contains("BEACON") || name.contains("CONDUIT")) {
            return PropSize.LARGE;
        }

        // Default to medium
        return PropSize.MEDIUM;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropType propType = (PropType) o;
        return material == propType.material;
    }

    @Override
    public int hashCode() {
        return Objects.hash(material);
    }

    @Override
    public String toString() {
        return "PropType{" +
                "material=" + material +
                ", size=" + size +
                '}';
    }
}

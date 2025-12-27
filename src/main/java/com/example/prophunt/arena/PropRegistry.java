package com.example.prophunt.arena;

import com.example.prophunt.disguise.PropSize;
import com.example.prophunt.disguise.PropType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Registry of valid props for an arena.
 * Only blocks that exist in the arena can be used as props.
 */
public class PropRegistry {

    private final Map<Material, PropType> props;
    private final Map<PropSize, List<PropType>> propsBySize;

    public PropRegistry() {
        this.props = new LinkedHashMap<>();
        this.propsBySize = new EnumMap<>(PropSize.class);
        for (PropSize size : PropSize.values()) {
            propsBySize.put(size, new ArrayList<>());
        }
    }

    /**
     * Adds a prop to the registry.
     *
     * @param propType the prop type to add
     */
    public void addProp(PropType propType) {
        if (propType == null || propType.getMaterial() == null) return;

        props.put(propType.getMaterial(), propType);
        propsBySize.get(propType.getSize()).add(propType);
    }

    /**
     * Adds a prop by material with auto-detected size.
     *
     * @param material the material
     */
    public void addProp(Material material) {
        if (material == null || !material.isBlock()) return;

        PropSize size = PropType.determineSizeForMaterial(material);
        addProp(new PropType(material, size));
    }

    /**
     * Adds a prop by material with specified size.
     *
     * @param material the material
     * @param size the size
     */
    public void addProp(Material material, PropSize size) {
        if (material == null || !material.isBlock() || size == null) return;

        addProp(new PropType(material, size));
    }

    /**
     * Removes a prop from the registry.
     *
     * @param material the material to remove
     * @return true if removed
     */
    public boolean removeProp(Material material) {
        PropType removed = props.remove(material);
        if (removed != null) {
            propsBySize.get(removed.getSize()).remove(removed);
            return true;
        }
        return false;
    }

    /**
     * Checks if a material is a valid prop.
     *
     * @param material the material
     * @return true if valid prop
     */
    public boolean isValidProp(Material material) {
        return props.containsKey(material);
    }

    /**
     * Gets a prop type by material.
     *
     * @param material the material
     * @return the prop type, or null if not found
     */
    public PropType getProp(Material material) {
        return props.get(material);
    }

    /**
     * Gets all registered props.
     *
     * @return unmodifiable collection of props
     */
    public Collection<PropType> getProps() {
        return Collections.unmodifiableCollection(props.values());
    }

    /**
     * Gets props by size category.
     *
     * @param size the size
     * @return unmodifiable list of props with that size
     */
    public List<PropType> getPropsBySize(PropSize size) {
        return Collections.unmodifiableList(propsBySize.get(size));
    }

    /**
     * Gets the count of props in each size category.
     *
     * @return map of size to count
     */
    public Map<PropSize, Integer> getCountsBySize() {
        Map<PropSize, Integer> counts = new EnumMap<>(PropSize.class);
        for (PropSize size : PropSize.values()) {
            counts.put(size, propsBySize.get(size).size());
        }
        return counts;
    }

    /**
     * Gets a random prop.
     *
     * @return random prop, or null if empty
     */
    public PropType getRandomProp() {
        if (props.isEmpty()) return null;
        List<PropType> propList = new ArrayList<>(props.values());
        return propList.get(new Random().nextInt(propList.size()));
    }

    /**
     * Gets a random prop of a specific size.
     *
     * @param size the size
     * @return random prop of that size, or null if none
     */
    public PropType getRandomProp(PropSize size) {
        List<PropType> sizedProps = propsBySize.get(size);
        if (sizedProps.isEmpty()) return null;
        return sizedProps.get(new Random().nextInt(sizedProps.size()));
    }

    /**
     * Clears all props from the registry.
     */
    public void clear() {
        props.clear();
        for (PropSize size : PropSize.values()) {
            propsBySize.get(size).clear();
        }
    }

    /**
     * Gets the total number of props.
     *
     * @return prop count
     */
    public int size() {
        return props.size();
    }

    /**
     * Checks if the registry is empty.
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return props.isEmpty();
    }

    /**
     * Saves the registry to a configuration section.
     *
     * @param section the section to save to
     */
    public void save(ConfigurationSection section) {
        for (PropSize size : PropSize.values()) {
            List<String> materials = propsBySize.get(size).stream()
                    .map(prop -> prop.getMaterial().name())
                    .collect(Collectors.toList());
            section.set(size.name().toLowerCase(), materials);
        }
    }

    /**
     * Loads the registry from a configuration section.
     *
     * @param section the section to load from
     */
    public void load(ConfigurationSection section) {
        clear();
        if (section == null) return;

        for (PropSize size : PropSize.values()) {
            List<String> materials = section.getStringList(size.name().toLowerCase());
            for (String materialName : materials) {
                try {
                    Material material = Material.valueOf(materialName.toUpperCase());
                    addProp(material, size);
                } catch (IllegalArgumentException ignored) {
                    // Invalid material, skip
                }
            }
        }
    }

    /**
     * Gets a formatted summary of the registry.
     *
     * @return summary string
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Total: ").append(size()).append(" props\n");
        for (PropSize size : PropSize.values()) {
            int count = propsBySize.get(size).size();
            sb.append(size.getDisplayName()).append(": ").append(count).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "PropRegistry{" +
                "total=" + size() +
                ", small=" + propsBySize.get(PropSize.SMALL).size() +
                ", medium=" + propsBySize.get(PropSize.MEDIUM).size() +
                ", large=" + propsBySize.get(PropSize.LARGE).size() +
                '}';
    }
}

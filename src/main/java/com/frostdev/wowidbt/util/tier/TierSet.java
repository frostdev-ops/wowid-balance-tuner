package com.frostdev.wowidbt.util.tier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.List;

public class TierSet {
    private final String name;
    private final TierSetType type;
    private List<Item> items;
    private double multiplier;

    public TierSet(String name, TierSetType type){
        this.name = name;
        this.type = type;
    }

    public String getName(){
        return name;
    }

    public void setMultiplier(double multiplier){
        this.multiplier = multiplier;
    }

    public double getMultiplier(){
        return multiplier;
    }

    public TierSetType getType(){
        return type;
    }

    public List<Item> getItems(){
        return items;
    }

    public void addItem(String item_name){
        items.add(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(item_name.split(":")[0], item_name.split(":")[1])));
    }

    public void removeItem(String item_name){
        items.remove(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(item_name.split(":")[0], item_name.split(":")[1])));
    }

    public void clearItems(){
        items.clear();
    }

    public boolean containsItem(String item_name){
        return items.contains(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(item_name.split(":")[0], item_name.split(":")[1])));
    }

    public boolean isEmpty(){
        return items.isEmpty();
    }

    public int size(){
        return items.size();
    }

    public boolean equals(TierSet tier){
        return this.type.equals(tier.getType());
    }



}

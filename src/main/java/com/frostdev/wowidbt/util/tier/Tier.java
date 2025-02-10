package com.frostdev.wowidbt.util.tier;

import java.util.ArrayList;
import java.util.List;

public class Tier {
    private final TierType type;
    private List<TierSet> sets;
    private final String name;
    private double multiplier;
    public Tier(String name, TierType type){
        this.type = type;
        this.name = name;
    }
    public Tier getTier(){
        return this;
    }

    public TierType getType() {
        return type;
    }

    public void setMultiplier(double multiplier){
        this.multiplier = multiplier;
    }

    public double getMultiplier(){
        return multiplier;
    }

    public List<TierSet> getSets(){
        return sets;
    }

    public void addSet(TierSet set){
        sets.add(set);
    }

    public void removeSet(TierSet set){
        sets.remove(set);
    }

    public void clearSets(){
        sets.clear();
    }

    public boolean containsSet(TierSet set){
        return sets.contains(set);
    }

    public boolean isEmpty(){
        return sets.isEmpty();
    }

    public int size(){
        return sets.size();
    }

    public TierSet getSetByName(String name){
        for(TierSet set : sets){
            if(set.getName().equals(name)){
                return set;
            }
        }
        return null;
    }

    public List<TierSet> getSetsByType(TierSetType type){
        List<TierSet> setList = new ArrayList<>();
        for(TierSet set : sets){
            if(set.getType().equals(type)){
                setList.add(set);
            }
        }
        return setList;
    }

    public boolean equals(Tier tier){
        return this.type.equals(tier.getType());
    }
    public String getName(){
        return name;
    }


}

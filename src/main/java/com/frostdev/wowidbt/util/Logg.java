package com.frostdev.wowidbt.util;

public enum Logg {
    ;
    public static final String LOG_NFZ_ENTER = "Player entered a no fly zone";
    public static final String LOG_ADDED_TO_FLIGHT_LIST = "Player was flying, adding to creative flight list";
    public static final String LOG_SET_TO_NFZ = "Setting player to no fly";
    public static final String LOG_SCHEDULED_TASK_SET_TO_NFZ = "Setting player to no fly scheduled task";
    public static final String LOG_STILL_IN_NFZ = "Player still in no fly zone, ensuring flight disabled and waiting";
    public static final String LOG_LEFT_NFZ = "Player left no fly zone, re-enabling flight";
    public static final String LOG_SET_TO_FLY = "Player set to fly";
    public static final String LOG_REMOVED_FROM_NFZ_LIST = "Player removed from no fly zone list";
    public static final String LOG_REMOVED_FROM_FLIGHT_LIST = "Player removed from creative flight list";
}
package anightdazingzoroark.riftlib.ridePositionLogic;

public class DynamicRidePosUtils {
    public static boolean locatorCanBeRidePos(String locatorName) {
        return locatorName != null && locatorName.startsWith("rider_pos_");
    }

    public static boolean locatorCanBeControllerPos(String locatorName) {
        return locatorName != null && locatorName.equals("rider_pos_main");
    }

    public static int locatorRideIndex(String locatorName) {
        if (!locatorCanBeRidePos(locatorName) || locatorCanBeControllerPos(locatorName)) return -1;
        return Integer.parseInt(locatorName.substring(10));
    }
}

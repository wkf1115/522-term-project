package auto.util;

import auto.data.Invariance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class CompareInvarince {
    ConcurrentLinkedDeque<Invariance> oldInvariances;
    ConcurrentLinkedDeque<Invariance> newInvariances;

    static String greenText = "\033[32m";
    static String redText = "\033[31m";
    static String resetText = "\033[0m";

    public static boolean compare(ConcurrentLinkedDeque<Invariance> oldInvariances, ConcurrentLinkedDeque<Invariance> newInvariances){

        Map<String, Invariance> oldMap = new HashMap();
        Map<String, Invariance> newMap = new HashMap();

        for (Invariance invariance : oldInvariances){
            oldMap.put(invariance.getName(), invariance);
        }

        for (Invariance invariance : newInvariances){
            newMap.put(invariance.getName(), invariance);
        }

        return oldMap.equals(newMap);
    }


    public static String outputDifference(ConcurrentLinkedDeque<Invariance> oldInvariances, ConcurrentLinkedDeque<Invariance> newInvariances){

        Map<String, Invariance> oldMap = new HashMap();
        Map<String, Invariance> newMap = new HashMap();

        for (Invariance invariance : oldInvariances){
            oldMap.put(invariance.getName(), invariance);
        }

        for (Invariance invariance : newInvariances){
            newMap.put(invariance.getName(), invariance);
        }

        StringBuilder oldI = new StringBuilder();
        StringBuilder newI = new StringBuilder();

        if(oldInvariances.peek() == null){
            newI.append(newInvariances.peek().getOriginalType());
            return getStarLine() + "null \n" + getStarLine() + newI.toString() + "\n";
        }


        for (Map.Entry<String, Invariance> entry : newMap.entrySet()) {

//            System.out.println("newqdqdqdqdqnenwqdwqdqdb = " + newMap);
//            System.out.println("olddbiasdiasidvadgisagdia = " + oldMap);

            String key = entry.getKey();

            Invariance newInvariance = newMap.get(key);
            Invariance oldInvariance = oldMap.get(key);

            assert oldInvariance != null;

            newI.append(getStarLine());
            newI.append(key + "\n");

            oldI.append(getStarLine());
            oldI.append(key + "\n");

            if (!newInvariance.equals(oldInvariance)){
                if (!oldInvariance.getEuqalMap().equals(newInvariance.getEuqalMap())){
                    MapDifference<String, String> difference = compareMaps(oldInvariance.getEuqalMap(), newInvariance.getEuqalMap());
                    List<String> oldDiff = convertMapToStringList(difference.mapA);
                    List<String> newDiff = convertMapToStringList(difference.mapB);
                    for(String s : oldDiff){
                        oldI.append(greenText + " --- " + s + resetText + "\n");
                    }

                    for(String s : newDiff){
                        newI.append(redText + " +++ " + s + resetText + "\n");
                    }
                }
                if(!oldInvariance.getOneOfMap().equals(newInvariance.getOneOfMap())){
                    MapDifference<String, String> difference = compareMaps(oldInvariance.getOneOfMap(), newInvariance.getOneOfMap());
                    List<String> oldDiff = convertMapToStringList(difference.mapA);
                    List<String> newDiff = convertMapToStringList(difference.mapB);
                    for(String s : oldDiff){
                        oldI.append(greenText + " --- " + s + resetText + "\n");
                    }

                    for(String s : newDiff){
                        newI.append(redText + " +++ " + s + resetText + "\n");
                    }
                }
                if (!oldInvariance.getHasOnlyOneMap().equals(newInvariance.getHasOnlyOneMap())){
                    MapDifference<String, String> difference = compareMaps(oldInvariance.getHasOnlyOneMap(), newInvariance.getHasOnlyOneMap());
                    List<String> oldDiff = convertMapToStringList(difference.mapA);
                    List<String> newDiff = convertMapToStringList(difference.mapB);
                    for(String s : oldDiff){
                        oldI.append(greenText + " --- " + s + resetText + "\n");
                    }

                    for(String s : newDiff){
                        newI.append(redText + " +++ " + s + resetText + "\n");
                    }
                }
                oldI.append(getStarLine());
                newI.append(getStarLine());
            }
        }

        return "****************************\n change of old invariants : \n" + oldI.append("\n")
                .append("****************************\n change of new invariants : \n").append(newI).toString() + "\n";

    }

    private static List<String> convertMapToStringList(Map<?, ?> map) {
        List<String> keyValueStrings = new ArrayList<>();

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();
            keyValueStrings.add(key + "==" + value);
        }

        return keyValueStrings;
    }

    private static class MapDifference<K, V> {
        public Map<K, V> mapA;
        public Map<K, V> mapB;

        public MapDifference(Map<K, V> mapA, Map<K, V> mapB) {
            this.mapA = mapA;
            this.mapB = mapB;
        }
    }

    private static <K, V> MapDifference<K, V> compareMaps(Map<K, V> mapA, Map<K, V> mapB) {
        Map<K, V> diffA = new HashMap<>(mapA);
        Map<K, V> diffB = new HashMap<>(mapB);


        for (K key : mapA.keySet()) {
            if (mapB.containsKey(key) && mapB.get(key).equals(mapA.get(key))) {
                diffA.remove(key);
                diffB.remove(key);
            }
        }

        return new MapDifference<>(diffA, diffB);
    }

    private static String getStarLine(){
        return "**********************************************\n";
    }

}

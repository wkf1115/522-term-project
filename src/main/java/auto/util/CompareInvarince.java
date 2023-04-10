package auto.util;

import auto.data.Invariance;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class CompareInvarince {
    ConcurrentLinkedDeque<Invariance> oldInvariances;
    ConcurrentLinkedDeque<Invariance> newInvariances;

    public static boolean compare(ConcurrentLinkedDeque<Invariance> oldInvariances, ConcurrentLinkedDeque<Invariance> newInvariances){

        ConcurrentHashMap<String, Invariance> oldMap = new ConcurrentHashMap();
        ConcurrentHashMap<String, Invariance> newMap = new ConcurrentHashMap();

        for (Invariance invariance : oldInvariances){
            oldMap.put(invariance.getName(), invariance);
        }

        for (Invariance invariance : newInvariances){
            newMap.put(invariance.getName(), invariance);
        }

        return oldMap.equals(newMap);
    }

}

package auto.util;

import auto.data.Invariance;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenerateInvarince {
    // now it can only support length 2147483645
    public static ConcurrentLinkedDeque<Invariance> generate(String invarinceValue){

        String[] sections = invarinceValue.split("={75,}\n");
        ConcurrentLinkedDeque<Invariance> res = new ConcurrentLinkedDeque<>();

        for (int j = 1; j < sections.length; j++) {
            Invariance invariance = new Invariance();
            invariance.setOriginalType(invarinceValue);
            Map<String, String> euqalMap = new HashMap<>();
            Map<String, String> oneOfMap = new HashMap<>();
            Map<String, String> hasOnlyOneMap = new HashMap<>();
            String[] lines = sections[j].split("\\r?\\n"); //split by line
            String firstLine = lines[0];
            invariance.setName(firstLine);

            for(int i = 1; i < lines.length; i++){
                if(lines[i].contains(" == ")){
                    String[] temp = lines[i].split(" == ");
                    euqalMap.put(temp[0], temp[1]);
                }else if(lines[i].contains(" has only one value")){
                    String[] temp = lines[i].split(" ");
                    hasOnlyOneMap.put(temp[0], "1");
                }else if(lines[i].contains(" one of ")){
                    String[] temp = lines[i].split(" ");
                    Pattern pattern = Pattern.compile("\\{(.+?)\\}");
                    Matcher matcher = pattern.matcher(lines[i]);
                    String content = " ";
                    if (matcher.find()) {
                        content = matcher.group(1);
                    }
                    oneOfMap.put(temp[0], content);
                }
            }
            invariance.setEuqalMap(euqalMap);
            invariance.setOneOfMap(oneOfMap);
            invariance.setHasOnlyOneMap(hasOnlyOneMap);
            res.add(invariance);
        }
        return res;
    }
}

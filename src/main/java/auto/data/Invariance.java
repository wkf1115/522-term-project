package auto.data;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Invariance {
    private String name;
    private Map<String, String> euqalMap;
    private Map<String, String> oneOfMap;
    private Map<String, String> hasOnlyOneMap;

    private String originalType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getEuqalMap() {
        return euqalMap;
    }

    public void setEuqalMap(Map<String, String> euqalMap) {
        this.euqalMap = euqalMap;
    }

    public Map<String, String> getOneOfMap() {
        return oneOfMap;
    }

    public void setOneOfMap(Map<String, String> oneOfMap) {
        this.oneOfMap = oneOfMap;
    }

    public Map<String, String> getHasOnlyOneMap() {
        return hasOnlyOneMap;
    }

    public String getOriginalType() {
        return originalType;
    }

    public void setOriginalType(String originalType) {
        this.originalType = originalType;
    }

    public void setHasOnlyOneMap(Map<String, String> hasOnlyOneMap) {
        this.hasOnlyOneMap = hasOnlyOneMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invariance that = (Invariance) o;
        return name.equals(that.name) && Objects.equals(euqalMap, that.euqalMap) && Objects.equals(oneOfMap, that.oneOfMap) && Objects.equals(hasOnlyOneMap, that.hasOnlyOneMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, euqalMap, oneOfMap, hasOnlyOneMap);
    }

    @Override
    public String toString() {
        return "Invariance{" +
                "name='" + name + '\'' +
                ", euqalMap=" + euqalMap +
                ", oneOfMap=" + oneOfMap +
                ", hasOnlyOneMap=" + hasOnlyOneMap +
                ", originalType='" + originalType + '\'' +
                '}';
    }
}

package inc.huduk.tldr_inator.repository;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryContentHolder {

    Map<String, String> map = new ConcurrentHashMap<>();

    public boolean contains(String key) {
        return map.containsKey(key);
    }

    public void add(String key, String content) {
        map.put(key, content);
    }

    public String get(String key) {
        return map.get(key);
    }

    public void clear(String key) {
        map.remove(key);
    }
}

package inc.huduk.tldr_inator.repository;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class ChatHistory {

    @Value("${chat.history.size}")
    private int historySize;

    Map<String, Queue<String>> history = new HashMap<>();

    public void add(String id, String response) {
        var q = history.computeIfAbsent(id, x -> new LinkedList<>());
        if (q.size() == historySize) q.poll();
        q.add(response);
    }

    public String history(String id) {
        return history.getOrDefault(id, new LinkedList<>()).stream().reduce("", (acc, item)-> acc + '\n' +  item);
    }

    public void clear(String id) {
        history.remove(id);
    }
}
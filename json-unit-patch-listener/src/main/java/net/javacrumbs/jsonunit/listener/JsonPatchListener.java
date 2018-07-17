package net.javacrumbs.jsonunit.listener;

import com.google.gson.GsonBuilder;
import net.javacrumbs.jsonunit.core.listener.Difference;
import net.javacrumbs.jsonunit.core.listener.DifferenceContext;
import net.javacrumbs.jsonunit.core.listener.DifferenceListener;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonPatchListener implements DifferenceListener {

    private final List<Difference> differences = new ArrayList<>();

    private DifferenceContext context;

    @Override
    public void diff(Difference difference, DifferenceContext differenceContext) {
        this.context = differenceContext;
        differences.add(difference);
    }

    public List<Difference> getDifferences() {
        return differences;
    }

    public DifferenceContext getContext() {
        return context;
    }

    private String getPath(Difference difference) {
        switch (difference.getType()) {
            case DIFFERENT:
                return difference.getActualPath();

            case MISSING:
                return difference.getExpectedPath();

            case EXTRA:
                return difference.getActualPath();
        }
        throw new IllegalArgumentException("Difference has unknown type");
    }

    private List<Object> getPatch(Difference difference) {
        List<Object> result = new ArrayList<>();

        switch (difference.getType()) {
            case DIFFERENT:
                result.add(difference.getExpected());
                result.add(difference.getActual());
                return result;

            case MISSING:
                result.add(difference.getExpected());
                result.add(0);
                result.add(0);
                return result;

            case EXTRA:
                result.add(difference.getActual());
                return result;
        }
        return result;
    }

    public String getJsonPatch() {
        Map<String, Object> jsonDiffPatch = new HashMap<>();
        getDifferences().forEach(difference -> {

            String field = getPath(difference);
            Map<String, Object> currentMap = jsonDiffPatch;

            String fieldWithDots = StringUtils.replace(field, "[", ".");
            int len = fieldWithDots.length();
            int left = 0;
            int right = 0;
            while (left < len) {
                right = fieldWithDots.indexOf('.', left);
                if (right == -1) {
                    right = len;
                }
                String fieldName = fieldWithDots.substring(left, right);
                fieldName = StringUtils.remove(fieldName, "]");

                if (right != len) {
                    if (!currentMap.containsKey(fieldName)) {
                        currentMap.put(fieldName, new HashMap<>());
                    }

                    currentMap = (Map<String, Object>) currentMap.get(fieldName);

                    if (field.charAt(right) == '[') {
                        if (!currentMap.containsKey(fieldName)) {
                            currentMap.put("_t", "a");
                        }
                    }
                } else {
                    List<?> actualExpectedValue = getPatch(difference);
                    currentMap.put(fieldName, actualExpectedValue);
                }
                left = right + 1;
            }
        });

        return new GsonBuilder().create().toJson(jsonDiffPatch);
    }
}

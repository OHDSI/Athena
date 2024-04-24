package com.odysseusinc.athena.glue;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import lombok.SneakyThrows;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.opentest4j.AssertionFailedError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GenericSteps {
    @Autowired
    private World world;


    @When("^(?:which|it) is a list containing:$")
    public void assertListContains(DataTable dt) {
        assertList(dt.asMaps(), toList(world.getCursor()));
    }

    public static List<?> toList(Object cursor) {
        if (cursor instanceof Page) {
            return ((Page<?>) cursor).getContent();
        } else if (cursor instanceof List) {
            return (List<?>) cursor;
        } else if (cursor.getClass().isArray()) {
            return Arrays.asList((Object[]) cursor);
        } else if (cursor instanceof Throwable) {
            throw new AssertionFailedError("Found a previous error:", (Throwable) cursor);
        } else {
            throw new AssertionFailedError("Not a list: " + cursor.getClass());
        }
    }


    private <T> List<T> assertList(List<Map<String, String>> expectations, List<T> cursor) {

        List<T> values = new ArrayList<>(cursor);
        for (Map<String, String> expectation : expectations) {
            T found = expectation.entrySet().stream().reduce(
                    values.stream(), (stream, entry) -> stream.filter(matchesEntry(entry)), this::throwingCombiner
            ).findFirst().orElseThrow(() -> {
                String actual = dumpProperties(cursor, expectation.keySet());
                return new AssertionFailedError(
                        MessageFormat.format("For expectation [{0}], matching actual item not found. Actual items:{1}", expectation.toString(), actual)
                );
            });
            values.remove(found);
        }
        return values;
    }

    private <T> Predicate<T> matchesEntry(Entry<String, String> entry) {
        return item -> {
            String actual = Optional.ofNullable(getProperty(item, entry.getKey())).map(Object::toString).orElse(null);
            String value = entry.getValue();
            return matchWithCaptureOrAlias(value, actual);
        };
    }
    @SneakyThrows
    public static <T> Object getProperty(T item, String key) {
        String[] split = key.split("->", 2);
        try {
            Object resolvedValue = PropertyUtils.getProperty(item, split[0].trim());
            if (split.length == 1) {
                return resolvedValue;
            } else {
                String nextKey = split[1].trim();
                if (resolvedValue instanceof List) {
                    List<Object> list = (List<Object>) resolvedValue;
                    List<Object> result = new ArrayList<>();
                    for (Object value : list) {
                        Object nestedValue = getProperty(value, nextKey);
                        result.add(nestedValue);
                    }
                    return result;
                } else {
                    return getProperty(resolvedValue, nextKey);
                }
            }
        } catch (NoSuchMethodException | NestedNullException e) {
            return null;
        }
    }


    private void assertWithCapture(String expected, String actual) {
        Assertions.assertTrue(matchWithCaptureOrAlias(expected, actual), () ->
                MessageFormat.format("Expected [{0}], Actual [{1}]", expected, actual)
        );
    }

    private boolean matchWithCaptureOrAlias(String expected, String actual) {
        if (expected == null && actual == null) {
            return true;
        } else if (StringUtils.startsWith(expected, "~")) {
            String regex = StringUtils.substringAfter(expected, "~");
            return matchWithCapture(regex, actual);
        } else {
            return Objects.equals(world.resolveAlias(expected), actual);
        }
    }

    private boolean matchWithCapture(String regex, String actual) {
        Matcher matcher = Pattern.compile(regex, Pattern.DOTALL).matcher(actual);
        if (matcher.find()) {
            List<String> groups = findNamedGroups(regex);
            groups.forEach(name ->
                    world.capture(name, matcher.group(name))
            );
            return true;
        } else {
            return false;
        }
    }

    private List<String> findNamedGroups(String pattern) {
        Matcher matcher = Pattern.compile("\\(\\?<(?<name>.+?)>.+\\)").matcher(pattern);
        List<String> groups = new LinkedList<>();
        while (matcher.find()) {
            // Using regex named groups to find if pattern contains named groups
            groups.add(matcher.group("name"));
        }
        return groups;
    }

    private static <T> String dumpProperties(List<T> cursor, Set<String> keys) {
        String actual = cursor.stream().map(item ->
                keys.stream().map(key -> key + ": " + getProperty(item, key)).collect(Collectors.joining(", "))
        ).collect(Collectors.joining("\n - ", "\n - ", "\n==="));
        return actual;
    }

    private <T> T throwingCombiner(T a, T b) {
        throw new AssertionFailedError();
    }


}
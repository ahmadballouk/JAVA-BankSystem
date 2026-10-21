package menu;

import java.util.List;
import java.util.function.Function;

public final class IdBinarySearch {

    private IdBinarySearch() {
    }

    public static <T> T find(List<T> sortedItems, String id, Function<T, String> idGetter) {
        if (sortedItems == null || sortedItems.isEmpty()
                || id == null || id.isBlank() || idGetter == null) {
            return null;
        }

        int low = 0;
        int high = sortedItems.size() - 1;
        String target = id.trim();

        while (low <= high) {
            int middle = low + (high - low) / 2;
            String currentId = idGetter.apply(sortedItems.get(middle));
            int comparison = String.CASE_INSENSITIVE_ORDER.compare(currentId, target);

            if (comparison == 0) {
                return sortedItems.get(middle);
            }
            if (comparison < 0) {
                low = middle + 1;
            } else {
                high = middle - 1;
            }
        }

        return null;
    }
}

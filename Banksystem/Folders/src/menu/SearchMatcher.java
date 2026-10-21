package menu;

public final class SearchMatcher {

    private SearchMatcher() {
    }

    public static boolean matches(String value, String query) {
        if (value == null || query == null || query.isBlank()) return false;

        String text = normalize(value);
        String search = normalize(query);
        if (text.isEmpty() || search.isEmpty()) return false;
        if (text.contains(search)) return true;

        int distance = distance(text, search);
        int limit = search.length() <= 4 ? 1 : search.length() <= 8 ? 2 : 3;
        return distance <= limit || distance <= Math.max(text.length(), search.length()) / 4;
    }

    private static String normalize(String value) {
        return value.toLowerCase(java.util.Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]", "");
    }

    private static int distance(String first, String second) {
        int[] previous = new int[second.length() + 1];
        int[] current = new int[second.length() + 1];

        for (int j = 0; j <= second.length(); j++) previous[j] = j;
        for (int i = 1; i <= first.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= second.length(); j++) {
                current[j] = Math.min(
                        Math.min(current[j - 1] + 1, previous[j] + 1),
                        previous[j - 1] + (first.charAt(i - 1) == second.charAt(j - 1) ? 0 : 1));
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }
        return previous[second.length()];
    }
}

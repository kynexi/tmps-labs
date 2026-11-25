package categorization;

import java.util.List;

public class KeywordCategoryHandler extends CategoryHandler {
    private final String category;
    private final List<String> keywords;

    public KeywordCategoryHandler(String category, String... keywords) {
        this.category = category;
        this.keywords = List.of(keywords);
    }

    @Override
    public String handle(CategoryRequest request) {
        String text = request.description().toLowerCase();
        for (String k : keywords) {
            if (text.contains(k.toLowerCase())) return category;
        }
        return passToNext(request);
    }
}
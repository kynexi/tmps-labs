package categorization;

// HANDLER: each one either decides the category or passes the request on
public abstract class CategoryHandler {
    private CategoryHandler next;

    public CategoryHandler linkWith(CategoryHandler next) {
        this.next = next;
        return next;            // lets you write a.linkWith(b).linkWith(c)
    }

    public abstract String handle(CategoryRequest request);

    protected String passToNext(CategoryRequest request) {
        return next == null ? null : next.handle(request);   // null = "nobody knew"
    }
}
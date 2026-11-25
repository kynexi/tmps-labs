package categorization;

import config.AppSettings;

// the client only talks to this; the request never "falls off" the end
public class CategoryChain {
    private CategoryHandler head;

    public CategoryChain(CategoryHandler head) { this.head = head; }

    // runtime change: a rule added by the user is checked first
    public void addFirst(CategoryHandler handler) {
        handler.linkWith(head);
        head = handler;
    }

    public String resolve(CategoryRequest request) {
        String category = head == null ? null : head.handle(request);
        return category != null ? category : AppSettings.getInstance().getDefaultCategory();
    }
}
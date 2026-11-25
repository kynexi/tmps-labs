package categorization;

import model.TransactionType;

public class IncomeCategoryHandler extends CategoryHandler {
    @Override
    public String handle(CategoryRequest request) {
        return request.type() == TransactionType.INCOME ? "income" : passToNext(request);
    }
}
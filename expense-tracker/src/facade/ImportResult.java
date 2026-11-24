package facade;

import java.util.List;

public record ImportResult(int imported, List<String> rejected) {
}
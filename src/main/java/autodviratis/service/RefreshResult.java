package autodviratis.service;

import java.util.List;

public record RefreshResult(List<SourceStatus> sourceStatuses, int listingCount) {

    public RefreshResult {
        sourceStatuses = List.copyOf(sourceStatuses);
    }
}

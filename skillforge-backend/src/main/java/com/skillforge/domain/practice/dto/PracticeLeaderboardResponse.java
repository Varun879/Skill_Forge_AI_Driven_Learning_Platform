package com.skillforge.domain.practice.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PracticeLeaderboardResponse {

    private final LocalDateTime generatedAt;
    private final int totalParticipants;
    private final List<PracticeLeaderboardEntryResponse> entries;

    public PracticeLeaderboardResponse(
            LocalDateTime generatedAt,
            int totalParticipants,
            List<PracticeLeaderboardEntryResponse> entries) {
        this.generatedAt = generatedAt;
        this.totalParticipants = totalParticipants;
        this.entries = entries;
    }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public int getTotalParticipants() { return totalParticipants; }
    public List<PracticeLeaderboardEntryResponse> getEntries() { return entries; }
}

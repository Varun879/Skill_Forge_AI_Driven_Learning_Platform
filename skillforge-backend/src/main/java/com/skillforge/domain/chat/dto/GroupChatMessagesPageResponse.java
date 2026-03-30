package com.skillforge.domain.chat.dto;

import java.util.List;

public class GroupChatMessagesPageResponse {

    private Long courseId;
    private List<GroupChatMessageResponse> messages;
    private int page;
    private int size;
    private long totalElements;
    private boolean hasNext;

    public GroupChatMessagesPageResponse(Long courseId,
                                         List<GroupChatMessageResponse> messages,
                                         int page,
                                         int size,
                                         long totalElements,
                                         boolean hasNext) {
        this.courseId = courseId;
        this.messages = messages;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.hasNext = hasNext;
    }

    public Long getCourseId() { return courseId; }
    public List<GroupChatMessageResponse> getMessages() { return messages; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public boolean isHasNext() { return hasNext; }
}

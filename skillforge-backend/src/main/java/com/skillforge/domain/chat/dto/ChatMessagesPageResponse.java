package com.skillforge.domain.chat.dto;

import java.util.List;

public class ChatMessagesPageResponse {

    private ChatRoomResponse room;
    private List<ChatMessageResponse> messages;
    private int page;
    private int size;
    private long totalElements;
    private boolean hasNext;

    public ChatMessagesPageResponse(ChatRoomResponse room,
                                    List<ChatMessageResponse> messages,
                                    int page,
                                    int size,
                                    long totalElements,
                                    boolean hasNext) {
        this.room = room;
        this.messages = messages;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.hasNext = hasNext;
    }

    public ChatRoomResponse getRoom() {
        return room;
    }

    public List<ChatMessageResponse> getMessages() {
        return messages;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public boolean isHasNext() {
        return hasNext;
    }
}

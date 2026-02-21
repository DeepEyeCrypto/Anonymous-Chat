package com.phantomnet.core.database.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001e\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\nJ\u001a\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\f2\u0006\u0010\u000f\u001a\u00020\bJ\u0012\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00110\r0\fJ\u001e\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u000f\u001a\u00020\b2\u0006\u0010\u0014\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\nJ\u001e\u0010\u0015\u001a\u00020\u00132\u0006\u0010\u000f\u001a\u00020\b2\u0006\u0010\u0014\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\nJ\f\u0010\u0016\u001a\u00020\u000e*\u00020\u0017H\u0002J\f\u0010\u0016\u001a\u00020\u0011*\u00020\u0006H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0018"}, d2 = {"Lcom/phantomnet/core/database/repository/RoomRepository;", "", "db", "Lcom/phantomnet/core/database/PhantomDatabase;", "(Lcom/phantomnet/core/database/PhantomDatabase;)V", "createRoom", "Lcom/phantomnet/core/database/entity/RoomEntity;", "name", "", "type", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getMessages", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/phantomnet/core/database/model/Message;", "roomId", "getRooms", "Lcom/phantomnet/core/database/model/Room;", "insertRevealedMessage", "", "text", "insertSentMessage", "toModel", "Lcom/phantomnet/core/database/entity/MessageEntity;", "database_debug"})
public final class RoomRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.phantomnet.core.database.PhantomDatabase db = null;
    
    public RoomRepository(@org.jetbrains.annotations.NotNull()
    com.phantomnet.core.database.PhantomDatabase db) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.phantomnet.core.database.model.Room>> getRooms() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.phantomnet.core.database.model.Message>> getMessages(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object createRoom(@org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String type, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.phantomnet.core.database.entity.RoomEntity> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object insertSentMessage(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, @org.jetbrains.annotations.NotNull()
    java.lang.String text, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object insertRevealedMessage(@org.jetbrains.annotations.NotNull()
    java.lang.String roomId, @org.jetbrains.annotations.NotNull()
    java.lang.String text, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final com.phantomnet.core.database.model.Room toModel(com.phantomnet.core.database.entity.RoomEntity $this$toModel) {
        return null;
    }
    
    private final com.phantomnet.core.database.model.Message toModel(com.phantomnet.core.database.entity.MessageEntity $this$toModel) {
        return null;
    }
}
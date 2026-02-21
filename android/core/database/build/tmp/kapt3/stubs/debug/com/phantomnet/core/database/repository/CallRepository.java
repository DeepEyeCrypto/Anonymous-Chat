package com.phantomnet.core.database.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0005\u001a\u00020\u0006H\u0086@\u00a2\u0006\u0002\u0010\u0007J\u001a\u0010\b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\t2\u0006\u0010\f\u001a\u00020\rJ&\u0010\u000e\u001a\u00020\u00062\u0006\u0010\u000f\u001a\u00020\r2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\rH\u0086@\u00a2\u0006\u0002\u0010\u0013J.\u0010\u0014\u001a\u00020\u00062\u0006\u0010\u000f\u001a\u00020\r2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u0015\u001a\u00020\r2\u0006\u0010\u0016\u001a\u00020\rH\u0086@\u00a2\u0006\u0002\u0010\u0017R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0018"}, d2 = {"Lcom/phantomnet/core/database/repository/CallRepository;", "", "callLogDao", "Lcom/phantomnet/core/database/dao/CallLogDao;", "(Lcom/phantomnet/core/database/dao/CallLogDao;)V", "clearAllCallLogs", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getLogsForConversation", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/phantomnet/core/database/entity/CallLogEntity;", "conversationId", "", "logCallEnd", "sessionId", "durationSec", "", "outcome", "(Ljava/lang/String;ILjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "logCallStart", "direction", "privacyMode", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "database_debug"})
public final class CallRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.phantomnet.core.database.dao.CallLogDao callLogDao = null;
    
    public CallRepository(@org.jetbrains.annotations.NotNull()
    com.phantomnet.core.database.dao.CallLogDao callLogDao) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.phantomnet.core.database.entity.CallLogEntity>> getLogsForConversation(@org.jetbrains.annotations.NotNull()
    java.lang.String conversationId) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object logCallStart(@org.jetbrains.annotations.NotNull()
    java.lang.String sessionId, @org.jetbrains.annotations.NotNull()
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull()
    java.lang.String direction, @org.jetbrains.annotations.NotNull()
    java.lang.String privacyMode, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object logCallEnd(@org.jetbrains.annotations.NotNull()
    java.lang.String sessionId, int durationSec, @org.jetbrains.annotations.NotNull()
    java.lang.String outcome, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object clearAllCallLogs(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}
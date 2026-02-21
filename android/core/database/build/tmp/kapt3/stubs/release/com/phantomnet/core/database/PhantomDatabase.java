package com.phantomnet.core.database;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&J\b\u0010\u0005\u001a\u00020\u0006H&J\b\u0010\u0007\u001a\u00020\bH&J\b\u0010\t\u001a\u00020\nH&J\b\u0010\u000b\u001a\u00020\fH&\u00a8\u0006\r"}, d2 = {"Lcom/phantomnet/core/database/PhantomDatabase;", "Landroidx/room/RoomDatabase;", "()V", "callLogDao", "Lcom/phantomnet/core/database/dao/CallLogDao;", "conversationDao", "Lcom/phantomnet/core/database/dao/ConversationDao;", "messageDao", "Lcom/phantomnet/core/database/dao/MessageDao;", "personaDao", "Lcom/phantomnet/core/database/dao/PersonaDao;", "roomDao", "Lcom/phantomnet/core/database/dao/RoomDao;", "database_release"})
@androidx.room.Database(entities = {com.phantomnet.core.database.entity.PersonaEntity.class, com.phantomnet.core.database.entity.ConversationEntity.class, com.phantomnet.core.database.entity.MessageEntity.class, com.phantomnet.core.database.entity.RoomEntity.class, com.phantomnet.core.database.entity.CallLogEntity.class}, version = 1, exportSchema = false)
public abstract class PhantomDatabase extends androidx.room.RoomDatabase {
    
    public PhantomDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.phantomnet.core.database.dao.PersonaDao personaDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.phantomnet.core.database.dao.ConversationDao conversationDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.phantomnet.core.database.dao.MessageDao messageDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.phantomnet.core.database.dao.RoomDao roomDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.phantomnet.core.database.dao.CallLogDao callLogDao();
}
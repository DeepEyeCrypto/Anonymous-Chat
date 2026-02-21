package com.phantomnet.core.database;

/**
 * Factory to create the encrypted Room database.
 *
 * The passphrase should be derived from the persona root key stored
 * in EncryptedSharedPreferences. This keeps the DB unreadable without
 * the Android Keystore master key.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010%\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0012\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J \u0010\u0007\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u0005H\u0002J\u000e\u0010\r\u001a\u00020\u000e2\u0006\u0010\b\u001a\u00020\tJ \u0010\u000f\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\u0010\u001a\u00020\u0005R\u001a\u0010\u0003\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\u00060\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0011"}, d2 = {"Lcom/phantomnet/core/database/PhantomDatabaseFactory;", "", "()V", "instances", "", "", "Lcom/phantomnet/core/database/PhantomDatabase;", "buildDatabase", "context", "Landroid/content/Context;", "passphrase", "", "dbName", "destroy", "", "getInstance", "nameSuffix", "database_release"})
public final class PhantomDatabaseFactory {
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Map<java.lang.String, com.phantomnet.core.database.PhantomDatabase> instances = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.phantomnet.core.database.PhantomDatabaseFactory INSTANCE = null;
    
    private PhantomDatabaseFactory() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.phantomnet.core.database.PhantomDatabase getInstance(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    byte[] passphrase, @org.jetbrains.annotations.NotNull()
    java.lang.String nameSuffix) {
        return null;
    }
    
    private final com.phantomnet.core.database.PhantomDatabase buildDatabase(android.content.Context context, byte[] passphrase, java.lang.String dbName) {
        return null;
    }
    
    public final void destroy(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
}
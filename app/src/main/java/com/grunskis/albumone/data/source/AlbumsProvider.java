package com.grunskis.albumone.data.source;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.grunskis.albumone.data.source.local.AlbumOneDbHelper;
import com.grunskis.albumone.data.source.local.AlbumOnePersistenceContract;

public class AlbumsProvider extends ContentProvider {
    private static final int ALBUMS = 100;
    private static final int ALBUM = 101;
    private static final int PHOTOS = 200;
    private static final int PHOTO = 201;
    private static final int DOWNLOADS = 300;
    private static final int DOWNLOAD = 301;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private AlbumOneDbHelper mDbHelper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = AlbumOnePersistenceContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, AlbumOnePersistenceContract.AlbumEntry.TABLE_NAME, ALBUMS);
        matcher.addURI(authority, AlbumOnePersistenceContract.AlbumEntry.TABLE_NAME + "/*",
                ALBUM);

        matcher.addURI(authority, AlbumOnePersistenceContract.PhotoEntry.TABLE_NAME, PHOTOS);
        matcher.addURI(authority, AlbumOnePersistenceContract.PhotoEntry.TABLE_NAME + "/*",
                PHOTO);

        matcher.addURI(authority, AlbumOnePersistenceContract.DownloadEntry.TABLE_NAME, DOWNLOADS);
        matcher.addURI(authority, AlbumOnePersistenceContract.DownloadEntry.TABLE_NAME + "/*",
                DOWNLOAD);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new AlbumOneDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case ALBUMS:
                retCursor = mDbHelper.getReadableDatabase().query(
                        AlbumOnePersistenceContract.AlbumEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case ALBUM:
                String[] where = {uri.getLastPathSegment()};
                retCursor = mDbHelper.getReadableDatabase().query(
                        AlbumOnePersistenceContract.AlbumEntry.TABLE_NAME,
                        projection,
                        AlbumOnePersistenceContract.AlbumEntry._ID + " = ?",
                        where,
                        null,
                        null,
                        sortOrder
                );
                break;

            case PHOTOS:
                retCursor = mDbHelper.getReadableDatabase().query(
                        AlbumOnePersistenceContract.PhotoEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case PHOTO:
                retCursor = mDbHelper.getReadableDatabase().query(
                        AlbumOnePersistenceContract.PhotoEntry.TABLE_NAME,
                        projection,
                        AlbumOnePersistenceContract.PhotoEntry._ID + " = ?",
                        new String[]{uri.getLastPathSegment()},
                        null,
                        null,
                        sortOrder
                );
                break;

            case DOWNLOAD:
            case DOWNLOADS:
                retCursor = mDbHelper.getReadableDatabase().query(
                        AlbumOnePersistenceContract.DownloadEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context context = getContext();
        if (context != null) {
            retCursor.setNotificationUri(context.getContentResolver(), uri);
        }
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ALBUMS:
                return AlbumOnePersistenceContract.CONTENT_ALBUMS_TYPE;
            case ALBUM:
                return AlbumOnePersistenceContract.CONTENT_ALBUM_TYPE;
            case PHOTOS:
                return AlbumOnePersistenceContract.CONTENT_PHOTOS_TYPE;
            case DOWNLOADS:
                return AlbumOnePersistenceContract.CONTENT_DOWNLOADS_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        long _id;
        Uri returnUri;

        switch (match) {
            case ALBUMS:
                _id = db.insert(AlbumOnePersistenceContract.AlbumEntry.TABLE_NAME, null,
                        contentValues);
                if (_id > 0) {
                    returnUri = AlbumOnePersistenceContract.AlbumEntry.buildUriWith(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            case PHOTOS:
                _id = db.insert(AlbumOnePersistenceContract.PhotoEntry.TABLE_NAME, null,
                        contentValues);
                if (_id > 0) {
                    returnUri = AlbumOnePersistenceContract.PhotoEntry.buildUriWith(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            case DOWNLOADS:
                _id = db.insert(AlbumOnePersistenceContract.DownloadEntry.TABLE_NAME, null,
                        contentValues);
                if (_id > 0) {
                    returnUri = AlbumOnePersistenceContract.DownloadEntry.buildUriWith(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context context = getContext();
        if (context != null) {
            context.getContentResolver().notifyChange(uri, null);
        }
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        String tableName;

        switch (match) {
            case ALBUMS:
                tableName = AlbumOnePersistenceContract.AlbumEntry.TABLE_NAME;
                break;

            case PHOTOS:
                tableName = AlbumOnePersistenceContract.PhotoEntry.TABLE_NAME;
                break;

            case PHOTO:
                tableName = AlbumOnePersistenceContract.PhotoEntry.TABLE_NAME;
                selection = AlbumOnePersistenceContract.PhotoEntry._ID + " = ?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        int rowsDeleted = db.delete(tableName, selection, selectionArgs);
        Context context = getContext();
        if (context != null && (selection == null || rowsDeleted != 0)) {
            context.getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case ALBUM:
                rowsUpdated = db.update(AlbumOnePersistenceContract.AlbumEntry.TABLE_NAME, values,
                        selection, selectionArgs);
                break;

            case DOWNLOAD:
                rowsUpdated = db.update(AlbumOnePersistenceContract.DownloadEntry.TABLE_NAME, values,
                        selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context context = getContext();
        if (context != null && rowsUpdated != 0) {
            context.getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}

package pl.tomaszmartin.stuff.data;

import android.app.SearchManager;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by tomaszmartin on 14.06.2015.
 * This class is a contract for database and provider.
 *
 */

public class NotesContract {

    public static final String CONTENT_AUTHORITY = "pl.tomaszmartin.stuff";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_NOTES = "notes";
    public static final String PATH_SINGLE_NOTE = "note";
    public static final String PATH_QUERY = "query";
    public static final String PATH_SUGGESTION = SearchManager.SUGGEST_URI_PATH_QUERY;

    public static final class NoteEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_NOTES).build();
        public static final Uri CONTENT_ITEM_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_NOTES).appendPath(PATH_SINGLE_NOTE).build();
        public static final Uri CONTENT_QUERY_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_NOTES).appendPath(PATH_QUERY).build();
        public static final String CONTENT_DIR_TYPE = "vnd.android.cursor.dir/" +
                CONTENT_AUTHORITY + PATH_NOTES;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" +
                CONTENT_AUTHORITY + PATH_NOTES;

        public static final String TABLE_NAME = "notes";

        // Define the names of the columns
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_FILE_NAME = "file_name";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_DATE_CREATED = "created";
        public static final String COLUMN_DATE_LAST_MODIFIED = "last_modifed";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_IMAGE_URI = "image_uri";

        public static final String[] NOTE_COLUMNS = {
                COLUMN_ID,
                COLUMN_TITLE,
                COLUMN_DESCRIPTION,
                COLUMN_FILE_NAME,
                COLUMN_TYPE,
                COLUMN_DATE_CREATED,
                COLUMN_DATE_LAST_MODIFIED,
                COLUMN_CATEGORY,
                COLUMN_IMAGE_URI
        };

        public static Uri buildNoteUri(long id) {
            return ContentUris.withAppendedId(CONTENT_ITEM_URI, id);
        }

        public static Uri buildAllNotesUri() {
            return CONTENT_URI;
        }

        public static Uri buildQueryUri(String query) {
            return Uri.withAppendedPath(CONTENT_QUERY_URI, query);
        }

    }
}
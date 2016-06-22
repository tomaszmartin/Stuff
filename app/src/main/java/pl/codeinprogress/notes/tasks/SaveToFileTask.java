package pl.codeinprogress.notes.tasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import pl.codeinprogress.notes.R;
import pl.codeinprogress.notes.data.EncryptionHelper;
import pl.codeinprogress.notes.firebase.FirebaseApplication;

/**
 * Created by tomaszmartin on 21.06.2015.
 */

public class SaveToFileTask extends AsyncTask<String, Void, Void> {

    private Context context;
    private String path;
    private String password;

    public SaveToFileTask(Context ctx, String path) {
        this.context = ctx.getApplicationContext();
        this.path = path;
        if (this.context instanceof FirebaseApplication) {
            password = ((FirebaseApplication) context).getAuthHandler().getCredentials().getId();
        } else {
            password = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
    }

    @Override
    protected Void doInBackground(String... params) {
        saveToFile(params[0]);

        return null;
    }

    private void saveToFile(String contents) {
        if (!contents.isEmpty() && !path.isEmpty()) {
            try {
                EncryptionHelper encryptionHelper = new EncryptionHelper(password);
                contents = encryptionHelper.encrypt(contents);
                FileOutputStream fos = context.openFileOutput(path, Activity.MODE_PRIVATE);
                fos.write(contents.getBytes());
                fos.close();
                if (context instanceof FirebaseApplication) {
                    saveToFirebase(context.openFileInput(path));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void saveToFirebase(InputStream stream) {
        FirebaseApplication application = (FirebaseApplication) context;
        FirebaseStorage storage = application.getStorage();
        StorageReference reference = storage.getReferenceFromUrl(context.getString(R.string.firebase_storage_bucket));
        StorageReference current = reference.child(path);
        UploadTask uploadTask = current.putStream(stream);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(SaveToFileTask.class.getSimpleName(), "Failed to save file to Firebase this time");
                Log.d(SaveToFileTask.class.getSimpleName(), "Reason is " + exception.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(SaveToFileTask.class.getSimpleName(), "File saved to Firebase");
            }
        });
    }

    private void log(String message) {
        Log.d(SaveToFileTask.class.getSimpleName(), message);
    }

}

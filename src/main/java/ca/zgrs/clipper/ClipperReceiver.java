package ca.zgrs.clipper;

import android.app.Activity;
import android.text.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/*
 * Receives broadcast commands and controls clipboard accordingly.
 * The broadcast receiver is active only as long as the application, or its service is active.
 */
public class ClipperReceiver extends BroadcastReceiver {
    private static String TAG = "ClipboardReceiver";

    public static String ACTION_GET = "clipper.get";
    public static String ACTION_GET_SHORT = "get";
    public static String ACTION_SET = "clipper.set";
    public static String ACTION_SET_SHORT = "set";
    public static String EXTRA_TEXT = "text";

    public static boolean isActionGet(final String action) {
        return ACTION_GET.equals(action) || ACTION_GET_SHORT.equals(action);
    }

    public static boolean isActionSet(final String action) {
        return ACTION_SET.equals(action) || ACTION_SET_SHORT.equals(action);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");

        ClipboardManager cb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (isActionSet(intent.getAction())) {
            Log.d(TAG, "Setting text into clipboard");
            String text = intent.getStringExtra(EXTRA_TEXT);
            if (text != null) {
                cb.setText(text);
                setResultCode(Activity.RESULT_OK);
                setResultData("Text is copied into clipboard.");
            } else {
                setResultCode(Activity.RESULT_CANCELED);
                setResultData("No text is provided. Use -e text \"text to be pasted\"");
            }
        } else if (isActionGet(intent.getAction())) {
            Log.d(TAG, "Getting text from clipboard");
            CharSequence clip = cb.getText();
            if (clip != null) {
                Log.d(TAG, String.format("Clipboard text: %s", clip));
                setResultCode(Activity.RESULT_OK);
                setResultData(clip.toString());
            } else {
                setResultCode(Activity.RESULT_CANCELED);
                setResultData("");
            }
        } else {
            switch (intent.getAction()) {
                case "clipper.setfile":
                    String filePath = intent.getStringExtra("filepath");
                    if (filePath != null) {
                        File sourceFile = new File(filePath);
                        if (! sourceFile.exists()) {
                            setResultCode(Activity.RESULT_CANCELED);
                            setResultData("Source file does not exist.");
                            Log.d(TAG, "Source file does not exist.");
                        } else {
                            String fileText = readAllTextFromFile(sourceFile);
                            if (fileText == null) {
                                setResultCode(Activity.RESULT_CANCELED);
                                setResultData("Error reading source file.");
                                Log.d(TAG, "Error reading source file.");
                            } else {
                                cb.setText(fileText);
                                setResultCode(Activity.RESULT_OK);
                                setResultData("Text from file is copied into clipboard.");
                                Log.d(TAG, "File text now in clipboard. length " + fileText.length());
                            }
                        }
                    } else {
                        setResultCode(Activity.RESULT_CANCELED);
                        setResultData("No source filepath is provided. Use -e filepath \"/sdcard/file_to_read.txt\"");
                        Log.d(TAG, "No source filepath is provided.");
                    }
                    break;
                default:
                    Log.w(TAG, "unmatched action: " + intent.getAction());
                    break;
            }
        }
    }

    public static String readAllTextFromFile(File sourceFile) {
        //Get the text file
        File file = sourceFile;

        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
            return text.toString();
        }
        catch (IOException e) {
            Log.e(TAG, "IOException reading file", e);
            return null;
        }
    }
}
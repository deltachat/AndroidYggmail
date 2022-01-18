package chat.delta.androidyggmail.logging;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileLogger {

    private static final String TAG = FileLogger.class.getSimpleName();
    public static String DEBUG_LOG = "/log.txt";
    private final Object LOG_WRITER_LOCK = new Object();
    private BufferedWriter logWriter;
    Handler fileWriterHandler;
    HandlerThread handlerThread;
    LogObservable logObservable;


    public  FileLogger(Context context) {
        try {
            File file = new File(context.getCacheDir() + DEBUG_LOG);
            file.createNewFile();
            logWriter  =  new BufferedWriter(new FileWriter(context.getCacheDir() + DEBUG_LOG));
            handlerThread = new HandlerThread("fileWriterHandler");
            handlerThread.start();
            fileWriterHandler = new Handler(handlerThread.getLooper());
            logObservable = LogObservable.getInstance();
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String message) {
        logObservable.addLog(message);
        fileWriterHandler.post(() -> {
            try {
                synchronized (LOG_WRITER_LOCK) {
                    logWriter.write(message);
                    logWriter.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void quit() {
        handlerThread.quit();
        new Thread(this::closeLogWriter).start();
    }

    private void closeLogWriter() {
        try {
            synchronized (LOG_WRITER_LOCK) {
                logWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

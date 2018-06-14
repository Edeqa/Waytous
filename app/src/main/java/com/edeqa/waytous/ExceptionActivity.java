package com.edeqa.waytous;

import android.annotation.SuppressLint;
import android.app.ApplicationErrorReport;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionActivity extends AppCompatActivity {

    public static final String EXCEPTION = "exception"; //NON-NLS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final AlertDialog dialog = new AlertDialog.Builder(ExceptionActivity.this).create();
        dialog.setTitle(getString(R.string.app_information));

        @SuppressLint("InflateParams") View content = getLayoutInflater().inflate(R.layout.activity_exception, null); //NON-NLS

        Throwable exception = (Throwable) getIntent().getSerializableExtra(EXCEPTION);

        ApplicationErrorReport.CrashInfo crashInfo = new ApplicationErrorReport.CrashInfo(exception);
        String exName = crashInfo.exceptionClassName;
        String causeName = crashInfo.stackTrace;

        TextView text = content.findViewById(R.id.tv_exception);

        CharSequence boldExName = createSpanned(exName, new StyleSpan(Typeface.BOLD));
        CharSequence boldCauseName = createSpanned(causeName, new StyleSpan(Typeface.BOLD));
        CharSequence crashTemplate = getText(R.string.caught_an_exception_s);
        CharSequence crashMessage = TextUtils.replace(crashTemplate,
                new String[] { "%1$s", "%2$s" }, //NON-NLS
                new CharSequence[] { boldExName, boldCauseName });
        text.setText(crashMessage);

        EditText etExceptionTrace = content.findViewById(R.id.et_exception_trace);

        final StringWriter textException = new StringWriter();
        exception.printStackTrace(new PrintWriter(textException));

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        etExceptionTrace.setMaxHeight((int) (metrics.heightPixels/1.5));

        etExceptionTrace.setText(getString(R.string.in_version_s_d, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE, textException.toString()));
        etExceptionTrace.setMovementMethod(new ScrollingMovementMethod());

        etExceptionTrace.setRawInputType(InputType.TYPE_CLASS_TEXT);
        etExceptionTrace.setTextIsSelectable(true);

        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.report), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Intent intent;
                        //noinspection HardCodedStringLiteral
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://waytous.myjetbrains.com/youtrack/newIssue") //NON-NLS
                                .buildUpon()
                                .appendQueryParameter("project", "WTU")
                                .appendQueryParameter("summary", getString(R.string.uncaught_exception_in_waytous, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE))
                                .appendQueryParameter("description", textException.toString())
                                .build());
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                        }
                        startActivity(intent);

                    }
                }).start();
                finish();
            }
        });

        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.restart), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                moveTaskToBack(true);
                finish();
                startActivity(new Intent(ExceptionActivity.this, MainActivity.class));
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });

        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        dialog.setView(content);
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private CharSequence createSpanned(String s, Object... spans) {
        SpannableStringBuilder sb = new SpannableStringBuilder(s);
        for (Object span : spans) {
            sb.setSpan(span, 0, sb.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return sb;
    }
}
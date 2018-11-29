package com.example.dell.travalong;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;

/**
 * Implementation of App Widget functionality.
 */
public class UserProfileWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.user_profile_widget_provider);
        views.setTextViewText(R.id.widget_user_name, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.user_profile_widget_provider);

        Intent showIngredientIntent = new Intent(context,UserProfileActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,showIngredientIntent,0);
        views.setOnClickPendingIntent(R.id.show_user_profile,pendingIntent);


        SharedPreferences sharedPreferences = context.getSharedPreferences("USER_PROFILE_PREF",Context.MODE_PRIVATE);
        String widgetUserName = sharedPreferences.getString("USER_NAME","");
        String widgetProfileImage = sharedPreferences.getString("USER_PROFILE_IMAGE","");
        String widgetFollowerCount = sharedPreferences.getString("USER_PROFILE_FOLLOWERS_COUNT","");
        String widgetPostsCount = sharedPreferences.getString("USER_PROFILE_POST_COUNT","");
        String widgetLikesCount = sharedPreferences.getString("USER_PROFILE_LIKES_COUNT","");

        views.setTextViewText(R.id.widget_user_name, widgetUserName);
        Picasso.get().load(widgetProfileImage).into(views,R.id.widget_user_profile_backpath,new int[] {});
        views.setTextViewText(R.id.widget_user_followers_value, widgetFollowerCount);
        views.setTextViewText(R.id.widget_user_posts_value, widgetPostsCount);
        views.setTextViewText(R.id.widget_user_likes_value, widgetLikesCount);


        AppWidgetManager.getInstance(context).updateAppWidget(
                new ComponentName(context, UserProfileWidgetProvider.class), views);
    }
}


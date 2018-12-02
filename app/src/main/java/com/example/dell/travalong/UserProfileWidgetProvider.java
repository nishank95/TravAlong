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

        
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.profile_act_pref_key),Context.MODE_PRIVATE);
        String widgetUserName = sharedPreferences.getString(context.getString(R.string.user_name_pref_key),"");
        String widgetProfileImage = sharedPreferences.getString(context.getString(R.string.profile_image_pref_key),"");
        String widgetFollowerCount = sharedPreferences.getString(context.getString(R.string.profile_followers_count_pref_key),"");
        String widgetPostsCount = sharedPreferences.getString(context.getString(R.string.profile_post_count_pref_key),"");
        String widgetLikesCount = sharedPreferences.getString(context.getString(R.string.profile_likes_count_pref_key),"");

        views.setTextViewText(R.id.widget_user_name, widgetUserName);
        Picasso.get().load(widgetProfileImage).into(views,R.id.widget_user_profile_backpath,new int[] {});
        views.setTextViewText(R.id.widget_user_followers_value, widgetFollowerCount);
        views.setTextViewText(R.id.widget_user_posts_value, widgetPostsCount);
        views.setTextViewText(R.id.widget_user_likes_value, widgetLikesCount);


        AppWidgetManager.getInstance(context).updateAppWidget(
                new ComponentName(context, UserProfileWidgetProvider.class), views);
    }
}


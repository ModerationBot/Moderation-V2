package me.just.moderation.utils;

import com.jagrosh.jdautilities.command.CommandEvent;
import me.just.moderation.Bot;
import me.just.moderation.Constants;
import net.dv8tion.jda.core.entities.*;
import okhttp3.*;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static me.just.moderation.Bot.ModerationBot;

public class MiscUtils
{
    public static ThreadFactory newThreadFactory(String threadName)
    {
        return newThreadFactory(threadName, true);
    }

    public static ThreadFactory newThreadFactory(String threadName, boolean isdaemon)
    {
        return (r) ->
        {
            final Thread t = new Thread(r, threadName);
            t.setDaemon(isdaemon);
            t.setUncaughtExceptionHandler((final Thread thread, final Throwable throwable) ->
                    System.out.println("There was a uncaught exception in the {} threadpool" + thread.getName() + throwable));
            return t;
        };
    }

    public static String hastebin(final String text)
    {
        try
        {
            final String server = "https://hastebin.com/"; //requires trailing slash
            final Response response = Bot.httpClient.newCall(
                    new Request.Builder()
                            .post(RequestBody.create(MediaType.parse("text/plain"), text))
                            .url(server + "documents")
                            .header("User-Agent", "Mozilla/5.0 JDA-Butler")
                            .build()
            ).execute();

            if(!response.isSuccessful())
                return null;

            try(ResponseBody body = response.body())
            {
                if(body == null)
                    throw new IOException("We received an OK response without body when POSTing to hastebin");
                final JSONObject obj = new JSONObject(new JSONTokener(body.charStream()));
                return server + obj.getString("key");
            }

        }
        catch (final Exception e)
        {
            System.out.println("Error posting text to hastebin" + e);
            e.printStackTrace();
            return null;
        }
    }

    public static void announce(TextChannel channel, Role role, Message message, boolean slowmode)
    {
        CompletionStage<?> base;
        if (slowmode)
        {
            base = channel.getManager().setSlowmode(30).submit();
            base.thenRun(() -> channel.getManager().setSlowmode(0).queueAfter(2, TimeUnit.MINUTES));
        }
        else
        {
            base = CompletableFuture.completedFuture(null);
        }

        base.thenRun(() ->
                role.getManager().setMentionable(true).queue(v ->
                        channel.sendMessage(message).queue(v2 ->
                                role.getManager().setMentionable(false).queue()
                        )
                )
        );
    }

    public static Member getMemberFromMentionOrID(String getfrom, Guild g) {
        String userid;
        Member m = null;

        try {
            m = g.getMemberById(getfrom);
        }catch (Exception ex) {
        }
        try {
            userid = getfrom.split("<@")[1].split(">")[0];
            userid = userid.replace("!", "");
            if (userid.contains("&"))
                return null;
            try {
                m = g.getMemberById(userid);
            } catch (NullPointerException ex) {
            }
        } catch (Exception e) {
        }
        return m;
    }

    public static Guild getServer(String serverid) {
        try {
            return Bot.ModerationBot.getShardManager().getGuildById(serverid);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

/*
 * Copyright 2017 John Grosh (john.a.grosh@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.just.moderation;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.examples.command.PingCommand;
import me.just.moderation.database.MysqlConnector;
import me.just.moderation.utils.BlockingSessionController;
import me.just.moderation.utils.FormatUtil;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.cache.CacheFlag;
import net.dv8tion.jda.webhook.WebhookClient;
import net.dv8tion.jda.webhook.WebhookClientBuilder;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Bot extends ListenerAdapter {

    public static Bot ModerationBot;
    private ShardManager shards; // list of all logins the bot has
    private final ScheduledExecutorService threadpool; // threadpool to use for timings
    private final WebhookClient webhook;
    private final static Logger LOG = LoggerFactory.getLogger("Bot");
    public static OkHttpClient httpClient;
    private static MysqlConnector mySql;

    private Bot(String webhookUrl) {
        threadpool = Executors.newScheduledThreadPool(20);
        webhook = new WebhookClientBuilder(webhookUrl).build();
    }
    // protected methods
    protected void setShardManager(ShardManager shards) {
        this.shards = shards;
    }

    // public getters
    public ShardManager getShardManager() {
        return shards;
    }

    public ScheduledExecutorService getThreadpool() {
        return threadpool;
    }

    public WebhookClient getWebhook() {
        return webhook;
    }

    public static MysqlConnector getMySql() {
        return mySql;
    }

    // public methods
    public void shutdown() {
        try {
            threadpool.shutdown();
            shards.shutdown();
        } catch (Exception e) {

        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.exit(0);
            }
        }, 0);
    }


    @Override
    public void onReady(ReadyEvent event) {
        webhook.send(Constants.SUCCESS + "Shard `" + (event.getJDA().getShardInfo().getShardId() + 1) + "/"
                + event.getJDA().getShardInfo().getShardTotal() + "` has connected. Guilds: `"
                + event.getJDA().getGuilds().size() + "` Users: `" + event.getJDA().getUsers().size() + "`");
        getThreadpool().submit(() -> {
            //Async things
        });
    }

    public static void main(int shardTotal, int shardSetId, int shardSetSize) throws Exception {
        // load tokens from a file
        // 0 - ModerationBot token
        // 1 - webhook
        try {
            List<String> tokens = Files.readAllLines(Paths.get("config.txt"));

            // instantiate a Bot with a database connector
            Bot bot = new Bot(tokens.get(1));
            ModerationBot = bot;
            Bot.httpClient = new OkHttpClient.Builder().build();


            // instantiate an event waiter
            EventWaiter waiter = new EventWaiter(Executors.newSingleThreadScheduledExecutor(), false);

            // build the client to deal with commands
            CommandClient client = new CommandClientBuilder()
                    .setPrefix(Constants.PREFIX)
                    // .setAlternativePrefix("g!")
                    .setOwnerId(Constants.OWNERID)
                    .setGame(Game.watching("Just#0001"))
                    .setEmojis(Constants.SUCCESS, "\uD83D\uDCA5", "\uD83D\uDCA5")
                    .setHelpConsumer(event -> event.replyInDm(FormatUtil.formatHelp(event),
                            m -> event.getMessage().addReaction(Constants.HELPMESSAGEREACTION).queue(s -> {
                            }, f -> {
                            }),
                            f -> event.replyWarning("Help could not be sent because you are blocking Direct Messages")))
                    // .setDiscordBotsKey(tokens.get(1))
                    // .setCarbonitexKey(tokens.get(5))
                    // .setDiscordBotListKey(tokens.get(6))
                    .addCommands(
                            new PingCommand()
                    ).build();

            bot.getWebhook().send(Constants.LOADING + " Connecting to database...");
            try {
                mySql = new MysqlConnector(Constants.MYSQLHOST, Constants.MYSQLPORT, Constants.MYSQLUSERNAME, Constants.MYSQLPASSWORD, Constants.MYSQLDATABASE)
                        .initialize();
                bot.getWebhook().send(Constants.SUCCESS + " Connected to the database");
            } catch (Exception e) {
                bot.getWebhook().send(Constants.ERROR + " Cloudn't connect to the database. Shutting down...");
                bot.shutdown();
                return;
            }


            bot.getWebhook().send(Constants.LOADING + " Starting shards `" + (shardSetId * shardSetSize + 1) + " - " + ((shardSetId + 1) * shardSetSize) + "` of `" + shardTotal + "`...");
            // start logging in
            bot.setShardManager(new DefaultShardManagerBuilder()
                    .setShardsTotal(shardTotal)
                    .setShards(shardSetId * shardSetSize, (shardSetId + 1) * shardSetSize - 1)
                    .setToken(tokens.get(0))
                    .setAudioEnabled(false)
                    .setGame(Game.playing("Loading..."))
                    .setStatus(OnlineStatus.DO_NOT_DISTURB)
                    .addEventListeners(client, waiter, bot)
                    .setSessionController(new BlockingSessionController())
                    .setDisabledCacheFlags(EnumSet.of(CacheFlag.VOICE_STATE, CacheFlag.GAME))
                    .build());
        } catch (Exception ex) {
            File f = new File("config.txt");
            if (!f.exists()) f.createNewFile();
            LOG.error("The Config.txt file is invalid or doesn't exist");
            ex.printStackTrace();
        }
    }
}

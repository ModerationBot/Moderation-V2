package core;

import commands.cmdPing;
import listeners.commandListener;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import util.SECRETS;

import javax.security.auth.login.LoginException;

public class Main {
    public static void main(String[] Args) {

        JDABuilder builder = new JDABuilder(AccountType.BOT);

        builder.setToken(SECRETS.TOKEN);
        builder.setAutoReconnect(true);
        builder.setGame(Game.playing("In Programmierung"));
        builder.setStatus(OnlineStatus.DO_NOT_DISTURB);

        //listener
        builder.addEventListener(new commandListener());

        //commands
        commandHandler.commands.put("ping", new cmdPing());

        try {
            JDA jda = builder.buildBlocking();
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

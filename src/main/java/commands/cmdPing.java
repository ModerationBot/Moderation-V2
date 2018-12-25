package commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.awt.*;

public class cmdPing implements Command {
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {

        event.getTextChannel().sendMessage(new EmbedBuilder()
                .setColor(Color.green)
                .setTitle("Ping")
                .setDescription("Mein Ping: " +event.getJDA().getPing())
                .setFooter("Moderation V2", event.getJDA().getSelfUser().getAvatarUrl())
                .build()


        ).queue();
    }

    @Override
    public void executed(boolean sucess, MessageReceivedEvent event) {

    }

    @Override
    public String help() {
        return null;
    }
}

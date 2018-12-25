package me.just.moderation;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModerationBot {

    public static boolean Debug = false;

    private final static Logger LOG = LoggerFactory.getLogger("Init");

    public static void main(String[] args)
    {
        if(args.length==0)
        {
            LOG.error("Must include command line arguments");
        }
        else try
        {
            if (args.length == 5 ||args.length == 4) {
                switch (args[0]) {
                    case "bot":
                        try {
                            if (args[4].equals("-debug")) {
                                LOG.info("Debug Mode activated");
                                Debug = true;
                            } else {
                                LOG.info("Debug Mode dectivated");
                            }
                        } catch (Exception e) {
                            LOG.info("Debug Mode dectivated");
                        }
                        try {
                            LOG.info("Starting Bot...");
                            Bot.main(Integer.parseInt(args[1]), Integer.parseInt(args[2]), args.length > 3 ? Integer.parseInt(args[3]) : 16);
                        } catch (ArrayIndexOutOfBoundsException ex) {
                            LOG.error("Invalid start arguments");
                        }

                    case "none":

                    default:
                        LOG.error(String.format("Invalid startup type '%s'", args[0]));
                        return;
                }
            } else {
                LOG.error(String.format("Invalid startup type '%s'", args[0]));
                return;
            }
        }
        catch (Exception e)
        {
            LOG.error(""+e);
            e.printStackTrace();
        }
    }
}


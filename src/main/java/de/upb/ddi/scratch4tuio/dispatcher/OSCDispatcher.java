package de.upb.ddi.scratch4tuio.dispatcher;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.SimpleLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Main class
 */
public class OSCDispatcher {

    public static final int DEFAULT_PORT = 3333;

    public static void main(String[] args) throws InterruptedException {
        CommandLineArguments cli = new CommandLineArguments();
        CmdLineParser parser = new CmdLineParser(cli);
        try {
            parser.parseArgument(args);
        } catch( CmdLineException e ) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            return;
        }

        // Set log level down for verbose mode
        if( cli.silent ) {
            System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "ERROR");
            System.setProperty(SimpleLogger.LOG_KEY_PREFIX + "com.corundumstudio.socketio", "ERROR");
        } else if( cli.verbose ) {
            System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");
            System.setProperty(SimpleLogger.LOG_KEY_PREFIX + "com.corundumstudio.socketio", "INFO");
        } else {
            System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "INFO");
            System.setProperty(SimpleLogger.LOG_KEY_PREFIX + "com.corundumstudio.socketio", "ERROR");
        }
        // Configure simple logger
        System.setProperty(SimpleLogger.SHOW_SHORT_LOG_NAME_KEY, "TRUE");

        // Since we might have changed the logging level before, the logger is instantiated here and not in the header
        final Logger log = LoggerFactory.getLogger(OSCDispatcher.class);

        log.info("Starting dispatcher...");
        OSCSocketIODispatcher dispatcher = new OSCSocketIODispatcher(cli.port);
        dispatcher.startSocketIOServer();
        dispatcher.connect();

        log.info("OSC dispatcher is running.");
        log.debug("ports: 5000 => {}", cli.port);

        try {
            final String help = "> Available commands: restart, quit";
            final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            while(true) {
                Thread.sleep(100);

                System.out.println(help);
                System.out.print("> ");
                String userCommand = reader.readLine();

                if(userCommand.equals("restart")) {
                    log.info("Restarting dispatcher...");
                    dispatcher.disconnect();
                    dispatcher.stopSocketIOServer();
                    dispatcher = null;
                    Thread.sleep(100);

                    dispatcher = new OSCSocketIODispatcher(cli.port);
                    dispatcher.startSocketIOServer();
                    dispatcher.connect();
                    log.info("OSC dispatcher was restarted and is running again.");
                } else if(userCommand.equals("quit") || userCommand.equals("exit")) {
                    log.info("Shutting down...");
                    dispatcher.disconnect();
                    dispatcher.stopSocketIOServer();
                    Thread.sleep(100);

                    log.info("...bye!");
                    return;
                } else {
                    System.err.println("Command not recognized.");
                }
            }
        } catch (IOException e) {
            log.trace(e.getMessage(), e);

            log.error("Shutting down because of error...");
            dispatcher.disconnect();
            dispatcher.stopSocketIOServer();
        }
    }

    /**
     * Configuration of cli arguments for args4j
     */
    private static class CommandLineArguments {
        @Option(name = "-p", aliases = {"--port"},
                usage = "port number to listen for osc messages from")
        public int port = DEFAULT_PORT;

        @Option(name = "-v", aliases = {"--verbose"},
                usage = "sets verbose mode")
        public boolean verbose = false;

        @Option(name = "-s", aliases = {"--silent"},
                usage = "sets silent mode (overwrites -v)")
        public boolean silent = false;
    }
}

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CmdLineParser {

    private final int DEFAULT_LIFTS = 40;
    private final int DEFAULT_AVG_RIDES = 10;
    private final int MAX_THREADS = 1024;
    private final int MAX_SKIERS = 100000;
    private final int MIN_LIFTS = 5;
    private final int MAX_LIFTS = 60;
    private final int MAX_AVG_RIDES = 20;

    private Options options;
    private DefaultParser parser;

    public CmdLineParser() {
        this.options = new Options();
        this.parser = new DefaultParser();

        Option threads = Option.builder()
                .longOpt("threads")
                .argName("threads")
                .hasArg()
                .required(true)
                .desc("maximum number of threads to run (numThreads - max 1024)")
                .build();
        options.addOption(threads);

        Option skiers = Option.builder()
                .longOpt("skiers")
                .argName("skiers")
                .hasArg()
                .required(true)
                .desc("number of skier to generate lift rides for (numSkiers - max 100000)")
                .build();
        options.addOption(skiers);

        Option lifts = Option.builder()
                .longOpt("lifts")
                .argName("lifts")
                .hasArg()
                .desc("number of ski lifts (numLifts - range 5-60, default 40)")
                .build();
        options.addOption(lifts);

        Option avgRides = Option.builder()
                .longOpt("avgRides")
                .argName("avgRides")
                .hasArg()
                .desc("mean numbers of ski lifts each skier rides each day (numRuns - default 10, max 20)")
                .build();
        options.addOption(avgRides);

        Option ipAndPort = Option.builder()
                .longOpt("ipAndPort")
                .argName("ipAndPort")
                .hasArg()
                .required(true)
                .desc("IP/port address of the server")
                .build();
        options.addOption(ipAndPort);

        Option context = Option.builder()
                .longOpt("context")
                .argName("context")
                .hasArg()
                .desc("Server context")
                .build();
        options.addOption(context);
    }

    public InputArgs parseInputArgs(String[] args) throws ParseException {
        List<String> res = new ArrayList<>(Arrays.asList(args));

        if (this.options.getOption("lifts").getValue() == null) {
            res.add("--lifts");
            res.add(String.valueOf(DEFAULT_LIFTS));
        }

        if (this.options.getOption("avgRides").getValue() == null) {
            res.add("--avgRides");
            res.add(String.valueOf(DEFAULT_AVG_RIDES));
        }

        args = res.toArray(new String[0]);
        CommandLine cmd = this.parser.parse(this.options, args);

        HelpFormatter helper = new HelpFormatter();
        helper.printHelp("client", options);
        System.out.println();

        InputArgs inputArgs;
        try {
            inputArgs = new InputArgs(
                    Integer.parseInt(cmd.getOptionValue("threads")),
                    Integer.parseInt(cmd.getOptionValue("skiers")),
                    Integer.parseInt(cmd.getOptionValue("lifts")),
                    Integer.parseInt(cmd.getOptionValue("avgRides")),
                    cmd.getOptionValue("ipAndPort"),
                    cmd.getOptionValue("context")
            );

            if (inputArgs.getNumThread() <= 0 || inputArgs.getNumThread() > MAX_THREADS) {
                throw new ParseException("Threads number is not correct.");
            }
            if (inputArgs.getNumSkier() <= 0 || inputArgs.getNumSkier() > MAX_SKIERS) {
                throw new ParseException("Number of skiers is not correct.");
            }
            if (inputArgs.getNumLift() < MIN_LIFTS || inputArgs.getNumLift() > MAX_LIFTS) {
                throw new ParseException("Number of lifts is not correct.");
            }
            if (inputArgs.getNumAvgRide() < 0 || inputArgs.getNumAvgRide() > MAX_AVG_RIDES) {
                throw new ParseException("Number of average rides is not correct.");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            helper.printHelp("Usage:", options);
            return null;
        }

        return inputArgs;
    }
}

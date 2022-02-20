import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class LatencyProcessor {

    private static final String CSV_FILE = "./latency_records.csv";

    List<LatencyRecord> latencies;
    double meanLatency;
    double medianLatency;
    double throughput;
    double percent_99;
    double minLatency;
    double maxLatency;

    public LatencyProcessor(List<LatencyRecord> latencies, double throughput) {
        this.latencies = latencies;
        this.throughput = throughput;
    }

    public void writeToCSV() throws IOException {
        FileWriter out = new FileWriter(CSV_FILE);

        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT)) {
            this.latencies.forEach(r -> {
                try {
                    printer.printRecord(r.getStartTime(), r.getType(), r.getLatency(),
                            r.getResCode());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void processAndPrintResults() {
        DescriptiveStatistics s = new DescriptiveStatistics();
        List<Long> resTimes = new ArrayList<>();
        for (LatencyRecord r : this.latencies) {
            resTimes.add(r.getLatency());
            s.addValue(r.getLatency());
        }
        Collections.sort(resTimes);
        meanLatency = (double) calculateAverage(resTimes);
        medianLatency = (double) calculateMedian(resTimes);
        percent_99 = (double) s.getPercentile(99);
        minLatency = (double) Collections.min(resTimes);
        maxLatency = (double) Collections.max(resTimes);

        System.out.printf("mean response time:\t\t\t %.2f millisecs\n", meanLatency);
        System.out.printf("median response time:\t\t\t %.2f millisecs\n", medianLatency);
        System.out.printf("throughput:\t\t\t\t\t\t\t %.2f request/second\n", throughput);
        System.out.printf("99th response time:\t\t\t %.2f millisecs\n", percent_99);
        System.out.printf("min response time:\t\t\t %.2f millisecs\n", minLatency);
        System.out.printf("max response time:\t\t\t %.2f millisecs\n", maxLatency);
    }

    private double calculateAverage(List<Long> resTimes) {
        return resTimes.stream().mapToDouble(a -> a).average().orElse(0.0);
    }

    private long calculateMedian(List<Long> resTimes) {
        int middle = resTimes.size() / 2;
        middle = middle > 0 && middle % 2 == 0 ? middle - 1 : middle;
        return resTimes.get(middle);
    }

    public long percentile(List<Long> latencies, double percentile) {
        int index = (int) Math.ceil(percentile / 100.0 * latencies.size());
        return latencies.get(index - 1);
    }
}

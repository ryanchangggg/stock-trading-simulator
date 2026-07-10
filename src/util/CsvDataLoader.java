package util;

import model.market.Market;
import model.stock.EquityStock;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Stream;

/**
 * Loads stock data from CSV files into the Market.
 * <p>
 * Scans a directory for CSV files with OHLCV data
 * (Date,Open,High,Low,Close,Volume) and registers each
 * as an EquityStock in the market with the latest close price.
 */
public class CsvDataLoader {

    private final Market market;

    public CsvDataLoader(Market market) {
        this.market = market;
    }

    public void loadAll(String dataDir) throws IOException {
        Path dir = Paths.get(dataDir);
        if (!Files.isDirectory(dir)) return;

        try (Stream<Path> files = Files.list(dir)) {
            files.filter(p -> p.toString().endsWith(".csv"))
                 .forEach(this::loadFile);
        }
    }

    private void loadFile(Path path) {
        try {
            String symbol = path.getFileName().toString()
                .replace(".csv", "").toUpperCase();
            List<String> lines = Files.readAllLines(path);
            if (lines.size() < 2) return;

            // Find the last non-empty data line to get the latest price
            String lastLine = null;
            for (int i = lines.size() - 1; i >= 1; i--) {
                String line = lines.get(i).trim();
                if (!line.isEmpty()) { lastLine = line; break; }
            }
            if (lastLine == null) return;

            String[] parts = lastLine.split(",");
            if (parts.length < 5) return;

            BigDecimal close = new BigDecimal(parts[4].trim());
            String companyName = symbol; // CSV doesn't include company name
            String sector = "N/A";

            EquityStock stock = new EquityStock(
                symbol, companyName, sector, close, 0, 0);
            market.addStock(stock);

        } catch (Exception e) {
            System.err.println("Skipping " + path + ": " + e.getMessage());
        }
    }
}

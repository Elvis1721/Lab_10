package it.unibo.mvc;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

/**
 * Application entry point for DrawNumber game.
 */
public final class DrawNumberApp implements DrawNumberViewObserver {

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * Constructor.
     *
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        int min = 0;
        int max = 100;
        int attempts = 100;

        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }

        final InputStream in = Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("config.yml"));
        try (BufferedReader br = new BufferedReader(
            new InputStreamReader(in, StandardCharsets.UTF_8)
        )) {
            String string;
            StringTokenizer token;

            for (int i = 0; i < 3; i++) {
                string = Objects.requireNonNull(br.readLine());
                token = new StringTokenizer(string, ":");
                switch (token.nextToken()) {
                    case "minimum" -> min = Integer.parseInt(token.nextToken().trim());
                    case "maximum" -> max = Integer.parseInt(token.nextToken().trim());
                    case "attempts" -> attempts = Integer.parseInt(token.nextToken().trim());
                    default -> IO.println("switch entered defalut case");
                }
            }
        } catch (final IOException ignored) {
            IO.println("Exception catched in Configuration");
        }
        this.model = new DrawNumberImpl(min, max, attempts);
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (final IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    @SuppressFBWarnings(
        value = "DM_EXIT",
        justification = "Acceptable for exercising purposes."
    )
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * Application entry point.
     *
     * @throws FileNotFoundException if the configuration file cannot be fetched
     */
    public static void main() throws FileNotFoundException {
        new DrawNumberApp(new DrawNumberViewImpl(), 
        new DrawNumberViewImpl(), 
        new PrintStreamView("output.log"), 
        new PrintStreamView(System.out)
        );
    }

}

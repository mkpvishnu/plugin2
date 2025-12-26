package com.example.prophunt.game;

import com.example.prophunt.PropHuntPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

/**
 * Manages game timers and countdowns.
 */
public class GameTimer {

    private final PropHuntPlugin plugin;
    private final Game game;

    private BukkitTask currentTask;
    private int timeRemaining;
    private boolean running;

    public GameTimer(PropHuntPlugin plugin, Game game) {
        this.plugin = plugin;
        this.game = game;
        this.timeRemaining = 0;
        this.running = false;
    }

    /**
     * Starts a countdown timer.
     *
     * @param seconds duration in seconds
     * @param onTick called each second with remaining time
     * @param onComplete called when timer finishes
     */
    public void start(int seconds, Consumer<Integer> onTick, Runnable onComplete) {
        stop();

        this.timeRemaining = seconds;
        this.running = true;

        currentTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!running) {
                    cancel();
                    return;
                }

                // Call tick callback
                if (onTick != null) {
                    onTick.accept(timeRemaining);
                }

                // Check if complete
                if (timeRemaining <= 0) {
                    running = false;
                    cancel();
                    if (onComplete != null) {
                        onComplete.run();
                    }
                    return;
                }

                timeRemaining--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }

    /**
     * Stops the current timer.
     */
    public void stop() {
        running = false;
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }
    }

    /**
     * Pauses the timer.
     */
    public void pause() {
        running = false;
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }
    }

    /**
     * Resumes a paused timer.
     *
     * @param onTick tick callback
     * @param onComplete completion callback
     */
    public void resume(Consumer<Integer> onTick, Runnable onComplete) {
        if (timeRemaining > 0) {
            start(timeRemaining, onTick, onComplete);
        }
    }

    /**
     * Gets remaining time in seconds.
     *
     * @return remaining seconds
     */
    public int getTimeRemaining() {
        return timeRemaining;
    }

    /**
     * Gets remaining time formatted.
     *
     * @return formatted time string (m:ss)
     */
    public String getFormattedTime() {
        int minutes = timeRemaining / 60;
        int seconds = timeRemaining % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * Checks if timer is running.
     *
     * @return true if running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Adds time to the timer.
     *
     * @param seconds seconds to add
     */
    public void addTime(int seconds) {
        this.timeRemaining += seconds;
    }

    /**
     * Removes time from the timer.
     *
     * @param seconds seconds to remove
     */
    public void removeTime(int seconds) {
        this.timeRemaining = Math.max(0, this.timeRemaining - seconds);
    }

    /**
     * Sets the remaining time.
     *
     * @param seconds new time
     */
    public void setTime(int seconds) {
        this.timeRemaining = Math.max(0, seconds);
    }

    /**
     * Creates a delayed task.
     *
     * @param delayTicks delay in ticks
     * @param task the task to run
     * @return the scheduled task
     */
    public BukkitTask runDelayed(long delayTicks, Runnable task) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        }.runTaskLater(plugin, delayTicks);
    }

    /**
     * Creates a repeating task.
     *
     * @param delayTicks initial delay
     * @param periodTicks period between runs
     * @param task the task to run
     * @return the scheduled task
     */
    public BukkitTask runRepeating(long delayTicks, long periodTicks, Runnable task) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        }.runTaskTimer(plugin, delayTicks, periodTicks);
    }
}

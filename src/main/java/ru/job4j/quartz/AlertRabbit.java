package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

/**
 * Quartz scheduler.
 * Doing actions by schedule: writing current date by interval to DB.
 * Getting interval from .properties file.
 */
public class AlertRabbit {
    private static final Logger LOG = LoggerFactory.getLogger(AlertRabbit.class);

    public static void main(String[] args) {
        Properties pr = new Properties();
        try (InputStream is = AlertRabbit.class.getClassLoader()
                .getResourceAsStream("rabbit.properties")) {
            pr.load(is);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }

        try (Connection conn = DriverManager.getConnection(
                pr.getProperty("url"),
                pr.getProperty("username"),
                pr.getProperty("password"))) {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("store", conn);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(Integer.parseInt(pr.getProperty("rabbit.interval")))
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10_000);
            scheduler.shutdown();
        } catch (SQLException | SchedulerException | InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public static class Rabbit implements Job {

        public Rabbit() {
            System.out.println(hashCode());
        }

        @Override
        public void execute(JobExecutionContext context) {
            System.out.println("Rabbit runs here ...");
            Connection conn = (Connection) context
                    .getJobDetail()
                    .getJobDataMap()
                    .get("store");
            try (PreparedStatement ps = conn.prepareStatement(
                         "insert into rabbit (created_date) values (?)")) {
                ps.setLong(1, System.currentTimeMillis());
                ps.executeUpdate();
            } catch (SQLException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }
}
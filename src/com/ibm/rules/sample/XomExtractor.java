package com.ibm.rules.sample;

import ilog.rules.teamserver.brm.IlrBaseline;
import ilog.rules.teamserver.brm.IlrResource;
import ilog.rules.teamserver.brm.IlrRuleProject;
import ilog.rules.teamserver.client.IlrRemoteSessionFactory;
import ilog.rules.teamserver.model.*;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class XomExtractor {

    public static void run(String url, String datasource, String user, String password, String project, String baselineName, String xomName, String filePath) {

        IlrSessionFactory factory = new IlrRemoteSessionFactory();
        IlrSession session = null;

        try {

			// connect to Decision Center
            factory.connect(user, password, url, datasource);
            session = factory.getSession();
            session.beginUsage();

            // get the requested project
            IlrRuleProject ruleProject = IlrSessionHelper.getProjectNamed(session, project);
            if (ruleProject == null) {
                System.err.format("Project not found: '%s' %n", project);
                return;
            }

            // get the requested baseline, or main if it can't be found
            IlrBaseline baseline;
            if (baselineName != null) {
                baseline = IlrSessionHelper.getBaselineNamed(session, ruleProject, baselineName);
                if (baseline == null) {
                    System.err.format("Baseline not found: '%s' - using current %n", baselineName);
                    baseline = ruleProject.getCurrentBaseline();
                }
            } else {
                baseline = ruleProject.getCurrentBaseline();
            }
            session.setWorkingBaseline(baseline);

            // builds a query to find all resources
            IlrDefaultSearchCriteria criteria = new IlrDefaultSearchCriteria("Find all resources");
            List<IlrElementDetails> elements = session.findElementDetails(criteria);

            // find the requested XOM within the resources
            IlrResource xom = null;
            for (IlrElementDetails element : elements) {
                if (element.getName().equals(xomName)) {
                    xom = (IlrResource) element;
                }
            }
            // writes the XOM to disk
            if (xom != null) {
                byte[] xomBytes = xom.getBody();
                Path path = Paths.get(filePath);
                Files.write(path, xomBytes);
                System.out.format("Saved file: '%s' %n", filePath);
            } else {
                System.err.format("XOM not found: '%s' %n", xomName);
            }

        } catch (IlrConnectException | IlrApplicationException | IOException e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.endUsage();
            }
        }

    }

    public static void main(String args[]) {

        Options options = new Options();

        Option url = Option.builder("url").hasArg().argName("url").required().desc("Decision Center URL").build();
        Option datasource = Option.builder("datasource").hasArg().argName("datasource").required().desc("JDBC datasource").build();
        Option user = Option.builder("user").hasArg().argName("user").required().desc("User name").build();
        Option password = Option.builder("password").hasArg().argName("password").required().desc("User password").build();
        Option project = Option.builder("project").hasArg().argName("project").required().desc("Project name").build();
        Option baseline = Option.builder("baseline").hasArg().argName("baseline").desc("Baseline (current if not provided)").build();
        Option xom = Option.builder("xom").hasArg().argName("xom").required().desc("XOM name").build();
        Option filepath = Option.builder("filepath").hasArg().argName("filepath").required().desc("Output file path").build();

        options.addOption(url);
        options.addOption(datasource);
        options.addOption(user);
        options.addOption(password);
        options.addOption(project);
        options.addOption(baseline);
        options.addOption(xom);
        options.addOption(filepath);

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            XomExtractor.run(
                    cmd.getOptionValue("url"),
                    cmd.getOptionValue("datasource"),
                    cmd.getOptionValue("user"),
                    cmd.getOptionValue("password"),
                    cmd.getOptionValue("project"),
                    cmd.getOptionValue("baseline"),
                    cmd.getOptionValue("xom"),
                    cmd.getOptionValue("filepath")
            );
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java", options);
        }

    }

}
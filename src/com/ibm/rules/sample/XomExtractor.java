package com.ibm.rules.sample;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import ilog.rules.teamserver.brm.IlrBaseline;
import ilog.rules.teamserver.brm.IlrBrmPackage;
import ilog.rules.teamserver.brm.IlrResource;
import ilog.rules.teamserver.brm.IlrRuleProject;
import ilog.rules.teamserver.client.IlrRemoteSessionFactory;
import ilog.rules.teamserver.model.IlrApplicationException;
import ilog.rules.teamserver.model.IlrConnectException;
import ilog.rules.teamserver.model.IlrDefaultSearchCriteria;
import ilog.rules.teamserver.model.IlrElementDetails;
import ilog.rules.teamserver.model.IlrObjectNotFoundException;
import ilog.rules.teamserver.model.IlrSession;
import ilog.rules.teamserver.model.IlrSessionFactory;
import ilog.rules.teamserver.model.IlrSessionHelper;
import ilog.rules.teamserver.model.permissions.IlrRoleRestrictedPermissionException;

public class XomExtractor {


	private IlrBaseline findBaseline(IlrSession session, String url, String datasource, String user, String password, String project, String baselineName) throws IlrObjectNotFoundException {
		IlrBaseline baseline = null;
		
        // get the requested project
        IlrRuleProject ruleProject = IlrSessionHelper.getProjectNamed(session, project);
        if (ruleProject == null) {
            System.err.format("Project not found: '%s' %n", project);
        } else {
	        // get the requested baseline, or main if none was specified
	        IlrBaseline mainBaseline = ruleProject.getCurrentBaseline(); // initializes to the main branch
	        baseline = mainBaseline;
	        if (baselineName != null) {
	            baseline = IlrSessionHelper.getBaselineNamed(session, ruleProject, baselineName);
	            if (baseline == null) {
	                System.err.format("Baseline not found: '%s' - using main %n", baselineName);
	                baseline = mainBaseline;
	            }
	        }
        }
        
		return baseline;
	}
	
	private IlrResource findXOM(IlrSession session, String xomName) throws IlrRoleRestrictedPermissionException, IlrObjectNotFoundException {
        // builds a query to find the XOM resource
        //
        IlrBrmPackage brm = session.getBrmPackage(); // BR Model package, to get meta data from
        IlrDefaultSearchCriteria criteria = new IlrDefaultSearchCriteria(brm.getResource()); // select the EClass for "resources"
        criteria.setFeatures(Arrays.asList(brm.getModelElement_Name())); // add a selection criteria on the name
        criteria.setValues(Arrays.asList(xomName)); // whose value should be xomName
        List<IlrElementDetails> elements = session.findElementDetails(criteria); // run the query

        // Check if we found anything
        IlrResource xom = elements.isEmpty() ? null : (IlrResource)elements.get(0);
        return xom;
	}
	
	public void extractXOM(String url, String datasource, String user, String password, String project, String baselineName, String xomName, String filePath) {

        IlrSessionFactory factory = new IlrRemoteSessionFactory();
        IlrSession session = null;

        try {

			// connect to Decision Center
            factory.connect(user, password, url, datasource);
            session = factory.getSession();
            session.beginUsage();

            IlrBaseline baseline = findBaseline(session, url, datasource, user, password, project, baselineName);
            session.setWorkingBaseline(baseline);

            IlrResource xom = findXOM(session, xomName);
                        
            // writes the XOM to disk
            if (xom != null) {
                byte[] xomBytes = xom.getBody();
                Path path = Paths.get(filePath);
                Files.write(path, xomBytes);
                System.err.format("Saved file: '%s' %n", filePath);
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

	public void uploadXOM(String url, String datasource, String user, String password, String project, String baselineName, String xomName, String filePath) {

        IlrSessionFactory factory = new IlrRemoteSessionFactory();
        IlrSession session = null;

        try {

			// connect to Decision Center
            factory.connect(user, password, url, datasource);
            session = factory.getSession();
            session.beginUsage();

            IlrBaseline baseline = findBaseline(session, url, datasource, user, password, project, baselineName);
            session.setWorkingBaseline(baseline);

            IlrResource xom = findXOM(session, xomName);
            IlrBrmPackage brm = session.getBrmPackage(); // BR Model package, to get meta data from

            // reads the new XOM from disk
            if (xom != null) {
                Path path = Paths.get(filePath);
                byte[] xomBytes = Files.readAllBytes(path);
                // store this new files in the resource
                xom.setRawValue(brm.getResource_Body(), xomBytes);
                // and commits this to Decision Center
                session.commit(xom);
                System.err.format("Read file: '%s' %n", filePath);
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

        Option command = Option.builder("command").hasArg().argName("command").required().desc("Command (download or upload)").build();
        Option url = Option.builder("url").hasArg().argName("url").required().desc("Decision Center URL").build();
        Option datasource = Option.builder("datasource").hasArg().argName("datasource").required().desc("JDBC datasource").build();
        Option user = Option.builder("user").hasArg().argName("user").required().desc("User name").build();
        Option password = Option.builder("password").hasArg().argName("password").required().desc("User password").build();
        Option project = Option.builder("project").hasArg().argName("project").required().desc("Project name").build();
        Option baseline = Option.builder("baseline").hasArg().argName("baseline").desc("Baseline (current if not provided)").build();
        Option xom = Option.builder("xom").hasArg().argName("xom").required().desc("XOM name").build();
        Option filepath = Option.builder("filepath").hasArg().argName("filepath").required().desc("Output file path").build();

        options.addOption(command);
        options.addOption(url);
        options.addOption(datasource);
        options.addOption(user);
        options.addOption(password);
        options.addOption(project);
        options.addOption(baseline);
        options.addOption(xom);
        options.addOption(filepath);

        CommandLineParser parser = new DefaultParser();
        XomExtractor xomExtractor = new XomExtractor();
        try {
            CommandLine cmd = parser.parse(options, args);
            String runCommand = cmd.getOptionValue("command");
            if ("download".equalsIgnoreCase(runCommand)) {
	            xomExtractor.extractXOM(
	                    cmd.getOptionValue("url"),
	                    cmd.getOptionValue("datasource"),
	                    cmd.getOptionValue("user"),
	                    cmd.getOptionValue("password"),
	                    cmd.getOptionValue("project"),
	                    cmd.getOptionValue("baseline"),
	                    cmd.getOptionValue("xom"),
	                    cmd.getOptionValue("filepath")
	            );
            }
            if ("upload".equalsIgnoreCase(runCommand)) {
	            xomExtractor.uploadXOM(
	                    cmd.getOptionValue("url"),
	                    cmd.getOptionValue("datasource"),
	                    cmd.getOptionValue("user"),
	                    cmd.getOptionValue("password"),
	                    cmd.getOptionValue("project"),
	                    cmd.getOptionValue("baseline"),
	                    cmd.getOptionValue("xom"),
	                    cmd.getOptionValue("filepath")
	            );
            }
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java", options);
        }

    }

}
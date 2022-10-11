import org.apache.commons.io.FileUtils

import java.util.concurrent.TimeUnit

/**
 * @author Josef Sustacek
 */
interface Orca {
    File sharedVolumePath()
    File buildsPath()
    default File specResultsBasePath() {
        return new File(System.getProperty('ORCA_TEST__SPEC_RESULTS_BASE_PATH'))
    }
    List<String> orcaRunCommandParts()

    default Process startOrca(String... orcaArgs) {
        def orca = (orcaRunCommandParts() + (orcaArgs ? orcaArgs.toList() : []))

        return startSubProcess(orca.toArray(new String[0]))
    }

    // before all specs
    default void setupSpec() {
        def specResultsBasePath = specResultsBasePath()
        specResultsBasePath.mkdirs()
    }

    // before one Orca run (~ 1 Spock spec)
    default void setup() {
        // expected to be present for the 'docker -v ...' mounts, so need to be created
        // before the RUN command is executed
        sharedVolumePath().mkdirs()
        buildsPath().mkdirs()
    }

    // after one Orca run (~ 1 Spock spec)
    default void cleanup() {
        def buildsPath = buildsPath()
        def sharedVolumePath = sharedVolumePath()

        // archive the results
        def featureResultsDir = new File(specResultsBasePath(), 'feature_run_' + System.currentTimeMillis())
        FileUtils.copyDirectory(buildsPath, new File(featureResultsDir, buildsPath.name))
        FileUtils.copyDirectory(sharedVolumePath, new File(featureResultsDir, sharedVolumePath.name))

        print "Feature run results (Orca build dir, shared volume, possibly stdout as a file...) archived in '${featureResultsDir.absolutePath}'"

        // clean the "build" dirs of ORCA
        FileUtils.cleanDirectory(buildsPath)
        FileUtils.cleanDirectory(sharedVolumePath)
    }

    // helper methods
    default ProcessBuilder buildSubProcess(String... command) {
        def pb = new ProcessBuilder(command)
        pb.redirectErrorStream(true)

        return pb
    }

    default Process startSubProcess(String... command) {
        def processBuilder = buildSubProcess(command)

        println "Starting sub-process: \n  " + processBuilder.command().join(' ')

        // merge stdout + stderr
        processBuilder.redirectErrorStream(true)

        // this would cause streaming to stdout
        //        processBuilder.inheritIO()

        return processBuilder.start()
    }

}

class NativeOrca implements Orca {

    File sharedVolumePath() {
        // as hard-coded in ORCA sources
        return new File('/opt/liferay/shared-volume')
    }

    @Override
    File buildsPath() {
        // as hard-coded in ORCA sources
        return new File('/opt/liferay/orca/builds')
    }

    @Override
    List<String> orcaRunCommandParts() {
        // has to be in $PATH
        return 'orca'
    }

}

class DockerOrca implements Orca {

    File sharedVolumePath() {
        return new File(System.getProperty('ORCA_TEST_DOCKER__SHARED_VOLUME_PATH'))
    }

    File buildsPath() {
        return new File(System.getProperty('ORCA_TEST_DOCKER__BUILDS_PATH'))
    }

    // The 'run' command for Orca as passed from Gradle
    List<String> orcaRunCommandParts() {
        def separator = System.getProperty('ORCA_TEST_DOCKER__COMMAND_PARTS_SEPARATOR')
        def parts = System.getProperty('ORCA_TEST_DOCKER__RUN_COMMAND_PARTS')

        return parts.split(separator)?.toList()
    }

    // The 'cleanup' command for Orca, as passed from Gradle
    List<String> orcaCleanBeforeRunCommandParts() {
        def separator = System.getProperty('ORCA_TEST_DOCKER__COMMAND_PARTS_SEPARATOR')
        def parts = System.getProperty('ORCA_TEST_DOCKER__CLEAN_COMMAND_PARTS')

        return parts.split(separator)?.toList()
    }

    void setup() {
        def orcaCleanBeforeRun = startSubProcess(orcaCleanBeforeRunCommandParts().toArray(new String[0]))

        orcaCleanBeforeRun.waitFor(15, TimeUnit.SECONDS)

//        println "ORCA CLEAN: " + orcaCleanBeforeRun.inputStream.text
    }

}

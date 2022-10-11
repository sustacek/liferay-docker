import org.apache.commons.io.FileUtils

import java.util.concurrent.TimeUnit

class BuildSpec extends BaseOrcaSpec {

    def "'build' should create docker-compose.yml"(int timeoutSeconds) {
        when:
            def orcaRun = orca.startOrca('build', '1.0.0')
            orcaRun.waitFor(timeoutSeconds, TimeUnit.SECONDS)

        then:
            orcaRun.exitValue() == 0

            def stdout = orcaRun.inputStream.getText('utf-8')

            stdout.contains('Using the default single server configuration.')

            stdout.contains('Building antivirus')
            stdout.contains('Building backup')
            stdout.contains('Building db')
            stdout.contains('Building liferay')
            stdout.contains('Building log-proxy')
            stdout.contains('Building log-server')
            stdout.contains('Building search')
            stdout.contains('Building web-server')

            def dockerCompose = new File(orca.buildsPath(), '1.0.0/docker-compose.yml')
            dockerCompose.isFile()

            // TODO use YAML parser?
            dockerCompose.text.contains('services:')
        
            dockerCompose.text.contains('antivirus:')
            dockerCompose.text.contains('backup:')
            dockerCompose.text.contains('ci:')
            dockerCompose.text.contains('db:')
            dockerCompose.text.contains('liferay:')
            dockerCompose.text.contains('log-proxy:')
            dockerCompose.text.contains('log-server:')
            dockerCompose.text.contains('search:')
            dockerCompose.text.contains('web-server:')

        cleanup:
            dumpOrcaStdoutToFile(stdout)

        where:
            timeoutSeconds = 30
    }
}
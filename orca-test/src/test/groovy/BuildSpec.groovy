import groovy.yaml.YamlSlurper
import spock.lang.Ignore

import java.util.concurrent.TimeUnit

class BuildSpec extends BaseOrcaSpec {

    def "'build <version>' should create docker-compose.yml based on default config"(int timeoutSeconds) {
        when:
            // build some non-default (non-1.0.0) version
            def orcaRun = orca.startOrca([:], 'build', '2.1.42')
            orcaRun.waitFor(timeoutSeconds, TimeUnit.SECONDS)

        then:
            orcaRun.exitValue() == 0

            def stdout = orcaRun.inputStream.getText('utf-8')

            stdout.contains('Using the default single server configuration.')

            def dockerCompose = new File(orca.buildsPath(), '2.1.42/docker-compose.yml')
            dockerCompose.isFile()

            // Parse YAML, confirm the content
            def ys = new YamlSlurper()

            def dockerComposeYaml = ys.parseText(dockerCompose.text)

            dockerComposeYaml.services != null

            dockerComposeYaml.services.antivirus?.image == 'antivirus:2.1.42'
            dockerComposeYaml.services.backup?.image == 'backup:2.1.42'
            dockerComposeYaml.services.ci?.image == 'ci:2.1.42'
            dockerComposeYaml.services.db?.image == 'db:2.1.42'
            dockerComposeYaml.services.liferay?.image == 'liferay:2.1.42'
            dockerComposeYaml.services.'log-proxy'?.image == 'log-proxy:2.1.42'
            dockerComposeYaml.services.'log-server'?.image == 'log-server:2.1.42'
            dockerComposeYaml.services.search?.image == 'search:2.1.42'
            dockerComposeYaml.services.vault?.image == 'vault:2.1.42'
            dockerComposeYaml.services.'web-server'?.image == 'web-server:2.1.42'

        cleanup:
            dumpOrcaStdoutToFile(stdout)
            orcaRun.destroyForcibly()

        where:
            timeoutSeconds = 60
    }

    def "'build <version>' with ORCA_HOST and ORCA_CONFIG should fail for non-matching host"(int timeoutSeconds) {
        when:
            // build some non-default (non-1.0.0) version
            def orcaRun =
                    orca.startOrca(
                            [
                                    ORCA_CONFIG: 'three_servers',
                                    ORCA_HOST: 'vm-999'
                            ],
                            'build', '2.1.42')
            orcaRun.waitFor(timeoutSeconds, TimeUnit.SECONDS)

        then:
            orcaRun.exitValue() == 1

            def stdout = orcaRun.inputStream.getText('utf-8')

            stdout.contains('Using configuration configs/three_servers.yml.')
            stdout.contains('Unable to find a matching host')

        cleanup:
            dumpOrcaStdoutToFile(stdout)
            orcaRun.destroyForcibly()

        where:
            timeoutSeconds = 60
    }

    def "'build <version>' with ORCA_HOST and ORCA_CONFIG should create docker-compose.yml based on provided config"(int timeoutSeconds) {
        when:
            // build some non-default (non-1.0.0) version
            def orcaRun =
                    orca.startOrca(
                            [
                                    ORCA_CONFIG: 'three_servers',
                                    ORCA_HOST: 'vm-1'
                            ],
                            'build', '2.1.42')
            orcaRun.waitFor(timeoutSeconds, TimeUnit.SECONDS)
            
        then:
            orcaRun.exitValue() == 0

            def stdout = orcaRun.inputStream.getText('utf-8')

            stdout.contains('Using configuration configs/three_servers.yml.')

            def dockerCompose = new File(orca.buildsPath(), '2.1.42/docker-compose.yml')
            dockerCompose.isFile()

            // Parse YAML, confirm the content
            def ys = new YamlSlurper()

            def dockerComposeYaml = ys.parseText(dockerCompose.text)

            dockerComposeYaml.services != null

            dockerComposeYaml.services.db?.image == 'db:2.1.42'
            dockerComposeYaml.services.liferay?.image == 'liferay:2.1.42'
            dockerComposeYaml.services.'log-proxy'?.image == 'log-proxy:2.1.42'
            dockerComposeYaml.services.search?.image == 'search:2.1.42'
            dockerComposeYaml.services.'web-server'?.image == 'web-server:2.1.42'

            // no other services except the wanted ones
            dockerComposeYaml.services.size() == 5


        cleanup:
            dumpOrcaStdoutToFile(stdout)
            orcaRun.destroyForcibly()

        where:
            timeoutSeconds = 60
    }

    def "'build <empty>' should print help message"(int timeoutSeconds) {
        when:
            def orcaRun = orca.startOrca([:], 'build')

            orcaRun.waitFor(timeoutSeconds, TimeUnit.SECONDS)

        then:
            orcaRun.exitValue() != 0

            def stdout = orcaRun.inputStream.getText('utf-8')

            stdout.contains('Usage: scripts/build_services.sh <version>')

        cleanup:
            orcaRun.destroyForcibly()

        where:
            timeoutSeconds = 5
    }
}
package org.gradle.plugins.build

import groovy.json.JsonSlurper
import org.gradle.util.VersionNumber

class GradleVersionData {

    static List<String> getNightlyVersions() {
        def releaseNightly = getLatestReleaseNightly()
        releaseNightly ? [releaseNightly] + getLatestNightly() : [getLatestNightly()]
    }

    private static String getLatestNightly() {
        new JsonSlurper().parse(new URL("https://services.gradle.org/versions/nightly")).version
    }

    private static String getLatestReleaseNightly() {
        new JsonSlurper().parse(new URL("https://services.gradle.org/versions/release-nightly")).version
    }

    static List<String> getReleasedVersions() {
        new JsonSlurper().parse(new URL("https://services.gradle.org/versions/all"))
                .findAll { !it.nightly && !it.snapshot } // filter out snapshots and nightlies
                .findAll { !it.rcFor || it.activeRc } // filter out inactive rcs
                .findAll { !it.milestoneFor } // filter out milestones
                .collect { VersionNumber.parse(it.version as String) }
                .findAll { it.major >= 5 } // only 5.0 and above
                .inject([] as List<VersionNumber>) { releasesToTest, version -> // only test against latest patch versions
                    if (!releasesToTest.any { it.major == version.major && it.minor == version.minor }) {
                        return releasesToTest + version
                    } else {
                        return releasesToTest
                    }
                }
                .collect { it.toString() }
    }

}

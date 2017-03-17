
import com.beust.kobalt.plugin.packaging.assemble
import com.beust.kobalt.plugin.publish.bintray
import com.beust.kobalt.project

val p = project {
    name = "kobalt-retrolambda"
    artifactId = name
    group = "com.beust"
    version = "1.0.9"

    dependencies {
      compile("com.beust:kobalt-plugin-api:1.0.8")
    }

    assemble {
        mavenJars {
            fatJar = true
        }
    }

    bintray {
        publish = true
    }
}

import com.beust.kobalt.plugin.kotlin.kotlinProject
import com.beust.kobalt.plugin.packaging.assemble
import com.beust.kobalt.plugin.publish.jcenter

val p = kotlinProject {
    name = "kobalt-retrolambda"
    artifactId = name
    group = "com.beust"
    version = "0.2"

    dependencies {
        compile("com.beust:kobalt:")
    }

    assemble {
        mavenJars {
            fatJar = true
        }
    }

    jcenter {
    }
}

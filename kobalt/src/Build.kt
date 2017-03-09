
import com.beust.kobalt.plugin.packaging.assemble
import com.beust.kobalt.plugin.publish.bintray
import com.beust.kobalt.project
import com.beust.kobalt.repos

val bs = buildScript {
    val r = repos("https://dl.bintray.com/cbeust/maven")
    //val pl = plugins(file(homeDir("kotlin/kobalt-retrolambda/kobaltBuild/libs/kobalt-retrolambda-0.3.jar")))
}

val p = project {
    name = "kobalt-retrolambda"
    artifactId = name
    group = "com.beust"
    version = ".939"

    dependencies {
      compile("com.beust:kobalt-plugin-api:0.939")
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

rootProject.name = "gui-sample-viewer"

val sampleProjects = listOf("colorful-buttons", "colorful-buttons_with-sourceset-separation")
sampleProjects.forEach {
    include(it)
    project(":$it").projectDir = file("samples/$it")
}

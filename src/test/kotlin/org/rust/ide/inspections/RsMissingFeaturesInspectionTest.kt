/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.ide.inspections

import com.intellij.testFramework.InspectionTestUtil
import org.rust.FileTree
import org.rust.cargo.RsWithToolchainTestBase
import org.rust.cargo.project.model.cargoProjects
import org.rust.cargo.project.workspace.FeatureState
import org.rust.cargo.project.workspace.PackageFeature
import org.rust.fileTree

class RsMissingFeaturesInspectionTest : RsWithToolchainTestBase() {
    fun `test missing dependency feature`() = doTest(
        fileTree {
            toml("Cargo.toml", """
                [workspace]
                members = ["foo", "bar"]
            """)

            dir("foo") {
                toml("Cargo.toml", """
                    [package]
                    name = "foo"
                    version = "0.1.0"
                    authors = []

                    [dependencies]
                    bar = { path = "../bar", features = ["feature_bar"] }
                """)
                dir("src") {
                    file("main.rs", """
                        <warning descr="Missing features: bar/feature_bar">
                        fn main() {}
                        </warning>
                    """)
                }
            }

            dir("bar") {
                toml("Cargo.toml", """
                    [package]
                    name = "bar"
                    version = "0.1.0"
                    authors = []

                    [features]
                    feature_bar = [] # disabled
                """)
                dir("src") {
                    file("lib.rs", "")
                }
            }
        },
        pkgWithFeature = "bar",
        featureName = "feature_bar",
        fileToCheck = "foo/src/main.rs"
    )

    fun `test missing required target feature`() = doTest(
        fileTree {
            toml("Cargo.toml", """
                [package]
                name = "hello"
                version = "0.1.0"
                authors = []

                [[bin]]
                name = "main"
                path = "src/main.rs"
                required-features = ["feature_hello"]

                [features]
                feature_hello = []

                [dependencies]
            """)
            dir("src") {
                file("main.rs", """
                    <warning descr="Missing features: hello/feature_hello">
                    fn main() {}
                    </warning>
                """)
                file("lib.rs", "")
            }
        },
        pkgWithFeature = "hello",
        featureName = "feature_hello",
        fileToCheck = "src/main.rs"
    )

    private fun doTest(tree: FileTree, pkgWithFeature: String, featureName: String, fileToCheck: String) {
        tree.create()

        val cargoProject = project.cargoProjects.allProjects.singleOrNull() ?: error("Cargo project is not created")
        val workspace = cargoProject.workspace ?: error("Workspace is not created")
        val pkg = workspace.packages.find { it.name == pkgWithFeature } ?: error("Package $pkgWithFeature not found")

        project.cargoProjects.modifyFeatures(cargoProject, setOf(PackageFeature(pkg, featureName)), FeatureState.Disabled)

        val enabledInspections = InspectionTestUtil.instantiateTool(RsMissingFeaturesInspection::class.java)
        myFixture.enableInspections(enabledInspections)

        myFixture.openFileInEditor(cargoProjectDirectory.findFileByRelativePath(fileToCheck)!!)
        myFixture.checkHighlighting(
            /* checkWarnings = */ true,
            /* checkInfos = */ false,
            /* checkWeakWarnings = */ false
        )
    }
}

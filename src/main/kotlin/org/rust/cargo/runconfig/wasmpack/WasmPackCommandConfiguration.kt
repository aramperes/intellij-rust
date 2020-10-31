/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.cargo.runconfig.wasmpack

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.util.execution.ParametersListUtil
import org.rust.cargo.project.settings.toolchain
import org.rust.cargo.runconfig.RsCommandConfiguration
import org.rust.cargo.runconfig.ui.WasmPackCommandConfigurationEditor
import org.rust.cargo.toolchain.WasmPackCommandLine
import org.rust.cargo.toolchain.tools.wasmPack

class WasmPackCommandConfiguration(
    project: Project,
    name: String,
    factory: ConfigurationFactory
) : RsCommandConfiguration(project, name, factory) {
    override var command: String = "build"

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> =
        WasmPackCommandConfigurationEditor(project)

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState? {
        val wasmPack = environment.project.toolchain?.wasmPack() ?: return null
        val workingDirectory = workingDirectory?.toFile() ?: return null
        return WasmPackCommandRunState(environment, this, wasmPack, workingDirectory)
    }

    fun setFromCmd(cmd: WasmPackCommandLine) {
        command = ParametersListUtil.join(cmd.command, *cmd.additionalArguments.toTypedArray())
        workingDirectory = cmd.workingDirectory
    }
}

/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.wsl.sdk.add

import com.intellij.openapi.projectRoots.Sdk
import org.rust.ide.sdk.add.RsAddSdkPanel
import org.rust.ide.sdk.add.RsAddSdkProvider

class RsAddWslSdkProvider : RsAddSdkProvider {
    override fun createPanel(existingSdks: List<Sdk>): RsAddSdkPanel? = null
}

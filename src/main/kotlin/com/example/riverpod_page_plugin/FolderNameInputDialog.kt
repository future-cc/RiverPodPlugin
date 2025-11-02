package com.example.riverpod_page_plugin

import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.ValidationInfo
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
/**
 * 文件夹名字输入弹窗
 */
object FolderNameInputDialog {
    fun showTwoInputDialog(): Pair<String?, String?> {
        val panel = JPanel()
        panel.layout = java.awt.GridLayout(2, 2, 8, 8)

        val folderLabel = JLabel("Enter folder name (e.g. home):")
        val folderField = JTextField()

        val descLabel = JLabel("Enter description:")
        val descField = JTextField()

        panel.add(folderLabel)
        panel.add(folderField)
        panel.add(descLabel)
        panel.add(descField)

        val builder = DialogBuilder()
        builder.setTitle("Riverpod Page Generator")
        builder.centerPanel(panel)
        // ✅ 处理 OK 按钮点击事件
        builder.setOkOperation {
            val folderText = folderField.text.trim()
            if (folderText.isEmpty()) {
                // 使用内置提示框显示错误
                javax.swing.JOptionPane.showMessageDialog(
                    panel,
                    "Folder name cannot be empty!",
                    "Validation Error",
                    javax.swing.JOptionPane.WARNING_MESSAGE
                )
            } else {
                builder.dialogWrapper.close(0)
            }
        }

        val exitCode = builder.show()
        return if (exitCode == 0) {
            Pair(folderField.text.trim(), descField.text.trim())
        } else {
            Pair(null, null)
        }
    }

}